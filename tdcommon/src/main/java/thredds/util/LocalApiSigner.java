/*
 * Copyright (c) 2025 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package thredds.util;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.stream.Collectors;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;


/**
 * Signature based authentication for the TDS local API
 * <p/>
 * <p/>
 * Use an HMAC SHA256 algorithm to generate a hash based on components of
 * properties of the request and an ISO formatted date/time string (rounded
 * down to the nearest time boundary [five second window by default]) The
 * client and server must have access to the same key used to initialize the
 * hash generator.
 *
 * @since 5.7
 *
 */
public final class LocalApiSigner {
  // Header key used to identify the signature hash
  public static final String LOCAL_API_SIGNATURE_HEADER_V1 = "X-TDS-Local-Api-Signature-V1";

  private static final HexFormat HEX_FORMAT = HexFormat.of();
  private static final String SIGNING_ALGO = "HmacSHA256";
  private static final int DEFAULT_VALID_WINDOW_SIZE = 5; // seconds

  private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(LocalApiSigner.class);

  private final Mac mac;
  private final int validWindowSize; // in seconds

  private static class SigningWindowInSeconds {
    // date/time format used in signing, e.g. 2025-07-25T01:08:10Z
    private static final ZoneOffset ZONE_OFFSET = ZoneOffset.UTC;
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'hh:mm:ssv").withZone(ZONE_OFFSET);

    private final ZonedDateTime current, previous;

    private SigningWindowInSeconds(int windowSize) {
      // truncate to whole second
      ZonedDateTime now = Instant.now().truncatedTo(ChronoUnit.SECONDS).atZone(ZONE_OFFSET);
      int currentSeconds = now.getSecond();
      int windowIncrements = Math.floorDiv(currentSeconds, windowSize);
      ZonedDateTime base = now.minusSeconds(currentSeconds);
      // current start of X second window
      current = base.plusSeconds(windowIncrements * (long) windowSize);
      // previous start of X second window
      previous = current.minusSeconds(windowIncrements * (long) windowSize);
      logger.debug("current time {}", DATE_TIME_FORMATTER.format(now));
      logger.debug("current window {}", DATE_TIME_FORMATTER.format(current));
      logger.debug("previous window {}", DATE_TIME_FORMATTER.format(previous));
    }

    static SigningWindowInSeconds get(int windowSize) {
      return new SigningWindowInSeconds(windowSize);
    }

    String current() {
      return DATE_TIME_FORMATTER.format(current);
    }

    String previous() {
      return DATE_TIME_FORMATTER.format(previous);
    }
  }

  public LocalApiSigner(String key) {
    this(key, DEFAULT_VALID_WINDOW_SIZE);
  }

  LocalApiSigner(String key, int windowSize) {
    SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), SIGNING_ALGO);
    validWindowSize = windowSize; // seconds
    try {
      mac = Mac.getInstance(SIGNING_ALGO);
      mac.init(secretKeySpec);
    } catch (NoSuchAlgorithmException | InvalidKeyException e) {
      throw new RuntimeException(e);
    }
  }

  private String getPartialStringToSignGet(String url) {
    // signature is path plus query, but query needs to have params ordered
    // all decoded values (no percent encoded items)
    URI uri = URI.create(url);
    String stringToSign = uri.getPath();
    if (uri.getQuery() != null) {
      String orderedQuery = Arrays.stream(uri.getQuery().split("&")).sorted().collect(Collectors.joining("&"));
      stringToSign = String.format("%s?%s", stringToSign, orderedQuery);
    }
    return stringToSign;
  }

  private String getHexSig(String stringToSign) {
    return HEX_FORMAT.formatHex(mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8)));
  }

  /**
   * An HMAC SHA256 algorithm is used to generate a hash of the path and query
   * components of the request, along with the ISO formatted date/time (rounded
   * down to the nearest time boundary [five second window by default]).
   *
   * @param url string of the GET request url
   * @return signature hash
   */

  public String generateSignatureGet(String url) {
    String stringToSign = SigningWindowInSeconds.get(validWindowSize).current() + getPartialStringToSignGet(url);
    return getHexSig(stringToSign);
  }

  public boolean verifySignatureGet(String url, String expectedSignature) {
    String partialStringToSign = getPartialStringToSignGet(url);
    // prepare strings for signature verification based on the current and previous date/time stamps
    // in case the overhead of making/receiving a request passes the time window boundary.
    String stringToSignCurrent = SigningWindowInSeconds.get(validWindowSize).current() + partialStringToSign;
    String stringToSignPrevious = SigningWindowInSeconds.get(validWindowSize).previous() + partialStringToSign;
    logger.debug("expected signature: {}", expectedSignature);
    logger.debug("actual signature (current window): {}", getHexSig(stringToSignCurrent));
    logger.debug("actual signature (previous window): {}", getHexSig(stringToSignPrevious));
    return getHexSig(stringToSignCurrent).equals(expectedSignature)
        || getHexSig(stringToSignPrevious).equals(expectedSignature);
  }
}

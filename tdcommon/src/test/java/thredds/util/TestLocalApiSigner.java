/*
 * Copyright (c) 2025 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package thredds.util;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;

public class TestLocalApiSigner {

  LocalApiSigner localApiSigner = new LocalApiSigner("test-key");

  @Test
  public void testSamePath() {
    String url1 = "http://localhost:8080/my/path";
    String url2 = "http://localhost:8080/my/path";
    assertThat(localApiSigner.generateSignatureGet(url1)).isEqualTo(localApiSigner.generateSignatureGet(url2));
  }

  @Test
  public void testDiffPath() {
    String url1 = "http://localhost:8080/my/path";
    String url2 = "http://localhost:8080/my/other/path";
    assertThat(localApiSigner.generateSignatureGet(url1)).isNotEqualTo(localApiSigner.generateSignatureGet(url2));
  }

  @Test
  public void testSamePathOneQuery() {
    String url1 = "http://localhost:8080/my/path";
    String url2 = "http://localhost:8080/my/path?param1=value1";
    assertThat(localApiSigner.generateSignatureGet(url1)).isNotEqualTo(localApiSigner.generateSignatureGet(url2));
  }

  @Test
  public void testDiffPathSameQuery() {
    String url1 = "http://localhost:8080/my/path?param1=val1";
    String url2 = "http://localhost:8080/my/other/path?param1=val1";
    assertThat(localApiSigner.generateSignatureGet(url1)).isNotEqualTo(localApiSigner.generateSignatureGet(url2));
  }

  @Test
  public void testDiffPathDiffQuery() {
    String url1 = "http://localhost:8080/my/path?param1=val1";
    String url2 = "http://localhost:8080/my/other/path?param2=val2";
    assertThat(localApiSigner.generateSignatureGet(url1)).isNotEqualTo(localApiSigner.generateSignatureGet(url2));
  }

  @Test
  public void testParamOrder() {
    String url1 = "http://localhost:8080/my/path?param1=val1&param2=val2";
    String url2 = "http://localhost:8080/my/path?param2=val2&param1=val1";
    assertThat(localApiSigner.generateSignatureGet(url1)).isEqualTo(localApiSigner.generateSignatureGet(url2));
  }

  @Test
  public void testVerification() {
    LocalApiSigner localApiSigner = new LocalApiSigner("test-key");
    String url1 = "http://localhost:8080/my/path?param1=val1";
    String expectedSignature = localApiSigner.generateSignatureGet(url1);
    assertThat(localApiSigner.verifySignatureGet(url1, expectedSignature)).isTrue();
  }

  @Test
  public void testVerificationExpired() throws InterruptedException {
    final int timeoutMs = 1000; // milliseconds
    final LocalApiSigner localApiSigner = new LocalApiSigner("test-key", timeoutMs / 1000);
    final String url1 = "http://localhost:8080/my/path?param1=val1";
    final String expectedSignature = localApiSigner.generateSignatureGet(url1);
    Thread.sleep(timeoutMs + 1);
    assertThat(localApiSigner.verifySignatureGet(url1, expectedSignature)).isFalse();
  }
}

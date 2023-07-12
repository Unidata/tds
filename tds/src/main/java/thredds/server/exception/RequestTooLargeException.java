/*
 * Copyright (c) 1998-2018 John Caron and University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package thredds.server.exception;

public class RequestTooLargeException extends RuntimeException {

  private static final String MESSAGE_FORMAT = "Requested %d, max size = %d";

  public RequestTooLargeException(String message) {
    super(message);
  }

  public RequestTooLargeException(long requested, long maxBytes) {
    super(String.format(MESSAGE_FORMAT, requested, maxBytes));
  }

  public RequestTooLargeException(String message, Exception cause) {
    super(message, cause);
  }

}

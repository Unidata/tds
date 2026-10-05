/*
 * Copyright (c) 2026 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package thredds.server;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ucar.ma2.InvalidRangeException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TdsErrorHandlingTest {

  @Test
  public void testHandleInvalidRangeException() {
    TdsErrorHandling errorHandling = new TdsErrorHandling();
    InvalidRangeException ex = new InvalidRangeException("xaxis: value 0.071061 not in axis x;");

    ResponseEntity<String> response = errorHandling.handle(ex);
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().contains("Invalid Range: xaxis: value 0.071061 not in axis x;"));
  }
}

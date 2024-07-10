/*
 * Copyright (c) 1998-2018 John Caron and University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */
package thredds.servlet.restrict;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletException;
import java.io.IOException;

/**
 * No-op implementation of Authorizer. Always returns authorize=true.
 *
 * @author caron
 * @since Dec 28, 2009
 */


public class AuthorizerNoop implements Authorizer {

  @Override
  public void setRoleSource(RoleSource roleSource) {}

  @Override
  public boolean authorize(HttpServletRequest req, HttpServletResponse res, String role)
      throws IOException, ServletException {
    return true;
  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {}
}

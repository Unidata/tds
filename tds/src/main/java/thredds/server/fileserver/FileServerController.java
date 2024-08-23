/*
 * Copyright (c) 1998-2018 John Caron and University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package thredds.server.fileserver;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import thredds.core.TdsRequestedDataset;
import thredds.servlet.ServletUtil;
import thredds.util.TdsPathUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * HTTP File Serving
 *
 * handles /fileServer/*
 */
@Controller
@RequestMapping("/fileServer")
public class FileServerController {
  protected static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FileServerController.class);

  @RequestMapping("**")
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    String reqPath = TdsPathUtils.extractPath(req, "fileServer/");
    if (reqPath == null)
      return;

    if (!TdsRequestedDataset.resourceControlOk(req, res, reqPath)) { // LOOK or process in TdsRequestedDataset.getFile
                                                                     // ??
      return;
    }

    ServletUtil.writeMFileToResponse(req, res, reqPath);
  }

}

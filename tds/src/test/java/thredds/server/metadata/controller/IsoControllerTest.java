/*
 * Copyright (c) 2020-2025 University Corporation for Atmospheric Research/Unidata
 * See LICENSE.txt for license information.
 */

package thredds.server.metadata.controller;

import static com.google.common.truth.Truth.assertThat;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import org.junit.BeforeClass;
import org.junit.Test;
import thredds.client.catalog.Dataset;
import thredds.server.metadata.nciso.util.ThreddsTranslatorUtil;
import thredds.server.metadata.service.EnhancedMetadataService;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.NetcdfDatasets;

/**
 * Testing the IsoController
 */
public class IsoControllerTest {

  private static File xslFile;

  @BeforeClass
  public static void setXls() throws IOException {
    xslFile = File.createTempFile("TDS-IsoControllerTest-", null);
    xslFile.deleteOnExit();
    try (InputStream is = ThreddsTranslatorUtil.class.getResourceAsStream("/resources/xsl/nciso/UnidataDD2MI.xsl");
        OutputStream os = new FileOutputStream(xslFile);) {
      assertThat(is).isNotNull();
      is.transferTo(os);
    }
  }

  @Test
  public void testFakeIsoController() throws Exception {
    NetcdfDataset netCdfDataset =
        NetcdfDatasets.openDataset("src/test/resources/thredds/server/ncIso/extent/test.ncml");
    Writer writer = new StringWriter();
    // Get Thredds level metadata if it exists
    Dataset ids = null;

    // Enhance with file and dataset level metadata
    EnhancedMetadataService.enhance(netCdfDataset, ids, writer);

    String ncml = writer.toString();
    writer.flush();
    writer.close();
    InputStream is = new ByteArrayInputStream(ncml.getBytes("UTF-8"));
    Writer isoWriter = new StringWriter();
    ThreddsTranslatorUtil.transform(xslFile, is, isoWriter);
    is.close();
    String iso = isoWriter.toString();
    assertThat(iso).isNotEmpty();
  }

}

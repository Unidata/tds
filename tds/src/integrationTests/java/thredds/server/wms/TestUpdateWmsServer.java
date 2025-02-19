/*
 * Copyright (c) 1998-2022 University Corporation for Atmospheric Research/Unidata
 * See LICENSE.txt for license information.
 */

package thredds.server.wms;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import jakarta.servlet.http.HttpServletResponse;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.invoke.MethodHandles;
import thredds.test.util.TestOnLocalServer;
import thredds.util.ContentType;

import static com.google.common.truth.Truth.assertThat;

@Ignore("File locks on windows prevent files from being updated")
public class TestUpdateWmsServer {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final Namespace NS_WMS = Namespace.getNamespace("wms", "http://www.opengis.net/wms");

  private static final String DIR = "src/test/content/thredds/public/testdata/";
  private static final Path SRC_FILE_1 = Paths.get(DIR, "testGridAsPoint.nc");
  private static final Path SRC_FILE_2 = Paths.get(DIR, "testData.nc");
  private static final String FILENAME = "testUpdate.nc";

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder(new File(DIR));

  @Test
  public void testUpdateFile() throws IOException, JDOMException {
    final String filePath = temporaryFolder.getRoot().getName() + "/" + FILENAME;
    final String path = "/wms/localContent/" + filePath + "?service=WMS&version=1.3.0&request=GetCapabilities";
    final String endpoint = TestOnLocalServer.withHttpPath(path);

    // Check initial WMS output
    createTestFile(SRC_FILE_1, FILENAME);
    final byte[] result = TestOnLocalServer.getContent(endpoint, HttpServletResponse.SC_OK, ContentType.xmlwms);
    checkLayerNameInXml(result, "withT1Z1");

    // Update test file and check that WMS output is updated
    createTestFile(SRC_FILE_2, FILENAME);
    final byte[] updatedResult = TestOnLocalServer.getContent(endpoint, HttpServletResponse.SC_OK, ContentType.xmlwms);
    checkLayerNameInXml(updatedResult, "Z_sfc");
  }

  private void createTestFile(Path srcFile, String filename) throws IOException {
    Files.copy(srcFile, Paths.get(temporaryFolder.getRoot().toString(), filename), StandardCopyOption.REPLACE_EXISTING);
  }

  private void checkLayerNameInXml(byte[] result, String expectedLayerName) throws IOException, JDOMException {
    final Reader reader = new StringReader(new String(result, StandardCharsets.UTF_8));
    final SAXBuilder saxBuilder = new SAXBuilder();
    saxBuilder.setExpandEntities(false);
    final Document doc = saxBuilder.build(reader);

    final XPathExpression<Element> xpath = XPathFactory.instance()
        .compile("//wms:Capability/wms:Layer/wms:Layer/wms:Layer/wms:Name", Filters.element(), null, NS_WMS);
    final Element element = xpath.evaluateFirst(doc);
    assertThat(element.getTextTrim()).isEqualTo(expectedLayerName);
  }
}

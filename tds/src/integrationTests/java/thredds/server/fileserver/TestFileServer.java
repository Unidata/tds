/*
 * Copyright (c) 1998-2018 John Caron and University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */
package thredds.server.fileserver;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import javax.servlet.http.HttpServletResponse;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thredds.test.util.TestOnLocalServer;
import thredds.util.ContentType;
import ucar.httpservices.HTTPException;
import ucar.httpservices.HTTPFactory;
import ucar.httpservices.HTTPMethod;
import ucar.httpservices.HTTPSession;
import ucar.unidata.util.test.category.NeedsCdmUnitTest;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

/**
 * Describe
 *
 * @author caron
 * @since 3/8/2016.
 */
@RunWith(Parameterized.class)
@Category(NeedsCdmUnitTest.class)
public class TestFileServer {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Parameterized.Parameters(name = "{0}")
  public static List<Object[]> getTestParameters() {
    List<Object[]> result = new ArrayList<>();

    // TODO - Consider consolidating files and catalog.
    // Currently uses files from various locations and served through different catalog files.
    result.add(new Object[] {"fileServer/scanLocal/point.covjson.json", ContentType.json.toString(),
        "33fea472b71590b888e31870d745d71b"});
    result.add(new Object[] {"fileServer/rdaTest/ds094.2_dt/files/flxf01.gdas.A_PCP.SFC.01Z.grb2.gbx9",
        ContentType.binary.toString(), "f86072586ad8d66feed8cad2168f24a0"});
    result.add(new Object[] {"fileServer/testStationFeatureCollection/files/Surface_METAR_20060325_0000.nc",
        ContentType.netcdf.toString(), "4cc46253988cc9d1292f4fe4cd73f71f"});
    result.add(new Object[] {"fileServer/scanLocal/2004050312_eta_211.nc", ContentType.netcdf.toString(),
        "ed42786276bd33960e123941712d6ea4"});
    result.add(new Object[] {"fileServer/scanLocal/esfgTest.html", ContentType.html.toString(),
        "78e429e61fd0e605547ffcabd48178d0"});
    result.add(new Object[] {"fileServer/testNAMfmrc/files/20060925_0600.nc", ContentType.netcdf.toString(),
        "9bfc2f566b18851b02ee472f20be1acd"});

    result.add(new Object[] {"fileServer/s3-thredds-test-data/ncml/nc/namExtract/20060925_0600.nc",
        ContentType.netcdf.toString(), "9bfc2f566b18851b02ee472f20be1acd"});

    // make sure files don't get removed
    result.add(new Object[] {"fileServer/scanCdmUnitTests/formats/netcdf3/files/ctest0.nc",
        ContentType.netcdf.toString(), "4b514d280c034222e8e5b8401fee268c"});

    return result;
  }

  String path;
  String type;
  String md5;

  public TestFileServer(String path, String type, String md5) {
    this.path = path;
    this.type = type;
    this.md5 = md5;
  }

  @Test
  public void downloadFile() {
    String endpoint = TestOnLocalServer.withHttpPath(path);
    TestOnLocalServer.getContent(endpoint, 200, type);
  }

  @Test
  public void shouldHaveCorrectContent() throws IOException {
    final String endpoint = TestOnLocalServer.withHttpPath(path);
    final byte[] content = TestOnLocalServer.getContent(endpoint, 200);

    try (InputStream is = new ByteArrayInputStream(content)) {
      String observedMd5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(is);
      assertThat(observedMd5).isEqualTo(md5);
    }
  }

  @Test
  public void shouldReturnHead() throws HTTPException {
    final String endpoint = TestOnLocalServer.withHttpPath(path);

    try (HTTPSession session = HTTPFactory.newSession(endpoint)) {
      final HTTPMethod method = HTTPFactory.Head(session);

      final int status = method.execute();
      assertThat(status).isEqualTo(HttpServletResponse.SC_OK);

      Optional<String> header = method.getResponseHeaderValue(ContentType.HEADER);
      assertThat(header.isPresent()).isTrue();
      assertThat(header.get()).isEqualTo(type);

      assertThat(method.getResponseAsBytes()).isNull();
    }
  }
}

/*
 * Copyright (c) 2025 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package thredds.server.admin.collection;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import thredds.mock.web.MockTdsContextLoader;
import thredds.servlet.filter.LocalApiFilter;
import thredds.util.LocalApiSigner;
import ucar.unidata.util.test.category.NeedsCdmUnitTest;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {"/WEB-INF/applicationContext.xml", "/WEB-INF/spring-servlet.xml"},
    loader = MockTdsContextLoader.class)
@Category(NeedsCdmUnitTest.class)
public class TestLocalCollectionController {

  @Autowired
  private WebApplicationContext wac;

  private MockMvc mockMvc;

  private static LocalApiSigner localApiSigner;
  private static Path keyFile;

  @BeforeClass()
  public static void setupSigner() throws IOException {
    String key = "my-tmp-key";
    localApiSigner = new LocalApiSigner(key);
    // tmp file to hold key
    Path tmpDir = Files.createTempDirectory("tdm");
    tmpDir.toFile().deleteOnExit();
    keyFile = tmpDir.resolve("id");
    Files.writeString(keyFile, key, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
    System.setProperty("tds.local.api.key", keyFile.toAbsolutePath().toString());
  }

  @AfterClass
  public static void tearDown() {
    if (keyFile.toFile().exists()) {
      keyFile.toFile().delete();
    }
  }

  @Before
  public void setup() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(wac).addFilter(new LocalApiFilter()).build();
  }

  @Test
  public void checkLocalRequestUnsigned() throws Exception {
    String path = "/local/collection/showStatus";
    // make sure request has same address as local server
    RequestBuilder rb = MockMvcRequestBuilders.get(path).servletPath(path).with(request -> {
      request.setRemoteAddr(request.getLocalAddr());
      return request;
    });

    // not signed, so should fail
    this.mockMvc.perform(rb).andExpect(MockMvcResultMatchers.status().is(404)).andReturn();
  }

  @Test
  public void checkLocalRequestSigned() throws Exception {
    String path = "/local/collection/showStatus";
    // make sure request has same address as local server
    RequestBuilder rb = MockMvcRequestBuilders.get(path).servletPath(path).with(request -> {
      request.setRemoteAddr(request.getLocalAddr());
      // add signature header
      request.addHeader(LocalApiSigner.LOCAL_API_SIGNATURE_HEADER_V1, localApiSigner.generateSignatureGet(path));
      return request;
    });

    this.mockMvc.perform(rb).andExpect(MockMvcResultMatchers.status().is(200)).andReturn();
  }

  @Test
  public void checkNonLocalRequest() throws Exception {
    String path = "/local/collection/showStatus";

    // set request address as Quad9 DNS (could be anything that is NOT the local server address)
    RequestBuilder rb = MockMvcRequestBuilders.get(path).servletPath(path).with(request -> {
      request.setRemoteAddr("9.9.9.9");
      return request;
    });

    this.mockMvc.perform(rb).andExpect(MockMvcResultMatchers.status().is(404)).andReturn();
  }
}

/*
 * Copyright (c) 2024-2025 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package thredds.tds;

import static com.google.common.truth.Truth.assertThat;

import java.nio.charset.StandardCharsets;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.Test;
import thredds.test.util.TestOnLocalServer;

public class TestZarr {
  final private static String ZARR_DIR_PATH = "localContent/zarr/zarr_test_data.zarr";
  final private static String ZARR_ZIP_PATH = "localContent/zarr/zarr_test_data.zip";
  final private static String ZARR_S3_PATH = "s3-zarr/zarr_test_data.zarr/";
  final private static String ZARR_DIR_DATASET_SCAN_PATH = "scanLocalZarr/zarr_test_data.zarr/";
  final private static String ZARR_S3_DATASET_SCAN_PATH = "s3-dataset-scan-zarr/zarr_test_data.zarr/";

  @Test
  public void shouldOpenZarrDirectory() {
    checkWithOpendap(ZARR_DIR_PATH);
  }

  @Test
  public void shouldOpenZarrZip() {
    checkWithOpendap(ZARR_ZIP_PATH);
  }

  @Test
  public void shouldOpenObjectStoreZarrFile() {
    checkWithOpendap(ZARR_S3_PATH);
  }

  @Test
  public void shouldOpenDatasetScanZarr() {
    checkWithOpendap(ZARR_DIR_DATASET_SCAN_PATH);
    checkWithOpendap(ZARR_S3_DATASET_SCAN_PATH);
  }

  @Test
  public void shouldOpenZarrTwice() {
    // Test it works correctly with the netcdf file cache
    checkWithOpendap(ZARR_DIR_PATH);
    checkWithOpendap(ZARR_DIR_PATH);
  }

  @Test
  public void dirFileServerLocal() {
    // The HTTPServer service normally returns a status code 400 for requests for a directory
    // However, in the case of a Zarr dataset, we should return a 200 with a zero byte response
    checkWithHttpServer(ZARR_DIR_DATASET_SCAN_PATH);
  }

  @Test
  public void dirFileServerS3() {
    // The HTTPServer service normally returns a status code 400 for requests for a directory
    // However, in the case of a Zarr dataset, we should return a 200 with a zero byte response
    checkWithHttpServer(ZARR_S3_DATASET_SCAN_PATH);
  }

  private static void checkWithHttpServer(String path) {
    final String endpoint = TestOnLocalServer.withHttpPath("fileServer/" + path);
    TestOnLocalServer.getContent(endpoint, HttpServletResponse.SC_OK);
  }

  private static void checkWithOpendap(String path) {
    final String endpoint = TestOnLocalServer.withHttpPath("dodsC/" + path + ".dds");
    final byte[] content = TestOnLocalServer.getContent(endpoint, HttpServletResponse.SC_OK);
    final String stringContent = new String(content, StandardCharsets.UTF_8);

    assertThat(stringContent).contains("Int32 /group_with_attrs/F_order_array[dim0 = 20][dim1 = 20];");
  }
}

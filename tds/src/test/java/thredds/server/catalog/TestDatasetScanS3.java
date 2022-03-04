/*
 * Copyright (c) 1998-2023 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */
package thredds.server.catalog;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thredds.client.catalog.Catalog;
import thredds.client.catalog.Dataset;
import java.io.*;
import java.lang.invoke.MethodHandles;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

public class TestDatasetScanS3 {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final String CATALOG = "thredds/server/catalog/TestDatasetScanS3.xml";

  @Test
  public void shouldFindDatasetScanInCatalog() throws IOException {
    final ConfigCatalog catalog = TestConfigCatalogBuilder.getFromResource(CATALOG);
    assertThat(catalog).isNotNull();

    final Dataset dataset = catalog.findDatasetByID("testS3DatasetScan");
    assertThat(dataset).isNotNull();
    assertThat(dataset.hasProperty("DatasetScan")).isTrue();
    assertThat(dataset).isInstanceOf(DatasetScan.class);

    final DatasetScan datasetScan = (DatasetScan) dataset;
    assertThat(datasetScan.getServiceNameDefault()).isEqualTo("all");

    final DatasetScanConfig config = datasetScan.getConfig();
    assertThat(config.path).isEqualTo("s3-dataset-scan");
    assertThat(config.scanDir).isEqualTo("cdms3:thredds-test-data?test-dataset-scan/#delimiter=/");
  }

  @Test
  public void shouldMakeDatasetScan() throws IOException {
    final ConfigCatalog catalog = TestConfigCatalogBuilder.getFromResource(CATALOG);
    assertThat(catalog).isNotNull();

    final DatasetScan datasetScan = (DatasetScan) catalog.findDatasetByID("testS3DatasetScan");
    final Catalog scanCatalog =
        datasetScan.makeCatalogForDirectory("s3-dataset-scan/", catalog.getBaseURI()).makeCatalog();

    assertThat(scanCatalog.getDatasets().size()).isEqualTo(1);
    assertThat(scanCatalog.getDatasets().get(0).hasProperty("DatasetScan")).isTrue();
    final List<Dataset> datasets = scanCatalog.getDatasets().get(0).getDatasets();
    assertThat(datasets.size()).isEqualTo(5);

    // files in default sort order with directories at the end
    assertThat(datasets.get(0).getName()).isEqualTo("fultrak.hd5");
    assertThat(datasets.get(1).getName()).isEqualTo("testgrid1.nc");
    assertThat(datasets.get(2).getName()).isEqualTo("testgrid2.nc");
    assertThat(datasets.get(3).getName()).isEqualTo("sub-dir");
    assertThat(datasets.get(4).getName()).isEqualTo("sub-dir-2");
  }

  @Test
  public void shouldSort() throws IOException {
    final ConfigCatalog catalog = TestConfigCatalogBuilder.getFromResource(CATALOG);
    assertThat(catalog).isNotNull();

    final DatasetScan datasetScan = (DatasetScan) catalog.findDatasetByID("testS3DatasetScanSorted");
    final Catalog scanCatalog =
        datasetScan.makeCatalogForDirectory("s3-dataset-scan-sorted/", catalog.getBaseURI()).makeCatalog();

    assertThat(scanCatalog.getDatasets().size()).isEqualTo(1);
    final List<Dataset> datasets = scanCatalog.getDatasets().get(0).getDatasets();
    assertThat(datasets.size()).isEqualTo(5);

    // files get reverse sorted with directories at the end
    assertThat(datasets.get(0).getName()).isEqualTo("testgrid2.nc");
    assertThat(datasets.get(1).getName()).isEqualTo("testgrid1.nc");
    assertThat(datasets.get(2).getName()).isEqualTo("fultrak.hd5");
    assertThat(datasets.get(3).getName()).isEqualTo("sub-dir-2");
    assertThat(datasets.get(4).getName()).isEqualTo("sub-dir");
  }

  @Test
  public void shouldFilterFiles() throws IOException {
    final ConfigCatalog catalog = TestConfigCatalogBuilder.getFromResource(CATALOG);
    assertThat(catalog).isNotNull();

    final DatasetScan datasetScan = (DatasetScan) catalog.findDatasetByID("testS3DatasetScanFiltered");
    final Catalog scanCatalog =
        datasetScan.makeCatalogForDirectory("s3-dataset-scan-filtered/", catalog.getBaseURI()).makeCatalog();

    assertThat(scanCatalog.getDatasets().size()).isEqualTo(1);
    final List<Dataset> datasets = scanCatalog.getDatasets().get(0).getDatasets();
    assertThat(datasets.size()).isEqualTo(4);

    assertThat(datasets.get(0).getName()).isEqualTo("testgrid1.nc");
    assertThat(datasets.get(1).getName()).isEqualTo("testgrid2.nc");
    assertThat(datasets.get(2).getName()).isEqualTo("sub-dir");
    assertThat(datasets.get(3).getName()).isEqualTo("sub-dir-2");
  }

  @Test
  public void shouldFilterFolders() throws IOException {
    final ConfigCatalog catalog = TestConfigCatalogBuilder.getFromResource(CATALOG);
    assertThat(catalog).isNotNull();

    final DatasetScan datasetScan = (DatasetScan) catalog.findDatasetByID("testS3DatasetScanFolderFiltered");
    final Catalog scanCatalog =
        datasetScan.makeCatalogForDirectory("s3-dataset-scan-folder-filtered/", catalog.getBaseURI()).makeCatalog();

    assertThat(scanCatalog.getDatasets().size()).isEqualTo(1);
    final List<Dataset> datasets = scanCatalog.getDatasets().get(0).getDatasets();
    assertThat(datasets.size()).isEqualTo(4);

    assertThat(datasets.get(0).getName()).isEqualTo("fultrak.hd5");
    assertThat(datasets.get(1).getName()).isEqualTo("testgrid1.nc");
    assertThat(datasets.get(2).getName()).isEqualTo("testgrid2.nc");
    assertThat(datasets.get(3).getName()).isEqualTo("sub-dir");
  }

  @Test
  public void shouldFilterFilesAndFolders() throws IOException {
    final ConfigCatalog catalog = TestConfigCatalogBuilder.getFromResource(CATALOG);
    assertThat(catalog).isNotNull();

    final DatasetScan datasetScan = (DatasetScan) catalog.findDatasetByID("testS3DatasetScanFileFolderFiltered");
    final Catalog scanCatalog = datasetScan
        .makeCatalogForDirectory("s3-dataset-scan-file-folder-filtered/", catalog.getBaseURI()).makeCatalog();

    assertThat(scanCatalog.getDatasets().size()).isEqualTo(1);
    final List<Dataset> datasets = scanCatalog.getDatasets().get(0).getDatasets();
    assertThat(datasets.size()).isEqualTo(1);

    assertThat(datasets.get(0).getName()).isEqualTo("sub-dir-2");
  }

  @Test
  public void shouldScanSubDir() throws IOException {
    final ConfigCatalog catalog = TestConfigCatalogBuilder.getFromResource(CATALOG);
    assertThat(catalog).isNotNull();

    final DatasetScan datasetScan = (DatasetScan) catalog.findDatasetByID("testS3DatasetScan");
    final Catalog scanCatalog =
        datasetScan.makeCatalogForDirectory("s3-dataset-scan/sub-dir/", catalog.getBaseURI()).makeCatalog();

    assertThat(scanCatalog.getDatasets().size()).isEqualTo(1);
    final List<Dataset> datasets = scanCatalog.getDatasets().get(0).getDatasets();
    assertThat(datasets.size()).isEqualTo(1);
    assertThat(datasets.get(0).getName()).isEqualTo("1day.nc");
  }

  @Test
  public void shouldRefuseNonSubDirectory() throws IOException {
    final ConfigCatalog catalog = TestConfigCatalogBuilder.getFromResource(CATALOG);
    assertThat(catalog).isNotNull();

    final DatasetScan datasetScan = (DatasetScan) catalog.findDatasetByID("testS3DatasetScan");
    final FileNotFoundException exception = assertThrows(FileNotFoundException.class,
        () -> datasetScan.makeCatalogForDirectory("s3-dataset-scan/not-a-sub-dir/", catalog.getBaseURI()));
    assertThat(exception.getMessage()).contains("Directory does not exist");
  }

  @Test
  public void shouldRefuseNonDirectory() throws IOException {
    final ConfigCatalog catalog = TestConfigCatalogBuilder.getFromResource(CATALOG);
    assertThat(catalog).isNotNull();

    final DatasetScan datasetScan = (DatasetScan) catalog.findDatasetByID("testS3DatasetScanWithoutDelimiter");
    final FileNotFoundException exception = assertThrows(FileNotFoundException.class,
        () -> datasetScan.makeCatalogForDirectory("s3-dataset-scan-without-delimiter/", catalog.getBaseURI()));
    assertThat(exception.getMessage()).contains("Not a directory");
  }
}

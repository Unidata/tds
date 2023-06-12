package thredds.server.catalog.tracker;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import org.jdom2.Element;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thredds.client.catalog.Dataset;

public class TestDatasetTrackerChronicle {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Rule
  public final TemporaryFolder tempFolder = new TemporaryFolder();

  @Test
  public void shouldNotTrackDatasetWithoutNcmlAndRestrictions() throws IOException {
    try (DatasetTrackerChronicle datasetTracker =
        new DatasetTrackerChronicle(tempFolder.getRoot().getAbsolutePath(), 1, 1)) {
      assertThat(datasetTracker.getCount()).isEqualTo(0);

      final Dataset dataset = Dataset.makeStandalone("path", "featureType", "dataFormat", "serviceType");
      datasetTracker.trackDataset(1, dataset, null);
      assertThat(datasetTracker.getCount()).isEqualTo(0);
    }
  }

  @Test
  public void shouldTrackDatasetWithShortNcml() throws IOException {
    try (DatasetTrackerChronicle datasetTracker =
        new DatasetTrackerChronicle(tempFolder.getRoot().getAbsolutePath(), 1, 1)) {
      assertThat(datasetTracker.getCount()).isEqualTo(0);

      datasetTracker.trackDataset(1, mockDataset(100, "path"), null);
      assertThat(datasetTracker.getCount()).isEqualTo(1);
    }
  }

  @Test
  public void shouldTrackDatasetWithLongNcml() throws IOException {
    try (DatasetTrackerChronicle datasetTracker =
        new DatasetTrackerChronicle(tempFolder.getRoot().getAbsolutePath(), 1, 1)) {
      assertThat(datasetTracker.getCount()).isEqualTo(0);

      datasetTracker.trackDataset(1, mockDataset(1_000, "path"), null);
      assertThat(datasetTracker.getCount()).isEqualTo(1);
    }
  }

  @Test
  public void shouldTrackMultipleDatasetsWithNcml() throws IOException {
    try (DatasetTrackerChronicle datasetTracker =
        new DatasetTrackerChronicle(tempFolder.getRoot().getAbsolutePath(), 10, 1)) {
      assertThat(datasetTracker.getCount()).isEqualTo(0);

      datasetTracker.trackDataset(1, mockDataset(100, "path1"), null);
      datasetTracker.trackDataset(1, mockDataset(1_000, "path2"), null);
      datasetTracker.trackDataset(1, mockDataset(10_000, "path3"), null);
      assertThat(datasetTracker.getCount()).isEqualTo(3);
    }
  }

  private Dataset mockDataset(int ncmlLength, String path) {
    final Dataset dataset = mock(Dataset.class);
    final Element element = new Element("name", "namespace");
    element.setAttribute("attribute", "a".repeat(ncmlLength));
    when(dataset.getNcmlElement()).thenReturn(element);
    when(dataset.getUrlPath()).thenReturn(path);
    return dataset;
  }
}

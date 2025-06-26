/*
 * Copyright (c) 2023-2025 University Corporation for Atmospheric Research/Unidata
 * See LICENSE.txt for license information.
 */

package thredds.server.metadata.service;

import static com.google.common.truth.Truth.assertWithMessage;

import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Test;
import org.w3c.dom.Node;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;
import org.xmlunit.util.Predicate;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.NetcdfDatasets;

public class TestEnhancedMetadataService {

  @Test
  public void shouldReturnMetadata() throws Exception {
    final Path path = Paths.get("src/test/content/thredds/public/testdata/testgrid1.nc");
    final Writer writer = new StringWriter();

    try (NetcdfDataset netcdfDataset = NetcdfDatasets.openDataset(path.toAbsolutePath().toString())) {
      EnhancedMetadataService.enhance(netcdfDataset, null, writer);
    }

    // don't compare elements with current datetime or version
    final Predicate<Node> filter = node -> !(node.hasAttributes() && node.getAttributes().getNamedItem("name") != null
        && (node.getAttributes().getNamedItem("name").getNodeValue().equals("metadata_creation")
            || node.getAttributes().getNamedItem("name").getNodeValue().equals("nciso_version")));

    final Diff diff = DiffBuilder
        .compare(Input.fromStream(getClass().getResourceAsStream("/thredds/server/ncIso/testgrid1.ncml.xml")))
        .withTest(writer.toString()).withNodeFilter(filter).build();
    assertWithMessage(diff.toString()).that(diff.hasDifferences()).isFalse();
  }
}

/*
 * Copyright (c) 2011-2020 Applied Science Associates and University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package ucar.nc2.ft.ugrid;

import java.io.IOException;
import java.util.Formatter;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.dt.UGridDataset;
import ucar.nc2.dt.ugrid.Mesh;
import ucar.nc2.dt.ugrid.geom.LatLonPoint2D;
import ucar.nc2.dt.ugrid.geom.LatLonRectangle2D;
import ucar.nc2.ft.FeatureDatasetFactoryManager;
import ucar.nc2.util.CancelTask;
import ucar.unidata.geoloc.LatLonRect;

/**
 * Moved from ucar/netcdfjavaugrid/AppTest.java
 */
public class UGridDatasetStandardFactoryTest {
  final static String RESOURCE_PATH = "/cases/";

  // no real testing done in here yet. need to clean-up and actually test.
  // @Test
  public void testUgridDataset() {
    CancelTask cancelTask = null;

    String unstructured =
        UGridDatasetStandardFactoryTest.class.getResource(RESOURCE_PATH + "fvcom/fvcom_delt.ncml").getPath();

    try {
      UGridDataset ugrid = (UGridDataset) FeatureDatasetFactoryManager.open(FeatureType.UGRID, unstructured, cancelTask,
          new Formatter());
      long startTime;
      long endTime;
      for (UGridDataset.Meshset ms : ugrid.getMeshsets()) {
        Mesh m = ms.getMesh();
        System.out.println(m.toString());
        // We build now, to see how grids compare in index time
        // System.out.println("Building RTree...");
        startTime = System.currentTimeMillis();
        m.buildRTree();
        endTime = System.currentTimeMillis();
        // System.out.println("RTree build took: " + (double) (endTime - startTime) / 1000 + " seconds.");
        // System.out.println("RTree contains: " + m.getTreeSize() + " entries.");

        // Skip the point extraction
        /*
         * if (m.getTreeSize() > 0) {
         * // Query a random point within the bounding box of the Mesh
         * LatLonRect bounds = m.getLatLonBoundingBox();
         * double query_lat = bounds.getLatMin() + (Math.random() * (bounds.getLatMax() - bounds.getLatMin()));
         * double query_lon = bounds.getLonMin() + (Math.random() * (bounds.getLonMax() - bounds.getLonMin()));
         * LatLonPoint query_point = new LatLonPointImpl(query_lat, query_lon);
         * System.out.println("Random query point: " + query_lat + "," + query_lon);
         *
         * // Get the Cell that the point is in
         * Cell cell = m.getCellFromLatLon(query_lat, query_lon);
         * System.out.println("Cell containing point located.");
         *
         * List<LatLonPoint2D> vertices = cell.getPolygon().getVertices();
         * System.out.println("Cell vertices (" + vertices.size() + "):");
         * for (LatLonPoint2D p : vertices) {
         * System.out.println(p.getLatitude() + "," + p.getLongitude());
         * }
         * System.out.println("Cell center: " + cell.getPolygon().getCentroid().getLatitude() + "," +
         * cell.getPolygon().getCentroid().getLongitude());
         *
         * // The Cell contains many Entities, which are points which data
         * // can lay on. Can test if the Entity is on the boundry.
         * System.out.println("Data locations within the Cell:");
         * for (Entity e : cell.getEntities()) {
         * System.out.println(e.getClass().getSimpleName() + ": " + e.getGeoPoint().getLatitude() + "," +
         * e.getGeoPoint().getLongitude());
         * }
         * for (UGridDatatype v : ms.getMeshVariables()) {
         * v = (MeshVariable) v;
         * // Extract value for each variable at the query point
         * System.out.print(v.getName() + ": ");
         * System.out.print(v.readPointData(query_point));
         * System.out.println();
         * System.out.println(m.extractDataPoints(v).size());
         * }
         * }
         */

        /*
         * This is what we are subsetting
         *
         * ul-------------
         * |xxxxxx| |
         * |xxxxxx| |
         * ---------------
         * | | |
         * | | |
         * --------------lr
         */


        /* Skip variable level subsetting */
        /*
         * if (m.getSize() > 0) {
         *
         * LatLonRect bounds = m.getLatLonBoundingBox();
         * LatLonRectangle2D rect = new LatLonRectangle2D(bounds.getUpperLeftPoint().getLatitude(),
         * bounds.getUpperLeftPoint().getLongitude(), bounds.getLowerRightPoint().getLatitude(),
         * bounds.getLowerRightPoint().getLongitude());
         * LatLonRectangle2D rect2 = new LatLonRectangle2D(new
         * LatLonPoint2D.Double(rect.getUpperLeftPoint().getLatitude(), rect.getUpperLeftPoint().getLongitude()), new
         * LatLonPoint2D.Double(rect.getCenterLatitude(), rect.getCenterLongitude()));
         * LatLonRect h = rect2.toLatLonRect();
         * System.out.println("Subsetting the top left corner of bounding box...");
         *
         * // Subset the first variable into a new UGridDataset (all in memory)
         * MeshVariable var = (MeshVariable)ms.getMeshVariables().get(0);
         * System.out.println("Subsetting: " + var.getName());
         * UGridDataset ug2 = var.subsetToSelf(h);
         * //assert (ug2.getMeshVariables().size() > 0);
         * Mesh m2 = ug2.getMeshVariableByName(var.getName()).getMeshset().getMesh();
         * m2.buildRTree();
         * System.out.println(m2.toString());
         * }
         */
      }

      // Subset the entire UGridDataset

      System.out.println("Subsetting the entire UGridDataset bounding box...");
      Mesh m3 = ugrid.getMeshsets().get(0).getMesh();
      m3.buildRTree();
      LatLonRect bounds = m3.getLatLonBoundingBox();
      LatLonRectangle2D rect =
          new LatLonRectangle2D(bounds.getUpperLeftPoint().getLatitude(), bounds.getUpperLeftPoint().getLongitude(),
              bounds.getLowerRightPoint().getLatitude(), bounds.getLowerRightPoint().getLongitude());
      LatLonRectangle2D rect2 = new LatLonRectangle2D(
          new LatLonPoint2D.Double(rect.getUpperLeftPoint().getLatitude(), rect.getUpperLeftPoint().getLongitude()),
          new LatLonPoint2D.Double(rect.getCenterLatitude(), rect.getCenterLongitude()));
      LatLonRect h = rect2.toLatLonRect();
      UGridDataset ug3 = ugrid.subset(h);
      Mesh m4;
      for (UGridDataset.Meshset ms3 : ug3.getMeshsets()) {
        m4 = ms3.getMesh();
        m4.buildRTree();
        System.out.println(m4.toString());
      }
      System.out.println("Done");

    } catch (IOException e) {
      System.out.println(e);
    }
  }
}

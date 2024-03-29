/*
 * Copyright (c) 1998-2018 John Caron and University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */
package thredds.server.wcs.v1_0_0_1;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;
import thredds.server.wcs.Request;
import ucar.nc2.ft2.coverage.*;
import ucar.nc2.time.CalendarDateRange;
import ucar.unidata.geoloc.LatLonPoint;
import ucar.unidata.geoloc.LatLonRect;
import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class DescribeCoverage extends WcsRequest {

  private List<String> coverages;

  private Document describeCoverageDoc;

  public DescribeCoverage(Request.Operation operation, String version, WcsDataset dataset,
      @Nonnull List<String> coverages) throws WcsException {
    super(operation, version, dataset);

    this.coverages = coverages;
    if (this.coverages.size() < 1)
      throw new IllegalArgumentException("Coverage list must contain at least one ID [" + this.coverages.size() + "].");
    List<String> badCovIds = new ArrayList<>();
    for (String curCov : coverages) {
      if (!this.getWcsDataset().isAvailableCoverageName(curCov))
        badCovIds.add(curCov);
    }
    if (badCovIds.size() > 0) {
      throw new WcsException("Coverage ID list contains one or more unknown IDs [" + badCovIds + "].");
    }
  }

  public Document getDescribeCoverageDoc() {
    if (this.describeCoverageDoc == null)
      describeCoverageDoc = generateDescribeCoverageDoc();
    return describeCoverageDoc;
  }

  public void writeDescribeCoverageDoc(PrintWriter pw) throws IOException {
    XMLOutputter xmlOutputter = new XMLOutputter(org.jdom2.output.Format.getPrettyFormat());
    xmlOutputter.output(getDescribeCoverageDoc(), pw);
  }

  public String writeDescribeCoverageDocAsString() throws IOException {
    XMLOutputter xmlOutputter = new XMLOutputter(org.jdom2.output.Format.getPrettyFormat());
    return xmlOutputter.outputString(getDescribeCoverageDoc());
  }

  Document generateDescribeCoverageDoc() {
    // CoverageDescription (wcs) [1]
    Element coverageDescriptionsElem = new Element("CoverageDescription", wcsNS);
    coverageDescriptionsElem.addNamespaceDeclaration(gmlNS);
    coverageDescriptionsElem.addNamespaceDeclaration(xlinkNS);
    coverageDescriptionsElem.setAttribute("version", this.getVersion());
    // ToDo Consider dealing with "updateSequence"
    // coverageDescriptionsElem.setAttribute( "updateSequence", this.getCurrentUpdateSequence() );

    for (String curCoverageId : this.coverages)
      coverageDescriptionsElem.addContent(genCoverageOfferingElem(curCoverageId));

    return new Document(coverageDescriptionsElem);
  }

  public Element genCoverageOfferingElem(String covId) {
    WcsCoverage coverage = this.getWcsDataset().getAvailableCoverage(covId);
    CoverageCoordSys gridCoordSystem = coverage.getCoordinateSystem();

    // CoverageDescription/CoverageOffering (wcs) [1..*]
    Element covDescripElem = genCoverageOfferingBriefElem("CoverageOffering", covId, coverage.getLabel(),
        coverage.getDescription(), gridCoordSystem);

    // CoverageDescription/CoverageOffering/domainSet [1]
    covDescripElem.addContent(genDomainSetElem(coverage));

    // CoverageDescription/CoverageOffering/rangeSet [1]
    covDescripElem.addContent(genRangeSetElem(coverage));

    // CoverageDescription/CoverageOffering/supportedCRSs [1]
    covDescripElem.addContent(genSupportedCRSsElem(coverage));

    // CoverageDescription/CoverageOffering/supportedFormats [1]
    covDescripElem.addContent(genSupportedFormatsElem(coverage));

    // CoverageDescription/CoverageOffering/supportedInterpolations [0..1]
    covDescripElem.addContent(genSupportedInterpolationsElem());

    return covDescripElem;
  }

  private Element genDomainSetElem(WcsCoverage coverage) {
    // ../domainSet
    Element domainSetElem = new Element("domainSet", wcsNS);

    // ../domainSet/spatialDomain [0..1] AND/OR temporalDomain [0..1]
    domainSetElem.addContent(genSpatialDomainElem(coverage));
    CoverageCoordAxis1D timeCoord = (CoverageCoordAxis1D) coverage.getCoordinateSystem().getTimeAxis();
    if (timeCoord != null) {
      domainSetElem.addContent(genTemporalDomainElem(timeCoord));
    }

    return domainSetElem;
  }

  private Element genSpatialDomainElem(WcsCoverage coverage) {
    // ../domainSet/spatialDomain
    Element spatialDomainElem = new Element("spatialDomain", wcsNS);

    // ../domainSet/spatialDomain/gml:Envelope [1..*]
    spatialDomainElem.addContent(this.genEnvelopeElem(coverage.getCoordinateSystem()));

    // ../domainSet/spatialDomain/gml:RectifiedGrid [0..*]
    spatialDomainElem.addContent(this.genRectifiedGridElem(coverage));

    // ../domainSet/spatialDomain/gml:Polygon [0..*]

    return spatialDomainElem;
  }

  private Element genRectifiedGridElem(WcsCoverage coverage) {
    // ../spatialDomain/gml:RectifiedGrid
    Element rectifiedGridElem = new Element("RectifiedGrid", gmlNS);

    CoverageCoordAxis1D xaxis = (CoverageCoordAxis1D) coverage.getCoordinateSystem().getXAxis();
    CoverageCoordAxis1D yaxis = (CoverageCoordAxis1D) coverage.getCoordinateSystem().getYAxis();
    CoverageCoordAxis1D zaxis = (CoverageCoordAxis1D) coverage.getCoordinateSystem().getZAxis();

    // ../spatialDomain/gml:RectifiedGrid@srsName [0..1] (URI)
    rectifiedGridElem.setAttribute("srsName", coverage.getNativeCrs());

    // ../spatialDomain/gml:RectifiedGrid@dimension [1] (positive integer)
    int ndim = (zaxis != null) ? 3 : 2;
    rectifiedGridElem.setAttribute("dimension", Integer.toString(ndim));

    // ../spatialDomain/gml:RectifiedGrid/gml:limits [1]
    int[] minValues = new int[ndim];
    int[] maxValues = new int[ndim];

    maxValues[0] = (xaxis.getNcoords() - 1);
    maxValues[1] = (yaxis.getNcoords() - 1);
    if (zaxis != null)
      maxValues[2] = (zaxis.getNcoords() - 1);

    Element limitsElem = new Element("limits", gmlNS);

    // ../spatialDomain/gml:RectifiedGrid/gml:limits/gml:GridEnvelope [1]
    // ../spatialDomain/gml:RectifiedGrid/gml:limits/gml:GridEnvelope/gml:low [1] (integer list)
    // ../spatialDomain/gml:RectifiedGrid/gml:limits/gml:GridEnvelope/gml:high [1] (integer list)
    limitsElem.addContent(new Element("GridEnvelope", gmlNS)
        .addContent(new Element("low", gmlNS).addContent(genIntegerListString(minValues)))
        .addContent(new Element("high", gmlNS).addContent(genIntegerListString(maxValues))));

    rectifiedGridElem.addContent(limitsElem);

    // ../spatialDomain/gml:RectifiedGrid/gml:axisName [1..*] (string)
    rectifiedGridElem.addContent(new Element("axisName", gmlNS).addContent("x"));
    rectifiedGridElem.addContent(new Element("axisName", gmlNS).addContent("y"));
    if (zaxis != null)
      rectifiedGridElem.addContent(new Element("axisName", gmlNS).addContent("z"));

    // ../spatialDomain/gml:RectifiedGrid/gml:origin [1]
    // ../spatialDomain/gml:RectifiedGrid/gml:origin/gml:pos [1] (space separated list of double values)
    // ../spatialDomain/gml:RectifiedGrid/gml:origin/gml:pos@dimension [0..1] (number of entries in list)
    double[] origin = new double[ndim];
    origin[0] = xaxis.getCoordMidpoint(0);
    origin[1] = yaxis.getCoordMidpoint(0);
    if (zaxis != null)
      origin[2] = zaxis.getCoordMidpoint(0);

    rectifiedGridElem.addContent(
        new Element("origin", gmlNS).addContent(new Element("pos", gmlNS).addContent(genDoubleListString(origin))));

    // ../spatialDomain/gml:RectifiedGrid/gml:offsetVector [1..*] (space separated list of double values)
    // ../spatialDomain/gml:RectifiedGrid/gml:offsetVector@dimension [0..1] (number of entries in list)
    double[] xoffset = new double[ndim];
    xoffset[0] = xaxis.getResolution();
    rectifiedGridElem.addContent(new Element("offsetVector", gmlNS).addContent(genDoubleListString(xoffset)));

    double[] yoffset = new double[ndim];
    yoffset[1] = yaxis.getResolution();
    rectifiedGridElem.addContent(new Element("offsetVector", gmlNS).addContent(genDoubleListString(yoffset)));

    if (zaxis != null) {
      double[] zoffset = new double[ndim];
      zoffset[2] = zaxis.getResolution();
      rectifiedGridElem.addContent(new Element("offsetVector", gmlNS).addContent(genDoubleListString(zoffset)));
    }

    return rectifiedGridElem;
  }

  private String genIntegerListString(int[] values) {
    StringBuilder buf = new StringBuilder();
    for (int intValue : values) {
      if (buf.length() > 0)
        buf.append(" ");
      buf.append(intValue);
    }
    return buf.toString();
  }

  private String genDoubleListString(double[] values) {
    StringBuilder buf = new StringBuilder();
    for (double doubleValue : values) {
      if (buf.length() > 0)
        buf.append(" ");
      buf.append(doubleValue);
    }
    return buf.toString();
  }

  private Element genEnvelopeElem(CoverageCoordSys gcs) {
    // spatialDomain/Envelope
    Element envelopeElem;
    CoverageCoordAxis timeCoord = gcs.getTimeAxis();
    if (timeCoord != null)
      envelopeElem = new Element("EnvelopeWithTimePeriod", wcsNS);
    else
      envelopeElem = new Element("Envelope", wcsNS);

    // spatialDomain/Envelope@srsName [0..1] (URI)
    envelopeElem.setAttribute("srsName", "urn:ogc:def:crs:OGC:1.3:CRS84");

    LatLonRect llbb = wcsDataset.getDataset().getLatlonBoundingBox();
    LatLonPoint llpt = llbb.getLowerLeftPoint();
    LatLonPoint urpt = llbb.getUpperRightPoint();

    double lon = llpt.getLongitude() + llbb.getWidth();
    int posDim = 2;
    String firstPosition = llpt.getLongitude() + " " + llpt.getLatitude();
    String secondPosition = lon + " " + urpt.getLatitude();
    String posDimString = Integer.toString(posDim);

    // spatialDomain/Envelope/gml:pos [2] (space separated list of double values)
    // spatialDomain/Envelope/gml:pos@dimension [0..1] (number of entries in list)
    envelopeElem.addContent(
        new Element("pos", gmlNS).addContent(firstPosition).setAttribute(new Attribute("dimension", posDimString)));
    envelopeElem.addContent(
        new Element("pos", gmlNS).addContent(secondPosition).setAttribute(new Attribute("dimension", posDimString)));

    // spatialDomain/Envelope/gml:timePostion [2]
    if (timeCoord != null) {
      CalendarDateRange dateRange = timeCoord.getDateRange();
      envelopeElem.addContent(new Element("timePosition", gmlNS).addContent(dateRange.getStart().toString()));
      envelopeElem.addContent(new Element("timePosition", gmlNS).addContent(dateRange.getEnd().toString()));
    }

    return envelopeElem;
  }

  private Element genTemporalDomainElem(CoverageCoordAxis1D timeAxis) {
    Element temporalDomainElem = new Element("temporalDomain", wcsNS);
    // temporalDomain/timePosition [1..*]
    for (int i = 0; i < timeAxis.getNcoords(); i++) {
      double val = timeAxis.getCoordMidpoint(i);
      temporalDomainElem.addContent(new Element("timePosition", gmlNS).addContent(timeAxis.makeDate(val).toString()));
    }

    return temporalDomainElem;
  }

  private Element genRangeSetElem(WcsCoverage coverage) {
    WcsRangeField rangeField = coverage.getRangeField();
    // rangeSet
    Element rangeSetElem = new Element("rangeSet", wcsNS);

    // rangeSet/RangeSet
    // rangeSet/RangeSet@semantic
    // rangeSet/RangeSet@refSys
    // rangeSet/RangeSet@refSysLabel
    Element innerRangeSetElem = new Element("RangeSet", wcsNS);

    // rangeSet/RangeSet/description [0..1]
    if (rangeField.getDescription() != null)
      innerRangeSetElem.addContent(new Element("description").addContent(rangeField.getDescription()));

    // rangeSet/RangeSet/name [1]

    innerRangeSetElem.addContent(new Element("name", wcsNS).addContent(rangeField.getName()));

    // rangeSet/RangeSet/label [1]
    innerRangeSetElem.addContent(new Element("label", wcsNS).addContent(rangeField.getLabel()));

    WcsRangeField.Axis vertAxis = rangeField.getAxis();
    if (vertAxis != null) {
      // rangeSet/RangeSet/axisDescription [0..*]
      Element axisDescElem = new Element("axisDescription", wcsNS);

      // rangeSet/RangeSet/axisDescription/AxisDescription [1]
      Element innerAxisDescElem = new Element("AxisDescription", wcsNS);

      // rangeSet/RangeSet/axisDescription/AxisDescription/name [1]
      // rangeSet/RangeSet/axisDescription/AxisDescription/label [1]
      innerAxisDescElem.addContent(new Element("name", wcsNS).addContent(vertAxis.getName()));
      innerAxisDescElem.addContent(new Element("label", wcsNS).addContent(vertAxis.getLabel()));

      // rangeSet/RangeSet/axisDescription/AxisDescription/values [1]
      Element valuesElem = new Element("values", wcsNS);

      // rangeSet/RangeSet/axisDescription/AxisDescription/values/singleValue [1..*]
      // ----- interval is alternate for singleValue
      // rangeSet/RangeSet/axisDescription/AxisDescription/values/interval
      // rangeSet/RangeSet/axisDescription/AxisDescription/values/interval/min [0..1]
      // rangeSet/RangeSet/axisDescription/AxisDescription/values/interval/max [0..1]
      // rangeSet/RangeSet/axisDescription/AxisDescription/values/interval/res [0..1]
      // -----
      for (String curVal : vertAxis.getValues())
        valuesElem.addContent(new Element("singleValue", wcsNS).addContent(curVal));

      // rangeSet/RangeSet/axisDescription/AxisDescription/values/default [0..1]

      innerAxisDescElem.addContent(valuesElem);
      axisDescElem.addContent(innerAxisDescElem);
      innerRangeSetElem.addContent(axisDescElem);
    }


    // rangeSet/RangeSet/nullValues [0..1]
    // rangeSet/RangeSet/nullValues/{interval|singleValue} [1..*]
    if (coverage.hasMissingData()) {
      innerRangeSetElem.addContent(new Element("nullValues", wcsNS).addContent(new Element("singleValue", wcsNS)
          // ToDo Is missing always NaN?
          .addContent("NaN")));
    }

    return rangeSetElem.addContent(innerRangeSetElem);
  }

  private Element genSupportedCRSsElem(WcsCoverage coverage) {
    // supportedCRSs
    Element supportedCRSsElem = new Element("supportedCRSs", wcsNS);

    // supportedCRSs/requestCRSs [1..*] (wcs) (space separated list of strings)
    // supportedCRSs/requestCRSs@codeSpace [0..1] (URI)
    supportedCRSsElem.addContent(new Element("requestCRSs", wcsNS).addContent(coverage.getDefaultRequestCrs()));

    // supportedCRSs/responseCRSs [1..*] (wcs) (space separated list of strings)
    // supportedCRSs/responseCRSs@codeSpace [0..1] (URI)
    supportedCRSsElem.addContent(new Element("responseCRSs", wcsNS).addContent(coverage.getNativeCrs()));

    return supportedCRSsElem;
  }

  private Element genSupportedFormatsElem(WcsCoverage coverage) {
    // supportedFormats
    // supportedFormats@nativeFormat [0..1] (string)
    Element supportedFormatsElem = new Element("supportedFormats", wcsNS);

    // supportedFormats/formats [1..*] (wcs) (space separated list of strings)
    // supportedFormats/formats@codeSpace [0..1] (URI)
    for (Request.Format curFormat : coverage.getSupportedCoverageFormatList()) {
      supportedFormatsElem.addContent(new Element("formats", wcsNS).addContent(curFormat.toString()));
    }

    return supportedFormatsElem;
  }

  private Element genSupportedInterpolationsElem() {
    // supportedInterpolations
    // supportedInterpolations@default [0..1] ???
    Element supportedInterpolationsElem = new Element("supportedInterpolations", wcsNS);

    // supportedInterpolations/interpolationMethod [1..*]
    supportedInterpolationsElem.addContent(new Element("interpolationMethod", wcsNS).addContent("none"));

    return supportedInterpolationsElem;
  }
}

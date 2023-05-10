/*
 * Copyright (c) 1998 - 2014. University Corporation for Atmospheric Research/Unidata
 * Portions of this software were developed by the Unidata Program at the
 * University Corporation for Atmospheric Research.
 *
 * Access and use of this software shall impose the following obligations
 * and understandings on the user. The user is granted the right, without
 * any fee or cost, to use, copy, modify, alter, enhance and distribute
 * this software, and any derivative works thereof, and its supporting
 * documentation for any purpose whatsoever, provided that this entire
 * notice appears in all copies of the software, derivative works and
 * supporting documentation. Further, UCAR requests that the user credit
 * UCAR/Unidata in any publications that result from the use of this
 * software or in any product that includes this software. The names UCAR
 * and/or Unidata, however, may not be used in any advertising or publicity
 * to endorse or promote any products or commercial entity unless specific
 * written permission is obtained from UCAR/Unidata. The user also
 * understands that UCAR/Unidata is not obligated to provide the user with
 * any support, consulting, training or assistance of any kind with regard
 * to the use, operation and performance of this software nor to provide
 * the user with any updates, revisions, new versions or "bug fixes."
 *
 * THIS SOFTWARE IS PROVIDED BY UCAR/UNIDATA "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL UCAR/UNIDATA BE LIABLE FOR ANY SPECIAL,
 * INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING
 * FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT,
 * NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION
 * WITH THE ACCESS, USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package thredds.server.catalog.builder;

import org.jdom2.Attribute;
import org.jdom2.DataConversionException;
import org.jdom2.Element;
import thredds.client.catalog.Catalog;
import thredds.inventory.MFile;
import thredds.inventory.MFiles;
import ucar.nc2.util.AliasTranslator;
import thredds.server.catalog.DatasetScanConfig;
import ucar.unidata.util.StringUtil2;
import java.io.File;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Optional;

/**
 * Builder of DatasetScanConfig
 *
 * @author John
 * @since 1/12/2015
 */
public class DatasetScanConfigBuilder {
  Formatter errlog;
  boolean fatalError;

  public DatasetScanConfigBuilder(Formatter errlog) {
    this.errlog = errlog;
  }

  public boolean hasFatalError() {
    return fatalError;
  }

  /*
   * <xsd:element name="datasetScan" substitutionGroup="dataset">
   * <xsd:complexType>
   * <xsd:complexContent>
   * <xsd:extension base="DatasetType">
   * <xsd:sequence>
   * <xsd:element ref="filter" minOccurs="0" maxOccurs="1"/>
   * <xsd:element ref="namer" minOccurs="0" maxOccurs="1"/>
   * <xsd:element ref="sort" minOccurs="0" maxOccurs="1"/>
   * <xsd:element ref="addLatest" minOccurs="0" maxOccurs="1"/>
   * <xsd:element ref="addProxies" minOccurs="0" maxOccurs="1"/>
   * <xsd:element name="addDatasetSize" minOccurs="0" maxOccurs="1"/>
   * <xsd:element ref="addTimeCoverage" minOccurs="0" maxOccurs="1"/>
   * </xsd:sequence>
   * 
   * <xsd:attribute name="path" type="xsd:string" use="required"/>
   * <xsd:attribute name="location" type="xsd:string"/>
   * <xsd:attribute name="addLatest" type="xsd:boolean"/>
   * <xsd:attribute name="filter" type="xsd:string"/> <!-- deprecated : use filter element -->
   * </xsd:extension>
   * </xsd:complexContent>
   * </xsd:complexType>
   * </xsd:element>
   */
  public DatasetScanConfig readDatasetScanConfig(Element dsElem) {
    DatasetScanConfig result = new DatasetScanConfig();

    result.name = dsElem.getAttributeValue("name");
    result.path = StringUtil2.trim(dsElem.getAttributeValue("path"), '/');
    if (result.path == null) {
      errlog.format("ERROR: must specify path attribute.%n");
      fatalError = true;
    }

    String scanDir = dsElem.getAttributeValue("location");
    if (scanDir == null) {
      errlog.format("ERROR: must specify directory root in location attribute.%n");
      fatalError = true;
    } else {
      result.scanDir = AliasTranslator.translateAlias(scanDir);
      MFile scanFile = MFiles.create(result.scanDir);
      if (!scanFile.exists()) {
        errlog.format("ERROR: directory %s does not exist%n", result.scanDir);
        fatalError = true;
      }
    }

    result.restrictAccess = dsElem.getAttributeValue("restrictAccess");

    // look for ncml
    Element ncmlElem = dsElem.getChild("netcdf", Catalog.defNS);
    if (ncmlElem != null) {
      ncmlElem.detach();
      result.ncmlElement = ncmlElem;
    }

    // Read filter element
    Element filterElem = dsElem.getChild("filter", Catalog.defNS);
    result.filters = readDatasetScanFilter(filterElem);

    // Read namer element
    Element namerElem = dsElem.getChild("namer", Catalog.defNS);
    result.namers = readDatasetScanNamer(namerElem);

    // Read filesSort or sort element
    Element filesSortElem = dsElem.getChild("filesSort", Catalog.defNS);
    if (filesSortElem != null)
      result.isSortIncreasing = readFilesSort(filesSortElem);
    Element sorterElem = dsElem.getChild("sort", Catalog.defNS);
    if (!result.isSortIncreasing.isPresent() && sorterElem != null)
      result.isSortIncreasing = readSort(sorterElem);

    // Deal with latest
    String addLatestAttribute = dsElem.getAttributeValue("addLatest");
    Element addLatestElem = dsElem.getChild("addLatest", Catalog.defNS); // not in docs
    Element addProxiesElem = dsElem.getChild("addProxies", Catalog.defNS);
    result.addLatest = readDatasetScanAddProxies(addProxiesElem, addLatestElem, addLatestAttribute);

    /*
     * Read addDatasetSize element.
     * Element addDsSizeElem = dsElem.getChild("addDatasetSize", Catalog.defNS);
     * if (addDsSizeElem != null) { // docs: default true
     * if (addDsSizeElem.getTextNormalize().equalsIgnoreCase("false"))
     * result.addDatasetSize = false;
     * }
     */

    // Read addTimeCoverage element.
    Element addTimeCovElem = dsElem.getChild("addTimeCoverage", Catalog.defNS);
    if (addTimeCovElem != null) {
      result.addTimeCoverage = readDatasetScanAddTimeCoverage(addTimeCovElem);
    }

    return result;
  }

  /*
   * <xsd:element name="filter">
   * <xsd:complexType>
   * <xsd:choice>
   * <xsd:sequence minOccurs="0" maxOccurs="unbounded">
   * <xsd:element name="include" type="FilterSelectorType" minOccurs="0"/>
   * <xsd:element name="exclude" type="FilterSelectorType" minOccurs="0"/>
   * </xsd:sequence>
   * </xsd:choice>
   * </xsd:complexType>
   * </xsd:element>
   * 
   * <xsd:complexType name="FilterSelectorType">
   * <xsd:attribute name="regExp" type="xsd:string"/>
   * <xsd:attribute name="wildcard" type="xsd:string"/>
   * <xsd:attribute name="atomic" type="xsd:boolean"/>
   * <xsd:attribute name="collection" type="xsd:boolean"/>
   * </xsd:complexType>
   */
  private List<DatasetScanConfig.Filter> readDatasetScanFilter(Element filterElem) {
    List<DatasetScanConfig.Filter> filters = new ArrayList<>();
    if (filterElem == null)
      return null;

    for (Element curElem : filterElem.getChildren()) {
      String regExpAttVal = curElem.getAttributeValue("regExp");
      String wildcardAttVal = curElem.getAttributeValue("wildcard");
      String lastModLimitAttValS = curElem.getAttributeValue("lastModLimitInMillis");
      if (regExpAttVal == null && wildcardAttVal == null && lastModLimitAttValS == null) {
        // If no regExp or wildcard attributes, skip this selector.
        errlog.format(
            "WARN: readDatasetScanFilter(): no regExp, wildcard, or lastModLimitInMillis attribute in filter child <%s>%n",
            curElem.getName());

      } else {
        // Determine if applies to atomic datasets, default true.
        String atomicAttVal = curElem.getAttributeValue("atomic");
        boolean atomic = (atomicAttVal == null || !atomicAttVal.equalsIgnoreCase("false"));

        // Determine if applies to collection datasets, default false.
        String collectionAttVal = curElem.getAttributeValue("collection");
        boolean notCollection = collectionAttVal == null || !collectionAttVal.equalsIgnoreCase("true");

        // Determine if include or exclude selectors.
        boolean includer = true;
        if (curElem.getName().equals("exclude")) {
          includer = false;
        } else if (!curElem.getName().equals("include")) {
          errlog.format("WARN: readDatasetScanFilter(): unhandled filter child <%s>.%n", curElem.getName());
          continue;
        }

        // check for errors
        long lastModLimitAttVal = -1;
        if (lastModLimitAttValS != null) {
          try {
            lastModLimitAttVal = Long.parseLong(lastModLimitAttValS);
          } catch (NumberFormatException e) {
            errlog.format("WARN: readDatasetScanFilter(): lastModLimitInMillis not valid <%s>.%n", curElem);
          }
        }

        filters.add(new DatasetScanConfig.Filter(regExpAttVal, wildcardAttVal, lastModLimitAttVal, atomic,
            !notCollection, includer));
      }
    }

    return filters;
  }

  /*
   * <xsd:element name="namer">
   * <xsd:complexType>
   * <xsd:choice maxOccurs="unbounded">
   * <xsd:element name="regExpOnName" type="NamerSelectorType"/>
   * <xsd:element name="regExpOnPath" type="NamerSelectorType"/>
   * </xsd:choice>
   * </xsd:complexType>
   * </xsd:element>
   * 
   * <xsd:complexType name="NamerSelectorType">
   * <xsd:attribute name="regExp" type="xsd:string"/>
   * <xsd:attribute name="replaceString" type="xsd:string"/>
   * </xsd:complexType>
   */

  protected List<DatasetScanConfig.Namer> readDatasetScanNamer(Element namerElem) {
    List<DatasetScanConfig.Namer> result = new ArrayList<>();
    if (namerElem == null)
      return result;

    for (Element curElem : namerElem.getChildren()) {
      String regExp = curElem.getAttributeValue("regExp");
      String replaceString = curElem.getAttributeValue("replaceString");

      boolean onName = curElem.getName().equals("regExpOnName");
      boolean onPath = curElem.getName().equals("regExpOnPath");
      if (!onName && !onPath) {
        errlog.format("WARN: readDatasetScanNamer(): namer child '%s'%n", curElem.getName());
        continue;
      }
      result.add(new DatasetScanConfig.Namer(onName, regExp, replaceString));
    }
    return result;
  }

  /*
   * <xsd:element name="sort">
   * <xsd:complexType>
   * <xsd:choice>
   * <xsd:element name="lexigraphicByName">
   * <xsd:complexType>
   * <xsd:attribute name="increasing" type="xsd:boolean"/>
   * </xsd:complexType>
   * </xsd:element>
   * <xsd:element name="crawlableDatasetSorterImpl" minOccurs="0" type="UserImplType"/>
   * </xsd:choice>
   * </xsd:complexType>
   * </xsd:element>
   */


  protected Optional<Boolean> readFilesSort(Element sorterElem) {
    String increasingString = sorterElem.getAttributeValue("increasing");
    if (increasingString != null) {
      if (increasingString.equalsIgnoreCase("true"))
        return Optional.of(true);
      else if (increasingString.equalsIgnoreCase("false"))
        return Optional.of(false);
    }
    return Optional.empty();
  }

  protected Optional<Boolean> readSort(Element sorterElem) {
    Element lexSortElem = sorterElem.getChild("lexigraphicByName", Catalog.defNS);
    if (lexSortElem != null) {
      String increasingString = lexSortElem.getAttributeValue("increasing");
      if (increasingString != null) {
        if (increasingString.equalsIgnoreCase("true"))
          return Optional.of(true);
        else if (increasingString.equalsIgnoreCase("false"))
          return Optional.of(false);
      }
    }
    return Optional.empty();
  }

  protected DatasetScanConfig.AddLatest readDatasetScanAddProxies(Element addProxiesElem, Element addLatestElem,
      String addLatestAttribute) {

    // handle "addLatest attribute
    if (addLatestAttribute != null && addLatestAttribute.equalsIgnoreCase("true")) {
      return new DatasetScanConfig.AddLatest(); // use defaults
    }

    // Handle "addLatest" elements.
    if (addLatestElem != null) {
      return readDatasetScanAddLatest(addLatestElem);
    }

    // Handle old "addProxies" elements.
    if (addProxiesElem != null) {
      for (Element curChildElem : addProxiesElem.getChildren()) {

        // Handle "simpleLatest" child elements.
        if (curChildElem.getName().equals("simpleLatest")) {
          return readDatasetScanAddLatest(curChildElem);
        }

        if (curChildElem.getName().equals("latestComplete")) {
          return readDatasetScanAddLatest(curChildElem);
        }
      }
    }

    return null;
  }

  /*
   * <xsd:complexType name="addLatestType">
   * <xsd:attribute name="name" type="xsd:string"/>
   * <xsd:attribute name="top" type="xsd:boolean"/>
   * <xsd:attribute name="serviceName" type="xsd:string"/>
   * <xsd:attribute name="lastModifiedLimit" type="xsd:float"/> <!-- minutes -->
   * </xsd:complexType>
   */


  private DatasetScanConfig.AddLatest readDatasetScanAddLatest(Element addLatestElem) {

    String latestName = "latest.xml";
    String serviceName = "Resolver";
    boolean latestOnTop = true;
    boolean isResolver = true;

    String tmpLatestName = addLatestElem.getAttributeValue("name");
    if (tmpLatestName != null)
      latestName = tmpLatestName;

    String tmpserviceName = addLatestElem.getAttributeValue("serviceName");
    if (tmpserviceName != null)
      serviceName = tmpserviceName;

    // Does latest go on top or bottom of list.
    Attribute topAtt = addLatestElem.getAttribute("top");
    if (topAtt != null) {
      try {
        latestOnTop = topAtt.getBooleanValue();
      } catch (DataConversionException e) {
        latestOnTop = true;
      }
    }

    // Get lastModified limit.
    String lastModLimitVal = addLatestElem.getAttributeValue("lastModifiedLimit");
    long lastModLimit = -1;
    if (lastModLimitVal != null)
      lastModLimit = Long.parseLong(lastModLimitVal) * 60 * 1000; // convert minutes to millisecs

    return new DatasetScanConfig.AddLatest(latestName, serviceName, latestOnTop, lastModLimit);
  }

  /*
   * <xsd:element name="addTimeCoverage">
   * <xsd:complexType>
   * <xsd:attribute name="datasetNameMatchPattern" type="xsd:string"/>
   * <xsd:attribute name="datasetPathMatchPattern" type="xsd:string"/>
   * <xsd:attribute name="startTimeSubstitutionPattern" type="xsd:string"/>
   * <xsd:attribute name="duration" type="xsd:string"/>
   * </xsd:complexType>
   * </xsd:element>
   */

  protected DatasetScanConfig.AddTimeCoverage readDatasetScanAddTimeCoverage(Element addTimeCovElem) {
    String matchName = addTimeCovElem.getAttributeValue("datasetNameMatchPattern");
    String matchPath = addTimeCovElem.getAttributeValue("datasetPathMatchPattern");
    String subst = addTimeCovElem.getAttributeValue("startTimeSubstitutionPattern");
    String duration = addTimeCovElem.getAttributeValue("duration");

    boolean err = false;
    if (subst == null) {
      errlog.format("WARN: readDatasetScanAddTimeCoverage(): must have startTimeSubstitutionPattern elem=<%s>%n",
          addTimeCovElem);
      err = true;
    } else if (duration == null) {
      errlog.format("WARN: readDatasetScanAddTimeCoverage(): must have duration elem=<%s>%n", addTimeCovElem);
      err = true;
    } else if (matchName == null && matchPath == null) {
      errlog.format(
          "WARN: readDatasetScanAddTimeCoverage(): must have either datasetNameMatchPattern or datasetPathMatchPattern elem=<%s>%n",
          addTimeCovElem);
      err = true;
    }

    return err ? null : new DatasetScanConfig.AddTimeCoverage(matchName, matchPath, subst, duration);
  }

}

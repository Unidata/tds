/*
 * Copyright (c) 2026 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

dependencyResolutionManagement {
  repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
  repositories {
    // be very specific about where non-maven-central-based artifacts are sourced
    exclusiveContent {
      forRepository {
        maven {
          name = "unidata-3rdparty"
          url = uri("https://artifacts.unidata.ucar.edu/repository/unidata-3rdparty/")
        }
      }
      filter { includeModule("org.bounce", "bounce") }
    }

    // use unidata-all so that excluseiveContent can be used
    // (as of 9.3.0, cannot have overlapping filters even when repositories are
    // configured for release or snapshot only)
    exclusiveContent {
      forRepository {
        maven {
          name = "unidata-all"
          url = uri("https://artifacts.unidata.ucar.edu/repository/unidata-all/")
        }
      }
      filter {
        // direct
        includeModule("EDS", "nciso-common")
        includeModule("edu.ucar", "bufr")
        includeModule("edu.ucar", "cdm-core")
        includeModule("edu.ucar", "cdm-image")
        includeModule("edu.ucar", "cdm-mcidas")
        includeModule("edu.ucar", "cdm-misc")
        includeModule("edu.ucar", "cdm-radial")
        includeModule("edu.ucar", "cdm-s3")
        includeModule("edu.ucar", "cdm-zarr")
        includeModule("edu.ucar", "cdm-test-utils")
        includeModule("edu.ucar", "dap4")
        includeModule("edu.ucar", "grib")
        includeModule("edu.ucar", "httpservices")
        includeModule("edu.ucar", "netcdf4")
        includeModule("edu.ucar", "netcdf-java-bom")
        includeModule("edu.ucar", "opendap")
        includeModule("edu.ucar", "uicdm")
        includeModule("edu.ucar", "waterml")

        includeModule("uk.ac.rdg.resc", "edal-cdm")
        includeModule("uk.ac.rdg.resc", "edal-common")
        includeModule("uk.ac.rdg.resc", "edal-graphics")
        includeModule("uk.ac.rdg.resc", "edal-godiva")
        includeModule("uk.ac.rdg.resc", "edal-wms")

        // transitive
        includeModule("EDS", "threddsIso-parent")
        includeModule("edu.ucar", "jj2000")
        includeModule("edu.ucar", "libaec-jna")
        includeModule("edu.ucar", "libblosc2-jna")
        includeModule("edu.ucar", "netcdf-java-platform")
        includeModule("edu.ucar", "udunits")
        includeModule("edu.ucar", "uibase")
        includeModule("edu.ucar.unidata", "libaec-native")
        includeModule("edu.ucar.unidata", "libblosc2-native")
        includeModule("edu.wisc.ssec", "visad-mcidas-slim-ucar-ns")
        includeModule("uk.ac.rdg.resc", "edal")
        includeModule("uk.ac.rdg.resc", "edal-coveragejson")

        // transitive from nciso...problematic
        includeModule("edu.ucar", "netcdf-java-testing-platform")
        includeModule("edu.ucar", "tds-plugin-bom")
        includeModule("edu.ucar", "tds-platform")
      }
    }
    mavenCentral()
  }
  versionCatalogs { create("tdsLibs") { from(files("gradle/tds.libs.versions.toml")) } }
}

includeBuild("build-logic-tds")

rootProject.name = "thredds-data-server"

//
// no subproject dependencies
//
include(":d4servlet")

project(":d4servlet").projectDir = file("dap4/d4servlet")

include(":docs")

include(":tdcommon")

include(":tds-ugrid")

// depends on :d4servlet
include(":d4ts")

project(":d4ts").projectDir = file("dap4/d4ts")

// depends on tdcommon
include(":tds-test-utils")

include(":tdm")

// depends on tds-test-utils
include(":opendap-servlet")

project(":opendap-servlet").projectDir = file("opendap/server")

// depends on opendap-servlet
include(":dtswar")

project(":dtswar").projectDir = file("opendap/dtswar")

// depends on tds-ugrid

include(":tds-ui")

// the TDS

include(":tds")

//
// platforms used by all
//
include("tds-platform")

include("tds-testing-platform")

include(
  "tds-plugin-bom"
)

// To debug netCDF-Java, uncomment the two includeBuild calls
// below to create a composite build, making sure to point them
// to your local copy of the netCDF-Java repository

// includeBuild("../netcdf-java/build-logic-ncj")
// includeBuild("../netcdf-java")

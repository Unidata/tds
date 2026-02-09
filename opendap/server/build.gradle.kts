/*
 * Copyright (c) 2026 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

plugins { id("tds-java-library-conventions") }

description = "OPeNDAP DAP2 servlet used by the THREDDS Data Server (TDS)."

extra["project.title"] = "OPeNDAP DAP2 servlet code"

dependencies {
  implementation(platform(project(":tds-platform")))

  implementation(tdsLibs.jdom2)
  implementation(tdsLibs.slf4j.api)
  implementation(tdsLibs.ucar.cdmCore)
  implementation(tdsLibs.ucar.opendap)

  compileOnly(tdsLibs.jakarta.servletApi)

  testImplementation(platform(project(":tds-testing-platform")))

  testImplementation(project(":tds-test-utils"))

  testImplementation(tdsLibs.google.truth)
  testImplementation(tdsLibs.ucar.cdmTestUtils)

  testCompileOnly(tdsLibs.junit4)

  testRuntimeOnly(tdsLibs.junit5.platformLauncher)
  testRuntimeOnly(tdsLibs.junit5.vintageEngine)
}

/*
 * Copyright (c) 2026 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

plugins { id("tds-java-library-conventions") }

description = "OPeNDAP DAP4 servlet used by the THREDDS Data Server (TDS)."

extra["project.title"] = "OPeNDAP DAP4 servlet code"

dependencies {
  implementation(platform(project(":tds-platform")))

  implementation(tdsLibs.commons.lang3)
  implementation(tdsLibs.slf4j.api)
  implementation(tdsLibs.ucar.cdmCore)
  implementation(tdsLibs.ucar.dap4)
  implementation(tdsLibs.ucar.httpservices)

  compileOnly(tdsLibs.jakarta.servletApi)

  testImplementation(platform(project(":tds-testing-platform")))

  testImplementation(tdsLibs.google.truth)
  testImplementation(tdsLibs.jakarta.servletApi)
  testImplementation(tdsLibs.mockito.core)
  testImplementation(tdsLibs.ucar.cdmTestUtils)

  testCompileOnly(tdsLibs.junit4)

  testRuntimeOnly(tdsLibs.junit5.platformLauncher)
  testRuntimeOnly(tdsLibs.junit5.vintageEngine)
}

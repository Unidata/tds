/*
 * Copyright (c) 2026 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

plugins { id("java-library-conventions") }

description = "OPeNDAP DAP4 servlet used by the THREDDS Data Server (TDS)."

extra["project.title"] = "OPeNDAP DAP4 servlet code"

dependencies {
  implementation(platform(project(":tds-platform")))

  implementation(libs.commons.lang3)
  implementation(libs.slf4j.api)
  implementation(libs.ucar.cdmCore)
  implementation(libs.ucar.dap4)
  implementation(libs.ucar.httpservices)

  compileOnly(libs.jakarta.servletApi)

  testImplementation(platform(project(":tds-testing-platform")))

  testImplementation(libs.google.truth)
  testImplementation(libs.jakarta.servletApi)
  testImplementation(libs.mockito.core)
  testImplementation(libs.ucar.cdmTestUtils)

  testCompileOnly(libs.junit4)

  testRuntimeOnly(libs.junit5.platformLauncher)
  testRuntimeOnly(libs.junit5.vintageEngine)
}

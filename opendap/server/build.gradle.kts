/*
 * Copyright (c) 2026 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

plugins { id("java-library-conventions") }

description = "OPeNDAP DAP2 servlet used by the THREDDS Data Server (TDS)."

extra["project.title"] = "OPeNDAP DAP2 servlet code"

dependencies {
  implementation(platform(project(":tds-platform")))

  implementation(libs.jdom2)
  implementation(libs.slf4j.api)
  implementation(libs.ucar.cdmCore)
  implementation(libs.ucar.opendap)

  compileOnly(libs.jakarta.servletApi)

  testImplementation(platform(project(":tds-testing-platform")))

  testImplementation(project(":tds-test-utils"))

  testImplementation(libs.google.truth)
  testImplementation(libs.ucar.cdmTestUtils)

  testCompileOnly(libs.junit4)

  testRuntimeOnly(libs.junit5.platformLauncher)
  testRuntimeOnly(libs.junit5.vintageEngine)
}

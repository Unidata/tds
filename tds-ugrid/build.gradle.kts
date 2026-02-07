/*
 * Copyright (c) 2026 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

plugins { id("java-library-conventions") }

description = "Experimental UGRID support for netCDF-java."

extra["project.title"] = "TDS UGRID Support"

dependencies {
  implementation(platform(project(":tds-platform")))

  implementation(libs.colt)
  implementation(libs.findbugs.jsr305)
  implementation(libs.guava)
  implementation(libs.slf4j.api)
  implementation(libs.ucar.cdmCore)
  implementation(libs.ucar.cdmS3)

  testImplementation(platform(project(":tds-testing-platform")))

  testImplementation(project(":tds-test-utils"))

  testImplementation(libs.google.truth)
  testImplementation(libs.ucar.cdmTestUtils)

  testCompileOnly(libs.junit4)

  testRuntimeOnly(libs.junit5.platformLauncher)
  testRuntimeOnly(libs.junit5.vintageEngine)
}

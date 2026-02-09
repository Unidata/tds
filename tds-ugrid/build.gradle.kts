/*
 * Copyright (c) 2026 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

plugins { id("tds-java-library-conventions") }

description = "Experimental UGRID support for netCDF-java."

extra["project.title"] = "TDS UGRID Support"

dependencies {
  implementation(platform(project(":tds-platform")))

  implementation(tdsLibs.colt)
  implementation(tdsLibs.findbugs.jsr305)
  implementation(tdsLibs.guava)
  implementation(tdsLibs.slf4j.api)
  implementation(tdsLibs.ucar.cdmCore)
  implementation(tdsLibs.ucar.cdmS3)

  testImplementation(platform(project(":tds-testing-platform")))

  testImplementation(project(":tds-test-utils"))

  testImplementation(tdsLibs.google.truth)
  testImplementation(tdsLibs.ucar.cdmTestUtils)

  testCompileOnly(tdsLibs.junit4)

  testRuntimeOnly(tdsLibs.junit5.platformLauncher)
  testRuntimeOnly(tdsLibs.junit5.vintageEngine)
}

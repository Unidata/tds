/*
 * Copyright (c) 2026 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

plugins { id("java-base-conventions") }

dependencies {
  implementation(platform(project(":tds-platform")))
  implementation(platform(project(":tds-testing-platform")))

  implementation(project(":tdcommon"))

  implementation(libs.slf4j.api)
  implementation(libs.ucar.cdmCore)
  implementation(libs.ucar.cdmTestUtils)
  implementation(libs.ucar.httpservices)

  compileOnly(libs.junit4)
}

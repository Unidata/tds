/*
 * Copyright (c) 2026 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

plugins { id("tds-java-base-conventions") }

dependencies {
  implementation(platform(project(":tds-platform")))
  implementation(platform(project(":tds-testing-platform")))

  implementation(project(":tdcommon"))

  implementation(tdsLibs.slf4j.api)
  implementation(tdsLibs.ucar.cdmCore)
  implementation(tdsLibs.ucar.cdmTestUtils)
  implementation(tdsLibs.ucar.httpservices)

  compileOnly(tdsLibs.junit4)
}

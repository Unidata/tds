/*
 * Copyright (c) 2026 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

plugins {
  id("java-base-conventions")
  war
}

description = "OPeNDAP DAP2 Test Server"

dependencies {
  implementation(platform(project(":tds-platform")))

  implementation(project(":opendap-servlet"))

  implementation(libs.findbugs.jsr305)
  implementation(libs.jdom2)
  implementation(libs.slf4j.api)
  implementation(libs.ucar.cdmCore)
  implementation(libs.ucar.opendap)

  compileOnly(libs.jakarta.servletApi)

  runtimeOnly(libs.glassfish.jstl)
  runtimeOnly(libs.jakarta.jstlApi)
}

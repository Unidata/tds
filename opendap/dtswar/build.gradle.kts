/*
 * Copyright (c) 2026 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

plugins {
  id("tds-java-base-conventions")
  war
}

description = "OPeNDAP DAP2 Test Server"

dependencies {
  implementation(platform(project(":tds-platform")))

  implementation(project(":opendap-servlet"))

  implementation(tdsLibs.findbugs.jsr305)
  implementation(tdsLibs.jdom2)
  implementation(tdsLibs.slf4j.api)
  implementation(tdsLibs.ucar.cdmCore)
  implementation(tdsLibs.ucar.opendap)

  compileOnly(tdsLibs.jakarta.servletApi)

  runtimeOnly(tdsLibs.glassfish.jstl)
  runtimeOnly(tdsLibs.jakarta.jstlApi)
}

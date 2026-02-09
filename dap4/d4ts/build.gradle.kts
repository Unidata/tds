/*
 * Copyright (c) 2026 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

plugins {
  id("tds-java-base-conventions")
  war
}

description = "OPeNDAP DAP4 Test Server"

dependencies {
  implementation(platform(project(":tds-platform")))

  implementation(project(":d4servlet"))

  implementation(tdsLibs.slf4j.api)
  implementation(tdsLibs.ucar.cdmCore)
  implementation(tdsLibs.ucar.dap4)

  compileOnly(tdsLibs.jakarta.servletApi)

  runtimeOnly(tdsLibs.log4j.slf4j2Impl)
  runtimeOnly(tdsLibs.log4j.jakartaWeb)
}

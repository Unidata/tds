/*
 * Copyright (c) 2026 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

plugins {
  id("java-base-conventions")
  war
}

description = "OPeNDAP DAP4 Test Server"

dependencies {
  implementation(platform(project(":tds-platform")))

  implementation(project(":d4servlet"))

  implementation(libs.slf4j.api)
  implementation(libs.ucar.cdmCore)
  implementation(libs.ucar.dap4)

  compileOnly(libs.jakarta.servletApi)

  runtimeOnly(libs.log4j.slf4j2Impl)
  runtimeOnly(libs.log4j.jakartaWeb)
}

/*
 * Copyright (c) 2026 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

plugins {
  id("java-base-conventions")
  application
}

dependencies {
  implementation(platform(project(":tds-platform")))

  runtimeOnly(project(":tds-ugrid"))

  runtimeOnly(libs.ucar.uicdm)
}

application { mainClass = "ucar.nc2.ui.ToolsUI" }

tasks.register("runTdsMonitor", JavaExec::class) {
  group = "application"
  dependsOn("classes")
  mainClass = "thredds.ui.monitor.TdsMonitor"
  classpath = sourceSets.main.get().runtimeClasspath
}

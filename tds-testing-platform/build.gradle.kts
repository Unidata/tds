/*
 * Copyright (c) 2026 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

plugins { id("platform-conventions") }

description =
  "Platform containing the test-only dependencies of the public artifacts that comprise the THREDDS Data Server."

extra["project.title"] = "TDS test-only 3rd party libraries"

// allow references to other BOMs
javaPlatform.allowDependencies()

dependencies {
  api(platform(libs.junit5.bom))

  constraints {
    api(libs.beust.jcommander)
    api(libs.commons.io)
    api(libs.google.truth)
    api(libs.hamcrest.core)
    api(libs.jaxen)
    api(libs.junit4)
    api(libs.logback.classic)
    api(libs.mockito.core)
    api(libs.pragmatists.junitparams)
    api(libs.springframework.beans)
    api(libs.springframework.context)
    api(libs.springframework.core)
    api(libs.springframework.springTest)
    api(libs.ucar.cdmTestUtils)
    api(libs.xmlunit.core)

    runtime(libs.junit5.platformLauncher)
    runtime(libs.junit5.vintageEngine)
    runtime(libs.logback.classic)
  }
}

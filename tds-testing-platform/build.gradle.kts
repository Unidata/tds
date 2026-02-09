/*
 * Copyright (c) 2026 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

plugins { id("tds-platform-conventions") }

description =
  "Platform containing the test-only dependencies of the public artifacts that comprise the THREDDS Data Server."

extra["project.title"] = "TDS test-only 3rd party libraries"

// allow references to other BOMs
javaPlatform.allowDependencies()

dependencies {
  api(platform(tdsLibs.junit5.bom))

  constraints {
    api(tdsLibs.beust.jcommander)
    api(tdsLibs.commons.io)
    api(tdsLibs.google.truth)
    api(tdsLibs.hamcrest.core)
    api(tdsLibs.jaxen)
    api(tdsLibs.junit4)
    api(tdsLibs.logback.classic)
    api(tdsLibs.mockito.core)
    api(tdsLibs.pragmatists.junitparams)
    api(tdsLibs.springframework.beans)
    api(tdsLibs.springframework.context)
    api(tdsLibs.springframework.core)
    api(tdsLibs.springframework.springTest)
    api(tdsLibs.ucar.cdmTestUtils)
    api(tdsLibs.xmlunit.core)

    runtime(tdsLibs.junit5.platformLauncher)
    runtime(tdsLibs.junit5.vintageEngine)
    runtime(tdsLibs.logback.classic)
  }
}

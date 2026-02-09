/*
 * Copyright (c) 2026 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

plugins { base }

val catalogs = extensions.getByType<VersionCatalogsExtension>()

group = "edu.ucar"

version = catalogs.named("tdsLibs").findVersion("tds").get().requiredVersion

description = "A component to the THREDDS Data Server (TDS)."

extra["project.isRelease"] = !version.toString().endsWith("SNAPSHOT")

extra["project.title"] = "TDS modules"

extra["project.vendor"] = "UCAR/Unidata"

extra["project.url"] = "https://www.unidata.ucar.edu/software/tds/"

extra["docVersion"] = version.toString()

// the minimumVersion of java supported
// will be the bytecode produced by the project for all java compilation
// will be used to run the tests (test, not testWithJdkX), generate code coverage reports, etc.
// other versions of java can be used to run the tests, but this is configured in
// testing-conventions.gradle.kts
project.extra["project.minimumJdkVersion"] = "17"

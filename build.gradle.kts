/*
 * Copyright (c) 2026 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

plugins {
  id("tds-java-base-conventions")
  alias(tdsLibs.plugins.spotless)
}

description = "The NSF Unidata THREDDS Data Server (TDS)."

// To upgrade gradle, update the version and expected checksum values below
// and run ./gradlew wrapper twice
tasks.wrapper {
  distributionType = Wrapper.DistributionType.ALL
  gradleVersion = "9.5.1"
  distributionSha256Sum = "c72fb9991f6025cbe337d52ba77e531b3faf62bdd3e348fe1ccee9f51c71adb0"
}

spotless {
  // check all gradle build scripts (build-logic-tds has its own formatting check)
  kotlinGradle {
    target("*.gradle.kts", "**/*.gradle.kts")
    targetExclude("build-logic-tds/**/*")
    ktfmt().googleStyle()
  }
}

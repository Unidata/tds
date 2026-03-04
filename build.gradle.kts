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
  gradleVersion = "9.3.1"
  distributionSha256Sum = "17f277867f6914d61b1aa02efab1ba7bb439ad652ca485cd8ca6842fccec6e43"
}

spotless {
  // check all gradle build scripts (build-logic-tds has its own formatting check)
  kotlinGradle {
    target("*.gradle.kts", "**/*.gradle.kts")
    targetExclude("build-logic-tds/**/*")
    ktfmt().googleStyle()
  }
}

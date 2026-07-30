/*
 * Copyright (c) 2026 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

plugins {
  id("tds-java-base-conventions")
  alias(tdsLibs.plugins.spotless)
  id("tds-versions-conventions")
}

description = "The NSF Unidata THREDDS Data Server (TDS)."

// To upgrade gradle, update the version and expected checksum values below
// and run ./gradlew wrapper twice
tasks.wrapper {
  distributionType = Wrapper.DistributionType.BIN
  gradleVersion = "9.6.1"
  distributionSha256Sum = "9c0f7faeeb306cb14e4279a3e084ca6b596894089a0638e68a07c945a32c9e14"
}

spotless {
  // check all gradle build scripts (build-logic-tds has its own formatting check)
  kotlinGradle {
    target("*.gradle.kts", "**/*.gradle.kts")
    targetExclude("build-logic-tds/**/*")
    ktfmt().googleStyle()
  }
}

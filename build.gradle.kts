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
  gradleVersion = "9.7.1"
  distributionSha256Sum = "acd53f1edaf02f1a8ff99879f8a34b302661a057d9b063ae9e35b552f804d20a"
}

spotless {
  // check all gradle build scripts (build-logic-tds has its own formatting check)
  kotlinGradle {
    target("*.gradle.kts", "**/*.gradle.kts")
    targetExclude("build-logic-tds/**/*")
    ktfmt().googleStyle()
  }
}

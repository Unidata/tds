/*
 * Copyright (c) 2026 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.create

plugins {
  id("base-conventions")
  `java-platform`
  id("artifact-publishing-conventions")
}

publishing {
  publications { create<MavenPublication>("platform") { from(components["javaPlatform"]) } }
}

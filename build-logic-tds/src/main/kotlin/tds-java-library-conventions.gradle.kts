/*
 * Copyright (c) 2026 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

plugins {
  id("tds-java-base-conventions")
  `java-library`
  id("tds-artifact-publishing-conventions")
  id("com.diffplug.spotless")
}

java { withSourcesJar() }

tasks.withType<Jar>().configureEach {
  duplicatesStrategy = DuplicatesStrategy.FAIL
  manifest {
    attributes["Implementation-Title"] = project.extra.get("project.title")
    attributes["Implementation-Version"] = "${project.version}"
    attributes["Implementation-Vendor-Id"] = "${project.group}"
    attributes["Implementation-Vendor"] = project.extra.get("project.vendor")
    attributes["Implementation-URL"] = project.extra.get("project.url")
    attributes["Created-By"] = "Gradle ${gradle.gradleVersion}"
    System.getProperty("java.version")?.let { attributes["Build-Jdk"] = it }
    System.getProperty("user.name")?.let { attributes["Built-By"] = it }
  }

  from(rootDir.absolutePath) {
    include("LICENSE")
    into("META-INF/")
  }
}

publishing {
  publications {
    create<MavenPublication>("mavenJava") {
      from(components["java"])
      versionMapping {
        usage("java-api") { fromResolutionOf("runtimeClasspath") }
        usage("java-runtime") { fromResolutionResult() }
      }
    }
  }
}

spotless {
  java {
    target("src/*/java/**/*.java")
    eclipse().configFile("$rootDir/project-files/code-styles/eclipse-style-guide.xml")
    encoding("UTF-8")
  }
}

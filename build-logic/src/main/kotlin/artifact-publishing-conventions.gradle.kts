/*
 * Copyright (c) 2026 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

plugins { `maven-publish` }

publishing {
  repositories {
    maven {
      if ((project.extra.get("project.isRelease") as? Boolean ?: false)) {
        name = "releases"
        url = uri("https://artifacts.unidata.ucar.edu/repository/unidata-releases/")
      } else {
        name = "snapshots"
        url = uri("https://artifacts.unidata.ucar.edu/repository/unidata-snapshots/")
      }
      credentials {
        username = extra.properties["artifacts.username"] as? String
        password = extra.properties["artifacts.password"] as? String
      }
    }
  }
}

tasks.withType<GenerateModuleMetadata> { enabled = false }

val augmentPom =
  tasks.register("augmentPom") {
    publishing.publications.filterIsInstance<MavenPublication>().forEach { pub ->
      pub.pom {
        name.set("${project.group}:${project.name}")
        url.set("${project.extra.get("project.url")}")
        description.set("${project.description}")
        licenses {
          license {
            name.set("BSD 3-Clause License")
            url.set("https://github.com/Unidata/tds/blob/main/LICENSE")
          }
        }
        developers {
          developer {
            name.set("THREDDS Data Server (TDS) Developers")
            email.set("support-thredds@unidata.ucar.edu")
            organization.set("NSF Unidata")
            organizationUrl.set("https://unidata.ucar.edu")
          }
        }
        scm {
          connection.set("scm:git:https://github.com/unidata/tds.git")
          developerConnection.set("scm:git:https://github.com/unidata/tds.git")
          url.set("https://github.com/unidata/tds")
        }
      }
    }
  }

tasks.withType<GenerateMavenPom>().configureEach { dependsOn(augmentPom) }

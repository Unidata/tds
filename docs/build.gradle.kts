/*
 * Copyright (c) 2026 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

import java.io.OutputStream

plugins { base }

description = "Generate the project documentation sets."

extra["project.title"] = "Project documentation"

val docsets = listOf("adminguide", "quickstart", "userguide")
val buildAllJekyllSitesTaskName = "buildAllJekyllSites"

// copy shared files
val filesToCopy: CopySpec = copySpec {
  from("${projectDir}/shared")
  include("**/*.golden")
  rename("(.*).golden", "$1")
}

val copySharedJekyllFilesTask =
  tasks.register<Copy>("copySharedJekyllFiles") {
    group = "documentation-set"
    into(projectDir)
    docsets.forEach { docset -> into(docset) { with(filesToCopy) } }
  }

// clean up shared files that were copied during the jekyll site build tasks
tasks.clean {
  val tree = fileTree("${projectDir}/shared")
  tree.include("**/*.golden")
  tree.forEach {
    val relFileToDelete = projectDir.toPath().resolve("shared").relativize(it.toPath())
    docsets.forEach { docset ->
      val fileToDelete = file("${projectDir}/${docset}").toPath().resolve(relFileToDelete)
      val fileToDeleteStr = fileToDelete.toString().replace(Regex("\\.golden\$"), "")
      delete(fileToDeleteStr)
    }
  }
}

// aggregator task (aggregate individual doc set builds)
tasks.register(buildAllJekyllSitesTaskName) {
  group = "Documentation"
  description = "Build all jekyll sites."
}

// Documentation build using Docker
val catalogs = extensions.getByType<VersionCatalogsExtension>()

val docTheme =
  "unidata-jekyll-docs:${catalogs.named("tdsLibs").findVersion("unidata-doc-theme").get().requiredVersion}"

val isGitHub = System.getenv("GITHUB_ACTIONS") != null
val imageBaseUrl = if (isGitHub) "ghcr.io/unidata" else "docker.io/unidata"
val dockerImage = "${imageBaseUrl}/${docTheme}"

// make string upper camel case
// examples: cdm -> Cdm, userguide -> UserGuide
fun makeUpperCamelCase(docSet: String): String {
  var taskName = docSet.replaceFirstChar { it.uppercase() }
  taskName = taskName.replace("guide", "Guide")
  return taskName
}

docsets.forEach { docset ->
  val partialTaskName = makeUpperCamelCase(docset)
  val siteBuildDir = layout.buildDirectory.dir("site/${docset}")
  val buildJekyllSite =
    tasks.register<Exec>("build${partialTaskName}") {
      group = "documentation-set"
      description = "Build ${docset} jekyll site."
      val buildDocInputs = fileTree("./${docset}/src/site")
      buildDocInputs.exclude(".jekyll-cache")
      inputs.files(buildDocInputs)
      outputs.dir(siteBuildDir)
      commandLine(
        "docker",
        "run",
        "--rm",
        "-e",
        "SRC_DIR=/tds/docs/${docset}/src/site",
        "-v",
        "${rootDir}:/tds",
        "-v",
        "./${relativePath(siteBuildDir.get().toString())}:/site",
        dockerImage,
        "build",
      )
      dependsOn(copySharedJekyllFilesTask)
    }
  val serveJekyllSite =
    tasks.register<Exec>("serve${partialTaskName}") {
      group = "documentation-set"
      description = "Serve $docset jekyll site."
      commandLine(
        "docker",
        "run",
        "--rm",
        "-d",
        "--name",
        "tds-docs-server",
        "-e",
        "SRC_DIR=/tds/docs/${docset}/src/site",
        "-v",
        "${rootDir}:/tds",
        "-p",
        "4005:4005",
        dockerImage,
        "serve",
        "--livereload",
      )
      standardOutput = OutputStream.nullOutputStream()
      dependsOn(copySharedJekyllFilesTask)
      doLast {
        val msg = "TDS ${docset} available at http://localhost:4005"
        val bannerBorder = String(CharArray(msg.length + 4) { '#' })
        println("\n${bannerBorder}\n# ${msg} #\n${bannerBorder}")
      }
    }
  tasks.named(buildAllJekyllSitesTaskName).configure({ dependsOn(buildJekyllSite) })
}

tasks.register<Exec>("stopServe") {
  group = "documentation-set"
  description = "Stop the local server used while live editing the netCDF-Java documentation."
  commandLine("docker", "stop", "tds-docs-server")
  docsets.forEach { docset ->
    delete("${projectDir}/${docset}/src/site/Gemfile")
    delete("${projectDir}/${docset}/src/site/Gemfile.lock")
  }
}

tasks.build { dependsOn(buildAllJekyllSitesTaskName) }

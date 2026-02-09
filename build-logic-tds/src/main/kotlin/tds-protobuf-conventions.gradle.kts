/*
 * Copyright (c) 2026 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

plugins {
  id("tds-java-base-conventions")
  id("com.google.protobuf")
}

val libCatalog = extensions.getByType(VersionCatalogsExtension::class.java).named("tdsLibs")

protobuf.protoc { artifact = libCatalog.findLibrary("protobuf-protoc").get().get().toString() }

tasks.withType(JacocoReport::class.java).configureEach {
  classDirectories.setFrom(
    sourceSets.main.get().output.asFileTree.matching { exclude("**/generated/**") }
  )
}

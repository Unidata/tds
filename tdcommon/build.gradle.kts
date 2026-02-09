/*
 * Copyright (c) 2026 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

plugins {
  id("tds-java-library-conventions")
  id("tds-protobuf-conventions")
}

description = "A collection of utilities needed server-side, including THREDDS catalog handling."

extra["project.title"] = "Server-side common library"

dependencies {
  implementation(platform(project(":tds-platform")))

  implementation(tdsLibs.eclipse.serializerPersistenceBinaryJdk8)
  implementation(tdsLibs.eclipse.serializerPersistenceBinaryJdk17)
  implementation(tdsLibs.eclipse.storeCache)
  implementation(tdsLibs.findbugs.jsr305)
  implementation(tdsLibs.guava)
  implementation(tdsLibs.jakarta.annotationApi)
  implementation(tdsLibs.jakarta.validationApi)
  implementation(tdsLibs.jdom2)
  implementation(tdsLibs.protobuf)
  implementation(tdsLibs.quartz)
  implementation(tdsLibs.re2j)
  implementation(tdsLibs.slf4j.api)
  implementation(tdsLibs.springframework.beans)
  implementation(tdsLibs.springframework.context)
  implementation(tdsLibs.springframework.core)
  implementation(tdsLibs.ucar.cdmCore)
  implementation(tdsLibs.ucar.cdmS3)
  implementation(tdsLibs.ucar.cdmZarr)
  implementation(tdsLibs.ucar.grib)

  testImplementation(platform(project(":tds-testing-platform")))

  testImplementation(tdsLibs.google.truth)
  testImplementation(tdsLibs.mockito.core)
  testImplementation(tdsLibs.ucar.cdmTestUtils)

  testCompileOnly(tdsLibs.junit4)

  testRuntimeOnly(tdsLibs.junit5.platformLauncher)
  testRuntimeOnly(tdsLibs.junit5.vintageEngine)
  testRuntimeOnly(tdsLibs.logback.classic)
}

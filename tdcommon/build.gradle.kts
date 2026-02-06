/*
 * Copyright (c) 2026 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

plugins {
  id("java-library-conventions")
  id("protobuf-conventions")
}

description = "A collection of utilities needed server-side, including THREDDS catalog handling."

extra["project.title"] = "Server-side common library"

dependencies {
  implementation(platform(project(":tds-platform")))

  implementation(libs.eclipse.serializerPersistenceBinaryJdk8)
  implementation(libs.eclipse.serializerPersistenceBinaryJdk17)
  implementation(libs.eclipse.storeCache)
  implementation(libs.findbugs.jsr305)
  implementation(libs.guava)
  implementation(libs.jakarta.annotationApi)
  implementation(libs.jakarta.validationApi)
  implementation(libs.jdom2)
  implementation(libs.protobuf)
  implementation(libs.quartz)
  implementation(libs.re2j)
  implementation(libs.slf4j.api)
  implementation(libs.springframework.beans)
  implementation(libs.springframework.context)
  implementation(libs.springframework.core)
  implementation(libs.ucar.cdmCore)
  implementation(libs.ucar.cdmS3)
  implementation(libs.ucar.cdmZarr)
  implementation(libs.ucar.grib)

  testImplementation(platform(project(":tds-testing-platform")))

  testImplementation(libs.google.truth)
  testImplementation(libs.mockito.core)
  testImplementation(libs.ucar.cdmTestUtils)

  testCompileOnly(libs.junit4)

  testRuntimeOnly(libs.junit5.platformLauncher)
  testRuntimeOnly(libs.junit5.vintageEngine)
  testRuntimeOnly(libs.logback.classic)
}

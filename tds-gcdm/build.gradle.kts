/*
 * Copyright (c) 2025 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

import com.github.psxpaul.task.JavaExecFork

plugins {
  id("tds-java-base-conventions")
  application
  alias(tdsLibs.plugins.execfork)
}

description = "NetcdfFile provider for gCDM."

extra["project.title"] = "IOSP for gCDM"

dependencies {
  implementation(platform(project(":tds-platform")))

  implementation(project(":tds"))

  implementation(tdsLibs.ucar.cdmCore)
  implementation(tdsLibs.ucar.gcdm) {
    exclude(group = "ch.qos.logback", module = "logback-classic")
  }

  implementation(tdsLibs.grpc.protobuf)
  implementation(tdsLibs.grpc.stub)
  implementation(tdsLibs.protobuf)
  implementation(tdsLibs.guava)
  implementation(tdsLibs.slf4j.api)

  compileOnly(tdsLibs.findbugs.jsr305)

  runtimeOnly(tdsLibs.grpc.nettyShaded)

  testImplementation(platform(project(":tds-testing-platform")))

  testImplementation(tdsLibs.google.truth)

  testCompileOnly(tdsLibs.junit4)

  testRuntimeOnly(tdsLibs.junit5.platformLauncher)
  testRuntimeOnly(tdsLibs.junit5.vintageEngine)
  testRuntimeOnly(tdsLibs.logback.classic)
}

application { mainClass = "ucar.gcdm.server.GcdmServer" }

val startDaemon =
  tasks.register<JavaExecFork>("startDaemon") {
    classpath = sourceSets.main.get().runtimeClasspath
    main = "ucar.gcdm.server.GcdmServer"
    // To attach the debugger to the gcdm server add to the jvmArgs
    // '-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005'
    jvmArgs = listOf("-Xmx512m", "-Djava.awt.headless=true")
    standardOutput = project.layout.buildDirectory.file("gcdm_logs/gcdm.log")
    errorOutput = project.layout.buildDirectory.file("gcdm_logs/gcdm-error.log")
    waitForPort = 16111
    waitForOutput = "Server started, listening on 16111"
    dependsOn(tasks.jar, tasks.testClasses)
  }

tasks.withType<Test> { dependsOn(startDaemon) }

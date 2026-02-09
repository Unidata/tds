/*
 * Copyright (c) 2026 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

plugins {
  id("tds-base-conventions")
  java
  jacoco
}

val minimumTestVersion = project.extra["project.minimumJdkVersion"]

// test tasks will be created for each of the versions listed here (e.g. test11, test17, etc.)
// this will allow the tests to be compiled to and ran with multiple versions of java
val testLtsVersions = listOf(minimumTestVersion, "21", "25")

project.extra["project.testLtsVersions"] = testLtsVersions

// Various checks for filtering tests to run
val testdataDirKey = "unidata.testdata.path"
val isCdmUnitTestDirAvailable = isSystemPropertyAValidDirectory(testdataDirKey)

val jenkinsEnvVar = "JENKINS_URL"
var isJenkins = System.getenv(jenkinsEnvVar) != null

val pullRequestEnvVar = "GITHUB_ACTIONS"
val isPullRequestCheck = System.getenv(pullRequestEnvVar) != null

val runAllTests = isSystemPropertySet("runAllTestsExceptIgnored")
val isRdaDataAvailable = isSystemPropertySet("rdaDataAvailable")

fun isSystemPropertyAValidDirectory(sysPropKey: String): Boolean {
  val sysPropVal = System.getProperty(sysPropKey)
  if (sysPropVal != null) {
    val f = File(sysPropVal)
    if (f.exists() && f.isDirectory) {
      return true
    }
  }
  System.setProperty(sysPropKey, "${layout.buildDirectory}/NO/$sysPropKey/FOUND/")
  return false
}

fun isSystemPropertySet(sysPropKey: String): Boolean {
  val sysPropVal = System.getProperty(sysPropKey)
  return sysPropVal != null
}

val testAll =
  tasks.register("testAll") {
    group = "verification"
    description = "Aggregate task to run all tests using JDK $minimumTestVersion}."
    dependsOn(tasks.test)
  }

tasks {
  withType<Test>().configureEach {
    // pass along the location of native libraries, if set
    System.getenv("JNA_PATH")?.let { setEnvironment(Pair("JNA_PATH", it)) }
    System.getProperty("jna.library.path")?.let { systemProperty("jna.library.path", it) }
    jvmArgs("-Xmx1024m")
    systemProperties[testdataDirKey] = System.getProperty(testdataDirKey)
    useJUnitPlatform {
      includeEngines("junit-vintage")
      excludeEngines("junit-jupiter")
      // Decide how to handle test failures.
      // put all log messages inside a doFirst closure so that messages only appear
      // during the execution phase
      if (isJenkins && !(project.extra.get("project.isRelease") as? Boolean ?: false)) {
        // On Jenkins, don't let test failures fail the build unless we are doing we release; we
        // want
        // the full test report.
        ignoreFailures = true
      } else {
        // Otherwise, fail the build at the first sign of failure.
        ignoreFailures = false
      }
      // Option to run all tests regardless of environment or resource availability
      if (runAllTests) {
        doFirst { logger.warn("Running all tests except those explicitly annotated with @Ignore.") }
      } else {
        // Don't skip tests on Jenkins, except NotJenkins ones.
        if (!isCdmUnitTestDirAvailable && !isJenkins) {
          excludeTags("ucar.unidata.util.test.category.NeedsCdmUnitTest")
          doFirst { logger.warn("Skipping all NeedsCdmUnitTest tests.") }
        }

        if (isPullRequestCheck) {
          excludeTags(
            "ucar.unidata.util.test.category.NotPullRequest",
            "ucar.unidata.util.test.category.NeedsExternalResource",
          )
          doFirst {
            logger.warn(
              "Skipping all NotPullRequest tests: detected that we're running in the GitHub Actions environment."
            )
          }
        }

        if (!isRdaDataAvailable) {
          excludeTags("ucar.unidata.util.test.category.NeedsRdaData")
          doFirst { logger.warn("Skipping all tests that require access to RDA data.") }
        }
      }
    }
  }
}

// the basic test task will use the minimumVersion of java to compile and run
tasks.test {
  javaLauncher.set(
    project.javaToolchains.launcherFor {
      languageVersion = JavaLanguageVersion.of(minimumTestVersion.toString().toInt())
    }
  )
  // report only generated after test running with minimumTestVersion JDK
  finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
  reports {
    xml.required = true
    html.required = true
  }
  dependsOn(tasks.test) // tests are required to run before generating the report
}

// these test tasks run with a specific version of java, other than the minimum version,
// and are named as testWithJavaX (e.g. testWithJdk11, testWithJdk17, etc.)
testLtsVersions
  .filter { it != minimumTestVersion }
  .forEach {
    tasks.register<Test>("testWithJdk${it}") {
      group = "verification"
      description = "Runs the test task using JDK ${it}."
      testClassesDirs = sourceSets.test.get().output.classesDirs
      classpath = sourceSets.test.get().runtimeClasspath
      javaLauncher.set(
        project.javaToolchains.launcherFor {
          languageVersion = JavaLanguageVersion.of(it.toString().toInt())
        }
      )
    }
  }

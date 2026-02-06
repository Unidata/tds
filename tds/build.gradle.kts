/*
 * Copyright (c) 2026 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import org.akhikhl.gretty.FarmExtension

plugins {
  id("java-library-conventions")
  alias(libs.plugins.cyclonedx.bom)
  war
  alias(libs.plugins.gretty)
}

description =
  "The THREDDS Data Server (TDS) is a web server that provides catalog and data access services for " +
    "scientific data using OPeNDAP, OGC WCS and WMS, HTTP, and other remote-data-access protocols."

extra["project.title"] = "THREDDS Data Server (TDS)"

val downloadsDir = rootProject.layout.buildDirectory.dir("downloads").get()

//////////////////////////////////////
// integration testing source setup //
//////////////////////////////////////

sourceSets {
  create("integrationTests") {
    compileClasspath += sourceSets.main.get().output
    compileClasspath += sourceSets.test.get().output
    runtimeClasspath += sourceSets.main.get().output
    runtimeClasspath += sourceSets.test.get().output
  }
  create("freshInstallTests") {
    compileClasspath += sourceSets.main.get().output
    runtimeClasspath += sourceSets.main.get().output
  }
}

val integrationTestsImplementation by
  configurations.getting { extendsFrom(configurations.implementation.get()) }
val integrationTestsRuntimeOnly by configurations.getting

configurations["integrationTestsRuntimeOnly"].extendsFrom(configurations.runtimeOnly.get())

val freshInstallTestsImplementation by
  configurations.getting { extendsFrom(configurations.implementation.get()) }
val freshInstallTestsRuntimeOnly by configurations.getting

configurations["freshInstallTestsRuntimeOnly"].extendsFrom(configurations.runtimeOnly.get())

val gwt by configurations.creating { extendsFrom(configurations.implementation.get()) }

///////////////////////////
// dependency management //
///////////////////////////

dependencies {
  implementation(platform(project(":tds-platform")))

  // general dependencies
  implementation(project(":tdcommon"))

  implementation(libs.commons.lang3)
  implementation(libs.coverity.escapers) // todo: replace with google escapers?
  implementation(libs.findbugs.jsr305)
  implementation(libs.guava)
  implementation(libs.jdom2)
  implementation(libs.jodaTime)
  implementation(libs.json)
  implementation(libs.quartz)
  implementation(libs.ucar.cdmCore)
  implementation(libs.ucar.cdmMisc)
  implementation(libs.ucar.grib)
  implementation(libs.ucar.httpservices)

  runtimeOnly(project(":tds-ugrid"))

  runtimeOnly(libs.ucar.bufr)
  runtimeOnly(libs.ucar.cdmImage)
  runtimeOnly(libs.ucar.cdmMcidas)
  runtimeOnly(libs.ucar.cdmRadial)
  runtimeOnly(libs.ucar.cdmS3)
  runtimeOnly(libs.ucar.cdmZarr)
  runtimeOnly(libs.ucar.netcdf4)

  // logging
  implementation(libs.slf4j.api)
  runtimeOnly(libs.log4j.slf4j2Impl)
  runtimeOnly(libs.log4j.jakartaWeb)

  /////////////////////////////////////
  // General Web Server Dependencies //
  /////////////////////////////////////

  // Spring
  implementation(libs.springframework.beans)
  implementation(libs.springframework.context)
  implementation(libs.springframework.core)
  implementation(libs.springframework.web)
  implementation(libs.springframework.webmvc)

  // Needed for "xmlns:security" schema in applicationContext.xml.
  runtimeOnly(libs.springsecurity.config)

  // Needed for FilterChainProxy in applicationContext.xml.
  runtimeOnly(libs.springsecurity.web)

  // servlet
  providedCompile(libs.jakarta.servletApi)

  // @Resource annotation
  implementation(libs.jakarta.annotationApi)

  // jstl / jsp
  runtimeOnly(libs.glassfish.jstl)
  runtimeOnly(libs.jakarta.jstlApi)

  // JSR 303 (bean validation) with Hibernate Validator
  implementation(libs.jakarta.validationApi)
  runtimeOnly(libs.glassfish.jakartaEl)
  runtimeOnly(libs.hibernate.validator)

  implementation(libs.thymeleaf.spring6)

  ///////////////////////////////////
  // Service specific dependencies //
  ///////////////////////////////////

  // DAP2
  implementation(project(":opendap-servlet"))
  implementation(libs.ucar.opendap)

  // DAP4
  implementation(project(":d4servlet"))
  implementation(libs.ucar.dap4)

  // NCSS
  implementation(libs.sensorweb.xmlOmV20)
  implementation(libs.sensorweb.xmlWaterMLV20)
  implementation(libs.ucar.waterml)

  // threddsIso
  implementation(libs.nciso.common)

  // edal-java / ncwms related libs
  implementation(libs.edal.cdm)
  implementation(libs.edal.common)
  implementation(libs.edal.godiva)
  implementation(libs.edal.graphics)
  implementation(libs.edal.wms)
  implementation(libs.oro)
  runtimeOnly(libs.jaxen)

  // GWT for Godiva3
  gwt(libs.gwt.dev)
  gwt(libs.gwt.user)

  /////////////
  // Testing //
  /////////////

  testImplementation(platform(project(":tds-testing-platform")))

  testImplementation(project(":tds-test-utils"))

  testImplementation(libs.beust.jcommander) // todo: replace usage with core java
  testImplementation(libs.commons.io)
  testImplementation(libs.google.truth)
  testImplementation(libs.hamcrest.core)
  testImplementation(libs.jaxen) // Needed for XPath operations in mock tests
  testImplementation(libs.logback.classic)
  testImplementation(libs.pragmatists.junitparams)
  testImplementation(libs.springframework.springTest)
  testImplementation(libs.ucar.cdmTestUtils)
  testImplementation(libs.xmlunit.core)

  testCompileOnly(libs.junit4)

  testRuntimeOnly(libs.junit5.platformLauncher)
  testRuntimeOnly(libs.junit5.vintageEngine)

  // integration test
  integrationTestsImplementation(project(":tds-test-utils"))

  integrationTestsImplementation(libs.google.truth)
  integrationTestsImplementation(libs.junit4)
  integrationTestsImplementation(libs.springframework.springTest)
  integrationTestsImplementation(libs.ucar.cdmTestUtils)

  integrationTestsRuntimeOnly(libs.junit5.platformLauncher)
  integrationTestsRuntimeOnly(libs.junit5.vintageEngine)

  // fresh installation tests
  freshInstallTestsImplementation(project(":tdcommon"))
  freshInstallTestsImplementation(project(":tds-test-utils"))

  freshInstallTestsImplementation(libs.google.truth)
  freshInstallTestsImplementation(libs.junit4)
  freshInstallTestsImplementation(libs.slf4j.api)
  freshInstallTestsImplementation(libs.springframework.beans)
  freshInstallTestsImplementation(libs.springframework.context)
  freshInstallTestsImplementation(libs.springframework.core)
  freshInstallTestsImplementation(libs.ucar.cdmTestUtils)
  freshInstallTestsImplementation(libs.xmlunit.core)

  freshInstallTestsRuntimeOnly(libs.junit5.platformLauncher)
  freshInstallTestsRuntimeOnly(libs.junit5.vintageEngine)
}

// without the "-parameters" compile option, we get failures like
// java.lang.IllegalArgumentException: Name for argument of type [java.lang.String] not specified,
// and parameter name information not available via reflection. Ensure that the compiler uses the
// '-parameters' flag. at
// org.springframework.web.method.annotation.AbstractNamedValueMethodArgumentResolver.updateNamedValueInfo(AbstractNamedValueMethodArgumentResolver.java:186) at
// radar server @RequestMapping(value = "**/{dataset}")
tasks.withType(JavaCompile::class.java).configureEach { options.compilerArgs.add("-parameters") }

/////////////////////
// SBOM generation //
/////////////////////

tasks.cyclonedxDirectBom {
  xmlOutput = downloadsDir.file("thredds-${project.version}-sbom.xml")
  jsonOutput = downloadsDir.file("thredds-${project.version}-sbom.json")
}

/////////////////////
// GWT for Godiva3 //
/////////////////////

val gwtDir = layout.buildDirectory.dir("gwt").get().asFile
val extraDir = layout.buildDirectory.dir("extra").get().asFile

val compileGwt =
  tasks.register<JavaExec>("compileGwt") {
    group = "build"
    inputs.files(projectDir.resolve("src/main/resources/Godiva3.gwt.xml"))
    inputs.files(sourceSets.main.get().java.srcDirs)
    inputs.files(sourceSets.main.get().resources.srcDirs)
    outputs.dir(gwtDir)
    doFirst {
      if (!gwtDir.exists()) {
        file(gwtDir).mkdirs()
      }
    }
    mainClass.set("com.google.gwt.dev.Compiler")
    classpath(
      gwt,
      sourceSets.main
        .get()
        .runtimeClasspath, // For 'uk/ac/rdg/resc/godiva/Godiva.gwt.xml' in "edal-java"
      sourceSets.main.get().resources, // For Godiva3.gwt.xml in 'tds/src/main/resources'
    )
    args(
      listOf(
        "Godiva3", // The GWT module, from edal-godiva.
        "-war",
        gwtDir,
        "-logLevel",
        "WARN", // Only get log messages at level WARN or above. We don't want the spammy output.
        "-localWorkers",
        "2",
        "-compileReport",
        "-extra",
        extraDir,
      )
    )
    maxHeapSize = "512M"
  }

///////////////////////////
// Configure thredds WAR //
///////////////////////////

tasks.war {
  doFirst {
    classpath
      ?.find { it.name.contains("servlet-api") }
      ?.let { throw GradleException("Found a servlet-api JAR in the WAR classpath: ${it.name}") }
  }
  // Replace '$projectVersion' and '$buildTimestamp' placeholders with the correct values.
  // Currently, we only use those placeholders in tds.properties and README.txt.
  val iso8601Format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
  var buildProps =
    mapOf(
      Pair("projectVersion", project.version),
      Pair("buildTimestamp", iso8601Format.format(Date())),
    )

  // War CopySpec already includes everything in 'src/main/webapp', which tds.properties lives
  // within. So, the from() and into() methods aren't needed.
  filesMatching("**/tds.properties") { expand(buildProps) }

  from("README.txt") {
    into("docs")
    expand(buildProps)
  }

  dependsOn(compileGwt)
  from(gwtDir)
  destinationDirectory = downloadsDir
  archiveFileName = "thredds##${project.version}.war"
}

val copyWebappFilesForTests by
  tasks.registering(Copy::class) {
    from("src/main/webapp")
    from("src/main/webapp/WEB-INF/classes")
    into(sourceSets.test.get().java.destinationDirectory)
  }

tasks.processTestResources { dependsOn(copyWebappFilesForTests) }

tasks.withType(Test::class.java).configureEach { exclude("**/migrateToJunit5/**") }

////////////////////////////////////////
// integration tests and gretty tasks //
////////////////////////////////////////

// allow servlet container to be configured by setting a system property
var servletContainerName = System.getProperty("tds.test.gretty.container")

if (servletContainerName == null || servletContainerName.isEmpty()) {
  servletContainerName = "tomcat10"
}

val cleanTestContentRoot = layout.buildDirectory.dir("freshInstallTests").get().asFile
val cleanFreshInstallContentRoot =
  tasks.register("cleanFreshInstallContentRoot") {
    doFirst {
      // clean up any existing fresh installation files
      if (cleanTestContentRoot.exists()) {
        cleanTestContentRoot.deleteRecursively()
      }
      // ensure thredds home exists
      cleanTestContentRoot.mkdirs()
    }
  }

gretty {
  servletContainer = "$servletContainerName"
  httpsEnabled = true
  sslKeyStorePath = "$rootDir/gradle/gretty/tomcat/keystore"
  sslKeyStorePassword = "secret666"
  realmConfigFile = "$rootDir/gradle/gretty/tomcat/tomcat-users.xml"
  contextPath = "/thredds"
}

farms {
  farm(
    "FullConfig",
    delegateClosureOf<FarmExtension> {
      servletContainer = "$servletContainerName"
      systemProperty("tds.content.root.path", projectDir.resolve("src/test/content").absolutePath)
      httpPort = 8081
      System.getProperty("unidata.testdata.path")?.let {
        systemProperty("unidata.testdata.path", it)
      }
    },
  )
  farm(
    "FreshInstall",
    delegateClosureOf<FarmExtension> {
      servletContainer = "$servletContainerName"
      systemProperty("tds.content.root.path", cleanTestContentRoot.absolutePath)
      httpPort = 8081
    },
  )
}

val freshInstallTests =
  tasks.register<Test>("freshInstallTests") {
    description = "Runs fresh install tests."
    group = "verification"
    useJUnitPlatform()
    testClassesDirs = sourceSets["freshInstallTests"].output.classesDirs
    classpath = sourceSets["freshInstallTests"].runtimeClasspath
    dependsOn("farmBeforeIntegrationTestFreshInstall")
    dependsOn(cleanFreshInstallContentRoot)
    finalizedBy("farmAfterIntegrationTestFreshInstall")
    shouldRunAfter(tasks.test)
    systemProperty("tds.content.root.path", cleanTestContentRoot.absolutePath)
    // Use built-in Xalan XSLT instead of Saxon-HE.
    // This works around an error we were seeing in org.xmlunit.builder.DiffBuilder.build():
    //    java.lang.ClassCastException: net.sf.saxon.value.ObjectValue cannot be cast to
    // net.sf.saxon.om.NodeInfo
    //        ...
    //        at
    // thredds.tds.TestFreshTdsInstall.shouldReturnExpectedClientCatalog(TestFreshTdsInstall.java:72)
    systemProperty(
      "javax.xml.transform.TransformerFactory",
      "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl",
    )
  }

tasks.testAll.configure { dependsOn(freshInstallTests) }

val integrationTests =
  tasks.register<Test>("integrationTests") {
    description = "Runs integration tests."
    group = "verification"
    useJUnitPlatform()
    testClassesDirs = sourceSets["integrationTests"].output.classesDirs
    classpath = sourceSets["integrationTests"].runtimeClasspath
    dependsOn("farmBeforeIntegrationTestFullConfig")
    finalizedBy("farmAfterIntegrationTestFullConfig")
    shouldRunAfter(freshInstallTests)
    systemProperty("tds.content.root.path", projectDir.resolve("src/test/content").absolutePath)
  }

tasks.testAll.configure { dependsOn(integrationTests) }

project.afterEvaluate {
  tasks.findByName("farmBeforeIntegrationTestFreshInstall")?.dependsOn(cleanFreshInstallContentRoot)
}

tasks.test {
  useJUnitPlatform()
  systemProperty("tds.content.root.path", projectDir.resolve("src/test/content").absolutePath)
}

////////////////////////////////////////////////////////////
// prevent certain transitive dependencies from making it //
// onto the classpath of thredds.war                      //
////////////////////////////////////////////////////////////

configurations.all {
  // STAX is already included in Java 1.6+; no need for a third-party dependency.
  // check with:
  // ./gradlew -q tds:dependencyInsight --configuration runtimeClasspath --dependency stax-api
  exclude(group = "stax", module = "stax-api")
  // exclude commons-logging
  // Standard Commons Logging discovery in action with spring-jcl (remove to avoid potential
  // conflicts)
  exclude(group = "commons-logging", module = "commons-logging")
}

// "testRuntime" extends from "runtime", meaning that "testRuntime" will get the log4j dependencies
// declared in "runtime". However, we want logback-classic to be the logger during tests, so
// exclude all the log4j stuff.
configurations.testRuntimeOnly { exclude("org.apache.logging.log4j") }

val createChecksums =
  tasks.register("createChecksums") {
    group = "build"
    description = "Create .sha1, .sha256, and .md5 checksum files for the uber-jars."

    doLast {
      listOf("MD5", "SHA-1", "SHA-256").forEach { algorithm ->
        fileTree(downloadsDir) { include("thredds*.war", "thredds*.json", "thredds*.xml") }
          .forEach { jarFile ->
            MessageDigest.getInstance(algorithm)
              .let { md ->
                md.digest(file(jarFile).readBytes()).let {
                  BigInteger(1, it).toString(16).padStart(md.digestLength * 2, '0')
                }
              }
              .let { checksum ->
                downloadsDir
                  .file("${jarFile.name}.${algorithm.lowercase().replace("-", "")}")
                  .asFile
                  .writeText(checksum)
              }
          }
      }
    }
    dependsOn(tasks.cyclonedxBom, tasks.war)
  }

tasks.assemble { dependsOn(tasks.cyclonedxBom, createChecksums) }

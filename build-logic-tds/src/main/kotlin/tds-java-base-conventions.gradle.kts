/*
 * Copyright (c) 2026 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

plugins {
  id("tds-base-conventions")
  id("tds-testing-conventions")
}

tasks {
  // everything gets compiled to the minimum supported version byte code
  withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release = project.extra["project.minimumJdkVersion"].toString().toInt()
  }

  withType<Javadoc>().configureEach {
    group = "documentation"
    options {
      encoding = "UTF-8"
      // options past here require the standard java doclet
      require(this is StandardJavadocDocletOptions)
      charSet = "UTF-8"
      docEncoding = "UTF-8"
      addStringOption("-release", project.extra["project.minimumJdkVersion"].toString())
      // so many invalid HTML errors...need to add this for now to
      // generate javadocs
      addBooleanOption("Xdoclint:none", true)
    }
  }
}

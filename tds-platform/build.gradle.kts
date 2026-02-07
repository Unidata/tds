/*
 * Copyright (c) 2026 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

plugins { id("platform-conventions") }

description =
  "BOM containing the public artifacts, and their third-party dependencies, that comprise the THREDDS Data Server."

extra["project.title"] = "TDS BOM with 3rd party libraries"

// allow references to other BOMs
javaPlatform.allowDependencies()

dependencies {
  // netCDF-Java and 3rd party dependencies
  api(platform(libs.jakarta.jakartaeeBom))
  api(platform(libs.log4j.bom))
  api(platform(libs.springframework.bom))
  api(platform(libs.springsecurity.bom))
  api(platform(libs.ucar.netcdfJavaBom))

  constraints {
    api(libs.colt)
    api(libs.commons.lang3)
    api(libs.coverity.escapers)
    api(libs.eclipse.serializerPersistenceBinaryJdk8)
    api(libs.eclipse.serializerPersistenceBinaryJdk17)
    api(libs.eclipse.storeCache)
    api(libs.edal.cdm)
    api(libs.edal.common)
    api(libs.edal.godiva)
    api(libs.edal.graphics)
    api(libs.edal.wms)
    api(libs.findbugs.jsr305)
    api(libs.guava)
    api(libs.gwt.dev)
    api(libs.gwt.user)
    api(libs.jakarta.annotationApi)
    api(libs.jakarta.servletApi)
    api(libs.jakarta.validationApi)
    api(libs.jdom2)
    api(libs.jodaTime)
    api(libs.json)
    api(libs.nciso.common)
    api(libs.oro)
    api(libs.protobuf)
    api(libs.quartz)
    api(libs.re2j)
    api(libs.sensorweb.xmlOmV20)
    api(libs.sensorweb.xmlWaterMLV20)
    api(libs.slf4j.api)
    api(libs.springframework.beans)
    api(libs.springframework.context)
    api(libs.springframework.core)
    api(libs.springframework.web)
    api(libs.springframework.webmvc)
    api(libs.thymeleaf.spring6)
    api(libs.ucar.cdmCore)
    api(libs.ucar.cdmMisc)
    api(libs.ucar.cdmS3)
    api(libs.ucar.cdmZarr)
    api(libs.ucar.dap4)
    api(libs.ucar.grib)
    api(libs.ucar.httpservices)
    api(libs.ucar.opendap)
    api(libs.ucar.waterml)

    runtime(libs.glassfish.jakartaEl)
    runtime(libs.glassfish.jstl)
    runtime(libs.hibernate.validator)
    runtime(libs.jakarta.jstlApi)
    runtime(libs.jaxen)
    runtime(libs.log4j.jakartaWeb)
    runtime(libs.log4j.slf4j2Impl)
    runtime(libs.springsecurity.config)
    runtime(libs.springsecurity.web)
    runtime(libs.ucar.bufr)
    runtime(libs.ucar.cdmImage)
    runtime(libs.ucar.cdmMcidas)
    runtime(libs.ucar.cdmRadial)
    runtime(libs.ucar.cdmS3)
    runtime(libs.ucar.cdmZarr)
    runtime(libs.ucar.netcdf4)
  }
}

/*
 * Copyright (c) 2026 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

plugins { id("tds-platform-conventions") }

description =
  "BOM containing the public artifacts, and their third-party dependencies, that comprise the THREDDS Data Server."

extra["project.title"] = "TDS BOM with 3rd party libraries"

// allow references to other BOMs
javaPlatform.allowDependencies()

dependencies {
  // netCDF-Java and 3rd party dependencies
  api(platform(tdsLibs.jakarta.jakartaeeBom))
  api(platform(tdsLibs.log4j.bom))
  api(platform(tdsLibs.springframework.bom))
  api(platform(tdsLibs.springsecurity.bom))
  api(platform(tdsLibs.ucar.netcdfJavaBom))

  constraints {
    api(tdsLibs.colt)
    api(tdsLibs.commons.lang3)
    api(tdsLibs.coverity.escapers)
    api(tdsLibs.eclipse.serializerPersistenceBinaryJdk8)
    api(tdsLibs.eclipse.serializerPersistenceBinaryJdk17)
    api(tdsLibs.eclipse.storeCache)
    api(tdsLibs.edal.cdm)
    api(tdsLibs.edal.common)
    api(tdsLibs.edal.godiva)
    api(tdsLibs.edal.graphics)
    api(tdsLibs.edal.wms)
    api(tdsLibs.findbugs.jsr305)
    api(tdsLibs.guava)
    api(tdsLibs.gwt.dev)
    api(tdsLibs.gwt.user)
    api(tdsLibs.jakarta.annotationApi)
    api(tdsLibs.jakarta.servletApi)
    api(tdsLibs.jakarta.validationApi)
    api(tdsLibs.jdom2)
    api(tdsLibs.jodaTime)
    api(tdsLibs.json)
    api(tdsLibs.nciso.common)
    api(tdsLibs.oro)
    api(tdsLibs.protobuf)
    api(tdsLibs.quartz)
    api(tdsLibs.re2j)
    api(tdsLibs.sensorweb.xmlOmV20)
    api(tdsLibs.sensorweb.xmlWaterMLV20)
    api(tdsLibs.slf4j.api)
    api(tdsLibs.springframework.beans)
    api(tdsLibs.springframework.context)
    api(tdsLibs.springframework.core)
    api(tdsLibs.springframework.web)
    api(tdsLibs.springframework.webmvc)
    api(tdsLibs.thymeleaf.spring6)
    api(tdsLibs.ucar.cdmCore)
    api(tdsLibs.ucar.cdmMisc)
    api(tdsLibs.ucar.cdmS3)
    api(tdsLibs.ucar.cdmZarr)
    api(tdsLibs.ucar.dap4)
    api(tdsLibs.ucar.grib)
    api(tdsLibs.ucar.httpservices)
    api(tdsLibs.ucar.opendap)
    api(tdsLibs.ucar.waterml)

    runtime(tdsLibs.glassfish.jakartaEl)
    runtime(tdsLibs.glassfish.jstl)
    runtime(tdsLibs.hibernate.validator)
    runtime(tdsLibs.jakarta.jstlApi)
    runtime(tdsLibs.jaxen)
    runtime(tdsLibs.log4j.jakartaWeb)
    runtime(tdsLibs.log4j.slf4j2Impl)
    runtime(tdsLibs.springsecurity.config)
    runtime(tdsLibs.springsecurity.web)
    runtime(tdsLibs.ucar.bufr)
    runtime(tdsLibs.ucar.cdmImage)
    runtime(tdsLibs.ucar.cdmMcidas)
    runtime(tdsLibs.ucar.cdmRadial)
    runtime(tdsLibs.ucar.cdmS3)
    runtime(tdsLibs.ucar.cdmZarr)
    runtime(tdsLibs.ucar.netcdf4)
    runtime(tdsLibs.unidata.libblosc2Native)
  }
}

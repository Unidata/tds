/*
 * Copyright (c) 2026 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

plugins { id("platform-conventions") }

description = "A Bill of Materials (BOM) for TDS plugins to utilize."

extra["project.title"] = "Bill of Materials for THREDDS Data Server (TDS) Plugins"

// allow references to other BOMs
javaPlatform.allowDependencies()

dependencies {
  api(platform(project(":tds-platform")))
  constraints {
    // TODO - pull out what is needed from TDS into a new gradle subproject
    // to make a lighter weight dependency for plugin implementations to target
    api(project(":tds"))
  }
}

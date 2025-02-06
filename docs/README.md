# Documentation

All gradle commands shown below must be run from the top level of the TDS repository.

## Requirements

In addition to Java, Docker is now required for building the TDS docsets.

## Jekyll-based documentation

All docsets are published in the Nexus raw repository `docs-tds`.
They are published with the following structure:

`tds/<version indicator>/docset/page.html`

where version indicator will be a version string of the form `major.minor`.
If the version is also the current stable release, the documentation will also be published under the version indicator of `current`.

The TDS has four docsets:
* TDS Administrator's Guide (`adminguide/`)
* TDS Developer's Guide (`devguide/`)
* TDS Quick Start Guide (`quickstart/`)
* TDS User's Guide (`usersguide/`)

The four docsets above do share some content, which can be found in the directory `shared/`.
For more information, see [shared/README.md](shared/README.md).
      

### Gradle Tasks

* Build
    * `adminguide`: `./gradlew :docs:buildAdminGuide`
    * `devguide`: `./gradlew :docs:buildDevGuide`
    * `quickstart`: `./gradlew :docs:buildQuickstart`
    * `userguide`: `./gradlew :docs:buildUserGuide`

* Serve
    * same as `build*` tasks above, but using `serve*` (e.g. `./gradlew :docs:serveUserGuide`)
    * useful for live editing, as changes to .md files will be picked up and content regenerated on the fly
    * note that only one docset can be served at a time
    * when finished serving a doc set, run `./gradlew :docs:stopServe` to shutdown the docker container hosting the server

* Build all Jekyll-based sites (shortcut for building all jekyll-based documentation sets)
    * `./gradlew :docs:buildAllJekyllSites`

* Publish to nexus
    * same as `build*` tasks above, but using `publishAsVersioned*` or `publishAsCurrent*` (e.g. `./gradlew :docs:PublishAsVersionedUserGuide`)
    * use `./gradlew :docs:publishAllJekyllSitesAsVersioned` or `./gradlew :docs:publishAllJekyllSitesAsCurrent` to publish all Jekyll-based docsets to nexus

* Remove from nexus
    * same as `build*` tasks above, but using `deleteVersioned*FromNexus` or `deleteCurrent*FromNexus` (e.g. `./gradlew :docs:deleteVersionedUserGuideFromNexus`)
    * use `./gradlew :docs:deleteAllJekyllSitesVersionedFromNexus` or `./gradlew :docs:deleteAllJekyllSitesCurrentFromNexus` to remove all Jekyll-based docsets from nexus

## Convenience tasks for managing all documentation at once

* Build
    * `./gradlew :docs:build`

* Publish to nexus
    * `./gradlew :docs:publishAllDocsAsVersioned` or `./gradlew :docs:publishAllDocsAsCurrent`

* Remove from nexus
    * `./gradlew :docs:deleteAllDocsVersionedFromNexus` or `./gradlew :docs:deleteAllDocsCurrentFromNexus`

## Delete Tasks

When running a `delete` task, `dryRun` mode will be used by default for safety.
A summary of what would be removed from nexus will be shown in the console, but nothing will be removed from the server.
To actually delete the files from nexus, be sure to set the `dryRun` property to false when running gradle, like so:

~~~shell
./gradlew -PdryRun=false :docs:deleteAllDocsVersionedFromNexus
~~~

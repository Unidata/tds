# Documentation

All gradle commands shown below must be run from the top level of the TDS repository.

## Requirements

In addition to Java, Docker is now required for building the TDS docsets.

## Jekyll-based documentation

The TDS has three docsets:
* TDS Administrator's Guide (`adminguide/`)
* TDS Quick Start Guide (`quickstart/`)
* TDS User's Guide (`usersguide/`)

The three docsets above do share some content, which can be found in the directory `shared/`.
For more information, see [shared/README.md](shared/README.md).

### Gradle Tasks

* Build individual docsets
    * `adminguide`: `./gradlew :docs:buildAdminGuide`
    * `quickstart`: `./gradlew :docs:buildQuickstart`
    * `userguide`: `./gradlew :docs:buildUserGuide`

* Serve
    * same as `build*` tasks above, but using `serve*` (e.g. `./gradlew :docs:serveUserGuide`)
    * useful for live editing, as changes to .md files will be picked up and content regenerated on the fly
    * note that only one docset can be served at a time
    * when finished serving a doc set, run `./gradlew :docs:stopServe` to shutdown the docker container hosting the server

* Build all Jekyll-based docsets
    * `./gradlew :docs:buildAllJekyllSites`

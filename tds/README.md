## THREDDS Data Server

Test directories:
* `test/`: unit and integration tests (mock tests, which do not require the spin-up of an embedded server)
* `freshInstallTest/`: tests related to the bootstrapping process of the TDS configuration directory (`tds.content.root.path`).
* `integrationTests`: integration tests that require an embedded server (managed by the `gretty` plugin)

Both `freshInstallTest` and `integrationTests` tests use the gradle plugin `gretty` to spin-up an embedded TDS server.

Note: `README.txt` is a template that is populated and put into the `.war` file by the build process.

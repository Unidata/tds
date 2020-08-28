`root/`: Contains script plugins that should be applied only to the root project.

`any/`: Contains script plugins that can be applied to any project, including the root. 
Most of what we need in the `any/` directory can be found in these two script plugins:

  1. `java-internal`: Applied to non-published java project (`:testUtil`)
  Uses the following plugins:
     * [java](https://docs.gradle.org/current/userguide/java_plugin.html)
     * `gradle/any/javadoc.gradle`
  2. `java-test-only`: Applied to non-published, test only projects
  Uses the following plugins:
     * `java-internal.gradle`
     * `testing.gradle`
     * `coverage.gradle`
  3. `java.gradle`: Not directly applied to any project, but inherited by `java-published.gradle` and `war.gradle`
  Uses the following plugins:
     * `java-test-only.gradle`
     * `javadoc.gradle`
     * `archiving.gradle`
  4. `java-published.gradle`: Used by java projects that are tested and published.
  Uses the following plugins:
     * `java.gradle`
     * `publishing.gradle`
  5. `war.gradle`: Used by web projects that are tested and published (publishes both jar and war files).
  Uses the following plugins:
     * `java.gradle`
     * `publishing.gradle`
     * [war](https://docs.gradle.org/current/userguide/war_plugin.html)

`gretty/`: Contains configuration files related to the `gretty` plugin (logging, cert for testing, etc.)

**TODO:** There are still a few Gradle things left to do, but at least we're fully functional at this point.

1. Address any issues in our plugin scripts identified by gradle in terms of Gradle 7 compatibility.

   ~~~
   Deprecated Gradle features were used in this build, making it incompatible with Gradle 7.0.
   Use '--warning-mode all' to show the individual deprecation warnings.
   See https://docs.gradle.org/6.5.1/userguide/command_line_interface.html#sec:command_line_warnings
   ~~~

   This is more about doing proactive maintenance with our gradle infrastructure.
   I do not want to be in the position of trying to jump three major versions again :-)

2. Find new dependency license checker
   The license plugin we used in the past does not seem to work with the java-library and java-platform plugins.
   Sad times.

3. Start using `cdm-test-utils` and refactor the `:testUtil` subproject to have only what builds on the new `cdm-test-utils` package.

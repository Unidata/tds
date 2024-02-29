@echo off
rem ENVARS for Tomcat
set CATALINA_HOME=\usr\local\tomcat
set CATALINA_BASE=\usr\local\tomcat
set JAVA_HOME=\usr\local\java

rem TDS specific ENVARS
rem Define where the TDS content directory will live
rem THIS IS CRITICAL and there is NO DEFAULT - the TDS will not start without this.
set CONTENT_ROOT=data\content
set CONTENT_ROOT_PATH=-Dtds.content.root.path="%CONTENT_ROOT%"

rem Set java prefs related variables (used by the wms service, for example)
set JAVA_PREFS_ROOTS=-Djava.util.prefs.systemRoot="%CONTENT_ROOT%\thredds\javaUtilPrefs" -Djava.util.prefs.userRoot="%CONTENT_ROOT%\thredds\javaUtilPrefs"

rem Some commonly used JAVA_OPTS settings:
set NORMAL=-Xmx4096m -Xms512m
set HEAP_DUMP=-XX:+HeapDumpOnOutOfMemoryError
set HEADLESS=-Djava.awt.headless=true

rem Standard setup.
set JAVA_OPTS=%CONTENT_ROOT_PATH% %JAVA_PREFS_ROOTS% %NORMAL% %HEAP_DUMP% %HEADLESS% 

rem Uncomment the following line if you want to display the JAVA_OPTS
rem echo %JAVA_OPTS%
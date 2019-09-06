#!/usr/bin/env bash

if [[ $TASK == "test-tds" ]]
then
    echo "Testing TDS PR"
    CONTENT_ROOT="-Dtds.content.root.path=$TRAVIS_BUILD_DIR/tds/src/test/content"
    DOWNLOAD_DIR="-Dtds.download.dir=/tmp/download"
    UPLOAD_DIR="-Dtds.upload.dir=/tmp/upload"
    SYSTEM_PROPS="$CONTENT_ROOT $DOWNLOAD_DIR $UPLOAD_DIR"

    $TRAVIS_BUILD_DIR/gradlew $SYSTEM_PROPS --info --stacktrace testAll --refresh-dependencies
elif [[ $TASK == "spotless" ]]
then
    echo "Checking code style with spotless"
    $TRAVIS_BUILD_DIR/gradlew spotlessJavaCheck
else
    echo "I do not understand TASK = ${TASK}"
    echo "TASK must be either test-tds or spotless"
fi
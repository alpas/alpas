#!/usr/bin/env bash
# Copied from Ktorm's repo
last_commit=$(git log --pretty=format:'%d' | grep HEAD)

if [[ ${last_commit} =~ "tag: " ]]
then
    echo "New version found, auto uploading archives to bintray..."
    ./gradlew bintrayUpload --stacktrace
else
    echo "New version not found, exiting..."
fi

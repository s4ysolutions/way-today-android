#!/usr/bin/env bash
echo "GS"
echo $GS
echo $GS > "$APPCENTER_SOURCE_DIRECTORY/app/google-services.json"
echo "cat"
cat "$APPCENTER_SOURCE_DIRECTORY/app/google-services.json"
echo $GS |sed 's/\\"/"/g' > "$APPCENTER_SOURCE_DIRECTORY/app/google-services.json"
echo "sed"
cat "$APPCENTER_SOURCE_DIRECTORY/app/google-services.json"

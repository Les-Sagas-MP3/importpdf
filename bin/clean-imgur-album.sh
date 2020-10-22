#!/bin/bash

set -x

BEARER_TOKEN=''
ACCESS_TOKEN=''
ALBUM_HASH=''

# Request list of images in album
curl --location --request GET "https://api.imgur.com/3/album/${ALBUM_HASH}/images" \
--header "Authorization: Bearer ${BEARER_TOKEN}" \
--header "accesstoken=${ACCESS_TOKEN}; is_authed=1" | jq '.data' | jq -r '.[].id' > listIds.txt

# Remove return carriages in temp file
tr -d '\r' < listIds.txt > listIdsWithoutCarriage.txt

# For each line of temp file, delete according image
while read p; do
  curl --location --request DELETE "https://api.imgur.com/3/image/${p}" --header "Authorization: Bearer ${BEARER_TOKEN}"
done < listIdsWithoutCarriage.txt
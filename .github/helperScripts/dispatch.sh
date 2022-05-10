#!/bin/bash

USAGE='Usage:
./dispatch.sh -t|--tag <docker-tag> \
	-u|--tdsurl <threddsWarURL> \
	-k|--token <access-token>'

while [[ $# > 0 ]]
do
	key=$1
	case $key in
		-t|--tag)
			TAG=$2
			shift
			;;
		-u|--tdsurl)
			URL=$2
			shift
			;;
		-k|--token)
			TOKEN=$2
			shift
			;;
		*)
			echo "Option $1 not recognized"
			echo $USAGE
			exit 1
	esac
	shift
done

if [[ -z "$TAG" ||  -z "$URL" || -z "$TOKEN" ]];
then
	echo "Invalid number of arguments"
	echo $USAGE
	exit 1
fi

curl \
	-X POST \
	-H "Accept: application/vnd.github.v3+json" \
	-H "Authorization: token ${TOKEN}" \
	https://api.github.com/repos/unidata/thredds-docker/dispatches \
	-d '{"event_type":"upstreamTDS","client_payload":{"tag":"'"$TAG"'", "threddsWarURL":"'"$URL"'"}}'

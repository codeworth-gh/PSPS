#!/bin/bash

# Another deploy version. This one takes the new version and the server, and generates a 
# backup of the original server, and copies relevant config files from the old version to the new.

if (( $# != 2 )); then
    echo "usage: ./deploy.sh <server name> <new version>"
    exit 1
fi

echo deploying from $2
echo into $1

BAK=$1-bak
mv $1 $BAK
mv $2 $1
mv $BAK/conf/server.conf $1/conf/
mv $BAK/conf/logback.xml $1/conf/
mkdir $1/logs

chown -R play $1
chgrp -R play $1
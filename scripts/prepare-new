#!/bin/bash

if [ $# -ne 2 ]
    then
        echo Usage: prepare-new new old
        echo   where: new - path to new play app
        echo   where: old - path to old play app, from which we copy config files.
        exit 1
fi

NEW=$1
OLD=$2

echo Preparing app $NEW based on app $OLD.

chown -R play $NEW
chgrp -R play $NEW
cp $OLD/conf/server.conf $NEW/conf/
cp $OLD/conf/logback.xml $NEW/conf/


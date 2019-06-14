#!/bin/sh
APP_NAME=gateway-zuul-server
DEPLOY_DIR=/data/wwwroot/boot

cd `dirname $0`
cd `pwd`/../
SOURCE_DIR=`pwd`

if [ ! -d $SOURCE_DIR/src ] ; then
    echo "Cannot find Source Code. DIR: "$SOURCE_DIR/src 2>&2
    exit 1
fi

echo '******* srouce dir: '$SOURCE_DIR
echo '******* delpoy dir: '$DEPLOY_DIR
echo '******* delpoy app: '$APP_NAME

WWW=$DEPLOY_DIR/$APP_NAME

if [ ! -d $WWW ] ; then
    mkdir $WWW -p
    mkdir $WWW/bak -p
else
    cd $WWW
    mv $APP_NAME* $WWW/bak
fi

cd $SOURCE_DIR
echo '******* 1/3 pull code'
git pull

echo '******* 2/3 assemble jar'
./gradlew clean
./gradlew assemble


echo '******* 3/3 copy jar'
cd $WWW
cp $SOURCE_DIR/build/libs/$APP_NAME* ./
ls $APP_NAME*

echo '******* delpoy finished , already running list:'

ps -ef | grep $APP_NAME
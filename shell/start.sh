#!/bin/sh
APP_NAME=gateway-zuul-server
APP_PORT=11010
#APP_MODE=dev

DEPLOY_DIR=/data/wwwroot/boot

if [ ! -d $DEPLOY_DIR/$APP_NAME ] ; then
    echo "Cannot find delpoy dir: "$DEPLOY_DIR/$APP_NAME 2>&2
    exit 1
fi

while [ -n "$1" ]
do
    case "$1" in
        -h) echo "-m | --mode [required] enum: prod, test, dev"
            echo "-p | --port [optional] default: "$APP_PORT
            exit 1
            ;;
        -m|--mode)
            #echo "m2:"$2
            if [ x$2 != x ]; then
                APP_MODE=$2
                shift 2
            else 
                shift 1
            fi
            ;;
         -p|--port)
            #echo "p2:"$2
            if [ x$2 != x ]; then
                APP_PORT=$2
                shift 2
            else 
                shift 1
            fi
            ;;
        *) echo "$1 is not an option"
            exit 1
            ;;
    esac
done


if [ -z "$APP_MODE" ] ; then
    echo "Type: -m | --mode [required] enum: prod, test, dev"
    exit 1
fi

JAVA=$(which java)
if [ -z "$JAVA" ]; then
    echo "Cannot find a Java JDK. Please set either set JAVA or put java (>=1.8) in your PATH." 2>&2
    exit 1
fi


APP_DIR=$DEPLOY_DIR/$APP_NAME
cd $APP_DIR


JAR_NAME=`ls $APP_NAME*.jar`
if [ -z "$JAR_NAME" ] ; then
    echo "Cannot find jar." 2>&2
    exit 1
fi

JAR_FILE=$APP_DIR/$JAR_NAME

ps -ef | grep $APP_NAME | grep -v grep | awk '{print $2}' | xargs kill -9

echo "****** Starting ******************"
echo "jar : "$JAR_FILE
echo "port: "$APP_PORT
echo "mode: "$APP_MODE

JAVA_OPTS='-server -Xrs -Xmx1024m -Xms256m -Xmn256m -XX:PermSize=256m -Xss256k -XX:+DisableExplicitGC -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled -XX:+UseCMSCompactAtFullCollection -XX:LargePageSizeInBytes=128m -XX:+UseFastAccessorMethods -XX:+UseCMSInitiatingOccupancyOnly -XX:CMSInitiatingOccupancyFraction=70 -XX:SurvivorRatio=8 -Xverify:none -Djava.net.preferIPv4Stack=true -Djava.security.egd=file:/dev/./urandom -Dfile.encoding=UTF8 -Dlog4j.log.port=8001'

nohup java $JVM_OPTS -jar $JAR_FILE --spring.profiles.active=$APP_MODE --server.port=$APP_PORT >/dev/null 2>&1 &

echo "****** Start Finished ************"

ps -ef | grep $APP_NAME
#!/bin/sh
#Configure database
export MOKAI_DB_ENGINE=mysql

APP_NAME="mokai-web"
APP_LONG_NAME="Mokai Web"
MAIN_CLASS="org.mokai.web.admin.jogger.Main"
JAVA_OPTS="-Xmx600m"
PIDFILE="$HOME/$APP_NAME.pid"

# search for the 'java' command
if [ -z "$JAVACMD" ] ; then
  if [ -n "$JAVA_HOME"  ] ; then
      JAVACMD="$JAVA_HOME/bin/java"
  else
    JAVACMD=`which java`
  fi
fi

if [ ! -x "$JAVACMD" ] ; then
  echo "Error: JAVA_HOME is not defined correctly." 1>&2
  echo "  We cannot execute $JAVACMD" 1>&2
  exit 1
fi

# locate the script and move to the parent directory 
cd $(dirname $0)
cd ..

# set the classpath
CLASSPATH="$CLASSPATH:lib/*"

getpid() {
    if [ -f "$PIDFILE" ]
    then
        if [ -r "$PIDFILE" ]
        then
            pid=`cat "$PIDFILE"`
            if [ "X$pid" != "X" ]
            then
                # It is possible that 'a' process with the pid exists but that it is not the
                #  correct process.  This can happen in a number of cases, but the most
                #  common is during system startup after an unclean shutdown.
                # The ps statement below looks for the specific wrapper command running as
                #  the pid.  If it is not found then the pid file is considered to be stale.
                pidtest=`ps ax | grep "$MAIN_CLASS" | tail -1`
                if [ "X$pidtest" = "X" ]
                then
                    # This is a stale pid file.
                    rm -f "$PIDFILE"
                    echo "Removed stale pid file: $PIDFILE"
                    pid=""
                fi
            fi
        else
            echo "Cannot read $PIDFILE."
            exit 1
        fi
    fi
}

if [ "$1" = "start" ] ; then
        echo "Running $APP_LONG_NAME ..."
        shift
        getpid
        if [ "X$pid" = "X" ]
        then
            $JAVACMD $JAVA_OPTS -classpath $CLASSPATH $MAIN_CLASS  > /dev/null 2>&1 &
            echo $! > $PIDFILE
        else
            echo "$APP_LONG_NAME is already running."
            exit 1
        fi

elif [ "$1" = "stop" ] ; then
        echo "Stopping $APP_LONG_NAME ..."
        shift
        getpid
        if [ "X$pid" = "X" ]
        then
            echo "$APP_LONG_NAME was not running."
        else
            kill -15 `cat $PIDFILE`
            rm -rf $PIDFILE
        fi

elif [ "$1" = "run" ] ; then
        shift
        getpid
        if [ "X$pid" = "X" ]
        then
            $JAVACMD $JAVA_OPTS -classpath $CLASSPATH $MAIN_CLASS "$@"
        else
            echo "$APP_LONG_NAME is already running."
            exit 1
        fi

else
        echo "Usage:"
        echo "$APP_NAME (start|run|stop)"
        echo " start - start $APP_NAME in the background"
        echo " run - start $APP_NAME in the foreground"
        echo " stop - stop $APP_NAME"
        exit 0
fi

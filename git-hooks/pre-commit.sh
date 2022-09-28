#!/bin/bash

mvn spotless:apply
RESULT=$?

if [ "$RESULT" = 0 ] ; then
    echo "$(tput setf 2)[$(tput setf 3)SPOTLESS$(tput setf 2)]$(tput sgr0) Project formatting validated."
    exit 0
else
    echo 1>&2 "$(tput setf 2)[$(tput setf 3)SPOTLESS$(tput setf 2)]$(tput sgr0) At least one file is incorrectly formatted. Run 'mvn spotless:apply' to fix automatically."
    exit 1
fi
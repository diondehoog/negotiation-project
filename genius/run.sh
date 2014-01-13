#!/bin/bash
CURDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
FOLDER=$CURDIR/negotiator
FOLDER2=$CURDIR/negotiator/group7

if [ ! -d "$FOLDER" ]
then
mkdir $FOLDER
fi

if [ ! -e "$FOLDER2" ]
then
ln -s $CURDIR/../NegotiationAgent/bin/negotiator/* $FOLDER
fi

cd $CURDIR & java -jar negosimulator.jar

#!/bin/bash
# This shell can be used for a Jenkins post job after runing the mutation scan
# Can be extend to set all parameter for the jar to this shell. Currently only the minimum mutation coverage is mandatory.
# If the mutation coverage is below the minimum the Jenkins Job will fail

# Concat Pit Reports and publish to an specific directory. Only usefull when using multi module project

JARLOCATION=LocationToTheJar
PROJECTROOT=PathToTheRootOfYourProject
COPYTO=PathWhereToPublishThePITReport
PITDIRNAME=pit-reports

NOW=`date +"%Y-%m-%dT%T.%3N"`

echo "minimum coverage needed: " $1

if echo "$1" | egrep -q '^[0-9]+$'; then
    java -jar $JARLOCATION --projectRoot $PROJECTROOT --copyTo $COPYTO --pitDirName $PITDIRNAME --force true --minimumCoverage $1
    chmod 755 -R $COPYTO
else
    echo $NOW : Failure, "$0 requires minimum coverage number"
    exit 1
fi
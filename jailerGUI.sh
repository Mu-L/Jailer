#!/bin/sh

instdir=`dirname $0`
cd $instdir
 
LIB=lib

# JDBC-driver
# CP=$CP:<driver-jar>

# configuration files in the config directory
CP=$CP:config

# the libraries
CP=$CP:$LIB/junit.jar
CP=$CP:$LIB/log4j-api-2.17.2.jar
CP=$CP:$LIB/log4j-core-2.17.2.jar
CP=$CP:$LIB/log4j-slf4j-impl-2.17.2.jar
CP=$CP:$LIB/slf4j-api-1.7.25.jar
CP=$CP:$LIB/flatlaf-3.3.jar
CP=$CP:$LIB/args4j.jar
CP=$CP:$LIB/prefuse.jar
CP=$CP:$LIB/sdoc-0.5.0-beta.jar
CP=$CP:$LIB/liquibase-core-4.27.0.jar
CP=$CP:$LIB/commons-collections4-4.4.jar
CP=$CP:$LIB/commons-lang3-3.14.0.jar
CP=$CP:$LIB/commons-text-1.11.0.jar
CP=$CP:$LIB/opencsv-5.9.jar
CP=$CP:$LIB/snakeyaml-2.2.jar
CP=$CP:$LIB/jackson-core-2.16.1.jar
CP=$CP:$LIB/jackson-annotations-2.16.1.jar
CP=$CP:$LIB/jackson-databind-2.16.1.jar
CP=$CP:$LIB/jsqlparser-3.2.jar
CP=$CP:$LIB/tablefilter-swing-5.3.1.jar
CP=$CP:jailer.jar

# echo $CP

java -Xmx1200M -Djava.util.Arrays.useLegacyMergeSort=true -cp $CP net.sf.jailer.ui.JailerUI "$@"


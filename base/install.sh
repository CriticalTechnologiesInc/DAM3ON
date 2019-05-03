#!/bin/bash

# Critical Technologies Inc. (CTI)
# Adam Wiethuechter <adam.wiethuechter@critical.com>
# 2019-05-02

display_usage()
{
	echo
	echo "Usage:"
	echo "	sudo $0 <scripts_dir> <source_dir> <imports_dir>"
	echo "	$0 -h"
	echo
	echo "Options:"
	echo " -h, --help : Display usage instructions"
	echo
	echo "Developer Comments:"
	echo "The argument directories all should be full paths with their trailing '/' removed."
	echo "If no options are given to the script it will default its directories to the following:"
	echo 
	echo "	scripts_dir : current directory + '/scripts'"
	echo "	source_dir  : current directory + '/usr-local-src'"
	echo "	imports_dir : current directory + '/imports'"
	echo
}

case $1 in 
	-h|--help)
		display_usage
		;;
	*)
		start=$SECONDS
		if [[ $# == 1 ]]
		then
			SCRIPTS_DIR=$(pwd)/scripts
			SOURCE_DIR=$(pwd)/usr-local-src
			IMPORTS_DIR=$(pwd)/imports
		else
			SCRIPTS_DIR=$2
			SOURCE_DIR=$3
			IMPORTS_DIR=$4
		fi
		sudo chmod -R +x $SCRIPTS_DIR/*

		# =================================================
		# DAM3ON Build
		# =================================================

		$SCRIPTS_DIR/dmcryptluks.sh
        $SCRIPTS_DIR/java8.sh $SOURCE_DIR
        $SCRIPTS_DIR/tomcat.sh $SOURCE_DIR $IMPORTS_DIR
        $SCRIPTS_DIR/postfix.sh
        $SCRIPTS_DIR/mailman.sh $SOURCE_DIR

		# ================================================
		# Append other installer scripts after this point!
		# ================================================
		# souce /path/to/script [options]

		end=$SECONDS
		echo "Finished script $0 on $(date)"
		echo "Duration: $((end-start)) seconds."
		;;
esac
#!/bin/bash

# Critical Technologies Inc. (CTI)
# Adam Wiethuechter <adam.wiethuechter@critical.com>
# 2019-05-02

display_usage()
{
	echo
	echo "Usage:"
	echo "	sudo $0 <scripts_dir> <source_dir> <imports_dir> <configs_dir>"
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
	ehco "	configs_dir : current directory + '/configs'"
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
			CONFIGS_DIR=$(pwd)/configs
		else
			SCRIPTS_DIR=$2
			SOURCE_DIR=$3
			IMPORTS_DIR=$4
			CONFIGS_DIR=$5
		fi

		# ===================================================
		# External Installers
		# ===================================================
		# unzip $IMPORTS_DIR/<package>.zip
		# source $IMPORTS_DIR/<package>/path/to/script [options]

		unzip $IMPORTS_DIR/cti-base_build_*.zip

		sudo chmod -R +x $IMPORTS_DIR/*.sh
		source $IMPORTS_DIR/cti-base_build_*/install.sh

		# =================================================
		# DAM3ON Build
		# =================================================

		sudo chmod -R +x $SCRIPTS_DIR/*
		$SCRIPTS_DIR/dmcryptluks.sh
        $SCRIPTS_DIR/java8.sh $SOURCE_DIR
        $SCRIPTS_DIR/tomcat.sh $SOURCE_DIR $CONFIGS_DIR
        $SCRIPTS_DIR/postfix.sh
        $SCRIPTS_DIR/mailman.sh $SOURCE_DIR

		end=$SECONDS
		echo "Finished script $0 on $(date)"
		echo "Duration: $((end-start)) seconds."
		;;
esac
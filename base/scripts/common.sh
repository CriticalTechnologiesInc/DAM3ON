#!/bin/bash

# Critical Technologies Inc. (CTI)
# Adam Wiethuechter <adam.wiethuechter@critical.com>
# 2019-05-02

install_header()
{
	local PACKAGE_NAME=$1
	echo
	echo "Starting $PACKAGE_NAME installation..."
	echo

	# read will ret 0 if all good, unless if timeout is exceed
	read -n 1 -s -r -t 10 -p "Press any key to skip this installation..."
	if [ $? == 0 ]
	then
		echo
		echo
		echo "Skipping $PACKAGE_NAME installation..."
		echo
		exit 0
	fi
	echo
	echo
}

install_footer()
{
	local PACKAGE_NAME=$1
	echo
	echo "Finished $PACKAGE_NAME installation..."
	echo
}
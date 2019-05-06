#!/bin/bash
#
# Critical Technologies Inc. (CTI)
# Adam T. Wiethuechter <adam.wiethuechter@critical.com>
# 2019-05-04
#

source scripts/common.sh

display_usage()
{
	echo
	echo "Usage:"
	echo "	$0 --base --pdp"
	echo "	$0 --base --pep"
	echo "	$0 --base-full --pdp"
	echo "	$0 --base-full --pep"
	echo "	$0 -h"
	echo
	echo "Options:"
	echo "	-h, --help  : Display usage instructions"
	echo "	--base      : Include base (w/ no imports folder) into installer package"
	echo "	--base-full : Include full base folder into installer package"
	echp "	--pdp       : Include PDP files into installer package"
	echo "	--pep       : Include PEP fiels into installer package"
	echo
}

# check for dos2unix
if ! dpkg -s dos2unix >/dev/null 2>&1; then
	sudo apt install dos2unix
fi

# grab the commit id
commit=$(git log --oneline -1 -- .)
package_name=dam3on_$(date +%Y%m%d)_${commit:0:7}

# create the package folder and add version info
mkdir $package_name
echo $(date +%Y%m%d)_${commit:0:7} > $package_name/VERSION

# copy all the standard stuff in
cp -rv base/ $package_name/
cp -rv scripts/ $package_name
cp -rv support-docs/ $package_name/
cp -v INSTALL $package_name/
# cp -v install.sh $package_name/

case $1 in
	--base)
		# remove the cti-base-build
		rm $package_name/imports/*
		;; 
	-h|--help)
		display_usage
		;;
	*)
		raise_error "Unknown argument: $1"
		display_usage
		;;
esac

case $2 in
	--pdp)
		cp -rv db+pdp/ $package_name/
		;;
	--pep)
		cp -rv pep/ $package_name/
		cp -rv git-annex/ $package_name/
		;;
	--client)
		cp -rv client/ $package_name/
	*)
		raise_error "Unknown argument: $2"
		display_usage
		;;
esac

# run dos2unix to clean up anything nasty
find $package_name/ -type f -print0 | xargs -0 dos2unix

# zip it up and remove building folder to leave zip
zip -r $package_name.zip $package_name/
rm -rf $package_name/
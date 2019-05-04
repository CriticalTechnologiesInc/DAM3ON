#!/bin/bash

# Critical Technologies Inc. (CTI)
# Adam Wiethuechter <adam.wiethuechter@critical.com>
# April 21, 2019

source common.sh

PACKAGE_NAME="DMCRYPTLUKS"

install_header $PACKAGE_NAME

sudo apt install -y cryptsetup

install_footer $PACKAGE_NAME
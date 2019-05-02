#!/bin/bash

# Critical Technologies Inc. (CTI)
# Adam Wiethuechter <adam.wiethuechter@critical.com>
# April 21, 2019

sudo cp $1/ucspi-tcp-0.88.tar.gz /usr/local/src/
cd /usr/local/src/
sudo tar -zxvf ucspi-tcp-0.88.tar.gz
cd ucspi-tcp-0.88
sudo mv conf-cc conf-cc.orig
sudo cp $2/conf-cc.uscpitcp conf-cc
sudo make
sudo make setup check

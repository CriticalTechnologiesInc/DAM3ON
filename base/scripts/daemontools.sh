#!/bin/bash

# Critical Technologies Inc. (CTI)
# Adam Wiethuechter <adam.wiethuechter@critical.com>
# April 21, 2019

sudo cp $1/daemontools-0.76.tar.gz /usr/local/src/
cd /usr/local/src/
sudo tar -zxvf daemontools-0.76.tar.gz
cd admin/daemontools-0.76/src/
sudo mv conf-cc conf-cc.orig
sudo cp $2/conf-cc.dt conf-cc
cd /usr/local/src/admin/daemontools-0.76/
sudo ./package/install
sudo cp $2/rclocal.dt /etc/rc.local
sudo chmod a+x /etc/rc.local
sudo apt install -y csh
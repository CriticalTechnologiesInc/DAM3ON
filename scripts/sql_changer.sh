#!/bin/bash
#
# Critical Technologies Inc. (CTI)
# Adam T. Wiethuechter <adam.wiethuechter@critical.com>
# 2019-05-03
#

IP_ADDR=$1
NET_MASK=$2
PASSWD=$3

sudo sed -i -e "s/192.168.211.170/$IP_ADDR/g" ../db+pdp/sql/users.sql
sudo sed -i -e "s/255.255.255.0/$NET_MASK/g" ../db+pdp/sql/users.sql
sudo sed -i -e "s/CriticalAardvark#25/$PASSWD/g" ../db+pdp/sql/users.sql
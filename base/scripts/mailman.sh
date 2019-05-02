#!/bin/bash

# Critical Technologies Inc. (CTI)
# Adam Wiethuechter <adam.wiethuechter@critical.com>
# April 21, 2019

sudo cp $1/mailman-2.1.15-with-pgp-smime_2012-08-28-patch.tar.gz /usr/local/src/
sudo cp $1/GnuPGInterface-0.3.2.tar.gz /usr/local/src/

sudo groupadd mailman
sudo useradd -c "GNU Mailman" -s /no/shell -d /no/home -g mailman mailman
cd /usr/local
sudo mkdir mailman
sudo chown root:mailman mailman/
sudo chmod a+rx,g+ws mailman/

cd /usr/local/src/
sudo tar -zxvf GnuPGInterface-0.3.2.tar.gz
cd GnuPGInterface-0.3.2
sudo python setup.py build
sudo python setup.py install
sudo apt install -y python-pip
sudo -H pip install --upgrade pip
sudo -H pip install python-gnupg

cd /usr/local/src/
sudo tar -zxvf mailman-2.1.15-with-pgp-smime_2012-08-28-patch.tar.gz
cd mailman-2.1.15-with-pgp-smime_2012-08-28-patch
sudo ./configure
sudo make
sudo make install
sudo cp -rp /usr/local/lib/python2.7/dist-packages/* /usr/local/mailman/pythonlib/
cd /usr/local/mailman/bin
sudo ./check_perms -f
sudo ./check_perms -f
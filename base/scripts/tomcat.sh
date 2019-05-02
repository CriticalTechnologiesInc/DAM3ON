#!/bin/bash

# Critical Technologies Inc. (CTI)
# Adam Wiethuechter <adam.wiethuechter@critical.com>
# April 21, 2019

sudo cp $1/apache-tomcat-8.5.40.tar.gz /usr/local/src/
cd /usr/local/src/
sudo tar -zxvf apache-tomcat-8.5.40.tar.gz
sudo -s <<EOF
cd /usr/local/src/apache-tomcat-8.5.40/webapps
rm -rf *
cd /usr/local/src/apache-tomcat-8.5.40/bin
mv startup.sh startup.sh.orig
cp $2/startup.sh .
chmod 750 startup.sh
EOF
cp $2/tomcatstart.sh /usr/local/bin
sudo chmod a+x /usr/local/bin/tomcatstart.sh

# Add all users to tomcat group
oldIFS=$IFS
getent passwd | while IFS=: read -r name password uid gid gecos home shell; do
    # only users that own their home directory
    if [ -d "$home" ] && [ "$(stat -c %u "$home")" = "$uid" ]; then
        sudo usermod -aG adm "$name"
    fi
done
IFS=$oldIFS
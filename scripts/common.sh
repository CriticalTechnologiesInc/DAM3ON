#!/bin/bash
#
# Critical Technologies Inc. (CTI)
# Adam T. Wiethuechter <adam.wiethuechter@critical.com>
# 2019-05-02
#

raise_error() 
{
	local error_message="$@"
	echo "${error_message}" 1>&2;
}

tomcat_config()
{
	local TOMCAT_VERSION=$1
	sudo -s <<EOF
	if [[ ! -f /usr/local/src/apache-tomcat-$TOMCAT_VERSION/conf/server.xml.orig ]]
	then
		mv /usr/local/src/apache-tomcat-$TOMCAT_VERSION/conf/server.xml /usr/local/src/apache-tomcat-$TOMCAT_VERSION/conf/server.xml.orig
	fi
	cp usr-local-tomcat/server.xml /usr/local/src/apache-tomcat-$TOMCAT_VERSION/conf/
	cd /usr/local/src/apache-tomcat-$TOMCAT_VERSION/bin
	sudo ./catalina.sh stop
	sudo ./catalina.sh start
EOF
}

apache_config()
{
	if [[ ! -f /etc/apache2/sites-enabled/000-default-le-ssl.conf.orig ]]
	then
		sudo mv /etc/apache2/sites-enabled/000-default-le-ssl.conf /etc/apache2/sites-enabled/000-default-le-ssl.conf.orig
	fi
	sudo cp etc-apache2/000-default-le-ssl.conf /etc/apache2/sites-enabled/
	if [[ ! -f /etc/libapache2-mod-jk/workers.properties.orig ]]
	then
		sudo mv /etc/libapache2-mod-jk/workers.properties /etc/libapache2-mod-jk/workers.properties.orig
	fi
	sudo cp etc-libapache2/workers.properties /etc/libapache2-mod-jk/
	if [[ ! -f /etc/apache2/ports.conf.orig ]]
	then
		sudo mv /etc/apache2/ports.conf /etc/apache2/ports.conf.orig
	fi
	sudo cp etc-apache2/ports.conf /etc/apache2/
}
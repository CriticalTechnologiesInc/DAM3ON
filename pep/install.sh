#!/bin/bash

display_usage()
{
	echo
	echo "Usage:"
	echo "	sudo $0 -e <pep-server-fqdn> <pep-server-alias> <apache-tomcat-version>"
	sudo "	sudo $0 -i <pdp-key-package-location> <pdp-server-fqdn> <pdp-server-alias> <pep-server-alias>"
	sudo "	sudo $0 -s <pdp-key-package-location> <pep-server-alias> <pdp-server-fqdn> <pep-server-fqdn>"
	echo "	sudo $0 -k <pep-server-alias>"
	echo "	sudo $0 -u <pdp-server-fqdn> <pep-server-fqdn>"
	echo "	sudo $0 -c <apache-tomcat-version>"
	echo "	$0 -h"
	echo
	echo "Options:"
	echo "	-e, --pep           : Install Policy Enformement Point (PEP)"
	echo "	-i, --key-import    : Import XACML keys from PDP into PEP"
	echo "	-s, --import-mysql  : Import MySQL keys from PDP into PEP"
	echo "	-k, --keys-pep      : Create Key Package for PEP"
	echo "	-u, --update        : Update configuration files for PEP"
	echo "	-c, --clean-pep     : Clean up after PEP install"
	echo
}

raise_error() 
{
	local error_message="$@"
	echo "${error_message}" 1>&2;
}

clean_pep_install_debris()
{
	echo "Policy Enformement Point Install Clean Up..."
	local TOMCAT_VERSION=$1
	sudo rm /etc/apache2/ports.conf.orig
	sudo -s <<EOF
	rm /usr/local/src/apache-tomcat-$TOMCAT_VERSION/conf/server.xml.orig
EOF
	sudo rm /etc/libapache2-mod-jk/workers.properties.orig
	sudo rm /etc/apache2/sites-enabled/000-default-le-ssl.conf.orig
	echo "PEP cleanup complete!"
}

make_pep_key_package()
{
	echo "Creating Policy Enformement Point Key Package..."
	local SERVER_ALIAS=$1
	cd ~/Downloads
	mkdir keys-$SERVER_ALIAS-pep/
	# xacml keys
	sudo cp /etc/webxacml/$SERVER_ALIAS.crt keys-$SERVER_ALIAS-pep/
	zip -r keys-$SERVER_ALIAS-pep.zip keys-$SERVER_ALIAS-pep/
	echo "Key package for PEP created in ~/Downloads!"
}

apache_config()
{
	if [[ ! -f /etc/apache2/sites-enabled/000-default-le-ssl.conf.orig ]]
	then
		sudo mv /etc/apache2/sites-enabled/000-default-le-ssl.conf /etc/apache2/sites-enabled/000-default-le-ssl.conf.orig
	fi
	sudo cp etc-apache2/000-default-le-ssl.conf /etc/apache2/sites-enabled/

	# change fqdns to match server
	sudo sed -i -e "s/ctidev4.critical.com/$1/g" /etc/apache2/sites-enabled/000-default-le-ssl.conf

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

import_xacml_pdp_keys()
{
	echo "Importing PDP XACML keys...
	"
	local PDP_KEYS=$1
	local PDP_FQDN=$2
	local PDP_ALIAS=$3
	local PEP_ALIAS=$4

	cd /etc/webxacml
	sudo keytool -import -trustcacerts -alias $PDP_FQDN -file $PDP_KEYS$PDP_ALIAS.crt -keystore pep-$PEP_ALIAS.jks

	echo "PDP XACML Keys imported!"
	echo "Please now run the following command: sudo $0 -s <pdp-key-package-location> <pep-server-alias> <pdp-server-fqdn> <pep-server-fqdn>"
}

import_mysql_keypairs()
{
	echo "Importing PDP MySQL keypairs..."
	local PDP_KEYS=$1
	local PEP_ALIAS=$2
	local PDP_FQDN=$3
	local PEP_FQDN=$4

	cd ~
	sudo openssl req -sha1 -newkey rsa:2048 -days 3650 -nodes -keyout $PEP_ALIAS.pem > req-$PEP_ALIAS.pem
	sudo openssl rsa -in $PEP_ALIAS.pem -out $PEP_ALIAS.pem
	sudo openssl x509 -sha1 -req -in req-$PEP_ALIAS.pem -extfile ${PDP_KEYS}v3.ext -days 3650 -CA ${PDP_KEYS}cacert.pem -CAkey ${PDP_KEYS}mysql-ca-key.pem -set_serial 01 > cert-$PEP_ALIAS.pem
	sudo mkdir /etc/webxacml/mysql_keys
	sudo cp ${PDP_KEYS}cacert.pem /etc/webxacml/mysql_keys
	sudo mv cert-$PEP_ALIAS.pem $PEP_ALIAS.pem /etc/webxacml/mysql_keys

	cd /usr/lib/jvm/java-8-oracle/jre/lib/security
	sudo keytool -import -trustcacerts -alias $PDP_FQDN -file /etc/webxacml/mysql_keys/cacert.pem -keystore cacerts

	cd /etc/webxacml/mysql_keys/
	sudo openssl x509 -outform DER -in cert-$PEP_ALIAS.pem -out $PEP_ALIAS.cert
	sudo keytool -import -file $PEP_ALIAS.cert -keystore keystore -alias $PEP_FQDN
	cd /etc/webxacml/mysql_keys
	sudo chmod 666 *.pem *.cert

	echo "PDP MySQL keypairs imported!"
	echo "Please now run the following command: sudo $0 -k <pep-server-alias>"
}

update_config()
{
	sudo sed -i -e "s/ctidev2.critical.com/$1/g" /etc/webxacml/config.xml
	sudo sed -i -e "s/ctidev4.critical.com/$2/g" /etc/webxacml/config.xml
}

install_pep()
{
	echo "Policy Enformement Point Install..."

	local FQDN=$1
	local ALIAS=$2
	local VERSION=$3

	# apt installs
	sudo apt install -y libapache2-mod-jk libapache2-mod-php7.2 php7.2-mysql php-xml

	# logs
	sudo touch /var/log/xacml3.log
	sudo chmod 666 /var/log/xacml3.log

	# webxacml files
	if [[ ! -d /etc/webxacml ]]
	then
		sudo mkdir /etc/webxacml/
	fi
	sudo cp -r etc-webxacml/* /etc/webxacml/

	# web files
	sudo cp -r var-www-html/* /var/www/html/

	# war files
	sudo cp WAR/* /usr/local/src/apache-tomcat-$VERSION/webapps/

	# web servers config
	apache_config $FQDN
	tomcat_config $VERSION
	sudo service apache2 restart

	# Generate XACML keypair
	cd /etc/webxacml/
	sudo keytool -genkey -alias $FQDN -keyalg RSA -keystore pep-$ALIAS.jks -keysize 2048
	sudo keytool -export -alias $FQDN -file $ALIAS.crt -keystore pep-$ALIAS.jks

	echo "PEP installation complete!"
	echo "Please now run the following command: sudo $0 -i <pdp-key-package-location> <pdp-server-fqdn> <pdp-server-alias> <pep-server-alias>"
}

echo "DAM3ON Policy Enforcement Point (PEP) Installer Stating"
start=$SECONDS

case $1 in
	-e|--pep)
		install_pep $2 $3 $4
		;;
	-k|--keys-pep)
		make_pep_key_package $2
		;;
	-c|--clean-pep)
		clean_pep_install_debris $2
		;;
	-i|--import-keys)
		import_xacml_pdp_keys $2 $3 $4 $5
		;;
	-s|--import-mysql)
		import_mysql_keypairs $2 $3 $4 $5
		;;
	-u|--update)
		update_config $2 $3
		;;
	-h|--help)
		display_usage
		;;
	*)
		raise_error "Unknown argument: $1"
		display_usage
		;;
esac
end=$SECONDS
echo "DAM3ON Policy Enforcement Point (PEP) Installer Exiting"

echo "Finished script $0 on $(date)"
echo "Duration: $((end-start)) seconds."
#!/bin/bash
#
# Critical Technologies Inc. (CTI)
# Adam T. Wiethuechter <adam.wiethuechter@critical.com>
# 2019-05-02
#

generate_xacml_keys()
{
	local FQDN=$2
	local ALIAS=$3

	cd /etc/webxacml
	if [[ $1 -eq "-e" ]]
	then
		sudo keytool -genkey -alias $FQDN -keyalg RSA -keystore pep-$ALIAS.jks -keysize 2048
		sudo keytool -export -alias $FQDN -file $ALIAS.crt -keystore pep-$ALIAS.jks
	else
		sudo keytool -genkey -alias $FQDN -keyalg RSA -keystore pdp-$ALIAS.jks -keysize 2048
		sudo keytool -export -alias $FQDN -file $ALIAS.crt -keystore pdp-$ALIAS.jks
	fi
}

make_pdp_key_package()
{
	echo "Creating Policy Enformement Point Key Package..."
	local SERVER_ALIAS=$1
	cd ~/Downloads
	mkdir keys-$SERVER_ALIAS-pdp/
	# xacml keys
	sudo cp /etc/webxacml/$SERVER_ALIAS.crt keys-$SERVER_ALIAS-pdp/
	# mysql keys
	sudo cp /etc/mysql/cacert.pem /etc/mysql/mysql-ca-key.pem /etc/mysql/v3.ext keys-$SERVER_ALIAS-pdp/
	zip -r keys-$SERVER_ALIAS-pdp.zip keys-$SERVER_ALIAS-pdp/
	echo "Key package for PDP created in ~/Downloads!"
}

import_xacml_pdp_keys() # tested man on pdp = good
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
	echo "Please now run the following command: sudo $0 -ki -m <pdp-key-package-location> <pep-server-alias> <pdp-server-fqdn> <pep-server-fqdn>"
}

make_pep_key_package() # good - copy from old pep script
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

import_xacml_pep_keys() # good - copy from old pep script
{
	echo "Importing PEP XACML keys...
	"
	local PEP_KEYS=$1
	local PEP_FQDN=$2
	local PEP_ALIAS=$3
	local PDP_ALIAS=$4

	cd /etc/webxacml
	sudo keytool -import -trustcacerts -alias $PEP_FQDN -file $PEP_KEYS$PEP_ALIAS.crt -keystore pdp-$PDP_ALIAS.jks

	echo "PEP XACML Keys imported!"
	echo "Please now run the following command: sudo $0 -ki -m <pdp-key-package-location> <pep-server-alias> <pdp-server-fqdn> <pep-server-fqdn>"
}

import_mysql_pep_keypairs() # good - copy from old pep script
{
	echo "Importing PDP MySQL keypairs..."
	local PDP_KEYS=$1
	local PEP_ALIAS=$2
	local PDP_FQDN=$3
	local PEP_FQDN=$4

	# SECTION 2
	cd ~
	sudo openssl req -sha1 -newkey rsa:2048 -days 3650 -nodes -keyout $PEP_ALIAS.pem > req-$PEP_ALIAS.pem
	sudo openssl rsa -in $PEP_ALIAS.pem -out $PEP_ALIAS.pem
	sudo openssl x509 -sha1 -req -in req-$PEP_ALIAS.pem -extfile ${PDP_KEYS}v3.ext -days 3650 -CA ${PDP_KEYS}cacert.pem -CAkey ${PDP_KEYS}mysql-ca-key.pem -set_serial 01 > cert-$PEP_ALIAS.pem
	sudo mkdir /etc/webxacml/mysql_keys
	sudo cp ${PDP_KEYS}cacert.pem /etc/webxacml/mysql_keys
	sudo mv cert-$PEP_ALIAS.pem $PEP_ALIAS.pem /etc/webxacml/mysql_keys

	# SECTION 3
	cd /usr/lib/jvm/java-8-oracle/jre/lib/security
	sudo keytool -import -trustcacerts -alias $PDP_FQDN -file /etc/webxacml/mysql_keys/cacert.pem -keystore cacerts

	# SECTION 4
	cd /etc/webxacml/mysql_keys/
	sudo openssl x509 -outform DER -in cert-$PEP_ALIAS.pem -out $PEP_ALIAS.cert
	sudo keytool -import -file $PEP_ALIAS.cert -keystore keystore -alias $PEP_FQDN
	cd /etc/webxacml/mysql_keys
	sudo chmod 666 *.pem *.cert

	echo "PDP MySQL keypairs imported!"
	echo "Please now run the following command: sudo $0 -ke <pep-server-alias>"
}

import_mysql_pdp_keypairs()
{
	echo "Importing PDP MySQL keypairs..."

	local ALIAS=$1
	local FQDN=$2

	# SECTION 2
	cd ~
	sudo openssl req -sha1 -newkey rsa:2048 -days 3650 -nodes -keyout $ALIAS.pem > req-$ALIAS.pem
	sudo openssl rsa -in $PEP_ALIAS.pem -out $PEP_ALIAS.pem
	sudo openssl x509 -sha1 -req -in req-$ALIAS.pem -extfile /etc/mysql/v3.ext -days 3650 -CA /etc/mysql/cacert.pem -CAkey /etc/mysql/mysql-ca-key.pem -set_serial 01 > cert-$ALIAS.pem
	sudo mkdir /etc/webxacml/mysql_keys
	sudo cp /etc/mysql/cacert.pem /etc/webxacml/mysql_keys
	sudo mv cert-$ALIAS.pem $ALIAS.pem /etc/webxacml/mysql_keys

	# SECTION 3
	cd /usr/lib/jvm/java-8-oracle/jre/lib/security
	sudo keytool -import -trustcacerts -alias $FQDN -file /etc/webxacml/mysql_keys/cacert.pem -keystore cacerts

	# SECTION 4
	cd /etc/webxacml/mysql_keys/
	sudo openssl x509 -outform DER -in cert-$ALIAS.pem -out $ALIAS.cert
	sudo keytool -import -file $ALIAS.cert -keystore keystore -alias $FQDN
	cd /etc/webxacml/mysql_keys
	sudo chmod 666 *.pem *.cert

	echo "PDP MySQL keypairs imported!"
	echo "Please now run the following command: sudo $0 -ke <pdp-server-alias>"
}

generate_mysql_keys()
{
	echo "Generating MySQL keypair..."

	# SECTION 1
	cd /etc/mysql
	sudo openssl genrsa 2048 > mysql-ca-key.pem
	sudo openssl req -sha1 -new -x509 -nodes -days 3650 -key mysql-ca-key.pem > cacert.pem

	cat > /etc/mysql/v3.ext << ENDOFFILE
	authorityKeyIdentifier=keyid,issuer
	basicConstraints=CA:FALSE
	keyUsage = digitalSignature, nonRepudiation, keyEncipherment, dataEncipherment
ENDOFFILE

	sudo openssl req -sha1 -newkey rsa:2048 -days 3650 -nodes -keyout server-key.pem > server-req.pem
	sudo openssl rsa -in server-key.pem -out server-key.pem
	sudo openssl x509 -sha1 -req -in server-req.pem -extfile v3.ext -days 3650 -CA cacert.pem -CAkey mysql-ca-key.pem -set_serial 01 > server-cert.pem
	sudo chmod 666 *.pem

	echo "MySQL keypair generated!"
}
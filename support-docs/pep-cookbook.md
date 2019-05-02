## Introduction

This document is one module for installing the DA3MON system. Here you will be guided in installing the Policy Enforcement Point (PEP) for the DA3MON Architecture.

You will need 'sudo' permissions during this install process.

## Prerequisites

It is assumed that the DA3MON base build instructions have been completed before this documentation. Please refer to "DMNBld.docx" for details.

While not required it is also expected that the PDP has been setup at this time as well following the instructions found in 'pdp+db/pdp+db-cookbook.txt'. The following files are required from that server to perform some sections. Specifically the 'cacert.crt', 'mysql-ca-key.pem', 'v3.ext' file from SECTION 9 and exported the '.crt' file from SECTION 2.

## XACML Log

Create XACML log and set permissions:
```
sudo touch /var/log/xacml3.log
sudo chmod 666 /var/log/xacml3.log
```

## SSL Certificates Auto Renewal

### Step 1
We must edit our crontab to add a job.
```
sudo crontab -e
```

Add to the bottom of the crontab file:
```
25 6 * * * certbot renew --post-hook "service apache2 restart"
```

---

The first two numbers in the above command represent the minute and hour fields. The above will run everyday at 6:25AM. To relieve the burden it is best practice to change this value for each server so they all do not renew at the exact same time.

---

## Install webxacml, Web and WAR Files

### Step 1
Perform the following action to copy the files to the correct destination:
```
sudo mkdir /etc/webxacml/
sudo cp -r etc-webxcaml/* /etc/webxcaml/
```
This will copy everything (including directory structure and file into the destination).

### Step 2
Move files to the www directory.
```
sudo cp -r var-www-html/* /var/www/html/
```

### Step 3
We now move the WAR files into location.
```
sudo cp WAR/* /usr/local/src/apache-tomcat-<version>/webapps/
```

---

NOTE: The WAR programs load config.xml only once on startup for efficiency. If changes are made, tomcat must be restarted for them to take effect.

---

## Generate XACML Keypair

Please refer to the support document 'xacml-keypairs.txt' for details on how to configure the XACML keys for DAM3ON systems. This file can be found in the '../support-doc/' directory of this deliverable.

For this build you will want to follow SECTION 1 as well as SECTION 2. If you already have a PDP key to import into the PEP also follow SECTION 3.

## Apache Reverse Proxy & SSL VirtualHost

This section will setup Apache to reverse proxy for Tomcat.

### Step 1
Save the original conf file and open the new one.
```
sudo cp /etc/apache2/sites-enabled/000-default-le-ssl.conf /etc/apache2/sites-enabled/000-default-le-ssl.conf.orig
sudo nano /etc/apache2/sites-enabled/000-default-le-ssl.conf
```

#### Step 1A
Add the following into the IfModule block:
```
<VirtualHost *:8080>
	ServerAdmin webmaster@localhost
	ServerSignature Off

	JKMount /* ajp13_worker

	#LogLevel info ssl:warn

	ErrorLog ${APACHE_LOG_DIR}/error.log
	CustomLog ${APACHE_LOG_DIR}/access.log combined

	SSLCertificateFile /etc/letsencrypt/live/<FQDN>/fullchain.pem
	SSLCertificateKeyFile /etc/letsencrypt/live/<FQDN>/privkey.pem
	Include /etc/letsencrypt/options-ssl-apache.conf
	ServerName <FQDN>
</VirtualHost>
```

---

NOTE: Be sure to fill in the FQDN fields. An example would be to replace them with "ctidev2.critical.com" (without the quotes).

---

#### Step 1B
Add the follow to the VirtualHost \*:443 block
```
ServerSignature Off
ErrorDocument 404 /access_denied.html
ErrorDocument 403 /access_denied.html

<Directory "/var/www/html/api/includes">
        Options -Indexes
        Require all denied
</Directory>
<Directory "/var/www/html/media">
        Options -Indexes
</Directory>
<Directory "/var/www/html/pap/includes">
        Options -Indexes
        Require all denied
</Directory>
<Directory "/var/www/html/data_mining">
        AllowOverride All
</Directory>
<Directory "/var/www/html/test">
        AllowOverride All
</Directory>
<Directory "/var/www/html/upload_data_mining">
        Options -Indexes
        AllowOverride All
</Directory>
<Directory "/var/www/html/certs">
        Options -Indexes
        AllowOverride All
</Directory>
<Directory "/var/www/html/pcerts">
        Options -Indexes
        AllowOverride All
</Directory>
```

### Step 2
Install and configure libapache2.
```
sudo apt install libapache2-mod-jk
sudo cp /etc/libapache2-mod-jk/workers.properties /etc/libapache2-mod-jk/workers.properties.orig
sudo nano /etc/libapache2-mod-jk/workers.properties
```

#### Step 2A
Change the two lines:
```
workers.tomcat_home=/home/share/tomcat8
to
workers.tomcat_home=/usr/local/src/apache-tomcat-<version>
```
```
worker.ajp13_worker.host=localhost
to
worker.ajp13_worker.host=127.0.0.1
```

---

Be sure to set the version to your installed Tomcat version. As of writing this documentation current version is 8.5.38.

---

### Step 3
```
sudo cp /usr/local/src/apache-tomcat-<version>/conf/server.xml /usr/local/src/apache-tomcat-<version>/conf/server.xml.orig
sudo nano /usr/local/src/apache-tomcat-<version>/conf/server.xml
```

---

You may not have access to the "conf" directory initially. To gain access do perform a `sudo -s`. Upon completing Steps 3A-3B exit back out of 'root'

---

#### Step 3A
Comment out the following (line 69):
```
<Connector port="8080" protocol="HTTP/1.1" connectionTimeout="20000" redirectPort="8443" />
```

#### Step 3B
Change the following:
```
<Connector port="8009" protocol="AJP/1.3" redirectPort="8443" />
to:
<Connector port="8009" address="127.0.0.1" protocol="AJP/1.3" redirectPort="8443" />
```

---

Be sure to exit 'root' here.

---

### Step 4
```
sudo cp /etc/apache2/ports.conf /etc/apache2/ports.conf.orig
sudo nano /etc/apache2/ports.conf
```

#### Step 4A
Add the following to the "<IfModule ssl_module>" block.
```
Listen 8080
```

### Step 5
Restart the service
```
cd /usr/local/src/apache-tomcat-8.5.38/bin
sudo ./catalina.sh stop
sudo ./catalina.sh start
sudo service apache2 restart
```

---

By restarting the Tomcat service first we avoid the issue of Apache not loading due to port 8080 already being used.

---

## MySQL Keypair Configuration

Please refer to the support document 'mysql-keypairs.txt' for details on how to configure MySQL keypairs for DAM3ON systems. This file can be found in the '../support-doc/' directory of this deliverable.

For this build you will follow SECTION's 2-4.

## Web File Configuration

Check the following files to make sure they are configured correctly:

### Step 1
Check every line that it confirms with your configuration settings of the
system.
```
/var/www/html/api/includes/includes.php
```

Be sure that the paths for the cacert, client_cert and client_key are pointing to the correct file directory and names. You will be required to know the exact IP address of the server hosting the database (which should also be the PDP).
```
/var/www/html/pap/includes/includes.php
```

Be sure that the paths for the cacert, client_cert and client_key are pointing to the correct file directory and names.

You will be required to know the FQDN of the server hosting the database (which should also be the PDP). This is the policy_location variable.

The mport must also match the database port being used.

Another thing to change will be the passwords for the users listed in the file.


### Step 2
Check the "domain_self" variable (line 2) for its accuracy:
```
/var/www/html/login/js/cti.js
```

The variable should be pointing to the FQDN of the PEP box being configured.

### Step 3
Check that the "action" argument on the "<form>" tag is correct:
```
/var/www/html/submitcap/index.php
```

The line to check is line #151 (as of 02-18-2019). The URL should point to the server hosting the mailman-top instance.

## Install PHP Dependencies
```
sudo apt install libapache2-mod-php7.2 php7.2-mysql
sudo service apache2 restart
```

## webxacml Configuration

### Step 1
Check the following file:
```
sudo nano /etc/webxacml/config.xml
```

Look for any items, such as FQDNs and passwords that are not set to the configuration you have set.

Specifically any place where file paths are marked should be checked for file name and paths. Also any instance of PDP or PEP should be checked to be sure they are referenced by their alias which was set. URLs are another item that must be check as well.

---

You may have to return to this file once a PDP is setup to fill in any missing fields related to them. In the preloaded 'config.xml' located in 'pep/webxacml/' anywhere "ctidev4.critical.com" is located is referencing a PEP server. "ctidev2.critical.com" likewise represents a PDP server.

---

## Testing and Finishing Up

### Step 1
It is always best to reboot and restart the services after performing the
install process. Also install any missing packages from above:
```
sudo apt install php-xml
sudo service apache2 restart
sudo reboot
```

### Step 2
To check everything is up and running go to the following link:
```
https://<FQDN>
```

You should be presented with the DA3MON login screen. We can now run a few tests to determine if everything is working as intended.

#### Step 2A
See 'support-docs/chrome-pgp-keys.txt' on how to setup your PGP keys for access in Chrome web browser.

#### Step 2B
Once your keys are setup head to the PEP. Hit "Secure Access" and use the resource "authonly-pgp".

Enter your email address you used setting up your PGP keys. A text box will appear. Click the little icon of a paper and pen in it to open Mailvelope.

Hit "Options" in the bottom left corner, check the box for "Sign message with key:" and hit the "Sign Only" button. Enter your password as prompted.

Once you return to the main DAM3ON page hit "Request Access". If successful you should see a webpage with the text "This is an example of a resource that only requires PGP authentication."

### Step 3
Once it is confirmed the system is up and running we can clean away some debris.
```
sudo rm /etc/apache2/ports.conf.orig
sudo rm /usr/local/src/apache-tomcat-<version>/conf/server.xml.orig
sudo rm /etc/libapache2-mod-jk/workers.properties.orig
sudo rm /etc/apache2/sites-enabled/000-default-le-ssl.conf.orig
```

### Step 4
Create a key package to be moved to other servers.
```
cd ~/Downloads
mkdir keys-<server_alias>-pep/
sudo cp /etc/webxacml/<server_alias>.crt
zip -r keys-<server_alias>-pep.zip keys-<server_alias>-pep/
```

The server_alias.crt key is from XACML Keypair export.
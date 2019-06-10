# Policy Decision Point (PDP)

## Introduction

This document is one module for installing the DA3MON system.

Here you will be guided in installing the Policy Decision Point (PDP) and the database for DA3MON Architecture.

You will need 'sudo' permissions during this install process.

## Prerequisites

It is assumed that the DA3MON base build instructions have been completed before this documentation.

Please refer to "DMNBld.docx" for details.

## Install webxacml and WAR files

### Step 1
Perform the following action to copy the files to the correct destination:
```
sudo mkdir /etc/webxacml/
sudo cp -r etc-webxcaml/* /etc/webxcaml/
```
This will copy everything (including directory structure and files into the
destination).

### Step 2
Once done we must now touch a new log file for XACML and set permissions:
```
sudo touch /var/log/xacml3.log
sudo chmod 666 /var/log/xacml3.log
```

### Step 3
We now move the WAR files into location.
```
sudo cp WAR/* /usr/local/src/apache-tomcat-<version>/webapps/
```

```
NOTE: The WAR programs load config.xml only once on startup for efficiency. If changes are made, tomcat must be restarted for them to take effect.
```

## Generate XACML Keypair

Please refer to the support document 'xacml-keypairs.txt' for details on how to configure the XACML keys for DAM3ON systems. This file can be found in the '../support-doc/' directory of this deliverable.

For this build you will want to follow SECTION 1 as well as SECTION 2.

If you already have PEP keys to import into the PDP also follow SECTION 3.

## Install MySQL

### Step 1
Install various dependencies for MySQL via apt.
```
sudo apt install mysql-server mysql-client
```

### Step 2
We now perform a secure installation of MySQL.
```
sudo mysql_secure_installation
```

#### Step 2A
For the prompts answer in the following order (an example of the prompts
is below for reference):
```
no, yes, yes, yes, yes
```
The first "no" is asking about having a password check module be used
which we do not require so it can be skipped.

You will next be prompted to give a password for MySQL's "root" user.
This is very hard to change, so make a note of it as it will be used later.

Example of output:
```
Remove anonymous users? (Press y|Y for Yes, any other key for No) : y
Success.

Normally, root should only be allowed to connect from
'localhost'. This ensures that someone cannot guess at
the root password from the network.

Disallow root login remotely? (Press y|Y for Yes, any other key for No) : y
Success.

By default, MySQL comes with a database named 'test' that
anyone can access. This is also intended only for testing,
and should be removed before moving into a production
environment.

Remove test database and access to it? (Press y|Y for Yes, any other key for No) : y
 - Dropping test database...
Success.

 - Removing privileges on test database...
Success.

Reloading the privilege tables will ensure that all changes
made so far will take effect immediately.

Reload privilege tables now? (Press y|Y for Yes, any other key for No) : y
Success.

All done!
```

### Step 3
Next we alter the MySQL configuration to fit our needs.
Copy the old configuration to save it in case something breaks.
```
sudo cp /etc/mysql/mysql.conf.d/mysqld.cnf /etc/mysql/mysql.conf.d/mysqld.cnf.orig
sudo nano /etc/mysql/mysql.conf.d/mysqld.cnf
```

#### Step 3A
We will change the "bind-address" field in file we opened:
```
bind-address = 127.0.0.1
```
to
```
bind-address = 0.0.0.0
```

#### OPTIONAL: Step 3B
You are free to change the port number (i.e. 4655). Be sure to open this port on the machines local firewall and the network firewall.

## PHP and Other Dependencies

### Step 1
First we need to add some older package lists to get a dependency:
```
sudo nano /etc/apt/sources.list
```

Add the following lines:
```
deb http://us.archive.ubuntu.com/ubuntu/ xenial-updates main restricted universe multiverse
deb http://us.archive.ubuntu.com/ubuntu/ xenial main restricted universe multiverse
deb http://us.archive.ubuntu.com/ubuntu/ xenial-security main restricted universe multiverse
```

### Step 2
From apt:
```
sudo apt install php7.2 libapache2-mod-php7.2 libmysqlclient-dev
```

From pip:
```
sudo pip install xmltodict tweepy pillow pyteaser facebook-sdk BeautifulSoup flask mysqlclient
```

## PHPMyAdmin [OPTIONAL - Highly Recommended]

Please refer to the support document 'phpmyadmin.txt' for details on how to install phpMyAdmin keys for DAM3ON systems. This file can be found in the '../support-doc/' directory of this deliverable.

## SSL Certificates Auto Renewal

### Step 1
We must edit our crontab to add a job.
```
sudo crontab -e
```

####  Step 1A
Add to the bottom of the crontab file:
```
25 6 * * * certbot renew --post-hook "service apache2 restart"
```

The first two numbers in the above command represent the minute and hour fields. The above will run everyday at 6:25AM.

To relieve the burden it is best practice to change this value for each server so they all do not renew at the exact same time.

## Apache Reverse Proxy
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

NOTE: Be sure to fill in the <FQDN> fields. An example would be to replace them with "ctidev2.critical.com" (without the quotes).

#### Step 1B
Remove the "VirtualHost \*:433" block.

#### Step 1C
Remove "VirtualHost \*:80" block from 'default.conf'.
```
sudo cp /etc/apache2/sites-enabled/000-default.conf /etc/apache2/sites-enabled/000-default.conf.orig
```

NOTE: By just renaming the file it removes it from Apache2's view of the configuration.

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
worker.ajp13_worker.host=127.0.0.1
```
to:
```
workers.tomcat_home=/usr/local/src/apache-tomcat-<version>
worker.ajp13_worker.host=localhost
```

Be sure to set the <version> to your installed Tomcat version. As of writing this documentation current version is 8.5.38.

### Step 3
```
sudo cp /usr/local/src/apache-tomcat-<version>/conf/server.xml /usr/local/src/apache-tomcat-<version>/conf/server.xml.orig
sudo nano /usr/local/src/apache-tomcat-<version>/conf/server.xml
```

You may not have access to the "conf" directory initially. To gain access do perform a 'sudo -s'. Upon completing Steps 3A-3B exit back out of 'root'

#### Step 3A
Comment out the following (line 69):
```
<Connector port="8080" protocol="HTTP/1.1" connectionTimeout="20000" redirectPort="8443" />
```

To comment out the line wrap it in '<!--' and '-->' tags. Like this:
```
<!--
<Connector port="8080" protocol="HTTP/1.1" connectionTimeout="20000" redirectPort="8443" />
-->
```

#### Step 3B
Change the following:
```
<Connector port="8009" protocol="AJP/1.3" redirectPort="8443" />
```
to:
```
<Connector port="8009" address="127.0.0.1" protocol="AJP/1.3" redirectPort="8443" />
```

Be sure to exit 'root' here.

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
Restart the services
```
cd /usr/local/src/apache-tomcat-<version>/bin
sudo ./catalina.sh stop
sudo ./catalina.sh start
sudo a2enmod cgid
sudo service apache2 restart
```

By restarting the Tomcat service first we avoid the issue of Apache not loading due to port 8080 already being used.

## Database Setup

Please refer to the support document 'database-setup.txt' for details on how to configure the database for DAM3ON systems. This file can be found in the '../support-doc/' directory of this deliverable.

## MySQL Keypair Configuration

Please refer to the support document 'mysql-keypairs.txt' for details on how to configure MySQL keypairs for DAM3ON systems. This file can be found in the '../support-doc/' directory of this deliverable.

For this build you will follow all sections in the document in the order documented.

## Configure MySQL

### Step 1
Edit the following file:
```
sudo cp /etc/mysql/mysql.conf.d/mysqld.cnf /etc/mysql/mysql.conf.d/mysqld.cnf.orig
sudo nano /etc/mysql/mysql.conf.d/mysqld.cnf
```

#### Step 1A
Add this and/or uncomment to bottom. Make sure paths are correct to where you actually put the keys (the same keys created in SECTION 9).
```
ssl-ca=/etc/mysql/cacert.pem
ssl-cert=/etc/mysql/server-cert.pem
ssl-key=/etc/mysql/server-key.pem
```

### Step 2
Restart the service.
```
sudo service mysql restart
```

## Configure and Run webxacml

### Step 1
Backup and open the following for edit:
```
sudo cp /etc/webxacml/mod-db-api.py /etc/webxacml/mod-db-api.py.orig
sudo nano /etc/webxacml/mod-db-api.py
```

#### Step 1A
Modify the "passwd" argument on line 29 to reflect the password that was set for "file_user". Also make sure the database port number also reflects what has been set.

### Step 2
Check the following file:
```
sudo nano /etc/webxacml/config.xml
```

Look for any items, such as FQDNs and passwords that are not set to the configuration you have set.

Specifically any place where file paths are marked should be checked for file name and paths. Also any instance of PDP or PEP should be checked to be sure they are referenced by their alias which was set. URLs are another item that must be check as well.

You may have to return to this file once a PEP is setup to fill in any missing fields related to them. In the preloaded 'config.xml' located in 'db+pdp/webxacml/' anywhere "ctidev4.critical.com" is located is referencing a PEP server. "ctidev2.critical.com" likewise represents a PDP server.

### Step 3
Install TPM Qoute Tools. A special version is included in this release that must be installed.

First get the packages we need:
```
sudo apt install tpm-tools libtspi1-dev
```

Build the tool:
```
tar -xf tpm-quote-tools-1.0.5.tar.gz
cd tpm-quote-tools-1.0.5/
./configure
make
sudo make install
```

Now downgrade libtspi1:
```
sudo apt install libtspi1=0.3.13-4
```

### Step 4
Run /etc/webxacml/mod-db-api.py in the background.

There are few different ways this can be done. Choose whatever one works best for you.

#### Step 4A
Using screen perform the following actions:
```
screen -S <name>
python mod-db-api.py
CTRL+A, D
```

The final command "CTRL+A, D" will detach you from that screen. To reattach run 'screen -r <name>'.

#### Step 4B
Run the program as a background process:
```
python mod-db-api.py &
```

#### Step 4C
Set to run on start up (and in the background):
```
sudo nano ~/rc.local
```

Add to the bottom of the file the following:
```
python mod-db-api.py &
```

## Testing and Finishing Up

### Step 1
It is always best to reboot and restart the services after performing the install process.
```
sudo reboot
```

### Step 2
To check everything is up and running go to the following link:
```
https://<FQDN>:8080/WebPDP/pdp
```

You should see displayed in your browser:
```
"you found me"
```

### Step 3
Once it is confirmed the system is up and running we can clean away somed debris.
```
sudo rm /etc/webxacml/mod-db-api.py.orig /
	/etc/apache2/ports.conf.orig /
	/usr/local/src/apache-tomcat-<version>/conf/server.xml.orig /
	/etc/libapache2-mod-jk/workers.properties.orig /
	/etc/apache2/sites-enabled/000-default-le-ssl.conf.orig /
	/usr/share/phpmyadmin/libraries/sql.lib.php.orig /
	/usr/share/phpmyadmin/libraries/plugin_interface.lib.php.orig
```

### Step 4
Create a key package to be moved to other servers.

You will want to export the 'cacert.pem', 'mysql-ca-key.pem' and 'v3.ext' files to other servers  that will be accessing the MySQL database. Do this by copying these files to a safe place to export off the server later:
```
cd ~/Downloads
mkdir keys-<server_alias>-pdp/
sudo cp /etc/mysql/cacert.pem /etc/mysql/mysql-ca-key.pem /etc/mysql/v3.ext keys-<server_alias<-pdp/
sudo cp /etc/webxacml/<server_alias>.crt
zip -r keys-<server_alias>-pdp.zip keys-<server_alias<-pdp/
```

The <server_alias>.crt key is from XACML Keypair export. All other files are from MySQL keypair creation.

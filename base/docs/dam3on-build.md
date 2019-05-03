# DAM3ON Base Build

## Prerequisites

Complete the [Minimal build](minimal-build.html) before attempting to run this installer.

## Script Command
```
./install.sh
```

If the scripts/, usr-local-src/ and import/ directories are located elsewhere (not in the base-build folder with the install script) then run the following:
```
./install.sh path/to/scripts path/to/usr-local-src path/to/imports
``` 

NOTE: that the final '/' on the directory paths are missing. *THIS IS INTENTIONAL.* If you autocomplete the path in your shell it will automatically add a '/' to the end - **REMOVE IT**. The scripts **WILL** fail if it is present!

## DAM3ON Specific Packages

This document shows the various manual things that may be needed after the DAM3ON installer has been run or how to update various packages.

It also documents specific things that may need to be done by the user duing the installer script being run.

Almost everything included in the DAM3ON Build is scripted to be installed using the various scripts in 'scripts/dam3on/'.
Please review those scripts for exact command details on installations.

### Java8
In April 2019 Oracle changed there terms. To get Java8 you will need to create an Oracle account (free) to access the downloads to the Java JDK/JRE.

This is the JDK/JRE CTI uses: https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html

You will need jdk8-8u211-linux-x64.tar.gz and jdk8-8u212-linux-x64.tar.gz from the above page. Move them into usr-local-src/ and the installer will take care of the rest.

If the package is updated you will have to go into the script to change version numbers as they are hardcoded into the script currently.

### Apache Tomcat

Add the line "/usr/local/bin/tomcatstart.sh &" into rc.local for it to start up during boot.

NOTE – default Tomcat port is 8080; web server visible at http://localhost:8080.

Add users to group to access files:
```
sudo usermod -aG adm <username>
```

### Postfix

Select "No configuration" during the install. To configure later use the following command:
```
sudo dpkg-reconfigure postfix
```

### Secure List Server (mailman)

Mailman is NOT configured during install. See other documentation to configure Mailman for your system.
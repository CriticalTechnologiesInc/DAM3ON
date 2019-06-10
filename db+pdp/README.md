# Policy Decision Point (PDP)

## Introduction

This document is one module for installing the DA3MON system. Here you will be guided in installing the Policy Decision Point (PDP) for the DA3MON Architecture as well as its associated database.

You will need 'sudo' permissions during this install process.

## Prerequisites

It is assumed that the DA3MON base build instructions have been completed before this documentation. Please refer to "DMNBld.docx" for details.

While not required it is also expected that the PEP has been setup at this time as well following the instructions found in 'pep/pep-cookbook.md'. The following files are required from that server to perform some sections.

## Installing

### Scripted Install

Unzip the `db+pdp.zip` deliverable, enter into the directory and run a `sudo chmod +x install.sh` to setup for the installer script.

Run the following install commands to install a PEP:
```
sudo ./pdp -i <pep-server-fqdn> <pep-server-alias> <apache-tomcat-version>
sudo ./pdp -ki -x <pdp-key-package-location> <pdp-server-fqdn> <pdp-server-alias> <pep-server-alias>
sudo ./pep -c <apache-tomcat-version>
```

The `<pep-key-package-location>` is from various PEP installs and should be moved onto your target host and be unzipped for this installation.

### Manual Install

See the [pdp-cookbook](../support-docs/pdp-cookbook.md) for detailed instruction on how to install the PEP.

## Post Installation

After installation is complete be sure to check the following files:
```
/etc/webxacml/config.xml
/etc/webxacml/mod-db-api.py
```

Check for any items, such as FQDNs and passwords that are not set to the configuration you have set. Specifically any place where file paths are marked should be checked for file name and paths. Also any instance of PDP or PEP should be checked to be sure they are referenced by their alias which was set. URLs are another item that must be check as well.

There are also a few specific things left that need to be done.

### PHPMyAdmin Login Fix

Due to PHPMyAdmin not allowing you to login via "root" user on the webpage we must elevate the privileges of the default "phpmyadmin" user to a global level.

Login to mysql in the terminal:
```
sudo mysql -u root -p
```

Run the following commands and exit back to main terminal:
```
GRANT ALL PRIVILEGES ON *.* TO 'phpmyadmin'@'localhost' WITH GRANT OPTION;
FLUSH PRIVILEGES;
exit
```

### Crontab Changes

Run a `sudo crontab -e` and add the following lines to the crontab:
```
25 6 * * * certbot renew --post-hook "service apache2 restart"
```

### Database Setup

See ../support-docs/database-setup.txt for details.

### Additions to rc.local

Open up `/etc/rc.local` and add the following line before the `exit 0`:
```
python mod-db-api.py &
```

Once all this is done perform a `sudo reboot` to bring the system up clean.

Be sure to copy the created key package (located in `~/Downloads`) onto the PEP server to import the keys during its install.

## Credits

[Critical Technologies Inc. (CTI)](https://www.critical.com/)

- **Jeremy Fields** - Development and documentation - fieldsjd@critical.com
- **Adam Wiethuechter** - Documentation - adam.wiethuechter@critical.com

Distributed under the GNU General Public License v2.0. See [LICENSE](LICENSE) for more information.
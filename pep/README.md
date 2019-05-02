# DAM3ON Policy Enforcement Point (PEP)

## Introduction

This document is one module for installing the DA3MON system. Here you will be guided in installing the Policy Enforcement Point (PEP) for the DA3MON Architecture.

You will need 'sudo' permissions during this install process.

## Prerequisites

It is assumed that the DA3MON base build instructions have been completed before this documentation. Please refer to "DMNBld.docx" for details.

While not required it is also expected that the PDP has been setup at this time as well following the instructions found in 'pdp+db/pdp+db-cookbook.txt'. The following files are required from that server to perform some sections. Specifically the 'cacert.crt', 'mysql-ca-key.pem', 'v3.ext' file from SECTION 9 and exported the '.crt' file from SECTION 2.

## Scripted Install

Unzip the `pep.zip` deliverable, enter into the directory and run a `sudo chmod +x install.sh` to setup for the installer script.

Run the following install commands to install a PEP:
```
sudo ./install.sh -e <pep-server-fqdn> <pep-server-alias> <apache-tomcat-version>
sudo ./install.sh -i <pdp-key-package-location> <pdp-server-fqdn> <pdp-server-alias> <pep-server-alias>
sudo ./install.sh -s <pdp-key-package-location> <pep-server-alias> <pdp-server-fqdn> <pep-server-fqdn>
sudo ./install.sh -k <pep-server-alias>
sudo ./install.sh -u <pdp-server-fqdn> <pep-server-fqdn>
sudo ./install.sh -c <apache-tomcat-version>
```

The `<pdp-key-package-location>` is from the PDP install and should be moved onto your target host and be unzipped for this installation.

### Post Installation

After installation is complete be sure to check the following files:
```
/var/www/html/api/includes/includes.php
/var/www/html/pap/includes/includes.php
/var/www/html/login/js/cti.js
/var/www/html/submitcap/index.php
/etc/webxacml/config.xml
```

Check for any items, such as FQDNs and passwords that are not set to the configuration you have set. Specifically any place where file paths are marked should be checked for file name and paths. Also any instance of PDP or PEP should be checked to be sure they are referenced by their alias which was set. URLs are another item that must be check as well.

Another item that must be done is adding to the crontab (using `sudo crontab -e`) the following line:
```
25 6 * * * certbot renew --post-hook "service apache2 restart"
```

With that perform a reboot (`sudo reboot`) of the system and the PEP should be ready to go.

Be sure to copy the created key package (located in `~/Downloads`) onto the PDP server to import the key.

## Manual Install

See the [pep-cookbook](pep-cookbook.md) for detailed instruction on how to install the PEP.

## Credits

[Critical Technologies Inc. (CTI)](https://www.critical.com/)

- **Jeremy Fields** - Development and documentation - fieldsjd@critical.com
- **Adam Wiethuechter** - Documentation - adam.wiethuechter@critical.com

Distributed under the GNU General Public License v2.0. See [LICENSE](LICENSE) for more information.
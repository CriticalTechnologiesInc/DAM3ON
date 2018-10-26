#!/bin/bash
# Author: Jeremy Fields
# Email: fieldsjd@critical.com
# Date: 12-19-16
#
# Description:
# This script will install the CTI native messaging host on a linux system.

#### Variables ######
chrome_ext_id="hmbmimahikfnkjjgopopfngigflklbij"
username=$SUDO_USER
chrome_nm="/home/$username/chrome_nm"
#### End variables ##

# Make sure it's run as sudo/root
if [ "$EUID" -ne 0 ]
  then echo "Must run as sudo. Aborting."
  exit
fi

# Make sure necessary programs are installed
command -v python >/dev/null 2>&1 || { echo "python is required but it's not installed.  Aborting." >&2; exit 1; }
command -v google-chrome >/dev/null 2>&1 || { echo "google-chrome is required but it's not installed.  Aborting." >&2; exit 1; }
command -v tpm_mkuuid >/dev/null 2>&1 || { echo "tpm_mkuuid is required but it's not installed (install trousers, tpm tools, libtspi-dev, and tpm quote tools.  Aborting." >&2; exit 1; }
command -v tpm_getquote >/dev/null 2>&1 || { echo "tpm_getquote is required but it's not installed (install trousers, tpm tools, libtspi-dev, and tpm quote tools.  Aborting." >&2; exit 1; }

if [ $(dpkg-query -W -f='${Status}' python-tk 2>/dev/null | grep -c "ok installed") -eq 0 ];
then
  echo "python-tk is missing! Exiting now"
  exit
fi

# Make sure that /home/USERNAME/chrome_nm/ exists
if [ ! -d $chrome_nm ];
    then echo "/home/$username/chrome_nm/ is missing! Exiting now"
    exit
fi

# Make sure native-messaging-example-host exists, and make it executable
if [ -f $chrome_nm/native-messaging-example-host ];
then
   chmod 777 $chrome_nm/native-messaging-example-host
else
   echo "Missing native-messaging-example-host! Exiting now."
   exit
fi

# Make sure that /etc/opt/chrome/native-messaging-hosts/ exists
if [ ! -d /etc/opt/chrome/native-messaging-hosts ];
  then
    mkdir -p /etc/opt/chrome/native-messaging-hosts
fi


# Build the manifest for the chrome extension
json="{
  \"name\": \"com.google.chrome.example.echo\",\n
  \"description\": \"CTI PCR Chrome Native Messaging API Host\",\n
  \"path\": \"/home/$username/chrome_nm/native-messaging-example-host\",\n
  \"type\": \"stdio\",\n
  \"allowed_origins\": [\n
	\"chrome-extension://$chrome_ext_id/\"\n
  ]\n
}"

# Put the chrome manifest in the right place w/ right permissions
echo -e $json > /etc/opt/chrome/native-messaging-hosts/com.google.chrome.example.echo.json
chmod o+r /etc/opt/chrome/native-messaging-hosts/com.google.chrome.example.echo.json


# The chrome specific stuff is done. Now onto TPM stuff...

# Make sure that /home/$username/chrome_nm/tpm/ exists
if [ ! -d $chrome_nm/tpm/ ];
  then
    mkdir $chrome_nm/tpm
    chown $username.$username $chrome_nm/tpm
fi

# Build blank sable manifest
sable="{\"files\":[]}"

# Put the sable manifest in the right place w/ right permissions
echo -e $sable > $chrome_nm/tpm/sable_manifest.json
chown $username.$username $chrome_nm/tpm/sable_manifest.json

if [ ! -f $chrome_nm/tpm/uuid.txt ];
  then
    cd $chrome_nm/tpm
    tpm_mkuuid uuid.txt
    chown $username.$username uuid.txt
fi

if [[ ! -f $chrome_nm/tpm/tpm_key.blob || ! -f $chrome_nm/tpm/tpm_key.pubk ]];
  then
   echo "Don't forget to make an AIK! You can do so by running the following commands."
   echo "cd /home/$username/chrome_nm/tpm && tpm_mkaik tpm_key.blob tpm_key.pubk"
   echo "cp tpm_key.pubk \$(sha256sum uuid.txt | cut -d\" \" -f 1).pubk"
   echo "--------------------------------------------------------------"
   echo "For smart card support, please run smart_card_install.sh"
fi

echo
echo "Success!"

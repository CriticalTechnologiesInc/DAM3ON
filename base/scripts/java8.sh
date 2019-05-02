#!/bin/bash

# Critical Technologies Inc. (CTI)
# Adam Wiethuechter <adam.wiethuechter@critical.com>
# April 21, 2019

sudo apt install -y software-properties-common
# sudo add-apt-repository ppa:webupd8team/java
# sudo apt update
# sudo apt install -y oracle-java8-installer
# sudo apt install -y oracle-java8-set-default

# source install java8 jdk
sudo cp $1/jdk-8u211-linux-x64.tar.gz /usr/local/src
cd /usr/local/src
sudo tar -zxvf jdk-8u211-linux-x64.tar.gz
# sudo tar -zxvf jdk-8u212-linux-x64.tar.gz

sudo mkdir -p /usr/lib/jvm/java-8-oracle
cd jdk1.8.0_211
sudo cp -r * /usr/lib/jvm/java-8-oracle

sudo update-alternatives --install /usr/bin/java java /usr/lib/jvm/java-8-oracle/bin/java 100
sudo update-alternatives --install /usr/bin/javac javac /usr/lib/jvm/java-8-oracle/bin/javac 100

sudo update-alternatives --display java
sudo update-alternatives --display javac
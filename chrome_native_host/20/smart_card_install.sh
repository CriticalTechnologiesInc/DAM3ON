sudo apt-get install pcscd libccid libpcsclite-dev libssl-dev libreadline-dev autoconf automake build-essential docbook-xsl xsltproc libtool pkg-config python-pip
sudo pip install pykcs11 pyopenssl
tar xfvz opensc-*.tar.gz
cd opensc-*
./bootstrap
./configure --prefix=/usr --sysconfdir=/etc/opensc
make
sudo make install
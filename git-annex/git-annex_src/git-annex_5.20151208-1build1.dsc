-----BEGIN PGP SIGNED MESSAGE-----
Hash: SHA256

Format: 3.0 (quilt)
Source: git-annex
Binary: git-annex
Architecture: any
Version: 5.20151208-1build1
Maintainer: Richard Hartmann <richih@debian.org>
Homepage: http://git-annex.branchable.com/
Standards-Version: 3.9.6
Vcs-Git: git://git.kitenet.net/git-annex
Testsuite: autopkgtest
Build-Depends: debhelper (>= 9), ghc (>= 7.4), cabal-install, libghc-mtl-dev (>= 2.1.1), libghc-missingh-dev, libghc-data-default-dev, libghc-hslogger-dev, libghc-pcre-light-dev, libghc-cryptonite-dev | libghc-cryptohash-dev (>= 0.11.0), libghc-sandi-dev, libghc-utf8-string-dev, libghc-aws-dev (>= 0.9.2-2~), libghc-conduit-dev, libghc-resourcet-dev, libghc-quickcheck2-dev, libghc-monad-control-dev (>= 0.3), libghc-exceptions-dev (>= 0.6), libghc-unix-compat-dev, libghc-dlist-dev, libghc-uuid-dev, libghc-json-dev, libghc-aeson-dev, libghc-ifelse-dev, libghc-bloomfilter-dev, libghc-edit-distance-dev, libghc-hinotify-dev [linux-any], libghc-stm-dev (>= 2.3), libghc-dbus-dev (>= 0.10.7) [linux-any], libghc-fdo-notify-dev (>= 0.3) [linux-any], libghc-yesod-dev (>= 1.2.6.1) [i386 amd64 arm64 armel armhf kfreebsd-i386 kfreebsd-amd64 powerpc ppc64el s390x], libghc-yesod-core-dev (>= 1.2.19) [i386 amd64 arm64 armel armhf kfreebsd-i386 kfreebsd-amd64 powerpc ppc64el s390x], libghc-yesod-form-dev (>= 1.3.15) [i386 amd64 arm64 armel armhf kfreebsd-i386 kfreebsd-amd64 powerpc ppc64el s390x], libghc-yesod-static-dev (>= 1.2.4) [i386 amd64 arm64 armel armhf kfreebsd-i386 kfreebsd-amd64 powerpc ppc64el s390x], libghc-yesod-default-dev (>= 1.2.0) [i386 amd64 arm64 armel armhf kfreebsd-i386 kfreebsd-amd64 powerpc ppc64el s390x], libghc-shakespeare-dev (>= 2.0.0) [i386 amd64 arm64 armel armhf kfreebsd-i386 kfreebsd-amd64 powerpc ppc64el s390x], libghc-clientsession-dev [i386 amd64 arm64 armel armhf kfreebsd-i386 kfreebsd-amd64 powerpc ppc64el s390x], libghc-warp-dev (>= 3.0.0.5) [i386 amd64 armel armhf armhf kfreebsd-i386 kfreebsd-amd64 powerpc ppc64el s390x], libghc-warp-tls-dev [i386 amd64 armel armhf armhf kfreebsd-i386 kfreebsd-amd64 powerpc ppc64el s390x], libghc-wai-dev [i386 amd64 armel armhf armhf kfreebsd-i386 kfreebsd-amd64 powerpc ppc64el s390x], libghc-wai-extra-dev [i386 amd64 armel armhf armhf kfreebsd-i386 kfreebsd-amd64 powerpc ppc64el s390x], libghc-dav-dev (>= 1.0) [i386 amd64 arm64 armel armhf kfreebsd-i386 kfreebsd-amd64 powerpc ppc64el s390x hurd-i386], libghc-persistent-dev [i386 amd64 arm64 armel armhf kfreebsd-i386 kfreebsd-amd64 powerpc ppc64el s390x hurd-i386], libghc-persistent-template-dev [i386 amd64 arm64 armel armhf kfreebsd-i386 kfreebsd-amd64 powerpc ppc64el s390x hurd-i386], libghc-persistent-sqlite-dev [i386 amd64 arm64 armel armhf kfreebsd-i386 kfreebsd-amd64 powerpc ppc64el s390x hurd-i386], libghc-esqueleto-dev [i386 amd64 arm64 armel armhf kfreebsd-i386 kfreebsd-amd64 powerpc ppc64el s390x hurd-i386], libghc-securemem-dev, libghc-byteable-dev, libghc-dns-dev, libghc-case-insensitive-dev, libghc-http-types-dev, libghc-http-conduit-dev, libghc-blaze-builder-dev, libghc-crypto-api-dev, libghc-network-multicast-dev, libghc-network-info-dev [linux-any kfreebsd-any], libghc-safesemaphore-dev, libghc-network-protocol-xmpp-dev (>= 0.4.3-1+b1), libghc-gnutls-dev (>= 0.1.4), libghc-xml-types-dev, libghc-async-dev, libghc-monad-logger-dev, libghc-feed-dev (>= 0.3.9.2), libghc-regex-tdfa-dev, libghc-tasty-dev (>= 0.7), libghc-tasty-hunit-dev, libghc-tasty-quickcheck-dev, libghc-tasty-rerun-dev, libghc-optparse-applicative-dev (>= 0.11.0), libghc-cabal-dev, libghc-torrent-dev, lsof [linux-any], ikiwiki, perlmagick, git (>= 1:1.8.1), rsync, wget, curl, openssh-client, git-remote-gcrypt (>= 0.20130908-6)
Package-List:
 git-annex deb utils optional arch=any
Checksums-Sha1:
 10d022ede77f515c8cb341a7b60b760df507ba8a 5309296 git-annex_5.20151208.orig.tar.xz
 2f86b2f6b5face7160d881eb467f43919bd87c26 74008 git-annex_5.20151208-1build1.debian.tar.xz
Checksums-Sha256:
 bcb8da2577b4be51fd30148f4b6a5b51f5853caffc4b35cb6e5173c7b2add569 5309296 git-annex_5.20151208.orig.tar.xz
 acc66e185d5d2a99b6af330d9c19d6cd91bd3c3b4ce48663ea392fd32f8e84aa 74008 git-annex_5.20151208-1build1.debian.tar.xz
Files:
 7ca1c49d1b17487da0b8081cad01a119 5309296 git-annex_5.20151208.orig.tar.xz
 30859e66c7a3d6c0122fce1870866574 74008 git-annex_5.20151208-1build1.debian.tar.xz

-----BEGIN PGP SIGNATURE-----
Version: GnuPG v1

iQIcBAEBCAAGBQJWxPXCAAoJEL1+qmB3j6b1iPEP/2bfVgEZecMtAkglzQze3d8i
ejhRkeVyHAsOumjkuYr3bcQfZB/dy+1Z5E1AS5p8N5ike5CCCJuvnx6ajt2nGIik
nq7wi+kOa1AnMk5jRcetDNMyPAj6sActmYB7fkOGXpeodAbq4yyVUcfnJObqH+M5
9l2Uhh7aZHxzb2SVHNNHT10+VN+1sD9S52p7TEwKSyFinJsa0pfQCimkt/WjJ5px
ugzBRaZubk7ZfytTGvUJJK3b/9qxnMkzsAFJuR/GScrc9tnzwqrpOZA1vSMnRTZ1
vYyqxneA1g5YmrPsl9CD+be4BOffrGaNBF3S3KnX35T2Ks1+k88Fc180bnTGoxhD
0v/jgPj27oY/ANYR2IcI8SxC69rDvN6Gme5DJIXMzUUFqj3a/gqUAq5nAN4Vk5QW
5aiVCweBj0CPt/MeNWSzhRupJGr07Jc5TEuplEIDFcx4ZKIgNE7i92JZLfub1YOJ
IbY26VPX0MdxHgtXL7M9PLpYkTWRMVwMbPORGKqTQsYrdU+2jRtRk08KWYscNPq+
DXP5zmGcYY6W+xImRVLKTgJftxmF1W1Qnz/UWgYFZgGp2oNeCipuld/5DDfb3rBf
3/bhfspplGBQqW0eEJiSCzFkiNYSzGG7L3VEmxR/R3hzRjD/GOTpPdz5HFwt5Jba
TNDzeZPvNwThNBM23X3W
=5kgn
-----END PGP SIGNATURE-----

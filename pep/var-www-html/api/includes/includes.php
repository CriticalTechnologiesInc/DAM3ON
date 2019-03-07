<?php
$cacert = "/etc/webxacml/mysql_keys/cacert.pem";
$client_cert = "/etc/webxacml/mysql_keys/cert-dev4.pem";
$client_key = "/etc/webxacml/mysql_keys/dev4.pem";

# database port
$mport = 4655;

# server IP of the DB server 
$servername = "209.217.211.170";
#$servername = "192.168.211.169";
$username = "nonce_user";
$password = "CriticalAardvark#25";
$dbname = "nonce_db";
$nonce_table = "nonce_table";
$nonce_ttl = 300; # Magic number. 300 seconds = 5 minutes. The nonce is good for 5 minutes.

# server IP of the DB server 
$pservername = "209.217.211.170";
#$pservername = "192.168.211.169";
$pusername = "pnonce_user";
$ppassword = "CriticalAardvark#25";
$pdbname = "pnonce_db";
$pnonce_table = "pnonce_table";
?>

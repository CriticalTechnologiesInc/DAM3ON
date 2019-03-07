<?php
$cacert = "/etc/webxacml/mysql_keys/cacert.pem";
$client_cert = "/etc/webxacml/mysql_keys/cert-dev4.pem";
$client_key = "/etc/webxacml/mysql_keys/dev4.pem";

$mport = 4655;

$policy_location = "ctidev2.critical.com";
$policy_user = "critical";
$policy_pswd = "CriticalAardvark#25";
$policy_db = "xacml3_policy";

$conn = mysqli_init();
mysqli_ssl_set($conn, $client_key, $client_cert, $cacert, NULL, NULL);
mysqli_real_connect($conn, $policy_location, $policy_user, $policy_pswd, $policy_db, $mport, NULL, MYSQLI_CLIENT_SSL_DONT_VERIFY_SERVER_CERT); # we use self-signed

# $conn = new mysqli($policy_location, $policy_user, $policy_pswd, $policy_db); # old / without SSL
?>

<?php
session_start(); if($_SESSION["https://".$_SERVER["HTTP_HOST"]."/viewalerts/"] == "permit" && isset($_SESSION["res"])) :

function getMsgByList(){
    $stupid = include("includes/includes.php");
    # THIS IS NEW FOR SSL
    $conn = mysqli_init();
    mysqli_ssl_set($conn, $client_key, $client_cert, $cacert, NULL, NULL);
    mysqli_real_connect($conn, $servername, $username, $password, $dbname, $mport, NULL, MYSQLI_CLIENT_SSL_DONT_VERIFY_SERVER_CERT); # we use self-signed
    # END NEW SSL STUFF
    if ($conn->connect_error) {trigger_error('Database connection failed: ' . $conn->connect_error, E_USER_ERROR);exit();}

    $query = "SELECT msgid,time FROM " . $alert_table . " WHERE listname=? ORDER BY time DESC";
    $stmt = $conn->prepare($query);

    if ($stmt) {
        $stmt->bind_param("s", $_SESSION["res"]);
        $stmt->execute();
	$result = $stmt->get_result();
	$num_rows = mysqli_num_rows($result);
	if($num_rows > 0){
		while($row = $result->fetch_array(MYSQLI_NUM)){
			if(isset($_GET["msgid"]) && $_GET["msgid"] == $row[0]){
				header('Location: ' . 'https://'.$_SERVER["HTTP_HOST"].'/viewalert/index.php?msgid='.$row[0]);
			}
			echo "MSGID: <a href=\"https://".$_SERVER["HTTP_HOST"]."/viewalert/index.php?msgid=".$row[0]."\">".$row[0]."</a> at time: ".date('m/d/Y H:i:s', $row[1]/1000);
			echo "<br/>";
		}
	}else{
		echo "No alerts";
	}

        $stmt->close();
    }else{
	echo "fail";
    }
}
?>


<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8">
    <title>View Alerts</title>
	<link rel="stylesheet" href="style.css">
	<link rel="icon" href="/icon.ico" type="image/x-icon" />
	<link rel="shortcut icon" href="/icon.ico" type="image/x-icon" />
  </head>
  <body>
<br/><br/>


<div id="maindiv" class="login" style="color:white;">
<h2 style="color:white;text-align:center;">Alerts for list: <?php echo $_SESSION["res"];?></h2>

<?php getMsgByList();?>
</div>

</body>
</html>

<?php else:
   header('Location: ' . 'https://'.$_SERVER["HTTP_HOST"].'/access_denied.html', true, 303);
   die();
 endif; ?>

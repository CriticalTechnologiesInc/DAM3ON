<?php
session_start();
if(($_SESSION["https://".$_SERVER["HTTP_HOST"]."/viewalert/"] == "permit" || $_SESSION["https://".$_SERVER["HTTP_HOST"]."/viewalerts/"] == "permit") && isset($_SESSION["res"])) :

	function getMsgById($id){
    		$stupid = include("includes/includes.php");
    		# THIS IS NEW FOR SSL
    		$conn = mysqli_init();
    		mysqli_ssl_set($conn, $client_key, $client_cert, $cacert, NULL, NULL);
    		mysqli_real_connect($conn, $servername, $username, $password, $dbname, $mport, NULL, MYSQLI_CLIENT_SSL_DONT_VERIFY_SERVER_CERT); # we use self-signed
    		# END NEW SSL STUFF
    		if ($conn->connect_error) {trigger_error('Database connection failed: ' . $conn->connect_error, E_USER_ERROR);exit();}

    		$query = "SELECT cap,listname FROM " . $alert_table . " WHERE msgid=?";
    		$stmt = $conn->prepare($query);
    		if ($stmt) {
        		$stmt->bind_param("s", $id);
        		$stmt->execute();
        		$stmt->bind_result($cap,$listname);
        		$stmt->store_result();
        		$stmt->fetch();
        		$stmt->close();

			if($listname == $_SESSION["res"]){
				$alert = new SimpleXMLElement($cap);
//				$same = (string)$alert->info[0]->area[0]->geocode[1]->value; // 1071
				$geocodes = $alert->info[0]->area[0]->geocode; // 1071
				$same = [];
				for($x=0; $x < $geocodes->count(); $x++){
					$same[$x] = (string)$geocodes[$x]->value;
				}
				if($same != "" || $same != null){
					$_SESSION["same"] = $same;
				}else{
					$_SESSION["same"] = "";
				}
				return $cap;
			}else{
				return "unauthorized";
			}
    		}else{
        		header('Location: ' . 'https://'.$_SERVER["HTTP_HOST"].'/access_denied.html', true, 303);
        		die();
    		}
	}



	if(isset($_GET["msgid"])){
		$cap = getMsgById($_GET["msgid"]);
		header("Content-type: text/xml");
		print trim($cap);
	}elseif(isset($_SESSION["arg"])){
		$cap = getMsgById($_SESSION["arg"]);
		header("Content-type: text/xml");
		print trim($cap);
	}else{
	   	header('Location: ' . 'https://'.$_SERVER["HTTP_HOST"].'/access_denied.html', true, 303);
	   	die();
	}

else:
   header('Location: ' . 'https://'.$_SERVER["HTTP_HOST"].'/access_denied.html', true, 303);
   die();
endif;

?>

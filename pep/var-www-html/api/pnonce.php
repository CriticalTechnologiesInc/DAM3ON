<?php include("includes/includes.php");
# This function takes in an email address, queries the DB for it, and returns the result as a JSON object
function validateNonce($email, $conn, $pnonce_table){
    $query = "SELECT decision, url, url_json, subject, cap, resource, action, atoken, arg FROM " . $pnonce_table . " WHERE nonce=?";
    $stmt = $conn->prepare($query);
    if ($stmt) {
        $stmt->bind_param("s", $email);
        $stmt->execute();
        $stmt->bind_result($r_decision, $url, $url_json, $subject, $cap, $res, $act, $token, $arg);
        $stmt->store_result();
        $stmt->fetch();
        $stmt->close();
        if($r_decision == "Permit"){
                permitExtraUrls($url_json);
		$_SESSION["subject"] = $subject;
		$_SESSION["action"] = $act;

				if(!($cap === NULL)){
					$_SESSION['res_type'] = "file";
					$_SESSION['cap'] = $cap;
					$_SESSION['res'] = $res;
				}else{
					$_SESSION['res'] = $res;
					$_SESSION['res_type'] = "url";
				}

				if(!($token === NULL)){
					$_SESSION['token'] = $token;
				}

				if(!($arg === NULL)){
					$_SESSION['arg'] = $arg;
				}

                return $url;
        }else{
                return "http://".$_SERVER["HTTP_HOST"]."/access_denied.html";
        }
    }
}
# This function returns how many nonces are associated with a user
function howManyNonces($nonce, $conn, $pnonce_table){
    $query = "SELECT COUNT(*) FROM " . $pnonce_table . " WHERE nonce=?";
    $stmt = $conn->prepare($query);
    if ($stmt) {
            $stmt->bind_param("s", $nonce);
            $stmt->execute();
            $stmt->bind_result($r_count);
            $stmt->store_result();
            $stmt->fetch();
            $stmt->close();
            return $r_count;
    }
}
# This function deletes all nonces associated with a user
function deleteNonces($nonce, $conn, $pnonce_table){
    $query = "DELETE FROM " . $pnonce_table . " WHERE nonce=?";
    $stmt = $conn->prepare($query);
    if ($stmt){
        $stmt->bind_param("s", $nonce);
        $stmt->execute();
        $stmt->close();
    }
}

function denyUser($reason){
        echo '<h2>Access Denied</h2>';
        echo '<h4>Reason: '.$reason.'</h4>';
}
function permitUser($url){
        $_SESSION[$url] = "permit";
        redirect($url);
}
function permitExtraUrls($url_json){
        ini_set('session.cookie_domain', '.critical.com' );
        session_start();
        $jarr = json_decode($url_json);
                # NEW check if empty
                if(empty($jarr)){
                        return;
                }else{
                        foreach ($jarr as $value){
                                $_SESSION[$value] = "permit";
                        }
                }
}
function redirect($url, $statusCode = 303) {
   header('Location: ' . $url, true, $statusCode);
   die();
}
# This is main? idk how to php
if( $_POST["nonce"] ) {
   $nonce = $_POST["nonce"];
#    $conn = new mysqli($pservername, $pusername, $ppassword, $pdbname);
    # THIS IS NEW FOR SSL
    $conn = mysqli_init();
    mysqli_ssl_set($conn, $client_key, $client_cert, $cacert, NULL, NULL);
    mysqli_real_connect($conn, $pservername, $pusername, $ppassword, $pdbname, $mport, NULL, MYSQLI_CLIENT_SSL_DONT_VERIFY_SERVER_CERT); # we use self-signed
    # END NEW SSL STUFF
    if ($conn->connect_error) {trigger_error('Database connection failed: ' . $conn->connect_error, E_USER_ERROR);exit();}
    $howmany = howManyNonces($nonce, $conn, $pnonce_table);
    if ($howmany == 1){
        $url = validateNonce($nonce, $conn, $pnonce_table);
        deleteNonces($nonce, $conn, $pnonce_table);
        permitUser($url);
        exit();
    }else if ($howmany > 1){
        deleteNonces($nonce, $conn, $pnonce_table);
        denyUser('Possible account tampering - too many entries in DB');
        exit();
    } else {
        denyUser('No verification information available.');
        exit();
    }
} else {
   denyUser('No verification information provided');
   exit();
}
?>


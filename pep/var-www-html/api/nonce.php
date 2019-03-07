<?php
include("includes/includes.php");


# This function takes in an email address, queries the DB for it, and returns the result as a JSON object
function getNonceData($email, $conn, $nonce_table){
    $query = "SELECT email, nonce, time FROM " . $nonce_table . " WHERE email=?";
    $stmt = $conn->prepare($query);

    if ($stmt) {
        $stmt->bind_param("s", $email);
        $stmt->execute();
        $stmt->bind_result($r_email, $r_nonce, $r_time);

        while ($stmt->fetch()) {
            echo "{\"email\":\"" . $r_email . "\", \"nonce\":\"" . $r_nonce . "\",\"time\":\"" . $r_time . "\"}";
        }
        $stmt->close();
    }
}

# This function returns how many nonces are associated with a user
function howManyNonces($email, $conn, $nonce_table){
    $query = "SELECT COUNT(*) FROM " . $nonce_table . " WHERE email=?";
    $stmt = $conn->prepare($query);

    if ($stmt) {
            $stmt->bind_param("s", $email);
            $stmt->execute();
            $stmt->bind_result($r_count);
            $stmt->store_result();
            $stmt->fetch();
            $stmt->close();
            return $r_count;
    }
}

# This function deletes all nonces associated with a user
function deleteNonces($email, $conn, $nonce_table){
    $query = "DELETE FROM " . $nonce_table . " WHERE email=?";
    $stmt = $conn->prepare($query);
    if ($stmt){
        $stmt->bind_param("s", $email);
        $stmt->execute();
        $stmt->close();
    }
}

# This function generates a new nonce
function generateNonce($email, $conn, $nonce_table){
    $query = "INSERT INTO " . $nonce_table . " VALUE (?,?,?)";
    $stmt = $conn->prepare($query);
    $nonce = generateRandomString();
    $date = new DateTime();
    $timestamp = $date->getTimestamp();
    $nonce = $nonce . $timestamp;
    if ($stmt){
        $stmt->bind_param("sss", $nonce, $email, $timestamp);
        $stmt->execute();
        $stmt->close();
    }
}

# This function checks if a nonce is still valid, if not, it deletes the current nonce(s)
# and generates a new one
function checkNonceTime($email, $conn, $nonce_table, $nonce_ttl){
    $query = "SELECT time FROM " . $nonce_table . " WHERE email=?";
    $stmt = $conn->prepare($query);

    if ($stmt) {
        $stmt->bind_param("s", $email);
        $stmt->execute();
        $stmt->bind_result($r_time);
        $stmt->store_result();
        $stmt->fetch();
        $stmt->close();
    }
    $date = new DateTime();
    $timestamp = $date->getTimestamp();
    $r_time = $r_time + $nonce_ttl;
    if ($r_time <  $timestamp){
        deleteNonces($email, $conn, $nonce_table);
        generateNonce($email, $conn, $nonce_table);
    }
}

# This function generates a random string
function generateRandomString($length = 10) {
    $characters = '0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ';
    $charactersLength = strlen($characters);
    $randomString = '';
    for ($i = 0; $i < $length; $i++) {
        $randomString .= $characters[rand(0, $charactersLength - 1)];
    }
    return $randomString;
}

# This is main? idk how to php
if( $_GET["email"] ) {
   $email = $_GET["email"];
   if (!filter_var($email, FILTER_VALIDATE_EMAIL)){
       echo "error";
       exit();
   }

    # THIS IS NEW FOR SSL
    $conn = mysqli_init();
    mysqli_ssl_set($conn, $client_key, $client_cert, $cacert, NULL, NULL);
    mysqli_real_connect($conn, $servername, $username, $password, $dbname, $mport, NULL, MYSQLI_CLIENT_SSL_DONT_VERIFY_SERVER_CERT); # we use self-signed
    # END NEW STUFF

#    $conn = new mysqli($servername, $username, $password, $dbname);
#    if ($conn->connect_error) {trigger_error('Database connection failed: '  . $conn->connect_error, E_USER_ERROR);exit();}

    $howmany = howManyNonces($email, $conn, $nonce_table);

    if ($howmany == 1){
        # If there is only 1 nonce (as there should be), then make sure it's still valid
        # If it is, return it.
        checkNonceTime($email, $conn, $nonce_table, $nonce_ttl);
        getNonceData($email, $conn, $nonce_table);
        exit();
    }else if ($howmany > 1){
        # If there is more than one nonce associated with a user, something went wrong
        # Delete them all, and generate a new nonce
        # Then return that nonce
        deleteNonces($email, $conn, $nonce_table);
        generateNonce($email, $conn, $nonce_table);
        getNonceData($email, $conn, $nonce_table);
        exit();
    } else {
        # Otherwise, the user doesn't have a nonce associated with them.
        # Generate a nonce, and return it to the user.
        generateNonce($email, $conn, $nonce_table);
        getNonceData($email, $conn, $nonce_table);
        exit();
    }

} else {
   echo "error";
   exit();
}
?>

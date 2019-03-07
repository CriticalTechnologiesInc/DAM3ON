<?php session_start();
if(!(isset($_SESSION["res"]) && isset($_SESSION["subject"]) && isset($_SESSION["action"]) && $_SESSION["https://".$_SERVER["HTTP_HOST"]."/mailman/"] == "permit"))
{
	header('Location: ' . 'https://'.$_SERVER["HTTP_HOST"].'/access_denied.html', true, 303);
	die();
}
?>
<!DOCTYPE html>
<html>
 <head>
    <meta charset="UTF-8">
    <title>Mailman - Loading...</title>
    <link rel="stylesheet" href="css/normalize.css">
    <link rel="stylesheet" href="css/style.css">
    <link rel="stylesheet" href="css/jquery-ui.css" />
    <link rel="icon" href="/icon.ico" type="image/x-icon" />
    <link rel="shortcut icon" href="/icon.ico" type="image/x-icon" />
  </head>
  <body>
  <br/><br/>
  <h2 style="color:white;text-align:center;">Mailman</h2>
  <br/>

  <p style="color:white;text-align:center;">Attempting to perform the action "<?php echo $_SESSION["action"]."\" on subject ".$_SESSION['subject']." for resource ".$_SESSION['res'];?></p>

<br/>
<?php
	$pieces = explode("@", $_SESSION["res"]);

	$url = 'http://'.$pieces[1].'/api/enforcer.php';

	$data = array('subject' => $_SESSION["subject"], 'action' => $_SESSION["action"], 'resource' => $_SESSION["res"], 'token' => 'asecuretoken' );

	// use key 'http' even if you send the request to https://...
	$options = array(
	    'http' => array(
	        'header'  => "Content-type: application/x-www-form-urlencoded\r\n",
	        'method'  => 'POST',
	        'content' => http_build_query($data)
	    )
	);
	$context  = stream_context_create($options);
	$result = file_get_contents($url, false, $context);
	echo '<p style="color:white; text-align:center;">';
	echo "The result was: ";
	if ($result === FALSE) {
		echo "There was an error with the enforcer API";
	 }else{
		echo $result;
	}
	echo "</p>";
?>
<br/>
<a style="text-decoration:none;" href="https://<?php echo $_SERVER["HTTP_HOST"];?>"><button style="margin:0 auto; width:14%; display:block;" class="btn btn-primary btn-block btn-large" type="button">Return to landing page</button></a>

</body>
</html>

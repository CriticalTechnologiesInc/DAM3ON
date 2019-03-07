<?php session_start(); if($_SESSION["https://".$_SERVER["HTTP_HOST"]."/authonly-pgp_qemu/"] == "permit") :?>
<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8">
    <title>PGP Authentication Only</title>
    <link rel="stylesheet" href="css/normalize.css">
    <link rel="stylesheet" href="css/style.css">
    <link rel="icon" href=/"icon.ico" type="image/x-icon" />
    <link rel="shortcut icon" href="/icon.ico" type="image/x-icon" />
  </head>

	<body>
		<h2 style="color:white; text-align:center;">PGP Authentication Only</h2></br>
		<p style="margin-left:40px;color:white;">
			This is an demo of a resource that only requires PGP authentication.
		</p>
	</body>
</html>
<?php else:
   header('Location: ' . 'http://'.$_SERVER["HTTP_HOST"].'/access_denied.html', true, 303);
   die();
 endif; ?>


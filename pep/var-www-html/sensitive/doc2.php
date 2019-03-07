<?php
session_start();
if($_SESSION["https://".$_SERVER["HTTP_HOST"]."/sensitive/"] == "permit") :
?>

<!DOCTYPE html>
<html>
	<head>
		<meta charset="UTF-8">
		<title>Sensitive Resource</title>
		<link rel="stylesheet" href="/login/css/normalize.css">
		<link rel="stylesheet" href="/login/css/style.css">
		<script src="/login/js/jquery.min.js"></script>
		<script src="/login/js/prefixfree.min.js"></script>
		<link rel="icon" href="/icon.ico" type="image/x-icon" />
		<link rel="shortcut icon" href="/icon.ico" type="image/x-icon" />
	</head>
	<body>

	<style type="text/css" media="screen">
		a:link { color:#bacbff; text-decoration: underline; }
		a:visited { color:#8485e0; text-decoration: underline; }
		a:hover { color:#33348e; text-decoration: underline; }
		a:active { color:#7476b4; text-decoration: underline; }
	</style>

	<h2 style="color:white;text-align:center;">This is another SENSITIVE document</h2> 
	<p>
Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.
	</p>
	<a href="doc1.php">Click here to go back to the first doc1.php</a>
	</body>
</html> 

<?php else:
   header('Location: ' . 'https://'.$_SERVER["HTTP_HOST"].'/access_denied.html', true, 303);
   die();
 endif;
?>

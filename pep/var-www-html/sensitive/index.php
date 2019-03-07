<?php
session_start();
if($_SESSION["https://".$_SERVER["HTTP_HOST"]."/sensitive/"] == "permit") :
?>
<!DOCTYPE html>
<html>
	<head>
		<meta charset="UTF-8"/>
		<title>Sensitive Resource</title>
		<link rel="stylesheet" href="/login/css/normalize.css"/>
		<link rel="stylesheet" href="/login/css/style.css"/>
		<script src="/login/js/jquery.min.js"></script>
		<script src="/login/js/prefixfree.min.js"></script>
		<link rel="icon" href="/icon.ico" type="image/x-icon" />
		<link rel="shortcut icon" href="/icon.ico" type="image/x-icon" />
		<style type="text/css" media="screen">
			a:link { color:#bacbff; text-decoration: underline; }
			a:visited { color:#8485e0; text-decoration: underline; }
			a:hover { color:#33348e; text-decoration: underline; }
			a:active { color:#7476b4; text-decoration: underline; }
		</style>
	</head>

	<body>
		<h2 style="color:white;text-align:center;">Sensitive Material</h2>
		</br>

		<div style="width:20%;margin:0 auto;">
			<ul>
				<li><a href="doc1.php">doc1.php</a></li>
			</br>
			<li><a href="doc2.php">doc2.php</a></li>
			</ul>
		</div>
	</body>
</html>
<?php
else:
	header('Location: ' . 'https://'.$_SERVER["HTTP_HOST"].'/access_denied.html', true, 303);
	die();
endif;
?>

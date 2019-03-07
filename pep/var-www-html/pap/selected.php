<?php session_start(); if($_SESSION["https://".$_SERVER["HTTP_HOST"]."/pap/"] == "permit") :?>
<!DOCTYPE html>
<html>

  <head>
    <meta charset="UTF-8">
    <title>Policy Administration Point</title>
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

<h2 style="color:white; text-align:center;">Policy Administration Point</h2></br>
<div style="width:45%; margin:0 auto;">
	<a href="index.php">Back to main</a></br>
	<a href="/logout.php">Logout</a></br>
	<h4>Policies Currently in "Selected Policies"</h4>
	<ul id="names"></ul>

	<div>
		Policy: <input id='input_add' type="text" name="add"><button id='add'>Add</button><br>
		Policy: <input id='input_remove' type="text" name="remove"><button id='remove'>Remove</button><br>
		<button id='refresh'>Refresh</button>
	</div>
	<script src="sel.js"></script>
</div>
</body>
</html>
<?php else:
   header('Location: ' . 'https://'.$_SERVER["HTTP_HOST"].'/access_denied.html', true, 303);
   die();
 ?> <?php endif; ?>


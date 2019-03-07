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
<div style="margin-left:40px;">
<a href="index.php">Back to main</a></br>
<a href="/logout.php">Logout</a></br>
<select id="names">
<option value = "">---Select---</option>
</select>
<button id='button'>Load</button></br>
<button id='save'>Save</button>
<button style="margin-left:40px;" id='cancel'>Cancel</button>
<button style="margin-left:40px;" id='delete'>Delete</button></br>
</div>
<br/><br/>

<textarea style="overflow-y:scroll;width:80%; height:62%; display:block; margin:0 auto;" id='text_area'></textarea>
<script src="main.js"></script>
</body>
</html>
<?php else:
   header('Location: ' . 'https://'.$_SERVER["HTTP_HOST"].'/access_denied.html', true, 303);
   die();
 ?> <?php endif; ?>



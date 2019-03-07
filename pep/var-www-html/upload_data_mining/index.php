<?php session_start(); if($_SESSION["https://".$_SERVER["HTTP_HOST"]."/upload_data_mining/"] == "permit") :?> 
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


<h2 style="color:white;text-align:center;">Upload to data_mining</h2>

<div style="width:22%;margin:0 auto;">
	<form action="upload.php" method="post" enctype="multipart/form-data">
	    Select image to upload:
	    <input type="file" name="fileToUpload" id="fileToUpload"><br/>
	    <input type="submit" value="Upload Image" name="submit">
	</form>

	<p>
		<a href="/data_mining/">Click here to visit the "data_mining" resource"</a>
	</p>
</div>

</body> </html> 
<?php else:
   header('Location: ' . 'https://'.$_SERVER["HTTP_HOST"].'/access_denied.html', true, 303);
   die();
 ?> <?php endif; ?>

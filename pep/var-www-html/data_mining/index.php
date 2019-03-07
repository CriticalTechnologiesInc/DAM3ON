<?php
session_start();
// get filename from database or somewhere else
$filename = $_GET["file"];

if($_SESSION["https://".$_SERVER["HTTP_HOST"]."/data_mining/"] == "permit"):?>


<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8">
    <title>Data Mining</title>
    <link rel="stylesheet" href="/login/css/normalize.css">
	<link rel="stylesheet" href="/view/css/style.css">
	<script src="/login/js/jquery.min.js"></script> 
	<script src="/login/js/prefixfree.min.js"></script>
        <link rel="icon" href="/icon.ico" type="image/x-icon" />
        <link rel="shortcut icon" href="/icon.ico" type="image/x-icon" /> 
  </head>
  <body>
<style type="text/css" media="screen">
a:link { color:#bacbff; text-decoration: underline; }
a:visited { color:#33348e; text-decoration: underline; }
a:hover { color:#33348e; text-decoration: underline; }
a:active { color:#7476b4; text-decoration: underline; }
</style>


<?php
echo '<div style="color:white;text-align:center;">';
echo 'Click on any link to download file<br/>';
echo 'Click <a href="/logout.php">here</a> to logout.</br>';
echo '</div>';

echo '<br/><br/>';

echo '<div style="padding-left:35%;overflow-y:scroll;height:80%;">';
if ($handle = opendir('.')) {

    while (false !== ($entry = readdir($handle))) {

        if ($entry != "." && $entry != "..") {
		if($entry != ".htaccess" && $entry != "index.php" && $entry != "index2.php"){
            echo "<a href=$entry>$entry</a></br>";
		}
        }
    }

    closedir($handle);
}
echo '</div>';
?>
</body>
</html>
<?php else:
header('Location: ' . 'https://'.$_SERVER["HTTP_HOST"].'/access_denied.html', true, 303);
   die();
?><?php endif;?>

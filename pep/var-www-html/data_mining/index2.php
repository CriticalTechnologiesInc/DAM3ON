<?php
session_start();
// get filename from database or somewhere else
$filename = $_GET["file"];

if($_SESSION["https://".$_SERVER["HTTP_HOST"]."/data_mining/"] == "permit"){
	if(!($filename == ".htaccess" || $filename == "index.php" || $filename == "index2.php")){
		header("Pragma: public");
		header("Expires: 0");
		header("Cache-Control: must-revalidate, post-check=0, pre-check=0");

		header('Content-Type: application/octet-stream');
		//forces a download
		header("Content-Type: application/force-download");
		header('Content-Disposition: attachment; filename='.$filename);

		readfile($filename);
   }
}else{
	header('Location: ' . 'https://'.$_SERVER["HTTP_HOST"].'/access_denied.html', true, 303);
	   die();}
?>



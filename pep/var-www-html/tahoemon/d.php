<?php
	if(isset($_GET["n"]) && !empty($_GET["n"])){
	        $externalURL = "http://ctidev".$_GET["n"].".critical.com:462".$_GET["n"]."/statistics?t=json";
	        $externalData = file_get_contents($externalURL);
        	echo $externalData;
	}
?>

<?php
function alert_msg($message){
	echo '<script language="javascript">';
	echo 'alert("'.$message.'")';
	echo '</script>';
}
function console_msg($message){
	echo '<script language="javascript">';
	echo 'console.log("'.$message.'")';
	echo '</script>';
}
?>

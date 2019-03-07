<?php
session_start();
// get filename from database or somewhere else
if($_SESSION["http://".$_SERVER["HTTP_HOST"]."/radio/"] == "permit"){
	$filename = $_GET["file"];
	$REPO = 'Radio_repo';
	$TAHOE_REPO = "Radio";
		if(!($filename == ".htaccess" || $filename == ".git")){
			$command = escapeshellcmd('sudo /etc/webxacml/annex/annex_prep '.$TAHOE_REPO.' get ' . base64_encode(htmlspecialchars($_GET['file'])));
			$output = shell_exec($command);
			if($output == "success\n"){
				header("Pragma: public");
				header("Expires: 0");
				header("Cache-Control: must-revalidate, post-check=0, pre-check=0");
				header('Content-Type: application/octet-stream');
				//forces a download
				header("Content-Type: application/force-download");
				header('Content-Disposition: attachment; filename='.$filename);
				readfile('/opt/git/'.$REPO.'/'.$filename);
				$command = escapeshellcmd('sudo /etc/webxacml/annex/annex_prep '.$TAHOE_REPO.' drop ' . base64_encode(htmlspecialchars($_GET['file'])));
				$output = shell_exec($command);
				// can't do an is/else on this $output because if any HTML is written
				// it's written to the octet-stream which just puts it in the file being downloaded :(
			} else {
				echo '<script>alert("Error: '.$output.'");</script>';
				echo '</br><a href="index.php">Back to listing</a>';
			}
	   }
}else{
	header('Location: ' . 'http://'.$_SERVER["HTTP_HOST"].'/access_denied.html', true, 303);
	die();}
?>


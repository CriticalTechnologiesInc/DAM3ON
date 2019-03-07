<?php
session_start();

if(isset($_SESSION["https://".$_SERVER["HTTP_HOST"]."/getfile/"]) && isset($_SESSION['res_type']) &&  isset($_SESSION['cap']) &&  isset($_SESSION['res'])){
	if($_SESSION["https://".$_SERVER["HTTP_HOST"]."/getfile/"] == "permit"){
		$file_location = str_replace("file://", "", $_SESSION['res']);
		$pieces = explode("/", $file_location);
		$TAHOE_REPO = str_replace("_repo", "", $pieces[0]);
		$filename = str_replace($TAHOE_REPO."_repo/", "", $file_location);
//		$filename = basename($file_location);

		$command = escapeshellcmd('sudo /etc/webxacml/tahoepep/./tahoepepWrapper.py get '.$file_location.' '.$_SESSION['cap']);
		//$command = escapeshellcmd('sudo java -jar /etc/webxacml/tahoepep/tahoepep.jar '.$file_location.' '.$_SESSION['cap'].' get');

		exec($command, $pg_out, $output); // this will return the exit code in $output


		if($output == "0"){
			header('Content-Description: File Transfer');
			header('Content-Type: application/octet-stream');
			header('Content-Disposition: attachment; filename="'.basename($file_location).'"');
			header('Expires: 0');
			header('Cache-Control: must-revalidate');
			header('Pragma: public');
			header('Content-Length: ' . filesize('/opt/git/'.$file_location));

			readfile('/opt/git/'.$file_location);
			$command = escapeshellcmd('sudo /etc/webxacml/annex/annex_prep '.$TAHOE_REPO.' drop ' . base64_encode($filename));
			$annexOutput = shell_exec($command);
                        if($annexOutput == "success\n"){
                                $command = escapeshellcmd('sudo /etc/webxacml/tahoepep/./tahoepepWrapper.py drop '.$file_location.' '.$_SESSION['cap']);
                                shell_exec($command);
				session_destroy();
                        }else{
                                echo '<script>alert("Error dropping file: '.$annexOutput.'"'.$filename.');</script>';
                        }
		} else {
			echo '<script>alert("Error: '.$output.'");</script>';
			echo '</br><a href="https://'.$_SERVER["HTTP_HOST"].'">Back to login</a>';
		}
	}else{
		header('Location: https://'.$_SERVER["HTTP_HOST"].'/access_denied.html');
		die();
	}
} else {
	header('Location: https://'.$_SERVER["HTTP_HOST"].'/access_denied.html');
	die();
}
?>


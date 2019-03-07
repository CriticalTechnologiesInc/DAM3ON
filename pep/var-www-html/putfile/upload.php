<?php
session_start();
$DEBUG = false;
if($_SESSION["https://".$_SERVER["HTTP_HOST"]."/putfile/"] == "permit" && isset($_SESSION["res"])){
        $target_dir = $_POST["dest"]; // /opt/git/Radio_repo/testdir/
	$repo = $_SESSION["res"]; // Radio_repo
        $TAHOE_REPO = str_replace("_repo", "", $repo); // Radio
        $target_file = $target_dir . basename($_FILES["fileToUpload"]["name"]); // /opt/git/Radio_repo/testdir/doc.txt
	$forAnnexPrep = explode($repo.'/', $target_file)[1];
        $uploadOk = 1;
        $imageFileType = pathinfo($target_file,PATHINFO_EXTENSION);

	if($DEBUG){
		echo "imageFileType: " . $imageFileType . "<br>";
		echo "target_file: " . $target_file . "<br>";
	}

        // Check if file already exists
        if (file_exists($target_file)) {
                echo "Sorry, file already exists.";
                $uploadOk = 0;
        }
        // Check file size
        if ($_FILES["fileToUpload"]["size"] > 150000000) { // 150MB?
                echo "Sorry, your file is too large.";
                $uploadOk = 0;
        }
        // Allow certain file formats
        if(!($imageFileType == "txt" || $imageFileType == "mp4")) {
                echo "Sorry, only TXT or MP4 files are allowed. Type: " . $imageFileType;
                $uploadOk = 0;
        }
        // Check if $uploadOk is set to 0 by an error
        if ($uploadOk == 0) {
                echo "Sorry, your file was not uploaded.";

        // if everything is ok, try to upload file
        } else {
                $move_py = escapeshellcmd('sudo /etc/webxacml/annex/php_move '.base64_encode(htmlspecialchars($_FILES["fileToUpload"]["tmp_name"])).' '.base64_encode(htmlspecialchars($target_dir.$_FILES["fileToUpload"]["name"])));
		if($DEBUG){
			echo "CMD: " . 'sudo /etc/webxacml/annex/php_move '.base64_encode(htmlspecialchars($_FILES["fileToUpload"]["tmp_name"])).' '.base64_encode(htmlspecialchars($target_dir.$_FILES["fileToUpload"]["name"]));
			echo "<br>";
			echo "fileToUpload[tmp_name]: " . $_FILES["fileToUpload"]["tmp_name"] . "<br>";
			echo "fileToUpload[name]: " . $target_dir.$_FILES["fileToUpload"]["name"] . "<br>";
		}
                $move_output = shell_exec($move_py);
                if ($move_output == "success\n"){
                      $command = escapeshellcmd('sudo /etc/webxacml/annex/annex_prep '.$TAHOE_REPO.' add ' . base64_encode(htmlspecialchars($forAnnexPrep)));
                      $output = shell_exec($command);
                      echo $output."</br></br>";
                      if($output == "success\n"){
                              echo '<script>alert("Success");</script>';
                      } else {
                              echo '<script>alert("Error:'.$output.'");</script>';
                      }
                      echo '</br><a href="index.php">Back to listing</a>';
                      die();


                } else {
                        echo "Sorry, there was an error uploading your file.";
                }
        }
}else{
header('Location: ' . 'http://'.$_SERVER["HTTP_HOST"].'/access_denied.html', true, 303);
   die();}

?>


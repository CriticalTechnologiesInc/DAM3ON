<?php
session_start();
if($_SESSION["http://".$_SERVER["HTTP_HOST"]."/radio/"] == "permit"){
        $target_dir = "/opt/git/Radio_repo/";
        $TAHOE_REPO = "Radio";
        $target_file = $target_dir . basename($_FILES["fileToUpload"]["name"]);
        $uploadOk = 1;
        $imageFileType = pathinfo($target_file,PATHINFO_EXTENSION);

        // Check if file already exists
        if (file_exists($target_file)) {
                echo "Sorry, file already exists.";
                $uploadOk = 0;
        }
        // Check file size
        if ($_FILES["fileToUpload"]["size"] > 1000000) {
                echo "Sorry, your file is too large.";
                $uploadOk = 0;
        }
        // Allow certain file formats
        if($imageFileType != "txt") {
                echo "Sorry, only TXT files are allowed.";
                $uploadOk = 0;
        }
        // Check if $uploadOk is set to 0 by an error
        if ($uploadOk == 0) {
                echo "Sorry, your file was not uploaded.";
        // if everything is ok, try to upload file
        } else {
                $move_py = escapeshellcmd('sudo /etc/webxacml/annex/php_move '.base64_encode(htmlspecialchars($_FILES["fileToUpload"]["tmp_name"])).' '.base64_encode(htmlspecialchars($target_dir.$_FILES["fileToUpload"]["name"])));
                $move_output = shell_exec($move_py);
                if ($move_output == "success\n"){
                      $command = escapeshellcmd('sudo /etc/webxacml/annex/annex_prep '.$TAHOE_REPO.' add ' . base64_encode(htmlspecialchars(basename($_FILES["fileToUpload"]["name"]))));
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


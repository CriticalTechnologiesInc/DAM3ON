<?php session_start(); ?>
<?php if($_SESSION["http://".$_SERVER["HTTP_HOST"]."/radio/"] ==
"permit") :?>

<!DOCTYPE html>
<html>
<body>

<form action="upload.php" method="post" enctype="multipart/form-data">
    Select .txt to upload:
    <input type="file" name="fileToUpload" id="fileToUpload"><br/>
    <input type="submit" value="Upload Text" name="submit">
</form>


<p>#########################################</p>
<?php
$REPO = "Radio_repo";

if ($handle = opendir('/opt/git/'.$REPO)) {

    while (false !== ($entry = readdir($handle))) {

        if ($entry != "." && $entry != ".." && $entry != ".git") {

            echo '<a href="get_file.php?file='.$entry.'">'.$entry.'</a></br>';
        }
    }

    closedir($handle);
}

?>
</body> </html>
<?php else:
   header('Location: ' .
'http://'.$_SERVER["HTTP_HOST"].'/access_denied.html', true, 303);
   die();
 ?> <?php endif; ?>



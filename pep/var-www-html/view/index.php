<?php
session_start();
$repo = $_SESSION["res"];

if($_SESSION["https://".$_SERVER["HTTP_HOST"]."/view/"] == "permit" && isset($_SESSION["res"])) :?>

<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8">
    <title>List Files - <?php session_start();echo $_SESSION["res"];?> </title>
    <link rel="stylesheet" href="css/normalize.css">
    <link rel="stylesheet" href="css/style.css">
    <link rel="icon" href=/"icon.ico" type="image/x-icon" />
    <link rel="shortcut icon" href="/icon.ico" type="image/x-icon" />
  </head>
  <body>

<br/><br/>
<h1 style="color:white;text-align:center;"> Listing files for repository: <?php session_start();echo $_SESSION["res"];?></h1>
<br/><br/>

	<div style="padding-left:34%;margin: 0 auto;">
		<?php
		session_start();
		$basedir = "/opt/git/";
		$repo = $_SESSION["res"];
		echo '<ul>';
		echo '<li style="color:white;">'.$repo.'/<span style="color:#d1bff8;"> - (Root directory)</span></li>';

		function listFolderFiles($dir){
		    $ffs = scandir($dir);
		    echo '<ul>';
		    foreach($ffs as $ff){
		        if($ff != '.' && $ff != '..' && $ff != '.git'){
		            echo '<li style="color:white;">'.$ff;
		            if(is_dir($dir.'/'.$ff)){ echo ' <span style="color:#d1bff8;"> - (Directory)</span>';  listFolderFiles($dir.'/'.$ff);}
		            echo '</li>';
			        }
			    }
		    echo '</ul>';
		}

		listFolderFiles($basedir.$repo);
		echo '</ul>';

		?>
	</div>
</body>
</html>
<?php else:
   header('Location: ' . 'http://'.$_SERVER["HTTP_HOST"].'/access_denied.html', true, 303);
   die();
 ?> <?php endif; ?>


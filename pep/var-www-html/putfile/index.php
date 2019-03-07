<?php
session_start();
$repo = $_SESSION["res"];

if($_SESSION["https://".$_SERVER["HTTP_HOST"]."/putfile/"] == "permit" && isset($_SESSION["res"]) && file_exists('/opt/git/'.$repo)) :?>

<!DOCTYPE html>
<html>

 <head>
    <meta charset="UTF-8">
    <title>Upload - <?php echo $_SESSION["res"];?></title>
    <link rel="stylesheet" href="css/normalize.css">
    <link rel="stylesheet" href="css/style.css">
    <link rel="stylesheet" href="css/jquery-ui.css" />
    <script src="/login/js/jquery.min.js"></script>
    <script src="/login/js/jquery-ui.min.js"></script>
    <script src="/login/js/prefixfree.min.js"></script>
    <link rel="icon" href="/icon.ico" type="image/x-icon" />
    <link rel="shortcut icon" href="/icon.ico" type="image/x-icon" />

  </head>


	<body>

<br/><br/><br/>
<br/>
		<h1 style="text-align:center;color:white;">Uploading to repository: <span style="color:#d5d8ae;"><?php echo $_SESSION["res"];?></span></h1>
<br/><br/>
		<div id="main" style="width:30%;margin:0 auto">
		<form action="upload.php" method="post" enctype="multipart/form-data">
		<div style="color:white;margin-top:10px;float:left;">Step 1: Select file to upload: </div><input style="float:right; width:50%;" type="file" name="fileToUpload" id="fileToUpload"><br/><br/>
		<div style="color:white;margin-top:10px;float:left;">Step 2: Select destination: </div><select style="float:right;margin-bottom:10px;width:50%;" name="dest">

<?php
	function getAllSubDirectories( $directory, $directory_seperator )
	{
        	$dirs = array_map( function($item)use($directory_seperator){ return $item . $directory_seperator;}, array_filter( glob( $directory . '*' ), 'is_dir') );
	        foreach( $dirs AS $dir )
        	{
                	$dirs = array_merge( $dirs, getAllSubDirectories( $dir, $directory_seperator ) );
	        }
        	return $dirs;
	}
	$basedir = "/opt/git/";
	$dirs = getAllSubDirectories($basedir.$repo.'/', '/');
	array_unshift($dirs, $basedir.$repo.'/');

	foreach($dirs as $dir){
		echo "<option value=\"".$dir."\">".str_replace($basedir, "", $dir)."</option>\n";
	}
?>
		</select>
		<br/><br/>
		<div style="color:white;float:left;margin-top:19px;"> Step 3: Upload:</div> <input style="margin-top:10px;width:50%;float:right;" type="submit" value="Upload File" name="submit">
	</form>
	</div>
    </body>
</html>
<?php else:
   header('Location: ' . 'http://'.$_SERVER["HTTP_HOST"].'/access_denied.html', true, 303);
   die();
 ?> <?php endif; ?>

<?php

if( isset($_GET["file"]) || strpos($_SERVER['REQUEST_URI'],"pcerts") ){
	$handle = fopen($_GET["file"], "r");
	$switch = false;
	$xml = false;
	$crt = false;

	if(strpos($_GET["file"], ".xml")){
		$xml = true;
	}else if(strpos($_GET["file"], ".crt")){
		$crt = true;
	}


	if(isset($_GET["pretty"]) || strpos($_SERVER['REQUEST_URI'],"pcerts")){
		$switch = true;
	}

	if ($handle && $xml) {
		header('Content-type: application/xml');
		$x = 0;
		while (($line = fgets($handle)) !== false) {
			if($x == 1 && $switch){
				echo "<?xml-stylesheet type=\"text/xsl\" href=\"style.xsl\"?>";
				echo $line;
			}else{
				echo $line;
			}
			$x = $x + 1;
		}

		fclose($handle);
	} else if($crt){
		while (($line = fgets($handle)) !== false) {
			 echo $line;
		}
	} else {
		echo "error - bad cert";
	}
} else{ ?>

<html>
  <head>
    <meta charset="UTF-8">
    <title>Certificates</title>
    <link rel="stylesheet" href="css/style.css">
  </head>
  <body>
      <div class="maindiv">
	<h1>CTI Certificates</h1>
	<table>
		<tr>
			<td class="left">guard</td>
			<td><a href="https://ctidev4.critical.com/pcerts/guard.xml">Pretty</a></td>
			<td><a href="https://ctidev4.critical.com/certs/guard.xml">Ugly</a></td>
		</tr>
		<tr>
                        <td class="left">trustedp1</td>
                        <td><a href="https://ctidev4.critical.com/pcerts/trustedp1.xml">Pretty</a></td>
                        <td><a href="https://ctidev4.critical.com/certs/trustedp1.xml">Ugly</a></td>
                </tr>
		<tr>
                        <td class="left">p1insurance</td>
                        <td><a href="https://ctidev4.critical.com/pcerts/p1insurance.xml">Pretty</a></td>
                        <td><a href="https://ctidev4.critical.com/certs/p1insurance.xml">Ugly</a></td>
                </tr>
		<tr>
                        <td class="left">p3proof</td>
                        <td><a href="https://ctidev4.critical.com/pcerts/p3proof.xml">Pretty</a></td>
                        <td><a href="https://ctidev4.critical.com/certs/p3proof.xml">Ugly</a></td>
                </tr>
		<tr>
			<td class="left">ultimate</td>
			<td><a href="https://ctidev4.critical.com/pcerts/ultimate.xml">Pretty</a></td>
			<td><a href="https://ctidev4.critical.com/certs/ultimate.xml">Ugly</a></td>
		</tr>
		<tr>
			<td class="left">pentest</td>
			<td><a href="https://ctidev4.critical.com/pcerts/pentest.xml">Pretty</a></td>
			<td><a href="https://ctidev4.critical.com/certs/pentest.xml">Ugly</a></td>
		</tr>
	</table>

	<h1>Untrusted Certs</h1>
	<table>
		<tr>
                        <td class="left">untrustedp1p3</td>
                        <td><a href="https://ctidev4.critical.com/pcerts/untrustedp1p3.xml">Pretty</a></td>
                        <td><a href="https://ctidev4.critical.com/certs/untrustedp1p3.xml">Ugly</a></td>
                </tr>
	</table>

	<hr/>
	<p>
		Click <a download href="https://ctidev4.critical.com/certs/nobody.systems.crt">here</a> to download the public key used to sign the certificates above.
	</p>
      </div>
  </body>
</html>
<?php } ?>

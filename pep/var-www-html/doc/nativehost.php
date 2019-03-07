<!DOCTYPE html>
<html>
	<head>
		<meta charset="UTF-8"/>
		<title>Native Host</title>
		<link rel="stylesheet" href="css/style.css"/>
		<script src="js/jquery.min.js"></script>
	</head>
	<body>
		<?php include("header.html");?>
		<div id="maindiv" class="login">
			<h1>Native Host Program</h1>
			<div>
				<h3>DAM3ON Attestation</h3>
				<p>
					Some resources require attestation. To perform attestation, you must have a TPM 1.2 chip in your computer. In order to use the TPM chip to perform a quote operation and send 
					the quote as part of your request to access a resource, you must download and install our 'native host' program. This program works together with the 
					<a href="ctipcrext.html">CTI PCR extension</a> to access the TPM and send the quote back to our Policy Decision Point. To download the Native Host program, click 
					<a href="https://<?php echo $_SERVER["HTTP_HOST"];?>/media/chrome_nm.zip">here</a>. This download contains a README.txt file which explains in detail how to install and set up the Native Host program.
				</p>
			</div>
			<div>
				<h3>DAM3ON Smart Card Authentication</h3>
				<p>
					The Chrome Native host program also supports smart card authentication with DAM3ON. Refer to README on how to enable smart card support.
					To download the Native Host, click  <a href="https://<?php echo $_SERVER["HTTP_HOST"];?>/media/chrome_nm.zip">here</a>. 
				</p>
			</div>
		</div>
	</body>
</html>

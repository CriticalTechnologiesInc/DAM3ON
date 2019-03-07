<!DOCTYPE html>
<html>
	<head>
		<meta charset="UTF-8"/>
		<title>Secure Login Setup</title>
		<link rel="stylesheet" href="css/style.css"/>
		<link rel="chrome-webstore-item" href="https://chrome.google.com/webstore/detail/hmbmimahikfnkjjgopopfngigflklbij">
		<script src="js/jquery.min.js"></script>
	</head>
	<body>
		<?php include("header.html");?>
		<div class="login">
			<h3>How to Install</h3>
			<p id="not-installed">
			While in <a href="https://www.google.com/chrome/">Google Chrome</a> or <a href="https://www.chromium.org/getting-involved/download-chromium">Chromium</a>, you can
			simply click this <button class="btn" onclick="chrome.webstore.install()" id="install-button">Add to Chrome</button>
			button to install the extension. Alternativly, you can
			visit the <a href="https://chrome.google.com/webstore/detail/critical-pcr-extension/hmbmimahikfnkjjgopopfngigflklbij">CTI PCR Extension</a> in the Chrome Web Store to install the extension.
			<br/><br/>
			</p>
			<p id="installed" style="display:none;">The extension is already installed!</p>


			<hr/>
			<h3>Usage</h3>
			<p>
				Once installed, you'll see a small icon in the top right corner of chrome that looks like this: 
				<a href="https://chrome.google.com/webstore/detail/critical-pcr-extension/hmbmimahikfnkjjgopopfngigflklbij"><img style="width:25px;height:25px;vertical-align:middle;" src="media/ctiexticon.png"/></a> <br/>
				You can click this icon and you will be prompted to add a resource. This is simply a convenient place to store common resources that you personally access, and is entirely optional.
				If this feature is used, it will provide a drop down menu of these values on the login page so that you can select one, rather than type it in. Also, you can click on the name of a 
				resource from within the extension and it will take you to the login page with that resource already selected. You can also re-arrange these resources in order of personal preference.
				Lastly, if you are signed into Chrome, this list will persist across all Chrome instances on any device that is logged into the same account.		
			</p>


		</div>

	</body>
			<script>
				(function(){
					var tId = setInterval(function() {if (document.readyState == "complete") onComplete()}, 11);
					function onComplete(){
					        clearInterval(tId);
						setTimeout(function(){
						if (document.getElementById('cti-pcr-extension')) {
                		                        document.getElementById('not-installed').style = 'display:none;';
                                		        document.getElementById('installed').style = '';
		                                }}, 50);
					};
				})()
			</script>

</html>

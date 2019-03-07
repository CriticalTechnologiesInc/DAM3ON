<!DOCTYPE html>
<html>
        <head>
                <meta charset="UTF-8"/>
                <title>Mailvelope/PGP</title>
                <link rel="stylesheet" href="css/style.css"/>
		<script src="js/jquery.min.js"></script>
        </head>
        <body>
		<?php include("header.html");?>
                <div id="maindiv" class="login">
                        <h1>Mailvelope and PGP</h1>

						<div>
							<h3>DAM3ON Authentication</h3>
							<p>
								Many resources require users to be authenticated. We use <a href="https://en.wikipedia.org/wiki/Pretty_Good_Privacy">Pretty Good Privacy (PGP)</a> for authentication. While it is not required, we recommend
								using the Mailvelope Chrome extension. It is a very easy-to-use, open source tool that allows users to store PGP keys and use these keys for signing and encrypting.
								You can download <a href="https://chrome.google.com/webstore/detail/mailvelope/kajibbejlbohfaggdiogboambcijhkke">Mailvelope from the Chrome Web Store</a>.
								If you need additional help installing the Mailvelope extension, you can read <a href="https://www.mailvelope.com/en/help">Mailvelope's documentation page</a>.
							</p>
							
							<hr/>
							<h3>Mailvelope Key Setup</h3>
							<p>
								Now that you have Mailvelope installed, you will need to add your public/private key pair to Mailvelope. After installation, an icon of a lock and key should appear next to the address bar of your browser.
								Left-click this icon and then click on the options button at the bottom of the pop-up window. This should open a new tab. If you already have a PGP key pair, click 'Import Keys' on the left side of the screen
								and choose a method to import your key to Mailvelope. If you do not have PGP key pair already, click 'Generate Key' on the left side of the screen and fill out the form to create a PGP key pair.
							</p>
							
							<hr/>
							<h3>Using Mailvelope with DAM3ON</h3>
							<p>
								If a resource requires authentication, you will be prompted to enter your email address on the login page. This must be the email address associated with your PGP key pair. Once you enter your email address,
								a textbox will appear with information already filled in. DO NOT edit this text! It is important that this is not changed as we verify the text inside has not been tampered with; if it has been changed, you will
								be denied access to the resource your are requesting. The text in this box needs to be signed with PGP. If you are using Mailvelope for the first time, you will need to click on the Mailvelope icon and click the 
								'Add' button near the top of the pop-up window. This should only need to be done the first time you use Mailvelope on this page. Now, when the textbox appears, a small button should appear in the upper right side
								of the text box. Click this button and then click 'Options' in the lower left of the popup window. Check the 'Sign message with key' box and if you have more than one key saved in Mailvelope, make sure to select 
								the proper key from the drop down menu. Click 'Sign Only' and enter the password for your key when prompted. After a few seconds, Mailvelope will automatically replace the text in the box with the signed version.
								You may now proceed and click 'Request Access'.
							</p>	
								
							<p>
								NOTE: If you have other means of signing with a PGP key (ie. command line or other PGP programs) you will need to
								copy ALL the text in the box, sign it with your private key, and REPLACE the text in the box with the signed version. 
							</p>
							
							<hr/>
							<h3>Uploading Keys for DAM3ON</h3>
							<p>
								You will also need to upload your public key to Mailvelopes public key server. 
								We use <a href="https://keys.mailvelope.com/demo.html">Mailvelope's key server</a> to find your public key so that we can verify your signature
								from the above mentioned textbox. If your keys are in Mailvelope, you can upload to Mailvelopes key-server directly from Mailvelope.
							</p>	
						</div>

                </div>
        </body>
</html>



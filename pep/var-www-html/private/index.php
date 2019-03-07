<?php
session_start();
if($_SESSION["https://".$_SERVER["HTTP_HOST"]."/private/"] == "permit") :
?> 

<!DOCTYPE HTML>
<!--
	Autonomy by TEMPLATED
    templated.co @templatedco
    Released for free under the Creative Commons Attribution 3.0 license (templated.co/license)
-->
<html>
	<head>
		<title>Private</title>
		<meta http-equiv="content-type" content="text/html; charset=utf-8" />
		<script src="js/skel.min.js"></script>
		<script src="js/init.js"></script>
	</head>
	<body>

		<!-- Header -->
		<div id="header">
			<div class="container">
				
				<!-- Logo -->
				<div id="logo">
					<h1><a href="#">Private</a></h1>
				</div>

			</div>
		</div>
		<!-- Header -->
		<div id="featured">
			<div class="container">
				<div class="row">
					<div class="12u">
						<section>
							<header>
								<h2>Document List</h2>
					<p><a href="doc1.php">Link to doc1.php</a></p>
					<p><a href="doc2.php">Link to doc2.php</a></p>
					<p><a href="/logout.php">Logout</a></p>
							</header>
						</section>
					</div>		
				</div>
			</div>
		</div>
	</body>
</html>
<?php else:
   header('Location: ' .
'https://'.$_SERVER["HTTP_HOST"].'/access_denied.html', true, 303);
   die();
 ?> <?php endif; ?>

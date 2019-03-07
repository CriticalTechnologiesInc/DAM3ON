<?php session_start(); if($_SESSION["https://".$_SERVER["HTTP_HOST"]."/private/"] ==
"permit") :?> 
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
								<h2>Doc 2 - Another Private document</h2>
<p><a href="/private">Link to Private Index</a></p>
							</header>
							<p> Everyday carry
Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.
literally cliche williamsburg actually. </p>

							
							
							
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

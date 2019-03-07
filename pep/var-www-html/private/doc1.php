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
								<h2>Doc 1</h2>
					<p><a href="/private">Link to Private Index</a></p>
							</header>
							<p> Everyday carry
cold-pressed forage, actually butcher tousled asymmetrical distillery
health goth bitters. Crucifix DIY cronut, twee viral meggings retro deep
v. Before they sold out VHS kickstarter blue bottle tousled. Sartorial
fingerstache kombucha, salvia retro kale chips chia lomo freegan.
Post-ironic umami taxidermy, beard godard sriracha you probably haven't
heard of them trust fund jean shorts banh mi vinyl echo park brooklyn
art party. Kickstarter yr 3 wolf moon readymade cold-pressed hammock.
Sartorial vinyl kombucha, whatever slow-carb flexitarian hammock tofu
before they sold out cred shoreditch vice. Typewriter synth scenester,
cold-pressed etsy normcore four dollar toast mixtape intelligentsia
post-ironic mumblecore. Farm-to-table before they sold out gluten-free
tote bag poutine. Mlkshk put a bird on it kickstarter, try-hard 90's
microdosing paleo brooklyn chicharrones umami street art quinoa organic
viral ramps. Food truck PBR&B blue bottle poutine yuccie mixtape
literally portland, williamsburg etsy XOXO direct trade flannel small
batch. Four loko helvetica fingerstache shoreditch, pour-over
single-origin coffee tacos kinfolk wayfarers polaroid organic bushwick.
Selfies organic irony, biodiesel semiotics banh mi vegan tattooed
mixtape. Fashion axe shabby chic mumblecore organic meggings. Blue
bottle seitan artisan 3 wolf moon plaid deep v. Before they sold out
pop-up cardigan marfa blog deep v kickstarter austin yr, tumblr plaid.
You probably haven't heard of them pickled hella craft beer, gochujang
try-hard 8-bit stumptown photo booth pitchfork. Meditation ethical
artisan, VHS etsy art party readymade trust fund. Craft beer etsy
selfies poutine chartreuse celiac, pork belly green juice truffaut.
Fixie iPhone ugh migas pop-up, etsy viral cray flannel organic raw denim
helvetica bitters. Williamsburg ramps cliche twee bushwick listicle.
Twee butcher mumblecore wayfarers, before they sold out YOLO chia retro
helvetica fingerstache chicharrones meditation. Kickstarter synth
actually waistcoat. Marfa kickstarter vice thundercats lo-fi banh mi
single-origin coffee freegan, shabby chic etsy butcher brunch. Put a
bird on it selfies master cleanse, food truck pork belly +1 actually DIY
you probably haven't heard of them. Twee occupy paleo umami etsy before
they sold out, pop-up 8-bit pabst slow-carb kogi post-ironic. Mustache
chambray mlkshk freegan crucifix gentrify gastropub, bushwick mixtape.
Scenester banh mi locavore squid, narwhal humblebrag shoreditch tilde
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

<?php
session_start();
if($_SESSION["https://".$_SERVER["HTTP_HOST"]."/sensitive/"] == "permit") :
?>

<!DOCTYPE html>
        <head>

<meta charset="UTF-8">
    <title>Sensitive Resource</title>
    <link rel="stylesheet" href="/login/css/normalize.css">
        <link rel="stylesheet" href="/login/css/style.css">
        <script src="/login/js/jquery.min.js"></script>
        <script src="/login/js/prefixfree.min.js"></script>
<link rel="icon" href="/icon.ico" type="image/x-icon" />
<link rel="shortcut icon" href="/icon.ico" type="image/x-icon" />


        </head>
        <body>

<style type="text/css" media="screen">
a:link { color:#bacbff; text-decoration: underline; }
a:visited { color:#8485e0; text-decoration: underline; }
a:hover { color:#33348e; text-decoration: underline; }
a:active { color:#7476b4; text-decoration: underline; }
</style>

        <h2 style="color:white; text-align:center;">This is a SENSITIVE document</h2> <p> Everyday carry
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

<p>
<a href="doc2.php">And here's a link to an associated document with the same permissions.</a>
</p>

        </body>
</html>

<?php
else:
header('Location: ' . 'https://'.$_SERVER["HTTP_HOST"].'/access_denied.html', true, 303);
die();
endif;
?>

<?php
include_once 'includes/includes.php';
include_once 'includes/common.php';

function get_mime_type($friendlyName){
	// TODO lookup type from cap json
	return "application/dash+xml";
}

function get_caps(){
        $string = file_get_contents(CAPS_FILE);
        $json_a = json_decode($string, true);
        return $json_a;
}

?>
<!DOCTYPE html>
<html>
<head>
   <meta charset=utf-8 />
   <title>mpeg-dash + vjs7 + chromecast demo</title>

   <link href="assets/video-js.css" rel="stylesheet">
   <script src="assets/video.js"></script>
   <script src="assets/silvermine-videojs-chromecast.min.js"></script>
   <link href="assets/silvermine-videojs-chromecast.css" rel="stylesheet">
   <script src="assets/videojs-contrib-quality-levels.min.js"></script>
   <script type="text/javascript" src="assets/cast_sender.js"></script>
   <script type="text/javascript" src="assets/cast_framework.js"></script>

<style>
	body{
background: rgb(51,51,51);
background: radial-gradient(circle, rgba(51,51,51,0.8519782913165266) 0%, rgba(40,108,136,0.8407738095238095) 100%);
	}
.video-js {
  /* The base font size controls the size of everything, not just text.
     All dimensions use em-based sizes so that the scale along with the font size.
     Try increasing it to 15px and see what happens. */
  font-size: 10px;

  /* The main font color changes the ICON COLORS as well as the text */
  color: #fff;
}

/* The "Big Play Button" is the play button that shows before the video plays.
   To center it set the align values to center and middle. The typical location
   of the button is the center, but there is trend towards moving it to a corner
   where it gets out of the way of valuable content in the poster image.*/
.vjs-default-skin .vjs-big-play-button {
  /* The font size is what makes the big play button...big. 
     All width/height values use ems, which are a multiple of the font size.
     If the .video-js font-size is 10px, then 3em equals 30px.*/
  font-size: 3em;

  line-height: 1.5em;
  height: 1.5em;
  width: 3em;

  /* 0.06666em = 2px default */
  border: 0.06666em solid #fff;
  /* 0.3em = 9px default */
  border-radius: 0.3em;

    /* Align center */
    left: 45%;
    top: 50%;
    margin-left: -(3em / 2);
    margin-top: -(1.5em / 2);
}

/* The default color of control backgrounds is mostly black but with a little
   bit of blue so it can still be seen on all-black video frames, which are common. */
.video-js .vjs-control-bar,
.video-js .vjs-big-play-button,
.video-js .vjs-menu-button .vjs-menu-content {
  /* IE8 - has no alpha support */
  background-color: #23aaa1;
  /* Opacity: 1.0 = 100%, 0.0 = 0% */
  background-color: rgba(#23aaa1, 0.7);
}

/* Slider - used for Volume bar and Progress bar */
.video-js .vjs-slider {
  background-color: lighten(#23aaa1, 33%);
  background-color: rgba(lighten(#23aaa1, 33%), 0.5);
}

/* The slider bar color is used for the progress bar and the volume bar
   (the first two can be removed after a fix that's coming) */
.video-js .vjs-volume-level,
.video-js .vjs-play-progress,
.video-js .vjs-slider-bar {
  background: #fff;
}

/* The main progress bar also has a bar that shows how much has been loaded. */
.video-js .vjs-load-progress {
  /* For IE8 we'll lighten the color */
  background: lighten(lighten(#23aaa1, 33%), 25%);
  /* Otherwise we'll rely on stacked opacities */
  background: rgba(lighten(#23aaa1, 33%), 0.5);
}

/* The load progress bar also has internal divs that represent
   smaller disconnected loaded time ranges */
.video-js .vjs-load-progress div {
  /* For IE8 we'll lighten the color */
  background: lighten(lighten(#23aaa1, 33%), 50%);
  /* Otherwise we'll rely on stacked opacities */
  background: rgba(lighten(#23aaa1, 33%), 0.75);
}
</style>
</head>
<body>
<img style="width:150px;" src="images/omni.png"/>
<?php
if(isset($_GET["v"]) && !empty($_GET["v"])):
	$vname = $_GET["v"];
	$caps = get_caps();
	if(isset($caps[$vname])):
?>

<div style="width:80%;height:80%;margin:0 auto;">
   <video id="video_1" class="video-js vjs-default-skin" controls preload="auto" data-setup='{ "fluid": "true" }'>
	<source src="<?php echo LOCAL_PROXY_URL . $vname . "&f=index.mpd"; ?>" type='<?php echo $caps[$vname]["type"]; ?>'>
   </video>
</div>

   <script>
var options = {
   // Must specify the 'chromecast' Tech first
   techOrder: [ 'chromecast', 'html5' ], // Required
   plugins: {chromecast: {receiverAppID: '41A0729C'}}};

   var player = videojs('video_1', options, function() {
			var player = this;
			player.chromecast();
		});

let qualityLevels = player.qualityLevels();

// disable quality levels with less than 720 horizontal lines of resolution when added
// to the list.
//qualityLevels.on('addqualitylevel', function(event) {
//  let qualityLevel = event.qualityLevel;
//  if (qualityLevel.height >= 720) {
//    qualityLevel.enabled = true;
//  } else {
//    qualityLevel.enabled = false;
//  }
//});


// example function that will toggle quality levels between SD and HD, defining and HD
// quality as having 720 horizontal lines of resolution or more
let toggleQuality = (function() {
  let enable720 = true;

  return function() {
    for (var i = 0; i < qualityLevels.length; i++) {
      let qualityLevel = qualityLevels[i];
      if (qualityLevel.height >= 720) {
        qualityLevel.enabled = enable720;
      } else {
        qualityLevel.enabled = !enable720;
      }
    }
    enable720 = !enable720;
  };
})();

let currentSelectedQualityLevelIndex = qualityLevels.selectedIndex; // -1 if no level selected

// Listen to change events for when the player selects a new quality level
qualityLevels.on('change', function() {
  console.log('Quality Level changed!');
  console.log('New level:', qualityLevels[qualityLevels.selectedIndex]);
  alert("Quality level changed - new level: " + qualityLevels[qualityLevels.selectedIndex]["width"] + "x" + qualityLevels[qualityLevels.selectedIndex]["height"]);
});

   </script>


<button onclick="toggleQuality()">Toggle Quality</button>

	<?php else: ?>
		<h1>No entry for provided video argument!</h1>
	<?php endif; ?>

<?php else: ?>
	<h1>Missing video argument!</h1>
<?php endif; ?>
</body>
</html>

<!DOCTYPE html>
<html>
<head>
   <meta charset=utf-8 />
   <title>mpeg-dash + vjs7 + chromecast demo</title>

   <link href="assets/video-js.css" rel="stylesheet">
   <script src="assets/video.js"></script>
   <script src="assets/silvermine-videojs-chromecast.min.js"></script>
   <link href="assets/silvermine-videojs-chromecast.css" rel="stylesheet">
<!--   <script type="text/javascript" src="https://www.gstatic.com/cv/js/sender/v1/cast_sender.js?loadCastFramework=1"></script>-->

   <script src="https://cdn.jsdelivr.net/npm/videojs-contrib-quality-levels@2.0.9/dist/videojs-contrib-quality-levels.min.js"></script>

   <script type="text/javascript" src="assets/cast_sender.js"></script>
   <script type="text/javascript" src="assets/cast_framework.js"></script>
</head>
<body>

<?php

// this stays on vs1.php
function get_mime_type($friendlyName){
	// if .mpd, then return "application/dash+xml"
	return "nope";
}

// we should take friendly name as input, then manually construct the source element
// i.e.: given "hls.m3u8" ==> <source src="https://ctidev4.critical.com/omni/cb.php?v=hls.m3u8" type="application/x-mpegURL">
// then cb.php converts hls.m3u8 into it's Tahoe uri, and serves that transparently

?>

<div style="width:80%;height:80%;margin:0 auto;">
   <video id="video_1" class="video-js vjs-default-skin" controls preload="auto" data-setup='{ "fluid": "true" }'>

<!-- remote mp4 | works -->
<!-- <source src="http://www.caminandes.com/download/03_caminandes_llamigos_1080p.mp4" type="video/mp4"> -->

<!-- remote HLS | works -->
<!--<source src="https://mnmedias.api.telequebec.tv/m3u8/29880.m3u8" type='application/x-mpegURL'>-->

<!-- remote dash | works -->
<!--<source src="https://bitmovin-a.akamaihd.net/content/MI201109210084_1/mpds/f08e80da-bf1d-4e3d-8899-f0f6155f6efa.mpd" type='application/dash+xml'>-->

<!-- local bitmovin mpd, remote bitmovin content | works -->
<!--<source src="https://ctidev4.critical.com/omni/x.mpd" type='application/dash+xml'>-->

<!-- local bitmovin mpd, tahoe content | works -->
<!--<source src="https://ctidev4.critical.com/omni/index.mpd" type='application/dash+xml'>-->

<!-- tahoe bitmovin | works -->
<!--<source src="https://ctidev4.critical.com/omni/cb.php?u=http://ctidev3.critical.com:4623/uri/URI:DIR2-MDMF:pakfpkslyt3iu7flvgnvwe5cgq:fhz3qn3gmixxdozq7u7grnj7gvcmolowlddylzh4e4bi6ywhzpfq/index.mpd" type='application/dash+xml'>-->

<!-- hls tahoe | works? -->
<!--<source src="https://ctidev4.critical.com/omni/cb.php?u=http://ctidev4.critical.com:4624/uri/URI%3ADIR2%3Abdig6srnym2d6vx5s2r4docwpe%3Afkvfgdz4yfhf63qnqh6g7hegl3ifq6jkvr3g6vq776n2uzs53bvq/hls.m3u8" type='application/x-mpegURL'>-->
<!--<source src="https://cti4dev.critical.com/uri/URI%3ADIR2%3Abdig6srnym2d6vx5s2r4docwpe%3Afkvfgdz4yfhf63qnqh6g7hegl3ifq6jkvr3g6vq776n2uzs53bvq/hls.m3u8" type='application/x-mpegURL'>-->


<!-- 720p dash tahoe | doesn't chromecast cat 3 code 3016 "unexpected command, player is in IDLE state so media session ID is not valid yet"-->
<!--<source src="https://ctidev4.critical.com/omni/cb.php?u=http://ctidev4.critical.com:4624/uri/URI%3ADIR2-MDMF%3A7elduqhljdmbajkd2qeas3b67e%3Aixtckyt3qdxhgxybc4etadgnedx6es5tqzdp6qk4n55pqwaazsea/big_buck_bunny_720p_h264_dash.mpd" type='application/dash+xml'>-->
<!--<source src="https://ctidev4.critical.com/omni/cb.php?u=http://ctidev4.critical.com:4624/uri/URI:DIR2-MDMF:x5j4sexics2no2fk2ent53vpdq:j5bgudi43cdhv34gkws7mlnmttdye32rmwp5ywnb2szo6lo4a3fq/bbb720-bento.mpd" type='application/dash+xml'>-->

<!-- 720p dash local | doesn't chromecast cat 3 code 3016 "unexpected command, player is in IDLE state so media session ID is not valid yet"-->
<!--<source src="https://ctidev4.critical.com/omni/big_buck_bunny_720p_h264_dash.mpd" type='application/dash+xml'>-->

<!-- 480p dash tahoe | doesn't chromecast cat 3 code 3016 "unexpected command, player is in IDLE state so media session ID is not valid yet"-->
<!--<source src="https://ctidev4.critical.com/omni/cb.php?u=http://ctidev4.critical.com:4624/uri/URI%3ADIR2-MDMF%3A7elduqhljdmbajkd2qeas3b67e%3Aixtckyt3qdxhgxybc4etadgnedx6es5tqzdp6qk4n55pqwaazsea/big_buck_bunny_480p_h264_dash.mpd" type='application/dash+xml'>-->
<!--<source src="https://ctidev4.critical.com/omni/cb.php?u=http://ctidev4.critical.com:4624/uri/URI:DIR2-MDMF:udogzgxlv5xfygkuyictdt2kue:nccqxo4umtajgrwlw4jitkyj4qc7qklqriwvefcfncl7qvro4f4a/bbb480-bento.mpd" type='application/dash+xml'>-->
<!--iosm vs mp42 -->
<!--<source src="https://ctidev4.critical.com/omni/cb.php?u=http://ctidev4.critical.com:4624/uri/URI:DIR2-MDMF:exu6ybya6naern3mqvei7bqbu4:46ve5vc5lptvylg3ppvul6csqqna5oksrj7czvddshf3bvagjkxa/bbb480-bento.mpd" type='application/dash+xml'>-->

<!-- 480p mp4 tahoe -->
<!--<source src="https://ctidev4.critical.com/omni/cb.php?u=http://ctidev4.critical.com:4624/uri/URI%3ADIR2-MDMF%3A7elduqhljdmbajkd2qeas3b67e%3Aixtckyt3qdxhgxybc4etadgnedx6es5tqzdp6qk4n55pqwaazsea/big_buck_bunny_480p_h264.mp4" type='video/mp4'>-->


<!-- 480p + 720p mp4 Tahoe -->
<source src="https://ctidev4.critical.com/omni/cb.php?u=http://ctidev4.critical.com:4624/uri/URI:DIR2-MDMF:pnvrqr2div7c4r5gcjbs7zcvpe:5kjvyjw4mxhymeqko6qirfa3uax6oiygei666clnfoyqc5m33sfq/index.mpd" type='application/dash+xml'>


   </video>
</div>

   <script>
var options = {
   // Must specify the 'chromecast' Tech first
   techOrder: [ 'chromecast', 'html5' ], // Required
   plugins: {
      chromecast: {
//         receiverAppID: '98A49342' // style
         receiverAppID: '41A0729C' // app
      }
   }
};

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
});

   </script>

</body>
</html>

<!DOCTYPE html>
<html>
<head>
   <meta charset=utf-8 />
   <title>mpeg-dash + vjs7 + chromecast demo</title>

   <link href="assets/video-js.css" rel="stylesheet">
   <script src="assets/video.js"></script>
   <script src="assets/silvermine-videojs-chromecast.min.js"></script>
   <link href="assets/silvermine-videojs-chromecast.css" rel="stylesheet">
   <script type="text/javascript" src="https://www.gstatic.com/cv/js/sender/v1/cast_sender.js?loadCastFramework=1"></script>
</head>
<body>

   <video id="video_1" class="video-js vjs-default-skin" controls preload="auto" data-setup='{ "fluid": "true" }'>

<!-- remote mp4 | works -->
<!-- <source src="http://www.caminandes.com/download/03_caminandes_llamigos_1080p.mp4" type="video/mp4"> -->

<!-- remote HLS | works -->
<!--<source src="https://mnmedias.api.telequebec.tv/m3u8/29880.m3u8" type='application/x-mpegURL'>-->

<!-- remote dash | works -->
<!--<source src="https://bitmovin-a.akamaihd.net/content/MI201109210084_1/mpds/f08e80da-bf1d-4e3d-8899-f0f6155f6efa.mpd" type='application/dash+xml'>-->

<!-- local bitmovin mpd, remote bitmovin content | works -->
<!--<source src="https://ctidev4.critical.com/omni/x.mpd" type='application/dash+xml'>-->

<!-- tahoe bitmovin | works -->
<!--<source src="https://ctidev4.critical.com/omni/cb.php?u=http://ctidev3.critical.com:4623/uri/URI:DIR2-MDMF:pakfpkslyt3iu7flvgnvwe5cgq:fhz3qn3gmixxdozq7u7grnj7gvcmolowlddylzh4e4bi6ywhzpfq/index.mpd" type='application/dash+xml'>-->

<!-- local bitmovin mpd, tahoe content | works -->
<!--<source src="https://ctidev4.critical.com/omni/index.mpd" type='application/dash+xml'>-->

<!-- hls tahoe | works -->
<source src="https://ctidev4.critical.com/omni/cb.php?u=http://ctidev4.critical.com:4624/uri/URI%3ADIR2%3Abdig6srnym2d6vx5s2r4docwpe%3Afkvfgdz4yfhf63qnqh6g7hegl3ifq6jkvr3g6vq776n2uzs53bvq/hls.m3u8" type='application/x-mpegURL'>


<!-- 720p dash tahoe | doesn't chromecast cat 3 code 3016 "unexpected command, player is in IDLE state so media session ID is not valid yet"-->
<!--<source src="https://ctidev4.critical.com/omni/cb.php?u=http://ctidev4.critical.com:4624/uri/URI%3ADIR2-MDMF%3A7elduqhljdmbajkd2qeas3b67e%3Aixtckyt3qdxhgxybc4etadgnedx6es5tqzdp6qk4n55pqwaazsea/big_buck_bunny_720p_h264_dash.mpd" type='application/dash+xml'>-->

<!-- 720p dash local | doesn't chromecast cat 3 code 3016 "unexpected command, player is in IDLE state so media session ID is not valid yet"-->
<!--<source src="https://ctidev4.critical.com/omni/big_buck_bunny_720p_h264_dash.mpd" type='application/dash+xml'>-->

<!-- 480p dash tahoe | doesn't chromecast cat 3 code 3016 "unexpected command, player is in IDLE state so media session ID is not valid yet"-->
<!--<source src="https://ctidev4.critical.com/omni/cb.php?u=http://ctidev4.critical.com:4624/uri/URI%3ADIR2-MDMF%3A7elduqhljdmbajkd2qeas3b67e%3Aixtckyt3qdxhgxybc4etadgnedx6es5tqzdp6qk4n55pqwaazsea/big_buck_bunny_480p_h264_dash.mpd" type='application/dash+xml'>-->

<!-- 480p mp4 tahoe -->
<!--<source src="https://ctidev4.critical.com/omni/cb.php?u=http://ctidev4.critical.com:4624/uri/URI%3ADIR2-MDMF%3A7elduqhljdmbajkd2qeas3b67e%3Aixtckyt3qdxhgxybc4etadgnedx6es5tqzdp6qk4n55pqwaazsea/big_buck_bunny_480p_h264.mp4" type='video/mp4'>-->

   </video>


   <script>
var options;
options = {
   // Must specify the 'chromecast' Tech first
   techOrder: [ 'chromecast', 'html5' ], // Required
   plugins: {
      chromecast: {
//         receiverAppID: '98A49342' // style
         receiverAppID: '41A0729C' // app
      }
   }
};

      videojs('video_1', options, function() {
			var player = this;
			player.chromecast();
		});
   </script>

</body>
</html>

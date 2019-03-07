<!DOCTYPE html>
<html>
<head>
   <meta charset=utf-8 />
   <title>mpeg-dash + vjs7 + chromecast demo</title>

   <link href="video-js.css" rel="stylesheet">
   <script src="video.js"></script>
   <script src="silvermine-videojs-chromecast.min.js"></script>
   <link href="silvermine-videojs-chromecast.css" rel="stylesheet">
   <script type="text/javascript" src="https://www.gstatic.com/cv/js/sender/v1/cast_sender.js?loadCastFramework=1"></script>
</head>
<body>

   <video id="video_1" class="video-js vjs-default-skin" controls preload="auto" data-setup='{ "fluid": "true" }'>

 <source src="av1.webm" type="video/webm">

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

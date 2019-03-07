<?php
session_start();
 if($_SESSION["https://".$_SERVER["HTTP_HOST"]."/omni/"] == "permit") :?> 

<script src="assets/hls.js"></script>

<video controls style="width:100%;height:100%" id="video"></video>
<script>
//  var url = "https://ctidev4.critical.com/omni/master.m3u8";
//  var url = "https://ctidev4.critical.com/omni/cb.php?u=http://ctidev4.critical.com:4624/uri/URI:CHK:uzbwjiqyngtz27eveydjbrlcwm:cd4ubpp7zez5j7r5lxnxxyfma7eikfvxdwrs3gfc6gsmd7w6jgrq:3:5:1877";
//  var url = "https://ctidev4.critical.com/omni/cb.php?u=http://ctidev4.critical.com:4624/uri/URI:CHK:e6eweo7ju3wh42ryc242cqskpu:5rfdlxkt4opl4d4w5nnahzwnk7tfrksbfxtcqowzd35g6eirne2q:3:5:1878";
  var url = "https://ctidev4.critical.com/omni/cb.php?u=http://ctidev4.critical.com:4624/uri/URI:CHK:yo2ajl52nlox4bkvqixhfbbmtu:kid4phsx27whbgff6momwif65lt5ozl5g3rkkmcchkrfqpwbxqla:3:5:1877";
  var video = document.getElementById('video');
  if(Hls.isSupported()) {
    var hls = new Hls();
    hls.loadSource(url);
    hls.attachMedia(video);
    hls.on(Hls.Events.MANIFEST_PARSED,function() {
      video.play();
  });
 }
 // hls.js is not supported on platforms that do not have Media Source Extensions (MSE) enabled.
 // When the browser has built-in HLS support (check using `canPlayType`), we can provide an HLS manifest (i.e. .m3u8 URL) directly to the video element throught the `src` property.
 // This is using the built-in support of the plain video element, without using hls.js.
 // Note: it would be more normal to wait on the 'canplay' event below however on Safari (where you are most likely to find built-in HLS support) the video.src URL must be on the user-driven
 // white-list before a 'canplay' event will be emitted; the last video event that can be reliably listened-for when the URL is not on the white-list is 'loadedmetadata'.
  else if (video.canPlayType('application/vnd.apple.mpegurl')) {
    video.src = url;
    video.addEventListener('loadedmetadata',function() {
      video.play();
    });
  }
</script>

 <?php
   else:
   header('Location: ' . 'https://'.$_SERVER["HTTP_HOST"].'/access_denied.html', true, 303);
   die();
   endif; ?>


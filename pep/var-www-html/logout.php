<?php
session_start();
function redirect($url, $statusCode = 303) {
   header('Location: ' . $url, true, $statusCode);
   die();
}
session_destroy();
redirect("https://".$_SERVER["HTTP_HOST"]);

?>

<?php
include_once 'includes/includes.php';
include_once 'includes/common.php';

function parseHeaders( $headers ){
    $head = array();
    foreach( $headers as $k=>$v ){
        $t = explode( ':', $v, 2 );
       	if( isset( $t[1] ) )
            $head[ trim($t[0]) ] = trim( $t[1] );
       	else{
            $head[] = $v;
       	    if( preg_match( "#HTTP/[0-9\.]+\s+([0-9]+)#",$v, $out ) )
               	$head['response_code'] = intval($out[1]);
        }
   }
    return $head['response_code'];
}

function get_caps(){
	$string = file_get_contents(CAPS_FILE);
	$json_a = json_decode($string, true);
	return $json_a;
}


function convertFriendlyToCap($friendlyName){
	$caps = get_caps();
	if(isset($caps[$friendlyName])){
        	return $caps[$friendlyName]["dircap"];
	}else{
		alert_msg("CAPS json did not have a key matching: ". $friendlyName);
	}
}

function createUrl($cap, $file){
	return LOCAL_TAHOE_URL . $cap . "/" . $file;
}

if(isset($_GET["v"]) && !empty($_GET["v"]) && isset($_GET["f"]) && !empty($_GET["f"])){
	$cap = convertFriendlyToCap($_GET["v"]);
	$externalURL = createUrl($cap, $_GET["f"]);
	$externalData = file_get_contents($externalURL);
	$rc = parseHeaders($http_response_header);
        if($rc == "200" || $rc == "201"){
		foreach ($http_response_header as $value) {

		    if (preg_match('/^Content-Type:/i', $value)) {
		        // Successful match
			$ext = pathinfo($_GET["f"], PATHINFO_EXTENSION);
			if($ext == "mpd"){
				header("Content-Type: application/dash+xml");
			}elseif($ext == "m3u8"){
				header("Content-Type: application/x-mpegURL");
			}else{
			        header($value,false);
			}
		    }
		}
                echo $externalData;
        }else{
                header("HTTP/1.1 ".$rc." Error");
        }
}
?>

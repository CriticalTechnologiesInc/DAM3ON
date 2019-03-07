<?php


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


$friendly_to_cap = array(
    "hls.m3u8" => "URI%3ADIR2%3Abdig6srnym2d6vx5s2r4docwpe%3Afkvfgdz4yfhf63qnqh6g7hegl3ifq6jkvr3g6vq776n2uzs53bvq",
    "index.mpd" => "URI:DIR2-MDMF:pakfpkslyt3iu7flvgnvwe5cgq:fhz3qn3gmixxdozq7u7grnj7gvcmolowlddylzh4e4bi6ywhzpfq"
);

$local_tahoe_url = "https://cti4dev.critical.com/uri/";
$local_proxy_url = "https://ctidev4.critical.com/omni/cb.php?u=";

function convertFriendlyToTahoeUrl($friendlyName){
        $url = $local_tahoe_url . $friendly_to_cap[friendlyName] . "/" . $friendlyName;
        return $url;
}

if(isset($_GET["u"]) && !empty($_GET["u"])){

//	$externalURL = convertFriendlyToTahoeUrl($_GET["u"]);

	$externalURL = $_GET["u"];
	$externalData = file_get_contents($externalURL);
	$rc = parseHeaders($http_response_header);
        if($rc == "200" || $rc == "201"){
		foreach ($http_response_header as $value) {
//		    if ( preg_match( '/^(?:Content-Type|Content-Language|Accept-Ranges|ETag|Last-Modified|Set-Cookie):/i', $value ) ) {
//		    	header( $header, false );
//		    }

		    if (preg_match('/^Content-Type:/i', $value)) {
		        // Successful match
			$ext = pathinfo($externalURL, PATHINFO_EXTENSION);
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
                header("HTTP/1.0 ".$rc." Not Found");
        }
}
?>

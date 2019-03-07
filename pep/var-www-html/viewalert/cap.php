<xsl:stylesheet version = "1.0" xmlns:xsl = "http://www.w3.org/1999/XSL/Transform" xmlns:cap="urn:oasis:names:tc:emergency:cap:1.2">
  <xsl:template match="/">
    <html>
      <xsl:for-each select="cap:alert">
        <head>
          <title>
            <xsl:value-of select="cap:status" />
          </title>
        </head>
        <body>
          <style type="text/css">
            body{
              font-family:Arial;
              font-size:12pt;
              background-color:555555;
            }
            p{
              font-family:Arial;
              font-size:10pt;
              color:white;
            }
            .info{
              background-color:#808080;
              border-style:solid;
              border-width:3px;
              padding:5px;
              clear:both;
            }
              .info p{
              color:black;
            }
            .alert{
              border: 5px solid #CCCDBB;
              padding:50px;
              width:65%;
              margin:auto;
            }
            .topTable{
              font-family:Arial;
            }
              .topTable td{
              color:white;
              width:100%;
            }
            .label {
              font-weight: bold;
              vertical-align: text-top;
              text-align: right;
              padding-left:5%;
              padding-right:5%;
              color:white;
              width: 25%;
            }
            td {
              width:75%;
              color:white;
            }
            a {
              word-wrap:break-word;
            }
            table{
              table-layout:fixed;
              width:100%;
            }
            .box{
              background-color:grey;
              text-color:white;
              border: 2px solid black;
              width:initial;
            }
            .label2{
              width: 25%;
              font-weight: bold;
              vertical-align: text-top;
              text-align: right;
            }
            .resources{
              width:initial;
            }
            #map-canvas {
              width: auto;
              height: 500px;
            }
            #polygon {
              color: #222;
            }
            .lngLat {
              color: #fff;
              margin-bottom: 5px;
            }
            .lngLat .one {
              padding-left: 250px;
            }
            .lngLat .two {
              padding-left: 34px;
            }
            #clipboard-btn {
              float: left;
              margin-right: 10px;
              padding: 6px 8px;
              -moz-border-radius: 3px;
              -webkit-border-radius: 3px;
              border-radius: 3px;
            }
            #polygon {
              height: 70px;
              border: solid 2px #eee;
              -moz-border-radius: 3px;
              -webkit-border-radius: 3px;
              border-radius: 3px;
              -moz-box-shadow: inset 0 2px 5px #444;
              -webkit-box-shadow: inset 0 2px 5px #444;
              box-shadow: inset 0 2px 5px #444;
            }
            #polygon, .lngLat {
              font-family: arial, sans-serif;
              font-size: 12px;
              padding-top: 10px;
            }
          </style>
          <br/>
          <br/>
          <div class="alert">
            <h1 style="font-size:16pt;color:bisque;text-align:center;">
              <a style="color:#9a7bb7;" href="https://<?php echo $_SERVER["HTTP_HOST"];?>/viewalert/index.php?msgid={cap:identifier}">Alert ID:<xsl:value-of select="cap:identifier" /></a>
            </h1>
            <table class="topTable">
              <tbody>
                <tr class="aInfo">
                  <td>
                    <table>
                      <tr>
                        <td class="label">Sent:</td>
                        <td>
                          <xsl:value-of select="cap:sent" />
                        </td>
                      </tr>
                      <tr>
                        <td class="label">Sender:</td>
                        <td>
                          <xsl:value-of select="cap:sender" />
                        </td>
                      </tr>
                      <tr>
                        <td class="label">Scope:</td>
                        <td>
                          <xsl:value-of select="cap:scope" />
                        </td>
                      </tr>
                    </table>
                  </td>
                  <td>
                    <table>
                      <tr>
                        <td class="label">Status: </td>
                        <td>
                          <xsl:choose>
                            <xsl:when test="cap:status[text()='Actual']">
                              <span style="color:red;font-weight:bold;">
                                <xsl:value-of select="cap:status" />
                              </span>
                            </xsl:when>
                            <xsl:when test="cap:status[text()='Exercise']">
                              <span style="color:red;font-weight:bold;">
                                <xsl:value-of select="cap:status" />
                              </span>
                            </xsl:when>
                            <xsl:otherwise>
                              <xsl:value-of select="cap:status" />
                            </xsl:otherwise>
                          </xsl:choose>
                        </td>
                      </tr>
                      <tr>
                        <td class="label">Message Type:</td>
                        <td>
                          <xsl:value-of select="cap:msgType" />
                        </td>
                      </tr>
                      <tr>
                        <td class="label">Code:</td>
                        <td>
                          <xsl:value-of select="cap:code" />
                        </td>
                      </tr>
                    </table>
                  </td>
                </tr>
              </tbody>
            </table>

            <xsl:for-each select="cap:info">
              <div class="info">
                <table>
                  <tr>
                    <td class="label">Event:</td>
                    <td>
                      <strong>
                        <em>
                          <xsl:value-of select="cap:event" />
                        </em>
                      </strong>
                    </td>
                  </tr>

                  <xsl:if test="cap:headline">
                    <tr>
                      <td class="label">Headline:</td>
                      <td>
                        <xsl:value-of select="cap:headline" />
                      </td>
                    </tr>
                  </xsl:if>

                  <tr>
                    <td class="label">Expires:</td>
                    <td>
                      <xsl:value-of select="cap:expires" />
                    </td>
                  </tr>

                  <tr>
                    <td class="label">Urgency:</td>
                    <td>
                      <xsl:choose>
                        <xsl:when test="cap:urgency[text()='Immediate']">
                          <span style="color:red;font-weight:bold;">
                            <xsl:value-of select="cap:urgency" />
                          </span>
                        </xsl:when>
                        <xsl:when test="cap:urgency[text()='Expected']">
                          <span style="color:orange;font-weight:bold;">
                            <xsl:value-of select="cap:urgency" />
                          </span>
                        </xsl:when>
                        <xsl:when test="cap:urgency[text()='Future']">
                          <span style="color:yellow;font-weight:bold;">
                            <xsl:value-of select="cap:urgency" />
                          </span>
                        </xsl:when>
                        <xsl:when test="cap:urgency[text()='Past']">
                          <span style="color:yellowgreen;font-weight:bold;">
                            <xsl:value-of select="cap:urgency" />
                          </span>
                        </xsl:when>
                        <xsl:otherwise>
                          <span style="color:blue;font-weight:bold;">
                            <xsl:value-of select="cap:urgency" />
                          </span>
                        </xsl:otherwise>
                      </xsl:choose>
                    </td>
                  </tr>

                  <tr>
                    <td class="label">Certainty:</td>
                    <td>
                      <xsl:choose>
                        <xsl:when test="cap:certainty[text()='Observed']">
                          <span style="color:red;font-weight:bold;">
                            <xsl:value-of select="cap:certainty" />
                          </span>
                        </xsl:when>
                        <xsl:when test="cap:certainty[text()='Likely']">
                          <span style="color:orange;font-weight:bold;">
                            <xsl:value-of select="cap:certainty" />
                          </span>
                        </xsl:when>
                        <xsl:when test="cap:certainty[text()='Possible']">
                          <span style="color:yellow;font-weight:bold;">
                            <xsl:value-of select="cap:certainty" />
                          </span>
                        </xsl:when>
                        <xsl:when test="cap:certainty[text()='Unlikely']">
                          <span style="color:yellowgreen;font-weight:bold;">
                            <xsl:value-of select="cap:certainty" />
                          </span>
                        </xsl:when>
                        <xsl:otherwise>
                          <span style="color:blue;font-weight:bold;">
                            <xsl:value-of select="cap:certainty" />
                          </span>
                        </xsl:otherwise>
                      </xsl:choose>
                    </td>
                  </tr>

                  <tr>
                    <td class="label">Severity:</td>
                    <td>
                      <xsl:choose>
                        <xsl:when test="cap:severity[text()='Extreme']">
                          <span style="color:red;font-weight:bold;">
                            <xsl:value-of select="cap:severity" />
                          </span>
                        </xsl:when>
                        <xsl:when test="cap:severity[text()='Severe']">
                          <span style="color:orange;font-weight:bold;">
                            <xsl:value-of select="cap:severity" />
                          </span>
                        </xsl:when>
                        <xsl:when test="cap:severity[text()='Moderate']">
                          <span style="color:yellow;font-weight:bold;">
                            <xsl:value-of select="cap:severity" />
                          </span>
                        </xsl:when>
                        <xsl:when test="cap:severity[text()='Minor']">
                          <span style="color:yellowgreen;font-weight:bold;">
                            <xsl:value-of select="cap:severity" />
                          </span>
                        </xsl:when>
                        <xsl:otherwise>
                          <span style="color:blue;font-weight:bold;">
                            <xsl:value-of select="cap:severity" />
                          </span>
                        </xsl:otherwise>
                      </xsl:choose>
                    </td>
                  </tr>

		  <xsl:if test="cap:responseType">
                    <tr>
                      <td class="label">Response Type:</td>
                      <td>
                        <xsl:value-of select="cap:responseType" />
                      </td>
                    </tr>
		  </xsl:if>

                  <tr>
                    <td class="label">SAME Event Code:</td>
                    <td>
                      <xsl:value-of select="cap:eventCode/cap:value" />
                    </td>
                  </tr>

                  <xsl:if test="cap:description">
                    <tr>
                      <td class="label" style="vertical-align:text-top;">Description:</td>
                      <td>
                        <xsl:value-of select="cap:description" />
                      </td>
                    </tr>
                  </xsl:if>

                  <xsl:if test="cap:instruction">
                    <tr>
                      <td class="label">Instructions:</td>
                      <td>
                        <xsl:value-of select="cap:instruction" />
                      </td>
                    </tr>
                  </xsl:if>

                  <tr>
                    <td class="label">Category:</td>
                    <td>
                      <xsl:for-each select="cap:category">
                        <xsl:value-of select="." />
                        <xsl:if test="position() != last()">, </xsl:if>
                      </xsl:for-each>
                    </td>
                  </tr>

                  <xsl:if test="../cap:references">
                    <tr>
                      <td class="label">References:</td>
                      <td>
                        <table>
                          <xsl:call-template name="parse_refs">
                            <xsl:with-param name="list">
                              <xsl:value-of select="../cap:references" />
                            </xsl:with-param>
                          </xsl:call-template>
                        </table>
                      </td>
                    </tr>
                  </xsl:if>

                  <tr class="areas">
                    <td class="label" style="vertical-align:text-top;">Area(s) Affected:</td>
                    <xsl:for-each select="cap:area">
                      <td>
                        <table class="box">
                          <tr>
                            <td class="label2">Description:</td>
                            <td>
                              <xsl:value-of select="cap:areaDesc" />
                            </td>
                          </tr>

                          <xsl:if test="cap:geocode">
                            <tr>
                              <td class="label2">Geocodes:</td>
                              <td>
                              	<xsl:for-each select="cap:geocode">
                                	<xsl:value-of select="." />
					<xsl:if test="position()!= last()">, </xsl:if>
                                </xsl:for-each>
                             </td>
                            </tr>
                          </xsl:if>

                          <xsl:if test="cap:polygon">
                            <tr>
                              <td class="label2">Coordinates:</td>
                              <td style="font-size:small;">
			      <xsl:for-each select="cap:polygon">
                                <xsl:value-of select="." />
				<xsl:if test="position()!=last()">; </xsl:if>
			      </xsl:for-each>
                              </td>
                            </tr>
                          </xsl:if>
                          <div id="map" style="">
                            <div id="map-canvas"></div>
                            <script src='https://maps.googleapis.com/maps/api/js?v=3.exp&amp;key=AIzaSyD_jdtG9i87ryx3-wXAtVv1ZFyEA9MrapY'></script>
                            <script src='states.json'></script>
			    <script type="text/javascript">
                              function initialize(coords) {
                                var coordsList = coords.split(";");
                                var gPolys = [];
                                var maxLat = -90;
                                var minLat = 90;
                                var maxLng = -180;
                                var minLng = 180;
                                for(i=0; i &lt; coordsList.length; i++){
                                  splitCoordinates = coordsList[i].split(" ");
                                  gCoords = [];
                                  for(count = 0; count &lt; splitCoordinates.length; count++){
                                    splits = splitCoordinates[count].split(",");
                                    //convert splitCoords to floats
                                    fLat = parseFloat(splits[0]);
                                    fLng = parseFloat(splits[1]);
                                    if(fLat &lt; minLat){
                                      minLat = fLat;
                                    }
                                    if(fLat &gt; maxLat){
                                      maxLat = fLat;
                                    }
                                    if(fLng &lt; minLng){
                                      minLng = fLng;
                                    }
                                    if(fLng &gt; maxLng){
                                      maxLng = fLng;
                                    }

                                    splitCoordinates[count]=[fLat, fLng];
                                    gCoords[count] = new google.maps.LatLng(splitCoordinates[count][0],splitCoordinates[count][1]);
                                  }
                                  gPolys[i] = new google.maps.Polygon({
                                    paths: gCoords,
                                    draggable: false,
                                    editable: false,
                                    strokeColor: '#0000FF',
                                    strokeOpacity: 0.8,
                                    strokeWeight: 2,
                                    fillColor: '#0000FF',
                                    fillOpacity: 0.35
                                  });
                                }

                                bigPoly = new google.maps.Polygon({
                                  paths: [new google.maps.LatLng(minLat,minLng), new google.maps.LatLng(minLat,maxLng), new google.maps.LatLng(maxLat,maxLng), new google.maps.LatLng(maxLat,minLng)],
                                  draggable: false,
                                  editable: false,
                                  strokeColor: '#0000FF',
                                  strokeOpacity: 0.8,
                                  strokeWeight: 2,
                                  fillColor: '#0000FF',
                                  fillOpacity: 0.35
                                });

                                // General Options
                                var mapOptions = {
                                  draggable: true,
                                  mapTypeId: google.maps.MapTypeId.RoadMap
                                };

                                var map2 = new google.maps.Map(document.getElementById('map-canvas'),mapOptions);

                                map2.fitBounds(bigPoly.getBounds());
                                for(i=0; i &lt; gPolys.length; i++){
                                  gPolys[i].setMap(map2);
                                  var center = gPolys[i].getBounds().getCenter();
                                  new google.maps.Marker({
                                    position : center,
                                    map : map2,
                                  });
                                }

                                //google.maps.event.addListener(myPolygon, "dragend", getPolygonCoords);
                                //google.maps.event.addListener(myPolygon.getPath(), "insert_at", getPolygonCoords);
                                //google.maps.event.addListener(myPolygon.getPath(), "remove_at", getPolygonCoords);
                                //google.maps.event.addListener(myPolygon.getPath(), "set_at", getPolygonCoords);
                                return map2;
                              }

                              google.maps.Polygon.prototype.getBounds = function() {
                                var bounds = new google.maps.LatLngBounds();
                                var paths = this.getPaths();
                                var path;
                                for (var i = 0; i &lt; paths.getLength(); i++) {
                                  path = paths.getAt(i);
                                  for (var ii = 0; ii &lt; path.getLength(); ii++) {
                                    bounds.extend(path.getAt(ii));
                                  }
                                }
                                return bounds;
                              }

                              <?php
                                session_start();

                                $string = file_get_contents("/var/www/html/viewalert/big.json");
                                $json_a = json_decode($string, true);

                                if(isset($_SESSION['same'])){
				  $geocodes = $_SESSION['same'];
				  $polys = [];
				  for($idx=0; $idx < count($geocodes); $idx++){
					$polys[$idx] = $json_a[$geocodes[$idx]];
				  }

                                  if($polys === []){
                                    echo 'var polysToPlot = []';
                                  }else{
                                    $json_b = json_encode($polys);
                                    echo 'var polysToPlot = '.$json_b.';';
                                  }
                                  echo "";
                                }else{
                                  echo 'var polysToPlot = []';
                                }
                              ?>
                              <xsl:if test="cap:polygon">
				<xsl:for-each select="cap:polygon">
					console.log('<xsl:value-of select="."/>');
	                                polysToPlot[polysToPlot.length] = '<xsl:value-of select="."/>';
				</xsl:for-each>
                              </xsl:if>
			      <xsl:if test="cap:geocode">
				<xsl:for-each select="cap:geocode">
					<xsl:if test="./cap:valueName = 'FIPS'">
						var fips = '<xsl:value-of select="./cap:value"/>';
						polysToPlot[polysToPlot.length] = states[fips];
					</xsl:if>
				</xsl:for-each>
			      </xsl:if>
				console.log(polysToPlot.length);
                              var polygonsString = "";
                              for(idx = 0; idx &lt; polysToPlot.length; idx ++){
                                polygonsString = polygonsString + polysToPlot[idx] + ";"
                              }

                              if(polygonsString != ""){
                                initialize(polygonsString);
                              }else{
                                document.getElementById("map").style = "display:none;";
                              }
                            </script>
                          </div>
                        </table>
                      </td>
                    </xsl:for-each>
                  </tr>

                  <tr>
                    <td class="label">Sender Name:</td>
                    <td>
                      <xsl:value-of select="cap:senderName" />
                    </td>
                  </tr>

                  <tr>
                    <td class="label">URL:</td>
                    <td>
                      <a href="{cap:web}">
                        <xsl:value-of select="cap:web"/>
                      </a>
                    </td>
                  </tr>

                  <tr>
                    <td class="label">Contact:</td>
                    <td>
                      <xsl:value-of select="cap:contact" />
                    </td>
                  </tr>

                  <!-- jeremy was here -->
                  <xsl:for-each select="cap:parameter">
                    <tr>
                      <td class="label">
                        <xsl:choose>
                          <xsl:when test="cap:valueName='listname'">
                            Listname
                          </xsl:when>
                          <xsl:when test="cap:valueName='auth-web'">
                            Auth Web
                          </xsl:when>
                          <xsl:otherwise>
                            <xsl:value-of select="cap:valueName"/>
                          </xsl:otherwise>
                        </xsl:choose>
                      </td>
                      <td>
                        <xsl:choose>
                          <xsl:when test="cap:valueName='auth-web'">
                            <a href="{cap:value}">
                              <xsl:value-of select="cap:value"/>
                            </a>
                          </xsl:when>
                          <xsl:otherwise>
                            <xsl:value-of select="cap:value"/>
                          </xsl:otherwise>
                        </xsl:choose>
                      </td>
                    </tr>
                  </xsl:for-each>

                  <xsl:if test="cap:resource">
                    <tr>
                      <td class="label" style="vertical-align:text-top;">Resources:</td>
                      <td>
                        <table class="box">
                          <xsl:for-each select="cap:resource">
                            <tr>
                              <td class="label2">Description:</td>
                              <td>
                                <xsl:value-of select="cap:resourceDesc" />
                              </td>
                            </tr>
                            <tr>
                              <td class="label2">MIME Type:</td>
                              <td>
                                <xsl:value-of select="cap:mimeType" />
                              </td>
                            </tr>
                            <xsl:if test="cap:uri">
                              <tr>
                                <td class="label2">URI:</td>
                                <td>
                                  <a href="{cap:uri}">
                                    <xsl:value-of select="cap:uri" />
                                  </a>
                                </td>
                              </tr>
                            </xsl:if>
                            <xsl:if test="cap:size">
                              <tr>
                                <td class="label2">Size:</td>
                                <td>
                                  <xsl:value-of select="cap:size" />
                                </td>
                              </tr>
                            </xsl:if>
                          </xsl:for-each>
                        </table>
                      </td>
                    </tr>
                  </xsl:if>
                </table>
              </div>
            </xsl:for-each>
          </div>
        </body>
      </xsl:for-each>
    </html>
  </xsl:template>

  <xsl:template name="parse_refs">
    <xsl:param name="list" />
    <xsl:variable name="newlist" select="concat(normalize-space($list),' ')" />
    <xsl:variable name="first" select="substring-before($newlist,' ')" />
    <xsl:variable name="remaining" select="substring-after($newlist, ' ')" />
    <tr>
      <xsl:call-template name="add_link">
        <xsl:with-param name="ref" select="$first" />
        <xsl:with-param name="count" select="0" />
      </xsl:call-template>
    </tr>
    <xsl:if test="$remaining">
      <xsl:call-template name="parse_refs">
        <xsl:with-param name="list" select="$remaining" />
      </xsl:call-template>
    </xsl:if>
  </xsl:template>

  <xsl:template name="add_link">
    <xsl:param name="ref" />
    <xsl:param name="count" />
    <xsl:variable name="piece" select="substring-before($ref,',')" />
    <xsl:variable name="rest" select="substring-after($ref,',')" />
    <xsl:choose>
      <xsl:when test="$count=0">
        <td style="font-size:small;">
          <xsl:value-of select="$piece" />
        </td>
      </xsl:when>
      <xsl:when test="$count=2">
        <td style="font-size:small;">
          <xsl:value-of select="$ref" />
        </td>
      </xsl:when>
      <xsl:otherwise>
        <td style="font-size:small;">
          <a href="https://<?php echo $_SERVER["HTTP_HOST"];?>/viewalert/index.php?msgid={$piece}">
            <xsl:value-of select="$piece" />
          </a>
        </td>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:if test="$count &lt; 2">
      <xsl:call-template name="add_link">
        <xsl:with-param name="ref" select="$rest" />
        <xsl:with-param name="count" select="$count+1" />
      </xsl:call-template>
    </xsl:if>
  </xsl:template>
</xsl:stylesheet>

var statesPlotted = {};
var users_location_is_drawn = false;
// onchange function for selecting states
$('#fipscode').change(function() {
	var selectedValues = $('#fipscode').val();		// get states currently selected (by FIPS code) ex: ['01','08','17']
	if(selectedValues == null){ selectedValues = []; }	// if none currently selected set list to be empty rather than undefined
	var polysToDraw = [];

	// For each selected FIPS, make sure we haven't already plotted it, if we haven't: add it to a list of states that need drawn
	for(var i=0; i < selectedValues.length; i++){
		if(Object.keys(statesPlotted).indexOf(selectedValues[i])==-1){
			polysToDraw.push(selectedValues[i]);
		}
	}
	var polysToRemove = [];

	// For each state currently drawn, check to see if it is still selected, if it is NOT still selected: add it to a list of states to be removed from the map
	for(var i=0; i < Object.keys(statesPlotted).length; i++){
		var spKeys = Object.keys(statesPlotted);
		if(selectedValues.indexOf(spKeys[i])==-1){
			polysToRemove.push(spKeys[i]);
		}
	}

	// For each state that needs drawn, draw it
	for(var i=0; i < polysToDraw.length; i++){
		var points = states[polysToDraw[i]].split(" ");
		var gPoints = [];
		// make google points
		for(var j=0; j < points.length; j++){
			var tmp = points[j].split(",");
			gPoints.push(new google.maps.LatLng(tmp[0],tmp[1]));
		}
		// create the google polygon, not draggable, not editable, not removable (by right-click), and not added to CAP
		var statePolyId = plotPoly(gPoints,false,false,false,false);
		var fips = polysToDraw[i];
		// add state to list of drawn states
		statesPlotted[fips]=statePolyId;
	}

	// For each polygon that needs removed, remove it
	for(var i=0; i<polysToRemove.length; i++){
		var polyId = statesPlotted[polysToRemove[i]];
		console.log("polyid = " + polyId);
		polysOnMap[polyId].setMap(null);	// remove it from the map
//		polysOnMap.splice(polyId,1);
		polysOnMap[polyId]=null;		// remove it from list of polygons on the map
		delete statesPlotted[polysToRemove[i]];	// remove it from list of states drawn
	}
	// update map bounds so map is centered around all polygons currently mapped
	map.fitBounds(getTotalBounds());
	getPolygonCoords(polysOnMap);

});

function plotDefaultPolygon() {
        //43.09831,-75.20406
	//43.09672,-75.20406
 	//43.09687,-75.2082
	//43.09851,-75.21019
	//43.09947,-75.20854
        var gPoints = [];
	gPoints.push(new google.maps.LatLng(43.09831,-75.20406));
	gPoints.push(new google.maps.LatLng(43.09672,-75.20406));
	gPoints.push(new google.maps.LatLng(43.09687,-75.2082));
	gPoints.push(new google.maps.LatLng(43.09851,-75.21019));
	gPoints.push(new google.maps.LatLng(43.09947,-75.20854));
        plotPoly(gPoints,false,false,true,true);
}

google.maps.Polygon.prototype.getBounds = function() {
        var bounds = new google.maps.LatLngBounds();
        var paths = this.getPaths();
        var path;
        for (var i = 0; i < paths.getLength(); i++) {
          path = paths.getAt(i);
          for (var ii = 0; ii < path.getLength(); ii++) {
            bounds.extend(path.getAt(ii));
          }
        }
        return bounds;
};

function enableBlur(){
        // Enabled spinny blur
        document.getElementById('container').hidden = false;
        document.getElementById('maindiv').style = "-webkit-filter: blur(2px);-moz-filter: blur(2px);-ms-filter: blur(2px);-o-filter: blur(2px);filter: blur(2px);"
	var body = document.body, html = document.documentElement;
	var height = Math.max( body.scrollHeight, body.offsetHeight,
                       html.clientHeight, html.scrollHeight, html.offsetHeight );
	var x = (height/2)-(window.innerHeight/2);
	scrollTo(0,x);
//	var rec = document.getElementById('container').getBoundingClientRect();
//	scrollTo(0,rec.bottom+(window.innerHeight/2));
}
function disableBlur(){
        // hide blur and spinny icon thing
        document.getElementById('container').hidden = true;
        document.getElementById('maindiv').style = "";
}


function plotPoly(polygon, drag, edit, remove, addPolyToCap, userslocation = false) {
                var userPoly = new google.maps.Polygon({
                        paths: polygon,
                        draggable: drag,
                        editable: edit,
                        strokeColor: '#FF0000',
                        strokeOpacity: 0.8,
                        strokeWeight: 2,
                        fillColor: '#FF0000',
                        fillOpacity: 0.35,
                        justinsID: polysOnMap.length,
			cap: addPolyToCap
                });
		if(remove){
	                userPoly.addListener('rightclick', function(){
				if(userslocation){
					users_location_is_drawn = false;
				}
                        	var id = userPoly.justinsID;
                	        polysOnMap[id].setMap(null);
//				polysOnMap.splice(id,1);
        	                polysOnMap[id]=null;
	                       	getPolygonCoords(polysOnMap);
				map.fitBounds(getTotalBounds());
                	});
		}
                polysOnMap[userPoly.justinsID] = userPoly;
                userPoly.setMap(map);
		map.fitBounds(getTotalBounds());

		function dummy(){
			getPolygonCoords(polysOnMap);
		}
		// these listeners update the textarea when moving or editing a polygon
		if(edit || drag){
			google.maps.event.addListener(userPoly.getPath(), "insert_at", dummy);
			google.maps.event.addListener(userPoly.getPath(), "set_at", dummy);
		}
		getPolygonCoords(polysOnMap);
		return userPoly.justinsID;
};

// function that gets bounds for ALL polygons currently on the map so that all can be visible at once
function getTotalBounds(){
	var totalBounds;
	totalBounds = new google.maps.LatLngBounds();
	var notNulls = 0;
	for(var c=0;c<polysOnMap.length;c++){
		if(polysOnMap[c]!=null){
			var polyBounds = polysOnMap[c].getBounds();
			totalBounds.extend(polyBounds.getNorthEast());
			totalBounds.extend(polyBounds.getSouthWest());
			notNulls++;
		}
	}
	if(notNulls==0){
		totalBounds = new google.maps.LatLngBounds(new google.maps.LatLng(20.022416577253143, -135.814453125), new google.maps.LatLng(53.98679846534042, -56.185546875));
		console.log("totalBounds: " + totalBounds);
	}
	return totalBounds;
}


function getLocation() {
	if(users_location_is_drawn){
		return;
	}
	var startPos;
  	var geoOptions = {
		maximumAge: 5 * 60 * 1000,
  	}
	var geoSuccess = function(position) {
		startPos = position;
		var coords = []
		var lat = startPos.coords.latitude;
		var long = startPos.coords.longitude;
  		var userGPoints = [new google.maps.LatLng(lat-.05, long-.1),new google.maps.LatLng(lat+.05, long-.1),new google.maps.LatLng(lat+.05, long+.1),new google.maps.LatLng(lat-.05, long+.1),new google.maps.LatLng(lat-.05, long-.1)];
		plotPoly(userGPoints, true, true, true, true, true);
		users_location_is_drawn=true;
	};

	var geoError = function(error) {
		plotDefaultPolygon();
		console.log('Error occurred. Error code: ' + error.code);
		// error.code can be:
		//   0: unknown error
		//   1: permission denied
		//   2: position unavailable (error response from location provider)
	    	//   3: timed out
  	};

	// check for Geolocation support
	if (navigator.geolocation) {
		navigator.geolocation.getCurrentPosition(geoSuccess, geoError, geoOptions);
	}else {
		alert("Geolocation is not supported on your device!");
	}
};



// Polygon Coordinates
var polysOnMap = [];
  var myLatLng = new google.maps.LatLng(39, -96);

  var mapOptions = {
    zoom: 4,
    center: myLatLng,
    mapTypeId: google.maps.MapTypeId.RoadMap
  };
var map = new google.maps.Map(document.getElementById('map-canvas'),mapOptions);

//var myPolygon;
function initialize() {

//map.addListener('mousemove', function (event) {
//            displayCoordinates(event.latLng);
//      });

//function displayCoordinates(pnt) {
//          var coordsLabel = document.getElementById("polygon");
//          var lat = pnt.lat();
//          lat = lat.toFixed(4);
//          var lng = pnt.lng();
//          lng = lng.toFixed(4);
//          coordsLabel.innerHTML = "Latitude: " + lat + "  Longitude: " + lng;
//      }



//////////////////////////////////////////////////////////////////////

  google.maps.event.addListener(map,'click', function(e){
    var zoom = map.getZoom();
    if(zoom > 2){
	var pointClicked = e.latLng;
	map.panTo(pointClicked);
	var bounds = map.getBounds();
	var zoom = map.getZoom();
	var neLat = bounds.getNorthEast().lat();
	var neLng = bounds.getNorthEast().lng();
	var swLat = bounds.getSouthWest().lat();
	var swLng = bounds.getSouthWest().lng();

	var latDiff = (Math.abs(neLat - swLat))/zoom;
	neLat = (neLat - latDiff);
	swLat = (swLat + latDiff);

	if ((swLng > 0) && (neLng < 0)){
	  	diff = (360 - Math.abs(neLng) - Math.abs(swLng))/zoom;
	}else{
		diff = (Math.abs(neLng - swLng)%180)/(zoom);
	}

	neLng = neLng - diff;
	swLng = swLng + diff;

	var rectCoords = [
		new google.maps.LatLng(neLat,neLng),
		new google.maps.LatLng(swLat,neLng),
		new google.maps.LatLng(swLat,swLng),
		new google.maps.LatLng(neLat,swLng)
	]

	plotPoly(rectCoords, true, true, true, true)
	getPolygonCoords(polysOnMap);
    }else{
	// TODO make popup window saying 'please zoom in'
	var point = e.latLng;
	map.panTo(point);
	var zoom = map.getZoom();
	var contentString = "<h3>Zoom level is " + zoom +". Please zoom in at least " + (3-zoom);
	if(zoom == 2){contentString+=" level</h3>";}else{contentString+=" levels</h3>";}
	var infowindow = new google.maps.InfoWindow({content:contentString,position:point});
	infowindow.open(map);
    }
  });//end map cick listener

//////////////////////////////////////////////////////////////////////

plotDefaultPolygon();

}// end initialize();

//Display Coordinates below map
function getPolygonCoords(polys) {
	var htmlStr = "";
	for(var c = 0; c < polys.length; c++){
		if(polys[c] == null){
			continue;
		}
		if(!polys[c].cap){ // check if this polygon should be added to CAP alert
			continue;
		}
		var len = polys[c].getPath().getLength();
		for (var i = 0; i < len; i++) {
			//htmlStr += "new google.maps.LatLng(" + myPolygon.getPath().getAt(i).toUrlValue(5) + "), ";
			//Use this one instead if you want to get rid of the wrap > new google.maps.LatLng(),
			htmlStr += polys[c].getPath().getAt(i).toUrlValue(5) + ";";
		}
		htmlStr =  htmlStr.replace(/;\s*$/, "");
		htmlStr += "::"
	}
		htmlStr =  htmlStr.replace(/::\s*$/, "");

	document.getElementById("polygon").innerHTML = htmlStr;

}

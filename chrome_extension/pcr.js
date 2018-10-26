if(window.location.href == "https://ctidev4.critical.com/login/"){
	chrome.storage.sync.get("resources", function (obj){
		if(obj.resources && obj.resources.length > 0){
			var dl = document.getElementById("resource_list");
			
			for(var idx = 0; idx < obj.resources.length; idx ++){
				var opt = document.createElement("option");
				opt.value = obj.resources[idx];
				dl.appendChild(opt);
			}
		}
	});
}


// Leave a trace on our pages so we can detect that the extension is called.
// And yes, this is actually a recommended solution.
var trace = document.createElement('div');
trace.style = "display: none;";
trace.id = "cti-pcr-extension";
trace.innerHTML = getExtVersion();
document.body.appendChild(trace);

function getExtVersion() { 
	var manifestData = chrome.runtime.getManifest();
	return manifestData.version;
} 

// Adding a listening for receiving a message back from background.js
chrome.runtime.onMessage.addListener( function(request, sender, sendResponse) {

	if(request.stat == "success"){
		try{
			var obj = JSON.parse(request.data);
			if(obj.type == "attest"){
				sessionStorage.setItem("tpm_quote", obj.tpm_quote);
				if(obj.spec == "1.2"){
					sessionStorage.setItem("uuid", obj.uuid);
					sessionStorage.setItem("pcrinfo", JSON.stringify(obj.pcrinfo));
				}
				sessionStorage.setItem("files", JSON.stringify(obj.files));
				
			}else if(obj.type == "scard"){
				sessionStorage.setItem("signature", obj.signature);
				sessionStorage.setItem("certificate", obj.cert);
				sessionStorage.setItem("email", obj.email);
			}
			
			if(sessionStorage.getItem("auth") != "true" && sessionStorage.getItem("scard") != "true"){
				location.href="javascript:sendAttestOnlyRequest(); void 0";
				
			}else if(sessionStorage.getItem("auth") == "true"){
				location.href="javascript:enableAuth2Button(); void 0";  
				
			}else if(sessionStorage.getItem("scard") == "true" && sessionStorage.getItem("attest") == "true" && obj.type == "attest"){
				location.href="javascript:sendScardAttestRequest(); void 0";
				
			}else if(sessionStorage.getItem("scard") == "true" && sessionStorage.getItem("attest") != "true"){
				location.href="javascript:sendScardRequest(); void 0";
				
			} else if(sessionStorage.getItem("scard") == "true" && sessionStorage.getItem("attest") == "true" && obj.type == "scard"){
				location.href="javascript:doAttest(); void 0";
			}
		}catch(err){
			location.href="javascript:openDialogMessage('Error', 'Some error occurred with the native host'); void 0";  
		}
	}else if(request.stat == "fail"){
		location.href="javascript:openDialogMessage('Error', 'Native host failed or is not installed properly OR certificate error '); void 0";  
	}else{
		location.href="javascript:openDialogMessage('Error', 'Some error occurred with the native host'"+request.stat+"); void 0";  
	}

});

// Function for when the "attest button" is clicked. This send a message to background.js
function attestBtn(){	
		try {
				// Enabled spinny blur
	document.getElementById('container').hidden = false;
	document.getElementById('maindiv').style = "-webkit-filter: blur(2px);-moz-filter: blur(2px);-ms-filter: blur(2px);-o-filter: blur(2px);filter: blur(2px);";
	var elem = document.getElementById('attest-popup-button');
        elem.parentNode.removeChild(elem);
			} catch(e) {
			}
	msg = sessionStorage.getItem("json_response");
          chrome.extension.sendRequest({message: msg, greeting: "attest-button"});
}

var port = chrome.runtime.connect();
	// This is listening for a message from the loaded HTML page
	window.addEventListener("message", function(event) {
	// We only accept messages from ourselves
	if (event.source != window)
		return;

  if (event.data.type && event.data.type == "ext_cmd" && event.data.text == "attest") {
		askAttest();
  }else if(event.data.type && event.data.type == "ext_cmd" && event.data.text == "scard"){
		chrome.extension.sendRequest({greeting: "scard"});
  }
}, false);

// Run the confirmation box right off the bat.
// If the user clicks "OK", send a message to background.js to tell it
// to get the PCR values, otherwise inject an attestation button so
// the user can perform it later.
function askAttest(){
	var r = confirm("Press OK to run attestation code");
	if (r == true) {
		var msg = sessionStorage.getItem("json_response");
		chrome.extension.sendRequest({message: msg, greeting: "alert-button"});
		
	} else {
		var buttonOnSite = document.getElementById("auth_step2"),
		parent = buttonOnSite.parentElement,
		next = buttonOnSite.nextSibling,
		button = document.createElement("button"),
		text = document.createTextNode("Attest");
		button.appendChild(text);
		button.id = "attest-popup-button";
		button.className = "btn btn-primary btn-block btn-large";
		try {
			document.getElementById('container').hidden = true;
			document.getElementById('maindiv').style = "";
			} catch(e) {
			}
		
		if (next) parent.insertBefore(button, next);
		else parent.appendChild(button);
		
		document.getElementById('attest-popup-button').addEventListener('click', attestBtn);
	}
}

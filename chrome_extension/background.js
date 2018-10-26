// Taken from chrome example for accessing native host. Can't explain this, it may not even be necessary.
var port = null;
var getKeys = function(obj){
   var keys = [];
   for(var key in obj){
      keys.push(key);
   }
   return keys;
}

// This is the function called on receiving a native message from the native host
function onNativeMessage(message) {
	sendDataToPcrJs(message);
}

// This is just a function needed when creating a native host connection.
function onDisconnected() {
  port = null;
  	chrome.tabs.query({active: true, currentWindow: true}, function(tabs) {
        chrome.tabs.sendMessage(tabs[0].id, {stat: "fail"}, function(response) {
            console.log('failed');
        });
    });
}

// This function takes in the response data from the onNativeMessage function, and sends it to pcr.js
function sendDataToPcrJs(info){

	chrome.tabs.query({active: true, currentWindow: true}, function(tabs) {
        chrome.tabs.sendMessage(tabs[0].id, {data: JSON.stringify(info), stat: info.status}, function(response) {
            console.log('success');
        });
    });
}

// This function creates a connection to the native host, and sends the pcr_indices and nonce to that program
function getpcrs(json_response, hostName){
	port = chrome.runtime.connectNative(hostName);
	port.onMessage.addListener(onNativeMessage);
	port.onDisconnect.addListener(onDisconnected);
	
	var jRespObj = JSON.parse(json_response);
	
	message = {"type":"attest", "nonce": jRespObj.nonce, "pcr_indices": jRespObj.pcr_indices, "sable":jRespObj.sable};
    port.postMessage(message);
}

function getScard(hostName){
	port = chrome.runtime.connectNative(hostName);
	port.onMessage.addListener(onNativeMessage);
	port.onDisconnect.addListener(onDisconnected);
	message = {"type":"scard"};
    port.postMessage(message);
}

function nowHostname(request, hostname){
	if (request.greeting == "alert-button" || request.greeting == "attest-button"){
		getpcrs(request.message, hostname);
	}else if (request.greeting == "scard"){
		getScard(hostname);
	}
}

// This listens for messages from pcr.js
chrome.extension.onRequest.addListener(function(request, sender)
{
	chrome.storage.sync.get("spec", function (obj){
		var hostName = "com.google.chrome.example.echo";
		if(obj.spec){if(obj.spec === "20"){var hostName = "com.critical.nh2";}}
		nowHostname(request, hostName);
	});

});


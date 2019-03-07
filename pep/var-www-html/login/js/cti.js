var pcr_idx = "";
var domain_self = "https://ctidev4.critical.com"

sessionStorage.clear();

// This is called by pcr.js after attestation is done,
// and is used if auth is needed as well
function enableAuth2Button(){
        if(sessionStorage.getItem("auth") == "true"){
                document.getElementById("auth_step2").disabled = false;
                disableBlur();
        }
}
$("#attest-popup-button").click(function () {
        enableBlur();
});

// Request Access1
$("#first_request_access").click(function () {
        // Make sure the user provided a resource. Otherwise, alert them and stop execution
        if(document.getElementById("resource").value == ""){
                alert("You must provide a resource");
                return;
        }
        // for now, action is static
        //sessionStorage.setItem("action", "access");
        var acts = document.getElementById("actions");
        sessionStorage.setItem("action", acts.options[acts.selectedIndex].value);

        // disable button and resource and actions
        document.getElementById("actions").disabled = true;
        document.getElementById('first_request_access').disabled = true;
        document.getElementById('resource').disabled = true;
        enableBlur();
        // Build JSON for very first request
        var myObject = {
                resource : document.getElementById('resource').value,
                action : sessionStorage.getItem("action"),
                step : "multi-one"
        };
        var requestData = JSON.stringify(myObject);
        // Send data to PEP to create request, evaluate, send response
        jQuery.ajax({
                url : domain_self + ":8080/LoginWebInterfacePEP/dam3on/multi",
                type : "POST",
                data : requestData,
                dataType : "text",
                contentType : "text/plain; charset=utf-8",
                                error : function(jqXHR, exception){
                                        if(jqXHR.status != 200){
                                                openDialogMessage("Server Error", "A server error ocurred. Status: " + jqXHR.status);
                                        }},
                success : function (data, status) {
                        // On response...
                        var response = JSON.parse(data);
                        // We shouldn't change resource throught transaction
                        document.getElementById('resource').disabled = true;
                        disableBlur();
                        // If there's no advice..
                        // No advice == {"adviceBool":"false", "destination":"www.google.com"}
                        if (response.decision == "Permit" || response.decision == "Deny" ){
                                if (response.decision == "Permit" && response.adviceBool == "false"){
                                        // Forward user to destination
                                        window.location.href = response.url;
                                } else if (response.decision == "Deny" && response.adviceBool == "true") {
                                        // Remove this button
                                        var element = document.getElementById("first_request_access");
                                        element.outerHTML = "";
                                        delete element;
                                        // We have to make a second request, and the action shouldn't be the same // This is dumb, action is unconditionally set on first button click
                                        // Otherwise, loop over advice..
                                        for(var i = 0; i < response.advice.length; i++) {
                                                //Grab advice
                                                var adv = response.advice[i];
                                                if(adv == "attest"){
                                                        checkExtension();
                                                        sessionStorage.setItem("attest", "true");
                                                } else if (adv == "auth") {
                                                        // If auth is needed, show second button
                                                        document.getElementById('auth_step1').style = "";
                                                        // Set "auth" == true for future use
                                                        sessionStorage.setItem("auth", "true");
                                                        // Make email box visible
                                                        document.getElementById("email").type = "text";
                                                        document.getElementById("email").value = ""; //  on repeated uses, a previous anon email may still be cached
                                                        document.getElementById("email").focus();
						} else if (adv == "scard") {
							sessionStorage.setItem("scard", "true");
						} else if (adv == "cctxid") {
							// make new field visible
                                                        sessionStorage.setItem("cctxid", "true");
                                                        document.getElementById('cctxid').type="text";
                                                } else {
                                                        openDialogMessage("Error", "Policy gave invalid advice");
                                                }
                                        }

                                        if(sessionStorage.getItem("attest") == "true" && checkExtension()){
                                                if(sessionStorage.getItem("auth") != "true" && sessionStorage.getItem("scard") != "true"){
                                                        doAnonymousAttest(response.pcr_indices, response.sable_hash);
                                                }else if(sessionStorage.getItem("scard") == "true"){
							doSmartCardAuthentication();
						}
						if(sessionStorage.getItem("attest") == "true"){
                                                        window.pcr_idx = response.pcr_indices;
							window.sable = response.sable_hash;
                                                }
                                        } else if(sessionStorage.getItem("attest") == "true" && !checkExtension()){
                                                openDialogMessage("Error", "Attestation is required, but the CTI PCR Extension is not installed");
                                        } else if(sessionStorage.getItem("scard") == "true" && !checkExtension()){
												openDialogMessage("Error", "Smart Card authentication is required, but the CTI PCR Extension is not installed");
										} else if(sessionStorage.getItem("scard") == "true" && checkExtension()){
												doSmartCardAuthentication();
										}

                                }else{
                                        openDialogMessage("Policy Error", "Denied without any advice");
                                                                }
                        } else {
                                accessDenied();
//                              openDialogMessage("Decision Error", "Indeterminate or Not Applicable. The resource you requested likely does not exist.");
                        }
        }});
});


function sendScardAttestRequest(){
                enableBlur();
                var attestScardAuthRequest = {
         	       signature : sessionStorage.getItem("signature"), // scard stuff
			certificate : sessionStorage.getItem("certificate"), // scard stuff
        	        tpm_quote : sessionStorage.getItem("tpm_quote"), //attest stuff
                	pcrinfo : JSON.parse(sessionStorage.getItem("pcrinfo")),
	                uuid : sessionStorage.getItem("uuid"),
        	        files : JSON.parse(sessionStorage.getItem("files")),
			resource : document.getElementById('resource').value,
	                action : sessionStorage.getItem("action"),
        	        step : "multi-both-sm"
                };
                var requestData = JSON.stringify(attestScardAuthRequest);
                jQuery.ajax({
                        url : domain_self + ":8080/LoginWebInterfacePEP/dam3on/multi",
                        type : "POST",
                        data : requestData,
                        dataType : "text",
                        contentType : "text/plain; charset=utf-8",
                        error : function(jqXHR, exception){
                        if(jqXHR.status != 200){
                                openDialogMessage("Server Error", "A server error ocurred. Status: " + jqXHR.status);
                        }},
                        success : function (data, status) {
                                disableBlur();
                                var json = $.parseJSON(data);
                                if(json.decision == "Permit"){
                                        pnonceForward(json.nonce);
                                } else {
                                        accessDenied();
                                }
                        }
                });
}

function sendScardRequest(){
	if (sessionStorage.getItem("scard") == "true" && sessionStorage.getItem("attest") != "true"){
                enableBlur();
                var authRequest = {
                        signature : sessionStorage.getItem("signature"), // sm auth stuff
						certificate : sessionStorage.getItem("certificate"), // sm auth stuff
						resource : document.getElementById('resource').value,
						action : sessionStorage.getItem("action"),
                        step : "multi-auth-sm"
                        };
                        var requestData = JSON.stringify(authRequest);
                                        jQuery.ajax({
                        url : domain_self + ":8080/LoginWebInterfacePEP/dam3on/multi",
                        type : "POST",
                        data : requestData,
                        dataType : "text",
                        contentType : "text/plain; charset=utf-8",
						error : function(jqXHR, exception){
									if(jqXHR.status != 200){
										openDialogMessage("Server Error", "A server error ocurred. Status: " + jqXHR.status);
									}
								},
                        success : function (data, status) {
									disableBlur();
									var json = $.parseJSON(data);
									if(json.decision == "Permit"){
                                        pnonceForward(json.nonce);
									} else {
                                        accessDenied();
									}
                        }
                });
        } else if(sessionStorage.getItem("scard") == "true" && sessionStorage.getItem("signature") == null) {
                alert("You must run the authentication code before requesting access.");
        } else {
                openDialogMessage("Error", "An error occurred determining what information is required. This should never happen");
        }
	
}

function doSmartCardAuthentication(){
	enableBlur();
	window.postMessage({type : "ext_cmd", text : "scard"}, "*");
}



// First step of doing auth (fill sigbox so user can sign)
$("#auth_step1").click(function () {

        // If we're doing auth ...
        if(sessionStorage.getItem("auth") == "true"){
                        sessionStorage.getItem("action")
                var sub = document.getElementById("email").value;
                var act = sessionStorage.getItem("action");
                var res = document.getElementById("resource").value;
                var cctxid_val = null;
                if(sessionStorage.getItem("cctxid") == "true"){
                        console.log("setting cctxid_val to " + document.getElementById('cctxid').value);
                        cctxid_val = document.getElementById('cctxid').value;
                }
                // if null, alert and quit
                if (res == "" || sub == "" || (sessionStorage.getItem("cctxid") == "true" && cctxid_val == null) ) {
                        alert("Please fill in the form")
                // otherwise..
                } else {
                        document.getElementById("auth_step1").disabled = true;
                        // disable email box
                        document.getElementById("email").disabled = true;
                        document.getElementById("cctxid").disabled = true;
                        // make & populate sig box
                        makeSigBox(sub, act, res, cctxid_val);

                        // ajax can't return nonce, so we add is separetly along with doing attestation code
                        if(sessionStorage.getItem("attest") == "true"){
                                doAttest();
                        } 
                        addNonceToSigBox(sub);
                        // hide this button
                        var element = document.getElementById("auth_step1");
                        element.outerHTML = "";
                        delete element;
                        // show third button
                        document.getElementById("auth_step2").style = "";
                }
        } else {
                openDialogMessage("Error", "An error occurred with authentication"); // accessDenied();
        }
});
// Second step of doing auth (user has signed now)
$("#auth_step2").click(function () {
        // If we're doing Auth AND attest, build the proper request, and send away. If permit, forward to Pnonce, otherwise access_denied
        if(sessionStorage.getItem("auth") == "true" && sessionStorage.getItem("attest") == "true" && sessionStorage.getItem("tpm_quote") != null){
                enableBlur();
                var attestAuthRequest = {
                pgp_signature : document.getElementById('sigbox').value, // auth stuff
                tpm_quote : sessionStorage.getItem("tpm_quote"), //attest stuff
                                pcrinfo : JSON.parse(sessionStorage.getItem("pcrinfo")),
                uuid : sessionStorage.getItem("uuid"),
                                files : JSON.parse(sessionStorage.getItem("files")),
                step : "multi-both"
                };
                var requestData = JSON.stringify(attestAuthRequest);
                jQuery.ajax({
                        url : domain_self + ":8080/LoginWebInterfacePEP/dam3on/multi",
                        type : "POST",
                        data : requestData,
                        dataType : "text",
                        contentType : "text/plain; charset=utf-8",
                        error : function(jqXHR, exception){
                        if(jqXHR.status != 200){
                                openDialogMessage("Server Error", "A server error ocurred. Status: " + jqXHR.status);
                        }},
                        success : function (data, status) {
                                disableBlur();
                                var json = $.parseJSON(data);
                                if(json.decision == "Permit"){
                                        pnonceForward(json.nonce);
                                } else {
                                        accessDenied();
                                }
                        }
                });
        // If we're doing auth ONLY, build the proper request, and send away. If permit, forward to Pnonce, otherwise access_denied
        } else if (sessionStorage.getItem("auth") == "true" && sessionStorage.getItem("attest") != "true"){
                enableBlur();
                var authRequest = {
                        pgp_signature : document.getElementById('sigbox').value, // auth stuff
                        step : "multi-auth"
                        };
                        var requestData = JSON.stringify(authRequest);
                                        jQuery.ajax({
                        url : domain_self + ":8080/LoginWebInterfacePEP/dam3on/multi",
                        type : "POST",
                        data : requestData,
                        dataType : "text",
                        contentType : "text/plain; charset=utf-8",
                                                error : function(jqXHR, exception){
                                                        if(jqXHR.status != 200){
                                openDialogMessage("Server Error", "A server error ocurred. Status: " + jqXHR.status);
                                                        }},
                        success : function (data, status) {
                                disableBlur();
                                var json = $.parseJSON(data);
                                if(json.decision == "Permit"){
                                        pnonceForward(json.nonce);
                                } else {
                                        accessDenied();
                                }
                        }
                });
        } else if(sessionStorage.getItem("attest") == "true" && sessionStorage.getItem("tpm_quote") == null) {
                alert("You must run the attestation code before requesting access.");
        } else {
                openDialogMessage("Error", "An error occurred determining what information is required. This should never happen");
        }
});
// HELPER FUNCTIONS BELOW // obvious function. if we ever need to change it, we only need to change this one place now.
function accessDenied(){
        window.location.replace(domain_self + "/access_denied.html");
}

function doAnonymousAttest(pcri, sableyn){
                var anonEmail = makeAnonEmail() + "@critical.com";
                document.getElementById("email").value = anonEmail;
                jQuery.ajax({
                        url : domain_self + "/api/nonce.php", // No COR problem since same domain
                        type : "GET",
                        data : {email : anonEmail},
                        dataType : "text",
                        contentType : "text/plain; charset=utf-8",
                        error : function(jqXHR, exception){
                                if(jqXHR.status != 200){
                                openDialogMessage("Server Error", "A server error ocurred. Status: " + jqXHR.status);
                                }},
                        success : function (data, status) {
                                var resp_json = JSON.parse(data);
                                var jResponse = {
                                        nonce : resp_json.nonce,
                                        pcr_indices : pcri,
					sable: sableyn
                                };
                                var jjresponse = JSON.stringify(jResponse);
                                // Put JSON needed by extension/native host into HTML
                                sessionStorage.setItem("json_response", jjresponse);
                                // Tell extension to do it's thing
                                enableBlur();
                                window.postMessage({type : "ext_cmd",text : "attest"}, "*");
                        }
                });
}
function doAttest(){
		var em_tmp = "";
		if(sessionStorage.getItem("scard") == "true"){
			em_tmp = sessionStorage.getItem("email");
		}else{
			em_tmp = document.getElementById("email").value
		}
                jQuery.ajax({
                        url : domain_self + "/api/nonce.php", // No COR problem since same domain
                        type : "GET",
                        data : {email : em_tmp},
                        dataType : "text",
                        contentType : "text/plain; charset=utf-8",
                        error : function(jqXHR, exception){
                                if(jqXHR.status != 200){
                                        openDialogMessage("Server Error", "A server error ocurred. Status: " + jqXHR.status);
                                }},
                        success : function (data, status) {
                                var resp_json = JSON.parse(data);
                                var jResponse = {
                                        nonce : resp_json.nonce,
                                        pcr_indices : window.pcr_idx,
					sable: window.sable
                                };
                                var jjresponse = JSON.stringify(jResponse);

                                // Put JSON needed by extension/native host into HTML
                                sessionStorage.setItem("json_response", jjresponse);
                                // Tell extension to do it's thing
                                enableBlur();
                                document.getElementById("auth_step2").disabled = true;
                                window.postMessage({type : "ext_cmd",text : "attest"}, "*");
                        }
                });
}
// obvious function. call nonce API and return nonce
function addNonceToSigBox(subj){
        jQuery.ajax({
                        url : domain_self + "/api/nonce.php", // No COR problem since same domain
                        type : "GET",
                        data : {email : subj},
                        dataType : "text",
                        contentType : "text/plain; charset=utf-8",
                        error : function(jqXHR, exception){
                        if(jqXHR.status != 200){
                                openDialogMessage("Server Error", "A server error ocurred. Status: " + jqXHR.status);
                        }},
                        success : function (data, status) {
                                var json = $.parseJSON(data);
                                var sigbox = document.getElementById("sigbox");
                                sigbox.innerHTML = sigbox.innerHTML + '\nNonce:'+json.nonce;
        }});
}
// this actually doesn't really need to be it's own function since it's only called once, but // I'll deal with it later.
function sendAttestOnlyRequest(){
        enableBlur();
   var attestRequest = {
            subject : document.getElementById('email').value,
                action : sessionStorage.getItem("action"),
                resource : document.getElementById('resource').value,
                tpm_quote : sessionStorage.getItem("tpm_quote"),
                pcrinfo : JSON.parse(sessionStorage.getItem("pcrinfo")),
                uuid : sessionStorage.getItem("uuid"),
                files : JSON.parse(sessionStorage.getItem("files")),
                step : "multi-attest-only"
                };
        var requestData = JSON.stringify(attestRequest);
        jQuery.ajax({
                url : domain_self + ":8080/LoginWebInterfacePEP/dam3on/multi",
                type : "POST",
                data : requestData,
                dataType : "text",
                contentType : "text/plain; charset=utf-8",
                error : function(jqXHR, exception){
                        if(jqXHR.status != 200){
                                openDialogMessage("Server Error", "A server error ocurred. Status: " + jqXHR.status);
                        }},
                success : function (data, status) {
                        var response = $.parseJSON(data);
                        if(response.decision == "Permit"){
                                pnonceForward(response.nonce);
                        } else {
                                accessDenied();
                        }
                }
        });
}
// Creates and populates the signature box
function makeSigBox(sub, act, res, cctxid_val){
        var resObj = document.getElementById("email"),
        parent = resObj.parentElement,
        next = resObj.nextSibling,
        sigtext = document.createElement("textarea"),
        text = document.createTextNode('Subject:' + sub
                                + '\nAction:' + act
                                + '\nResource:' + res);
        if (cctxid_val != null){
                text.nodeValue = text.nodeValue + '\nCCTXID:' + cctxid_val;
        }else{
                console.log("cctxid_val is null");
                console.log(cctxid_val);
        }
        sigtext.appendChild(text);
        sigtext.id = "sigbox";
        sigtext.style = "width:475px;height:110px;";
        if (next)
                parent.insertBefore(sigtext, next);
        else
                parent.appendChild(sigtext);
}
function enableBlur(){
        // Enabled spinny blur
        document.getElementById('container').hidden = false;
        document.getElementById('maindiv').style = "-webkit-filter: blur(2px);-moz-filter: blur(2px);-ms-filter: blur(2px);-o-filter: blur(2px);filter: blur(2px);"
}
function disableBlur(){
        // hide blur and spinny icon thing
        document.getElementById('container').hidden = true;
        document.getElementById('maindiv').style = "";
}
function makeAnonEmail() {
    var text = "";
    var possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    for( var i=0; i < 12; i++ )
        text += possible.charAt(Math.floor(Math.random() * possible.length));
    return text;
}
// Given a pnonce, we POST it to the pnonce.php which takes care of everything for us.
function pnonceForward(pnonce){
        var form = $('<form action="/api/pnonce.php" method="post">' +
        '<input type="hidden" name="nonce" value="' + pnonce + '" />' +
        '</form>');
        $('body').append(form);
        form.submit();
}

window.addEventListener('keypress', function (e) {
    if (e.keyCode == 13) {
        var b1 = document.getElementById("first_request_access");
        var b2 = document.getElementById("auth_step1");
        var b3 = document.getElementById("auth_step2");

        try{
                var b1s = b1.style.visibility;
        }catch(err){
                var b1s = "hidden";
        }

        try{
                var b2s = b2.style.visibility;
        }catch(err){var b2s = "hidden";}

        try{
                var b3s = b3.style.visibility;
        }catch(err){var b3s = "hidden";}

        if(b1s !== "hidden"){
                b1.click();
        }else if(b2s !== "hidden"){
                b2.click();
        }else if(b3s !== "hidden"){
                b3.click();
        }else{
                console.log("something went wrong. all buttons are hidden");
        }

    }
}, false);

// Check if PCR extension is installed
function checkExtension(){
        try{
                var extVersion = document.getElementById("cti-pcr-extension").innerHTML;
        } catch(err){
                return false;
        }

        if(extVersion != null){
                return true;
        }else{
                return false;
        }
}

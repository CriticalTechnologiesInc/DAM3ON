$(function() {
  // this initializes the dialog (and uses some common options that I do)
  $("#dialog").dialog({
    autoOpen : false, modal : true, show : "blind", hide : "blind", close : closeDialogMessage, width:500, height: 150
  });
});

function openDialogMessage(title, message){
	var dm = document.getElementById("dialog");
	dm.innerHTML = message + '<br/><br/> Need help? Try our <a href="/login/index.html?resource=documentation&action=access&go=true">documentation</a>';
	$("#dialog").dialog('option', 'title', title);
	$("#dialog").dialog("open");
}

function closeDialogMessage(){
	window.location.replace("/");
}


$(document).ready(function(){
refreshListing();
 })
 
 function refreshListing(){
	var sel = document.getElementById('names');
	sel.innerHTML="";
	document.getElementById('text_area').value = "";
	 jQuery.ajax({
                        url : '/pap/back.php',
                        type : 'POST',
                        data : {'step':'pnames'},
                        success : function (data, status) {
                                                        var sel = document.getElementById('names');
                                                        var jnames = JSON.parse(data);
                                                        for(var i = 0; i<jnames.length; i++){
                                                                   var opt = document.createElement("option");
                                                                   opt.value= jnames[i].name;
                                                                   opt.innerHTML = jnames[i].name; // whatever property it has

                                                                   // then append it to the select element
                                                                   sel.appendChild(opt);
                                                        }

                        }
        });
 }
 
$("#button").click(function () {
                var pdata = document.getElementById('names').value;

                jQuery.ajax({
                        url : '/pap/back.php',
                        type : 'POST',
                        data : {'data':pdata, 'step':'load'},
                        success : function (data, status) {
                                        $('#text_area').val(data);
                                        document.getElementById("names").disabled = true;
                        }
        });
});

$("#save").click(function () {
                var pdata = document.getElementById('text_area').value;
                var pname = document.getElementById('names').value;

                var r = confirm("Are you sure you want to save '" + pname + "'?");
                if(r == true){

                jQuery.ajax({
                        url : '/pap/back.php',
                        type : 'POST',
                        data : {'data':pdata, 'step':'save', 'name':pname},
                        success : function (data, status) {
                                        if(status == "success"){
                                                alert("Your policy has been saved!");
                                        }
                                        document.getElementById("names").disabled = false;
                        }
        });
}
});

$("#delete").click(function () {
	var pname = document.getElementById('names').value;

	var r = confirm("Are you sure you want to delete '" + pname + "'?");
	if(r == true){
		var rr = confirm("Are you ABSOLUTELY sure you want to delete '" + pname + "'?");
		if(rr == true){
			jQuery.ajax({
					url : '/pap/back.php',
					type : 'POST',
					data : {'pname':pname, 'step':'deletepol'},
					success : function (data, status) {
									alert(data);
									refreshListing();
									document.getElementById("names").disabled = false;
					}
			});
		}
	}
});

$("#cancel").click(function () {
document.getElementById('names').disabled = false;
document.getElementById('text_area').value = "";
});


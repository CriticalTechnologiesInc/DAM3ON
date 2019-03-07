$(document).ready(function(){
	jQuery.ajax({
				url : '/pap/back.php',
				type : 'POST',
				data : {'step':'selected'},
				success : function (data, status) {
								var sel = document.getElementById('names');
								var jnames = JSON.parse(data);
								for(var i = 0; i<jnames.length; i++){
									   var opt = document.createElement("li");
									   opt.value= jnames[i].name;
									   opt.innerHTML = jnames[i].name; // whatever property it has

									   // then append it to the select element
									   sel.appendChild(opt);
								}
								
				}
	});

 });
 
 $("#refresh").click(function () {
	refreshListing();
});

function refreshListing(){
	var sel = document.getElementById('names');
	sel.innerHTML="";
	jQuery.ajax({
		url : '/pap/back.php',
		type : 'POST',
		data : {'step':'selected'},
		success : function (data, status) {
						var sel = document.getElementById('names');
						var jnames = JSON.parse(data);
						for(var i = 0; i<jnames.length; i++){
							   var opt = document.createElement("li");
							   opt.value= jnames[i].name;
							   opt.innerHTML = jnames[i].name; // whatever property it has

							   // then append it to the select element
							   sel.appendChild(opt);
						}
						
		}
});
}

 $("#add").click(function () {
		var pname = document.getElementById('input_add').value;
		jQuery.ajax({
		url : '/pap/back.php',
		type : 'POST',
		data : {'step':'seladd', 'pname':pname},
		success : function (data, status) {
			alert(data);
			refreshListing();
		}
	});
});

 $("#remove").click(function () {
		var pname = document.getElementById('input_remove').value;
		var r = confirm("Are you sure you want to remove '" + pname + "'?");
		if(r == true){
			jQuery.ajax({
			url : '/pap/back.php',
			type : 'POST',
			data : {'step':'selremove', 'pname':pname},
			success : function (data, status) {
				alert(data);
				refreshListing();
			}
		});
		}
});

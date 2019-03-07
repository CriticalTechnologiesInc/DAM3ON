// This is basically "on load"
document.addEventListener('DOMContentLoaded', function() {

	chrome.storage.sync.get("spec", function (obj){
		if(obj.spec){
			if(obj.spec === "20"){
				document.getElementById("spec20").checked = true;
				document.getElementById("spec12").checked = false;
			}else if(obj.spec === "12"){
				document.getElementById("spec12").checked = true;
				document.getElementById("spec20").checked = false;
			}
		}
	});

	chrome.storage.sync.get("resources", function (obj){
		if(obj.resources){
			for(var idx = 0; idx < obj.resources.length; idx ++){
				addItemToList(obj.resources[idx]);
			}
		}
		
		var links = document.getElementsByTagName("a");
		for (var i = 0; i < links.length; i++) {
			(function () {
				var ln = links[i];
				var loc = ln.href;
				ln.onclick = function () {
					chrome.tabs.create({active: true, url: loc});
				};
			})();
		}
		
		var editableList = Sortable.create(items, {
			onUpdate: onChangeUpdate,
			filter:'.ignore',
			handle:'.drag-handle',
			fallbackTolerance: 4,
			onFilter:function(evt){
				console.log(evt.item);
				if(evt.item.nodeName == "LI"){
					var el = editableList.closest(evt.item); // get dragged item
					el && el.parentNode.removeChild(el);
					onChangeUpdate();
				}
			}
		});
	});
});

// Any time a change is made to the list
function onChangeUpdate(){
	var ul = document.getElementById("items");
	var items = ul.getElementsByTagName("li");
	var tmp = [];
	for (var i = 0; i < items.length; ++i) {
		tmp[i] = items[i].getElementsByTagName('a')[0].innerHTML;
	}
	
	chrome.storage.sync.set({'resources':tmp}, function(){});
}

// Listen for "resource" button press
document.getElementById("addresource").addEventListener("click", function(){
	var inpt = document.getElementById("resource");
	if(validateResourceInput(inpt.value)){
		document.getElementById("warn").innerHTML = "";
		chrome.storage.sync.get("resources", chromeSync);
	}else{
		inpt.value = "";
		document.getElementById("warn").innerHTML = "Invalid input!";		
	}
	
});

var radios = document.forms["spec"].elements["spec"];
for(var i = 0, max = radios.length; i < max; i++) {
	radios[i].onclick = function() {
		chrome.storage.sync.set({'spec': this.value});
	}
}

// visually add an LI to the UL
function addItemToList(item){
	var ul = document.getElementById("items");
	var li = document.createElement("li");
	
	var x = document.createElement("i");
	x.className = "js-remove ignore";
	x.innerHTML = "X";
	
	var handle = document.createElement("span");
	handle.className = "drag-handle";
	handle.innerHTML = 	'\u2630';

	var lnk = document.createElement("a");
	lnk.innerHTML = item;
	lnk.className = "fallback";
	lnk.href = "https://ctidev4.critical.com/login/index.html?resource=" + item;
	
	li.append(handle);
	li.appendChild(lnk);
	li.appendChild(x);
    ul.appendChild(li);
}

// callback for when we do chrome.storage.sync.get
function chromeSync(obj){
	var res = document.getElementById("resource");
	if(res.value){
		if(!obj.resources){
			obj.resources = [];
		}
		obj.resources.push(res.value);
		chrome.storage.sync.set({'resources': obj.resources}, addItemToList(res.value));
	}else{
		document.getElementById("warn").innerHTML = "Must input a value!";
	}
	res.value = "";
}
	  
function validateResourceInput(str){
	return str.length < 128 && str.length > 0;
}
	  
chrome.storage.onChanged.addListener(function(changes, namespace) {
        for (key in changes) {
          var storageChange = changes[key];
          console.log('Storage key "%s" in namespace "%s" changed. ' +
                      'Old value was "%s", new value is "%s".',
                      key,
                      namespace,
                      storageChange.oldValue,
                      storageChange.newValue);
        }
      });
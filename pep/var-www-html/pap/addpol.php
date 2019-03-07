<?php session_start(); ?> <?php if($_SESSION["https://".$_SERVER["HTTP_HOST"]."/pap/"] == "permit") :?>
<!DOCTYPE html>
<html>

  <head>
    <meta charset="UTF-8">
    <title>Policy Administration Point</title>
    <link rel="stylesheet" href="/login/css/normalize.css">
        <link rel="stylesheet" href="/login/css/style.css">
        <script src="/login/js/jquery.min.js"></script>
        <script src="/login/js/prefixfree.min.js"></script>
<link rel="icon" href="/icon.ico" type="image/x-icon" />
<link rel="shortcut icon" href="/icon.ico" type="image/x-icon" />
  </head>


<body>

<style type="text/css" media="screen">
a:link { color:#bacbff; text-decoration: underline; }
a:visited { color:#8485e0; text-decoration: underline; }
a:hover { color:#33348e; text-decoration: underline; }
a:active { color:#7476b4; text-decoration: underline; }
</style>


<h2 style="color:white; text-align:center;">Policy Administration Point</h2></br>
<a href="index.php">Back to main</a></br>
<a href="/logout.php">Logout</a></br>

<div style="height:80%;width:80%;margin:0 auto;">
	Policy: <input id='input_add' type="text" name="add"><button id='addpol'>Add</button><br>
	<textarea style="width:100%;height:80%;" id='text_area'></textarea>
	<script>
	 $("#addpol").click(function () {
		var pname = document.getElementById('input_add').value;
		var pol = document.getElementById('text_area').value;
		jQuery.ajax({
		url : '/pap/back.php',
		type : 'POST',
		data : {'step':'addpol', 'pname':pname, 'policy':pol},
		success : function (data, status) {
			alert(data);
			}
		});
	});
	</script>
</div>

</body>
</html>
<?php else:
   header('Location: ' . 'https://'.$_SERVER["HTTP_HOST"].'/access_denied.html', true, 303);
   die();
 ?> <?php endif; ?>



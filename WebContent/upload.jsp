<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<link href="style.css" rel="stylesheet" type="text/css">
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.4.2/jquery.js"></script>
<script type="text/javascript">
function validate() {
	message = "";
	email = document.forms.form1.email.value;
	file = document.forms.form1.file.value;
	type = document.forms.form1.format.value;
	
	if(!email.match(/.*?@.*?\..*?/)) {
		message += "<br>Enter a valid email address";
	}
	if(!file.match(/(\/|\\).{1,}\.csv/)) {
		message += "<br>Choose a valid file (extension .csv)";
	}
	if(type == "(choose one)") {
		message += "<br>Choose a format";
	}
	
	return message;
}

$(document).ready(function(){
	$("#upload").click(function() {
		var message = validate();
		
		if(message == "") {
			return true;
		}
		else {
			$(".error").html(message);
			return false;
		}
	});
	
});
</script>
<title></title>
</head>
<body>

<div style="width:300px;margin-left:auto;margin-right:auto;margin-top:auto;margin-bottom:auto;">
	<br>
	<h2>upload csv</h2>
	<div class="error" style="color:red;text-align:center;font-size:75%;">
	
	</div>
	<form name="form1" ENCTYPE='multipart/form-data' id="uploadForm" action="uploadServlet" method="post">
		<ol>
			<li>
				<label>email</label> 
				<input type="text" placeholder="example@email.com" id="email" name="email"><br>
			</li>
			<li>
				<label>&nbsp;</label>
				<input type="file" id="file" name="file"><br>
			</li>
			<li>
				<label>format</label>
				<select name="format">
					<option>(choose one)</option>
					<option>example</option>
				</select>
			</li>
			<li>
				<label>&nbsp;</label>
				<input type="submit" id="upload" name="submit"><br>
			</li>
		</ol>
	</form>
</div>
</body>
</html>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%
	String email = (String)request.getParameter("email");
	String error = (String)request.getAttribute("error");
	String context = (String)request.getContextPath();
	email = email == null ? "":email;
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<link href="/GeoApi/style.css" rel="stylesheet" type="text/css">
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.4.2/jquery.js"></script>
<script type="text/javascript" src="/GeoApi/js/fileuploader.js"></script>
<link href="/GeoApi/fileuploader.css" rel="stylesheet" type="text/css">
<script type="text/javascript">
$(document).ready(function() {
	var canSubmit = false;

	if ($.trim($("#error").html())=="") {
		$("#error").hide();
	}
	
	$('#fileuploaded').hide();
	
	var uploader = new qq.FileUploader({
		action: '<%=context%>/bulk/upload',
		element: document.getElementById('fileuploader'),
		allowedExtensions:['tsv','csv', 'txt'],
		multiple:false,
		template: '<div class="qq-uploader">' + 
	        '<div class="qq-upload-drop-area"><span>Drop file here to upload</span></div>' +
	        '<div class="custom-button qq-upload-button"><span>Select a file</span></div>' +
	        '<ul class="qq-upload-list"></ul>' + 
     		'</div>',
     	onSubmit: doSubmit,
   		onComplete: doComplete
	});

	function doSubmit(id, fileName) {
		return true;
	}

	function doComplete(id, fileName, responseJson) {		
		$('.qq-upload-drop-area').hide();
		$('.qq-upload-button').hide();
		$('#fileuploader').slideUp(function() {
			$('#fileuploaded').slideDown();
		});

		var html="";
		if(responseJson.success == true && responseJson.type != "null") {
			html = "<div id=\"uploaded-response\"><span class=\"hl-text\">";
			html    += 		responseJson.file 
								+ "</span> has been successfully uploaded.  It is a <span class=\"hl-text\">" 
								+ responseJson.type 
								+ "</span> file.<br/><br/>";
			html    += 		"The file contains <span class=\"hl-text\">" 
								+ responseJson.count 
								+ "</span> records and will take approximately <span class=\"hl-text\">" 
								+ Math.round((responseJson.count/20000)*100)/100 
								+ "</span> hours to complete.<br/><br/>";
			html 	+=		"Click <span class=\"hl-text\">Submit</span> to finalize this request.<br/><br/>";
			html 	+=		"<div id=\"select-new-file\">Select a different file</div>";
			html    += 	"</div>";

			$('#fileName').val(responseJson.systemFile);
			$('#header').val(responseJson.header);
			$('#type').val(responseJson.type);

			canSubmit = true;
		}
		else {
			//take care of bad headers or blank files here
			html = "<div id=\"uploaded-response\">";

			if(responseJson.type == "null") {
				html += "The file uploaded (<span class=\"hl-text\">" 
					+ fileName 
					+ "</span>) isn't handled by SAGE yet, please send an email for support.<br/><br/>";
			}
			else if(responseJson.count == 0) {
				html += "The file uploaded (<span class=\"hl-text\">" 
					+ fileName 
					+ "</span>) did not have any readable text.<br/><br/>";
			}
			else {
				html +=		"There was an issue uploading the file (<span class=\"hl-text\">" 
					+ fileName 
					+ "</span>), please try again.<br/><br/>";
			}
			
			html 	+=		"<div id=\"select-new-file\">Select a different file</div>";
			html    += 	"</div>";

			canSubmit = false;
		}

		$('#fileuploaded').html(html);

		/*$('#select-new-file').slideUp(0, function() {
			$('#select-new-file').css('visibility','hidden');
			$('#select-new-file').hide();
		});*/

		$('#select-new-file').click(function() {
			$('.qq-upload-button').show();
			$('#fileuploaded').slideUp(function() {
				$('#fileuploader').slideDown();
			});
		});
	}

	$('.custom-submit-button').click(function() {
		var message = validate();
		if(message == "" && canSubmit) {
			$('#form_submit').html("Saving...");

			canSubmit = false;
			
			$('#uploadForm').submit();
			return true;
		}
		else {
			if(!canSubmit) {
				message += "<br>Select a valid file";
			}
			$("#error").html(message);
			if(!$("#error").is(":visible")) {
				$("#error").slideToggle(500);
			}			
			return false;
		}
	});
	
	function validate() {
		message = "";
		email = $('#email').val();//document.forms.form1.email.value;
		
		if(!email.match(/.*?@nysenate\.gov/)) {
			message += "<br>Enter a valid email address (@nysenate.gov)";
		}

		return message;
	}
});
</script>
<title></title>
</head>
<body>
<div id="upload-body">
	<br>
	<h2>upload 3rd party file</h2>
	<div id="error">
		<% if(error != null) { %>
			<br/><%=error %>
		<% } %>
	</div>
	<form id="uploadForm" method="post" action="submit">
		<ol>
			<li>
				<label>email</label> 
				<input value="<%= email %>" type="text" placeholder="example@email.com" id="email" name="email"><br>
			</li>
			<li>
				<div id="fileuploader">
					<noscript><p>Please enable Javascript to use the file uploader</p></noscript>
				</div>
				<div id="fileuploaded">
					
				</div>
			</li>
			<li class="submit-li">
				<label>&nbsp;</label>
				<div class="custom-submit-button custom-button"><span id="form_submit">Submit</span></div>
			</li>
		</ol>
		<input type="hidden" id="fileName" name="fileName">
		<input type="hidden" id="header" name="header">
		<input type="hidden" id="type" name="type">
	</form>
</div>
</body>
</html>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%
			String email = (String)request.getParameter("email");
%>
<html>
	<head>
		<link href="/GeoApi/style.css" rel="stylesheet" type="text/css">
		<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
		<link href="/GeoApi/fileuploader.css" rel="stylesheet" type="text/css">
		<title></title>
		
	</head>
	<body>
		<div id="upload-body">
			<br>
			<h2>success</h2>
			Your request has been added.  You will receive an email at <span class="hl-text"><%=email %></span> when it is complete.
			<br/>
			<br/>
		</div>
	</body>
</html>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%
			String email = (String)request.getAttribute("email");
			int queue = (Integer)request.getAttribute("queue");
			int records = (Integer)request.getAttribute("records");
			String error = (String)request.getAttribute("error");
%>
<html>
	<head>
		<link href="/GeoApi/css/style.css" rel="stylesheet" type="text/css">
		<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
		<title></title>
		
	</head>
	<body>
		<div style="width:300px;margin-left:auto;margin-right:auto;margin-top:auto;margin-bottom:auto;">
			<br>
			<% if(error != null) { %>
				<h2>error</h2>
				<div class="error" style="color:red;text-align:center;font-size:75%">
					<%= error %>
				</div>
			<% } else { %>
				<h2>success</h2>
				Your request has been added.  You will receive an email at <%=email %> when it is complete.
				<br/>
				<br/>
				<%=queue > 0 ? "You are number <b>" + (queue + 1) + "</b> in the queue. Based on the <b>" + records + " </b> records" 
				 + " in front of you it will take roughly <b>" + (records/90000) + "</b> days to begin processing."  :"You are next in the queue."%>
			<% } %>
		</div>
	</body>
</html>
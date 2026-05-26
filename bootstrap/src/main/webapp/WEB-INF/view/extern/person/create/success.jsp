<%@ include file="/WEB-INF/utils/tags.jsp" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE HTML><%
	be.vdab.gebruikersbeheer.derden.extension.FlashMap.processSessionMessages(request);
%>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>Nieuwe gebruiker aangemaakt</title>
</head>
<body>
<div>
	<h1>Nieuwe gebruiker aanmaken</h1>
	<div id="message" class="notificationOk">
		De gebruiker werd succesvol aangemaakt.
	</div>
</div>
</body>
</html>
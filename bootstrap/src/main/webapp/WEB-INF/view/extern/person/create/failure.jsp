<%@ include file="/WEB-INF/utils/tags.jsp" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE HTML><%
	be.vdab.gebruikersbeheer.derden.extension.FlashMap.processSessionMessages(request);
%>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>Nieuwe gebruiker aanmaken</title>
</head>
<body>
<div>
	<h1>Nieuwe gebruiker aanmaken</h1>
	<c:if test="${not empty errorMessage}">
		<div id="message" class="notificationErrorWarning">
			<c:out value="${errorMessage}"/>
		</div>
	</c:if>
	<c:if test="${empty errorMessage}">
		<div id="message" class="notificationErrorWarning">
			Er is een probleem met uw verzoek. Probeer het later opnieuw of contacteer de servicelijn op het nummer 0800 30 700.
		</div>
	</c:if>
</div>
</body>
</html>

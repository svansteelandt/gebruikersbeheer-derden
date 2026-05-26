<%@ page import="be.vdab.gebruikersbeheer.derden.util.SecurityUtils" %>
<%@ page import="be.vdab.iam.oidc.authentication.principal.VdabPrincipal" %>
<%@ include file="/WEB-INF/utils/tags.jsp" %>
<%
	VdabPrincipal vdabPrincipal = SecurityUtils.getInstance().getSessionIngelogdeUser();
	boolean interneUser = false;
	if (vdabPrincipal != null) {
		String username = vdabPrincipal.getUsername();

		if (username != null) {
			interneUser = vdabPrincipal.getSecurityDomain() == be.vdab.iam.oidc.authentication.SecurityDomain.INTERN;
		}
	}
%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE HTML>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>Home</title>
</head>
<body>
<% if (interneUser) { %>
<a href="<c:url value="/intern/search"/>">My VDAB</a>
<% } else { %>
<a href="<c:url value="/extern/organization"/>">My VDAB</a>
<% } %>
</body>
</html>
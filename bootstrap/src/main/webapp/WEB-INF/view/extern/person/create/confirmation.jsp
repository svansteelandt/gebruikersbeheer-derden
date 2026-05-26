<%@ include file="/WEB-INF/utils/tags.jsp"%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE HTML><%
	be.vdab.gebruikersbeheer.derden.extension.FlashMap.processSessionMessages(request);
%>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>Nieuwe gebruiker aanmaken</title>
	<script type="text/javascript">
		var isSubmitted = false;

		function doSubmit() {
			if (isSubmitted) {
				return false;
			}
			isSubmitted = true;
			return true;
		}
	</script>
</head>
<body>
<div>
	<h1>Nieuwe gebruiker aanmaken</h1>
	<p>
		Wil je als nieuwe gebruiker aangemaakt worden voor Organisatie <c:out value="${organisationName}" />, met e-mailadres <c:out value="${newEmailAddress}" /> en telefoonnummer <c:out value="${newMobileNumber}" />?
	</p>
	<form method="post" onsubmit="return doSubmit();">
		<input type="hidden" value="<c:out value="${token}" />">
		<input value="Bevestig" type="submit" id="submitButton"> of
		<a href="<c:out value="${cancelLink}" />">Annuleer</a>
	</form>
</div>
</body>
</html>

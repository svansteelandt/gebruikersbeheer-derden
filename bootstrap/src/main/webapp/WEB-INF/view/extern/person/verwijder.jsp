<%@ include file="/WEB-INF/utils/tags.jsp"%><%
%><%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE HTML>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Verwijder gebruiker</title>

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
	<form:form id="deletePersonForm" modelAttribute="personCommand">
		<h1>
			Verwijder gebruiker '<c:out value="${person.fullName}" />'
		</h1>
		<div class="warningBox">
			<h3>
				Ben je zeker dat je gebruiker '<c:out value="${person.fullName}" />' wil verwijderen?
			</h3>
			<div class="formButtons">
				<input type="submit" value="Verwijder" id="submitButton" onclick="return doSubmit();"/> of <a href="<c:url value="/extern/organization/${admindomain.dn.globalId}/overview"></c:url>" id="cancelButton">Annuleer</a>
			</div>
		</div>
	</form:form>
</body>
</html>

<%@ include file="/WEB-INF/utils/tags.jsp"%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE HTML>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Activeer gebruiker</title>

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
<form:form id="restorePersonForm" modelAttribute="personCommand">
    <h1>
        Activeer gebruiker '<c:out value="${person.fullName}" />'
    </h1>
    <div class="warningBox">
        <h3>Ben je zeker dat je gebruiker "<c:out value="${person.fullName}" />" wil activeren?</h3>
        <div class="formButtons">
            <input type="submit" value="Activeer" id="submitButton" onclick="return doSubmit();"/> of <a href="<c:url value="/extern/organization/${admindomain.dn.globalId}/person/overzicht"/>" id="cancelButton">Annuleer</a>
        </div>
    </div>
</form:form>
</body>
</html>

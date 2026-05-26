<%@ include file="/WEB-INF/utils/tags.jsp" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE HTML>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>Wijzig gebruiker</title>

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
<br/>
<form:form id="editPersonForm" modelAttribute="personCommand">
	<h1>
		Gebruiker:
		<c:out value="${personCommand.person.fullName}"/>
		- Wijzig gegevens
	</h1>

	<form:errors path="*">
		<div class="notificationErrorWarning">
			<h2>Vul alle verplichte velden in.</h2>
		</div>
	</form:errors>

	<p class="formHelp">Hieronder kan je de gegevens wijzigen.</p>
	<div class="blockDisplay">
		<h2>Wijzig gebruiker</h2>

		<div class="content">
			<table class="paddedTable" style="width: 100%">
				<tr class="sep">
					<td width="120" class="formLabel"><label for="person.firstName">Voornaam:<span class="formCompulsory">*</span> </label></td>
					<td>
						<spring:bind path="person.firstName">
							<c:if test="${status.error}"><img src="<%=request.getContextPath()%>/styles/notifications/error.gif"
							                                  title="${status.errorMessage}"/></c:if>
							<form:input path="person.firstName" size="25" maxlength="25"/>
							<c:if test="${status.error}">
								<div><span id="person.firstName.errors" class="errorMessage">${status.errorMessage}</span></div>
							</c:if>
						</spring:bind>
					</td>
				</tr>
				<tr class="sep">
					<td class="formLabel"><label for="person.lastName">Naam:<span class="formCompulsory">*</span> </label></td>
					<td>
						<spring:bind path="person.lastName">
							<c:if test="${status.error}"><img src="<%=request.getContextPath()%>/styles/notifications/error.gif"
							                                  title="${status.errorMessage}"/></c:if>
							<form:input path="person.lastName" size="25" maxlength="25"/>
							<c:if test="${status.error}">
								<div><span id="person.lastName.errors" class="errorMessage">${status.errorMessage}</span></div>
							</c:if>
						</spring:bind>
					</td>
				</tr>
				<c:if test="${personCommand.person.rrnAccessible}">
					<tr class="sep">
						<td width="120" class="formLabel"><label for="person.nationalNumber">Rijksregisternr.:<span class="formCompulsory"></span> </label></td>
						<td>
							<span id="person.nationalNumber"><c:out value="${personCommand.person.nationalNumberFormatted}"/></span>
							<div><form:errors path="person.nationalNumber" cssClass="errorMessage"/></div>
						</td>
					</tr>
				</c:if>
				<tr class="sep">
					<td width="120" class="formLabel"><label for="person.emailAddress">E-mail:<span class="formCompulsory">*</span> </label></td>
					<td>
						<spring:bind path="person.emailAddress">
							<c:if test="${status.error}"><img src="<%=request.getContextPath()%>/styles/notifications/error.gif"
							                                  title="${status.errorMessage}"/></c:if>
							<form:input path="person.emailAddress" size="75" maxlength="100"/>
							<c:if test="${status.error}">
								<div><span id="person.emailAddress.errors" class="errorMessage">${status.errorMessage}</span></div>
							</c:if>
						</spring:bind>
					</td>
				</tr>

				<tr class="sep">
					<td width="120" class="formLabel"><label for="person.phone">Telefoon:<span class="formCompulsory">*</span></label></td>
					<td>
						<spring:bind path="person.phone">
							<c:if test="${status.error}"><img src="<%=request.getContextPath()%>/styles/notifications/error.gif"
							                                  title="${status.errorMessage}"/></c:if>
							<form:input path="person.phone" size="20" maxlength="20"/>
							<c:if test="${status.error}">
								<div><span id="person.phone.errors" class="errorMessage">${status.errorMessage}</span></div>
							</c:if>
						</spring:bind>
					</td>
				</tr>

				<tr class="sep">
					<td width="120" class="formLabel"><label for="person.mobile">Gsm:<span class="formCompulsory">*</span></label></td>
					<td>
						<spring:bind path="person.mobile">
							<c:if test="${status.error}"><img src="<%=request.getContextPath()%>/styles/notifications/error.gif"
							                                  title="${status.errorMessage}"/></c:if>
							<form:input path="person.mobile" size="20" maxlength="20"/>
							<c:if test="${status.error}">
								<div><span id="person.mobile.errors" class="errorMessage">${status.errorMessage}</span></div>
							</c:if>
						</spring:bind>
						<br/>We gebruiken dit nummer om de gebruiker een sms-code te sturen als die inlogt met gebruikersnaam en wachtwoord.
					</td>
				</tr>

				<tr class="sep">
					<td width="120" class="formLabel"><label for="person.vdabUid">Gebruikersnaam<span class="formCompulsory">*</span> </label></td>
					<td>
						<c:choose>
							<c:when test="${personCommand.person.profileName =='vdabvirtual'}">
								<span id="person.vdabUid"><c:out value="${personCommand.person.userId}"/></span>
							</c:when>
							<c:otherwise>
								<span id="person.vdabUid"><c:out value="${personCommand.person.vdabUid}"/></span>
							</c:otherwise>
						</c:choose>
					</td>
				</tr>
			</table>
		</div>
		<div class="formButtons">
			<input type="submit" value="Bewaar" id="submitButton" onclick="return doSubmit();"/> of <a
				href="<c:url value="/extern/organization/${admindomain.dn.globalId}/person/${personCommand.person.dn.globalId}/user_detail"/>"
				id="cancelButton">Annuleer</a>
		</div>
	</div>
	<div class="infoBox" style="border-width:1px;margin-top:1em">
		<h3>Is het wachtwoord van <c:out value="${personCommand.person.firstName}"/> kwijt?</h3>
		<p>Als je <a
				href="/gebruikersbeheer-derden/extern/organization/${admindomain.dn.globalId}/person/${personCommand.person.dn.globalId}/password/reset"
				id="passwordForgottenVrouw">hier</a> klikt, dan wordt er naar het bovenvermelde emailadres een mail gestuurd met een link om een nieuw
			wachtwoord te kiezen.</p>
	</div>
</form:form>
</body>
</html>

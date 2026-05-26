<%@ include file="/WEB-INF/utils/tags.jsp" %>
<%
%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE HTML><%
	be.vdab.gebruikersbeheer.derden.extension.FlashMap.processSessionMessages(request);
%>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>Nieuwe gebruiker</title>

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
<h1>
	<a id="maincontent"></a>
	<c:out value="${adminDomain.name}"/>
	Nieuwe gebruiker aanmaken
</h1>

<c:if test="${not empty errorCreate}">
	<div id="message" class="<c:out value="${errorCreate.type}" />">
		<c:set var="msg" value="${errorCreate.text}" scope="page"/>
		<c:out value="${msg}"/>
	</div>
</c:if>

<form:form id="createPersonForm" modelAttribute="personCommand" method="post">
	<form:errors path="*">
		<div class="notificationErrorWarning">
			<h2>Vul alle verplichte velden in.</h2>
		</div>
	</form:errors>
	<p class="formHelp">Maak een nieuwe gebruiker aan en geef hem toegangsrechten.</p>
	<div class="formStandard">
		<table class="paddedTable">
			<tr class="sep">
				<td width="120" class="formLabel">
					<label for="person.nationalNumber">Rijksregisternr:<span class="formCompulsory">*</span> </label>
				</td>
				<td>
					<spring:bind path="person.nationalNumber">
						<c:if test="${status.error}"><img src="<%=request.getContextPath()%>/styles/notifications/error.gif"
						                                  title="${status.errorMessage}"/></c:if>
						<form:input path="person.nationalNumber"/>
						<c:if test="${status.error}">
							<div><span id="person.nationalNumber.errors" class="errorMessage">${status.errorMessage}</span></div>
						</c:if>
					</spring:bind>
					<div class="formHelp">Heeft deze gebruiker geen rijksregisternummer? Bel dan naar ons gratis nummer 0800 30 700.</div>
				</td>
			</tr>

			<tr class="sep">
				<td width="120" class="formLabel">
					<label for="person.firstName">Voornaam:<span class="formCompulsory">*</span> </label>
				</td>
				<td>
					<spring:bind path="person.firstName">
						<c:if test="${status.error}"><img src="<%=request.getContextPath()%>/styles/notifications/error.gif"
						                                  title="${status.errorMessage}"/></c:if>
						<form:input path="person.firstName" size="25" maxlength="25" cssClass="error"/> <br/>
						<c:if test="${status.error}">
							<div><span id="person.firstName.errors" class="errorMessage">${status.errorMessage}</span></div>
						</c:if>
					</spring:bind>
				</td>
			</tr>

			<tr class="sep">
				<td width="120" class="formLabel">
					<label for="person.lastName">Naam:<span class="formCompulsory">*</span> </label>
				</td>
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

			<tr class="sep">
				<td class="formLabel">
					<label for="person.emailAddress">E-mail:<span class="formCompulsory">*</span> </label>
				</td>
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
				<td class="formLabel">
					<label for="person.phone">Telefoon:<span class="formCompulsory">*</span> </label>
				</td>
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
				<td class="formLabel"><label for="person.mobile">Gsm:<span class="formCompulsory">*</span></label></td>
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

			<c:if test="${not empty personCommand.roles}">
				<tr>
					<td class="formLabel">Toegangsrechten:</td>
					<td>
						<ul id="toegangsrechtenList" class="fieldList" style="margin:0">
							<c:forEach var="role" items="${personCommand.roles}" varStatus="i">
								<c:if test="${not empty role.vdabRoleName}">
									<c:if test="${role.available}">
										<li><label for="roles${i.index}.hasRole1">
											<form:checkbox path="roles[${i.index}].hasRole" disabled="${role.hasRole}"/>
											<strong><c:out value="${role.vdabRoleName}"/></strong>
											<div><c:out value="${role.vdabRoleDescription}"/></div>
										</label></li>
									</c:if>
									<c:if test="${!role.available}">
										<li class="passive"><input type="checkbox" disabled="disabled"/><strong><c:out value="${role.vdabRoleName}"/></strong>Gebruikers
											beheren (maximum aantal administrators bereikt)
										</li>
									</c:if>
								</c:if>
							</c:forEach>
						</ul>
					</td>
				</tr>
			</c:if>
		</table>

		<div class="formButtons line">
			<input type="submit" value="Bewaar" id="submitButton" onclick="return doSubmit();"/> of <a
				href="<c:url value="/extern/organization/${admindomain.dn.globalId}/person/overzicht"/>" id="cancelButton">Annuleer</a>
		</div>
	</div>
</form:form>
</body>
</html>

<%@ include file="/WEB-INF/utils/tags.jsp"%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE HTML>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <script type="text/javascript">
        $(document).ready(function() {
            makeSortTable('1');
        });

        var isSubmitted = false;

        function doSubmit() {
	        if (isSubmitted) {
		        return false;
	        }
	        isSubmitted = true;
	        return true;
        }
    </script>
    <title>Wijzig toegangsrechten</title>
</head>

<body>
<a id="maincontent"></a><h1>Gebruiker: <c:out value="${personCommand.person.fullName}"/> - Wijzig toegangsrechten</h1>
<p class="formHelp">Hieronder kan je de toegangsrechten wijzigen.</p>
<form:form id="personRolesForm" modelAttribute="personCommand">
    <div class="blockDisplay">
        <h2>Toegangsrechten</h2>
        <div class="content">
            <table id="sorter" class="sortableTable">
                <thead>
                <tr>
                    <th class="header"><a>Naam</a></th>
                    <th class="header"><a>Omschrijving</a></th>
                    <th class="header">Toegang</th>
                </tr>
                </thead>

                <tbody>
                <c:forEach var="role" items="${personCommand.person.roles}" varStatus="i">
                    <c:if test="${not empty role.vdabRoleName}">
                        <tr class="sep">
                            <td><c:out value="${role.vdabRoleName}"/></td>
                            <td class="passive"><c:out value="${role.vdabRoleDescription}"/></td>

                            <c:if test="${(ingelogde == personCommand.person.vdabUid) && (role.adminRole)}">
                                <td><input type="checkbox" checked="checked" disabled="disabled"  /><span class="passive">Niet beschikbaar</span></td>
                            </c:if>
                            <c:if test="${!((ingelogde == personCommand.person.vdabUid) && (role.adminRole))}">
                                <c:if test="${role.available}">
                                    <c:if test="${!personCommand.person.roles[i.index].pending}">
                                        <td><form:checkbox path="person.roles[${i.index}].hasRole" /></td>
                                    </c:if>
                                    <c:if test="${personCommand.person.roles[i.index].pending}">
                                        <td><input type="checkbox" checked="checked" disabled="disabled"  /><span class="passive">aanvraag wordt verwerkt</span></td>
                                    </c:if>
                                </c:if>
                                <c:if test="${!role.available}">
                                    <td><input type="checkbox" disabled="disabled" /><span class="passive">maximum aantal administrators bereikt</span></td>
                                </c:if>
                            </c:if>
                        </tr>
                    </c:if>
                </c:forEach>

                </tbody>
            </table>

            <div class="formButtons">
                <input type="submit" value="Bewaar" id="submitButton" onclick="return doSubmit();"/> of <a href="<c:url value="/extern/organization/${admindomain.dn.globalId}/person/${personCommand.person.dn.globalId}/user_detail"/>" id="cancelButton">Annuleer</a>
            </div>
        </div>
    </div>
</form:form>
</body>
</html>
<%@ include file="/WEB-INF/utils/tags.jsp"%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE HTML><%
    be.vdab.gebruikersbeheer.derden.extension.FlashMap.processSessionMessages(request);
%>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Gebruiker</title>
</head>
<body>
<form:form id="detailPersonForm" modelAttribute="personCommand" method="post">
    <a class="actionBack"  style="float:right" href="<c:url value="/extern/organization/${admindomain.dn.globalId}/overview"/>">Terug naar <c:out value="${admindomain.name}"/></a>
    <h1>Gebruiker: <c:out value="${person.fullName}"/></h1>

    <c:if test="${not empty errorMessageMinAdmins}">
        <div id="message" class="<c:out value="${errorMessageMinAdmins.type}" />">
            Je kan deze administrator niet verwijderen. Je moet minstens 1 administrator aanduiden.
        </div>
    </c:if>

    <c:if test="${not empty errorMessageMaxAdmins}">
        <div id="message" class="<c:out value="${errorMessageMaxAdmins.type}" />">
            Maximum aantal admins bereikt.
        </div>
    </c:if>

    <c:if test="${not empty passwordReset}">
        <c:set var="mymessage" value="${passwordReset}" scope="page" />
        <div id="message" class="<c:out value="${mymessage.type}" />">
            <c:out value="${mymessage.text}"/>
        </div>
    </c:if>

    <p class="formHelp">Hieronder vind je meer info over deze gebruiker en zijn toegangsrechten.</p>

    <div class="blockDisplay">
        <h2>Gegevens</h2>
        <div class="blockActions"><a class="actionEdit" href="<c:url value="/extern/organization/${admindomain.dn.globalId}/person/${person.dn.globalId}/edit"/>" id="editPerson">Wijzig</a></div>
        <div class="content">
            <table class="paddedTable" style="width:100%">
                <tr class="sep">
                    <td width="120" class="readLabel">Naam:</td>
                    <td><c:out value="${person.fullName}"/></td>
                </tr>
                <c:if test="${person.rrnAccessible}">
                    <tr class="sep">
                        <td width="120" class="readLabel">Rijksregisternr:</td>
                        <td><c:out value="${person.nationalNumberFormatted}"/></td>
                    </tr>
                </c:if>
                <tr class="sep">
                    <td class="readLabel">E-mail:</td>
                    <td><a href="mailto:${person.emailAddress}"><c:out value="${person.emailAddress}"/></a></td>
                </tr>
                <tr class="sep">
                    <td width="120" class="readLabel">Telefoon:</td>
                    <td><c:out value="${person.phone}"/></td>
                </tr>
                <tr class="sep">
                    <td width="120" class="readLabel">Gsm:</td>
                    <td><c:out value="${person.mobile}"/></td>
                </tr>
                <tr class="sep">
                    <td width="120" class="readLabel">Gebruikersnaam:</td>
                    <td>
                        <c:choose>
                            <c:when test="${person.profileName =='vdabvirtual'}">
                                <c:out value="${person.userId}"/>
                            </c:when>
                            <c:otherwise>
                                <c:out value="${person.vdabUid}"/>
                            </c:otherwise>
                        </c:choose>
                    </td>
                </tr>
                <tr class="sep">
                    <td class="readLabel">Vestiging:</td>
                    <td><c:out value="${admindomain.name}"/> (<c:out value="${admindomain.ikpIntern}"/>)</td>
                </tr>
            </table>
        </div>
    </div>
    <div class="blockDisplay">
        <h2>Toegangsrechten</h2>
        <div class="blockActions"><a href="<c:url value="/extern/organization/${admindomain.dn.globalId}/person/${person.dn.globalId}/gebruiker_wijzig_rechten"/>" class="actionEdit" id="editRights">Wijzig</a></div>
        <div class="content">
            <table id="sorter" class="sortableTable" style="width:100%">
                <thead>
                <tr>
                    <th><a>Naam </a></th>
                    <th colspan="2"><a>Omschrijving </a></th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="role" items="${person.roles}">
                    <c:if test="${role.hasRole and (not empty role.vdabRoleName)}">
                        <tr>
                            <td><c:out value="${role.vdabRoleName}"/></td>
                            <td><c:out value="${role.vdabRoleDescription}"/></td>

                            <c:if test="${!role.pending}">
                                <td>&nbsp;</td>
                            </c:if>
                            <c:if test="${role.pending}">
                                <td>aanvraag wordt verwerkt</td>
                            </c:if>
                        </tr>
                    </c:if>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </div>
	<div class="infoBox" style="border-width:1px;margin-top:1em">
        <h3>Is het wachtwoord van <c:out value="${person.firstName}"/> kwijt?</h3>
        <p>Als je <a href="/gebruikersbeheer-derden/extern/organization/${admindomain.dn.globalId}/person/${person.dn.globalId}/password/reset" id="passwordForgottenVrouw">hier</a> klikt, dan wordt er naar het bovenvermelde emailadres een mail gestuurd met een link om een nieuw wachtwoord te kiezen.</p>
    </div>

</form:form>
</body>
</html>

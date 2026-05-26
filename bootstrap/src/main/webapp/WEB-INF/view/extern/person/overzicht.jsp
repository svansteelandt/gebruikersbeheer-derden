<%@ include file="/WEB-INF/utils/tags.jsp"%><%
%><%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE HTML><%
    be.vdab.gebruikersbeheer.derden.extension.FlashMap.processSessionMessages(request);
%>
<html>
<head>
    <meta http-equiv="content-language" content="nl" />
    <meta http-equiv="content-type" content="text/html; charset=utf-8" />
    <meta content="" name="description"/>
    <meta content="" name="keywords"/>

    <title>Toegangsbeheer</title>
    <script type="text/javascript" class="init">
        $(document).ready(function() {
            $('.sorter').dataTable( {
                "aaSorting": [[ 0, "asc" ]],
                "bPaginate": true,
                "bFilter": false,
                "lengthMenu": [[10, 25, 50, -1], [10, 25, 50, "All"]],
                "oLanguage": {
                    "sEmptyTable" : "Geen gebruikers gevonden",
                    "sSearch": "Filter:",
                    "sLengthMenu": "Toon _MENU_ rijen",
                    "sInfoEmpty": "",
                    "sZeroRecords": "Geen gebruikers gevonden",
                    "sInfo" : "_START_ tot _END_ van _TOTAL_ resultaten",
                    "sInfoFiltered": " (gefilterd uit _MAX_ resultaten)",
                    "sInfoPostFix": "",
                    "oPaginate": {
                        "sFirst": "Eerste",
                        "sLast": "Laatste",
                        "sNext": "Volgende",
                        "sPrevious": "Vorige"
                    }
                }
            } );
        } );
    </script>
</head>

<body class="werkgever">
<form:form id="detailToegangsrechtenForm" modelAttribute="adminDomainCommand" method="post">
    <c:if test="${admindomainsize > 1}">
        <a class="actionBack" style="float:right" href="<c:url value="/extern/organization"/>">Terug naar alle vestigingen</a>
    </c:if>
    <h1>Toegangsbeheer <c:out value="${admindomain.name}" /></h1>
    <c:if test="${not empty insertPerson}">
        <div id="message" class="<c:out value="${insertPerson.type}" />">
            <c:set var="person" value="${insertPerson.text}" scope="page" />
            <h2>Gebruiker '<c:out value="${person}"  />' werd aangemaakt. </h2>
            <br />
            <c:out value="${person}"  /> zal een e-mail ontvangen met de instructies om een paswoord te kiezen.
            <c:if test="${not empty errorMessageMinAdmins}">
                Je kan deze administrator niet verwijderen. Je moet minstens 1 administrator aanduiden.
            </c:if>
            <c:if test="${not empty errorMessageMaxAdmins}">
                Maximum aantal admins bereikt.
            </c:if>
        </div>
    </c:if>
    <c:if test="${empty insertPerson}">
        <c:if test="${not empty errorMessageMaxAdmins}">
            <div id="message" class="<c:out value="${errorMessageMaxAdmins.type}" />">
                Maximum aantal admins bereikt.
            </div>
        </c:if>
        <c:if test="${not empty errorMessageMinAdmins}">
            <div id="message" class="<c:out value="${errorMessageMinAdmins.type}" />">
                Je kan deze administrator niet verwijderen. Je moet minstens 1 administrator aanduiden.
            </div>
        </c:if>
    </c:if>
    <c:if test="${not empty insertPersonWithoutRoles}">
        <div id="message" class="<c:out value="${insertPersonWithoutRoles.type}" />">
            <c:set var="person" value="${insertPersonWithoutRoles.text}" scope="page" />
            <h2>Gebruiker ' <c:out value="${person}"  /> ' werd aangemaakt. </h2>
        </div>
    </c:if>

    <c:if test="${not empty deletePerson}">
        <div id="message" class="<c:out value="${deletePerson.type}" />">
            <h2>Gebruiker '<c:out value="${deletePerson.text}"  />' succesvol verwijderd. </h2>
        </div>
    </c:if>

    <c:if test="${not empty errorMessageMinAdminsOnDeleteUser}">
        <div id="message" class="<c:out value="${errorMessageMinAdminsOnDeleteUser.type}" />">
            Je kan deze administrator niet verwijderen. Je moet minstens 1 administrator aanduiden.
        </div>
    </c:if>

    <c:if test="${not empty errorCreate}">
        <div id="message" class="<c:out value="${errorCreate.type}" />">
            <c:out value="${errorCreate.text}" />
        </div>
    </c:if>
    <c:if test="${not empty restorePerson}">
        <div id="message" class="<c:out value="${restorePerson.type}" />">
            <h2>Gebruiker '<c:out value="${restorePerson.text}"  />' succesvol geactiveerd.
                <c:if test="${not empty passwordReset}">
                    <br /><c:out value="${passwordReset.text}" />
                </c:if>
            </h2>
        </div>
    </c:if>
    <p class="formHelp">Hieronder vind je een overzicht van de mensen in deze vestiging die toegang hebben tot Mijn VDAB en welke toegangsrechten ze hebben.</p>

    <div style="float: right; clear: none; margin-top: 18px;"><a target="_blank" href="<c:url value="/extern/organization/${admindomain.dn.globalId}/exporteer" />">Exporteer</a></div>
    <h2 style="clear: both; margin-top: 24px; margin-bottom: 12px;">Gebruikers</h2>
    <table class="display sorter sortableTable">
        <thead>
        <tr>
            <th style="width:20%"><a>Naam</a></th>
            <th style="width:20%"><a>Gebruikersnaam</a></th>
            <th style="width:30%"><a>Toegangsrechten</a></th>
            <th style="width:15%">Geblokkeerd</th>
            <th style="width:15%">Verwijder</th>
        </tr>
        </thead>
        <tfoot>
        <tr class="addItem">
            <td colspan="5"><a class="actionNewCandidate" href="<c:url value="/extern/organization/${admindomain.dn.globalId}/person/create"/>">Voeg gebruiker toe</a></td>
        </tr>
        <c:if test="${fn:length(persons) > 20}">
            <tr>
                <td colspan="4" class="tableNext">
                    <div id="pagerOverview" style="width: 300px; display:inline; float:left;">
                        <span class="pagedisplay"></span>
                    </div>
                    <div id="pager" class="pager" style="width: 150px; display:inline; float:right;">
                    </div>
                </td>
            </tr>
        </c:if>
        </tfoot>
        <tbody>
        <c:forEach var="person" items="${persons}">
            <tr>
                <td><a href="<c:url value="/extern/organization/${admindomain.dn.globalId}/person/${person.dn.globalId}/user_detail"/>"><c:out value="${person.fullName}" /></a></td>
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
                <td><c:out value="${person.displayRole}"/></td>
                <td>
                    <c:if test="${person.suspend}">
                        Ja
                    </c:if>
                    <c:if test="${not person.suspend}">
                        Nee
                    </c:if>
                </td>
                <c:if test="${ingelogde != person.vdabUid}">
                    <td class="loneCell"><a class="actionLoneDelete" href="<c:url value="/extern/organization/${admindomain.dn.globalId}/person/${person.dn.globalId}/delete"/>"><img src="<c:url value="/images/action/trash.gif"/>" /></a></td>
                </c:if>
                <c:if test="${ingelogde == person.vdabUid}">
                    <td></td>
                </c:if>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</form:form>
<br/>

<h2 style="clear: both; margin-top: 24px; margin-bottom: 12px;">Verwijderde gebruikers</h2>
<table class="sorter sortableTable">
    <thead>
    <tr>
        <th style="width:20%"><a>Naam</a></th>
        <th style="width:20%"><a>Gebruikersnaam</a></th>
        <th style="width:45%" nowrap="nowrap"><a>Reden van verwijdering</a></th>
        <th style="width:15%">Actie</th>
    </tr>
    </thead>

    <tbody>
    <c:forEach var="person" items="${personsinprullenbak}">
        <tr>
            <td nowrap="nowrap"><c:out value="${person.fullName}"/></td>
            <td nowrap="nowrap"><c:out value="${person.vdabUid}"/></td>
            <td nowrap="nowrap"><c:out value="${person.deleteDescription}"/></td>
            <td>
                <a href="<c:url value="/extern/organization/${admindomain.globalId}/person/${person.dn.globalId}/restore"/>">Activeer</a>
            </td>
        </tr>
    </c:forEach>
    </tbody>
</table>

<form:form id="detailToegangsrechtenForm2" style="margin-top: 30px;" modelAttribute="adminDomainCommand" method="post">
    <h2 style="clear: both; margin-top: 24px; margin-bottom: 12px;">Toegangsrechten</h2>
    <table class="sorter sortableTable">
        <thead>
        <tr>
            <th style="width:30%"><a>Naam</a></th>
            <th style="width:55%"><a>Omschrijving</a></th>
            <th style="width:15%" nowrap="nowrap">Wijzig gebruikers</th>
        </tr>
        </thead>

        <tbody>
        <c:forEach var="role" items="${admindomain.roles}">
            <c:if test="${not empty role.vdabRoleName}">
                <tr>
                    <td nowrap="nowrap"><c:out value="${role.vdabRoleName}" /></td>
                    <td class="passive"><c:out value="${role.vdabRoleDescription}" /></td>
                    <c:if test="${role.available}">
                        <td><a class="actionUsers" href="<c:url value="/extern/organization/${admindomain.dn.globalId}/role/${role.dn.globalId}"/>">Wijzig</a></td>
                    </c:if>
                    <c:if test="${!role.available}">
                        <td><span class="passive">Niet beschikbaar</span></td>
                    </c:if>
                </tr>
            </c:if>
        </c:forEach>
        </tbody>
    </table>
</form:form>
<br/><br/>
<a href="https://leren-partner.vdab.be/course/index.php?categoryid=11" rel="noopener" id="demolink" class="actionInfo" target="_blank">Wegwijs in
	toegangsbeheer</a>
</body>
</html>

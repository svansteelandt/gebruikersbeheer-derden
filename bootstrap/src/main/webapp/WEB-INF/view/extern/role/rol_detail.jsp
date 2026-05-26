<%@ include file="/WEB-INF/utils/tags.jsp"%><%
%><%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE HTML>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Toegangsrecht:</title>

    <script type="text/javascript">
        $(document).ready(function() {
            $('#sorter').dataTable( {
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

            $("#sorter input").click(function() {
                $(this).parents("table").trigger("update");
            })

            <c:if test="${roleCommand.roleObject.adminRole}">
            $("input:checkbox").change(function() {
                checkAantal();
            });

            checkAantal();

            $("#submitButton").click(function() {
                //tijdelijke fix om waarden bij submit toch door te sturen
                $("input:checkbox:checked.editableCheckbox").removeAttr('disabled');
            });
            </c:if>
        });

        <c:if test="${roleCommand.roleObject.adminRole}">
        var maxAantalAdmins= '${administrators_maxcount}';
        var minAantalAdmins = '${administrators_mincount}';

        function checkAantal() {
            var currentAantalAdmins = $("input:checkbox:checked.editableCheckbox").length;
            if (currentAantalAdmins >= maxAantalAdmins) {
                $("input:checkbox:not(:checked).editableCheckbox").attr('disabled', true);
            } else {
                $("input:checkbox:not(:checked).editableCheckbox").removeAttr('disabled');
            }

            if (currentAantalAdmins <= minAantalAdmins) {
                $("input:checkbox:checked.editableCheckbox").attr('disabled', true);
            } else {
                $("input:checkbox:checked.editableCheckbox").removeAttr('disabled');
            }
        }
        </c:if>
    </script>
</head>
<body>
<form:form method="post" id="personRolesForm" modelAttribute="roleCommand">
    <a href="<c:url value="/extern/organization/"/>" class="actionBack" style="float:right">Terug naar <c:out value="${admindomain.name}" /></a>
    <h1><a id="maincontent"></a><c:out value="${admindomain.name}" /> - Toegangsrecht:  "<c:out value="${roleCommand.roleObject.vdabRoleName}"/>"</h1>
    <p class="formHelp">Hieronder vind je een overzicht van alle gebruikers die toegang hebben tot 'Mijn VDAB'. Je kan dit toegangsrecht wijzigen.</p>

    <div class="blockDisplay">
        <h2>Gebruikers met toegangsrecht "<c:out value="${roleCommand.roleObject.vdabRoleName}"></c:out>"</h2>
        <div class="content">
            <table id="sorter" class="display sortableTable" style="width:100%">
                <thead>
                <tr>
                    <th><a>Naam</a></th>
                    <th><a>Gebruikersnaam</a></th>
                    <th><a>Heeft toegang tot</a></th>
                </tr>
                </thead>

                <tbody>
                <c:forEach var="person" items="${roleCommand.personObject}" varStatus="i">
                    <tr class="sep">
                        <td><label for="personObject${i.index}.hasRole1"><c:out value="${person.fullName}"/></label></td>
                        <td><c:out value="${person.vdabUid}"/></td>

                        <c:if test="${(ingelogde == person.vdabUid) && (roleCommand.roleObject.adminRole)}">
                            <td><input type="checkbox" checked="checked" disabled="disabled" id="personObject${i.index}.hasRole1"/><span class="passive">Niet beschikbaar</span></td>
                        </c:if>

                        <c:if test="${!((ingelogde == person.vdabUid) && (roleCommand.roleObject.adminRole))}">
                            <c:if test="${!roleCommand.personObject[i.index].pending}">
                                <td><form:checkbox cssClass="editableCheckbox" value="personObject[${i.index}].hasRole" path="personObject[${i.index}].hasRole" /></td>
                            </c:if>
                            <c:if test="${roleCommand.personObject[i.index].pending}">
                                <td><input type="checkbox" checked="checked" disabled="disabled" id="personObject${i.index}.hasRole1"/><span class="passive">aanvraag wordt verwerkt</span></td>
                            </c:if>
                        </c:if>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
            <br/>
            <div class="formButtons">
                <input type="submit" value="Bewaar" id="submitButton"/> of <a href="<c:url value="/extern/organization/${admindomain.dn.globalId}/overview"/>"  id="cancelButton">Annuleer</a>
            </div>
        </div>
    </div>
</form:form>
</body>
</html>
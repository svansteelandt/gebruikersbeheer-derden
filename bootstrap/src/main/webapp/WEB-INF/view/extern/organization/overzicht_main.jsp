<%@ include file="/WEB-INF/utils/tags.jsp"%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE HTML>
<html lang="nl-BE">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>kies vestiging</title>
	<script type="text/javascript" class="init">
		$(document).ready(function () {
			$('#sorter').dataTable({
				"bPaginate": true,
				"bFilter": false,
				"order": [[1, "asc"]],
				"oLanguage": {
					"sSearch": "Filter:",
					"sLengthMenu": "Toon _MENU_ rijen",
					"sInfoEmpty": "",
					"sInfo": "_START_ tot _END_ van _TOTAL_ resultaten",
					"sInfoFiltered": " (gefilterd uit _MAX_ resultaten)",
					"sInfoPostFix": "",
					"oPaginate": {
						"sFirst": "Eerste",
						"sLast": "Laatste",
						"sNext": "Volgende",
						"sPrevious": "Vorige"
					}
				}
			});
		});
	</script>
</head>
<body>
<form:form id="overviewOrganizationForm" modelAttribute="adminDomainCommand">
	<h1>Gebruikersbeheer</h1>
	<p class="formHelp">Hieronder vind je een overzicht van al je vestigingen. Klik op de vestiging voor meer details.</p>
	<h3 id="tableTitle">Je vestigingen</h3>
	<table aria-describedby="tableTitle" id="sorter" class="display sortableTable">
		<thead>
		<tr>
			<th scope="col"><a>Naam</a></th>
			<th scope="col"><a>Adres</a></th>
			<th scope="col"><a>IKP-Nr.</a></th>
		</tr>
		</thead>
		<tfoot>
		<tr class="addItem">
			<td colspan="3"><span class="passive">Wil je nog een vestiging toevoegen? Contacteer de servicelijn op het nummer 0800 30 700.</span></td>
		</tr>
		</tfoot>
		<tbody>
		<c:forEach var="domain" items="${adminDomains}">
			<tr>
				<td nowrap="nowrap">
					<a href="<c:url value="/extern/organization/${domain.dn.globalId}/overview"/>"><c:out value="${domain.name}"/></a>
					<c:if test="${domain.headQuarter}">
						(hoofdzetel)
					</c:if>
				</td>
				<td class="passive"><c:out value="${domain.street}"></c:out>, <c:out value="${domain.postalcode}"></c:out> <c:out
						value="${domain.city}"></c:out></td>
				<td class="passive"><c:out value="${domain.ikp}"></c:out></td>
			</tr>
		</c:forEach>
		</tbody>
	</table>
</form:form>
<br/><br/>
<a href="https://leren-partner.vdab.be/course/index.php?categoryid=11" rel=noopener id="demolink" class="actionInfo" target="_blank">Wegwijs in
	toegangsbeheer</a>
</body>
</html>
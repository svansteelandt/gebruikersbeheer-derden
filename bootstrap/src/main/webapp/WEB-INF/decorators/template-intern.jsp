<%@ page import="be.vdab.gebruikersbeheer.derden.util.SecurityUtils" %>
<%@ include file="/WEB-INF/utils/tags.jsp" %>
<%@ page import="be.vdab.iam.oidc.authentication.principal.VdabPrincipal" %>
<%@ page import="be.vdab.gebruikersbeheer.util.common.constants.RoleNames" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="nl">
<head>
	<meta http-equiv="content-language" content="nl"/>
	<meta http-equiv="content-type" content="text/html; charset=utf-8"/>
	<meta content="" name="description"/>
	<meta content="" name="keywords"/>

	<title>Userbeheer, <sitemesh:write property='title'/> - VDAB</title>

	<script type="text/javascript" src="/gebruikersbeheer-derden/scripts/jquery-3.6.0.min.js"></script>
	<script type="text/javascript" src="/gebruikersbeheer-derden/scripts/jquery.ui-1.13.1.min.js"></script>
	<script type="text/javascript" src="/gebruikersbeheer-derden/scripts/jquery.dataTables-1.11.5.min.js"></script>

	<link href="/gebruikersbeheer-derden/styles/application.css" rel="stylesheet" type="text/css">
	<style type="text/css" media="screen">
		/*<![CDATA[*/
		#infoHeader {
			color: #ccc;
			background-color: #333;
			padding: 5px;
			padding-right: 30px;
			padding-left: 30px;
			height: 20px;
		}

		/*]]>*/
	</style>
	<style type="text/css" media="screen">
		@import "https://www.vdab.be/style/frame_consulent.css";
		@import "https://www.vdab.be/style/main.css";
		@import "https://www.vdab.be/style/css_vac.css";

		/*<![CDATA[*/
		#sbox-overlay {
			background: url('gray.png') top left repeat;
		}

		a.hiddenLink.red {
			color: #FF0000;
		}

		a.hiddenLink.red:hover {
			color: #933;
		}

		/*]]>*/
	</style>

	<!--[if lt IE 7]>
	<style media="screen" type="text/css">
		#container {
			height: 100%;
		}
	</style>
	<![endif]-->

	<sitemesh:write property='head'/>
</head>

<body class="wg"><%
	VdabPrincipal vdabPrincipal = SecurityUtils.getInstance().getSessionIngelogdeUser();
	String fullName = vdabPrincipal.getUsername();

	boolean enableEditFunctionality = vdabPrincipal.hasAnyRole(RoleNames.ROL_BEHEERDERS_DERDEN, RoleNames.ROL_BEHEERDERS_DERDEN_ZONDER_RRN);
	boolean enableRFI = vdabPrincipal.hasRole(RoleNames.ROL_CVS_RFI);
	boolean enableCVSApproval = vdabPrincipal.hasRole((RoleNames.ROL_CVS_APPROVAL);

	String environment = System.getProperty("ENVIRONMENT");
	boolean isProduction = "PRD".equalsIgnoreCase(environment);
	boolean hideInfoHeader = System.getProperty("be.vdab.infoheader.visible") != null && "false".equalsIgnoreCase(System.getProperty("be.vdab.infoheader.visible"));
	if (!isProduction && !hideInfoHeader) { %>
<div id="infoHeader">
            <span style="font-weight: bold; float:left">
                <%=environment%>
            </span>
	<span style="float:right;">versie: ${project.version} (gecompileerd op:  ${build.time})</span>
</div>
<% } %>

<div id="container">
	<div id="header">
		<div id="access">
			<a href="#maincontent" accesskey="S">Ga direct naar de inhoud (Access Key S)</a>
		</div>
		<a href="https://www.vdab.be" id="logo" name="logo">
			<img src="${logo_url}" alt="VDAB" title="VDAB (Vlaamse Dienst voor Arbeidsbemiddeling en Beroepsopleiding)"/>
		</a>
		<div class="hiddenHeader">Algemene links</div>
		<ul id="login" style="float: right">
			<li>Je bent aangemeld als <a href="javascript:void(0)" onclick="$('#userDetailsFrame').show();" id="username"><%=fullName%>
			</a></li>

			<li class="last">
				<a href="/logout">Afmelden</a>
			</li>
		</ul>

		<div id="userDetailsFrame" style="display: none;">
			<a href="javascript:void(0)" class="image" id="close" onclick="$('#userDetailsFrame').hide();"><img
					src="/gebruikersbeheer-derden/styles/action/close_icon.png" alt="Sluit"/> </a>
			<iframe frameborder="0"></iframe>
		</div>

		<h2>
			Userbeheer <a href="/link/" id="applicationSwitcher">Switch naar andere applicatie
		</a>
		</h2>
		<ul class="applicationSwap">

			<li><a href="/link/"><span>Vacaturebeheer</span> </a></li>
			<li><a href="/link/"><span>Opleidingenbeheer</span> </a></li>
			<li><a href="/link/"><span>Nieuwe registratie</span> </a></li>
		</ul>
		<div id="serviceNav">
			<ul id="serviceMenu" style="float: left">
				<% if (enableEditFunctionality) { %>
				<% if (enableRFI) { %>
				<li><a href="<c:url value="/intern/search/takenweblerendata"/>">Webleren Data</a></li>
				<% } %>
				<% if (enableCVSApproval) { %>
				<li><a href="<c:url value="/intern/search/takengoedkeuring"/>">Goedkeuring CVS</a></li>
				<% } %>
				<% } %>
				<li class="last"><a href="<c:url value="/intern/search/zoeken"/>">Vestiging opzoeken</a></li>
				<li class="last"><a href="<c:url value="/intern/search/zoekengebruikersnaam"/>">Gebruiker opzoeken</a></li>
			</ul>
		</div>
	</div>

	<div id="contentContainer" class="clearfix">
		<div id="content">
			<sitemesh:write property='body'/>
		</div>
	</div>

	<div id="footer">
		<div id="footerCenter">
			<div id="footerText">
				<p>&copy; 2019</p>
			</div>
		</div>
	</div>
</div>
</body>
</html>

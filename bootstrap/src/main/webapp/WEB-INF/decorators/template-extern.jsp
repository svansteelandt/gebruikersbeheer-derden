<%@ include file="/WEB-INF/utils/tags.jsp"%><%
%><%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="nl" lang="nl">
<head>
    <meta http-equiv="content-language" content="nl" />
    <meta http-equiv="content-type" content="text/html; charset=utf-8" />
    <meta content="" name="description" />
    <meta content="" name="keywords" />
    <title>VDAB ITIM - ,<sitemesh:write property='title'/> - VDAB</title>
    <link rel="stylesheet" href="/css/online.css" type="text/css" media="screen" />

    <c:url value="/styles/application.css" var="application_css_url" />
    <link rel="stylesheet" type="text/css" href="${application_css_url}" media="screen" />

	<script type="text/javascript" src="/gebruikersbeheer-derden/scripts/jquery-3.6.0.min.js"></script>
	<script type="text/javascript" src="/gebruikersbeheer-derden/scripts/jquery.ui-1.13.1.min.js"></script>
	<script type="text/javascript" src="/gebruikersbeheer-derden/scripts/jquery.dataTables-1.11.5.min.js"></script>

    <sitemesh:write property='head'/>
    <style type="text/css" media="screen">
        .paginate_enabled_previous { margin-right: 4px; }
        .paginate_enabled_previous:before { content: '\003C\0020'; }
        .paginate_enabled_next { margin-left: 4px; }
        .paginate_enabled_next:after { content: ' \003E'; }
        .paginate_disabled_previous {
            visibility: hidden;
            margin-right: 4px;
        }
        .paginate_disabled_next {
            visibility: hidden;
            margin-left: 4px;
        }

    </style>
</head>
<body class="werkgever"><%
    String environment = System.getProperty("ENVIRONMENT");
    boolean isProduction = "PRD".equalsIgnoreCase(environment);
    boolean hideInfoHeader = System.getProperty("infoheaderVisible") != null && "false".equalsIgnoreCase(System.getProperty("infoheaderVisible"));

    String werkgeversUrl = System.getProperty("werkgeversUrl");
    String headerUri= System.getProperty("headerUri");
    String footerUri= System.getProperty("footerUri");

	if (werkgeversUrl == null){
		System.err.println("Missing parameter 'service.werkgevers.extern.url'.");
		werkgeversUrl = "https://werkgevers.vdab.be";
	}

    if (headerUri == null){
        System.err.println("Missing parameter 'tim.vdab-extern.header.uri'.");
        headerUri= "/sites/default/files/headerfooter/wg-hd.htm";
    }
    if (footerUri == null){
        System.err.println("Missing parameter 'tim.vdab-extern.footer.uri'.");
        footerUri= "/sites/default/files/headerfooter/wg-ft.htm";
    }

    String header= werkgeversUrl + headerUri;
    String footer= werkgeversUrl + footerUri;

    if (!isProduction && !hideInfoHeader) {
%>
<div id="infoHeader">
    <span style="font-weight: bold; float:left">
        <%=environment%>
    </span>
    <span style="float:right;">
        versie: ${project.version} (gecompileerd op:  ${build.time})
    </span>
</div>
<% } %>
<div id="container">
    <c:catch>
        <c:import url="<%=header%>" />
    </c:catch>
    <div id="contentContainer" class="clearfix">
        <div id="content">
            <div class="paddingBox">
                <sitemesh:write property='body'/>
            </div>
        </div>
    </div>
    <c:catch>
        <c:import url="<%=footer%>" />
    </c:catch>
</div>
</body>
</html>
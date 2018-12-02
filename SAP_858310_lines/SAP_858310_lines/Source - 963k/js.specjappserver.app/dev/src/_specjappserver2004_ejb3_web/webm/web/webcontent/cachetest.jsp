<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<HTML>
<HEAD>
<META http-equiv="Content-Style-Type" content="text/css">
<TITLE>Cache Tests</TITLE>
</HEAD>
<BODY bgcolor="#ffffff" link="#000099" vlink="#000099">
<%@ page import="java.util.Collection, java.util.Iterator, java.util.HashMap, java.math.BigDecimal, org.spec.jappserver.servlet.helper.*, org.spec.jappserver.orders.helper.*" session="true" isThreadSafe="true" isErrorPage="false"%>
<TABLE width="645" align="left">
		<UL>
			<% BigDecimal test1 = (BigDecimal) request.getAttribute("cacheTest"); %>
			<FONT size="5" color="#000000"><b>SPECjAppServer2004 Cache Consistency Test</b></FONT>
			<BR></BR>
		</UL>
<P><BR>
</P>
<UL>
	<b>Cache test results are based on this automobile price: </b>  <%out.println(test1);%>
</UL>
</BODY>
</HTML>

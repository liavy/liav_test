<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<HTML>
<HEAD>
<META http-equiv="Content-Style-Type" content="text/css">
<TITLE>Welcome to SPECjAppServer 2004</TITLE>

</HEAD>
<BODY bgcolor="#ffffff" link="#000099" vlink="#000099">
<%@ page isThreadSafe="true" session="TRUE" isErrorPage="false"%>
<%@ include file="mfg_incl_header.jsp" %>

<TABLE width="645">
    <TBODY>
        <%
        String strAction = (String)request.getAttribute("displayAction") ; 
        String nextAction = (String)request.getAttribute("nextAction");
        String defaultWoID = (String)request.getAttribute("defaultWoID");
        %>
        <TR>
            <TD bgcolor="#cccccc" width="200"><b><%= strAction %> Work Order</b></TD>
        </TR>
        <TR>
            <TD width="624">
            <FONT color="#cc0000"><%= request.getAttribute("results") %></FONT> 
            <BR>
            <B><p><b> Please enter work order number you wish to <%= strAction %></b></p></B>
            <HR>
        </TR>
        <FORM METHOD=POST ACTION="app?action=<%= nextAction %>">
     
        <TR>
            <TD align="right" width="624"> <FONT size="-1">Order  Number </FONT> &nbsp; &nbsp;
            </TD>
        </TR>
        <TR>
            <TD align="right" width="624">
            <% 
            if (defaultWoID == null) {
            %>
                <INPUT NAME="order_id" TYPE=text SIZE="10" > &nbsp; &nbsp;
            <%
            } else {
            %>
                <INPUT NAME="order_id" TYPE=text SIZE="10" VALUE=<%= defaultWoID %> > &nbsp; &nbsp;
            <%
            }
            %>
            </TD>
        </TR>
        
        
        <TR>
            <TD align="right" width="624">
            <INPUT TYPE=submit value="<%= strAction %>"> <INPUT TYPE=reset>&nbsp; &nbsp;
            </TD>
        </TR>
        
    </TBODY>
</TABLE>
<%@ include file="mfg_incl_footer.jsp" %>
</BODY>
</HTML>

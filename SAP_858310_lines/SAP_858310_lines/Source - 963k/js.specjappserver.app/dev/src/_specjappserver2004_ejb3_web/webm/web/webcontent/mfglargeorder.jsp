<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<HTML>
<HEAD>
<META http-equiv="Content-Style-Type" content="text/css">
<TITLE>Welcome to SPECjAppServer 2004</TITLE>

</HEAD>
<BODY bgcolor="#ffffff" link="#000099" vlink="#000099">
<%@ page import="java.util.Vector, org.spec.jappserver.mfg.helper.LargeOrderInfo" isThreadSafe="true" session="TRUE" isErrorPage="false"%>
<%@ include file="mfg_incl_header.jsp" %>

<TABLE width="645">
    <TBODY>
        <TR>
            <TD bgcolor="#cccccc" width="200"><b>Large Orders Information</b></TD>
        </TR>
        <%
        LargeOrderInfo large_order;
        String assemblyId;
        int qty;
        String dueDate;
        int oLineId;
        int salesId;
    
        Vector listLOs = (Vector) request.getAttribute("listLOs");
        
        if (listLOs.size() == 0 ) {
         %>
             <TR>
                 <TD>            
                 <FONT color="#cc0000"><BR> No pending large orders at this time </font>
             </TR>
         <%
        } else {
        %>
        
        
            <TR>
		        <TD width="624"><B><p><b> There are <%= listLOs.size() %> large orders. They are listed below.<BR>
                To schedule an order please click <STRONG> Assembly ID </STRONG> of the order </b></p></B>
                <HR>
            </TR>
            
            <TR>
            <TD width="624">
                <TABLE border="1" style="font-size: smaller">
                <CAPTION align="top"><b>Large Orders </b></CAPTION>
                <TBODY>
                   <TR align="center">
                       <TD>Assembly ID</TD>
                       <TD>Quantity</TD>
                       <TD>Due Date</TD>
                   </TR>

		<% 
				for (int i =0; i < listLOs.size(); i++) {
					large_order = (LargeOrderInfo) listLOs.elementAt(i);
					assemblyId = large_order.assemblyId;
					qty = large_order.qty;
					dueDate = String.valueOf(large_order.dueDate);
					oLineId = large_order.orderLineNumber;
					salesId = large_order.salesOrderId;
		%>
					
             
                    <TR bgcolor="#fafcb6" align="center">
					<TD> 
					<A HREF="app?action=mfgschedulewo&assemblyId=<%= assemblyId %>&qty=<%= qty %>&dueDate=<%=dueDate %>&oLineId=<%= oLineId %>&salesId=<%= salesId %> ">
					<%= assemblyId %> 
					</A>
					&nbsp;</TD>
					<TD> <%= qty %> &nbsp;</TD>
					<TD> <%= dueDate %> &nbsp;</TD>
                    </TR>
		<%
				}	
		%>
				</TABLE>

        <%
        }
        %>
    
            <BR>
            </TD>
        </TR>
        
        
        
    </TBODY>
</TABLE>
<%@ include file="mfg_incl_footer.jsp" %>
</BODY>
</HTML>

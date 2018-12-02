<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<HTML>
<HEAD>
<META http-equiv="Content-Style-Type" content="text/css">
<TITLE>Vehciles to Purchase</TITLE>
</HEAD>
<BODY bgcolor="#ffffff" link="#000099" vlink="#000099">
<%@ page import="java.util.Collection, java.util.Iterator, java.math.BigDecimal, org.spec.jappserver.orders.helper.*, org.spec.jappserver.servlet.helper.*" session="true" isThreadSafe="true" isErrorPage="false"%>
<jsp:useBean id="results" scope="request" type="java.lang.String" />
<jsp:useBean id="itemInfo" type="org.spec.jappserver.servlet.helper.VehicleSearch" scope="request"/>
<jsp:useBean id="specUtils" class="org.spec.jappserver.servlet.helper.SpecUtils" scope="session"/>
<%@ include file="dealership_incl_header.jsp" %>
<TABLE width="646">
    <TBODY>
        <TR>
            <TD width="640">
            <TABLE width="642">
                <TBODY>
                    <TR>
                        <TD width="636"></TD>
                    </TR>
                    <TR>
                        <TD bgcolor="#cccccc" width="636"> <b>Vehicles Available</b></TD>
                    </TR>
                    <TR>
                        <TD align="center" width="636">
                        <TABLE border="1" style="font-size: smaller">
                            <TBODY>
                                <TR align="center">
                                    <TD>Inventory ID</TD>
                                    <TD>Name</TD>
                                    <TD>Description</TD>
                                    <TD>MSRP</TD>                           
                                    <TD>Purchase Price</TD>
                                    <TD>Discount</TD>
                                    <TD>Quantity</TD>                                    
                                    <TD>Shopping Cart Operation</TD>
                                    <TD></TD>
                                </TR>
                      

<% 
try{
	Iterator invItr = itemInfo.vehicles.iterator();
	
	while (invItr.hasNext()){
		ItemsDataBean idb = (ItemsDataBean) invItr.next();
%>
                                <TR bgcolor="#fafcb6" align="center">
                                	<FORM>
                                    <TD><%= idb.getId() %>&nbsp;</TD>
                                    <TD><%= idb.getName() %>&nbsp;</TD>
                                    <TD><%= idb.getDescription() %>&nbsp;</TD>
                                    <TD><%= specUtils.formatCurrency(idb.getPrice()) %>&nbsp;</TD>
                                    <TD><%= specUtils.formatCurrency(idb.getPrice().multiply(SpecUtils.oneBigDec.subtract(idb.getDiscount())).setScale(2, BigDecimal.ROUND_DOWN)) %>&nbsp;</TD>
                                    <TD><%= specUtils.formatNumber(idb.getDiscount().multiply(SpecUtils.onehundredBigDec)) %>%&nbsp;</TD>
                                    <TD><INPUT type="text" name="quantity" size="5" value="10">&nbsp;</TD>
                                    <TD><INPUT type="submit" name="action" value="Add to Cart">&nbsp;</TD>
                                    <%= "<INPUT type=\"hidden\" name=\"itemId\" value=\"" + idb.getId() + "\">" %>
									<%= "<INPUT type=\"hidden\" name=\"name\" value=\"" + idb.getName() + "\">" %>
									<%= "<INPUT type=\"hidden\" name=\"description\" value=\"" + idb.getDescription() + "\">" %>
									<%= "<INPUT type=\"hidden\" name=\"price\" value=\"" + idb.getPrice() + "\">" %>
									<%= "<INPUT type=\"hidden\" name=\"discount\" value=\"" + idb.getDiscount() + "\">" %>
<!-- driver-tag-start name:<%= idb.getName() %> description:<%= idb.getDescription() %> price:<%= idb.getPrice() %> discount:<%= idb.getDiscount() %> driver-tag-end  
-->
                                    </FORM>
                                </TR>
                                <% 
	}
}
catch (Exception e)
{
     System.out.println("portfolio.jsp: error displaying user holdings");
}
				%>                                                                               
                            </TBODY>
                        </TABLE>
                        <CENTER>
                        <%
                        if (itemInfo.min > 0) {
                       	%>
                       	<A HREF="app?action=View_Items&browse=bkwd&category=0">previous</A>
                       	<%
                        }
                        %>
                         | <%= itemInfo.min + 1 %> - <%= itemInfo.max %> of <%= itemInfo.total %> | 
                        <%
                        if (itemInfo.max < itemInfo.total) {
                       	%>
                       	<A HREF="app?action=View_Items&browse=fwd&category=0">next</A>
                       	<%
                        }
                        %>                         
                        </CENTER>
                        </TD>
                    </TR>
                </TBODY>
            </TABLE>
            </TD>
        </TR>            
    </TBODY>
</TABLE>
<%@ include file="dealership_incl_footer.jsp" %>
</BODY>
</HTML>

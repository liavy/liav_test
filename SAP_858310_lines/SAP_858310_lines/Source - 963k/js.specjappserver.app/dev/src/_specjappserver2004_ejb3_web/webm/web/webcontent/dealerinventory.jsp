<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<HTML>
<HEAD>
<META http-equiv="Content-Style-Type" content="text/css">
<TITLE>Dealership Inventory</TITLE>
</HEAD>
<BODY bgcolor="#ffffff" link="#000099" vlink="#000099">
<%@ page import="java.util.Collection, java.util.Iterator, java.util.HashMap, java.math.BigDecimal, org.spec.jappserver.corp.helper.*, org.spec.jappserver.orders.helper.*, org.spec.jappserver.servlet.helper.*" session="true" isThreadSafe="true" isErrorPage="false"%>
<jsp:useBean id="inventory" type="java.util.Collection" scope="request" />
<jsp:useBean id="itemInfo" type="java.util.HashMap" scope="request"/>
<jsp:useBean id="openOrders" type="java.util.Collection" scope="request"/>
<jsp:useBean id="specUtils" class="org.spec.jappserver.servlet.helper.SpecUtils" scope="session"/>
<%@ include file="dealership_incl_header.jsp" %>

<%
	String message = (String) request.getAttribute("results_cancel"); 
	if(message!=null){
%>
	<TABLE width="645">
		<TBODY>
			<TR>
			<TD align="center" width="645"><FONT color="#ff0000"><B><p><b><%= message%>&nbsp;</b></p></B></FONT></TD>
			</TR>
		</TBODY>
	</TABLE>
<%		
	}
%>
<%
	message = (String) request.getAttribute("results_sell"); 
	if(message!=null){
%>
	<TABLE width="645">
		<TBODY>
			<TR>
			<TD align="center" width="645"><FONT color="#ff0000"><B><p><b><%= message%>&nbsp;</b></p></B></FONT></TD>
			</TR>
		</TBODY>
	</TABLE>
<%		
	}
%>	
<%
	Iterator invItr = inventory.iterator();
	int numVehicles = 0;
	while (invItr.hasNext()){
		CustomerInventoryDataBean csrdb = (CustomerInventoryDataBean) invItr.next();
		numVehicles += csrdb.getQuantity();
	}
%>
<TABLE width="645">
    <TBODY>
        <TR>
            <TD valign="top" width="643">
            <TABLE width="639">
                <TBODY>

                    <TR>
                        <TD colspan="5" bgcolor="#cccccc" width="200"><b>
                        Dealership
                        Inventory</b></TD>
                        <TD bgcolor="#cccccc" align="right" width="429"><B>Number of 
                        Vehicles in Inventory: </B><%= numVehicles
%></TD>
                    </TR>
                    <TR align="center">
                        <TD colspan="6" width="633">
                        <CENTER>
                        <p></p>
                        </CENTER>
                        <TABLE border="1" style="font-size: smaller">
                            <CAPTION align="top"><b>Current Inventory </b></CAPTION>
                            <TBODY>
                                <TR align="center">
                                    <TD>Inventory ID</TD>
                                    <TD>Name</TD>
                                    <TD>Description</TD>
                                    <TD>Quantity</TD>
                                    <TD>MSRP</TD>
                                    <TD>Invoice Price</TD>
                                    <TD>Discount</TD>                                    
                                    <TD>Total Value</TD>
                                    <TD></TD>
                                </TR>
<% 
BigDecimal totalValue = SpecUtils.zeroBigDec;

try{
	invItr = inventory.iterator();
	
	while (invItr.hasNext()){
		CustomerInventoryDataBean csrdb = (CustomerInventoryDataBean) invItr.next();
		ItemsDataBean itemdb = (ItemsDataBean) itemInfo.get(csrdb.getVehicle());
		BigDecimal currentTotal = itemdb.getPrice().multiply(new BigDecimal(csrdb.getQuantity()));
		BigDecimal quantity = new BigDecimal(csrdb.getQuantity());
		BigDecimal discountedPrice = csrdb.getTotalCost().divide(quantity, 2, BigDecimal.ROUND_DOWN);
		totalValue = totalValue.add(currentTotal);
%>
                                <TR bgcolor="#fafcb6" align="center">
                                    <TD><%= csrdb.getId() %>&nbsp;</TD>
                                    <TD><%= itemdb.getName() %>&nbsp;</TD>
                                    <TD><%= itemdb.getDescription() %>&nbsp;</TD>
                                    <TD><%= csrdb.getQuantity() %>&nbsp;</TD>
                                    <TD><%= specUtils.formatCurrency(itemdb.getPrice()) %>&nbsp;</TD>
                                    <TD><%= specUtils.formatCurrency(discountedPrice) %>&nbsp;</TD>                                   
                                    <TD><%= specUtils.formatNumber(SpecUtils.oneBigDec.subtract(discountedPrice.divide(itemdb.getPrice(), 4, BigDecimal.ROUND_UP)).multiply(SpecUtils.onehundredBigDec).setScale(2, BigDecimal.ROUND_UP)/*itemdb.getDiscount().multiply(SpecUtils.onehundredBigDec)*/) %>%&nbsp;</TD>
                                    <TD><%= specUtils.formatCurrency(itemdb.getPrice().multiply(quantity)) %>&nbsp;</TD>
                                    <TD><A HREF=<%= "app?action=sellinventory&vehicleToSell="+csrdb.getId()+"&total="+csrdb.getTotalCost()%>>Sell</A>&nbsp;<!--quantity <%= csrdb.getQuantity() %> --></TD>
                                </TR>
                                <% 
	}
}
catch (Exception e)
{
     System.out.println("portfolio.jsp: error displaying user holdings");
}
				%>
                                <TR align="center">
                                    <TD></TD>
                                    <TD></TD>
                                    <TD></TD>
                                    <TD></TD>
                                    <TD></TD>
                                    <TD></TD>
                                    <TD align="center"><B>Total</B></TD>
                                    <TD align="center"><%= specUtils.formatCurrency(totalValue) %></TD>
                                    <TD align="center"></TD>
                                </TR>
                            </TBODY>
                        </TABLE>
                        <CENTER>
                        <p></p>
                        </CENTER>
                        </TD>
                    </TR>
 <%
 Iterator orderItr = openOrders.iterator();
 if(orderItr.hasNext()){
 %>                   
                    <TR align="center">
                        <TD colspan="6" width="633">
                        <CENTER>
                        </CENTER>
						<font size="2"><b>Open Orders</b></font>
                        <CENTER>
                        </CENTER>
                        </TD>
                    </TR>

<%
while (orderItr.hasNext()){
	OrderDataBean odb = (OrderDataBean) orderItr.next();
%>                    
                    <TR align="center">
                        <TD colspan="6" width="633">
                        <CENTER>
                        <p></p>
                        </CENTER>
                        <TABLE border="1" style="font-size: smaller" width="633">                            
                            <TBODY>
                                <TR align="center" bgcolor="#fafcb6">
                                    <TD colspan=2>Order ID: <%= odb.getOrderID() %></TD>
                                    <TD colspan=2>Entry Date: <%= specUtils.formatDate(odb.getEntryDate()) %></TD>
                                    <TD colspan=2><A HREF=<%= "\"app?action=cancelorder&orderID="+odb.getOrderID()+"\"" %>>cancel order</A>&nbsp;</TD>
                                </TR>
                                <TR bgcolor="#fafcb6">
                                	<TD>&nbsp;</TD>
                                	<TD colspan=5>Order Line Items</TD>
                                </TR>
                                <TR align="center">
                                	<TD bgcolor="#fafcb6">ID</TD>
                                	<TD>Name</TD>
                                	<TD>Description</TD>
                                	<TD>Quantity</TD>
                                	<TD>Unit Price</TD>
                                	<TD>Total</TD>	
                                </TR>
<% 
try{	
	Iterator lineItr = odb.getOrderLines().iterator();
	
	while (lineItr.hasNext()){
		OrderLineDataBean oldb = (OrderLineDataBean) lineItr.next();
		ItemsDataBean itemdb = (ItemsDataBean) itemInfo.get(oldb.getItemID());
		BigDecimal itemPrice = oldb.getTotalValue().divide(new BigDecimal(oldb.getQuantity()), 2, BigDecimal.ROUND_DOWN);
		//BigDecimal itemPrice = itemdb.getPrice().multiply((SpecUtils.oneBigDec.subtract(itemdb.getDiscount()))).setScale(2, BigDecimal.ROUND_DOWN);
		//BigDecimal currentTotal = itemPrice.multiply(new BigDecimal(oldb.getQuantity()));
%>
                                <TR align="center">
                                    <TD bgcolor="#fafcb6"><%= oldb.getID() %>&nbsp;</TD>
                                    <TD><%= itemdb.getName() %>&nbsp;</TD>
                                    <TD><%= itemdb.getDescription() %>&nbsp;</TD>
                                    <TD><%= oldb.getQuantity() %>&nbsp;</TD>
                                    <TD><%= specUtils.formatCurrency(itemPrice) %>&nbsp;</TD>
                                    <TD><%= specUtils.formatCurrency(oldb.getTotalValue()) %>&nbsp;</TD>
                                </TR>
                                <% 
	}
}
catch (Exception e)
{
     System.out.println("portfolio.jsp: error displaying user holdings");
}
				%>
                                <TR bgcolor="#fafcb6" align="center">
                                    <TD>&nbsp;</TD>
                                    <TD>Ord Status: <%= odb.getOrderStatus() %></TD>
                                    <TD>&nbsp;</TD>
                                    <TD>Discount: <%= specUtils.formatNumber(odb.getDiscount()) %>%</TD>
                                    <TD>&nbsp;</TD>
                                    <TD><%= specUtils.formatCurrency(odb.getTotal()) %>&nbsp;</TD>
                                </TR>
                            </TBODY>
                        </TABLE>
                        <CENTER>
                        <p></p>
                        </CENTER>
                        </TD>
                    </TR>                    
<%
}
}
%>                    
                    <TR>
                        <TD colspan="6" width="633"></TD>
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

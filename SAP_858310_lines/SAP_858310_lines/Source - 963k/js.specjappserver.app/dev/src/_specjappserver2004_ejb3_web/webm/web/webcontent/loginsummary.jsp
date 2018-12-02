<%@ page import="java.util.Collection, java.util.Iterator, java.math.BigDecimal, org.spec.jappserver.servlet.helper.*" session="true" isThreadSafe="true" isErrorPage="false"%>
<jsp:useBean id="specUtils" class="org.spec.jappserver.servlet.helper.SpecUtils" scope="session"/>

            <TABLE border="1" bgcolor="#ffffff" width="100%" style="font-size: smaller">
                <TBODY>
                <%
                //HttpSession session = request.getSession();
                %>
                
                    <TR>
                        <TD colspan="2" bgcolor="#000000" align="center" height="15"><FONT color="#ffffff"><B>
                        User Navigation Summary<BR></B></FONT></TD>
                    </TR>
                    <TR>
                        <TD align="right" bgcolor="#fafcb6" width="100"> <A href="sessioncreated">User logged in date</A></TD>
                        <TD align="center" valign="middle" bgcolor="#ffffff" width="141"><%= session.getAttribute("sessionCreationDate") %>&nbsp;</TD>
                    </TR>
                    <TR>
                        <TD align="right" bgcolor="#fafcb6"><A href="totalpurchases">Total Value of Purchases</A></TD>
                        <TD align="center" valign="middle"><%= specUtils.formatCurrency(((BigDecimal)session.getAttribute("totalPurchaseDebits"))) %>&nbsp;</TD>
                    </TR>
                    <TR>
                        <TD align="right" bgcolor="#fafcb6" width="100"> <A href="totalsales">Total Value of Sales</A></TD>
                        <TD align="center" valign="middle" bgcolor="#ffffff" width="141"><%= specUtils.formatCurrency((BigDecimal)session.getAttribute("totalSalesProfits")) %>&nbsp;</TD>
                    </TR>
                    <TR>
                        <TD align="right" bgcolor="#fafcb6"><A href="ordersplaced">Orders Placed</A></TD>
                        <TD align="center" valign="middle"><%= session.getAttribute("ordersPlaced") %>&nbsp;</TD>
                    </TR>
                    <TR>
                        <TD align="right" bgcolor="#fafcb6" width="100"> <A href="orderscancelled">Orders Cancelled</A></TD>
                        <TD align="center" valign="middle" bgcolor="#ffffff" width="141"><%= session.getAttribute("ordersCancelled") %>&nbsp;</TD>
                    </TR>
                    <TR>
                        <TD align="right" bgcolor="#fafcb6"><A href="inventorysold">Inventory Sold</A></TD>
                        <TD align="center" valign="middle"><%= session.getAttribute("holdingsSold") %>&nbsp;</TD>
                    </TR>
                    <TR>
                        <TD align="right" bgcolor="#fafcb6"><A href="totalpages">Total Pages Visited</A></TD>
                        <%
                        	int totalPagesVisited = ((Integer)session.getAttribute("inventoryPageVisits")).intValue()+((Integer)session.getAttribute("shoppingCartPageVisits")).intValue()+((Integer)session.getAttribute("browsePageVisits")).intValue()+((Integer)session.getAttribute("homepageVisits")).intValue(); 
                        %>
                        <TD align="center" valign="middle"><%= totalPagesVisited %>&nbsp;</TD>
                    </TR>
                </TBODY>
            </TABLE>
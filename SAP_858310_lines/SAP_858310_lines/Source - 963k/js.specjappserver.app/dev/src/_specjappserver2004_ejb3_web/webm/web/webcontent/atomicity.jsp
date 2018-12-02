<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<HTML>
<HEAD>
<META http-equiv="Content-Style-Type" content="text/css">
<TITLE>Atomicity Tests</TITLE>
</HEAD>
<BODY bgcolor="#ffffff" link="#000099" vlink="#000099">
<%@ page import="java.util.Collection, java.util.Iterator, java.util.HashMap, java.math.BigDecimal, org.spec.jappserver.servlet.helper.*, org.spec.jappserver.orders.helper.*" session="true" isThreadSafe="true" isErrorPage="false"%>
<TABLE width="645" align="left">
		<UL>
			<% Boolean test1 = (Boolean) request.getAttribute("test1"); %>
			<% Boolean test2 = (Boolean) request.getAttribute("test2"); %>
			<% Boolean test3 = (Boolean) request.getAttribute("test3"); %>
			<FONT size="5" color="#000000"><b>SPECjAppServer2004 Transaction Atomicity Test</b></FONT>
			<BR></BR>
		</UL>
<P><BR>
</P>
<UL>
	<b>Definition:</b>  The System Under Test must guarantee that all Transactions are atomic; the system will either perform all individual operations on the data, or will assure that no partially-completed operations leave any effects on the data. The tests that were executed with their results below were used to determine if the System Under Test you are operating on meets all the transactional atomicity requirements. If any of the tests have a result of "FAILED" your system is not set up to ensure transaction atomicity
</UL>
<UL>
	<b>Test 1: </b>This test checks to see if the proper transaction atomicity levels are being upheld in transactions associated with the benchmark. This test case drives placing an order for immediate insertion into the dealerships inventory. An exception is raised after placing the order and while adding the inventory to the dealers inventory table. This should cause the transaction changes to be removed from the database and all other items returned to how they existed before the transaction took place. This test case has three steps which are as follows to verify atomicity 
	<p>
	1.) Query database to check how many inventory items the dealership has, the dealerships account balance, and the number of orders which have been placed for the dealer inside of the dealer domain. These numbers are the initial metrics that the final test cases should line up with after rolling back the transaction.
	</p>
	2.) Drives the above transaction which causes a transaction rollback exception to occur.
	<p> 
	3.) Query database to check how many inventory items the dealership has, the dealerships account balance, and the number of orders which have been placed for the dealer inside of the dealer domain. These numbers should equal those in step 1) for the test case to be successful and the transaction to have been atomic. 
	</p>
	
	<%		if(test1.booleanValue()==true){
				out.println("<LI>Atomicity Test One: <b><FONT color=\"#ff0000\">" +  "PASSED" + "</b></FONT></LI><BR></BR>");
			}else{
				out.println("<LI>Atomicity Test One: <b><FONT color=\"#ff0000\">" +  "FAILED" + "</b></FONT></LI><BR></BR>");
			}
	%>
	<p></p>		
	<b>Test 2: </b>This test transaction simply tests that the application server is working properly and that it is able to insert an order as in Atomicity test 1 but without causing the exception and have it show up in the database. 
	<%
			if(test2.booleanValue()==true){
				out.println("<LI>Atomicity Test Two: <b><FONT color=\"#ff0000\">" +  "PASSED" + "</b></FONT></LI><BR></BR>");
			}else{
				out.println("<LI>Atomicity Test Two: <b><FONT color=\"#ff0000\">" +  "FAILED" + "</b></FONT></LI><BR></BR>");
			}
	%>
	<p></p>		
	<b>Test 3: </b>This test checks to see if the proper transaction atomicity levels are being upheld in transaction associated with the benchmark and specifically the messaging subsystem in this test case. This test case drives placing a order which contains a large order and an item to be insert immediately into the dealerships inventory. An exception is raised after placing the order and while adding the inventory to the dealers inventory table. This should cause the transaction changes to be removed from the database, messages removed from queue and all other items returned to how they existed before the transaction took place. This test case has three steps which are as follows to verify atomicity. 
	<p>  
	1.) Query database to check how many inventory items the dealership has, the dealerships account balance, and the number of orders which have been placed for the dealer inside of the dealer domain. Also the large order table is queried to check how many large orders exist in the database before we drive the transaction. These numbers are the initial metrics that the final test cases should line up with after rolling back the transaction. 
	</p> 
	2.) Drives the above listed transaction which causes a transaction rollback exception to occur.
	<p>
	3.) Query database to check how many inventory items the dealership has, the dealerships account balance, and the number of orders which have been placed for the dealer inside of the dealer domain. Also query the large order table to check how many large orders there are in the table. These numbers should equal those in step 1) for the test case to be successful and the transaction to have been atomic. 
	</p>
	<%
			if(test3.booleanValue()==true){
				out.println("<LI>Atomicity Test Three: <b><FONT color=\"#ff0000\">" +  "PASSED" + "</b></FONT></LI><BR></BR>");
			}else{
				out.println("<LI>Atomicity Test Three: <b><FONT color=\"#ff0000\">" +  "FAILED" + "</b></FONT></LI><BR></BR>");
			}
	%>
</UL>
</TABLE>
</BODY>
</HTML>
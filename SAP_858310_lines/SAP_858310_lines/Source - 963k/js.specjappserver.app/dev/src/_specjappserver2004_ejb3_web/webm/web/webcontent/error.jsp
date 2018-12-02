<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<%@ page import="java.io.*, java.lang.reflect.*" %>
<TABLE>
  <TBODY>
    <TR>
            <TD bgcolor="#ff0000" align="left" width="545" colspan="5" height="10"><FONT color="#ffffff"><B>SPECjAppServer2004 Error</B></FONT></TD>
            <TD align="center" bgcolor="#000000" width="100" height="10"><FONT color="#ffffff"><B>SPECj2004</B></FONT></TD>
        </TR>
</TABLE>
<DIV align="left"></DIV>
<TABLE width="645" height="144">
	<TBODY>
		<TR>
			<TD width="3"></TD>
			<TD width="645">
			<HR>
			</TD>
			<TD width="8"></TD>
		</TR>
		<%
		String error = (String) request.getAttribute("error");
		%>
		<TR>
			<TD bgcolor="#e7e4e7" rowspan="4" width="3"></TD>
			<TD width="712"><B><FONT color="#000000">A <%= error %> has occured during
			SPECjAppServer2004 processing</FONT><FONT size="-2">.</FONT></B><BR>
			Please consult the
			application server error logs for further details.</TD>
			<TD bgcolor="#e7e4e7" rowspan="4" width="8"></TD>
		</TR>
		<TR>
			<TD width="645"><FONT size="-1"><% 

	  String message = null;
	  int status_code = -1;
	  String exception_info = null;
	  String url = null;
	
	  Object myReport = null;
	  Exception theException = null;
	  Integer status = null;
	  message = (String) request.getAttribute("message");
	  status_code = Integer.parseInt((String)request.getAttribute("status_code"));
	  theException = (Exception) request.getAttribute("exception");
	  if (message == null)
	  {
	     message = error;
	  }
	  if (theException == null)
	  {
	     exception_info = "not available";
	  } else
	  {
	     exception_info = theException.toString();
	  }
      try
      {
			  url = request.getRequestURL().toString(); 
      } catch (Exception e)
      {
         url = "information not available";
      }

      //output is all done here. 

      out.println("<br><br><b>Processing request:</b>" +  url);      
      out.println("<br><b>StatusCode:</b> " +  status_code);
      out.println("<br><b>Message:</b>" + message);
      out.println("<br><b>Exception:</b>" + exception_info);

%></FONT><FONT size="-1"> </FONT></TD>
		</TR>
		<TR>
			<TD align="left" width="712"></TD>
		</TR>
		<TR>
			<TD width="645">
			<HR>
			</TD>
		</TR> 
	</TBODY>
</TABLE>
<TABLE>
  <TBODY>
    <TR>
            <TD bgcolor="#ff0000" align="left" width="545" colspan="5" height="10"><FONT color="#ffffff"><B>SPECjAppServer2004 Error</B></FONT></TD>
            <TD align="center" bgcolor="#000000" width="100" height="10"><FONT color="#ffffff"><B>SPECj2004</B></FONT></TD>
        </TR>
    </TBODY>
</TABLE>

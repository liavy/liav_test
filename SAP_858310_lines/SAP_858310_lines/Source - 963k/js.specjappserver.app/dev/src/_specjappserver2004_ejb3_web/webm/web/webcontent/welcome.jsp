
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<HTML>
<HEAD>
<TITLE>Dealership Login</TITLE>
</HEAD>
<BODY bgcolor="#ffffff" link="#000099">
<%@ page session="false" %>
<TABLE style="font-size: smaller" width="641">
	<TBODY>
		<tr>
			<TD bgcolor="#8080c0" align="left" width="500" height="10"><font
				color="#FFFFFF"><b>SPECjAppServer2004</b></font><FONT
				color="#ffffff"><B> Login</B></FONT></TD>
			<TD align="center" bgcolor="#000000" width="127" height="10"><font
				color="#FFFFFF"><b>SpecJ200</b></font><FONT color="#ffffff"><B>4</B></FONT></TD>
		</tr>
	</TBODY>
</TABLE>
<DIV align="left"></DIV>
<TABLE width="642">
	<TBODY>
		<TR>
			<%String message = (String) request.getAttribute("results");%>
			<TD width="8" bgcolor="#e7e4e7" rowspan="3"></TD>
			<TD width="624"><B>
			<p><b>Welcome to SPEC&#39;s Automobile Dealership Vehicle Ordering
			System.</b></p>
			<%=message%> Please Log in using your Dealership ID</B>
			<HR>
			</TD>
		</TR>
		<TR>
			<TD align="right" width="624"><FONT size="-1">Dealership ID &nbsp;
			&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;
			&nbsp; &nbsp; &nbsp;</FONT></TD>
		</TR>
		<TR>
			<TD align="right" width="624">
			<FORM action="app" method="POST">&nbsp; &nbsp;<INPUT size="10"
				type="text" name="uid" value="1"> &nbsp; &nbsp; &nbsp; <INPUT
				type="submit" value="Log in"><INPUT type="hidden" name="action"
				value="login"></FORM>
			</TD>
		</TR>
	</TBODY>
</TABLE>

<TABLE height="52" style="font-size: smaller" width="641">
	<TBODY>
		<TR>
			<TD colspan="2" width="632" height="16">
			<HR>
			</TD>
		</TR>
		<tr>
			<TD bgcolor="#8080c0" align="left" width="500" height="10"><B><FONT
				color="#ffffff"> SPECjAppServer2004 Login</FONT></B></TD>
			<TD align="center" bgcolor="#000000" width="124" height="10"><font
				color="#FFFFFF"><b>SpecJ2004</b></font><FONT color="#ffffff"><B></B></FONT></TD>
		</tr>
		<TR>
			<TD colspan="2" align="center" width="632" height="15">Copyright
			2004, SPEC Corporation</TD>
		</TR>
	</TBODY>
</TABLE>
</BODY>
</HTML>

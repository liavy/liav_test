<%@ page contentType="text/html" import="com.sap.archtech.daservice.commands.*" %>

<html>

<head>
  <title>Data Archiving Service</title>
</head>

<body>

<table border="1" width="100%" cellpadding="5" cellspacing="5">

  <tr>
    <td width="20%"><img border="0" src="/DataArchivingService/sap_corporate.gif" width="61" height="33">
    </td>
    <td width="80%" bgcolor="#003060">
      <h1><b><font face="Arial" color="#FFFFFF" size="5">XML Data Archiving Service</font></b>
      <font face="Arial" color="#003060" size="4"><%=MasterMethod.DASVERSION%></font>
      </h1>
    </td>
  </tr>

  <tr>
    <td width="20%" bgcolor="#EAEAEA" valign="top">
      <p style="margin-top: 12"><font face="Arial" color="#FF9900" weight="bold" size="4"><b>Documentation</b></font></p>
      <p><u><font face="Arial" size="2"><a href="/DataArchivingService/characteristics.html">Characteristics</a></font></u>
      <p><u><font face="Arial" size="2"><a href="/DataArchivingService/installation.html">Configuration</a></font></u></p>
      <p><u><font face="Arial" size="2"><a href="/DataArchivingService/specification.html">Core Functions</a></font></u>
    </td>
    <td width="80%" bgcolor="#FFFFFF" valign="top">
      <h3><font face="Arial" color="#000080">XML DAS</font></h3>
      <p><font face="Arial" color="#000080" size="2">The XML DAS (XML Data
      Archiving Service) is part of AS_JAVA. This service is used by
      application systems to carry out XML-based data archiving.
      <ul>
        <li>
          <font face="Arial" color="#000080">XML-based archiving is similar to
          the ADK-based archiving that has been available so far as part of SAP
          NetWeaver: It is used to remove business-complete data, meaning data
          that is no longer needed in everyday business processes, from the
          database, but still allows access to this data at a later point in
          time.</font>
        </li>
        <li>
          <font face="Arial" color="#000080">XML-based data archiving is
          different from ADK-based archiving in that data objects, such as
          documents or business objects, are sent to the XML DAS as complete
          objects in the form of an XML document. From a technical viewpoint, the documents
          sent to the XML DAS are independent from the application system and
          can therefore be read across different systems.</font>
        </li>
        <li>
          <font face="Arial" color="#000080">SAP only allows the XML
          DAS to be used via an Archive Connector. This applies to both the
          archiving of documents and the reading of archived data. XML DAS Connectors
          are available for ABAP and Java.</font>
        </li>
      </ul>
      <p>With SAP NetWeaver, XML-based archiving is available to certain SAP applications, which have
      developed documentation specifically on this topic.</p>
      <p>To be able to use the XML DAS to store documents you need to have a file system or a WebDAV system connected to
      the SAP J2EE Engine.</font></p>
    </td>
  </tr>

  <tr>
    <td width="20%" bgcolor="#EAEAEA" valign="top">
      <p style="margin-top: 12"><font face="Arial" color="#FF9900" weight="bold" size="4"><b>Administration</b></font></p>
      <p><font face="Arial" size="2"><u><a href="/DataArchivingService/cas/cas_list.jsp">Define Archive Stores</a></u></font></p>
      <p><font face="Arial" size="2"><u><a href="/DataArchivingService/tas/tas_list.jsp">Test Archive Stores</a></u></font></p>
      <p><font face="Arial" size="2"><u><a href="/DataArchivingService/shp/shp_enter.jsp">Synchronize Home Path</a></u></font></p>
      <p><font face="Arial" size="2"><u><a href="/DataArchivingService/aas/aas_entry.jsp">Assign Archive Stores</a></u></font></p>
      <p><font face="Arial" size="2"><u><a href="/DataArchivingService/aas/aas_list_all.jsp">List Archive Paths</a></u></font></p>
      <br>
      <p><font face="Arial" size="2"><u><a href="/DataArchivingService/pre/pre_enter.jsp">Pack Resources</a></u></font></p>
      <p><font face="Arial" size="2"><u><a href="/DataArchivingService/ure/ure_enter.jsp">Unpack Resources</a></u></font></p>
      <p><font face="Arial" size="2"><u><a href="/DataArchivingService/pst/pst_enter.jsp">Display (Un)Pack Progress</a></u></font></p>
    </td>
    <td width="80%" bgcolor="#FFFFFF" valign="top">
      <h3><font face="Arial" color="#000080">XML DAS Administration</font></h3>
      <p><font face="Arial" color="#FF0000" size="2">
      This user interface is no longer supported. Please use the SAP NetWeaver Administrator integrated XML DAS Administration instead. 
      (For example, use the alias /nwa/xmldas of your AS_JAVA).
      </font>
      <font face="Arial" color="#000080" size="2">
      <ul>
        <li>
          Work with archive stores for the actual storage of resources
        </li>
        <ul>
          <li>
            Define and map archive stores to storage systems
          </li>
          <li>
            Test the availability of archive stores
          </li>
        </ul>
        <p>        
        <li>
          Work with home paths of archiving objects or archiving sets
        </li>
        <ul>
          <li>
            Create new home paths for test purposes
          </li>
          <li>
            Assign home collections or application collections to archive stores
          </li>
          <li>
            List all archive paths
          </li>
        </ul>
        <p>
        <li>
          Compress resources into fewer but more compact files or WebDAV resources
        </li>
        <ul>
          <li>
            Start pack operation
          </li>
          <li>
            Start unpack operation
          </li>
          <li>
            Monitor progress and status of pack or unpack operations
          </li>
        </ul>
      </ul>
</font></p>
    </td>
  </tr>

</table>

</body>

</html>

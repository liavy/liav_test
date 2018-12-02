package com.sap.archtech.archconn.util;

import java.io.*;
import com.sap.tc.logging.*;

/**
 * Helper class used by XML Generator.
 * 
 * @author D025792
 * @version 1.0
 * 
 */
public class GenerateXSDThread extends Thread
{

  private static final Location loc = Location.getLocation("com.sap.archtech.archconn");
  private static final Category cat = Category.getCategory(Category.APPS_COMMON_ARCHIVING, "Connector");

  private PipedOutputStream ww;

  GenerateXSDThread(PipedOutputStream pw)
  {
    this.ww = pw;
  }

  public void run()
  {
    try
    {
      BufferedWriter sw = new BufferedWriter(new OutputStreamWriter(this.ww, "UTF-8"));
      sw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
      sw.write("<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'>\n");
      sw.write(" <xs:element name=\"personnel\">\n");
      sw.write("  <xs:complexType>\n");
      sw.write("   <xs:sequence>\n");
      sw.write("     <xs:element ref=\"person\" minOccurs='1' maxOccurs='unbounded'/>\n");
      sw.write("   </xs:sequence>\n");
      sw.write("  </xs:complexType>\n");
      sw.write("	<xs:unique name=\"unique1\">\n");
      sw.write("	 <xs:selector xpath=\"person\"/>\n");
      sw.write("	 <xs:field xpath=\"name/given\"/>\n");
      sw.write("	 <xs:field xpath=\"name/family\"/>\n");
      sw.write("	</xs:unique>\n");
      sw.write("	<xs:key name='empid'>\n");
      sw.write("	 <xs:selector xpath=\"person\"/>\n");
      sw.write("	 <xs:field xpath=\"@id\"/>\n");
      sw.write("	</xs:key>\n");
      sw.write("		<xs:keyref name=\"keyref1\" refer='empid'>\n");
      sw.write("		 <xs:selector xpath=\"person\"/>\n");
      sw.write("		 <xs:field xpath=\"link/@manager\"/>\n");
      sw.write("		</xs:keyref>\n");
      sw.write("	  </xs:element>\n");
      sw.write("	  <xs:element name=\"person\">\n");
      sw.write("		<xs:complexType>\n");
      sw.write("		 <xs:sequence>\n");
      sw.write("			<xs:element ref=\"name\"/>\n");
      sw.write("			<xs:element ref=\"email\" minOccurs='0' maxOccurs='unbounded'/>\n");
      sw.write("			<xs:element ref=\"url\"   minOccurs='0' maxOccurs='unbounded'/>\n");
      sw.write("			<xs:element ref=\"link\"  minOccurs='0' maxOccurs='1'/>\n");
      sw.write("		 </xs:sequence>\n");
      sw.write("		 <xs:attribute name=\"id\"  type=\"xs:ID\" use='required'/>\n");
      sw.write("		 <xs:attribute name=\"note\" type=\"xs:string\"/>\n");
      sw.write("		 <xs:attribute name=\"contr\" default=\"false\">\n");
      sw.write("		  <xs:simpleType>\n");
      sw.write("			<xs:restriction base = \"xs:string\">\n");
      sw.write("			  <xs:enumeration value=\"true\"/>\n");
      sw.write("			  <xs:enumeration value=\"false\"/>\n");
      sw.write("			</xs:restriction>\n");
      sw.write("		  </xs:simpleType>\n");
      sw.write("		 </xs:attribute>\n");
      sw.write("		 <xs:attribute name=\"salary\" type=\"xs:integer\"/>\n");
      sw.write("		</xs:complexType>\n");
      sw.write("	  </xs:element>\n");
      sw.write("  <xs:element name=\"name\">\n");
      sw.write("	<xs:complexType>\n");
      sw.write("	 <xs:all>\n");
      sw.write("	  <xs:element ref=\"family\"/>\n");
      sw.write("	  <xs:element ref=\"given\"/>\n");
      sw.write("	 </xs:all>\n");
      sw.write("	</xs:complexType>\n");
      sw.write(" </xs:element>\n");
      sw.write(" <xs:element name=\"family\" type='xs:string'/>\n");
      sw.write(" <xs:element name=\"given\" type='xs:string'/>\n");
      sw.write(" <xs:element name=\"email\" type='xs:string'/>\n");
      sw.write(" <xs:element name=\"url\">\n");
      sw.write("	<xs:complexType>\n");
      sw.write("	 <xs:attribute name=\"href\" type=\"xs:string\" default=\"http://\"/>\n");
      sw.write("	</xs:complexType>\n");
      sw.write("  </xs:element>\n");
      sw.write("  <xs:element name=\"link\">\n");
      sw.write("	<xs:complexType>\n");
      sw.write("	 <xs:attribute name=\"manager\" type=\"xs:IDREF\"/>\n");
      sw.write(" <xs:attribute name=\"subordinates\" type=\"xs:IDREFS\"/>\n");
      sw.write("	</xs:complexType>\n");
      sw.write("</xs:element>\n");
      sw.write("<xs:notation name='gif' public='-//APP/Photoshop/4.0' system='photoshop.exe'/>\n");
      sw.write("</xs:schema>\n");

      sw.flush();
      sw.close();
    }
    catch (IOException ioex)
    {
      cat.logThrowableT(Severity.ERROR, loc, "GenerateXSDThread.run()", ioex);
    }
  }
}

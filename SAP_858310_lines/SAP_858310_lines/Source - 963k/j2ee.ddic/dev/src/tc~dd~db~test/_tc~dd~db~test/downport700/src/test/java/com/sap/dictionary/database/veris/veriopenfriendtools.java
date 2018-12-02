package com.sap.dictionary.database.veris;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.sql.Connection;
import java.util.ArrayList;

import org.xml.sax.InputSource;

import com.sap.dictionary.database.dbs.Database;
import com.sap.dictionary.database.dbs.DbFactory;
import com.sap.dictionary.database.dbs.DbTable;
import com.sap.dictionary.database.dbs.JddException;
import com.sap.dictionary.database.dbs.XmlExtractor;
import com.sap.dictionary.database.dbs.XmlMap;
import com.sap.dictionary.database.friendtools.FriendTools;
import com.sap.dictionary.database.friendtools.FriendToolsFactory;
import com.sap.dictionary.database.opentools.DbTableOpenTools;

import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import junit.framework.Assert;

public class VeriOpenFriendTools extends VeriStarter {
  private Connection con;	
  private InputStream stream;
	
  public VeriOpenFriendTools() {
	super(); 
  }
		   
  public static void exec() throws Exception {
    new TestSuite(VeriOpenFriendTools.class).run(new TestResult());
  }
		  
  public void setUp() throws Exception{
	con = getConnection();
  }

  public void tearDown() throws Exception {
	con.close();  
  }
  
  public void testDirectOperations() {
	File file;
	   
	try { 			  
		DbTableOpenTools oTools = new DbTableOpenTools(con);
		oTools.switchOnTrace();
		oTools.switchOnTrace(1);
		oTools.setTraceSeverity(1);
		oTools.getTraceSeverity();
		stream = VeriOpenFriendTools.class.getResourceAsStream("verifiles/file6.xml");  
		oTools.createTable("TMP_AA",stream,false,false);
		oTools.switchOffTrace();
		oTools.getInfoText();
		stream = VeriOpenFriendTools.class.getResourceAsStream("verifiles/file7.xml");  
		oTools.createTable("TMP_SD",stream);
		stream = VeriOpenFriendTools.class.getResourceAsStream("verifiles/file8.xml");  
		oTools.createView("TMP_TM111",stream);
		oTools.dropTable("TMP_AA");
		oTools.dropTable("TMP_SD");
		oTools.dropView("TMP_TM111");
		
		file = new File("C:\\eclipseWorkspaceJDT1\\DatabaseVeri\\src\\com\\" +
				           "sap\\dictionary\\database\\veris\\verifiles\\file6.xml");
		oTools.createTable("TMP_AA",file);
		oTools.dropTable("TMP_AA");	
	}
	catch (Exception ex)  {
	  ex.printStackTrace();	
	}
  }
  
  public void testFriendOperations() {
	try { 	
		FriendTools fTools = FriendToolsFactory.getInstance(con);
		stream = VeriOpenFriendTools.class.getResourceAsStream("verifiles/file6.xml"); 
		fTools.createTable("TMP_AA",stream);
		fTools.modifyTable("TMP_AA",stream);
		fTools.xmlWrite("TMP_AA",stream);
		String s = fTools.xmlRead("TMP_AA");
		String s1 = getString(stream);
		if (!s.equalsIgnoreCase(s1))
			Assert.fail("xml read/write failure");
		fTools.xmlDelete("TMP_AA");
		fTools.dropTable("TMP_AA");
	}
	catch (Exception ex) {
	  ex.printStackTrace();	
	}				 
  }
  
  public void testCheckTable() throws Exception {
	stream = VeriOpenFriendTools.class.getResourceAsStream("verifiles/file6.xml");    
	XmlMap xmlMap = extractXmlMap(getString(stream));
	DbFactory factory = new DbFactory(con);
	DbTable tabViaXml = factory.makeTable("TMP_AA");
	tabViaXml.setCommonContentViaXml(xmlMap);
	tabViaXml.check();
  }
  
  private static String getString(InputStream stream)
	 throws Exception{
	   String s;
	   InputStreamReader reader = new InputStreamReader(stream);
	   BufferedReader bufferedReader = new BufferedReader(reader);
	   StringBuffer buffer = new StringBuffer();
	   while ((s = bufferedReader.readLine()) != null) {
		  buffer.append(s);
	   }	
	   return buffer.toString();	
  }
  
  private static XmlMap extractXmlMap(Object xmlData) throws Exception {
		if (xmlData == null)
			return null;
		else if (xmlData instanceof XmlMap)
			return (XmlMap) xmlData;
		else if (xmlData instanceof String)
			return new XmlExtractor().map(new InputSource(new StringReader(
				(String)xmlData)));
		else
			return null;

	}
}

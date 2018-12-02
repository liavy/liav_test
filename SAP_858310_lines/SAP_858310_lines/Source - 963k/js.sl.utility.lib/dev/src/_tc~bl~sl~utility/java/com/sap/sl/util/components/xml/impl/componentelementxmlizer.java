package com.sap.sl.util.components.xml.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.sap.sl.util.components.api.ComponentElementIF;
import com.sap.sl.util.components.api.ComponentElementXMLException;
import com.sap.sl.util.components.api.ComponentFactoryIF;
import com.sap.sl.util.components.api.SCVersionIF;
import com.sap.sl.util.components.xml.api.ComponentElementXMLizerException;
import com.sap.sl.util.components.xml.api.ComponentElementXMLizerIF;

/**
 * @author d030435
 */

public class ComponentElementXMLizer extends DefaultHandler implements ComponentElementXMLizerIF {
  private ComponentElementIF component=null;
  private String COMPONENTELEMENTTAG="componentelement";
  
  public ComponentElementXMLizer(ComponentElementIF component) {
    this.component=component;
  }
  public ComponentElementXMLizer(String xmlelement) throws ComponentElementXMLizerException {
    readFromXML(xmlelement);  
  }
  public void fromXML(String xmlelement) throws ComponentElementXMLizerException {
    readFromXML(xmlelement);  
  }
  public ComponentElementIF getComponentElement() {
    return component;
  }
  public String getXML() {
    StringBuffer xmlelem=new StringBuffer("<"+COMPONENTELEMENTTAG+" ");
    if (component.getName()!=null) {
      xmlelem.append(" "+"name"+"=\""+component.getName()+"\"");
    }
    if (component.getVendor()!=null) {
      xmlelem.append(" "+"vendor"+"=\""+component.getVendor()+"\"");
    }
    if (component.getComponentType()!=null) {
      xmlelem.append(" "+"componenttype"+"=\""+component.getComponentType()+"\"");
    }
    if (component.getSubsystem()!=null) {
      xmlelem.append(" "+"subsystem"+"=\""+component.getSubsystem()+"\"");
    }
    if (component.getLocation()!=null) {
      xmlelem.append(" "+"location"+"=\""+component.getLocation()+"\"");
    }
    if (component.getCounter()!=null) {
      xmlelem.append(" "+"counter"+"=\""+component.getCounter()+"\"");
    }
    if (component.getSCVendor()!=null) {
      xmlelem.append(" "+"scvendor"+"=\""+component.getSCVendor()+"\"");
    }
    if (component.getSCName()!=null) {
      xmlelem.append(" "+"scname"+"=\""+component.getSCName()+"\"");
    }
    if (component.getRelease()!=null) {
      xmlelem.append(" "+"release"+"=\""+component.getRelease()+"\"");
    }
    if (component.getServiceLevel()!=null) {
      xmlelem.append(" "+"servicelevel"+"=\""+component.getServiceLevel()+"\"");
    }
    if (component.getPatchLevel()!=null) {
      xmlelem.append(" "+"patchlevel"+"=\""+component.getPatchLevel()+"\"");
    }
    if (component.getDeltaVersion()!=null) {
      xmlelem.append(" "+"deltaversion"+"=\""+component.getDeltaVersion()+"\"");
    }
    if (component.getUpdateVersion()!=null) {
      xmlelem.append(" "+"updateversion"+"=\""+component.getUpdateVersion()+"\"");
    }
    if (component.getApplyTime()!=null) {
      xmlelem.append(" "+"applytime"+"=\""+component.getApplyTime()+"\"");
    }
    if (component.getSCElementTypeID()!=null) {
      xmlelem.append(" "+"scelementtypeid"+"=\""+component.getSCElementTypeID()+"\"");
    }
    if (component.getSPElementTypeID()!=null) {
      xmlelem.append(" "+"spelementtypeid"+"=\""+component.getSPElementTypeID()+"\"");
    }
    if (component.getSPName()!=null) {
      xmlelem.append(" "+"spname"+"=\""+component.getSPName()+"\"");
    }
    if (component.getSPVersion()!=null) {
      xmlelem.append(" "+"spversion"+"=\""+component.getSPVersion()+"\"");
    }
    if (component.getComponentProvider()!=null) {
      xmlelem.append(" "+"componentprovider"+"=\""+component.getComponentProvider()+"\"");
    }  
    if (component.getChecksumAlgorithmName()!=null) {
      xmlelem.append(" "+"srcchecksumalgorithm"+"=\""+component.getChecksumAlgorithmName()+"\"");
    } 
    if (component.getChecksumValue()!=null) {
      xmlelem.append(" "+"srcchecksumvalue"+"=\""+component.getChecksumValue()+"\"");
    }   
    if (component.getServerType()!=null) {
      xmlelem.append(" "+"servertype"+"=\""+component.getServerType()+"\"");
    } 
    if (component.getSourceServer()!=null) {
      xmlelem.append(" "+"sourceserver"+"=\""+component.getSourceServer()+"\"");
    } 
    if (component.getChangeNumber()!=null) {
      xmlelem.append(" "+"changenumber"+"=\""+component.getChangeNumber()+"\"");
    } 
    if (component.getProjectName()!=null) {
      xmlelem.append(" "+"projectname"+"=\""+component.getProjectName()+"\"");
    }    
    if (component.getComponentVersion()!=null) {
      String _compversion=component.getComponentVersion().getXML();
      _compversion=replaceAll(_compversion,"&","&amp;"); // must be at position 1
      _compversion=replaceAll(_compversion,"<","&lt;");
      _compversion=replaceAll(_compversion,">","&gt;");
      _compversion=replaceAll(_compversion,"\"","&quot;");
      _compversion=replaceAll(_compversion,"'","&apos;");
      xmlelem.append(" "+"componentversion"+"=\""+_compversion+"\"");
    }    
    xmlelem.append("/> ");  
    return xmlelem.toString();
  }
  private void readFromXML(String xmlelement) throws ComponentElementXMLizerException {
    try {
      DefaultHandler defhand=new ComponentElementXMLizer(component);
      SAXParserFactory parserfactory=SAXParserFactory.newInstance();
      parserfactory.setValidating(false);  
      SAXParser parser=parserfactory.newSAXParser();
      ByteArrayInputStream bais=new ByteArrayInputStream(xmlelement.getBytes());
      parser.parse(bais,defhand);
      component=((ComponentElementXMLizer)defhand).getComponentElement();
    }
    catch (ParserConfigurationException e) {
      throw new ComponentElementXMLizerException("ParserConfigurationException"+e.getMessage());
    }
    catch (SAXException e) {
      throw new ComponentElementXMLizerException("SAXException"+e.getMessage());
    }
    catch (FactoryConfigurationError e) {
      throw new ComponentElementXMLizerException("FactoryConfigurationError"+e.getMessage());
    }
    catch (IOException e) {
      throw new ComponentElementXMLizerException("IOException"+e.getMessage());
    }
  }
  public void startElement(String ns, String localname, String qname, Attributes attributes) throws SAXException {
    String vendor          = null;
    String name            = null;
    String componenttype   = null;
    String subsystem       = null;
    String location        = null;
    String counter         = null;
    String scvendor        = null;
    String scname          = null;
    String release         = null;
    String servicelevel    = null;
    String patchlevel      = null;
    String deltaversion    = null;
    String updateversion   = null;
    String applytime       = null;
    String scelementtypeid = null;
    String spelementtypeid = null;
    String spname          = null;
    String spversion       = null;
    String componentprovider = null;
    String srcchecksumalgorithm = null;
	String srcchecksumvalue = null;
    String servertype      = null;
    String sourceserver    = null;
    String changenumber    = null;
    String projectname     = null;
    SCVersionIF componentversion = null;
    String _name=("".equals(localname))?qname:localname;
    int ind;
    if (COMPONENTELEMENTTAG.equals(_name)) {
      ind=attributes.getIndex("name");
      if (ind!=-1) {
        name=attributes.getValue(ind);
      }
      ind=attributes.getIndex("vendor");
      if (ind!=-1) {
        vendor=attributes.getValue(ind);
      }
      ind=attributes.getIndex("componenttype");
      if (ind!=-1) {
        componenttype=attributes.getValue(ind);
      }
      ind=attributes.getIndex("subsystem");
      if (ind!=-1) {
        subsystem=attributes.getValue(ind);
      }
      ind=attributes.getIndex("location");
      if (ind!=-1) {
        location=attributes.getValue(ind);
      }
      ind=attributes.getIndex("counter");
      if (ind!=-1) {
        counter=attributes.getValue(ind);
      }
      ind=attributes.getIndex("scvendor");
      if (ind!=-1) {
        scvendor=attributes.getValue(ind);
      }
      ind=attributes.getIndex("scname");
      if (ind!=-1) {
        scname=attributes.getValue(ind);
      }
      ind=attributes.getIndex("release");
      if (ind!=-1) {
        release=attributes.getValue(ind);
      }
      ind=attributes.getIndex("servicelevel");
      if (ind!=-1) {
        servicelevel=attributes.getValue(ind);
      }  
      ind=attributes.getIndex("patchlevel");
      if (ind!=-1) {
        patchlevel=attributes.getValue(ind);
      }    
      ind=attributes.getIndex("deltaversion");
      if (ind!=-1) {
        deltaversion=attributes.getValue(ind);
      }
      ind=attributes.getIndex("updateversion");
      if (ind!=-1) {
        updateversion=attributes.getValue(ind);
      }
      ind=attributes.getIndex("applytime");
      if (ind!=-1) {
        applytime=attributes.getValue(ind);
      }
      ind=attributes.getIndex("scelementtypeid");
      if (ind!=-1) {
        scelementtypeid=attributes.getValue(ind);
      }
      ind=attributes.getIndex("spelementtypeid");
      if (ind!=-1) {
        spelementtypeid=attributes.getValue(ind);
      }
      ind=attributes.getIndex("spname");
      if (ind!=-1) {
        spname=attributes.getValue(ind);
      }
      ind=attributes.getIndex("spversion");
      if (ind!=-1) {
        spversion=attributes.getValue(ind);
      }  
      ind=attributes.getIndex("componentprovider");
      if (ind!=-1) {
        componentprovider=attributes.getValue(ind);
      }   
	  ind=attributes.getIndex("srcchecksumalgorithm");
	  if (ind!=-1) {
		srcchecksumalgorithm=attributes.getValue(ind);
	  }
	  ind=attributes.getIndex("srcchecksumvalue");
	  if (ind!=-1) {
		srcchecksumvalue=attributes.getValue(ind);
	  }
      ind=attributes.getIndex("servertype");
      if (ind!=-1) {
        servertype=attributes.getValue(ind);
      }
      ind=attributes.getIndex("sourceserver");
      if (ind!=-1) {
        sourceserver=attributes.getValue(ind);
      }
      ind=attributes.getIndex("changenumber");
      if (ind!=-1) {
        changenumber=attributes.getValue(ind);
      }
      ind=attributes.getIndex("projectname");
      if (ind!=-1) {
        projectname=attributes.getValue(ind);
      }
      ind=attributes.getIndex("componentversion");
      if (ind!=-1) {
      	String xmlcomponentversion=attributes.getValue(ind);      
        xmlcomponentversion=replaceAll(xmlcomponentversion,"&apos;","'");
        xmlcomponentversion=replaceAll(xmlcomponentversion,"&quot;","\"");
        xmlcomponentversion=replaceAll(xmlcomponentversion,"&gt;",">");
        xmlcomponentversion=replaceAll(xmlcomponentversion,"&lt;","<");
        xmlcomponentversion=replaceAll(xmlcomponentversion,"&amp;","&"); // must be the last position
    	  try {
					componentversion=ComponentFactoryIF.getInstance().createSCVersionFromXML(xmlcomponentversion);
				}
				catch (ComponentElementXMLException e) {
          throw new SAXException("wrong componentversion xmlelement: "+e.getMessage());
				}
      }
      component=ComponentFactoryIF.getInstance().createComponentElement(vendor,name,componenttype,subsystem,location,counter,scvendor,scname,release,servicelevel,patchlevel,deltaversion,updateversion,applytime,scelementtypeid,spelementtypeid,spname,spversion,servertype,sourceserver,changenumber,projectname,componentversion,componentprovider,srcchecksumalgorithm,srcchecksumvalue);
    }
    else {
      throw new SAXException("wrong xmlelement");
    }
  } 
  private static String replaceAll(String s, String olds, String news) {
    if (olds==null || news==null || s==null) {
      return s;
    }
    final int oldslength=olds.length();
    final int newslength=news.length();
    int i=0;
    int t=0;
    if (oldslength==0) {
      return s;
    }
    if (olds.equals(news)) {
      return s;
    }
    StringBuffer sbuff=new StringBuffer(s);
    String _s=s;
    while ((i=_s.indexOf(olds,t))>-1) {
      sbuff.delete(i,i+oldslength);
      sbuff.insert(i,news);
      t=i+newslength;
      _s=sbuff.toString();
    }
    return sbuff.toString();
  }
}

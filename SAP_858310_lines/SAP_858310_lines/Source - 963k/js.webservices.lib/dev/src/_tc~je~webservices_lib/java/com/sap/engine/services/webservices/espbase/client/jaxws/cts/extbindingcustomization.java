/**
 * 
 * 
 * @version 1.0
 * @author Dimitar Velichkov, dimitar.velichkov@sap.com
 * 
 * Copyright SAP AG
 *  
 */

package com.sap.engine.services.webservices.espbase.client.jaxws.cts;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sap.engine.lib.xml.dom.DOM;
import com.sap.engine.lib.xml.parser.DOMParser;
import com.sap.engine.services.webservices.espbase.client.ProxyGeneratorConfigNew;
import com.sap.engine.services.webservices.espbase.client.ProxyGeneratorNew;
import com.sap.engine.services.webservices.espbase.client.jaxws.bindext.ExtendedBinding;
import com.sap.engine.services.webservices.espbase.client.jaxws.bindext.ExtendedBindingOperation;
import com.sap.engine.services.webservices.espbase.client.jaxws.bindext.ExtendedDefinition;
import com.sap.engine.services.webservices.espbase.client.jaxws.bindext.ExtendedEndpoint;
import com.sap.engine.services.webservices.espbase.client.jaxws.bindext.ExtendedOperationMod;
import com.sap.engine.services.webservices.espbase.client.jaxws.bindext.ExtendedPortType;
import com.sap.engine.services.webservices.espbase.client.jaxws.bindext.ExtendedPorttypeOperation;
import com.sap.engine.services.webservices.espbase.client.jaxws.bindext.ExtendedService;
import com.sap.engine.services.webservices.espbase.mappings.EndpointMapping;
import com.sap.engine.services.webservices.espbase.mappings.InterfaceMapping;
import com.sap.engine.services.webservices.espbase.mappings.MappingRules;
import com.sap.engine.services.webservices.espbase.mappings.OperationMapping;
import com.sap.engine.services.webservices.espbase.mappings.ParameterMapping;
import com.sap.engine.services.webservices.espbase.mappings.ServiceMapping;
import com.sap.engine.services.webservices.espbase.wsdl.Definitions;
import com.sap.engine.services.webservices.jaxrpc.exceptions.JaxWsMappingException;
import com.sap.engine.services.webservices.jaxrpc.schema2java.SchemaToJavaConfig;

public class ExtBindingCustomization {

  public static final String    JAXWS_NS     = "http://java.sun.com/xml/ns/jaxws";
  public static final String    JAXWS_VER    = "2.0";
  
  private DOMParser             parser       = null;
  private Document              parsedWsdl   = null;
  private Document              bindingExt   = null;
  private HashMap               origToModMap = null;
  private String                inWsdlPath   = null;
  private ByteArrayOutputStream outputWsdl   = null;
  private String                newPkgName   = null;

  /**
   * Create a resolver object
   */
  public ExtBindingCustomization() {
     /**
    parser = new DOMParser();
    origToModMap = new HashMap();
    outputWsdl = new ByteArrayOutputStream();
    */
  }

  /**
   * Return the modified WSDL 
   * 
   */
  public InputSource getModifiedWsdl() {

    if (parsedWsdl == null) {
      return null;
    }

    InputSource modded = new InputSource();
    modded.setByteStream(new ByteArrayInputStream(outputWsdl.toByteArray()));
    modded.setSystemId(inWsdlPath);

    return modded;
  }

  public Document getModifiedWsdlDOM() {
    return parsedWsdl;
  }

  private void serializeParsedWsdl() throws JaxWsMappingException {
    outputWsdl.reset();
    try {
      TransformerFactory.newInstance().newTransformer().transform(new DOMSource(parsedWsdl), new StreamResult(outputWsdl));
    } catch (Exception e) {
      throw new JaxWsMappingException(JaxWsMappingException.WSDL_SERIALIZATION_ERROR, e);
    }
  }

  /**
   * Final step - modify the WSDL DOM tree with the binding language extensions
   * 
   * @throws IOException -
   *           error writing to output file
   * 
   */
  private void modifyInputWSDL() {

    Attr jaxwsNsDecl = parsedWsdl.createAttribute("xmlns:jaxws");
    jaxwsNsDecl.setValue(JAXWS_NS);
    ((Element) parsedWsdl.getFirstChild()).setAttributeNodeNS(jaxwsNsDecl);

    Set allElemsToModify = origToModMap.keySet();
    for (Iterator it = allElemsToModify.iterator(); it.hasNext();) {

      Node toMod = (Node) it.next();
      Element bindingEl = parsedWsdl.createElement("jaxws:bindings");
      List mods = (List) origToModMap.get(toMod);

      for (int i = 0; i < mods.size(); i++) {      
        Node insertMe = parsedWsdl.importNode((Node) mods.get(i), true);
        bindingEl.appendChild(insertMe);        
      }
      toMod.insertBefore(bindingEl, toMod.getLastChild());
    }

  }

  // load and parse ext. doc
  private void loadAndParseExt(URL fileLoc) throws JaxWsMappingException {

    InputStream urlStr = null;
    try {
      urlStr = fileLoc.openStream();
      bindingExt = parser.parse(urlStr);
    }

    catch (IOException e) {
      throw new JaxWsMappingException(JaxWsMappingException.BAD_BINDING_DOC_PATH, e, fileLoc.toString());
    }

    catch (Exception e) {
      throw new JaxWsMappingException(JaxWsMappingException.EXT_DOC_PARSING_ERROR, e, fileLoc.toString());
    }

    finally {
      try {
        if (urlStr != null) {
          urlStr.close();
        }
      } catch (IOException e) {
         //$JL-EXC$
      }
    }

    Element root = bindingExt.getDocumentElement();
    String version = root.getAttribute("version");    

    if (version != null && version.length() != 0 && !version.equals(JAXWS_VER)) {
      throw new JaxWsMappingException(JaxWsMappingException.BAD_JAXWS_VERSION, new Object[] { JAXWS_VER });
    }

    inWsdlPath = root.getAttribute("wsdlLocation");
    if(inWsdlPath == null || inWsdlPath.length() == 0){
      throw new JaxWsMappingException(JaxWsMappingException.EXT_DOC_PARSING_ERROR, fileLoc.toString());
    }

    URL wsdlUrl = null;
    try {
      wsdlUrl = new URL(inWsdlPath); // Try to set the
    }

    catch (MalformedURLException e) { //probably references a file?
      File parentDir = new File(fileLoc.getPath()).getParentFile();
      try{
        wsdlUrl = new File(parentDir + System.getProperty("file.separator") + inWsdlPath).toURL();
        inWsdlPath = wsdlUrl.toString();
      }
      catch(MalformedURLException bad){
        throw new JaxWsMappingException(JaxWsMappingException.BAD_BINDING_DOC_PATH, e, fileLoc.toString());
      }
    }

    try {
      parseToDOM(wsdlUrl);
    } catch (Exception e) {
      throw new JaxWsMappingException(JaxWsMappingException.WSDL_DOM_ERROR, e);
    }

  }

  private void parseToDOM(URL inWsdl) throws SAXException, IOException {

    InputStream wsdlStr = null;

    try {
      wsdlStr = inWsdl.openStream();
      parsedWsdl = parser.parse(wsdlStr);
    }

    finally {
      try {
        if (wsdlStr != null) {
          wsdlStr.close();
        }
      } catch (IOException e) {
        //$JL-EXC$
      }
    }

  }

  /**
   * 
   * Load an external binding file. The WSDL to be modified is specified as an
   * attribute in the file's root element
   * 
   * @param fileLoc
   *          The external file's URL
   * @throws JaxWsMappingException
   */
  public void loadBindingFile(URL fileLoc) throws JaxWsMappingException {

    loadAndParseExt(fileLoc);
    
    // get all binding nodes
    NodeList bindingNodes = bindingExt.getElementsByTagNameNS(JAXWS_NS, "bindings");
    int numNodes = bindingNodes.getLength();

    ArrayList allBindingMods;

    for (int i = 0; i < numNodes; i++) {

      allBindingMods = new ArrayList();

      Element elem = (Element) bindingNodes.item(i);
      String xpath = elem.getAttribute("node");

      // root binding declaration, no 'node' attribute, so set it to the root
      // element
      if (xpath.equals("")) {
        xpath = "/wsdl:definitions";
        elem.setAttribute("node", xpath);
      }

      // this binding declaration MUST have a parent!
      // the xpath is relative however, make it absolute
      else {
        String oldVal = elem.getAttribute("node");
        elem.setAttribute("node", ((Element) elem.getParentNode()).getAttribute("node") + "/" + oldVal);
        xpath = elem.getAttribute("node");
      }

      Node found;

      try {
        found = DOM.toNode(xpath, parsedWsdl);
      }

      catch (NullPointerException e) {
        throw new JaxWsMappingException(JaxWsMappingException.EXTERNAL_BINDINGF_NOTFORMED, xpath);
      }

      if (found == null) {
        throw new JaxWsMappingException(JaxWsMappingException.NODE_NOT_FOUND, xpath, inWsdlPath);
      }

      NodeList bindingMods = elem.getChildNodes();
      int childNodesLength = bindingMods.getLength();
      for (int j = 0; j < childNodesLength; ++j) {

        if (bindingMods.item(j).getNodeType() != Node.ELEMENT_NODE || bindingMods.item(j).getNodeName().equals("jaxws:bindings")) {
          continue;
        }
        allBindingMods.add((Element) bindingMods.item(j));
      }

      // map a node from the input WSDL to all required modifications
      origToModMap.put(found, allBindingMods);

    }

    modifyInputWSDL();
    serializeParsedWsdl();

  }

  public ExtendedDefinition applyMapping(ProxyGeneratorConfigNew pGenConf) throws JaxWsMappingException {

    Definitions def = pGenConf.getWsdl();
    MappingRules map = pGenConf.getMappingRules();
    SchemaToJavaConfig schemaToJava = pGenConf.getSchemaConfig();

    ExtendedDefinition extDef = new ExtendedDefinition(def, true);
    extDef.applyExtensions();

    ServiceMapping[] allServMaps = map.getService();
    String newPkgName = getNewPkgName(pGenConf.getWsdl());

    for (int i = 0; i < allServMaps.length; ++i) {
      // set service SIclass name
      ExtendedService exServ = extDef.getExtendedService(allServMaps[i].getServiceName());

      if (exServ != null && exServ.getNewName() != null) {
        allServMaps[i].setSIName(newPkgName + "." + exServ.getNewName());
      }

      EndpointMapping[] endPts = allServMaps[i].getEndpoint();
      for (int j = 0; j < endPts.length; ++j) {

        ExtendedEndpoint extEP = (ExtendedEndpoint) exServ.getExtendedChild(new QName(endPts[i].getPortQName()));

        if (extEP != null && extEP.getMethodName() != null) {
          // TODO: provider??
          // set port 'getter' method
          endPts[i].setPortJavaName(extEP.getMethodName());
        }

        InterfaceMapping intMap = map.getInterface(endPts[i].getPortPortType(), endPts[i].getPortBinding());
        ExtendedPortType extInt = extDef.getExtendedPortType(intMap.getPortType());
                        
        if (extInt != null) {
          
          String newName = extInt.getClassName();
          if (newName != null) {
            intMap.setSEIName(newPkgName + "." + newName);            
          }                    
                    
          OperationMapping[] allOps = intMap.getOperation();
          for (int k = 0; k < allOps.length; ++k) {
            OperationMapping theOp = allOps[k];
            ParameterMapping[] paramMaps = theOp.getParameter();

            ExtendedPorttypeOperation extOp = (ExtendedPorttypeOperation) extInt.getExtendedChild(new QName(theOp.getWSDLOperationName()));
            if (extOp != null) {

              // set the new operation name
              if (extOp.getOpName() != null) {
                theOp.setJavaMethodName(extOp.getOpName());
              }

              // TODO: unwrap operation if enableWrapperStyle is false
              if (extOp.getWrapperStyle() != null && !Boolean.valueOf(extOp.getWrapperStyle()).booleanValue()) {
                // pGen.unwrapOperation(intMap, theOp, pGenConf);
              }

              for (int paramIx = 0; paramIx < paramMaps.length; ++paramIx) {
                ParameterMapping paramMapping = paramMaps[paramIx];
                ExtendedOperationMod pTypeMod = (ExtendedOperationMod) extOp
                    .getExtendedChild(new QName(paramMapping.getWSDLParameterName()));

                if (pTypeMod != null) {

                  //portType/operation/fault class extension
                  if(pTypeMod.getType() == ExtendedOperationMod.FAULT){
                    String newFaultClass = pTypeMod.getFaultClass();
                    paramMapping.setJavaType(newPkgName + "." + newFaultClass);
                    
                  }
                  else{
                    String javaType = pTypeMod.getJavaType(schemaToJava);
                    paramMapping.setJavaType(javaType);

                    paramMapping.setJavaType(javaType);
                    paramMapping.setWSDLParameterName(pTypeMod.getNewName());
                  }

                }

              }
              
              ExtendedBinding extBinding = extDef.getExtendedBinding(endPts[i].getPortBinding());
              // get <soap:binding><soap:operation> mods
              ExtendedBindingOperation extBindingOp = (ExtendedBindingOperation) extBinding.getExtendedChild(new QName(theOp
                  .getWSDLOperationName()));
              // TODO: no current model representation for <soap:operation>
              // extensions - mimeContent, headerMapping(?)

              // parameter extensions

              if (extBindingOp != null) {

                for (int paramIx = 0; paramIx < paramMaps.length; ++paramIx) {
                  ParameterMapping paramMapping = paramMaps[paramIx];

                  ExtendedOperationMod bMod = (ExtendedOperationMod) extBindingOp.getExtendedChild(new QName(paramMapping
                      .getWSDLParameterName()));
                  if (bMod != null) {

                    if (!paramMapping.isHeader()) {

                      String excMsg;
                      if (bMod.getType() == ExtendedOperationMod.FAULT) {
                        excMsg = ExtendedBindingOperation.EXCEPTION.toString();
                      } else {
                        excMsg = ExtendedPorttypeOperation.PARAMETER.toString();
                      }
                      throw new JaxWsMappingException(JaxWsMappingException.UNSUPPORTED_EXTENSION, excMsg);
                    }

                    // the new java type;
                    String javaType = bMod.getJavaType(schemaToJava);
                    paramMapping.setJavaType(javaType);
                    paramMapping.setWSDLParameterName(bMod.getNewName());

                  }
                }

              }
            }

          }

        }

      }

    }

    return extDef;

  }
  
  public void setNewPkgName(String name){
    newPkgName = name;
  }
  
  public String getNewPkgName(Definitions defs) throws JaxWsMappingException {
    if(newPkgName == null){
      ExtendedDefinition extDef = new ExtendedDefinition(defs, false);
      extDef.applyExtensions();
      newPkgName = extDef.getExtPkgName();  
    }
    return newPkgName;
  }

}

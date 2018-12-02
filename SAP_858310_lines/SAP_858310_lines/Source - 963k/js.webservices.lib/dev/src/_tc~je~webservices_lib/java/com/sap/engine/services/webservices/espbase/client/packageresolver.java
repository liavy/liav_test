package com.sap.engine.services.webservices.espbase.client;

import java.util.Properties;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.sap.engine.services.webservices.espbase.wsdl.Definitions;
import com.sap.engine.services.webservices.espbase.wsdl.ExtensionElement;
import com.sap.engine.services.webservices.espbase.wsdl.ObjectList;
import com.sun.xml.bind.api.impl.NameConverter;

public class PackageResolver implements PackageResolverInterface {
  
  private String packageName;
  
  PackageResolver(ProxyGeneratorConfigNew proxyGeneratorCfg, boolean isJaxws) {
    packageName = determinePackageName(proxyGeneratorCfg, isJaxws);
  }
  
  private String determinePackageName(ProxyGeneratorConfigNew proxyGeneratorCfg, boolean isJaxws) {
    String proxyGenCfgPackageName = proxyGeneratorCfg.getOutputPackage();
    if(proxyGenCfgPackageName != null) {
      return(proxyGenCfgPackageName);
    } else if(!isJaxws) {
      return("");
    }
    
    Definitions wsdlDefs = proxyGeneratorCfg.getWsdl();
    String wsdlDefsTargetNS = determineWsdlDefinitionsTargetNamespace(wsdlDefs);
    String customizationPackageName = determineCustomizationPackageName(wsdlDefs, wsdlDefsTargetNS);
    return(customizationPackageName == null ? NameConverter.smart.toPackageName(wsdlDefsTargetNS) : customizationPackageName);
  }
  
  private String determineWsdlDefinitionsTargetNamespace(Definitions wsdlDefs) {
    String wsdlDefsTargetNS = wsdlDefs.getProperty(Definitions.TARGET_NS);
    return(wsdlDefsTargetNS == null ? "" : wsdlDefsTargetNS);
  }
  
  private String determineCustomizationPackageName(Definitions wsdlDefs, String wsdlDefsTargetNS) {
    ObjectList extensionElems = wsdlDefs.getChildren(Definitions.EXTENSION_ELEMENT_ID);
    for(int i = 0; i < extensionElems.getLength(); i++) {
      ExtensionElement extensionElem = (ExtensionElement)(extensionElems.item(i));
      Element extensionElemContent = extensionElem.getContent();
      if(JAXWS_BINDINGS_EXTENSION_NS.equals(extensionElemContent.getNamespaceURI()) && 
         JAXWS_BINDINGS_EXTENSION_ELEMENT_NAME.equals(extensionElemContent.getLocalName()) &&
         wsdlDefsTargetNS.equals(extensionElem.getOwnerNamespace())) {
        return(getPackageNameFromBindingsExtension(extensionElemContent));
      }
    }
    return(null);
  }
  
  private String getPackageNameFromBindingsExtension(Element bindingsElement) {
    NodeList packageElements = bindingsElement.getElementsByTagNameNS(JAXWS_BINDINGS_EXTENSION_NS, JAXWS_PACKAGE_EXTENSION_ELEMENT_NAME);
    if(packageElements != null && packageElements.getLength() > 0) {
      Element packageElement = (Element)(packageElements.item(0));
      if(packageElement.hasAttribute(JAXWS_PACKAGE_EXTENSION_ATTRIB_NAME)) {
        return(packageElement.getAttribute(JAXWS_PACKAGE_EXTENSION_ATTRIB_NAME));
      }
    }
    return(null);
  }
  
  public String resolve(String namespace) {
    return(packageName);
  }
}

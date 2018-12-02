package com.sap.engine.services.webservices.espbase.client;

import java.util.Properties;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import com.sap.engine.services.webservices.espbase.wsdl.Definitions;
import com.sap.engine.services.webservices.espbase.wsdl.ExtensionElement;
import com.sap.engine.services.webservices.espbase.wsdl.ObjectList;
import com.sun.xml.bind.api.impl.NameConverter;

public class PackageResolverJAXWS implements PackageResolverInterface {
  
    private ProxyGeneratorConfigNew proxyGeneratorCfg;
    private Properties namespaceToPackageMapping;
    
    PackageResolverJAXWS(ProxyGeneratorConfigNew proxyGeneratorCfg, boolean isJaxws) {
      this.proxyGeneratorCfg = proxyGeneratorCfg;
      if(proxyGeneratorCfg.getOutputPackage() == null && isJaxws) {
        namespaceToPackageMapping = createNamespaceToPackageMapping();
      }
    }
    
    private Properties createNamespaceToPackageMapping() {
      Definitions wsdlDefs = proxyGeneratorCfg.getWsdl();
      Properties namespaceToPackageMapping = new Properties();
      ObjectList extensionElems = wsdlDefs.getChildren(Definitions.EXTENSION_ELEMENT_ID);
      for(int i = 0; i < extensionElems.getLength(); i++) {
        ExtensionElement extensionElem = (ExtensionElement)(extensionElems.item(i));
        Element extensionElemContent = extensionElem.getContent();
        if(JAXWS_BINDINGS_EXTENSION_NS.equals(extensionElemContent.getNamespaceURI()) && JAXWS_BINDINGS_EXTENSION_ELEMENT_NAME.equals(extensionElemContent.getLocalName())) {
          String packageName = getPackageNameFromBindingsExtension(extensionElemContent);
          if(packageName != null) {
            namespaceToPackageMapping.put(extensionElem.getOwnerNamespace(), packageName);
          }
        }
      }
      return(namespaceToPackageMapping);
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
      return(namespaceToPackageMapping != null ? resolveFromNSToPackageMapping(namespace == null ? "" : namespace) : 
                                                 proxyGeneratorCfg.getOutputPackage());
    }
    
    private String resolveFromNSToPackageMapping(String namespace) {
      String packageName = namespaceToPackageMapping.getProperty(namespace);
      if(packageName == null) {
        packageName = NameConverter.smart.toPackageName(namespace);
        namespaceToPackageMapping.put(namespace, packageName);
      }
      return(packageName);
    }
    
  
}

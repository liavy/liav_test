package com.sap.engine.services.webservices.server.deploy.jee5;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

import javax.xml.namespace.QName;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import com.sap.engine.lib.descriptors5.web.ServletType;
import com.sap.engine.lib.descriptors5.web.WebAppType;
import com.sap.engine.lib.descriptors5.web.WebAppType.Choice1;
import com.sap.engine.lib.descriptors5.webservices.EjbLinkType;
import com.sap.engine.lib.descriptors5.webservices.FullyQualifiedClassType;
import com.sap.engine.lib.descriptors5.webservices.HandlerChainType;
import com.sap.engine.lib.descriptors5.webservices.HandlerChainsType;
import com.sap.engine.lib.descriptors5.webservices.PathType;
import com.sap.engine.lib.descriptors5.webservices.PortComponentType;
import com.sap.engine.lib.descriptors5.webservices.ServiceImplBeanType;
import com.sap.engine.lib.descriptors5.webservices.ServletLinkType;
import com.sap.engine.lib.descriptors5.webservices.WebserviceDescriptionType;
import com.sap.engine.lib.descriptors5.webservices.WebservicesType;
import com.sap.engine.lib.descriptors5.webservices.XsdQNameType;
import com.sap.engine.lib.processor.impl.WebProcessor5;
import com.sap.engine.lib.processor.impl.WebServicesProcessor5;
import com.sap.engine.services.webservices.tools.ReferenceByteArrayOutputStream;
import com.sap.engine.services.webservices.tools.SharedDocumentBuilders;
import com.sap.engine.services.webservices.webservices630.server.deploy.util.IOUtil;
import com.sap.engine.services.webservices.webservices630.server.deploy.util.jar.JarUtil;
import com.sap.lib.javalang.annotation.AnnotationRecord;
import com.sap.lib.javalang.annotation.AnnotationRecord.NamedMember;
import com.sap.lib.javalang.element.ClassInfo;
import com.sap.lib.javalang.element.ElementInfo;
import com.sap.lib.javalang.file.FileInfo;
import com.sap.lib.javalang.tool.ReadResult;

public class WSAnnotationConvertor {
  
  private Hashtable<QName, Hashtable<String, AnnotationRecord>> wsAnnotationsPerArch = new Hashtable<QName, Hashtable<String, AnnotationRecord>>();
  private Hashtable<String, AnnotationRecord> wsSEIAnnotaions = new Hashtable<String, AnnotationRecord>();
  private Hashtable<String, Choice1> ServletChoiseHT = null;
  private Hashtable<String, HandlerChainsType> HandlersByPath = new Hashtable<String, HandlerChainsType>();
  private Hashtable<String, PortComponentType> existingDescriptor = new Hashtable<String, PortComponentType>();
  private Hashtable<String, String> lunchExistingDescrRestElements = new Hashtable<String, String>();
  private WebservicesType webServicesDescriptor;
  private ArrayList<AnnotationRecord> wsAnnotations;
  private Map<String, List<AnnotationRecord>> annotationMap = null;
  private JarFile archiveJarFile = null;
  private ReadResult annotations;
  private static final String WEB_SERVICE_VERSION = "1.2";
  private static final String WEB_XML = "web.xml";
  private static final String WEBSERVICES_XML = "webservices.xml";
  private static final String WEB_INF = "WEB-INF";
  private static final String META_INF = "META-INF";
  private String tmpPath = "", jarName = "" ,jarForWork = "";
  private boolean isItWar = false, isDescriptorExists = false, isTempJarExists = false;
  
  public Hashtable<QName, Hashtable<String, AnnotationRecord>> getWSAnnotations() {
    return wsAnnotationsPerArch;	  
  }
  
  public void convertAnnotations(ReadResult parsedAnnotations, String tempDir, String archiveFileName) throws ConversionException {
   
    annotations = parsedAnnotations;
    tmpPath = tempDir;
    //
    try {
    	for (FileInfo file : ((com.sap.lib.javalang.file.FolderInfo) annotations.getProcessedFiles()[0]).getFiles()) {
     
	      if(!checkArchive0(file.getName(), archiveFileName)) {
	        continue;  
	      }
	      
	      if (clearAndInitialVARs(file)){
	        continue;
	      }
	      if (checkArhive(file)) {
	        continue;
	      }
	      
	      loadExistingDescriptor();
	      
	      if(annotationMap.containsKey("javax.jws.WebService")) {
	        wsAnnotations = (ArrayList<AnnotationRecord>) annotationMap.get("javax.jws.WebService");
	        groupSEIAnnotaions(wsAnnotations);
	        groupWSAnnotations(wsAnnotations);
	      } 
	      
	      if(annotationMap.containsKey("javax.xml.ws.WebServiceProvider")) {
	        if(wsAnnotations != null){
	          wsAnnotations = null ;
	        }
	        wsAnnotations = (ArrayList<AnnotationRecord>) annotationMap.get("javax.xml.ws.WebServiceProvider");
	        groupSEIAnnotaions(wsAnnotations);
	        groupWSAnnotations(wsAnnotations);
	      }
	      if (wsAnnotationsPerArch.isEmpty() || wsAnnotationsPerArch == null){
	        continue; 
	      }
	      if (isItWar){
	          //
	          fillServletChoiseHT();
	      }
	      if (annotationMap.containsKey("javax.jws.HandlerChain")){
	
	        loadModuleHandlers((ArrayList<AnnotationRecord>)annotationMap.get("javax.jws.HandlerChain"));
	      }
	 
	      loadWSAnnotationsFromModule();
      } //// end loop
    }finally {
      closeJar();
    }
  }

  private void groupWSAnnotations(ArrayList<AnnotationRecord> wsAnnotations) {
   
    // groups annotations in wsAnnotationsPerService by qname
    if (wsAnnotations == null || wsAnnotations.isEmpty()) {
      return;
    }
    String name, serviceName, targetNamespace;
    javax.xml.namespace.QName qname;
    Hashtable<String, AnnotationRecord> groupedByServiceAnnotations;
    boolean neededGeneration = false;

    for (AnnotationRecord currWsAnnotation : wsAnnotations) {
      name = ""; targetNamespace = ""; serviceName = "";qname = null;
      if(currWsAnnotation == null){
        continue;
      }
      if (((com.sap.lib.javalang.element.ClassInfo) currWsAnnotation.getOwner()).isInterface()) {
        continue;
      }
      name = generateMemberValue(currWsAnnotation, "name", neededGeneration);
      targetNamespace = generateMemberValue(currWsAnnotation, "targetNamespace", neededGeneration);///????
      
      if(isDescriptorExists && lunchExistingDescrRestElements.containsKey(name)){
        serviceName = lunchExistingDescrRestElements.get(name);
      }else{
        serviceName = generateMemberValue(currWsAnnotation, "serviceName", neededGeneration);
      }
      qname = new javax.xml.namespace.QName(targetNamespace, serviceName);

      groupedByServiceAnnotations = new Hashtable();
	  if (wsAnnotationsPerArch.containsKey(qname)) {
	    groupedByServiceAnnotations = wsAnnotationsPerArch.get(qname);
	  }
	  groupedByServiceAnnotations.put(name, currWsAnnotation);
	  wsAnnotationsPerArch.put(qname, groupedByServiceAnnotations);
    }// end loop
  }

  private void groupSEIAnnotaions(ArrayList<AnnotationRecord> wsAnnotations) {
    
    String className = "";
    //
    if (wsAnnotations == null || wsAnnotations.isEmpty()) {
      return;
    }
    for (AnnotationRecord currWsAnnotation : wsAnnotations) {
      if(currWsAnnotation == null){
        continue;
      }
      if (!(((com.sap.lib.javalang.element.ClassInfo) currWsAnnotation.getOwner()).isInterface())) {
        continue;
      }
      className = "";
      className = currWsAnnotation.getOwner().getName();
      if (!wsSEIAnnotaions.containsKey(className)) {
        wsSEIAnnotaions.put(className, currWsAnnotation);
      }
            
    }// end loop
  }

  private void loadWSAnnotationsFromModule()throws ConversionException{
    
    //wsAnnotationsPerArch -> Hashtable<javax.xml.namespace.QName, Hashtable<String, AnnotationRecord>>
    Hashtable servicesForQname = new Hashtable();
    Enumeration e_wsAnnotSrv = wsAnnotationsPerArch.keys();
    WebserviceDescriptionType[] wsDescTypeArray = new WebserviceDescriptionType[wsAnnotationsPerArch.size()];
    int i = 0;
    //
    while (e_wsAnnotSrv.hasMoreElements()) {
      if (!servicesForQname.isEmpty()) {
        servicesForQname = new Hashtable();
      }
      servicesForQname = wsAnnotationsPerArch.get(e_wsAnnotSrv.nextElement());
      if (servicesForQname.isEmpty()) {
        continue;
      }
      wsDescTypeArray[i] = loadWsDescriptionTypeArray(servicesForQname);
      i++;
    }// end loop
    
    webServicesDescriptor = new WebservicesType();
    webServicesDescriptor.setWebserviceDescription(wsDescTypeArray);
    webServicesDescriptor.setVersion(WEB_SERVICE_VERSION);
    
   try {
	   String tmpjarForWork = IOUtil.getFileNameWithoutExt(jarName) + "_ws" + IOUtil.getFileExtension(jarName);
	   if(isTempJarExists){
		   buildJar(webServicesDescriptor, WEBSERVICES_XML, tmpPath, jarForWork, tmpjarForWork);
	   } else { 
		   buildJar(webServicesDescriptor, WEBSERVICES_XML, tmpPath, jarName, tmpjarForWork);
	   }
	} catch (RemoteException e) {
		throw new ConversionException("webservices_5307", new String[]{tmpPath + File.separator + WEBSERVICES_XML}, e );
	} catch (FileNotFoundException e) {
		throw new ConversionException("webservices_5308", new String[]{tmpPath + File.separator + WEBSERVICES_XML}, e );
	} catch (IOException e) {
		throw new ConversionException("webservices_5309", new String[]{tmpPath + File.separator + WEBSERVICES_XML}, e );
	} catch (Exception e) {
    	        throw new ConversionException("webservices_5310", new String[]{tmpPath + File.separator + jarForWork}, e );
  	}
  }

  private WebserviceDescriptionType loadWsDescriptionTypeArray(Hashtable<String, AnnotationRecord> servicesForQname) {
    
    AnnotationRecord serviceAnotationRecord = null;
    String serviceNameSTR = "", nameSTR = "",endpointString = "";
    int i = 0;
    boolean neededGENERATION = false;// in this case it is a need to generate value
    com.sap.engine.lib.descriptors5.webservices.String name = new com.sap.engine.lib.descriptors5.webservices.String();
    PortComponentType portComponentTypeTmp = null;
    FullyQualifiedClassType fullyQualifiedClassType;
    PortComponentType[] portComponentTypeArray = new PortComponentType[servicesForQname.size()];
    // /
    Enumeration e_wsForQname = servicesForQname.keys();
    
    while (e_wsForQname.hasMoreElements()) {
      serviceAnotationRecord = null; nameSTR = "";endpointString = "";fullyQualifiedClassType = null;
      name = new com.sap.engine.lib.descriptors5.webservices.String();
      //
      serviceAnotationRecord = (AnnotationRecord) servicesForQname.get(e_wsForQname.nextElement());
      if (serviceAnotationRecord == null) {
        if (e_wsForQname.hasMoreElements()){
          continue;
        }else{
          return null;
        }
      }
      nameSTR = generateMemberValue(serviceAnotationRecord, "name",neededGENERATION);
      
      if(isDescriptorExists){
    	  portComponentTypeTmp = genPortCompWithExistingValue(nameSTR);
      }else{
        portComponentTypeTmp = new PortComponentType();
      }
      // set - <rn3:port-component><rn3:port-component-name>
      name.set_value(nameSTR);
      portComponentTypeTmp.setPortComponentName(name);
      // set - <rn3:port-component><rn3:wsdl-service>
      portComponentTypeTmp.setWsdlService(generateWSDLserviceValue(portComponentTypeTmp,serviceAnotationRecord));
      // set - <rn3:port-component><rn3:wsdl-port
      portComponentTypeTmp.setWsdlPort(generateWsdlPortValue(portComponentTypeTmp,serviceAnotationRecord));
      // set - <rn3:port-component><rn3:service-endpoint-interface>
      endpointString = generateMemberValue(serviceAnotationRecord, "endpointInterface",isDescriptorExists);
      
      if(endpointString.equals("")) {
        fullyQualifiedClassType = portComponentTypeTmp.getServiceEndpointInterface();
        if(portComponentTypeTmp.getServiceEndpointInterface()!= null && !fullyQualifiedClassType.get_value().toString().equals("")) {
         endpointString = portComponentTypeTmp.getServiceEndpointInterface().get_value().toString();
        }else{
          endpointString = generateMemberValue(serviceAnotationRecord, "endpointInterface",neededGENERATION);
        }
      }
      if (!endpointString.equals("")) {
        fullyQualifiedClassType = new FullyQualifiedClassType();
        fullyQualifiedClassType.set_value(endpointString);
        portComponentTypeTmp.setServiceEndpointInterface(fullyQualifiedClassType);
      }
      // set - <rn3:port-component><rn3:service-impl-bean> -- <rn3:ejb-link> -- or --<>
      portComponentTypeTmp.setServiceImplBean(serviceImplBeanTypeGeneration(serviceAnotationRecord, portComponentTypeTmp));
      // set - <rn3:port-component><rn3:handler-chains><rn3:handler-chain> -> <rn3:handler> <rn3:handler-name>
      
      setHandlers(serviceAnotationRecord,portComponentTypeTmp);
      //
      portComponentTypeArray[i] = portComponentTypeTmp;
      i++;
      if (isDescriptorExists && lunchExistingDescrRestElements.containsKey(nameSTR)){
        if (serviceNameSTR.equals("")) {
          serviceNameSTR = lunchExistingDescrRestElements.get(nameSTR);
        }
        lunchExistingDescrRestElements.remove(nameSTR);
      }      
      if (serviceNameSTR.equals("")) {
          serviceNameSTR = generateMemberValue(serviceAnotationRecord, "serviceName", false);
      }
    } // end loop
    
    /*if(!existingDescriptor.isEmpty()){
     /tuk proveriava za su6testvuva6ti portove kum tozi service ostanali v ExistingDescriptor
      loadfromExisitngDescriptorRemain(serviceName_helpStr);
    }*/

    WebserviceDescriptionType wsDescType = new WebserviceDescriptionType();
    PathType pt = new PathType();
    pt.set_value("");
    wsDescType.setJaxrpcMappingFile(pt);
    wsDescType.setPortComponent(portComponentTypeArray);
    com.sap.engine.lib.descriptors5.webservices.String descName = new com.sap.engine.lib.descriptors5.webservices.String();
    descName.set_value(serviceNameSTR);
    wsDescType.setWebserviceDescriptionName(descName);
    pt = new PathType();
    pt.set_value(generateMemberValue(serviceAnotationRecord,"wsdlLocation",neededGENERATION));
        
    if (!pt.get_value().equals("")) {
      wsDescType.setWsdlFile(pt);
    }
    return wsDescType;
  }

  private String generateMemberValue(AnnotationRecord annotation, String neededMember,boolean neededGeneration) {
    
    // get needed member from implementation annotaions
    //if neededGeneration = false => whole generation    
    NamedMember nameMember = annotation.getMember(neededMember);
    //
    if (!(nameMember == null) && !nameMember.getStringValue().equals("")) {
      return nameMember.getStringValue();
    }
    if(neededGeneration){
      return "";
    }
    if (neededMember.equals("targetNamespace")) {
      return targetNamespaceGeneration(annotation.getOwner().getName(),annotation.getMember("endpointInterface"));
    }
    // get default value according to ws specification
    String FQAclassName = ((ElementInfo) annotation.getOwner()).getName();
    String annClassName = FQAclassName.substring(FQAclassName.lastIndexOf(".") + 1);
    
    if (FQAclassName.equals("") || annClassName.equals("")) {
      return "";
    }
    
    if (neededMember.equals("name")) {
      if(annotation.getTypeName().equals("javax.jws.WebService")){
        return annClassName;
      }else{
        return FQAclassName;
      }
    } else if (neededMember.equals("serviceName")) {
      return annClassName + "Service";
    } else if (neededMember.equals("portName")) {
      String portName = "";
      portName = generateMemberValue(annotation,"name",true);
      if(portName.equals("")) {
        portName = annClassName;
      }
      return portName + "Port";
    } else{
      //endpointInterface and wsdlLocation
      return "";
    }
  }
  
  private XsdQNameType generateWSDLserviceValue(PortComponentType portComponentTypeTmp,AnnotationRecord serviceAnotationRecord){
    
    XsdQNameType xsdQnameSERVICE = null;
    String serviceName_helpStr = "", targetNamespace = ""; 
    boolean neededGeneration = false;
    //gen service name

    if(isDescriptorExists && portComponentTypeTmp.getWsdlService() != null){
      xsdQnameSERVICE = portComponentTypeTmp.getWsdlService();
      serviceName_helpStr = xsdQnameSERVICE.get_value().getLocalPart().toString();
      targetNamespace = portComponentTypeTmp.getWsdlService().get_value().getNamespaceURI().toString();
    }
    if (serviceName_helpStr.equals("")){
      serviceName_helpStr = generateMemberValue(serviceAnotationRecord, "serviceName", neededGeneration);
    }
    com.sap.engine.lib.descriptors5.webservices.String serviceName = new com.sap.engine.lib.descriptors5.webservices.String();
    serviceName.set_value(serviceName_helpStr);
    
    //gen targetNamespace
    if (targetNamespace.equals("")){
      targetNamespace = generateMemberValue(serviceAnotationRecord,"targetNamespace",neededGeneration);
    }
    if (xsdQnameSERVICE == null){
      xsdQnameSERVICE = new XsdQNameType();
    }
    xsdQnameSERVICE.set_value(new QName(targetNamespace, serviceName.get_value()));
    
    return xsdQnameSERVICE;
  }
  
  private XsdQNameType generateWsdlPortValue(PortComponentType portComponentTypeTmp,AnnotationRecord serviceAnotationRecord){
    
    XsdQNameType xsdQnamePORT = null;
    String portName = "", targetNamespace = "";
    boolean neededGeneration = false;
    
    //gen portName
    
    if (isDescriptorExists && portComponentTypeTmp.getWsdlPort() != null){
      xsdQnamePORT = portComponentTypeTmp.getWsdlPort();
      portName = xsdQnamePORT.get_value().getLocalPart().toString();
    }
    if(portName.equals("")) {
      portName = generateMemberValue(serviceAnotationRecord, "portName",neededGeneration);
    }

    //gen targetNamespace
    targetNamespace = generateMemberValue(serviceAnotationRecord,"targetNamespace",neededGeneration);
    if (xsdQnamePORT == null){
      xsdQnamePORT = new XsdQNameType();
    }

    xsdQnamePORT.set_value(new QName(targetNamespace, portName));
    
    return xsdQnamePORT;
  }

  private String targetNamespaceGeneration (String fqaClassName, NamedMember endpntInterfaceNameMember) {
    
    // get needed member from SEI annotaions
    String endPntInterface = "";
    if(endpntInterfaceNameMember != null){
		endPntInterface = endpntInterfaceNameMember.getName();
		
		if (!endPntInterface.equals("")) {
			NamedMember TNSnameMember = null;
			
			AnnotationRecord seiAnnotaions = wsSEIAnnotaions.get(endPntInterface);
			  
			if (seiAnnotaions != null) {
				TNSnameMember = seiAnnotaions.getMember("targetNamespace");
			}
			
			if (TNSnameMember != null && !TNSnameMember.getStringValue().equals("")) {
			      return TNSnameMember.getStringValue();
		    }
		 }
    }
       
    StringBuffer result = new StringBuffer();
    result.append("http://");
    String[] parts = fqaClassName.split("\\.");
    
    //-2 because: + -1 to remove class name/+ -1 array begins with 0 
    for (int i = parts.length - 2; i > -1; --i) {
      result.append(parts[i] + ".");
    }
    result.deleteCharAt(result.length() - 1);
    result.append("/");
    return result.toString();
  }

  private ServiceImplBeanType serviceImplBeanTypeGeneration(AnnotationRecord currAnnotaion, PortComponentType portComponentTypeTmp) {
    
    ServiceImplBeanType serviceImplBean;
    if (isDescriptorExists){
      serviceImplBean = portComponentTypeTmp.getServiceImplBean();
      if(serviceImplBean != null){
        if (serviceImplBean.getEjbLink() != null && serviceImplBean.getEjbLink().get_value() != null){
          return serviceImplBean;
        }
      }
    }
    serviceImplBean = new ServiceImplBeanType();
    String className = currAnnotaion.getOwner().getName();
    
    if(!isItWar){
      Map<String, AnnotationRecord> annotaionsTemp = currAnnotaion.getOwner().getAnnotations();
      NamedMember name = null;
      String nameStr = "";
      if (annotaionsTemp.containsKey("javax.ejb.Stateless")){
        name = annotaionsTemp.get("javax.ejb.Stateless").getMember("name");
      }else{
        if (annotaionsTemp.containsKey("javax.ejb.Stateful")){
          name = annotaionsTemp.get("javax.ejb.Stateful").getMember("name");
        }
      }
      if(name != null && !name.getStringValue().equals("")) {
        nameStr = name.getStringValue();
      }else{
        nameStr = className.substring(className.lastIndexOf(".")+ 1);
      }
      EjbLinkType ejbLinkType = new EjbLinkType();
      ejbLinkType.set_value(nameStr);
      serviceImplBean.setEjbLink(ejbLinkType);
      return serviceImplBean;
       
    }else{
      ServletLinkType servletLinkType = new ServletLinkType();
      String servletLinkValue = "";
      if (ServletChoiseHT != null && ServletChoiseHT.containsKey(className)){
        servletLinkValue = (String) ServletChoiseHT.get(className).getServlet().getServletName().get_value(); 

        if (servletLinkValue.equals("")){
          servletLinkValue = className; 
        }
      }else{
        servletLinkValue = className;
      }
      servletLinkType.set_value(servletLinkValue);
      serviceImplBean.setServletLink(servletLinkType);
      
      return serviceImplBean;
    }
  }

  private void fillServletChoiseHT() throws ConversionException{
    
    //filling hash table with choice groups
    if (ServletChoiseHT == null){
      ServletChoiseHT = new Hashtable();
    }else{
      ServletChoiseHT.clear();
    }
   
    WebProcessor5 webProcessor = WebProcessor5.getInstance();
    WebAppType parssedWebXml = null;
    InputStream webXmlInputStreamTmp = null;
    Choice1[] choice1Arr;
    JarEntry jarEntr_WEBxml;
    try{
      jarEntr_WEBxml = archiveJarFile.getJarEntry("WEB-INF/"+ WEB_XML);
      if (jarEntr_WEBxml == null){
        return;
      }
      webXmlInputStreamTmp = archiveJarFile.getInputStream(jarEntr_WEBxml);
      parssedWebXml = (WebAppType)webProcessor.parse(webXmlInputStreamTmp);
      
      if (parssedWebXml == null){
        return;
       }
      choice1Arr = parssedWebXml.getChoiceGroup1();
      ServletType servletType;
      String servletClassName = null;
      com.sap.engine.lib.descriptors5.javaee.FullyQualifiedClassType fullyQualifiedClassName; 
      for (int i = 0; i < choice1Arr.length; i++) {
        servletType = null;servletClassName = "";
        //
        if(((Choice1) choice1Arr[i]).getServlet() != null) {
          servletType = ((Choice1) choice1Arr[i]).getServlet();
          
          if (servletType == null) {
			continue;
          }
          
          fullyQualifiedClassName = servletType.getChoiceGroup1().getServletClass();
	      if(fullyQualifiedClassName != null) {
	        servletClassName = servletType.getChoiceGroup1().getServletClass().get_value();
	        if (servletClassName == null || servletClassName.equals("")) {
	          continue;
	        }          
	        ServletChoiseHT.put(servletClassName, (Choice1) choice1Arr[i]);
	      }
        }
      }//end loop
    } catch (IOException ioException) {
        throw new ConversionException("webservices_5303", new String[]{archiveJarFile.getName()}, ioException );
    } catch (SAXException saxException) {
        throw new ConversionException("webservices_5304", new String[]{archiveJarFile.getName()}, saxException );
    }
    finally{    	
      try{
        if (webXmlInputStreamTmp != null){
          webXmlInputStreamTmp.close();
        }
      }catch (IOException e) {
    	// $JL-EXC$
      }       
   }
 }
  
  private void loadModuleHandlers(ArrayList<AnnotationRecord> handlerChains)throws ConversionException{ 
   
    if (handlerChains.isEmpty()){
     return;
   }
   //
   InputStream handlerChainsDescriptorIn = null;
   ReferenceByteArrayOutputStream buf = null;
   String handlerChainsDescriptorRelPath,annotatedClassPackage,key;
   JarEntry handlerChainsDescriptorEntry;
   Transformer transformer = null;
   WebServicesProcessor5 webServicesProcessor = WebServicesProcessor5.getInstance();
   //
   try { 
     for(AnnotationRecord currHandlerChainsAnnotation : handlerChains){
       //
       handlerChainsDescriptorIn = null; buf = null; handlerChainsDescriptorEntry = null; 
       handlerChainsDescriptorRelPath = ""; annotatedClassPackage = "";key = "";transformer = null;
       //
       annotatedClassPackage = ((com.sap.lib.javalang.element.ClassInfo)currHandlerChainsAnnotation.getOwner()).getPackage();
       handlerChainsDescriptorRelPath = (new StringBuilder()).append(annotatedClassPackage.replace('.', '/'))
         .append("/").append(currHandlerChainsAnnotation.getMember("file").getStringValue()).toString();
       if(annotatedClassPackage.equals("") || handlerChainsDescriptorRelPath.equals("")){
         return;
       }
       if (isItWar) {
         handlerChainsDescriptorRelPath = (new StringBuilder()).append("WEB-INF/classes/").append(
              handlerChainsDescriptorRelPath).toString();
       }
       handlerChainsDescriptorEntry = archiveJarFile.getJarEntry(handlerChainsDescriptorRelPath);
       if (handlerChainsDescriptorEntry == null) {
         continue;
       }
       handlerChainsDescriptorIn = archiveJarFile.getInputStream(handlerChainsDescriptorEntry);
         // load descriptor to DOM
       Element rootEl = SharedDocumentBuilders.parse(SharedDocumentBuilders.NAMESPACE_AWARE_DB,handlerChainsDescriptorIn).getDocumentElement();
           // remove {http://java.sun.com/xml/ns/javaee}protocol-bindings elements, since it is union 
           // which is currently not supported by serialziation framework
       NodeList n_list = rootEl.getElementsByTagNameNS("http://java.sun.com/xml/ns/javaee", "protocol-bindings");
       
       for (int i = 0; i < n_list.getLength(); i++) {
         Element toBeRemoved = (Element) n_list.item(i);
         toBeRemoved.getParentNode().removeChild(toBeRemoved);
       }//end clear loop
 
       buf = new ReferenceByteArrayOutputStream();
       transformer = TransformerFactory.newInstance().newTransformer();
       transformer.transform(new DOMSource(rootEl), new StreamResult(buf));
       handlerChainsDescriptorIn = new ByteArrayInputStream(buf.getContentReference(), 0, buf.size());
       HandlerChainsType handlerChainsDescriptor = (HandlerChainsType) webServicesProcessor.parseHandlersDescriptor(handlerChainsDescriptorIn);
       key = annotatedClassPackage + "." + currHandlerChainsAnnotation.getMember("file").getStringValue();
       HandlersByPath.put(key, handlerChainsDescriptor);

     } //end loop - currHandlerAnnotaion
  } catch(Exception ex){
    throw new ConversionException("webservices_5302", new String[]{archiveJarFile.getName()}, ex );
  }finally {
    try {
      if (handlerChainsDescriptorIn != null) {
        handlerChainsDescriptorIn.close();
      }
    } catch (IOException e) {
    	// $JL-EXC$
    }
    
    try{
      if (buf != null)
        buf.close();
    }catch (IOException io){
    	// $JL-EXC$
    }
  } 
 }
  
  private PortComponentType setHandlers(AnnotationRecord annotaion, PortComponentType portComponent){
    
    boolean changeOfPortComponent = false;//setNewHandlers -> changes portComponent
    
    if(!isDescriptorExists){
      setNewHandlers(annotaion, portComponent);
      return portComponent;
    }
    
    if(portComponent.getChoiceGroup1() == null){
      setNewHandlers(annotaion, portComponent);
      return portComponent;
    }
    
    HandlerChainType[] descriptorHandlers = portComponent.getChoiceGroup1().getHandlerChains().getHandlerChain();
    if(descriptorHandlers.length == 0 || descriptorHandlers == null){
      setNewHandlers(annotaion, portComponent);
      return portComponent;    
    }

    return portComponent;
  }
  
  private PortComponentType setNewHandlers(AnnotationRecord annotaion, PortComponentType portComponent){
    
    String handlerChainsPath = "";
    ClassInfo helpClassInfo = (ClassInfo)annotaion.getOwner();
    AnnotationRecord handlerChainsAnnotations = helpClassInfo.getAnnotation("javax.jws.HandlerChain");;
    
    if (handlerChainsAnnotations == null){
      helpClassInfo = null; handlerChainsAnnotations = null;
      //
      NamedMember endpointInterfnameMemb = annotaion.getMember("endpointInterface");
      
      if(endpointInterfnameMemb == null || endpointInterfnameMemb.getStringValue() == null){
        return portComponent;
      }
      AnnotationRecord seiAnnotaions = null;
      
      seiAnnotaions = wsSEIAnnotaions.get(endpointInterfnameMemb.getStringValue());
    
      if (seiAnnotaions != null ) {
    	helpClassInfo = (ClassInfo)seiAnnotaions.getOwner();
    	handlerChainsAnnotations = helpClassInfo.getAnnotation("javax.jws.HandlerChain");
      }
    }
    
    if(handlerChainsAnnotations == null || helpClassInfo == null){
      return portComponent;
    }
    handlerChainsPath = helpClassInfo.getPackage()+"."+handlerChainsAnnotations.getMember("file").getStringValue();
    HandlerChainsType handlerChainsDescriptor = null;
    
   if(handlerChainsPath != null){
	   handlerChainsDescriptor = HandlersByPath.get(handlerChainsPath);
   }
     
    
    if(handlerChainsDescriptor == null){
      return portComponent;
    }
    
    HandlerChainType[] hcs = handlerChainsDescriptor.getHandlerChain();
    ArrayList<HandlerChainType> filtered = new ArrayList<HandlerChainType>();// contains chains that
                                                                             //  are relevant for the current port.
    for (HandlerChainType type : hcs) {
      filtered.add(type);
      if (type.getChoiceGroup1() != null) {
        if (type.getChoiceGroup1().getPortNamePattern() != null) {
          String v = type.getChoiceGroup1().getPortNamePattern();
          String pLName = portComponent.getWsdlPort().get_value().getLocalPart();
          if (v.indexOf(pLName) == -1) { // chain dedicated for another port
            filtered.remove(filtered.size() - 1);
          }
        }
      }
   }
    
    handlerChainsDescriptor.setHandlerChain(filtered.toArray(new HandlerChainType[filtered.size()]));
    PortComponentType.Choice1 handlerChainsDescriptor2 = new PortComponentType.Choice1();
    handlerChainsDescriptor2.setHandlerChains(handlerChainsDescriptor);
    portComponent.setChoiceGroup1(handlerChainsDescriptor2);

   return portComponent;
 }
  
  private void loadExistingDescriptor()throws ConversionException{
    
    JarEntry existingDescriptorEntry = null;
    InputStream existingDescriptorInputStream = null;
    String neededDir = "";
    if(isItWar){
      neededDir = "WEB-INF/";
    }else{
      neededDir = "META-INF/";
    }
    //
    try{
      existingDescriptorEntry = archiveJarFile.getJarEntry(neededDir + WEBSERVICES_XML);
      if (existingDescriptorEntry == null){
        return;
      }
      existingDescriptorInputStream = archiveJarFile.getInputStream(existingDescriptorEntry);
      WebServicesProcessor5 serviceProcessor = WebServicesProcessor5.getInstance();
      WebservicesType webServiceType = (WebservicesType) serviceProcessor.parse(existingDescriptorInputStream);
      WebserviceDescriptionType[] existingDescriptorDescTypeArr = webServiceType.getWebserviceDescription();

      if ( existingDescriptorDescTypeArr == null || existingDescriptorDescTypeArr.length == 0){
        return;
      }
      //
      String portCompName = "", wsDescriptionName = "";
      PortComponentType[] portComponentTypeArray;
      //
      for (WebserviceDescriptionType currDescTypeExistingDescriptor: existingDescriptorDescTypeArr){
        portCompName = ""; wsDescriptionName = ""; portComponentTypeArray = null;
        //
        if(currDescTypeExistingDescriptor == null){
        	continue;
        }
        
        portComponentTypeArray = currDescTypeExistingDescriptor.getPortComponent();
        if (portComponentTypeArray == null || portComponentTypeArray.length == 0 ){
          continue;
        }
        
        wsDescriptionName = currDescTypeExistingDescriptor.getWebserviceDescriptionName().get_value();
        for (PortComponentType currPort:portComponentTypeArray){
          portCompName = "" ; 
          if (currPort == null){
              continue;
          }
            
          portCompName = currPort.getPortComponentName().get_value().toString();
          if (portCompName.equals("")) {
            continue;
          }
          
          existingDescriptor.put(portCompName, currPort);
          lunchExistingDescrRestElements.put(portCompName , wsDescriptionName);
          
        }//end inner loop
      }//end outer loop
      
      isDescriptorExists = true;
      
    }catch (IOException e) {
      throw new ConversionException("webservices_5305", new String[]{archiveJarFile.getName()}, e );
    }catch (SAXException e) {
      throw new ConversionException("webservices_5306", new String[]{archiveJarFile.getName()}, e );
	}
    finally{
      try {
        if (existingDescriptorInputStream != null) {
          existingDescriptorInputStream.close();
        }
      } catch (IOException io) {
        // $JL-EXC$
      }
    }
  }
  

  private PortComponentType genPortCompWithExistingValue(String keyPortComponentName){
	  
    PortComponentType portCompExistingDescr = new PortComponentType();
    if (keyPortComponentName == null || !existingDescriptor.containsKey(keyPortComponentName)) {
	  return portCompExistingDescr;
	}
    portCompExistingDescr = existingDescriptor.get(keyPortComponentName);
    existingDescriptor.remove(keyPortComponentName);
    return portCompExistingDescr;      
    
  }
  
  private PortComponentType[] loadfromExisitngDescriptorRemain(String keyService){
    
    //lunchExistingDescrRestElements  => <portComponentNameSTR,webserviceDescriptionNameSTR>
    //existingDescriptor  => <portComponentNameSTR,portComponentType>
    
    Enumeration e_existDescrRestEl = lunchExistingDescrRestElements.keys();
    String wsDescriptionName = "" , portCompName = "";
    ArrayList<PortComponentType> portComponentTypeTMP_AL = new ArrayList();
    int countLength = 0;
    if (!lunchExistingDescrRestElements.containsValue(keyService)){
      //if there is no such webservice description name
      return null;///!!!!!
    }
    while (e_existDescrRestEl.hasMoreElements()){
      wsDescriptionName = "";portCompName = "";
      wsDescriptionName = lunchExistingDescrRestElements.get(e_existDescrRestEl.nextElement());
      portCompName = (String)e_existDescrRestEl.nextElement();
      if (keyService.equals(wsDescriptionName)) {
        if(existingDescriptor.containsKey(portCompName)){
          portComponentTypeTMP_AL.add(existingDescriptor.get(portCompName));
          existingDescriptor.remove(portCompName);
          lunchExistingDescrRestElements.remove(portCompName);
          countLength =+ 1;
        }
      }
    }//end loop
  
    PortComponentType[] portComponentType = new PortComponentType[countLength];
    
    return portComponentType;
  }

  private boolean clearAndInitialVARs(FileInfo file) throws ConversionException{
    
    jarName = ""; jarForWork = ""; boolean continuueLoop = false;
    isDescriptorExists = false; isTempJarExists = false;
    
    if (file.getClassLevelAnnotations() == null || file.getClassLevelAnnotations().isEmpty()){
      continuueLoop = true;
      return continuueLoop;
    }
    annotationMap = file.getClassLevelAnnotations();
    if (annotationMap.isEmpty() || annotationMap == null){
      continuueLoop = true;
      return continuueLoop;
    }
    if (!annotationMap.containsKey("javax.jws.WebService") && !annotationMap.containsKey("javax.xml.ws.WebServiceProvider")){
      continuueLoop = true;
      return continuueLoop;
    }
    if (!wsSEIAnnotaions.isEmpty()){
      wsSEIAnnotaions = new Hashtable();
    }
    ServletChoiseHT = null;
    if (wsAnnotations != null) {
      wsAnnotations = null;
    }
    if (!wsAnnotationsPerArch.isEmpty()) {
      wsAnnotationsPerArch = new Hashtable();
    }
    
    if (!HandlersByPath.isEmpty()){
      HandlersByPath = new Hashtable();
    }
     if (!existingDescriptor.isEmpty()){
       existingDescriptor.clear();
     }
     if(!lunchExistingDescrRestElements.isEmpty()){
       lunchExistingDescrRestElements.clear();
     }
    jarName = file.getFullPath().replace(tmpPath + File.separator, "");
    if (jarName.contains("extracted")){
      jarName = jarName.replace("extracted.", "");
    }
    
    if (jarName.endsWith(".war")){
      isItWar = true;
    }else{
      isItWar = false;
    }
    if (archiveJarFile != null){
      archiveJarFile = null;
    }
    
    try{
      jarForWork = IOUtil.getFileNameWithoutExt(jarName) + "_ws" + IOUtil.getFileExtension(jarName);
      if (new File(tmpPath,jarForWork).exists()){
        archiveJarFile = new JarFile(new File(tmpPath, jarForWork));
        isTempJarExists = true;
      }else{
        archiveJarFile = new JarFile(new File(tmpPath, jarName));
      }
        
    }catch (IOException e) {
      try{
        if(archiveJarFile != null){
          archiveJarFile.close();
        }
      }catch (IOException io) {
    	// $JL-EXC$
      }
     
      continuueLoop = true;
      throw new ConversionException("webservices_5300", new String[]{jarForWork, jarName}, e );
      
    }
   return continuueLoop;
   
 }
  
 private boolean checkArhive(FileInfo file) {
    
    String archiveName = file.getName();
    boolean continueLoop = false;
    if (!isItWar && !jarName.endsWith(".jar")) {
      continueLoop = true;
      return continueLoop;
    }
    if (archiveName.contains("extracted")){
      archiveName = archiveName.replace("extracted.", "");
    }
      if (!jarName.equals(archiveName)) {
        continueLoop = true;
      }
      return continueLoop;
  }
 
 private void closeJar(){
    try{
      if(archiveJarFile != null){
        archiveJarFile.close();
      } 
    }catch (IOException e) {
    	// $JL-EXC$
    }
  }
 
 private boolean checkArchive0(String srcFileName, String destFileName) {   
      
   if(destFileName.endsWith(".jar")) {
     return srcFileName.equals(destFileName);     
   }
   
   if(destFileName.endsWith(".war")) {             
     String destFileNameTmp = destFileName.substring(0, destFileName.lastIndexOf(".")) + ".extracted.war";      
     return srcFileName.equals(destFileNameTmp); 
   }  
   return false;    
 }

 /**
  * 
  * Build Jar from given Jar file adding deploy descriptors
  * @param descriptorObj descriptor object to be serialized 
  * @param webServiceDescriptor - name web service client deploy descriptor (web.xml, ejb-jar.xml or application-client.xml) 
  * @param tmpPath - path to source war/jar archives location
  * @param moduleRelativeFileUri - name of source jar/war archive
  * @param newArchiveFileName - name of new built jar/war archive
  * @throws IOException 
  * 
  */
 private void buildJar(WebservicesType descriptorObj, String webServiceDescriptor, String tmpPath, String moduleRelativeFileUri,  String newArchiveFileName) throws IOException{
	 String webServicesXmlDir = null;
	 JarFile srcArchiveJarFile = null;
	 File archiveFileNew = null;
	 FileOutputStream out = null;
	 JarOutputStream jarOut = null;
	 
	 WebServicesProcessor5 serviceProcessor = WebServicesProcessor5.getInstance();
	 
	 try {
		 if(isItWar) {
			 webServicesXmlDir = WEB_INF;
		 } else {
			 webServicesXmlDir = META_INF;
		 }
		 srcArchiveJarFile = new JarFile(tmpPath + File.separator + moduleRelativeFileUri);
		 archiveFileNew = new File(tmpPath + File.separator + newArchiveFileName);
         out = new FileOutputStream(archiveFileNew); 
         jarOut = new JarOutputStream(out);
         
         JarUtil.copyEntries(srcArchiveJarFile, new String[0], new String[]{webServicesXmlDir + "/" + webServiceDescriptor}, new String[0],new String[]{},  false, jarOut);         
		 try { 
			  jarOut.putNextEntry(new JarEntry(webServicesXmlDir + "/" + webServiceDescriptor));
			  serviceProcessor.build(descriptorObj, jarOut);	  	 
			} finally {
			  try {
		        jarOut.closeEntry();  
		      } catch(Exception e) {
		        // $JL-EXC$		
		      }	  
		    } 
    } finally {
      try {
        if(srcArchiveJarFile != null) {
        	srcArchiveJarFile.close();	
        }  
      } catch(Exception e) {
        // $JL-EXC$	   
      }     
      try {
        if(jarOut != null) {
        	jarOut.close(); 	
        }  
      } catch(Exception e) {
        // $JL-EXC$	   
      }
      try {
        if(out != null) {
          out.close();	
        }  
      } catch(Exception e) {
        // $JL-EXC$	   
      }  
      
    } 
 }
 
 	/**
	 * Get target namespace
	 * 
	 * @param fullyQualifiedAnnClassName
	 *            - fully qualified class name
	 * 
	 * @return result - return target namespace
	 */
	public static String getTargetNamespace(String fullyQualifiedAnnClassName) {
		StringBuffer result = new StringBuffer();
		result.append("http://");
		String[] parts = fullyQualifiedAnnClassName.split("\\.");
		for (int i = parts.length - 1; i > -1; --i) {
			result.append(parts[i] + ".");
		}
		result.deleteCharAt(result.length() - 1);
		result.append("/");
		return result.toString();
	}
 
	public static String getPackage(String className) {
		int cutIndex = className.lastIndexOf(".");
		if (cutIndex == -1) {
			return "";
		} else {
			return className.substring(0, cutIndex);
		}
	}
}
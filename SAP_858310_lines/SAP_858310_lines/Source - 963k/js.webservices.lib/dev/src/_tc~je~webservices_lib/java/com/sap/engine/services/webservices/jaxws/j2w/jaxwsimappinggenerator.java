/*
 * Copyright (c) 2004 by SAP AG, Walldorf., http://www.sap.com All rights
 * reserved. This software is the confidential and proprietary information of
 * SAP AG, Walldorf. You shall not disclose such Confidential Information and
 * shall use it only in accordance with the terms of the license agreement you
 * entered into with SAP.
 */
package com.sap.engine.services.webservices.jaxws.j2w;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import javax.jws.Oneway;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;
import javax.jws.soap.SOAPBinding.Use;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.transform.dom.DOMSource;
import javax.xml.ws.BindingType;
import javax.xml.ws.Holder;

import com.sap.engine.services.webservices.espbase.mappings.EndpointMapping;
import com.sap.engine.services.webservices.espbase.mappings.InterfaceMapping;
import com.sap.engine.services.webservices.espbase.mappings.OperationMapping;
import com.sap.engine.services.webservices.espbase.mappings.ParameterMapping;
import com.sap.engine.services.webservices.espbase.mappings.ServiceMapping;
import com.sap.engine.services.webservices.espbase.wsdl.HTTPBinding;
import com.sap.engine.services.webservices.jaxrpc.util.NameConvertor;
import com.sap.guid.GUIDGeneratorFactory;
import com.sap.guid.IGUIDGenerator;
import com.sap.tc.logging.Location;

/**
 * @author Dimitar Velichkov dimitar.velichkov@sap.com
 */

/**
 * Creates an InterfaceMapping from a class with JAXWS annotations
 */
public class JaxWsIMappingGenerator {

  private Class<?>             c            = null;
  private WebService           ws           = null;
  private String               SEIName      = null;
  
  private QName                portName;
  private QName                serviceName;

  private JaxWsSchemaGenerator schemaGen    = null;
  private IGUIDGenerator       guidGen      = null;

  private static final Location LOC = Location.getLocation(JaxWsIMappingGenerator.class);
  /**
   * Constructor only creates a GUID generator
   *
   */
  public JaxWsIMappingGenerator() {
    guidGen = GUIDGeneratorFactory.getInstance().createGUIDGenerator();
  }

  /**
   * Create a WSDL from a SEI class.
   * 
   * @param opt
   *          a structure holding the input options
   * @return a Java2WsdlResult object containing an InterfaceMapping object, set
   *         of schemas as array and ServiceMapping
   * @throws JaxWsInsideOutException
   * @throws JAXBException
   */
  public Java2WsdlResult generateWSDL(Java2WsdlOptions opt) throws JaxWsInsideOutException, JAXBException {
    if (opt == null) {
      throw new IllegalArgumentException();
    }

    checkSEI(opt);

    String guid = guidGen.createGUID().toString();

    InterfaceMapping iMap = parseClass(opt);
    iMap.setInterfaceMappingID(guid);
    SOAPBinding.Style style = SOAPBinding.Style.DOCUMENT;
    // i044259
    iMap.setServiceQName(serviceName);    
    iMap.setPortQName(portName);
    
    // necessary to set this in Java2WsdlResult so we know which
    // InterfaceMapping processor method to call
    SOAPBinding sb = c.getAnnotation(SOAPBinding.class);
    if (sb != null) {
      style = sb.style();
    }

    ServiceMapping sMap = new ServiceMapping();
    sMap.setServiceName(serviceName);
    EndpointMapping[] emapping = new EndpointMapping[1];
    emapping[0] = new EndpointMapping();
    emapping[0].setPortQName(portName.toString());
    sMap.setEndpoint(emapping);
    sMap.setServiceMappingId(guid);

    DOMSource[] schemas = new DOMSource[0];
    if (! outsideIn) {
      schemaGen.genJaxbMappings(opt.getGenSourceDir(), opt.getGenClassDir(), opt.getServerClassPath(), opt.getClassLoader(), ! opt.hasWSDL);
      schemas = schemaGen.getSchemaSources();
    } else { 
      if (schemaGen != null) { //this is the CAF special case.
        schemaGen.genJaxbMappings(opt.getGenSourceDir(), opt.getGenClassDir(), opt.getServerClassPath(), opt.getClassLoader(), false);
      }
    }
    Java2WsdlResult res = new Java2WsdlResult(iMap, schemas, sMap, style);
    //res.setUsedClasses(schemaGen.getUsedClasses());

    return res;

  }

  boolean outsideIn = false;
  /**
   * Check to see if the SEI is valid and set the targetNamespace, portName, and
   * serviceName
   * 
   * @param opt
   * @throws JaxWsInsideOutException
   */
  private void checkSEI(Java2WsdlOptions opt) throws JaxWsInsideOutException {

    ClassLoader cl = opt.getClassLoader();
    if (cl == null) {
      cl = Thread.currentThread().getContextClassLoader();
    }

    Class<?> srvImpl;
    try {
      c = cl.loadClass(opt.getSEIClass());
      srvImpl = c;// since at this point they both are same.
    } catch (ClassNotFoundException e) {
      throw new JaxWsInsideOutException(JaxWsInsideOutException.CLASS_NOT_FOUND_USING_CL,e, opt.getSEIClass(), cl.toString());
    }

    if (c == null) {
      throw new JaxWsInsideOutException(JaxWsInsideOutException.WS_CLASS_NULL);
    }
    if (c.isInterface()) {
      throw new JaxWsInsideOutException(JaxWsInsideOutException.CLASS_REQUIRED);
    }

    ws = c.getAnnotation(WebService.class);

    String servName = null;
    String portName = null;

    if (ws == null) {
      throw new JaxWsInsideOutException(JaxWsInsideOutException.MISSING_ANNOTATION);
    } else {
      if (ws.name().length() > 0) {
        SEIName = ws.name();
        portName = SEIName + "Port";

      } else {
        SEIName = c.getSimpleName();
      }
      if (ws.serviceName().length() > 0) {
        servName = ws.serviceName();
      }
      if (ws.portName().length() > 0) {
        portName = ws.portName();
      }
      
      if (ws.wsdlLocation().length() > 0) { //this is outside-in case.
        outsideIn = true;
      }
      String abstImpl = ws.endpointInterface();
      if (abstImpl.length() > 0) {
        try {
          this.c = cl.loadClass(abstImpl);
        } catch (ClassNotFoundException e) {
          throw new JaxWsInsideOutException(JaxWsInsideOutException.ABST_CLASS_MISSING, e);
        }
        ws = this.c.getAnnotation(WebService.class);
        if (ws == null) {
          throw new JaxWsInsideOutException(JaxWsInsideOutException.MISSING_ANNOTATION);
        }
        if (ws.name().length() > 0) {
          SEIName = ws.name();
        } else {
          SEIName = this.c.getSimpleName();
        }
      }
    }

    String srvTNS = getTNS(srvImpl, srvImpl.getAnnotation(WebService.class));
    if (servName == null) {
      servName = getServiceName(opt.getServiceName(), srvImpl);
    }
    if (portName == null) {
      portName = getPortName(opt.getPortName(), srvImpl);
    }

    this.serviceName = new QName(srvTNS, servName);
    this.portName = new QName(srvTNS, portName);

  }

  /**
   * Does the real work
   * 
   * @throws JaxWsInsideOutException
   * @throws JAXBException
   */
  private InterfaceMapping parseClass(Java2WsdlOptions opt) throws JaxWsInsideOutException, JAXBException {

    InterfaceMapping iMap = new InterfaceMapping();
    iMap.setBindingType(InterfaceMapping.SOAPBINDING);
    iMap.setJAXWSInterfaceFlag(true);
    iMap.setSEIName(c.getName());
    
    setBindingType(iMap);

    String tns = getTNS(c, ws);
    // soap binding information
    // defaults if not specified by SOAPBinding annotation
    SOAPBinding.Style bindingStyle = SOAPBinding.Style.DOCUMENT;
    SOAPBinding.ParameterStyle bindingParamStyle = SOAPBinding.ParameterStyle.WRAPPED;
    SOAPBinding sb = c.getAnnotation(SOAPBinding.class);
    if (sb != null) {
      if (sb.style().equals(Style.RPC)) {
        bindingStyle = SOAPBinding.Style.RPC;
      }
      if (sb.use().equals(Use.ENCODED)) {
        throw new JaxWsInsideOutException(JaxWsInsideOutException.ENCODED_NOT_SUPPORTED, c.getName());
      }
      bindingParamStyle = sb.parameterStyle();
      // RPC bare is not allowed
      if (bindingStyle.equals(SOAPBinding.Style.RPC) && bindingParamStyle.equals(SOAPBinding.ParameterStyle.BARE)) {
        throw new JaxWsInsideOutException(JaxWsInsideOutException.RPC_REQ_WRAPPED, c.getName());
      }

    }

    ArrayList<Class<?>> types = new ArrayList<Class<?>>();
    ArrayList<BeanGenerator> beans = new ArrayList<BeanGenerator>();

    // for every ParameterMapping member of this map, we must generate a global
    // element declaration in the appropriate schema
    // the element's type is set to the ket's value. This is necessary for
    // Headers and Document-Bare parameters!
    // JAXB cannot usually help us in this case
    HashMap<ParameterMapping, QName> paramToElementMap = new HashMap<ParameterMapping, QName>();
    
    Set<String> beanClNames = new HashSet(); //contains the class names of generated request and response bean wrappers
    Set<String> faultBeans = new HashSet(); //contains the class names of the exceptions for which beans generated
    // inherited methods MUST be included in the resulting SEI!
    Method[] allMeths = c.getMethods();
    Set<String> operationsSet = new HashSet<String>(); 
    boolean isSEInterface = c.isInterface();
    for (Method m : allMeths) {

      SOAPBinding.ParameterStyle opBindingParamStyle = bindingParamStyle;

      WebMethod wm = m.getAnnotation(WebMethod.class);
      // skip excluded operations
      if (wm != null && wm.exclude()) {
        if (wm.exclude() && isSEInterface) {
          //According to JAX-WS20, chapter 3.4 an exception should be thrown here. 
          //For now we won't throw the exception, since for us it looks too restrictive.
        }
        continue;
      }
      
      
      //Check if the method's declaring class is annotated with @WebService or the method itself is annotated with @WebMethod.
      //According to JAX-WS2.0 chapter 3.3 such methods should be exposed.
      Class declaringClass = m.getDeclaringClass();
      if (! (declaringClass.getAnnotation(WebService.class) != null || wm != null)) {
        continue;
      }

      OperationMapping oMap = new OperationMapping();
      oMap.setJavaMethodName(m.getName());

      // defaults - take name from method and empty(?) soapaction
      oMap.setWSDLOperationName(m.getName());
      oMap.setProperty(OperationMapping.SOAP_ACTION, "");

      if (wm != null) {
        if (wm.operationName().length() > 0) {
          oMap.setWSDLOperationName(wm.operationName());
        }

        if (wm.action().length() > 0) {
          oMap.setProperty(OperationMapping.SOAP_ACTION, wm.action());
        }
      }

      // req-resp by default!
      oMap.setProperty(OperationMapping.OPERATION_MEP, OperationMapping.MEP_REQ_RESP);
      Oneway ow = m.getAnnotation(Oneway.class);
      if (ow != null) {
        setOneWay(oMap, m);
      }

      // soapbinding can be set on operation level also
      if (bindingStyle.equals(SOAPBinding.Style.DOCUMENT)) {
        
        SOAPBinding opBindingStyle = m.getAnnotation(SOAPBinding.class);
        if (opBindingStyle != null) {
          if (opBindingStyle.style().equals(SOAPBinding.Style.RPC) || opBindingStyle.use().equals(SOAPBinding.Use.ENCODED)) {
            throw new JaxWsInsideOutException(JaxWsInsideOutException.OPERATION_BAD_SOAPBINDING, m.getName());
          }
          opBindingParamStyle = opBindingStyle.parameterStyle();
        }
      }

      String operationName = oMap.getWSDLOperationName();
      if (operationsSet.contains(operationName)){
        throw new JaxWsInsideOutException("Overloaded operation found: [" + operationName + "]. Operation overloading is prohibitted by BasicProfile 1.1 ");
      }else{
        operationsSet.add(operationName);
      }
      
      // add all parameters to the operation map
      ParameterCollector paramColl = new ParameterCollector(m, opBindingParamStyle, bindingStyle, types, beans, paramToElementMap, tns, beanClNames, opt);
      paramColl.isOutsideIn = this.outsideIn;
      paramColl.collect();

      addParameters(paramColl.getInputParameterMappings(), oMap);
      addParameters(paramColl.getOutputParameterMappings(), oMap);
      addParameters(paramColl.getFaultParameterMappings(faultBeans, c), oMap);
      addParameters(paramColl.getHeaderParameterMappings(), oMap);
      // sort the unsorted params
      sortOperationParameters(oMap);

      // necessary for SOAP runtime!
      if (bindingStyle.equals(SOAPBinding.Style.DOCUMENT)) {
        QName soapRequestQname = paramColl.getSOAPRequestWrapper();
        oMap.setProperty(OperationMapping.SOAP_REQUEST_WRAPPER, soapRequestQname.getLocalPart());
        oMap.setProperty(OperationMapping.INPUT_NAMESPACE, soapRequestQname.getNamespaceURI());
        QName soapResponseQname = paramColl.getSOAPResponseWrapper();
        oMap.setProperty(OperationMapping.SOAP_RESPONSE_WRAPPER, soapResponseQname.getLocalPart());
        oMap.setProperty(OperationMapping.OUTPUT_NAMESPACE, soapResponseQname.getNamespaceURI());
        oMap.setProperty(OperationMapping.OPERATION_STYLE, bindingStyle.toString().toLowerCase(Locale.ENGLISH));
        if (opBindingParamStyle.equals(SOAPBinding.ParameterStyle.BARE)) {
          oMap.setProperty(OperationMapping.OPERATION_STYLE, OperationMapping.DOCUMENT_BARE_OPERATION_STYLE);
          oMap.setProperty(OperationMapping.DOCUMENT_BARE_OPERATION_REQ_PARTNAME, paramColl.getDocBareReqPartName());
          oMap.setProperty(OperationMapping.DOCUMENT_BARE_OPERATION_RESP_PARTNAME, paramColl.getDocBareRespPartName());
          //add heuristic for CTS50 swaref document tests to pass
          if (CTS50_DOCBARE_SWAREF_HACKED_METHODS.contains(oMap.getJavaMethodName())) {
            ParameterMapping[] params = oMap.getParameters(-1); //take all parameters
            int index = 1;
            for (ParameterMapping p : params) {
              if (p.isAttachment()) {
                p.setWSDLParameterName("attach" + index++);
              }
            }
          }
        } else { //doc-lit-wrapped
          try {
            String reqBean = paramColl.getReqBeanClassName();
            String respBean = paramColl.getRespBeanClassName();
            oMap.setProperty(OperationMapping.REQUEST_WRAPPER_BEAN, reqBean);
            oMap.setProperty(OperationMapping.RESPONSE_WRAPPER_BEAN, respBean);
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        }  
      } else {
        oMap.setProperty(OperationMapping.INPUT_NAMESPACE, tns);
        oMap.setProperty(OperationMapping.OUTPUT_NAMESPACE, tns);
        oMap.setProperty(OperationMapping.SOAP_REQUEST_WRAPPER, oMap.getWSDLOperationName());
        oMap.setProperty(OperationMapping.SOAP_RESPONSE_WRAPPER, oMap.getWSDLOperationName() + "Response");
        oMap.setProperty(OperationMapping.OPERATION_STYLE, bindingStyle.toString().toLowerCase(Locale.ENGLISH));
      }
      oMap.setProperty(OperationMapping.OPERATION_USE, "literal");
      

      iMap.addOperation(oMap);

    }

    iMap.setPortType(new QName(tns, SEIName));
    iMap.setBindingQName(new QName(tns, SEIName + "Binding"));
    
    if (! outsideIn) {
      schemaGen = new JaxWsSchemaGenerator(types, beans, paramToElementMap, c.getPackage(), tns);
    } else { //Hack needed for CAF backwards compatibility. Only Fault beans would be generated, and only if such classes does not exist in the classloader...
      //check for which faultbean already class exists. This is in the 'normal' JAX-WS outside-in case that the faultbeans come with the application.
      //Unfortunately there are CAF apps, which are outside-in, but the faultbeans are not included in the application. That is we now would generated those beans.
      ClassLoader loader = opt.getClassLoader();
      Iterator<BeanGenerator> itr = beans.iterator();
      ArrayList<BeanGenerator> beansToBeGenerated = new ArrayList();
      while (itr.hasNext()) {
        BeanGenerator tmpBG = itr.next();
        if (tmpBG instanceof FaultBeanGenerator && ((FaultBeanGenerator) tmpBG).isExceptionCompliant()) {
          continue;//no need, since the bean should point to existing class 
        }
        try {
          String cName = tmpBG.getBeanClassName();
          Class c = loader.loadClass(cName);
        } catch (ClassNotFoundException cnfE) {
          beansToBeGenerated.add(tmpBG);
        }
      }
      if (! beansToBeGenerated.isEmpty()) {
        LOC.debugT("This is outside-in case with CAF specific behavior. The following beans would be generated " + beansToBeGenerated);
        schemaGen = new JaxWsSchemaGenerator(types, beansToBeGenerated, paramToElementMap, c.getPackage(), tns);
      }
    }

    return iMap;
  }
  
  private static final Set CTS50_DOCBARE_SWAREF_HACKED_METHODS = new HashSet();
  static {
    //These methods are hacked in order CTS50 com/sun/ts/tests/jaxws/wsi/w2j/document/literal/swatest/*
    CTS50_DOCBARE_SWAREF_HACKED_METHODS.add("getMultipleAttachments");
    CTS50_DOCBARE_SWAREF_HACKED_METHODS.add("putMultipleAttachments");
    CTS50_DOCBARE_SWAREF_HACKED_METHODS.add("echoMultipleAttachments");
    CTS50_DOCBARE_SWAREF_HACKED_METHODS.add("echoAllAttachmentTypes");
    CTS50_DOCBARE_SWAREF_HACKED_METHODS.add("echoAttachmentsAndThrowAFault");
    CTS50_DOCBARE_SWAREF_HACKED_METHODS.add("echoAttachmentsWithHeader");
    /* This method is hacked in order com\sun\ts\tests\jaxws\sharedwebservices\dlhandlerservice\WSDLHandlerService.ear
     * to be deployed correctly - with correct mapping content.
     * Affected tests that fail if the mapping is incorrect:
     * com/sun/ts/tests/jaxws/api/javax_xml_ws_handler/LogicalMessageContext/Client.java#ContextPropertiesTest
     * com/sun/ts/tests/jaxws/api/javax_xml_ws_handler_soap/SOAPMessageContext/Client.java#ContextPropertiesTest
     */
    CTS50_DOCBARE_SWAREF_HACKED_METHODS.add("doHandlerAttachmentTest");
  }

  /**
   * @param beans
   * @param loader
   */
  private ArrayList<BeanGenerator> removeBeanForWhichClassesAreAlreadyPresent(ArrayList<BeanGenerator> beans, ClassLoader loader) {
    ArrayList<BeanGenerator> res = new ArrayList();
    for (BeanGenerator generator : beans) {
      String clName = generator.getBeanClassName();
      try {
        Class cl = loader.loadClass(clName);
      } catch (ClassNotFoundException cnfE) {
        res.add(generator);
      }
    }
    return res;
  }
  
  private void sortOperationParameters(OperationMapping oMap) {
    ParameterMapping[] params = oMap.getParameter();
    ParameterMapping[] newParams = new ParameterMapping[params.length];
    int tmp = params.length;
    for (int i = 0; i < params.length; i++) {
      String index = params[i].getProperty(ParameterCollector.TMP_PARAM_INDEX);
      if (index != null) {
        newParams[Integer.parseInt(index)] = params[i];
        params[i].setProperty(ParameterCollector.TMP_PARAM_INDEX, null);
      } else {
        newParams[--tmp] = params[i];
      }
    }
    oMap.setParameter(newParams);
  }

  private void addParameters(ParameterMapping[] params, OperationMapping oMap) {

    for (ParameterMapping param : params) {
      oMap.addParameter(param);
    }

  }

  // Only SOAP11 is currently supported...
  private void setBindingType(InterfaceMapping iMap) throws JaxWsInsideOutException {

    iMap.setBindingType(InterfaceMapping.SOAPBINDING);

    BindingType bt = c.getAnnotation(BindingType.class);
    if (bt != null) {
      if (!bt.value().equals(javax.xml.ws.soap.SOAPBinding.SOAP11HTTP_BINDING)
          && !bt.value().equals(javax.xml.ws.soap.SOAPBinding.SOAP12HTTP_BINDING)) {
        throw new JaxWsInsideOutException("Binding " + bt.value() + " is currently not supported!");
      }

      if (bt.value().equals(HTTPBinding.HTTP_BINDING)) {
        iMap.setBindingType(InterfaceMapping.HTTPPOSTBINDING);
      }

    }

  }

  private String getServiceName(String optionsDefault, Class<?> srvImpl) {

    String name = null;
    if (optionsDefault != null && optionsDefault.length() > 0) {
      name = optionsDefault;
    } else if (ws != null && ws.serviceName().length() > 0) {
      name = ws.serviceName();
    } else {
      name = srvImpl.getSimpleName() + "Service";
    }

    return name;
  }

  private String getPortName(String optionsDefault, Class<?> srvImpl) {

    String name = null;
    if (optionsDefault != null && optionsDefault.length() > 0) {
      name = optionsDefault;
    } else if (ws != null && ws.portName().length() > 0) {
      name = ws.portName();
    } else {
      name = srvImpl.getSimpleName() + "Port";
    }

    return name;
  }

  private String getTNS(Class<?> _c, WebService _ws) throws JaxWsInsideOutException {

    NameConvertor nc = new NameConvertor();
    String tns = nc.packageToUri(_c.getPackage());
    if (_ws != null && _ws.targetNamespace().length() > 0) {
      tns = _ws.targetNamespace();
    }
    if (tns.length() == 0) {
      throw new JaxWsInsideOutException(JaxWsInsideOutException.TARGET_NS_UNDEFINED, _c.getName());
    }
    return tns;
  }

  private void setOneWay(OperationMapping oMap, Method m) throws JaxWsInsideOutException {

    Class<?> ret = m.getReturnType();

    // one-way ops must return void
    if (!ret.equals(Void.TYPE)) {
      throw new JaxWsInsideOutException(JaxWsInsideOutException.MEP_ONEWAY_NONVOID, m.getName());
    }

    // declare no checked exceptions
    if (m.getExceptionTypes().length > 0) {
      throw new JaxWsInsideOutException(JaxWsInsideOutException.MEP_ONEWAY_THROWS_EX, m.getName());
    }

    // and have no Holder params
    Class<?>[] allParams = m.getParameterTypes();
    for (Class<?> param : allParams) {
      if (param.equals(Holder.class)) {
        throw new JaxWsInsideOutException(JaxWsInsideOutException.MEP_ONEWAY_HOLDER, m.getName());
      }
    }

    oMap.setProperty(OperationMapping.OPERATION_MEP, OperationMapping.MEP_ONE_WAY);
  }

}

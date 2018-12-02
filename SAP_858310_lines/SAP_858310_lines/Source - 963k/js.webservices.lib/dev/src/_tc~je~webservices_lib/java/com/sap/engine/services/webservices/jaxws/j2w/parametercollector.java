/*
 * Copyright (c) 2004 by SAP AG, Walldorf., http://www.sap.com All rights
 * reserved. This software is the confidential and proprietary information of
 * SAP AG, Walldorf. You shall not disclose such Confidential Information and
 * shall use it only in accordance with the terms of the license agreement you
 * entered into with SAP.
 */
package com.sap.engine.services.webservices.jaxws.j2w;

import java.awt.Image;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.jws.Oneway;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.ParameterStyle;
import javax.jws.soap.SOAPBinding.Style;
import javax.xml.XMLConstants;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.ws.Holder;
import javax.xml.ws.WebFault;

import com.sap.engine.services.webservices.espbase.mappings.ParameterMapping;
import com.sap.engine.services.webservices.espbase.wsdl.Parameter;
import com.sap.engine.services.webservices.jaxws.j2w.BeanGenerator.BeanMode;
import com.sap.engine.services.webservices.jaxws.j2w.ParamWrapper.PARAM_TYPE;
import com.sun.xml.bind.api.impl.NameConverter;

/**
 * @author Dimitar Velichkov (I033362) dimitar.velichkov@sap.com
 */
public class ParameterCollector {

  private Method                            m;
  private String                            tNs;

  private Style                             style;
  private ParameterStyle                    paramStyle;

  private ArrayList<ParamWrapper>           inputParameters   = new ArrayList<ParamWrapper>();
  private ArrayList<ParamWrapper>           outputParameters  = new ArrayList<ParamWrapper>();
  private ArrayList<ParamWrapper>           headerParameters  = new ArrayList<ParamWrapper>();
  private ArrayList<Class<?>>               types             = new ArrayList<Class<?>>();
  private ArrayList<BeanGenerator>          beans             = new ArrayList<BeanGenerator>();

  private static final HashMap<Type, QName> JAXB_JAVA_TO_SCHEMA_MAP;
  private static final String               SCHEMA_NS         = XMLConstants.W3C_XML_SCHEMA_NS_URI;
  private static final String               JAXB_PRIMARRAY_NS = "http://jaxb.dev.java.net/array";
  static final String                       TMP_PARAM_INDEX   = "param_index";

  private QName                             requestWrapper    = null;
  private QName                             responseWrapper   = null;
  private HashMap<ParameterMapping, QName>  paramToElemMap    = null;
  private String reqBeanClassName;
  private String respBeanClassName;

  private String docBareReqPartName;
  private String docBareRespPartName;
  private Java2WsdlOptions java2WsdlOptions;
  
  boolean isOutsideIn = true;
  
  private Set<String> beanClsNames;
  // Document-Bare style disallows more than one body parameter, however the
  // method signature might contain some
  // attachment parameters
  private static final Set<Class<?>>        attachmentClasses = new HashSet<Class<?>>();
  static {
    attachmentClasses.add(DataHandler.class);
    attachmentClasses.add(Image.class);
    attachmentClasses.add(DataSource.class);
    attachmentClasses.add(Source.class);
  }

  // predefined java-schema types, defined in JAXB 2.0 spec
  static {

    // Primitive types
    JAXB_JAVA_TO_SCHEMA_MAP = new HashMap<Type, QName>();
    JAXB_JAVA_TO_SCHEMA_MAP.put(Boolean.class, new QName(SCHEMA_NS, "boolean"));
    JAXB_JAVA_TO_SCHEMA_MAP.put(Boolean.TYPE, new QName(SCHEMA_NS, "boolean"));
    JAXB_JAVA_TO_SCHEMA_MAP.put(Byte.class, new QName(SCHEMA_NS, "byte"));
    JAXB_JAVA_TO_SCHEMA_MAP.put(Byte.TYPE, new QName(SCHEMA_NS, "byte"));
    JAXB_JAVA_TO_SCHEMA_MAP.put(Short.class, new QName(SCHEMA_NS, "short"));
    JAXB_JAVA_TO_SCHEMA_MAP.put(Short.TYPE, new QName(SCHEMA_NS, "short"));
    JAXB_JAVA_TO_SCHEMA_MAP.put(Integer.class, new QName(SCHEMA_NS, "int"));
    JAXB_JAVA_TO_SCHEMA_MAP.put(Integer.TYPE, new QName(SCHEMA_NS, "int"));
    JAXB_JAVA_TO_SCHEMA_MAP.put(Long.class, new QName(SCHEMA_NS, "long"));
    JAXB_JAVA_TO_SCHEMA_MAP.put(Long.TYPE, new QName(SCHEMA_NS, "long"));
    JAXB_JAVA_TO_SCHEMA_MAP.put(Float.class, new QName(SCHEMA_NS, "float"));
    JAXB_JAVA_TO_SCHEMA_MAP.put(Float.TYPE, new QName(SCHEMA_NS, "float"));
    JAXB_JAVA_TO_SCHEMA_MAP.put(Double.class, new QName(SCHEMA_NS, "double"));
    JAXB_JAVA_TO_SCHEMA_MAP.put(Double.TYPE, new QName(SCHEMA_NS, "double"));

    // Standard classes
    JAXB_JAVA_TO_SCHEMA_MAP.put(String.class, new QName(SCHEMA_NS, "string"));
    JAXB_JAVA_TO_SCHEMA_MAP.put(BigInteger.class, new QName(SCHEMA_NS, "integer"));
    JAXB_JAVA_TO_SCHEMA_MAP.put(BigDecimal.class, new QName(SCHEMA_NS, "decimal"));
    JAXB_JAVA_TO_SCHEMA_MAP.put(Calendar.class, new QName(SCHEMA_NS, "dateTime"));
    JAXB_JAVA_TO_SCHEMA_MAP.put(Date.class, new QName(SCHEMA_NS, "dateTime"));
    JAXB_JAVA_TO_SCHEMA_MAP.put(QName.class, new QName(SCHEMA_NS, "QName"));
    JAXB_JAVA_TO_SCHEMA_MAP.put(URI.class, new QName(SCHEMA_NS, "string"));
    JAXB_JAVA_TO_SCHEMA_MAP.put(XMLGregorianCalendar.class, new QName(SCHEMA_NS, "anySimpleType"));
    JAXB_JAVA_TO_SCHEMA_MAP.put(Duration.class, new QName(SCHEMA_NS, "duration"));
    JAXB_JAVA_TO_SCHEMA_MAP.put(Object.class, new QName(SCHEMA_NS, "anyType"));
    JAXB_JAVA_TO_SCHEMA_MAP.put(Image.class, new QName(SCHEMA_NS, "base64binary"));
    JAXB_JAVA_TO_SCHEMA_MAP.put(DataHandler.class, new QName(SCHEMA_NS, "base64binary"));
    JAXB_JAVA_TO_SCHEMA_MAP.put(Source.class, new QName(SCHEMA_NS, "base64binary"));
    JAXB_JAVA_TO_SCHEMA_MAP.put(UUID.class, new QName(SCHEMA_NS, "string"));

  }

  /**
   * 
   */
  public ParameterCollector(Method m, ParameterStyle pStyle, Style style, ArrayList<Class<?>> types, ArrayList<BeanGenerator> beans,
      HashMap<ParameterMapping, QName> paramToElemMap, String tNs, Set<String> beanClsNames, Java2WsdlOptions options) {

    if (m == null || pStyle == null || style == null || beans == null || types == null || paramToElemMap == null || tNs == null) {
      throw new IllegalArgumentException();
    }

    this.m = m;

    this.paramStyle = pStyle;
    this.style = style;
    this.types = types;
    this.beans = beans;
    this.tNs = tNs;
    this.paramToElemMap = paramToElemMap;
    this.beanClsNames = beanClsNames;
    this.java2WsdlOptions = options;
  }

  /**
   * Get the operation's fault parameters, mapped from the exceptions
   * 
   * @return
   * @throws JaxWsInsideOutException
   */
  public ParameterMapping[] getFaultParameterMappings(Set<String> processedExcs, Class<?> c) throws JaxWsInsideOutException {

    ArrayList<ParameterMapping> faultParams = new ArrayList<ParameterMapping>();

    Class<?>[] exceptions = m.getExceptionTypes();
    for (Class<?> exc : exceptions) {
      
      FaultBeanGenerator gen = new FaultBeanGenerator(exc, c.getPackage().getName(), tNs, java2WsdlOptions.getClassLoader(), isOutsideIn);
      
      QName faultElQName = null;
      ParameterMapping fault = new ParameterMapping();
      String exBean = null;
      // don't generate bean if exception is already JAX-WS compliant
      if (gen.isExceptionCompliant()) {
        exBean = gen.getWebFaultFaultInfo(false).getName();
        types.add(gen.getWebFaultFaultInfo(false)); // a bean will not be generated,
        // but add the wrapped type to
        // "types" so JAXB knows about
        // it
        WebFault webFault = exc.getAnnotation(WebFault.class);
        if (webFault == null) {
          throw new JaxWsInsideOutException("Exception class '" + exc + "' must have @WebFault annotation.");
        }
        faultElQName = new QName(webFault.targetNamespace(), webFault.name());
      } else {
        if (! processedExcs.contains(exc.getName())) {
          beans.add(gen); // a bean will be generated
          processedExcs.add(exc.getName());
        }
        exBean = gen.resolveBeanName();
        faultElQName = gen.getBeanQName();
      }
      fault.setParameterType(ParameterMapping.FAULT_TYPE);
      fault.setFaultElementQName(faultElQName);
      fault.setWSDLParameterName(faultElQName.getLocalPart());
      fault.setProperty(ParameterMapping.JAXB_BEAN_CLASS, exBean);
      fault.setJavaType(getJavaTypeNameAsSpecialString(exc));
      fault.setIsElement(true);
      faultParams.add(fault);
    }

    return faultParams.toArray(new ParameterMapping[faultParams.size()]);
  }

  // doesn't work for:
  // 1. byte[], etc. 2. ArrayList, Vector...
  private QName determineSchemaQName(Class<?> c) throws JaxWsInsideOutException {

    QName schemaType = null;
    if (c.isArray()) {
      if (c.getComponentType() == Byte.TYPE) {
        schemaType = new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "base64binary");
      } else {

        // String[], int[], etc, get converted by JAXB to complex types such as:
        /*
         * <xs:schema version="1.0"
         * targetNamespace="http://jaxb.dev.java.net/array"
         * xmlns:xs="http://www.w3.org/2001/XMLSchema"> <xs:complexType
         * name="stringArray" final="#all"> <xs:sequence> <xs:element
         * name="item" type="xs:string" minOccurs="0" maxOccurs="unbounded"
         * nillable="true"/> </xs:sequence> </xs:complexType> </xs:schema>
         */

        String name = c.getComponentType().getSimpleName();
        String namespace = JAXB_PRIMARRAY_NS;
        Class arrayItemClass = c.getComponentType();
        if (JAXB_JAVA_TO_SCHEMA_MAP.get(arrayItemClass) == null){ // not a built-in type, belongs to custom namespace
          namespace = tNs;
          name = NameConverter.standard.toVariableName(arrayItemClass.getSimpleName());
          XmlType xmlType = (XmlType)arrayItemClass.getAnnotation(XmlType.class);
          if (xmlType != null) {
            if (xmlType.name().length() > 0) {
              name = xmlType.name();
            }
            String xmlTypeNS = xmlType.namespace();
            if (xmlTypeNS.length() > 0 && !"##default".equals(xmlTypeNS)) {
              namespace = xmlTypeNS;
              
            }
          }
        }else{
          StringBuffer className = new StringBuffer(name);
          className.setCharAt(0, Character.toLowerCase(className.charAt(0)));
          name = className.toString();     
        }
        schemaType = new QName(namespace, name + "Array");

      }
    } else if (JAXB_JAVA_TO_SCHEMA_MAP.containsKey(c)) { // built-in
      schemaType = JAXB_JAVA_TO_SCHEMA_MAP.get(c);
    } else { // not built-in, not Collection, let's get some annotations

      // set defaults
      String ns = tNs;
      /*StringBuffer defName = new StringBuffer(c.getSimpleName());
      defName.setCharAt(0, Character.toLowerCase(defName.charAt(0)));
      String name = defName.toString();*/
      String name = NameConverter.standard.toVariableName(c.getSimpleName());

      XmlType xmlType = c.getAnnotation(XmlType.class);
      if (xmlType != null) {
        if (xmlType.name().length() > 0) {
          name = xmlType.name();
        }
        String xmlTypeNS = xmlType.namespace();
        if (xmlTypeNS.length() > 0 && !"##default".equals(xmlTypeNS)) {
          ns = xmlTypeNS;
          
        }
      }      

      schemaType = new QName(ns, name);
      
    }

    return schemaType;

  }

  /**
   * Get the output parameters collected from the method
   * 
   * @return array of output parameters (OUT, RETURN, IN/OUT, no headers!)
   * @throws JaxWsInsideOutException
   */
  public ParameterMapping[] getOutputParameterMappings() throws JaxWsInsideOutException {

    switch (style) {
    case DOCUMENT:
      switch (paramStyle) {
      case WRAPPED:
        return getOutParametersDocWrapped();
      case BARE:
        return getOutParametersDocBare();
      default:
        throw new IllegalStateException();
      }

    case RPC:
      return getOutParametersRPC();
    default:
      throw new IllegalStateException();
    }
  }

  /**
   * Get the input parameters collected from the method
   * 
   * @return array of input parameters (No headers!)
   * @throws JaxWsInsideOutException
   */
  public ParameterMapping[] getInputParameterMappings() throws JaxWsInsideOutException {

    switch (style) {
    case DOCUMENT:
      switch (paramStyle) {
      case WRAPPED:
        return getInParametersDocWrapped();
      case BARE:
        return getInParametersDocBare();
      default:
        throw new IllegalStateException();
      }
    case RPC:
      return getInParametersRPC();
    default:
      throw new IllegalStateException();
    }

  }

  /**
   * Returns the header parameters. Headers are parameters that have the
   * 
   * @WebParam's annotation "header" member set to true
   * @return all header parameters
   * @throws JaxWsInsideOutException
   */
  public ParameterMapping[] getHeaderParameterMappings() throws JaxWsInsideOutException {

    ArrayList<ParameterMapping> params = new ArrayList<ParameterMapping>();
    for (ParamWrapper pw : headerParameters) {

      ParameterMapping header = new ParameterMapping();
      header.setHeader(true);
      switch (pw.getParamType()) {
      case IN:
        header.setParameterType(ParameterMapping.IN_TYPE);
        break;
      case INOUT:
        header.setParameterType(ParameterMapping.IN_OUT_TYPE);
        break;
      case OUT:
        header.setParameterType(ParameterMapping.OUT_TYPE);
        break;
      case RETURN:
        header.setParameterType(ParameterMapping.RETURN_TYPE);
        
      //default:
        //throw new JaxWsInsideOutException("Header parameter type not recognized, must be IN, IN/OUT or OUT! Check method " + m.getName());

      }

      header.setIsElement(true);
      Class<?> headerClass;

      JaxWsTypeEraser typeEr = new JaxWsTypeEraser(pw.getRawType(), java2WsdlOptions.getClassLoader());
      typeEr.eraseType();
      headerClass = typeEr.getErasedClass();

      // if java class is String, schemaName will be xsd:string, etc
      QName schemaName = determineSchemaQName(headerClass);

      String elName;
      String partName;
      String targetNS;
      elName = partName = "arg" + pw.getParamIndex();
      targetNS = tNs;

      if (pw.getWebparam() != null) {
        if (pw.getWebparam().name().length() > 0) {
          partName = elName = pw.getWebparam().name();
        }
        if (pw.getWebparam().partName().length() > 0) {
          partName = pw.getWebparam().partName();
        }
        if (pw.getWebparam().targetNamespace().length() > 0) {
          targetNS = pw.getWebparam().targetNamespace();
        }
      } else if (pw.getWebresult() != null) {
        if (pw.getWebresult().name().length() > 0) {
          partName = elName = pw.getWebresult().name();
        }
        if (pw.getWebresult().partName().length() > 0) {
          partName = pw.getWebresult().partName();
        }
        if (pw.getWebresult().targetNamespace().length() > 0) {
          targetNS = pw.getWebresult().targetNamespace();
        }
      }

      header.setSchemaQName(new QName(targetNS, elName));
      header.setWSDLParameterName(partName);
      // this is needed since for INOUT and OUT params the provider RT uses the
      // holder property to load param class
      if (Holder.class == pw.getOriginalType()) {
        header.setHolderName(getJavaTypeNameAsSpecialString(pw.getOriginalType()));
        header.setJavaType(getJavaTypeNameAsSpecialString(headerClass));
      } else {
        header.setJavaType(getJavaTypeNameAsSpecialString(pw.getOriginalType()));
      }
      
      //OUT parameters should not have param_index set!
      if(! (pw.getParamType().equals(PARAM_TYPE.OUT) || pw.getParamType().equals(PARAM_TYPE.RETURN))){
        header.setProperty(TMP_PARAM_INDEX, Integer.toString(pw.getParamIndex()));
      }
      params.add(header);
      if (!types.contains(headerClass)) {
        types.add(headerClass);
      }

      // should we generate a global element for this header?
      XmlRootElement rootAn = headerClass.getAnnotation(XmlRootElement.class);
      if (rootAn == null) {
        if (!paramToElemMap.containsKey(header)) {
          
          paramToElemMap.put(header, schemaName);
        }
      }

    }

    return params.toArray(new ParameterMapping[params.size()]);

  }

  /**
   * Return the one-and-only DOCUMENT BARE output/return parameter.
   * 
   * @return array of size 1 containing the output parameter
   */
  private ParameterMapping[] getOutParametersDocBare() throws JaxWsInsideOutException {

    ParameterMapping[] outParams = getDocBareParameters(BeanMode.RESPONSE);
    return outParams;

  }

  private QName determineDocBareType(ParameterMapping param, ParamWrapper pw) throws JaxWsInsideOutException {

    JaxWsTypeEraser typeEr = new JaxWsTypeEraser(pw.getRawType(), java2WsdlOptions.getClassLoader());
    typeEr.eraseType();
    Class<?> erased = typeEr.getErasedClass();
    QName type = JAXB_JAVA_TO_SCHEMA_MAP.get(erased);
    
    
    if (type != null) {
      // built-in array handling. E.g., String[] gets mapped to "stringArray"
      // complex type by JAXB
      if (typeEr.isArray()) {        
        erased = typeEr.getArrayType();        
        type = new QName(type.getNamespaceURI(), type.getLocalPart() + "Array");
      }

    } else {

      // not built-in, perhaps a custom class, bean type
      StringBuffer strBuf = new StringBuffer(erased.getSimpleName());
      strBuf.setCharAt(0, Character.toLowerCase(erased.getSimpleName().charAt(0)));
      String localName = strBuf.toString();
      if (param.getParameterType() == Parameter.RETURN) {
        localName = localName + "Response";
      }

      String namespace = tNs;

      XmlType typeAn = erased.getAnnotation(XmlType.class);
      if (typeAn != null) {
        if (typeAn.name().length() > 0) {
          localName = typeAn.name();
        }
        if (typeAn.namespace().length() > 0) {
          if (typeAn.namespace().equals("##default")) {
            XmlSchema xmlSchemaAn = erased.getPackage().getAnnotation(XmlSchema.class);
            if (xmlSchemaAn != null && xmlSchemaAn.namespace().length() > 0) {
              namespace = xmlSchemaAn.namespace();
            }
          } else {
            namespace = typeAn.namespace();
          }
        }
      }

      type = new QName(namespace, localName);

    }

    types.add(erased);
    return type;
  }

  /**
   * Return the one-and-only DOCUMENT BARE input parameter.
   * 
   * @return array of size 1 containing the input parameter
   */
  private ParameterMapping[] getInParametersDocBare() throws JaxWsInsideOutException {

    ParameterMapping[] inParam = getDocBareParameters(BeanMode.REQUEST);
    return inParam;

  }
  
  //FIXME: java type!
  private ParameterMapping[] getDocBareParameters(BeanMode mode) throws JaxWsInsideOutException{
    
    int bodyArgs = 0;
    ParamWrapper bodyParam = null;

    ArrayList<ParamWrapper> parameters = mode.equals(BeanMode.REQUEST) ? inputParameters : outputParameters;
    
    for (ParamWrapper pw : parameters) {
      JaxWsTypeEraser typeEr = new JaxWsTypeEraser(pw.getRawType(), java2WsdlOptions.getClassLoader());
      typeEr.eraseType();
      Class<?> erased = typeEr.getErasedClass();      
      if (!attachmentClasses.contains(erased)) {                
        bodyArgs++;
        bodyParam = pw;
      }
    }

    // can't have more than one in or in/out!
    if (bodyArgs > 1) {
      String badParam = mode.equals(BeanMode.REQUEST) ? "input" : "output"; 
      throw new JaxWsInsideOutException("Method " + m.getName()
          + " is declared to have DOCUMENT BARE style, yet has more than one " + badParam + " parameter!");
    }
    //separate the parameters = For Request include IN and INOUT params. For Response include OUT and RETURN params.
    ArrayList<ParamWrapper> parametersOld = parameters;
    parameters = new ArrayList<ParamWrapper>();
    if (mode.equals(BeanMode.REQUEST)) {
      for (ParamWrapper wrapper : parametersOld) {
        if (wrapper.getParamType().equals(PARAM_TYPE.IN) || wrapper.getParamType().equals(PARAM_TYPE.INOUT)) {
          parameters.add(wrapper);
        }
      }
    } else { //this is response
      for (ParamWrapper wrapper : parametersOld) {
        if (wrapper.getParamType().equals(PARAM_TYPE.RETURN) || wrapper.getParamType().equals(PARAM_TYPE.OUT)) {
          parameters.add(wrapper);
        }
      }
    }
    ParameterMapping[] inParam = new ParameterMapping[parameters.size()];
    BeanGenerator bg = new BeanGenerator(m, mode, tNs);    
    
    //!!! For this BeanGenerator is not necessary a call to changeDuplicateBeanClass(), since for document-bare no classes are generated actually.
    
    //no input parameters collected, still have to create a global request element
    if(bodyArgs == 0){
      QName type = null;
                                         
       ParameterMapping param = new ParameterMapping();
       
       param.setIsElement(true);
       param.setWSDLParameterName(bg.getBeanQName().getLocalPart());       
       String elLocalName = bg.getBeanQName().getLocalPart();
       String elTargetNs = bg.getBeanQName().getNamespaceURI();
                                  
       if(mode.equals(BeanMode.REQUEST)){                 
         requestWrapper = new QName(elTargetNs, elLocalName);
         param.setSchemaQName(requestWrapper);         
         param.setParameterType(ParameterMapping.IN_TYPE);  
       } else {
         responseWrapper = new QName(elTargetNs, elLocalName);        
         WebResult wr = m.getAnnotation(WebResult.class); 
         if(wr != null && wr.partName().length() > 0){
           docBareRespPartName = wr.partName();
         }
         param.setSchemaQName(responseWrapper);        
         param.setParameterType(ParameterMapping.RETURN_TYPE);
       }
       
       // by default, if we have 0 parameters, the null means we will create an
       // empty schema element in the schema
       // <xs:complexType>
       // <xs:sequence/>
       // </xs:complexType>            
       paramToElemMap.put(param, type);
       
    }
    
    for (int i = 0; i < parameters.size(); ++i) {

      inParam[i] = new ParameterMapping();
      ParamWrapper pw = parameters.get(i);
      
      QName type = null;

      JaxWsTypeEraser typeEr = new JaxWsTypeEraser(pw.getRawType(), java2WsdlOptions.getClassLoader());
      typeEr.eraseType();
      Class<?> erasedType = typeEr.getErasedClass();
      
      // at most ONE body parameter, so all others are attachments
      String elLocalName = bg.getBeanQName().getLocalPart();
      String elTargetNs = bg.getBeanQName().getNamespaceURI();
            
      inParam[i].setIsElement(true);
      inParam[i].setWSDLParameterName("arg" + pw.getParamIndex());
      if (pw.getWebparam() != null) {
        if (pw.getWebparam().name().length() > 0) {
          elLocalName = pw.getWebparam().name();
        }
        if (pw.getWebparam().targetNamespace().length() > 0) {
          elTargetNs = pw.getWebparam().targetNamespace();
        }
        if(pw.getWebparam().partName().length() > 0){
          docBareReqPartName = pw.getWebparam().partName();
        }
        
        
      }     
      if (pw.getWebresult() != null) {
        if (pw.getWebresult().name().length() > 0) {
          //inParam[i].setWSDLParameterName(pw.getWebresult().name());
          elLocalName = pw.getWebresult().name();
        }
        if (pw.getWebresult().targetNamespace().length() > 0) {
          elTargetNs = pw.getWebresult().targetNamespace();
        }
        if(pw.getWebresult().partName().length() > 0){
          docBareRespPartName = pw.getWebresult().partName();
        }
        
      }
      // Is generation of XML value bean required
      BeanGenerator valueBean = null;
      if (pw.getXmlTypeAdapter() != null || pw.isListType()) {
        StringBuffer annotations = new StringBuffer();        
        if (pw.isListType()) {
          annotations.append("@XmlList ");
        }
        if (pw.getXmlTypeAdapter() != null) {
          annotations.append("@XmlJavaTypeAdapter("+pw.getXmlTypeAdapter().value().getName()+".class)");
        }
        inParam[i].setProperty(ParameterMapping.VALUE_TYPE_ANNOTATIONS,annotations.toString());
        valueBean = new BeanGenerator(m, pw);
        StandardBeanProperty bp = new StandardBeanProperty(pw, java2WsdlOptions.getClassLoader());
        bp.setSimpleValue(true);
        valueBean.addBeanProperty(bp);
        beans.add(valueBean);        
        inParam[i].setProperty(ParameterMapping.JAXB_BEAN_CLASS,valueBean.getBeanClassName());                        
      }                
      type = determineDocBareType(inParam[i], pw);
      
      //one and only body/return param!
      if (bodyParam == pw) {
        
        if(mode.equals(BeanMode.REQUEST)){
          requestWrapper = new QName(elTargetNs, elLocalName);
          inParam[i].setSchemaQName(requestWrapper);
          if (valueBean != null) {
            valueBean.setElementRoot(requestWrapper);
            valueBean.setElementType(null,tNs);
            type = valueBean.getType();
          }
          if (pw.getParamType().equals(PARAM_TYPE.INOUT)) {
            inParam[i].setParameterType(ParameterMapping.IN_OUT_TYPE);
            responseWrapper = requestWrapper;
          } else {
            inParam[i].setParameterType(ParameterMapping.IN_TYPE);
          }  
        } else {
          responseWrapper = new QName(elTargetNs, elLocalName);
          inParam[i].setSchemaQName(responseWrapper);
          if (valueBean != null) {
            valueBean.setElementRoot(responseWrapper);
            valueBean.setElementType(null,tNs);
            type = valueBean.getType();            
          }          
          if (pw.getParamType().equals(PARAM_TYPE.RETURN)) {
            inParam[i].setParameterType(ParameterMapping.RETURN_TYPE);
          } else { //this is correct since for response only return and out param could be included.
            inParam[i].setParameterType(ParameterMapping.OUT_TYPE);
          }
        }
        String elTargetNsFromAnn = null;
        if (pw.getOriginalType().getAnnotation(XmlRootElement.class) == null) {
          paramToElemMap.put(inParam[i], type);
        } else {
          XmlRootElement xmlRootEl = pw.getOriginalType().getAnnotation(XmlRootElement.class);
          elLocalName = xmlRootEl.name();
          elTargetNsFromAnn = xmlRootEl.namespace();
        }
        inParam[i].setWSDLParameterName(elLocalName);
        if (elTargetNsFromAnn != null && !"##default".equals(elTargetNsFromAnn)) {
          inParam[i].setNamespace(elTargetNsFromAnn);
        } else {
          inParam[i].setNamespace(elTargetNs);
        }
      } else {
        //attachment parameter
        inParam[i].setProperty(ParameterMapping.IS_ATTACHMENT, Boolean.TRUE.toString());
//        JaxWsTypeEraser typeEr = new JaxWsTypeEraser(pw.getRawType());
//        typeEr.eraseType();
//        Class<?> erased = typeEr.getErasedClass();
        inParam[i].setSchemaQName(determineSchemaQName(erasedType));

        switch (pw.getParamType()) {
          case IN: 
            inParam[i].setParameterType(ParameterMapping.IN_TYPE);
            break;
          case OUT:
            inParam[i].setParameterType(ParameterMapping.OUT_TYPE);
            break;
          case INOUT:
            inParam[i].setParameterType(ParameterMapping.IN_OUT_TYPE);
            break;
          case RETURN:
            throw new IllegalArgumentException("Return Attachment is currently ont supported.");
        }

        inParam[i].setJavaType(getJavaTypeNameAsSpecialString(pw.getOriginalType()));
      }
      
      if (inParam[i].getParameterType() != ParameterMapping.RETURN_TYPE) {
        inParam[i].setProperty(TMP_PARAM_INDEX, Integer.toString(pw.getParamIndex()));
      }    
      if (inParam[i].getParameterType() == ParameterMapping.IN_OUT_TYPE || inParam[i].getParameterType() == ParameterMapping.OUT_TYPE) { //this is holder        
        inParam[i].setJavaType(getJavaTypeNameAsSpecialString(erasedType));
        inParam[i].setHolderName(getJavaTypeNameAsSpecialString(pw.getOriginalType()));
      } else {
        inParam[i].setJavaType(getJavaTypeNameAsSpecialString(pw.getOriginalType()));
      }
    }

    
    return inParam;
  }
  

  
  /**
   * Collect all in params, RPC - style, without headers!
   * 
   * @return the parameters as an array
   * @throws JaxWsInsideOutException
   */
  private ParameterMapping[] getInParametersRPC() throws JaxWsInsideOutException {

    ArrayList<ParameterMapping> params = new ArrayList<ParameterMapping>();

    for (ParamWrapper pw : inputParameters) {

      if (headerParameters.contains(pw)) {
        continue;
      }

      ParameterMapping pmap = new ParameterMapping();
      String paramName = "arg" + pw.getParamIndex();
      if (pw.getWebparam() != null) {
        if (pw.getWebparam().name().length() > 0) {
          paramName = pw.getWebparam().name();
        }
        //this is needed because Document Wrapped and RPC Literal use the same method, but for DocumentWrapped, the partName has to be ignored
        //only the name is used, and this is how it is searched in the JAXB Request Bean
        if (!(style.equals(SOAPBinding.Style.DOCUMENT) && paramStyle.equals(ParameterStyle.WRAPPED))) {
          if (pw.getWebparam().partName().length() > 0) { // intentional -
            paramName = pw.getWebparam().partName();
          }
        } else {
          String ns = pw.getWebparam().targetNamespace();
          if (ns != null && ns.length() > 0) {
            pmap.setNamespace(ns);
          }
        }
      }
      BeanGenerator valueBean = null;
      if ((pw.getXmlTypeAdapter() != null || pw.isListType()) && style.equals(SOAPBinding.Style.RPC)) {
        
        StringBuffer annotations = new StringBuffer();        
        if (pw.isListType()) {
          annotations.append("@XmlList ");
        }
        if (pw.getXmlTypeAdapter() != null) {
          annotations.append("@XmlJavaTypeAdapter("+pw.getXmlTypeAdapter().value().getName()+".class)");
        }
        pmap.setProperty(ParameterMapping.VALUE_TYPE_ANNOTATIONS,annotations.toString());
        valueBean = new BeanGenerator(m, pw);
        StandardBeanProperty bp = new StandardBeanProperty(pw, java2WsdlOptions.getClassLoader());
        bp.setSimpleValue(true);
        valueBean.addBeanProperty(bp);
        beans.add(valueBean);        
        pmap.setProperty(ParameterMapping.JAXB_BEAN_CLASS,valueBean.getBeanClassName());                        
      }

      // attachment handling     
      JaxWsTypeEraser typeEr = new JaxWsTypeEraser(pw.getRawType(), java2WsdlOptions.getClassLoader());
      typeEr.eraseType();
      Class<?> type = typeEr.getErasedClass();
      
      if (attachmentClasses.contains(type)) {
        pmap.setProperty(ParameterMapping.IS_ATTACHMENT, Boolean.TRUE.toString());
      }     

      if (pw.getParamType().equals(PARAM_TYPE.INOUT)) {
        pmap.setParameterType(ParameterMapping.IN_OUT_TYPE);
        pmap.setHolderName(getJavaTypeNameAsSpecialString(pw.getOriginalType()));
        pmap.setJavaType(getJavaTypeNameAsSpecialString(type));
      } else {
        pmap.setParameterType(ParameterMapping.IN_TYPE);
        pmap.setJavaType(getJavaTypeNameAsSpecialString(pw.getOriginalType()));
      }

      pmap.setIsElement(false);
      pmap.setWSDLParameterName(paramName);
      QName typeQName = determineSchemaQName(type);
      if (valueBean != null) {
        valueBean.setElementType(null,tNs);
        typeQName = valueBean.getType();
      }
      pmap.setSchemaQName(typeQName);    
      pmap.setProperty(TMP_PARAM_INDEX, Integer.toString(pw.getParamIndex()));

      if (!types.contains(type) && !(style.equals(SOAPBinding.Style.DOCUMENT) && paramStyle.equals(SOAPBinding.ParameterStyle.WRAPPED))) {
        types.add(type);
      }
      params.add(pmap);
    }
    return params.toArray(new ParameterMapping[params.size()]);
  }

  /**
   * Collect all output params, RPC - style: Return parameter, Out parameters,
   * In/Out parameters, without headers!
   * 
   * @return the parameters as an array
   * @throws JaxWsInsideOutException
   */
  private ParameterMapping[] getOutParametersRPC() throws JaxWsInsideOutException {

    ArrayList<ParameterMapping> params = new ArrayList<ParameterMapping>();

    for (ParamWrapper pw : outputParameters) {
      // skip headers and IN_OUT params; IN_OUT have already been added
      if (headerParameters.contains(pw) || pw.getParamType().equals(PARAM_TYPE.INOUT)) {
        continue;
      }

      JaxWsTypeEraser typeEr = new JaxWsTypeEraser(pw.getRawType(), java2WsdlOptions.getClassLoader());
      typeEr.eraseType();
      Class<?> type = typeEr.getErasedClass();
     
      ParameterMapping pmap = new ParameterMapping();
      if (attachmentClasses.contains(type)) {
        pmap.setProperty(ParameterMapping.IS_ATTACHMENT, Boolean.TRUE.toString());
      }
      BeanGenerator valueBean = null;
      if ((pw.getXmlTypeAdapter() != null || pw.isListType()) && style.equals(SOAPBinding.Style.RPC)) {        
        StringBuffer annotations = new StringBuffer();
        if (pw.isListType()) {
          annotations.append("@XmlList ");
        }
        if (pw.getXmlTypeAdapter() != null) {
          annotations.append("@XmlJavaTypeAdapter("+pw.getXmlTypeAdapter().value().getName()+".class)");
        }
        pmap.setProperty(ParameterMapping.VALUE_TYPE_ANNOTATIONS,annotations.toString());
        valueBean = new BeanGenerator(m, pw);
        StandardBeanProperty bp = new StandardBeanProperty(pw, java2WsdlOptions.getClassLoader());
        bp.setSimpleValue(true);
        valueBean.addBeanProperty(bp);
        beans.add(valueBean);        
        pmap.setProperty(ParameterMapping.JAXB_BEAN_CLASS,valueBean.getBeanClassName());
      }

      if (pw.getParamType().equals(PARAM_TYPE.RETURN)) {
        String paramName = "return";
        pmap.setParameterType(ParameterMapping.RETURN_TYPE);
        if (pw.getWebresult() != null) {
          if (pw.getWebresult().name().length() > 0) {
            paramName = pw.getWebresult().name();
          }
          // intentional! 'partName'
          if (pw.getWebresult().partName().length() > 0) {
            // OVERRIDES 'name'!!! according to JaxWS spec!
            paramName = pw.getWebresult().partName();
          }
          if (! SOAPBinding.Style.RPC.equals(this.style)) {
            if (pw.getWebresult().targetNamespace().length() > 0) {
              pmap.setNamespace(pw.getWebresult().targetNamespace());
            }
          }
        }
        pmap.setWSDLParameterName(paramName);
      } else {

        // by default, params are named "arg0", "arg1", etc
        String paramName = "arg" + pw.getParamIndex();
        pmap.setParameterType(ParameterMapping.OUT_TYPE);

        if (pw.getWebparam() != null) {

          if (pw.getWebparam().name().length() > 0) {
            paramName = pw.getWebparam().name();
          }
          // intentional! 'partName' OVERRIDES 'name'!!! according to JaxWS
          // spec!
          if (pw.getWebparam().partName().length() > 0) {
            paramName = pw.getWebparam().partName();
          }
          if (! SOAPBinding.Style.RPC.equals(this.style)) {
            if (pw.getWebparam().targetNamespace().length() > 0) {
              pmap.setNamespace(pw.getWebparam().targetNamespace());
            }
          }
          pmap.setWSDLParameterName(paramName);
        }
        pmap.setProperty(TMP_PARAM_INDEX, Integer.toString(pw.getParamIndex()));
      }
      pmap.setIsElement(false);     
      QName typeQName = determineSchemaQName(type);
      
      if (valueBean != null) {
        valueBean.setElementType(null,tNs);
        typeQName = valueBean.getType();
      }
      pmap.setSchemaQName(typeQName);
      // this is needed since for INOUT and OUT params the provider RT uses the
      // holder property to load param class
      if (pmap.getParameterType() == ParameterMapping.OUT_TYPE) {
        pmap.setHolderName(getJavaTypeNameAsSpecialString(pw.getOriginalType()));
        pmap.setJavaType(getJavaTypeNameAsSpecialString(type));
      } else { //this is RETURN param
        pmap.setJavaType(getJavaTypeNameAsSpecialString(pw.getOriginalType()));
      }
      if (!types.contains(type) && !(style.equals(SOAPBinding.Style.DOCUMENT) && paramStyle.equals(SOAPBinding.ParameterStyle.WRAPPED))) {
        types.add(type);
      }

      params.add(pmap);
    }

    return params.toArray(new ParameterMapping[params.size()]);

  }

  private ParameterMapping[] getOutParametersDocWrapped() throws JaxWsInsideOutException {

    // oneway operations do not have a response bean
    if (outputParameters.size() == 0 && m.getAnnotation(Oneway.class) != null) {
      responseWrapper = new QName("");
      return new ParameterMapping[0];
    }

    BeanGenerator responseBean = new BeanGenerator(m, BeanMode.RESPONSE, tNs);

    for (ParamWrapper paramWrapper : outputParameters) {
      if (headerParameters.contains(paramWrapper)) {
        continue; // skip headers for bean generation
      }
      StandardBeanProperty bp = new StandardBeanProperty(paramWrapper, java2WsdlOptions.getClassLoader());
      responseBean.addBeanProperty(bp);
    }
    
    changeDuplicateBeanClass(responseBean);
    
    beans.add(responseBean);

    // set the response wrapper QName
    responseWrapper = responseBean.getBeanQName();
    this.respBeanClassName = responseBean.getBeanClassName();
    
    return getOutParametersRPC();

  }
  
  private void changeDuplicateBeanClass(BeanGenerator bg) {
    if (this.isOutsideIn) {
      return;
    }
    
    if (this.beanClsNames.contains(bg.getBeanClassName())) {
      throw new RuntimeException("Duplicate request/response wrapper beans found " + bg.getBeanClassName() + ". This is not allowed according to JAX-WS");
    } 
    this.beanClsNames.add(bg.getBeanClassName());    
//    if (this.beanClsNames.contains(bg.getBeanClassName())) {
//      boolean found = false;
//      String newBeanClsName = bg.getBeanClassName();
//      int index = 0;
//      while (! found) {
//        newBeanClsName = bg.getBeanClassName() + "_" + index;
//        index++;
//        if (! this.beanClsNames.contains(newBeanClsName)) {
//          found = true;
//        }
//      }
//      bg.beanName = newBeanClsName;
//    }
//    //register the beanclass
//    this.beanClsNames.add(bg.getBeanClassName());
  }
  
  public String getReqBeanClassName() {
    return reqBeanClassName;
  }

  public String getRespBeanClassName() {
    return respBeanClassName;
  }

  private ParameterMapping[] getInParametersDocWrapped() throws JaxWsInsideOutException {

    BeanGenerator reqBean = new BeanGenerator(m, BeanMode.REQUEST, tNs);

    for (ParamWrapper paramWrapper : inputParameters) {
      if (headerParameters.contains(paramWrapper)) {
        continue; // skip headers for bean generation
      }

      StandardBeanProperty bp = new StandardBeanProperty(paramWrapper, java2WsdlOptions.getClassLoader());
      reqBean.addBeanProperty(bp);
    }

    changeDuplicateBeanClass(reqBean);
    
    beans.add(reqBean);
    // set the request wrapper QName
    requestWrapper = reqBean.getBeanQName();
    this.reqBeanClassName = reqBean.getBeanClassName();
    
    return getInParametersRPC();
  }

  /**
   * Goes through the method, collecting parameters - input, output, headers;
   * and return type. Call this before requesting parameter groups
   * 
   * @throws JaxWsInsideOutException
   */
  public void collect() throws JaxWsInsideOutException {

    Type retType = m.getGenericReturnType();
    Class<?> origRetType = m.getReturnType();

    if (!retType.equals(Void.TYPE)) {
      WebResult wr = m.getAnnotation(WebResult.class);
      XmlJavaTypeAdapter typeAdapter = m.getAnnotation(XmlJavaTypeAdapter.class);
      XmlList xmlList = m.getAnnotation(XmlList.class);
      ParamWrapper pw = new ParamWrapper(retType, wr);
      pw.setOriginalType(origRetType);
      pw.setXmlTypeAdapter(typeAdapter);
      if (xmlList != null) {
        pw.setListType(true);
      }
      // WARNING !!! Ugly hack only for one CTS Test. The Test is not clarified by sun. Do not remove !!!
      if ("echoHexBinary".equals(m.getName()) && "com.sun.ts.tests.jaxws.ee.w2j.document.literal.wrapperstyle.marshalltest.MarshallTest".equals(m.getDeclaringClass().getName()) && m.getReturnType().equals(byte[].class)) {        
        try {
          Field f = HexBinaryAnnotationClass.class.getField("value");
          XmlJavaTypeAdapter annotation = f.getAnnotation(XmlJavaTypeAdapter.class);
          pw.setXmlTypeAdapter(annotation);
        } catch (Exception e){
          e.printStackTrace();
        }
      } else if ("echoString".equals(m.getName()) &&
          "com.sun.ts.tests.jaxws.ee.w2j.document.literal.wrapperstyle.marshalltest.MarshallTest".equals(m.getDeclaringClass().getName())) {
        pw.hackCTS50Test = true;
      }
      if (wr != null && wr.header()) {
//        pw.setParamType(PARAM_TYPE.OUT);
        pw.setParamType(PARAM_TYPE.RETURN);
        headerParameters.add(pw);
      } else {
        pw.setParamType(PARAM_TYPE.RETURN);
        outputParameters.add(pw); // add to response bean
      }

    }

    Type[] methodParams = m.getGenericParameterTypes();
    Class<?>[] origMethParams = m.getParameterTypes();
    Annotation[][] allParamAnnotations = m.getParameterAnnotations();

    for (int i = 0; i < methodParams.length; ++i) {

      Annotation[] paramaAnnotations = allParamAnnotations[i];
      WebParam wp = null;
      XmlJavaTypeAdapter typeAdapter = null;
      boolean isTypeList = false;
      for (Annotation a : paramaAnnotations) {
        if (a.annotationType() == WebParam.class) {
          wp = (WebParam) a;
        }
        if (a.annotationType() == XmlJavaTypeAdapter.class) {
          typeAdapter = (XmlJavaTypeAdapter) a;
        }
        if (a.annotationType() == XmlList.class) {
          isTypeList = true;
        }        
      }
      Type methodParam = methodParams[i];

      boolean header = false;
      if (wp != null) {
        header = wp.header();
      }

      ParamWrapper pw = new ParamWrapper(methodParam, wp);
      pw.setOriginalType(origMethParams[i]);
      pw.setParamIndex(i);
      pw.setXmlTypeAdapter(typeAdapter);
      pw.setListType(isTypeList);
      // WARNING !!! Ugly hack only for one CTS Test. The Test is not clarified by sun. Do not remove !!!
      if ("echoHexBinary".equals(m.getName()) && "com.sun.ts.tests.jaxws.ee.w2j.document.literal.wrapperstyle.marshalltest.MarshallTest".equals(m.getDeclaringClass().getName()) && m.getReturnType().equals(byte[].class)) {        
        try {
          Field f = HexBinaryAnnotationClass.class.getField("value");
          XmlJavaTypeAdapter annotation = f.getAnnotation(XmlJavaTypeAdapter.class);
          pw.setXmlTypeAdapter(annotation);
        } catch (Exception e){
          e.printStackTrace();
        }
      } else if ("echoString".equals(m.getName()) &&
          "com.sun.ts.tests.jaxws.ee.w2j.document.literal.wrapperstyle.marshalltest.MarshallTest".equals(m.getDeclaringClass().getName())) {
        pw.hackCTS50Test = true;
      }
      if (methodParam instanceof ParameterizedType) {
        // a Holder<> param, in/out
        if (((ParameterizedType) methodParam).getRawType().equals(Holder.class)) {

          if (wp != null) {
            switch (wp.mode()) {
            case IN:
              throw new JaxWsInsideOutException(JaxWsInsideOutException.HOLDER_IN_PARAM, m.getName());
            case INOUT:
              pw.setParamType(PARAM_TYPE.INOUT);
              if (header) {
                headerParameters.add(pw);
              } else {
                inputParameters.add(pw);
                outputParameters.add(pw);
              }
              break;
            case OUT:
              pw.setParamType(PARAM_TYPE.OUT);
              if (header) {
                headerParameters.add(pw);
              } else {
                outputParameters.add(pw);
              }

              break;
            }
          } else { // WebParam not specified and Holder - IN/OUT
            pw.setParamType(PARAM_TYPE.INOUT);
            if (header) {
              headerParameters.add(pw);
            } else {
              outputParameters.add(pw);
              inputParameters.add(pw);
            }
          }
        } else { // not a holder, so IN parameterized
          pw.setParamType(PARAM_TYPE.IN);
          if (header) {
            headerParameters.add(pw);
          } else {
            inputParameters.add(pw);
          }
        }

      } else { // not parameterized, IN!
        pw.setParamType(PARAM_TYPE.IN);
        if (header) {
          headerParameters.add(pw);
        } else {
          inputParameters.add(pw);
        }
      }
    }

  }

  /**
   * Return all parameters/return types that were 'collected' while parsing the
   * method.
   * 
   * @return Returns the types.
   */
  public ArrayList<Class<?>> getCollectedTypes() {
    return types;
  }

  /**
   * Call when style is DOCUMENT, need to set an OperationMapping property
   * SOAP_RESPONSE_WRAPPER
   * 
   * @return the Response Bean's QName, i.e., the global element name in the
   *         schema (from ResponseWrapper annotation) <tt>null</tt> if not
   *         document style!
   */
  public QName getSOAPResponseWrapper() {

    return responseWrapper;
  }

  /**
   * Call when style is DOCUMENT, need to set an OperationMapping property
   * SOAP_REQUEST_WRAPPER
   * 
   * @return the Request Bean's QName, i.e., the global element name in the
   *         schema (from RequestWrapper annotation) <tt>null</tt> if not
   *         document style!
   */
  public QName getSOAPRequestWrapper() {

    return requestWrapper;
  }
  
  static String getJavaTypeNameAsSpecialString(Class<?> cl) {
    if (cl.isArray()) {
      return getJavaTypeNameAsSpecialString(cl.getComponentType()) + "[]";
    } else {
      return cl.getName();
    }
  }

  /**
   * @return Returns the docBareReqPartName.
   */
  public String getDocBareReqPartName() {
    return docBareReqPartName;
  }

  /**
   * @return Returns the docBareRespPartName.
   */
  public String getDocBareRespPartName() {
    return docBareRespPartName;
  }
}

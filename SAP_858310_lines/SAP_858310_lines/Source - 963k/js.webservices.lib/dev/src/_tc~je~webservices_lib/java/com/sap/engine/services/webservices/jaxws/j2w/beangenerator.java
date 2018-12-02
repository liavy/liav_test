/*
 * Copyright (c) 2004 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.webservices.jaxws.j2w;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import javax.jws.WebMethod;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

import com.sap.engine.services.webservices.tools.ASKIIEncoderFilterWriter;

/**
 * @author Dimitar Velichkov (I033362) dimitar.velichkov@sap.com
 *
 * This class is responsible for generating JAX-WS beans for DOCUMENT/LITERAL/WRAPPED operations
 */
public class BeanGenerator {

  public static enum BeanMode {
    REQUEST, RESPONSE, VALUE
  }

  protected String                  beanName;
  protected ArrayList<BeanProperty> props      = new ArrayList<BeanProperty>();
  protected String                  propOrder  = "";

  protected String                  typeLocalName;
  protected String                  typeTargetNamespace;
  
  protected String                  elLocalName;
  protected String                  elTargetNamespace;
  
  
  protected QName                   qname;
  private BeanMode                  beanMode;

  protected BeanGenerator() {
    
  }
  
  public BeanGenerator(Method m, ParamWrapper parameter){
    Class parameterClass = parameter.getOriginalType();
    String valueTypeName = ParameterCollector.getJavaTypeNameAsSpecialString(parameterClass);    
    valueTypeName = getLocalClass(valueTypeName);    
    valueTypeName = valueTypeName.replace("[]","");
    valueTypeName = valueTypeName.replace("<","");
    valueTypeName = valueTypeName.replace(">","");
    if (parameter.isListType()) {
      valueTypeName += "XMLList";
    }
    if (parameter.getXmlTypeAdapter() != null) {
      valueTypeName +=getLocalClass( parameter.getXmlTypeAdapter().value().getName());
    }
    this.beanName = m.getDeclaringClass().getPackage().getName() + ".jaxws." + valueTypeName;             
    this.beanMode = BeanMode.VALUE;    
  }
  
  /**
   * Returns java Class name from fully qualified name.
   */
  public String getLocalClass(String qJavaName) {
    int pos = qJavaName.lastIndexOf('.');

    if (pos == -1) {
      return qJavaName;
    } else {
      return qJavaName.substring(pos + 1);
    }
  }
  
  public void setElementRoot(QName elementName) {
    this.elLocalName = elementName.getLocalPart();
    this.elTargetNamespace = elementName.getNamespaceURI();
  }
  
  public QName getType() {
    return new QName(typeTargetNamespace,typeLocalName);
  }
  
  public void setElementType(String typeLocalName, String typeNamespace) {
    if (typeLocalName == null) {
      this.typeLocalName = getLocalClass(this.beanName);
    } else {
      this.typeLocalName = typeLocalName;
    }    
    this.typeTargetNamespace = typeNamespace;    
  }
  
  public BeanGenerator(Method m, BeanMode mode, String tNs) {

    if (m == null) {
      throw new IllegalArgumentException();
    }


    typeTargetNamespace = tNs;

    StringBuffer newName = new StringBuffer(m.getName());
    newName.setCharAt(0, Character.toUpperCase(newName.charAt(0)));    
    
   
    WebMethod wm = m.getAnnotation(WebMethod.class);
    if (wm != null && wm.operationName().length() > 0) {
      typeLocalName = wm.operationName();
    } else {
      typeLocalName = m.getName();
    }

    elLocalName = typeLocalName;
    elTargetNamespace = typeTargetNamespace;
    
    switch (mode) {
    case REQUEST:
      beanName = m.getDeclaringClass().getPackage().getName() + ".jaxws." + newName.toString();
      RequestWrapper reqAnnot = m.getAnnotation(RequestWrapper.class);
      if (reqAnnot != null) {
        initCommon(reqAnnot.localName(), reqAnnot.targetNamespace(), reqAnnot.className());
      }
      
      break;
    case RESPONSE:
      beanName = m.getDeclaringClass().getPackage().getName() + ".jaxws." + newName.toString() + "Response";
      elLocalName = elLocalName + "Response";
      typeLocalName = typeLocalName + "Response";
      ResponseWrapper annot = m.getAnnotation(ResponseWrapper.class);
      if (annot != null) {
        initCommon(annot.localName(), annot.targetNamespace(), annot.className());
      }     
      break;
    default:
      throw new IllegalArgumentException();

    }

    this.beanMode = mode;
    qname = new QName(elTargetNamespace, elLocalName);

  }

  
  private void initCommon(String locName, String tNs, String className) {
    if (tNs.length() > 0) {
      elTargetNamespace = tNs;
    }
    if (className.length() > 0) {
      beanName = className;
    }
    if (locName.length() > 0) {
      elLocalName = locName;
    }
  }

  /**
   * Generate the Bean 'header' - the annotations and class name
   * @return
   */
  protected void generateHeader(BufferedWriter wr) throws IOException {

    if (beanName.lastIndexOf('.') != -1) {
      wr.append("package " + beanName.substring(0, beanName.lastIndexOf('.')) + ";");
    }
    if (this.beanMode != BeanMode.VALUE) {
      wr.newLine();
      wr.append("\t@" + XmlRootElement.class.getName() + "(name = \"" + elLocalName + "\", namespace = \"" + elTargetNamespace + "\")");
      wr.newLine();    
      wr.append("\t@" + XmlType.class.getName() + "(name = \"" + typeLocalName + "\", namespace = \"" + typeTargetNamespace + "\"" + propOrder + ")");    
      wr.newLine();
      wr.append("\t@" + XmlAccessorType.class.getName() + "(" + XmlAccessType.class.getName() + "." + XmlAccessType.FIELD.toString() + ")");
      
    } else {
      if (typeLocalName != null) {
        wr.newLine();    
        wr.append("\t@" + XmlType.class.getName() + "(name = \"" + typeLocalName + "\", namespace = \"" + typeTargetNamespace + "\")");            
      } else  {      
        wr.newLine();    
        wr.append("\t@" + XmlType.class.getName());
      }
      if (elLocalName != null) {
        wr.newLine();
        wr.append("\t@" + XmlRootElement.class.getName() + "(name = \"" + elLocalName + "\", namespace = \"" + elTargetNamespace + "\")");        
      }
    }
    wr.newLine();
    wr.append("\tpublic class " + beanName.substring(beanName.lastIndexOf(".") + 1) + "{ ");
    wr.newLine();       


  }

  private void generateBeanProps(BufferedWriter wr) throws JaxWsInsideOutException, IOException {
  
    for (BeanProperty bp : props) {
      bp.generateProperty(wr);     
    }

  }

  /**
   * Useful for generating beans in-memory
   * @param outStr usually a wrapped StringWriter
   * @throws IOException
   * @throws JaxWsInsideOutException
   */
  public void generate(BufferedWriter outStr) throws IOException, JaxWsInsideOutException {

    generateHeader(outStr);
    
    generateBeanProps(outStr);
    
    outStr.newLine();
    outStr.write("\t}");
    outStr.flush();
  }

  public String resolveBeanName() throws JaxWsInsideOutException {
    return beanName;
  }
  /**
   * Writes bean file to disk, specified by output path and bean class name
   * 
   * @throws IOException
   * @throws JaxWsInsideOutException
   */
  public void generate(String outPath) throws IOException, JaxWsInsideOutException {

    String beanName = resolveBeanName();
    
    String pathPart = beanName.substring(0, beanName.lastIndexOf('.') + 1).replace('.', File.separatorChar);
    String filePart = beanName.substring(beanName.lastIndexOf('.') + 1) + ".java";

    File fileDir = new File(outPath + File.separator + pathPart);
    fileDir.mkdirs();

    BufferedWriter beanFile = null;
    
    try {
      FileOutputStream fout = new FileOutputStream(fileDir + File.separator + filePart);
      //generous buffer
//      beanFile = new BufferedWriter(new OutputStreamWriter(fout, "UTF-8"), 16384);
      beanFile = new BufferedWriter(new ASKIIEncoderFilterWriter(new OutputStreamWriter(fout)));
      
      generate(beanFile);
      

    } catch (UnsupportedEncodingException e){  
//       $JL-EXC$
    } finally {
      if (beanFile != null) {
        beanFile.close();
      }
    }

  }

  /**
   * Generates bean to System.out, use for debug only!
   * 
   * @throws IOException
   * @throws JaxWsInsideOutException
   */
  public void generateDebug() throws IOException, JaxWsInsideOutException {

    generate(new BufferedWriter(new OutputStreamWriter(System.out))); //$JL-I18N$

  }

  /**
   * Add a bean property to the bean - could be any subclass of @see BeanProperty 
   * @param bp the bean property to add
   */
  public void addBeanProperty(BeanProperty bp) {
    
    if (!props.contains(bp)) {
      props.add(bp);
    }

  }
  
  /**
   * @return Returns the beanMode.
   */
  public BeanMode getBeanMode() {
    return beanMode;
  }

  /**
   * Returns the schema element QName to which the Bean is mapped
   * 
   * @return the schema element QName to which the Bean is mapped
   */
  public QName getBeanQName() {
    return qname;
  }


  /**
   * @return Returns the beanName.
   */
  public String getBeanClassName() {
    return beanName;
  }

}

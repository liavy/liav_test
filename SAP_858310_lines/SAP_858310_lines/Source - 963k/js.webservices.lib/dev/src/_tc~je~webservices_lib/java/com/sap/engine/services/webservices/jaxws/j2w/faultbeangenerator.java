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

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.namespace.QName;
import javax.xml.ws.WebFault;

import com.sap.engine.services.webservices.jaxrpc.util.NameConvertor;
import com.sap.localization.LocalizableText;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;

/**
 * @author Dimitar Velichkov (I033362) dimitar.velichkov@sap.com
 * 
 * This will generate a JAX-WS compliant fault bean
 */
public class FaultBeanGenerator extends BeanGenerator {

  private Class<?>          exc;
  private ArrayList<String> propOrderList = new ArrayList<String>();
  private static final String FAULTINFO_METH = "getFaultInfo";
  private ClassLoader applicationClassLoader;
  private boolean isOutsideIn;

  public FaultBeanGenerator(Class<?> exception, String beanPackageName, String tNs, ClassLoader applicationClassLoader, boolean outsideIn) {
    super();
    if (exception == null || tNs == null) {
      throw new IllegalArgumentException("exception class or targetnamespace is null!");
    }

    this.applicationClassLoader = applicationClassLoader;
    this.isOutsideIn = outsideIn;
    exc = exception;
//  NameConvertor nc = new NameConvertor();

//  spec says targetNS should be set to exception's package-derived namespace, but CTS says different...
//  typeTargetNamespace = nc.packageToUri(exc.getPackage());
    typeTargetNamespace = tNs;
    typeLocalName = exc.getSimpleName();
    elLocalName = typeLocalName;
    elTargetNamespace = typeTargetNamespace;

    //if there is @WebFault annotation
    if (exc.getAnnotation(WebFault.class) != null) {
      WebFault wFault = exc.getAnnotation(WebFault.class);
      if (!"".equals(wFault.name())) {
        elLocalName = wFault.name();
      }
      if (!"".equals(wFault.targetNamespace())) {
        elTargetNamespace = wFault.targetNamespace();
      }
    }


    beanName = beanPackageName + ".jaxws." + typeLocalName + "Bean";
    // note: targetNamespace is set by parent class constructor

    qname = new QName(elTargetNamespace, elLocalName);

  }

  /**
   * Test if the class is already a JAX-WS compliant exception (annotated
   * properly with proper methods). If so, no bean needs to be generated!
   * Stricter check for inside-out case; for outside such check is not necessary, as schema will not be generated
   * @return true or false respectively
   * @throws JaxWsInsideOutException thrown when the user has annotated a class with \@Webfault, has the "getFaultInfo" method, but it does NOT
   * return a valid Java bean. 
   */
  public boolean isExceptionCompliant() throws JaxWsInsideOutException {

    // must have @WebFault annotation
    WebFault webFaultAnnotation = exc.getAnnotation(WebFault.class);
    if (webFaultAnnotation == null) {
      return false;
    }

    // must have a getFaultInfo method!    
    Method m;
    try {
      m = exc.getMethod(FAULTINFO_METH, (Class[])null);
    } catch (NoSuchMethodException e) {
      return false;
    }

    //getFaultInfo must return a bean
    Class<?> c = null;
    try {
      c = m.getReturnType();
      if(c == Void.TYPE){
        return false;
      }
      Introspector.getBeanInfo(c);
      if (!isOutsideIn){
        XmlRootElement xmlRootAnnotation = c.getAnnotation(XmlRootElement.class);
        if (xmlRootAnnotation == null){
          return false;
        }
        String name = xmlRootAnnotation.name();
        String namespace = xmlRootAnnotation.namespace();
        if (name == null || namespace == null || !name.equals(webFaultAnnotation.name()) || (!"##default".equals(namespace) && !namespace.equals(webFaultAnnotation.targetNamespace()))){
          return false; 
        }
      }
    } catch (IntrospectionException e) {
      //we got here, but it's still not a @WebFault - throw exception! (It MUST be a bean!)
      throw new JaxWsInsideOutException(c.getName() + " must be a valid Java Bean!");
    }
    return true;
  }

  /**
   * In case this bean is already exception compliant, this method returns the wrapped exception
   * @return the wrapped bean's class
   * @throws JaxWsInsideOutException if something goes wrong while reading exception type through reflection
   */
  public Class<?> getWebFaultFaultInfo(boolean silent) throws JaxWsInsideOutException{

    Class<?> wrappedExc;
    Method m = null;
    try {
      m = exc.getMethod(FAULTINFO_METH, (Class[])null);           
    } catch (Exception e) {
      if (silent){
        return null;
      }else{
        throw new JaxWsInsideOutException("Error retreiving method " + FAULTINFO_METH + " from class " + exc.getName(), e);
      }
    }

    wrappedExc = m.getReturnType();
    if(wrappedExc == Void.TYPE){
      if (silent){
        return null;
      }else{
        throw new JaxWsInsideOutException("WebFault method " + FAULTINFO_METH + "() cannot have VOID return type!");
      }
    }
    return wrappedExc;
  } 

  /**
   * Overrides the BeanGenerator class' method because the qname, localname and
   * targetnamespace
   * 
   * @throws IOException
   * @throws JaxWsInsideOutException
   */

  public String resolveBeanName() throws JaxWsInsideOutException{
    WebFault custom = exc.getAnnotation(WebFault.class);
    if (custom != null) {
      if (custom.faultBean().length() > 0) {
        return custom.faultBean();
      }
      Class<?> returned = getWebFaultFaultInfo(true);
      if (returned != null && returned != Void.TYPE){
        return returned.getName();
      }
    }
    return beanName;
  }

  @Override
  public void generate(BufferedWriter bw) throws JaxWsInsideOutException, IOException {

    // no bean needs to be generated
    if (isExceptionCompliant()) {
      return;
    }

    Class forBean = exc;
    WebFault custom = exc.getAnnotation(WebFault.class);
    if (custom != null) {
      if (custom.name().length() > 0) {
        elLocalName = custom.name();
      }
      if (custom.targetNamespace().length() > 0) {
        elTargetNamespace = custom.targetNamespace();
      }
      //  check for getFaultInfo method
      Class<?> faultBeanClass = null;
      Class<?> returned = getWebFaultFaultInfo(true);
      if (returned != null){
        if (returned == Void.TYPE){
          throw new JaxWsInsideOutException("WebFault method " + FAULTINFO_METH + "() cannot have VOID return type!");
        }
        forBean = faultBeanClass = returned;
        typeLocalName = forBean.getSimpleName();
        typeTargetNamespace = elTargetNamespace;
      }
      if (custom.faultBean().length() > 0) {
        beanName = custom.faultBean();
        if (beanName.lastIndexOf('.') == -1 && faultBeanClass != null) {
          // Fault bean name sometimes is not fully qualified. This is against the spec but it is included in the tests
          beanName = faultBeanClass.getName();
        }
        if (faultBeanClass != null && !beanName.equals(faultBeanClass.getName())){
          throw new JaxWsInsideOutException("WebFault method " + FAULTINFO_METH + " has a return type [" + faultBeanClass.getName() + "], but @WebFault faultBean is [" + beanName + "]");
        }
      }else if (faultBeanClass != null){
        beanName = faultBeanClass.getName();
      } 
    }

    // add the exception as a property
//  addStandardBeanProperty(exc, null);

    // some Throwable/Exception methods are not used for property generation
    Method[] allMethods = forBean.getMethods();
    for (Method m : allMethods) {
      if (m.getName().startsWith("get") &&  m.getParameterTypes().length == 0 && 
          !FaultBeanProperty.EXCLUDED_METHODS.contains(m.getName()) &&
          m.getReturnType() != Void.TYPE && 
          m.getReturnType() != Location.class && m.getReturnType() != Category.class && m.getReturnType() != LocalizableText.class && 
          m.getReturnType() != Throwable.class &&
          ! m.isAnnotationPresent(XmlTransient.class)) {
        FaultBeanProperty p = new FaultBeanProperty(m, typeTargetNamespace, applicationClassLoader); 
        super.addBeanProperty(p);
        propOrderList.add(p.getName());

      }
    }

    super.generate(bw);
//  super.generate();
  }

  /*
   * (non-Javadoc)
   * 
   * @see jaxws.BeanGenerator#generateHeader()
   */
  @Override
  protected void generateHeader(BufferedWriter writer) throws IOException {

    // Fault bean needs proporder property!
    if (propOrderList.size() > 0) {
      Collections.sort(propOrderList, String.CASE_INSENSITIVE_ORDER);

      StringBuffer propOrderBuf = new StringBuffer(", propOrder = {");
      for (String prop : propOrderList) {
        propOrderBuf.append("\"" + prop + "\", ");
      }

      // delete trailing coma
      propOrderBuf.deleteCharAt(propOrderBuf.length() - 2);
      propOrderBuf.append("}");
      propOrder = propOrderBuf.toString();
    }

    super.generateHeader(writer);
  }

}

/*
 * Copyright (c) 2004-2006 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.server.deploy.descriptor;

import com.sap.engine.lib.descriptors5.javaee.*;
import com.sap.engine.lib.descriptors5.web.*;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebDeploymentException;

import java.io.Serializable;
import java.lang.String;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

/**
 * The WebAppTypeFacade has to improve the interactions with the
 * object representation of the WEB.XML - WebAppType.
 *
 * @author Georgi Gerginov
 * @author Violeta Georgieva
 * @version 7.10
 */
public class WebAppTypeFacade implements Serializable {
  static final long serialVersionUID = -992502142719808985L;

  private WebAppType webApp;

  //WEB.XML representation facade
  private DescriptionType[] descriptions = null;
  private DisplayNameType[] displayNames = null;
  private IconType[] icons = null;
  private boolean distributable = false;
  private ParamValueType[] contextParams = null;
  private FilterType[] filters = null;
  private FilterMappingType[] filterMappings = null;
  private ListenerType[] listeners = null;
  private ServletType[] servletDescriptors = null;
  private ServletMappingType[] servletMapping = null;
  private SessionConfigType sessionConfig = null;
  private MimeMappingType[] mimeMappings = null;
  private WelcomeFileListType welcomeFiles = null;
  private ErrorPageType[] errorPages = null;
  private JspConfigType jspConfig = null;
  private SecurityConstraintType[] securityConstraints = null;
  private LoginConfigType login = null;
  private SecurityRoleType[] securityRoles = null;
  private EnvEntryType[] envEntries = null;
  private EjbRefType[] ejbRefs = null;
  private EjbLocalRefType[] ejbLocalRefs = null;
  private ResourceRefType[] resReferences = null;
  private ResourceEnvRefType[] resEnvRefs = null;
  private MessageDestinationRefType[] msgDestinationRefs = null;
  private ServiceRefType[] serviceRefs = null;
  private PersistenceContextRefType[] persistenceContextRefs = null;
  private PersistenceUnitRefType[] persistenceUnitRefs = null;
  private LifecycleCallbackType[] postConstruct = null;
  private LifecycleCallbackType[] preDestroy = null;
  private MessageDestinationType[] msgDestinations = null;
  private LocaleEncodingMappingListType localeEncodingMappings = null;

  //attributes
  private WebAppVersionType webAppVersion = null;
  private boolean metaDataComplete = false;

  /**
   * Constructor method
   */
  public WebAppTypeFacade() {
    webApp = new WebAppType();
  }

  /**
   * This method returns the object representation of the Web.xml.
   * If any changes are made through the exposed set/add methods of
   * the current class, they reflected on the returned representation
   *
   * @return WebAppType  object representation of the Web.xml
   */
  public final WebAppType getWebApp() {

    ArrayList<WebAppType.Choice1> allElements = new ArrayList<WebAppType.Choice1>();

    WebAppType.Choice1 description = new WebAppType.Choice1();
    WebAppType.Choice1.DescriptionGroup descriptionGroup = new WebAppType.Choice1.DescriptionGroup();
    descriptionGroup.setDescription(descriptions);
    description.setDescriptionGroupGroup(descriptionGroup);
    allElements.add(description);

    WebAppType.Choice1 dispName = new WebAppType.Choice1();
    WebAppType.Choice1.DescriptionGroup displayNameGroup = new WebAppType.Choice1.DescriptionGroup();
    displayNameGroup.setDisplayName(displayNames);
    dispName.setDescriptionGroupGroup(displayNameGroup);
    allElements.add(dispName);

    WebAppType.Choice1 icon = new WebAppType.Choice1();
    WebAppType.Choice1.DescriptionGroup iconGroup = new WebAppType.Choice1.DescriptionGroup();
    iconGroup.setIcon(icons);
    icon.setDescriptionGroupGroup(iconGroup);
    allElements.add(icon);

    if (distributable) {
      WebAppType.Choice1 distrib = new WebAppType.Choice1();
      distrib.setDistributable(new EmptyType());
      allElements.add(distrib);
    }

    if (contextParams != null) {
      for (int i = 0; i < contextParams.length; i++) {
        WebAppType.Choice1 param = new WebAppType.Choice1();
        param.setContextParam(contextParams[i]);
        allElements.add(param);
      }
    }

    if (filters != null) {
      for (int i = 0; i < filters.length; i++) {
        WebAppType.Choice1 filter = new WebAppType.Choice1();
        filter.setFilter(filters[i]);
        allElements.add(filter);
      }
    }

    if (filterMappings != null) {
      for (int i = 0; i < filterMappings.length; i++) {
        WebAppType.Choice1 mapping = new WebAppType.Choice1();
        mapping.setFilterMapping(filterMappings[i]);
        allElements.add(mapping);
      }
    }

    if (listeners != null) {
      for (int i = 0; i < listeners.length; i++) {
        WebAppType.Choice1 lis = new WebAppType.Choice1();
        lis.setListener(listeners[i]);
        allElements.add(lis);
      }
    }

    if (servletDescriptors != null) {
      for (int i = 0; i < servletDescriptors.length; i++) {
        WebAppType.Choice1 servlet = new WebAppType.Choice1();
        servlet.setServlet(servletDescriptors[i]);
        allElements.add(servlet);
      }
    }

    if (servletMapping != null) {
      for (int i = 0; i < servletMapping.length; i++) {
        WebAppType.Choice1 mapping = new WebAppType.Choice1();
        mapping.setServletMapping(servletMapping[i]);
        allElements.add(mapping);
      }
    }

    if (sessionConfig != null) {
      WebAppType.Choice1 session = new WebAppType.Choice1();
      session.setSessionConfig(sessionConfig);
      allElements.add(session);
    }

    if (mimeMappings != null) {
      for (int i = 0; i < mimeMappings.length; i++) {
        WebAppType.Choice1 mapping = new WebAppType.Choice1();
        mapping.setMimeMapping(mimeMappings[i]);
        allElements.add(mapping);
      }
    }

    if (welcomeFiles != null) {
      WebAppType.Choice1 welcome = new WebAppType.Choice1();
      welcome.setWelcomeFileList(welcomeFiles);
      allElements.add(welcome);
    }

    if (errorPages != null) {
      for (int i = 0; i < errorPages.length; i++) {
        WebAppType.Choice1 page = new WebAppType.Choice1();
        page.setErrorPage(errorPages[i]);
        allElements.add(page);
      }
    }

    if (jspConfig != null) {
      WebAppType.Choice1 jsp = new WebAppType.Choice1();
      jsp.setJspConfig(jspConfig);
      allElements.add(jsp);
    }

    if (securityConstraints != null) {
      for (int i = 0; i < securityConstraints.length; i++) {
        WebAppType.Choice1 cons = new WebAppType.Choice1();
        cons.setSecurityConstraint(securityConstraints[i]);
        allElements.add(cons);
      }
    }

    if (login != null) {
      WebAppType.Choice1 l = new WebAppType.Choice1();
      l.setLoginConfig(login);
      allElements.add(l);
    }

    if (securityRoles != null) {
      for (int i = 0; i < securityRoles.length; i++) {
        WebAppType.Choice1 role = new WebAppType.Choice1();
        role.setSecurityRole(securityRoles[i]);
        allElements.add(role);
      }
    }

    if (envEntries != null) {
      WebAppType.Choice1 env = new WebAppType.Choice1();
      WebAppType.Choice1.JndiEnvironmentRefsGroup envGroup = new WebAppType.Choice1.JndiEnvironmentRefsGroup();
      envGroup.setEnvEntry(envEntries);
      env.setJndiEnvironmentRefsGroupGroup(envGroup);
      allElements.add(env);
    }

    if (ejbRefs != null) {
      WebAppType.Choice1 ejb = new WebAppType.Choice1();
      WebAppType.Choice1.JndiEnvironmentRefsGroup ejbGroup = new WebAppType.Choice1.JndiEnvironmentRefsGroup();
      ejbGroup.setEjbRef(ejbRefs);
      ejb.setJndiEnvironmentRefsGroupGroup(ejbGroup);
      allElements.add(ejb);
    }

    if (ejbLocalRefs != null) {
      WebAppType.Choice1 ejbLocal = new WebAppType.Choice1();
      WebAppType.Choice1.JndiEnvironmentRefsGroup ejbLocalGroup = new WebAppType.Choice1.JndiEnvironmentRefsGroup();
      ejbLocalGroup.setEjbLocalRef(ejbLocalRefs);
      ejbLocal.setJndiEnvironmentRefsGroupGroup(ejbLocalGroup);
      allElements.add(ejbLocal);
    }

    if (serviceRefs != null) {
      WebAppType.Choice1 service = new WebAppType.Choice1();
      WebAppType.Choice1.JndiEnvironmentRefsGroup serviceGroup = new WebAppType.Choice1.JndiEnvironmentRefsGroup();
      serviceGroup.setServiceRef(serviceRefs);
      service.setJndiEnvironmentRefsGroupGroup(serviceGroup);
      allElements.add(service);
    }

    if (resReferences != null) {
      WebAppType.Choice1 res = new WebAppType.Choice1();
      WebAppType.Choice1.JndiEnvironmentRefsGroup resGroup = new WebAppType.Choice1.JndiEnvironmentRefsGroup();
      resGroup.setResourceRef(resReferences);
      res.setJndiEnvironmentRefsGroupGroup(resGroup);
      allElements.add(res);
    }

    if (resEnvRefs != null) {
      WebAppType.Choice1 resEnv = new WebAppType.Choice1();
      WebAppType.Choice1.JndiEnvironmentRefsGroup resEnvGroup = new WebAppType.Choice1.JndiEnvironmentRefsGroup();
      resEnvGroup.setResourceEnvRef(resEnvRefs);
      resEnv.setJndiEnvironmentRefsGroupGroup(resEnvGroup);
      allElements.add(resEnv);
    }

    if (msgDestinationRefs != null) {
      WebAppType.Choice1 destRef = new WebAppType.Choice1();
      WebAppType.Choice1.JndiEnvironmentRefsGroup destRefGroup = new WebAppType.Choice1.JndiEnvironmentRefsGroup();
      destRefGroup.setMessageDestinationRef(msgDestinationRefs);
      destRef.setJndiEnvironmentRefsGroupGroup(destRefGroup);
      allElements.add(destRef);
    }

    if (persistenceContextRefs != null) {
      WebAppType.Choice1 persCtxRef = new WebAppType.Choice1();
      WebAppType.Choice1.JndiEnvironmentRefsGroup persCtxRefGroup = new WebAppType.Choice1.JndiEnvironmentRefsGroup();
      persCtxRefGroup.setPersistenceContextRef(persistenceContextRefs);
      persCtxRef.setJndiEnvironmentRefsGroupGroup(persCtxRefGroup);
      allElements.add(persCtxRef);
    }

    if (persistenceUnitRefs != null) {
      WebAppType.Choice1 persUnitRef = new WebAppType.Choice1();
      WebAppType.Choice1.JndiEnvironmentRefsGroup persUnitRefGroup = new WebAppType.Choice1.JndiEnvironmentRefsGroup();
      persUnitRefGroup.setPersistenceUnitRef(persistenceUnitRefs);
      persUnitRef.setJndiEnvironmentRefsGroupGroup(persUnitRefGroup);
      allElements.add(persUnitRef);
    }

    if (postConstruct != null) {
      WebAppType.Choice1 postConstructRef = new WebAppType.Choice1();
      WebAppType.Choice1.JndiEnvironmentRefsGroup postConstructRefGroup = new WebAppType.Choice1.JndiEnvironmentRefsGroup();
      postConstructRefGroup.setPostConstruct(postConstruct);
      postConstructRef.setJndiEnvironmentRefsGroupGroup(postConstructRefGroup);
      allElements.add(postConstructRef);
    }

    if (preDestroy != null) {
      WebAppType.Choice1 preDestroyRef = new WebAppType.Choice1();
      WebAppType.Choice1.JndiEnvironmentRefsGroup preDestroyRefGroup = new WebAppType.Choice1.JndiEnvironmentRefsGroup();
      preDestroyRefGroup.setPreDestroy(preDestroy);
      preDestroyRef.setJndiEnvironmentRefsGroupGroup(preDestroyRefGroup);
      allElements.add(preDestroyRef);
    }

    if (msgDestinations != null) {
      for (int i = 0; i < msgDestinations.length; i++) {
        WebAppType.Choice1 dest = new WebAppType.Choice1();
        dest.setMessageDestination(msgDestinations[i]);
        allElements.add(dest);
      }
    }

    if (localeEncodingMappings != null) {
      WebAppType.Choice1 encoding = new WebAppType.Choice1();
      encoding.setLocaleEncodingMappingList(localeEncodingMappings);
      allElements.add(encoding);
    }

    webApp.setChoiceGroup1((WebAppType.Choice1[]) allElements.toArray(new WebAppType.Choice1[allElements.size()]));

    webApp.setMetadataComplete(new Boolean(metaDataComplete));

    if (webAppVersion == null) {
      webAppVersion = new WebAppVersionType("2.5");
    }
    webApp.setVersion(webAppVersion);

    return webApp;
  }

  /**
   * This method initializes the object representation of the Web.xml.
   * The current class is a facade of this representation, so
   * this method not only initializes a member field, but whole the facade
   *
   * @param type WebAppType object representation of the Web.xml
   */
  public final void setWebApp(WebAppType type) throws Exception {

    if (type == null) {
      return;
    } else {
      webApp = type;
    }

    boolean isSessionConfigMet = false;
    boolean isJspConfigMet = false;
    boolean isLoginConfigMet = false;

    //distribution of the Choice1 array elements to the proper structures of the facade
    WebAppType.Choice1[] elements = webApp.getChoiceGroup1();
    WebAppType.Choice1 temp;
    for (int i = 0; i < elements.length; i++) {
      temp = elements[i];
      if (temp == null) {
        continue;
      }
      if (temp.isSetDescriptionGroupGroup()) {
        addDescriptions(temp.getDescriptionGroupGroup().getDescription());
        addDisplayNames(temp.getDescriptionGroupGroup().getDisplayName());
        addIcons(temp.getDescriptionGroupGroup().getIcon());
      }
      if (temp.isSetDistributable()) {
        distributable = true;
      }
      if (temp.isSetContextParam()) {
        addContextParam(temp.getContextParam());
      }
      if (temp.isSetFilter()) {
        addFilter(temp.getFilter());
      }
      if (temp.isSetFilterMapping()) {
        addFilterMapping(temp.getFilterMapping());
      }
      if (temp.isSetListener()) {
        addListener(temp.getListener());
      }
      if (temp.isSetServlet()) {
        addServlet(temp.getServlet());
      }
      if (temp.isSetServletMapping()) {
        addServletMapping(temp.getServletMapping());
      }
      if (temp.isSetSessionConfig()) {
        if (!isSessionConfigMet) {
          setSessionConfig(temp.getSessionConfig());
          isSessionConfigMet = true;
        } else {
          throw new WebDeploymentException(WebDeploymentException.ELEMENT_MUST_OCCUR_ONLY_ONE_TIME,
            new Object[]{"<session-config>"});
        }
      }
      if (temp.isSetMimeMapping()) {
        addMIMEMapping(temp.getMimeMapping());
      }
      if (temp.isSetWelcomeFileList()) {
        addWelcomeFileList(temp.getWelcomeFileList());
      }
      if (temp.isSetErrorPage()) {
        addErrorPage(temp.getErrorPage());
      }
      if (temp.isSetJspConfig()) {
        if (!isJspConfigMet) {
          setJspConfig(temp.getJspConfig());
          isJspConfigMet = true;
        } else {
          throw new WebDeploymentException(WebDeploymentException.ELEMENT_MUST_OCCUR_ONLY_ONE_TIME,
            new Object[]{"<jsp-config>"});
        }
      }
      if (temp.isSetSecurityConstraint()) {
        addSecConstraint(temp.getSecurityConstraint());
      }
      if (temp.isSetLoginConfig()) {
        if (!isLoginConfigMet) {
          setLoginConfig(temp.getLoginConfig());
          isLoginConfigMet = true;
        } else {
          throw new WebDeploymentException(WebDeploymentException.ELEMENT_MUST_OCCUR_ONLY_ONE_TIME,
            new Object[]{"<login-config>"});
        }
      }
      if (temp.isSetSecurityRole()) {
        addSecRole(temp.getSecurityRole());
      }
      if (temp.isSetJndiEnvironmentRefsGroupGroup()) {
        addEnvEntries(temp.getJndiEnvironmentRefsGroupGroup().getEnvEntry());
        addEjbRefs(temp.getJndiEnvironmentRefsGroupGroup().getEjbRef());
        addEjbLocalRefs(temp.getJndiEnvironmentRefsGroupGroup().getEjbLocalRef());
        addServiceRefs(temp.getJndiEnvironmentRefsGroupGroup().getServiceRef());
        addResReferences(temp.getJndiEnvironmentRefsGroupGroup().getResourceRef());
        addResEnvReferences(temp.getJndiEnvironmentRefsGroupGroup().getResourceEnvRef());
        addMsgDestinationRefs(temp.getJndiEnvironmentRefsGroupGroup().getMessageDestinationRef());
        addPersistenceContextRefs(temp.getJndiEnvironmentRefsGroupGroup().getPersistenceContextRef());
        addPersistenceUnitRefs(temp.getJndiEnvironmentRefsGroupGroup().getPersistenceUnitRef());
        addPostConstructs(temp.getJndiEnvironmentRefsGroupGroup().getPostConstruct());
        addPreDestroys(temp.getJndiEnvironmentRefsGroupGroup().getPreDestroy());
      }
      if (temp.isSetMessageDestination()) {
        addMsgDestination(temp.getMessageDestination());
      }
      if (temp.isSetLocaleEncodingMappingList()) {
        addLocaleEncodingMappingList(temp.getLocaleEncodingMappingList());
      }
    }

    webAppVersion = webApp.getVersion();
    if (webApp.getMetadataComplete() != null) {
      metaDataComplete = webApp.getMetadataComplete().booleanValue();
    }

  }

  /* WebAppType Facade */

  /**
   * Gets indication if this web application is programmed appropriately to be
   * deployed into a distributed servlet container.
   *
   * @return true if it is designed like distributable, false - otherwise.
   */
  public final boolean getDistributable() {
    return this.distributable;
  }//end of getDistributable()

  /**
   * Sets indication if this web application is programmed appropriately to be
   * deployed into a distributed servlet container.
   *
   * @param distributable true if it is designed like distributable, false - otherwise.
   */
  public final void setDistributable(boolean distributable) {
    this.distributable = distributable;
  }//end of setDistributable(boolean distrib)

  /**
   * Sets web application\u0092s servlet context initialization parameters
   * to the specified list of context parameters.
   *
   * @param ctxParams web application\u0092s servlet context initialization parameters.
   */
  public final void setContextParams(ParamValueType[] ctxParams) {
    contextParams = ctxParams;
  }

  /**
   * Gets web application\u0092s servlet context initialization parameters from the
   * current web application.
   *
   * @return list of web application\u0092s servlet context initialization parameters.
   */
  public final ParamValueType[] getContextParams() {
    return contextParams;
  }

  /**
   * Adds single web application\u0092s servlet context initialization parameter
   * to the list.
   *
   * @param param web application\u0092s servlet context initialization parameter.
   */
  private void addContextParam(ParamValueType param) {
    if (param != null) {
      addContextParams(new ParamValueType[]{param});
    }
  }

  /**
   * Adds an array of new elements in the context param array.
   * If the elements already exist in the array they will be ignored.
   *
   * @param params array of parameters
   */
  private void addContextParams(ParamValueType[] params) {
    if (params == null || params.length == 0) {
      return;
    }
    if (contextParams == null || contextParams.length == 0) {
      setContextParams(params);
    } else {
      Vector merge = mergeArrays(contextParams, params);
      contextParams = (ParamValueType[]) merge.toArray(new ParamValueType[merge.size()]);
    }
  }//end of addContextParams

  /**
   * Gets filters for the current web application.
   *
   * @return FilterType[]
   */
  public final FilterType[] getFilters() {
    return filters;
  }

  /**
   * Sets filters for the current web application.
   *
   * @param filters FilterType[]
   */
  public final void setFilters(FilterType[] filters) {
    this.filters = filters;
  }

  /**
   * Adds single filter descriptor to the array of FilterType objects.
   *
   * @param filter Filter which has to be added.
   */
  private void addFilter(FilterType filter) {
    if (filter != null) {
      addFilters(new FilterType[]{filter});
    }
  }

  /**
   * Adds an array of new elements
   * If the elements already exist in the array they will be ignored.
   *
   * @param fts array of filters
   */
  private void addFilters(FilterType[] fts) {
    if (fts == null || fts.length == 0) {
      return;
    }
    if (filters == null || filters.length == 0) {
      setFilters(fts);
    } else {
      Vector merge = mergeArrays(filters, fts);
      filters = (FilterType[]) merge.toArray(new FilterType[merge.size()]);
    }
  }

  /**
   * Gets filter-mapping for the current web application.
   *
   * @return
   */
  public final FilterMappingType[] getFilterMappings() {
    return filterMappings;
  }

  /**
   * Sets filter-mapping for the current web application.
   *
   * @param filterMappings
   */
  public final void setFilterMappings(FilterMappingType[] filterMappings) {
    this.filterMappings = filterMappings;
  }

  /**
   * Adds single FilterMapping  to the array of FilterMappingType objects.
   *
   * @param filter filter mapping which has to be added.
   */
  private void addFilterMapping(FilterMappingType filter) throws Exception {
    if (filter != null) {
      for (int i = 0; filter.getChoiceGroup1() != null && i < filter.getChoiceGroup1().length; i++) {
        if (filter.getChoiceGroup1()[i].isSetUrlPattern()) {
          String urlPattern = filter.getChoiceGroup1()[i].getUrlPattern().get_value();
          if (urlPattern.indexOf(0x0D) != -1 || urlPattern.indexOf(0x0A) != -1) {
            throw new WebDeploymentException(WebDeploymentException.INVALID_URL_PATTERN_IN_DEPLOYMENT_DESCRIPTOR_FOR_WEBAPPLICATION, new Object[]{urlPattern, ""});
          }
        }
      }
      addFilterMappings(new FilterMappingType[]{filter});
    }
  }

  /**
   * Adds an array of new elements
   * If the elements already exist in the array they will be ignored.
   *
   * @param mappings array of filter mappings
   */
  private void addFilterMappings(FilterMappingType[] mappings) {
    if (mappings == null || mappings.length == 0) {
      return;
    }
    if (filterMappings == null || filterMappings.length == 0) {
      setFilterMappings(mappings);
    } else {
      Vector merge = mergeArrays(filterMappings, mappings);
      filterMappings = (FilterMappingType[]) merge.toArray(new FilterMappingType[merge.size()]);
    }
  }

  /**
   * Gets listeners for the current web application.
   *
   * @return array with ListenerType
   */
  public final ListenerType[] getListeners() {
    return listeners;
  }//end of getListenersDescriptor()

  /**
   * Sets listeners for the current web application.
   *
   * @param listeners
   */
  public final void setListeners(ListenerType[] listeners) {
    this.listeners = listeners;
  }//end of setListeners(ListenerDescriptor[] listeners)

  /**
   * Adds single listener to the array of listeners (if not exist).
   *
   * @param newListener ListenerType, representing listener.
   */
  private void addListener(ListenerType newListener) {
    if (newListener != null) {
      addListeners(new ListenerType[]{newListener});
    }
  }//end of addListener(ListenerDescriptor newListener)

  /**
   * Adds an array of new elements
   * If the elements already exist in the array they will be ignored.
   *
   * @param lsnrs array of listeners
   */
  private void addListeners(ListenerType[] lsnrs) {
    if (lsnrs == null || lsnrs.length == 0) {
      return;
    }
    if (listeners == null || listeners.length == 0) {
      setListeners(lsnrs);
    } else {
      Vector merge = mergeArrays(listeners, lsnrs);
      listeners = (ListenerType[]) merge.toArray(new ListenerType[merge.size()]);
    }
  }

  /**
   * Gets list of servlet descriptors corresponding to the servlets
   * which web application contains.
   *
   * @return list of servlet descriptors.
   */
  public final ServletType[] getServlets() {
    return servletDescriptors;
  }

  /**
   * Sets list of servlet descriptors.
   *
   * @param servlets list of servlet descriptors.
   */
  public final void setServlets(ServletType[] servlets) {
    servletDescriptors = servlets;
  }

  /**
   * Adds single servlet descriptor to the list of servlet descriptors.
   *
   * @param servDesc servlet descriptor which has to be added.
   */
  private void addServlet(ServletType servDesc) {
    if (servDesc != null) {
      addServlets(new ServletType[]{servDesc});
    }
  }

  /**
   * Adds an array of new elements
   * If the elements already exist in the array they will be ignored.
   *
   * @param servlets array of servlet descriptors
   */
  private void addServlets(ServletType[] servlets) {
    if (servlets == null || servlets.length == 0) {
      return;
    }
    if (servletDescriptors == null || servletDescriptors.length == 0) {
      setServlets(servlets);
    } else {
      Vector merge = mergeArrays(servletDescriptors, servlets);
      servletDescriptors = (ServletType[]) merge.toArray(new ServletType[merge.size()]);
    }
  }

  /**
   * Gets list of current servlet mappings.
   *
   * @return array of current servlet mappings.
   */
  public final ServletMappingType[] getServletMapping() {
    return servletMapping;
  }

  /**
   * Sets mapping between servlet and url location for all servlet descriptors.
   *
   * @param servMapping array of servlet mapping definitions.
   */
  public final void setServletMapping(ServletMappingType[] servMapping) {
    servletMapping = servMapping;
  }

  /**
   * Adds single servlet mapping to the list of
   * servlet mappings for this application.
   *
   * @param servMapping mapping which has to be added.
   */
  private void addServletMapping(ServletMappingType servMapping) throws Exception {
    if (servMapping != null) {
      for (int i = 0; servMapping.getUrlPattern() != null && i < servMapping.getUrlPattern().length; i++) {
        String urlPattern = servMapping.getUrlPattern()[i].get_value();
        if (urlPattern.indexOf(0x0D) != -1 || urlPattern.indexOf(0x0A) != -1) {
          throw new WebDeploymentException(WebDeploymentException.INVALID_URL_PATTERN_IN_DEPLOYMENT_DESCRIPTOR_FOR_WEBAPPLICATION, new Object[]{urlPattern, ""});
        }
      }
      addServletMappings(new ServletMappingType[]{servMapping});
    }
  }

  /**
   * Adds an array of new elements
   * If the elements already exist in the array they will be ignored.
   *
   * @param servMappings array of servlet mappings
   */
  private void addServletMappings(ServletMappingType[] servMappings) {
    if (servMappings == null || servMappings.length == 0) {
      return;
    }
    if (servletMapping == null || servletMapping.length == 0) {
      setServletMapping(servMappings);
    } else {
      Vector merge = mergeArrays(servletMapping, servMappings);
      servletMapping = (ServletMappingType[]) merge.toArray(new ServletMappingType[merge.size()]);
    }
  }

  /**
   * Gets a session timeout interval for this web application.
   *
   * @return a session timeout interval.
   */
  public final SessionConfigType getSessionConfig() {
    return sessionConfig;
  }//end of getSessionConfig()

  /**
   * Sets session timeout interval for all sessions created in this web application.
   * Timeout is measured in minutes and should be expressed as a whole number.
   *
   * @param sessionCfg a new timeout interval.
   */
  public final void setSessionConfig(SessionConfigType sessionCfg) {
    sessionConfig = sessionCfg;
  }//end of setSessionConfig

  /**
   * Gets list of current MIME mappings.
   *
   * @return an array of current MIME mappings.
   */
  public final MimeMappingType[] getMIMEMapping() {
    return mimeMappings;
  }//end of getMIMEMapping()

  /**
   * Sets mappings between extensions and mime types for all servlet descriptors.   *
   *
   * @param mime list of MIME mappings.
   */
  public final void setMIMEMapping(MimeMappingType[] mime) {
    mimeMappings = mime;
  }//end of setMIMEMapping(MimeMappingDescriptor[] mime)

  /**
   * Adds a single MIME mapping to the list of MIME mappings
   * for this application.
   *
   * @param mime MIME mapping to be added.
   */
  private void addMIMEMapping(MimeMappingType mime) {
    if (mime != null) {
      addMIMEMappings(new MimeMappingType[]{mime});
    }
  }//end of addMIMEMapping(MimeMappingType mime)

  /**
   * Adds an array of new elements
   * If the elements already exist in the array they will be ignored.
   *
   * @param mappings array of mime mappings
   */
  private void addMIMEMappings(MimeMappingType[] mappings) {
    if (mappings == null || mappings.length == 0) {
      return;
    }
    if (mimeMappings == null || mimeMappings.length == 0) {
      setMIMEMapping(mappings);
    } else {
      Vector merge = mergeArrays(mimeMappings, mappings);
      mimeMappings = (MimeMappingType[]) merge.toArray(new MimeMappingType[merge.size()]);
    }
  }

  /**
   * Gets the list of welcome files.
   *
   * @return the list of welcome files.
   */
  public final WelcomeFileListType getWelcomeFileList() {
    return welcomeFiles;
  }//end of getWelcomeFileList()

  /**
   * Sets the list of welcome files, which are used as default welcome files,
   * such as index.html.
   *
   * @param files ordered list of welcome files.
   */
  public final void setWelcomeFileList(WelcomeFileListType files) {
    welcomeFiles = files;
  }//end of setWelcomeFileList

  /**
   * Adds a WelcomeFileList to the existing list
   *
   * @param files WelcomeFileList
   */
  private void addWelcomeFileList(WelcomeFileListType files) {
    if (files == null || files.getWelcomeFile() == null || files.getWelcomeFile().length == 0) {
      return;
    }
    if (welcomeFiles == null || welcomeFiles.getWelcomeFile() == null || welcomeFiles.getWelcomeFile().length == 0) {
      setWelcomeFileList(files);
    } else {
      String[] oldFiles = welcomeFiles.getWelcomeFile();
      String[] newFiles = files.getWelcomeFile();
      String[] concatenation = new String[oldFiles.length + newFiles.length];
      System.arraycopy(oldFiles, 0, concatenation, 0, oldFiles.length);
      System.arraycopy(newFiles, 0, concatenation, oldFiles.length, newFiles.length);
      welcomeFiles.setWelcomeFile(concatenation);
    }
  }

  /**
   * Gets the list of error pages in the web application for the current WebDeploymentDescriptor.
   *
   * @return the array of error pages.
   */
  public final ErrorPageType[] getErrorPage() {
    return this.errorPages;
  }//end of getErrorPage()

  /**
   * Sets list of pages which to be used when an error occurs.
   *
   * @param errorPages list of error pages.
   */
  public final void setErrorPage(ErrorPageType[] errorPages) {
    this.errorPages = errorPages;
  }//end of setErrorPage(ErrorPageDescriptor[] errorPages)

  /**
   * Adds single error page to the list of error pages
   * for this web deployment descriptor.
   *
   * @param ePage the error page to be added.
   */
  private void addErrorPage(ErrorPageType ePage) {
    if (ePage != null) {
      addErrorPages(new ErrorPageType[]{ePage});
    }
  }//end of addErrorPage

  /**
   * Adds an array of new elements
   * If the elements already exist in the array they will be ignored.
   *
   * @param pages array of error pages
   */
  private void addErrorPages(ErrorPageType[] pages) {
    if (pages == null || pages.length == 0) {
      return;
    }
    if (errorPages == null || errorPages.length == 0) {
      setErrorPage(pages);
    } else {
      Vector merge = mergeArrays(errorPages, pages);
      errorPages = (ErrorPageType[]) merge.toArray(new ErrorPageType[merge.size()]);
    }
  }

  /**
   * Gets a list of JSP configurations.
   *
   * @return a list of configurations.
   */
  public final JspConfigType getJspConfig() {
    return this.jspConfig;
  }//end of JspConfig()

  /**
   * Sets the new list of JSP configurations.
   *
   * @param cfg a new list.
   */
  public final void setJspConfig(JspConfigType cfg) {
    this.jspConfig = cfg;
  }//end of setJspConfig

  /**
   * Gets the current list of security constraints.
   *
   * @return the current list of security constraints.
   */
  public final SecurityConstraintType[] getSecConstraints() {
    return this.securityConstraints;
  }//end of getSecConstraints()

  /**
   * Sets a new list of security constraints.
   *
   * @param secConstr a new list of security constraints.
   */
  private void setSecConstraints(SecurityConstraintType[] secConstr) {
    this.securityConstraints = secConstr;
  }//end of setSecConstraints(SecurityConstraintType[] secConstr)

  /**
   * Adds a single security constraint to the list.
   *
   * @param constraint a new security constraint.
   */
  private void addSecConstraint(SecurityConstraintType constraint) throws Exception {
    if (constraint != null) {
      WebResourceCollectionType[] webResourceCollectionTypes = constraint.getWebResourceCollection();
      if (webResourceCollectionTypes != null && webResourceCollectionTypes.length > 0) {
        for (int i = 0; i < webResourceCollectionTypes.length; i++) {
          UrlPatternType[] urlPatterns = webResourceCollectionTypes[i].getUrlPattern();
          if (urlPatterns != null && urlPatterns.length > 0) {
            for (int j = 0; j < urlPatterns.length; j++) {
              String urlPattern = urlPatterns[j].get_value();
              if (urlPattern.indexOf(0x0D) != -1 || urlPattern.indexOf(0x0A) != -1) {
                throw new WebDeploymentException(WebDeploymentException.INVALID_URL_PATTERN_IN_DEPLOYMENT_DESCRIPTOR_FOR_WEBAPPLICATION, new Object[]{urlPattern, ""});
              }
            }
          }
        }
      }
      addSecConstraints(new SecurityConstraintType[]{constraint});
    }
  }//end of addSecConstraint(SecurityConstraintType constraints)

  /**
   * Adds an array of new elements
   * If the elements already exist in the array they will be ignored.
   *
   * @param constraints array of security constraints
   */
  private void addSecConstraints(SecurityConstraintType[] constraints) {
    if (constraints == null || constraints.length == 0) {
      return;
    }
    if (securityConstraints == null || securityConstraints.length == 0) {
      setSecConstraints(constraints);
    } else {
      Vector merge = mergeArrays(securityConstraints, constraints);
      securityConstraints = (SecurityConstraintType[]) merge.toArray(new SecurityConstraintType[merge.size()]);
    }
  }

  /**
   * Gets the login configuration for the current web application.
   *
   * @return the login configuration.
   */
  public final LoginConfigType getLoginConfig() {
    return this.login;
  }//end of getLoginConfig()

  /**
   * Sets a new login configuration for the current web application.
   *
   * @param loginConfig a new configuration.
   */
  private void setLoginConfig(LoginConfigType loginConfig) {
    this.login = loginConfig;
  }//end of setLoginConfig(LoginConfigType loginConfig)

  /**
   * Gets current list of security roles.
   *
   * @return list of roles.
   */
  public final SecurityRoleType[] getSecurityRoles() {
    return this.securityRoles;
  }

  /**
   * Sets new list of security roles.
   *
   * @param secRoles new list of security roles.
   */
  private void setSecurityRoles(SecurityRoleType[] secRoles) {
    this.securityRoles = secRoles;
  }

  /**
   * Adds single security role to the list.
   *
   * @param role security role to be added.
   */
  private void addSecRole(SecurityRoleType role) {
    if (role != null) {
      addSecRoles(new SecurityRoleType[]{role});
    }
  }

  /**
   * Adds an array of new elements
   * If the elements already exist in the array they will be ignored.
   *
   * @param roles array of security roles
   */
  private void addSecRoles(SecurityRoleType[] roles) {
    if (roles == null || roles.length == 0) {
      return;
    }
    if (securityRoles == null || securityRoles.length == 0) {
      setSecurityRoles(roles);
    } else {
      Vector merge = mergeArrays(securityRoles, roles);
      securityRoles = (SecurityRoleType[]) merge.toArray(new SecurityRoleType[merge.size()]);
    }
  }

  /**
   * Gets the current list of environment entries.
   *
   * @return the current list of environment entries.
   */
  public final EnvEntryType[] getEnvEntries() {
    return this.envEntries;
  }//end of getEnvEntries()

  /**
   * Sets a new list of environment entries.
   *
   * @param envEntries a new list of environment entries.
   */
  public final void setEnvEntries(EnvEntryType[] envEntries) {
    this.envEntries = envEntries;
  }//end of setEnvEntries(EnvEntryDescriptor[] envEntries)

  /**
   * Adds a single environment entry to the list.
   *
   * @param entry a new entry.
   */
  public final void addEnvEntry(EnvEntryType entry) {
    if (entry != null) {
      addEnvEntries(new EnvEntryType[]{entry});
    }
  }//end of addEnvEntry(EnvEntryType entry)

  /**
   * Adds an array of new elements
   * If the elements already exist in the array they will be ignored.
   *
   * @param entries array of environment entries
   */
  private void addEnvEntries(EnvEntryType[] entries) {
    if (entries == null || entries.length == 0) {
      return;
    }
    if (envEntries == null || envEntries.length == 0) {
      setEnvEntries(entries);
    } else {
      Vector merge = mergeArrays(envEntries, entries);
      envEntries = (EnvEntryType[]) merge.toArray(new EnvEntryType[merge.size()]);
    }
  }

  /**
   * Gets the current list of EJB references.
   *
   * @return the current list of EJB references.
   */
  public final EjbRefType[] getEjbRefs() {
    return this.ejbRefs;
  }//end of getEjbRefs()

  /**
   * Sets a new EJB references list.
   *
   * @param ejbRefs a new list of EJB references.
   */
  public final void setEjbRefs(EjbRefType[] ejbRefs) {
    this.ejbRefs = ejbRefs;
  }//end of setEjbRefs(EjbRefType[] ejbRefs1)

  /**
   * Add a single EJB reference to the list of EJB references.
   *
   * @param ejbRef a new EJB reference.
   */
  public final void addEjbRef(EjbRefType ejbRef) {
    if (ejbRef != null) {
      addEjbRefs(new EjbRefType[]{ejbRef});
    }
  }//end of addEjbRef(EjbRefType ejbRef)

  /**
   * Adds an array of new elements
   * If the elements already exist in the array they will be ignored.
   *
   * @param refs array of references
   */
  private void addEjbRefs(EjbRefType[] refs) {
    if (refs == null || refs.length == 0) {
      return;
    }
    if (ejbRefs == null || ejbRefs.length == 0) {
      setEjbRefs(refs);
    } else {
      Vector merge = mergeArrays(ejbRefs, refs);
      ejbRefs = (EjbRefType[]) merge.toArray(new EjbRefType[merge.size()]);
    }
  }

  /**
   * Gets the current list of local EJB references.
   *
   * @return list of EJB references.
   */
  public final EjbLocalRefType[] getEjbLocalRefs() {
    return this.ejbLocalRefs;
  }//end of getEjbLocalRefs()

  /**
   * Sets a new local EJB references list.
   *
   * @param ejbLocalRefs local EJB references list
   */
  public final void setEjbLocalRefs(EjbLocalRefType[] ejbLocalRefs) {
    this.ejbLocalRefs = ejbLocalRefs;
  }//end of setEjbLocalRefs

  /**
   * Add a single local EJB reference to the list of local EJB references.
   *
   * @param ejbLocalRef local EJB reference
   */
  public final void addEjbLocalRef(EjbLocalRefType ejbLocalRef) {
    if (ejbLocalRef != null) {
      addEjbLocalRefs(new EjbLocalRefType[]{ejbLocalRef});
    }
  }//end of addEjbLocalRef

  /**
   * Adds an array of new elements
   * If the elements already exist in the array they will be ignored.
   *
   * @param refs array of references
   */
  private void addEjbLocalRefs(EjbLocalRefType[] refs) {
    if (refs == null || refs.length == 0) {
      return;
    }
    if (ejbLocalRefs == null || ejbLocalRefs.length == 0) {
      setEjbLocalRefs(refs);
    } else {
      Vector merge = mergeArrays(ejbLocalRefs, refs);
      ejbLocalRefs = (EjbLocalRefType[]) merge.toArray(new EjbLocalRefType[merge.size()]);
    }
  }//end of addEjbLocalRefs

  /**
   * Gets the current list of resource references from this web deployment descriptor.
   *
   * @return a current list of resource references.
   */
  public final ResourceRefType[] getResReferences() {
    return this.resReferences;
  }//end of getResReferences()

  /**
   * Sets a new list of resource references for this web deployment descriptor.
   *
   * @param reference a new list of resource references.
   */
  public final void setResReference(ResourceRefType[] reference) {
    resReferences = reference;
  }//end of setResReference(ResourceRefType[] reference)

  /**
   * Adds a single resource reference to the list.
   *
   * @param resReference resource reference which has to be added.
   */
  public final void addResReference(ResourceRefType resReference) {
    if (resReference != null) {
      addResReferences(new ResourceRefType[]{resReference});
    }
  }//end of addResReference(ResourceRefType resReference)

  /**
   * Adds an array of new elements
   * If the elements already exist in the array they will be ignored.
   *
   * @param refs array of resource references
   */
  private void addResReferences(ResourceRefType[] refs) {
    if (refs == null || refs.length == 0) {
      return;
    }
    if (resReferences == null || resReferences.length == 0) {
      setResReference(refs);
    } else {
      Vector merge = mergeArrays(resReferences, refs);
      resReferences = (ResourceRefType[]) merge.toArray(new ResourceRefType[merge.size()]);
    }
  }

  /**
   * Gets the current list of resource environment references from this web deployment descriptor.
   *
   * @return a current list of resource environment references.
   */
  public final ResourceEnvRefType[] getResEnvReferences() {
    return this.resEnvRefs;
  }//end of getResEnvReferences()

  /**
   * Sets a new list of resource references for this web deployment descriptor.
   *
   * @param resEnvReferences list of resource environment references.
   */
  public final void setResEnvReferences(ResourceEnvRefType[] resEnvReferences) {
    this.resEnvRefs = resEnvReferences;
  }//end of setResEnvReferences(ResourceEnvRefDescriptor[] resEnvReferences)

  /**
   * Adds a single resource environment reference to the list.
   *
   * @param resEnvReference resource environment reference which has to be added.
   */
  public final void addResEnvReference(ResourceEnvRefType resEnvReference) {
    if (resEnvReference != null) {
      addResEnvReferences(new ResourceEnvRefType[]{resEnvReference});
    }
  }//end of addResEnvReference

  /**
   * Adds an array of new elements
   * If the elements already exist in the array they will be ignored.
   *
   * @param refs array of resource environment references
   */
  private void addResEnvReferences(ResourceEnvRefType[] refs) {
    if (refs == null || refs.length == 0) {
      return;
    }
    if (resEnvRefs == null || resEnvRefs.length == 0) {
      setResEnvReferences(refs);
    } else {
      Vector merge = mergeArrays(resEnvRefs, refs);
      resEnvRefs = (ResourceEnvRefType[]) merge.toArray(new ResourceEnvRefType[merge.size()]);
    }
  }

  /**
   * Gets array of message destinations
   *
   * @return message destinations array
   */
  public final MessageDestinationType[] getMsgDestinations() {
    return msgDestinations;
  }

  /**
   * Sets the message destinations array
   *
   * @param types message destinations array
   */
  public final void setMsgDestinations(MessageDestinationType[] types) {
    msgDestinations = types;
  }

  /**
   * Adds new element in the message destination array.
   * If the element already exist in the array it will be ignored.
   *
   * @param destination message destination
   */
  private void addMsgDestination(MessageDestinationType destination) {
    if (destination != null) {
      addMsgDestinations(new MessageDestinationType[]{destination});
    }
  }//end of addMsgDestination

  /**
   * Adds an array of new elements
   * If the elements already exist in the array they will be ignored.
   *
   * @param dests array of message destinations
   */
  private void addMsgDestinations(MessageDestinationType[] dests) {
    if (dests == null || dests.length == 0) {
      return;
    }
    if (msgDestinations == null || msgDestinations.length == 0) {
      setMsgDestinations(dests);
    } else {
      Vector merge = mergeArrays(msgDestinations, dests);
      msgDestinations = (MessageDestinationType[]) merge.toArray(new MessageDestinationType[merge.size()]);
    }
  }

  /**
   * Gets array of message destination references
   *
   * @return array of message destination references
   */
  public final MessageDestinationRefType[] getMsgDestinationRefs() {
    return msgDestinationRefs;
  }

  /**
   * Sets the message destination references array
   *
   * @param types array of message destination references
   */
  public final void setMsgDestinationRefs(MessageDestinationRefType[] types) {
    msgDestinationRefs = types;
  }

  /**
   * Adds new element in the message destination references array.
   * If the element already exist in the array it will be ignored.
   *
   * @param destinationRef message destination reference
   */
  public final void addMsgDestinationRef(MessageDestinationRefType destinationRef) {
    if (destinationRef != null) {
      addMsgDestinationRefs(new MessageDestinationRefType[]{destinationRef});
    }
  }//end of addMsgDestinationRef

  /**
   * Adds an array of new elements
   * If the elements already exist in the array they will be ignored.
   *
   * @param refs array of message destination references
   */
  private void addMsgDestinationRefs(MessageDestinationRefType[] refs) {
    if (refs == null || refs.length == 0) {
      return;
    }
    if (msgDestinationRefs == null || msgDestinationRefs.length == 0) {
      setMsgDestinationRefs(refs);
    } else {
      Vector merge = mergeArrays(msgDestinationRefs, refs);
      msgDestinationRefs = (MessageDestinationRefType[]) merge.toArray(new MessageDestinationRefType[merge.size()]);
    }
  }

  /**
   * Gets array of webservices references
   *
   * @return array of webservices references
   */
  public final ServiceRefType[] getServiceRefs() {
    return serviceRefs;
  }

  /**
   * Sets the array of webservices references
   *
   * @param types array of webservices references
   */
  private void setServiceRefs(ServiceRefType[] types) {
    serviceRefs = types;
  }

  /**
   * Adds new element in the webservices references array.
   * If the element already exist in the array it will be ignored.
   *
   * @param serviceRef webservice reference
   */
  public final void addServiceRef(ServiceRefType serviceRef) {
    if (serviceRef != null) {
      addServiceRefs(new ServiceRefType[]{serviceRef});
    }
  }//end of addServiceRef

  /**
   * Adds an array of new elements
   * If the elements already exist in the array they will be ignored.
   *
   * @param refs array of webservice references
   */
  private void addServiceRefs(ServiceRefType[] refs) {
    if (refs == null || refs.length == 0) {
      return;
    }
    if (serviceRefs == null || serviceRefs.length == 0) {
      setServiceRefs(refs);
    } else {
      Vector merge = mergeArrays(serviceRefs, refs);
      serviceRefs = (ServiceRefType[]) merge.toArray(new ServiceRefType[merge.size()]);
    }
  }

  public final LifecycleCallbackType[] getPreDestroy() {
    return preDestroy;
  }

  private void setPreDestroy(LifecycleCallbackType[] preDestroy) {
    this.preDestroy = preDestroy;
  }

  public final void addPreDestroy(LifecycleCallbackType preDestroy) {
    if (preDestroy != null) {
      addPreDestroys(new LifecycleCallbackType[]{preDestroy});
    }
  }

  private void addPreDestroys(LifecycleCallbackType[] types) {
    if (types == null || types.length == 0) {
      return;
    }
    if (preDestroy == null || preDestroy.length == 0) {
      setPreDestroy(types);
    } else {
      Vector merge = mergeArrays(preDestroy, types);
      preDestroy = (LifecycleCallbackType[]) merge.toArray(new LifecycleCallbackType[merge.size()]);
    }
  }

  public final LifecycleCallbackType[] getPostConstruct() {
    return postConstruct;
  }

  private void setPostConstruct(LifecycleCallbackType[] postConstruct) {
    this.postConstruct = postConstruct;
  }

  public final void addPostConstruct(LifecycleCallbackType postConstruct) {
    if (postConstruct != null) {
      addPostConstructs(new LifecycleCallbackType[]{postConstruct});
    }
  }

  private void addPostConstructs(LifecycleCallbackType[] types) {
    if (types == null || types.length == 0) {
      return;
    }
    if (postConstruct == null || postConstruct.length == 0) {
      setPostConstruct(types);
    } else {
      Vector merge = mergeArrays(postConstruct, types);
      postConstruct = (LifecycleCallbackType[]) merge.toArray(new LifecycleCallbackType[merge.size()]);
    }
  }

  public final PersistenceUnitRefType[] getPersistenceUnitRefs() {
    return persistenceUnitRefs;
  }

  private void setPersistenceUnitRefs(PersistenceUnitRefType[] persistenceUnitRefs) {
    this.persistenceUnitRefs = persistenceUnitRefs;
  }

  public final void addPersistenceUnitRef(PersistenceUnitRefType refs) {
    if (refs != null) {
      addPersistenceUnitRefs(new PersistenceUnitRefType[]{refs});
    }
  }

  private void addPersistenceUnitRefs(PersistenceUnitRefType[] refs) {
    if (refs == null || refs.length == 0) {
      return;
    }
    if (persistenceUnitRefs == null || persistenceUnitRefs.length == 0) {
      setPersistenceUnitRefs(refs);
    } else {
      Vector merge = mergeArrays(persistenceUnitRefs, refs);
      persistenceUnitRefs = (PersistenceUnitRefType[]) merge.toArray(new PersistenceUnitRefType[merge.size()]);
    }
  }

  public final PersistenceContextRefType[] getPersistenceContextRefs() {
    return persistenceContextRefs;
  }

  private void setPersistenceContextRefs(PersistenceContextRefType[] persistenceContextRefs) {
    this.persistenceContextRefs = persistenceContextRefs;
  }

  public final void addPersistenceContextRef(PersistenceContextRefType refs) {
    if (refs != null) {
      addPersistenceContextRefs(new PersistenceContextRefType[]{refs});
    }
  }

  private void addPersistenceContextRefs(PersistenceContextRefType[] refs) {
    if (refs == null || refs.length == 0) {
      return;
    }
    if (persistenceContextRefs == null || persistenceContextRefs.length == 0) {
      setPersistenceContextRefs(refs);
    } else {
      Vector merge = mergeArrays(persistenceContextRefs, refs);
      persistenceContextRefs = (PersistenceContextRefType[]) merge.toArray(new PersistenceContextRefType[merge.size()]);
    }
  }

  /**
   * Gets list with locale encoding mappings
   *
   * @return list with locale encoding mappings
   */
  public final LocaleEncodingMappingListType getLocaleEncodingMappings() {
    return localeEncodingMappings;
  }

  /**
   * Sets the list with locale encoding mappings
   *
   * @param type list with locale encoding mappings
   */
  public final void setLocaleEncodingMappings(LocaleEncodingMappingListType type) {
    localeEncodingMappings = type;
  }

  /**
   * Adds a LocaleEncodingMappingList to the existing list
   *
   * @param mappings LocaleEncodingMappingList
   */
  private void addLocaleEncodingMappingList(LocaleEncodingMappingListType mappings) {
    if (mappings == null || mappings.getLocaleEncodingMapping() == null || mappings.getLocaleEncodingMapping().length == 0) {
      return;
    }
    if (localeEncodingMappings == null || localeEncodingMappings.getLocaleEncodingMapping() == null || localeEncodingMappings.getLocaleEncodingMapping().length == 0) {
      setLocaleEncodingMappings(mappings);
    } else {
      LocaleEncodingMappingType[] oldMappings = localeEncodingMappings.getLocaleEncodingMapping();
      LocaleEncodingMappingType[] newMappings = mappings.getLocaleEncodingMapping();
      LocaleEncodingMappingType[] concatenation = new LocaleEncodingMappingType[oldMappings.length + newMappings.length];
      System.arraycopy(oldMappings, 0, concatenation, 0, oldMappings.length);
      System.arraycopy(newMappings, 0, concatenation, oldMappings.length, newMappings.length);
      localeEncodingMappings.setLocaleEncodingMapping(concatenation);
    }
  }

  /**
   * Gets array of localized descriptions of the deployment descriptor
   *
   * @return array of localized descriptions
   */
  public final DescriptionType[] getDescriptions() {
    return descriptions;
  }

  /**
   * Sets the array of descriptions
   *
   * @param types array of localized descriptions
   */
  public final void setDescriptions(DescriptionType[] types) {
    descriptions = types;
  }

  /**
   * Adds new element in the descriptions array.
   * If the element already exist in the array it will be ignored.
   *
   * @param description
   */
  public final void addDescription(DescriptionType description) {
    if (description != null) {
      addDescriptions(new DescriptionType[]{description});
    }
  }//end of addDescription

  /**
   * Adds an array of new elements in the descriptions array.
   * If the elements already exist in the array they will be ignored.
   *
   * @param descs array of descriptions
   */
  private void addDescriptions(DescriptionType[] descs) {
    if (descs == null || descs.length == 0) {
      return;
    }
    if (descriptions == null || descriptions.length == 0) {
      setDescriptions(descs);
    } else {
      Vector merge = mergeArrays(descriptions, descs);
      descriptions = (DescriptionType[]) merge.toArray(new DescriptionType[merge.size()]);
    }
  }//end of addDescriptions


  /**
   * Gets array of localized display names of the deployment descriptor
   *
   * @return array of localized display names
   */
  public final DisplayNameType[] getDisplayNames() {
    return displayNames;
  }

  /**
   * Sets the array of display names
   *
   * @param types array of localized display names
   */
  public final void setDisplayNames(DisplayNameType[] types) {
    displayNames = types;
  }

  /**
   * Adds new element in the display names array.
   * If the element already exist in the array it will be ignored.
   *
   * @param name display name
   */
  public final void addDisplayName(DisplayNameType name) {
    if (name != null) {
      addDisplayNames(new DisplayNameType[]{name});
    }
  }//end of addDisplayName

  /**
   * Adds an array of new elements in the display names array.
   * If the elements already exist in the array they will be ignored.
   *
   * @param names array of names
   */
  private void addDisplayNames(DisplayNameType[] names) {
    if (names == null || names.length == 0) {
      return;
    }
    if (displayNames == null || displayNames.length == 0) {
      setDisplayNames(names);
    } else {
      Vector merge = mergeArrays(displayNames, names);
      displayNames = (DisplayNameType[]) merge.toArray(new DisplayNameType[merge.size()]);
    }
  }//end of addDisplayNames

  /**
   * Gets array of icons
   *
   * @return array of icons
   */
  public final IconType[] getIcons() {
    return icons;
  }

  /**
   * Sets the array of icons
   *
   * @param types array of icons
   */
  private void setIcons(IconType[] types) {
    icons = types;
  }

  /**
   * Adds new element in the icons array.
   * If the element already exist in the array it will be ignored.
   *
   * @param icon
   */
  public final void addIcon(IconType icon) {
    if (icon != null) {
      addIcons(new IconType[]{icon});
    }
  }//end of addIcon

  /**
   * Adds an array of new elements
   * If the elements already exist in the array they will be ignored.
   *
   * @param ics array of icons
   */
  private void addIcons(IconType[] ics) {
    if (ics == null || ics.length == 0) {
      return;
    }
    if (icons == null || icons.length == 0) {
      setIcons(ics);
    } else {
      Vector merge = mergeArrays(icons, ics);
      icons = (IconType[]) merge.toArray(new IconType[merge.size()]);
    }
  }

  public final WebAppVersionType getWebAppVersion() {
    return webAppVersion;
  }

  public final void setWebAppVersion(WebAppVersionType webAppVersion) {
    this.webAppVersion = webAppVersion;
  }

  public final boolean isMetaDataComplete() {
    return metaDataComplete;
  }

  public final void setMetaDataComplete(boolean metaDataComplete) {
    this.metaDataComplete = metaDataComplete;
  }

  private Vector mergeArrays(Object[] array1, Object[] array2) {
    if (array1 == null && array2 == null) {
      return null;
    }

    if (array1 != null && array2 == null) {
      return new Vector(Arrays.asList(array1));
    }

    if (array1 == null && array2 != null) {
      return new Vector(Arrays.asList(array2));
    }

    Vector result = new Vector(Arrays.asList(array1));

    int j = 0;
    for (int i = 0; i < array2.length; i++) {
      for (j = 0; j < array1.length; j++) {
        if (array2[i].equals(array1[j])) {
          break;
        }
      }
      if (j == array1.length) {
        result.add(array2[i]);
      }
    }
    return result;
  }//end of mergeArrays(Object[] array1, Object[] array2)

}//end of class

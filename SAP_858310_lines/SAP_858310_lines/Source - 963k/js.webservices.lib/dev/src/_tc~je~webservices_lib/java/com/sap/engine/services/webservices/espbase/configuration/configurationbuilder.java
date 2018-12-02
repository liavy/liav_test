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
package com.sap.engine.services.webservices.espbase.configuration;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.rmi.server.UID;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sap.engine.lib.xml.dom.DOM;
import com.sap.engine.services.webservices.espbase.client.bindings.PublicProperties;
import com.sap.engine.services.webservices.espbase.configuration.cfg.SoapApplicationRegistry;
import com.sap.engine.services.webservices.espbase.configuration.exceptions.ExceptionConstants;
import com.sap.engine.services.webservices.espbase.configuration.exceptions.ResourceAccessor;
import com.sap.engine.services.webservices.espbase.configuration.idempotency.IdempotencyConfigurationAppender;
import com.sap.engine.services.webservices.espbase.configuration.marshallers.MarshallerRegistry;
import com.sap.engine.services.webservices.espbase.configuration.marshallers.wsrm.RMConfigurationMarshaller;
import com.sap.engine.services.webservices.espbase.configuration.misc.DTProfileConvertor;
import com.sap.engine.services.webservices.espbase.wsdl.Base;
import com.sap.engine.services.webservices.espbase.wsdl.Binding;
import com.sap.engine.services.webservices.espbase.wsdl.Definitions;
import com.sap.engine.services.webservices.espbase.wsdl.Endpoint;
import com.sap.engine.services.webservices.espbase.wsdl.ExtensionElement;
import com.sap.engine.services.webservices.espbase.wsdl.Interface;
import com.sap.engine.services.webservices.espbase.wsdl.ObjectList;
import com.sap.engine.services.webservices.espbase.wsdl.Operation;
import com.sap.engine.services.webservices.espbase.wsdl.SOAPBinding;
import com.sap.engine.services.webservices.espbase.wsdl.SOAPBindingOperation;
import com.sap.engine.services.webservices.espbase.wsdl.wsdl11.WSDL11Constants;
import com.sap.engine.services.webservices.jaxrpc.encoding.XMLMarshaller;
import com.sap.engine.services.webservices.wspolicy.Policy;
import com.sap.engine.services.webservices.wspolicy.PolicyDomLoader;
import com.sap.engine.services.webservices.wspolicy.PolicyException;
import com.sap.tc.logging.Location;

/**
 * Javadoc goes here...
 * 
 * Copyright (c) 2004, SAP-AG
 * 
 * @author Dimitar Angelov
 * @version 1.0, 2004-12-2
 */
public class ConfigurationBuilder implements ConfigurationManipulator {

  public static final String DEFAULT_SERVICE_NAME = "DefaultService";

  public static final String DEFAULT_SERVICE_NS = "urn:defaultServiceNS";

  private PolicyConvertor policyConvertor = new PolicyConvertor();

  private FeatureConvertor featureConvertor = new FeatureConvertor();

  private List confMarshallers = new ArrayList();

  private Map intfDefs = new Hashtable(); // keys Interface QName, values
                                          // InterfaceDefinition

  /**
   * Constructs instance, initialized with specified factory
   * 
   * @param factory
   *          factory initalized with IConfiguraionMarshaler instances
   * @throws ConfigurationMarshallerException
   */
  public ConfigurationBuilder(ConfigurationMarshallerFactory factory) throws ConfigurationMarshallerException {
    reInitialize(factory);
  }

  /**
   * Initializes the instance using <code>factory</code> object.
   */
  public synchronized void reInitialize(ConfigurationMarshallerFactory factory) throws ConfigurationMarshallerException {
    try {
      policyConvertor.clearMarshallers();
      this.confMarshallers = factory.getMarshallers();
      policyConvertor.addMarshallers(this.confMarshallers);
    } catch (Exception e) {
      throw new ConfigurationMarshallerException(e);
    }
  }

  /**
   * Constructs instance, initialized with default configuration marshaller
   * factory. Calling this constructor is equal to call
   * <code>new ConfigurationBuilder(ConfigurationMarshallerFactory.newInstance())</code>
   * 
   * @throws ConfigurationMarshallerException
   */
  public ConfigurationBuilder() throws ConfigurationMarshallerException {
    this(MarshallerRegistry.getInitializedFactory());
  }

  /**
   * Converts <code>pList</code> properties into DOM Element assertions.
   * 
   * @param pList
   *          properties
   * @param rootDoc
   *          Document object which to be use for creation of DOM Elements.
   * @return List containing DOM Element objects.
   */
  public synchronized List marshalAssertions(PropertyListType pList, Document rootDoc, String wsdlLevel, int mode)
      throws ConfigurationMarshallerException {
    return policyConvertor.marshalAssertions(pList, rootDoc, wsdlLevel, mode);
  }

  /**
   * Checks whether the <code>bh</code> object contains properties defined in
   * NW04.
   */
  public synchronized boolean isNW04PropertyAnnotated(Behaviour bh, StringBuffer errorMSG)
      throws ConfigurationMarshallerException {
    PropertyType[] pList = bh.getSinglePropertyList().getProperty();
    QName qname;
    for (int i = 0; i < pList.length; i++) {
      qname = new QName(pList[i].getNamespace(), pList[i].getName());
      if (!isNW04Defined(qname, IConfigurationMarshaller.PROVIDER_MODE)) {
        errorMSG.append("Property " + qname + " is not defined in NW04.");
        return false;
      }
    }
    return true;
  }

  /**
   * Filters the properties, <code>props</code>, that are about to be shown
   * in sap-wsdl as features.
   * 
   * @param props
   *          properties to be filtered
   * @param level
   *          the configuration level of the properties
   * @param mode
   *          the processing mode - client or server
   * @return returns a filtered array of properties
   */
  public synchronized PropertyType[] filterPropertiesForFeatures(Behaviour parent, PropertyType[] props, String level,
      int mode) throws ConfigurationMarshallerException {
    // check that for each property there is configuration marshaller
    checkPropertyListAgainsMarshallers(props, this.confMarshallers, mode);
    ArrayList res = new ArrayList();
    PropertyType kProps[];
    IConfigurationMarshaller cM;
    for (int i = 0; i < confMarshallers.size(); i++) {
      cM = (IConfigurationMarshaller) confMarshallers.get(i);
      kProps = getPropertiesForMarshaller(cM, props, mode);
      List tmpList = cM.processPropertiesForNW04Features(parent, kProps, level, mode);
      if (tmpList != null) {
        res.addAll(tmpList);
      }
    }
    return (PropertyType[]) res.toArray(new PropertyType[res.size()]);
  }

  /**
   * Builds configuration from <code>def</code> in
   * IConfigurationMarshaller.CONSUMER_MODE mode.
   */
  public synchronized ConfigurationRoot create(Definitions def) throws Exception {
    return this.create(def, IConfigurationMarshaller.CONSUMER_MODE);
  }

  /**
   * Builds configuration from <code>def</code> and specified
   * <code>mode</code> - IConfigurationMarshaller.CONSUMER_MODE or
   * IConfigurationMarshaller.PROVIDER_MODE
   */
  public synchronized ConfigurationRoot create(Definitions def, int mode) throws Exception {
    // clear state
    intfDefs.clear();

    // find what is the annotation type of the definition
    boolean isP = isPolicyAnnotated(def);
    // process Interface(s)
    Interface intf;
    ObjectList intfs = def.getInterfaces();
    for (int i = 0; i < intfs.getLength(); i++) {
      intf = (Interface) intfs.item(i);
      intfDefs.put(intf.getName(), createInterfaceDefinition(intf, isP, mode));
    }

    // process service(s) and binding(s)
    Service[] ss;
    ServiceData s;
    ObjectList sL = def.getServices();
    if (sL.getLength() > 0) { // wsdl with service(s)
      com.sap.engine.services.webservices.espbase.wsdl.Service wsdlS;
      ss = new Service[sL.getLength()];
      for (int i = 0; i < sL.getLength(); i++) {
        wsdlS = (com.sap.engine.services.webservices.espbase.wsdl.Service) sL.item(i);
        // processing Endpoints
        List bDs = new ArrayList();
        QName intfQn, bQn;
        BindingData bD;
        ObjectList endPs = wsdlS.getEndpoints();
        for (int e = 0; e < endPs.getLength(); e++) {
          bQn = ((Endpoint) endPs.item(e)).getBinding();
          intfQn = def.getBinding(bQn).getInterface();
          // set references to Variant
          InterfaceDefinition intDef = (InterfaceDefinition) intfDefs.get(intfQn);
          List<BindingData> bDList = createBindingData((Endpoint) endPs.item(e), def, isP, mode);
          for (BindingData bindingData2 : bDList) {
            // connect BindingData to Variant. The InterfaceDefinition must have
            // exactly one variant
            bindingData2.setVariantName(intDef.getVariant()[0].getName());
            bDs.add(bindingData2);
          }
        }
        // create ServiceData
        s = new ServiceData();
        s.setBindingData((BindingData[]) bDs.toArray(new BindingData[bDs.size()]));
        s.setName(wsdlS.getName().getLocalPart());
        s.setNamespace(wsdlS.getName().getNamespaceURI());
        // create Service
        Service service = new Service();
        service.setName(s.getName());
        service.setServiceData(s);
        ss[i] = service;
      }
    } else { // parse the bindings. WSDL without service(s)
      List bDs = new ArrayList();
      Binding curB;
      QName intfQn;
      ObjectList bindings = def.getBindings();
      for (int i = 0; i < bindings.getLength(); i++) {
        curB = (Binding) bindings.item(i);
        List<BindingData> bDList = createBindingData(curB, isP, mode);
        intfQn = curB.getInterface();
        // set references to Variant.
        InterfaceDefinition intDef = (InterfaceDefinition) intfDefs.get(intfQn);
        // connect BindingData to Variant. The InterfaceDefinition must have
        // exactly one variant
        for (BindingData bindingData : bDList) {
          bindingData.setVariantName(intDef.getVariant()[0].getName());
          bDs.add(bindingData);
        }
      }
      // create default service to hold the BDs
      s = new ServiceData();
      s.setBindingData((BindingData[]) bDs.toArray(new BindingData[bDs.size()]));
      s.setName(DEFAULT_SERVICE_NAME);
      s.setNamespace(DEFAULT_SERVICE_NS);
      Service service = new Service();
      service.setName(s.getName());
      service.setServiceData(s);
      ss = new Service[1];
      ss[0] = service;
    }
    // create InterfaceDefinitionCollection
    InterfaceDefinitionCollection iDefCol = new InterfaceDefinitionCollection();
    Collection values = intfDefs.values();
    InterfaceDefinition arrIDefs[] = (InterfaceDefinition[]) values.toArray(new InterfaceDefinition[values.size()]);
    iDefCol.setInterfaceDefinition(arrIDefs);
    // create ServiceCollection
    ServiceCollection sCol = new ServiceCollection();
    sCol.setService(ss);
    // create Configuration Root
    ConfigurationRoot cRoot = new ConfigurationRoot();
    cRoot.setDTConfig(iDefCol);
    cRoot.setRTConfig(sCol);
    // check for DTProfiles, available only in ESR wsdls
    ObjectList oList = def.getChildren(ExtensionElement.EXTENSION_ELEMENT_ID);
    for (int i = 0; i < oList.getLength(); i++) {
      Element elem = ((ExtensionElement) oList.item(i)).getContent();
      if (DTProfileConvertor.DTPROFILE_NS.equals(elem.getNamespaceURI()) && "properties".equals(elem.getLocalName())) {
        // DTProfileConvertor.
        InterfaceDefinition[] intfDefs = iDefCol.getInterfaceDefinition();
        if (intfDefs.length == 1) {
          Variant v[] = intfDefs[0].getVariant();
          BindingData bd = null;
          Service srvs[] = cRoot.getRTConfig().getService();
          if (srvs.length == 1) {
            BindingData[] bds = srvs[0].getServiceData().getBindingData();
            if (bds.length == 1) {
              bd = bds[0];
            }
          }
          if (v.length == 1 && bd != null) { // only wsdl with single BD and
                                              // InterfaceData are parsed for
                                              // special ESR artefacts
            // cRoot.getRTConfig().getService()[0].get
            InterfaceData intfData = v[0].getInterfaceData();

            DTProfileConvertor.applyDTProfiles(intfData, bd, elem, mode);
          }
        }
      }
    }

    applySoapApplicationIDs(cRoot.getDTConfig().getInterfaceDefinition(), mode);
    IdempotencyConfigurationAppender.appendIdempotencyConfiguration(def, cRoot);
    return cRoot;
  }

  private void applySoapApplicationIDs(InterfaceDefinition[] defs, int mode) throws Exception {
    for (int i = 0; i < defs.length; i++) {
      InterfaceDefinition def = defs[i];
      SoapApplicationRegistry.applySoapApplicationProperty(def, CONSUMER_MODE == mode);
    }
  }

  private InterfaceDefinition createInterfaceDefinition(Interface intf, boolean isP, int mode) throws Exception {
    Element def = (Element) intf.getDomElement().getParentNode();
    InterfaceData intfData = null;
    // processing the alternatives on the interface level
    if (isP) {
      Policy p = loadPolicies(intf, def);
      List listIntfDatas = policyConvertor.unmarshalAlternatives(InterfaceData.class, p,
          IConfigurationMarshaller.INTERFACE_LEVEL, mode);
      if (listIntfDatas.size() >= 1) {
        intfData = (InterfaceData) listIntfDatas.get(0);
      } else {
        intfData = new InterfaceData();
      }
    } else {
      intfData = new InterfaceData();
      List features = loadFeatures(intf, def);
      featureConvertor.unmarshal(intfData, features);
    }
    // processing the operation alternatives
    List listOpDatas = new ArrayList();
    ObjectList ops = intf.getOperations();
    for (int i = 0; i < ops.getLength(); i++) {
      List tmpList = createOperationData(ops.item(i), def, isP, IConfigurationMarshaller.INTERFACE_OPERATION_LEVEL,
          mode);
      OperationData opD;
      if (tmpList.size() >= 1) { // if only one alternative is available use it
        opD = (OperationData) tmpList.get(0);
      } else { // ignore all alternative and create empty one.
        opD = new OperationData();
      }
      opD.setName(((Operation) ops.item(i)).getName());
      listOpDatas.add(opD);
    }
    intfData.setOperation((OperationData[]) listOpDatas.toArray(new OperationData[listOpDatas.size()]));

    if (!isP) { // for features wsdl invoke NW04->NY convertion method
      InterfaceData newID = intfData.makeCopyWithoutProperties();
      this.convertNW04CfgToNY(intfData, newID, mode);
      intfData = newID;
    }
    // InterfaceData intfData = (InterfaceData) listIntfDatas.get(0); //exactly
    // one entity must be available
    intfData.setName(intf.getName().getLocalPart());
    intfData.setNamespace(intf.getName().getNamespaceURI());

    // apply defaults
    if (isP) {
      applyDPToInterfaceData(intfData, intf, mode);
    }
    // checking IntefaceDatas for validity
    checkBehaviorConfiguration(intfData, mode);
    // create variant
    Variant v = new Variant();
    v.setInterfaceData(intfData);
    v.setName(intf.getName().getLocalPart());
    // create IntfDefinition
    InterfaceDefinition intDef = new InterfaceDefinition();
    intDef.setName(intf.getName().getLocalPart());
    intDef.setId(new UID().toString() + "_" + intf.getName().toString());
    intDef.setVariant(new Variant[] { v });

    return intDef;
  }

  /**
   * Returns BindingData object
   */
  private List<BindingData> createBindingData(Endpoint ep, Definitions def, boolean isP, int mode) throws Exception {
    Binding binding = def.getBinding(ep.getBinding());
    List<BindingData> bDList = createBindingData(binding, isP, mode);
    String name;
    // set URL value
    String url = ep.getProperty(Endpoint.URL);
    for (int i = 0; i < bDList.size(); i++) {
      BindingData bindingData = bDList.get(i);
      bindingData.setUrl(url);
      if (url != null && url.contains(PublicProperties.TRANSPORT_BINDING_LOCAL_CALL_HOST_PORT)) { // this
                                                                                                  // is a
                                                                                                  // local
                                                                                                  // call
                                                                                                  // endpoint,
                                                                                                  // local
                                                                                                  // call-ize
                                                                                                  // the
                                                                                                  // binding
                                                                                                  // data
                                                                                                  // too
        PropertyListType plt = bindingData.getSinglePropertyList();
        plt.addProperty(PublicProperties.TRANSPORT_BINDING_FEATURE, PublicProperties.TRANSPORT_BINDING_LOCAL_CALL,
            "true");
      }
      // set the name attribute equal to the port@name
      name = ep.getName();
      if (i == 0) {
        bindingData.setName(name);
      } else {
        bindingData.setName(name + i);
      }
    }
    return bDList;
  }

  /**
   * Returns BindingData object
   */
  private List<BindingData> createBindingData(Binding b, boolean isP, int mode) throws Exception {
    Element def = (Element) b.getDomElement().getParentNode();
    List<BindingData> res = new ArrayList<BindingData>();

    if (isP) {
      Policy p = loadPolicies(b, def);
      List bDs = policyConvertor.unmarshalAlternatives(BindingData.class, p, IConfigurationMarshaller.BINDING_LEVEL,
          mode);
      if (bDs.size() >= 1) { // in case exactly one alternative is resolved,
                              // use it
        // bD = (BindingData) bDs.get(0);
        res.addAll(bDs);
      } else { // otherwize create an empty one
        res.add(new BindingData());
      }
    } else {
      List features = loadFeatures(b, def);
      BindingData bD = new BindingData();
      featureConvertor.unmarshal(bD, features);
      res.add(bD);
    }

    BindingData bD;
    for (int bb = 0; bb < res.size(); bb++) {
      bD = (BindingData) res.get(bb);

      ObjectList operations = null;
      if (b instanceof SOAPBinding) {
        SOAPBinding soapBinding = (SOAPBinding) b;
        operations = soapBinding.getOperations();
        SOAPBindingOperation cO;
        OperationData opData;

        OperationData[] opDataArr = new OperationData[operations.getLength()];
        for (int i = 0; i < operations.getLength(); i++) {
          cO = (SOAPBindingOperation) operations.item(i);
          List l = createOperationData(cO, def, isP, IConfigurationMarshaller.BINDING_OPERATION_LEVEL, mode);
          if (l.size() >= 1) { // in case exactly one alternative is resolved,
                                // use it
            opData = (OperationData) l.get(0);
          } else {// create an empty one
            opData = new OperationData();
          }
          opData.setName(cO.getName());
          opDataArr[i] = opData;
        }
        bD.setOperation(opDataArr);
      }
      if (!isP) { // for features wsdl invoke NW04->NY method
        BindingData newBD = bD.makeCopyWithoutProperties();
        this.convertNW04CfgToNY(bD, newBD, mode);
        bD = newBD;
        res.set(bb, bD);
      }

      // setting name
      bD.setBindingNamespace(b.getName().getNamespaceURI());
      bD.setBindingName(b.getName().getLocalPart());
      bD.setName(b.getName().getLocalPart());
      InterfaceDefinition intfDef = (InterfaceDefinition) intfDefs.get(b.getInterface());
      bD.setInterfaceId(intfDef.getId());
      // apply defaults
      if (isP) {
        applyDPToBindingData(bD, mode);
        if (intfDef.getVariant().length > 0) { // this always should be
                                                // 'true'...
          applyDefaultPropertiesNew(intfDef.getVariant()[0].getInterfaceData(), bD, mode);
        }
      }
      // check BindingData for validity
      checkBehaviorConfiguration(bD, mode);
    }
    return mode == IConfigurationMarshaller.PROVIDER_MODE ? mergeBindingData(res) : res;
    // return res;
  }

  private List<BindingData> mergeBindingData(List<BindingData> bDatas) {
    if (bDatas.size() < 2) {
      return bDatas;
    }
    int startPos = 0;
    BindingData combined;
    while (startPos < bDatas.size() -1){
      for (int index = startPos; index < bDatas.size() - 1;){
        combined = combineBindingDatas(bDatas.get(index), bDatas.get(index + 1));
        if (combined != null) {
          bDatas.set(index, combined);
          bDatas.remove(index + 1);
        } else {
          index++;
        }
      }
      startPos ++;
    }
    return bDatas;
  }

  private BindingData combineBindingDatas(BindingData bd1, BindingData bd2) { // add bd2's properties to bd if they only differ in their security properties
    if (compareBindingDatasNonSecurityProperties(bd1, bd2)) { // combine
      // combine properties from TRANSPORT_WARANTEE_FEATURE and AUTHENTICATION_FEATURE namespaces
      return doCombineBindingDatas(bd1, bd2);
    } else {
      return null;
    }
  }

  private BindingData doCombineBindingDatas(BindingData bd1, BindingData bd2) {
    PropertyListType plt1 = bd1.getSinglePropertyList();
    PropertyListType plt2 = bd2.getSinglePropertyList();
    combinePropertiesFromNamespace(PublicProperties.AUTHENTICATION_FEATURE, plt1, plt2);
    combinePropertiesFromNamespace(PublicProperties.TRANSPORT_WARANTEE_FEATURE, plt1, plt2);
    
    /*List<PropertyType> toBeAdded = new ArrayList<PropertyType>();
    for (PropertyType pt : pt1) {
      value1 = pt.get_value();
      ptt = plt2.getProperty(pt.getNamespace(), pt.getName());
      if (ptt == null) {
        continue;
      }
      if ((value1 == null && ptt.get_value() == null) || value1.equals(ptt.get_value())) {
        continue;
      }
      toBeAdded.add(ptt);
    }
    for (PropertyType pt : toBeAdded) {
      plt1.addProperty(pt);
    }*/
    return bd1;
  }
  
  private void combinePropertiesFromNamespace(String namespace, PropertyListType plt1, PropertyListType plt2){
    PropertyType[] pt1 = plt1.getPropertiesByNS(namespace);
    String value1;
    PropertyType ptt;
    
    List<PropertyType> toBeAdded = new ArrayList<PropertyType>();
    for (PropertyType pt : pt1) {
      value1 = pt.get_value();
      ptt = plt2.getProperty(pt.getNamespace(), pt.getName());
      if (ptt == null) {
        continue;
      }
      if ((value1 == null && ptt.get_value() == null) || value1.equals(ptt.get_value())) {
        continue;
      }
      toBeAdded.add(ptt);
    }
    for (PropertyType pt : toBeAdded) {
      plt1.addProperty(pt);
    }
  }

  private boolean compareBindingDatasNonSecurityProperties(BindingData bd1, BindingData bd2) {
    PropertyListType pl1 = bd1.getSinglePropertyList();
    PropertyListType pl2 = bd2.getSinglePropertyList();
    PropertyType[] pt1 = pl1.getProperty();
    String ns;
    PropertyType ptt;
    for (PropertyType pt : pt1) {
      ns = pt.getNamespace();
      if (PublicProperties.AUTHENTICATION_FEATURE.equals(ns) || PublicProperties.TRANSPORT_WARANTEE_FEATURE.equals(ns)) { // don't compare security-related properties
        continue;
      }
      ptt = pl2.getProperty(pt.getNamespace(), pt.getName());
      if (ptt == null) {
        return false;
      }
      if (ptt.get_value() != null) {
        if (!ptt.get_value().equals(pt.get_value())) {
          return false;
        }
      } else {
        if (pt.get_value() != null) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Returns a List of OperationData objects. If the <code>op</code> object
   * does not contains alternatives, or it contains but they where not abled to
   * be resoled, a default OperationData entity is created. Also the default
   * operation-level properties are applied.
   */
  private List createOperationData(Base op, Element defE, boolean isP, String level, int mode) throws Exception {
    List result;
    if (isP) {
      Policy p = loadPolicies(op, defE);
      result = policyConvertor.unmarshalAlternatives(OperationData.class, p, level, mode);
    } else {
      List features = loadFeatures(op, defE);
      OperationData opData = new OperationData();
      featureConvertor.unmarshal(opData, features);
      result = new ArrayList();
      result.add(opData);
    }

    return result;
  }

  /**
   * Reads all <wsp:PolicyReference>, <wsp:Policy> elements and
   * 
   * @wsp:policyURIs attr. Merges all referenced policies into on.
   * 
   * @param base
   *          The conmponent which policies should be resolved
   * @param baseRoot
   *          The root org.w3c.dom.Element (wsdl:definitions) which contains the
   *          policy definitions.
   */
  public static Policy loadPolicies(Base base, Element baseRoot) throws PolicyException {
    List elements = getExtensionElements(base, Policy.POLICY_NS, Policy.POLICYREFERENCE_ELEMENT);
    List policies = new ArrayList(elements.size());
    NodeList refPolicies = baseRoot.getElementsByTagNameNS(Policy.POLICY_NS, Policy.POLICY_ELEMENT);
    // loading PolicyReferences
    for (int i = 0; i < elements.size(); i++) {
      policies.add(PolicyDomLoader.loadPolicyReference((Element) elements.get(i), refPolicies));
    }
    // loading @policyURIs policies
    String pURIs = base.getExtensionAttr(new QName(Policy.POLICY_NS, Policy.POLICYURIS_ATTR));
    if (pURIs != null && pURIs.length() > 0) {
      policies.add(PolicyDomLoader.loadPolicyURIs(pURIs, refPolicies));
    }
    // loading local Policy elements
    elements = getExtensionElements(base, Policy.POLICY_NS, Policy.POLICY_ELEMENT);
    for (int i = 0; i < elements.size(); i++) {
      policies.add(PolicyDomLoader.loadPolicy((Element) elements.get(i), refPolicies));
    }

    return Policy.mergePolicies(policies);
  }

  /**
   * Reads all sap:useFeature elements and loads the referenced features.
   */
  private List loadFeatures(Base base, Element baseRoot) throws Exception {
    List featureRefs = getExtensionElements(base, FeatureConvertor.FEATURES_NS, FeatureConvertor.USEFEATURE_ELEMENT);
    List features = DOM.getChildElementsByTagNameNS(baseRoot, FeatureConvertor.FEATURES_NS,
        FeatureConvertor.FEATURE_ELEMENT);
    List result = new ArrayList(featureRefs.size());
    String attrV;
    // loading features
    for (int i = 0; i < featureRefs.size(); i++) {
      attrV = ((Element) featureRefs.get(i)).getAttribute(FeatureConvertor.FEATURE_ATTR);
      attrV = DOM.qnameToLocalName(attrV);
      int f = 0;
      for (; f < features.size(); f++) {
        if (attrV.equals(((Element) features.get(f)).getAttribute(FeatureConvertor.NAME_ATTR))) {
          result.add(features.get(f));
          break;
        }
      }
      if (f == features.size()) {
        throw new ConfigurationMarshallerException(ResourceAccessor.getResourceAccessor(),
            ExceptionConstants.CANNOT_FOND_SAP_FEATURE, new Object[] { attrV });
      }
    }

    return result;
  }

  public static boolean isSAPFeatureAnnotated(Definitions def){
    final String sapNS = "http://www.sap.com/webas";
    final String feat  = "sap:SAP_WSDL";
    
    List extElements = getExtensionElements(def, WSDL11Constants.WSDL_NS, WSDL11Constants.DOCUMENTATION_ELEMENT);
    Element elem1;
    Node node;
    NodeList nodeList;
    for (int i = 0; i < extElements.size(); i ++){
      elem1 = (Element)extElements.get(i);
      nodeList = elem1.getChildNodes();
      for (int j = 0; j < nodeList.getLength(); j ++){
        node = nodeList.item(j);
        if (node instanceof Element) {
          if (feat.equals(node.getNodeName()) && sapNS.equals(node.getNamespaceURI())){
            return true;
          }
        }
      }
    }
    return false;
  }
  
  public static boolean isPolicyAnnotated(Definitions def) {
    if (getExtensionElements(def, Policy.POLICY_NS, Policy.USINGPOLICY_ELEMENT).size() != 0) {
      return true;
    }
    return false;
  }

  /**
   * Returns List of Element objects
   */
  private static List getExtensionElements(Base b, String ns, String lName) {
    ObjectList exs = b.getChildren(Base.EXTENSION_ELEMENT_ID);
    List result = new ArrayList(exs.getLength());
    Element elem;
    for (int i = 0; i < exs.getLength(); i++) {
      elem = ((ExtensionElement) exs.item(i)).getContent();
      if (elem != null && ns.equals(elem.getNamespaceURI()) && lName.equals(elem.getLocalName())) {
        result.add(elem);
      }
    }
    return result;
  }

  // private String removeVariantProperty(Behaviour b) {
  // PropertyListType[] pLType = b.getPropertyList();
  // if (pLType != null && pLType.length == 1) { //if it has alternative
  // PropertyType[] pType = pLType[0].getProperty();
  // for (int p = 0; p < pType.length; p++) {
  // if
  // (DefaultConfigurationMarshaller.VARIANTNAME_NS.equals(pType[p].getNamespace())
  // &&
  // DefaultConfigurationMarshaller.VARIANTNAME_PROPERTY.equals(pType[p].getName()))
  // {
  // String vName = pType[p].get_value();
  // //remove the variant property
  // PropertyType[] newPTArr = new PropertyType[pType.length - 1];
  // System.arraycopy(pType, 0, newPTArr, 0, p);
  // System.arraycopy(pType, p + 1, newPTArr, p, pType.length - p - 1);
  // pLType[0].setProperty(newPTArr);
  // return vName;
  // }
  // }
  // }
  //    
  // return null;
  // }

  private void applyDPToInterfaceData(InterfaceData intfD, Interface intf, int mode)
      throws ConfigurationMarshallerException {
    // check whether all the Operations from the Interface are reflected in the
    // InterfaceData
    OperationData ops[] = intfD.getOperation();
    ObjectList intfOps = intf.getOperations();
    List newOps = new ArrayList();
    OperationData opData;
    Operation op;
    for (int p = 0; p < intfOps.getLength(); p++) {
      op = (Operation) intfOps.item(p);
      boolean found = false;
      for (int i = 0; i < ops.length; i++) {
        if (ops[i].getName().equals(op.getName())) {
          found = true;
          break;
        }
      }
      // in there is no OperationData for the Operation, create one
      if (!found) {
        opData = new OperationData();
        opData.setName(op.getName());
        newOps.add(opData);
      }
    }
    // append the new operations into the InterfaceData
    if (newOps.size() > 0) {
      OperationData[] newArr = new OperationData[ops.length + newOps.size()];
      System.arraycopy(ops, 0, newArr, 0, ops.length);
      for (int i = 0; i < newOps.size(); i++) {
        newArr[i + ops.length] = (OperationData) newOps.get(i);
      }
      intfD.setOperation(newArr);
    }

    // apply defaults to OperationDatas first
    ops = intfD.getOperation();
    for (int i = 0; i < ops.length; i++) {
      applyDefaultProperties(ops[i], IConfigurationMarshaller.INTERFACE_OPERATION_LEVEL, mode);
    }
    // apply defaults to InterfaceData
    applyDefaultProperties(intfD, IConfigurationMarshaller.INTERFACE_LEVEL, mode);
  }

  private void applyDPToBindingData(BindingData bd, int mode) throws ConfigurationMarshallerException {
    // apply defaults to OperationDatas first
    OperationData ops[] = bd.getOperation();
    if (ops != null) {
      for (int i = 0; i < ops.length; i++) {
        applyDefaultProperties(ops[i], IConfigurationMarshaller.BINDING_OPERATION_LEVEL, mode);
      }
    }
    // apply defaults to BindingData
    applyDefaultProperties(bd, IConfigurationMarshaller.BINDING_LEVEL, mode);
  }

  /**
   * Applies the default properties for the given level.
   */
  private void applyDefaultProperties(Behaviour b, String level, int mode) throws ConfigurationMarshallerException {
    IConfigurationMarshaller cfgM;
    PropertyType[] pList;
    if (b.getPropertyList() != null && b.getPropertyList().length == 1) {
      pList = b.getPropertyList()[0].getProperty();
    } else {
      pList = new PropertyType[0];
    }
    // loads the array into List
    List l = new ArrayList();
    for (int i = 0; i < pList.length; i++) {
      l.add(pList[i]);
    }
    // traversing the confMarshallers
    for (int i = 0; i < confMarshallers.size(); i++) {
      cfgM = (IConfigurationMarshaller) confMarshallers.get(i);
      cfgM.applyDefaultProperties(l, level, mode);
    }

    pList = (PropertyType[]) l.toArray(new PropertyType[l.size()]);
    PropertyListType[] pLType = new PropertyListType[1];
    pLType[0] = new PropertyListType();
    pLType[0].setProperty(pList);
    b.setPropertyList(pLType);
  }

  /**
   * Applies the default properties for the given level.
   */
  private void applyDefaultPropertiesNew(InterfaceData intfData, BindingData bd, int mode)
      throws ConfigurationMarshallerException {
    IConfigurationMarshaller cfgM;
    // traversing the confMarshallers
    for (int i = 0; i < confMarshallers.size(); i++) {
      cfgM = (IConfigurationMarshaller) confMarshallers.get(i);
      if (cfgM instanceof RMConfigurationMarshaller) {
        ((RMConfigurationMarshaller) cfgM).applyDefaultProperties(intfData, bd, mode);
      }
    }
  }

  // private void connectBindingDataToVariant(BindingData bd,
  // InterfaceDefinition intDef, int mode) throws
  // ConfigurationMarshallerException {
  // List result = new ArrayList();
  // Variant[] vs = intDef.getVariant();
  // Set baseSet = new HashSet(); //this set initially contains all the
  // variants' names
  // for (int i = 0; i < vs.length; i++) {
  // result.add(vs[i]);
  // baseSet.add(vs[i].getName());
  // }
  // IConfigurationMarshaller cfgM;
  // for (int i = 0; i < confMarshallers.size(); i++) {
  // cfgM = (IConfigurationMarshaller) confMarshallers.get(i);
  // Set set = cfgM.findVariantsForBindingData(result, bd, mode);
  // baseSet.retainAll(set); //do intersection of the sets
  // }
  // if (baseSet.size() > 0) {
  // bd.setVariantName((String) baseSet.iterator().next());
  // }
  // }

  /**
   * Checks the validity of Behavior object using IConfigurationMarshaller
   * implementations currently registered. As parameter only InterfaceData and
   * BindingData object could be passed.
   */
  private void checkBehaviorConfiguration(Behaviour b, int mode) throws ConfigurationMarshallerException {
    List msg = new ArrayList();
    IConfigurationMarshaller cfgM;
    boolean isValid = true;
    for (int i = 0; i < confMarshallers.size(); i++) {
      cfgM = (IConfigurationMarshaller) confMarshallers.get(i);
      isValid = isValid && cfgM.checkConfiguration(b, msg, mode);
    }
    if (!isValid) {
      // create exception message
      String lineSeparator = System.getProperty("line.separator");
      StringBuffer buf = new StringBuffer();
      for (int i = 0; i < msg.size(); i++) {
        buf.append(msg.get(i).toString());
        buf.append(lineSeparator);
      }
      throw new ConfigurationMarshallerException(ResourceAccessor.getResourceAccessor(),
          ExceptionConstants.INVALID_CONFIGURATION_FOUND, new Object[] { serializeObject(b), buf.toString() });
    }
  }

  /**
   * Checks whether property is been defined in NW04.
   */
  private boolean isNW04Defined(QName prop, int mode) throws ConfigurationMarshallerException {
    IConfigurationMarshaller cfgMarsh;
    for (int i = 0; i < confMarshallers.size(); i++) {
      cfgMarsh = (IConfigurationMarshaller) confMarshallers.get(i);
      if (checkQNameSetForQName(cfgMarsh.getKnownProperties(mode), prop)) {
        return cfgMarsh.isNW04Defined(prop, mode);
      }
    }
    throw new ConfigurationMarshallerException(ResourceAccessor.getResourceAccessor(),
        ExceptionConstants.PROPERTY_NOT_RECORGNIZED, new Object[] { prop });
  }

  private static String serializeObject(Behaviour b) throws ConfigurationMarshallerException {
    try {
      XMLMarshaller marshaller = new XMLMarshaller();
      InputStream config = ConfigurationFactory.class
          .getResourceAsStream("/com/sap/engine/services/webservices/espbase/configuration/frm/types.xml");
      marshaller.init(config, ConfigurationBuilder.class.getClassLoader());
      QName elementName = new QName("http://www.sap.com/webas/710/ws/configuration-descriptor", "Behaviour");
      ByteArrayOutputStream buf = new ByteArrayOutputStream();
      marshaller.marshal(b, elementName, new QName("http://www.sap.com/webas/710/ws/configuration-descriptor",
          "Behaviour"), buf);
      return buf.toString(); // $JL-I18N$
    } catch (Exception e) {
      throw new ConfigurationMarshallerException(e);
    }
  }

  /**
   * Checks whether for each property in the list has a marshaller which can
   * process it.
   */
  static void checkPropertyListAgainsMarshallers(PropertyType[] props, List cfgMarshallers, int mode)
      throws ConfigurationMarshallerException {
    Set superSet = new HashSet();
    IConfigurationMarshaller ml;
    // join all supported properties into one superset.
    for (int i = 0; i < cfgMarshallers.size(); i++) {
      ml = (IConfigurationMarshaller) cfgMarshallers.get(i);
      superSet.addAll(ml.getKnownProperties(mode));
    }
    QName qname;
    for (int i = 0; i < props.length; i++) {
      qname = new QName(props[i].getNamespace(), props[i].getName());
      if (!checkQNameSetForQName(superSet, qname)) {
        throw new ConfigurationMarshallerException(ResourceAccessor.getResourceAccessor(),
            ExceptionConstants.MISSSING_ICONFIGURATION_MARSHALLER, new Object[] { qname });
      }
    }
  }

  /**
   * @return an array of all properties, which <code>marsh</code> resolves
   *         from the array <code>pListType</code>.
   */
  static PropertyType[] getPropertiesForMarshaller(IConfigurationMarshaller marsh, PropertyType[] pListType, int mode) {
    Set s = marsh.getKnownProperties(mode);
    List result = new ArrayList();
    QName qname;
    for (int i = 0; i < pListType.length; i++) {
      qname = new QName(pListType[i].getNamespace(), pListType[i].getName());
      if (checkQNameSetForQName(s, qname)) {
        result.add(pListType[i]);
      }
    }
    return (PropertyType[]) result.toArray(new PropertyType[result.size()]);
  }

  /**
   * checks the given set <code>s</code>, whether contains the specified
   * object <code>qname</code>
   */
  static boolean checkQNameSetForQName(Set s, QName qname) {
    if (s.contains(qname)) {
      return true;
    }
    // try to check the set for qname with ns equal to 'qname' and localname
    // equal to '*'
    // This is how the missing security IConfigurationMarshaller will be
    // overcome for now
    QName nQN = new QName(qname.getNamespaceURI(), "*");
    return s.contains(nQN);
  }

  /**
   * Invokes 'getDefaultConfigurationRTConfigForDT' method of all registered
   * IConfigurationMarshaller implementations using the <code>dtProp</code>
   * and <code>level</code> parameters.
   * 
   * @return List of ProprtyType objects, which represent the default runtime
   *         configuration.
   */
  private List invokeGetDefaultRTConfigForDTOnMarshallers(PropertyType[] dtProp, String level, int mode)
      throws ConfigurationMarshallerException {
    if ((!IConfigurationMarshaller.INTERFACE_LEVEL.equals(level))
        && (!IConfigurationMarshaller.INTERFACE_OPERATION_LEVEL.equals(level))) {
      throw new IllegalArgumentException("Invalid 'level' parameter.");
    }
    // check whether each property is recorgnized
    checkPropertyListAgainsMarshallers(dtProp, this.confMarshallers, mode);
    IConfigurationMarshaller cM;
    PropertyType tmpP[];
    List res = new ArrayList();
    List tmpRes;
    for (int i = 0; i < this.confMarshallers.size(); i++) {
      cM = (IConfigurationMarshaller) this.confMarshallers.get(i);
      tmpP = getPropertiesForMarshaller(cM, dtProp, mode);
      tmpRes = cM.getDefaultRTConfigForDT(tmpP, level, mode);
      if (tmpRes != null) {
        res.addAll(tmpRes);
      }
    }
    return res;
  }

  public synchronized void updateBindingDataWithDefaults(BindingData bd, Variant v)
      throws ConfigurationMarshallerException {
    this.updateBindingDataWithDefaults(bd, v, ConfigurationManipulator.PROVIDER_MODE);
  }

  public synchronized void updateBindingDataWithDefaults(BindingData bd, Variant v, int mode)
      throws ConfigurationMarshallerException {
    InterfaceData intData = v.getInterfaceData();
    List res;
    PropertyListType plType;
    // parse interface-binding relation
    res = invokeGetDefaultRTConfigForDTOnMarshallers(intData.getSinglePropertyList().getProperty(),
        IConfigurationMarshaller.INTERFACE_LEVEL, mode);
    plType = new PropertyListType();
    plType.setProperty((PropertyType[]) res.toArray(new PropertyType[res.size()]));
    bd.setPropertyList(new PropertyListType[] { plType });
    // parse operation level
    OperationData bdOp, intOp, bdOps[];
    bdOps = bd.getOperation();
    for (int i = 0; i < bdOps.length; i++) {
      bdOp = bdOps[i];
      intOp = intData.getOperationData(bdOp.getName());
      if (intOp != null) {
        res = invokeGetDefaultRTConfigForDTOnMarshallers(intOp.getSinglePropertyList().getProperty(),
            IConfigurationMarshaller.INTERFACE_OPERATION_LEVEL, mode);
        plType = new PropertyListType();
        plType.setProperty((PropertyType[]) res.toArray(new PropertyType[res.size()]));
        bdOp.setPropertyList(new PropertyListType[] { plType });
      } else {
        throw new ConfigurationMarshallerException(ResourceAccessor.getResourceAccessor(),
            ExceptionConstants.CANNOT_FIND_INTERFACE_OPERATION, new Object[] { bdOp.getName() });
      }
    }
  }

  public synchronized boolean checkRTConfigurationAgainstDT(BindingData rt, InterfaceData dt, List msg, int mode)
      throws ConfigurationMarshallerException {
    IConfigurationMarshaller cm;
    boolean res = true;
    for (int i = 0; i < this.confMarshallers.size(); i++) {
      cm = (IConfigurationMarshaller) this.confMarshallers.get(i);
      res = res && cm.checkRTConfigurationAgainstDT(rt, dt, msg, mode);
    }
    return res;
  }

  public synchronized Behaviour convertNW04CfgToNY(Behaviour nw04, Behaviour ny, int mode)
      throws ConfigurationMarshallerException {
    final Location LOC = Location.getLocation(ConfigurationBuilder.class);
    IConfigurationMarshaller cm;
    for (int i = 0; i < this.confMarshallers.size(); i++) {
      cm = (IConfigurationMarshaller) this.confMarshallers.get(i);
      try {
        cm.convertNW04CfgToNY(nw04, ny, mode);
      } catch (Exception e) {
        throw new ConfigurationMarshallerException(e);
      }
    }
    return ny;
  }

  /*
   * public static void main(String[] arg) throws Exception { WSDLLoader l = new
   * WSDLLoader(); Definitions def =
   * l.load("http://10.55.70.45:50200/HelloService/HelloBeanPort?wsdl&mode=ws_policy");
   * System.out.println("22333322"); //Definitions def =
   * l.load("http://localhost:56000/service1/Config1?wsdl&mode=ws_policy");
   * ConfigurationMarshallerFactory f =
   * ConfigurationMarshallerFactory.newInstance(); //f.registerMarshaller(new
   * RMConfigurationMarshaller()); f.registerMarshaller(new
   * SecurityConfigurationMarshaller()); ConfigurationBuilder b = new
   * ConfigurationBuilder(f); //ConfigurationRoot cfg = b.create(def,
   * IConfigurationMarshaller.PROVIDER_MODE); ConfigurationRoot cfg =
   * b.create(def); ConfigurationFactory.save(cfg, System.out); // BindingData
   * bD = new BindingData(); // bD.setBindingName("BindingName"); //
   * bD.setBindingNamespace("BindingNamespace"); // bD.setUrl("localhost"); //
   * PropertyListType pLType = new PropertyListType(); // PropertyType pType =
   * new PropertyType(); // pType.setName("pName"); //
   * pType.setNamespace("pNS"); // pType.set_value("property-value"); //
   * pLType.setProperty(new PropertyType[]{pType}); // bD.setPropertyList(new
   * PropertyListType[]{pLType}); // String s = serializeObject(bD); //
   * System.out.println(s); }
   */
}

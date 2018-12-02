/*
 * Copyright (c) 2003 by SAP Labs Bulgaria,
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP Labs Bulgaria. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Bulgaria.
 */
package com.sap.engine.services.webservices.jaxr.impl.uddi_v2.connector;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Vector;

import javax.xml.registry.Connection;
import javax.xml.registry.JAXRException;
import javax.xml.registry.UnexpectedObjectException;
import javax.xml.registry.infomodel.Association;
import javax.xml.registry.infomodel.Classification;
import javax.xml.registry.infomodel.ClassificationScheme;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.EmailAddress;
import javax.xml.registry.infomodel.ExtensibleObject;
import javax.xml.registry.infomodel.ExternalIdentifier;
import javax.xml.registry.infomodel.ExternalLink;
import javax.xml.registry.infomodel.InternationalString;
import javax.xml.registry.infomodel.Key;
import javax.xml.registry.infomodel.LocalizedString;
import javax.xml.registry.infomodel.Organization;
import javax.xml.registry.infomodel.PersonName;
import javax.xml.registry.infomodel.PostalAddress;
import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.infomodel.Service;
import javax.xml.registry.infomodel.ServiceBinding;
import javax.xml.registry.infomodel.Slot;
import javax.xml.registry.infomodel.SpecificationLink;
import javax.xml.registry.infomodel.TelephoneNumber;
import javax.xml.registry.infomodel.User;

import com.sap.engine.services.webservices.jaxr.BulkResponseImpl;
import com.sap.engine.services.webservices.jaxr.JAXRNestedException;
import com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport;
import com.sap.engine.services.webservices.jaxr.impl.uddi_v2.Inquire;
import com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.AccessPoint;
import com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.Address;
import com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.BindingDetail;
import com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.BindingTemplate;
import com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.BusinessDetailExt;
import com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.BusinessEntity;
import com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.BusinessEntityExt;
import com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.BusinessInfo;
import com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.BusinessService;
import com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.Contact;
import com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.Description;
import com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.DiscoveryURL;
import com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.Email;
import com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.GetBindingDetail;
import com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.GetBusinessDetailExt;
import com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.GetServiceDetail;
import com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.GetTModelDetail;
import com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.InstanceDetails;
import com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.KeyedReference;
import com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.Name;
import com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.OverviewDoc;
import com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.Phone;
import com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.PublisherAssertion;
import com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.RelatedBusinessInfo;
import com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.RelatedBusinessesList;
import com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.ServiceDetail;
import com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.ServiceInfo;
import com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.SharedRelationships;
import com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.TModel;
import com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.TModelDetail;
import com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.TModelInfo;
import com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.TModelInstanceInfo;
import com.sap.engine.services.webservices.jaxr.infomodel.AssociationImpl;
import com.sap.engine.services.webservices.jaxr.infomodel.ClassificationImpl;
import com.sap.engine.services.webservices.jaxr.infomodel.ClassificationSchemeImpl;
import com.sap.engine.services.webservices.jaxr.infomodel.ConceptImpl;
import com.sap.engine.services.webservices.jaxr.infomodel.EmailAddressImpl;
import com.sap.engine.services.webservices.jaxr.infomodel.ExternalIdentifierImpl;
import com.sap.engine.services.webservices.jaxr.infomodel.ExternalLinkImpl;
import com.sap.engine.services.webservices.jaxr.infomodel.InternationalStringImpl;
import com.sap.engine.services.webservices.jaxr.infomodel.KeyImpl;
import com.sap.engine.services.webservices.jaxr.infomodel.LocalizedStringImpl;
import com.sap.engine.services.webservices.jaxr.infomodel.OrganizationImpl;
import com.sap.engine.services.webservices.jaxr.infomodel.PersonNameImpl;
import com.sap.engine.services.webservices.jaxr.infomodel.PostalAddressImpl;
import com.sap.engine.services.webservices.jaxr.infomodel.ServiceBindingImpl;
import com.sap.engine.services.webservices.jaxr.infomodel.ServiceImpl;
import com.sap.engine.services.webservices.jaxr.infomodel.SlotImpl;
import com.sap.engine.services.webservices.jaxr.infomodel.SpecificationLinkImpl;
import com.sap.engine.services.webservices.jaxr.infomodel.TelephoneNumberImpl;
import com.sap.engine.services.webservices.jaxr.infomodel.UserImpl;

/**
 * @author Alexander Zubev (alexander.zubev@sap.com)
 */
public class Converter {
  private static final String CATEGORIZATION_TMODEL_ID = "uuid:C1ACF26D-9672-4404-9D70-39B756E62AB4";
  private static final String CATEGORIZATION_VALUE = "categorization";

  private static final String RELATIONSHIP_TMODEL_ID = "uuid:807A2C6A-EE22-470D-ADC7-E0424A337C03";
  private static final String RELATIONSHIP_VALUE = "peer-peer";

  private static final String TO_KEY = "toKey";
  private static final String FROM_KEY = "fromKey";
  
  private Inquire inquire;
  private BulkResponseImpl response;
  private Connection con; 
  private Hashtable cachedRegistryObjects = new Hashtable();

  private static KeyedReference schemeReference;
  private static KeyedReference associationReference;

  static {
    schemeReference =  new KeyedReference();

    schemeReference.setTModelKey(CATEGORIZATION_TMODEL_ID);
    schemeReference.setKeyValue(CATEGORIZATION_VALUE);
  }

  Converter(Inquire inquire, BulkResponseImpl response, Connection con) {
    this.inquire = inquire;
    this.response = response;
    this.con = con;
  }

  static String[] uddiFindQualifiers(Collection qualifiersCol) {
    if (qualifiersCol == null) {
      return null;
    }
    String[] uddiFQ = new String[qualifiersCol.size()];
    Iterator it = qualifiersCol.iterator();
    for (int i = 0; i < uddiFQ.length; i++) {
      uddiFQ[i] = it.next().toString();
    }
    return uddiFQ;
  }
  
  static Name[] uddiNames(Collection namesCol) throws JAXRException {
    if (namesCol == null) {
      return null;
    }
    //Name[] names = new Name[namesCol.size()];
    ArrayList<Name> res = new ArrayList<Name>(); 
    Iterator it = namesCol.iterator();
    while (it.hasNext()) {
      Object nameObj = it.next();
      if (nameObj instanceof String) {
        Name name = new Name();
        name.setSimpleContent(nameObj.toString());
        res.add(name);
      } else if (nameObj instanceof LocalizedString) {
        Name name = new Name();
        LocalizedString ls = (LocalizedString) nameObj;
        //name.setLang(ls.getLocale().getLanguage());
        name.setLang(ls.getLocale().toString().replace('_', '-'));
        name.setSimpleContent(ls.getValue());
        res.add(name);
      } else if (nameObj instanceof InternationalString) {
        InternationalString internStr = (InternationalString) nameObj;
        Collection locStrings = internStr.getLocalizedStrings();
        Name[] _names = uddiNames(locStrings);
        if (_names != null) {
          for (int i = 0; i < _names.length; i++) {
            res.add(_names[i]);
          }
        }
      }
    }
    return res.toArray(new Name[res.size()]);
  }
  
  static KeyedReference[] uddiCategoryBag(Collection classifications) throws JAXRException {
    if (classifications == null || classifications.size() == 0) {
      return null;
    }
    KeyedReference[] refs = new KeyedReference[classifications.size()];
    Iterator it = classifications.iterator();
    for (int i = 0; i < refs.length; i++) {
      KeyedReference ref = new KeyedReference();
      Classification classification = (Classification) it.next();
      ClassificationScheme scheme = classification.getClassificationScheme();
      if (scheme != null) {
        ref.setTModelKey(scheme.getKey().getId());
      }
      InternationalString name = classification.getName();
      if (name != null) {
        String value = name.getValue(Locale.getDefault());
        if (value == null) {
          value = name.getValue(new Locale("en", "us"));
          if (value == null) {
            Iterator nameIT = name.getLocalizedStrings().iterator();
            if (nameIT.hasNext()) {
              value = ((LocalizedString) nameIT.next()).getValue();
            }
          }
        }
        if (value != null) {
          ref.setKeyName(value);
        }
      }
      if (classification.getValue() != null) {
        ref.setKeyValue(classification.getValue());
      }
      refs[i] = ref;
    }
    return refs;
  }
  
  static String[] uddiTModelBag(Collection specifications) throws JAXRException {
    if (specifications == null) {
      return null;
    }
    String[] tModels = new String[specifications.size()];
    Iterator it = specifications.iterator();
    for (int i = 0; i < tModels.length; i++) {
      Concept concept = (Concept) it.next();
      tModels[i] = concept.getKey().getId();
    }
    return tModels;
  }
  
  static KeyedReference[] uddiIdentifiersBag(Collection externalIDs) throws JAXRException {
    if (externalIDs == null || externalIDs.size() == 0) {
      return null;
    }
    KeyedReference[] refs = new KeyedReference[externalIDs.size()];
    Iterator it = externalIDs.iterator();
    for (int i = 0; i < refs.length; i++) {
      KeyedReference ref = new KeyedReference();
      ExternalIdentifier extID = (ExternalIdentifier) it.next();
      ClassificationScheme scheme = extID.getIdentificationScheme();
      if (scheme != null) {
        ref.setTModelKey(scheme.getKey().getId());
      }
      InternationalString name = extID.getName();
      if (name != null) {
        String value = name.getValue(Locale.getDefault());
        if (value == null) {
          value = name.getValue(new Locale("en", "us"));
          if (value == null) {
            Iterator nameIT = name.getLocalizedStrings().iterator();
            if (nameIT.hasNext()) {
              value = ((LocalizedString) nameIT.next()).getValue();
            }
          }
        }
        if (value != null) {
          ref.setKeyName(value);
        }
      }
      if (extID.getValue() != null) {
        ref.setKeyValue(extID.getValue());
      }
      refs[i] = ref;
    }
    return refs;
  }
  
  private Collection jaxrExtIdentifiers(KeyedReference[] refs) throws RemoteException, DispositionReport, JAXRException {
    if (refs == null) {
      return new Vector();
    }
    int size = refs.length;
    Vector extIDs = new Vector(size);
    for (int i = 0; i < size; i++) {
      KeyedReference ref = refs[i];
      ExternalIdentifier extID = new ExternalIdentifierImpl(con);
      String name = ref.getKeyName();
      if (name != null) {
        Name uddiName = new Name();
        uddiName.setSimpleContent(name);
        extID.setName(jaxrName(new Name[] {uddiName}));        
      }
      String value = ref.getKeyValue();
      if (value != null) {
        extID.setValue(value);
      }
      TModel tModel = getTModel(ref.getTModelKey());
      ClassificationScheme scheme = (ClassificationScheme) jaxrConceptOrScheme(tModel, true);
      extID.setIdentificationScheme(scheme);
      extIDs.addElement(extID);
    }
    return extIDs;
  }
  
  static DiscoveryURL[] uddiDiscoveryURLs(Collection externalLinks) throws JAXRException {
    if (externalLinks == null || externalLinks.size() == 0) {
      return null;
    }
    DiscoveryURL[] urls = new DiscoveryURL[externalLinks.size()];
    Iterator it = externalLinks.iterator();
    for (int i = 0; i < urls.length; i++) {
      ExternalLink link = (ExternalLink) it.next();
      DiscoveryURL url = new DiscoveryURL();
      url.setSimpleContent(link.getExternalURI());
      url.setUseType("businessEntity");
      urls[i] = url;
    }
    return urls;
  }
  
  private Collection jaxrExternalLinks(DiscoveryURL[] urls) throws JAXRException {
    if (urls == null) {
      return new Vector();
    }
    Vector links = new Vector(urls.length);
    for (int i = 0; i < urls.length; i++) {
      ExternalLink link = new ExternalLinkImpl(con);
      DiscoveryURL url = urls[i];
      link.setExternalURI(url.getSimpleContent());      
      //link.setName(url.getUseType()); - this is defined by the spec, but seems to be unusable
      links.addElement(link);
    }
    return links;
  }
  
  private BusinessEntityExt[] getBusinessDetailExt(String[] businessKeys) throws RemoteException, DispositionReport {
    GetBusinessDetailExt req = new GetBusinessDetailExt();
    req.setGeneric(UDDI2Connector.GENERIC);
    req.setBusinessKey(businessKeys);
    BusinessDetailExt result = inquire.getBusinessDetailExt(req);
    if ("true".equals(result.getTruncated())) {
      response.setPartialResponse(true);
    } 
    return result.getBusinessEntityExt();
  }
  
  private BusinessService[] getServiceDetail(String[] serviceKeys) throws RemoteException, DispositionReport {
    GetServiceDetail req = new GetServiceDetail();
    req.setGeneric(UDDI2Connector.GENERIC);
    req.setServiceKey(serviceKeys);
    ServiceDetail result = inquire.getServiceDetail(req);
    if ("true".equals(result.getTruncated())) {
      response.setPartialResponse(true);
    } 
    return result.getBusinessService();
  }
  
  private ServiceBinding getBinding(String bindingKey, Service service) throws RemoteException, DispositionReport, JAXRException {
    if (bindingKey == null) {
      return null;    
    }
    GetBindingDetail req = new GetBindingDetail();
    req.setBindingKey(new String[] {bindingKey});
    req.setGeneric(UDDI2Connector.GENERIC);
    BindingDetail result = inquire.getBindingDetail(req);
    if ("true".equals(result.getTruncated())) {
      response.setPartialResponse(true);
    }
    BindingTemplate[] templates = result.getBindingTemplate();
    if (templates == null || templates.length == 0) {
      return null; 
    }    
    return jaxrServiceBinding(templates[0], service);
  }

  private TModel getTModel(String tModelKey) throws RemoteException, DispositionReport, JAXRException {
    if (tModelKey == null) {
      return null;    
    }
    TModel[] tModels = getTModels(new String[] {tModelKey});
    if (tModels == null || tModels.length == 0) {
      return null; 
    }
    return tModels[0];    
  }

  private TModel[] getTModels(String[] tModelKeys) throws RemoteException, DispositionReport, JAXRException {
    if (tModelKeys == null || tModelKeys.length == 0) {
      return null;    
    }
    GetTModelDetail req = new GetTModelDetail();
    req.setGeneric(UDDI2Connector.GENERIC);
    req.setTModelKey(tModelKeys);
    TModelDetail result = inquire.getTModelDetail(req);
    if ("true".equals(result.getTruncated())) {
      response.setPartialResponse(true);
    }
    return result.getTModel();
  }
  
  private ServiceBinding jaxrServiceBinding(BindingTemplate template, Service service) throws RemoteException, DispositionReport, JAXRException {
    ServiceBindingImpl binding = new ServiceBindingImpl(con);
    binding.setKey(new KeyImpl(template.getBindingKey()));
    BindingTemplate.Choice1 choice = template.getChoiceGroup1();
    if (choice.hasAccessPoint()) {
      try {
        binding.setAccessURI(choice.getAccessPoint().getSimpleContent());
      } catch (Exception e) {
        throw new JAXRNestedException(e);        
      }
      //TODO - choice.getAccessPoint().getUseType()
    } else if (choice.hasHostingRedirector()) {
      String bindingKey;
      try {
        bindingKey = choice.getHostingRedirector().getBindingKey();
      } catch (Exception e) {
        throw new JAXRNestedException(e);
      }
      binding.setTargetBinding(getBinding(bindingKey, service));
    }
    binding.setDescription(jaxrDescription(template.getDescription()));
    if (service != null) {
      binding.setService(service);
    } else {
      String serviceKey = template.getServiceKey();
      BusinessService[] services = getServiceDetail(new String[] {serviceKey});
      if (services != null && services.length > 0) {
        binding.setService(jaxrService(services[0]));
      }
    }
    addSpecificationLinks(template.getTModelInstanceDetails(), binding);
    return binding;
  }
  
  static BindingTemplate[] uddiBindingTemplates(Collection bindings) throws JAXRException {
    if (bindings == null) {
      return null; 
    }
    int size = bindings.size();
    Iterator it = bindings.iterator();
    BindingTemplate[] templates = new BindingTemplate[size];
    for (int i = 0; i < size; i++) {
      BindingTemplate template = new BindingTemplate();
      ServiceBinding binding = (ServiceBinding) it.next();
      if (binding.getKey() != null) {
        template.setBindingKey(binding.getKey().getId());
      } else {
        template.setBindingKey("");
      }
      //TODO - template.setChoiceGroup1()
      BindingTemplate.Choice1 choice = new BindingTemplate.Choice1();
      if (binding.getAccessURI() != null) {
        AccessPoint point = new AccessPoint();
        point.setURLType("http");
        point.setSimpleContent(binding.getAccessURI());
        choice.setAccessPoint(point);
      }
      template.setChoiceGroup1(choice);
      
      template.setDescription(uddiDescription(binding.getDescription()));
      if (binding.getService() != null && binding.getService().getKey() != null) {
        template.setServiceKey(binding.getService().getKey().getId());
      }
      template.setTModelInstanceDetails(uddiTModelInstaceDetails(binding.getSpecificationLinks()));
      templates[i] = template;
    }
    return templates;
  }
  
  private static TModelInstanceInfo[] uddiTModelInstaceDetails(Collection links) throws JAXRException {
    if (links == null) {
      return null;
    }
    int size = links.size();
    TModelInstanceInfo[] infos = new TModelInstanceInfo[size];
    Iterator it = links.iterator();
    for (int i = 0; i < size; i++) {
      TModelInstanceInfo info = new TModelInstanceInfo();
      SpecificationLink link = (SpecificationLink) it.next();
      info.setDescription(uddiDescription(link.getDescription()));
      InstanceDetails details = new InstanceDetails();
      details.setDescription(uddiDescription(link.getUsageDescription()));
      if (link.getUsageParameters() != null) {
        StringBuffer buf = new StringBuffer();
        Iterator usageIT = link.getUsageParameters().iterator();
        while (usageIT.hasNext()) {
          buf.append(usageIT.next());
          if (usageIT.hasNext()) {
            buf.append(", ");
          }
        }
        if (buf.length() > 0) {
          details.setInstanceParms(buf.toString());
        }
      }
      details.setOverviewDoc(uddiOverviewDoc(link.getExternalLinks()));
      info.setInstanceDetails(details);
      if (link.getSpecificationObject() != null && link.getSpecificationObject().getKey() != null) {
        info.setTModelKey(link.getSpecificationObject().getKey().getId());
      }
      infos[i] = info;
//      SpecificationLink link = new SpecificationLinkImpl(con, binding);
//      TModelInstanceInfo info = infos[i];
//      link.setDescription(jaxrDescription(info.getDescription()));
//      InstanceDetails details = info.getInstanceDetails();
//      if (details != null) {
//        link.setUsageDescription(jaxrDescription(details.getDescription()));
//        if (details.getInstanceParms() != null) {
//          Vector params = new Vector(1);
//          params.addElement(details.getInstanceParms()); 
//          link.setUsageParameters(params);
//        }
//        addExternalLink(link, details.getOverviewDoc());
//      }
//      TModel tModel = getTModel(info.getTModelKey());
//      link.setSpecificationObject(jaxrConceptOrScheme(tModel, false));      
//      binding.addSpecificationLink(link);
    }
    return infos;
  }

  private static Collection jaxrEmails(Email[] emails) throws JAXRException {
    if (emails == null) {
      return new Vector();
    }
    Vector emailAddresses = new Vector(emails.length);    
    for (int i = 0; i < emails.length; i++) {
      EmailAddress address = new EmailAddressImpl();
      address.setAddress(emails[i].getSimpleContent());
      address.setType(emails[i].getUseType());
      emailAddresses.addElement(address);
    }
    return emailAddresses;
  }
  
  private static Email[] uddiEmails(Collection emailAddresses) throws JAXRException {
    if (emailAddresses == null || emailAddresses.isEmpty()) {
      return null;
    }
    int size = emailAddresses.size();
    Iterator it = emailAddresses.iterator();
    Email[] emails = new Email[size];
    for (int i = 0; i < size; i++) {
      Email email = new Email();
      EmailAddress emailAddress = (EmailAddress) it.next();
      email.setSimpleContent(emailAddress.getAddress());
      email.setUseType(emailAddress.getType());
      emails[i] = email;
    }
    return emails;
  }
  
  private static PersonName jaxrPersonName(String name) throws JAXRException {
    if (name == null) {
      return null;
    }
    PersonName personName = new PersonNameImpl();
    personName.setFullName(name);
    return personName;
  }
  
  private Collection jaxrPostalAddresses(Address[] addresses) throws JAXRException {
    if (addresses == null) {
      return null;
    }
    Vector postalAddresses = new Vector(addresses.length);
    for (int i = 0; i < addresses.length; i++) {
      PostalAddress postalAddress = new PostalAddressImpl(con);
      Address address = addresses[i];
      //TODO - find a way to represent UDDI address to JAXR
      postalAddress.setType(address.getUseType());
      addJaxrSlot(Slot.SORT_CODE_SLOT, address.getSortCode(), postalAddress);
      postalAddresses.addElement(postalAddress);
    }
    return postalAddresses;
  }
  
  private static Address[] uddiAddresses(Collection postalAddresses) throws JAXRException {
    if (postalAddresses == null || postalAddresses.isEmpty()) {
      return null;
    }
    int size = postalAddresses.size();
    Iterator it = postalAddresses.iterator();
    Address[] addresses = new Address[size];
    for (int i = 0; i < addresses.length; i++) {
      Address address = new Address();
      PostalAddress postalAddress = (PostalAddress) it.next();
      //TODO - address.setAddressLine()
      address.setSortCode(getJaxrSlot(Slot.SORT_CODE_SLOT, postalAddress));
      address.setUseType(postalAddress.getType());
      addresses[i] = address;
    }
    return addresses;
  }
  
  private static LocalizedString jaxrLocalizedString(String name, String locale) throws JAXRException {
    LocalizedString str = new LocalizedStringImpl();
    str.setValue(name);
    if (locale != null) {
      String[] locParts = locale.split("-");

      switch (locParts.length) {
        case 1: {
          str.setLocale(new Locale(locParts[0]));
          break;
        }
        case 2: {
          str.setLocale(new Locale(locParts[0], locParts[1]));
          break;
        }
        case 3: {
          str.setLocale(new Locale(locParts[0], locParts[1], locParts[2]));
          break;
        }
      }
    } else {
      str.setLocale(Locale.getDefault());
    }
    return str;
  }
  
  private static InternationalString jaxrDescription(Description[] descriptions) throws JAXRException {
    if (descriptions == null) {
      return null;
    }
    InternationalString desc = new InternationalStringImpl();
    for (int i = 0; i < descriptions.length; i++) {
      desc.addLocalizedString(jaxrLocalizedString(descriptions[i].getSimpleContent(), descriptions[i].getLang()));
    }
    return desc;
  }
  
  private static Description[] uddiDescription(InternationalString desc) throws JAXRException {
    if (desc == null) {
      return null;
    }
    Collection localizedStrings = desc.getLocalizedStrings();
    if (localizedStrings.isEmpty()) {
      return null;
    }
    int size = localizedStrings.size();
    Iterator it = localizedStrings.iterator();
    Description[] descs = new Description[size];
    for (int i = 0; i < descs.length; i++) {
      LocalizedString lstring = (LocalizedString) it.next();
      Description descr = new Description();
      //name.setLang(lstring.getLocale().getLanguage());
      descr.setLang(lstring.getLocale().toString().replace('_', '-'));
      descr.setSimpleContent(lstring.getValue());
      descs[i] = descr;
    }
    return descs;
  }
  
  private static Collection jaxrPhones(Phone[] phones) throws JAXRException {
    if (phones == null) {
      return new Vector();
    }
    Vector jaxrPhones = new Vector(phones.length);
    for (int i = 0; i < phones.length; i++) {
      TelephoneNumber telephone = new TelephoneNumberImpl();
      Phone phone = phones[i];
      telephone.setType(phone.getUseType());
      telephone.setNumber(phone.getSimpleContent());
      jaxrPhones.addElement(telephone);
    }
    return jaxrPhones;
  }
  
  private static Phone[] uddiPhones(Collection telephones) throws JAXRException {
    if (telephones == null || telephones.isEmpty()) {
      return null;
    }
    int size = telephones.size();
    Iterator it = telephones.iterator();
    Phone[] phones = new Phone[size];
    for (int i = 0; i < size; i++) {
      Phone phone = new Phone();
      TelephoneNumber telephone = (TelephoneNumber) it.next();
      phone.setSimpleContent(telephone.getNumber());
      phone.setUseType(telephone.getType());
      phones[i] = phone;
    }
    return phones;
  }
  
  private void addJaxrUsers(Contact[] contacts, Organization org) throws JAXRException {
    if (contacts == null) {
      return;      
    }
    for (int i = 0; i < contacts.length; i++) {
      Contact contact = contacts[i];
      User user = new UserImpl(con, org);
      user.setEmailAddresses(jaxrEmails(contact.getEmail()));
      user.setPersonName(jaxrPersonName(contact.getPersonName()));
      user.setPostalAddresses(jaxrPostalAddresses(contact.getAddress()));
      user.setDescription(jaxrDescription(contact.getDescription()));
      user.setTelephoneNumbers(jaxrPhones(contact.getPhone()));
      user.setType(contact.getUseType());
      org.addUser(user);
    }
  }
  
  private static Contact[] uddiContacts(Collection users) throws JAXRException {
    if (users == null || users.isEmpty()) {
      return null;
    }
    int size = users.size();
    Iterator it = users.iterator();
    Contact[] contacts = new Contact[size];
    for (int i = 0; i < size; i++) {
      Contact contact = new Contact();
      User user = (User) it.next();
      contact.setAddress(uddiAddresses(user.getPostalAddresses()));
      contact.setDescription(uddiDescription(user.getDescription()));
      contact.setEmail(uddiEmails(user.getEmailAddresses()));
      if (user.getPersonName() != null) {
        contact.setPersonName(user.getPersonName().getFullName());
      }
      contact.setPhone(uddiPhones(user.getTelephoneNumbers(null)));
      contact.setUseType(user.getType());
      contacts[i] = contact;
    }
    return contacts;
  }
  
  private Service jaxrService(BusinessService businessService) throws RemoteException, DispositionReport, JAXRException {
    String key = businessService.getServiceKey();
    if (key != null && cachedRegistryObjects.get(key) != null) {
      return (Service) cachedRegistryObjects.get(key);
    }
    Service service = new ServiceImpl(con);
    if (key != null) {
      cachedRegistryObjects.put(key, service);
      service.setKey(new KeyImpl(key));
    }
    addJaxrServiceBindings(businessService.getBindingTemplates(), service);
    if (businessService.getBusinessKey() != null) {
      String businessKey = businessService.getBusinessKey();
      if (cachedRegistryObjects.get(businessKey) != null) {
        service.setProvidingOrganization((Organization) cachedRegistryObjects.get(businessKey));
      } else {
        BusinessEntityExt[] entities = getBusinessDetailExt(new String[] {businessKey});
        if (entities != null && entities.length > 0) {
          service.setProvidingOrganization(jaxrOrganization(entities[0]));
        }
      }
    }
    service.setClassifications(jaxrClassifications(businessService.getCategoryBag()));
    service.setDescription(jaxrDescription(businessService.getDescription()));
    service.setName(jaxrName(businessService.getName()));
    return service;
  }
  
  static BusinessService[] uddiBusinessServices(Collection services) throws JAXRException {
    if (services == null || services.size() == 0) {
      return null;      
    }
    int size = services.size();
    Iterator it = services.iterator();
    BusinessService[] businessServices = new BusinessService[size];
    for (int i = 0; i < size; i++) {
      BusinessService businessService = new BusinessService();
      Service service = (Service) it.next();
      businessService.setBindingTemplates(uddiBindingTemplates(service.getServiceBindings()));
      if (service.getProvidingOrganization() != null) {
        Key key = service.getProvidingOrganization().getKey();
        if (key != null) {
          businessService.setBusinessKey(key.getId());
        }
      }
      businessService.setCategoryBag(uddiCategoryBag(service.getClassifications()));
      businessService.setDescription(uddiDescription(service.getDescription()));
      businessService.setName(uddiNames(service.getName()));
      if (service.getKey() != null) {
        businessService.setServiceKey(service.getKey().getId());
      } else {
        businessService.setServiceKey("");
      }
      businessServices[i] = businessService;
    }
    return businessServices;
  }
  
  private void addJaxrServices(BusinessService[] services, Organization org) throws RemoteException, DispositionReport, JAXRException {
    if (services == null) {
      return;
    }
    for (int i = 0; i < services.length; i++) {
      org.addService(jaxrService(services[i]));
    }
  }
  
  private Collection jaxrClassifications(KeyedReference[] refs) throws RemoteException, DispositionReport, JAXRException {
    if (refs == null) {
      return new Vector();
    }
    int size = refs.length;
    Vector classifications = new Vector(size);
    for (int i = 0; i < size; i++) {
      KeyedReference ref = refs[i];
      String tModelKey = ref.getTModelKey();
      Classification classification = new ClassificationImpl(con);
      String name = ref.getKeyName();
      if (name != null) {
        Name uddiName = new Name();
        uddiName.setSimpleContent(name);
        classification.setName(jaxrName(new Name[] {uddiName}));        
      }
      ClassificationScheme scheme;
      if (cachedRegistryObjects.get(tModelKey) == null) {
        TModel tModel = getTModel(tModelKey);
        scheme = (ClassificationScheme) jaxrConceptOrScheme(tModel, true);
      } else {
        scheme = (ClassificationScheme) cachedRegistryObjects.get(tModelKey);
      }
      classification.setClassificationScheme(scheme);
      String value = ref.getKeyValue();
      if (value != null) {
        classification.setValue(value);
      }
      classifications.addElement(classification);
    }
    return classifications;
  }
  
  private static InternationalString jaxrName(Name[] names) throws JAXRException {
    if (names == null) {
      return null;
    }
    InternationalString name = new InternationalStringImpl();
    for (int i = 0; i < names.length; i++) {
      name.addLocalizedString(jaxrLocalizedString(names[i].getSimpleContent(), names[i].getLang()));
    }
    return name;
  }
  
  private static Name[] uddiNames(InternationalString jaxrName) throws JAXRException {
    if (jaxrName == null) {
      return null;
    }
    Collection localizedStrings = jaxrName.getLocalizedStrings();
    if (localizedStrings.isEmpty()) {
      return null;
    }
    int size = localizedStrings.size();
    Iterator it = localizedStrings.iterator();
    Name[] names = new Name[size];
    for (int i = 0; i < names.length; i++) {
      LocalizedString lstring = (LocalizedString) it.next();
      Name name = new Name();
      //name.setLang(lstring.getLocale().getLanguage());
      name.setLang(lstring.getLocale().toString().replace('_', '-'));
      name.setSimpleContent(lstring.getValue());
      names[i] = name;
    }
    return names;
  }

  private static void addJaxrSlot(String name, String value, ExtensibleObject object) throws JAXRException {
    if (value == null) {
      return;
    }
    SlotImpl slot = new SlotImpl();
    slot.setName(name);
    Vector values = new Vector();
    values.add(value);
    slot.setValues(values);
    object.addSlot(slot);
  }
  
  void addJaxrOrganizations(BusinessInfo[] businessInfo) throws JAXRException, RemoteException, DispositionReport {
    if (businessInfo == null || businessInfo.length == 0) {
      return;
    }
    int size = businessInfo.length;
    String[] businessKeys = new String[size];
    for (int i = 0; i < size; i++) {
      BusinessInfo info = businessInfo[i];
      businessKeys[i] = info.getBusinessKey();
    } 
    BusinessEntityExt[] entities = getBusinessDetailExt(businessKeys);
    for (int i = 0; i < entities.length; i++) {      
      response.addObject(jaxrOrganization(entities[i]));
    }
  }

  void addJaxrOrganizations(BusinessEntityExt[] businessEntities) throws JAXRException, RemoteException, DispositionReport {
    if (businessEntities == null || businessEntities.length == 0) {
      return;
    }

    for (int i = 0; i < businessEntities.length; i++) {
      response.addObject(jaxrOrganization(businessEntities[i]));
    }
  }

  private void addExternalLink(RegistryObject object, OverviewDoc doc) throws JAXRException {
    if (doc == null) {
      return;
    }
    ExternalLink extLink = new ExternalLinkImpl(con);
    extLink.setDescription(jaxrDescription(doc.getDescription()));
    extLink.setExternalURI(doc.getOverviewURL());
    object.addExternalLink(extLink);
  }
  
  private RegistryObject jaxrConceptOrScheme(TModel tModel, boolean scheme) throws RemoteException, DispositionReport, JAXRException {
    return jaxrConceptOrScheme(tModel, scheme, false);
  }
  
  private RegistryObject jaxrConceptOrScheme(TModel tModel, boolean scheme, boolean createConcept) throws RemoteException, DispositionReport, JAXRException {
    if (tModel == null) {
      return null;
    }
    String tModelKey = tModel.getTModelKey();
    if (tModelKey != null && cachedRegistryObjects.get(tModelKey) != null) {
      return (RegistryObject) cachedRegistryObjects.get(tModelKey);  
    }

    boolean isScheme = false;
    if (!createConcept) {
      if (tModel.getCategoryBag() != null) {
        KeyedReference[] refs = tModel.getCategoryBag();
        if (refs != null) {
          for (int i = 0; i < refs.length; i++) {
            String id = refs[i].getTModelKey();
            if (CATEGORIZATION_TMODEL_ID.equalsIgnoreCase(id)) {
              String value = refs[i].getKeyValue();

              if (value.equalsIgnoreCase("identifier") || value.equalsIgnoreCase("namespace") ||
                  value.equalsIgnoreCase("categorization") || value.equalsIgnoreCase("postaladdress")) {
                isScheme = true;
                break;
              } 
//              else {
//                throw new JAXRException("Keyed reference value for the classification scheme in the category bag is not valid '" + value + "'");
//              }
            }
          }
        }
      }
      if ((scheme && !isScheme) || (!scheme && isScheme)) {
        return null;
      }
    }
    RegistryObject conceptScheme;
    if (isScheme) {
      conceptScheme = new ClassificationSchemeImpl(con);
    } else {
      conceptScheme = new ConceptImpl(con);
    }
    cachedRegistryObjects.put(tModelKey, conceptScheme);
    addJaxrSlot(Slot.AUTHORIZED_NAME_SLOT, tModel.getAuthorizedName(), conceptScheme);
    conceptScheme.setClassifications(jaxrClassifications(tModel.getCategoryBag()));
    conceptScheme.setDescription(jaxrDescription(tModel.getDescription()));
    conceptScheme.setExternalIdentifiers(jaxrExtIdentifiers(tModel.getIdentifierBag()));
    conceptScheme.setName(jaxrName(new Name[] {tModel.getName()}));
    addJaxrSlot(Slot.OPERATOR_SLOT, tModel.getOperator(), conceptScheme);
    addExternalLink(conceptScheme, tModel.getOverviewDoc());
    conceptScheme.setKey(new KeyImpl(tModelKey));
    return conceptScheme;
  }
  
  static TModel[] uddiTModels(Collection conceptsOrSchemes) throws JAXRException {
    if (conceptsOrSchemes == null) {
      return null;
    }
    int size = conceptsOrSchemes.size();
    Iterator it = conceptsOrSchemes.iterator();
    TModel[] tModels = new TModel[size];
    for (int i = 0; i < size; i++) {
      TModel tModel = new TModel();
      RegistryObject obj = (RegistryObject) it.next();
      tModel.setAuthorizedName(getJaxrSlot(Slot.AUTHORIZED_NAME_SLOT, obj));
      tModel.setCategoryBag(uddiCategoryBag(obj.getClassifications()));
      tModel.setDescription(uddiDescription(obj.getDescription()));
      tModel.setIdentifierBag(uddiIdentifiersBag(obj.getExternalIdentifiers()));
      Name[] names = uddiNames(obj.getName());
      if (names != null && names.length > 0) {
        tModel.setName(names[0]);
      }
      tModel.setOperator(getJaxrSlot(Slot.OPERATOR_SLOT, obj));
      tModel.setOverviewDoc(uddiOverviewDoc(obj.getExternalLinks()));
      if (obj.getKey() != null) {
        tModel.setTModelKey(obj.getKey().getId());
      } else {
        tModel.setTModelKey("");
      }
      tModels[i] = tModel;
    }
    return tModels;
  }
  
  private static OverviewDoc uddiOverviewDoc(Collection extLinks) throws JAXRException {
    if (extLinks == null || extLinks.isEmpty()) {
      return null;
    }
    OverviewDoc doc = new OverviewDoc();
    Iterator linksIT = extLinks.iterator();
    ExternalLink extLink = (ExternalLink) linksIT.next();
    doc.setDescription(uddiDescription(extLink.getDescription()));
    doc.setOverviewURL(extLink.getExternalURI());
    return doc;
  }
  
  private void addSpecificationLinks(TModelInstanceInfo[] infos, ServiceBinding binding) throws RemoteException, DispositionReport, JAXRException {
    if (infos == null) {
      return;
    }
    for (int i = 0; i < infos.length; i++) {
      SpecificationLink link = new SpecificationLinkImpl(con, binding);
      TModelInstanceInfo info = infos[i];
      link.setDescription(jaxrDescription(info.getDescription()));
      InstanceDetails details = info.getInstanceDetails();
      if (details != null) {
        link.setUsageDescription(jaxrDescription(details.getDescription()));
        if (details.getInstanceParms() != null) {
          Vector params = new Vector(1);
          params.addElement(details.getInstanceParms()); 
          link.setUsageParameters(params);
        }
        addExternalLink(link, details.getOverviewDoc());
      }
      TModel tModel = getTModel(info.getTModelKey());
      RegistryObject obj = jaxrConceptOrScheme(tModel, false, true);
      if (obj != null) {
        link.setSpecificationObject(obj);
      }      
      binding.addSpecificationLink(link);
    }
  }

  private void addJaxrServiceBindings(BindingTemplate[] templates, Service service) throws RemoteException, DispositionReport, JAXRException {
    if (templates == null) {
      return;
    }
    for (int i = 0; i < templates.length; i++) {
      BindingTemplate template = templates[i];      
      service.addServiceBinding(jaxrServiceBinding(template, service));
    }
  }
  
  private Organization jaxrOrganization(BusinessEntityExt entityExt) throws RemoteException, DispositionReport, JAXRException {
    if (entityExt == null) {
      return null;
    }
    BusinessEntity entity = entityExt.getBusinessEntity();
    String key = entity.getBusinessKey();
    if (key != null && cachedRegistryObjects.get(key) != null) {
      return (Organization) cachedRegistryObjects.get(key);
    }
    Organization org = new OrganizationImpl(con);
    if (key != null) {
      cachedRegistryObjects.put(key, org);
      org.setKey(new KeyImpl(entity.getBusinessKey()));      
    }
    addJaxrUsers(entity.getContacts(), org);
    addJaxrServices(entity.getBusinessServices(), org);
    org.setClassifications(jaxrClassifications(entity.getCategoryBag()));
    org.setDescription(jaxrDescription(entity.getDescription()));
    org.setExternalLinks(jaxrExternalLinks(entity.getDiscoveryURLs()));
    org.setExternalIdentifiers(jaxrExtIdentifiers(entity.getIdentifierBag()));
    org.setName(jaxrName(entity.getName()));
    addJaxrSlot(Slot.AUTHORIZED_NAME_SLOT, entity.getAuthorizedName(), org);
    addJaxrSlot(Slot.OPERATOR_SLOT, entity.getOperator(), org);
    return org;
  }
  
  private static String getJaxrSlot(String slotName, ExtensibleObject obj) throws JAXRException {
    Slot slot = obj.getSlot(slotName);
    if (slot == null) {
      return null;
    }
    Iterator it = slot.getValues().iterator();
    if (it.hasNext()) {
      return it.next().toString();
    }
    return null;
  }
  
  static BusinessEntity[] uddiBusinessEntities(Collection organizations) throws JAXRException {
    if (organizations == null) {
      return null;
    }
    Iterator it = organizations.iterator();
    BusinessEntity[] entities = new BusinessEntity[organizations.size()];

    try {
      for (int i = 0; i < entities.length; i++) {
        Organization org = (Organization) it.next();
        BusinessEntity entity = new BusinessEntity();
        entity.setAuthorizedName(getJaxrSlot(Slot.AUTHORIZED_NAME_SLOT, org));
        if (org.getKey() != null) {
          entity.setBusinessKey(org.getKey().getId());
        } else {
          entity.setBusinessKey("");
        }
        entity.setBusinessServices(uddiBusinessServices(org.getServices()));
        entity.setCategoryBag(uddiCategoryBag(org.getClassifications()));
        entity.setContacts(uddiContacts(org.getUsers()));
        entity.setDescription(uddiDescription(org.getDescription()));
        entity.setDiscoveryURLs(uddiDiscoveryURLs(org.getExternalLinks()));
        entity.setIdentifierBag(uddiIdentifiersBag(org.getExternalIdentifiers()));
        entity.setName(uddiNames(org.getName()));
        entity.setOperator(getJaxrSlot(Slot.OPERATOR_SLOT, org));
        entities[i] = entity;
      }
    } catch (ClassCastException ex) {
      throw new UnexpectedObjectException("Incorrect organization object applied");
    }

    return entities;
  }
  
  void addJaxrServices(ServiceInfo[] serviceInfo) throws JAXRException, RemoteException, DispositionReport {
    if (serviceInfo == null || serviceInfo.length == 0) {
      return;
    }
    int size = serviceInfo.length;
    String[] serviceKeys = new String[size];
    for (int i = 0; i < size; i++) {
      ServiceInfo info = serviceInfo[i];
      serviceKeys[i] = info.getServiceKey();
    }
    BusinessService[] services = getServiceDetail(serviceKeys);
    for (int i = 0; i < services.length; i++) {
      response.addObject(jaxrService(services[i]));
    }
  }

  void addJaxrServices(BusinessService[] services) throws JAXRException, RemoteException, DispositionReport {
    if (services == null || services.length == 0) {
      return;
    }

    for (int i = 0; i < services.length; i++) {
      response.addObject(jaxrService(services[i]));
    }
  }

  void addJaxrConceptsOrSchemes(TModelInfo[] tModelInfos, boolean schemes) throws JAXRException, RemoteException, DispositionReport {
    if (tModelInfos == null || tModelInfos.length == 0) {
      return;
    }
    int size = tModelInfos.length;
    String[] tModelKeys = new String[size];
    for (int i = 0; i < size; i++) {
      TModelInfo info = tModelInfos[i];
      tModelKeys[i] = info.getTModelKey();
    }
    TModel[] tModels = getTModels(tModelKeys);
    for (int i = 0; i < tModels.length; i++) {
      RegistryObject obj = jaxrConceptOrScheme(tModels[i], schemes);
      if (obj != null) {
        if (!response.getCollection().contains(obj)) {
          response.addObject(obj);
        }
      }
    }
  }

  void addJaxrConceptsOrSchemes(TModel[] tModels, boolean schemes) throws JAXRException, RemoteException, DispositionReport {
    if (tModels == null || tModels.length == 0) {
      return;
    }
    for (int i = 0; i < tModels.length; i++) {
      RegistryObject obj = jaxrConceptOrScheme(tModels[i], schemes);
      if (obj != null) {
        if (!response.getCollection().contains(obj)) {
          response.addObject(obj);
        }
      }
    }
  }

  void addJaxrServiceBindings(BindingTemplate[] templates) throws JAXRException, RemoteException, DispositionReport {
    if (templates == null || templates.length == 0) {
      return;
    }
    int size = templates.length;
    for (int i = 0; i < size; i++) {
      response.addObject(jaxrServiceBinding(templates[i], null));
    }
  }
  
  void addJaxrAssociations(RelatedBusinessesList list, String sourceObjectId, String targetObjectId) throws JAXRException, RemoteException, DispositionReport {
    RelatedBusinessInfo[] infos = list.getRelatedBusinessInfos();
    String searchedKey = list.getBusinessKey();
    if (infos == null || infos.length == 0) {
      return;
    }
    int size = infos.length;
    for (int i = 0; i < size; i++) {
      RelatedBusinessInfo info = infos[i];
      String direction;
      if (searchedKey.equals(sourceObjectId)) {
        direction = TO_KEY;
      } else {
        direction = FROM_KEY;
      }
      Organization sourceOrg = null;
      SharedRelationships[] relationships = info.getSharedRelationships();
      if (relationships == null || relationships.length == 0) {
        continue;
      }
      InternationalString descriptions = jaxrDescription(info.getDescription());
      InternationalString names = jaxrName(info.getName());
      for (int j = 0; j < relationships.length; j++) {
        SharedRelationships rel = relationships[j];
        if (!direction.equalsIgnoreCase(rel.getDirection())) {
          continue;
        }
        if (sourceOrg == null) {
          BusinessEntityExt[] entities = getBusinessDetailExt(new String[] {searchedKey});
          if (entities != null && entities.length > 0) {
            sourceOrg = jaxrOrganization(entities[0]);
          } else {
            throw new JAXRException("Source organization not found, business key is: " + info.getBusinessKey());
          }
        }
        Association association = new AssociationImpl(con);
        association.setSourceObject(sourceOrg);
        association.setDescription(descriptions);
        association.setName(names);
        KeyedReference[] references = rel.getKeyedReference();
        //TODO - make associationType from refrences
        response.addObject(association);
      }
    }
  }
  
  static Collection entitiesToKeys(BusinessEntity[] entities) {
    if (entities == null) {
      return new Vector();
    }
    int size = entities.length;
    Vector keys = new Vector(size);
    for (int i = 0; i < size; i++) {
      BusinessEntity be = entities[i];
      keys.addElement(new KeyImpl(be.getBusinessKey()));
      
      
    }
    return keys;
  }

  static Collection servicesToKeys(BusinessService[] services, Collection jaxrServices) throws JAXRException {
    if (services == null) {
      return new Vector();
    }
    int jaxrServicesSize = jaxrServices.size();
    int size = services.length;
    
    Iterator itr = null;
    if (jaxrServicesSize == size) {
      itr = jaxrServices.iterator();
    }
    
    Vector keys = new Vector(size);
    for (int i = 0; i < size; i++) {
      KeyImpl k = new KeyImpl(services[i].getServiceKey());
      keys.addElement(k);
      if (itr != null) {
        Service s = (Service) itr.next();
        s.setKey(k);
      }
      
    }
    return keys;
  }

  static Collection templatesToKeys(BindingTemplate[] templates) {
    if (templates == null) {
      return new Vector();
    }
    int size = templates.length;
    Vector keys = new Vector(size);
    for (int i = 0; i < size; i++) {
      keys.addElement(new KeyImpl(templates[i].getBindingKey()));
    }
    return keys;
  }

  static Collection tModelsToKeys(TModel[] tModels) {
    if (tModels == null) {
      return new Vector();
    }
    int size = tModels.length;
    Vector keys = new Vector(size);
    for (int i = 0; i < size; i++) {
      keys.addElement(new KeyImpl(tModels[i].getTModelKey()));
    }
    return keys;
  }

  static String[] keysToStrings(Collection jaxrKeys) throws JAXRException {
    if (jaxrKeys == null) {
      return null;
    }
    int size = jaxrKeys.size();
    String[] keys = new String[size];
    Iterator it = jaxrKeys.iterator();
    for (int i = 0; i < size; i++) {
      Key key = (Key) it.next();
      keys[i] = key.getId();
    }
    return keys;
  }

  public static void applyTModelSchemeRefs(TModel[] tModels) {
    for (int i = 0; i < tModels.length; i++) {
      TModel tModel = tModels[i];

      int size = 0;
      KeyedReference[] refs = null;

      if (tModel.getCategoryBag() != null) {
        size = tModel.getCategoryBag().length + 1;
        refs = new KeyedReference[size];
        System.arraycopy(tModel.getCategoryBag(), 0, refs, 0, tModel.getCategoryBag().length);
      } else {
        size = 1;
        refs = new KeyedReference[size];
      }

      refs[size - 1] = schemeReference;

      tModel.setCategoryBag(refs);
    }
  }

  public static PublisherAssertion[] uddiPublisherAssertions(Collection associations) throws JAXRException {
    if (associations == null) {
      return null;
    }
    Iterator it = associations.iterator();
    PublisherAssertion[] assertions = new PublisherAssertion[associations.size()];
    for (int i = 0; i < assertions.length; i++) {
      Association assoc = (Association) it.next();
      PublisherAssertion assertion = new PublisherAssertion();
      assertion.setFromKey(assoc.getSourceObject().getKey().getId());
      assertion.setToKey(assoc.getTargetObject().getKey().getId());
      assertion.setKeyedReference(getRelationTypeReference(assoc.getAssociationType()));
      assertions[i] = assertion;
    }
    return assertions;
  }

  private static KeyedReference getRelationTypeReference(Concept associationType) throws JAXRException {
    KeyedReference relReference = new KeyedReference();

    relReference.setTModelKey(RELATIONSHIP_TMODEL_ID);
    relReference.setKeyValue(RELATIONSHIP_VALUE);
    relReference.setKeyName(associationType.getName().getValue());

    return relReference;
  }

  public static Collection jaxrAssociations(PublisherAssertion[] publisherAssertion) throws JAXRException, RemoteException, DispositionReport {
    return new Vector();
  }
}
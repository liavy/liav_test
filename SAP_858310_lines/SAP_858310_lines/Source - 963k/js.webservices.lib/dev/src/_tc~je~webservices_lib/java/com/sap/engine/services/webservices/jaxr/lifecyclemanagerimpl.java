package com.sap.engine.services.webservices.jaxr;

import com.sap.engine.services.webservices.jaxr.infomodel.*;

import javax.activation.DataHandler;
import javax.xml.registry.*;
import javax.xml.registry.infomodel.*;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.Vector;

public class LifeCycleManagerImpl implements LifeCycleManager {
  protected Connection[] connections;
  protected Connection associatedConnection;
  protected RegistryService registryService;
  
  public LifeCycleManagerImpl(Connection[] connections, RegistryService registryService) {
    this.connections = connections;
    associatedConnection = connections[0];
    this.registryService = registryService;
  }
  
  public Association createAssociation(RegistryObject targetObject, Concept associationType) throws JAXRException {
    Association association = new AssociationImpl(associatedConnection);
    association.setTargetObject(targetObject);
    association.setAssociationType(associationType);
    return association;
  }

  public Classification createClassification(ClassificationScheme scheme, InternationalString name, String value) throws JAXRException {
    Classification classification = new ClassificationImpl(associatedConnection);
    classification.setClassificationScheme(scheme);
    classification.setName(name);
    classification.setValue(value);
    return classification;
  }
  
  public Classification createClassification(ClassificationScheme scheme, String name, String value) throws JAXRException {
    return createClassification(scheme, createInternationalString(name), value);
  }

  public Classification createClassification(Concept concept) throws JAXRException, InvalidRequestException {
    if (concept.getClassificationScheme() == null) {
      throw new InvalidRequestException("The specified Concept isn't under a ClassificationScheme");
    }

    Classification classification = new ClassificationImpl(associatedConnection);
    classification.setConcept(concept);

    return classification;
  }
  
  public ClassificationScheme createClassificationScheme(Concept concept) throws JAXRException, InvalidRequestException {
    if (concept.getParentConcept() != null || concept.getClassificationScheme() != null) {
      throw new InvalidRequestException("The specified Concept has a parent Concept or is under a ClassificationScheme");
    }
    ClassificationScheme scheme = new ClassificationSchemeImpl(((RegistryObjectImpl) concept).getConnection());
    scheme.addSlots(concept.getSlots());
    scheme.setKey(concept.getKey());
    scheme.setDescription(concept.getDescription());
    scheme.setName(concept.getName());
    scheme.addAssociations(concept.getAssociations());
    scheme.setExternalIdentifiers(concept.getExternalIdentifiers());
    scheme.addExternalLinks(concept.getExternalLinks());
    scheme.addChildConcepts(concept.getChildrenConcepts());
    return scheme;
  }

  public ClassificationScheme createClassificationScheme(InternationalString name, InternationalString description) throws JAXRException, InvalidRequestException {
    ClassificationScheme scheme = new ClassificationSchemeImpl(associatedConnection);
    scheme.setName(name);
    scheme.setDescription(description);
    return scheme;
  }
  
  public ClassificationScheme createClassificationScheme(String name, String description) throws JAXRException, InvalidRequestException {
    return createClassificationScheme(createInternationalString(name), createInternationalString(description));
  }
  
  public Concept createConcept(RegistryObject parent, InternationalString name, String value) throws JAXRException {
    ConceptImpl concept = new ConceptImpl(associatedConnection);

    concept.setParent(parent);
    concept.setName(name);
    concept.setValue(value);

    if (parent instanceof ClassificationScheme) {
      concept.setClassificationScheme((ClassificationScheme) parent);
      ((ClassificationScheme) parent).addChildConcept(concept);
    }

    return concept;
  }
  
  public Concept createConcept(RegistryObject parent, String name, String value) throws JAXRException {
    return createConcept(parent, createInternationalString(name), value);
  }
  
  public EmailAddress createEmailAddress(String address) throws JAXRException {
    return createEmailAddress(address, null);
  }
  
  public EmailAddress createEmailAddress(String address, String type) throws JAXRException {
    EmailAddress email = new EmailAddressImpl();
    email.setAddress(address);
    email.setType(type);
    return email;
  }
  
  public ExternalIdentifier createExternalIdentifier(ClassificationScheme identificationScheme, InternationalString name, String value) throws JAXRException {
    ExternalIdentifier identifier = new ExternalIdentifierImpl(associatedConnection);
    identifier.setIdentificationScheme(identificationScheme);
    identifier.setName(name);
    identifier.setValue(value);
    return identifier;
  }
  
  public ExternalIdentifier createExternalIdentifier(ClassificationScheme identificationScheme, String name, String value) throws JAXRException {
    return createExternalIdentifier(identificationScheme, createInternationalString(name), value);
  }
  
  public ExternalLink createExternalLink(String externalURI, InternationalString description) throws JAXRException {
    ExternalLink link = new ExternalLinkImpl(associatedConnection);
    link.setExternalURI(externalURI);
    link.setDescription(description);
    return link;
  }
  
  public ExternalLink createExternalLink(String externalURI, String description) throws JAXRException {
    return createExternalLink(externalURI, createInternationalString(description));
  }
  
  public ExtrinsicObject createExtrinsicObject(DataHandler repositoryItem) throws JAXRException {
    throw new UnsupportedCapabilityException("Level 1 capability feature(createExtrinsicObject)");
  }
  
  public InternationalString createInternationalString() throws JAXRException {
    return new InternationalStringImpl();
  }
  
  public InternationalString createInternationalString(Locale l, String s) throws JAXRException {
    InternationalString str = new InternationalStringImpl();
    str.setValue(l, s);
    return str;
  }
  
  public InternationalString createInternationalString(String s) throws JAXRException {
    return new InternationalStringImpl(s);
  }
  
  public Key createKey(String id) throws JAXRException {
    return new KeyImpl(id);
  }
  
  public LocalizedString createLocalizedString(Locale l, String s) throws JAXRException {
    return createLocalizedString(l, s, null);
  }
  
  public LocalizedString createLocalizedString(Locale l, String s, String charSetName) throws JAXRException {
    LocalizedString str = new LocalizedStringImpl();
    str.setLocale(l);
    str.setValue(s);
    str.setCharsetName(charSetName);
    return str;
  }
  
  public  Object createObject(String className) throws JAXRException, InvalidRequestException, UnsupportedCapabilityException {
    if (className.equals("AuditableEvent") || className.equals("ExtrinsicObject") || className.equals("RegistryEntry") || className.equals("Versionable") || className.equals("RegistryPackage"))
        throw new UnsupportedCapabilityException("Can not create object of type " + className + " at Capability Level 0");

    Constructor constructor = null;

    try {
      constructor = Class.forName("com.sap.engine.services.webservices.jaxr.infomodel." + className + "Impl").getConstructors()[0];
    } catch(ClassNotFoundException cnfe) {
      throw new InvalidRequestException("The specified className(" + className + ") is not part of the infomodel package");
    }

    try {
      Object[] params = new Object[constructor.getParameterTypes().length];
      Object obj = constructor.newInstance(params);
      return obj;
    } catch (Exception e) {
      throw new JAXRNestedException(e);
    }
  }
  
  public Organization createOrganization(InternationalString name) throws JAXRException {
    Organization org = new OrganizationImpl(associatedConnection);
    org.setName(name);
    return org;
  }
  
  public Organization createOrganization(String name) throws JAXRException {
    return createOrganization(createInternationalString(name));
  }
  
  public PersonName createPersonName(String fullName) throws JAXRException {
    PersonName person = new PersonNameImpl();
    person.setFullName(fullName);
    return person;
  }
  
  public PersonName createPersonName(String firstName, String middleName, String lastName) throws JAXRException {
    throw new UnsupportedCapabilityException("Level 1 capability feature(createPersonName)");
  }
  
  public PostalAddress createPostalAddress(String streetNumber, String street, String city, String stateOrProvince, String country, String postalCode, String type) throws JAXRException {
    PostalAddress address = new PostalAddressImpl(associatedConnection);
    address.setStreetNumber(streetNumber);
    address.setStreet(street);
    address.setCity(city);
    address.setStateOrProvince(stateOrProvince);
    address.setCountry(country);
    address.setPostalCode(postalCode);
    address.setType(type);
    return address;
  }
  
  public RegistryPackage createRegistryPackage(InternationalString name) throws JAXRException {
    throw new UnsupportedCapabilityException("Level 1 capability feature(createRegistryPackage)");
  }

  public RegistryPackage createRegistryPackage(String name) throws JAXRException {
    throw new UnsupportedCapabilityException("Level 1 capability feature(createRegistryPackage)");
  }
  
  public Service createService(InternationalString name) throws JAXRException {
    Service s = new ServiceImpl(associatedConnection);
    s.setName(name);
    return s;
  }
  
  public Service createService(String name) throws JAXRException {
    return createService(createInternationalString(name));
  }
  
  public ServiceBinding createServiceBinding() throws JAXRException {
    return new ServiceBindingImpl(associatedConnection);
  }
 
  public Slot createSlot(String name, Collection values, String slotType) throws JAXRException {
    Slot s = new SlotImpl();
    s.setName(name);
    s.setValues(values);
    s.setSlotType(slotType);
    return s;
  }
    
  public Slot createSlot(String name, String value, String slotType) throws JAXRException {
    Vector v = new Vector(1);
    v.addElement(value);
    return createSlot(name, v, slotType);
  }
  
  public SpecificationLink createSpecificationLink() throws JAXRException {
    return new SpecificationLinkImpl(associatedConnection, null);
  }
  
  public TelephoneNumber createTelephoneNumber() throws JAXRException {
    return new TelephoneNumberImpl();
  }
  
  public User createUser() throws JAXRException {
    return new UserImpl(associatedConnection, null);
  }
  
  public BulkResponse deleteObjects(Collection keys) throws JAXRException {
    throw new UnsupportedCapabilityException("Not supported yet");
//    if ((keys == null) || (keys.size() == 0)) {
//      throw new JAXRException("The specified collection of keys is either null or empty");
//    }
//    
//    BulkResponseImpl response = new BulkResponseImpl(false, new Vector(), new Vector());
//    for (int i=0; i < connections.length; i++) {
//      ConnectionImpl connection = (ConnectionImpl) connections[i];      
//      BulkResponse resp = connection.getRegistryConnector().deleteObjects(keys);
//      ((RegistryServiceImpl) registryService).storeBulkResponse(resp);
//      response.addAnotherBulkResponse(resp);
//    }
//    ((RegistryServiceImpl) registryService).storeBulkResponse(response);
//    return response;
  }
  
  public BulkResponse deprecateObjects(Collection keys) throws JAXRException {
    throw new UnsupportedCapabilityException("Level 1 capability feature(deprecateObjects)");
  }
  
  public RegistryService getRegistryService() throws JAXRException {
    return registryService;
  }
  
  public BulkResponse saveObjects(Collection objects) throws JAXRException {
    if ((objects == null) || (objects.size() == 0)) {
      throw new JAXRException("The specified collection of objects is either null or empty");
    }

    BulkResponseImpl response = new BulkResponseImpl(false, new Vector(), new Vector());

    for (int i=0; i < connections.length; i++) {
      ConnectionImpl connection = (ConnectionImpl) connections[i];

      BulkResponse resp = null;
      Iterator itor = objects.iterator();

      Vector coll = new Vector(1);

      while (itor.hasNext()) {
        Object obj = itor.next();

        coll.clear();
        coll.add(obj);

        if (obj instanceof Concept) {
          resp = connection.getRegistryConnector().saveConcepts(coll);
        } else if (obj instanceof ClassificationScheme) {
          resp = connection.getRegistryConnector().saveClassificationSchemes(coll);
        } else if (obj instanceof Organization) {
          resp = connection.getRegistryConnector().saveOrganizations(coll);
        } else if (obj instanceof ServiceBinding) {
          resp = connection.getRegistryConnector().saveServiceBindings(coll);
        } else if (obj instanceof Service) {
          resp = connection.getRegistryConnector().saveServices(coll);
        }
      }

      if (resp != null) {
        ((RegistryServiceImpl) registryService).storeBulkResponse(resp);
        response.addAnotherBulkResponse(resp);
      }
    }
    ((RegistryServiceImpl) registryService).storeBulkResponse(response);
    return response;
  }
  
  public BulkResponse unDeprecateObjects(Collection keys) throws JAXRException {
    throw new UnsupportedCapabilityException("Level 1 capability feature(unDeprecateObjects)");
  }
  
  public BulkResponse deleteObjects(Collection keys, String objectType) throws JAXRException {
    if ((keys == null) || (keys.size() == 0)) {
      throw new JAXRException("The specified collection of keys is either null or empty");
    }

    BulkResponseImpl response = new BulkResponseImpl(false, new Vector(), new Vector());

    for (int i=0; i < connections.length; i++) {
      ConnectionImpl connection = (ConnectionImpl) connections[i];
      BulkResponse resp = null;

      if (LifeCycleManager.CONCEPT.equals(objectType)) {
        resp = connection.getRegistryConnector().deleteConcepts(keys);
      } else if (LifeCycleManager.CLASSIFICATION_SCHEME.equals(objectType)) {
        resp = connection.getRegistryConnector().deleteClassificationSchemes(keys);
      } else if (LifeCycleManager.SERVICE_BINDING.equals(objectType)) {
        resp = connection.getRegistryConnector().deleteServiceBindings(keys);
      } else if (LifeCycleManager.ORGANIZATION.equals(objectType)) {
        resp = connection.getRegistryConnector().deleteOrganizations(keys);
      } else if (LifeCycleManager.SERVICE.equals(objectType)) {
        resp = connection.getRegistryConnector().deleteServices(keys);
      }

      if (resp != null) {
        ((RegistryServiceImpl) registryService).storeBulkResponse(resp);
        response.addAnotherBulkResponse(resp);
      }
    }
    ((RegistryServiceImpl) registryService).storeBulkResponse(response);
    return response;
  }
}
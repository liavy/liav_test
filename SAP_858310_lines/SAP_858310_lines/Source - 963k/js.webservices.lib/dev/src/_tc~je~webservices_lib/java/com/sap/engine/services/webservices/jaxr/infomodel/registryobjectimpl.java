package com.sap.engine.services.webservices.jaxr.infomodel;

import com.sap.engine.services.webservices.jaxr.JAXRNestedException;

import javax.xml.registry.Connection;
import javax.xml.registry.JAXRException;
import javax.xml.registry.LifeCycleManager;
import javax.xml.registry.UnsupportedCapabilityException;
import javax.xml.registry.infomodel.*;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

public class RegistryObjectImpl extends ExtensibleObjectImpl implements RegistryObject {
  private Key key;
  private InternationalString description;
  private InternationalString name;
  private Vector classifications;
  private Vector associations;
  private Vector externalIdentifiers;
  private Vector externalLinks;
  private Connection connection;
  private Organization organization;
  
  public RegistryObjectImpl(Connection connection) {
    super(connection);

    classifications = new Vector();
    associations = new Vector();
    externalIdentifiers = new Vector();
    externalLinks = new Vector();

    this.connection = connection;
  }
  
  public void setKey(Key key) throws JAXRException {
    this.key = key;
  }

  public Key getKey() throws JAXRException {
    return key;
  }
  
  public void setDescription(InternationalString description) throws JAXRException {
    this.description = description;
  }

  public InternationalString getDescription() throws JAXRException {
    return description;
  }
    
  public void setName(InternationalString name) throws JAXRException {
    this.name = name;
  }

  public InternationalString getName() throws JAXRException {
    return name;
  }

  public String toXML() throws JAXRException {
    return "<registry_object></registry_object>";
  }
  
  public void addClassification(Classification classification) throws JAXRException {
    classifications.addElement(classification);
    classification.setClassifiedObject(this);
  }
  
  public void addClassifications(Collection classifications) throws JAXRException {
    Iterator iterator = classifications.iterator();
    while (iterator.hasNext()) {
      try {
        addClassification((Classification) iterator.next());
      } catch (Exception e) {
        throw new JAXRNestedException(e);
      }
    }
  }
  
  public void removeClassification(Classification classification) throws JAXRException {
    if (classifications.remove(classification)) {
      classification.setClassifiedObject(null);
    } else {
      InternationalString name = classification.getName();
      throw new JAXRException("The registryObject does not contain this Classification: " + ((name != null) ? name.getValue() : "<NO NAME SPECIFIED>"));
    }
  }
  
  public void removeClassifications(Collection classifications) throws JAXRException {
    try {
      Classification[] classes = (Classification[]) classifications.toArray(new Classification[classifications.size()]);

      for (int i = 0; i < classes.length; i++) {
        removeClassification(classes[i]);
      }
    } catch (Exception e) {
      throw new JAXRNestedException(e);
    }
  }
  
  public void setClassifications(Collection classifications) throws JAXRException {
    this.classifications = new Vector();
    Iterator iterator = classifications.iterator();
    while (iterator.hasNext()) {
      try {
        addClassification((Classification) iterator.next());
      } catch (Exception e) {
        throw new JAXRNestedException(e);
      }
    }
  }
  
  public Collection getClassifications() throws JAXRException {
    return classifications;
  }
  
  public Collection getAuditTrail() throws JAXRException {
    throw new UnsupportedCapabilityException("Level 1 capability feature(getAuditTrail)");
  }
  
  public void addAssociation(Association association) throws JAXRException {
    if (association != null) {
      association.setSourceObject(this);
      associations.addElement(association);
    }
  }
  
  public void addAssociations(Collection associations) throws JAXRException {
    Iterator iterator = associations.iterator();
    while (iterator.hasNext()) {
      try {
        addAssociation((Association) iterator.next());
      } catch (Exception e) {
        throw new JAXRNestedException(e);
      }
    }
  }
  
  public void removeAssociation(Association association) throws JAXRException {
    associations.remove(association);
  }
  
  public void removeAssociations(Collection associations) throws JAXRException {
    try {
      Association[] assocs = (Association[]) associations.toArray(new Association[associations.size()]);

      for (int i = 0; i < assocs.length; i++) {
        removeAssociation(assocs[i]);
      }
    } catch (Exception e) {
      throw new JAXRNestedException(e);
    }
  }
  
  public void setAssociations(Collection associations) throws JAXRException {
    this.associations = new Vector(associations);
  }
  
  public Collection getAssociations() throws JAXRException {
    return associations;
  }
  
  public Collection getAssociatedObjects() throws JAXRException {
    throw new UnsupportedCapabilityException("Level 1 capability feature(getAssociatedObjects)");
  }
  
  public void addExternalIdentifier(ExternalIdentifier externalIdentifier) throws JAXRException {
    externalIdentifiers.addElement(externalIdentifier);
    ((ExternalIdentifierImpl) externalIdentifier).setRegistryObject(this);
  }
  
  public void addExternalIdentifiers(Collection externalIdentifiers) throws JAXRException {
    Iterator iterator = externalIdentifiers.iterator();
    while (iterator.hasNext()) {
      try {
        addExternalIdentifier((ExternalIdentifier) iterator.next());
      } catch (Exception e) {
        throw new JAXRNestedException(e);
      }
    }
  }
  
  public void removeExternalIdentifier(ExternalIdentifier externalIdentifier) throws JAXRException {
    if (externalIdentifiers.remove(externalIdentifier)) {
      ((ExternalIdentifierImpl) externalIdentifier).setRegistryObject(null);
    } else {
      InternationalString name = externalIdentifier.getName();
      throw new JAXRException("The registryObject does not contain this ExternalIdentifier: " + ((name != null) ? name.getValue() : "<NO NAME SPECIFIED>"));
    }
  }
  
  public void removeExternalIdentifiers(Collection externalIdentifiers) throws JAXRException {
    try {
      ExternalIdentifier[] extIds = (ExternalIdentifier[]) externalIdentifiers.toArray(new ExternalIdentifier[externalIdentifiers.size()]);

      for (int i = 0; i < extIds.length; i++) {
        removeExternalIdentifier(extIds[i]);
      }
    } catch (Exception e) {
      throw new JAXRNestedException(e);
    }
  }
  
  public void setExternalIdentifiers(Collection externalIdentifiers) throws JAXRException {
    this.externalIdentifiers = new Vector();
    Iterator iterator = externalIdentifiers.iterator();
    while (iterator.hasNext()) {
      try {
        addExternalIdentifier((ExternalIdentifier) iterator.next());
      } catch (Exception e) {
        throw new JAXRNestedException(e);
      }
    }
  }
  
  public Collection getExternalIdentifiers() throws JAXRException {
    return externalIdentifiers;
  }
  
  public void addExternalLink(ExternalLink externalLink) throws JAXRException {
    externalLinks.addElement(externalLink);
    externalLink.getLinkedObjects().add(this);
  }
  
  public void addExternalLinks(Collection externalLinks) throws JAXRException {
    Iterator iterator = externalLinks.iterator();
    while (iterator.hasNext()) {
      try {
        addExternalLink((ExternalLink) iterator.next());
      } catch (Exception e) {
        throw new JAXRNestedException(e);
      }
    }
  }
  
  public void removeExternalLink(ExternalLink externalLink) throws JAXRException {
    externalLinks.remove(externalLink);
  }
  
  public void removeExternalLinks(Collection externalLinks) throws JAXRException {
    try {
      ExternalLink[] extLinks = (ExternalLink[]) externalLinks.toArray(new ExternalLink[externalLinks.size()]);

      for (int i = 0; i < extLinks.length; i++) {
        removeExternalLink(extLinks[i]);
      }
    } catch (Exception e) {
      throw new JAXRNestedException(e);
    }
  }
  
  public void setExternalLinks(Collection externalLinks) throws JAXRException {
    this.externalLinks = new Vector(externalLinks);
  }
  
  public Collection getExternalLinks() throws JAXRException {
    return externalLinks;
  }
  
  public Concept getObjectType() throws JAXRException {
    throw new UnsupportedCapabilityException("Level 1 capability feature(getObjectType)");
  }
  
  public Organization getSubmittingOrganization() throws JAXRException {
    return organization;
  }
  
  protected void setSubmittingOrganization(Organization o) throws JAXRException {
    this.organization = o;
  }
  
  public Collection getRegistryPackages() throws JAXRException {
    throw new UnsupportedCapabilityException("Level 1 capability feature(getRegistryPackages)");
  }
  
  public Connection getConnection() throws JAXRException {
    return connection;
  }

  public LifeCycleManager getLifeCycleManager() throws JAXRException {
    return connection.getRegistryService().getBusinessLifeCycleManager();
  }
}
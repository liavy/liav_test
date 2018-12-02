package com.sap.engine.services.webservices.jaxr.infomodel;

import com.sap.engine.services.webservices.jaxr.JAXRNestedException;

import javax.xml.registry.Connection;
import javax.xml.registry.JAXRException;
import javax.xml.registry.UnsupportedCapabilityException;
import javax.xml.registry.infomodel.ClassificationScheme;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.InternationalString;
import javax.xml.registry.infomodel.LocalizedString;

import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.Vector;

public class ClassificationSchemeImpl extends RegistryEntryImpl implements ClassificationScheme {
  public static final Locale EN_US_LOCALE = new Locale("en", "US");
  protected Vector concepts;
  
  public ClassificationSchemeImpl(Connection connection) {
    super(connection);
    concepts = new Vector();
  }

  public ClassificationSchemeImpl(Connection connection, String predefeinedName) throws JAXRException {
    this(connection);
    LocalizedString localizedName = new LocalizedStringImpl();
    localizedName.setLocale(EN_US_LOCALE);
    localizedName.setValue(predefeinedName);
    InternationalString name = new InternationalStringImpl();
    name.addLocalizedString(localizedName);
    //add the default locale
    LocalizedString defaultLocalizedName = new LocalizedStringImpl();
    defaultLocalizedName.setLocale(Locale.getDefault());
    defaultLocalizedName.setValue(predefeinedName);
    name.addLocalizedString(defaultLocalizedName);
    setName(name);
  }
  
  public void addChildConcept(Concept concept) throws JAXRException {
//    if ((concept.getClassificationScheme() != null) || (concept.getParentConcept() != null)) {
//      throw new JAXRException("The specified concept has a parent concept or it is already bound to another ClassificationScheme");
//    }
    ((ConceptImpl) concept).setClassificationScheme(this);
    concepts.addElement(concept);
  }
  
  public void addChildConcepts(Collection concept) throws JAXRException {
    Iterator iterator = concept.iterator();
    while (iterator.hasNext()) {
      try {
        addChildConcept((Concept) iterator.next());
      } catch (Exception e) {
        throw new JAXRNestedException(e);
      }
    }
  }
  
  public int getChildConceptCount() throws JAXRException {
    return concepts.size();
  }
  
  public Collection getChildrenConcepts() throws JAXRException {
    return concepts;
  }
  
  public Collection getDescendantConcepts() throws JAXRException {
    Vector descendant = new Vector(getChildrenConcepts());
    for (int i=0; i < getChildConceptCount(); i++) {
      Collection children = ((Concept) concepts.elementAt(i)).getDescendantConcepts();
      if ((children != null) && (children.size() > 0)) {
        descendant.addAll(children);
      }
    }
    return descendant;
  }
  
  public int getValueType() throws JAXRException {
    throw new UnsupportedCapabilityException("Level 1 capability featue(getValueType)");
  }

  public boolean isExternal() throws JAXRException {
    return (getChildConceptCount() == 0);
  }
  
  public void removeChildConcept(Concept concept) throws JAXRException {
    if (concepts.remove(concept)) {
      ((ConceptImpl) concept).setClassificationScheme(null);
    } else {
      InternationalString name = concept.getName();
      throw new JAXRException("The ClassificationScheme does not contain this Concept: " + ((name != null) ? name.getValue() : "<NO NAME SPECIFIED>"));
    }
  }
  
  public void removeChildConcepts(Collection concepts) throws JAXRException {
    try {
      Concept[] parts = (Concept[]) concepts.toArray(new Concept[concepts.size()]);

      for (int i = 0; i < parts.length; i++) {
        removeChildConcept(parts[i]);
      }
    } catch (Exception ex) {
      throw new JAXRNestedException(ex);
    }
  }
  
  public void setValueType(int valueType) throws JAXRException {
    throw new UnsupportedCapabilityException("Level 1 capability featue(setValueType)");
  }
}
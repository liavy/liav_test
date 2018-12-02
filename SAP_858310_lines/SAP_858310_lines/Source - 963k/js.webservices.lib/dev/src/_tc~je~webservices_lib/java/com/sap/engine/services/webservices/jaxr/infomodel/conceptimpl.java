package com.sap.engine.services.webservices.jaxr.infomodel;

import com.sap.engine.services.webservices.jaxr.JAXRNestedException;

import javax.xml.registry.Connection;
import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.ClassificationScheme;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.InternationalString;
import javax.xml.registry.infomodel.RegistryObject;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

public class ConceptImpl extends RegistryObjectImpl implements Concept {
  private String value;
  private ClassificationScheme classificationScheme;
  private Vector concepts;
  private RegistryObject parent;

  public ConceptImpl(Connection connection) {
    super(connection);
    concepts = new Vector();
  }

  public void addChildConcept(Concept concept) throws JAXRException {
//    if ((concept.getClassificationScheme() != null) || (concept.getParentConcept() != null)) {
//      throw new JAXRException("The specified concept has a parent concept or it is already bound to another ClassificationScheme");
//    }
//    ((ConceptImpl) concept).setClassificationScheme(getClassificationScheme());
    ((ConceptImpl) concept).setParent(this);
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
  
  public ClassificationScheme getClassificationScheme()  throws JAXRException {
    return classificationScheme;
  }
  
  public Collection getDescendantConcepts() throws JAXRException {
    Vector descendant = new Vector(getChildrenConcepts());
    for (int i=0; i < getChildConceptCount(); i++) {
      Iterator children = ((Concept) concepts.elementAt(i)).getDescendantConcepts().iterator();
      while (children.hasNext()) {
        descendant.addElement(children.next());
      }
    }
    return descendant;
  }
  
  public Concept getParentConcept() throws JAXRException {
    Concept concept = null;

    if (parent instanceof Concept) {
      concept = (Concept) parent;
    }

    return concept;
  }
  
  public String getValue() throws JAXRException {
    return value;
  }
  
  public void removeChildConcept(Concept concept) throws JAXRException {
    if (concepts.remove(concept)) {
      ((ConceptImpl) concept).setParent(null);
    } else {
      InternationalString name = concept.getName();
      throw new JAXRException("The Concept does not contain this childConcept: " + ((name != null) ? name.getValue() : "<NO NAME SPECIFIED>"));
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
  
  public void setValue(String value) throws JAXRException {
    this.value = value;
  }
  
  public void setClassificationScheme(ClassificationScheme scheme) {
    this.classificationScheme = scheme;
  }
  
  public void setParent(RegistryObject parent) {
    this.parent = parent;
  }

  public RegistryObject getParent() throws JAXRException {
    return parent;
  }

  public String getPath() throws JAXRException {
    String parentPath = "";

    if (getParent() != null) {
      if (getParent() instanceof ClassificationScheme) {
        parentPath = "/" + getParent().getName().getValue();
      } else if (getParent() instanceof Concept) {
        parentPath = ((Concept) getParent()).getPath();
      }
    }

    return parentPath + "/" + getValue();
  }
}
package com.sap.engine.services.webservices.jaxr.infomodel;

import java.util.Collection;
import java.util.Vector;

import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.Slot;

public class SlotImpl implements Slot {
  private String name;
  private String slotType;
  private Vector values;
  
  public SlotImpl() {
    values = new Vector();
  }
  
  public String getName() throws JAXRException {
    return name;    
  }
  
  public void setName(String name) throws JAXRException {
    this.name = name;
  }
  
  public String getSlotType() throws JAXRException {
    return slotType;
  }
  
  public void setSlotType(String slotType) throws JAXRException {
    this.slotType = slotType;
  }
  
  public Collection getValues() throws JAXRException {
    return values;
  }
  
  public void setValues(Collection values) throws JAXRException {
    this.values = new Vector(values);
  }
}
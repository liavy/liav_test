package com.sap.engine.services.webservices.jaxr.infomodel;

import com.sap.engine.services.webservices.jaxr.JAXRNestedException;

import javax.xml.registry.Connection;
import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.ExtensibleObject;
import javax.xml.registry.infomodel.Slot;
import java.util.Collection;
import java.util.Vector;

public class ExtensibleObjectImpl implements ExtensibleObject {
  private Vector slots;
  
  public ExtensibleObjectImpl(Connection con) {
    slots = new Vector();
  }
  
  public void addSlot(Slot slot) throws JAXRException {
    slots.addElement(slot);
  }
  
  public void addSlots(Collection slots) throws JAXRException {
    this.slots.addAll(slots);
  }
  
  public void removeSlot(String slotName) throws JAXRException {
    for (int i=0; i < slots.size(); i++) {
      Slot slot = (Slot) slots.elementAt(i);
      if (slotName.equals(slot.getName())) {
        slots.remove(i);
        return;
      }
    }
  }
  
  public void removeSlots(Collection slotNames) throws JAXRException {
    try {
      String[] parts = (String[]) slotNames.toArray(new String[slotNames.size()]);

      for (int i = 0; i < parts.length; i++) {
        removeSlot(parts[i]);
      }
    } catch (Exception ex) {
      throw new JAXRNestedException(ex);
    }
  }
  
  public Slot getSlot(String slotName) throws JAXRException {
    for (int i=0; i < slots.size(); i++) {
      Slot slot = (Slot) slots.elementAt(i);
      if (slotName.equals(slot.getName())) {
        return slot;
      }
    }
    return null;
  }
  
  public Collection getSlots() throws JAXRException {
    return slots;
  }
}
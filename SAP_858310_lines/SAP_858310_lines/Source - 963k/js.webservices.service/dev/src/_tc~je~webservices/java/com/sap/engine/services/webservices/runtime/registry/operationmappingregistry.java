package com.sap.engine.services.webservices.runtime.registry;

import com.sap.engine.services.webservices.exceptions.RegistryException;
import com.sap.engine.services.webservices.exceptions.PatternKeys;

import java.util.ArrayList;

/**
 * Title:  
 * Description: 
 * Copyright: Copyright (c) 2000
 * Company: Sap Labs Sofia 
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public class OperationMappingRegistry {

  private ExtHashtable operations = null;

  public OperationMappingRegistry() {
    this.operations = new ExtHashtable();
  }

  public ArrayList getOperations(com.sap.engine.interfaces.webservices.runtime.Key[] keys) {
    ArrayList resultSet = getAsArray(keys[0]);
    ArrayList tempSet = null;
    for (int i = 1; i < keys.length; i++) {
      if (resultSet.size() == 0) return resultSet;
      tempSet = getAsArray(keys[i]);
      resultSet = getIntersection(resultSet, tempSet);
    }
    return resultSet;
  }

  public void addOperation(com.sap.engine.interfaces.webservices.runtime.Key[] keys, com.sap.engine.interfaces.webservices.runtime.OperationDefinition operation) throws RegistryException {
    ArrayList identicalOperations = getOperations(keys);
    if (identicalOperations.size() != 0)
        throw new RegistryException(PatternKeys.WS_DUBLICATE_ELEMENT, new Object[]{keys, this.getClass().getName(), "operation name '" + operation.getOperationName() + "'"});
    for (int i = 0; i < keys.length; i++) {
      operations.register(keys[i], operation);
    }
  }

  private ArrayList getAsArray(com.sap.engine.interfaces.webservices.runtime.Key key) {
    Object value = operations.get(key);
    if (value == null) return new ArrayList();
    if (value instanceof ArrayList)
      return (ArrayList)value;
    else {
      ArrayList valueAsArr = new ArrayList();
      valueAsArr.add(value);
      return valueAsArr;
    }
  }

  private ArrayList getIntersection(ArrayList set1, ArrayList set2) {
    ArrayList intersectionSet = new ArrayList();
    if (set1 == null || set2 == null) return intersectionSet;
    if (set1.size() == 0 || set2.size() == 0) return intersectionSet;

    for (int i = 0; i < set1.size(); i++) {
      Object currentObj = set1.get(i);
      if (set2.contains(currentObj)) intersectionSet.add(currentObj);
    }

    return intersectionSet;
  }

  private String getMessage(com.sap.engine.interfaces.webservices.runtime.Key[] keys, String operationName) {
    String message =  "Unable to register operation " + operationName + "\n"
                    + "Operation with the following keys has been registered already: \n";
    for (int i = 0; i < keys.length; i++) {
      message += "key[" + i + "] = " + keys[i] + "\n";
    }
    return message;
  }

  public String toString() {
    if (operations != null) {
      return operations.toString();
    }
    return null;
  }
}
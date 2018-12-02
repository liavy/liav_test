package com.sap.engine.services.webservices.espbase.client.jaxws.metadata;

import java.lang.reflect.Method;
import java.util.Vector;

import com.sap.engine.services.webservices.espbase.client.bindings.ParameterObject;
import com.sun.xml.bind.api.TypeReference;

public class OperationMetadata  {
  
  private Vector<ParameterObject> operationParams;
  private Vector<ParameterObject> operationFaultParams;
  private ParameterObject operationReturnParam;
  private Vector<TypeReference> typeReferences;
  private ParameterObject[] allParamObjects;
  private Method syncMethod;
  
  protected OperationMetadata() {
    operationParams = new Vector();
    operationFaultParams = new Vector();
    typeReferences = new Vector();
  }
  
  protected void setSyncMehtod(Method syncMethod) {
    this.syncMethod = syncMethod;
  }
  
  public Method getSyncMethod() {
    return(syncMethod);
  }
  
  protected void addTypeReferenece(TypeReference typeReference) {
    typeReferences.add(typeReference);
  }
  
  protected void addOperationParameter(ParameterObject paramObject) {
    operationParams.add(paramObject);
  }
  
  protected void addFaultParameter(ParameterObject paramObject) {
    operationFaultParams.add(paramObject);
  }
  
  protected void setReturnParameter(ParameterObject operationReturnParam) {
    this.operationReturnParam = operationReturnParam;
  }
  
  public ParameterObject[] getOperationAllParameterObjects() {
    if(allParamObjects == null) {
      allParamObjects = createOperationAllParameterObjects();
    }
    return(allParamObjects);
  }
  
  private ParameterObject[] createOperationAllParameterObjects() {
    ParameterObject[] allParamObjects = new ParameterObject[operationParams.size() + operationFaultParams.size() + (operationReturnParam == null ? 0 : 1)];
    initOperationAllParameterObjects(allParamObjects, operationParams, 0);
    if(operationReturnParam != null) {
      allParamObjects[operationParams.size()] = operationReturnParam;
      initOperationAllParameterObjects(allParamObjects, operationFaultParams, operationParams.size() + 1);
    } else {
      initOperationAllParameterObjects(allParamObjects, operationFaultParams, operationParams.size());
    }
    return(allParamObjects);
  }
  
  public static ParameterObject[] createOperationParameterObjects(OperationMetadata operationMetadata) {
    ParameterObject[] allParamObjects = operationMetadata.getOperationAllParameterObjects();
    ParameterObject[] newParamObjects = new ParameterObject[allParamObjects.length];
    for(int i = 0; i < newParamObjects.length; i++) {
      newParamObjects[i] = new ParameterObject();       
      newParamObjects[i].parameterType = allParamObjects[i].parameterType;
      newParamObjects[i].typeReference = allParamObjects[i].typeReference;
    }    
    return(newParamObjects);
  }

  private static void initOperationParameterObjects(ParameterObject[] allParamObjects, Vector<ParameterObject> paramObjects, int startIndex) {
    int index = startIndex;
  }
  

  private void initOperationAllParameterObjects(ParameterObject[] allParamObjects, Vector<ParameterObject> paramObjects, int startIndex) {
    int index = startIndex;
    for(int i = 0; i < paramObjects.size(); i++) {
      allParamObjects[index++] = paramObjects.get(i); 
    }
  }

  public void reset() {
    for(int i = 0 ;i < operationFaultParams.size(); i++) {
      ParameterObject faultParamObject = operationFaultParams.get(i);  
      faultParamObject.parameterValue = null;
    }
  }
  
  public Vector<TypeReference> getTypeReferences() {
    return(typeReferences);
  }
  
  public Vector<ParameterObject> getOperationParameters() {
    return(operationParams);
  }
  
  public Vector<ParameterObject> getOperationFaultParameters() {
    return(operationFaultParams);
  }
  
  public ParameterObject getOperationReturnParameter() {
    return(operationReturnParam);
  }
}

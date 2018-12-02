package com.sap.engine.services.webservices.server.deploy.jee5;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.jws.WebMethod;

import com.sap.lib.javalang.annotation.AnnotationRecord;
import com.sap.lib.javalang.annotation.AnnotationRecord.NamedMember;
import com.sap.lib.javalang.element.ClassInfo;
import com.sap.lib.javalang.element.MethodInfo;

public class AnnotationParser {
	
  private static String EXCLUDE = "exclude"; 
  
  private ClassInfo implClassInfo; 
  private ClassInfo seiClassInfo; 
  
  private Hashtable<String, MethodInfo> wsMethods; 
  private Hashtable<String, MethodInfo> wsMethodsInterface;
  private Hashtable<String, String> operationNames; 
  
  public AnnotationParser() {
	  
  }
  
  public AnnotationParser(ClassInfo implClassInfo, ClassInfo seiClassInfo) {
    this.implClassInfo = implClassInfo; 
    this.seiClassInfo = seiClassInfo; 
  }
  
  public void setImplClassInfo(ClassInfo implClassInfo) {
	this.implClassInfo = implClassInfo;
  }
  
  public void setSeiClassInfo(ClassInfo seiClassInfo) {
	this.seiClassInfo = seiClassInfo;
  }
  
  public ClassInfo getImplClassInfo() {
	return implClassInfo;
  }  
  
  public ClassInfo getSeiClassInfo() {
	return seiClassInfo;
  }
 
  public Hashtable<String, MethodInfo> getWSMethods() {
    if(wsMethods == null) {
      if(seiClassInfo == null) {
        wsMethods = getWSMethods(implClassInfo);
      } else {
    	wsMethods = new Hashtable<String, MethodInfo>();
        getMethodsClass(getWSMethodsInterface().keySet(), implClassInfo, wsMethods);	  
      }
    }  
    
    return wsMethods;
  }  
  
  public Hashtable<String, MethodInfo> getWSMethodsInterface() {
    if(wsMethodsInterface == null) {
      if(seiClassInfo == null) {
        wsMethodsInterface = getWSMethods();  
      }	else {
        wsMethodsInterface = getWSMethods(seiClassInfo); 
      }
    } 	  
    
    return wsMethodsInterface; 
  }
  
  public Hashtable<String, String> getOperationNames() {
   if(operationNames == null) {
     operationNames = getOperationNames(getWSMethodsInterface()); 
   }
   
   return operationNames; 	  
  }
  
  public static Hashtable<String, MethodInfo> getWSMethods(ClassInfo classInfo) {
    Hashtable<String, MethodInfo> wsMethods = new Hashtable<String, MethodInfo>(); 
    
	if(classInfo.isInterface()) {
      getWSMethodsInterface(classInfo, wsMethods);	
    } else {
      getWSMethodsClass(classInfo, wsMethods); 	
    }	      
	
	return wsMethods;
  }
  
  public static void getMethodsClass(Set methodNames, ClassInfo classInfo, Hashtable<String, MethodInfo> methods) {
    if(classInfo == null || classInfo.getName().equals(Object.class.getName())) {
      return;  	
    }	  
    
    getMethodsClass(methodNames, classInfo.getSuperclass(), methods);
    getMethods(methodNames, classInfo, methods); 
  } 
  
  public static Hashtable<String, String> getOperationNames(Hashtable<String, MethodInfo> wsMethods) {
    if(wsMethods == null || wsMethods.size() == 0) {
      return new Hashtable<String, String>(); 	
    }	  
    
    Hashtable<String, String> operationNames = new Hashtable<String, String>(); 
    Enumeration<MethodInfo> enumer = wsMethods.elements();
    MethodInfo methodInfo; 
    String methodName; 
    Map<String, AnnotationRecord> annotations;
    AnnotationRecord webMethodAnnotation; 
    NamedMember operationNameMember; 
    String operationName = null; 
    while(enumer.hasMoreElements()) {
      methodInfo = enumer.nextElement();
      methodName = methodInfo.getName();
      operationName = null; 
      annotations = methodInfo.getAnnotations(); 
      if(annotations != null || annotations.size() != 0) {
        webMethodAnnotation = annotations.get(WebMethod.class.getName());
        if(webMethodAnnotation != null) {
          operationNameMember = webMethodAnnotation.getMember("operationName"); 
          if(operationNameMember != null) {
            operationName = operationNameMember.getStringValue();	  
          }
        }
      }
      
      if(operationName == null || operationName.equals("")) {
        operationName = methodName;   
      }
      
      operationNames.put(methodName, operationName);                  
    }    
    
    return operationNames; 
  }
  
  private static void getWSMethodsInterface(ClassInfo classInfo, Hashtable<String, MethodInfo> wsMethods) {
    Map<String, ClassInfo> interfaceInfoes = classInfo.getInterfaces();
    
    if(interfaceInfoes != null && interfaceInfoes.size() != 0) {
      Iterator iter = interfaceInfoes.entrySet().iterator();      
      while(iter.hasNext()) {
        getWSMethodsInterface((ClassInfo)((Map.Entry)iter.next()).getValue(), wsMethods);	  
      }	
    }
    
    getWSMethods(classInfo, wsMethods);    
  }
  
  private static void getWSMethodsClass(ClassInfo classInfo, Hashtable<String, MethodInfo> wsMethods) {
    if(classInfo == null || classInfo.getName().equals(Object.class.getName())) {
      return; 	
    }  
    
    getWSMethodsClass(classInfo.getSuperclass(), wsMethods); 
    getWSMethods(classInfo, wsMethods); 
  }
  
  private static void getWSMethods(ClassInfo classInfo, Hashtable<String, MethodInfo> wsMethods) {
    boolean isInterface = classInfo.isInterface();   
	MethodInfo[] methodInfoes = classInfo.getMethods();
    if(methodInfoes == null || methodInfoes.length == 0) {
      return;   	
    }
    	
    Map<String, AnnotationRecord> annotations; 
    String methodName;    
    AnnotationRecord webMethodAnnotation;      
    NamedMember excludeMember; 
    for(MethodInfo methodInfo: methodInfoes) {
      if((methodInfo.getModifiers() & MethodInfo.PUBLIC_MODIFIER) == 0) {
        continue; 	  
      }          
      
      methodName = methodInfo.getName();  
      annotations = methodInfo.getAnnotations();      
      webMethodAnnotation = annotations.get(WebMethod.class.getName()); 
      if(webMethodAnnotation != null ) {
    	excludeMember = webMethodAnnotation.getMember(EXCLUDE); 
    	if((excludeMember == null || !excludeMember.getBooleanValue())) {        
          wsMethods.put(methodName, methodInfo);  
        } else {
          if(isInterface) {
            //TODO - throw exception	  
          }
        }
      } else {
    	if(isInterface || (!isInterface && (classInfo.getAnnotation("javax.jws.WebService") != null || classInfo.getAnnotation("javax.xml.ws.WebServiceProvider") != null) )) {
    	  wsMethods.put(methodName, methodInfo);	
    	}    	
      }      
    }   
  }
  
  private static void getMethods(Set methodNames, ClassInfo classInfo, Hashtable<String, MethodInfo> methods) {
    MethodInfo[] methodInfoes = classInfo.getMethods(); 
    if(methodInfoes == null || methodInfoes.length == 0) {
      return ;	
    }
    
    String methodName; 
    for(MethodInfo methodInfo: methodInfoes) {
      methodName = methodInfo.getName(); 
      if(methodNames.contains(methodName)) {
        methods.put(methodName, methodInfo); 	  
      } 	
    }	  
  }  
  
  public boolean containsAnnotationsOR(String[] classLevelAnnotationNames) {
    return containsAnnotationsOR(classLevelAnnotationNames,  implClassInfo.getAnnotations());	  
  }
  
  public boolean containsAnnotationsORMethodLevel(String[] methodLevelAnnotationNames) {
    return containsAnnotationsOR(methodLevelAnnotationNames, getWSMethods()); 	  
  } 
  
  public boolean isSAPWebService() {
    return containsAnnotationsOR(AnnotationConstants.classLevelDTAnnotations_SAP, implClassInfo.getAnnotations()) || containsAnnotationsOR(AnnotationConstants.methodLevelDTAnnotations_SAP, getWSMethods());    	
  }
  
  private static boolean containsAnnotationsOR(String[] annotationNames, Hashtable<String, MethodInfo> methodInfoes) {
    if(methodInfoes == null || methodInfoes.size() == 0) {
      return false; 	
    }	  
    
    Enumeration enumer = methodInfoes.elements();
    MethodInfo methodInfo; 
    while(enumer.hasMoreElements()) {
      methodInfo = (MethodInfo)enumer.nextElement(); 
      if(containsAnnotationsOR(annotationNames, methodInfo.getAnnotations())) {
        return true;   
      }
    }    
    
    return false; 
  }
  
  private static boolean containsAnnotationsOR(String[] annotationNames, Map<String, AnnotationRecord> annotationRecords) {
    if(annotationRecords == null || annotationRecords.size() == 0) {
      return false; 	
    }
    
    for(String annotationName: annotationNames) {
      if(annotationRecords.containsKey(annotationName)) {
        return true;   
      }	
    }
    
    return false;         	  	 	 
  }  
 
}

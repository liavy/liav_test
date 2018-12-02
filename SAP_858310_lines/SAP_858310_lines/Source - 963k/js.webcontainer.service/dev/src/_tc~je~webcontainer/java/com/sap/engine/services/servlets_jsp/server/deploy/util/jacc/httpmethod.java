package com.sap.engine.services.servlets_jsp.server.deploy.util.jacc;

import com.sap.engine.services.httpserver.lib.protocol.Methods;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class HttpMethod {
  public static final String HTTP_METHODS_ALL = "_HTTP_ _METHODS_ _ALL_";
  public static final String[] STANDARD_HTTP_METHODS = {"GET", "HEAD", "POST", "PUT", "OPTIONS", "TRACE", "DELETE"};

  protected static final String TRANSPORT_GUARANTEE_NONE = "NONE";
  protected static final String TRANSPORT_GUARANTEE_INTEGRAL = "INTEGRAL";
  protected static final String TRANSPORT_GUARANTEE_CONFIDENTIAL = "CONFIDENTIAL";

  private String httpMethod = null;
  private boolean isStandard = false; 
  private HashSet securityRoles = new HashSet();
  private String transportGuarantee = TRANSPORT_GUARANTEE_NONE;
  private boolean methodDenied = false; // Access to this method is denied for all (roles)

  public HttpMethod(String httpMethod) {
    this.httpMethod = httpMethod;
    isStandard = isStandardHttpMethod(httpMethod);
    transportGuarantee = TRANSPORT_GUARANTEE_NONE;
  }

  public String getMethodName() {
    return httpMethod;
  }

  public boolean isStandard() {
    return isStandard;
  }

  public String getTransportGuarantee() {
    return transportGuarantee;
  }
  
  public boolean isNoneTransportGuarantee() {
    return transportGuarantee.equals(TRANSPORT_GUARANTEE_NONE);
  }

  public void addSecurityRole(String roleName) {
    if (methodDenied) {
      return;
    }
    securityRoles.add(roleName);
  }
  
  public boolean hasAnyRole() {
    return !securityRoles.isEmpty();
  }

  public String[] getAllRoles() {
    if (securityRoles.isEmpty()) {
      return null;
    }
    String[] result = new String[securityRoles.size()];
    Iterator en = securityRoles.iterator();
    int i = 0;
    while (en.hasNext()) {
      result[i++] = (String)en.next();
    }
    return result;
  }

  public void setMethodDenied() {
    securityRoles.clear();
    methodDenied = true;
  }

  public boolean isMethodDenied() {
    return methodDenied;
  }

  public boolean containsRole(String role) {
    return securityRoles.contains(role);
  }

  public void setTransportGuarantee(String transportGuarantee) {
    if (TRANSPORT_GUARANTEE_CONFIDENTIAL.equals(this.transportGuarantee)) {
      return;
    } else if (TRANSPORT_GUARANTEE_INTEGRAL.equals(this.transportGuarantee)) {
      if (TRANSPORT_GUARANTEE_CONFIDENTIAL.equals(transportGuarantee)) {
        this.transportGuarantee = TRANSPORT_GUARANTEE_CONFIDENTIAL;
      }
    } else {
      this.transportGuarantee = transportGuarantee;
      if (this.transportGuarantee == null || this.transportGuarantee.equals("")) {
        this.transportGuarantee = TRANSPORT_GUARANTEE_NONE;
      }
    }
  }
  
  
  static String[] getMethods(List<HttpMethod> httpMethodsList)  {
    if (httpMethodsList == null || httpMethodsList.isEmpty()) {
      return null;
    }
    List<String> methodsAsStrList = new LinkedList<String>();
    for (Iterator iter = httpMethodsList.iterator(); iter.hasNext();) {
      HttpMethod httpMethod = (HttpMethod) iter.next();
      methodsAsStrList.add(httpMethod.getMethodName());
    }
    return methodsAsStrList.toArray(new String[]{});
  } // 

  public static boolean isStandardHttpMethod(String httpMethod) {
    return Methods.GET.equals(httpMethod) || Methods.POST.equals(httpMethod)
            || Methods.PUT.equals(httpMethod) || Methods.HEAD.equals(httpMethod)
            || Methods.DELETE.equals(httpMethod) || Methods.OPTIONS.equals(httpMethod)
            || Methods.TRACE.equals(httpMethod);
  }
}

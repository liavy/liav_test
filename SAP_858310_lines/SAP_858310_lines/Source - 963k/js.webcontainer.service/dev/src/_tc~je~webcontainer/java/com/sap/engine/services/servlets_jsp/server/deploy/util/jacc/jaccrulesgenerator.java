package com.sap.engine.services.servlets_jsp.server.deploy.util.jacc;

import com.sap.engine.services.servlets_jsp.server.LogContext;

import javax.security.jacc.PolicyConfiguration;
import javax.security.jacc.PolicyContextException;
import javax.security.jacc.WebResourcePermission;
import javax.security.jacc.WebUserDataPermission;

import com.sap.tc.logging.Location;

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

public class JACCRulesGenerator {
  final static Location LOCATION;

  private static final String RULES_SEPARATOR = ":";
  
  static {
    LOCATION = LogContext.getLocationSecurity();
  }

  private PolicyConfiguration policyConfiguration = null;
  
  public JACCRulesGenerator(PolicyConfiguration policyConfiguration) {
    this.policyConfiguration = policyConfiguration;
  }

  public void generateAndAddPermissionsIntoPolicyConfiguration(Hashtable allUrlPatternConstraints, String[] allApplicationRoles)
        throws PolicyContextException {
    Enumeration en = allUrlPatternConstraints.elements();

    Vector uncheckedNoneUserDataPermissions = new Vector(10);         // TODO - Obsolete. Check for delete
    Vector uncheckedConfidentialUserDataPermissions = new Vector(10); // TODO - Obsolete. Check for delete
    Vector uncheckedIntegralUserDataPermissions = new Vector(10);     // TODO - Obsolete. Check for delete
    
    boolean hasSlashStarPatternDefined = allUrlPatternConstraints.get(UrlPattern.slashStar) != null;
    
    // iterate over URL pattern constraints
    while (en.hasMoreElements()) {
      UrlPatternConstraint urlPatternConstraint = (UrlPatternConstraint)en.nextElement();
      String ruleName = generateRuleName(urlPatternConstraint, allUrlPatternConstraints);

      uncheckedNoneUserDataPermissions.clear();
      uncheckedConfidentialUserDataPermissions.clear();
      uncheckedIntegralUserDataPermissions.clear();
      
      // JACC 1.1 p.25 (middle) - "/" and "*.ext" are made irrelevant by "/*" if defined
      if (hasSlashStarPatternDefined 
          && ( urlPatternConstraint.getUrlPattern().getPatternType() == UrlPattern.PATTERN_DEFAULT
              || urlPatternConstraint.getUrlPattern().getPatternType() == UrlPattern.PATTERN_EXTENSION)) {
        if (urlPatternConstraint.getUrlPattern().getPatternType() == UrlPattern.PATTERN_DEFAULT 
            && !urlPatternConstraint.hasMethodsDefined()) {
          // do not generate info message - it is default added pattern w/o explicit rules in web.xml
        } else {
          if (LOCATION.beInfo()) {
            LOCATION.debugT("Security constraint pattern \"" + urlPatternConstraint.getUrlPattern().getPattern() 
                + "\" is ignored. It is made irrelevant by existing \"/*\" pattern. ");
          }
        }  
      } else {
        // pattern is not irrelevant
        
        //find excluded
        HttpMethod[] excludedMethods = urlPatternConstraint.getAllExcludedMethods();
        if (excludedMethods != null && excludedMethods.length > 0) {
          String[] methodsTGConfidential = getMethods(excludedMethods, HttpMethod.TRANSPORT_GUARANTEE_CONFIDENTIAL);
          String[] methodsTGIntegral = getMethods(excludedMethods, HttpMethod.TRANSPORT_GUARANTEE_INTEGRAL);
          String[] methodsTGNone = getMethods(excludedMethods, HttpMethod.TRANSPORT_GUARANTEE_NONE);
          generatePermissionsExcluded(ruleName, methodsTGConfidential, HttpMethod.TRANSPORT_GUARANTEE_CONFIDENTIAL);
          generatePermissionsExcluded(ruleName, methodsTGIntegral, HttpMethod.TRANSPORT_GUARANTEE_INTEGRAL);
          generatePermissionsExcluded(ruleName, methodsTGNone, HttpMethod.TRANSPORT_GUARANTEE_NONE);
        }
  
        //find per role
        String[] allUsedRoles = urlPatternConstraint.getAllRoles();
        if (allUsedRoles != null) {
          for (int i = 0; allUsedRoles != null && i < allUsedRoles.length; i++) {
            HttpMethod[] methodsForRole = urlPatternConstraint.getAllMethods(allUsedRoles[i]);
            if (methodsForRole != null && methodsForRole.length > 0) {
              String[] methodsTGConfidential = getMethods(methodsForRole, HttpMethod.TRANSPORT_GUARANTEE_CONFIDENTIAL);
              String[] methodsTGIntegral = getMethods(methodsForRole, HttpMethod.TRANSPORT_GUARANTEE_INTEGRAL);
              String[] methodsTGNone = getMethods(methodsForRole, HttpMethod.TRANSPORT_GUARANTEE_NONE);
              if (allUsedRoles[i].equals("*")) {
                for (int j = 0; allApplicationRoles != null && j < allApplicationRoles.length; j++) {
                  generatePermissionsPerRole(allApplicationRoles[j], ruleName, methodsTGConfidential, HttpMethod.TRANSPORT_GUARANTEE_CONFIDENTIAL, uncheckedConfidentialUserDataPermissions);
                  generatePermissionsPerRole(allApplicationRoles[j], ruleName, methodsTGIntegral, HttpMethod.TRANSPORT_GUARANTEE_INTEGRAL, uncheckedIntegralUserDataPermissions);
                  generatePermissionsPerRole(allApplicationRoles[j], ruleName, methodsTGNone, HttpMethod.TRANSPORT_GUARANTEE_NONE, uncheckedNoneUserDataPermissions);
                }
              } else {
                generatePermissionsPerRole(allUsedRoles[i], ruleName, methodsTGConfidential, HttpMethod.TRANSPORT_GUARANTEE_CONFIDENTIAL, uncheckedConfidentialUserDataPermissions);
                generatePermissionsPerRole(allUsedRoles[i], ruleName, methodsTGIntegral, HttpMethod.TRANSPORT_GUARANTEE_INTEGRAL, uncheckedIntegralUserDataPermissions);
                generatePermissionsPerRole(allUsedRoles[i], ruleName, methodsTGNone, HttpMethod.TRANSPORT_GUARANTEE_NONE, uncheckedNoneUserDataPermissions);
              }
            }
          }
        }
  
        //find unchecked
        List<HttpMethod> mentionedMtdsInUncheckedRules = new LinkedList<HttpMethod>();
        List<HttpMethod> mentionedMtdsInExclOrRoleRules = new LinkedList<HttpMethod>(); 
        urlPatternConstraint.getAllUncheckedMethods(mentionedMtdsInUncheckedRules, mentionedMtdsInExclOrRoleRules);
        
        if (urlPatternConstraint.getUrlPattern().getPatternType() == UrlPattern.PATTERN_DEFAULT
            && mentionedMtdsInUncheckedRules.size() == 0  && mentionedMtdsInExclOrRoleRules.size() == 0) {
          // not explicitly defined rule in web.xml so add for all NONE
          urlPatternConstraint.addHttpMethod(HttpMethod.HTTP_METHODS_ALL, false);
          urlPatternConstraint.addHttpMethod(HttpMethod.HTTP_METHODS_ALL, false);
          urlPatternConstraint.setTransportGuarantee(HttpMethod.HTTP_METHODS_ALL, HttpMethod.TRANSPORT_GUARANTEE_NONE);
        }
        
        if (mentionedMtdsInUncheckedRules != null && mentionedMtdsInUncheckedRules.size() > 0) {
          HttpMethod[] explicitlyDeclaredUnchecked = mentionedMtdsInUncheckedRules.toArray(new HttpMethod[mentionedMtdsInUncheckedRules.size()]);
          String[] methodsTGConfidential = getMethods(explicitlyDeclaredUnchecked, HttpMethod.TRANSPORT_GUARANTEE_CONFIDENTIAL);
          String[] methodsTGIntegral = getMethods(explicitlyDeclaredUnchecked, HttpMethod.TRANSPORT_GUARANTEE_INTEGRAL);
          String[] methodsTGNone = getMethods(explicitlyDeclaredUnchecked, HttpMethod.TRANSPORT_GUARANTEE_NONE);
          if ( nullOrEmpty(methodsTGConfidential) && nullOrEmpty(methodsTGIntegral) 
               && (mentionedMtdsInExclOrRoleRules == null || mentionedMtdsInExclOrRoleRules.size() == 0) ) {
            // optimization required in CTS 5
            methodsTGNone = new String[]{HttpMethod.HTTP_METHODS_ALL};
          } else {
            generatePermissionsUnchecked(ruleName, methodsTGConfidential, HttpMethod.TRANSPORT_GUARANTEE_CONFIDENTIAL, uncheckedConfidentialUserDataPermissions);
            generatePermissionsUnchecked(ruleName, methodsTGIntegral, HttpMethod.TRANSPORT_GUARANTEE_INTEGRAL, uncheckedIntegralUserDataPermissions);
          }
          generatePermissionsUnchecked(ruleName, methodsTGNone, HttpMethod.TRANSPORT_GUARANTEE_NONE, uncheckedNoneUserDataPermissions);
        }
  
        generateRemainingUncheckedNonePermissions(urlPatternConstraint, ruleName);
      } // else - pattern is not irrelevant 
    } // while
  } // generateAndAddPermissionsIntoPolicyConfiguration()

  
  private void generateRemainingUncheckedNonePermissions(UrlPatternConstraint urlPatternConstraint, String rule) throws PolicyContextException {
    // WebUserDataPermissions
    List<HttpMethod> httpMethodsDiffThanUncheckedNone = urlPatternConstraint.getHttpMethodsDiffThanUncheckedNone();
    String[] methodsStrArr = HttpMethod.getMethods(httpMethodsDiffThanUncheckedNone);
    boolean beInfo = LOCATION.beInfo();
    if (methodsStrArr == null) {
      //  generate for all
      if (beInfo) {
        LOCATION.infoT("UNCHECKED: [" + rule + "], [ null ], [NONE] UDP(true)");
      }
      policyConfiguration.addToUncheckedPolicy(new WebUserDataPermission(rule, null)); 
    } else if (contains(methodsStrArr, HttpMethod.HTTP_METHODS_ALL)){
       // do not generate - already generated for rule for remaining
    } else {
      // non-empty list w/o ALL - build exception list, i.e. "for all methods except of theese"
      String exceptionList = "!" + getComaSeparatedList(methodsStrArr);
      if (beInfo) {
        LOCATION.infoT("UNCHECKED: [" + rule + "], [ " + exceptionList + " ], [NONE] UDP(false)");
      }
      policyConfiguration.addToUncheckedPolicy(new WebUserDataPermission(rule, exceptionList));
    }
    
    
    // for remaining unchecked WebResourcePermissions
    List<HttpMethod> httpMethodsForUncheckedNoneExceptionList = urlPatternConstraint.getHttpMethodsForUncheckedNoneResPermExceptionList();
    methodsStrArr = HttpMethod.getMethods(httpMethodsForUncheckedNoneExceptionList);
    if (methodsStrArr == null) {
      //  generate for all
      if (beInfo) {
      	LOCATION.infoT("UNCHECKED: [" + rule + "], [ null ] ResP(true)");
      }
      policyConfiguration.addToUncheckedPolicy(new WebResourcePermission(rule, (String)null)); 
    } else if (contains(methodsStrArr, HttpMethod.HTTP_METHODS_ALL)){
       // do not generate - already generated for rule for remaining
    } else {
      // non-empty list w/o ALL - build exception list
      String exceptionList = "!" + getComaSeparatedList(methodsStrArr);
      if (beInfo) {
      	LOCATION.infoT("UNCHECKED: [" + rule + "], [ " + exceptionList + " ] ResP(false)");
      }
      policyConfiguration.addToUncheckedPolicy(new WebResourcePermission(rule, exceptionList));
    }
  } // generateRemainingUncheckedNonePermissions()

  /**
   * Generates the URL pattern followed with all qualifying URL patterns separated with colons.
   * Ex: <code>&quot;/*:/path1/*:/path2/res3:/path2/path3/*&quot;</code> 
   * @param urlPatternConstraint
   * @param allUrlPatternConstraints
   * @return
   */
  private String generateRuleName(UrlPatternConstraint urlPatternConstraint, Hashtable allUrlPatternConstraints) {
    Vector urlPatternDependencies = findDependencies(urlPatternConstraint.getUrlPattern(), allUrlPatternConstraints);
    String rule = urlPatternConstraint.getUrlPattern().getPattern();
    Enumeration dependencies = urlPatternDependencies.elements();
    while (dependencies.hasMoreElements()) {
      rule += RULES_SEPARATOR + dependencies.nextElement();
    }
    return rule;
  }

  /**
   * Generates excluded JACC permissions into policy configuration for this transport guarantee 
   * @param rule URL pattern with optional qualifiers
   * @param httpMethods HTTP methods
   * @param transportGuarantee
   * @throws PolicyContextException
   */
  private void generatePermissionsExcluded(String rule, String[] httpMethods, String transportGuarantee) throws PolicyContextException {
    if (httpMethods == null) {
      return;
    }
    //TODO - check page 21 1st paragraph of the JavaACC 1.0 spec
    if (contains(httpMethods, HttpMethod.HTTP_METHODS_ALL)) {
      // exclude for all
      if (LOCATION.beInfo()) {
      	LOCATION.infoT("EXCLUDED: [" + rule + "], [" + arrayToString(null) + "], [" + transportGuarantee + "] BothP(" + true + ")");
      	LOCATION.debugT("  (Full HTTP methods list : [" + arrayToString(httpMethods) + "])");
      }
      policyConfiguration.addToExcludedPolicy(new WebResourcePermission(rule, (String[]) null));
      policyConfiguration.addToExcludedPolicy(new WebUserDataPermission(rule, null, transportGuarantee));
    } else {
      // not exclude for all HTTP methods
      if (LOCATION.beInfo()) {
      	LOCATION.infoT("EXCLUDED: [" + rule + "], [" + arrayToString(httpMethods) + "], [" + transportGuarantee + "] BothP(" + true + ")");
      }
      policyConfiguration.addToExcludedPolicy(new WebResourcePermission(rule, httpMethods));
      policyConfiguration.addToExcludedPolicy(new WebUserDataPermission(rule, httpMethods, transportGuarantee));
    }
  } // generatePermissionsExcluded()

  
  /**
   * Generates JACC permissions per role into policy configuration for the provided transport guarantee 
   * @param rule URL pattern with optional qualifiers
   * @param httpMethods HTTP methods
   * @param transportGuarantee
   * @throws PolicyContextException
   */
  private void generatePermissionsPerRole(String roleName, String rule, String[] httpMethods, String transportGuarantee, Vector uncheckedNoneUserDataPermissions) throws PolicyContextException {
    if (httpMethods == null) {
      return;
    }
    //TODO - check page 21 1st paragraph of the JavaACC 1.0 spec
    // - JACC 1.0 sample p. 27 & 3.1.3.1 Translating security-constraint Elements p. 20-21
    boolean genNowUserDataPermission; 
    String permissionsGeneratedStr; 
    if (HttpMethod.TRANSPORT_GUARANTEE_NONE.equals(transportGuarantee)) { // NONE should be accumulated and optimized. From role+unchecked = unchecked UDP (maybe ALL))
      genNowUserDataPermission = false;
      permissionsGeneratedStr = "ResP";
    } else {
      genNowUserDataPermission = true;
      permissionsGeneratedStr = "BothP";
    }
    if (contains(httpMethods, HttpMethod.HTTP_METHODS_ALL)) {
      httpMethods = null;
    }
    if (LOCATION.beInfo()) {
    	LOCATION.infoT("PER ROLE: [" + rule + "], [" + arrayToString(httpMethods) + "], [" + transportGuarantee + "] - ROLE: [" + roleName + "] " + permissionsGeneratedStr);
    }
    policyConfiguration.addToRole(roleName, new WebResourcePermission(rule, (String[])httpMethods));
    
    if (genNowUserDataPermission) {
      policyConfiguration.addToUncheckedPolicy(new WebUserDataPermission(rule, (String[]) httpMethods, transportGuarantee));
    } else {
      uncheckedNoneUserDataPermissions.addElement(httpMethods); // for generation at the end of processing pattern (unchecked for all but already used methods - in excluded or role/unchecked rules)
    }
  } // generatePermissionsPerRole()

  /**
   * Generates unchecked permissions. I.e. WebResourcePermissions. Also generates and WebUSerDataPermissions if transport guarantee
   * is not NONE. In such cases they should be merged with ROLE one's and optimized. 
   * @param rule - qualified URL pattern
   * @param httpMethods - String[] with HTTP methods for unchecked ruler
   * @param transportGuarantee 
   * @param uncheckedNoneUserDataPermissions accumulates HTTP methods for unchecked WebUserDataPermission. 
   *      Should be generated after accumulating role unchecked and unchecked (elements contain String[])
   * @throws PolicyContextException
   */
  private void generatePermissionsUnchecked(String rule, String[] httpMethods, String transportGuarantee, 
      Vector uncheckedNoneUserDataPermissions) throws PolicyContextException {
    if (httpMethods == null) {
      return;
    }
    boolean genNowUserDataPermission; 
    String permissionsGeneratedStr; 
    if (HttpMethod.TRANSPORT_GUARANTEE_NONE.equals(transportGuarantee)) { // NONE should be accumulated and optimized. From role+unchecked = unchecked UDP (maybe ALL))
      genNowUserDataPermission = false;
      permissionsGeneratedStr = "ResP";
    } else {
      genNowUserDataPermission = true;
      permissionsGeneratedStr = "BothP";
    }
    if (contains(httpMethods, HttpMethod.HTTP_METHODS_ALL)) {
      httpMethods = null;
    }
    if (LOCATION.beInfo()) {
    	LOCATION.infoT("UNCHECKED: [" + rule + "], [" + arrayToString(httpMethods) + "], [" + transportGuarantee + "]  " 
          + permissionsGeneratedStr);
    }
    policyConfiguration.addToUncheckedPolicy(new WebResourcePermission(rule, (String[]) httpMethods));
    if (genNowUserDataPermission) {
      policyConfiguration.addToUncheckedPolicy(new WebUserDataPermission(rule, (String[]) httpMethods, transportGuarantee));
    } else {
      uncheckedNoneUserDataPermissions.addElement(httpMethods); // for generation at the end of processing pattern (unchecked for all but already used methods - in excluded or role/unchecked rules)
    }
  } // generatePermissionsUnchecked()

 
  private static String arrayToString(Object[] objArr) {
    if (objArr == null) {
      return "[] null";
    }
    StringBuffer sb = new StringBuffer();
    sb.append("[");
    Object obj;
    int arrLen = objArr.length;
    for (int i = 0; i < arrLen; i++) {
      obj = objArr[i];
      if (i > 0) {
        sb.append(",");
      }
      sb.append(obj);
    }
    sb.append("]");
    return sb.toString();
  } // arrayToString()

  private Vector findDependencies(UrlPattern urlPattern, Hashtable allUrlPatternConstraints) {
    Vector result = new Vector();
    Enumeration en = allUrlPatternConstraints.elements();
    while (en.hasMoreElements()) {
      UrlPatternConstraint nextUrlPatterncoConstraint = (UrlPatternConstraint)en.nextElement();
      int compare = UrlPattern.compare(urlPattern, nextUrlPatterncoConstraint.getUrlPattern());
      if (compare == 2) {
        // TODO - canonicalize
        if (nextUrlPatterncoConstraint.getUrlPattern().getPatternType() == UrlPattern.PATTERN_EXACT
                && !nextUrlPatterncoConstraint.getUrlPattern().getPattern().startsWith("/")) {
          addSorted("/" + nextUrlPatterncoConstraint.getUrlPattern().getPattern(), result);
        } else {
          addSorted(nextUrlPatterncoConstraint.getUrlPattern().getPattern(), result);
        }
      }
    }
    return result;
  }

  private void addSorted(String urlPatter, Vector allUrlPatterns) {
    Object[] en = allUrlPatterns.toArray();
    if (en == null || en.length == 0) {
      allUrlPatterns.add(urlPatter);
      return;
    }
    boolean insertNeeded = true;
    for (int i = 0; i < en.length; i++) {
      String next = (String) en[i];
      int result = UrlPattern.compare(new UrlPattern(urlPatter), new UrlPattern(next));
      if (result == 2) {
        allUrlPatterns.add(allUrlPatterns.indexOf(next), urlPatter);
        //allUrlPatterns.remove(next);
        insertNeeded = false;
        break;
      } else if (result == 1) {
        allUrlPatterns.add(allUrlPatterns.indexOf(next) + 1, urlPatter);
        insertNeeded = false;
        break;
      }
    }
    if (insertNeeded) {
      allUrlPatterns.add(0, urlPatter);
    }
  }

  private String[] getMethods(HttpMethod[] httpMethods, String transportGuarantee) {
    if (httpMethods == null || httpMethods.length == 0) {
      return null;
    }
    if (transportGuarantee == null || transportGuarantee.equals("")) {
      transportGuarantee = HttpMethod.TRANSPORT_GUARANTEE_NONE;
    }
    Vector result = new Vector();
    for (int i = 0; i < httpMethods.length; i++) {
      if (transportGuarantee.equals(httpMethods[i].getTransportGuarantee())) {
        result.add(httpMethods[i].getMethodName());
      }
    }
    if (result.isEmpty()) {
      return null;
    }
    String[] resultStr = new String[result.size()];
    Enumeration en = result.elements();
    int i = 0;
    while (en.hasMoreElements()) {
      resultStr[i] = (String)en.nextElement();
      i++;
    }
    return resultStr;
  }

  private boolean contains(String[] httpMethods, String searchMethod) {
    if (httpMethods == null) {
      return false;
    }
    for (int i = 0; i < httpMethods.length; i++) {
      if (httpMethods[i].equals(searchMethod)) {
        return true;
      }
    }
    return false;
  }
  
  static boolean nullOrEmpty(Object[] arr) {
    return arr == null || arr.length == 0;
  } // nullOrEmpty()
  
  
  static String getComaSeparatedList(String[] array) {
    if (array == null || array.length == 0) {
      return null;
    }
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < array.length - 1; i++) {
      sb.append(array[i]);
      sb.append(",");
    }
    sb.append(array[array.length-1]);
    return sb.toString();
  }

  /*
  public static void main(String[] args) {
    String testPattern = "/test/*";
    Vector testVector = new Vector();
    testVector.add("/test/a/b/*");
    testVector.add("/test/a/*");
    testVector.add("/*");
    new JACCRulesGenerator(null, null).addSorted(testPattern, testVector);
    System.out.println("testVector = " + testVector);
  }
  */
}

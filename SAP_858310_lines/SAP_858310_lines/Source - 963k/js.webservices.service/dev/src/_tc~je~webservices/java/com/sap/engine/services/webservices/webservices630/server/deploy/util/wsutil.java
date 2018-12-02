package com.sap.engine.services.webservices.webservices630.server.deploy.util;

import com.sap.engine.services.webservices.webservices630.server.deploy.common.WSBaseConstants;
import com.sap.engine.services.webservices.webservices630.server.deploy.wsclient.WSClientsConstants;

import javax.naming.*;
import java.io.File;
import java.util.*;

/**
 * Title: WSUtil
 * Description: The class provides help methods.
 * Copyright: Copyright (c) 2003
 * Company: Sap Labs Sofia
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public class WSUtil {

  public static String getModuleNameByType(String fileName) {
    String extension = IOUtil.getFileExtension(fileName);
    int moduleType = getModuleType(extension);
    if(moduleType != -1) {
      return getJarNameAsString(fileName) + moduleType;
    } else {
      return replaceForbiddenChars(fileName);
    }
  }

  public static String getFileNameAsString(String fileName) {
    String tmpString = replaceForbiddenChars(fileName);
    return tmpString.replace('.', '_');
  }

  public static String getJarNameAsString(String jarName) {
    jarName = new File(jarName).getName();
    if (jarName.lastIndexOf(".") != -1) {
      jarName = jarName.substring(0, jarName.lastIndexOf("."));
    }
    return replaceForbiddenChars(jarName);
  }

  public static String replaceForbiddenChars(String str) {
    char[] forbiddenChars = new char[]{'-', '\\', '/', ':', '*', '?', '"', '<', '>', '|', '~', ' '};
    int charsLength = forbiddenChars.length;
    for (int i = 0; i < charsLength; i++) {
      str = str.replace(forbiddenChars[i], '_');
    }
    return str;
  }

  public static String cutString(String str, String subStr) {
    String resultStr = str;

    int cutIndex = str.indexOf(subStr);
    if (cutIndex != -1) {
      resultStr = str.substring(cutIndex);
    }

    return resultStr;
  }

  public static String getIndentString(String prefix, ArrayList strs) {
    String nl = "\n";
    if(strs == null) {
      return null;
    }
    String resultStr = "";
    for(int i = 0; i < strs.size(); i++) {
      String str = (String)strs.get(i);
      resultStr += prefix + str + nl;
    }

    return resultStr;
  }

  public static String[] getSeparateStrings(String str, String delimiter) {
    StringTokenizer stringTokenizer = new StringTokenizer(str, delimiter);

    ArrayList separateStrings = new ArrayList();
    while(stringTokenizer.hasMoreTokens()) {
      String nextStr = stringTokenizer.nextToken();
      separateStrings.add(nextStr);
    }

    return getAsArrayOfStrings(separateStrings);
  }

  public static String[] getAsArrayOfStrings(ArrayList strArrayList) {
    String[] stringsArray = new String[strArrayList.size()];

    for(int i = 0; i < strArrayList.size(); i++) {
      stringsArray[i] = (String)strArrayList.get(i);
    }

    return stringsArray;
  }

  public static String[] addPrefixToStrings(String prefix, String[] strings) {
    return addStrToStrings(prefix, strings, true);
  }

  public static String[] addSuffixToStrings(String[] strings, String suffix) {
    return addStrToStrings(suffix, strings, false);
  }

  public static Vector addPrefixToStrings(String prefix, Vector strings) {
    return addStrToStrings(prefix, strings, true);
  }

  public static Vector addSuffixToStrings(Vector strings, String suffix) {
    return addStrToStrings(suffix, strings, false);
  }

  public static Vector addStrToStrings(String str, Vector strings, boolean isPrefix) {
     if (strings == null) {
      return new Vector();
    }

    Vector resultStrings = new Vector();
    for(int i = 0; i < strings.size(); i++) {
      if(isPrefix) {
        resultStrings.add(str + strings.get(i));
      } else {
        resultStrings.add(strings.get(i) + str);
      }
    }

    return resultStrings;
  }

  public static String[] addStrToStrings(String str, String[] strings, boolean isPrefix) {
    if (strings == null) {
      return new String[0];
    }

    String[] resultStrings = new String[strings.length];
    for(int i = 0; i < strings.length; i++) {
      if(isPrefix) {
        resultStrings[i] = str + strings[i];
      } else {
        resultStrings[i] = strings[i] + str;
      }
    }

    return resultStrings;
  }

  public static String[] unifyStrings(String[][] strings) {
    if(strings == null) {
      return new String[0];
    }
    
    String[] allStrings = new String[0];
    int size = strings.length;
    for(int i = 0; i < size; i++) {
      String[] currentStrings = strings[i];
      if(currentStrings == null) {
        continue; 
      }
      String[] newStrings = new String[allStrings.length + currentStrings.length];
      System.arraycopy(allStrings, 0, newStrings, 0, allStrings.length);
      System.arraycopy(currentStrings, 0, newStrings, allStrings.length, currentStrings.length);
      allStrings = newStrings;
    }
    return allStrings;
  }
  
  public static String[] unifyStrings(ArrayList<String[]> strs) {
    if(strs == null) {
      return new String[0];
    }
    
    if(strs.size() == 0) {
      return strs.get(0); 	
    }
        
    ArrayList<String> strList = new ArrayList<String>(); 
    for(String[] currentStrs: strs) {
      addStrings(currentStrs, strList); 	
    }
    
    return strList.toArray(new String[strList.size()]);
  }
  
  public static void printStrings(String[] strings) {
    if (strings == null) {
      return;
    }

    for(int i = 0; i < strings.length; i++) {
      String string = strings[i];
      System.out.println(string);
    }
  }

   public static boolean startsWith(String str, String[] prefixes) {
    return (startsWithPrefix(str, prefixes) != null);
  }

  public static String startsWithPrefix(String str, String[] prefixes) {
    for(int i = 0; i < prefixes.length; i++) {
      String prefix = prefixes[i];
      if(str.startsWith(prefix)) {
        return prefix;
      }
    }
    return null;
  }

  public static boolean endsWith(String str, String[] suffixes) {
    return (endsWithPrefix(str, suffixes) != null);
  }

  public static String endsWithPrefix(String str, String[] suffixes) {
    for(int i = 0; i < suffixes.length; i++) {
      String suffix = suffixes[i];
      if (str.endsWith(suffix)) {
        return suffix;
      }
    }

    return null;
  }

  public static String getUpLevelStringLeftToRight(String str, String delimiter, int level) {
    StringTokenizer tokenizer = new StringTokenizer(str, delimiter);

    if (level < 0) {
      return null;
    }

    int i = 0;
    while(tokenizer.hasMoreTokens()) {
      String currentStr = tokenizer.nextToken();
      if(i == level) {
        return currentStr;
      }
    }

    return null;
  }

  public static String getUpLevelStringRightToLeft(String str, String delimiter, int level) {
    StringTokenizer tokenizer = new StringTokenizer(str, delimiter);
    int tokensCount = tokenizer.countTokens();

    return getUpLevelStringLeftToRight(str, delimiter, tokensCount - level - 1);
  }

  public static String getLastToken(String str, String delimiter) {
    return getUpLevelStringRightToLeft(str, delimiter, 0);
  }

  public static String[] collectStringProperties(Properties properties, String delimiter) {
    if(properties == null) {
      return new String[0];
    }

    String[] strs = new String[0];
    Enumeration enum1 = properties.elements();
    while(enum1.hasMoreElements()) {
      String str = (String)enum1.nextElement();
      strs = WSUtil.unifyStrings(new String[][]{strs, collectStrings(str, delimiter)});
    }

    return strs;
  }

  public static Set collectPropertiesSet(Properties properties, String delimiter) {
    if(properties == null) {
      return new HashSet();
    }

    HashSet strSet = new HashSet();
    Enumeration enum1 = properties.elements();
    while(enum1.hasMoreElements()) {
      String str = (String)enum1.nextElement();
      strSet.addAll(collectStringsSet(str, delimiter));
    }

    return strSet;
  }

  public static String[] collectStrings(String str, String delimiter) {
    StringTokenizer strTokenizer = new StringTokenizer(str, delimiter);

    Vector strs = new Vector();
    while(strTokenizer.hasMoreTokens()) {
      strs.add(strTokenizer.nextToken());
    }

    String[] strsArr = new String[strs.size()];
    strs.copyInto(strsArr);

    return strsArr;
  }

  public static Set collectStringsSet(String str, String delimiter) {
    StringTokenizer strTokenizer = new StringTokenizer(str, delimiter);

    HashSet strSet = new HashSet();
    while(strTokenizer.hasMoreTokens()) {
      strSet.add(strTokenizer.nextToken());
    }

    return strSet;
  }

  public static Set collectSet(Object[] objs) {
    if(objs == null) {
      return new HashSet();
    }

    HashSet set = new HashSet();
    for(int i = 0; i < objs.length; i++) {
      set.add(objs[i]);
    }

    return set;
  }

  public static String getApplicationProviderName(String applicationName) {
    int cutIndex = applicationName.indexOf("/");

    if(cutIndex == -1) {
      return null;
    }

    return applicationName.substring(0, cutIndex);
  }

   public static String getPureApplicationName(String applicationName){
    int cutIndex = applicationName.indexOf("/");

    if(cutIndex == -1) {
      return null;
    }

    return applicationName.substring(cutIndex + 1);
  }

  public static Context createSubContext(Context rootCtx, String subCtxName) throws NamingException {
    String[] subCtxNames = WSUtil.getSeparateStrings(subCtxName, "/");
    if (subCtxNames == null || subCtxNames.length == 0) {
      return null;
    }

    Context ctx = rootCtx;
    for(int i = 0; i < subCtxNames.length; i++) {
      String currentSubCtxName = subCtxNames[i];
      try {
        ctx = ctx.createSubcontext(currentSubCtxName);
      } catch(NameAlreadyBoundException e) {
        ctx = (Context)ctx.lookup(currentSubCtxName);
      }
    }

    return ctx;
  }

  public static String getParentContexName(String jndiName) {
    int cutIndex = jndiName.lastIndexOf("/");

    if (cutIndex == -1) {
      return null;
    }

    return jndiName.substring(0, cutIndex);
  }

  public static String getJndiName(String jndiName) {
    int cutIndex = jndiName.lastIndexOf("/");

    if (cutIndex == -1) {
      return jndiName;
    }

    return jndiName.substring(cutIndex + 1);
  }

  public static Context lookUpContext(Context rootCtx, String subCtxName) throws NamingException {
    if(subCtxName == null) {
      return null;
    }

    String[] subCtxNames = getSeparateStrings(subCtxName, "/");
    Context ctx = rootCtx;
    for(int i = 0; i < subCtxNames.length; i++) {
      String currentCtxName = subCtxNames[i];
      ctx = (Context)ctx.lookup(currentCtxName);
    }

    return ctx;
  }

  public static void rebind(Context ctx, String jndiName, Object obj) throws NamingException {
    String parentCtxName = getParentContexName(jndiName);
    String realJndiName = getJndiName(jndiName);

    Context parentCtx = null;
    if(parentCtxName == null) {
      parentCtx = ctx;
    } else {
      parentCtx = createSubContext(ctx, parentCtxName);
    }

    parentCtx.rebind(realJndiName, obj);
  }

  public static void unbind(Context ctx, String jndiName) throws NamingException {
    ctx.unbind(jndiName);

    String parentCtx = getParentContexName(jndiName);
    destroyEmptyContext(ctx, parentCtx);
  }

  public static void destroyEmptyContext(Context rootCtx, String relCtxName) throws NamingException {
    if(relCtxName == null) {
      return;
    }

    String currentRelCtxName = relCtxName;
    int cutIndex = currentRelCtxName.length();
    while(cutIndex != -1) {
      currentRelCtxName = currentRelCtxName.substring(0, cutIndex);
      Context currentRelCtx = (Context)rootCtx.lookup(currentRelCtxName);
      if(isEmpty(currentRelCtx)) {
        destroyContext(rootCtx, currentRelCtxName);
      }

      cutIndex = currentRelCtxName.lastIndexOf("/");
    }
  }

  public static void destroyApplicationContext(Context ctx, String applicationName) throws NamingException {
    try {
      lookUpContext(ctx, applicationName);
    } catch (NameNotFoundException e) {
      return;
    }

    String providerName = getApplicationProviderName(applicationName);
    String pureApplicationName = getPureApplicationName(applicationName);
    Context providerCtx = lookUpContext(ctx, providerName);

    if (providerCtx != null) {
      destroyContext(providerCtx, pureApplicationName);
      if (isEmpty(providerCtx)) {
        destroyContext(ctx, providerName);
      }
    }
  }

  public static void destroyContext(Context root, String contextToDestroy) throws NamingException {
    emptyContext((Context) root.lookup(contextToDestroy));
    root.destroySubcontext(contextToDestroy);
  }

  public static void emptyContext(Context root) throws NamingException {
    NameClassPair pair = null;

    for (NamingEnumeration ne = root.list(""); ne.hasMoreElements();) {
      pair = (NameClassPair) ne.nextElement();

      if (isContext(pair.getClassName())) {
        emptyContext((Context) root.lookup(pair.getName()));
        root.destroySubcontext(pair.getName());
      } else {
        root.unbind(pair.getName());
      }
    }
  }

  public static boolean isContext(String name) {
    return ((name.indexOf("javax.naming.Context") != -1) || (name.indexOf("javax.naming.directory.DirContext") != -1));
  }

  private static boolean isEmpty(Context ctx) {
   try {
     NamingEnumeration ne = ctx.list("");
     return !ne.hasMoreElements();
   } catch (NamingException e) {
     return false;
   }
 }

  public static Reference getLinkReference(String jndiLinkName) {
    return new Reference(jndiLinkName, WSClientsConstants.COMPONENT_FACTORY,  null);
  }

  public static int getModuleType(String extension) {
    if (extension.equals(WSBaseConstants.WSAR_EXTENSION)) {
      return WSBaseConstants.WS_TYPE;
    } else if (extension.equals(WSBaseConstants.JAR_EXTENSION)) {
      return WSBaseConstants.EJB_TYPE;
    } else if (extension.equals(WSBaseConstants.WAR_EXTENSION)) {
      return WSBaseConstants.WEB_TYPE;
    } else {
      return -1;
    }
  }

  public static Properties unifyProperties(Properties props1, Properties props2, String delimiter) {
    if(props1 == null) {
      return props2;
    }
    if(props2 == null) {
      return props1;
    }

    Properties resultProps = new Properties();
    resultProps.putAll(makeSubtraction(props1, props2));
    resultProps.putAll(makeSubtraction(props2, props1));

    Properties intersectionProps = makeIntersection(props1, props2);
    Enumeration enum1 = intersectionProps.keys();
    while(enum1.hasMoreElements()) {
      String key = (String)enum1.nextElement();
      resultProps.setProperty(key, WSUtil.concatStrings(new String[]{props1.getProperty(key), props2.getProperty(key)}, delimiter));
    }

    return resultProps;
  }

  public static Properties unifyProperties(Properties[] props, String delimiter) {
    if(props == null) {
      return new Properties();
    }
    Properties resultProps = new Properties();
    for(int i = 0; i < props.length; i++) {
      resultProps = unifyProperties(resultProps, props[i], delimiter);
    }

    return resultProps;
  }

  public static Properties makeSubtraction(Properties t1, Hashtable t2) {
    if(t1 == null || t2 == null) {
      return new Properties();
    }

    Properties substractionTable = new Properties();
    Enumeration t1Enum = t1.keys();
    while(t1Enum.hasMoreElements()) {
      Object key = t1Enum.nextElement();
      if(!t2.containsKey(key)) {
        substractionTable.put(key, t1.get(key));
      }
    }

    return substractionTable;
  }

  public static Hashtable makeSubtraction(Hashtable t1, Hashtable t2) {
    if(t1 == null || t2 == null) {
      return new Hashtable();
    }

    Hashtable substractionTable = new Hashtable();
    Enumeration t1Enum = t1.keys();
    while(t1Enum.hasMoreElements()) {
      Object key = t1Enum.nextElement();
      if(!t2.containsKey(key)) {
        substractionTable.put(key, t1.get(key));
      }
    }

    return substractionTable;
  }

  public static Properties makeIntersection(Properties t1, Hashtable t2) {
    if(t1 == null || t2 == null) {
      return new Properties();
    }

    Properties intersectionTable = new Properties();
    Enumeration t1Enum = t1.keys();
    while(t1Enum.hasMoreElements()) {
      Object key = t1Enum.nextElement();
      if(t2.containsKey(key)) {
        intersectionTable.put(key, t1.get(key));
      }
    }

    return intersectionTable;
  }

  public static Hashtable makeIntersection(Hashtable t1, Hashtable t2) {
    if(t1 == null || t2 == null) {
      return new Hashtable();
    }

    Hashtable intersectionTable = new Hashtable();
    Enumeration t1Enum = t1.keys();
    while(t1Enum.hasMoreElements()) {
      Object key = t1Enum.nextElement();
      if(t2.containsKey(key)) {
        intersectionTable.put(key, t1.get(key));
      }
    }

    return intersectionTable;
  }

  public static Properties filterProperties(Set keySetFiltered, Properties props) {
    if(keySetFiltered == null) {
      return new Properties();
    }

    Properties propsFiltered = new Properties();
    Iterator iter = keySetFiltered.iterator();
    while(iter.hasNext()) {
      String key = (String)iter.next();
      if(props.containsKey(key)) {
        propsFiltered.put(key, props.getProperty(key));
      }
    }

    return propsFiltered;
  }

  public static String[] collectKeys(Properties props) {
    if(props == null) {
      return new String[0];
    }

    String[] keys = new String[props.size()];
    Enumeration enum1 = props.keys();
    int i = 0;
    while(enum1.hasMoreElements()) {
      keys[i++] = (String)enum1.nextElement();
    }

    return keys;
  }

  public static String[] collecProperties(Properties props) {
    if(props == null) {
      return new String[0];
    }

    String[] properties = new String[props.size()];
    Enumeration enum1 = props.elements();
    int i = 0;
    while(enum1.hasMoreElements()) {
      properties[i++] = (String)enum1.nextElement();
    }

    return properties;
  }

  public static Set collectKeySet(Hashtable table) {
    if(table == null) {
      return new HashSet();
    }

    HashSet keySet = new HashSet();
    Enumeration enum1 = table.keys();
    while(enum1.hasMoreElements()) {
      keySet.add(enum1.nextElement());
    }

    return keySet;
  }

  public static int getMaxPositiveInt(Set ints) {
    if(ints == null || ints.size() == 0) {
      return -1;
    }

    Iterator iter = ints.iterator();
    int maxInt = ((Integer)iter.next()).intValue();
    while(iter.hasNext()) {
      int currentInt = ((Integer)iter.next()).intValue();
      if(currentInt > maxInt) {
        maxInt = currentInt;
      }
    }

    return maxInt;
  }

  public static Set getMissedInts(Set ints, int minInt, int maxInt) {
    Set missedInts = new HashSet();

    for(int i = minInt; i <= maxInt; i++) {
      if(!ints.contains(new Integer(i))) {
        missedInts.add(new Integer(i));
      }
    }

    return missedInts;
  }
  
  public static int[] getIntArray(Set intSet) {
    if(intSet == null) {
      return new int[0];
    }

    Iterator iter = intSet.iterator();
    int[] intArray = new int[intSet.size()];
    int i = 0;
    while(iter.hasNext()) {
      intArray[i++] = ((Integer)iter.next()).intValue();
    }

    return intArray;
  }

  public static int[] unifyInts(int[][] ints) {
    if(ints == null) {
      return new int[0];
    }

    int[] allInts = new int[0];
    for(int i = 0; i < ints.length; i++) {
      int[] currentInts = ints[i];
      int[] newInts = new int[allInts.length + currentInts.length];
      System.arraycopy(allInts, 0, newInts, 0, allInts.length);
      System.arraycopy(currentInts, 0, newInts, allInts.length, currentInts.length);
      allInts = newInts;
    }

    return allInts;
  }
  
  public static String concatStrings(Vector<String> strs, String delimiter) {
    if(strs == null || strs.size() == 0) {
      return null;
    }

    StringBuffer strBuffer = new StringBuffer(); 
    for(String str: strs) {
      strBuffer.append(str);
      strBuffer.append(delimiter);
    }

    return strBuffer.toString(); 
  }
  
  public static String concatStrings(ArrayList<String> strs, String delimiter) {
    if(strs == null || strs.size() == 0) {
      return null;
    }

    StringBuffer strBuffer = new StringBuffer(); 
    for(String str: strs) {
      strBuffer.append(str);
      strBuffer.append(delimiter);
    }

    return strBuffer.toString(); 
  }
  
  public static String concatStrings(String[] strs, String delimiter) {
    if(strs == null || strs.length == 0) {
      return null;
    }

    StringBuffer strBuffer = new StringBuffer(); 
    for(String str: strs) {
      strBuffer.append(str); 
      strBuffer.append(delimiter);
    }

    return strBuffer.toString();
  }
  
  public static void addStrings(String[] srcStrs, ArrayList<String> destStrs) {
    if(srcStrs == null || srcStrs.length == 0) {
      return; 	
    }  
    
    for(String str: srcStrs) {
      destStrs.add(str);	
    }         
  }
  
  public boolean containsString(String str, String[] strs) {
   if(strs == null || strs.length == 0) {
      return false; 	
    }	  
    
    for(String currentStr: strs) {
      if(currentStr.equals(str)) {
        return true; 	  
      }	
    }
    
    return false; 
  }
  
}

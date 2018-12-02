/*
 * Copyright (c) 2000 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.server.application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.sap.engine.lib.descriptors5.web.ErrorPageType;
import com.sap.engine.lib.descriptors5.web.FilterMappingType;
import com.sap.engine.services.httpserver.interfaces.RequestPathMappings;
import com.sap.engine.services.httpserver.lib.util.MessageBytes;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.lib.Constants;
import com.sap.tc.logging.Location;

public class WebMappings {

  private static Location currentLocation = Location.getLocation(WebMappings.class);

  private HashMap<MessageBytes, MessageBytes> exactMap = new HashMap<MessageBytes, MessageBytes>();
  private HashMap<MessageBytes, MessageBytes> pathMap = new HashMap<MessageBytes, MessageBytes>();
  private HashMap<MessageBytes, MessageBytes> extMap = new HashMap<MessageBytes, MessageBytes>();

  private HashMap<MessageBytes, MessageBytes> exactMapJsp = null;
  private HashMap<MessageBytes, MessageBytes> pathMapJsp = null;
  private HashMap<MessageBytes, MessageBytes> extMapJsp = null;

  private List<ErrorPageType> errorCode = new ArrayList<ErrorPageType>();
  private List<ErrorPageType> errorException = new ArrayList<ErrorPageType>();

  private FilterMappingType[] filterMapping = null;

  private ConcurrentHashMap<MessageBytes, HashMap<String,String[]>> servletNameUrl = null;

  private MessageBytes servletMappedSlashStar = new MessageBytes("".getBytes());
  private boolean defaultServlet = false;
  private MessageBytes defaultServletName = null;

  private ClassLoader appClassLoader = null;

  public WebMappings(ClassLoader appClassLoader) {
    this.appClassLoader = appClassLoader;
  }

  public void addMapping(String servletMap, String servletName) {
    if (!servletMap.startsWith("/") && !servletMap.equals("") && !servletMap.startsWith("*.") && !servletMap.endsWith("/*")) {
      servletMap = "/" + servletMap;
    }

    MessageBytes MBservletMap = new MessageBytes(servletMap.getBytes());
    MessageBytes MBservletName = new MessageBytes(servletName.getBytes());

    //    if (MBservletMap.startsWith("/") && MBservletMap.endsWith("/*") || MBservletMap.equals("/")) {
    if (MBservletMap.startsWith("/") && MBservletMap.endsWith("/*")) {
      pathMap.put(MBservletMap, MBservletName);

      if (MBservletMap.equals("/*")) {
        servletMappedSlashStar = MBservletName;
      }

      return;
    }

    if (MBservletMap.equals("/")) {
      defaultServlet = true;
      defaultServletName = MBservletName;
      return;
    }

    if (MBservletMap.startsWith("*.")) {
      MBservletMap = new MessageBytes(MBservletMap.getBytes(1, MBservletMap.length() - 1));
      extMap.put(MBservletMap, MBservletName);
      return;
    }

    if (MBservletMap.endsWith("/*")) {
      MBservletMap.appendBefore("/".getBytes());
      exactMap.put(MBservletMap, MBservletName);
      return;
    }

    exactMap.put(MBservletMap, MBservletName);
    return;
  }

  public void addJspMapping(String urlPattern) {
    if (urlPattern == null) {
      return;
    }
    if (!urlPattern.startsWith("/")
      && !urlPattern.equals("")
      && !urlPattern.startsWith("*.")
      && !urlPattern.endsWith("/*")) {
      urlPattern = "/" + urlPattern;
    }
    MessageBytes MBjspMap = new MessageBytes(urlPattern.getBytes());
    //MessageBytes MBservletName = new MessageBytes(servletName.getBytes());
		MessageBytes MBjspName = new MessageBytes("jsp".getBytes());

    if (MBjspMap.startsWith("/") && MBjspMap.endsWith("/*")) {
      if( pathMapJsp == null ) {
        pathMapJsp = new HashMap<MessageBytes, MessageBytes>();
      }
      pathMapJsp.put(MBjspMap, MBjspName);
      return;
    }
    /*
    if (MBjspMap.equals("/")) {
      defaultServlet = true;
      defaultServletName = MBservletName;
      return;
    }
    */
    if (MBjspMap.startsWith("*.")) {
      MBjspMap = new MessageBytes(MBjspMap.getBytes(1, MBjspMap.length() - 1));
      if( extMapJsp == null ) {
        extMapJsp = new HashMap<MessageBytes, MessageBytes>();
      }
      extMapJsp.put(MBjspMap, MBjspName);
      return;
    }
    if (MBjspMap.endsWith("/*")) {
      MBjspMap.appendBefore("/".getBytes());
      exactMap.put(MBjspMap, MBjspName);
      return;
    }
    if( exactMapJsp == null ) {
      exactMapJsp = new HashMap<MessageBytes, MessageBytes>();
    }
    exactMapJsp.put(MBjspMap, MBjspName);
    return;
  }

  void addFilterMapping(FilterMappingType[] fMappings) {
    if (fMappings == null) {
      return;
    }
    if (filterMapping == null) {
      filterMapping = new FilterMappingType[fMappings.length];
      System.arraycopy(fMappings, 0, filterMapping, 0, fMappings.length);
    } else {
      FilterMappingType[] newFMappings = new FilterMappingType[filterMapping.length + fMappings.length];
      System.arraycopy(filterMapping, 0, newFMappings, 0, filterMapping.length);
      System.arraycopy(fMappings, 0, newFMappings, filterMapping.length, fMappings.length);
      filterMapping = newFMappings;
    }
  }

  public boolean isServletMappingExist(String servletName, String urlPattern) {
    MessageBytes MBServletMap = new MessageBytes(urlPattern.getBytes());
    MessageBytes MBServletName = exactMap.get(MBServletMap);
    if (MBServletName != null && MBServletName.equals(servletName)) {
      return true;
    }
    
    MBServletName = pathMap.get(MBServletMap);
    if (MBServletName != null && MBServletName.equals(servletName)) {
      return true;
    }
    
    MBServletName = extMap.get(MBServletMap);
    if (MBServletName != null && MBServletName.equals(servletName)) {
      return true;
    } 
    
    return false;
  }
  
  void addErrorPageForException(ErrorPageType errorPage) {
		if (!errorPage.getChoiceGroup1().isSetExceptionType()) {
			return;
		}
    Class exceptionClassToAdd;
    try {
      String exceptionTypeToAdd = errorPage.getChoiceGroup1().getExceptionType().get_value();
      exceptionClassToAdd = appClassLoader.loadClass(exceptionTypeToAdd);
    } catch (ClassNotFoundException cnfe) {
    	//TODO:Polly web app name?
    	LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logWarning(currentLocation, "ASJ.web.000393", 
    	  "Problem in error-page declaration in the web deployment descriptor web.xml. " +
    	  "Exception-type subtag is not specified properly.", cnfe, null, null);
      return;
		} catch (Exception e) {
	    	//TODO:Polly web app name?
			LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logWarning(currentLocation, "ASJ.web.000394",
				"Problem in error-page declaration in the web deployment descriptor web.xml. " +
				"Exception-type subtag is not specified properly.", e, null, null);
		 return;
		}

    Class exceptionClass;
    int size = errorException.size();
    for (int i = 0; i < size; i++) {
    String exceptionType = "";
      try {
        exceptionType = errorException.get(i).getChoiceGroup1().getExceptionType().get_value();
        exceptionClass = appClassLoader.loadClass(exceptionType);
      } catch (ClassNotFoundException cnfe) {
     	  //TODO:Polly web app name?
				LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logWarning(currentLocation, "ASJ.web.000395",
          "Cannot load exception type [{0}]. Error page mechanism will not be applied for this exception type.", 
          new Object[]{exceptionType}, cnfe, null, null);
        continue;
			} catch (Exception e) {
	  //TODO:Polly web app name?
				LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logWarning(currentLocation, "ASJ.web.000396",
		      "Cannot load exception type [{0}]. Error page mechanism will not be applied for this exception type.", 
		      new Object[]{exceptionType}, e, null, null);
				continue;
			}

      if (exceptionClass.isAssignableFrom(exceptionClassToAdd)) {
        errorException.add(i, errorPage);
        return;
      }
    }

    errorException.add(errorPage);
  }

  void initErrorPages(ErrorPageType[] errorPage) {
    if (errorPage != null) {
      for (int i = 0; i < errorPage.length; i++) {
        if (errorPage[i] != null && errorPage[i].getChoiceGroup1() != null) {
        	if(errorPage[i].getChoiceGroup1().isSetExceptionType()) {
						addErrorPageForException(errorPage[i]);
        	}
			if(errorPage[i].getChoiceGroup1().isSetErrorCode()) {
				errorCode.add(errorPage[i]);
			}
        }
      }
    }
  }

  public MessageBytes getServletMappedSlashStar() {
    return servletMappedSlashStar;
  }

  public void doMapCheck(MessageBytes requestURI, RequestPathMappings pathMappings, boolean withNoDefaultServlet) {
    MessageBytes temp;
    temp = checkExactPathJsp(requestURI);
    if (temp == null){
      temp = checkExactPath(requestURI);
    }
    if (temp != null) {
      pathMappings.setServletName(temp);
      //todo - da ne e comentirano!!
      //pathMappings.setServletPath(temp.toString());
      return;
    }

		if (pathMappings.getServletName() == null){
			checkPath(requestURI, pathMappings, withNoDefaultServlet);
		} /*else {
			//If a resource matches a URL pattern in both a <servlet-mapping> and
			//a <jsp-property-group>, the pattern that is most specific applies
			checkPath(requestURI, pathMappings, withNoDefaultServlet, jspPath);
		}*/
    if (!requestURI.endsWith("/")) {
      checkPathJsp(requestURI, pathMappings);
    }
    if (pathMappings.getServletName() == null) {
    	temp = checkExtensionJsp(requestURI);
    	if (temp == null){
    	  temp = checkExtension(requestURI);
    	}
      if (temp != null) {
        pathMappings.setServletName(temp);
      } else if (defaultServletName != null) {
        pathMappings.setServletName(defaultServletName);
        pathMappings.setServletPath(requestURI.toString());
      }
    }
  }


  public boolean doCheckWelcomeServlets(MessageBytes requestURI) {
    boolean result = false;
  	MessageBytes temp;
    temp = checkExactPath(requestURI);
    result = (temp != null);
    if (! result) {
	    //always checkPath(requestURI, welcomeFile, withNodDefaultServlet);
	    MessageBytes s = null;
	    MessageBytes ss = null;
	    MessageBytes maxSS = null;
	    for (Iterator<MessageBytes> enumeration = pathMap.keySet().iterator(); enumeration.hasNext();) {
	      s = enumeration.next();
	      ss = new MessageBytes(s.getBytes(0, (s.length() - 1)));
	      if (requestURI.startsWith(ss)) {
	        if ((maxSS == null) || (ss.length() > maxSS.length())) {
	          ss.setLength(ss.length() - 1);
	          maxSS = ss;
	        }
	        continue;
	      }
	      ss.setLength(ss.length() - 1);
	      if (requestURI.equals(ss)) {
					if ((maxSS == null) || (ss.length() > maxSS.length())) {
	          maxSS = ss;
					}
	        continue;
	      }
	    } //for
	    if (maxSS != null) {
	    	result = true;
	    }
	    //end checkPath
    }
    return result;
  }

  /**
   * Checks whether the specified URL path can denote the resource
   * as a JSP file, according to the JSP Configuration information.
   * @param _filename the URL path
   * @return servlet name specified for the JSP file.
   */
  private MessageBytes checkExactPathJsp(MessageBytes _filename) {
    if( exactMapJsp == null ) {
      return null;
    }
		Object obj = exactMapJsp.get(_filename);

		if (obj != null) {
			return ((MessageBytes) obj);
		}

		return null;

  }

  public String getExceptionErrorPage(Throwable ex) {
	  String errorType = "";
    for (int i = 0; i < errorException.size(); i++) {
      try {
        ErrorPageType er = errorException.get(i);
        if(er.getChoiceGroup1().isSetExceptionType()) {
        	errorType= er.getChoiceGroup1().getExceptionType().get_value();
        	Class c = appClassLoader.loadClass(errorType);
        	if (c.isAssignableFrom(ex.getClass())) {
          		return er.getLocation().get_value();
        	}
        }
      } catch (ClassNotFoundException e) {
    	  //TODO:POlly web app name?
        LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000397", 
          "Cannot load exception type [{0}]. Error page mechanism will not be applied for this exception type.", new Object[]{errorType}, e, null, null);
      } catch (NoClassDefFoundError e) {
        LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000398", 
          "Cannot load exception type [{0}]. Error page mechanism will not be applied for this exception type.", new Object[]{errorType}, e, null, null);
      } catch (Exception e) {
		  LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000399", 
		    "Cannot load exception type [{0}]. Error page mechanism will not be applied for this exception type.", new Object[]{errorType}, e, null, null);
	  }
    }
    return null;
  }

	/**
	 * Gets the filters of the passed 'filterDispatcher' type to be
	 * applied, matching the passed requestPath and servletName.
	 *
	 * @param requestPath
	 * @param servletName
	 * @param filterDispatcher	the supported types are stored in
	 * 			constants in lib.Constants.FILTER_DISPATCHER_...
	 * @return array of filter names; If 'filterDispatcher' is empty,
	 * 			returns null. If no filters found, returns null.
	 */
	public String[] getFilters(String requestPath, MessageBytes servletName, String filterDispatcher) {
		if (filterDispatcher == null) {
			return null;
		}
		if (filterMapping == null || filterMapping.length == 0) {
			return null;
		}

    List<String> filters = new ArrayList<String>();

    if (filterDispatcher.equals(Constants.FILTER_DISPATCHER_REQUEST) && servletName != null) {
      HashMap<String,String[]> filterChains = getServletNameURL(servletName);
      if (filterChains != null) {
        String [] foundFilterChains = filterChains.get(requestPath);
        if (foundFilterChains != null && foundFilterChains.length > 0) {
          return foundFilterChains;
        }
      }
    }

		try {
			//maps url first
			for (int i = 0; i < filterMapping.length; i++) {
        if (filterMapping[i].getChoiceGroup1() != null) {
          for (int j = 0; j < filterMapping[i].getChoiceGroup1().length; j++) {
            if (filterMapping[i].getChoiceGroup1()[j].isSetUrlPattern()) {
              if (checkFilterMap(filterMapping[i].getChoiceGroup1()[j].getUrlPattern().get_value(), requestPath)) {
                addCheckedDispatcher(filters, filterDispatcher, filterMapping[i]);
              }
            }
          }
        }
			}
			//maps servlet next
			if (servletName != null) {
				for (int i = 0; i < filterMapping.length; i++) {
          if (filterMapping[i].getChoiceGroup1() != null) {
            for (int j = 0; j < filterMapping[i].getChoiceGroup1().length; j++) {
              if (filterMapping[i].getChoiceGroup1()[j].isSetServletName()) {
                //servlet 2.5 adds also the special servlet name '*' in filter mapping
                if ("*".equals(filterMapping[i].getChoiceGroup1()[j].getServletName().get_value())) {
                  addCheckedDispatcher(filters, filterDispatcher, filterMapping[i]);
                } else if (servletName.equals(filterMapping[i].getChoiceGroup1()[j].getServletName().get_value())) {
                  addCheckedDispatcher(filters, filterDispatcher, filterMapping[i]);
                }
              }
            }
          }
				}
			}
		} catch (Exception ex) {
		  LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logWarning(currentLocation,  "ASJ.web.000400", 
		    "Incorrect data found in [{0}] filter-mapping tag in the web deployment descriptor web.xml.", new Object[]{filterMapping}, ex, null, null);
		}

		if (filters.size() > 0) {
			String[] res = new String[filters.size()];
			System.arraycopy(filters.toArray(), 0, res, 0, filters.size());
      if (filterDispatcher.equals(Constants.FILTER_DISPATCHER_REQUEST) && servletName != null) {
        HashMap<String,String[]> filterChains = getServletNameURL(servletName);
        if (filterChains == null) {
          filterChains = new HashMap<String,String[]>();
        }
        filterChains.put(requestPath, res);
        if( servletNameUrl == null ) {
          servletNameUrl = new ConcurrentHashMap<MessageBytes, HashMap<String,String[]>>();
        }
        servletNameUrl.put(servletName, filterChains);
      }
			return res;
		} else {
			return null;
		}
	} //getFilters

  public String getErrorCodeErrorPage(int code) {
    for (int i = 0; i < errorCode.size(); i++) {
      ErrorPageType er = errorCode.get(i);
      try{
      	if(er.getChoiceGroup1().isSetErrorCode() && er.getChoiceGroup1().getErrorCode().get_value()!=null){
      		int errorCode = er.getChoiceGroup1().getErrorCode().get_value().intValue();
      		if (errorCode == code) {
        		return er.getLocation().get_value();
      		}
      	}
      } catch(Exception ex) {
    	  //TODO:Polly app name?
		LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logWarning(currentLocation, "ASJ.web.000401", 
		  "Incorrect data found in error-code tag in the web deployment descriptor web.xml.", ex, null, null);
      }
    }
    return null;
  }

  private void checkPath(MessageBytes requestURI, RequestPathMappings pathMappings,
		boolean checkPathNoDefaultServlet) {
    MessageBytes s = null;
    MessageBytes ss = null;
    MessageBytes maxS = null;
    MessageBytes maxSS = null;

    for (Iterator<MessageBytes> enumeration = pathMap.keySet().iterator(); enumeration.hasNext();) {
      s = enumeration.next();
      if (checkPathNoDefaultServlet && defaultServlet && s.equals("/*")) {
        continue;
      }
      ss = new MessageBytes(s.getBytes(0, (s.length() - 1)));
      if (requestURI.startsWith(ss)) {
        if ((maxSS == null) || (ss.length() > maxSS.length())) {
          maxS = s;
          ss.setLength(ss.length() - 1);
          maxSS = ss;
        }
        continue;
      }
      ss.setLength(ss.length() - 1);
      if (requestURI.equals(ss)) {
		if ((maxSS == null) || (ss.length() > maxSS.length())) {
          maxS = s;
          maxSS = ss;
		}
        continue;
      }
    }
    if (maxSS != null) {
      pathMappings.setServletName((MessageBytes)pathMap.get(maxS));
      pathMappings.setServletPath(maxSS.toString());
    }
  }

  /**
   * Checks whether part of the specified URL path can denote the resource
   * as a JSP file, according to the JSP Configuration information. If this is
   * true, then set the servlet name for this JSP file
   * @param _filename the URL path
   * @return path that most restrictively denotes the resource as JSP file.
   */
	private MessageBytes checkPathJsp(MessageBytes requestURI, RequestPathMappings pathMappings) {
		MessageBytes s = null;    //key - path
		MessageBytes ss = null;   //the path without *
		MessageBytes maxS = null; //max matched key
		MessageBytes maxSS = null;//max matched path (max key without *)

    if( pathMapJsp == null ) {
      return null;
    }
		for (Iterator<MessageBytes> enumeration = pathMapJsp.keySet().iterator(); enumeration.hasNext();) {
			s = enumeration.next();
			//if (checkPathNoDefaultServlet && defaultServlet && s.equals("/*")) {
			//	continue;
			//}
			ss = new MessageBytes(s.getBytes(0, (s.length() - 1)));
			if (requestURI.startsWith(ss)) {
				if ((maxSS == null) || (ss.length() > maxSS.length())) {
					maxS = s;
					ss.setLength(ss.length() - 1);
					maxSS = ss;
				}
				continue;
			}
//			ss.setLength(ss.length() - 1);
//			if (requestURI.equals(ss)) {
//				if ((maxSS == null) || (ss.length() > maxSS.length())) {
//					maxS = s;
//					maxSS = ss;
//				}
//				continue;
//			}
		}
		if (maxSS != null) {
      if (pathMappings.getServletName() == null){
        pathMappings.setServletName((MessageBytes)pathMapJsp.get(maxS));
        //pathMappings.setServletPath(maxSS.toString());
      } else if (pathMappings.getServletPath() != null && pathMappings.getServletPath().length() < maxSS.toString().length()){
        pathMappings.setServletName((MessageBytes)pathMapJsp.get(maxS));
        pathMappings.setServletPath(null);
      }
    }
		return maxSS;
	}


  private MessageBytes checkExactPath(MessageBytes _filename) {
    Object obj = exactMap.get(_filename);

    if (obj != null) {
      return ((MessageBytes) obj);
    }

    return null;
  }

  private MessageBytes checkExtension(MessageBytes _filename) {
    Object obj = null;

    if (_filename.lastIndexOf('.') != -1) {
      _filename = new MessageBytes(_filename.getBytes(_filename.lastIndexOf('.')));
    } else {
      _filename = new MessageBytes(_filename.getBytes());
    }

    obj = extMap.get(_filename);

    if (obj != null) {
      return ((MessageBytes) obj);
    }

    return null;
  }

	/**
   * Checks whether the extension found in the specified URL path denotes the
   * resource as JSP file, according to the JSP Configuration information.
   * @param _filename the requested URL path
   * @return the name of the servlet if the resource can be determined as JSP
   * file.
   */
  private MessageBytes checkExtensionJsp(MessageBytes _filename) {
    if( extMapJsp == null ) {
      return null;
    }
		Object obj = null;

		if (_filename.lastIndexOf('.') != -1) {
			_filename = new MessageBytes(_filename.getBytes(_filename.lastIndexOf('.')));
		} else {
			_filename = new MessageBytes(_filename.getBytes());
		}

		obj = extMapJsp.get(_filename);

		if (obj != null) {
			return ((MessageBytes) obj);
		}

		return null;
	}

  private static boolean checkFilterMap(String testPath, String requestPath) {
    if (requestPath == null) {
      return false;
    }

    //ako savpadat
    if (testPath.equals(requestPath)) {
      return true;
    }

    // ako chast ot patia savpada ("/.../*")
    if (testPath.equals("/*")) {
      return true;
    }

    if (testPath.endsWith("/*")) {
      String comparePath = requestPath;

      while (true) {
        if (testPath.equals(comparePath + "/*")) {
          return true;
        }

        if (testPath.equals(comparePath + "*")) {
          return true;
        }

        int slash = comparePath.lastIndexOf("/");

        if (slash < 0) {
          break;
        }

        comparePath = comparePath.substring(0, slash);
      }

      return false;
    }

    // razshirenie
    if (testPath.startsWith("*.")) {
      int slash = requestPath.lastIndexOf("/");
      int period = requestPath.lastIndexOf(".");

      if ((slash >= 0) && (period > slash)) {
        return (testPath.equals("*." + requestPath.substring(period + 1)));
      }
    }

    if (testPath.startsWith(".")) {
      int slash = requestPath.lastIndexOf("/");
      int period = requestPath.lastIndexOf(".");

      if ((slash >= 0) && (period > slash)) {
        return (testPath.equals("." + requestPath.substring(period + 1)));
      }
    }

    return false;
  }

  /** Adds the filterMapping to the array of filters if it has dispatcher of type filterDispatcher */
  private void addCheckedDispatcher(List<String> filters, String filterDispatcher,
  								FilterMappingType filterMapping) {
  	com.sap.engine.lib.descriptors5.web.DispatcherType[] dispatchers = filterMapping.getDispatcher();//filterDispatchers
  	// REQUEST is when is explicitly specified or when nothing is specified:
  	if ((dispatchers == null || dispatchers.length == 0)
  		&&  Constants.FILTER_DISPATCHER_REQUEST.equals(filterDispatcher)) {
  		//filter for requests direct from client
  		filters.add(filterMapping.getFilterName().get_value());//filterName
  	} else if (dispatchers != null) {
  		//has specified dispatchers
  		for (int j = 0; j < dispatchers.length; j++) {
  			if (filterDispatcher.equals(dispatchers[j].get_value())) {
  				// filter for requests direct from client
  				filters.add(filterMapping.getFilterName().get_value());//filterName
  				break;
  			}
  		}
  	} // else skip this filter
  } //addCheckedDispatcher

  /**
   * Retrieves filters for this servlet.
   * @param servletName
   * @return
   */
  private HashMap<String,String[]> getServletNameURL(MessageBytes servletName){
    if( servletNameUrl == null ) {
      return null;
    }
    return servletNameUrl.get(servletName);
  }

}

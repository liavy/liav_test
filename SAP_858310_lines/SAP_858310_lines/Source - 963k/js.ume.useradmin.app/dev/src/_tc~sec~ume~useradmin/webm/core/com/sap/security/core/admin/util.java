package com.sap.security.core.admin;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.naming.InitialContext;
import javax.servlet.http.HttpServletRequest;

import com.sap.security.api.AttributeList;
import com.sap.security.api.IPrincipal;
import com.sap.security.api.IUser;
import com.sap.security.api.IUserFactory;
import com.sap.security.api.UMException;
import com.sap.security.api.UMFactory;
import com.sap.security.core.InternalUMFactory;
import com.sap.security.core.persistence.IPrincipalDatabag;
import com.sap.security.core.util.BeanException;
import com.sap.security.core.util.ErrorBean;
import com.sap.security.core.util.IUMTrace;
import com.sap.security.core.util.Message;
import com.sap.security.core.util.UMEPermission;
import com.sapmarkets.tpd.TradingPartnerDirectoryCommon;
import com.sapmarkets.tpd.master.PartnerID;
import com.sapmarkets.tpd.master.TradingPartnerDirectoryInterface;
import com.sapmarkets.tpd.master.TradingPartnerInterface;
import com.sapmarkets.tpd.util.TpdException;

public final class util {
    public final static String  VERSIONSTRING = "$Id: //engine/js.ume.useradmin.app/dev/src/_tc~sec~ume~useradmin/webm/core/com/sap/security/core/admin/util.java#1 $ from $DateTime: 2008/09/17 17:08:55 $ ($Change: 217715 $)";
    private static final IUMTrace trace = InternalUMFactory.getTrace(VERSIONSTRING);

    public static final String EMAIL_ADDRESS_PATTERN         = "ume.users.email_pattern";
    public static final String EMAIL_ADDRESS_PATTERN_DEFAULT = "*?@*?.?*";
    
    private static final String escapeChar = "^";
    public static final String empty = "";
    public static final String lineBreak ="\r\n";
    public final static String questionMark = "?";
    public final static String equalSign = "=";

    private static final String eIdentifier = "@"; // Email Identifier
    private static final String dSeperator = "."; // Domain Seperator
    private static final String squotation = "'"; // single quotation mark
    private static final char backslash = '\\';
    private static final char leftbracket = '(';
    private static final char rightbracket = ')';
    private static final char dbquotation = '\"';
    private static final char sgquotation = '\'';
    
	private static final char[] toBeEscaped = 
		{ 
			'$', '&', '+', ',', '/', ':', ';', '=', '?', '@',
			'\"', '\'', '<', '>', '#', '%', '{', '}', '|', 
			'\\', '~', '[', ']', '`', ' ', '^'
		};						
		
	private static Hashtable charValueTable = new Hashtable();
	private static Hashtable valueCharTable = new Hashtable();
	private final static String UmeAdminAppAlias = "/webdynpro/dispatcher/sap.com/tc~sec~ume~wd~umeadmin/UmeAdminApp";
	private final static String UmeEnduserAppAlias = "/webdynpro/dispatcher/sap.com/tc~sec~ume~wd~enduser/UmeEnduserApp";
	private final static String UmeSelfregAppAlias = "/webdynpro/dispatcher/sap.com/tc~sec~ume~wd~enduser/SelfregApp";
	private final static String useNewAlertPage = "useNewAlertPage";
	
	static {
		final int offset = 32;
		String ch, value;
		for (int i=0; i<toBeEscaped.length; i++) {
			ch = String.valueOf(toBeEscaped[i]);
			value = String.valueOf(offset+i);
			charValueTable.put(ch, value);
			valueCharTable.put(value, ch);
		}
	}
	
    public static String checkEmpty(String str) {
        return (str == null) ? null : (str.equals(empty) ? null : str.trim());
    } // checkEmpty

    public static String checkNull(String str) {
        return (str == null) ? empty : str.trim();
    } // checkNull

    public static Date checkEmpty(Date date) {
        return (date == null) ? null : (date.toString().equals(empty) ? null : date);
    } // checkEmpty

    public static String setEmpty(String str) {
        return (str == null) ? null : (str.equals(empty) ? empty : str.trim());
    }

    public static String checkNull(String str, String def) {
		return (str == null) ? def.trim() : str.trim();
    } // checkNull

    public static String setNull(String str) {
        return (str != null) ? ((str.trim()).length() == 0 ? null : str.trim()) : str.trim();
    } // setNull

    // check email validation
    /* there must be a '@' and a '.' in the address
     * there must be a '.' after the '@'
     * there must be at least one another character between the @ and the '.'
     * '@' can not be the first or the last character
     * '.' can not be the first or the last character
     * only one '@' is allowed.
     */
    public static void checkEmail(String email) throws BeanException {
	if (email != null)
	{
		String pattern = InternalUMFactory.getConfiguration().getStringDynamic(EMAIL_ADDRESS_PATTERN, EMAIL_ADDRESS_PATTERN_DEFAULT);
		//check email pattern case insensitive
		if (com.sap.security.core.util.imp.Util.patternMatch(email,pattern, true)) return;
	}
	throw new BeanException(UserBean.emailId, new Message(UserAdminMessagesBean.INPUT_MALFORMAT, UserBean.emailId));
    } // checkEmail

    // check phone/fax/mobile validation
    public static void checkPhone(String id, String value, String label) throws BeanException {
        String methodName = "checkPhone";
        if ( trace.bePath() ) {
        	trace.entering(methodName, new String[]{id, value});
    	} 

        // do legal character check
        char[] legal = {'(', ')', '/', '+', '-'};
        int length = value.length();
        char[] phoneCharArray = new char[length];
        phoneCharArray = value.toCharArray();

        // check Illegal Character
        boolean toDebug = trace.beDebug();
        for ( int i=0; i<length; i++) {
            char ch = phoneCharArray[i];
            if ( Character.isDigit(ch) ) {
                if ( toDebug ) {
                	trace.debugT(methodName, "a digit");
            	} 
                continue;
            } else if ( Character.isWhitespace(ch) ) {
            	if ( toDebug ) {
					trace.debugT(methodName, "a space");
            	}                
                continue;
            } else {
                boolean contains = false;
                for ( int j=0; j<legal.length; j++ ) {
                    Character pattern = new Character(legal[j]);
                    Character input = new Character(ch);
                    if ( toDebug ) {
						trace.debugT(methodName, "pattern input: ", new Character[]{pattern, input});
                    }                    
                    if ( 0 == pattern.compareTo(input)) {
                        contains = true;
                        if ( toDebug ) {
							trace.debugT(methodName, "contains legal punctuation marks");
                        }                        
                    } else {
                        continue;
                    }
                }
                if ( !contains ){
                	if ( toDebug ) {
						trace.debugT(methodName, "contains illegal char");
                	}                    
					StringBuffer legalStr = new StringBuffer();
                    for ( int j=0; j<legal.length; j++) {
                    	legalStr.append(legal[j]);
                    	legalStr.append(", ");
                    }
                    legalStr.append("0-9");
                    throw new BeanException(id, new Message(UserAdminMessagesBean.CONTAINS_ILLEGALCHAR, new String[]{label, legalStr.toString()}));
                } else {
                    if ( toDebug ) {
                    	trace.debugT(methodName, "no illegal char");
                    } 
                	continue;
                }
            }
        }
        // done

        int times = 0;
        int closetimes = 0;
        for ( int i=0; i<length; i++) {
            Character left = new Character(leftbracket);
            Character input = new Character(phoneCharArray[i]);
            Character right = new Character(rightbracket);
            if ( 0 == left.compareTo(input) )
                times++;
            if ( 0 == right.compareTo(input) )
                closetimes++;
        }
        if ( times != closetimes )
             throw new BeanException(id, new Message(UserAdminMessagesBean.INPUT_MALFORMAT, label));

        int pos = value.indexOf("+");
        if ( pos > 0 ) {
            if ( "(".equals(value.substring(0,0)) ) {
                if ( pos != 1 && !")".equals(value.substring(5,5)) )
                    throw new BeanException(id, new Message(UserAdminMessagesBean.INPUT_MALFORMAT, label));
            } else {
                if ( pos != 0 )
                    throw new BeanException(id, new Message(UserAdminMessagesBean.INPUT_MALFORMAT, label));
            }
        }
    } // checkPhone

    // check states in US

    public static ErrorBean checkField(Locale locale, String value, String desp) {
        boolean isValid = checkField(value);
        if ( !isValid ) {
            UserAdminLocaleBean localeBean = UserAdminLocaleBean.getInstance(locale);
            String orgUnitDesp = localeBean.get(desp);
            Message msg = new Message(UserAdminMessagesBean.MUST_BE_FILLED, orgUnitDesp);
            return new ErrorBean(msg);
        } else {
            return null;
        }
    } // checkField

    public static boolean checkField(String value) {
        if ( null == checkEmpty(value) ) {
            return false;
        } else {
            return true;
        }
    } // checkField

    public static Hashtable getParameters(IAccessToLogic proxy) {
        Hashtable _parameters = new Hashtable();
        Vector parmNames = getParameterNames(proxy);
        for (int i=0; i<parmNames.size(); i++) {
            String name = (String)parmNames.get(i);
            if ( null == proxy.getRequestParameter(name) ) continue;
            _parameters.put(name, proxy.getRequestParameter(name));
        }
        return _parameters;
    } // getParameters

    public static Vector getParameterNames(IAccessToLogic proxy) {
        Enumeration parameterNames = proxy.getRequestParameterNames();
        Vector names = new Vector();
        while ( parameterNames.hasMoreElements() ) {
            names.add(parameterNames.nextElement());
        }

        //if proxy is ComponentAccessToLogic add attribute names as well!
        if (!(proxy instanceof ServletAccessToLogic)) {
			Enumeration attributeNames = proxy.getRequestAttributeNames();
			while ( attributeNames.hasMoreElements() ) {
				String element = (String) attributeNames.nextElement();
				names.add(element);
			}
        }

        return names;
    } // getParameterNames

    public static String[] enumToStringArray(Enumeration listing) {
        Vector vec = new Vector();
        while ( listing.hasMoreElements() ) {
            vec.add(listing.nextElement());
        }
        String[] strArray = new String[vec.size()];
        strArray = (String[]) vec.toArray(strArray);
        return strArray;
    } // enumToStringArray


    public static String convertToUpperCase(String strToConvert) {
        char[] nameCharArray = strToConvert.toCharArray();
        for ( int k=0; k<strToConvert.length(); k++ ) {
            nameCharArray[k] = Character.toUpperCase(nameCharArray[k]);
        }
        return new String(nameCharArray);
    } // convertToUpperCase(String strToConvert)

    public static String convertDbQuotationToSingle(String strToConvert) {
        if ( null == checkEmpty(strToConvert) ) {
            return strToConvert;
        } else {
            StringBuffer result = new StringBuffer();
            for (int i=0; i<strToConvert.length(); i++ ) {
                char ch = strToConvert.charAt(i);
                if ( dbquotation == ch ) {
                    result.append(backslash);
                    result.append(squotation);
                } else if ( sgquotation == ch ) {
                    result.append(backslash);
                    result.append(ch);
                } else {
                    result.append(ch);
                }
            }
            return result.toString();
        }
    } // convertToUpperCase(String strToConvert)

    public static String filteringSpecialChar(String str) {
        if ( null == checkEmpty(str) ) {
            return str;
        } else {
            StringBuffer result = new StringBuffer();
            for (int i=0; i<str.length(); i++) {
                char ch = str.charAt( i );
                switch ( ch ) {
                    case '<':
                        result.append( "&lt;" );
                        break;
                    case '>':
                        result.append( "&gt;" );
                        break;
                    case '&':
                        result.append( "&amp;" );
                        break;
                    case '\t':
                        result.append( "&nbsp;&nbsp;" );
                        break;
                    case '\"':
                        result.append( "&quot;" );
                        break;
                    default:
                        result.append( ch );
                }
            }
            return result.toString();
        }
    } // filteringSpecialChar(String str)

    public static boolean isServlet23() {
        try {
			HttpServletRequest.class.getMethod("setCharacterEncoding", new Class[]{String.class});
            return true;
        } catch (NoSuchMethodException nsme) {
        	if ( trace.beInfo() ) {
        		trace.infoT("isServlet23", "setCharacterEncoding is not  available in JVM");
        	}
            return false;
        }
    } // isServlet23
	
	public static boolean isLocaleChanged(Locale oldLocale, Locale locale) {
		final String methodName = "isLocaleChanged";
		boolean isChanged = false;
		boolean toDebug = trace.beDebug();
		if ( null == locale ) {
			if ( null != oldLocale ) {
				isChanged = true;
			}
			if ( toDebug ) {
				trace.debugT(methodName, "no language is selected");
			}			
		} else {
			if ( toDebug ) {
				trace.debugT(methodName, "user chose language"+locale.toString());
			}			
			if ( !locale.equals(oldLocale) ) {
				if ( toDebug ) {
					trace.debugT(methodName, "change locale to"+locale.toString());
				}				
				isChanged = true;
			}
		}	
		return isChanged;	
	} // isLocaleChanged

    public static String getUniqueID(IAccessToLogic proxy) {
		String s = proxy.getRequestParameter(UserBean.uidId);
		if ( null == s ) return null;
		
		if ( s.indexOf(escapeChar) >=0 ) {
			return URLDecoder(s);
		} else {
			if ( s.indexOf("%") >=0 ) {
				return URLDecoder.decode(s);
			} else {
				return s;
			}
		}
    } // getUniqueID

	public static String getUniqueID(ListBean list) {
		if ( null == list ) return null;
		return (String)list.getSelectedObj();
	} // getUniqueID
	
    public static IUser getUser(IAccessToLogic proxy) {
        String uniqueID = getUniqueID(proxy);
        if ( null == uniqueID ) {
            return null;
        } else {
            return getUser(uniqueID);
        }
    } // getUser

    public static IUser getUser(String uniqueID) {
        final String methodName = "getUser(String uniqueID)";
        if ( null == uniqueID ) {
            if ( trace.beDebug() ) {
				trace.debugT("getUserIdFromRequest", "null pointer");
            }            
            return null;
        } else {
            IUserFactory uf = UMFactory.getUserFactory();
            try {
                return uf.getUser(uniqueID);
            } catch (UMException ex) {
                if ( trace.beWarning() ) {
					trace.warningT(methodName, "retrieve user from userid failed", new String[]{uniqueID});
                }                                
                return null;
            }
        }
    } // getUser(String uniqueID)

    public static String[] getUniqueIDs(IAccessToLogic proxy, String name) {
		String[] strs = proxy.getRequestParameterValues(name);
		if ( null == strs ) return null;
		
		String s;
		for ( int i=0; i<strs.length; i++ ) {
			s = strs[i];
			if ( s.indexOf(escapeChar) >=0 ) {
				strs[i] = URLDecoder(s);
			} else {
				if ( s.indexOf("%") >=0 ) {
					strs[i] = URLDecoder.decode(s);
				}
			}			
		}
		return strs;
    } // getUniqueIDs

    public static String[] getUniqueIDs(ListBean list) {
        Vector vec = list.getAllObjs();
        if ( vec.isEmpty() )
            return null;
        else
            return (String[])vec.toArray(new String[vec.size()]);
    } // getUniqueIDs(ListBean)

    public static IUser[] getUsers(Vector objs) {
        if (objs.get(0) instanceof IUser ) {
            return (IUser[]) objs.toArray(new IUser[objs.size()]);
        } else {
            return getUsers((String[]) objs.toArray(new String[objs.size()]));
        }
    } // getUsers(Vector)

    public static IUser[] getUsers(Object[] uniqueIDs) {
        if ( uniqueIDs[0] instanceof IUser ) {
            return (IUser[])uniqueIDs;
        } else {
            return getUsers((String[])uniqueIDs, null);
        }
    } // getUsers(Object[])

    public static IUser[] getUsers(String[] uniqueIds) {
        return getUsers(uniqueIds, null);
    } // getUsers(String[])

    public static IUser[] getUsers(String[] uniqueIds, String[] attributes) {
        final String methodName = "getUsers(String[] uniqueIds)";
        if ( null == uniqueIds ) {
            return null;
        } else {
            AttributeList populateAttris = new AttributeList();
            IUserFactory uf = UMFactory.getUserFactory();
            int size = uniqueIds.length;
            IUser[] users = new IUser[size];
            boolean failedtoGetUsers = false;
            if ( null != attributes ) {
                for (int i=0; i<attributes.length; i++) {
                    populateAttris.addAttribute(IPrincipal.DEFAULT_NAMESPACE, attributes[i]);
                }
            }

            try {
                if ( null != attributes ) users = uf.getUsers(uniqueIds);
                else users = uf.getUsers(uniqueIds, populateAttris);
            } catch (UMException ex) {
                trace.errorT(methodName, ex.getMessage(), ex);
                failedtoGetUsers = true;
            }

            if ( failedtoGetUsers ) {
                Vector v = new Vector();
                for ( int i=0; i<size; i++) {
                    try {
                        trace.debugT(methodName, "user id is: ", new String[]{uniqueIds[i]});
                        if ( null != attributes)
                            v.add(uf.getUser(uniqueIds[i], populateAttris));
                        else
                            v.add(uf.getUser(uniqueIds[i]));
                    } catch (UMException ex) {
                        trace.warningT(methodName, ex.getMessage(), ex);
                    }
                }
                users = (IUser[])v.toArray(new IUser[v.size()]);
            }
            return users;
        }
    } // getUsers(String[] uniqueIds, String[] attributes)

    public static IUser[] getUsers(IAccessToLogic proxy, String name) {
        return getUsers(getUniqueIDs(proxy, name));
    } // getUsers

    public static IUser[] getUsers(ListBean list) {
        return getUsers(getUniqueIDs(list));
    } // getUsers(ListBean)

    public static IUser[] getUsers(ListBean list, String[] attributes) {
        return getUsers(getUniqueIDs(list), attributes);
    } // getUsers(ListBean, String[])

	public static String[] getSelectedUniqueIDs(ListBean list) {
		Object[] objs = list.getSelectedObjsArray();
		if ( null == objs )
			return null;
		String [] ids = new String[objs.length];
		for ( int i=0; i<objs.length; i++ ) {
			ids[i] = (String)objs[i];
		}
		return ids;
	} // getSelectedUniqueIDs(ListBean)

    public static IUser[] getSelectedUsers(ListBean list) {
        String[] uniqueIDs = getSelectedUniqueIDs(list);
        if ( null == uniqueIDs ) {
            return null;
        } else {
            return getUsers(uniqueIDs);
        }
    } // getSelectedUsers(ListBean list)

    public static String[] removeDeletedIDs(String[] oriIDs, String[]deletedIDs) {
        String[] base = oriIDs;
        int k = 0;
        String[] result = null;
        for (int i=0; i<base.length; i++) {
            boolean deleted = false;
            for (int j=0; j<deletedIDs.length; j++) {
                if ( oriIDs[i].equals(deletedIDs[j]) ) {
                    deleted = true;
                    break;
                }
            }
            if ( !deleted ) result[k++] = oriIDs[i];
        }
        return result;
    } // removeDeletedIDs(uniqueIDs, deletedUsers)

    protected static int getOperator(String input) {
    	/*@todo: test for null */
    	if (CHECKNOTNULL(input))
    		return com.sap.security.api.ISearchAttribute.EQUALS_OPERATOR;

        if (input.indexOf("*") < 0 ) {
            return com.sap.security.api.ISearchAttribute.EQUALS_OPERATOR;
        } else {
            return com.sap.security.api.ISearchAttribute.LIKE_OPERATOR;
        }

    } // getOperator

    public static String alias(IAccessToLogic externalProxy, String servlet_name) {
        if (externalProxy instanceof ServletAccessToLogic) {
            return externalProxy.getContextURI()+servlet_name;
        } else {
            return externalProxy.getContextURI();
        }
    } //end of alias(IAccessToLogic)

    public static String alias(IAccessToLogic externalProxy, String servlet_name, String component) {
        if (externalProxy instanceof ServletAccessToLogic) {
            return externalProxy.getContextURI()+servlet_name;
        } else {
            return externalProxy.enforceContextURI(component);
        }
    } //end of alias(IAccessToLogic)

    public static TradingPartnerInterface getTP(String companyId) {
        if ( null == companyId ) return null;
        TradingPartnerDirectoryInterface tpd = TradingPartnerDirectoryCommon.getTPD();
        try {
            return tpd.getPartner(PartnerID.instantiatePartnerID(companyId));
        } catch (TpdException ex) {
        	if ( trace.beDebug() ) {
        		trace.debugT("getTP", companyId, ex);
        	}
            return null;
        }
    } // getTP

    public static void printOutRequest(IAccessToLogic proxy) {
        Enumeration parameterNames = proxy.getRequestParameterNames();
        Enumeration attNames = proxy.getRequestAttributeNames();
        Enumeration sessionAttNames = proxy.getSessionAttributeNames();

        trace.infoT("", "<<<<<<<<<<<<<<<<REQUEST PARAMETERS>>>>>>>>>>>>>>>>>>>");
        while (parameterNames.hasMoreElements()) {
            String name = (String) parameterNames.nextElement();
            String[] values = proxy.getRequestParameterValues(name);
            trace.infoT("", "name: " + name);

            for (int i = 0; i < values.length; i++) {
                trace.infoT("", "value: " + values[i]);
            }
        }

        trace.infoT("", "REQUEST ATTRIBUTES: ");
        while (parameterNames.hasMoreElements()) {
            String name = (String) attNames.nextElement();
            String value = proxy.getRequestAttribute(name).toString();
            trace.infoT("", "name: " + name);
            trace.infoT("", "value: " + value);
        }

        trace.infoT("", "SESSION ATTRIBUTES: ");
        while (parameterNames.hasMoreElements()) {
            String name = (String) sessionAttNames.nextElement();
            String value = proxy.getSessionAttribute(name).toString();
            trace.infoT("", "name: " + name);
            trace.infoT("", "value: " + value);
        }
    } // printOutRequest

    public static boolean CHECKNOTNULL(Object input)
    {
    	/*@todo: test for null */
    	if (null == input)  {
			/*
    		try {
    			input.toString();
    		} catch (Exception ex) {
    			System.out.println("WARNING: object is null");
    			ex.printStackTrace();
    		}
    		*/
    		return true;
    	}
    	return false;
    } // CHECKNOTNULL

	public static String URLEncoder(String s) {
		/* Characters that need to be encoded
		 * 1. ASCII Control characters
		 * 2. Non-ASCII characters
		 * 3. "Reserved characters"
		 * Character | Hex | Dec
		 * Dollar ("$") | 24 | 36
		 * Ampersand ("&") | 26 | 38
		 * Plus ("+") | 2B | 43
		 * Comma (",") | 2C | 44
		 * Forward slash/Virgule ("/") | 2F | 47
		 * Colon (":") | 3A | 58
		 * Semi-colon (";") | 3B | 59
		 * Equals ("=") | 3D | 61
		 * Question mark ("?") | 3F | 63
		 * 'At' symbol ("@") | 40 | 64
		 * 4. "Unsafe characters"
		 * Character | Hex | Dec
		 * Space | 20 | 32
		 * Quotation marks 22 | 34
		 * 'Less Than' symbol ("<") | 3C | 60
		 * 'Greater Than' symbol (">") | 3E | 62
		 * 'Pound' character ("#") | 23 | 35
		 * Percent character ("%") | 25 | 37
		 * Left Curly Brace ("{") | 7B | 123
		 * Right Curly Brace ("}") | 7D | 125
		 * Vertical Bar/Pipe ("|") | 7C | 124
		 * Backslash ("\") | 5C | 92
		 * Caret ("^") | 5E | 94
		 * Tilde ("~") | 7E | 126
		 * Left Square Bracket ("[") | 5B | 91
		 * Right Square Bracket ("]") | 5D | 93
		 * Grave Accent ("`") | 60 | 96
		 * How are characters URL encoded?
		 * URL encoding of a character consists of a "%" symbol, 
		 * followed by the two-digit hexadecimal representation 
		 * (case-insensitive) of the ISO-Latin code point for the character.
		 * or
		 * encodes every non-Latin1 character like %uxxxx where xxxx is the Unicode code point in hex characters.
		*/		
		if ( null == s ) 
			return null;
		
		int length = s.length();			
		StringBuffer result = new StringBuffer(length);
		String key;
		for (int i=0; i<length; i++) {
			char ch = s.charAt(i);
			key = String.valueOf(ch);
			if ( charValueTable.containsKey(key) ) {
				result.append(escapeChar).append(charValueTable.get(key));
			} else {
				result.append(ch);
			}			
		}
		return result.toString();	
	} // URLEncoder
	
	public static String URLDecoder(String s) {
		if ( null == s ) 
			return null;

		StringTokenizer st = new StringTokenizer(s, escapeChar);
		StringBuffer result = new StringBuffer(s.length());
		String str;
		String seg0;
		while ( st.hasMoreTokens() ) {
			str = st.nextToken();
			if ( str.length() < 2 ) {
				result.append(str);
			} else {
				seg0 = str.substring(0, 2);
				if ( s.startsWith(seg0) ) {
					result.append(str);		
				} else {
					if ( valueCharTable.containsKey(seg0) ) {
						result.append(valueCharTable.get(seg0));
						if ( str.length() > 2) 
							result.append(str.substring(2));
					} else {
						result.append(str);		
					}			
				}			
			}
		}
		return result.toString();
	} // URLDecoder
	
	public static boolean isRTL(Locale locale) {
		return (locale != null && (locale.getLanguage().equals("ar") || locale.getLanguage().equals("he") || locale.getLanguage().equals("iw")));
	}
	
	public static boolean checkNewUI(IAccessToLogic proxy) throws IOException, AccessToLogicException 
	{
		if (! useNewUI().equalsIgnoreCase("false"))
		{
			boolean hasWDAdminPermissions = false;
			checkWDAdminPermissions : {
				UMEPermission perm =
					new UMEPermission(
						IPrincipalDatabag.USER_TYPE,
						UMEPermission.ACTION_READ,
						UMEPermission.COM_SAP_SECURITY_CORE_USERMANAGEMENT_UNIQUEID,
						null,
						null);
				if (InternalUMFactory.loggedInUserHasPermission(perm))
				{
					hasWDAdminPermissions = true;
					break checkWDAdminPermissions;
				}
						
				perm =
					new UMEPermission(
						IPrincipalDatabag.ROLE_TYPE,
						UMEPermission.ACTION_READ,
						UMEPermission.COM_SAP_SECURITY_CORE_USERMANAGEMENT_UNIQUEID,
						null,
						null);
				if (InternalUMFactory.loggedInUserHasPermission(perm))
				{
					hasWDAdminPermissions = true;
					break checkWDAdminPermissions;
				}
			
				perm =
					new UMEPermission(
						IPrincipalDatabag.GROUP_TYPE,
						UMEPermission.ACTION_READ,
						UMEPermission.COM_SAP_SECURITY_CORE_USERMANAGEMENT_UNIQUEID,
						null,
						null);
				if (InternalUMFactory.loggedInUserHasPermission(perm))
				{
					hasWDAdminPermissions = true;
					break checkWDAdminPermissions;
				}
			}
			
			
			try 
			{
				if (useNewUI().equalsIgnoreCase("auto")) // if auto, check if webdynpro is deployed in the first place
				{
					if (hasWDAdminPermissions)
					{
						Object umeAdminAppObj = new InitialContext().lookup("webdynpro:java/sap.com/tc~sec~ume~wd~umeadmin/com.sap.security.core.wd.umeadmin.UmeAdminApp");
					}
					else
					{
						Object umeAdminAppObj = new InitialContext().lookup("webdynpro:java/sap.com/tc~sec~ume~wd~enduser/com.sap.security.core.wd.enduser.UmeEnduserApp");
					}
				}
				if (proxy instanceof ServletAccessToLogic)
				{
					if (hasWDAdminPermissions) proxy.sendRedirect(UmeAdminAppAlias);
					else proxy.sendRedirect(UmeEnduserAppAlias);
				}
				else
				{
					proxy.gotoPage(useNewAlertPage);
				}
				return true;
			}
			catch(Exception e)
			{
				trace.infoT("checkNewUI", "webdynpro not deployed, forwarding to the old UIs");
			}
		}
		return false;
	}
	
	public static boolean checkNewUISelfreg(IAccessToLogic proxy) throws IOException, AccessToLogicException 
	{
		if (! useNewUI().equalsIgnoreCase("false"))
		{
			try 
			{
				if (useNewUI().equalsIgnoreCase("auto"))
				{
					Object umeAdminAppObj = new InitialContext().lookup("webdynpro:java/sap.com/tc~sec~ume~wd~enduser/com.sap.security.core.wd.selfreg.SelfregApp");
				}
				
				if (proxy instanceof ServletAccessToLogic)
				{
					StringBuffer redirectURL = new StringBuffer(UmeSelfregAppAlias);
					String url = proxy.getRequestParameter("redirectURL");
					if (url != null) {
						redirectURL.append("?");
						redirectURL.append("redirectURL");
						redirectURL.append("=");
						redirectURL.append(URLEncoder.encode(URLDecoder.decode(url)));
					}
					proxy.sendRedirect(redirectURL.toString());
				}
				else
				{
					proxy.gotoPage(useNewAlertPage);
				}
				return true;
			}
			catch(Exception e)
			{
				trace.infoT("checkNewUI", "selfreg webdynpro not deployed, forwarding to the old UIs");
			}
		}
		return false;	
	}

	private static String useNewUI() {
		return InternalUMFactory.getConfiguration().getStringDynamic("ume.admin.wdactive", "auto");
	}
}

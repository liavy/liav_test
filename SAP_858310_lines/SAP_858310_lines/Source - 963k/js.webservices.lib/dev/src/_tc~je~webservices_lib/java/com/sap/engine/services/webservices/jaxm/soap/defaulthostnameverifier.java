package com.sap.engine.services.webservices.jaxm.soap;

import java.util.Locale;
import com.sap.tc.logging.Location;

/**
 * This class provides a default implementation of hostname verifier.
 * <br><br>
 * This verifier is called to compare the host name from the URL, 
 * where the client was connected, with the host name from server 
 * certificate. The way how hostname ist determined from server 
 * certificate is described {@link com.sap.security.core.server.https.SecureConnectionFactory here}.
 * <BR>
 * <BR>
 * There are two instances of DefaultHostnameVerifier available and 
 * they can be retrieved using the static methods:<br>
 * <ul><li><code> getNonStrictInstance () </code> - writes warning to <code>System.err</code> but returns true even if verification fails</li>
 * <li><code> getStrictInstance () </code> - returns false if verification fails</li>  
 * </ul>
 *  
 * The library uses <code>nonStrictInstance</code> if nothing was specified.
 * This can be changed for each factory by using  
 * <ul><li><code>factory.setHostnameVerifier( DefaultHostnameVerifier.getStictInstance() )</code></li>
 *	<li> or by implementing own verification rules.</li>
 *</ul>
 *       
 * <p>Copyright (c) 2003 SAP AG.
 */
public final class DefaultHostnameVerifier implements HostnameVerifier {
	
	private static final Location LOCATION =
			Location.getLocation(DefaultHostnameVerifier.class);
	
	static DefaultHostnameVerifier strictInstance = new DefaultHostnameVerifier( true );
	static DefaultHostnameVerifier nonstrictInstance = new DefaultHostnameVerifier( false );;		
	
	private boolean strict = false;

	private DefaultHostnameVerifier () {
		this( false );
	}
	private DefaultHostnameVerifier (boolean strict) {
		this.strict = strict;
	}
	
	/** 
	 * Compares the hostname with the name from server certifiate. 
	 * <br><br>
	 * This method compares parameters case insensitiv and accepts 
	 * first character <code>"*"</code> in <code>certHostname</code> as wildcard.
	 * <br>
	 */
	public boolean verify(String urlHostname, String certHostname) {
		final String me = "verify( String urlHostname, String certHostname )";			
		LOCATION.entering(me,new Object[] {urlHostname, certHostname});
		
		if ( urlHostname==null || certHostname==null ) {
			LOCATION.errorT ("inconsistent call to HostnameVerifier");		
			LOCATION.errorT ("HostnameVerifier returns: " + !strict);
			LOCATION.exiting() ;
			return !strict;	
		}
		
		urlHostname =  urlHostname.toLowerCase(Locale.ENGLISH) ;
		certHostname = certHostname.toLowerCase(Locale.ENGLISH);
		if (certHostname.startsWith("*")) {
			certHostname = certHostname.substring(1);
			if ((certHostname.length() == 0)
				|| urlHostname.endsWith(certHostname)) {
				LOCATION.infoT("hostname ok.");
				LOCATION.exiting() ;
				return true;
			}
		} else {
			if (urlHostname.equals(certHostname)) {
				LOCATION.infoT("hostname ok.");
				LOCATION.exiting() ;
				return true;
			}							
		}
		LOCATION.warningT ("name mismatch: " + urlHostname + " != " + certHostname);
		LOCATION.warningT ("HostnameVerifier returns: " + !strict);
		LOCATION.exiting() ;
		return !strict;	
	}
	
	public static DefaultHostnameVerifier getStrictInstance () {
		return strictInstance;
	}
	
	public static DefaultHostnameVerifier getNonStrictInstance () {
		 return nonstrictInstance;		
	}
}

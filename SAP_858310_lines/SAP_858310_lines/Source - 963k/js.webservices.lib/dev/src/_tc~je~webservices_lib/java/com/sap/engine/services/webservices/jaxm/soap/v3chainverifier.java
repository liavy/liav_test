package com.sap.engine.services.webservices.jaxm.soap;

import iaik.asn1.ObjectID;
import iaik.asn1.structures.GeneralName;
import iaik.asn1.structures.GeneralNames;
import iaik.asn1.structures.Name;
import iaik.security.ssl.ChainVerifier;
import iaik.security.ssl.SSLTransport;
import iaik.x509.V3Extension;
import iaik.x509.extensions.AuthorityKeyIdentifier;
import iaik.x509.extensions.BasicConstraints;
import iaik.x509.extensions.CRLDistributionPoints;
import iaik.x509.extensions.CRLNumber;
import iaik.x509.extensions.ExtendedKeyUsage;
import iaik.x509.extensions.IssuerAltName;
import iaik.x509.extensions.KeyUsage;
import iaik.x509.extensions.PolicyConstraints;
import iaik.x509.extensions.PolicyMappings;
import iaik.x509.extensions.ReasonCode;
import iaik.x509.extensions.SubjectAltName;
import iaik.x509.extensions.SubjectKeyIdentifier;
import iaik.x509.extensions.netscape.NetscapeBaseUrl;
import iaik.x509.extensions.netscape.NetscapeCaPolicyUrl;
import iaik.x509.extensions.netscape.NetscapeCaRevocationUrl;
import iaik.x509.extensions.netscape.NetscapeCertRenewalUrl;
import iaik.x509.extensions.netscape.NetscapeComment;
import iaik.x509.extensions.netscape.NetscapeRevocationUrl;
import iaik.x509.extensions.netscape.NetscapeSSLServerName;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Vector;

import com.sap.tc.logging.Location;
 

/**
 * This is an expiremental version of V3 Verifier for SSL.
 * 
 * It is needed because iaik.security.ssl.ChainVerifier 
 * doesn't support X.509v3 extensions at all.
 * 
 * It also provides a way to verify hostnames.
 * 
 *  <p>Copyright (c) 2003 SAP AG.
 * 
 */
public final class V3ChainVerifier extends ChainVerifier {
		
	private boolean trustV1 = true;
	private HostnameVerifier myVerifier = DefaultHostnameVerifier.getNonStrictInstance() ;

	private static final Location LOCATION =
	    Location.getLocation(V3ChainVerifier.class);

	/**
	 * Sets whether chain verifier should accept X509v1 certificates (default: true).
	 * 
	 * @param trust
	 */
	public void trustV1Certificates (boolean trust) {		
		trustV1 = trust;		
	}
	
	/**
	 * Sets the HostnameVerifier.
	 * 
	 * @param trust
	 */
	public void setHostnameVerifier(HostnameVerifier v) {		
		myVerifier = v;		
	}


	/**
	 * Additionaly to iaik.security.ssl.ChainVerifier this function checks v3 Extensions
	 * 
	 * 
	 * @see iaik.security.ssl.ChainVerifier#verifyChain(X509Certificate[], SSLTransport)
	 */
	public boolean verifyChain(java.security.cert.X509Certificate[] certs, SSLTransport transport) {
		final String me = "verifyChain ()";			
		LOCATION.entering(me,new Object[] {certs, transport});
		
		// ISASILK would establish a connection without trusted certs, but we don't like such behaivior. 
		if (size()==0) {
			LOCATION.errorT("Connections are not possible without trusted certificates.");	
			LOCATION.exiting() ;
			return false;
						
		}	
		if (LOCATION.beDebug() ) {
				LOCATION.debugT("Chain to verify:");
		        for ( int i=0; i<certs.length; i++) {
		            LOCATION.debugT("cert [" + i + "]");
		            LOCATION.debugT(" Subject: " + certs[i].getSubjectDN().toString());
		            LOCATION.debugT(" Issuer: " + certs[i].getIssuerDN().toString());
		            LOCATION.debugT(" Serial: " + certs[i].getSerialNumber().toString(0x10));
		            LOCATION.debugT(" Not before: " + certs[i].getNotBefore().toString());
		            LOCATION.debugT(" Not after: " + certs[i].getNotAfter().toString());
		        }
		}
		
		// this is an urgly hack to avoid some misconfiguration of apache server
		if (certs.length == 3 && certs[2].getSubjectDN().equals(certs[0].getIssuerDN())) {
			java.security.cert.X509Certificate _cert = certs[1];
			certs[1]= certs[2];
			certs[2] = _cert;
			LOCATION.warningT("chain resorted");			
		} 
		if (!super.verifyChain(certs, transport)) {
			LOCATION.errorT("Chain rejected by default verifier. IAIK log has more details.");
			LOCATION.exiting() ;
			return false;						
		}

		for (int i = 0; i < certs.length; i++) {
			if ( LOCATION.beDebug() )
				LOCATION.debugT("Certificate: " + certs[i]);
										
			if (certs[i].getVersion() == 0) {							
				LOCATION.debugT("Chain contains V1 certificate");
				if (!trustV1) {
					LOCATION.errorT("V1 certificate rejected");	
					LOCATION.exiting() ;
					return false;
				}
					
			}
			else {
				Enumeration extensions =
					((iaik.x509.X509Certificate) certs[i]).listExtensions();
				if (extensions != null) {
					while (extensions.hasMoreElements()) {
						V3Extension ext = (V3Extension) extensions.nextElement();
						ObjectID oid = ext.getObjectID();

						// EXPLICITLY HANDLED EXTENSIONS
						if (oid.equals(BasicConstraints.oid)) {
							BasicConstraints bc = (BasicConstraints) ext;
							LOCATION.debugT("BasicConstraints: " + bc);
							if (bc.ca()) {
								if (i == 0) {
									LOCATION.infoT( "Extension error: certificate at index 0 is marked CA certificate.");									
								}
								int pathLen = bc.getPathLenConstraint();
								if (pathLen != -1) {
									if (pathLen < i - 1) {
										LOCATION.errorT("Extension error: pathLenConstraint violated!");
										LOCATION.exiting() ;
										return false;
									}
								}
							} else {
								if (i != 0) {
									LOCATION.errorT("Extension error: certificate at index "
											+ i
											+ " is marked as non-CA certificate");
									LOCATION.exiting() ;
									return false;
										
								}
							}
						} else if (oid.equals(KeyUsage.oid)) {
							KeyUsage ku = (KeyUsage) ext;
							LOCATION.debugT ("KeyUsage: " + ku);
							if (i > 0) {
								int usage = ku.get();
								if ((usage & KeyUsage.keyCertSign) == 0) {
									LOCATION.errorT( "Extension error: keyusage does not allow certificate signing");
									LOCATION.exiting() ;
									return false;
								}
							}

							// NOT EXPLICITLY HANDLED STANDARD EXTENSIONS
						} else if (oid.equals(AuthorityKeyIdentifier.oid)) {							
							//	LOCATION.debugT(
							//		"AuthorityKeyIdentifier: " + ext);
							// ignore, no explicit handling
						} else if (oid.equals(CRLDistributionPoints.oid)) {
								LOCATION.warningT(
									"CRLDistributionPoint: " + ext +" ignored");
							// ignore, no explicit handling
						} else if (oid.equals(CRLNumber.oid)) {
							//	LOCATION.debugT ("CRLNumber: " + ext);
							// ignore, no explicit handling
							//        } else if( oid.equals(CertificatePolicies.oid) ) {
							//          LOCATION.debugT ("CertificatePolicies: " + ext);
							//          // ignore, no explicit handling
						} else if (oid.equals(ExtendedKeyUsage.oid)) {
							// LOCATION.debugT("ExtendedKeyUsage: " + ext);
							// ignore, no explicit handling
						} else if (oid.equals(IssuerAltName.oid)) {
							// LOCATION.debugT("IssuerAltName: " + ext);
							// ignore, no explicit handling
							//        } else if( oid.equals(NameConstraints.oid) ) {
							//          if( DEBUG_EXT ) System.out.println("NameConstraints: " + ext);
							//          // ignore, no explicit handling
							//        } else if( oid.equals(PolicyConstraints.oid) ) {
							//          if( DEBUG_EXT ) System.out.println("PolicyConstraints: " + ext);
							//          // ignore, no explicit handling
						} else if (oid.equals(PolicyMappings.oid)) {
							// LOCATION.debugT("PolicyMappings: " + ext);
							// ignore, no explicit handling
							//        } else if( oid.equals(PrivateKeyUsagePeriod.oid) ) {
							//          if( DEBUG_EXT ) System.out.println("PrivateKeyUsagePeriod: " + ext);
							//          // ignore, no explicit handling
						} else if (oid.equals(ReasonCode.oid)) {
							// LOCATION.debugT("ReasonCode: " + ext);
							// ignore, no explicit handling
						} else if (oid.equals(PolicyConstraints.oid)) {
							// LOCATION.debugT("PolicyConstraints: " + ext);
							// ignore, no explicit handling
						} else if (oid.equals(SubjectAltName.oid)) {
							// LOCATION.debugT("SubjectAltName: " + ext);
							// ignore, no explicit handling
						} else if (oid.equals(SubjectKeyIdentifier.oid)) {
							// LOCATION.debugT( "SubjectKeyIdentifier: " + ext);
							// ignore, no explicit handling

							// NOT EXPLICITLY HANDLED NETSCAPE EXTENSIONS
						} else if (oid.equals(NetscapeBaseUrl.oid)) {
							// LOCATION.debugT("NetscapeBaseUrl: " + ext);
							// ignore, no explicit handling
						} else if (oid.equals(NetscapeCaPolicyUrl.oid)) {
							// LOCATION.debugT("NetscapeCaPolicyUrl: " + ext);
							// ignore, no explicit handling
						} else if (oid.equals(NetscapeCaRevocationUrl.oid)) {
							LOCATION.warningT( "NetscapeCaRevocationUrl: " + ext +" ignored");
							// ignore, no explicit handling
						} else if (oid.equals(NetscapeCertRenewalUrl.oid)) {
							// LOCATION.debugT( "NetscapeCertRenewalUrl: " + ext);
							// ignore, no explicit handling
							//        } else if( oid.equals(NetscapeCertType.oid) ) {
							//          if( DEBUG_EXT ) System.out.println("NetscapeCertType: " + ext);
							//          // ignore, no explicit handling
						} else if (oid.equals(NetscapeComment.oid)) {
							// LOCATION.debugT("NetscapeComment: " + ext);
							// ignore, no explicit handling
						} else if (oid.equals(NetscapeRevocationUrl.oid)) {
							LOCATION.warningT("NetscapeRevocationUrl: " + ext +" ignored");
							// ignore, no explicit handling
						} else if (oid.equals(NetscapeSSLServerName.oid)) {
							// LOCATION.debugT("NetscapeSSLServerName: " + ext);
							// ignore, no explicit handling
						} else {
							// unsupported extension
							if (ext.isCritical()) {
								LOCATION.errorT( "Unhandled CRITICAL extension: "
											+ ext.getObjectID());
									return false;
											
							} else {
								LOCATION.infoT("Unhandled uncritical extension: "
											+ ext.getObjectID());
							}
						}
					}
				} else {
					LOCATION.debugT ("No extensions in certificate");
				}

			}

		}
		LOCATION.exiting() ;
		return true;
	}

  /** for documentation see IaikProvider.getTLSServerName() */
  static String[] getTLSServerName(iaik.x509.X509Certificate cert) {
    Vector v = new Vector();
    try {
      // first check SubjectAltName extension
      SubjectAltName altName = (SubjectAltName)cert.getExtension(SubjectAltName.oid);
      if( altName != null ) {
        GeneralNames names = altName.getGeneralNames();
        for( Enumeration e = names.getNames(); e.hasMoreElements(); ) {
          GeneralName name = (GeneralName)e.nextElement();
          if( name.getType() == GeneralName.dNSName ) {
            v.addElement(name.getName().toString());
          }
        }
      }
      // next check NetscapeSSLServerName extension
      NetscapeSSLServerName sslName = (NetscapeSSLServerName)cert.getExtension(NetscapeSSLServerName.oid);
      if( sslName != null ) {
        v.addElement(sslName.getSSLServerName());
      }
      // finally check subject DN
      Name name = (Name)cert.getSubjectDN();
      Object obj[] = name.getRDNValues(ObjectID.commonName);
      if( obj != null ) {
        for( int i=0; i<obj.length; i++ ) {
          v.addElement(obj[i].toString());
        }
      }
    } catch( Exception e ) {
      //$JL-EXC$
      // should not occur for a valid certificate
      // return what we have so far (below)
    }
    int n = v.size();
    String[] r = new String[n];
    v.copyInto(r);
    return r;
  }

  protected boolean verifyServer(X509Certificate[] certs, SSLTransport transport) {

	/*if( checkServerName == false ) {
      return true;
    }*/
    String transportName = transport.getRemotePeerName();
    if( transportName == null ) { // not supported by transport
      return true;
    }
    String[] names = getTLSServerName((iaik.x509.X509Certificate)certs[0]);
    if( names == null ) { // not supported by security provider
      return true;
    }
    if( names.length == 0 ) {
//      transport.debug("ChainVerifier: certificate does not contain a server name!");
      return false ||  !checkServerName;
    }
    transportName = transportName.toLowerCase(Locale.ENGLISH);
    
    for( int i=0; i<names.length; i++ ) {
  		if ( myVerifier.verify(transportName,names[i]) ) {
  			return true;
      }
    }
  //transport.debug("ChainVerifier: name mismatch: " + names[0] + " != " + transportName);
    return false ;//||  !checkServerName;
  }
}


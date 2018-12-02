package com.sap.security.core.jmx.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.security.AccessControlException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.OpenDataException;

import com.sap.security.api.AttributeList;
import com.sap.security.api.IGroup;
import com.sap.security.api.IMessage;
import com.sap.security.api.IPrincipal;
import com.sap.security.api.IPrincipalMaint;
import com.sap.security.api.IRole;
import com.sap.security.api.ISecurityPolicy;
import com.sap.security.api.IUser;
import com.sap.security.api.IUserAccount;
import com.sap.security.api.IUserMaint;
import com.sap.security.api.InvalidPasswordException;
import com.sap.security.api.NoSuchPrincipalException;
import com.sap.security.api.UMException;
import com.sap.security.api.UMFactory;
import com.sap.security.api.logon.ILoginConstants;
import com.sap.security.api.srvUser.IServiceUserFactory;
import com.sap.security.core.InternalUMFactory;
import com.sap.security.core.imp.AbstractUserAccount;
import com.sap.security.core.imp.User;
import com.sap.security.core.jmx.IJmxAttribute;
import com.sap.security.core.jmx.IJmxEntity;
import com.sap.security.core.jmx.IJmxMessage;
import com.sap.security.core.jmx.IJmxResult;
import com.sap.security.core.jmx.IJmxServer;
import com.sap.security.core.jmx.IJmxState;
import com.sap.security.core.logon.imp.SecurityPolicy;
import com.sap.security.core.persistence.datasource.imp.CompanyGroups;
import com.sap.security.core.persistence.imp.PrincipalDatabag;
import com.sap.security.core.role.IAction;
import com.sap.security.core.util.Base64;
import com.sap.security.core.util.imp.PasswordHash;
import com.sap.security.core.util.notification.SendMailAsynch;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

/**
 * Helper class for all modification functionality. Basically, this class maps
 * all {@link com.sap.security.core.jmx.IJmxAttribute} to the corresponding 
 * get/set methods of the modified {@link com.sap.security.api.IPrincipalMaint}.
 * For most of the attributes, a generic 
 * {@link com.sap.security.api.IPrincipalMaint#setAttribute(java.lang.String, java.lang.String, java.lang.String[])}
 * will be executed.
 * Furthermore, it contains functionality if more than one {@link com.sap.security.api.IPrincipal}
 * are changed (all table related actions like un/-locking of {@link com.sap.security.api.IUser} or
 * deleting of {@link com.sap.security.api.IPrincipal}).
 * Also password and e-mail handling is done here.
 */
class JmxModificationHelper {
	
	private static class AttributesChangedState{
		String lockState                = null;
		String approvalRequestCompanyId = null;
		
	}

    private final static String X509 = "X.509";

	private final static String LOGON_HELP_NAME_REQUIRED = "ume.logon.logon_help.name_required";
	 
	private final static String LOGON_HELP_SECURITY_QUESTION = "ume.logon.logon_help.securityquestion";

    private static Location myLoc = Location
            .getLocation(JmxModificationHelper.class);

    private static void modifyAttributes(String principalType,
            IPrincipalMaint principalMaint, IJmxAttribute[] attributes,
            String companyId) throws UMException {
        final String mn = "static private void modifyAttributes("
            + "String principalType, IPrincipalMaint principalMaint, IJmxAttribute[] attributes, String companyId)";
        if (myLoc.beInfo()) {
            myLoc.infoT(mn, "Principal {0}, companyId {1}", new Object[] {
                    principalMaint, companyId });
            for (int i = 0; i < attributes.length; i++) {
                myLoc
                        .infoT(
                                mn,
                                "New attribute {0}: namespace {1}, name {2}, value {3}",
                                new Object[] { new Integer(i + 1),
                                        attributes[i].getNamespace(),
                                        attributes[i].getName(),
                                        attributes[i].getValue() });
            }

        }
        for (int i = 0; i < attributes.length; i++) {
            String[] modificationValues = attributes[i].getValues();
            if (modificationValues != null && modificationValues.length > 0){
            	for (int j = 0; j < modificationValues.length; j++) {
					if (modificationValues[j] != null){
						modificationValues[j] = modificationValues[j].trim();
					} else {
						modificationValues[j] = CompanyPrincipalFactory.EMPTY;
					}
				}
            	principalMaint.setAttribute(attributes[i].getNamespace(),
            			attributes[i].getName(), modificationValues);
            } else {
            String[] values = principalMaint.getAttribute(attributes[i]
                    .getNamespace(), attributes[i].getName());
            if (values != null) {
                if (values.length > 0 && values[0] != null) {
                    String value = values[0];
                    if (!attributes[i].getValue().equals(value)) {
                        if (attributes[i].getValue().equals(CompanyPrincipalFactory.EMPTY)) {
                            principalMaint.setAttribute(attributes[i]
                                    .getNamespace(), attributes[i].getName(),
                                    null);
                        } else {
                            principalMaint.setAttribute(attributes[i]
                                    .getNamespace(), attributes[i].getName(),
                                    new String[] { attributes[i].getValue() });
                        }
                    }
                } else {
                    values = new String[1];
                    values[0] = attributes[i].getValue().trim();
                    principalMaint.setAttribute(attributes[i].getNamespace(),
                            attributes[i].getName(), values);
                }
            } else {
                if (!attributes[i].getValue().equals(CompanyPrincipalFactory.EMPTY)) {
                    values = new String[1];
                    values[0] = attributes[i].getValue().trim();
                    principalMaint.setAttribute(attributes[i].getNamespace(),
                            attributes[i].getName(), values);
                }
            }
        }
    }
    }

    private static void modifyAttribute(String principalType,
            IPrincipalMaint principalMaint, IJmxAttribute attribute,
            String companyId) throws UMException {
        IJmxAttribute[] attributes = new IJmxAttribute[] { attribute };
        modifyAttributes(principalType, principalMaint, attributes, companyId);
    }

    private static void modifyGroupAttributes(IGroup groupMaint,
            IJmxAttribute[] attributes, String companyId) throws UMException {
        final String mn = "static private void modifyGroupAttributes("
            + "IGroup groupMaint,IJmxAttribute[] attributes, String companyId)";
        if (myLoc.beInfo()) {
            myLoc.infoT(mn, "Principal {0}, companyId {1}", new Object[] {
                    groupMaint, companyId });
            for (int i = 0; i < attributes.length; i++) {
                myLoc
                        .infoT(
                                mn,
                                "New attribute {0}: namespace {1}, name {2}, value {3}",
                                new Object[] { new Integer(i + 1),
                                        attributes[i].getNamespace(),
                                        attributes[i].getName(),
                                        attributes[i].getValue() });
            }
        }
        String principalType = PrincipalDatabag.GROUP_TYPE;
        for (int i = 0; i < attributes.length; i++) {
            if (groupMaint.getUniqueID() != null) {
                if (!CompanyPrincipalFactory.getInstance(companyId)
                        .isPrincipalAttributeModifiable(
                                groupMaint.getUniqueID(),
                                attributes[i].getNamespace(),
                                attributes[i].getName())) {
                    continue;
                }
            } else {
                if (!CompanyPrincipalFactory.getInstance(companyId)
                        .isPrincipalAttributeCreateable(principalType,
                        		groupMaint,
                                attributes[i].getNamespace(),
                                attributes[i].getName())) {
                    continue;
                }
            }
            if (attributes[i].getNamespace().equals(
                    IPrincipal.DEFAULT_NAMESPACE)) {
                if (attributes[i].getName().equals(IPrincipal.DESCRIPTION)) {
                    if (groupMaint.getDescription() == null
                            || !groupMaint.getDescription().equals(
                                    attributes[i].getValue())) {
                        if (attributes[i].getValue().trim().equals(CompanyPrincipalFactory.EMPTY)) {
                            if (groupMaint.getDescription() != null) {
                                groupMaint.setDescription(null);
                            }
                        } else {
                            groupMaint.setDescription(attributes[i].getValue()
                                    .trim());
                        }
                    }
                    continue;
                }
            }
			if (attributes[i].getName().equals(CompanyGroups.COMPANY_ATTRIBUTE)) {
				// setting tenant or company attribute
				CompanyPrincipalFactory.getInstance(companyId).setCompany(
					groupMaint, attributes[i].getValue().trim());
				continue;
			}
            modifyAttribute(principalType, groupMaint, attributes[i], companyId);
        }
    }

    private static void modifyRoleAttributes(IRole roleMaint,
            IJmxAttribute[] attributes, String companyId) throws UMException {
        final String mn = "static private void modifyRoleAttributes("
            + "IRole roleMaint, IJmxAttribute[] attributes, String companyId)";
        if (myLoc.beInfo()) {
            myLoc.infoT(mn, "Principal {0}, companyId {1}", new Object[] {
                    roleMaint, companyId });
            for (int i = 0; i < attributes.length; i++) {
                myLoc
                        .infoT(
                                mn,
                                "New attribute {0}: namespace {1}, name {2}, value {3}",
                                new Object[] { new Integer(i + 1),
                                        attributes[i].getNamespace(),
                                        attributes[i].getName(),
                                        attributes[i].getValue() });
            }
        }
        String principalType = PrincipalDatabag.ROLE_TYPE;
        for (int i = 0; i < attributes.length; i++) {
            if (roleMaint.getUniqueID() != null) {
                if (!CompanyPrincipalFactory.getInstance(companyId)
                        .isPrincipalAttributeModifiable(
                                roleMaint.getUniqueID(),
                                attributes[i].getNamespace(),
                                attributes[i].getName())) {
                    continue;
                }
            } else {
                if (!CompanyPrincipalFactory.getInstance(companyId)
                        .isPrincipalAttributeCreateable(principalType,
                        		roleMaint,
                                attributes[i].getNamespace(),
                                attributes[i].getName())) {
                    continue;
                }
            }
            if (attributes[i].getNamespace().equals(
                    IPrincipal.DEFAULT_NAMESPACE)) {
                if (attributes[i].getName().equals(IPrincipal.DESCRIPTION)) {
                    if (roleMaint.getDescription() == null
                            || !roleMaint.getDescription().equals(
                                    attributes[i].getValue())) {
                        if (attributes[i].getValue().trim().equals(CompanyPrincipalFactory.EMPTY)) {
                            if (roleMaint.getDescription() != null) {
                                roleMaint.setDescription(null);
                            }
                        } else {
                            roleMaint.setDescription(attributes[i].getValue()
                                    .trim());
                        }
                    }
                    continue;
                }
            }
			if (attributes[i].getName().equals(CompanyGroups.COMPANY_ATTRIBUTE)) {
				// setting tenant or company attribute
				CompanyPrincipalFactory.getInstance(companyId).setCompany(
					roleMaint, attributes[i].getValue().trim());
				continue;
			}
            modifyAttribute(principalType, roleMaint, attributes[i], companyId);
        }
    }

	private static void modifyUserAttributes(
		IUserMaint userMaint,
		IJmxAttribute[] attributes,
		String companyId,
		AttributesChangedState attributesChanged)
			throws UMException {
        final String mn = "static private void modifyUserAttributes("
            + "IUserMaint userMaint, IJmxAttribute[] attributes, String companyId)";
        if (myLoc.beInfo()) {
            myLoc.infoT(mn, "Principal {0}, companyId {1}", new Object[] {
                    userMaint, companyId });
            for (int i = 0; i < attributes.length; i++) {
                myLoc
                        .infoT(
                                mn,
                                "New attribute {0}: namespace {1}, name {2}, value {3}",
                                new Object[] { new Integer(i + 1),
                                        attributes[i].getNamespace(),
                                        attributes[i].getName(),
                                        attributes[i].getValue() });
            }
        }
        String principalType = PrincipalDatabag.USER_TYPE;
        
        /*
         * Special LDAP handling:
         * Bind could fail on new user, only bind on special dummy user will be successfull
         * Therefore, create dummy user in create case to check if attributes are createable
         */
		IUserMaint dummyUser = null;
		if (userMaint.getUniqueID() == null){
			dummyUser = UMFactory.getUserFactory().newUser(userMaint.getUniqueName());
			dummyUser.setLastName(userMaint.getUniqueName());
			modifyAttributes(principalType, dummyUser, attributes, companyId);
		}
		

        for (int i = 0; i < attributes.length; i++) {
            if (userMaint.getUniqueID() != null) {
                if (!CompanyPrincipalFactory.getInstance(companyId)
                        .isPrincipalAttributeModifiable(
                                userMaint.getUniqueID(),
                                attributes[i].getNamespace(),
                                attributes[i].getName())) {
                    continue;
                }
            } else {
                if (!CompanyPrincipalFactory.getInstance(companyId)
                        .isPrincipalAttributeCreateable(principalType,
								dummyUser,
                                attributes[i].getNamespace(),
                                attributes[i].getName())) {
                    continue;
                }
            }
            if (attributes[i].getNamespace().equals(
                    IPrincipal.DEFAULT_NAMESPACE)) {
                if (attributes[i].getName().equals(User.FIRSTNAME)) {
                    if (userMaint.getFirstName() == null
                            || !userMaint.getFirstName().equals(
                                    attributes[i].getValue())) {
                        if (attributes[i].getValue().trim().equals(CompanyPrincipalFactory.EMPTY)) {
                            if (userMaint.getFirstName() != null) {
                                userMaint.setFirstName(null);
                            }
                        } else {
                            userMaint.setFirstName(attributes[i].getValue()
                                    .trim());
                        }
                    }
                    continue;
                }
                if (attributes[i].getName().equals(User.LASTNAME)) {
                    if (userMaint.getLastName() == null
                            || !userMaint.getLastName().equals(
                                    attributes[i].getValue())) {
                        if (attributes[i].getValue().trim().equals(CompanyPrincipalFactory.EMPTY)) {
                            if (userMaint.getLastName() != null) {
                                userMaint.setLastName(null);
                            }
                        } else {
                            userMaint.setLastName(attributes[i].getValue()
                                    .trim());
                        }
                    }
                    continue;
                }
                if (attributes[i].getName().equals(User.EMAIL)) {
                    if (userMaint.getEmail() == null
                            || !userMaint.getEmail().equals(
                                    attributes[i].getValue())) {
                        if (attributes[i].getValue().trim().equals(CompanyPrincipalFactory.EMPTY)) {
                            if (userMaint.getEmail() != null) {
                                userMaint.setEmail(null);
                            }
                        } else {
                            userMaint.setEmail(attributes[i].getValue().trim());
                        }
                    }
                    continue;
                }
                if (attributes[i].getName().equals(CompanyGroups.COMPANY_ATTRIBUTE)) {
                    // setting tenant or company attribute
                    CompanyPrincipalFactory.getInstance(companyId).setCompany(
                            userMaint, attributes[i].getValue().trim());
                    continue;
                }
				if (attributes[i].getName().equals(CompanyPrincipalFactory.APPROVAL_REQUEST_COMPANYID)) {
					//in selfreg case, set the special attribute on the changed state oject
					//in order to get it later again for sending the admin email
					attributesChanged.approvalRequestCompanyId = attributes[i].getValue();
					modifyAttribute(principalType, userMaint, attributes[i], companyId);
				}
                if (attributes[i].getName().equals(IPrincipal.UNIQUE_NAME)) {
                    // will be set by createUser()
                    continue;
                }
                if (attributes[i].getName().equals(User.LOCALE)) {
                    if (userMaint.getLocale() == null
                            || !userMaint.getLocale().equals(
                                    CompanyPrincipalFactory.createLocale(attributes[i].getValue()
                                            .trim()))) {
                        if (attributes[i].getValue().trim().equals(CompanyPrincipalFactory.EMPTY)) {
                            if (userMaint.getLocale() != null) {
                                userMaint.setLocale(null);
                            }
                        } else {
                            userMaint.setLocale(CompanyPrincipalFactory.createLocale(attributes[i]
                                    .getValue().trim()));
                        }
                    }
                    continue;
                }
            }
			if (attributes[i].getNamespace().equals(
					IServiceUserFactory.SERVICEUSER_NAMESPACE)) {
				if (attributes[i].getName().equals(IServiceUserFactory.SERVICEUSER_ATTRIBUTE)) {
					//will be set by create method
				}
			}
            modifyAttribute(principalType, userMaint, attributes[i], companyId);
        }
    }

    private static void modifyUserAccountAttributes(
		IUserAccount userAccountMaint,
		IUserMaint userMaint,
		IJmxAttribute[] attributes,
		String companyId,
		AttributesChangedState attributesChanged)
		throws UMException {
        final String mn = "static private void modifyUserAccountAttributes("
            + "IUserAccount userAccountMaint, IJmxAttribute[] attributes, String companyId)";
        if (myLoc.beInfo()) {
            myLoc.infoT(mn, "Principal {0}, companyId {1}", new Object[] {
                    userAccountMaint, companyId });
            for (int i = 0; i < attributes.length; i++) {
                myLoc
                        .infoT(
                                mn,
                                "New attribute {0}: namespace {1}, name {2}, value {3}",
                                new Object[] { new Integer(i + 1),
                                        attributes[i].getNamespace(),
                                        attributes[i].getName(),
                                        attributes[i].getValue() });
            }
        }
        String principalType = PrincipalDatabag.ACCOUNT_TYPE;
        for (int i = 0; i < attributes.length; i++) {
            if (userAccountMaint.getUniqueID() != null) {
                if (!CompanyPrincipalFactory.getInstance(companyId)
                        .isPrincipalAttributeModifiable(
                                userAccountMaint.getUniqueID(),
                                attributes[i].getNamespace(),
                                attributes[i].getName())) {
                    continue;
                }
            } else {
                if (!CompanyPrincipalFactory.getInstance(companyId)
                        .isPrincipalAttributeCreateable(principalType,
                        		userAccountMaint,
                                attributes[i].getNamespace(),
                                attributes[i].getName())) {
                    continue;
                }
            }
            if (attributes[i].getNamespace().equals(
                    IPrincipal.DEFAULT_NAMESPACE)) {
                if (attributes[i].getName().equals(
                        AbstractUserAccount.VALID_FROM)) {
                    Date d = null;
                    if (!attributes[i].getValue().trim().equals(CompanyPrincipalFactory.EMPTY)) {
                        try {
	                        d = new Date(
	                            new Long(attributes[i].getValue().trim())
	                            .longValue());
                        }
                        catch (NumberFormatException e) {
                            myLoc.traceThrowableT(Severity.ERROR, mn, "validfrom", e);
                        }
                    }
                    if (userAccountMaint.getValidFromDate() == null
                            || !userAccountMaint.getValidFromDate().equals(d)) {
                        if (attributes[i].getValue().trim().equals(CompanyPrincipalFactory.EMPTY)) {
                            if (userAccountMaint.getValidFromDate() != null) {
                                userAccountMaint.setValidFromDate(null);
                            }
                        } else {
                            if (d != null) 
                                userAccountMaint.setValidFromDate(d);
                        }
                    }
                    continue;
                }
                if (attributes[i].getName()
                        .equals(AbstractUserAccount.VALID_TO)) {
                    Date d = null;
                    if (!attributes[i].getValue().trim().equals(CompanyPrincipalFactory.EMPTY)) {
	                    try {
	                        d = new Date(new Long(
	                            attributes[i].getValue().trim())
	                            .longValue());
	                    }
	                    catch (NumberFormatException e) {
	                        myLoc.traceThrowableT(Severity.ERROR, mn, "validto", e);
	                    }
                    }
                    if (userAccountMaint.getValidToDate() == null
                            || !userAccountMaint.getValidToDate().equals(d)) {
                        if (attributes[i].getValue().trim().equals(CompanyPrincipalFactory.EMPTY)) {
                            if (userAccountMaint.getValidToDate() != null) {
                                userAccountMaint.setValidToDate(null);
                            }
                        } else {
                            if (d != null)
                                userAccountMaint.setValidToDate(d);
                        }
                    }
                    continue;
                }
                if (attributes[i].getName().equals(
                        ILoginConstants.LOGON_UID_ALIAS)) {
                    // will be set by createUserAccount()
                    continue;
                }
                if (attributes[i].getName().equals(
                        ILoginConstants.LOGON_PWD_ALIAS)) {
                    // will be set in mofifyEntity itself
                    continue;
                }
				if (attributes[i].getName().equals(CompanyGroups.COMPANY_ATTRIBUTE)) {
					// setting tenant or company attribute
					CompanyPrincipalFactory.getInstance(companyId).setCompany(
							userAccountMaint, attributes[i].getValue().trim());
					continue;
				}
				if (attributes[i]
					.getName()
					.equals(AbstractUserAccount.IS_ACCOUNT_LOCKED)
					|| attributes[i].getName().equals(
						AbstractUserAccount.IS_LOCKED)) {
					boolean locked;
					if (attributes[i].getValue().equalsIgnoreCase("true")) {
						locked = true;
					} else if (
						attributes[i].getValue().equalsIgnoreCase("false")
							|| attributes[i].getValue().equalsIgnoreCase("")) {
						locked = false;
					} else {
						continue;
					}
					if (userAccountMaint.isUserAccountLocked() != locked) {
						setLockState(userAccountMaint, userMaint, locked, null);
						if (locked) {
							attributesChanged.lockState =
								IJmxServer.MODIFY_ACTION_USER_LOCK;
						} else {
							attributesChanged.lockState =
								IJmxServer.MODIFY_ACTION_USER_UNLOCK;
						}
					}
					continue;
				}
				if (attributes[i].getName().equals(AbstractUserAccount.IS_PASSWORD_LOCKED)) {
					//Password lock has to be released by setting a new password
					continue;
				}
				if (attributes[i]
					.getName()
					.equals(IUserAccount.SECURITY_POLICY)) {
					String[] currentSecPol =
						userAccountMaint.getAttribute(
							IPrincipal.DEFAULT_NAMESPACE,
							IUserAccount.SECURITY_POLICY);
					if (currentSecPol == null) {
						//current sec pol is default; if to be changed sec pol is also default, do nothing
						if (IUserAccount
							.SECURITY_POLICY_TYPE_DEFAULT
							.equals(attributes[i].getValue())
							|| CompanyPrincipalFactory.EMPTY.equals(attributes[i].getValue())) {
							continue;
						}
					} else if (
						currentSecPol != null
							&& currentSecPol.length > 0
							&& IUserAccount.SECURITY_POLICY_TYPE_DEFAULT.equals(
								currentSecPol[0])) {
						//current sec pol is default; if to be changed sec pol is also default, do nothing
						if (IUserAccount
							.SECURITY_POLICY_TYPE_DEFAULT
							.equals(attributes[i].getValue())
							|| CompanyPrincipalFactory.EMPTY.equals(attributes[i].getValue())) {
							continue;
						}
					}
					modifyAttribute(
						principalType,
						userAccountMaint,
						attributes[i],
						companyId);
					continue;
				}
                if (attributes[i].getName().equals(
                        ILoginConstants.LOGON_CERT_ALIAS)) {
                    byte[] dec = Base64.decode(attributes[i].getValue());
                    ByteArrayInputStream bais = new ByteArrayInputStream(dec);
                    String[] stringCerts;
                    try {
                        ObjectInputStream ois = new ObjectInputStream(bais);
                        stringCerts = (String[]) ois.readObject();
                    } catch (IOException e) {
                        throw new UMException(e);
                    } catch (ClassNotFoundException e) {
                        throw new UMException(e);
                    }
                    try {
                        if (stringCerts.length < 1) {
                            userAccountMaint.setCertificates(null);
                        } else {
                            X509Certificate[] x509Certs = new X509Certificate[stringCerts.length];
                            CertificateFactory cf = CertificateFactory
                                    .getInstance(X509);
                            for (int j = 0; j < stringCerts.length; j++) {
                                if (stringCerts[j] != null) {
                                    byte[] bytes = Base64
                                            .decode(stringCerts[j]);
                                    bais = new ByteArrayInputStream(bytes);
                                    x509Certs[j] = (X509Certificate) cf
                                            .generateCertificate(bais);
                                }
                            }
                            userAccountMaint.setCertificates(x509Certs);
                        }
                    } catch (CertificateException ce) {
                        throw new UMException(ce);
                    }
                    continue;
                }
            }
			if (attributes[i].getName().equals(AbstractUserAccount.SECURITYANSWER)) {
				// do not do anything if sec policy question is not turned on
				if (checkPropertyRange(LOGON_HELP_SECURITY_QUESTION, "true")
					|| checkPropertyRange(LOGON_HELP_SECURITY_QUESTION, "predefined")) {
					String secAnswerClearText = attributes[i].getValue();
					// secAnswerClearText should never be null
					if (secAnswerClearText == null){
						secAnswerClearText = CompanyPrincipalFactory.EMPTY;
					} else {
						secAnswerClearText = secAnswerClearText.trim();
					}
					// do not do anything if UI delivers ********
					if (!secAnswerClearText.equals(CompanyPrincipalFactory.PASSWORD_STARS)){
						PasswordHash secAnswerHashUtil =
							new PasswordHash(
								userAccountMaint.getLogonUid(),
							secAnswerClearText.toUpperCase());
						String[] values = userAccountMaint
							.getAttribute(IPrincipal.DEFAULT_NAMESPACE, AbstractUserAccount.SECURITYANSWER);
						if (values == null
							|| values.length <= 0
							|| values[0] == null
							|| !secAnswerHashUtil.checkHash(values[0])) {
							// secAnswerClearText should never be empty, because it cannot be deleted
							if (secAnswerClearText.equals(CompanyPrincipalFactory.EMPTY)) {
								if (values != null
									&& values.length > 0
									&& values[0] != null) {
									userAccountMaint.setAttribute(
										IPrincipal.DEFAULT_NAMESPACE,
										AbstractUserAccount.SECURITYANSWER,
										null);
								}
							} else {
								userAccountMaint.setAttribute(
									IPrincipal.DEFAULT_NAMESPACE,
								AbstractUserAccount.SECURITYANSWER,
									new String[] { secAnswerHashUtil.getHash() });
							}
						}
						continue;					
					} else {
						continue;
					}
				} else {
					continue;
				}
			}
            modifyAttribute(principalType, userAccountMaint, attributes[i],
                    companyId);
        }
    }

    static int modifyEntities(String[] ids, String action, String message,
            String companyId) throws UMException {
        final String mn = "static int modifyEntities("
            + "String[] ids, String action, String message, String companyId)";
        if (myLoc.beInfo()) {
            myLoc.infoT(mn, "action {0}, message {1}, companyId {2}",
                    new Object[] { action, message, companyId });
            for (int i = 0; i < ids.length; i++) {
                myLoc.infoT(mn, "PrincipalId {0} is {1}", new Object[] {
                        new Integer(i + 1), ids[i] });
            }
        }
        if (action.equals(IJmxServer.MODIFY_ACTION_PRINCIPAL_DELETE)) {
            deleteEntities(ids, message, companyId);
            return IJmxServer.RESULT_OPERATION_OK;
        }
        if (action.equals(IJmxServer.MODIFY_ACTION_USER_EXPIREPASSWORD)) {
            generatePasswordsCommit(ids, message, companyId);
            return IJmxServer.RESULT_OPERATION_OK;
        }
        if (action.equals(IJmxServer.MODIFY_ACTION_USER_LOCK)) {
            setUsersLockStateCommit(ids, true, message, companyId);
            return IJmxServer.RESULT_OPERATION_OK;
        }
        if (action.equals(IJmxServer.MODIFY_ACTION_USER_UNLOCK)) {
            setUsersLockStateCommit(ids, false, message, companyId);
            return IJmxServer.RESULT_OPERATION_OK;
        }
        if (action.equals(IJmxServer.MODIFY_ACTION_USER_APPROVE)) {
            return approveUsersCommit(ids, message, companyId);
        }
        if (action.equals(IJmxServer.MODIFY_ACTION_USER_DENY)) {
            return denyUsersCommit(ids, message, companyId);
        }
        if (action.equals(IJmxServer.MODIFY_ACTION_PASSWORD_GENERATE)) {
            generatePasswordsCommit(ids, message, companyId);
            return IJmxServer.RESULT_OPERATION_OK;
        }
        if (action.equals(IJmxServer.MODIFY_ACTION_PASSWORD_DISABLE)) {
            disablePasswordsCommit(ids, companyId);
            return IJmxServer.RESULT_OPERATION_OK;
        }
        myLoc.errorT(mn, "Following Modify Action is not available: " + action);
        throw new UMException("Following Modify Action is not available: "
                + action);
    }

    static IJmxEntity modifyEntity(CompositeData dataMainDetail,
            CompositeData dataAdditionalDetail, String action, String message,
            String companyId, IJmxState jmxState) throws OpenDataException, UMException {
        final String mn = "static int modifyEntity("
            + "CompositeData dataMainDetail, CompositeData dataAdditionalDetail, String action, String message, String companyId)";
        JmxEntity result = new JmxEntity();
        
        JmxEntity requestEntity = new JmxEntity(dataMainDetail);
        if (myLoc.beInfo()) {
            myLoc
                    .infoT(mn, "requestEntity {0}",
                            new Object[] { requestEntity });
            myLoc.infoT(mn, "action {0}, message {1}, companyId {2}",
                    new Object[] { action, message, companyId });
        }
        IJmxAttribute[] requestDetailAttributes = requestEntity.getAttributes();
        if (myLoc.beDebug()) {
            for (int i = 0; i < requestDetailAttributes.length; i++) {
                myLoc
                        .debugT(
                                mn,
                                "requestDetailAttributes {0} from {1}: {2}",
                                new Object[] {
                                        new Integer(i + 1),
                                        new Integer(
                                                requestDetailAttributes.length + 1),
                                        requestDetailAttributes[i] });
            }
        }
        String uniqueId = requestEntity.getUniqueId();
        String principalType = CompanyPrincipalFactory.getInstance(
                companyId).getPrincipalType(uniqueId);
        if (principalType.equals(JmxActionFactoryWrapper.IACTION)) {
            JmxActionFactoryWrapper.modifyActionAttributesCommit(uniqueId,
                    requestDetailAttributes);
            return null;
		}
        if (CompanyPrincipalFactory.getInstance(companyId)
        		.isPrincipalModifiable(uniqueId)) {
        	
            /*
    		 * populate principal with all attributes (not only default ones)
    		 * to avoid lazy reads
    		 */
        	AttributeList aList = new AttributeList(false);
        	if (requestDetailAttributes != null){
    			for (int i = 0; i < requestDetailAttributes.length; i++) {
    				if (requestDetailAttributes[i] == null) {
    					continue;
    				}
    				aList.addAttribute(requestDetailAttributes[i].getNamespace(),
    						requestDetailAttributes[i].getName(),
    						AttributeList.TYPE_STRING);
    			}        		
        	}
            IPrincipal principal = CompanyPrincipalFactory
        		.getInstance(companyId).getPrincipal(uniqueId, aList);
            
            IPrincipalMaint principalMaint = CompanyPrincipalFactory
            	.getInstance(companyId).getMutablePrincipal(uniqueId);
            if (myLoc.beInfo()) {
                myLoc.infoT(mn, "principalMaint {0}",
                        new Object[] { principalMaint });
            }
            if (principalType.equals(CompanyPrincipalFactory.IUSER)) {
				AttributesChangedState attributesChanged = new AttributesChangedState();
            	String dummyUniqueName = UMFactory.getSecurityPolicy().generateLogonId();
            	IUserMaint userMaint = (IUserMaint) principalMaint;
				modifyUserAttributes(
					userMaint,
					requestDetailAttributes,
					companyId,
					attributesChanged);
                if (dataAdditionalDetail != null) {
                    requestEntity = new JmxEntity(dataAdditionalDetail);
                    if (myLoc.beInfo()) {
                        myLoc.infoT(mn, "additional requestEntity {0}",
                                new Object[] { requestEntity });
                    }
                    requestDetailAttributes = requestEntity.getAttributes();
                    String addUniqueId = requestEntity.getUniqueId();
                    if (CompanyPrincipalFactory.getInstance(companyId)
                            .isPrincipalModifiable(addUniqueId)) {
						principalType = CompanyPrincipalFactory.getInstance(
								companyId).getPrincipalType(addUniqueId);
						if (principalType
								.equals(CompanyPrincipalFactory.IUSERACCOUNT)) {
						IUserAccount userAccountMaint = (IUserAccount)CompanyPrincipalFactory
								.getInstance(companyId).getMutablePrincipal(
										addUniqueId);
							if (myLoc.beInfo()) {
								myLoc.infoT(mn, "userAccountMaint {0}",
										new Object[] { userAccountMaint });
							}
							modifyUserAccountAttributes(
								userAccountMaint,
								userMaint,
								requestDetailAttributes,
								companyId,
								attributesChanged);
                            String password = handlePasswordActions(action,
                                    (IUser) userMaint,
                                    (IUserAccount) userAccountMaint,
                                    requestDetailAttributes);
							if (userMaint.isModified()){
								if (userAccountMaint.isModified()){
									CompanyPrincipalFactory.getInstance(companyId).commitUser(userMaint, userAccountMaint);									
								} else {
									userMaint.save();
									userMaint.commit();
								}
							} else {
								if (userAccountMaint.isModified()){
									userAccountMaint.save();
									userAccountMaint.commit();			
								}
							}
							sendEmailForPasswordHandling(
								userMaint,
								action,
								password,
								message,
								attributesChanged);
							sendEmailForLockAttributeChange(
								userMaint,
								attributesChanged,
								message);
                        }
                    } else {
                        throw new UMException("Principal \"" + addUniqueId
                                + "\"is not modifiable");
                    }
                }
                result.setUniqueId(uniqueId);
                result.setMessages(JmxUtils
						.convertIPrincipalMessagesToIJmxMessageList(userMaint, jmxState.getLocale()));
            } else if (principalType.equals(CompanyPrincipalFactory.IGROUP)) {
                modifyGroupAttributes((IGroup) principalMaint,
                        requestDetailAttributes, companyId);
				if (principalMaint.isModified()){
					principalMaint.save();
					principalMaint.commit();	
				}
                result.setUniqueId(uniqueId);
                result.setMessages(JmxUtils
						.convertIPrincipalMessagesToIJmxMessageList(principalMaint, jmxState.getLocale()));
            } else if (principalType.equals(CompanyPrincipalFactory.IROLE)) {
                modifyRoleAttributes((IRole) principalMaint,
                        requestDetailAttributes, companyId);
				if (principalMaint.isModified()){
					principalMaint.save();
					principalMaint.commit();	
				}
                result.setUniqueId(uniqueId);
                result.setMessages(JmxUtils
						.convertIPrincipalMessagesToIJmxMessageList(principalMaint, jmxState.getLocale()));
            } else {
                modifyAttributes(principalType, principalMaint,
                        requestDetailAttributes, companyId);
				if (principalMaint.isModified()){
					principalMaint.save();
					principalMaint.commit();
				}
                result.setUniqueId(uniqueId);
                result.setMessages(JmxUtils
						.convertIPrincipalMessagesToIJmxMessageList(principalMaint, jmxState.getLocale()));
            }
            return result;
        } else {
            throw new UMException("Principal \"" + uniqueId
                    + "\"is not modifiable");
        }
    }

    /**
     * @param action
     * @param password
     */
	private static void sendEmailForPasswordHandling(
		IUser userTo,
		String action,
		String password,
		String message,
		AttributesChangedState attributesChanged)
		throws UMException {
        IUser userFrom = UMFactory.getAuthenticator().getLoggedInUser();
        if (IJmxServer.MODIFY_ACTION_PASSWORD_CHANGE_ADMIN.equals(action)) {
        	if (password != null && !password.trim().equals(CompanyPrincipalFactory.EMPTY)){
				SendMailAsynch.generateEmailOnUMEvent(userFrom, userTo,
					SendMailAsynch.USER_PASSWORD_RESET_PERFORMED, message,
					password);	
        	}
        } else if (IJmxServer.MODIFY_ACTION_PASSWORD_CHANGE_PROFILE
                .equals(action)) {
            return;
        } else if (IJmxServer.MODIFY_ACTION_PASSWORD_CREATE_ADMIN
                .equals(action)) {
            SendMailAsynch.generateEmailOnUMEvent(userFrom, userTo,
                    SendMailAsynch.USER_ACCOUNT_CREATE_PERFORMED, message,
                    password);
        } else if (IJmxServer.MODIFY_ACTION_PASSWORD_CREATE_SELFREG
                .equals(action)) {
        	/*
			 * if generate password is enabled, send new notification e-mail
			 * Not distinguishing it in SendEmailAsynch class
			 */
			if (InternalUMFactory.getConfiguration().getBooleanDynamic(
					"ume.admin.self.generate_password", false)) {
				SendMailAsynch
						.generateEmailOnUMEvent(
								userFrom,
								userTo,
								SendMailAsynch.USER_ACCOUNT_SELFREG_PERFORMED_PWD_GENERATED,
								message, password);
			} else {
				SendMailAsynch.generateEmailOnUMEvent(userFrom, userTo,
						SendMailAsynch.USER_ACCOUNT_SELFREG_PERFORMED, message,
						password);
			}
			if (attributesChanged != null && attributesChanged.approvalRequestCompanyId != null){
				SendMailAsynch.generateEmailToAdminOnUMEvent(
					userTo,
					SendMailAsynch.USER_ACCOUNT_CREATE_REQUEST,
					null);
			}
        } else if (IJmxServer.MODIFY_ACTION_PASSWORD_GENERATE.equals(action)) {
            SendMailAsynch.generateEmailOnUMEvent(userFrom, userTo,
                    SendMailAsynch.USER_PASSWORD_RESET_PERFORMED, message,
                    password);
		} else if (IJmxServer.MODIFY_ACTION_PASSWORD_CHANGE_GENERATE_ADMIN.equals(action)) {
			SendMailAsynch.generateEmailOnUMEvent(userFrom, userTo,
					SendMailAsynch.USER_PASSWORD_RESET_PERFORMED, message,
					password);
		} else if (IJmxServer.MODIFY_ACTION_PASSWORD_CREATE_GENERATE_ADMIN.equals(action)) {
			SendMailAsynch.generateEmailOnUMEvent(userFrom, userTo,
					SendMailAsynch.USER_ACCOUNT_CREATE_PERFORMED, message,
					password);
        } else if (IJmxServer.MODIFY_ACTION_PASSWORD_DISABLE.equals(action)) {
            SendMailAsynch.generateEmailOnUMEvent(userFrom, userTo,
                    SendMailAsynch.USER_PASSWORD_DISABLED, message);
		} else if (IJmxServer.MODIFY_ACTION_PASSWORD_CHANGE_DISABLE_ADMIN.equals(action)) {
			SendMailAsynch.generateEmailOnUMEvent(userFrom, userTo,
					SendMailAsynch.USER_PASSWORD_DISABLED, message);
		} else if (IJmxServer.MODIFY_ACTION_PASSWORD_CHANGE_DISABLE_PROFILE.equals(action)) {
			return;
		} else if (IJmxServer.MODIFY_ACTION_PASSWORD_CREATE_DISABLE_ADMIN.equals(action)) {
			SendMailAsynch.generateEmailOnUMEvent(userFrom, userTo,
					SendMailAsynch.CREATE_PERFORMED_PWD_DISABLED, message);
        }
    }

    static int deleteEntities(String[] ids, String message, String companyId)
            throws UMException {
        final String mn = "static int deleteEntities("
            + "String[] ids, String message, String companyId)";
        if (myLoc.beInfo()) {
            myLoc.infoT(mn, "message {0}, companyId {1}", new Object[] {
                    message, companyId });
        }
        for (int i = 0; i < ids.length; i++) {
            String uniqueId = ids[i];
            String type = CompanyPrincipalFactory.getInstance(companyId)
                    .getPrincipalType(uniqueId);
            boolean deleteable = CompanyPrincipalFactory.getInstance(companyId)
                    .isPrincipalDeletable(uniqueId);
            if (myLoc.beDebug()) {
                myLoc.debugT(mn, "principalId {0}, deleteable {1}",
                        new Object[] { uniqueId, new Boolean(deleteable) });
            }
            if (deleteable) {
                if (type.equals(CompanyPrincipalFactory.IUSER)) {
                    IUser user = (IUser) CompanyPrincipalFactory.getInstance(
                            companyId).getPrincipal(uniqueId);
                    String email = user.getEmail();
                    Locale locale = user.getLocale();
                    CompanyPrincipalFactory.getInstance(companyId).deleteUser(
                            uniqueId);
                    SendMailAsynch.generateEmailOnUserDeletion(UMFactory
                            .getAuthenticator().getLoggedInUser(), email,
                            locale, message);
                } else if (type.equals(CompanyPrincipalFactory.IROLE)) {
                    CompanyPrincipalFactory.getInstance(companyId).deleteRole(
                            uniqueId);
                } else if (type.equals(CompanyPrincipalFactory.IGROUP)) {
                    CompanyPrincipalFactory.getInstance(companyId).deleteGroup(
                            uniqueId);
                } else {
                    CompanyPrincipalFactory.getInstance(companyId)
                            .deletePrincipal(uniqueId);
                }
            } else {
                CompanyPrincipalFactory.getInstance(companyId)
                        .cleanupPrincipalDatabag(uniqueId);
            }
        }
        return IJmxServer.RESULT_OPERATION_OK;
    }

    /**
     * @param ids
     * @param message
     * @param companyId
     * @throws UMException
     */
    private static int denyUsersCommit(String[] ids, String message, String companyId)
            throws UMException {
        final String mn = "static private void denyUsers("
            + "String[] ids, String message, String companyId)";
        if (myLoc.beInfo()) {
            myLoc.infoT(mn, "message {0}, companyId {1}", new Object[] {
                    message, companyId });
        }
        int result = IJmxServer.RESULT_OPERATION_OK;
        if (CompanyPrincipalFactory.isCompanyConceptEnabled()) {
	        for (int i = 0; i < ids.length; i++) {
	            String uniqueId = ids[i];
	            if (myLoc.beDebug()) {
	                myLoc.debugT(mn, "principalId {0}", new Object[] { uniqueId });
	            }
	            String type = CompanyPrincipalFactory.getInstance(companyId)
	                    .getPrincipalType(uniqueId);
	            if (type.equals(CompanyPrincipalFactory.IUSER)) {
					IPrincipal user = CompanyPrincipalFactory.getInstance(
							companyId).getPrincipal(ids[i]);
					String[] values = user.getAttribute(IPrincipal.DEFAULT_NAMESPACE,
							CompanyPrincipalFactory.APPROVAL_REQUEST_COMPANYID);
	
					if (values != null && values.length > 0 && values[0] != null){
						if (myLoc.beInfo()){
							myLoc.infoT(mn, "Denying company {0} on user {1}, company context {2}", new Object[]{values[0], user.getUniqueID(), companyId});
						}
						IPrincipalMaint userMaint = CompanyPrincipalFactory.getInstance(
							companyId).getMutablePrincipal(ids[i]);
						CompanyPrincipalFactory.getInstance(companyId).setCompany(userMaint, CompanyPrincipalFactory.EMPTY);
						userMaint.setAttribute(IPrincipal.DEFAULT_NAMESPACE,
							CompanyPrincipalFactory.APPROVAL_REQUEST_COMPANYID,
							null);
						userMaint.save();
						userMaint.commit();
	                SendMailAsynch.generateEmailOnUMEvent(UMFactory
								.getAuthenticator().getLoggedInUser(), (IUser)userMaint,
	                        SendMailAsynch.USER_ACCOUNT_CREATE_DENIED, message);
					} else {
						if (myLoc.beInfo()){
							myLoc.infoT(mn, "User {0} has no approved company, company context {1}", new Object[]{user.getUniqueID(), companyId});
						}
						result = IJmxServer.RESULT_COMPANY_DENY_ERROR;
					}
	            } else {
	            	result = IJmxServer.RESULT_COMPANY_DENY_ERROR;
	            }
	        }
        } else {
        	result = IJmxServer.RESULT_COMPANY_DENY_ERROR;
        }
        return result;
    }

    /**
     * @param ids
     * @param message
     * @param companyId
     * @throws UMException
     */
    private static void generatePasswordsCommit(String[] ids, String message,
            String companyId) throws UMException {
        final String mn = "static private void generatePasswordsCommit(String[] ids, String message," +
            "String companyId)";
        if (myLoc.beDebug()) {
            for (int i = 0; i < ids.length; i++) {
                myLoc.debugT(mn, "ID: ", new Object[] { ids[i] });
            }
            myLoc.debugT(mn, "message: ", new Object[] { message });
        }
        for (int i = 0; i < ids.length; i++) {
            String type = CompanyPrincipalFactory.getInstance(companyId)
                    .getPrincipalType(ids[i]);
            if (type.equals(CompanyPrincipalFactory.IUSER)) {
                IUser user = (IUser) CompanyPrincipalFactory.getInstance(
                        companyId).getPrincipal(ids[i]);
                IUserAccount[] accounts = user.getUserAccounts();
                if (accounts != null && accounts.length > 0
                        && accounts[0] != null) {
                    accounts[0] = (IUserAccount) CompanyPrincipalFactory
                            .getInstance(companyId).getMutablePrincipal(
                                    accounts[0].getUniqueID());
                    String password = generatePassword(accounts[0]);
                    accounts[0].save();
                    accounts[0].commit();
                    SendMailAsynch.generateEmailOnUMEvent(UMFactory
                            .getAuthenticator().getLoggedInUser(), user,
                            SendMailAsynch.USER_PASSWORD_RESET_PERFORMED,
                            message, password);
                }
            }
        }
    }

    /**
     * @param principalType
     * @param uacc
     * 
     * @return password
     * @throws InvalidPasswordException
     */
    private static String generatePassword(IUserAccount uacc)
            throws InvalidPasswordException {
        ISecurityPolicy secPol = UMFactory.getSecurityPolicy();
        String password = secPol.generatePassword();
        uacc.setPassword(password);
        return password;
    }

	/**
	 * @param requestDetailAttributes
	 * @return
	 */
	private static String getAttributeValueFromAttributes(
			IJmxAttribute[] attributes, String namespace, String name) {
		String value = null;
		for (int i = 0; i < attributes.length; i++) {
			if (attributes[i].getNamespace().equals(
					namespace)) {
				if (attributes[i].getName().equals(
						name)) {
					value = attributes[i].getValue();
					break;
				}
			}
		}
		return value;
	}

    /**
     * @param requestDetailAttributes
     * @return
     */
    private static String getNewPasswordFromAttributes(
            IJmxAttribute[] attributes) {
		return getAttributeValueFromAttributes(
			attributes, IPrincipal.DEFAULT_NAMESPACE, ILoginConstants.LOGON_PWD_ALIAS);
    }

    /**
     * @param requestDetailAttributes
     * @return
     */
    private static String getOldPasswordFromAttributes(
            IJmxAttribute[] attributes) {
		return getAttributeValueFromAttributes(
			attributes, IPrincipal.DEFAULT_NAMESPACE, AbstractUserAccount.OLD_PASSWORD);
    }

    /**
     * @param action
     * @param uacc
     * @return
     * @throws UMException
     */
    private static String handlePasswordActions(String action, IUser user,
            IUserAccount uacc, IJmxAttribute[] requestDetailAttributes)
            throws UMException {
        final String mn = "private static String handlePasswordActions("
            + "String action, IUser user, IUserAccount uacc, IJmxAttribute[] requestDetailAttributes)";
        String password = null;
        if (myLoc.beInfo()) {
            myLoc.infoT(mn, "action {0}", new Object[] { action });
        }
        if (action.equals(IJmxServer.MODIFY_ACTION_PASSWORD_GENERATE) ||
			action.equals(IJmxServer.MODIFY_ACTION_PASSWORD_CHANGE_GENERATE_ADMIN) ||
			action.equals(IJmxServer.MODIFY_ACTION_PASSWORD_CREATE_GENERATE_ADMIN)) {
            password = generatePassword(uacc);
        } else if (action.equals(IJmxServer.MODIFY_ACTION_PASSWORD_DISABLE) ||
					action.equals(IJmxServer.MODIFY_ACTION_PASSWORD_CHANGE_DISABLE_ADMIN) || 
					action.equals(IJmxServer.MODIFY_ACTION_PASSWORD_CHANGE_DISABLE_PROFILE) ||
					action.equals(IJmxServer.MODIFY_ACTION_PASSWORD_CREATE_DISABLE_ADMIN)) {
            // password is null!
            uacc.setPasswordDisabled();
        } else if (action.equals(IJmxServer.MODIFY_ACTION_PASSWORD_CREATE_SELFREG)) {
			if (InternalUMFactory.getConfiguration().getBooleanDynamic("ume.admin.self.generate_password", false))
			{
				password = generatePassword(uacc);
			}
			else
			{
				password = getNewPasswordFromAttributes(requestDetailAttributes);
				if (password != null && !password.trim().equals(CompanyPrincipalFactory.EMPTY)) {
					uacc.setPassword(password);
					uacc.setPasswordChangeRequired(false);
				} else {
                myLoc
                        .errorT(
					 						mn,
					 						"No password given, password is {0}, cannot set password",
					 						new Object[] { password });
                throw new InvalidPasswordException(
                        SecurityPolicy.MISSING_PASSWORD);
				}
			}
        } else if (action
                .equals(IJmxServer.MODIFY_ACTION_PASSWORD_CREATE_ADMIN)) {
            password = getNewPasswordFromAttributes(requestDetailAttributes);
            if (password != null && !password.trim().equals(CompanyPrincipalFactory.EMPTY)) {
                uacc.setPassword(password);
                uacc.setPasswordChangeRequired(true);
            } else {
                myLoc
                        .errorT(
                                mn,
                                "No password given, password is {0}, cannot set password",
                                new Object[] { password });
                throw new InvalidPasswordException(
                        SecurityPolicy.MISSING_PASSWORD);
            }
        } else if (action
                .equals(IJmxServer.MODIFY_ACTION_PASSWORD_CHANGE_ADMIN)) {
            password = getNewPasswordFromAttributes(requestDetailAttributes);
            if (password != null && !password.trim().equals(CompanyPrincipalFactory.EMPTY)) {
                uacc.setPassword(password);
                uacc.setPasswordChangeRequired(true);
            } else {
                if (myLoc.beInfo()) {
                    myLoc
                            .infoT(
                                    mn,
                                    "No password given, password is {0}, do not change password",
                                    new Object[] { password });
                }
            }
        } else if (action
                .equals(IJmxServer.MODIFY_ACTION_PASSWORD_CHANGE_PROFILE)) {
            password = getNewPasswordFromAttributes(requestDetailAttributes);
            String oldPassword = getOldPasswordFromAttributes(requestDetailAttributes);
            if (password != null && !password.trim().equals(CompanyPrincipalFactory.EMPTY)
                    && oldPassword != null && !oldPassword.trim().equals(CompanyPrincipalFactory.EMPTY)) {
                uacc.setPassword(oldPassword, password);
            } else {
                if (myLoc.beInfo()) {
                    myLoc
                            .infoT(
                                    mn,
                                    "Password incomplete, password is {0}, old password is {1}, do not change password",
                                    new Object[] { password, oldPassword });
                }
            }
        }
        return password;
    }

    /**
     * @param ids
     * @param message
     * @param companyId
     * @throws UMException
     */
    private static void disablePasswordsCommit(String[] ids, String companyId)
            throws UMException {
        for (int i = 0; i < ids.length; i++) {
            String type = CompanyPrincipalFactory.getInstance(companyId)
                    .getPrincipalType(ids[i]);
            if (type.equals(CompanyPrincipalFactory.IUSER)) {
                IUser user = (IUser) CompanyPrincipalFactory.getInstance(
                        companyId).getPrincipal(ids[i]);
                IUserAccount[] accounts = user.getUserAccounts();
                if (accounts != null && accounts.length > 0
                        && accounts[0] != null) {
                    accounts[0] = (IUserAccount) CompanyPrincipalFactory
                            .getInstance(companyId).getMutablePrincipal(
                                    accounts[0].getUniqueID());
                    accounts[0].setPasswordDisabled();
                    accounts[0].save();
                    accounts[0].commit();
                }
            }
        }
    }

    /**
     * @param ids
     * @param message
     */
    private static void setUsersLockStateCommit(
		String[] ids,
		boolean locked,
		String message,
		String companyId)
		throws UMException {
        for (int i = 0; i < ids.length; i++) {
            String type = CompanyPrincipalFactory.getInstance(companyId)
                    .getPrincipalType(ids[i]);
            if (type.equals(CompanyPrincipalFactory.IUSER)) {
                IUser user = (IUser) CompanyPrincipalFactory.getInstance(
                        companyId).getPrincipal(ids[i]);
                IUserMaint userMaint = (IUserMaint) CompanyPrincipalFactory
                        .getInstance(companyId).getMutablePrincipal(
                                user.getUniqueID());
                IUserAccount[] accounts = user.getUserAccounts();
                if (accounts != null && accounts.length > 0
                        && accounts[0] != null) {
                    accounts[0] = CompanyPrincipalFactory
                            .getInstance(companyId).getMutableUserAccount(
                                    accounts[0].getUniqueID());
					setLockState(accounts[0], userMaint, locked, message);
                    CompanyPrincipalFactory.getInstance(companyId).commitUser(
                            userMaint, accounts[0]);
					AttributesChangedState attributesChanged = new AttributesChangedState();
					if (locked){
						attributesChanged.lockState = IJmxServer.MODIFY_ACTION_USER_LOCK;
					} else {
						attributesChanged.lockState = IJmxServer.MODIFY_ACTION_USER_UNLOCK;
					}
					sendEmailForLockAttributeChange(
						userMaint,
						attributesChanged,
						message);
					;
                }
            }
        }
    }

    private static void setLockState(
		IUserAccount userAccountMaint,
		IUserMaint userMaint,
		boolean locked,
		String message)
		throws UMException {
		//(un)locking useraccount
		userAccountMaint.setLocked(locked, IUserAccount.LOCKED_BY_ADMIN);
		//setting message, lockperson and date on user

		if (locked) {
			userAccountMaint.setAttribute(
					IPrincipal.DEFAULT_NAMESPACE,
					AbstractUserAccount.LOCK_TEXT,
					new String[] { message });
		} else {
			userAccountMaint.setAttribute(
					IPrincipal.DEFAULT_NAMESPACE,
					AbstractUserAccount.UNLOCK_TEXT,
					new String[] { message });
		}
		//delete old attributes if available
		if (userMaint
			.getAttribute(
				IPrincipal.DEFAULT_NAMESPACE,
				User.LOCKMESSAGE_OLD)
			!= null) {
				userMaint.setAttribute(
						IPrincipal.DEFAULT_NAMESPACE,
				User.LOCKMESSAGE_OLD,
				null);
		}
		if (userMaint
			.getAttribute(
				IPrincipal.DEFAULT_NAMESPACE,
				User.LOCKPERSON_OLD)
			!= null) {
			userMaint.setAttribute(
				IPrincipal.DEFAULT_NAMESPACE,
				User.LOCKPERSON_OLD,
				null);
			}
		if (userMaint
			.getAttribute(
				IPrincipal.DEFAULT_NAMESPACE,
				User.UNLOCKMESSAGE_OLD)
			!= null) {
			userMaint.setAttribute(
				IPrincipal.DEFAULT_NAMESPACE,
				User.UNLOCKMESSAGE_OLD,
				null);
		}
		if (userMaint
			.getAttribute(
				IPrincipal.DEFAULT_NAMESPACE,
				User.UNLOCKPERSON_OLD)
			!= null) {
			userMaint.setAttribute(
				IPrincipal.DEFAULT_NAMESPACE,
				User.UNLOCKPERSON_OLD,
				null);
		}
		if (userMaint
			.getAttribute(
				IPrincipal.DEFAULT_NAMESPACE,
				User.UNLOCKDATE_OLD)
			!= null) {
				userMaint.setAttribute(
						IPrincipal.DEFAULT_NAMESPACE,
				User.UNLOCKDATE_OLD,
				null);
			}
		if (userMaint
			.getAttribute(
				IPrincipal.DEFAULT_NAMESPACE,
				User.LOCKREASON_OLD)
			!= null) {
			userMaint.setAttribute(
				IPrincipal.DEFAULT_NAMESPACE,
				User.LOCKREASON_OLD,
				null);
		}
	}

    private static void sendEmailForLockAttributeChange(
		IUserMaint userMaint,
		AttributesChangedState attributesChanged,
		String message)
		throws UMException {
		if (attributesChanged != null && attributesChanged.lockState != null){
		}
		IUser currentUser = UMFactory.getAuthenticator()
				.getLoggedInUser();
		if (IJmxServer.MODIFY_ACTION_USER_LOCK.equals(attributesChanged.lockState)) {
			SendMailAsynch.generateEmailOnUMEvent(currentUser, userMaint,
					SendMailAsynch.USER_ACCOUNT_LOCK_PERFORMED,
					message);
		} else if (IJmxServer.MODIFY_ACTION_USER_UNLOCK.equals(attributesChanged.lockState)){
			SendMailAsynch.generateEmailOnUMEvent(currentUser, userMaint,
					SendMailAsynch.USER_ACCOUNT_UNLOCK_PERFORMED,
					message);
		}
	}

    /**
     * @param ids
     * @param message
     * @param companyId
     */
    private static int approveUsersCommit(String[] ids, String message,
            String companyId) throws UMException {
		final String mn = "static private void approveUsers(String[] ids, String message, String companyId)";
		int result = IJmxServer.RESULT_OPERATION_OK;
        if (CompanyPrincipalFactory.isCompanyConceptEnabled()) {
            for (int i = 0; i < ids.length; i++) {
                String type = CompanyPrincipalFactory.getInstance(companyId)
                        .getPrincipalType(ids[i]);
                if (type.equals(CompanyPrincipalFactory.IUSER)) {
					IPrincipal user = CompanyPrincipalFactory.getInstance(
							companyId).getPrincipal(ids[i]);
                    String[] values = user.getAttribute(IPrincipal.DEFAULT_NAMESPACE,
                            CompanyPrincipalFactory.APPROVAL_REQUEST_COMPANYID);
                    
					if (values != null && values.length > 0 && values[0] != null){
						if (myLoc.beInfo()){
							myLoc.infoT(mn, "setting company {0} on user {1}, company context {2}", new Object[]{values[0], user.getUniqueID(), companyId});
						}
						IPrincipalMaint userMaint = CompanyPrincipalFactory.getInstance(
								companyId).getMutablePrincipal(ids[i]);
						CompanyPrincipalFactory.getInstance(companyId).setCompany(userMaint, values[0]);
						userMaint.setAttribute(IPrincipal.DEFAULT_NAMESPACE,
								CompanyPrincipalFactory.APPROVAL_REQUEST_COMPANYID,
								null);
						userMaint.save();
						userMaint.commit();
						SendMailAsynch.generateEmailOnUMEvent(UMFactory
								.getAuthenticator().getLoggedInUser(),
								(IUser) userMaint,
								SendMailAsynch.USER_ACCOUNT_CREATE_APPROVAL,
								message);
					} else {
						if (myLoc.beInfo()){
							myLoc.infoT(mn, "User {0} has no approved company, company context {1}", new Object[]{user.getUniqueID(), companyId});
						}
						result = IJmxServer.RESULT_COMPANY_APPROVE_ERROR;
					}
                } else {
                	result = IJmxServer.RESULT_COMPANY_APPROVE_ERROR;
                }
            }
        } else {
        	result = IJmxServer.RESULT_COMPANY_APPROVE_ERROR;
        }
        return result;
    }

    static IJmxEntity createEntity(CompositeData dataMainDetail,
            CompositeData dataAdditionalDetail, String action, String companyId, IJmxState state)
            throws OpenDataException, UMException {
        final String mn = "static IJmxEntity createEntity("
            + "CompositeData dataMainDetail, CompositeData dataAdditionalDetail, String action, String companyId)";
        JmxEntity requestDetail = new JmxEntity(dataMainDetail);
        IJmxAttribute[] requestDetailAttributes = requestDetail.getAttributes();
        if (myLoc.beInfo()) {
            myLoc.infoT(mn, "requestDetail {0}, action {1}, companyId {2}",
                    new Object[] { requestDetail, action, companyId });
        }
        if (myLoc.beDebug()) {
            for (int i = 0; i < requestDetailAttributes.length; i++) {
                myLoc
                        .debugT(
                                mn,
                                "requestDetailAttributes {0} from {1}: {2}",
                                new Object[] {
                                        new Integer(i + 1),
                                        new Integer(
                                                requestDetailAttributes.length + 1),
                                        requestDetailAttributes[i] });
            }
        }
        String principalType = requestDetail.getType();
        String uniqueName = getAttributeValueFromAttributes(
        	requestDetailAttributes, IPrincipal.DEFAULT_NAMESPACE, IPrincipal.UNIQUE_NAME);
        if (uniqueName == null || uniqueName.trim().equals(CompanyPrincipalFactory.EMPTY)) {
            throw new UMException(
                    "Cannot create principal, unique name is missing");
        }
        if (principalType.equals(CompanyPrincipalFactory.IUSER)) {
			IUserMaint userMaint = null;
			boolean createServiceUser = false;
        	if (IServiceUserFactory.SERVICEUSER_VALUE.equals(
        			getAttributeValueFromAttributes(
        				requestDetailAttributes, IServiceUserFactory.SERVICEUSER_NAMESPACE, IServiceUserFactory.SERVICEUSER_ATTRIBUTE))){
        		createServiceUser = true;
        		userMaint = CompanyPrincipalFactory.getInstance(companyId).
        				newServiceUser(uniqueName);
        	} else {
				userMaint = CompanyPrincipalFactory.getInstance(companyId)
						.newUser(uniqueName);        		
        	}
			AttributesChangedState attributesChanged = new AttributesChangedState();
			modifyUserAttributes(
				userMaint,
				requestDetailAttributes,
				companyId,
				attributesChanged);
			IUserAccount userAccountMaint = null;
            if (createServiceUser){
            	IUserAccount[] accounts = userMaint.getUserAccounts();
            	if (accounts != null && accounts.length > 0 && accounts[0] != null){
            		userAccountMaint = CompanyPrincipalFactory.
            				getInstance(companyId).
            				getMutableUserAccount(accounts[0].getUniqueID());
            	} else {
            		myLoc.errorT(mn, "Service user {0} has no User Account", new Object[]{userMaint.getUniqueID()});
            		throw new UMException("Service user has no User Account");
            	}
            } else {
				userAccountMaint = CompanyPrincipalFactory.getInstance(companyId)
						.newUserAccount(uniqueName);
            }
            String password = null;
            if (dataAdditionalDetail != null) {
                JmxEntity requestAdditionalDetail = new JmxEntity(dataAdditionalDetail);
                requestDetailAttributes = requestAdditionalDetail.getAttributes();
				modifyUserAccountAttributes(
					userAccountMaint,
					userMaint,
					requestDetailAttributes,
					companyId,
					attributesChanged);
                password = handlePasswordActions(action, userMaint, userAccountMaint,
                        requestDetailAttributes);
            } else {
            	myLoc.errorT(mn, "No useraccount given");
                throw new UMException("No useraccount given");
            }
            if (createServiceUser){
				userMaint.save();
				userMaint.commit();
				userAccountMaint.save();
				userAccountMaint.commit();
            } else {
				CompanyPrincipalFactory.getInstance(companyId).commitUser(userMaint,
					userAccountMaint);
            }
			sendEmailForPasswordHandling(
				userMaint,
				action,
				password,
				null,
				attributesChanged);
			sendEmailForLockAttributeChange(
				userMaint, 
				attributesChanged,
				null);
			requestDetail.setUniqueId(userMaint.getUniqueID());
        } else if (principalType.equals(CompanyPrincipalFactory.IGROUP)) {
            IGroup group = CompanyPrincipalFactory.getInstance(companyId)
                    .newGroup(uniqueName);
            modifyGroupAttributes(group, requestDetailAttributes, companyId);
            group.save();
            group.commit();
            requestDetail.setUniqueId(group.getUniqueID());
        } else if (principalType.equals(CompanyPrincipalFactory.IROLE)) {
            IRole role = CompanyPrincipalFactory.getInstance(companyId)
                    .newRole(uniqueName);
            modifyRoleAttributes(role, requestDetailAttributes, companyId);
            role.save();
            role.commit();
            requestDetail.setUniqueId(role.getUniqueID());
        } else {
            throw new UMException(
                    "Cannot create principal, principal type is not supported");
        }
        /* Caution: this method is also used for self registration of users
         * It is possible that the current logged in User (in selfreg case something like "Guest")
         * has no permission for reading the whole newly created User data. Therefore, it has to be
         * checked if the JmxSearchHelper.getAllEntityDetails call fails because of an AccessError
         * Note: Here, getAllEntityDetails can be used, because this is a newly created principal
         * with no member assignments
         */
        try {
			return JmxSearchHelper.getAllEntityDetails(requestDetail.getUniqueId(), companyId, state);	
        } catch (AccessControlException e){
        	if (myLoc.beInfo()){
				myLoc.traceThrowableT(Severity.INFO, mn, e);
        	}
        }
        requestDetail.setModifyable(CompanyPrincipalFactory.getInstance(
                companyId).isPrincipalModifiable(requestDetail.getUniqueId()));
        return requestDetail;
    }

	/*
	 * Search for Roles: permission checks in order to determine if display role or not (to be checked by Role Persistence)
	 *   PCD Roles: check Portal ACL
	 * 	   Attributes to be filled by UI
	 * 	     namespace: "com.sap.security.core.search.transient"
	 * 	     attribute: "caller_id"
	 * 	     value:     "id of logged in user" --> to be set by Role Persistence itself
	 *   UME Roles: check UME action assignment permission read (by Role Persistence)
	 *     UMEPermission(
	 *       "ACTN", 
	 *       UMEPermission.ACTION_READ, 
	 *       UMEPermission.COM_SAP_SECURITY_CORE_USERMANAGEMENT_RELATION_PRINCIPAL_RELATION_MEMBER_ATTRIBUTE, 
	 *       null, 
	 *       null,
	 *       "id of logged in user")
	 * 
	 * Role Assignment (from Users / Groups / Roles): permission checks in order to determine if allow role assignment
	 *   PCD Roles: check Portal ACL
	 * 				+
	 *              check UME role assignment edit permission
	 * 				+
	 *              check UME user assignment edit permission
	 * 				/
	 *              check UME group assignment edit permission
	 *     attributeList.addAttribute("com.sap.security.pcd.aclprincipal",
	 *       "id of logged in user");
	 *   UME Roles: check UME action assignment edit permission
	 * 				+
	 *              check UME role assignment edit permission
	 * 				+
	 *              check UME user assignment edit permission
	 * 				/
	 *              check UME group assignment edit permission
	 * 
	 *     UMEPermission(
	 *       "ACTN",
	 *       UMEPermission.ACTION_EDIT,
	 *       UMEPermission.COM_SAP_SECURITY_CORE_USERMANAGEMENT_RELATION_PRINCIPAL_RELATION_PARENT_ATTRIBUTE,
	 *       null,
	 *       null,
	 *       "id of logged in user");
	 *     UMEPermission(
	 *       IPrincipalDatabag.ROLE_TYPE,
	 *       UMEPermission.ACTION_EDIT,
	 *       UMEPermission.COM_SAP_SECURITY_CORE_USERMANAGEMENT_RELATION_PRINCIPAL_RELATION_MEMBER_ATTRIBUTE,
	 *       null,
	 *       null,
	 *       "id of logged in user");
	 *     UMEPermission(
	 *       IPrincipalDatabag.USER_TYPE,
	 *       UMEPermission.ACTION_EDIT,
	 *       UMEPermission.COM_SAP_SECURITY_CORE_USERMANAGEMENT_RELATION_PRINCIPAL_RELATION_MEMBER_ATTRIBUTE,
	 *       null,
	 *       null,
	 *       "id of logged in user");
	 *     UMEPermission(
	 *       IPrincipalDatabag.GRUP_TYPE,
	 *       UMEPermission.ACTION_EDIT,
	 *       UMEPermission.COM_SAP_SECURITY_CORE_USERMANAGEMENT_RELATION_PRINCIPAL_RELATION_MEMBER_ATTRIBUTE,
	 *       null,
	 *       null,
	 *       "id of logged in user");
	 */
    static IJmxResult modifyEntityAssignments(String[] parentIds, String[] memberIds,
            boolean addMembers, String companyId, IJmxState state) throws UMException, OpenDataException {
        final String mn = "void modifyEntityMappings("
            + "String[] parentIds, String[] memberIds, boolean addMembers, String companyId) throws UMException";
        if (myLoc.beDebug()) {
            myLoc.debugT(mn, "AddMember is {0}", new Object[] { new Boolean(
                    addMembers) });
            myLoc.debugT(mn, "Company is {0}", new Object[] { companyId });
        }
        int result = IJmxServer.RESULT_OPERATION_OK;
        //if more than one company group is available as parent, no change should be done for these groups
        int numberOfCompanyGroups = CompanyPrincipalFactory.getNumberOfCompanyGroups(parentIds); 
        if (numberOfCompanyGroups > 1){
        	result = result | IJmxServer.RESULT_ASSIGN_COMPANY_GROUP_ONLY_SINGLE;
        }
        //Message list in order to collect messages from parents, e.g. ABAP Roles
        List<IMessage> messageList = new ArrayList<IMessage>();
        for (int p = 0; p < parentIds.length; p++) {
			String parentType = CompanyPrincipalFactory.getInstance(companyId)
					.getPrincipalType(parentIds[p]);
			if (myLoc.beDebug()) {
				myLoc.debugT(mn, "Parent type is {0}, Parent id is {1}",
						new Object[] { parentType, parentIds[p] });
			}
			/* Check if principal and member have already been assigned
			 */
			IPrincipal parent = CompanyPrincipalFactory.getInstance(companyId).getPrincipal(parentIds[p]);
			List membersToChange = new ArrayList(memberIds.length);
			for (int m = 0; m < memberIds.length; m++) {
				String memberType = null;
				// Action check now in CompanyPrincipalFactory
				CompanyPrincipalFactory.getInstance(companyId).invalidatePrincipalInCache(
					memberIds[m]);
				memberType = CompanyPrincipalFactory.getInstance(companyId)
						.getPrincipalType(memberIds[m]);
				if (myLoc.beDebug()) {
					myLoc.debugT(mn, "Member type is {0}, Member id is {1}",
							new Object[] { memberType, memberIds[m] });
				}
				/*
				 * parent type role
				 */
				if (parentType.equals(CompanyPrincipalFactory.IROLE)) {
					IRole parentReadonlyRole = (IRole) parent;
					if (memberType.equals(CompanyPrincipalFactory.IGROUP)) {
						if (addMembers) {
							if (parentReadonlyRole.isGroupMember(memberIds[m], false)) {
								if (myLoc.beInfo()) {
									myLoc
											.infoT(
													mn,
													"{0} {1} is already group member of {2} {3}, not assigning it",
													new Object[] { memberType,
															memberIds[m],
															parentType,
															parentIds[p] });
								}
								result =  result
										| IJmxServer.RESULT_ASSIGNED_ALREADY;
								continue;
							}
						} else {
							if (!parentReadonlyRole.isGroupMember(memberIds[m], false)) {
								if (myLoc.beInfo()) {
									myLoc
											.infoT(
													mn,
													"{0} {1} is not group member of {2} {3}, not removing it",
													new Object[] { memberType,
															memberIds[m],
															parentType,
															parentIds[p] });
								}
								result =  result
										| IJmxServer.RESULT_REMOVED_ALREADY;
								continue;
							}
						}
					}
					if (memberType.equals(CompanyPrincipalFactory.IUSER)) {
						if (addMembers) {
							if (parentReadonlyRole.isUserMember(memberIds[m], false)) {
								if (myLoc.beInfo()) {
									myLoc
											.infoT(
													mn,
													"{0} {1} is already user member of {2} {3}, not assigning it",
													new Object[] { memberType,
															memberIds[m],
															parentType,
															parentIds[p] });
								}
								result =  result
										| IJmxServer.RESULT_ASSIGNED_ALREADY;
								continue;
							}
						} else {
							if (!parentReadonlyRole.isUserMember(memberIds[m], false)) {
								if (myLoc.beInfo()) {
									myLoc
											.infoT(
													mn,
													"{0} {1} is not user member of {2} {3}, not removing it",
													new Object[] { memberType,
															memberIds[m],
															parentType,
															parentIds[p] });
								}
								result =  result
										| IJmxServer.RESULT_REMOVED_ALREADY;
								continue;
							}
						}
					}
					if (memberType.equals(CompanyPrincipalFactory.IROLE)) {
						if (myLoc.beInfo()) {
							myLoc
									.infoT(
											mn,
											"Tried to assign {0} {1} to {2} {3}, this is not possible",
											new Object[] { memberType,
													memberIds[m], parentType,
													parentIds[p] });
						}
						result =  result | IJmxServer.RESULT_ASSIGN_ERROR;
						continue;
					}
					if (memberType.equals(JmxActionFactoryWrapper.IACTION)) {
						IAction memberAction = JmxActionFactoryWrapper.getAction(
										memberIds[m]);
						if (addMembers) {
							if (JmxActionFactoryWrapper.isActionMember(parentReadonlyRole,
									memberAction)) {
								if (myLoc.beInfo()) {
									myLoc
											.infoT(
													mn,
													"{0} {1} is already action member of {2} {3}, not assigning it",
													new Object[] { memberType,
															memberIds[m],
															parentType,
															parentIds[p] });
								}
								result =  result
										| IJmxServer.RESULT_ASSIGNED_ALREADY;
								continue;
							}
						} else {
							if (!JmxActionFactoryWrapper.isActionMember(parentReadonlyRole,
									memberAction)) {
								if (myLoc.beInfo()) {
									myLoc
											.infoT(
													mn,
													"{0} {1} is not action member of {2} {3}, not removing it",
													new Object[] { memberType,
															memberIds[m],
															parentType,
															parentIds[p] });
								}
								result =  result
										| IJmxServer.RESULT_REMOVED_ALREADY;
								continue;
							}
						}
					}
				}
				/*
				 * parent type group
				 */
				if (parentType.equals(CompanyPrincipalFactory.IGROUP)) {
					IGroup parentReadonlyGroup = (IGroup) parent;
					if (memberType.equals(CompanyPrincipalFactory.IGROUP)) {
						if (CompanyPrincipalFactory.isCompanyGroup(parentReadonlyGroup.getUniqueID())){
							result = result | IJmxServer.RESULT_ASSIGN_COMPANY_GROUPS_NO_GROUP_MEMBER;
							continue;
						}
						if (addMembers) {
							if (parentReadonlyGroup.isGroupMember(memberIds[m], false)) {
								if (myLoc.beInfo()) {
									myLoc
											.infoT(
													mn,
													"{0} {1} is already group member of {2} {3}, not assigning it",
													new Object[] { memberType,
															memberIds[m],
															parentType,
															parentIds[p] });
								}
								result =  result
										| IJmxServer.RESULT_ASSIGNED_ALREADY;
								continue;
							}
						} else {
							if (!parentReadonlyGroup.isGroupMember(memberIds[m], false)) {
								if (myLoc.beInfo()) {
									myLoc
											.infoT(
													mn,
													"{0} {1} is not group member of {2} {3}, not removing it",
													new Object[] { memberType,
															memberIds[m],
															parentType,
															parentIds[p] });
								}
								result =  result
										| IJmxServer.RESULT_REMOVED_ALREADY;
								continue;
							}
						}
					}
					if (memberType.equals(CompanyPrincipalFactory.IUSER)) {
						if (CompanyPrincipalFactory.isCompanyGroup(parentReadonlyGroup.getUniqueID())){
							if (numberOfCompanyGroups > 1){
								continue;
							}
						}
						if (addMembers) {
							if (parentReadonlyGroup.isUserMember(memberIds[m], false)) {
								if (myLoc.beInfo()) {
									myLoc
											.infoT(
													mn,
													"{0} {1} is already user member of {2} {3}, not assigning it",
													new Object[] { memberType,
															memberIds[m],
															parentType,
															parentIds[p] });
								}
								result =  result
										| IJmxServer.RESULT_ASSIGNED_ALREADY;
								continue;
							}
						} else {
							if (!parentReadonlyGroup.isUserMember(memberIds[m], false)) {
								if (myLoc.beInfo()) {
									myLoc
											.infoT(
													mn,
													"{0} {1} is not user member of {2} {3}, not removing it",
													new Object[] { memberType,
															memberIds[m],
															parentType,
															parentIds[p] });
								}
								result =  result
										| IJmxServer.RESULT_REMOVED_ALREADY;
								continue;
							}
						}
					}
					if (memberType.equals(CompanyPrincipalFactory.IROLE)) {
						if (myLoc.beInfo()) {
							myLoc
									.infoT(
											mn,
											"Tried to assign {0} {1} to {2} {3}, this is not possible",
											new Object[] { memberType,
													memberIds[m], parentType,
													parentIds[p] });
						}
						result =  result | IJmxServer.RESULT_ASSIGN_ERROR;
						continue;
					}
				}
				/*
				 * parent type user
				 */
				if (parentType.equals(CompanyPrincipalFactory.IUSER)) {
					if (myLoc.beInfo()) {
						myLoc
								.infoT(
										mn,
										"Tried to assign {0} {1} to {2} {3}, this is not possible",
										new Object[] { memberType,
												memberIds[m], parentType,
												parentIds[p] });
					}
					result =  result | IJmxServer.RESULT_ASSIGN_ERROR;
					continue;
				}
				membersToChange.add(memberIds[m]);
			}
			/* Assigning parents and members
			 */
			if (membersToChange.size() < 1){
				continue;
			}
			IPrincipalMaint parentMaint;
			//not checking if principal is modifyable at all, only if attribute is modifyable
            if (CompanyPrincipalFactory.getInstance(companyId).isPrincipalAttributeModifiable(parentType, parentIds[p], IPrincipal.DEFAULT_RELATION_NAMESPACE, IPrincipal.PRINCIPAL_RELATION_MEMBER_ATTRIBUTE)){
				parentMaint = CompanyPrincipalFactory.getInstance(companyId).getMutablePrincipal(parentIds[p], IPrincipal.DEFAULT_RELATION_NAMESPACE, IPrincipal.PRINCIPAL_RELATION_MEMBER_ATTRIBUTE);            	
            } else {
				if (myLoc.beInfo()) {
					myLoc
						.infoT(
								mn,
								"Parent {0} is not modifiable on namespace {1} and attribute {2}",
								new Object[]{parentIds[p], IPrincipal.DEFAULT_RELATION_NAMESPACE, IPrincipal.PRINCIPAL_RELATION_MEMBER_ATTRIBUTE});
				}
				result = result | IJmxServer.RESULT_ASSIGNMENT_NO_ACCESS;
				continue;
            }
			for (Iterator memberIdsIt = membersToChange.iterator(); memberIdsIt.hasNext();){
				String memberId = (String)memberIdsIt.next();
                String memberType = null;
                // Action check now in CompanyPrincipalFactory
                memberType = CompanyPrincipalFactory.getInstance(companyId)
                        .getPrincipalType(memberId);
                //checking if member is assignable, also for Actions
				if (!CompanyPrincipalFactory.getInstance(companyId).isPrincipalMemberAssignable(memberId)){
					if (myLoc.beInfo()) {
						myLoc
							.infoT(
									mn,
									"Parent {0} is not modifiable",
									new Object[]{parentIds[p]});
					}
					result = result | IJmxServer.RESULT_ASSIGNMENT_NO_ACCESS;
					continue;            	
				}
                if (myLoc.beDebug()) {
                    myLoc.debugT(mn, "Member type is {0}, Member id is {1}",
                            new Object[] { memberType, memberId });
                }
                /*
                 * parent type role
                 */
                if (parentType.equals(CompanyPrincipalFactory.IROLE)) {
                    IRole parentRole = (IRole) parentMaint;
					if (!CompanyPrincipalFactory.getInstance(companyId).isPrincipalParentAssignable(parentIds[p])){
						if (myLoc.beInfo()) {
							myLoc
								.infoT(
										mn,
										"Parent {0} is not modifyable on namespace {1} and attribute {2}",
										new Object[]{parentIds[p], IPrincipal.DEFAULT_RELATION_NAMESPACE, IPrincipal.PRINCIPAL_RELATION_MEMBER_ATTRIBUTE});
						}
						result = result | IJmxServer.RESULT_ASSIGNMENT_NO_ACCESS;
						continue;
					}
                    if (memberType.equals(CompanyPrincipalFactory.IGROUP)) {
                        if (addMembers) {
                            if (myLoc.beInfo()) {
                                myLoc
                                        .infoT(
                                                mn,
                                                "Assigning {0} {1} as group member of {2} {3}",
                                                new Object[] { memberType,
                                                        memberId,
                                                        parentType,
                                                        parentIds[p] });
                            }
                            parentRole.addGroupMember(memberId);
                        } else {
                            if (myLoc.beInfo()) {
                                myLoc
                                        .infoT(
                                                mn,
                                                "Removing {0} {1} form group member of {2} {3}",
                                                new Object[] { memberType,
                                                        memberId,
                                                        parentType,
                                                        parentIds[p] });
                            }
                            parentRole.removeGroupMember(memberId);
                        }
                    }
                    if (memberType.equals(CompanyPrincipalFactory.IUSER)) {
                        if (addMembers) {
                            if (myLoc.beInfo()) {
                                myLoc
                                        .infoT(
                                                mn,
                                                "Assigning {0} {1} as user member of {2} {3}",
                                                new Object[] { memberType,
                                                        memberId,
                                                        parentType,
                                                        parentIds[p] });
                            }
                            parentRole.addUserMember(memberId);
                        } else {
                            if (myLoc.beInfo()) {
                                myLoc
                                        .infoT(
                                                mn,
                                                "Removing {0} {1} form user member of {2} {3}",
                                                new Object[] { memberType,
                                                        memberId,
                                                        parentType,
                                                        parentIds[p] });
                            }
                            parentRole.removeUserMember(memberId);
                        }
                    }
                    if (memberType.equals(JmxActionFactoryWrapper.IACTION)) {
                        IAction memberAction = JmxActionFactoryWrapper.getAction(
                                        memberId);
                        if (addMembers) {
                            if (myLoc.beInfo()) {
                                myLoc
                                        .infoT(
                                                mn,
                                                "Assigning {0} {1} as action member of {2} {3}",
                                                new Object[] { memberType,
                                                        memberId,
                                                        parentType,
                                                        parentIds[p] });
                            }
                            JmxActionFactoryWrapper.addActionMember(parentRole,
                                    memberAction);
                        } else {
                            if (myLoc.beInfo()) {
                                myLoc
                                        .infoT(
                                                mn,
                                                "Removing {0} {1} form action member of {2} {3}",
                                                new Object[] { memberType,
                                                        memberId,
                                                        parentType,
                                                        parentIds[p] });
                            }
                            JmxActionFactoryWrapper.removeActionMember(parentRole,
                                    memberAction);
                        }
                    }
                }
                /*
                 * parent type group
                 */
                if (parentType.equals(CompanyPrincipalFactory.IGROUP)) {
                    IGroup parentGroup = (IGroup) parentMaint;
                    if (memberType.equals(CompanyPrincipalFactory.IGROUP)) {
                        if (addMembers) {
                            if (myLoc.beInfo()) {
                                myLoc
                                        .infoT(
                                                mn,
                                                "Assigning {0} {1} as group member of {2} {3}",
                                                new Object[] { memberType,
                                                        memberId,
                                                        parentType,
                                                        parentIds[p] });
                            }
                            parentGroup.addGroupMember(memberId);
                        } else {
                            if (myLoc.beInfo()) {
                                myLoc
                                        .infoT(
                                                mn,
                                                "Removing {0} {1} form group member of {2} {3}",
                                                new Object[] { memberType,
                                                        memberId,
                                                        parentType,
                                                        parentIds[p] });
                            }
                            parentGroup.removeGroupMember(memberId);
                        }
                    }
                    if (memberType.equals(CompanyPrincipalFactory.IUSER)) {
						if (CompanyPrincipalFactory.isCompanyGroup(parentGroup.getUniqueID())){
							result = result | IJmxServer.RESULT_ASSIGN_COMPANY_GROUPS_PERFORMED;							
						}                    	
                        if (addMembers) {
                            if (myLoc.beInfo()) {
                                myLoc
                                        .infoT(
                                                mn,
                                                "Assigning {0} {1} as user member of {2} {3}",
                                                new Object[] { memberType,
                                                        memberId,
                                                        parentType,
                                                        parentIds[p] });
                            }
                            parentGroup.addUserMember(memberId);
                        } else {
                            if (myLoc.beInfo()) {
                                myLoc
                                        .infoT(
                                                mn,
                                                "Removing {0} {1} form user member of {2} {3}",
                                                new Object[] { memberType,
                                                        memberId,
                                                        parentType,
                                                        parentIds[p] });
                            }
                            parentGroup.removeUserMember(memberId);
                        }
                    }
                }
            }
            //Check if parent principal was modified, only commit if true
        	if (parentMaint.isModified()){
				parentMaint.save();
				parentMaint.commit();
				Iterator messageIt = parentMaint.getMessages(false);
				if (messageIt != null){
					while (messageIt.hasNext()){
						messageList.add((IMessage)messageIt.next());
					}
				}
        	}
        }
        JmxResult jmxResult = new JmxResult();
        jmxResult.setStatus(result);
        int messageSize;
        if (messageList != null && (messageSize = messageList.size()) > 0){
        	IJmxMessage[] messageArray = new IJmxMessage[messageSize];
        	int i = 0;
        	for (Iterator<IMessage> it = messageList.iterator(); it.hasNext();i++){
        		messageArray[i] = new JmxMessage(it.next(), state.getLocale());
        	}
        	jmxResult.setMessages(messageArray);
        }
        return jmxResult;
    }

	public static IJmxResult resetPassword(CompositeData[] attributes, IJmxState state) throws Exception {
        final String mn = "static private void modifyAttributes("
            + "String principalType, IPrincipalMaint principalMaint, IJmxAttribute[] attributes, String companyId)";
        if (myLoc.beInfo()) {
        }
        
        JmxResult rc = new JmxResult();
		Locale locale = state.getLocale();
		IUser iUser= null;
		IUserAccount iUserAccount= null;
		IUser iUserAlias = null;
		
		String logonid = null;
		String lastname = null;
		String firstname = null;
		String email = null;
		String securityanswer = null;
		
		// retrieve necessary information		
		for (int i = 0; i < attributes.length; i++) {
			IJmxAttribute attr = new JmxAttribute(attributes[i]);
			if ("j_user".equals(attr.getName())) {
				logonid = attr.getValue();
			} else if ("lastname".equals(attr.getName())) {
				lastname = attr.getValue();
			} else if ("firstname".equals(attr.getName())) {
				firstname = attr.getValue();
			} else if ("email".equals(attr.getName())) {
				email = attr.getValue();
			} else if (AbstractUserAccount.SECURITYANSWER.equals(attr.getName())) {
				securityanswer = attr.getValue();
			}
		}
		
		// check if necessary required information is available
		if (logonid == null) {
			return createResult(IJmxResult.STATUS_NOT_OK, "RESET_PASSWORD_INFO_ERROR", locale);
		}
		if (email == null) {
			return createResult(IJmxResult.STATUS_NOT_OK, "RESET_PASSWORD_INFO_ERROR", locale);
		}
		// check if name must be given
		if (InternalUMFactory.getConfiguration().getBooleanDynamic(LOGON_HELP_NAME_REQUIRED, false)
				&& (lastname == null)) {
			return createResult(IJmxResult.STATUS_NOT_OK, "RESET_PASSWORD_INFO_ERROR", locale);
		}

		/* 
		 * searching for user with logon id
		 */
		try {
			iUser = UMFactory.getUserFactory().getUserByLogonID(logonid);
		} catch (NoSuchPrincipalException nosuaex) {
	        if (myLoc.beInfo()) {
	            myLoc.infoT(mn, "No user found with logon id {0}", new Object[] {
	                    logonid });
	        }
		}
		
		/* 
		 * searching for user with logon id
		 */
		try {
			iUserAlias = UMFactory.getUserFactory().getUserByLogonAlias(logonid, null);
		} catch (NoSuchPrincipalException nosuaex) {
	        if (myLoc.beInfo()) {
	            myLoc.infoT(mn, "No user found with logon alias {0}", new Object[] {
	                    logonid });
	        }
		}
		
		/*
		 * neither logon id nor logon alias correct
		 */
		if (iUser == null && iUserAlias == null) {
	        if (myLoc.beInfo()) {
	            myLoc.infoT(mn, "No user found with logon id nor alias {0}. No password is reset.", new Object[] {
	                    logonid });
	        }
			return createResult(IJmxResult.STATUS_NOT_OK, "RESET_PASSWORD_INFO_ERROR", locale);
		}
		
		/*
		 * User enters logonid, email, first name, last name (name can be optionally hidden), all entries must match to get the password reset
		 * We will use the logonid both as userid and as alias and check whether we find users with the correct email and name.
		 * If anyone matches, we will use that one
		 * If both matches the userid wins.
		 */
		if (iUser == null) {
			iUser = iUserAlias;
			iUserAlias = null;
		}
		
		// check if entered data is correct
		if (!email.equalsIgnoreCase(iUser.getEmail())) {
			if ((iUserAlias == null) || (!email.equalsIgnoreCase(iUserAlias.getEmail()))) {
				
				return createResult(IJmxResult.STATUS_NOT_OK, "RESET_PASSWORD_INFO_ERROR", locale);
			} else if (iUserAlias != null) {
				iUser = iUserAlias;
			}
		}
		// check if name is correct
		if (InternalUMFactory.getConfiguration().getBooleanDynamic(LOGON_HELP_NAME_REQUIRED, false) &&
				( !lastname.equalsIgnoreCase(iUser.getLastName())
				|| (firstname!=null?!firstname.equalsIgnoreCase(iUser.getFirstName()):false)) ) {
			// check if logon alias will work
			if ((iUserAlias == null) ||
					( !lastname.equalsIgnoreCase(iUserAlias.getLastName())
					|| (firstname!=null?!firstname.equalsIgnoreCase(iUserAlias.getFirstName()):false)) ) {


				return createResult(IJmxResult.STATUS_NOT_OK, "RESET_PASSWORD_INFO_ERROR", locale);
			} else if (iUserAlias != null) {
				iUser = iUserAlias;
			}
		}
		
		// get user account
		iUserAccount = iUser.getUserAccounts()[0];
		
		String secQuestionMode = InternalUMFactory.getConfiguration().getStringDynamic(LOGON_HELP_SECURITY_QUESTION);
		if (("true".equalsIgnoreCase(secQuestionMode) || "predefined".equalsIgnoreCase(secQuestionMode))
				&& (securityanswer == null)) {
			// security answer enabled but not submitted, return security question
			String[] securityquestions = iUserAccount.getAttribute(IPrincipal.DEFAULT_NAMESPACE, AbstractUserAccount.SECURITYQUESTION);
			String[] securityquestionspredefined = iUserAccount.getAttribute(IPrincipal.DEFAULT_NAMESPACE, AbstractUserAccount.SECURITYQUESTIONPREDEFINED);
			
			rc.setStatus(IJmxResult.STATUS_UNDEFINED);

			JmxMessage[] messages = new JmxMessage[1];
			messages[0] = new JmxMessage();
			
			if ((securityquestions != null) && (securityquestions.length > 0)) {
				messages[0].setMessage(securityquestions[0]);
				messages[0].setLocalizedMessage(securityquestions[0]);
				rc.setMessages(messages);
				return rc;
			} else if ((securityquestionspredefined != null) && (securityquestionspredefined.length > 0)) {
				messages[0].setMessage(securityquestionspredefined[0]);
				messages[0].setLocalizedMessage(UMFactory.getSecurityPolicy().getLocalizedMessage(locale, securityquestionspredefined[0]));
				rc.setMessages(messages);
				return rc;
			} else {
				// no security message set
				rc.setStatus(IJmxResult.STATUS_UNDEFINED);
				messages[0].setLocalizedMessage(UMFactory.getSecurityPolicy().getLocalizedMessage(locale, "NO_SECURITY_QUESTION_SET"));
				rc.setMessages(messages);
				return rc;
			}
			
		}
		
		boolean wrongSecAnswer = true;
		if ((checkPropertyRange(LOGON_HELP_SECURITY_QUESTION, "true")
			|| checkPropertyRange(LOGON_HELP_SECURITY_QUESTION, "predefined"))) {
			if (securityanswer != null) {
				securityanswer = securityanswer.trim();
				PasswordHash secAnswerHashUtil =
					new PasswordHash(
						iUserAccount.getLogonUid(),
						securityanswer.toUpperCase());
				String[] values =
					iUserAccount.getAttribute(
						IPrincipal.DEFAULT_NAMESPACE,
						AbstractUserAccount.SECURITYANSWER);
				if (values != null && values.length > 0 && values[0] != null) {
					if (secAnswerHashUtil.checkHash(values[0])) {
						wrongSecAnswer = false;
					}
				}
			}
		} else {
			wrongSecAnswer = false;
		}
		
		if (wrongSecAnswer){
			rc.setStatus(JmxResult.STATUS_NOT_OK);

			JmxMessage[] messages = new JmxMessage[1];
			messages[0] = new JmxMessage();
			
			messages[0].setMessage(UMFactory.getSecurityPolicy().getLocalizedMessage(locale, "WRONG_SECURITY_ANSWER"));
			messages[0].setLocalizedMessage(UMFactory.getSecurityPolicy().getLocalizedMessage(locale, "WRONG_SECURITY_ANSWER"));
			
			rc.setMessages(messages);
			
			return rc;
		}
			
        // all data matched, assign a new password and email to user
		if (!iUserAccount.isMutable()) {
			iUserAccount = UMFactory.getUserAccountFactory().getMutableUserAccount(iUserAccount.getUniqueID());
		}
  		String newPassword = generatePassword(iUserAccount);
  		iUserAccount.setPassword(newPassword);
  		iUserAccount.save();
  		iUserAccount.commit();
  		sendEmailForPasswordHandling(iUser, IJmxServer.MODIFY_ACTION_PASSWORD_GENERATE, newPassword, null, null);
  		rc.setStatus(IJmxResult.STATUS_OK);
		JmxMessage[] messages = new JmxMessage[1];
		messages[0] = new JmxMessage();
		
		messages[0].setMessage("NEW_PSWD_ASSIGNED");
		messages[0].setLocalizedMessage(UMFactory.getSecurityPolicy().getLocalizedMessage(locale, "NEW_PSWD_ASSIGNED"));

		rc.setMessages(messages);
		
		return rc;
	}
	
	private static boolean checkPropertyRange(String property, String range) {
		if ((property == null || range == null))
			return false;
		
		String value = InternalUMFactory.getConfiguration().getStringDynamic(property, null);
		if (value == null) {
			return false;
		}
		
		return (range.equalsIgnoreCase(value));
	}
	
	private static IJmxResult createResult(int status, String key, Locale locale) throws Exception {
		JmxResult rc = new JmxResult();
		rc.setStatus(status);

		JmxMessage[] messages = new JmxMessage[1];
		messages[0] = new JmxMessage();
		messages[0].setMessage(key);
		messages[0].setLocalizedMessage(UMFactory.getSecurityPolicy().getLocalizedMessage(locale, key));
		
		rc.setMessages(messages);
		
		return rc;
	}
	
}
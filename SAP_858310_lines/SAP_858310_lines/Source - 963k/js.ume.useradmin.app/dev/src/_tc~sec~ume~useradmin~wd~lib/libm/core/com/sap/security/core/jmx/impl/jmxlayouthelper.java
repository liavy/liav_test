package com.sap.security.core.jmx.impl;

import java.util.ArrayList;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.OpenDataException;

import com.sap.security.api.IPrincipal;
import com.sap.security.api.IUser;
import com.sap.security.api.IUserAccount;
import com.sap.security.api.UMException;
import com.sap.security.api.logon.ILoginConstants;
import com.sap.security.api.srvUser.IServiceUserFactory;
import com.sap.security.core.imp.AbstractUserAccount;
import com.sap.security.core.imp.User;
import com.sap.security.core.jmx.IJmxEntity;
import com.sap.security.core.persistence.IPrincipalDatabag;
import com.sap.security.core.role.imp.PermissionRoles;
import com.sap.tc.logging.Location;

class JmxLayoutHelper {

    private static Location myLoc = Location.getLocation(JmxLayoutHelper.class);

    static IJmxEntity getAttributeLayoutInformation(String uniqueId,
            String princypalType, CompositeData[] dataPopulation,
            String companyId) throws UMException, OpenDataException {
        final String mn = "static IJmxEntity getAttributeLayoutInformation(String uniqueId,CompositeData[] dataPopulation, String companyId)";
        if (myLoc.beDebug()) {
            if (uniqueId == null || uniqueId.equals(CompanyPrincipalFactory.EMPTY)) {
                myLoc
                        .debugT(
                                mn,
                                "Evaluating createable attributes for principal type {0}",
                                new Object[] { princypalType });
            } else {
                myLoc.debugT(mn,
                        "Evaluating modifyable attributes for principal {0}",
                        new Object[] { uniqueId });
            }
        }
		JmxEntity returnEntity = new JmxEntity();
        IPrincipal dummyPrincipal = null;
		String privateType = null;
        String dummyUniqueName = null;
        if (uniqueId != null && !uniqueId.equals(CompanyPrincipalFactory.EMPTY)) {
            String publicType = CompanyPrincipalFactory.getInstance(companyId)
                    .getPrincipalType(uniqueId);
            privateType = CompanyPrincipalFactory
                    .getPrivatePrincipalTypeIdentifier(publicType);
            returnEntity.setType(publicType);
            returnEntity.setUniqueId(uniqueId);
            returnEntity.setModifyable(CompanyPrincipalFactory.getInstance(
                    companyId).isPrincipalModifiable(uniqueId));
        } else {
        	//create modus, create a dummy principal
            privateType = CompanyPrincipalFactory
            	.getPrivatePrincipalTypeIdentifier(princypalType);
			dummyPrincipal = CompanyPrincipalFactory.createDummyPrincipal(princypalType, null);
            returnEntity.setType(princypalType);
            returnEntity.setUniqueId(CompanyPrincipalFactory.EMPTY);
            returnEntity.setModifyable(
        		CompanyPrincipalFactory.getInstance(companyId).isPrincipalAttributeCreateable(
					privateType,
					dummyPrincipal, 
					IPrincipal.DEFAULT_NAMESPACE,
					CompanyPrincipalFactory.UNIQUEID));
        }
        ArrayList attributeList = new ArrayList();
        if (privateType.equals(IPrincipalDatabag.ROLE_TYPE)
                || privateType.equals(IPrincipalDatabag.GROUP_TYPE)) {
            attributeList.add(generateLayoutAttribute(privateType, uniqueId, dummyPrincipal,
                    IPrincipal.DEFAULT_NAMESPACE, IPrincipal.DESCRIPTION,
                    companyId));
            attributeList.add(generateLayoutAttribute(privateType, uniqueId, dummyPrincipal,
                    IPrincipal.DEFAULT_RELATION_NAMESPACE,
                    IPrincipal.PRINCIPAL_RELATION_MEMBER_ATTRIBUTE, companyId));
			attributeList.add(generateLayoutAttribute(privateType, uniqueId, dummyPrincipal,
					IPrincipal.DEFAULT_RELATION_NAMESPACE,
					IPrincipal.PRINCIPAL_RELATION_PARENT_ATTRIBUTE, companyId));
			attributeList.add(generateLayoutAttribute(privateType, uniqueId, dummyPrincipal,
					PermissionRoles.ROLE_NAMESPACE,	PermissionRoles.ACTIONS, companyId));
        } else if (privateType.equals(IPrincipalDatabag.USER_TYPE)) {
            attributeList.add(generateLayoutAttribute(privateType, uniqueId, dummyPrincipal,
                    IPrincipal.DEFAULT_NAMESPACE, User.ACCESSIBILITYLEVEL,
                    companyId));
            attributeList.add(generateLayoutAttribute(privateType, uniqueId, dummyPrincipal,
                    IPrincipal.DEFAULT_NAMESPACE, User.CITY, companyId));
            attributeList.add(generateLayoutAttribute(privateType,uniqueId, dummyPrincipal,
                    IPrincipal.DEFAULT_NAMESPACE, User.COMPANY, companyId));
            attributeList.add(generateLayoutAttribute(privateType, uniqueId, dummyPrincipal,
                    IPrincipal.DEFAULT_NAMESPACE, User.COUNTRY, companyId));
            attributeList.add(generateLayoutAttribute(privateType, uniqueId, dummyPrincipal,
                    IPrincipal.DEFAULT_NAMESPACE, User.DEPARTMENT, companyId));
            attributeList.add(generateLayoutAttribute(privateType, uniqueId, dummyPrincipal,
                    IPrincipal.DEFAULT_NAMESPACE, User.DESCRIPTION, companyId));
            attributeList.add(generateLayoutAttribute(privateType, uniqueId, dummyPrincipal,
                    IPrincipal.DEFAULT_NAMESPACE, User.EMAIL, companyId));
            attributeList.add(generateLayoutAttribute(privateType, uniqueId, dummyPrincipal,
                    IPrincipal.DEFAULT_NAMESPACE, User.FAX, companyId));
            attributeList.add(generateLayoutAttribute(privateType, uniqueId, dummyPrincipal,
                    IPrincipal.DEFAULT_NAMESPACE, User.FIRSTNAME, companyId));
            attributeList.add(generateLayoutAttribute(privateType, uniqueId, dummyPrincipal,
                    IPrincipal.DEFAULT_NAMESPACE, User.LOCALE, companyId));
            attributeList.add(generateLayoutAttribute(privateType, uniqueId, dummyPrincipal,
                    IPrincipal.DEFAULT_NAMESPACE, User.LASTNAME, companyId));
            attributeList.add(generateLayoutAttribute(privateType, uniqueId, dummyPrincipal,
                    IPrincipal.DEFAULT_NAMESPACE, User.MOBILE, companyId));
            attributeList.add(generateLayoutAttribute(privateType, uniqueId, dummyPrincipal,
                    IPrincipal.DEFAULT_NAMESPACE, CompanyPrincipalFactory.ORGUNIT, companyId));
            attributeList.add(generateLayoutAttribute(privateType, uniqueId, dummyPrincipal,
                    IPrincipal.DEFAULT_NAMESPACE, User.JOBTITLE, companyId));
            attributeList.add(generateLayoutAttribute(privateType, uniqueId, dummyPrincipal,
                    IPrincipal.DEFAULT_NAMESPACE, User.SALUTATION, companyId));
            attributeList.add(generateLayoutAttribute(privateType, uniqueId, dummyPrincipal,
                    IPrincipal.DEFAULT_NAMESPACE, User.STATE, companyId));
            attributeList
                    .add(generateLayoutAttribute(privateType, uniqueId, dummyPrincipal,
                            IPrincipal.DEFAULT_NAMESPACE, User.STREETADDRESS,
                            companyId));
            attributeList.add(generateLayoutAttribute(privateType, uniqueId, dummyPrincipal,
                    IPrincipal.DEFAULT_NAMESPACE, User.TELEPHONE, companyId));
            attributeList.add(generateLayoutAttribute(privateType, uniqueId, dummyPrincipal,
                    IPrincipal.DEFAULT_NAMESPACE, User.TIMEZONE, companyId));
            attributeList.add(generateLayoutAttribute(privateType, uniqueId, dummyPrincipal,
                    IPrincipal.DEFAULT_NAMESPACE, User.ZIP, companyId));
			attributeList.add(generateLayoutAttribute(privateType, uniqueId, dummyPrincipal,
					IPrincipal.DEFAULT_RELATION_NAMESPACE,
					IPrincipal.PRINCIPAL_RELATION_PARENT_ATTRIBUTE, companyId));
            if (uniqueId != null && !uniqueId.equals(CompanyPrincipalFactory.EMPTY)){
				JmxAttribute attr = new JmxAttribute();
				attr.setNamespace(IServiceUserFactory.SERVICEUSER_NAMESPACE);
				attr.setName(IServiceUserFactory.SERVICEUSER_ATTRIBUTE);
				attr.setModifyable(false);
				attributeList.add(attr);
            } else {
				attributeList.add(generateLayoutAttribute(privateType, uniqueId, dummyPrincipal,
						IServiceUserFactory.SERVICEUSER_NAMESPACE,
						IServiceUserFactory.SERVICEUSER_ATTRIBUTE,
						companyId));            	
            }
            //helper methods for UACC
            String uaccUniqueId = null;
			IPrincipal uaccPrincipal = null;
            if (uniqueId != null && !uniqueId.equals(CompanyPrincipalFactory.EMPTY)){
				IUserAccount[] accounts = ((IUser) CompanyPrincipalFactory
						.getInstance(companyId).getPrincipal(uniqueId))
						.getUserAccounts();
				if (accounts != null && accounts.length > 0 && accounts[0] != null) {
					uaccUniqueId = accounts[0].getUniqueID();
				}
            } else {
            	uaccPrincipal = CompanyPrincipalFactory.createDummyPrincipal(IPrincipalDatabag.ACCOUNT_TYPE, (IUser)dummyPrincipal);
            }
            attributeList.add(generateLayoutAttribute(IPrincipalDatabag.ACCOUNT_TYPE,
					uaccUniqueId, uaccPrincipal, IPrincipal.DEFAULT_NAMESPACE,
                    AbstractUserAccount.IS_LOCKED, companyId));
            attributeList.add(generateLayoutAttribute(IPrincipalDatabag.ACCOUNT_TYPE,
					uaccUniqueId, uaccPrincipal, IPrincipal.DEFAULT_NAMESPACE,
					AbstractUserAccount.IS_ACCOUNT_LOCKED, companyId));                    
            attributeList.add(generateLayoutAttribute(IPrincipalDatabag.ACCOUNT_TYPE,
					uaccUniqueId, uaccPrincipal, IPrincipal.DEFAULT_NAMESPACE,
                    AbstractUserAccount.VALID_FROM, companyId));
            attributeList.add(generateLayoutAttribute(IPrincipalDatabag.ACCOUNT_TYPE,
					uaccUniqueId, uaccPrincipal, IPrincipal.DEFAULT_NAMESPACE,
                    AbstractUserAccount.VALID_TO, companyId));
            attributeList.add(generateLayoutAttribute(IPrincipalDatabag.ACCOUNT_TYPE,
					uaccUniqueId, uaccPrincipal, IPrincipal.DEFAULT_NAMESPACE,
                    ILoginConstants.LOGON_PWD_ALIAS, companyId));
			attributeList.add(generateLayoutAttribute(IPrincipalDatabag.ACCOUNT_TYPE,
					uaccUniqueId, uaccPrincipal, IPrincipal.DEFAULT_NAMESPACE,
					AbstractUserAccount.IS_PASSWORD_DISABLED, companyId));
			attributeList.add(generateLayoutAttribute(IPrincipalDatabag.ACCOUNT_TYPE,
					uaccUniqueId, uaccPrincipal, IPrincipal.DEFAULT_NAMESPACE,
					ILoginConstants.LOGON_ALIAS, companyId));
			attributeList.add(generateLayoutAttribute(IPrincipalDatabag.ACCOUNT_TYPE,
					uaccUniqueId, uaccPrincipal, IPrincipal.DEFAULT_NAMESPACE,
					ILoginConstants.LOGON_CERT_ALIAS, companyId));
			attributeList.add(generateLayoutAttribute(IPrincipalDatabag.ACCOUNT_TYPE,
					uaccUniqueId, uaccPrincipal, IPrincipal.DEFAULT_NAMESPACE,
					AbstractUserAccount.SECURITY_POLICY, companyId));					
			attributeList.add(generateLayoutAttribute(IPrincipalDatabag.ACCOUNT_TYPE,
					uaccUniqueId, uaccPrincipal, IPrincipal.DEFAULT_NAMESPACE,
					AbstractUserAccount.SECURITYANSWER, companyId));
			attributeList.add(generateLayoutAttribute(IPrincipalDatabag.ACCOUNT_TYPE,
					uaccUniqueId, uaccPrincipal, IPrincipal.DEFAULT_NAMESPACE,
					AbstractUserAccount.SECURITYQUESTION, companyId));
			attributeList.add(generateLayoutAttribute(IPrincipalDatabag.ACCOUNT_TYPE,
					uaccUniqueId, uaccPrincipal, IPrincipal.DEFAULT_NAMESPACE,
					AbstractUserAccount.SECURITYQUESTIONPREDEFINED, companyId));
        }
        //continue with normal uniqueID and private Type
        if (dataPopulation != null && dataPopulation.length > 0){
        	for (int i = 0; i < dataPopulation.length; i++){
        		if (dataPopulation[i] != null){
        			JmxAttribute addAttr = new JmxAttribute(dataPopulation[i]);
					attributeList.add(generateLayoutAttribute(privateType,
							uniqueId, dummyPrincipal, addAttr.getNamespace(),
							addAttr.getName(), companyId));        			
        		}
        	}
        }
        returnEntity.setAttributes(attributeList);
        return returnEntity;
    }

    private static JmxAttribute generateLayoutAttribute(String principalType,
            String uniqueId, IPrincipal principal, String namespace, String name, String companyId)
            throws OpenDataException, UMException {
        final String mn = "private static JmxAttribute generateLayoutAttribute(String principalType, String uniqueId, String namespace, String name, String companyId)";
        JmxAttribute attr = new JmxAttribute();
        attr.setNamespace(namespace);
        attr.setName(name);
        boolean modifyable = false;
        if (uniqueId == null || uniqueId.equals(CompanyPrincipalFactory.EMPTY)) {
            modifyable = CompanyPrincipalFactory.getInstance(companyId)
                    .isPrincipalAttributeCreateable(principalType,
                            principal, attr.getNamespace(), attr.getName());
        } else {
            modifyable = CompanyPrincipalFactory.getInstance(companyId)
                    .isPrincipalAttributeModifiable(principalType, uniqueId,
                            attr.getNamespace(), attr.getName());
        }
        attr.setModifyable(modifyable);
		if (myLoc.beDebug()) {
			myLoc.debugT(mn, 
				new Object[]{principalType, uniqueId, namespace, name, companyId, new Boolean(modifyable)});
		}
        return attr;
    }

}
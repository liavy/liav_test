package com.sap.security.core.jmx.impl;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.OpenDataException;

import com.sap.security.api.AttributeList;
import com.sap.security.api.IGroup;
import com.sap.security.api.IGroupFactory;
import com.sap.security.api.IPrincipal;
import com.sap.security.api.IPrincipalFactory;
import com.sap.security.api.IPrincipalSearchFilter;
import com.sap.security.api.IPrincipalSet;
import com.sap.security.api.IRole;
import com.sap.security.api.ISearchAttribute;
import com.sap.security.api.ISearchResult;
import com.sap.security.api.IUser;
import com.sap.security.api.IUserAccount;
import com.sap.security.api.IUserAccountSearchFilter;
import com.sap.security.api.IUserSearchFilter;
import com.sap.security.api.NoAccessPermissionException;
import com.sap.security.api.NoSuchPrincipalException;
import com.sap.security.api.PrincipalIterator;
import com.sap.security.api.PrincipalNotAccessibleException;
import com.sap.security.api.UMException;
import com.sap.security.api.UMFactory;
import com.sap.security.api.UMRuntimeException;
import com.sap.security.api.logon.ILoginConstants;
import com.sap.security.core.imp.AbstractUserAccount;
import com.sap.security.core.imp.PrincipalIteratorImpl;
import com.sap.security.core.imp.Role;
import com.sap.security.core.imp.User;
import com.sap.security.core.jmx.IJmxAttribute;
import com.sap.security.core.jmx.IJmxEntity;
import com.sap.security.core.jmx.IJmxMessage;
import com.sap.security.core.jmx.IJmxState;
import com.sap.security.core.jmx.IJmxTable;
import com.sap.security.core.persistence.IPrincipalDatabag;
import com.sap.security.core.persistence.imp.PersistenceCollection;
import com.sap.security.core.persistence.imp.PrincipalDatabag;
import com.sap.security.core.util.Base64;
import com.sap.security.core.util.imp.Util;
import com.sap.security.core.util.resources.CoreMessageBean;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

class JmxSearchHelper {

	private static final String WILDCARD = "*";

    static final String USERACCOUNTID = "useraccountid";
    
	static final String DELETEABLE = "deleteable";

	static final String SEPARATOR = " | ";

    private static final Location myLoc = Location.getLocation(JmxSearchHelper.class);
	
    private static ISearchResult getSimpleEntitySearchResult(String searchString,
			String type, String[] datasourceIds,
			CompositeData[] searchAttributes, int i, boolean b,
			String companyId, String guid, IJmxState jmxState, List<IJmxMessage> additionalMessages) throws OpenDataException, UMException {
    	ISearchResult searchResult = SearchResultCache.getSearchResult(guid);
        if (searchResult != null) {
            return searchResult;
        }
        searchResult = CompanyPrincipalFactory.getInstance(
				companyId).simplePrincipalSearchByDatasources(searchString,
				type, datasourceIds, i, b, searchAttributes, jmxState.getLocale(), additionalMessages);
        SearchResultCache.addSearchResult(guid, searchResult);
        return searchResult;
    }

    /**
     * @param requestMainDetailAttributes
     * @param requestMainType
     * @param requestAdditionalDetailAttributes
     * @param requestAdditionalType
     * @param mainDatasourceIds
     * @param additionalDatasourceIds
     * @param showUnapprovedUsers
     * @param companyId
     * @param guid
     * @return
     * @throws OpenDataException
     * @throws UMException
     */
    private static ISearchResult getEntitySearchResult(
			IJmxAttribute[] requestMainDetailAttributes,
			String requestMainType,
			IJmxAttribute[] requestAdditionalDetailAttributes,
			String requestAdditionalType, String[] mainDatasourceIds,
			String[] additionalDatasourceIds, boolean showUnapprovedUsers,
			String companyId, String guid, IJmxState jmxState, List<IJmxMessage> additionalMessages) throws OpenDataException,
			UMException {
        ISearchResult searchResult = SearchResultCache.getSearchResult(guid);
        if (searchResult != null) {
            return searchResult;
        }
        if ((requestMainType != null && CompanyPrincipalFactory
				.getPrivatePrincipalTypeIdentifier(requestMainType).equals(
						IPrincipalDatabag.USER_TYPE))
				&& (requestAdditionalType != null && CompanyPrincipalFactory
						.getPrivatePrincipalTypeIdentifier(
								requestAdditionalType).equals(
								IPrincipalDatabag.ACCOUNT_TYPE))) {
            IUserSearchFilter mainsf = UMFactory.getUserFactory()
                    .getUserSearchFilter();
            IUserAccountSearchFilter additionalsf = UMFactory
                    .getUserAccountFactory().getUserAccountSearchFilter();
            //		check if we have here the initialy created attributes list
            if (requestMainDetailAttributes.length == 1
                    && requestMainDetailAttributes[0].getName().equals(CompanyPrincipalFactory.EMPTY)) {
            } else {
                for (int i = 0; i < requestMainDetailAttributes.length; i++) {
                    String value = requestMainDetailAttributes[i].getValue();
                    if (value == null) {
                        //do not check for null
                    } else {
                        mainsf.setSearchAttribute(
                                requestMainDetailAttributes[i].getNamespace(),
                                requestMainDetailAttributes[i].getName(),
                                requestMainDetailAttributes[i].getValue(),
                                requestMainDetailAttributes[i].getOperator(),
                                requestMainDetailAttributes[i]
                                        .getCaseSensitive());
                    }
                }
            }
            if (mainDatasourceIds != null
					&& mainDatasourceIds.length > 0
					&& mainDatasourceIds[0] != null
					&& !mainDatasourceIds[0]
							.equals(CompanyPrincipalFactory.EMPTY)) {
            	Set<String> datasources = CompanyPrincipalFactory
						.evaluateDatasourcesToSearchFor(
								mainDatasourceIds,
								CompanyPrincipalFactory
										.getPrivatePrincipalTypeIdentifier(requestMainType),
								jmxState.getLocale(), additionalMessages);
            	if (datasources != null){
            		for (Iterator it = datasources.iterator(); it.hasNext();){
            			String id = (String)it.next();
            			mainsf.setSearchAttribute(IPrincipal.DEFAULT_NAMESPACE,
								IPrincipal.DATASOURCE, id,
								ISearchAttribute.EQUALS_OPERATOR, false);
            		}
            	}
            }
            ArrayList<IJmxAttribute> requestAdditionalDetailNullAttributesList = new ArrayList<IJmxAttribute>();
            //		check if we have here the initialy created attributes list
            if (requestAdditionalDetailAttributes.length == 1
                    && requestAdditionalDetailAttributes[0].getName().equals(
                            CompanyPrincipalFactory.EMPTY)) {
            } else {
                for (int i = 0; i < requestAdditionalDetailAttributes.length; i++) {
                    String value = requestAdditionalDetailAttributes[i]
                            .getValue();
                    if (value == null) {
                        requestAdditionalDetailNullAttributesList
                                .add(requestMainDetailAttributes[i]);
                    } else {
                        additionalsf
                                .setSearchAttribute(
                                        requestAdditionalDetailAttributes[i]
                                                .getNamespace(),
                                        requestAdditionalDetailAttributes[i]
                                                .getName(),
                                        requestAdditionalDetailAttributes[i]
                                                .getValue(),
                                        requestAdditionalDetailAttributes[i]
                                                .getOperator(),
                                        requestAdditionalDetailAttributes[i]
                                                .getCaseSensitive());
                    }
                }
            }
            String[] datasourceIds = null;
            if (additionalDatasourceIds != null
					&& additionalDatasourceIds.length > 0
					&& additionalDatasourceIds[0] != null
					&& !additionalDatasourceIds[0]
							.equals(CompanyPrincipalFactory.EMPTY)) {
            	datasourceIds = additionalDatasourceIds;
            } else {
                if (mainDatasourceIds != null
    					&& mainDatasourceIds.length > 0
    					&& mainDatasourceIds[0] != null
    					&& !mainDatasourceIds[0]
    							.equals(CompanyPrincipalFactory.EMPTY)) {
                	datasourceIds = mainDatasourceIds;
                }
            }
            if (datasourceIds != null) {
            	Set<String> datasources = CompanyPrincipalFactory
						.evaluateDatasourcesToSearchFor(
								datasourceIds,
								CompanyPrincipalFactory
										.getPrivatePrincipalTypeIdentifier(requestAdditionalType),
								jmxState.getLocale(), additionalMessages);
            	if (datasources != null){
            		for (Iterator it = datasources.iterator(); it.hasNext();){
            			String id = (String)it.next();
            			additionalsf.setSearchAttribute(IPrincipal.DEFAULT_NAMESPACE,
								IPrincipal.DATASOURCE, id,
								ISearchAttribute.EQUALS_OPERATOR, false);
            		}
            	}
            }            
            if (showUnapprovedUsers) {
                searchResult = CompanyPrincipalFactory.getInstance(
                        companyId).searchUnapprovedUsers(mainsf, additionalsf);
            } else {
                searchResult = CompanyPrincipalFactory.getInstance(
                        companyId).searchUsers(mainsf, additionalsf);
            }
            SearchResultCache.addSearchResult(guid, searchResult);
            return searchResult;
        } else {
        	/* no combined search
        	 */
            IPrincipalSearchFilter sf = UMFactory.getPrincipalFactory()
					.getPrincipalSearchFilter(false, requestMainType);
            //check if we have here the initialy created attributes list
            if (requestMainDetailAttributes.length == 1
                    && requestMainDetailAttributes[0].getName().equals(CompanyPrincipalFactory.EMPTY)) {
            } else {
                for (int i = 0; i < requestMainDetailAttributes.length; i++) {
                    String value = requestMainDetailAttributes[i].getValue();
                    if (value == null) {
                        //do not search for null values
                    } else {
                        sf.setSearchAttribute(requestMainDetailAttributes[i]
                                .getNamespace(), requestMainDetailAttributes[i]
                                .getName(), requestMainDetailAttributes[i]
                                .getValue(), requestMainDetailAttributes[i]
                                .getOperator(), requestMainDetailAttributes[i]
                                .getCaseSensitive());
                    }
                }
            }
            if (mainDatasourceIds != null
					&& mainDatasourceIds.length > 0
					&& mainDatasourceIds[0] != null
					&& !mainDatasourceIds[0]
							.equals(CompanyPrincipalFactory.EMPTY)) {
            	Set<String> datasources = CompanyPrincipalFactory
						.evaluateDatasourcesToSearchFor(
								mainDatasourceIds,
								CompanyPrincipalFactory
										.getPrivatePrincipalTypeIdentifier(requestMainType),
								jmxState.getLocale(), additionalMessages);
            	if (datasources != null){
            		for (Iterator it = datasources.iterator(); it.hasNext();){
            			String id = (String)it.next();
            			sf.setSearchAttribute(IPrincipal.DEFAULT_NAMESPACE,
								IPrincipal.DATASOURCE, id,
								ISearchAttribute.EQUALS_OPERATOR, false);
            		}
            	}
            }
            searchResult = CompanyPrincipalFactory.getInstance(
                    companyId).searchPrincipals(sf);
            SearchResultCache.addSearchResult(guid, searchResult);
            return searchResult;	
        }
    }

    /**
     * @param dataDetail
     * @param recursiveSearch
     * @param guid
     * @return
     */
    private static ISearchResult getEntityMemberSearchResult(
			String uniqueId, String searchString, String memberType,
			String[] datasourceIds, CompositeData[] searchAttributes,
			boolean recursiveSearch, String companyId, String guid, IJmxState jmxState, List<IJmxMessage> additionalMessages)
			throws OpenDataException, UMException {
        ISearchResult searchResult = SearchResultCache.getSearchResult(guid);
        if (searchResult != null) {
            return searchResult;
        }
        IPrincipal principal = CompanyPrincipalFactory.getInstance(companyId)
                .getPrincipal(uniqueId);
        if (principal instanceof IPrincipalSet) {
            //Member result handling with type checking start
            IPrincipalSet principalSet = (IPrincipalSet) principal;
            PrincipalIteratorImpl memberResult = (PrincipalIteratorImpl) principalSet
                    .getMembers(recursiveSearch);
			if (memberResult.size() == 0) {
				//special check for everyone group, which is not returning the size
				if (!uniqueId.equals(IGroupFactory.EVERYONE_UNIQUEID)) {
				SearchResultCache.addSearchResult(guid, memberResult);
				return memberResult;
			}
			}
			if (searchString == null || searchString.trim().equals(CompanyPrincipalFactory.EMPTY)) {
				searchString = WILDCARD;
			}
			if (searchString.equals(WILDCARD)) {
				memberResult =
					CompanyPrincipalFactory.getInstance(
						companyId).getTypedPrincipalIterator(
						memberResult,
						memberType,
						datasourceIds,
						jmxState.getLocale());
				SearchResultCache.addSearchResult(guid, memberResult);
				return memberResult;
			}
			//Member result handling with type checking end
            //Simple search start
            PrincipalIteratorImpl simpleSearchResult = (PrincipalIteratorImpl) CompanyPrincipalFactory
					.getInstance(companyId).simplePrincipalSearchByDatasources(
							searchString, memberType, datasourceIds,
							ISearchAttribute.LIKE_OPERATOR, false, searchAttributes, jmxState.getLocale(), additionalMessages);
            if (simpleSearchResult.size() == 0) {
            	SearchResultCache.addSearchResult(guid, simpleSearchResult);
                return simpleSearchResult;
            }
            //					Simple search end
            //					Intersection evaluation start
            int state = simpleSearchResult.getState();
            PersistenceCollection intersectionCollection = new PersistenceCollection();
            if (memberResult.size() < simpleSearchResult.size()) {
                while (memberResult.hasNext()) {
                    Object member = memberResult.next();
                    if (simpleSearchResult.contains(member)) {
                        intersectionCollection.add(member);
                    }
                }
            } else {
                while (simpleSearchResult.hasNext()) {
                    Object member = simpleSearchResult.next();
                    if (memberResult.contains(member)) {
                        intersectionCollection.add(member);
                    }
                }
            }
            memberResult = new PrincipalIteratorImpl(intersectionCollection, PrincipalIterator.ITERATOR_TYPE_UNIQUEIDS_NOT_CHECKED);
            memberResult.setState(state);
            SearchResultCache.addSearchResult(guid, memberResult);
            return memberResult;
            //					Intersection evaluation end
        }
        throw new UMException("Principal is not a PrincipalSet");
    }

    /**
     * @param uniqueId
     * @param searchString
     * @param parentType
     * @param requestPopulationAttributes
     * @param recursiveSearch
     * @param guid
     * @return
     */
    private static ISearchResult getEntityParentSearchResult(
			String uniqueId, String searchString, String parentType,
			String[] datasourceIds, CompositeData[] searchAttributes,
			boolean recursiveSearch, String companyId, String guid, IJmxState jmxState, List<IJmxMessage> additionalMessages)
			throws OpenDataException, UMException {
    	ISearchResult searchResult = SearchResultCache.getSearchResult(guid);
        if (searchResult != null) {
            return searchResult;
        }
        //Get Parents wit type start
        IPrincipal principal = CompanyPrincipalFactory.getInstance(companyId)
                .getPrincipal(uniqueId);
        PrincipalIteratorImpl parentResult = (PrincipalIteratorImpl) principal
                .getParents(new String[] { parentType }, recursiveSearch);//TODO
        if (parentResult.size() == 0) {
        	SearchResultCache.addSearchResult(guid, parentResult);
            return parentResult;
        }
        //If search for all parents was performed, populate all principals and perform direct check
        if (searchString == null || searchString.trim().equals(CompanyPrincipalFactory.EMPTY)){//TODO
        	searchString = WILDCARD;
        }
        if (WILDCARD.equals(searchString.trim()) ){
			parentResult = CompanyPrincipalFactory.getInstance(
				companyId).getTypedPrincipalIterator(
				parentResult,
				parentType,
				datasourceIds,
				jmxState.getLocale());
        	SearchResultCache.addSearchResult(guid, parentResult);
			return parentResult;
        } else {
			PrincipalIteratorImpl simpleSearchResult = (PrincipalIteratorImpl) CompanyPrincipalFactory
					.getInstance(companyId).simplePrincipalSearchByDatasources(
							searchString, parentType, datasourceIds,
							ISearchAttribute.LIKE_OPERATOR, false, searchAttributes, jmxState.getLocale(), additionalMessages);
			if (simpleSearchResult.size() == 0) {
	        	SearchResultCache.addSearchResult(guid, simpleSearchResult);
				return simpleSearchResult;
			}
			//Simple search end
			//Intersection evaluation start
			int state = simpleSearchResult.getState();
			PersistenceCollection intersectionCollection = new PersistenceCollection();
			if (parentResult.size() < simpleSearchResult.size()) {
				while (parentResult.hasNext()) {
					Object parent = parentResult.next();
					if (simpleSearchResult.contains(parent)) {
						intersectionCollection.add(parent);
					}
				}
			} else {
				while (simpleSearchResult.hasNext()) {
					Object parent = simpleSearchResult.next();
					if (parentResult.contains(parent)) {
						intersectionCollection.add(parent);
					}
				}
			}
			parentResult = new PrincipalIteratorImpl(intersectionCollection, PrincipalIterator.ITERATOR_TYPE_UNIQUEIDS_NOT_CHECKED);
			parentResult.setState(state);
			SearchResultCache.addSearchResult(guid, parentResult);
			return parentResult;
			//Intersection evaluation end        	
        }
    }

    /**
     * @param requestPopulationAttributes
     * @return
     */
    private static AttributeList generateAttributeList(
            IJmxAttribute[] requestPopulationAttributes) {
        AttributeList al = new AttributeList(false);
        if (requestPopulationAttributes.length == 1
                && requestPopulationAttributes[0].getName().equals(CompanyPrincipalFactory.EMPTY)) {
        } else {
            for (int i = 0; i < requestPopulationAttributes.length; i++) {
                al
                        .addAttribute(requestPopulationAttributes[i]
                                .getNamespace(), requestPopulationAttributes[i]
                                .getName(), (requestPopulationAttributes[i]
                                .getBinary() == true ? AttributeList.TYPE_BLOB
                                : AttributeList.TYPE_STRING));
            }
        }
        return al;
    }

    /**
     * @param list
     * @param requestPopulationAttributes
     * @param principalType
     * @return
     */
	private static JmxTable getJmxTable(
		PrincipalIterator principalIt,
		IJmxAttribute[] requestPopulationAttributes,
		String principalType,
		String companyId,
		IJmxState state)
		throws OpenDataException, UMException {
		final String mn = "static JmxTable getJmxTable(" +
			"PrincipalIterator principalIt, IJmxAttribute[] requestPopulationAttributes, String principalType, String companyId)";
        //not checking principal type for null!
		String textInconsistentEntity = CoreMessageBean.getInstance(
				state.getLocale()).get(CoreMessageBean.INCONSISTENT_ENTITY);
        JmxTable table = new JmxTable();
        List<JmxTableRow> tableRowList = new ArrayList<JmxTableRow>();
        Set<String> notTranslateableDatasourceIds = new HashSet<String>();
		Map<String, String> translateableDatasourceIds = new HashMap<String, String>();
		
        /*
		 * populate principal with all attributes needed in the table (and not only default ones)
		 * to avoid lazy reads
		 */
    	AttributeList aList = new AttributeList(false);
    	if (requestPopulationAttributes != null){
			for (int i = 0; i < requestPopulationAttributes.length; i++) {
				if (requestPopulationAttributes[i] == null) {
					continue;
				}
				if (IPrincipal
					.DEFAULT_NAMESPACE
					.equals(requestPopulationAttributes[i].getNamespace())
					&& IPrincipal.DATASOURCE.equals(
						requestPopulationAttributes[i].getName())) {
					continue;
				}
				aList.addAttribute(requestPopulationAttributes[i].getNamespace(),
						requestPopulationAttributes[i].getName(),
						AttributeList.TYPE_STRING);
			}        		
    	}
    	if (IPrincipalFactory.IUSER.equals(principalType)
				|| IPrincipalDatabag.USER_TYPE.equals(principalType)) {
			aList.addAttribute(IPrincipal.DEFAULT_NAMESPACE, User.FIRSTNAME,
					AttributeList.TYPE_STRING);
			aList.addAttribute(IPrincipal.DEFAULT_NAMESPACE, User.LASTNAME,
					AttributeList.TYPE_STRING);
		}
		//ACL check on roles: add attribute for ACL namespace and uniqueID of current user
		if (IPrincipalFactory.IROLE.equals(principalType)
				|| IPrincipalDatabag.ROLE_TYPE.equals(principalType)) {
			aList.addAttribute(CompanyPrincipalFactory.NAMESPACE_ACL, UMFactory.getAuthenticator().getLoggedInUser().getUniqueID());
		}
    	
		Set<String> principalIdsSet = new HashSet<String>();
		while (principalIt.hasNext()) {
			principalIdsSet.add((String) principalIt.next());
		}
		String[] principalIdsArray = (String[]) principalIdsSet
				.toArray(new String[principalIdsSet.size()]);
    	IPrincipal[] principalsArray;
		String[] principalsErrorTextArray = new String[principalIdsArray.length];
		try {
			principalsArray = UMFactory.getPrincipalFactory().getPrincipals(
					principalIdsArray, aList);
		} catch (Exception e1) {
			if (myLoc.beInfo()) {
				myLoc.traceThrowableT(Severity.INFO, mn, e1);
			}
			principalsArray = new IPrincipal[principalIdsArray.length];
			for (int i = 0; i < principalIdsArray.length; i++) {
				try {
					principalsArray[i] = UMFactory.getPrincipalFactory()
							.getPrincipal(principalIdsArray[i], aList);
					/*
					 * in case a PCD or producer role is populated with acl check,
					 * a javax.naming.NoPermissionException will be catched and 
					 * rethrown as PrincipalNotAccessibleException in the GLRolePersistence.
					 * In order not to display the error in the table, flag the principalsErrorTextArray
					 * accordingly with text of NoAccessPermissionException.NO_ACCESS_PERMISSION_EXCEPTION_MESSAGE.
					 */
				} catch (NoAccessPermissionException e2){
					principalsErrorTextArray[i] = NoAccessPermissionException.NO_ACCESS_PERMISSION_EXCEPTION_MESSAGE;
					if (myLoc.beInfo()){
						myLoc.traceThrowableT(Severity.INFO, mn, "Not displaying principal {0} because of missing permissions", new Object[]{principalIdsArray[i]}, e2);
					}
				} catch (NoSuchPrincipalException e2) {
					principalsErrorTextArray[i] = JmxUtils.convertExceptionMessageToString(e2, state.getLocale());
					if (myLoc.beInfo()){
						myLoc.traceThrowableT(Severity.INFO, mn, "Displaying principal {0} as inconsistent", new Object[]{principalIdsArray[i]}, e2);
					}
				} catch (PrincipalNotAccessibleException e2) {
					/*
					 * Fallback if not NoAccessPermissionException was thrown,
					 * but a PrincipalNotAccessibleException with the corresponding message text
					 */
					if (NoAccessPermissionException.NO_ACCESS_PERMISSION_EXCEPTION_MESSAGE.equals(e2.getMessage())){
						principalsErrorTextArray[i] = NoAccessPermissionException.NO_ACCESS_PERMISSION_EXCEPTION_MESSAGE;						
						if (myLoc.beInfo()){
							myLoc.traceThrowableT(Severity.INFO, mn, "Not displaying principal {0} because of missing permissions", new Object[]{principalIdsArray[i]}, e2);
						}
					} else {
					principalsErrorTextArray[i] = JmxUtils.convertExceptionMessageToString(e2, state.getLocale());
					if (myLoc.beInfo()){
							myLoc.traceThrowableT(Severity.INFO, mn, "Displaying principal {0} as inconsistent", new Object[]{principalIdsArray[i]}, e2);
						}
					}
				}
			}
		}
		
    	for (int i = 0; i < principalsArray.length; i++) {
        	String uniqueId = principalIdsArray[i];
            JmxTableRow tableRow = new JmxTableRow();
            IPrincipal principal = null;
			// set unique id
			tableRow.setColUniqueId(uniqueId);
			principal = principalsArray[i];
            if (principal == null) {
            	if (principalsErrorTextArray[i] != null){
					if (principalsErrorTextArray[i].equals(NoAccessPermissionException.NO_ACCESS_PERMISSION_EXCEPTION_MESSAGE)){
						//do not display error in case of "NoAccessPermissionException"
						continue;
					}
    				tableRow.setCol0(uniqueId + SEPARATOR + principalsErrorTextArray[i]);
            	} else {
    				tableRow.setCol0(uniqueId + SEPARATOR + textInconsistentEntity);            		
            	}
				tableRowList.add(tableRow);
				continue;
			}
			try {
            if (principalType == null) {
//					should never occur, as we only search for one type at a time
                String type = CompanyPrincipalFactory.getInstance(companyId)
                        .getPrincipalType(uniqueId);
                if (type == null) {
					if (myLoc.beInfo()){
						myLoc.infoT(mn, "Principal type for principalId {0} is null", new Object[]{uniqueId});
					}
					tableRow.setCol0(
						uniqueId + 
						SEPARATOR + 
						textInconsistentEntity);
					tableRowList.add(tableRow);
					continue;
                }
                //set principal type if not available
                tableRow.setColType(type);
            } else {
                //set principal type if already available
                tableRow.setColType(principalType);
            }
            String uniqueNameReplacement = null;
            if (tableRow.getColType().equals(PrincipalDatabag.USER_TYPE)
                    || tableRow.getColType().equals(
                            CompanyPrincipalFactory.IUSER)) {
                IUser user = (IUser) principal;
                IUserAccount[] userAccounts = user.getUserAccounts();
                if (userAccounts == null) {
					if (myLoc.beInfo()){
						myLoc.infoT(mn, "UserId {0} has no user accounts", new Object[]{uniqueId});
					}
					tableRow.setCol0(
						uniqueId + 
						SEPARATOR + 
						textInconsistentEntity);
					tableRowList.add(tableRow);
					continue;
                }
                if (userAccounts.length > 0) {
                    if (userAccounts[0] == null) {
						if (myLoc.beInfo()){
							myLoc.infoT(mn, "UserId {0} has no user account", new Object[]{uniqueId});
						}
						tableRow.setCol0(
							uniqueId + 
							SEPARATOR + 
							textInconsistentEntity);
						tableRowList.add(tableRow);
						continue;
                    }
                    String refUniqueId = userAccounts[0].getUniqueID();
                    if (refUniqueId == null) {
						if (myLoc.beInfo()){
							myLoc.infoT(mn, "UserAccount {0} of UserId {1} has no uniqueId", new Object[]{userAccounts[0], uniqueId});
						}
						tableRow.setCol0(
							uniqueId + 
							SEPARATOR + 
							textInconsistentEntity);
						tableRowList.add(tableRow);
						continue;
                    }
                    //if type is user, set also the uacc unique id
                    tableRow.setColRefUniqueId(refUniqueId);
                    tableRow.setColStatus0(userAccounts[0].isUserAccountLocked());
                    uniqueNameReplacement = userAccounts[0].getLogonUid();
                } else {
					if (myLoc.beInfo()){
						myLoc.infoT(mn, "UserId {0} has {1} user accounts", new Object[]{uniqueId, new Integer(userAccounts.length)});
					}
					tableRow.setCol0(
						uniqueId + 
						SEPARATOR + 
						textInconsistentEntity);
					tableRowList.add(tableRow);
					continue;
                }
			} else if (
				tableRow.getColType().equals(PrincipalDatabag.ROLE_TYPE)
					|| tableRow.getColType().equals(
						CompanyPrincipalFactory.IROLE)) {
				String[] isRemotePcdRole = principal.getAttribute(Role.DEFAULT_NAMESPACE, Role.IS_REMOTE_PCD_ROLE);
				if (isRemotePcdRole != null && isRemotePcdRole.length > 0 && CompanyPrincipalFactory.TRUE.equalsIgnoreCase(isRemotePcdRole[0])){
					String[] remotePcdName = principal.getAttribute(Role.DEFAULT_NAMESPACE, Role.REMOTE_PCD_NAME);
					if (remotePcdName != null && remotePcdName.length > 0){
						uniqueNameReplacement = remotePcdName[0];
					}
				}
            }
            //set principal deleteable state
            tableRow.setColDeleteable(CompanyPrincipalFactory.getInstance(
                    companyId).isPrincipalDeletable(uniqueId));
            //set principal attributes
            for (int j = 0; j < requestPopulationAttributes.length; j++) {
                String type = principal.getAttributeType(
                        requestPopulationAttributes[j].getNamespace(),
                        requestPopulationAttributes[j].getName());
                if (type == null) {
                    tableRow.setTableRowValue(j, CompanyPrincipalFactory.EMPTY);
                } else if (type.equals(IPrincipal.STRING_TYPE)) {
                    String[] values = principal.getAttribute(
                            requestPopulationAttributes[j].getNamespace(),
                            requestPopulationAttributes[j].getName());
                    if (values != null && values.length > 0
                            && values[0] != null) {
                        //datasource translation
						if (IPrincipal.DATASOURCE.equals(requestPopulationAttributes[j].getName())){
							if (!notTranslateableDatasourceIds.contains(values[0])){
								String ds = (String)translateableDatasourceIds.get(values[0]);
								if (ds == null){
									ds = CompanyPrincipalFactory.localizeDatasourceId(values[0], state);
									if (ds == null){
										notTranslateableDatasourceIds.add(values[0]);
									} else {
										translateableDatasourceIds.put(values[0], ds);
										values[0] = ds;
									}
								} else {
									values[0] = ds;
								}
							}
						} else if (IPrincipal.UNIQUE_NAME.equals(requestPopulationAttributes[j].getName())){
							if (uniqueNameReplacement != null){
								values[0] = uniqueNameReplacement;
							}
						}
                        tableRow.setTableRowValue(j, values[0]);
                    } else {
                        tableRow.setTableRowValue(j, CompanyPrincipalFactory.EMPTY);
                    }
                } else {
                    tableRow.setTableRowValue(j, CompanyPrincipalFactory.EMPTY);
                }
            }
            tableRowList.add(tableRow);
	        /*
	         * Possibly, following exceptions are also able to occur after populating the principal
	         * Therefore, try/catch block again
	         */
			} catch (NoSuchPrincipalException e3) {
				String error = JmxUtils.convertExceptionMessageToString(e3, state.getLocale());
				if (error != null){
					tableRow.setCol0(uniqueId + SEPARATOR + error);
				} else {
					tableRow.setCol0(uniqueId + SEPARATOR + textInconsistentEntity);            		
				}
				if (myLoc.beInfo()){
					myLoc.traceThrowableT(Severity.INFO, mn, e3);
				}
				tableRowList.add(tableRow);
			} catch (PrincipalNotAccessibleException e3) {
				String error = JmxUtils.convertExceptionMessageToString(e3, state.getLocale());
				if (error != null){
					tableRow.setCol0(uniqueId + SEPARATOR + error);
				} else {
					tableRow.setCol0(uniqueId + SEPARATOR + textInconsistentEntity);            		
				}
				if (myLoc.beInfo()){
					myLoc.traceThrowableT(Severity.INFO, mn, e3);
				}
				tableRowList.add(tableRow);
			}
		}
        table.setTableRows(tableRowList);
        return table;
    }

    /**
     * @param requestPopulationAttributes
     * @param principal
     * @return jmxAttributeList or null
     * @throws OpenDataException
     * @throws UMException
     */
	private static ArrayList getJmxAttributeListWithKnownPopulationAttributes(
            IJmxAttribute[] requestPopulationAttributes, IPrincipal principal,
            String companyId) throws OpenDataException, UMException {
        if (requestPopulationAttributes.length == 1
                && requestPopulationAttributes[0].getName().equals(CompanyPrincipalFactory.EMPTY)) {
        } else {
            ArrayList<IJmxAttribute> jmxAttributeList = new ArrayList<IJmxAttribute>(
                    requestPopulationAttributes.length);
            for (int i = 0; i < requestPopulationAttributes.length; i++) {
                IJmxAttribute[] attr = getJmxAttributes(principal,
                        requestPopulationAttributes[i].getNamespace(),
                        requestPopulationAttributes[i].getName(), companyId);
                if (attr != null && attr.length > 0) {
                    if (attr.length == 1 && attr[0] != null) {
                        jmxAttributeList.add(attr[0]);
                    } else {
                        for (int k = 0; k < attr.length; k++) {
                            jmxAttributeList.add(attr[k]);
                        }
                    }
                }
            }
            return jmxAttributeList;
        }
        return null;
    }

    /**
     * @param principal
     * @return jmxAttrbuteList
     * @throws OpenDataException
     * @throws UMException
     */
	private static ArrayList getJmxAttributeListWithAllAttributes(IPrincipal principal, String companyId, IJmxState state)
            throws OpenDataException, UMException {
		final String mn = "static ArrayList getJmxAttributeList(IPrincipal principal, String companyId, IJmxState state)";
        ArrayList<IJmxAttribute> jmxAttributeList = new ArrayList<IJmxAttribute>();
        String[] namespaces = principal.getAttributeNamespaces();
        if (namespaces == null) {
            return jmxAttributeList;
        }
        
        /*
		 * populate principal with all attributes (not only default ones)
		 * to avoid lazy reads
		 */
        AttributeList aList = new AttributeList(false);
        aList.addAttribute(IPrincipal.DEFAULT_NAMESPACE, IPrincipal.DISPLAYNAME, AttributeList.TYPE_STRING);

        if (principal instanceof IUser){
			if (User.USER_DEFAULT_ATTRIBUTES != null) {
				int size = User.USER_DEFAULT_ATTRIBUTES.getSize();
				for (int i = 0; i < size; i++) {
					aList.addAttribute(
						User
							.USER_DEFAULT_ATTRIBUTES
							.getNameSpaceOfAttributeAt(
							i),
						User
							.USER_DEFAULT_ATTRIBUTES
							.getAttributeNameOfAttributeAt(
							i),
						User
							.USER_DEFAULT_ATTRIBUTES
							.getAttributeTypeOfAttributeAt(
							i));
				}
			}
        	aList.addAttribute(IPrincipal.DEFAULT_NAMESPACE, User.UNLOCKPERSON_OLD, AttributeList.TYPE_STRING);
			aList.addAttribute(IPrincipal.DEFAULT_NAMESPACE, User.UNLOCKMESSAGE_OLD);
			aList.addAttribute(IPrincipal.DEFAULT_NAMESPACE, User.UNLOCKDATE_OLD);
        	aList.addAttribute(IPrincipal.DEFAULT_NAMESPACE, User.LOCKPERSON_OLD, AttributeList.TYPE_STRING);
			aList.addAttribute(IPrincipal.DEFAULT_NAMESPACE, User.LOCKMESSAGE_OLD);
			aList.addAttribute(IPrincipal.DEFAULT_NAMESPACE, User.LOCKREASON_OLD);
        	aList.addAttribute(IPrincipal.DEFAULT_NAMESPACE, User.COMPANY, AttributeList.TYPE_STRING);
			aList.addAttribute(IPrincipal.DEFAULT_NAMESPACE, User.PRIVACY_STATEMENT_LINK, AttributeList.TYPE_STRING);
			aList.addAttribute(IPrincipal.DEFAULT_NAMESPACE, User.PRIVACY_STATEMENT_VERSION, AttributeList.TYPE_STRING);
			aList.addAttribute(IPrincipal.DEFAULT_NAMESPACE, User.PRIVACY_STATEMENT_TIMESTAMP, AttributeList.TYPE_STRING);
        }
        if (principal instanceof IUserAccount){
			if (AbstractUserAccount.ABSTRACT_USER_ACCOUNT_DEFAULT_ATTRIBUTES != null) {
				int size = AbstractUserAccount.ABSTRACT_USER_ACCOUNT_DEFAULT_ATTRIBUTES.getSize();
				for (int i = 0; i < size; i++) {
					aList.addAttribute(
						AbstractUserAccount
							.ABSTRACT_USER_ACCOUNT_DEFAULT_ATTRIBUTES
							.getNameSpaceOfAttributeAt(
							i),
						AbstractUserAccount
							.ABSTRACT_USER_ACCOUNT_DEFAULT_ATTRIBUTES
							.getAttributeNameOfAttributeAt(
							i),
						AbstractUserAccount
							.ABSTRACT_USER_ACCOUNT_DEFAULT_ATTRIBUTES
							.getAttributeTypeOfAttributeAt(
							i));
				}
			}
        	aList.addAttribute(IPrincipal.DEFAULT_NAMESPACE, ILoginConstants.LOGON_CERT_ALIAS, AttributeList.TYPE_BLOB);
        	aList.addAttribute(IPrincipal.DEFAULT_NAMESPACE, ILoginConstants.LOGON_ALIAS, AttributeList.TYPE_STRING);
        	aList.addAttribute(IPrincipal.DEFAULT_NAMESPACE, AbstractUserAccount.VALID_TO, AttributeList.TYPE_STRING); 
			aList.addAttribute(IPrincipal.DEFAULT_NAMESPACE, AbstractUserAccount.LOCK_DATE, AttributeList.TYPE_STRING);
			aList.addAttribute(IPrincipal.DEFAULT_NAMESPACE, AbstractUserAccount.LOCK_PERSON, AttributeList.TYPE_STRING);
			aList.addAttribute(IPrincipal.DEFAULT_NAMESPACE, AbstractUserAccount.LOCK_REASON, AttributeList.TYPE_STRING);
			aList.addAttribute(IPrincipal.DEFAULT_NAMESPACE, AbstractUserAccount.LOCK_TEXT, AttributeList.TYPE_STRING);
			aList.addAttribute(IPrincipal.DEFAULT_NAMESPACE, AbstractUserAccount.UNLOCK_DATE, AttributeList.TYPE_STRING);
			aList.addAttribute(IPrincipal.DEFAULT_NAMESPACE, AbstractUserAccount.UNLOCK_PERSON, AttributeList.TYPE_STRING);
			aList.addAttribute(IPrincipal.DEFAULT_NAMESPACE, AbstractUserAccount.UNLOCK_TEXT, AttributeList.TYPE_STRING);
			aList.addAttribute(IPrincipal.DEFAULT_NAMESPACE, AbstractUserAccount.SECURITYANSWER, AttributeList.TYPE_STRING);
			aList.addAttribute(IPrincipal.DEFAULT_NAMESPACE, AbstractUserAccount.SECURITYQUESTION, AttributeList.TYPE_STRING);
			aList.addAttribute(IPrincipal.DEFAULT_NAMESPACE, AbstractUserAccount.SECURITYQUESTIONPREDEFINED, AttributeList.TYPE_STRING);
			aList.addAttribute(IPrincipal.DEFAULT_NAMESPACE, AbstractUserAccount.PASSWORD_CHANGE_REQUIRED, AttributeList.TYPE_STRING);
			aList.addAttribute(IPrincipal.DEFAULT_NAMESPACE, AbstractUserAccount.IS_PASSWORD_DISABLED, AttributeList.TYPE_STRING);
        }
        /*
         * getJmxAttributeList will not be called for already available IRole and IGroup objects,
         * where member attributes could be available. This method will only be called for newly created IRole and IGroup objects.
         */
        if (principal instanceof IRole){
        	aList.addAttribute(IPrincipal.DEFAULT_NAMESPACE, IPrincipal.DESCRIPTION, AttributeList.TYPE_STRING); 
        }
        if (principal instanceof IGroup){
        	aList.addAttribute(IPrincipal.DEFAULT_NAMESPACE, IPrincipal.DESCRIPTION, AttributeList.TYPE_STRING); 
        }

        final int MAX_SIZE = 300;
        for (int i = 0; i < namespaces.length && aList.getSize()<MAX_SIZE ; i++) {
            if (namespaces[i] == null) {
                continue;
            }
            String[] attributeNames = principal
                    .getAttributeNames(namespaces[i]);
            if (attributeNames == null) {
                continue;
            }
            for (int j = 0; j < attributeNames.length && aList.getSize()<MAX_SIZE; j++) {
                if (attributeNames[j] == null) {
                    continue;
                }
                aList.addAttribute(namespaces[i], attributeNames[j], AttributeList.TYPE_UNKNOWN);
            }
        }

		try {
			principal = CompanyPrincipalFactory.getInstance(companyId)
				.getPrincipal(principal.getUniqueID(), aList);
		} catch (UMException e) {
			if (myLoc.beInfo()){
				myLoc.traceThrowableT(Severity.INFO, mn, e);
			}
		}

		/*
		 * now getting all attribute values
		 */
		for (int i = 0; i < namespaces.length; i++) {
            if (namespaces[i] == null) {
                continue;
            }
            String[] attributeNames = principal
                    .getAttributeNames(namespaces[i]);
            if (attributeNames == null) {
                continue;
            }
            for (int j = 0; j < attributeNames.length; j++) {
                if (attributeNames[j] == null) {
                    continue;
                }                
                IJmxAttribute[] attr = getJmxAttributes(principal,
                        namespaces[i], attributeNames[j], companyId);
                if (attr != null && attr.length > 0) {
                    if (attr.length == 1 && attr[0] != null) {
                        jmxAttributeList.add(attr[0]);
                    } else {
                        for (int k = 0; k < attr.length; k++) {
                            jmxAttributeList.add(attr[k]);
                        }
                    }
                }
            }
        }
        String type = CompanyPrincipalFactory.getInstance(companyId)
                .getPrincipalType(principal.getUniqueID());
		JmxAttribute jmxAttribute;
        if (type.equals(CompanyPrincipalFactory.IUSER)) {
            IUser user = (IUser) principal;
            IUserAccount[] accounts = user.getUserAccounts();
            if (accounts != null && accounts.length > 0 && accounts[0] != null) {
                String useraccountid = accounts[0].getUniqueID();
                if (useraccountid != null) {
                    jmxAttribute = new JmxAttribute(
                    	IPrincipal.DEFAULT_NAMESPACE,
                    	USERACCOUNTID,
                    	useraccountid);
                    jmxAttributeList.add(jmxAttribute);
                }
            }
            String[] unlockPerson = user.getAttribute(IPrincipal.DEFAULT_NAMESPACE, User.UNLOCKPERSON_OLD);
            if (unlockPerson != null && unlockPerson.length > 0 && unlockPerson[0] != null){
				try {
					String displayName = UMFactory.getUserFactory().getUser(unlockPerson[0]).getDisplayName();
					jmxAttribute = new JmxAttribute(
						IPrincipal.DEFAULT_NAMESPACE,
						User.UNLOCKPERSON_OLD,
						displayName);
					jmxAttributeList.add(jmxAttribute);
				} catch (UMException e) {
					if (myLoc.beInfo()){
						myLoc.infoT(
							mn,
							"Not able to evaluate displayname for user {0} {1}",
							new Object[] {
								User.UNLOCKPERSON_OLD,
								unlockPerson[0],
								e });
					}
					jmxAttribute = new JmxAttribute(
						IPrincipal.DEFAULT_NAMESPACE,
						User.UNLOCKPERSON_OLD,
						unlockPerson[0]);
					jmxAttributeList.add(jmxAttribute);
				} catch (UMRuntimeException e) {
					if (myLoc.beInfo()){
						myLoc.infoT(
							mn,
							"Not able to evaluate user {0} {1}",
							new Object[] {
								User.UNLOCKPERSON_OLD,
								unlockPerson[0],
								e });
					}
					jmxAttribute = new JmxAttribute(
						IPrincipal.DEFAULT_NAMESPACE,
						User.UNLOCKPERSON_OLD,
						unlockPerson[0]);
					jmxAttributeList.add(jmxAttribute);
				}
            }
			String[] lockPerson = user.getAttribute(IPrincipal.DEFAULT_NAMESPACE, User.LOCKPERSON_OLD);
			if (lockPerson != null && lockPerson.length > 0 && lockPerson[0] != null){
				try {
					String displayName = UMFactory.getUserFactory().getUser(lockPerson[0]).getDisplayName();
					jmxAttribute = new JmxAttribute(
						IPrincipal.DEFAULT_NAMESPACE,
						User.LOCKPERSON_OLD,
						displayName);
					jmxAttributeList.add(jmxAttribute);
				} catch (UMException e) {
					if (myLoc.beInfo()){
						myLoc.infoT(
							mn,
							"Not able to evaluate displayname for user {0} {1}",
							new Object[] {
								User.LOCKPERSON_OLD,
								lockPerson[0],
								e });
					}
					jmxAttribute = new JmxAttribute(
						IPrincipal.DEFAULT_NAMESPACE,
						User.LOCKPERSON_OLD,
						lockPerson[0]);
					jmxAttributeList.add(jmxAttribute);
				} catch (UMRuntimeException e) {
					if (myLoc.beInfo()){
						myLoc.infoT(
							mn,
							"Not able to evaluate user {0} {1}",
							new Object[] {
								User.LOCKPERSON_OLD,
								lockPerson[0],
								e });
					}
					jmxAttribute = new JmxAttribute(
						IPrincipal.DEFAULT_NAMESPACE,
						User.LOCKPERSON_OLD,
						lockPerson[0]);
					jmxAttributeList.add(jmxAttribute);
				}
			}
        }
		if (type.equals(CompanyPrincipalFactory.IUSERACCOUNT)) {
			IUserAccount userAccount = (IUserAccount) principal;
			String[] alias = principal.getAttribute(IPrincipal.DEFAULT_NAMESPACE, ILoginConstants.LOGON_ALIAS);
			if (alias != null && alias.length > 0 && alias[0] != null){
				jmxAttribute = new JmxAttribute(
					IPrincipal.DEFAULT_NAMESPACE,
					ILoginConstants.LOGON_ALIAS,
					alias[0]);
				jmxAttributeList.add(jmxAttribute);
			}
			Date date = userAccount.getValidToDate();
			if (date != null){
				jmxAttribute = new JmxAttribute(
					IPrincipal.DEFAULT_NAMESPACE,
					AbstractUserAccount.VALID_TO,
					Util.getTime( date.getTime() ));
				jmxAttributeList.add(jmxAttribute);
			}
			String[] values = userAccount.getAttribute(IPrincipal.DEFAULT_NAMESPACE, AbstractUserAccount.SECURITYANSWER);
			if (values != null && values.length > 0 && values[0] != null) {
				jmxAttribute =
					new JmxAttribute(
						IPrincipal.DEFAULT_NAMESPACE,
						AbstractUserAccount.SECURITYANSWER,
						CompanyPrincipalFactory.PASSWORD_STARS);
				jmxAttributeList.add(jmxAttribute);
			}
		}
		if (type.equals(CompanyPrincipalFactory.IROLE)) {
			IRole role = (IRole) principal;
			String description = role.getDescription();
			if (description != null){
				jmxAttribute = new JmxAttribute(
					IPrincipal.DEFAULT_NAMESPACE,
					IPrincipal.DESCRIPTION,
					description);
				jmxAttributeList.add(jmxAttribute);	
			}
		}
		if (type.equals(CompanyPrincipalFactory.IGROUP)) {
			IGroup group = (IGroup) principal;
			String description = group.getDescription();
			if (description != null){
				jmxAttribute = new JmxAttribute(
					IPrincipal.DEFAULT_NAMESPACE,
					IPrincipal.DESCRIPTION,
					description);
				jmxAttributeList.add(jmxAttribute);	
			}
		}
		String displayName = principal.getDisplayName();
		if (displayName != null){
			jmxAttribute = new JmxAttribute(
				IPrincipal.DEFAULT_NAMESPACE,
				IPrincipal.DISPLAYNAME,
				displayName);
			jmxAttributeList.add(jmxAttribute);
		}
		String[] dataSource = principal.getAttribute(IPrincipal.DEFAULT_NAMESPACE, IPrincipal.DATASOURCE);
		if (dataSource != null && dataSource.length > 0 && dataSource[0] != null){
			String ds = CompanyPrincipalFactory.localizeDatasourceId(dataSource[0], state);
			if (ds != null){
				dataSource[0] = ds;
			}
			jmxAttribute = new JmxAttribute(
				IPrincipal.DEFAULT_NAMESPACE,
				IPrincipal.DATASOURCE,
				dataSource[0]);
			jmxAttributeList.add(jmxAttribute);	
		}
		boolean deleteable = CompanyPrincipalFactory.getInstance(
			companyId).isPrincipalDeletable(principal.getUniqueID());
			jmxAttribute = new JmxAttribute(
				IPrincipal.DEFAULT_NAMESPACE,
				DELETEABLE,
				(new Boolean(deleteable)).toString());
			jmxAttributeList.add(jmxAttribute);
        String company = CompanyPrincipalFactory.getCompanyFromPrincpal(principal.getUniqueID());
        if (company != null){
			jmxAttribute = new JmxAttribute(
				IPrincipal.DEFAULT_NAMESPACE,
				User.COMPANY,
				company);
			jmxAttributeList.add(jmxAttribute);        	
        }
		return jmxAttributeList;
    }

    private static IJmxAttribute[] getJmxAttributes(IPrincipal principal,
            String namespace, String attributeName, String companyId)
            throws OpenDataException, UMException {
        String attributeType = principal.getAttributeType(namespace,
                attributeName);
        IJmxAttribute[] resultAttributes = null;
        if (attributeType != null) {
            if (attributeType.equals(IPrincipal.STRING_TYPE)) {
                String[] values = principal.getAttribute(namespace,
                        attributeName);
                if (values == null ){
                    resultAttributes = new IJmxAttribute[1];
                    JmxAttribute jmxAttribute = new JmxAttribute(namespace, attributeName, CompanyPrincipalFactory.EMPTY);
                    resultAttributes[0] = jmxAttribute;                    
                } else if (values.length > 0) {
                    if (values.length == 1 && values[0] != null) {
                        resultAttributes = new IJmxAttribute[1];
                        JmxAttribute jmxAttribute = new JmxAttribute(namespace, attributeName, values[0]);
                        resultAttributes[0] = jmxAttribute;
                    } else {
						resultAttributes = createJmxStringAttributes(values,
								namespace, attributeName);                    	
                    }
                }
            } else if (attributeType.equals(IPrincipal.BYTE_TYPE)) {
                if (attributeName.equals(ILoginConstants.LOGON_CERT_ALIAS)) {
                    if (CompanyPrincipalFactory.getInstance(companyId)
                            .getPrincipalType(principal.getUniqueID()).equals(
                                    CompanyPrincipalFactory.IUSERACCOUNT)) {
                        X509Certificate[] x509certs;
                        try {
                            x509certs = ((IUserAccount) principal)
                                    .getCertificates();
                            String[] stringCert = new String[x509certs.length];
                            if (x509certs != null && x509certs.length > 0) {
                                for (int i = 0; i < x509certs.length; i++) {
                                    if (x509certs[i] != null) {
                                        byte[] value = x509certs[i]
                                                .getEncoded();
                                        stringCert[i] = Base64.encode(value);
                                    }
                                }
                            }
                            resultAttributes = createJmxStringAttributes(
                                    stringCert, namespace, attributeName);
                        } catch (CertificateException ce) {
                            throw new UMException(ce);
                        }
                    }
                } else {
                    resultAttributes = new IJmxAttribute[1];
                    byte[] values = principal.getBinaryAttribute(namespace,
                            attributeName);
                    if (values != null) {
                        JmxAttribute jmxAttribute = new JmxAttribute();
                        jmxAttribute.setNamespace(namespace);
                        jmxAttribute.setName(attributeName);
                        jmxAttribute.setBinaryValue(Base64.encode(values));
                        jmxAttribute.setBinary(true);
                        resultAttributes[0] = jmxAttribute;
                    }
                }
            }
        }
        return resultAttributes;
    }

    private static IJmxAttribute[] createJmxStringAttributes(String[] values,
            String namespace, String attributeName) throws OpenDataException {
        List<String> attrList = new ArrayList<String>(values.length);
        for (int i = 0; i < values.length; i++) {
            if (values[i] != null) {
                attrList.add(values[i]);
            }
        }
        String[] notNullValues = new String[attrList.size()];
        int n = 0;
        for (Iterator it = attrList.iterator(); it.hasNext(); n++) {
            notNullValues[n] = (String) it.next();
        }
        IJmxAttribute[] resultAttributes = new IJmxAttribute[attrList.size()];
        int i = 0;
        for (Iterator it = attrList.iterator(); it.hasNext(); i++) {
            JmxAttribute jmxAttribute = new JmxAttribute();
            jmxAttribute.setNamespace(namespace);
            jmxAttribute.setName(attributeName);
            jmxAttribute.setValue((String) it.next());
            jmxAttribute.setValues(notNullValues);
            jmxAttribute.setBinary(false);
            resultAttributes[i] = jmxAttribute;
        }
        return resultAttributes;
    }

    /**
     * @param searchString
     * @param type
     * @param companyId
     * @param guid
     * @return
     * @throws OpenDataException
     * @throws UMException
     */
    static IJmxTable calculateSimpleEntityTable(String searchString,
			String type, String[] datasourceIds,
			CompositeData[] searchAttributes, String companyId, String guid, IJmxState jmxState)
			throws OpenDataException, UMException {
		if (type.equals(JmxActionFactoryWrapper.IACTION)) {
			return JmxActionFactoryWrapper.calculateSimpleActionTable(searchString,
					companyId, guid);
		}
		List<IJmxMessage> additionalMessages = new ArrayList<IJmxMessage>();
		ISearchResult sr = getSimpleEntitySearchResult(searchString, type,
				datasourceIds, searchAttributes,
				ISearchAttribute.LIKE_OPERATOR, false, companyId, guid, jmxState, additionalMessages);

		JmxTable result = new JmxTable();
		result.setSize(sr.size());
		result.setState(sr.getState());
		result.setGuid(guid);
		result.setTableRows(new JmxTableRow[0]);
		result.setMessages(JmxUtils.convertISearchResultMessagesToIJmxMessageList(sr, jmxState.getLocale(), additionalMessages));
		return result;

    }

    /**
     * @param searchString
     * @param type
     * @param dataPopulation
     * @param companyId
     * @param guid
     * @return
     * @throws OpenDataException
     * @throws UMException
     */
    static IJmxTable getSimpleEntityTable(String searchString, String type,
			String[] datasourceIds, CompositeData[] searchAttributes,
			CompositeData[] dataPopulation, String companyId, String guid,
			IJmxState state) throws OpenDataException, UMException {
		IJmxAttribute[] requestPopulationAttributes = JmxAttribute.generateJmxAttributes(dataPopulation);
    	if (type.equals(JmxActionFactoryWrapper.IACTION)) {
			return JmxActionFactoryWrapper.getSimpleActionTable(searchString,
				companyId, guid, requestPopulationAttributes, state.getLocale());
		}
    	List<IJmxMessage> additionalMessages = new ArrayList<IJmxMessage>();
		ISearchResult sr = JmxSearchHelper.getSimpleEntitySearchResult(
				searchString, type, datasourceIds, searchAttributes,
				ISearchAttribute.LIKE_OPERATOR, false, companyId, guid, state, additionalMessages);
		AttributeList attributeList = JmxSearchHelper
				.generateAttributeList(requestPopulationAttributes);
		PrincipalIterator principalIt = new PrincipalIterator(sr,
				PrincipalIterator.ITERATOR_TYPE_UNIQUEIDS_NOT_CHECKED, attributeList);

		//generate table result
		JmxTable returnTable =
			JmxSearchHelper.getJmxTable(
				principalIt,
				requestPopulationAttributes,
				type,
				companyId,
				state);
		returnTable.setSize(sr.size());
		returnTable.setState(sr.getState());
		returnTable.setGuid(guid);
		returnTable.setMessages(JmxUtils.convertISearchResultMessagesToIJmxMessageList(sr, state.getLocale(), additionalMessages));
		return returnTable;
    }

    /**
     * @param dataMainDetail
     * @param dataAdditionalDetail
     * @param mainDatasourceIds
     * @param additionalDatasourceIds
     * @param showUnapprovedUsers
     * @param companyId
     * @param guid
     * @return
     * @throws OpenDataException
     * @throws UMException
     */
    static IJmxTable calculateEntityTable(CompositeData dataMainDetail,
			CompositeData dataAdditionalDetail, String[] mainDatasourceIds,
			String[] additionalDatasourceIds, boolean showUnapprovedUsers,
			String companyId, String guid, IJmxState jmxState) throws OpenDataException,
			UMException {
		IJmxEntity requestMainDetail = new JmxEntity(dataMainDetail);
		IJmxAttribute[] requestMainDetailAttributes = requestMainDetail
				.getAttributes();
		String requestMainType = requestMainDetail.getType();
		if (requestMainType.equals(JmxActionFactoryWrapper.ACTION_TYPE)) {
			return JmxActionFactoryWrapper.calculateActionTable(
					requestMainDetailAttributes, companyId, guid);
		}
		IJmxEntity requestAdditionalDetail = null;
		IJmxAttribute[] requestAdditionalDetailAttributes = null;
		String requestAdditionalType = null;
		if (dataAdditionalDetail != null){
			requestAdditionalDetail = new JmxEntity(dataAdditionalDetail);
			requestAdditionalDetailAttributes = requestAdditionalDetail
					.getAttributes();
			requestAdditionalType = requestAdditionalDetail.getType();			
		}
		List<IJmxMessage> additionalMessages = new ArrayList<IJmxMessage>();
		ISearchResult sr = JmxSearchHelper.getEntitySearchResult(
				requestMainDetailAttributes, requestMainType,
				requestAdditionalDetailAttributes, requestAdditionalType,
				mainDatasourceIds, additionalDatasourceIds, showUnapprovedUsers,
				companyId, guid, jmxState, additionalMessages);
		JmxTable result = new JmxTable();
		result.setSize(sr.size());
		result.setState(sr.getState());
		result.setGuid(guid);
		result.setTableRows(new JmxTableRow[0]);
		result.setMessages(JmxUtils.convertISearchResultMessagesToIJmxMessageList(sr, jmxState.getLocale(), additionalMessages));
		return result;
    }

    /**
     * @param dataMainDetail
     * @param dataAdditionalDetail
     * @param mainDataPopulation
     * @param additionalDataPopulation
     * @param mainDatasourceIds
     * @param additionalDatasourceIds
     * @param showUnapprovedUsers
     * @param companyId
     * @param guid
     * @param state
     * @return
     * @throws OpenDataException
     * @throws UMException
     */
    static IJmxTable getEntityTable(CompositeData dataMainDetail,
			CompositeData dataAdditionalDetail,
			CompositeData[] mainDataPopulation,
			CompositeData[] additionalDataPopulation,
			String[] mainDatasourceIds, String[] additionalDatasourceIds,
			boolean showUnapprovedUsers, String companyId, String guid,
			IJmxState state) throws OpenDataException, UMException {
		IJmxEntity requestMainDetail = new JmxEntity(dataMainDetail);
		IJmxAttribute[] requestMainDetailAttributes = requestMainDetail
				.getAttributes();
		String requestMainType = requestMainDetail.getType();
		IJmxAttribute[] requestPopulationAttributes = JmxAttribute
			.generateJmxAttributes(mainDataPopulation);
		if (requestMainType.equals(JmxActionFactoryWrapper.ACTION_TYPE)) {
			return JmxActionFactoryWrapper.getActionTable(
					requestMainDetailAttributes, companyId, guid,
					requestPopulationAttributes, state.getLocale());
		}
		IJmxEntity requestAdditionalDetail = null;
		IJmxAttribute[] requestAdditionalDetailAttributes = null;
		String requestAdditionalType = null;
		if (dataAdditionalDetail != null){
			requestAdditionalDetail = new JmxEntity(dataAdditionalDetail);
			requestAdditionalDetailAttributes = requestAdditionalDetail
					.getAttributes();
			requestAdditionalType = requestAdditionalDetail.getType();			
		}
		List<IJmxMessage> additionalMessages = new ArrayList<IJmxMessage>();
		ISearchResult sr = JmxSearchHelper.getEntitySearchResult(
				requestMainDetailAttributes, requestMainType,
				requestAdditionalDetailAttributes, requestAdditionalType,
				mainDatasourceIds, additionalDatasourceIds, showUnapprovedUsers,
				companyId, guid, state, additionalMessages);
		AttributeList attributeList = JmxSearchHelper
				.generateAttributeList(requestPopulationAttributes);
		PrincipalIterator principalIt = new PrincipalIterator(sr,
				PrincipalIterator.ITERATOR_TYPE_UNIQUEIDS_NOT_CHECKED, attributeList);
		JmxTable returnTable = JmxSearchHelper.getJmxTable(principalIt,
				requestPopulationAttributes, requestMainType, companyId, state);
		returnTable.setSize(sr.size());
		returnTable.setState(sr.getState());
		returnTable.setGuid(guid);
		returnTable.setMessages(JmxUtils.convertISearchResultMessagesToIJmxMessageList(sr, state.getLocale(), additionalMessages));
		return returnTable;
    }

    /**
     * @param uniqueId
     * @param searchString
     * @param memberType
     * @param recursiveSearch
     * @param companyId
     * @param guid
     * @return
     * @throws UMException
     * @throws OpenDataException
     */
    static IJmxTable calculateEntityMemberTable(String uniqueId,
			String searchString, String memberType, String[] datasourceIds,
			CompositeData[] searchAttributes, boolean recursiveSearch,
			String companyId, String guid, IJmxState jmxState) throws OpenDataException,
			UMException {
		if (memberType.equals(JmxActionFactoryWrapper.ACTION_TYPE)
				&& CompanyPrincipalFactory.getInstance(companyId)
						.getPrincipalType(uniqueId).equals(
								CompanyPrincipalFactory.IROLE)) {
			return JmxActionFactoryWrapper.calculateActionMemberTable(uniqueId, searchString,
					companyId, guid);
		}
		List<IJmxMessage> additionalMessages = new ArrayList<IJmxMessage>();
		ISearchResult tempPrincipalIt = JmxSearchHelper
				.getEntityMemberSearchResult(uniqueId, searchString,
						memberType, datasourceIds, searchAttributes,
						recursiveSearch, companyId, guid, jmxState, additionalMessages);
		JmxTable result = new JmxTable();
		result.setSize(tempPrincipalIt.size());
		result.setState(tempPrincipalIt.getState());
		result.setGuid(guid);
		result.setTableRows(new JmxTableRow[0]);
		result.setMessages(JmxUtils.convertISearchResultMessagesToIJmxMessageList(tempPrincipalIt, jmxState.getLocale(), additionalMessages));
		return result;

    }

    /**
     * @param uniqueId
     * @param searchString
     * @param memberType
     * @param dataPopulation
     * @param recursiveSearch
     * @param companyId
     * @param guid
     * @return
     * @throws UMException
     * @throws OpenDataException
     */
    static IJmxTable getEntityMemberTable(String uniqueId, String searchString,
			String memberType, String[] datasourceIds,
			CompositeData[] searchAttributes, CompositeData[] dataPopulation,
			boolean recursiveSearch, String companyId, String guid,
			IJmxState state) throws OpenDataException, UMException {
		IJmxAttribute[] requestPopulationAttributes = JmxAttribute.generateJmxAttributes(dataPopulation);
    	if (memberType.equals(JmxActionFactoryWrapper.ACTION_TYPE)
				&& CompanyPrincipalFactory.getInstance(companyId)
						.getPrincipalType(uniqueId).equals(
								CompanyPrincipalFactory.IROLE)) {
			return JmxActionFactoryWrapper.getActionMemberTable(uniqueId, searchString,
					companyId, guid, requestPopulationAttributes, state.getLocale());
		}
		AttributeList attributeList = JmxSearchHelper
				.generateAttributeList(requestPopulationAttributes);
		List<IJmxMessage> additionalMessages = new ArrayList<IJmxMessage>();
		ISearchResult tempPrincipalIt = JmxSearchHelper
				.getEntityMemberSearchResult(uniqueId, searchString,
						memberType, datasourceIds, searchAttributes,
						recursiveSearch, companyId, guid, state, additionalMessages);
		PrincipalIterator principalIt = new PrincipalIterator(tempPrincipalIt,
				PrincipalIterator.ITERATOR_TYPE_UNIQUEIDS_NOT_CHECKED, attributeList);
		//generate table result
		JmxTable returnTable =
			JmxSearchHelper.getJmxTable(
				principalIt,
				requestPopulationAttributes,
				null,
				companyId,
				state);
		returnTable.setSize(tempPrincipalIt.size());
		returnTable.setState(tempPrincipalIt.getState());
		returnTable.setGuid(guid);
		returnTable.setMessages(JmxUtils.convertISearchResultMessagesToIJmxMessageList(tempPrincipalIt, state.getLocale(), additionalMessages));
		return returnTable;

    }

    /**
     * @param uniqueId
     * @param searchString
     * @param parentType
     * @param recursiveSearch
     * @param companyId
     * @param guid
     * @return
     * @throws OpenDataException
     * @throws UMException
     */
    static IJmxTable calculateEntityParentTable(String uniqueId,
			String searchString, String parentType, String[] datasourceIds,
			CompositeData[] searchAttributes, boolean recursiveSearch,
			String companyId, String guid, IJmxState jmxState) throws OpenDataException,
			UMException {
		if (parentType.equals(IPrincipalDatabag.ROLE_TYPE)
				&& CompanyPrincipalFactory.getInstance(companyId)
						.getPrincipalType(uniqueId).equals(
								JmxActionFactoryWrapper.IACTION)) {
			return JmxActionFactoryWrapper.calculateActionParentTable(uniqueId,
					searchString, recursiveSearch, companyId, guid);
		}
		List<IJmxMessage> additionalMessages = new ArrayList<IJmxMessage>();
		ISearchResult tempPrincipalIt = JmxSearchHelper
				.getEntityParentSearchResult(uniqueId, searchString,
						parentType, datasourceIds, searchAttributes,
						recursiveSearch, companyId, guid, jmxState, additionalMessages);
		JmxTable result = new JmxTable();
		result.setSize(tempPrincipalIt.size());
		result.setState(tempPrincipalIt.getState());
		result.setGuid(guid);
		result.setTableRows(new JmxTableRow[0]);
		result.setMessages(JmxUtils.convertISearchResultMessagesToIJmxMessageList(tempPrincipalIt, jmxState.getLocale(), additionalMessages));
		return result;
    }

    /**
     * @param uniqueId
     * @param searchString
     * @param parentType
     * @param dataPopulation
     * @param recursiveSearch
     * @param companyId
     * @param guid
     * @return
     * @throws UMException
     * @throws OpenDataException
     */
    static IJmxTable getEntityParentTable(String uniqueId, String searchString,
			String parentType, String[] datasourceIds,
			CompositeData[] searchAttributes, CompositeData[] dataPopulation,
			boolean recursiveSearch, String companyId, String guid,
			IJmxState state) throws OpenDataException, UMException {
		IJmxAttribute[] requestPopulationAttributes = JmxAttribute.generateJmxAttributes(dataPopulation);
		if (parentType.equals(IPrincipalDatabag.ROLE_TYPE)
				&& CompanyPrincipalFactory.getInstance(companyId)
						.getPrincipalType(uniqueId).equals(
								JmxActionFactoryWrapper.IACTION)) {
			return JmxActionFactoryWrapper.getActionParentTable(uniqueId,
					searchString, requestPopulationAttributes, recursiveSearch,
					companyId, guid);
		}
		AttributeList attributeList = JmxSearchHelper
				.generateAttributeList(requestPopulationAttributes);
		List<IJmxMessage> additionalMessages = new ArrayList<IJmxMessage>();
		ISearchResult tempPrincipalIt = JmxSearchHelper
				.getEntityParentSearchResult(uniqueId, searchString,
						parentType, datasourceIds, searchAttributes,
						recursiveSearch, companyId, guid, state, additionalMessages);
		PrincipalIterator principalIt = new PrincipalIterator(tempPrincipalIt,
				PrincipalIterator.ITERATOR_TYPE_UNIQUEIDS_NOT_CHECKED, attributeList);
		//generate table result
		JmxTable returnTable =
			JmxSearchHelper.getJmxTable(
				principalIt,
				requestPopulationAttributes,
				null,
				companyId,
				state);
		returnTable.setSize(tempPrincipalIt.size());
		returnTable.setState(tempPrincipalIt.getState());
		returnTable.setGuid(guid);
		returnTable.setMessages(JmxUtils.convertISearchResultMessagesToIJmxMessageList(tempPrincipalIt, state.getLocale(), additionalMessages));
		return returnTable;
    }

    /**
     * @param uniqueId
     * @param companyId
     * @return
     * @throws UMException
     * @throws OpenDataException
     */
    static IJmxEntity getAllEntityDetails(String uniqueId, String companyId, IJmxState state) throws OpenDataException, UMException {
		CompanyPrincipalFactory.getInstance(companyId).invalidatePrincipalInCache(uniqueId);
		JmxEntity responseJmxEntity = new JmxEntity();
		IPrincipal principal = CompanyPrincipalFactory.getInstance(companyId)
				.getPrincipal(uniqueId);
		String responseUniqueId = principal.getUniqueID();
		responseJmxEntity.setUniqueId(responseUniqueId);
		responseJmxEntity.setType(CompanyPrincipalFactory
				.getInstance(companyId).getPrincipalType(uniqueId));
		responseJmxEntity.setModifyable(CompanyPrincipalFactory.getInstance(
				companyId).isPrincipalModifiable(uniqueId));
		ArrayList attrList = JmxSearchHelper.getJmxAttributeListWithAllAttributes(
				principal,
				companyId,
				state);
		responseJmxEntity.setAttributes(attrList);
		responseJmxEntity.setMessages(JmxUtils.convertIPrincipalMessagesToIJmxMessageList(principal, state.getLocale()));
		return responseJmxEntity;
    }

    /**
     * @param uniqueId
     * @param dataPopulation
     * @param companyId
     * @param jmxState
     * @return
     * @throws OpenDataException
     * @throws UMException
     */
    static IJmxEntity getEntityDetails(String uniqueId,
			CompositeData[] dataPopulation, String companyId, IJmxState jmxState)
			throws OpenDataException, UMException {
    	CompanyPrincipalFactory.getInstance(companyId).invalidatePrincipalInCache(uniqueId);
		IJmxAttribute[] requestPopulationAttributes = JmxAttribute.generateJmxAttributes(dataPopulation);
		AttributeList populateAttributeList = JmxSearchHelper
				.generateAttributeList(requestPopulationAttributes);
		IPrincipal principal;
		if (JmxActionFactoryWrapper.isDisplayableAction(uniqueId)) {
			return JmxActionFactoryWrapper.getActionDetails(uniqueId,
					requestPopulationAttributes, jmxState);
		} else {
			principal = CompanyPrincipalFactory.getInstance(companyId)
					.getPrincipal(uniqueId, populateAttributeList);
		}
		JmxEntity responseJmxEntity = new JmxEntity();
		responseJmxEntity.setUniqueId(principal.getUniqueID());
		responseJmxEntity.setType(CompanyPrincipalFactory
				.getInstance(companyId).getPrincipalType(uniqueId));
		responseJmxEntity.setModifyable(CompanyPrincipalFactory.getInstance(
				companyId).isPrincipalModifiable(uniqueId));
		ArrayList attrList = JmxSearchHelper.getJmxAttributeListWithKnownPopulationAttributes(
				requestPopulationAttributes, principal, companyId);
		responseJmxEntity.setAttributes(attrList);
		responseJmxEntity.setMessages(JmxUtils.convertIPrincipalMessagesToIJmxMessageList(principal, jmxState.getLocale()));
		return responseJmxEntity;
    }

}
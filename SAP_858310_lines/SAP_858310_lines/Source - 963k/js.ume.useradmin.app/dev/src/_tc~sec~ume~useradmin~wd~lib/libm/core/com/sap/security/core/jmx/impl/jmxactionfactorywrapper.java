package com.sap.security.core.jmx.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import javax.management.openmbean.OpenDataException;

import com.sap.security.api.AttributeList;
import com.sap.security.api.IPrincipal;
import com.sap.security.api.IRole;
import com.sap.security.api.ISearchAttribute;
import com.sap.security.api.ISearchResult;
import com.sap.security.api.PrincipalIterator;
import com.sap.security.api.UMException;
import com.sap.security.core.InternalUMFactory;
import com.sap.security.core.imp.PrincipalFactory;
import com.sap.security.core.imp.PrincipalIteratorImpl;
import com.sap.security.core.jmx.IJmxAttribute;
import com.sap.security.core.jmx.IJmxEntity;
import com.sap.security.core.jmx.IJmxState;
import com.sap.security.core.jmx.IJmxTable;
import com.sap.security.core.persistence.IPersistenceCollection;
import com.sap.security.core.persistence.imp.PersistenceCollection;
import com.sap.security.core.persistence.imp.SearchCriteria;
import com.sap.security.core.role.ActionException;
import com.sap.security.core.role.IAction;
import com.sap.security.core.role.IActionFactory;
import com.sap.security.core.role.NoSuchActionException;
import com.sap.security.core.role.imp.Action;
import com.sap.security.core.role.imp.ActionFactory;
import com.sap.security.core.role.imp.PermissionRoles;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

public class JmxActionFactoryWrapper {

	static Location myLoc = Location.getLocation(JmxActionFactoryWrapper.class);
	
	final static String ACTION_TYPE = IActionFactory.ACTION_TYPE;
	
	final static String IACTION = IActionFactory.IACTION;
	
	static boolean isAction(String uniqueId){
		if (uniqueId.startsWith(ACTION_TYPE)){
			return true;
		}
		return false;
	}

	static boolean isDisplayableAction(String uniqueId){
		return isAction(uniqueId);
	}
	
	private static ISearchResult getActionMembers(IRole role, String searchString, String guid){
		ISearchResult searchResult = SearchResultCache.getSearchResult(guid);
		if (searchResult != null){
			return searchResult;
		}		
		IPersistenceCollection coll = new PersistenceCollection();
		for (Iterator it = PermissionRoles.getActions(role); it.hasNext();){
			coll.add(((IAction)it.next()).getUniqueID());
		}
		AttributeList aList = new AttributeList();
		aList.addAttribute(IPrincipal.DEFAULT_NAMESPACE, IPrincipal.UNIQUE_NAME);
		PrincipalIteratorImpl tempPrincipalIt = new PrincipalIteratorImpl(
				coll, PrincipalIteratorImpl.TYPE_UNIQUEIDS_NOT_CHECKED);
		coll = new PersistenceCollection();
		for (Iterator it = new PrincipalIterator(tempPrincipalIt,
				PrincipalIteratorImpl.TYPE_UNIQUEIDS_NOT_CHECKED, aList,
				searchString, ActionFactory.IACTION,
				ISearchAttribute.LIKE_OPERATOR, false); it.hasNext();){
			coll.add((String)it.next());
		}
		searchResult = new PrincipalIteratorImpl(coll,
				PrincipalIteratorImpl.TYPE_UNIQUEIDS_NOT_CHECKED);
		SearchResultCache.addSearchResult(guid, searchResult);
		return searchResult;
	}

	/**
	 * @param searchString
	 * @return Returns a Set of acions
	 * @throws UMException
	 */
	private static ISearchResult simpleActionSearch(String searchString, String guid) throws UMException{
		ISearchResult searchResult = SearchResultCache.getSearchResult(guid);
		if (searchResult != null){
			return searchResult;
		}
		int op = CompanyPrincipalFactory.getSearchOperator(searchString);
		searchResult = InternalUMFactory.getActionFactory().simpleActionSearch(
				searchString, op, false, null);
		SearchResultCache.addSearchResult(guid, searchResult);
		return searchResult;
	}
		
	static boolean isActionMember(IRole parentRole, IAction memberAction){
		return PermissionRoles.isAssignedAction(parentRole, memberAction);
	}

	static boolean addActionMember(IRole parentRole, IAction memberAction){
		return PermissionRoles.addAction(parentRole, memberAction);
	}
	
	static boolean removeActionMember(IRole parentRole, IAction memberAction){
		return PermissionRoles.removeAction(parentRole, memberAction);
	}
	
	static IAction getAction(String uniqueId) throws NoSuchActionException{
		final String mn = "static IAction getAction(String uniqueId)";
		if (uniqueId.startsWith(ACTION_TYPE)){
			try {
				return InternalUMFactory.getActionFactory().getAction(uniqueId);
			} catch (ActionException e) {
				myLoc.traceThrowableT(Severity.ERROR, mn, e);
				throw new NoSuchActionException(uniqueId);
			}
		} else {
			myLoc.errorT(mn, "UniqueID {0} does not start with {1}", new Object[]{uniqueId, ACTION_TYPE});
			throw new NoSuchActionException(uniqueId);
		}
	}
	
	static void invalidateCacheEntry(String uniqueId){
		ActionFactory.invalidateActionInCacheLocally(uniqueId);
	}

    static IJmxTable calculateActionMemberTable(String uniqueId, String searchString,
            String companyId, String guid) throws UMException,
            OpenDataException {
        IRole role = (IRole) CompanyPrincipalFactory.getInstance(companyId)
                .getPrincipal(uniqueId);
        ISearchResult searchResult = getActionMembers(role, searchString, guid);
        JmxTable result = new JmxTable();
		result.setSize(searchResult.size());
		result.setState(searchResult.getState());
		result.setGuid(guid);
		result.setTableRows(new JmxTableRow[0]);
		return result;
    }
    
    static IJmxTable getActionMemberTable(String uniqueId, String searchString,
            String companyId, String guid, IJmxAttribute[] populationAttributes, Locale locale) throws UMException,
            OpenDataException {
        IRole role = (IRole) CompanyPrincipalFactory.getInstance(companyId)
                .getPrincipal(uniqueId);
        ISearchResult sr = getActionMembers(role, searchString, guid);
        return getJmxActionTable(sr, populationAttributes, locale, guid);
    }

    static IJmxTable calculateSimpleActionTable(String searchString,
			String companyId, String guid) throws OpenDataException,
			UMException {
        //TODO handle this with companyprincipalfactory
        ISearchResult actions = simpleActionSearch(searchString, guid);
        JmxTable result = new JmxTable();
		result.setSize(actions.size());
		result.setState(actions.getState());
		result.setGuid(guid);
		result.setTableRows(new JmxTableRow[0]);
		return result;
    }
    
    static IJmxTable getSimpleActionTable(String searchString,
			String companyId, String guid,
			IJmxAttribute[] populationAttributes, Locale locale)
			throws OpenDataException, UMException {
        //TODO handle this with companyprincipalfactory
        ISearchResult actions = simpleActionSearch(searchString, guid);
        return getJmxActionTable(actions, populationAttributes, locale, guid);
    }

    private static JmxTable getJmxActionTable(ISearchResult actions,
			IJmxAttribute[] populationAttributes, Locale locale, String guid)
			throws OpenDataException, NoSuchActionException, ActionException {
    	final String mn = "private static IJmxTable getActionTable(Set actions, IJmxAttribute[] populationAttributes, Locale locale, String guid)";
        JmxTable resultTable = new JmxTable();
        ArrayList rows = new ArrayList();
        while (actions.hasNext()){
            IAction action = InternalUMFactory.getActionFactory().getAction(
					(String) actions.next());
            if (action == null){
            	continue;
            }
            JmxTableRow row = new JmxTableRow();
            String uniqueId = ActionFactory.getActionAttribute(
                	action, 
					IPrincipal.DEFAULT_NAMESPACE, 
					CompanyPrincipalFactory.UNIQUEID, 
					locale);
            if (uniqueId == null){
            	if (myLoc.beInfo()){
            		myLoc.infoT(mn, "Action id is null");
            	}
            	continue;
            }
            row.setColUniqueId(uniqueId);
            row.setColDeleteable(false);
            row.setColType(IACTION);
        	for (int i = 0; i < populationAttributes.length; i++){
        		String value = ActionFactory.getActionAttribute(
        			action, 
					populationAttributes[i].getNamespace(), 
					populationAttributes[i].getName(),
					locale);
        		if (value == null){
        			value = CompanyPrincipalFactory.EMPTY;
        		}
        		row.setTableRowValue(i, value);
        	}
            rows.add(row);
        }
        resultTable.setTableRows(rows);
        resultTable.setSize(actions.size());
        resultTable.setState(actions.getState());
        resultTable.setGuid(guid);
        return resultTable;
    }

	static IJmxTable calculateActionTable(
			IJmxAttribute[] requestMainDetailAttributes, String companyId,
			String guid) throws ActionException, OpenDataException {
		ISearchResult actions = actionSearch(requestMainDetailAttributes, companyId, guid);
        JmxTable result = new JmxTable();
		result.setSize(actions.size());
		result.setState(actions.getState());
		result.setGuid(guid);
		result.setTableRows(new JmxTableRow[0]);
		return result;
	}

	private static ISearchResult actionSearch(
			IJmxAttribute[] requestMainDetailAttributes, String companyId,
			String guid) throws ActionException {
		ISearchResult searchResult = SearchResultCache.getSearchResult(guid);
		if (searchResult != null){
			return searchResult;
		}
		String vendor = null;
		String application = null;
		String module = null;
		char type = '*';
		String name = null;
		if (requestMainDetailAttributes != null){
			for (int i = 0; i < requestMainDetailAttributes.length; i++){
				if (requestMainDetailAttributes[i] != null){
					if (IPrincipal.DEFAULT_NAMESPACE.equals(requestMainDetailAttributes[i].getNamespace())){
						if (Action.VENDOR.equals(requestMainDetailAttributes[i].getName())){
							vendor = requestMainDetailAttributes[i].getValue();
							continue;
						}
						if (Action.APPLICATION.equals(requestMainDetailAttributes[i].getName())){
							application = requestMainDetailAttributes[i].getValue();
							continue;
						}
						if (Action.MODULE.equals(requestMainDetailAttributes[i].getName())){
							module = requestMainDetailAttributes[i].getValue();
							continue;
						}
						if (Action.TYPE.equals(requestMainDetailAttributes[i].getName())){
							if (requestMainDetailAttributes[i].getValue() != null){
								type = requestMainDetailAttributes[i].getValue().charAt(0);	
							}
							continue;
						}
						if (Action.NAME.equals(requestMainDetailAttributes[i].getName())){
							name = requestMainDetailAttributes[i].getValue();
							continue;
						}
					}
				}
			}
		}
		String[] actions = InternalUMFactory.getActionFactory().searchActions(
				vendor, application, module, type, name);
		Set searchResultWithLinkedActions = new HashSet(actions.length);
		for (int i = 0; i < actions.length; i++){
			searchResultWithLinkedActions.add(actions[i]);
		}
		// limit search result to max. number of entries defined for the UI
		int maxSearchResults = InternalUMFactory.getConfiguration().getIntDynamic(
				PrincipalFactory.MAX_SEARCHRESULTS_KEY,
				PrincipalFactory.MAX_SEARCHRESULTS_DEFAULT);
		int elementsToRemove = searchResultWithLinkedActions.size() - maxSearchResults;
		int searchResultStatus = 0;
		if (elementsToRemove > 0) {
			searchResultStatus = searchResultStatus
					| SearchCriteria.SIZE_LIMIT_EXCEEDED;
			Iterator iterator = searchResultWithLinkedActions.iterator();
			for (int i = 0; i < elementsToRemove && iterator.hasNext(); i++) {
				// remove elements;
				iterator.next();
				iterator.remove();
			}
		}
		PrincipalIteratorImpl sizeLimitedSearchResult = new PrincipalIteratorImpl(
				searchResultWithLinkedActions,
				PrincipalIterator.ITERATOR_TYPE_UNIQUEIDS_NOT_CHECKED);
		sizeLimitedSearchResult.setState(searchResultStatus);
		SearchResultCache.addSearchResult(guid, sizeLimitedSearchResult);
		return sizeLimitedSearchResult;
	}

	static IJmxTable getActionTable(
			IJmxAttribute[] requestMainDetailAttributes, String companyId,
			String guid, IJmxAttribute[] populationAttributes, Locale locale)
			throws OpenDataException, NoSuchActionException, ActionException {
        ISearchResult actions = actionSearch(requestMainDetailAttributes,
				companyId, guid);
        return getJmxActionTable(actions, populationAttributes, locale, guid);
	}

	static IJmxTable calculateActionParentTable(String uniqueId, String searchString, boolean recursiveSearch, String companyId, String guid) {
		throw new UnsupportedOperationException();
	}

	static IJmxTable getActionParentTable(String uniqueId, String searchString, IJmxAttribute[] requestPopulationAttributes, boolean recursiveSearch, String companyId, String guid) {
		throw new UnsupportedOperationException();
	}

	public static IJmxEntity getActionDetails(String uniqueId,
			IJmxAttribute[] requestPopulationAttributes, IJmxState jmxState)
			throws OpenDataException, NoSuchActionException, ActionException {
		JmxEntity responseJmxEntity = new JmxEntity();
		IAction action = InternalUMFactory.getActionFactory().getAction(uniqueId);
		responseJmxEntity.setUniqueId(action.getUniqueID());
		responseJmxEntity.setType(IACTION);
		responseJmxEntity.setModifyable(false);
		ArrayList attrList = getActionAttributeList(
				requestPopulationAttributes, action, jmxState.getLocale());
		responseJmxEntity.setAttributes(attrList);
		return responseJmxEntity;
	}
	
    private static ArrayList getActionAttributeList(
			IJmxAttribute[] requestPopulationAttributes, IAction action,
			Locale locale) throws OpenDataException {
		if (requestPopulationAttributes.length == 1
				&& requestPopulationAttributes[0].getName().equals(
						CompanyPrincipalFactory.EMPTY)) {
		} else {
			ArrayList jmxAttributeList = new ArrayList(
					requestPopulationAttributes.length);
			for (int i = 0; i < requestPopulationAttributes.length; i++) {
				IJmxAttribute[] attr = getActionAttributes(action,
						requestPopulationAttributes[i].getNamespace(),
						requestPopulationAttributes[i].getName(), locale);
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
    
    private static IJmxAttribute[] getActionAttributes(IAction action,
			String namespace, String attributeName, Locale locale)
			throws OpenDataException {
		IJmxAttribute[] resultAttributes = null;
		String value = ActionFactory.getActionAttribute(action, namespace, attributeName,
				locale);
		if (value == null) {
			resultAttributes = new IJmxAttribute[1];
			JmxAttribute jmxAttribute = new JmxAttribute(namespace,
					attributeName, CompanyPrincipalFactory.EMPTY);
			resultAttributes[0] = jmxAttribute;
		} else {
			resultAttributes = new IJmxAttribute[1];
			JmxAttribute jmxAttribute = new JmxAttribute(namespace,
					attributeName, value);
			resultAttributes[0] = jmxAttribute;
		}
		return resultAttributes;
	}

    public static void modifyActionAttributesCommit(String uniqueId,
			IJmxAttribute[] attributes) throws UMException {
        final String mn = "public static void modifyActionAttributes(String uniqueId," +
			"IJmxAttribute[] attributes)";
        IAction action = InternalUMFactory.getActionFactory().getAction(uniqueId);
        if (myLoc.beInfo()) {
            myLoc.infoT(mn, "Action {0}", new Object[] {
                    uniqueId});
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
        String principalType = ACTION_TYPE;
//		IPrincipal dummyPrincipal = null;
//		if (roleMaint.getUniqueID() == null) {
//			dummyPrincipal = CompanyPrincipalFactory
//					.createDummyPrincipal(principalType,
//							CompanyPrincipalFactory.DUMMY_PRINCIPAL_NAME);
//		}
        
        for (int i = 0; i < attributes.length; i++) {
//            if (roleMaint.getUniqueID() != null) {
//                if (!CompanyPrincipalFactory.getInstance(companyId)
//                        .isPrincipalAttributeModifiable(
//                                roleMaint.getUniqueID(),
//                                attributes[i].getNamespace(),
//                                attributes[i].getName())) {
//                    continue;
//                }
//            } else {
//                if (!CompanyPrincipalFactory.getInstance(companyId)
//                        .isPrincipalAttributeCreateable(principalType,
//                        		dummyPrincipal,
//                                attributes[i].getNamespace(),
//                                attributes[i].getName())) {
//                    continue;
//                }
//            }
            if (attributes[i].getNamespace().equals(
                    IPrincipal.DEFAULT_NAMESPACE)) {
                if (attributes[i].getName().equals(Action.RUNASIDENTITY)) {
                    if (action.getRunAsIdentity(false) == null
                            || !action.getRunAsIdentity(false).equals(
                                    attributes[i].getValue())) {
                        if (attributes[i].getValue().trim().equals(CompanyPrincipalFactory.EMPTY)) {
                            if (action.getRunAsIdentity(false) != null) {
                                action.setRunAsIdentity(null);
                            }
                        } else {
                            action.setRunAsIdentity(attributes[i].getValue()
                                    .trim());
                        }
                    }
                    continue;
                }
            }
        }
        action.save();
        action.commit();
    }

	public static boolean isActionAttributeModifiable(String uniqueId,
			String namespace, String name) {
		if (namespace.equals(IPrincipal.DEFAULT_NAMESPACE)) {
			if (name.equals(Action.RUNASIDENTITY)) {
				if (ActionFactory.getActionType(uniqueId) == IAction.TYPE_J2EE_ROLE
						|| ActionFactory.getActionType(uniqueId) == IAction.TYPE_LINKED) {
					return true;
				}
			}
		}
		return false;
	}
    
}
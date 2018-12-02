package com.sap.security.core.jmx.impl;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.OpenDataException;

import com.sap.security.api.IMessage;
import com.sap.security.api.IPrincipal;
import com.sap.security.api.IUser;
import com.sap.security.api.InvalidLogonIdException;
import com.sap.security.api.InvalidPasswordException;
import com.sap.security.api.UMException;
import com.sap.security.api.UMFactory;
import com.sap.security.api.UMRuntimeException;
import com.sap.security.api.logon.ILoginConstants;
import com.sap.security.api.persistence.IDataSourceMetaData;
import com.sap.security.api.persistence.IR3Persistence;
import com.sap.security.api.persistence.IR3Persistence.ILDAPServerInfo;
import com.sap.security.api.umap.NoLogonDataAvailableException;
import com.sap.security.core.InternalUMFactory;
import com.sap.security.core.jmx.IJmxEntity;
import com.sap.security.core.jmx.IJmxMap;
import com.sap.security.core.jmx.IJmxMapEntry;
import com.sap.security.core.jmx.IJmxPermission;
import com.sap.security.core.jmx.IJmxProperty;
import com.sap.security.core.jmx.IJmxResult;
import com.sap.security.core.jmx.IJmxServer;
import com.sap.security.core.jmx.IJmxState;
import com.sap.security.core.jmx.IJmxTable;
import com.sap.security.core.jmx.util.UMTestConfigExtended;
import com.sap.security.core.persistence.IPersistenceCollection;
import com.sap.security.core.persistence.IPrincipalDatabag;
import com.sap.security.core.persistence.IPrincipalDatabagFactory;
import com.sap.security.core.persistence.datasource.imp.SuperUser;
import com.sap.security.core.persistence.datasource.imp.R3RoleDataSource;
import com.sap.security.core.persistence.imp.PrincipalDatabag;
import com.sap.security.core.persistence.imp.PrincipalDatabagFactory;
import com.sap.security.core.persistence.imp.SearchCriteria;
import com.sap.security.core.tools.ConsistencyCheckHelper;
import com.sap.security.core.tools.IConsistencyCheckResponse;
import com.sap.security.core.tools.imp.ConsistencyCheckResponse;
import com.sap.security.core.umap.imp.UserMapping;
import com.sap.security.core.util.UMEConfigurationPermission;
import com.sap.security.core.util.UMEPermission;
import com.sap.security.core.util.config.IProperty;
import com.sap.security.core.util.config.ISaveResult;
import com.sap.security.core.util.config.IUMConfigExtended;
import com.sap.security.core.util.config.UMConfigurationException;
import com.sap.security.core.util.imp.Util;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

public class JmxServer extends StandardMBean implements IJmxServer {

    private static final String EXCEPTION_CODE_MSG_SEPARATOR = " - ";

    private static Location myLoc = Location.getLocation(JmxServer.class);

    private static final String CLASSNAME = "classname:[";

    private static final String MESSAGE = "message:[";
    
    private static final String TYPE = "type:[";
    
    private static final String END = "]";
	
    public JmxServer() throws NotCompliantMBeanException {
        super(IJmxServer.class);
    }

	/**
	 * @deprecated
	 */
    public IJmxTable calculateSimpleEntityTable(String searchString,
            String type, String companyId, String guid, CompositeData[] state) throws Exception {
    	return this.calculateSimpleEntityTableByDatasources(searchString, type,
				null, null, companyId, guid, state);
    }
	/**
	 * @deprecated
	 */
    public IJmxTable getSimpleEntityTable(String searchString, String type,
            CompositeData[] dataPopulation, String companyId, String guid, CompositeData[] state)
            throws Exception {
    	return this.getSimpleEntityTableByDatasources(searchString, type, null,
				null, dataPopulation, companyId, guid, state);
    }
	/**
	 * @deprecated
	 */
    public IJmxTable calculateEntityTable(CompositeData dataMainDetail,
            String companyId, String guid, CompositeData[] state) throws Exception {
    	return this.calculateEntityTableByDatasources(dataMainDetail, null, null,
				null, false, companyId, guid, state);
    }
	/**
	 * @deprecated
	 */
    public IJmxTable getEntityTable(CompositeData dataMainDetail,
            CompositeData[] dataPopulation, String companyId, String guid, CompositeData[] state)
            throws Exception {
    	return this.getEntityTableByDatasources(dataMainDetail, null, null, null,
				dataPopulation, null, false, companyId, guid, state);
    }

    public IJmxTable calculateCombinedEntityTable(CompositeData dataMainDetail,
            CompositeData dataAdditionalDetail, boolean showUnapprovedUsers,
            String companyId, String guid, CompositeData[] state) throws Exception {
		IJmxState jmxState = new JmxState(state);
        try {
        	IJmxTable result = JmxSearchHelper.calculateEntityTable(dataMainDetail,
					dataAdditionalDetail, null, null, showUnapprovedUsers,
					companyId, guid, jmxState);
			if (myLoc.beInfo()){
				myLoc.infoT("calculateCombinedEntityTable", new Object[]{result});
			}
			return result;
        } catch (Exception e) {
			handleThrowable(e, jmxState);
            return null;
        }
    }

    public IJmxTable getCombinedEntityTable(CompositeData dataMainDetail,
            CompositeData dataAdditionalDetail, CompositeData[] dataPopulation,
            boolean showUnapprovedUsers, String companyId, String guid, CompositeData[] state)
            throws Exception {
		IJmxState jmxState = new JmxState(state);
        try {
			IJmxTable result = JmxSearchHelper.getEntityTable(dataMainDetail,
					dataAdditionalDetail, dataPopulation, null, null, null,
					showUnapprovedUsers, companyId, guid, jmxState);
			if (myLoc.beInfo()){
				myLoc.infoT("getCombinedEntityTable", new Object[]{result});
			}
			return result;
        } catch (Exception e) {
			handleThrowable(e, jmxState);
            return null;
        }
    }

	/**
	 * @deprecated
	 */
    public IJmxTable calculateEntityMemberTable(String uniqueId,
            String searchString, String memberType, boolean recursiveSearch,
            String companyId, String guid, CompositeData[] state) throws Exception {
    	return this.calculateEntityMemberTableByDatasources(uniqueId, searchString,
				memberType, null, null, recursiveSearch, companyId, guid, state);
    }

	/**
	 * @deprecated
	 */
    public IJmxTable getEntityMemberTable(String uniqueId, String searchString,
            String memberType, CompositeData[] dataPopulation,
            boolean recursiveSearch, String companyId, String guid, CompositeData[] state)
            throws Exception {
    	return this.getEntityMemberTableByDatasources(uniqueId, searchString,
				memberType, null, null, dataPopulation, recursiveSearch,
				companyId, guid, state);
    }

	/**
	 * @deprecated
	 */
    public IJmxTable calculateEntityParentTable(String uniqueId,
            String searchString, String parentType, boolean recursiveSearch,
            String companyId, String guid, CompositeData[] state) throws Exception {
    	return this.calculateEntityParentTableByDatasources(uniqueId, searchString,
				parentType, null, null, recursiveSearch, companyId, guid, state);
    }

	/**
	 * @deprecated
	 */
    public IJmxTable getEntityParentTable(String uniqueId, String searchString,
            String parentType, CompositeData[] dataPopulation,
            boolean recursiveSearch, String companyId, String guid, CompositeData[] state)
            throws Exception {
    	return this.getEntityParentTableByDatasources(uniqueId, searchString,
				parentType, null, null, dataPopulation, recursiveSearch,
				companyId, guid, state);
    }

    public IJmxEntity getAllEntityDetails(String uniqueId, String companyId, CompositeData[] state)
            throws Exception {
		IJmxState jmxState = new JmxState(state);
        try {
            IJmxEntity result = JmxSearchHelper.getAllEntityDetails(uniqueId, companyId, jmxState);
			if (myLoc.beInfo()){
				myLoc.infoT("getAllEntityDetails", new Object[]{result});
			}
			return result;
        } catch (Exception e) {
			handleThrowable(e, jmxState);
            return null;
        }
    }

    public IJmxEntity createEntity(CompositeData dataMainDetail,
            CompositeData dataAdditionalDetail, String action, String companyId, CompositeData[] state)
            throws Exception {
		IJmxState jmxState = new JmxState(state);
        try {
			IJmxEntity result = JmxModificationHelper.createEntity(dataMainDetail,
                    dataAdditionalDetail, action, companyId, jmxState);
			if (myLoc.beInfo()){
				myLoc.infoT("createEntity", new Object[]{result});
			}
			return result;
        } catch (Exception e) {
			handleThrowable(e, jmxState);
            return null;
        }
    }

	/**
	 * @deprecated
	 */
    public int modifyEntity(CompositeData dataMainDetail,
            CompositeData dataAdditionalDetail, String action, String message,
            String companyId, CompositeData[] state) throws Exception {
		IJmxState jmxState = new JmxState(state);
    	try {
			IJmxEntity result = JmxModificationHelper.modifyEntity(dataMainDetail,
                    dataAdditionalDetail, action, message, companyId, jmxState);
			if (myLoc.beInfo()){
				myLoc.infoT("modifyEntity", new Object[]{result});
			}
            return RESULT_OPERATION_OK;
        } catch (Exception e) {
			handleThrowable(e, jmxState);
            return 0;
        }
    }

    public IJmxEntity changeEntity(CompositeData dataMainDetail,
            CompositeData dataAdditionalDetail, String action, String message,
            String companyId, CompositeData[] state) throws Exception {
		IJmxState jmxState = new JmxState(state);
        try {
			IJmxEntity result = JmxModificationHelper.modifyEntity(dataMainDetail,
					dataAdditionalDetail, action, message, companyId, jmxState);
			if (myLoc.beInfo()){
				myLoc.infoT("changeEntity", new Object[]{result});
			}
			return result;
        } catch (Exception e) {
			handleThrowable(e, jmxState);
            return null;
        }
    }
    
    public int modifyEntities(String[] ids, String action, String message,
            String companyId, CompositeData[] state) throws Exception {
        try {
            return JmxModificationHelper.modifyEntities(ids, action, message,
                    companyId);
        } catch (Exception e) {
			IJmxState jmxState = new JmxState(state);
			handleThrowable(e, jmxState);
            return 0;
        }
    }

    public int deleteEntities(String[] ids, String message, String companyId, CompositeData[] state)
            throws Exception {
        try {
            return JmxModificationHelper
                    .deleteEntities(ids, message, companyId);
        } catch (Exception e) {
			IJmxState jmxState = new JmxState(state);
			handleThrowable(e, jmxState);
            return 0;
        }
    }

    public IJmxEntity getEntityDetails(String uniqueId,
            CompositeData[] dataPopulation, String companyId, CompositeData[] state) throws Exception {
		IJmxState jmxState = new JmxState(state);
    	try {
			IJmxEntity result = JmxSearchHelper.getEntityDetails(uniqueId, dataPopulation,
					companyId, jmxState);
			if (myLoc.beInfo()){
				myLoc.infoT("getEntityDetails", new Object[]{result});
			}
			return result;
        } catch (Exception e) {
			handleThrowable(e, jmxState);
            return null;
        }
    }

    public String batchExport(String[] uniqueIds, CompositeData[] state) throws Exception {
        try {
            String result = JmxBatch.batchExport(uniqueIds);
			if (myLoc.beInfo()){
				myLoc.infoT("batchExport", new Object[]{result});
			}
			return result;
        } catch (Exception e) {
			IJmxState jmxState = new JmxState(state);
			handleThrowable(e, jmxState);
            return null;
        }
    }

    public IJmxTable batchImport(String data, boolean overwrite, CompositeData[] state)
            throws Exception {
        try {
			IJmxTable result = JmxBatch.batchImport(data, overwrite);
			if (myLoc.beInfo()){
				myLoc.infoT("batchImport", new Object[]{result});
			}
			return result;
        } catch (Exception e) {
			IJmxState jmxState = new JmxState(state);
			handleThrowable(e, jmxState);
            return null;
        }
    }

    public IJmxMapEntry[] getUmeProperties(CompositeData[] state) throws Exception {
        try {
            Properties umeProps = InternalUMFactory.getConfigExtended().getAllPropertiesDynamic();
            JmxMapEntry prop;
            IJmxMapEntry[] returnProps = new IJmxMapEntry[umeProps.keySet()
                    .size()];
            int i = 0;
            for (Iterator it = umeProps.keySet().iterator(); it.hasNext(); i++) {
                prop = new JmxMapEntry();
                String key = (String) it.next();
                String value = (String) umeProps.get(key);
                prop.setKey(key);
                prop.setValue(value);
                returnProps[i] = prop;
            }
			if (myLoc.beInfo()){
				myLoc.infoT("getUmeProperties", new Object[]{returnProps});
			}
            return returnProps;
        } catch (Exception e) {
			IJmxState jmxState = new JmxState(state);
			handleThrowable(e, jmxState);
            return null;
        }
    }

    public boolean getCompanyConceptEnabled(CompositeData[] state) throws Exception {
        try {
            return CompanyPrincipalFactory.isCompanyConceptEnabled();
        } catch (Exception e) {
			IJmxState jmxState = new JmxState(state);
			handleThrowable(e, jmxState);
            return false;
        }
    }

    public IJmxTable getCompanies(CompositeData[] state) throws Exception {
		IJmxState jmxState = new JmxState(state);
        try {
            IJmxTable result = CompanyPrincipalFactory.getCompanies(jmxState);
			if (myLoc.beInfo()){
				myLoc.infoT("getCompanies", new Object[]{result});
			}
			return result;
        } catch (Exception e) {
			handleThrowable(e, jmxState);
            return null;
        }
    }

    public void cancelSearchRequest(String guid, CompositeData[] state) {
        SearchResultCache.invalidateSearchResult(guid);
    }

    public IJmxEntity getAttributeLayoutInformation(String uniqueId,
            String type, CompositeData[] dataPopulation, String companyId, CompositeData[] state)
            throws Exception {
		IJmxState jmxState = new JmxState(state);
    	try {
            IJmxEntity result = JmxLayoutHelper.getAttributeLayoutInformation(uniqueId,
                    type, dataPopulation, companyId);
			if (myLoc.beInfo()){
				myLoc.infoT("getAttributeLayoutInformation", new Object[]{result});
			}
			return result;
        } catch (Exception e) {
			handleThrowable(e, jmxState);
            return null;
        }
    }

    public int modifyEntityMappings(String[] parentIds, String[] memberIds,
            boolean addMembers, String companyId, CompositeData[] state) throws Exception {
		IJmxState jmxState = new JmxState(state);
        try {
            IJmxResult jmxResult = JmxModificationHelper.modifyEntityAssignments(parentIds,
                    memberIds, addMembers, companyId, jmxState);
            return jmxResult.getStatus();
        } catch (Exception e) {
			handleThrowable(e, jmxState);
            return 0;
        }
    }

    public IJmxResult modifyEntityAssignments(String[] parentIds, String[] memberIds,
            boolean addMembers, String companyId, CompositeData[] state) throws Exception {
		IJmxState jmxState = new JmxState(state);
        try {
            return JmxModificationHelper.modifyEntityAssignments(parentIds,
                    memberIds, addMembers, companyId, jmxState);
        } catch (Exception e) {
			handleThrowable(e, jmxState);
            return null;
        }
    }
    
    public void lock(String[] uniqueIds, CompositeData[] state) throws Exception {
        try {
            JmxLockingManager.createWriteLock(uniqueIds);
        }
	catch(Throwable e) {
            IJmxState jmxState = new JmxState(state);
            handleThrowable(e, jmxState);
        }
    }

    public void unlock(String[] uniqueIds, CompositeData[] state) throws Exception {
        try {
            JmxLockingManager.releaseWriteLock(uniqueIds);
        }
	catch(Throwable e) {
            IJmxState jmxState = new JmxState(state);
            handleThrowable(e, jmxState);
        }
    }

    public IJmxPermission[] hasPermission(CompositeData[] permissions,
            String companyId, CompositeData[] state) throws Exception {
        try {
            final String mn = "public IJmxPermission[] hasPermission(CompositeData[] permissions,String companyId)";
            if (myLoc.beInfo()) {
                myLoc.infoT(mn, "Evaluating permissions");
            }
            myLoc.entering(mn);
            IJmxPermission[] resultPermissions = new IJmxPermission[permissions.length];
            for (int i = 0; i < permissions.length; i++) {
                JmxPermission jmxPerm = new JmxPermission(permissions[i]);
                Permission javaPerm = CompanyPrincipalFactory.getInstance(
                        companyId).instantiatePermission(jmxPerm);
                if (javaPerm != null) {
                    jmxPerm.setPermission(InternalUMFactory.loggedInUserHasPermission(javaPerm));
                } else {
                    jmxPerm.setPermission(false);
                }
                resultPermissions[i] = jmxPerm;
                if (myLoc.beDebug()) {
                    myLoc.debugT(mn, "Permission {0} of {1}: {2}",
                            new Object[] { new Integer(i + 1),
                                    new Integer(permissions.length), jmxPerm });
                }
            }
            return resultPermissions;
        } catch (Exception e) {
			IJmxState jmxState = new JmxState(state);
			handleThrowable(e, jmxState);
            return null;
        }
    }

    public IJmxMapEntry[] readConfiguration(CompositeData[] state) throws Exception {
        try {
            Properties umeProps = InternalUMFactory.getConfigAdmin().getAllPropertiesAdmin();
            List<IJmxMapEntry> propList = new ArrayList<IJmxMapEntry>(umeProps.size());
            for (Enumeration keys = umeProps.keys(); keys.hasMoreElements();) {
                String key = (String) keys.nextElement();
                IJmxMapEntry entry = new JmxMapEntry(key, umeProps
                        .getProperty(key));
                propList.add(entry);
            }
			if (myLoc.beInfo()){
				myLoc.infoT("readConfiguration", JmxMapEntry.getArray(propList));
			}
            return JmxMapEntry.getArray(propList);
        } catch (Exception e) {
			IJmxState jmxState = new JmxState(state);
			handleThrowable(e, jmxState);
            return null;
        }
    }

    public IJmxProperty[] readConfigurationExt(int flags, CompositeData[] state) throws Exception {
        try {
            Collection<IProperty> properties = InternalUMFactory.getConfigAdmin()
                .getAllPropertiesExtendedAdmin(flags);
            
            List<IJmxProperty> jmxProperties = new ArrayList<IJmxProperty>(properties.size());
            Iterator<IProperty> propertiesIterator = properties.iterator();
            while(propertiesIterator.hasNext()) {
                IJmxProperty currentJmxProperty = new JmxProperty(propertiesIterator.next());

                if(myLoc.beDebug()) {
                    // Tracing all (even secure) properties is OK because the toString() method
                    // already omits secure values.
                    myLoc.debugT("readConfigurationExt", currentJmxProperty.toString());
                }

                jmxProperties.add(currentJmxProperty); 
            }

            return jmxProperties.toArray(new IJmxProperty[jmxProperties.size()]);
        } catch (Exception e) {
            IJmxState jmxState = new JmxState(state);
            handleThrowable(e, jmxState);
            return null;
        }
    }

    public boolean writeConfiguration(CompositeData[] properties, CompositeData[] state)
        throws Exception {

    	try {
    	    // This variant of writeConfiguration() has no way to pass the save result
    	    // information to the caller, so it simply ignores it.
        	writeConfigurationInternal(properties);
            return true;
        } catch (Exception e) {
			IJmxState jmxState = new JmxState(state);
			handleThrowable(e, jmxState);
            return false;
        }
    }

    public IJmxResult writeConfigurationExt(CompositeData[] properties, CompositeData[] state)
        throws Exception {

        IJmxState jmxState = new JmxState(state);
        
        try {
            ISaveResult result = writeConfigurationInternal(properties);
            int status = result.isServerRestartRequired() ? RESULT_CFG_SERVER_RESTART_REQUIRED : 0;
            return new JmxResult(status, result.getMessages(), jmxState);
        } catch (Exception e) {
            handleThrowable(e, jmxState);
            return null;
        }
    }

 	public IJmxEntity[] getUserMappingSystems(String principalUniqueId, CompositeData[] state)
	throws Exception {
		IJmxState jmxState = new JmxState(state);

		try {
            return JmxUserMappingHelper.getUserMappingSystems(principalUniqueId, jmxState.getLocale());
        }
		catch(Throwable e) {
			handleThrowable(e, jmxState);
            return null;
        }
    }

    public IJmxEntity getUserMappingData(String principalUniqueId, String systemId, CompositeData[] state)
    throws Exception {
        try {
            return JmxUserMappingHelper.getUserMappingData(principalUniqueId, systemId);
        }
        catch(Throwable e) {
			IJmxState jmxState = new JmxState(state);
			handleThrowable(e, jmxState);
            return null;
        }
    }

    public void storeUserMappingData(String principalUniqueId, String systemId,
    CompositeData[] logonData, CompositeData[] state) throws Exception {
        try {
            JmxUserMappingHelper.storeUserMappingData(principalUniqueId, systemId, logonData);
        }
        catch(Throwable e) {
			IJmxState jmxState = new JmxState(state);
			handleThrowable(e, jmxState);
        }
    }

    public void clearUserMappingData(String principalUniqueId, String systemId, CompositeData[] state)
    throws Exception {
        try {
            JmxUserMappingHelper.clearUserMappingData(principalUniqueId, systemId);            
        }
        catch(Throwable e) {
			IJmxState jmxState = new JmxState(state);
			handleThrowable(e, jmxState);
        }

    }
    
    public IJmxEntity[] getAvailableUserMappingConverters(CompositeData[] state) throws Exception {
        try {
            return JmxUserMappingHelper.getAvailableUserMappingConverters();
        }
        catch(Throwable e) {
			IJmxState jmxState = new JmxState(state);
			handleThrowable(e, jmxState);
            return null;
        }
    }

    public void startUserMappingConversion(String converterType, CompositeData[] state) throws Exception {
        try {
            JmxUserMappingHelper.startUserMappingConversion(converterType);
        }
        catch(Throwable e) {
			IJmxState jmxState = new JmxState(state);
			handleThrowable(e, jmxState);
        }
    }

    public void startUserMappingConversionByThreads(String converterType,
        int numberOfThreads, CompositeData[] state) throws Exception {
        try {
            JmxUserMappingHelper.startUserMappingConversion(converterType, numberOfThreads);
        }
        catch(Throwable e) {
			IJmxState jmxState = new JmxState(state);
			handleThrowable(e, jmxState);
        }
    }

    public void resetUserMappingConversionStatus(String converterType, CompositeData[] state) {
        JmxUserMappingHelper.resetUserMappingConversionStatus(converterType);
    }
    
    public int getUserMappingCryptoMode(CompositeData[] state) throws Exception {
        try {
            return UserMapping.getCryptoMode();
        }
        catch(Throwable e) {
			IJmxState jmxState = new JmxState(state);
			handleThrowable(e, jmxState);
            return -1;
        }
    }
    
    public String[] getSAPReferenceSystemCandidates(CompositeData[] state) throws Exception {
        try {
            return JmxUserMappingHelper.getSAPReferenceSystemCandidates();
        }
        catch(Throwable e) {
			IJmxState jmxState = new JmxState(state);
			handleThrowable(e, jmxState);
            return null;
        }
    }

    public IJmxEntity[] getSAPReferenceSystemCandidates2(CompositeData[] state) throws Exception {
		IJmxState jmxState = new JmxState(state);

		try {
            return JmxUserMappingHelper.getSAPReferenceSystemCandidates2(jmxState.getLocale());
        }
		catch(Throwable e) {
			handleThrowable(e, jmxState);
            return null;
        }
    }

	public IJmxEntity getUserByInverseUserMapping(
		String userId,
		String systemAlias,
		String systemType,
		CompositeData[] state)
		throws Exception {
		IJmxState jmxState = new JmxState(state);
		try {
			IJmxEntity result = JmxUserMappingHelper.getUserByInverseUserMapping(
				userId,
				systemAlias,
				systemType,
				jmxState);
			if (myLoc.beInfo()){
				myLoc.infoT("getUserByInverseUserMapping", new Object[]{result});
			}
			return result;
		} catch (Exception e) {
			handleThrowable(e, jmxState);
			return null;
		}
	}

    private void handleThrowable(Throwable t, IJmxState state) throws Exception {
    	final String mn = "private void handleThrowable(Throwable t, CompositeData[] state)";
		if (myLoc.beInfo()){
			myLoc.traceThrowableT(Severity.INFO, mn, t);
		}
        String originalMessage = t.getLocalizedMessage();
        // Special handling for NoLogonDataAvailableException: Integrate error
        // reason code into the message text (because only the exception text
        // is certain to be transported to the JMX client)
        if(t instanceof NoLogonDataAvailableException) {
            NoLogonDataAvailableException nldae = (NoLogonDataAvailableException) t;
            originalMessage = convertLocalizedMessageToString(
                nldae.getReason() + EXCEPTION_CODE_MSG_SEPARATOR + originalMessage);
        } else if (t instanceof UMException){
        	UMException ume = (UMException)t;
        	Iterator<IMessage> it = ume.getMessages(false);
        	if (it != null){
            	originalMessage = getMessagesAsString(it, ume, state);
        	} else {
        		if (t instanceof InvalidLogonIdException || t instanceof InvalidPasswordException){
        			originalMessage = convertLocalizedMessageToString(UMFactory
							.getSecurityPolicy().getLocalizedMessage(state.getLocale(),
									(Exception) t));
        		} else {
        			originalMessage = convertLocalizedMessageToString(originalMessage);
        		}
        	}
		} else if (t instanceof UMRuntimeException){
			UMRuntimeException ume = (UMRuntimeException)t;
        	Iterator<IMessage> it = ume.getMessages(false);
        	if (it != null){
            	originalMessage = getMessagesAsString(it, ume, state);
        	} else {
        		originalMessage = convertLocalizedMessageToString(originalMessage);
        	}
        } else if (t instanceof com.sap.engine.frame.core.locking.LockException) {
            com.sap.engine.frame.core.locking.LockException le = (com.sap.engine.frame.core.locking.LockException) t;

            // Don't provide information about the owner of the lock to normal end users.
            // The permission check ensures that the current user only gets the information
            // about the lock owner if it is allowed to read the logon ID (which is the value
            // returned by getCollisionUserName()) of the user owning the lock. As end users
            // can only modify their own profiles, that means that they only get that information
            // if they own the lock themselves, which is not critical regarding security.
            String returnedCollisionUserName = "";
            String collisionUserName = le.getCollisionUserName();
            if(collisionUserName != null) {
                IUser collisionUser = UMFactory.getUserFactory().getUserByLogonID(collisionUserName);
                IUser editingUser = UMFactory.getAuthenticator().getLoggedInUser();

                // Note: .._J_USER is the attribute name of the logon ID. 
                UMEPermission permission = new UMEPermission(IPrincipalDatabag.ACCOUNT_TYPE,
                    UMEPermission.ACTION_READ,
                    UMEPermission.COM_SAP_SECURITY_CORE_USERMANAGEMENT_J_USER,
                    collisionUser.getUniqueID(), null, editingUser.getUniqueID()
                );

                if(editingUser.hasPermission(permission)) returnedCollisionUserName = collisionUserName;
            }

            originalMessage = convertLocalizedMessageToString(returnedCollisionUserName + EXCEPTION_CODE_MSG_SEPARATOR + originalMessage);
        }
		if (myLoc.beInfo()){
			myLoc.infoT(mn, CLASSNAME + t.getClass().getName() + END + originalMessage);
		}
        throw new Exception(CLASSNAME + t.getClass().getName() + END + originalMessage , t);
    }
    
    private String getMessagesAsString(Iterator<IMessage> messages, Exception e, IJmxState state){
    	StringBuffer buf = new StringBuffer();
		while (messages.hasNext()){
			IMessage m = messages.next();
        	buf.append(MESSAGE);
			buf.append(m.getLocalizedMessage(state.getLocale()));
			buf.append(END);
			buf.append(TYPE);
			buf.append(m.getType());
			buf.append(END);
		}
		return buf.toString();
    }
    
    private String convertLocalizedMessageToString(String message){
    	StringBuffer buf = new StringBuffer();
    	buf.append(MESSAGE);
		buf.append(message);
		buf.append(END);
		buf.append(TYPE);
		buf.append(IMessage.TYPE_ERROR);
		buf.append(END);
		return buf.toString();
    }
    
    public IJmxMapEntry[] getFeatureList(CompositeData[] state){
		final String mn = "public IJmxMapEntry[] getFeatureList(CompositeData[] state)";
		try {
			/* Org restrictions are not available in 700, but in 710
			 * If true, WD UI would check if RBAM WD Plugin is available
			 * With setting to false, no check has to be done
			 */
			JmxMapEntry orgRestrictionsEntry = new JmxMapEntry(
					FEATURE_ORG_RESTRICTIONS_ENABLED, CompanyPrincipalFactory.TRUE);
			JmxMapEntry datasourceSearchEntry = new JmxMapEntry(
					FEATURE_DATASOURCE_SEARCH_ENABLED, CompanyPrincipalFactory.TRUE);
			String ldapIntegrationEnabled = CompanyPrincipalFactory.FALSE;
			try {
				Method[] m = Class.forName("com.sap.security.core.persistence.IPrincipalDatabagFactory").getMethods();
				for (int i = 0; m != null && i < m.length; i++) {
					if (m[i].getName().equalsIgnoreCase("shutdown")) {
						ldapIntegrationEnabled = "TRUE";
						break;
					}
				}	
			}
			catch (Throwable t) {
				myLoc.traceThrowableT(Severity.INFO, "Problem while checking backend LDAP ABAP integration", t);
			}

			JmxMapEntry ldapIntegrationEntry    = new JmxMapEntry(FEATURE_LDAP_INTEGRATION_ENABLED , ldapIntegrationEnabled);
			JmxMapEntry genericMapEntry         = new JmxMapEntry(FEATURE_GENERIC_MAP_ENABLED      , CompanyPrincipalFactory.TRUE);
			JmxMapEntry sapTimezoneEntry        = new JmxMapEntry(FEATURE_SAPTIMEZONE_ENABLED      , CompanyPrincipalFactory.TRUE);
			JmxMapEntry systemDisplayNamesEntry = new JmxMapEntry(FEATURE_DISPLAY_NAMES_FOR_SYSTEMS, CompanyPrincipalFactory.TRUE);
			JmxMapEntry messagingEntry          = new JmxMapEntry(FEATURE_MESSAGING_ENABLED        , CompanyPrincipalFactory.TRUE);
			JmxMapEntry downloadConfigEntry     = new JmxMapEntry(FEATURE_DOWNLOAD_CONFIG          , CompanyPrincipalFactory.TRUE);
			JmxMapEntry a1sSystemEntry          = new JmxMapEntry(FEATURE_IS_A1S_SYSTEM            , Boolean.toString(InternalUMFactory.getPlatformTools().isA1SSystem()));
			JmxMapEntry entityAssignmentsEntry  = new JmxMapEntry(FEATURE_MODIFY_ENTITY_ASSIGNMENTS_ENABLED, CompanyPrincipalFactory.TRUE);
			JmxMapEntry readConfigurationExt    = new JmxMapEntry(FEATURE_READ_CONFIGURATION_EXT   , CompanyPrincipalFactory.TRUE);
			JmxMapEntry writeConfigurationExt   = new JmxMapEntry(FEATURE_WRITE_CONFIGURATION_EXT  , CompanyPrincipalFactory.TRUE);
			IJmxMapEntry[] result = new IJmxMapEntry[] {
				orgRestrictionsEntry,
				datasourceSearchEntry,
				ldapIntegrationEntry,
				genericMapEntry,
				sapTimezoneEntry,
				systemDisplayNamesEntry,
				messagingEntry,
				downloadConfigEntry,
				a1sSystemEntry,
				entityAssignmentsEntry,
				readConfigurationExt,
				writeConfigurationExt
			};
			if (myLoc.beInfo()){
				myLoc.infoT("getFeatureList", result);
			}
			return result;
		} catch (OpenDataException e) {
			myLoc.traceThrowableT(Severity.ERROR, mn, e);
			return new IJmxMapEntry[]{};
		}
	}
    
	public IJmxMapEntry[] getDatasources(String type, CompositeData[] state)
			throws Exception {
		final String mn = "public IJmxMapEntry[] getDatasources(String type, CompositeData[] state)";
		IJmxState jmxState = new JmxState(state);
		type = CompanyPrincipalFactory.getPrivatePrincipalTypeIdentifier(type);
		IDataSourceMetaData[] dsmd = PrincipalDatabagFactory.getInstance()
				.getResponsibleDataSources(type);
		int additionalDatasources = 1;
		if (PrincipalDatabag.ROLE_TYPE.equals(type)){
			if (CompanyPrincipalFactory.isRemoteProducerAccessEnabled()){
				additionalDatasources = 3;
			}
		}
		if (dsmd != null) {
			JmxMapEntry[] datasources = new JmxMapEntry[dsmd.length + additionalDatasources];
			datasources[0] = new JmxMapEntry(
					CompanyPrincipalFactory.ALL_DATASOURCES,
					CompanyPrincipalFactory
							.localizeDatasourceId(
									CompanyPrincipalFactory.ALL_DATASOURCES_TRANSLATION,
									jmxState), true);
			if (additionalDatasources == 3){
				datasources[1] = new JmxMapEntry(
						CompanyPrincipalFactory.LOCAL_DATASOURCES,
						CompanyPrincipalFactory
								.localizeDatasourceId(
										CompanyPrincipalFactory.LOCAL_DATASOURCES_TRANSLATION,
										jmxState), true);
				datasources[2] = new JmxMapEntry(
						CompanyPrincipalFactory.REMOTE_DATASOURCES,
						CompanyPrincipalFactory
								.localizeDatasourceId(
										CompanyPrincipalFactory.REMOTE_DATASOURCES_TRANSLATION,
										jmxState), true);
			}
			for (int i = 0; i < dsmd.length; i++) {
				String id = dsmd[i].getDataSourceID();
				String description = dsmd[i].getDisplayName(jmxState.getLocale());
				if (description == null
						|| CompanyPrincipalFactory.EMPTY.equals(description)) {
					description = CompanyPrincipalFactory.localizeDatasourceId(id, jmxState);
					if (description == null){
						description = id;						
					}
				}
				datasources[i + additionalDatasources] = new JmxMapEntry(id,
						description, true);
			}
			Arrays.sort(datasources, additionalDatasources, datasources.length);
			if (myLoc.beInfo()){
				myLoc.infoT("getDatasources", datasources);
			}
			return datasources;			
		}
		if (myLoc.beInfo()){
			myLoc.infoT(mn, "No datasources available for type {0}",
					new Object[] { type });
		}
		return new IJmxMapEntry[0];
	}
	
	public byte[] readFile(String name, CompositeData[] state) throws Exception {
		try {
			UMEConfigurationPermission perm = new UMEConfigurationPermission(
				UMEConfigurationPermission.READ,
				UMEConfigurationPermission.ALL);
			InternalUMFactory.checkPermissionOfLoggedInUser(perm);
			InputStream is = InternalUMFactory.getConfigAdmin().readConfigFileAdmin(name);
			if (is != null){
				return Util.readInputStreamToBytes(is);
			}
		} catch (Exception t){
			IJmxState jmxState = new JmxState(state);
			handleThrowable(t, jmxState);
		}
		return null;
	}

	public void writeFile(String name, byte[] content, CompositeData[] state) throws Exception {
		try {
			UMEConfigurationPermission perm = new UMEConfigurationPermission(
				UMEConfigurationPermission.EDIT,
				UMEConfigurationPermission.ALL);
			InternalUMFactory.checkPermissionOfLoggedInUser(perm);
			InternalUMFactory.getConfigAdmin().writeConfigFile(name, content);
		} catch (Exception t){
			IJmxState jmxState = new JmxState(state);
			handleThrowable(t, jmxState);
		}
	}

	public String[] getFiles(CompositeData[] state) throws Exception {
		try {
			UMEConfigurationPermission perm = new UMEConfigurationPermission(
				UMEConfigurationPermission.READ,
				UMEConfigurationPermission.ALL);
			InternalUMFactory.checkPermissionOfLoggedInUser(perm);
			String[] result = InternalUMFactory.getConfigAdmin().getAllConfigFileNamesAdmin();
			if (myLoc.beInfo()){
				myLoc.infoT("getFiles", result);
			}
			return result;
		} catch (Exception t){
			IJmxState jmxState = new JmxState(state);
			handleThrowable(t, jmxState);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see com.sap.security.core.jmx.IJmxServer#ccToolGetCheckLogResults(javax.management.openmbean.CompositeData[])
	 */
	public String ccToolGetCheckLogResults(CompositeData[] state) throws Exception {
		return ConsistencyCheckHelper.getInstance().getCheckLogResults();
	}

	/* (non-Javadoc)
	 * @see com.sap.security.core.jmx.IJmxServer#ccToolGetIdsToRepair(java.lang.String, javax.management.openmbean.CompositeData[])
	 */
	public String[][] ccToolGetIdsToRepair(String errorClass, CompositeData[] state) throws Exception {
		Map data = ConsistencyCheckHelper.getInstance().getIdsToRepair();
		Collection elements = (Collection)data.get(errorClass);
		String[][] result = new String[0][0];
		if (elements != null)
		{
			int size = elements.size();
			int counter=0; 
			if (ConsistencyCheckHelper.SOLUTION_PRINCIPALS_TO_DELETE.equals(errorClass))
			{
				result = new String[size][3];
			} else if (ConsistencyCheckHelper.SOLUTION_ATTRIBUTES_TO_DELETE.equals(errorClass))
			{
				result = new String[size][5];
			} else if (ConsistencyCheckHelper.SOLUTION_VALUES_TO_DELETE.equals(errorClass))
			{
				result = new String[size][6];
			}

			for (Iterator iter=elements.iterator(); iter.hasNext();)
			{
				Object[] singleElement = (Object[])iter.next();
				Properties props  = (Properties)singleElement[1];
				if (ConsistencyCheckHelper.SOLUTION_PRINCIPALS_TO_DELETE.equals(errorClass))
				{
					result[counter][0] = (String)singleElement[0];
					result[counter][1] = (String)props.get(ConsistencyCheckResponse.SOLUTION_DATA_PIDS);
					result[counter][2] = (String)props.get(IConsistencyCheckResponse.KEY_DISPLAY_MESSAGE);
				} 
				else if (ConsistencyCheckHelper.SOLUTION_ATTRIBUTES_TO_DELETE.equals(errorClass))
				{
					result[counter][0] = (String)singleElement[0];
					result[counter][1] = (String)props.get(ConsistencyCheckResponse.SOLUTION_DATA_PIDS);
					result[counter][2] = (String)props.get(ConsistencyCheckResponse.SOLUTION_DATA_NAMESPACE);
					result[counter][3] = (String)props.get(ConsistencyCheckResponse.SOLUTION_DATA_ATTRIBUTE);
					result[counter][4] = (String)props.get(IConsistencyCheckResponse.KEY_DISPLAY_MESSAGE);
				} 
				else if (ConsistencyCheckHelper.SOLUTION_VALUES_TO_DELETE.equals(errorClass))
				{
					result[counter][0] = (String)singleElement[0];
					result[counter][1] = (String)props.get(ConsistencyCheckResponse.SOLUTION_DATA_PIDS);
					result[counter][2] = (String)props.get(ConsistencyCheckResponse.SOLUTION_DATA_NAMESPACE);
					result[counter][3] = (String)props.get(ConsistencyCheckResponse.SOLUTION_DATA_ATTRIBUTE);
					result[counter][4] = (String)props.get(ConsistencyCheckResponse.SOLUTION_DATA_VALUES);
					result[counter][5] = (String)props.get(IConsistencyCheckResponse.KEY_DISPLAY_MESSAGE);
				}
				counter++;
			}
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see com.sap.security.core.jmx.IJmxServer#ccToolGetRepairLogResults(javax.management.openmbean.CompositeData[])
	 */
	public String ccToolGetRepairLogResults(CompositeData[] state) throws Exception {
		return ConsistencyCheckHelper.getInstance().getRepairLogResults();
	}

	/* (non-Javadoc)
	 * @see com.sap.security.core.jmx.IJmxServer#ccToolGetWorkingDirectory(javax.management.openmbean.CompositeData[])
	 */
	public String ccToolGetWorkingDirectory(CompositeData[] state) throws Exception {
		return ConsistencyCheckHelper.getInstance().getWorkingDirectory();
	}

	/* (non-Javadoc)
	 * @see com.sap.security.core.jmx.IJmxServer#ccToolInconsistenciesFound(javax.management.openmbean.CompositeData[])
	 */
	public boolean ccToolInconsistenciesFound(CompositeData[] state) throws Exception {
		return ConsistencyCheckHelper.getInstance().inconsistenciesFound();
	}

	/* (non-Javadoc)
	 * @see com.sap.security.core.jmx.IJmxServer#ccToolInterruptChecking(javax.management.openmbean.CompositeData[])
	 */
	public void ccToolInterruptChecking(CompositeData[] state) throws Exception {
		ConsistencyCheckHelper.getInstance().interruptChecking();
	}

	/* (non-Javadoc)
	 * @see com.sap.security.core.jmx.IJmxServer#ccToolInterruptRepairing(javax.management.openmbean.CompositeData[])
	 */
	public void ccToolInterruptRepairing(CompositeData[] state) throws Exception {
		ConsistencyCheckHelper.getInstance().interruptRepairing();
	}

	/* (non-Javadoc)
	 * @see com.sap.security.core.jmx.IJmxServer#ccToolIsCheckLogAvailable(javax.management.openmbean.CompositeData[])
	 */
	public boolean ccToolIsCheckLogAvailable(CompositeData[] state) throws Exception {
		return ConsistencyCheckHelper.getInstance().isCheckLogAvailable();
	}

	/* (non-Javadoc)
	 * @see com.sap.security.core.jmx.IJmxServer#ccToolIsCheckStarted(javax.management.openmbean.CompositeData[])
	 */
	public boolean ccToolIsCheckStarted(CompositeData[] state) throws Exception {
		return ConsistencyCheckHelper.getInstance().isCheckStarted();
	}

	/* (non-Javadoc)
	 * @see com.sap.security.core.jmx.IJmxServer#ccToolIsRepairLogAvailable(javax.management.openmbean.CompositeData[])
	 */
	public boolean ccToolIsRepairLogAvailable(CompositeData[] state) throws Exception {
		return ConsistencyCheckHelper.getInstance().isRepairLogAvailable();
	}

	/* (non-Javadoc)
	 * @see com.sap.security.core.jmx.IJmxServer#ccToolIsRepairStarted(javax.management.openmbean.CompositeData[])
	 */
	public boolean ccToolIsRepairStarted(CompositeData[] state) throws Exception {
		return ConsistencyCheckHelper.getInstance().isRepairStarted();
	}

	/* (non-Javadoc)
	 * @see com.sap.security.core.jmx.IJmxServer#ccToolIsUndoFileAvailable(javax.management.openmbean.CompositeData[])
	 */
	public boolean ccToolIsUndoFileAvailable(CompositeData[] state) throws Exception {
		return ConsistencyCheckHelper.getInstance().isUndoFileAvailable();
	}

	/* (non-Javadoc)
	 * @see com.sap.security.core.jmx.IJmxServer#ccToolIsWorkingDirectorySet(javax.management.openmbean.CompositeData[])
	 */
	public boolean ccToolIsWorkingDirectorySet(CompositeData[] state) throws Exception {
		return ConsistencyCheckHelper.getInstance().isWorkingDirectorySet();
	}

	/* (non-Javadoc)
	 * @see com.sap.security.core.jmx.IJmxServer#ccToolSetWorkingDirectory(java.lang.String, javax.management.openmbean.CompositeData[])
	 */
	public int ccToolSetWorkingDirectory(String workingDirectory, CompositeData[] state) throws Exception {
		return ConsistencyCheckHelper.getInstance().setWorkingDirectory(workingDirectory);
	}

	/* (non-Javadoc)
	 * @see com.sap.security.core.jmx.IJmxServer#ccToolStartChecking(javax.management.openmbean.CompositeData[])
	 */
	public int ccToolStartChecking(CompositeData[] state) throws Exception {
		return ConsistencyCheckHelper.getInstance().startChecking();
	}

	/* (non-Javadoc)
	 * @see com.sap.security.core.jmx.IJmxServer#ccToolStartRepairing(java.lang.String[], boolean, javax.management.openmbean.CompositeData[])
	 */
	public int ccToolStartRepairing(String[] repairIDs, boolean notifyRegisteredListeners, CompositeData[] state) throws Exception {
		return ConsistencyCheckHelper.getInstance().startRepairing(repairIDs,notifyRegisteredListeners);
	}

	/* (non-Javadoc)
	 * @see com.sap.security.core.jmx.IJmxServer#ccToolStartRepairing(java.lang.String[], javax.management.openmbean.CompositeData[])
	 */
	public int ccToolStartRepairing(String[] repairIDs, CompositeData[] state) throws Exception {
		return ConsistencyCheckHelper.getInstance().startRepairing(repairIDs);
	}

	/* (non-Javadoc)
	 * @see com.sap.security.core.jmx.IJmxServer#ccToolDeleteFilesRecursively(java.lang.String, javax.management.openmbean.CompositeData[])
	 */
	public void ccToolDeleteFilesRecursively(String basepath, CompositeData[] state) throws Exception {
		File f = new File(basepath);
		ConsistencyCheckHelper.deleteFilesRecursively(f);
	}

	public IJmxTable calculateSimpleEntityTableByDatasources(
			String searchString, String type, String[] datasourceIds,
			CompositeData[] searchAttributes, String companyId, String guid,
			CompositeData[] state) throws Exception {
		IJmxState jmxState = new JmxState(state);
        try {
			IJmxTable result = JmxSearchHelper.calculateSimpleEntityTable(searchString,
					type, datasourceIds, searchAttributes, companyId, guid, jmxState);
			if (myLoc.beInfo()){
				myLoc.infoT("calculateSimpleEntityTableByDatasources", new Object[]{result});
			}
			return result;
        } catch (Exception e) {
            handleThrowable(e, jmxState);
            return null;
        }
	}

	public IJmxTable getSimpleEntityTableByDatasources(String searchString,
			String type, String[] datasourceIds,
			CompositeData[] searchAttributes, CompositeData[] population,
			String companyId, String guid, CompositeData[] state)
			throws Exception {
		IJmxState jmxState = new JmxState(state);
        try {
			IJmxTable result = JmxSearchHelper.getSimpleEntityTable(searchString, type,
					datasourceIds, searchAttributes, population, companyId,
					guid, jmxState);
			if (myLoc.beInfo()){
				myLoc.infoT("getSimpleEntityTableByDatasources", new Object[]{result});
			}
			return result;
        } catch (Exception e) {
			handleThrowable(e, jmxState);
            return null;
        }
	}

	public IJmxTable calculateEntityMemberTableByDatasources(String uniqueId,
			String searchString, String memberType, String[] datasourceIds,
			CompositeData[] searchAttributes, boolean recursiveSearch,
			String companyId, String guid, CompositeData[] state)
			throws Exception {
		IJmxState jmxState = new JmxState(state);
        try {
			IJmxTable result = JmxSearchHelper.calculateEntityMemberTable(uniqueId,
					searchString, memberType, datasourceIds, searchAttributes,
					recursiveSearch, companyId, guid, jmxState);
			if (myLoc.beInfo()){
				myLoc.infoT("calculateEntityMemberTableByDatasources", new Object[]{result});
			}
			return result;
        } catch (Exception e) {
			handleThrowable(e, jmxState);
            return null;
        }
	}

	public IJmxTable getEntityMemberTableByDatasources(String uniqueId,
			String searchString, String memberType, String[] datasourceIds,
			CompositeData[] searchAttributes, CompositeData[] population,
			boolean recursiveSearch, String companyId, String guid,
			CompositeData[] state) throws Exception {
		IJmxState jmxState = new JmxState(state);
        try {
			IJmxTable result = JmxSearchHelper.getEntityMemberTable(uniqueId, searchString,
					memberType, datasourceIds, searchAttributes, population,
					recursiveSearch, companyId,
                    guid, jmxState);
			if (myLoc.beInfo()){
				myLoc.infoT("getEntityMemberTableByDatasources", new Object[]{result});
			}
			return result;
        } catch (Exception e) {
			handleThrowable(e, jmxState);
            return null;
        }
	}

	public IJmxTable calculateEntityParentTableByDatasources(String uniqueId,
			String searchString, String parentType, String[] datasourceIds,
			CompositeData[] searchAttributes, boolean recursiveSearch,
			String companyId, String guid, CompositeData[] state)
			throws Exception {
		IJmxState jmxState = new JmxState(state);
        try {
			IJmxTable result = JmxSearchHelper.calculateEntityParentTable(uniqueId,
					searchString, parentType, datasourceIds, searchAttributes,
					recursiveSearch, companyId, guid, jmxState);
			if (myLoc.beInfo()){
				myLoc.infoT("calculateEntityParentTableByDatasources", new Object[]{result});
			}
			return result;
        } catch (Exception e) {
			handleThrowable(e, jmxState);
            return null;
        }
	}

	public IJmxTable getEntityParentTableByDatasources(String uniqueId,
			String searchString, String parentType, String[] datasourceIds,
			CompositeData[] searchAttributes, CompositeData[] population,
			boolean recursiveSearch, String companyId, String guid,
			CompositeData[] state) throws Exception {
		IJmxState jmxState = new JmxState(state);
        try {
			IJmxTable result = JmxSearchHelper.getEntityParentTable(uniqueId, searchString,
					parentType, datasourceIds, searchAttributes, population,
					recursiveSearch, companyId,
                    guid, jmxState);
			if (myLoc.beInfo()){
				myLoc.infoT("getEntityParentTableByDatasources", new Object[]{result});
			}
			return result;
        } catch (Exception e) {
			handleThrowable(e, jmxState);
            return null;
        }
	}

	public IJmxTable calculateEntityTableByDatasources(
			CompositeData mainDetail, CompositeData additionalDetail,
			String[] mainDatasourceIds, String[] additionalDatasourceIds,
			boolean showUnapprovedUsers, String companyId, String guid,
			CompositeData[] state) throws Exception {
		IJmxState jmxState = new JmxState(state);
        try {
			IJmxTable result = JmxSearchHelper.calculateEntityTable(mainDetail,
					additionalDetail, mainDatasourceIds, additionalDatasourceIds, showUnapprovedUsers,
					companyId, guid, jmxState);
			if (myLoc.beInfo()){
				myLoc.infoT("calculateEntityTableByDatasources", new Object[]{result});
			}
			return result;
        } catch (Exception e) {
			handleThrowable(e, jmxState);
            return null;
        }
	}

	public IJmxTable getEntityTableByDatasources(CompositeData mainDetail,
			CompositeData additionalDetail, String[] mainDatasourceIds,
			String[] additionalDatasourceIds,
			CompositeData[] mainPopulationAttributes,
			CompositeData[] additionalPopulationAttributes,
			boolean showUnapprovedUsers, String companyId, String guid,
			CompositeData[] state) throws Exception {
		IJmxState jmxState = new JmxState(state);
        try {
			IJmxTable result = JmxSearchHelper.getEntityTable(mainDetail,
					additionalDetail, mainPopulationAttributes,
					additionalPopulationAttributes, mainDatasourceIds,
					additionalDatasourceIds, showUnapprovedUsers, companyId, guid, jmxState);
			if (myLoc.beInfo()){
				myLoc.infoT("getEntityTableByDatasources", new Object[]{result});
			}
			return result;
        } catch (Exception e) {
			handleThrowable(e, jmxState);
            return null;
        }
	}
	
	public IJmxResult validateCurrentConfiguration(CompositeData[] properties, CompositeData[] state) throws Exception {
		JmxResult result = new JmxResult();
		int status = IJmxResult.STATUS_UNDEFINED;
		String message = "New UME configuration can be instantiated";
        String _SSLMessage = null;
		IPrincipalDatabagFactory _instance = null;
		try {
			Properties newProps = JmxUtils.convertJmxMapEntriesToProperties(properties);
			String partnerLDAPActive = (String) newProps.getProperty(PARTNER_LDAP_ACTIVE);
			//newProps.remove(PARTNER_LDAP_ACTIVE);
			if ((_instance = getPrincipalDatabagFactory(newProps)) == null) 
				throw new Exception("PrincipalDatabag Factory is null and could not be initialized correctly");
			else {
				status = IJmxResult.STATUS_OK;
                //check if ABAP or LDAP data source is active
                boolean abapActive = false;
                boolean ldapActive = false;
                IDataSourceMetaData[] metaData = _instance.getDataSourceMetaData();
                if (metaData != null) {
                    for (int i = 0; i < metaData.length; i++) {
                        if (metaData[i].getClassName().equals("com.sap.security.core.persistence.datasource.imp.R3Persistence")) {
                            abapActive = true;
                        }
                        else if (metaData[i].getClassName().equals("com.sap.security.core.persistence.datasource.imp.LDAPPersistence")) {
                            ldapActive = true;
                        }
                    }
                }
				if (abapActive && partnerLDAPActive != null && partnerLDAPActive.equalsIgnoreCase(TRUE)) {
					IDataSourceMetaData abapMetaData = getABAPMetaDataObject(_instance.getDataSourceMetaData());
                    if ( abapMetaData != null) {
    					Map map = (Map) abapMetaData.getProperty(IR3Persistence.PROPERTY_LDAP_SERVERS);
    					String serverID = newProps.getProperty(PARTNER_LDAP_SERVER_ID);
    					String ldapPwd = newProps.getProperty(PARTNER_LDAP_PASSWORD);
    					if (ldapPwd == null || ldapPwd.length() == 0) {
    						char[] pwdChar = InternalUMFactory.getConfiguration().getSecurePropertyDynamic(PARTNER_LDAP_PASSWORD);
    						if (pwdChar == null || pwdChar.length == 0) 
    							ldapPwd = "";
    						else 
    							ldapPwd = new String(pwdChar);
    					}
    					if (((ILDAPServerInfo)map.get(serverID)).checkAdminPassword(ldapPwd))
    						status = IJmxResult.STATUS_OK;
    					else {
    						status = IJmxResult.STATUS_NOT_OK;
    						message = "LDAP Sync Password can not be verified";
    					}
                    }
                    else {
                        status = IJmxResult.STATUS_UNDEFINED;
                        message = "No data regarding ABAP LDAP integration";
                    }
				}
                if (status == IJmxResult.STATUS_OK) {
                    final String TRUE = "true";
                    if (ldapActive && newProps.getProperty("ume.ldap.access.ssl")!=null &&
                            newProps.getProperty("ume.ldap.access.ssl").equalsIgnoreCase(TRUE)) {
                        //check in case of LDAPPersistence and SSL if the connection is really working?
                        IDataSourceMetaData[] metas = _instance.getDataSourceMetaData();
                        if (metas != null) {
                            for (int i = 0; i < metas.length; i++) {
                                try {
                                    if (metas[i].getClassName().equals("com.sap.security.core.persistence.datasource.imp.LDAPPersistence")) {
                                        if (TRUE.equalsIgnoreCase((String)metas[i].getProperty("PROP_SSL_ENABLED"))) {
                                            if (!TRUE.equalsIgnoreCase((String)metas[i].getProperty("PROP_SSL_WORKING"))) {
                                                status = IJmxResult.STATUS_NOT_OK;
                                                try {
                                                    message = (String)metas[i].getProperty("PROP_SSL_EXCEPTION_TXT");
                                                }
                                                catch (Exception e) {
                                                    myLoc.traceThrowableT(Severity.INFO, "Exception text for SSL problem can not be read from UI", e);
                                                    message = "LDAP SSL connection is not working, check trace file for details";
                                                }
                                            }
                                        }
                                    }
                                }
                                catch (Exception e) {
                                    myLoc.traceThrowableT(Severity.INFO, "Can not determine if SSL is working", e);
                                    _SSLMessage = "Can not determine if LDAP SSL is working, check trace file for details";
                                }
                            }
                        }
                    }
                }
                if (status == IJmxResult.STATUS_OK) {    
                    if (myLoc.beInfo()) {
                        myLoc.infoT("UME configuration is working in general, checking guest users");
                    }
                    try {
                        if (myLoc.beInfo()) {
                            myLoc.infoT("Check if the Guest user is unique");
                        }
                        String guestUserIds = InternalUMFactory.getConfiguration().getStringDynamic("ume.login.guest_user.uniqueids");
                        List _ids = JmxUtils.convertStringToList(guestUserIds, ",");
                        
                        for (int i = 0; i < _ids.size(); i++) {
                            SearchCriteria crit = new SearchCriteria(IPrincipalDatabag.ACCOUNT_TYPE);
                            crit.addEqualsElement(IPrincipal.DEFAULT_NAMESPACE, 
                                    ILoginConstants.LOGON_UID_ALIAS, 
                                    ((String) _ids.get(i)).trim(), 
                                    false);
                            
                            IPersistenceCollection coll = _instance.searchPrincipalDatabags(crit);
                            coll.remove(SuperUser.VIRTUAL_GUEST_ACCOUNT_ID);
                            if (coll.size() > 1) {
                                status = IJmxResult.STATUS_NOT_OK;
                                
                                //write it to the trace file;
                                StringBuffer sb = new StringBuffer();
                                sb.append("Guest user ids are not unique, found multiple objects for ");
                                sb.append(_ids.get(i)).append(" -- ");
                                Iterator it = coll.iterator();
                                while (it.hasNext()) {
                                    sb.append(it.next());
                                    if (it.hasNext()) {
                                        sb.append(" - ");
                                    }
                                }
                                myLoc.errorT("Check as Guest user ids are not unique, found ids {0}", new Object[]{sb.toString()});
                                message = sb.toString();
                                break;
                            }
                            else if (coll.isEmpty() && i == 0) {
                                status = IJmxResult.STATUS_NOT_OK; 
                                //write it to the trace file; 
                                StringBuffer sb = new StringBuffer(); 
                                sb.append("No object found for first guest user id "); 
                                sb.append(_ids.get(i));
                                myLoc.errorT("Check as first Guest user id is not found {0}", new Object[]{sb.toString()});
                            }
                        }
                    }
                    catch (Exception e) {
                        myLoc.traceThrowableT(Severity.ERROR, "Can not search for guest user ids for validation", e);
                        throw new Exception("Can not search for guest user ids for validation");
                    }
                }
                if (status == IJmxResult.STATUS_OK) {
                    if (myLoc.beInfo())
                        myLoc.infoT("Guest users are correctly configured, checking extended search");
                    
                    String searchid = newProps.getProperty("ume.extended_user_search_id");
                    if (searchid != null && searchid.length() > 0) {
                        try {
                            SearchCriteria crit = new SearchCriteria(true, IPrincipalDatabag.USER_TYPE);
                            IPersistenceCollection col = _instance.searchPrincipalDatabags(crit);
                            if (col.size() > 0) {
                                status = IJmxResult.STATUS_OK;
                                message = "Found only one user for provided id";
                            }
                        }
                        catch (Exception e) {
                            myLoc.traceThrowableT(Severity.ERROR, "Exception when performing extended "
                                    +"user search configuration check with id {0}",
                                    new Object[] {searchid}, 
                                    e);
                            throw e;
                        }
                    }
                    else {
                        if (myLoc.beInfo()) {
                            myLoc.infoT("Extended search can not be performed due to missing parameter");
                        }
                    }
                }
                if (_SSLMessage != null)
                    status = IJmxResult.STATUS_UNDEFINED;
                result.setStatus(status);
				JmxMessage msg = new JmxMessage();
                if (_SSLMessage == null) {
                    msg.setMessage(message);
                }
                else {
                    if (status != IJmxResult.STATUS_OK) {
                        msg.setMessage(message+". "+_SSLMessage);
                    }
                    else {
                        msg.setMessage(message);
                    }
                }
				result.setMessages(new JmxMessage[] {msg});
			}
		}
		catch (Throwable t) {
            myLoc.traceThrowableT(Severity.INFO, "During validation of UME configuration the following problem occured", t);
			result.setStatus(IJmxResult.STATUS_NOT_OK);
			JmxMessage msg = new JmxMessage();
			msg.setMessage("Validation of UME configuration failed "+t.getMessage());
			result.setMessages(new JmxMessage[] {msg});
		}
		finally {
			try {
				if (_instance != null)					
					_instance.shutdown();
			}
			catch (Exception e) {
				myLoc.traceThrowableT(Severity.ERROR, "Error when shutting down persistence manager", e);
			}
		}		
		return result;
	}
	
	private IDataSourceMetaData getABAPMetaDataObject(IDataSourceMetaData[] metaData) {
		IDataSourceMetaData _result = null;
		if (metaData != null && metaData.length > 0) {
			for (int i = 0; i < metaData.length && _result == null; i++) {
				if (metaData[i].getClassName().equalsIgnoreCase(R3_PERSISTENCE_CLASS_NAME)) {
					_result = metaData[i];
				}
			}
		}
		return _result;
	}
	
	private static final String R3_PERSISTENCE_CLASS_NAME = "com.sap.security.core.persistence.datasource.imp.R3Persistence";
	private static final String PARTNER_LDAP_ACTIVE = "ume.r3.connection.master.PartnerLDAPActive";
	private static final String PARTNER_LDAP_SERVER_ID = "ume.r3.connection.master.PartnerLDAPServerID";
	private static final String PARTNER_LDAP_PASSWORD = "ume.r3.connection.master.PartnerLDAPAdminPassword";
	private static final String TRUE = "true";

	public IJmxMap[] getLDAPSyncServerData(CompositeData[] properties, CompositeData[] state) throws Exception {
		IJmxMap[] serverData = new JmxMap[] {};
		JmxState jmxState = new JmxState(state);
		IPrincipalDatabagFactory pFactory = null;
		try {
			Properties newProps = JmxUtils.convertJmxMapEntriesToProperties(properties);
			newProps.remove(PARTNER_LDAP_ACTIVE);
			newProps.setProperty(PARTNER_LDAP_ACTIVE, "FALSE");

			pFactory = getPrincipalDatabagFactory(newProps);

			IDataSourceMetaData[] metaData = pFactory.getDataSourceMetaData();
			if (metaData != null && metaData.length > 0) {
				IDataSourceMetaData abapData = getABAPMetaDataObject(metaData);
				if (abapData != null) {
					Map sData = (Map)abapData.getProperty(IR3Persistence.PROPERTY_LDAP_SERVERS);
					if (sData != null) {
						serverData = new JmxMap[sData.size()];
						int counter = 0;
						Iterator keys = sData.keySet().iterator();
						while (keys. hasNext()) {
							String serverID = (String) keys.next();
							ILDAPServerInfo serverInfo = (ILDAPServerInfo) sData.get(serverID);
							JmxMap oneEntry = new JmxMap();
							oneEntry.setKey(serverInfo.getServerID());
							JmxMapEntry[] serverProps = new JmxMapEntry[7];
							serverProps[0] = new JmxMapEntry("ADMIN_USER", serverInfo.getAdminUser());
							serverProps[1] = new JmxMapEntry("BASE_DN", serverInfo.getBaseDN());
							serverProps[2] = new JmxMapEntry("FILTER_ATTRIBUTE", serverInfo.getFilterAttribute());
							serverProps[3] = new JmxMapEntry("HOST", serverInfo.getHost());
							serverProps[4] = new JmxMapEntry("LDAP_PRODUCT", serverInfo.getLDAPProduct());
							serverProps[5] = new JmxMapEntry("PORT", ""+serverInfo.getPort());
							serverProps[6] = new JmxMapEntry("SERVER_ID", serverInfo.getServerID());
							oneEntry.setEntries(serverProps);
							
							serverData[counter] = oneEntry;
							counter++;
						}
					}
				}
			}
		}
		catch (Throwable t) {
			handleThrowable(t, jmxState);
		}
		finally {
			if (pFactory != null)
				try {
					pFactory.shutdown();
				}
				catch (Exception e) {
					myLoc.traceThrowableT(Severity.INFO, "Problem shutting down persistence manager", e);
				}
		}
		return serverData;
	}
		
	private IPrincipalDatabagFactory getPrincipalDatabagFactory(Properties newProps) throws Exception {
		return PrincipalDatabagFactory.newInstance("UI_TEST"+System.currentTimeMillis(), new UMTestConfigExtended(newProps), true);
	}

	public IJmxMap getGenericMap(String identifier, CompositeData[] state) throws Exception {
		IJmxState jmxState = new JmxState(state);
		if (GENERIC_MAP_SAPTIMEZONE.equals(identifier)){
			return TimeZonesBean.getInstance(jmxState.getLocale()).getMap();
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see com.sap.security.core.jmx.IJmxServer#resetPassword(javax.management.openmbean.CompositeData[], javax.management.openmbean.CompositeData[])
	 */
	public IJmxResult resetPassword(CompositeData[] attributes, CompositeData[] state) throws Exception {
		IJmxState jmxState = new JmxState(state);
        try {
			IJmxResult result = JmxModificationHelper.resetPassword(attributes, jmxState);
			if (myLoc.beInfo()){
				myLoc.infoT("resetPassword", new Object[]{result});
			}
			return result;
        } catch (Exception e) {
			handleThrowable(e, jmxState);
            return null;
        }
		finally {
			try {
			}
			catch (Exception e) {
				myLoc.traceThrowableT(Severity.ERROR, "Error when shutting down persistence manager", e);
			}
		}		
	}

	public void invalidateCache(
		String cacheType,
		boolean clusterwide,
		CompositeData[] state)
		throws Exception {
		try {
			R3RoleDataSource.refreshRoleBuffer(false);
			InternalUMFactory.invalidateDefaultCaches(clusterwide);
        } catch (Exception e) {
			IJmxState jmxState = new JmxState(state);
			handleThrowable(e, jmxState);
		}
	}

	public byte[] downloadConfiguration(CompositeData[] state) throws Exception {
		return InternalUMFactory.getConfigExtended().downloadConfiguration();
	}

    private ISaveResult writeConfigurationInternal(CompositeData[] properties)
    throws OpenDataException, UMConfigurationException {
        IUMConfigExtended configExtended = InternalUMFactory.getConfigExtended();

        Properties nonsecureProperties = new Properties();
        Properties    secureProperties = new Properties();

        for (int i = 0; i < properties.length; i++) {
            IJmxMapEntry entry = new JmxMapEntry(properties[i]);
            String propertyName  = entry.getKey();
            String propertyValue = entry.getValue();

            // Split properties into non-secure and secure properties.
            if(configExtended.isPropertySecure(propertyName)) {
                secureProperties.setProperty(propertyName, propertyValue);
            }
            else {
                nonsecureProperties.setProperty(propertyName, propertyValue);
            }
        }

        return InternalUMFactory.getConfigAdmin().setProperties(nonsecureProperties, secureProperties);
    }

}

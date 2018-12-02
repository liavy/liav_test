package com.sap.security.core.jmx;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.OpenDataException;

import com.sap.security.api.IPrincipalFactory;
import com.sap.security.core.tools.ConsistencyCheckHelper;
import com.sap.security.core.tools.IConsistencyCheckResponse;
import com.sap.security.core.util.config.IUMConfigAdmin;

/**
 * This interface provides basic UME Jmx functionality.
 * Index: Entity describes the general principal, which can be User, Group, Role, Action etc.
 */
public interface IJmxServer {

	public static final String MODIFY_ACTION_USER_LOCK = "MODIFY_ACTION_USER_LOCK";

	public static final String MODIFY_ACTION_USER_UNLOCK = "MODIFY_ACTION_USER_UNLOCK";

	public static final String MODIFY_ACTION_USER_EXPIREPASSWORD = "MODIFY_ACTION_USER_EXPIREPASSWORD";

	public static final String MODIFY_ACTION_PRINCIPAL_DELETE = "MODIFY_ACTION_PRINCIPAL_DELETE";

	public static final String MODIFY_ACTION_USER_APPROVE = "MODIFY_ACTION_USER_APPROVE";

	public static final String MODIFY_ACTION_USER_DENY = "MODIFY_ACTION_USER_DENY";

	//TODO DELETE
	public static final String MODIFY_ACTION_PASSWORD_GENERATE = "MODIFY_ACTION_PASSWORD_GENERATE";

	public static final String MODIFY_ACTION_PASSWORD_CREATE_GENERATE_ADMIN = "MODIFY_ACTION_PASSWORD_CREATE_GENERATE";

	public static final String MODIFY_ACTION_PASSWORD_CHANGE_GENERATE_ADMIN = "MODIFY_ACTION_PASSWORD_CHANGE_GENERATE";

	//TODO DELETE
	public static final String MODIFY_ACTION_PASSWORD_DISABLE = "MODIFY_ACTION_PASSWORD_DISABLE";

	public static final String MODIFY_ACTION_PASSWORD_CREATE_DISABLE_ADMIN = "MODIFY_ACTION_PASSWORD_CREATE_DISABLE_ADMIN";

	public static final String MODIFY_ACTION_PASSWORD_CHANGE_DISABLE_ADMIN = "MODIFY_ACTION_PASSWORD_CHANGE_DISABLE_ADMIN";

	public static final String MODIFY_ACTION_PASSWORD_CHANGE_DISABLE_PROFILE = "MODIFY_ACTION_PASSWORD_CHANGE_DISABLE_PROFILE";

	public static final String MODIFY_ACTION_PASSWORD_CREATE_SELFREG = "MODIFY_ACTION_PASSWORD_CREATE_SELFREG";

	public static final String MODIFY_ACTION_PASSWORD_CREATE_ADMIN = "MODIFY_ACTION_PASSWORD_CREATE_ADMIN";

	public static final String MODIFY_ACTION_PASSWORD_CHANGE_PROFILE = "MODIFY_ACTION_PASSWORD_CHANGE_PROFILE";

	public static final String MODIFY_ACTION_PASSWORD_CHANGE_ADMIN = "MODIFY_ACTION_PASSWORD_CHANGE_ADMIN";

	public static final String UMAP_NAMESP_SYSTEM = "System";

	public static final String UMAP_NAMESP_LOGON_DATA = "LogonData";

	public static final String UMAP_NAMESP_MAPPING_DETAILS = "UmapDetail";

	public static final String UMAP_NAMESP_INVERSE_MAPPING = "InvMap";

	public static final String UMAP_NAMESP_CONV = "Conv";

	public static final String UMAP_ATTR_CRYPTO_STATUS = "Crypto";

	public static final String UMAP_ATTR_IS_REFERENCE_SYSTEM = "IsRefSys";

	public static final String UMAP_ATTR_HAS_MAPPING_DATA = "ExistingData";

	public static final String UMAP_ATTR_INDIRECT_MAPPING_SRC = "IndirectMappingSrc";

	public static final String UMAP_ATTR_INDIRECT_MAPPING_SRC_DISPLAY = "IndirectMappingSrcDisplay";

	public static final String UMAP_ATTR_INDIRECT_MAPPING_SRC_TYPE = "IndirectMappingSrcType";

	public static final String UMAP_ATTR_FIELDS = "Fields";

	public static final String UMAP_ATTR_DISPLAY_NAME = "DisplayName";

	public static final int RESULT_OPERATION_OK = 0x00;

	public static final int RESULT_ASSIGNED_ALREADY = 0x01;

	public static final int RESULT_REMOVED_ALREADY = 0x02;

	public static final int RESULT_ASSIGN_ERROR = 0x04;

	public static final int RESULT_REMOVE_ERROR = 0x08;

	public static final int RESULT_ASSIGNMENT_NO_ACCESS = 0x10;

	public static final int RESULT_ASSIGN_COMPANY_GROUP_ONLY_SINGLE = 0x20;

	public static final int RESULT_ASSIGN_COMPANY_GROUPS_NO_GROUP_MEMBER = 0x40;

	public static final int RESULT_ASSIGN_COMPANY_GROUPS_PERFORMED = 0x80;
	
	public static final int RESULT_COMPANY_APPROVE_ERROR = 0x100;
	
	public static final int RESULT_COMPANY_DENY_ERROR = 0x200;

    /* Results of writeConfigurationExt() */
    public static final int RESULT_CFG_SERVER_RESTART_REQUIRED = 1;
    
	public static final String STATE_LOCALE = "locale";

	public static final String FEATURE_ORG_RESTRICTIONS_ENABLED = "OrgRestrictionsEnabled";

	public static final String FEATURE_COMPANY_CONCEPT_ENABLED = "CompanyConceptEnabled";
	
	public static final String FEATURE_DATASOURCE_SEARCH_ENABLED = "DatasourceSearchEnabled";
	
	public static final String FEATURE_LDAP_INTEGRATION_ENABLED = "LDAPIntegrationEnabled";
	
	public static final String FEATURE_GENERIC_MAP_ENABLED = "GenericMapEnabled";
	
	public static final String FEATURE_SAPTIMEZONE_ENABLED = "SAPTimeZoneEnabled";
	
	public static final String FEATURE_MODIFY_ENTITY_ASSIGNMENTS_ENABLED = "ModifyEntityAssignmentsEnabled";
	
	public static final String GENERIC_MAP_SAPTIMEZONE = "SAPTimeZone";
	
	public static final String FEATURE_DISPLAY_NAMES_FOR_SYSTEMS = "DisplayNamesForSystems";
	
	public static final String FEATURE_MESSAGING_ENABLED = "MessagingEnabled";

	public static final String FEATURE_DOWNLOAD_CONFIG = "DownloadConfig";
	
	public static final String FEATURE_IS_A1S_SYSTEM = "isA1SSystem";
	
	public static final String FEATURE_READ_CONFIGURATION_EXT = "readConfigurationExt";

    public static final String FEATURE_WRITE_CONFIGURATION_EXT = "writeConfigurationExt";

	/* Message Type Constants */
	public static final int MESSAGE_TYPE_ERROR = 1;

	public static final int MESSAGE_TYPE_WARNING = 2;

	public static final int MESSAGE_TYPE_INFO = 4;

	public static final int MESSAGE_TYPE_SUCCESS = 8;

	/* Message Lifetime Constants */
	public static final int MESSAGE_LIFETIME_PERMANENT = 1;

	public static final int MESSAGE_LIFETIME_ONCE = 2;

	public static final int MESSAGE_LIFETIME_ONCE_TRX = 4;

	/* Message Category Constants */
	public static final int MESSAGE_CATEGORY_OBJECT = 1;

	public static final int MESSAGE_CATEGORY_PROCESS = 2;

	/**
	 * This method responds all available attributes for an entity
	 * 
	 * @param uniqueId
	 *            UniqueId of entity
	 * @param companyId
	 *            The current selected company
	 * @param state
	 *            An {@link IJmxMapEntry} array, which contains state
	 *            information like {@link #STATE_LOCALE}
	 * @return An {@link IJmxEntity} with all available attributes.
	 *         {@link IJmxEntity#UNIQUEID} contains the entity uniqueId,
	 *         {@link IJmxEntity#TYPE} contains the entity type,
	 *         {@link IJmxEntity#MODIFYABLE} contains the modification state of
	 *         the entity. The {@link IJmxEntity#ATTRIBUTES} are build as
	 *         follows: {@link IJmxAttribute#NAMESPACE} contains the attribute
	 *         namespace, {@link IJmxAttribute#NAME} contains the attribute
	 *         name, {@link IJmxAttribute#VALUE} contains the attribute value,
	 *         {@link IJmxAttribute#VALUES} contains the attribute values as
	 *         array.
	 * @throws OpenDataException,
	 *             UMException
	 */
	public IJmxEntity getAllEntityDetails(String uniqueId, String companyId,
			CompositeData[] state) throws Exception;

	/**
	 * This method responds the populated attributes for an entity
	 * 
	 * @param uniqueId
	 *            UniqueId of entity
	 * @param population
	 *            A {@link IJmxAttribute} array, which specifies the attributes
	 *            to be populated. Used fields: {@link IJmxAttribute#NAMESPACE}
	 *            for the attribute namespace and {@link IJmxAttribute#NAME} for
	 *            the attribute name
	 * @param companyId
	 *            The current selected company
	 * @param state
	 *            An array of {@link IJmxMapEntry}, which contains state
	 *            information like: Key {@link #STATE_LOCALE}, Value =
	 *            {@link java.lang.String} representation of
	 *            {@link java.util.Locale}
	 * @return An {@link IJmxEntity} with all available attributes.
	 *         {@link IJmxEntity#UNIQUEID} contains the entity uniqueId,
	 *         {@link IJmxEntity#TYPE} contains the entity type,
	 *         {@link IJmxEntity#MODIFYABLE} contains the modification state of
	 *         the entity. The {@link IJmxEntity#ATTRIBUTES} are build as
	 *         follows: {@link IJmxAttribute#NAMESPACE} contains the attribute
	 *         namespace, {@link IJmxAttribute#NAME} contains the attribute
	 *         name, {@link IJmxAttribute#VALUE} contains the attribute value,
	 *         {@link IJmxAttribute#VALUES} contains the attribute values as
	 *         array.
	 * @throws OpenDataException,
	 *             UMException
	 */
	public IJmxEntity getEntityDetails(String uniqueId,
			CompositeData[] population, String companyId, CompositeData[] state)
			throws Exception;

	/**
	 * This method calculates the data for an entity table by simple search
	 * 
	 * @param searchString
	 *            The search string to search for
	 * @param type
	 *            The type to search for
	 * @param companyId
	 *            The current selected company
	 * @param guid
	 *            A guid to save the search result
	 * @param state
	 *            An array of {@link IJmxMapEntry}, which contains state
	 *            information like: Key {@link #STATE_LOCALE}, Value =
	 *            {@link java.lang.String} representation of
	 *            {@link java.util.Locale}
	 * @return An {@link IJmxTable} object with filled {@link IJmxTable#SIZE}
	 *         and {@link IJmxTable#STATE} attributes. The
	 *         {@link IJmxTable#TABLEROWS} are empty.
	 * @throws OpenDataException,
	 *             UMException
	 */
	public IJmxTable calculateSimpleEntityTable(String searchString,
			String type, String companyId, String guid, CompositeData[] state)
			throws Exception;

	/**
	 * This method responds an entity table by simple search
	 * 
	 * @param searchString
	 *            The search string to search for
	 * @param type
	 *            The type to search for
	 * @param population
	 *            A {@link IJmxAttribute} array, which specifies the attributes
	 *            to be populated. Used fields: {@link IJmxAttribute#NAMESPACE}
	 *            for the attribute namespace and {@link IJmxAttribute#NAME} for
	 *            the attribute name
	 * @param companyId
	 *            The current selected company
	 * @param guid
	 *            A guid to get a previous evaluated search result
	 * @param state
	 *            An array of {@link IJmxMapEntry}, which contains state
	 *            information like: Key {@link #STATE_LOCALE}, Value =
	 *            {@link java.lang.String} representation of
	 *            {@link java.util.Locale}
	 * @return An {@link IJmxTable} object with filled {@link IJmxTable#SIZE}
	 *         and {@link IJmxTable#STATE} attributes. The
	 *         {@link IJmxTable#TABLEROWS} are filled with columns ordered by
	 *         the appearance of populated attributes, for example
	 *         {@link IJmxTableRow#COL0}, {@link IJmxTableRow#COL1} etc.
	 *         {@link IJmxTableRow#COL9} contains the already translated
	 *         datasource id of the entity. Furthermore,
	 *         {@link IJmxTableRow#COLUNIQUEID} contains the uniqueId for the
	 *         entity represented by the {@link IJmxTableRow},
	 *         {@link IJmxTableRow#COLDELETEABLE} contains the deleteable status
	 *         for the entity, {@link IJmxTableRow#COLTYPE} contains the type
	 *         for the entity. If the {@link IJmxTableRow#COLTYPE} represents an
	 *         {@link com.sap.security.api.IUser},
	 *         {@link IJmxTableRow#COLREFUNIQUEID} is filled with the
	 *         {@link com.sap.security.api.IUserAccount} uniqueId and
	 *         {@link IJmxTableRow#COLSTATUS0} with the current lock status of
	 *         the {@link com.sap.security.api.IUserAccount}.
	 * @throws OpenDataException,
	 *             UMException
	 */
	public IJmxTable getSimpleEntityTable(String searchString, String type,
			CompositeData[] population, String companyId, String guid,
			CompositeData[] state) throws Exception;

	/**
	 * This method calculates the data for an entity table by simple search on
	 * given datasources
	 * 
	 * @param searchString
	 *            The search string to search for
	 * @param type
	 *            The type to search for
	 * @param datasourceIds
	 *            The datasources to search on
	 * @param searchAttributes
	 *            additional search attributes
	 * @param companyId
	 *            The current selected company
	 * @param guid
	 *            A guid to save the search result
	 * @param state
	 *            An array of {@link IJmxMapEntry}, which contains state
	 *            information like: Key {@link #STATE_LOCALE}, Value =
	 *            {@link java.lang.String} representation of
	 *            {@link java.util.Locale}
	 * @return An {@link IJmxTable} object with filled {@link IJmxTable#SIZE}
	 *         and {@link IJmxTable#STATE} attributes. The
	 *         {@link IJmxTable#TABLEROWS} are empty.
	 * @throws OpenDataException,
	 *             UMException
	 */
	public IJmxTable calculateSimpleEntityTableByDatasources(
			String searchString, String type, String[] datasourceIds,
			CompositeData[] searchAttributes, String companyId, String guid,
			CompositeData[] state) throws Exception;
	
	/**
	 * This method responds an entity table by simple search on given
	 * datasources
	 * 
	 * @param searchString
	 *            The search string to search for
	 * @param type
	 *            The type to search for
	 * @param datasourceIds
	 *            The datasources to search on
	 * @param searchAttributes
	 *            additional search attributes
	 * @param population
	 *            A {@link IJmxAttribute} array, which specifies the attributes
	 *            to be populated. Used fields: {@link IJmxAttribute#NAMESPACE}
	 *            for the attribute namespace and {@link IJmxAttribute#NAME} for
	 *            the attribute name
	 * @param companyId
	 *            The current selected company
	 * @param guid
	 *            A guid to get a previous evaluated search result
	 * @param state
	 *            An array of {@link IJmxMapEntry}, which contains state
	 *            information like: Key {@link #STATE_LOCALE}, Value =
	 *            {@link java.lang.String} representation of
	 *            {@link java.util.Locale}
	 * @return An {@link IJmxTable} object with filled {@link IJmxTable#SIZE}
	 *         and {@link IJmxTable#STATE} attributes. The
	 *         {@link IJmxTable#TABLEROWS} are filled with columns ordered by
	 *         the appearance of populated attributes, for example
	 *         {@link IJmxTableRow#COL0}, {@link IJmxTableRow#COL1} etc.
	 *         {@link IJmxTableRow#COL9} contains the already translated
	 *         datasource id of the entity. Furthermore,
	 *         {@link IJmxTableRow#COLUNIQUEID} contains the uniqueId for the
	 *         entity represented by the {@link IJmxTableRow},
	 *         {@link IJmxTableRow#COLDELETEABLE} contains the deleteable status
	 *         for the entity, {@link IJmxTableRow#COLTYPE} contains the type
	 *         for the entity. If the {@link IJmxTableRow#COLTYPE} represents an
	 *         {@link com.sap.security.api.IUser},
	 *         {@link IJmxTableRow#COLREFUNIQUEID} is filled with the
	 *         {@link com.sap.security.api.IUserAccount} uniqueId and
	 *         {@link IJmxTableRow#COLSTATUS0} with the current lock status of
	 *         the {@link com.sap.security.api.IUserAccount}.
	 * @throws OpenDataException,
	 *             UMException
	 */
	public IJmxTable getSimpleEntityTableByDatasources(String searchString,
			String type, String[] datasourceIds,
			CompositeData[] searchAttributes, CompositeData[] population,
			String companyId, String guid, CompositeData[] state)
			throws Exception;
	
	/**
	 * This method calculates the data for an entity table by advanced search
	 * 
	 * @param detail
	 *            A {@link IJmxEntity} object, which specifies the search
	 *            criteria. Used fields: {@link IJmxEntity#TYPE} the entity type
	 *            to search for and a list of {@link IJmxEntity#ATTRIBUTES},
	 *            which contain the {@link IJmxAttribute#NAMESPACE} as attribute
	 *            namespace, {@link IJmxAttribute#NAME} as attribute name,
	 *            {@link IJmxAttribute#VALUE} as attribute value
	 * @param companyId
	 *            The current selected company
	 * @param guid
	 *            A guid to save the evaluated search result
	 * @param state
	 *            An {@link IJmxMapEntry} array, which contains state
	 *            information like {@link #STATE_LOCALE}
	 * @return An {@link IJmxTable} object with filled {@link IJmxTable#SIZE}
	 *         and {@link IJmxTable#STATE} attributes. The
	 *         {@link IJmxTable#TABLEROWS} are empty.
	 * @throws OpenDataException, UMException
	 */
	public IJmxTable calculateEntityTable(CompositeData entity,
			String companyId, String guid, CompositeData[] state)
			throws Exception;

	/**
	 * This method responds an entity table by advanced search
	 * 
	 * @param detail
	 *            A {@link IJmxEntity} object, which specifies the search
	 *            criteria. Used fields: {@link IJmxEntity#TYPE} the entity type
	 *            to search for and a list of {@link IJmxEntity#ATTRIBUTES},
	 *            which contain the {@link IJmxAttribute#NAMESPACE} as attribute
	 *            namespace, {@link IJmxAttribute#NAME} as attribute name,
	 *            {@link IJmxAttribute#VALUE} as attribute value
	 * @param population
	 *            A {@link IJmxAttribute} array, which specifies the attributes
	 *            to be populated. Used fields: {@link IJmxAttribute#NAMESPACE}
	 *            for the attribute namespace and {@link IJmxAttribute#NAME} for
	 *            the attribute name
	 * @param companyId
	 *            The current selected company
	 * @param guid
	 *            A guid to get a previous evaluated search result
	 * @param state
	 *            An array of {@link IJmxMapEntry}, which contains state
	 *            information like: Key {@link #STATE_LOCALE}, Value =
	 *            {@link java.lang.String} representation of
	 *            {@link java.util.Locale}
	 * @return An {@link IJmxTable} object with filled {@link IJmxTable#SIZE}
	 *         and {@link IJmxTable#STATE} attributes. The
	 *         {@link IJmxTable#TABLEROWS} are filled with columns ordered by
	 *         the appearance of populated attributes, for example
	 *         {@link IJmxTableRow#COL0}, {@link IJmxTableRow#COL1} etc.
	 *         {@link IJmxTableRow#COL9} contains the already translated
	 *         datasource id of the entity. Furthermore,
	 *         {@link IJmxTableRow#COLUNIQUEID} contains the uniqueId for the
	 *         entity represented by the {@link IJmxTableRow},
	 *         {@link IJmxTableRow#COLDELETEABLE} contains the deleteable status
	 *         for the entity, {@link IJmxTableRow#COLTYPE} contains the type
	 *         for the entity. If the {@link IJmxTableRow#COLTYPE} represents an
	 *         {@link com.sap.security.api.IUser},
	 *         {@link IJmxTableRow#COLREFUNIQUEID} is filled with the
	 *         {@link com.sap.security.api.IUserAccount} uniqueId and
	 *         {@link IJmxTableRow#COLSTATUS0} with the current lock status of
	 *         the {@link com.sap.security.api.IUserAccount}.
	 * @throws OpenDataException,
	 *             UMException
	 */
	public IJmxTable getEntityTable(CompositeData entity,
			CompositeData[] population, String companyId, String guid,
			CompositeData[] state) throws Exception;

	/**
	 * This calculates the data for an entity table with an combined search for
	 * two search criterias
	 * 
	 * @param detail
	 *            A {@link IJmxEntity} object, which specifies the search
	 *            criteria for the main entity (e.g.
	 *            {@link com.sap.security.api.IUser}). Used fields:
	 *            {@link IJmxEntity#TYPE} the entity type to search for and a
	 *            list of {@link IJmxEntity#ATTRIBUTES}, which contain the
	 *            {@link IJmxAttribute#NAMESPACE} as attribute namespace,
	 *            {@link IJmxAttribute#NAME} as attribute name,
	 *            {@link IJmxAttribute#VALUE} as attribute value
	 * @param additionalDetail
	 *            A {@link IJmxEntity} object, which specifies the search
	 *            criteria for the additional entity (e.g.
	 *            {@link com.sap.security.api.IUserAccount}). Used fields:
	 *            {@link IJmxEntity#TYPE} the entity type to search for and a
	 *            list of {@link IJmxEntity#ATTRIBUTES}, which contain the
	 *            {@link IJmxAttribute#NAMESPACE} as attribute namespace,
	 *            {@link IJmxAttribute#NAME} as attribute name,
	 *            {@link IJmxAttribute#VALUE} as attribute value
	 * @param datasourceIds
	 *            The datasources to search on
	 * @param searchAttributes
	 *            additional search attributes
	 * @param showUnapprovedUsers
	 *            Approved or unapproved {@link com.sap.security.api.IUser}'s should be displayed 
	 * @param companyId
	 *            The current selected company
	 * @param guid
	 *            A guid to save the evaluated search result
	 * @param state
	 *            An array of {@link IJmxMapEntry}, which contains state
	 *            information like: Key {@link #STATE_LOCALE}, Value =
	 *            {@link java.lang.String} representation of
	 *            {@link java.util.Locale}
	 * @return An {@link IJmxTable} object with filled {@link IJmxTable#SIZE}
	 *         and {@link IJmxTable#STATE} attributes. The
	 *         {@link IJmxTable#TABLEROWS} are empty.
	 * @throws OpenDataException,
	 *             UMException
	 */
	public IJmxTable calculateEntityTableByDatasources(
			CompositeData mainDetail, CompositeData additionalDetail,
			String[] mainDatasourceIds, String[] additionalDatasourceIds,
			boolean showUnapprovedUsers, String companyId, String guid,
			CompositeData[] state) throws Exception;

	/**
	 * This method responds an entity table with an combined search for two
	 * search criterias
	 * 
	 * @param detail
	 *            A {@link IJmxEntity} object, which specifies the search
	 *            criteria for the main entity (e.g.
	 *            {@link com.sap.security.api.IUser}). Used fields:
	 *            {@link IJmxEntity#TYPE} the entity type to search for and a
	 *            list of {@link IJmxEntity#ATTRIBUTES}, which contain the
	 *            {@link IJmxAttribute#NAMESPACE} as attribute namespace,
	 *            {@link IJmxAttribute#NAME} as attribute name,
	 *            {@link IJmxAttribute#VALUE} as attribute value
	 * @param additionalDetail
	 *            A {@link IJmxEntity} object, which specifies the search
	 *            criteria for the additional entity (e.g.
	 *            {@link com.sap.security.api.IUserAccount}). Used fields:
	 *            {@link IJmxEntity#TYPE} the entity type to search for and a
	 *            list of {@link IJmxEntity#ATTRIBUTES}, which contain the
	 *            {@link IJmxAttribute#NAMESPACE} as attribute namespace,
	 *            {@link IJmxAttribute#NAME} as attribute name,
	 *            {@link IJmxAttribute#VALUE} as attribute value     
	 * @param population
	 *            A {@link IJmxAttribute} array, which specifies the attributes
	 *            to be populated. Used fields: {@link IJmxAttribute#NAMESPACE}
	 *            for the attribute namespace and {@link IJmxAttribute#NAME} for
	 *            the attribute name
	 * @param datasourceIds
	 *            The datasources to search on
	 * @param searchAttributes
	 *            additional search attributes
	 * @param showUnapprovedUsers
	 *            Approved or unapproved {@link com.sap.security.api.IUser}'s should be displayed
	 * @param companyId
	 *            The current selected company
	 * @param guid
	 *            A guid to get a previous evaluated search result
	 * @param state
	 *            An array of {@link IJmxMapEntry}, which contains state
	 *            information like: Key {@link #STATE_LOCALE}, Value =
	 *            {@link java.lang.String} representation of
	 *            {@link java.util.Locale}
	 * @return An {@link IJmxTable} object with filled {@link IJmxTable#SIZE}
	 *         and {@link IJmxTable#STATE} attributes. The
	 *         {@link IJmxTable#TABLEROWS} are filled with columns ordered by
	 *         the appearance of populated attributes, for example
	 *         {@link IJmxTableRow#COL0}, {@link IJmxTableRow#COL1} etc.
	 *         {@link IJmxTableRow#COL9} contains the already translated
	 *         datasource id of the entity. Furthermore,
	 *         {@link IJmxTableRow#COLUNIQUEID} contains the uniqueId for the
	 *         entity represented by the {@link IJmxTableRow},
	 *         {@link IJmxTableRow#COLDELETEABLE} contains the deleteable status
	 *         for the entity, {@link IJmxTableRow#COLTYPE} contains the type
	 *         for the entity. If the {@link IJmxTableRow#COLTYPE} represents an
	 *         {@link com.sap.security.api.IUser},
	 *         {@link IJmxTableRow#COLREFUNIQUEID} is filled with the
	 *         {@link com.sap.security.api.IUserAccount} uniqueId and
	 *         {@link IJmxTableRow#COLSTATUS0} with the current lock status of
	 *         the {@link com.sap.security.api.IUserAccount}.
	 * @throws OpenDataException,
	 *             UMException
	 */
	public IJmxTable getEntityTableByDatasources(CompositeData mainDetail,
			CompositeData additionalDetail, String[] mainDatasourceIds,
			String[] additionalDatasourceIds,
			CompositeData[] mainPopulationAttributes,
			CompositeData[] additionalPopulationAttributes,
			boolean showUnapprovedUsers, String companyId, String guid,
			CompositeData[] state) throws Exception;

	/**
	 * This calculates the data for an entity table with an combined search for
	 * two search criterias
	 * 
	 * @param detail
	 *            A {@link IJmxEntity} object, which specifies the search
	 *            criteria for the main entity (e.g.
	 *            {@link com.sap.security.api.IUser}). Used fields:
	 *            {@link IJmxEntity#TYPE} the entity type to search for and a
	 *            list of {@link IJmxEntity#ATTRIBUTES}, which contain the
	 *            {@link IJmxAttribute#NAMESPACE} as attribute namespace,
	 *            {@link IJmxAttribute#NAME} as attribute name,
	 *            {@link IJmxAttribute#VALUE} as attribute value
	 * @param additionalDetail
	 *            A {@link IJmxEntity} object, which specifies the search
	 *            criteria for the additional entity (e.g.
	 *            {@link com.sap.security.api.IUserAccount}). Used fields:
	 *            {@link IJmxEntity#TYPE} the entity type to search for and a
	 *            list of {@link IJmxEntity#ATTRIBUTES}, which contain the
	 *            {@link IJmxAttribute#NAMESPACE} as attribute namespace,
	 *            {@link IJmxAttribute#NAME} as attribute name,
	 *            {@link IJmxAttribute#VALUE} as attribute value
	 * @param showUnapprovedUsers
	 *            Approved or unapproved {@link com.sap.security.api.IUser}'s should be displayed
	 * @param companyId
	 *            The current selected company
	 * @param guid
	 *            A guid to save the evaluated search result
	 * @param state
	 *            An array of {@link IJmxMapEntry}, which contains state
	 *            information like: Key {@link #STATE_LOCALE}, Value =
	 *            {@link java.lang.String} representation of
	 *            {@link java.util.Locale}
	 * @return An {@link IJmxTable} object with filled {@link IJmxTable#SIZE}
	 *         and {@link IJmxTable#STATE} attributes. The
	 *         {@link IJmxTable#TABLEROWS} are empty.
	 * @throws OpenDataException,
	 *             UMException
	 */
	public IJmxTable calculateCombinedEntityTable(CompositeData mainDetail,
			CompositeData additionalDetail, boolean showUnapprovedUsers,
			String companyId, String guid, CompositeData[] state)
			throws Exception;

	/**
	 * This method responds an entity table with an combined search for two
	 * search criterias
	 * 
	 * @param detail
	 *            A {@link IJmxEntity} object, which specifies the search
	 *            criteria for the main entity (e.g.
	 *            {@link com.sap.security.api.IUser}). Used fields:
	 *            {@link IJmxEntity#TYPE} the entity type to search for and a
	 *            list of {@link IJmxEntity#ATTRIBUTES}, which contain the
	 *            {@link IJmxAttribute#NAMESPACE} as attribute namespace,
	 *            {@link IJmxAttribute#NAME} as attribute name,
	 *            {@link IJmxAttribute#VALUE} as attribute value
	 * @param additionalDetail
	 *            A {@link IJmxEntity} object, which specifies the search
	 *            criteria for the additional entity (e.g.
	 *            {@link com.sap.security.api.IUserAccount}). Used fields:
	 *            {@link IJmxEntity#TYPE} the entity type to search for and a
	 *            list of {@link IJmxEntity#ATTRIBUTES}, which contain the
	 *            {@link IJmxAttribute#NAMESPACE} as attribute namespace,
	 *            {@link IJmxAttribute#NAME} as attribute name,
	 *            {@link IJmxAttribute#VALUE} as attribute value     
	 * @param population
	 *            A {@link IJmxAttribute} array, which specifies the attributes
	 *            to be populated. Used fields: {@link IJmxAttribute#NAMESPACE}
	 *            for the attribute namespace and {@link IJmxAttribute#NAME} for
	 *            the attribute name
	 * @param showUnapprovedUsers
	 *            Approved or unapproved {@link com.sap.security.api.IUser}'s should be displayed
	 * @param companyId
	 *            The current selected company
	 * @param guid
	 *            A guid to get a previous evaluated search result
	 * @param state
	 *            An array of {@link IJmxMapEntry}, which contains state
	 *            information like: Key {@link #STATE_LOCALE}, Value =
	 *            {@link java.lang.String} representation of
	 *            {@link java.util.Locale}
	 * @return An {@link IJmxTable} object with filled {@link IJmxTable#SIZE}
	 *         and {@link IJmxTable#STATE} attributes. The
	 *         {@link IJmxTable#TABLEROWS} are filled with columns ordered by
	 *         the appearance of populated attributes, for example
	 *         {@link IJmxTableRow#COL0}, {@link IJmxTableRow#COL1} etc.
	 *         {@link IJmxTableRow#COL9} contains the already translated
	 *         datasource id of the entity. Furthermore,
	 *         {@link IJmxTableRow#COLUNIQUEID} contains the uniqueId for the
	 *         entity represented by the {@link IJmxTableRow},
	 *         {@link IJmxTableRow#COLDELETEABLE} contains the deleteable status
	 *         for the entity, {@link IJmxTableRow#COLTYPE} contains the type
	 *         for the entity. If the {@link IJmxTableRow#COLTYPE} represents an
	 *         {@link com.sap.security.api.IUser},
	 *         {@link IJmxTableRow#COLREFUNIQUEID} is filled with the
	 *         {@link com.sap.security.api.IUserAccount} uniqueId and
	 *         {@link IJmxTableRow#COLSTATUS0} with the current lock status of
	 *         the {@link com.sap.security.api.IUserAccount}.
	 * @throws OpenDataException,
	 *             UMException
	 */
	public IJmxTable getCombinedEntityTable(CompositeData mainDetail,
			CompositeData additionalDetail, CompositeData[] population,
			boolean showUnapprovedUsers, String companyId, String guid,
			CompositeData[] state) throws Exception;

	/**
	 * This method calculates the data for an entity member table
	 * 
	 * @param uniqueId
	 *            Unique ID of the parent
	 * @param searchString
	 *            Criteria for the (simple) search
	 * @param memberType
	 *            Specifies, for which members types the search should be
	 *            performed
	 * @param recursiveSearch
	 *            This method does a recursive search if the parameter
	 *            recursiveSearch is set to true
	 * @param companyId
	 *            The current selected company
	 * @param guid
	 *            A guid to save the search result
	 * @param state
	 *            An array of {@link IJmxMapEntry}, which contains state
	 *            information like: Key {@link #STATE_LOCALE}, Value =
	 *            {@link java.lang.String} representation of
	 *            {@link java.util.Locale}
	 * @return An {@link IJmxTable} object with filled {@link IJmxTable#SIZE}
	 *         and {@link IJmxTable#STATE} attributes. The
	 *         {@link IJmxTable#TABLEROWS} are empty.
	 * @throws OpenDataException,
	 *             UMException
	 */
	public IJmxTable calculateEntityMemberTable(String uniqueId,
			String searchString, String memberType, boolean recursiveSearch,
			String companyId, String guid, CompositeData[] state)
			throws Exception;

	/**
	 * This method responds an entity member table
	 * 
	 * @param uniqueId
	 *            Unique ID of the parent
	 * @param searchString
	 *            Criteria for the (simple) search
	 * @param memberType
	 *            Specifies, for which members types the search should be
	 *            performed
	 * @param population
	 *            A {@link IJmxAttribute} array, which specifies the attributes
	 *            to be populated. Used fields: {@link IJmxAttribute#NAMESPACE}
	 *            for the attribute namespace and {@link IJmxAttribute#NAME} for
	 *            the attribute name
	 * @param recursiveSearch
	 *            This method does a recursive search if the parameter
	 *            recursiveSearch is set to true
	 * @param companyId
	 *            The current selected company
	 * @param guid
	 *            A guid to get a previous evaluated search result
	 * @param state
	 *            An array of {@link IJmxMapEntry}, which contains state
	 *            information like: Key {@link #STATE_LOCALE}, Value =
	 *            {@link java.lang.String} representation of
	 *            {@link java.util.Locale}
	 * @return An {@link IJmxTable} object with filled {@link IJmxTable#SIZE}
	 *         and {@link IJmxTable#STATE} attributes. The
	 *         {@link IJmxTable#TABLEROWS} are filled with columns ordered by
	 *         the appearance of populated attributes, for example
	 *         {@link IJmxTableRow#COL0}, {@link IJmxTableRow#COL1} etc.
	 *         {@link IJmxTableRow#COL9} contains the already translated
	 *         datasource id of the entity. Furthermore,
	 *         {@link IJmxTableRow#COLUNIQUEID} contains the uniqueId for the
	 *         entity represented by the {@link IJmxTableRow},
	 *         {@link IJmxTableRow#COLDELETEABLE} contains the deleteable status
	 *         for the entity, {@link IJmxTableRow#COLTYPE} contains the type
	 *         for the entity. If the {@link IJmxTableRow#COLTYPE} represents an
	 *         {@link com.sap.security.api.IUser},
	 *         {@link IJmxTableRow#COLREFUNIQUEID} is filled with the
	 *         {@link com.sap.security.api.IUserAccount} uniqueId and
	 *         {@link IJmxTableRow#COLSTATUS0} with the current lock status of
	 *         the {@link com.sap.security.api.IUserAccount}.
	 * @throws OpenDataException,
	 *             UMException
	 */
	public IJmxTable getEntityMemberTable(String uniqueId, String searchString,
			String memberType, CompositeData population[],
			boolean recursiveSearch, String companyId, String guid,
			CompositeData[] state) throws Exception;

	/**
	 * This method calculates the data for an entity parent table
	 * 
	 * @param uniqueId
	 *            Unique ID of the member
	 * @param searchString
	 *            Criteria for the (simple) search
	 * @param memberType
	 *            Specifies, for which members types the search should be
	 *            performed
	 * @param recursiveSearch
	 *            This method does a recursive search if the parameter
	 *            recursiveSearch is set to true
	 * @param companyId
	 *            The current selected company
	 * @param guid
	 *            A guid to save the search result
	 * @param state
	 *            An {@link IJmxMapEntry} array, which contains state
	 *            information like {@link #STATE_LOCALE}
	 * @return An {@link IJmxTable} object with filled {@link IJmxTable#SIZE}
	 *         and {@link IJmxTable#STATE} attributes. The
	 *         {@link IJmxTable#TABLEROWS} are empty.
	 * @throws OpenDataException,
	 *             UMException
	 */
	public IJmxTable calculateEntityParentTable(String uniqueId,
			String searchString, String parentType, boolean recursiveSearch,
			String companyId, String guid, CompositeData[] state)
			throws Exception;

	/**
	 * This method responds an entity parent table
	 * 
	 * @param uniqueId
	 *            Unique ID of the member
	 * @param searchString
	 *            Criteria for the (simple) search
	 * @param parentType
	 *            Specifies, for which parent types the search should be
	 *            performed
	 * @param recursiveSearch
	 *            This method does a recursive search if the parameter
	 *            recursiveSearch is set to true
	 * @param companyId
	 *            The current selected company
	 * @param guid
	 *            A guid to get a previous evaluated search result
	 * @param state
	 *            An array of {@link IJmxMapEntry}, which contains state
	 *            information like: Key {@link #STATE_LOCALE}, Value =
	 *            {@link java.lang.String} representation of
	 *            {@link java.util.Locale}
	 * @return An {@link IJmxTable} object with filled {@link IJmxTable#SIZE}
	 *         and {@link IJmxTable#STATE} attributes. The
	 *         {@link IJmxTable#TABLEROWS} are filled with columns ordered by
	 *         the appearance of populated attributes, for example
	 *         {@link IJmxTableRow#COL0}, {@link IJmxTableRow#COL1} etc.
	 *         {@link IJmxTableRow#COL9} contains the already translated
	 *         datasource id of the entity. Furthermore,
	 *         {@link IJmxTableRow#COLUNIQUEID} contains the uniqueId for the
	 *         entity represented by the {@link IJmxTableRow},
	 *         {@link IJmxTableRow#COLDELETEABLE} contains the deleteable status
	 *         for the entity, {@link IJmxTableRow#COLTYPE} contains the type
	 *         for the entity. If the {@link IJmxTableRow#COLTYPE} represents an
	 *         {@link com.sap.security.api.IUser},
	 *         {@link IJmxTableRow#COLREFUNIQUEID} is filled with the
	 *         {@link com.sap.security.api.IUserAccount} uniqueId and
	 *         {@link IJmxTableRow#COLSTATUS0} with the current lock status of
	 *         the {@link com.sap.security.api.IUserAccount}.
	 * @throws OpenDataException,
	 *             UMException
	 */
	public IJmxTable getEntityParentTable(String uniqueId, String searchString,
			String parentType, CompositeData population[],
			boolean recursiveSearch, String companyId, String guid,
			CompositeData[] state) throws Exception;
	
	/**
	 * This method calculates the data for an entity member table
	 * 
	 * @param uniqueId
	 *            Unique ID of the parent
	 * @param searchString
	 *            Criteria for the (simple) search
	 * @param memberType
	 *            Specifies, for which members types the search should be
	 *            performed
	 * @param datasourceIds
	 *            The datasources to search on
	 * @param searchAttributes
	 *            additional search attributes
	 * @param recursiveSearch
	 *            This method does a recursive search if the parameter
	 *            recursiveSearch is set to true
	 * @param companyId
	 *            The current selected company
	 * @param guid
	 *            A guid to save the search result
	 * @param state
	 *            An array of {@link IJmxMapEntry}, which contains state
	 *            information like: Key {@link #STATE_LOCALE}, Value =
	 *            {@link java.lang.String} representation of
	 *            {@link java.util.Locale}
	 * @return An {@link IJmxTable} object with filled {@link IJmxTable#SIZE}
	 *         and {@link IJmxTable#STATE} attributes. The
	 *         {@link IJmxTable#TABLEROWS} are empty.
	 * @throws OpenDataException,
	 *             UMException
	 */
	public IJmxTable calculateEntityMemberTableByDatasources(String uniqueId,
			String searchString, String memberType, String[] datasourceIds,
			CompositeData[] searchAttributes, boolean recursiveSearch,
			String companyId, String guid, CompositeData[] state)
			throws Exception;

	/**
	 * This method responds an entity member table
	 * 
	 * @param uniqueId
	 *            Unique ID of the parent
	 * @param searchString
	 *            Criteria for the (simple) search
	 * @param memberType
	 *            Specifies, for which members types the search should be
	 *            performed
	 * @param datasourceIds
	 *            The datasources to search on
	 * @param searchAttributes
	 *            additional search attributes
	 * @param population
	 *            A {@link IJmxAttribute} array, which specifies the attributes
	 *            to be populated. Used fields: {@link IJmxAttribute#NAMESPACE}
	 *            for the attribute namespace and {@link IJmxAttribute#NAME} for
	 *            the attribute name
	 * @param recursiveSearch
	 *            This method does a recursive search if the parameter
	 *            recursiveSearch is set to true
	 * @param companyId
	 *            The current selected company
	 * @param guid
	 *            A guid to get a previous evaluated search result
	 * @param state
	 *            An array of {@link IJmxMapEntry}, which contains state
	 *            information like: Key {@link #STATE_LOCALE}, Value =
	 *            {@link java.lang.String} representation of
	 *            {@link java.util.Locale}
	 * @return An {@link IJmxTable} object with filled {@link IJmxTable#SIZE}
	 *         and {@link IJmxTable#STATE} attributes. The
	 *         {@link IJmxTable#TABLEROWS} are filled with columns ordered by
	 *         the appearance of populated attributes, for example
	 *         {@link IJmxTableRow#COL0}, {@link IJmxTableRow#COL1} etc.
	 *         {@link IJmxTableRow#COL9} contains the already translated
	 *         datasource id of the entity. Furthermore,
	 *         {@link IJmxTableRow#COLUNIQUEID} contains the uniqueId for the
	 *         entity represented by the {@link IJmxTableRow},
	 *         {@link IJmxTableRow#COLDELETEABLE} contains the deleteable status
	 *         for the entity, {@link IJmxTableRow#COLTYPE} contains the type
	 *         for the entity. If the {@link IJmxTableRow#COLTYPE} represents an
	 *         {@link com.sap.security.api.IUser},
	 *         {@link IJmxTableRow#COLREFUNIQUEID} is filled with the
	 *         {@link com.sap.security.api.IUserAccount} uniqueId and
	 *         {@link IJmxTableRow#COLSTATUS0} with the current lock status of
	 *         the {@link com.sap.security.api.IUserAccount}.
	 * @throws OpenDataException,
	 *             UMException
	 */
	public IJmxTable getEntityMemberTableByDatasources(String uniqueId,
			String searchString, String memberType, String[] datasourceIds,
			CompositeData[] searchAttributes, CompositeData population[],
			boolean recursiveSearch, String companyId, String guid,
			CompositeData[] state) throws Exception;

	/**
	 * This method calculates the data for an entity parent table
	 * 
	 * @param uniqueId
	 *            Unique ID of the member
	 * @param searchString
	 *            Criteria for the (simple) search
	 * @param memberType
	 *            Specifies, for which members types the search should be
	 *            performed
	 * @param datasourceIds
	 *            The datasources to search on
	 * @param searchAttributes
	 *            additional search attributes
	 * @param recursiveSearch
	 *            This method does a recursive search if the parameter
	 *            recursiveSearch is set to true
	 * @param companyId
	 *            The current selected company
	 * @param guid
	 *            A guid to save the search result
	 * @param state
	 *            An {@link IJmxMapEntry} array, which contains state
	 *            information like {@link #STATE_LOCALE}
	 * @return An {@link IJmxTable} object with filled {@link IJmxTable#SIZE}
	 *         and {@link IJmxTable#STATE} attributes. The
	 *         {@link IJmxTable#TABLEROWS} are empty.
	 * @throws OpenDataException,
	 *             UMException
	 */
	public IJmxTable calculateEntityParentTableByDatasources(String uniqueId,
			String searchString, String parentType, String[] datasourceIds,
			CompositeData[] searchAttributes, boolean recursiveSearch,
			String companyId, String guid, CompositeData[] state)
			throws Exception;

	/**
	 * This method responds an entity parent table
	 * 
	 * @param uniqueId
	 *            Unique ID of the member
	 * @param searchString
	 *            Criteria for the (simple) search
	 * @param memberType
	 *            Specifies, for which parent types the search should be
	 *            performed
	 * @param datasourceIds
	 *            The datasources to search on
	 * @param searchAttributes
	 *            additional search attributes
	 * @param population
	 *            A {@link IJmxAttribute} array, which specifies the attributes
	 *            to be populated. Used fields: {@link IJmxAttribute#NAMESPACE}
	 *            for the attribute namespace and {@link IJmxAttribute#NAME} for
	 *            the attribute name
	 * @param recursiveSearch
	 *            This method does a recursive search if the parameter
	 *            recursiveSearch is set to true
	 * @param companyId
	 *            The current selected company
	 * @param guid
	 *            A guid to get a previous evaluated search result
	 * @param state
	 *            An array of {@link IJmxMapEntry}, which contains state
	 *            information like: Key {@link #STATE_LOCALE}, Value =
	 *            {@link java.lang.String} representation of
	 *            {@link java.util.Locale}
	 * @return An {@link IJmxTable} object with filled {@link IJmxTable#SIZE}
	 *         and {@link IJmxTable#STATE} attributes. The
	 *         {@link IJmxTable#TABLEROWS} are filled with columns ordered by
	 *         the appearance of populated attributes, for example
	 *         {@link IJmxTableRow#COL0}, {@link IJmxTableRow#COL1} etc.
	 *         {@link IJmxTableRow#COL9} contains the already translated
	 *         datasource id of the entity. Furthermore,
	 *         {@link IJmxTableRow#COLUNIQUEID} contains the uniqueId for the
	 *         entity represented by the {@link IJmxTableRow},
	 *         {@link IJmxTableRow#COLDELETEABLE} contains the deleteable status
	 *         for the entity, {@link IJmxTableRow#COLTYPE} contains the type
	 *         for the entity. If the {@link IJmxTableRow#COLTYPE} represents an
	 *         {@link com.sap.security.api.IUser},
	 *         {@link IJmxTableRow#COLREFUNIQUEID} is filled with the
	 *         {@link com.sap.security.api.IUserAccount} uniqueId and
	 *         {@link IJmxTableRow#COLSTATUS0} with the current lock status of
	 *         the {@link com.sap.security.api.IUserAccount}.
	 * @throws OpenDataException,
	 *             UMException
	 */
	public IJmxTable getEntityParentTableByDatasources(String uniqueId,
			String searchString, String parentType, String[] datasourceIds,
			CompositeData[] searchAttributes, CompositeData population[],
			boolean recursiveSearch, String companyId, String guid,
			CompositeData[] state) throws Exception;

	/**
	 * This method creates a new entity (e.g. user, group, role) and returns it.
	 * 
	 * @param mainEntity
	 *            A IJmxEntity object, which specifies the main entity to be
	 *            created. Used attributes from {@link IJmxEntity}: Type =
	 *            {@link com.sap.security.api.IPrincipalFactory#IROLE},
	 *            {@link com.sap.security.api.IPrincipalFactory#IGROUP},
	 *            {@link com.sap.security.api.IPrincipalFactory#IROLE}.
	 *            Attributes of {@link IJmxAttribute}: Name = Attribute name,
	 *            Namespace = Attribute namespace, Value = Attribute value (also
	 *            possible to use Values = Array of values).
	 * @param additionalEntity
	 *            A IJmxEntity object, which specifies the additional entity to
	 *            be created. Used attributes from {@link IJmxEntity}: Type =
	 *            {@link com.sap.security.api.IPrincipalFactory#IUSERACCOUNT}.
	 *            Attributes of {@link IJmxAttribute}: Name = Attribute name,
	 *            Namespace = Attribute namespace, Value = Attribute value (also
	 *            possible to use Values = Array of values).
	 * @param action
	 *            A modify action (for example, see
	 *            {@link #MODIFY_ACTION_PASSWORD_CREATE_ADMIN})
	 * @param companyId
	 *            The current selected company
	 * @param state
	 *            An array of {@link IJmxMapEntry}, which contains state
	 *            information like: Key {@link #STATE_LOCALE}, Value =
	 *            {@link java.lang.String} representation of
	 *            {@link java.util.Locale}
	 * @return The created {@link IJmxEntity} with all available attributes.
	 *         {@link IJmxEntity#UNIQUEID} contains the entity uniqueId,
	 *         {@link IJmxEntity#TYPE} contains the entity type,
	 *         {@link IJmxEntity#MODIFYABLE} contains the modification state of
	 *         the entity. The {@link IJmxEntity#ATTRIBUTES} are build as
	 *         follows: {@link IJmxAttribute#NAMESPACE} contains the attribute
	 *         namespace, {@link IJmxAttribute#NAME} contains the attribute
	 *         name, {@link IJmxAttribute#VALUE} contains the attribute value,
	 *         {@link IJmxAttribute#VALUES} contains the attribute values as
	 *         array.
	 * @throws OpenDataException,
	 *             UMException
	 */
	public IJmxEntity createEntity(CompositeData mainEntity,
			CompositeData additionalEntity, String action, String companyId,
			CompositeData[] state) throws Exception;

	/**
	 * This method modifies an entity
	 * 
	 * @param mainEntity
	 *            A IJmxEntity object, which specifies the main entity to be
	 *            modified. Used attributes from {@link IJmxEntity}: Type =
	 *            {@link com.sap.security.api.IPrincipalFactory#IROLE},
	 *            {@link com.sap.security.api.IPrincipalFactory#IGROUP},
	 *            {@link com.sap.security.api.IPrincipalFactory#IROLE}.
	 *            Attributes of {@link IJmxAttribute}: Name = Attribute name,
	 *            Namespace = Attribute namespace, Value = Attribute value (also
	 *            possible to use Values = Array of values).
	 * @param additionalEntity
	 *            A IJmxEntity object, which specifies the additional entity to
	 *            be modified. Used attributes from {@link IJmxEntity}: Type =
	 *            {@link com.sap.security.api.IPrincipalFactory#IUSERACCOUNT}.
	 *            Attributes of {@link IJmxAttribute}: Name = Attribute name,
	 *            Namespace = Attribute namespace, Value = Attribute value (also
	 *            possible to use Values = Array of values). A modify action
	 *            (for example, see {@link #MODIFY_ACTION_PASSWORD_CHANGE_ADMIN})
	 * @param action
	 *            A modify action (for example, see
	 *            {@link #MODIFY_ACTION_PASSWORD_CREATE_ADMIN})
	 * @param message
	 *            A message to the user
	 * @param companyId
	 *            The current selected company
	 * @param state
	 *            An array of {@link IJmxMapEntry}, which contains state
	 *            information like: Key {@link #STATE_LOCALE}, Value =
	 *            {@link java.lang.String} representation of
	 *            {@link java.util.Locale}
	 * @return int An integer value, which represents the modification status.
	 *         (See {@link IJmxServer#RESULT_OPERATION_OK})
	 * @throws OpenDataException,
	 *             UMException
	 */
	public int modifyEntity(CompositeData mainEntity,
			CompositeData additionalEntity, String action, String message,
			String companyId, CompositeData[] state) throws Exception;

	/**
	 * This method modifies an entity
	 * 
	 * @param mainEntity
	 *            A IJmxEntity object, which specifies the main entity to be
	 *            modified. Used attributes from {@link IJmxEntity}: Type =
	 *            {@link com.sap.security.api.IPrincipalFactory#IROLE},
	 *            {@link com.sap.security.api.IPrincipalFactory#IGROUP},
	 *            {@link com.sap.security.api.IPrincipalFactory#IROLE}.
	 *            Attributes of {@link IJmxAttribute}: Name = Attribute name,
	 *            Namespace = Attribute namespace, Value = Attribute value (also
	 *            possible to use Values = Array of values).
	 * @param additionalEntity
	 *            A IJmxEntity object, which specifies the additional entity to
	 *            be modified. Used attributes from {@link IJmxEntity}: Type =
	 *            {@link com.sap.security.api.IPrincipalFactory#IUSERACCOUNT}.
	 *            Attributes of {@link IJmxAttribute}: Name = Attribute name,
	 *            Namespace = Attribute namespace, Value = Attribute value (also
	 *            possible to use Values = Array of values). A modify action
	 *            (for example, see {@link #MODIFY_ACTION_PASSWORD_CHANGE_ADMIN})
	 * @param action
	 *            A modify action (for example, see
	 *            {@link #MODIFY_ACTION_PASSWORD_CREATE_ADMIN})
	 * @param message
	 *            A message to the user
	 * @param companyId
	 *            The current selected company
	 * @param state
	 *            An array of {@link IJmxMapEntry}, which contains state
	 *            information like: Key {@link #STATE_LOCALE}, Value =
	 *            {@link java.lang.String} representation of
	 *            {@link java.util.Locale}
	 * @return An {@link IJmxEntity} object, which represents the modified object.
	 * @throws Exception
	 */
    public IJmxEntity changeEntity(CompositeData dataMainDetail,
            CompositeData dataAdditionalDetail, String action, String message,
            String companyId, CompositeData[] state) throws Exception;
	
	/**
	 * This method modifies one or more entities
	 * 
	 * @param ids
	 *            specifies the entities to be modified by uniqueId
	 * @param action
	 *            A modify action (for example, see
	 *            {@link #MODIFY_ACTION_PASSWORD_CHANGE_GENERATE_ADMIN})
	 * @param message
	 *            A message to the user
	 * @param companyId
	 *            The current selected company
	 * @param state
	 *            An array of {@link IJmxMapEntry}, which contains state
	 *            information like: Key {@link #STATE_LOCALE}, Value =
	 *            {@link java.lang.String} representation of
	 *            {@link java.util.Locale}
	 * @return int An integer value, which represents the modification status.
	 *         (See {@link IJmxServer#RESULT_OPERATION_OK})
	 * @throws OpenDataException,
	 *             UMException
	 */
	public int modifyEntities(String[] ids, String action, String message,
			String companyId, CompositeData[] state) throws Exception;

	/**
	 * @deprecated
	 * This method modifies the mapping between parents and members. This can
	 * only be an adding or removing.
	 * 
	 * @param parentIds
	 *            The parents unique ids
	 * @param memberIds
	 *            The members unique ids
	 * @param addMembers
	 *            If true, the members will be added to the parents. If not,
	 *            they will be removed.
	 * @param state
	 *            An array of {@link IJmxMapEntry}, which contains state
	 *            information like: Key {@link #STATE_LOCALE}, Value =
	 *            {@link java.lang.String} representation of
	 *            {@link java.util.Locale}
	 * @param companyId
	 *            The current selected company
	 * @return int An integer value, which represents the modification status.
	 *         (See {@link IJmxServer#RESULT_OPERATION_OK})
	 * @deprecated Use {@link #modifyEntityAssignments(String[], String[], boolean, String, CompositeData[])} instead.
	 */
	@Deprecated
	public int modifyEntityMappings(String[] parentIds, String[] memberIds,
			boolean addMembers, String companyId, CompositeData[] state)
			throws Exception;

	/**
	 * This method modifies the assignments between parents and members.
	 * This can only be an adding or removing once at a time.
	 * 
	 * @param parentIds
	 *            The parents unique ids
	 * @param memberIds
	 *            The members unique ids
	 * @param addMembers
	 *            If true, the members will be added to the parents. If not,
	 *            they will be removed.
	 * @param state
	 *            An array of {@link IJmxMapEntry}, which contains state
	 *            information like: Key {@link #STATE_LOCALE}, Value =
	 *            {@link java.lang.String} representation of
	 *            {@link java.util.Locale}
	 * @param companyId
	 *            The current selected company
	 * @return result An {@link IJmxResult} object, which contains messages about the status.
	 */
	public IJmxResult modifyEntityAssignments(String[] parentIds, String[] memberIds,
			boolean addMembers, String companyId, CompositeData[] state)
			throws Exception;
	
	/**
	 * This method deletes one ore more entities
	 * 
	 * @param ids
	 *            The principals which should be deleted
	 * @param message
	 *            Message to users
	 * @param companyId
	 *            The current selected company
	 * @param state
	 *            An array of {@link IJmxMapEntry}, which contains state
	 *            information like: Key {@link #STATE_LOCALE}, Value =
	 *            {@link java.lang.String} representation of
	 *            {@link java.util.Locale}
	 * @return int An integer value, which represents the modification status.
	 *         (See {@link IJmxServer#RESULT_OPERATION_OK})
	 * @throws OpenDataException,
	 *             UMException
	 */
	public int deleteEntities(String[] ids, String message, String companyId,
			CompositeData[] state) throws Exception;

	/**
	 * This method evaluates if the company concept is enabled
	 * @param state
	 *            An array of {@link IJmxMapEntry}, which contains state
	 *            information like: Key {@link #STATE_LOCALE}, Value =
	 *            {@link java.lang.String} representation of
	 *            {@link java.util.Locale}
	 * @return boolean
	 * @throws UMException
	 */
	public boolean getCompanyConceptEnabled(CompositeData[] state)
			throws Exception;

	/**
	 * This method evaluates all available companies
	 * 
	 * @param state
	 *            An array of {@link IJmxMapEntry}, which contains state
	 *            information like: Key {@link #STATE_LOCALE}, Value =
	 *            {@link java.lang.String} representation of
	 *            {@link java.util.Locale}
	 * @return A {@link IJmxTable} with {@link IJmxTable#TABLEROWS}, where
	 *         {@link IJmxTableRow#COL0} contains the company description and
	 *         {@link IJmxTableRow#COL1} the company id
	 * @throws Exception
	 */
	public IJmxTable getCompanies(CompositeData[] state) throws Exception;

	/**
	 * Generates text with attributes of passed principal unique ids
	 * @param uniqueIds
	 *            contains uniqueIds of principals to be exported
	 * @param state
	 *            An array of {@link IJmxMapEntry}, which contains state
	 *            information like: Key {@link #STATE_LOCALE}, Value =
	 *            {@link java.lang.String} representation of
	 *            {@link java.util.Locale}
	 * @return text
	 * @throws OpenDataException
	 * @throws Exception
	 */
	public String batchExport(String[] uniqueIds, CompositeData[] state)
			throws Exception;

	/**
	 * Creates principals passed in the input stream
	 * 
	 * @param input
	 *            string with principal attributes
	 * @param state
	 *            An array of {@link IJmxMapEntry}, which contains state
	 *            information like: Key {@link #STATE_LOCALE}, Value =
	 *            {@link java.lang.String} representation of
	 *            {@link java.util.Locale}
	 * @return table with status infos of every principal processed
	 * @throws OpenDataException
	 * @throws Exception
	 */
	public IJmxTable batchImport(String data, boolean overwrite,
			CompositeData[] state) throws Exception;

	/**
	 * This method returns the current active UME properties.
	 * 
	 * @param state
	 *            An array of {@link IJmxMapEntry}, which contains state
	 *            information like: Key {@link #STATE_LOCALE}, Value =
	 *            {@link java.lang.String} representation of
	 *            {@link java.util.Locale}
	 * @return An IJmxMapEntry array, which reflects the UME properties
	 * @throws Exception
	 * @throws OpenDataException
	 */
	public IJmxMapEntry[] getUmeProperties(CompositeData[] state)
			throws Exception;

	/**
	 * This method deletes a search request out of the cache.
	 * 
	 * @param guid
	 * @param state
	 *            An array of {@link IJmxMapEntry}, which contains state
	 *            information like: Key {@link #STATE_LOCALE}, Value =
	 *            {@link java.lang.String} representation of
	 *            {@link java.util.Locale}
	 */
	public void cancelSearchRequest(String guid, CompositeData[] state);

	/**
	 * This method returns the layout information for the given principal. This
	 * contains attributes, which might be modifyable. If the uniqueId is not
	 * given, the param type will be taken for checking the attributs in create
	 * case
	 * 
	 * @param uniqueId
	 * @param type
	 * @param companyId
	 * @param layoutEntity
	 * @param mainEntity
	 *            A IJmxEntity object, which specifies the entity to be
	 *            checked. Used attributes from {@link IJmxEntity}: Type =
	 *            {@link com.sap.security.api.IPrincipalFactory#IROLE},
	 *            {@link com.sap.security.api.IPrincipalFactory#IGROUP},
	 *            {@link com.sap.security.api.IPrincipalFactory#IROLE}, {@link com.sap.security.api.IPrincipalFactory#IUSERACCOUNT}.
	 *            Used fields of {@link IJmxAttribute}: {@link IJmxAttribute#NAMESPACE} = attribute namespace, {@link IJmxAttribute#NAME} = attribute name
	 * @param state
	 *            An array of {@link IJmxMapEntry}, which contains state
	 *            information like: Key {@link #STATE_LOCALE}, Value =
	 *            {@link java.lang.String} representation of
	 *            {@link java.util.Locale}
	 * @return    A IJmxEntity object, which specifies the checked entity . Used attributes from {@link IJmxEntity}: Type =
	 *            {@link com.sap.security.api.IPrincipalFactory#IROLE},
	 *            {@link com.sap.security.api.IPrincipalFactory#IGROUP},
	 *            {@link com.sap.security.api.IPrincipalFactory#IROLE}, {@link com.sap.security.api.IPrincipalFactory#IUSERACCOUNT}.
	 *            Used fields of {@link IJmxAttribute}: {@link IJmxAttribute#NAMESPACE} = attribute namespace, {@link IJmxAttribute#NAME} = attribute name, {@link IJmxAttribute#MODIFYABLE} = attribute modification state
	 * @throws Exception
	 * @throws OpenDataException
	 */
	public IJmxEntity getAttributeLayoutInformation(String uniqueId,
			String type, CompositeData[] layoutEntity, String companyId,
			CompositeData[] state) throws Exception;

	/**
	 * Locks one ore more objects by its unique ids on base of the current user
	 * 
	 * @param uniqueIds
	 * @param state
	 *            An array of {@link IJmxMapEntry}, which contains state
	 *            information like: Key {@link #STATE_LOCALE}, Value =
	 *            {@link java.lang.String} representation of
	 *            {@link java.util.Locale}
	 * @throws Exception
	 */
	public void lock(String uniqueIds[], CompositeData[] state)
			throws Exception;

	/**
	 * Unlocks one ore more objects by its unique ids on base of the current
	 * user
	 * 
	 * @param uniqueIds
	 * @param state
	 *            An array of {@link IJmxMapEntry}, which contains state
	 *            information like: Key {@link #STATE_LOCALE}, Value =
	 *            {@link java.lang.String} representation of
	 *            {@link java.util.Locale}
	 * @throws Exception
	 */
	public void unlock(String uniqueIds[], CompositeData[] state)
			throws Exception;

	/**
	 * Checks the permission for the current logged in user
	 * 
	 * @param permissions
	 *            An {@link IJmxPermission} object array, which specifies the
	 *            permissions to be checked. All fields are used.
	 * @param companyId
	 *            The current selected company
	 * @param state
	 *            An array of {@link IJmxMapEntry}, which contains state
	 *            information like: Key {@link #STATE_LOCALE}, Value =
	 *            {@link java.lang.String} representation of
	 *            {@link java.util.Locale}
	 * @return An {@link IJmxPermission} array with same parameter as given as
	 *         parameter with additional {@link IJmxPermission#PERMISSION}
	 *         boolean information
	 * @throws Exception
	 * @throws OpenDataException
	 */
	public IJmxPermission[] hasPermission(CompositeData[] permissions,
			String company, CompositeData[] state) throws Exception;

	/**
	 * This method returns the current persistent UME properties.
	 * 
	 * @param state
	 *            An array of {@link IJmxMapEntry}, which contains state
	 *            information like: Key {@link #STATE_LOCALE}, Value =
	 *            {@link java.lang.String} representation of
	 *            {@link java.util.Locale}
	 * @return An {@link IJmxMapEntry} object, which reflects the UME
	 *         properties
	 * @throws Exception
	 * @throws OpenDataException
	 */
	public IJmxMapEntry[] readConfiguration(CompositeData[] state)
			throws Exception;

	/**
     * This method returns the current persistent UME properties including their
     * meta data.
	 * 
     * @param flags Bitwise ANDed flags specifying which property meta data is required;
     *            see {@link IUMConfigAdmin#getAllPropertiesExtendedAdmin(int)} for
     *            details; at the time when this method has been added, the only
     *            valid flag was
     *            {@link IUMConfigAdmin#PROPERTY_INFO_DIFFERING_INSTANCE_VALUES}
	 * @param state
     *            An array of {@link IJmxMapEntry}, which contains state
     *            information like: Key {@link #STATE_LOCALE}, Value =
     *            {@link java.lang.String} representation of
     *            {@link java.util.Locale}
	 * @return array of IJmxProperty instances for all UME properties
     * @throws Exception
     * @throws OpenDataException
	 */
	public IJmxProperty[] readConfigurationExt(int flags, CompositeData[] state) throws Exception;
	
	/**
	 * This method persists the given UME properties parameter
	 * 
	 * @param properties
	 * An {@link IJmxMapEntry} object, which reflects the UME
	 *         properties
	 * @param state
	 *            An array of {@link IJmxMapEntry}, which contains state
	 *            information like: Key {@link #STATE_LOCALE}, Value =
	 *            {@link java.lang.String} representation of
	 *            {@link java.util.Locale}
	 * @throws Exception
	 * @throws OpenDataException
	 */
	public boolean writeConfiguration(CompositeData[] properties,
			CompositeData[] state) throws Exception;

    /**
     * This method persists the given UME properties parameter and returns
     * more detailed information about the results than
     * {@link #writeConfiguration(CompositeData[], CompositeData[])}
     * (e.g. whether it is necessary to restart the server to make all
     * configuration changes effective).
     * 
     * @param properties
     * An {@link IJmxMapEntry} object, which reflects the UME
     *         properties
     * @param state
     *            An array of {@link IJmxMapEntry}, which contains state
     *            information like: Key {@link #STATE_LOCALE}, Value =
     *            {@link java.lang.String} representation of
     *            {@link java.util.Locale}
     * @return save result; the status code has the {@link #RESULT_CFG_SERVER_RESTART_REQUIRED}
     *         bit set if the server needs to be restarted to make all changes effective
     * @throws Exception
     * @throws OpenDataException
     */
    public IJmxResult writeConfigurationExt(CompositeData[] properties,
            CompositeData[] state) throws Exception;

	/**
	 * Returns the content of the given file name
	 * @param name	name of file
	 * @param state
	 *            An array of {@link IJmxMapEntry}, which contains state
	 *            information like: Key {@link #STATE_LOCALE}, Value =
	 *            {@link java.lang.String} representation of
	 *            {@link java.util.Locale}
	 * @return content
	 * @throws Exception
	 */
	public byte[] readFile(String name, CompositeData[] state) throws Exception;

	/**
	 * Sets the content of the given file name
	 * @param name	name of file
	 * @param content	content of the file
	 * @param state
	 *            An array of {@link IJmxMapEntry}, which contains state
	 *            information like: Key {@link #STATE_LOCALE}, Value =
	 *            {@link java.lang.String} representation of
	 *            {@link java.util.Locale}
	 * @throws Exception
	 */
	public void writeFile(String name, byte[] content, CompositeData[] state)
			throws Exception;

	/**
	 * Returns a String array of file names, which are listed in the persistent directory
	 * @param state
	 *            An array of {@link IJmxMapEntry}, which contains state
	 *            information like: Key {@link #STATE_LOCALE}, Value =
	 *            {@link java.lang.String} representation of
	 *            {@link java.util.Locale}
	 * @return file names
	 * @throws Exception
	 */
	public String[] getFiles(CompositeData[] state) throws Exception;

	/**
	 * Retrieve all systems (names and other relevant information) for which the
	 * principal with unique ID <code>principalUniqueId</code> can define user
	 * mapping data.
	 * 
	 * 
	 * Each {@link IJmxEntity} object returned follows the structure:
	 * 
	 * <table border="1" style="margin-top:1.5ex; margin-bottom:1.5ex;">
	 *   <tr>
	 *     <th>Property</th>
	 *     <th>Type</th>
	 *     <th>Description</th>
	 *   </tr>
	 *   <tr>
	 *     <td>getUniqueId()</td>
	 *     <td>String</td>
	 *     <td>Alias of the system (potentially qualified by the system landscape type)</td>
	 *   </tr>
	 *   <tr>
	 *     <td>getAttributes()</td>
	 *     <td>IJmxAttribute[]</td>
	 *     <td>See below</td>
	 *   </tr>
	 * </table>
	 * 
	 * The following {@link IJmxAttribute}s are available (all constants are 
	 * defined in interface {@link com.sap.security.core.jmx.IJmxServer}):
	 * 
	 * <table border="1" style="margin-top:1.5ex; margin-bottom:1.5ex;">
	 *   <tr>
	 *     <th>Namespace</th>
	 *     <th>Name</th>
	 *     <th>Type</th>
	 *     <th>Description</th>
	 *   </tr>
	 *   <tr>
	 *     <td><code>{@link #UMAP_NAMESP_SYSTEM}</code></td>
	 *     <td><code>{@link #UMAP_ATTR_CRYPTO_STATUS}</code></td>
	 *     <td>boolean</td>
	 *     <td><code>true</code> if UME configuration for encryption of user mapping
	 *       data for the system is valid; <code>false</code> if the configuration
	 *       needs to be fixed.</td>
	 *   </tr>
	 *   <tr>
	 *     <td><code>{@link #UMAP_NAMESP_SYSTEM}</code></td>
	 *     <td><code>{@link #UMAP_ATTR_IS_REFERENCE_SYSTEM}</code></td>
	 *     <td>boolean</td>
	 *     <td><code>true</code> if the system is the UME reference system for
	 *       which user mapping data will be verified and only be saved if it is
	 *       correct; </code>false</code> if it is a normal system for which user
	 *       mapping data is saved as entered without verification.</td>
	 *   </tr>
	 *   <tr>
	 *     <td><code>{@link #UMAP_NAMESP_SYSTEM}</code></td>
	 *     <td><code>{@link #UMAP_ATTR_HAS_MAPPING_DATA}</code></td>
	 *     <td>boolean</td>
	 *     <td><code>true</code> if the principal has existing user mapping data for
	 *       the system; <code>false</code> if there is no existing user mapping data
	 *       for the tuple of principal and system.</td>
	 *   </tr>
	 *   <tr>
	 *     <td><code>{@link #UMAP_NAMESP_SYSTEM}</code></td>
	 *     <td><code>{@link #UMAP_ATTR_FIELDS}</code></td>
	 *     <td>String</td>
	 *     <td>The definition of additional user mapping fields as entered in the
	 *       system definition (usually in Enterprise Portal's system landscape
	 *       editor). May be an empty String, <code>null</code> or completely
	 *       missing.</td>
	 *   </tr>
	 *   <tr>
	 *     <td><code>{@link #UMAP_NAMESP_SYSTEM}</code></td>
	 *     <td><code>{@link #UMAP_ATTR_DISPLAY_NAME}</code></td>
	 *     <td>String</td>
	 *     <td>Optional: The display name of the system.</td>
	 *   </tr>
	 * </table>
	 * 
	 * @param principalUniqueId
	 *     Unique ID of the principal for which to retrieve all systems
	 *            available for user mapping
	 * @param state
	 *            An array of {@link IJmxMapEntry}, which contains state
	 *            information like: Key {@link #STATE_LOCALE}, Value =
	 *            {@link java.lang.String} representation of
	 *            {@link java.util.Locale}
	 * @return Array of all relevant systems, each system is represented as
	 *         <code>IJmxEntity</code>.
	 * @throws Exception
	 */
	public IJmxEntity[] getUserMappingSystems(String principalUniqueId,
			CompositeData[] state) throws Exception;

	/**
	 * Retrieve actual user mapping data for the principal with unique ID
	 * <code>principalUniqueId</code> and the system with ID
	 * <code>systemId</code>.
	 * 
	 * Each {@link IJmxEntity} object returned follows the structure: 
	 *
	 * <table border="1" style="margin-top:1.5ex; margin-bottom:1.5ex;">
	 *   <tr>
	 *     <th>Property</th>
	 *     <th>Type</th>
	 *     <th>Description</th>
	 *   </tr>
	 *   <tr>
	 *     <td>getUniqueId()</td>
	 *     <td>String</td>
	 *     <td>Internal identifier for user mapping entry</td>
	 *   </tr>
	 *   <tr>
	 *     <td>getModifyable()</td>
	 *     <td>boolean</td>
	 *     <td>Flag whether user mapping data for the selected principal and system
	 *       can be added/modified (<code>true</code>) or whether the mapping data
	 *       is read-only (<code>false</code>)</td>
	 *   </tr>
	 *   <tr>
	 *     <td>getAttributes()</td>
	 *     <td>IJmxAttribute[]</td>
	 *     <td>See below</td>
	 *   </tr>
	 * </table>
	 *
	 * The following {@link IJmxAttribute}s are available (all constants are 
	 * defined in interface {@link com.sap.security.core.jmx.IJmxServer}):
	 * 
	 * <table border="1" style="margin-top:1.5ex; margin-bottom:1.5ex;">
	 *   <tr>
	 *     <th>Namespace</th>
	 *     <th>Name</th>
	 *     <th>Type</th>
	 *     <th>Description</th>
	 *   </tr>
	 *   <tr>
	 *     <td><code>{@link #UMAP_NAMESP_SYSTEM}</code></td>
	 *     <td><code>{@link #UMAP_ATTR_CRYPTO_STATUS}</code></td>
	 *     <td>boolean</td>
	 *     <td><code>true</code> if UME configuration for encryption of user mapping
	 *       data for the system is valid; <code>false</code> if the configuration
	 *       needs to be fixed.</td>
	 *   </tr>
	 *   <tr>
	 *     <td><code>{@link #UMAP_NAMESP_MAPPING_DETAILS}</code></td>
	 *     <td><code>{@link #UMAP_ATTR_INDIRECT_MAPPING_SRC}</code></td>
	 *     <td>String</td>
	 *     <td>If there is an existing user mapping which is inherited from another
	 *       principal: unique ID of the principal owning the inherited user mapping.
	 *       If there is no user mapping or the principal itself owns the mapping
	 *       (= direct mapping): <code>null</code></td>
	 *   </tr>
	 *   <tr>
	 *     <td><code>{@link #UMAP_NAMESP_MAPPING_DETAILS}</code></td>
	 *     <td><code>{@link #UMAP_ATTR_INDIRECT_MAPPING_SRC_DISPLAY}</code></td>
	 *     <td>String</td>
	 *     <td>If there is an existing user mapping which is inherited from another
	 *       principal: display name of the principal owning the inherited user
	 *       mapping. If there is no user mapping or the principal itself owns the
	 *       mapping (= direct mapping): <code>null</code></td>
	 *   </tr>
	 *   <tr>
	 *     <td><code>{@link #UMAP_NAMESP_MAPPING_DETAILS}</code></td>
	 *     <td><code>{@link #UMAP_ATTR_INDIRECT_MAPPING_SRC_TYPE}</code></td>
	 *     <td>String</td>
	 *     <td>If there is an existing user mapping which is inherited from another
	 *       principal: type of the principal owning the inherited user mapping.
	 *       Potential values: {@link IPrincipalFactory#IUSER},
	 *       {@link IPrincipalFactory#IGROUP}, {@link IPrincipalFactory#IROLE}.
	 *       If there is no user mapping or the principal itself owns the mapping
	 *       (= direct mapping): <code>null</code></td>
	 *   </tr>
	 *   <tr>
	 *     <td><code>{@link #UMAP_NAMESP_LOGON_DATA}</code></td>
	 *     <td>*</td>
	 *     <td>String</td>
	 *     <td>This namespace contains all user mapping fields and values available
	 *       for the principal and system. Attribute names are not fully predefined,
	 *       so you need to process all attributes with this namespace. For predefined
	 *       attribute names, see
	 *       {@link com.sap.security.api.umap.IUserMappingData}</td>
	 *   </tr>
	 *   <tr>
	 *     <td><code>{@link #UMAP_NAMESP_INVERSE_MAPPING}</code></td>
	 *     <td>*</td>
	 *     <td>String</td>
	 *     <td>If there is an existing user mapping for the principal and system and
	 *       there is at least one corresponding inverse user mapping, this namespace
	 *       contains all unique IDs of users who are mapped to the same backend user.
	 *       This can be used to warn the user if there are multiple users mapped to
	 *       the same backend user, which may lead to problems with inverse user
	 *       mapping.<br/>
	 *       The attribute name is irrelevant and can be an arbitrary String.
	 *       If there is no such inverse user mapping, there will not be any such
	 *       attribute.</td>
	 *   </tr>
	 * </table>
	 * 
	 * @param principalUniqueId
	 *     Unique ID of the principal for which to retrieve user mapping data
	 * @param systemId
	 *     ID (alias) of the system for which to retrieve user mapping data
	 * @param state
	 *            An array of {@link IJmxMapEntry}, which contains state
	 *            information like: Key {@link #STATE_LOCALE}, Value =
	 *            {@link java.lang.String} representation of
	 *            {@link java.util.Locale}
	 * @return <code>IJmxEntity</code> representing the corresponding
	 *         {@link com.sap.security.api.umap.IUserMappingData} object.
	 * @throws Exception
	 */
	public IJmxEntity getUserMappingData(String principalUniqueId,
			String systemId, CompositeData[] state) throws Exception;

	/**
	 * Store the logon data contained in <code>logonData</code> as user
	 * mapping data for the principal with unique ID
	 * <code>principalUniqueId</code> and the system with ID
	 * <code>systemId</code>.
	 * 
	 * @param principalUniqueId
	 *     Unique ID of the principal for which to save the user mapping data
	 * @param systemId
	 *     SystemId ID (alias) of the system for which to save the user mapping data
	 * @param logonData
	 *     The user mapping data to save, as {@link IJmxMapEntry}[]. The result
	 *     will be transformed to a usual <code>java.util.Map</code> and be passed to
	 *     {@link com.sap.security.api.umap.IUserMappingData#saveLogonData(java.util.Map)}
	 * @param state
	 *            An array of {@link IJmxMapEntry}, which contains state
	 *            information like: Key {@link #STATE_LOCALE}, Value =
	 *            {@link java.lang.String} representation of
	 *            {@link java.util.Locale}
	 * @throws Exception
	 */
	public void storeUserMappingData(String principalUniqueId, String systemId,
			CompositeData[] logonData, CompositeData[] state) throws Exception;

	/**
	 * Clear the user mapping data currently stored for the principal with
	 * unique ID <code>principalUniqueId</code> and the system with
	 * ID <code>systemId</code>.
	 * 
	 * @param principalUniqueId
	 *     Unique ID of the principal for which to clear the user mapping data
	 * @param systemId
	 *     ID (alias) of the system for which to clear the user mapping data
	 * @param state
	 *            An array of {@link IJmxMapEntry}, which contains state
	 *            information like: Key {@link #STATE_LOCALE}, Value =
	 *            {@link java.lang.String} representation of
	 *            {@link java.util.Locale}
	 * @throws Exception
	 */
	public void clearUserMappingData(String principalUniqueId, String systemId,
			CompositeData[] state) throws Exception;

	/**
	 * Retrieve a list of all user mapping converters that are available.
	 * 
	 * Each {@link IJmxEntity} object returned follows the structure:
	 *
	 * <table border="1" style="margin-top:1.5ex; margin-bottom:1.5ex;">
	 *   <tr>
	 *     <th>Property</th>
	 *     <th>Type</th>
	 *     <th>Description</th>
	 *   </tr>
	 *   <tr>
	 *     <td>getUniqueId()</td>
	 *     <td>String</td>
	 *     <td>Type of user mapping converter (return value of
	 *       {@link com.sap.security.api.umap.IUserMappingConverter#getType()})</td>
	 *   </tr>
	 *   <tr>
	 *     <td>getModifyable()</td>
	 *     <td>boolean</td>
	 *     <td>Flag describing whether the converter can be run in the current
	 *       system state (return value of
	 *       {@link com.sap.security.api.umap.IUserMappingConverter#isConversionPossible()})</td>
	 *   </tr>
	 *   <tr>
	 *     <td>getAttributes()</td>
	 *     <td>IJmxAttribute[]</td>
	 *     <td>See below</td>
	 *   </tr>
	 * </table>
	 *
	 * The following {@link IJmxAttribute}s are available (all constants are 
	 * defined in interface {@link com.sap.security.core.jmx.IJmxServer}):
	 * 
	 * <table border="1" style="margin-top:1.5ex; margin-bottom:1.5ex;">
	 *   <tr>
	 *     <th>Namespace</th>
	 *     <th>Name</th>
	 *     <th>Type</th>
	 *     <th>Description</th>
	 *   </tr>
	 *   <tr>
	 *     <td>{@link #UMAP_NAMESP_CONV}</td>
	 *     <td>*</td>
	 *     <td>*</td>
	 *     <td>Status information about the user mapping converter. The namespace is fixed. 
	 *       For the attribute names and the respective value data types, please refer to
	 *       {@link com.sap.security.api.umap.IUserMappingConverter#getConversionStatus()}.</td>
	 *   </tr>
	 * </table>
	 *
	 * @param state
	 *            An array of {@link IJmxMapEntry}, which contains state
	 *            information like: Key {@link #STATE_LOCALE}, Value =
	 *            {@link java.lang.String} representation of
	 *            {@link java.util.Locale}
	 * @return Array of entities, each entity represents one converter.
	 * @throws Exception
	 */
	public IJmxEntity[] getAvailableUserMappingConverters(CompositeData[] state)
			throws Exception;

	/**
	 * Start conversion of existing user mapping data using the specified
	 * converter.
	 * 
	 * @param converterType Type (unique ID) of the IUserMappingConverter
	 * @param state
	 *            An array of {@link IJmxMapEntry}, which contains state
	 *            information like: Key {@link #STATE_LOCALE}, Value =
	 *            {@link java.lang.String} representation of
	 *            {@link java.util.Locale}
	 * @throws Exception
	 */
	public void startUserMappingConversion(String converterType,
			CompositeData[] state) throws Exception;

	/**
	 * As {@link #startUserMappingConversion(String, CompositeData[])}, but
     * specifying the number of background threads to use for the conversion
     * process.
	 *
	 * @param converterType Type (unique ID) of the IUserMappingConverter
	 * @param numberOfThreads Number of background threads to use.
	 * @param state
	 *            An array of {@link IJmxMapEntry}, which contains state
	 *            information like: Key {@link #STATE_LOCALE}, Value =
	 *            {@link java.lang.String} representation of
	 *            {@link java.util.Locale}
	 * @throws Exception
	 */
	public void startUserMappingConversionByThreads(String converterType,
			int numberOfThreads, CompositeData[] state) throws Exception;

	/**
	 * Reset the status of the specified user mapping converter (to be called
	 * after the user has acknowledged the results of a completed conversion).
	 *
	 * @param converterType Type (unique ID) of the IUserMappingConverter
	 * @param state
	 *            An array of {@link IJmxMapEntry}, which contains state
	 *            information like: Key {@link #STATE_LOCALE}, Value =
	 *            {@link java.lang.String} representation of
	 *            {@link java.util.Locale}
	 */
	public void resetUserMappingConversionStatus(String converterType,
			CompositeData[] state);

	/**
	 * Determine the current encryption mode used by user mapping.
	 * @param state
	 *            An array of {@link IJmxMapEntry}, which contains state
	 *            information like: Key {@link #STATE_LOCALE}, Value =
	 *            {@link java.lang.String} representation of
	 *            {@link java.util.Locale}
	 * Potential values are:
	 * {@link com.sap.security.core.umap.imp.UserMapping#CRYPTO_MODE_WEAK}, 
	 * {@link com.sap.security.core.umap.imp.UserMapping#CRYPTO_MODE_CONVERT}, 
	 * {@link com.sap.security.core.umap.imp.UserMapping#CRYPTO_MODE_STRONG}
	 * @return One of the listed values.
	 * @throws Exception
	 */
	public int getUserMappingCryptoMode(CompositeData[] state) throws Exception;

	/**
	 * Retrieve a list of all systems that may be declared as SAP reference
	 * system in UME configuration.
	 * 
	 * <p>
	 * This old variant returns an array of Strings, each of them is a system alias
	 * qualified by the type of the system landscape which is responsible for the
	 * system. As the system landscape type should not be displayed on the UI, the
	 * new variant {@link #getSAPReferenceSystemCandidates2(CompositeData[])} should
	 * be used because it provides display names for all systems which contain a
	 * localized name for the system landscape.
	 * </p>
	 *
	 * @param state
	 *            An array of {@link IJmxMapEntry}, which contains state
	 *            information like: Key {@link #STATE_LOCALE}, Value =
	 *            {@link java.lang.String} representation of
	 *            {@link java.util.Locale}
	 * @return an array of system aliases
	 * @throws Exception
	 * @deprecated Use {@link #getSAPReferenceSystemCandidates2(CompositeData[])} instead.
	 */
	@Deprecated
	public String[] getSAPReferenceSystemCandidates(CompositeData[] state) throws Exception;

	/**
	 * Retrieve a list of all systems that may be declared as SAP reference
	 * system in UME configuration.
	 * 
	 * Each {@link IJmxEntity} object returned follows the structure: 
	 *
	 * <table border="1" style="margin-top:1.5ex; margin-bottom:1.5ex;">
	 *   <tr>
	 *     <th>Property</th>
	 *     <th>Type</th>
	 *     <th>Description</th>
	 *   </tr>
	 *   <tr>
	 *     <td>getUniqueId()</td>
	 *     <td>String</td>
	 *     <td>Internal identifier for the system, composed of the system landscape
	 *       type and the system alias</td>
	 *   </tr>
	 *   <tr>
	 *     <td>getAttributes()</td>
	 *     <td>IJmxAttribute[]</td>
	 *     <td>See below</td>
	 *   </tr>
	 * </table>
	 *
	 * The following {@link IJmxAttribute}s are available (all constants are 
	 * defined in interface {@link com.sap.security.core.jmx.IJmxServer}):
	 * 
	 * <table border="1" style="margin-top:1.5ex; margin-bottom:1.5ex;">
	 *   <tr>
	 *     <th>Namespace</th>
	 *     <th>Name</th>
	 *     <th>Type</th>
	 *     <th>Description</th>
	 *   </tr>
	 *   <tr>
	 *     <td><code>{@link #UMAP_NAMESP_SYSTEM}</code></td>
	 *     <td><code>{@link #UMAP_ATTR_DISPLAY_NAME}</code></td>
	 *     <td>String</td>
	 *     <td>Localized name for the system, which is composed of 1) the name of the system 
	 *       landscape responsible for the system and 2) the system alias.</td>
	 *   </tr>
	 * </table>
     * 
	 * @param state
	 *            An array of {@link IJmxMapEntry}, which contains state
	 *            information like: Key {@link #STATE_LOCALE}, Value =
	 *            {@link java.lang.String} representation of
	 *            {@link java.util.Locale}
	 * @return <code>IJmxEntity</code>s representing all required information about all
	 *         systems from which the ABAP reference system may be selected.
	 * @throws Exception
	 */
	public IJmxEntity[] getSAPReferenceSystemCandidates2(CompositeData[] state) throws Exception;

	/**
	 * Retrieve a user via inverse user mapping
	 * @param userId		the users logon id
	 * @param systemAlias	the alias for the system to search in
	 * @param systemType	the system type
	 * @param state
	 *            An array of {@link IJmxMapEntry}, which contains state
	 *            information like: Key {@link #STATE_LOCALE}, Value =
	 *            {@link java.lang.String} representation of
	 *            {@link java.util.Locale}
	 * @return
	 */
	public IJmxEntity getUserByInverseUserMapping(
		String userId,
		String systemAlias,
		String systemType,
		CompositeData[] state)
		throws Exception;

	/**
	 * Returns feature list of available functionality (for example, see {@link #FEATURE_ORG_RESTRICTIONS_ENABLED})
	 * @param state
	 *            An array of {@link IJmxMapEntry}, which contains state
	 *            information like: Key {@link #STATE_LOCALE}, Value =
	 *            {@link java.lang.String} representation of
	 *            {@link java.util.Locale}
	 * @return {@link IJmxMapEntry} array
	 */
	public IJmxMapEntry[] getFeatureList(CompositeData[] state);

	/**
	 * Returns id and description of available datasources
	 * 
	 * @param type
	 *            A {@link String} identifier for type (e.g.
	 *            {@link com.sap.security.api.IPrincipalFactory#IUSER},
	 *            {@link com.sap.security.api.IPrincipalFactory#IGROUP},
	 *            {@link com.sap.security.api.IPrincipalFactory#IROLE})
	 * @param state
	 *            An array of {@link IJmxMapEntry}, which contains state
	 *            information like: Key {@link #STATE_LOCALE}, Value =
	 *            {@link java.lang.String} representation of
	 *            {@link java.util.Locale}
	 * @return {@link IJmxMapEntry} array
	 */
	public IJmxMapEntry[] getDatasources(String type, CompositeData[] state) throws Exception;

	/*********************************************************************************************
	 * Methods and constants used by the UME Consistency Check Tool                                            *
	 *********************************************************************************************/
	public static final String CCTOOL_ERROR_CLASS_DELETE_PRINCIPAL = IConsistencyCheckResponse.SOLUTION_DELETE_PRINCIPAL;

	public static final String CCTOOL_ERROR_CLASS_DELETE_ATTRIBUTE = IConsistencyCheckResponse.SOLUTION_DELETE_ATTRIBUTE;

	public static final String CCTOOL_ERROR_CLASS_DELETE_VALUES = IConsistencyCheckResponse.SOLUTION_DELETE_VALUES;

	public static final int CCTOOL_RETURN_CODE_OK = ConsistencyCheckHelper.OK;

	public static final int CCTOOL_RETURN_CODE_CHECK_CURRENTLY_RUNNING = ConsistencyCheckHelper.CHECK_CURRENTLY_RUNNING;

	public static final int CCTOOL_RETURN_CODE_NO_WORKING_DIRECTORY_SPECIFIED = ConsistencyCheckHelper.NO_WORKING_DIRECTORY_SPECIFIED;

	public static final int CCTOOL_RETURN_CODE_WORKING_DIRECTORY_INVALID = ConsistencyCheckHelper.WORKING_DIRECTORY_INVALID;

	public static final int CCTOOL_RETURN_CODE_REPAIR_CURRENTLY_RUNNING = ConsistencyCheckHelper.REPAIR_CURRENTLY_RUNNING;

	/**
	 * Returns the logs contained in the check log file.
	 * 
	 * @param state An IJmxMapEntry array, which contains state information like the key STATE_LOCALE
	 * @return The content of the check log file or null.
	 * @throws Exception If an error occurred.
	 */
	public String ccToolGetCheckLogResults(CompositeData[] state)
			throws Exception;

	/**
	 * Returns the principal ids which could be repaired. First index for principal, second
	 * index for principal detail.
	 * 
	 * @param errorClass The error class (see constants CCTOOL_ERROR_CLASS_*)
	 * @param state
	 *            An array of {@link IJmxMapEntry}, which contains state
	 *            information like: Key {@link #STATE_LOCALE}, Value =
	 *            {@link java.lang.String} representation of
	 *            {@link java.util.Locale}
	 * @return The principal ids.
	 * @throws Exception If an error occurred.
	 */
	public String[][] ccToolGetIdsToRepair(String errorClass,
			CompositeData[] state) throws Exception;

	/**
	 * Returns the logs contained in the repair log file.
	 * 
	 * @param state
	 *            An array of {@link IJmxMapEntry}, which contains state
	 *            information like: Key {@link #STATE_LOCALE}, Value =
	 *            {@link java.lang.String} representation of
	 *            {@link java.util.Locale}
	 * @return The content of the repair log file or null.
	 * @throws Exception If an error occurred.
	 */
	public String ccToolGetRepairLogResults(CompositeData[] state)
			throws Exception;

	/**
	 * Returns the used working directory.
	 * @param state
	 *            An array of {@link IJmxMapEntry}, which contains state
	 *            information like: Key {@link #STATE_LOCALE}, Value =
	 *            {@link java.lang.String} representation of
	 *            {@link java.util.Locale}
	 * @return The used working directory or null.
	 * @throws Exception If an error occurred.
	 */
	public String ccToolGetWorkingDirectory(CompositeData[] state)
			throws Exception;

	/**
	 * Returns whether inconsistencies were found in previous check.
	 * 
	 * @param state
	 *            An array of {@link IJmxMapEntry}, which contains state
	 *            information like: Key {@link #STATE_LOCALE}, Value =
	 *            {@link java.lang.String} representation of
	 *            {@link java.util.Locale}
	 * @return true, if inconsistencies were found, otherwise false
	 * @throws Exception If an error occurred.
	 */
	public boolean ccToolInconsistenciesFound(CompositeData[] state)
			throws Exception;

	/**
	 * Interrupts the currently running check process.
	 * 
	 * @param state
	 *            An array of {@link IJmxMapEntry}, which contains state
	 *            information like: Key {@link #STATE_LOCALE}, Value =
	 *            {@link java.lang.String} representation of
	 *            {@link java.util.Locale}
	 * @throws Exception If an error occurred.
	 */
	public void ccToolInterruptChecking(CompositeData[] state) throws Exception;

	/**
	 * Interrupts the currently running repair process.
	 * 
	 * @param state
	 *            An array of {@link IJmxMapEntry}, which contains state
	 *            information like: Key {@link #STATE_LOCALE}, Value =
	 *            {@link java.lang.String} representation of
	 *            {@link java.util.Locale}
	 * @throws Exception If an error occurred.
	 */
	public void ccToolInterruptRepairing(CompositeData[] state)
			throws Exception;

	/**
	 * Returns whether a check log file is available.
	 * 
	 * @param state
	 *            An array of {@link IJmxMapEntry}, which contains state
	 *            information like: Key {@link #STATE_LOCALE}, Value =
	 *            {@link java.lang.String} representation of
	 *            {@link java.util.Locale}
	 * @return true if a check log file is available, otherwise false.
	 * @throws Exception If an error occurred.
	 */
	public boolean ccToolIsCheckLogAvailable(CompositeData[] state)
			throws Exception;

	/**
	 * Returns whether a check process is currently running.
	 * 
	 * @param state
	 *            An array of {@link IJmxMapEntry}, which contains state
	 *            information like: Key {@link #STATE_LOCALE}, Value =
	 *            {@link java.lang.String} representation of
	 *            {@link java.util.Locale}
	 * @return true if a check process is currently running, otherwise false.
	 * @throws Exception If an error occurred.
	 */
	public boolean ccToolIsCheckStarted(CompositeData[] state) throws Exception;

	/**
	 * Returns whether a repair log file is available.
	 * 
	 * @param state
	 *            An array of {@link IJmxMapEntry}, which contains state
	 *            information like: Key {@link #STATE_LOCALE}, Value =
	 *            {@link java.lang.String} representation of
	 *            {@link java.util.Locale}
	 * @return true if a repair log file is available, otherwise false.
	 * @throws Exception If an error occurred.
	 */
	public boolean ccToolIsRepairLogAvailable(CompositeData[] state)
			throws Exception;

	/**
	 * Returns whether a repair process is currently running.
	 * 
	 * @param state
	 *            An array of {@link IJmxMapEntry}, which contains state
	 *            information like: Key {@link #STATE_LOCALE}, Value =
	 *            {@link java.lang.String} representation of
	 *            {@link java.util.Locale}
	 * @return true if a repair process is currently running, otherwise false.
	 * @throws Exception If an error occurred.
	 */
	public boolean ccToolIsRepairStarted(CompositeData[] state)
			throws Exception;

	/**
	 * Returns whether an undo file is available from a previous repair process.
	 * 
	 * @param state
	 *            An array of {@link IJmxMapEntry}, which contains state
	 *            information like: Key {@link #STATE_LOCALE}, Value =
	 *            {@link java.lang.String} representation of
	 *            {@link java.util.Locale}
	 * @return true if an und file is available, otherwise false.
	 * @throws Exception If an error occurred.
	 */
	public boolean ccToolIsUndoFileAvailable(CompositeData[] state)
			throws Exception;

	/**
	 * Returns whether a working directory is set.
	 * 
	 * @param state
	 *            An array of {@link IJmxMapEntry}, which contains state
	 *            information like: Key {@link #STATE_LOCALE}, Value =
	 *            {@link java.lang.String} representation of
	 *            {@link java.util.Locale}
	 * @return true, if a working directory is set, otherwise false.
	 * @throws Exception If an error occurred.
	 */
	public boolean ccToolIsWorkingDirectorySet(CompositeData[] state)
			throws Exception;

	/**
	 * Sets the working directory.
	 * 
	 * @param workingDirectory The new working directory to use.
	 * @param state
	 *            An array of {@link IJmxMapEntry}, which contains state
	 *            information like: Key {@link #STATE_LOCALE}, Value =
	 *            {@link java.lang.String} representation of
	 *            {@link java.util.Locale}
	 * @return The result state of the operation (see CCTOOL_RETURN_CODE_*)
	 * @throws Exception If an error occurred.
	 */
	public int ccToolSetWorkingDirectory(String workingDirectory,
			CompositeData[] state) throws Exception;

	/**
	 * Starts a check process, if no check or repair process is running and a valid working directory is set.
	 * 
	 * @param state
	 *            An array of {@link IJmxMapEntry}, which contains state
	 *            information like: Key {@link #STATE_LOCALE}, Value =
	 *            {@link java.lang.String} representation of
	 *            {@link java.util.Locale}
	 * @return The result state of the operation (see CCTOOL_RETURN_CODE_*)
	 * @throws Exception If an error occurred.
	 */
	public int ccToolStartChecking(CompositeData[] state) throws Exception;

	/**
	 * Starts a repair process, if no check or repair process is running and a valid working directory is set.
	 * 
	 * @param repairIDs The ids which are going to be repaired.
	 * @param notifyRegisteredListeners if false, no notifications are sent to registered listeners
	 * @param state
	 *            An array of {@link IJmxMapEntry}, which contains state
	 *            information like: Key {@link #STATE_LOCALE}, Value =
	 *            {@link java.lang.String} representation of
	 *            {@link java.util.Locale}
	 * @return The result state of the operation (see CCTOOL_RETURN_CODE_*)
	 * @throws Exception If an error occurred.
	 */
	public int ccToolStartRepairing(String[] repairIDs,
			boolean notifyRegisteredListeners, CompositeData[] state)
			throws Exception;

	/**
	 * Deletes all files under the specified directory.
	 * 
	 * @param basepath The path to the file
	 * @param state
	 *            An array of {@link IJmxMapEntry}, which contains state
	 *            information like: Key {@link #STATE_LOCALE}, Value =
	 *            {@link java.lang.String} representation of
	 *            {@link java.util.Locale}
	 * @throws Exception If an error occurred.
	 */
	public void ccToolDeleteFilesRecursively(String basepath,
			CompositeData[] state) throws Exception;

	/**
	 * Checks the current UME configuration if the system comes up 
	 * 
	 * @param properties the properties maintained in the Configuration UI
	 * @param state
	 *            An array of {@link IJmxMapEntry}, which contains state
	 *            information like: Key {@link #STATE_LOCALE}, Value =
	 *            {@link java.lang.String} representation of
	 *            {@link java.util.Locale}
	 * @throws Exception If an error occurred.
	 */
	public IJmxResult validateCurrentConfiguration(CompositeData[] properties, CompositeData[] state)
			throws Exception;

	/**
	 * Returns in case of a valid UME-ABAP configuration data about the
	 * maintained LDAP Sync servers form the ABAP stack 
	 * 
	 * @param properties the properties maintained in the Configuration UI
	 * @param state
	 *            An array of {@link IJmxMapEntry}, which contains state
	 *            information like: Key {@link #STATE_LOCALE}, Value =
	 *            {@link java.lang.String} representation of
	 *            {@link java.util.Locale}
	 * @throws Exception If an error occurred.
	 */
	public IJmxMap[] getLDAPSyncServerData(CompositeData[] properties, CompositeData[] state)
			throws Exception;

	/**
	 * This generic method provides access to different kind of information.
	 * @param identifier	See for example {@link #GENERIC_MAP_SAPTIMEZONE}
	 * @param state
	 *            An array of {@link IJmxMapEntry}, which contains state
	 *            information like: Key {@link #STATE_LOCALE}, Value =
	 *            {@link java.lang.String} representation of
	 *            {@link java.util.Locale}
	 * @return A {@link IJmxMap} with the requested information
	 * @throws Exception
	 */
	public IJmxMap getGenericMap(String identifier, CompositeData[] state) throws Exception;
	
	/**
	 * 
	 * @param attributes
	 *            An array of {@link IJmxAttribute}, which contains information about the 
	 *            user account like logonID, names 
	 * @param state
	 *            An array of {@link IJmxMapEntry}, which contains state
	 *            information like: Key {@link #STATE_LOCALE}, Value =
	 *            {@link java.lang.String} representation of
	 *            {@link java.util.Locale}
	 * @return A {@link IJmxMap} with the requested information
	 */
	public IJmxResult resetPassword(CompositeData[] attributes, CompositeData[] state) throws Exception;
	
	/**
	 * Invalidates caches
	 * @param cacheType		The type of cache, default null
	 * @param clusterWide	If the cache should be invalidated locally or clusterwide
	 * @param state
	 *            An array of {@link IJmxMapEntry}, which contains state
	 *            information like: Key {@link #STATE_LOCALE}, Value =
	 *            {@link java.lang.String} representation of
	 *            {@link java.util.Locale}
	 */
	public void invalidateCache(
		String cacheType,
		boolean clusterWide,
		CompositeData[] state)
		throws Exception;

	/**
	 * Download UME configuration data for SAP support as ZIP file.
	 * @param state
	 *            An array of {@link IJmxMapEntry}, which contains state
	 *            information like: Key {@link #STATE_LOCALE}, Value =
	 *            {@link java.lang.String} representation of
	 *            {@link java.util.Locale}
	 * @return bytes of the configuration ZIP file
	 * @throws Exception
	 */
	public byte[] downloadConfiguration(CompositeData[] state) throws Exception;
}

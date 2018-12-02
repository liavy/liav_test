package com.sap.security.core.jmx.impl;

import java.rmi.dgc.VMID;
import java.security.AccessControlException;
import java.security.Permission;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.OpenDataException;

import com.sap.security.api.AttributeList;
import com.sap.security.api.IGroup;
import com.sap.security.api.IMessage;
import com.sap.security.api.IPrincipal;
import com.sap.security.api.IPrincipalFactory;
import com.sap.security.api.IPrincipalMaint;
import com.sap.security.api.IPrincipalSearchFilter;
import com.sap.security.api.IRole;
import com.sap.security.api.ISearchAttribute;
import com.sap.security.api.ISearchResult;
import com.sap.security.api.IUser;
import com.sap.security.api.IUserAccount;
import com.sap.security.api.IUserAccountSearchFilter;
import com.sap.security.api.IUserMaint;
import com.sap.security.api.IUserSearchFilter;
import com.sap.security.api.NoSuchPrincipalException;
import com.sap.security.api.PrincipalNotAccessibleException;
import com.sap.security.api.UMException;
import com.sap.security.api.UMFactory;
import com.sap.security.api.logon.ILoginConstants;
import com.sap.security.api.util.IUMParameters;
import com.sap.security.core.InternalUMFactory;
import com.sap.security.core.admin.UserAdminHelper;
import com.sap.security.core.admin.permissions.UMAdminPermissions;
import com.sap.security.core.imp.AbstractPrincipal;
import com.sap.security.core.imp.GroupFactory;
import com.sap.security.core.imp.PrincipalFactory;
import com.sap.security.core.imp.PrincipalIteratorImpl;
import com.sap.security.core.imp.Role;
import com.sap.security.core.imp.RoleFactory;
import com.sap.security.core.imp.TenantFactory;
import com.sap.security.core.imp.UserFactory;
import com.sap.security.core.jmx.IJmxAttribute;
import com.sap.security.core.jmx.IJmxMessage;
import com.sap.security.core.jmx.IJmxState;
import com.sap.security.core.jmx.IJmxTable;
import com.sap.security.core.persistence.IPrincipalDatabag;
import com.sap.security.api.persistence.IDataSourceMetaData;
import com.sap.security.core.persistence.datasource.PersistenceException;
import com.sap.security.core.persistence.datasource.imp.CompanyGroups;
import com.sap.security.core.persistence.datasource.imp.DataSourceBaseImplementation;
import com.sap.security.core.persistence.datasource.imp.RemoteRolesDataSource;
import com.sap.security.core.persistence.imp.PersistenceCollection;
import com.sap.security.core.persistence.imp.PrincipalDatabag;
import com.sap.security.core.persistence.imp.PrincipalDatabagFactory;
import com.sap.security.core.persistence.remote.fpn.IRemoteProducerAccess;
import com.sap.security.core.util.CompanyWrapper;
import com.sap.security.core.util.UMEConfigurationPermission;
import com.sap.security.core.util.UMEEntityAccessPermission;
import com.sap.security.core.util.UMEPermission;
import com.sap.security.core.util.resources.CoreMessageBean;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;
import com.sapmarkets.tpd.TradingPartnerDirectoryCommon;
import com.sapmarkets.tpd.master.PartnerID;
import com.sapmarkets.tpd.master.TradingPartnerDirectoryInterface;
import com.sapmarkets.tpd.master.TradingPartnerInterface;
import com.sapmarkets.tpd.util.TpdException;

/**
 * @author d031174
 * 
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class CompanyPrincipalFactory extends CompanyWrapper {

	/*
	 * public
	 */
	public static final String UNIQUEID = "uniqueid";

	public static final String ORGUNIT = "orgunit";
	
	public static final String UME_ROLE_PERSISTENCE = "UME_ROLE_PERSISTENCE";
	
	public static final String PCD_ROLE_PERSISTENCE = "PCD_ROLE_PERSISTENCE";

	public static final String COMPANIES_ALL_IDENTIFIER = InternalUMFactory.getConfiguration().getStringStatic(IUMParameters.UME_ADMIN_WD_TENANT_IDENTIFIER_ALL, "- All -");

	public static final String COMPANIES_NO_IDENTIFIER = InternalUMFactory.getConfiguration().getStringStatic(IUMParameters.UME_ADMIN_WD_TENANT_IDENTIFIER_NONE, "- None -");

	public static final String EMPTY = "";
	
	public static final String DUMMY_PRINCIPAL_NAME = "SAPDUMMYPRNC";

	public static final String PASSWORD_STARS = "********";

	/*
	 * private static
	 */
	private static final String COMPANIES_ALL = TenantFactory.ALL_TENANTS_VALUE;

	private static final String WILDCARD = "*";

	private static final String DOT = ".";
	
	public static final String TRUE = "true";
	
	public static final String FALSE = "false";
	
	private static final boolean UME_JMX_SEARCH_SIMPLE_ROLE_ACCESS__CHECK = InternalUMFactory.getConfiguration().getBooleanDynamic("ume.jmx.search.simple.role.access_check", true);

	private static HashMap myFactoryMap = new HashMap();

	private static Location myLoc =
		Location.getLocation(CompanyPrincipalFactory.class);

	public final static String NAMESPACE_ACL =
		"com.sap.security.pcd.aclprincipal";

	private boolean mTxSupport;

	/*
	 * Datasources
	 */
	//For translation
	public static final String ALL_DATASOURCES_TRANSLATION = "ALL_DATAOURCES";

	public static final String LOCAL_DATASOURCES_TRANSLATION = "LOCAL_DATAOURCES";
	
	public static final String REMOTE_DATASOURCES_TRANSLATION = "REMOTE_DATAOURCES";
	
	//For identification
	public static final String ALL_DATASOURCES = IPrincipal.DEFAULT_NAMESPACE + DOT + ALL_DATASOURCES_TRANSLATION;

	public static final String LOCAL_DATASOURCES = IPrincipal.DEFAULT_NAMESPACE + DOT + LOCAL_DATASOURCES_TRANSLATION;
	
	public static final String REMOTE_DATASOURCES = IPrincipal.DEFAULT_NAMESPACE + DOT + REMOTE_DATASOURCES_TRANSLATION;
	
	/*
	 * private
	 */
	private String myCompanyId;

	private CompanyPrincipalFactory(String companyId, boolean txSupport) {
		final String mn = "private CompanyPrincipalFactory(String companyId)";
		if (myLoc.beInfo()) {
			myLoc.infoT(
				mn,
				"Active Company Concept is {0}, given company id is {1}",
				new Object[] { ACTIVE_COMPANY_ATRRIBUTE_NAME, companyId });
		}
		mTxSupport = txSupport;
		switch (myUsedCompanyConcept) {
			//if CC is of, no company will be available
			case CC_OFF :
				companyId = null;
				break;
			/* if BPO is enabled, in addition no or all companies could be available
			 * companyId = null if external companyId ==
			 * 		null
			 * 		""
			 * 		"- NO -"
			 * companyId = "ALL" if external companyId ==
			 * 		"- ALL -"
			 */
			case CC_BPO :
				if (companyId == null
					|| EMPTY.equals(companyId)
					|| COMPANIES_NO_IDENTIFIER.equals(companyId)) {
					companyId = null;
				} else if (COMPANIES_ALL_IDENTIFIER.equals(companyId)) {
					companyId = COMPANIES_ALL;
				}
				break;
			/* if tpd is enabled, in addition all companies and guest company could be available
			 * companyId = null if external companyId ==
			 * 		null
			 * 		"- ALL -"
			 * companyId = ""	if
			 * 		""
			 * 		"- GUEST_USER_COMPANY -"
			 * 
			 * Notice:
			 * 1. if GUEST_USER_COMPANY is activated, all users have "" as company attribute
			 * 2. if company concept was enabled after users already self registered, these users
			 *    have null as company attribute
			 */
			case CC_TPD :
				if (companyId == null
					|| COMPANIES_ALL_IDENTIFIER.equals(companyId)) {
					companyId = null;
				} else if (EMPTY.equals(companyId)
					|| COMPANIES_NO_IDENTIFIER.equals(companyId)){
					companyId = "";
				}
				break;
			default :
				companyId = null;
				break;
		}
		if (myLoc.beInfo()) {
			myLoc.infoT(
				mn,
				"Internal company id is {0}",
				new Object[] { companyId });
		}
		this.myCompanyId = companyId;
	}

	/*
	 * Methods for company concept
	 */

	/**
	 * 
	 * @param String
	 *            companyId Company, for which the principal factory should work
	 *            for. In case of an empty string or null, the default
	 *            PrincipalFactory will be used.
	 * @return IPrincipalFactory An instance of the CompanyPrincipalFactory,
	 *         which works only for the specified companyId.
	 */
	public static CompanyPrincipalFactory getInstance(String companyId) {
		Object o;
		if ((o = myFactoryMap.get(companyId)) != null) {
			return (CompanyPrincipalFactory) o;
		}
		CompanyPrincipalFactory cpf = new CompanyPrincipalFactory(companyId, false);
		myFactoryMap.put(companyId, cpf);
		return cpf;
	}

	/**
	 * 
	 * @return Map A Map of company ids with its descriptions or an empty
	 *         Collection if company concept is not enabled. The Map consists of
	 *         "id"-"description"
	 * @throws UMException
	 * @throws OpenDataException
	 */
	public static IJmxTable getCompanies(IJmxState state)
		throws UMException, OpenDataException {
		final String mn = "public static IJmxTable getCompanies()";
		if (myLoc.beInfo()) {
			myLoc.infoT(
				mn,
				"Active Company Concept is {0}",
				new Object[] { ACTIVE_COMPANY_ATRRIBUTE_NAME });
		}
		CoreMessageBean coreMessageBean =
			new CoreMessageBean(state.getLocale());
		JmxTable returnTable = new JmxTable();
		JmxTableRow[] rows = new JmxTableRow[0];
		Map companies = new HashMap();
		switch (myUsedCompanyConcept) {
			case CC_BPO :
				if (TenantFactory.isLoggedInUserServiceProviderAdmin()) {
					if (myLoc.beInfo()) {
						myLoc.infoT(
							mn,
							"Logged in user is service provider admin");
					}
					Collection tenants =
						TenantFactory.getInstance().getAvailableTenants();
					Iterator tenantsIt = tenants.iterator();
					while (tenantsIt.hasNext()) {
						String tenant = (String) tenantsIt.next();
						companies.put(
							tenant,
							TenantFactory.getInstance().getTenantDescription(
								tenant));
						if (myLoc.beDebug()) {
							traceCompany(mn, tenant, TenantFactory.getInstance().getTenantDescription(tenant));
						}
					}
					rows = new JmxTableRow[companies.size()];
					TreeSet sortedkeys = new TreeSet(companies.keySet());
					int counter = 0;
					for (Iterator keyIt = sortedkeys.iterator();
						keyIt.hasNext();
						counter++) {
						String id = (String) keyIt.next();
						String desc = (String) companies.get(id);
						rows[counter] = new JmxTableRow();
						rows[counter].setCol0(id);
						rows[counter].setCol1(desc);
					}
				} else {
					if (myLoc.beInfo()) {
						myLoc.infoT(
							mn,
							"Logged in user is service provider admin");
					}
					rows = new JmxTableRow[1];
					rows[0] = new JmxTableRow();
					String tenant = TenantFactory.getTenantIdOfLoggedInUser();
					if (tenant != null) {
						rows[0].setCol0(tenant);
						rows[0].setCol1(
							TenantFactory.getInstance().getTenantDescription(
								tenant));
						if (myLoc.beDebug()) {
							traceCompany(mn, tenant, TenantFactory.getInstance().getTenantDescription(tenant));
						}
					} else {
						rows[0].setCol0(COMPANIES_NO_IDENTIFIER);
						rows[0].setCol1(
							coreMessageBean.get(
								CoreMessageBean.COMPANY_CONCEPT_NO_COMPANY));
						if (myLoc.beDebug()) {
							traceCompany(mn, COMPANIES_NO_IDENTIFIER, coreMessageBean.get(
								CoreMessageBean.COMPANY_CONCEPT_NO_COMPANY));
						}
					}
				}
				break;
			case CC_TPD :
				TradingPartnerDirectoryInterface tpd =
					TradingPartnerDirectoryCommon.getTPD();
				IUser loggedInUser =
					UMFactory.getAuthenticator().getLoggedInUser();
				if (myLoc.beInfo()) {
					myLoc.infoT(
						mn,
						"Logged in user is {0}",
						new Object[] { loggedInUser });
				}
				if (loggedInUser.hasPermission(
						new UMAdminPermissions(UserAdminHelper.MANAGE_ALL_COMPANIES))
					||
					loggedInUser.hasPermission(
						new UMEPermission(
									IPrincipalDatabag.USER_TYPE,
									UMEPermission.ACTION_READ,
									UMEPermission.COM_SAP_SECURITY_CORE_USERMANAGEMENT_UNIQUEID,
									null, null, loggedInUser.getUniqueID()))) {
					if (myLoc.beInfo()) {
						myLoc.infoT(
							mn,
							"Logged in user is allowed to see all companies");
					}
					//if TPD is enabled, add all companies
					try {
						Enumeration partners = tpd.getPartners();
						while (partners.hasMoreElements()) {
							TradingPartnerInterface tp =
								(TradingPartnerInterface) partners
									.nextElement();
							companies.put(tp.getPartnerID().toString(), tp.getDisplayName());
							if (myLoc.beDebug()) {
								traceCompany(mn, tp.getPartnerID().toString(), tp.getDisplayName());
							}
						}
					} catch (TpdException tpde) {
						myLoc.traceThrowableT(
							Severity.ERROR,
							mn,
							"Error retreiving companies",
							tpde);
						throw new UMException(
							tpde,
							"Error retreiving companies");
					}

					int counter = 0;
					rows = new JmxTableRow[companies.size()];
					TreeSet sortedkeys = new TreeSet(companies.keySet());
					for (Iterator keyIt = sortedkeys.iterator();
						keyIt.hasNext();
						counter++) {
						String id = (String) keyIt.next();
						String desc = (String) companies.get(id);
						rows[counter] = new JmxTableRow();
						rows[counter].setCol0(id);
						rows[counter].setCol1(desc);
					}
				} else {
					if (myLoc.beInfo()) {
						myLoc.infoT(
							mn,
							"Logged in user has not manage all companies");
					}
					rows = new JmxTableRow[1];
					rows[0] = new JmxTableRow();
					String company = loggedInUser.getCompany();
					if (company != null) {
						if (EMPTY.equals(company)){
							rows[0].setCol0(COMPANIES_NO_IDENTIFIER);
							rows[0].setCol1(
								coreMessageBean.get(
									CoreMessageBean
										.COMPANY_CONCEPT_NO_COMPANY));
							if (myLoc.beDebug()) {
								traceCompany(mn, COMPANIES_NO_IDENTIFIER, coreMessageBean.get(
									CoreMessageBean.COMPANY_CONCEPT_NO_COMPANY));
							}
						} else {
							try {
								TradingPartnerInterface tp =
									tpd.getPartner(PartnerID.instantiatePartnerID(company));
								if (tp != null) {
									rows[0].setCol0(tp.getPartnerID().toString());
									rows[0].setCol1(tp.getDisplayName());
									if (myLoc.beDebug()) {
										traceCompany(mn, tp.getPartnerID().toString(), tp.getDisplayName());
									}
								} else {
									rows[0].setCol0(COMPANIES_ALL_IDENTIFIER);
									rows[0].setCol1(
										coreMessageBean.get(
											CoreMessageBean
												.COMPANY_CONCEPT_ALL_COMPANIES));
									if (myLoc.beDebug()) {
										traceCompany(mn, COMPANIES_ALL_IDENTIFIER, coreMessageBean.get(
											CoreMessageBean.COMPANY_CONCEPT_ALL_COMPANIES));
									}
								}
							} catch (TpdException tpde) {
								myLoc.traceThrowableT(
									Severity.ERROR,
									mn,
									"Error retreiving companies",
									tpde);
								throw new UMException(
									tpde,
									"Error retreiving companies");
							}
						}
					} else {
						rows[0].setCol0(COMPANIES_NO_IDENTIFIER);
						rows[0].setCol1(
							coreMessageBean.get(
								CoreMessageBean.COMPANY_CONCEPT_NO_COMPANY));
						if (myLoc.beDebug()) {
							traceCompany(mn, COMPANIES_NO_IDENTIFIER, coreMessageBean.get(
								CoreMessageBean.COMPANY_CONCEPT_NO_COMPANY));
						}
					}
				}
				break;
			case CC_OFF :
				break;
			default :
				break;
		}
		returnTable.setTableRows(rows);
		returnTable.setSize(rows.length);
		returnTable.setState(ISearchResult.SEARCH_RESULT_OK);
		returnTable.setGuid(EMPTY);
		return returnTable;
	}

	private static void traceCompany(String mn, String id, String desc) {
		myLoc.debugT(
			mn,
			"Adding tenant {0}, description {1}",
			new Object[] {
				id,
				desc});
	}

	/**
	 * @return Active company attribute
	 */
	public static String getActiveCompanyAttributeName() {
		return ACTIVE_COMPANY_ATRRIBUTE_NAME;
	}

	/**
	 * Changes the company attribute the right way on a principal
	 * For creation, this is done by newUser, newUserAccount, newRole, newGroup etc.
	 * 	for BPO, the change is not allowed at all
	 * 	for TPD, the company can only be changed for users
	 * @param principalMaint
	 * @param company
	 * @throws UMException when principal is not muteable
	 */
	public void setCompany(IPrincipalMaint principalMaint, String company)
		throws UMException {
		final String mn =
			"public void setCompany(IPrincipalMaint principal, String value)";
		if (principalMaint.getUniqueID() == null){
			if (myLoc.beInfo()){
				myLoc.infoT(mn, "Principal is not persistent");
			}
			return;
		}
		String principalType =
			getPrincipalType(
				principalMaint.getUniqueID());
		if (myLoc.beInfo()) {
			myLoc.infoT(
				mn,
				"Principal is {0} company value {1}, active company concept is {2}",
				new Object[] {
					principalMaint,
					company,
					ACTIVE_COMPANY_ATRRIBUTE_NAME });
		}
		if (!principalMaint.isMutable()) {
			throw new UMException(
				"Principal " + principalMaint + " is not muteable");
		}
		AbstractPrincipal principal = (AbstractPrincipal) principalMaint;
		switch (myUsedCompanyConcept) {
			case CC_BPO :
				//no change of a tenant after creation
				break;
			case CC_TPD :
				//only setting a company on a user
				if (PrincipalFactory.IUSER.equals(principalType) && principal instanceof IUserMaint) {
					IUserMaint userMaint = (IUserMaint) principal;
					if (userMaint
						.getCompany()
						== null
						|| !userMaint.getCompany().equals(
							company)) {
						if (EMPTY.equals(company)
							|| COMPANIES_NO_IDENTIFIER.equals(company)
							|| COMPANIES_ALL_IDENTIFIER.equals(company)
							|| company == null) {
							userMaint.setCompany(EMPTY);
						} else {
							userMaint.setCompany(company);
						}
					}
				}
				break;
			default :
				//nothing todo if company concept is disabled
				break;
		}
	}

	/*
	 * Other helper methods
	 */
	/**
	 * @param requestDatasourceIds
	 * @param privateType
	 * @param locale
	 * @return {@link Set} of datasources or <code>null</code> if no
	 *         datasources are available
	 * @throws {@link PersistenceException}
	 */
	public static Set<String> evaluateDatasourcesToSearchFor(String[] requestDatasourceIds,
			String privateType, Locale locale, List<IJmxMessage> additionalMessages) throws PersistenceException, OpenDataException{
		final String mn = "static Set evaluateDatasourcesToSearchFor(String[] requestDatasourceIds,	String privateType, Locale locale)";
		if (requestDatasourceIds != null && requestDatasourceIds.length > 0
				&& requestDatasourceIds[0] != null
				&& !EMPTY.equals(requestDatasourceIds[0])) {
			Set<String> datasourceSet = new HashSet<String>(requestDatasourceIds.length);
			evaluateDatasourcesToSearchFor: {
				Set<String> requestDatasourceIdsSet = new HashSet<String>(
						requestDatasourceIds.length);
				Set<String> availableDatasources = getDatasourceIds(privateType);
				for (int i = 0; i < requestDatasourceIds.length; i++) {
					requestDatasourceIdsSet.add(requestDatasourceIds[i]);
				}
				/*
				 * for roles and activated remote concept, special handling
				 * is needed if all datasources is in the request datasource
				 * list, all datasources have to be checked for remote
				 * connectivity.
				 */
				if (PrincipalDatabag.ROLE_TYPE.equals(privateType)
						&& isRemoteProducerAccessEnabled()) {
					if (requestDatasourceIdsSet.contains(ALL_DATASOURCES)) {
						requestDatasourceIdsSet.remove(ALL_DATASOURCES);
						for (Iterator it = availableDatasources.iterator(); it
								.hasNext();) {
							String id = (String) it.next();
							if (id
									.startsWith(RemoteRolesDataSource.ID_PREFIX)) {
								try {
									InternalUMFactory
											.getRemoteProducerAccess()
											.checkConnectivity(
													id
															.substring(RemoteRolesDataSource.ID_PREFIX
																	.length()),
													locale);
									datasourceSet.add(id);
								} catch (Exception e) {
									traceConnectivityCheckException(additionalMessages, e, id, locale, mn);
								}
							} else {
								datasourceSet.add(id);
							}
						}
						break evaluateDatasourcesToSearchFor;
					}
					if (requestDatasourceIdsSet.contains(LOCAL_DATASOURCES)) {
						requestDatasourceIdsSet.remove(LOCAL_DATASOURCES);
						for (Iterator it = availableDatasources.iterator(); it
								.hasNext();) {
							String id = (String) it.next();
							if (!id
									.startsWith(RemoteRolesDataSource.ID_PREFIX)) {
								datasourceSet.add(id);
							}
						}
					}
					if (requestDatasourceIdsSet
							.contains(REMOTE_DATASOURCES)) {
						requestDatasourceIdsSet.remove(REMOTE_DATASOURCES);
						for (Iterator it = availableDatasources.iterator(); it
								.hasNext();) {
							String id = (String) it.next();
							if (id
									.startsWith(RemoteRolesDataSource.ID_PREFIX)) {
								try {
									InternalUMFactory
											.getRemoteProducerAccess()
											.checkConnectivity(
													id
															.substring(RemoteRolesDataSource.ID_PREFIX
																	.length()),
													locale);
									datasourceSet.add(id);
								} catch (Exception e) {
									traceConnectivityCheckException(additionalMessages, e, id, locale, mn);
								}
							}
						}
					}
					/*
					 * remaining datasources for roles and activated remote
					 * concept
					 */
					for (Iterator it = requestDatasourceIdsSet.iterator(); it
							.hasNext();) {
						String id = (String) it.next();
						if (!datasourceSet.contains(id)
								&& availableDatasources.contains(id)) {
							if (id
									.startsWith(RemoteRolesDataSource.ID_PREFIX)) {
								try {
									InternalUMFactory
											.getRemoteProducerAccess()
											.checkConnectivity(
													id
															.substring(RemoteRolesDataSource.ID_PREFIX
																	.length()),
													locale);
									datasourceSet.add(id);
								} catch (Exception e) {
									traceConnectivityCheckException(additionalMessages, e, id, locale, mn);
								}
							} else {
								datasourceSet.add(id);
							}
						}
					}
					break evaluateDatasourcesToSearchFor;
				}
				/*
				 * remaining datasources for roles and deactivated remote
				 * concept and all other types
				 */
				if (requestDatasourceIdsSet.contains(ALL_DATASOURCES)) {
					requestDatasourceIdsSet.remove(ALL_DATASOURCES);
					datasourceSet = availableDatasources;
					break evaluateDatasourcesToSearchFor;
				}
				for (Iterator it = requestDatasourceIdsSet.iterator(); it
						.hasNext();) {
					String id = (String) it.next();
					if (availableDatasources.contains(id)) {
						datasourceSet.add(id);
					}
				}
				break evaluateDatasourcesToSearchFor;
			}
			return datasourceSet;			
		} else {
			return null;
		}
	}
	
	private static void traceConnectivityCheckException(
			List<IJmxMessage> additionalMessages, Exception e, String producerId,
			Locale locale, String mn) throws OpenDataException {
		producerId = producerId
				.substring(RemoteRolesDataSource.ID_PREFIX.length());
		String producerName = producerId;
		String[] aliases = InternalUMFactory.getRemoteProducerAccess().getProducerAliases(new String[]{producerId});
		if (aliases != null && aliases.length > 0 && aliases[0] != null){
			producerName = aliases[0];
		}
		myLoc.traceThrowableT(Severity.ERROR, mn,
				"Error while connecting to remote producer {0} with id {1}",
				new Object[] { producerName, producerId }, e);

		IJmxMessage message = new JmxMessage(IMessage.TYPE_WARNING,
				IMessage.LIFETIME_ONCE, IMessage.CATEGORY_PROCESS,
				new java.util.Date(),
				"Error while connecting to remote producer \"" + producerName + "\"",
				MessageFormat.format(CoreMessageBean.getInstance(locale).get(
						CoreMessageBean.PRODUCER_NOT_AVAILABLE),
						new Object[] { producerName }), new VMID().toString());
		additionalMessages.add(message);

	}

	public static boolean isRemoteProducerAccessEnabled() {
		IRemoteProducerAccess pAccess = InternalUMFactory
				.getRemoteProducerAccess();
		if (pAccess != null) {
			Collection producerIDs = pAccess.getAvailabeProducers();
			if (producerIDs != null && producerIDs.size() > 0) {
				return true;
			}
		}
		return false;
	}
	
    public static String localizeDatasourceId(String datasourceId, IJmxState state){
		String ds = CoreMessageBean.getInstance(state.getLocale()).get(CoreMessageBean.DATASOURCE_SUBSTRING + datasourceId);
		if (CoreMessageBean.DEFAULT_LABEL.equals(ds)){
			return null;
		}
		return ds;
    }
    
	public static Set<String> getDatasourceIds(String type) throws PersistenceException {
		type = CompanyPrincipalFactory.getPrivatePrincipalTypeIdentifier(type);
		IDataSourceMetaData[] dsmd = PrincipalDatabagFactory.getInstance()
				.getResponsibleDataSources(type);
		if (dsmd != null){
			Set<String> ids = new HashSet<String>(dsmd.length);
			for (int i = 0; i < dsmd.length; i++) {
				ids.add(dsmd[i].getDataSourceID());
			}
			return ids;
		} else {
			return new HashSet<String>();
		}
	}
	
	public static int getSearchOperator(String val){
   	 	if ((val.indexOf('*') == -1) && (val.indexOf('?') == -1)) {
			return ISearchAttribute.EQUALS_OPERATOR;
   	    } else {
   	    	return ISearchAttribute.LIKE_OPERATOR;
   	    }
	}

	public static String getPublicPrincipalTypeIdentifier(String privatePrincipalType) {
		if (privatePrincipalType.equals(IPrincipalDatabag.USER_TYPE)) {
			return PrincipalFactory.IUSER;
		}
		if (privatePrincipalType.equals(IPrincipalDatabag.ACCOUNT_TYPE)) {
			return PrincipalFactory.IUSERACCOUNT;
		}
		if (privatePrincipalType.equals(IPrincipalDatabag.GROUP_TYPE)) {
			return PrincipalFactory.IGROUP;
		}
		if (privatePrincipalType.equals(IPrincipalDatabag.ROLE_TYPE)) {
			return PrincipalFactory.IROLE;
		}
		if (privatePrincipalType.equals(JmxActionFactoryWrapper.ACTION_TYPE)) {
			return JmxActionFactoryWrapper.IACTION;
		}
		return privatePrincipalType;
	}

	public static String getPrivatePrincipalTypeIdentifier(String publicPrincipalType) {
		if (publicPrincipalType.equals(PrincipalFactory.IUSER)) {
			return IPrincipalDatabag.USER_TYPE;
		}
		if (publicPrincipalType.equals(PrincipalFactory.IUSERACCOUNT)) {
			return IPrincipalDatabag.ACCOUNT_TYPE;
		}
		if (publicPrincipalType.equals(PrincipalFactory.IGROUP)) {
			return IPrincipalDatabag.GROUP_TYPE;
		}
		if (publicPrincipalType.equals(PrincipalFactory.IROLE)) {
			return IPrincipalDatabag.ROLE_TYPE;
		}
		if (publicPrincipalType.equals(JmxActionFactoryWrapper.IACTION)) {
			return JmxActionFactoryWrapper.ACTION_TYPE;
		}
		return publicPrincipalType;
	}

	/**
	 * Invalidate principal from PrincipalCache
	 * @param uniqueId
	 */
	public void invalidatePrincipalInCache(String uniqueId) throws UMException{
		if (uniqueId != null){
			String principalType = getPrincipalType(uniqueId);
			if (IPrincipalFactory.IUSER.equals(principalType)){
				UMFactory.getUserFactory().invalidateCacheEntry(uniqueId);
			} else if (IPrincipalFactory.IUSERACCOUNT.equals(principalType)){
				UMFactory.getUserAccountFactory().invalidateCacheEntry(uniqueId);
			} else if (IPrincipalFactory.IGROUP.equals(principalType)){
				GroupFactory.invalidateGroupInCacheLocally(uniqueId);
			} else if (IPrincipalFactory.IROLE.equals(principalType)){
				RoleFactory.invalidateRoleInCacheLocally(uniqueId);
			} else if (JmxActionFactoryWrapper.IACTION.equals(principalType)){
				JmxActionFactoryWrapper.invalidateCacheEntry(uniqueId);
			}			
		}
	}

	/**
	 * @param uniqueIds Array of unique ids
	 * @return the number of company groups
	 */
	public static int getNumberOfCompanyGroups(String[] uniqueIds) throws UMException{
		PrincipalFactory principalFactory = (PrincipalFactory)UMFactory.getPrincipalFactory();
		int amountCompGroups = 0;
		for (int i = 0; i < uniqueIds.length; i++){
			String[] idParts = principalFactory.getPrincipalIdParts(uniqueIds[i]);
			if (CompanyGroups.DATASOURCE_ID.equals(idParts[1]))
			{
				amountCompGroups++;
			}
		}
		return amountCompGroups;
	}

	/**
	 * @param uniqueId of group
	 * @return boolean if group is company group
	 */
	public static boolean isCompanyGroup(String uniqueId) throws UMException{
		PrincipalFactory principalFactory = (PrincipalFactory)UMFactory.getPrincipalFactory();
		String[] idParts = principalFactory.getPrincipalIdParts(uniqueId);
		if (CompanyGroups.DATASOURCE_ID.equals(idParts[1]))
		{
			return true;
		}
		return false;
	}

	public static Locale createLocale(String localeString) {
		final String mn = "private static Locale createLocale(String localeString)";
		if (myLoc.beInfo()) {
			myLoc.infoT(mn, "Locale string is {0}",
					new Object[] { localeString });
		}
		Locale result = null;
		try {
			if (localeString == null) {
				return null;
			}
			String language = null;
			String country = null;
			String variant = null;
			if (localeString.indexOf("_") == -1) {
				language = localeString;
				country = "";
			} else {
				language = localeString.substring(0, localeString.indexOf("_"));
				country = localeString.substring(localeString.indexOf("_") + 1);
				if (country.indexOf("_") == -1) {
					variant = null;
				} else {
					variant = country.substring(country.indexOf("_") + 1);
					country = country.substring(0, country.indexOf("_"));
				}

			}
			if (language != null) {
				if ("".equals(language)) {
					return null;
				}
				if ((country != null) && (variant != null)) {
					result = new Locale(language, country, variant);
					return result;
				} else if (country != null) {
					result = new Locale(language, country);
					return result;
				} else {
					result = new Locale(language, "");
					return result;
				}
			}
			return null;
		} finally {
			if (myLoc.beInfo()) {
				myLoc.infoT(mn, "Returned locale is {0}",
						new Object[] { result });
			}
		}
	}

	/**
	 * @param principalType the principal type to be created
	 * @param relatedUser if a user account hass to be created, the related user has to be given
	 * @return
	 * @throws UMException
	 */
	public static IPrincipal createDummyPrincipal(String principalType, IUser relatedUser) throws UMException{
		principalType = CompanyPrincipalFactory.getPrivatePrincipalTypeIdentifier(principalType);
		if (PrincipalDatabag.USER_TYPE.equals(principalType)){
			IUserMaint um = ((UserFactory)UMFactory.getUserFactory()).createDummyUserMaint();
			return um;
		}
		if (PrincipalDatabag.ACCOUNT_TYPE.equals(principalType)){
			String uniqueName = null;
			if (relatedUser == null){
				uniqueName = UMFactory.getSecurityPolicy().generateLogonId();
			} else {
				uniqueName = relatedUser.getUniqueName();
			}
			return UMFactory.getUserAccountFactory().newUserAccount(uniqueName);
		}
		if (PrincipalDatabag.GROUP_TYPE.equals(principalType)){
			return UMFactory.getGroupFactory().newGroup(CompanyPrincipalFactory.DUMMY_PRINCIPAL_NAME);
		}
		if (PrincipalDatabag.ROLE_TYPE.equals(principalType)){
			return UMFactory.getRoleFactory().newRole(CompanyPrincipalFactory.DUMMY_PRINCIPAL_NAME);
		}
		return null;
	}

	/*
	 * Methods for permission check
	 */
	public PrincipalIteratorImpl getTypedPrincipalIterator(
		PrincipalIteratorImpl principalIt,
		String requestPrincipalType,
		String[] datasourceIds,
		Locale locale)
		throws UMException, OpenDataException {
		final String mn =
			"public PrincipalIteratorImpl getTypedPrincipalIterator(PrincipalIteratorImpl principalIt, boolean mixedPrincipalTypes,	String requestPrincipalType)";
		if (requestPrincipalType == null || principalIt == null) {
			myLoc.errorT(
				mn,
				"Invalid parameter; principalType {0}, principalIt {1}",
				new Object[] { requestPrincipalType, principalIt });
			throw new UMException("Invalid parameter");
		}
		List<IJmxMessage> additionalMessages = new ArrayList<IJmxMessage>();
		Set<String> datasources = evaluateDatasourcesToSearchFor(datasourceIds,
				getPrivatePrincipalTypeIdentifier(requestPrincipalType), locale, additionalMessages);
		if (principalIt.hasNext()) {
			Collection coll = new PersistenceCollection();
			requestPrincipalType =
				getPublicPrincipalTypeIdentifier(requestPrincipalType);
			while (principalIt.hasNext()) {
				String uniqueId = (String) principalIt.next();
				String currentType = getPrincipalType(uniqueId);
				if (requestPrincipalType.equals(currentType)) {
					if (datasources != null){
						try {
							if (!datasources
									.contains(DataSourceBaseImplementation
											.staticSplitPrincipalDatabagID(uniqueId)[1])) {
								continue;
							}
						} catch (PersistenceException e) {
							myLoc.traceThrowableT(
								Severity.ERROR,
								mn,
								e);
							continue;
						}
					}
					if (PrincipalFactory.IROLE.equals(requestPrincipalType)) {
						try {
							checkAclForRole(uniqueId, true);
						} catch (PrincipalNotAccessibleException e) {
							if (myLoc.beDebug()) {
								myLoc.traceThrowableT(
									Severity.DEBUG,
									mn,
									e);
							}
							continue;
						} catch (UMException e) {
							if (myLoc.beDebug()) {
								myLoc.traceThrowableT(
									Severity.DEBUG,
									mn,
									e);
							}
							continue;
						}
						coll.add(uniqueId);
					} else {
						coll.add(uniqueId);
					}
				}
			}
			return new PrincipalIteratorImpl(coll, PrincipalIteratorImpl.ITERATOR_TYPE_UNIQUEIDS_NOT_CHECKED);
		} else {
			return principalIt;
		}
	}

	public Permission instantiatePermission(JmxPermission jmxPerm) {
		try
		{
			if (mTxSupport) InternalUMFactory.setHookInRunningTransaction(mTxSupport);
			
		final String mn =
			"public Permission instantiatePermission(JmxPermission jmxPerm)";
		if (myLoc.beDebug()) {
			myLoc.debugT(mn, "JmxPermission is {0}", new Object[] { jmxPerm });
		}
		if (jmxPerm.getType() != null
			&& jmxPerm.getType().trim().equals(EMPTY)) {
			jmxPerm.setType(null);
		}
		if (jmxPerm.getAction() != null
			&& jmxPerm.getAction().trim().equals(EMPTY)) {
			jmxPerm.setAction(null);
		}
		if (jmxPerm.getAttribute() != null
			&& jmxPerm.getAttribute().trim().equals(EMPTY)) {
			jmxPerm.setAttribute(null);
		}
		if (jmxPerm.getName() != null
			&& jmxPerm.getName().trim().equals(EMPTY)) {
			jmxPerm.setName(null);
		}
		if (jmxPerm.getUniqueId() != null
			&& jmxPerm.getUniqueId().trim().equals(EMPTY)) {
			jmxPerm.setUniqueId(null);
		}
		if (jmxPerm.getClassName().equals(UMEPermission.class.getName())) {
			String privateType = getPrivatePrincipalTypeIdentifier(jmxPerm.getType());
			return new UMEPermission(
				privateType,
				jmxPerm.getAction(),
				jmxPerm.getAttribute(),
				jmxPerm.getUniqueId(),
				myCompanyId);
		}
		if (jmxPerm
			.getClassName()
			.equals(UMAdminPermissions.class.getName())) {
			return new UMAdminPermissions(
				jmxPerm.getName(),
				jmxPerm.getAction());
		}
		if (jmxPerm
			.getClassName()
			.equals(UMEConfigurationPermission.class.getName())) {
			return new UMEConfigurationPermission(
				jmxPerm.getName(),
				jmxPerm.getAction());
		}
		return null;
	}
		finally
		{
			if (mTxSupport) InternalUMFactory.setHookInRunningTransaction(false);
		}
	}

	/*
	 * PCD Rolleneditierung --> populated attribute auf acl namespace check
	 * mitgeben, wenn Rolle zum editieren geholt wird: namespace:
	 * com.sap.security.acl.principal, attribut name: user unique id --> Role
	 * assignment permission (acl auf rolle)
	 * Role was already available before method call, so no internal
	 * Permission check required
	 */
	private static String checkAclForRole(String uniqueId, boolean readonlyAccess)
		throws UMException, PrincipalNotAccessibleException {
		final String mn =
			"public IRole checkAclForRole(IRole role, String companyId)";
		String datasource = DataSourceBaseImplementation
				.staticSplitPrincipalDatabagID(uniqueId)[1];
		IRole role = UMFactory.getRoleFactory().getRole(uniqueId);
		String [] isRemoteRole = role.getAttribute(Role.DEFAULT_NAMESPACE, Role.IS_REMOTE_PCD_ROLE);
		if (datasource.equals(PCD_ROLE_PERSISTENCE)
			|| (isRemoteRole != null
				&& isRemoteRole.length > 0
				&& "true".equalsIgnoreCase(isRemoteRole[0]))) {
			if (myLoc.beInfo()) {
				myLoc.infoT(
					mn,
					"Role {0} is a PCD or Remote Role",
					new Object[] { role.getUniqueID()});
			}
			UMEEntityAccessPermission perm;
			if (readonlyAccess){
				perm =
					new UMEEntityAccessPermission(
						UMEEntityAccessPermission.NAME_ROLE_PCD,
						UMEEntityAccessPermission.ACTION_VIEW);
			} else {
				perm =
					new UMEEntityAccessPermission(
						UMEEntityAccessPermission.NAME_ROLE_PCD,
						UMEEntityAccessPermission.ACTION_ASSIGN);	
			}
			if (!InternalUMFactory.loggedInUserHasPermission(perm)){
				IUser user = UMFactory.getAuthenticator().getLoggedInUser();
				if (myLoc.beInfo()) {
					myLoc.infoT(
						mn,
						"Logged in user {0} has not permission {1} in role {2}, performing ACL check",
						new Object[] { user, perm, uniqueId});
				}
				/* Checking if role is accessible with
				 * getting the role from RoleFactory with special attribute list
				 * if not accessible, Access Exception will be thrown
				 */
				AttributeList al = new AttributeList();
				al.addAttribute(NAMESPACE_ACL, user.getUniqueID());					
				if (myLoc.beInfo()) {
					myLoc.infoT(
						mn,
						"Attribute list {0}",
						new Object[] { al });
				}
				UMFactory.getRoleFactory().getRole(uniqueId, al);
				if (myLoc.beInfo()) {
					myLoc.infoT(
						mn,
						"ACL check successfull");
				}
				return uniqueId;
			} else {
				if (myLoc.beInfo()) {
					IUser user = UMFactory.getAuthenticator().getLoggedInUser();
					myLoc.infoT(
						mn,
						"Logged in user {0} has permission {1} in role {2}, skip ACL check, returning role.",
						new Object[] { user, perm, uniqueId});
				}
			   return uniqueId;
		   }
		} else {
			if (myLoc.beInfo()) {
				myLoc.infoT(
					mn,
					"Role {0} is a UME Role",
					new Object[] { role.getUniqueID()});
			}
			UMEEntityAccessPermission perm;
			if (readonlyAccess){
				perm =
					new UMEEntityAccessPermission(
						UMEEntityAccessPermission.NAME_ROLE_UME,
						UMEEntityAccessPermission.ACTION_VIEW);
			} else {
				perm =
					new UMEEntityAccessPermission(
						UMEEntityAccessPermission.NAME_ROLE_UME,
						UMEEntityAccessPermission.ACTION_ASSIGN);
			}
			if (!InternalUMFactory.loggedInUserHasPermission(perm)){
				if (myLoc.beInfo()) {
					IUser user = UMFactory.getAuthenticator().getLoggedInUser();
					myLoc.infoT(
						mn,
						"Logged in user {0} has not permission {1} in role {2}, throw PrincipalNotAccessibleException",
						new Object[] { user, perm, uniqueId});
				}
				throw new PrincipalNotAccessibleException(
					"Role "
						+ uniqueId
						+ " is not assignable for logged in user.");					
			} else {
				if (myLoc.beInfo()) {
					IUser user = UMFactory.getAuthenticator().getLoggedInUser();
					myLoc.infoT(
						mn,
						"Logged in user {0} has permission {1} in role {2}, returning role.",
						new Object[] { user, perm, uniqueId});
				}
				return uniqueId;
			}
		}
	}

	/**
	 * Checks for IPrincipal.DEFAULT_RELATION_NAMESPACE, IPrincipal.PRINCIPAL_RELATION_PARENT_ATTRIBUTE,
	 * but not on the persistence, only on UMEPermissions
	 * Member check does not contain check on roles, But correct check also for Actions
	 */
	public boolean isPrincipalMemberAssignable(String uniqueId) throws UMException {
		try
		{
			if (mTxSupport) InternalUMFactory.setHookInRunningTransaction(mTxSupport);
			
		final String mn =
			"public boolean isPrincipalAssignable(String uniqueId)";
		if (myLoc.beInfo()) {
			myLoc.infoT(
				mn,
				"uniqueId {0}, relationAttribute {1}, active company concept {2}, company context {3}",
				new Object[] {
					uniqueId,
					IPrincipal.DEFAULT_RELATION_NAMESPACE + DOT + IPrincipal.PRINCIPAL_RELATION_PARENT_ATTRIBUTE,
					ACTIVE_COMPANY_ATRRIBUTE_NAME,
					myCompanyId });
		}
		boolean result = true;
		String privateType =
			getPrivatePrincipalTypeIdentifier(
				getPrincipalType(uniqueId));
		if (result) {
			UMEPermission perm =
				new UMEPermission(
					privateType,
					UMEPermission.ACTION_EDIT,
					IPrincipal.DEFAULT_RELATION_NAMESPACE + DOT + IPrincipal.PRINCIPAL_RELATION_PARENT_ATTRIBUTE,
					uniqueId,
					myCompanyId);
			result = InternalUMFactory.loggedInUserHasPermission(perm);
			if (myLoc.beDebug()) {
				if (result) {
					myLoc.debugT(
						mn,
						"Logged in user {0} has permission {1}, principal is assignable",
						new Object[] {
							UMFactory.getAuthenticator().getLoggedInUser(),
							perm });
				} else {
					myLoc.debugT(
						mn,
						"Logged in user {0} has not permission {1}, principal is not assignable",
						new Object[] {
							UMFactory.getAuthenticator().getLoggedInUser(),
							perm });
				}
			}
		}
		return result;
	}
		finally
		{
			if (mTxSupport) InternalUMFactory.setHookInRunningTransaction(false);
		}
	}

	/**
	 * Checks for IPrincipal.DEFAULT_RELATION_NAMESPACE, IPrincipal.PRINCIPAL_RELATION_MEMBER_ATTRIBUTE
	 * Parent check does not contain check on actions
	 */
	public boolean isPrincipalParentAssignable(String uniqueId) throws UMException {
		try
		{
			if (mTxSupport) InternalUMFactory.setHookInRunningTransaction(mTxSupport);
			
		final String mn =
			"public boolean isPrincipalAssignable(String uniqueId)";
		if (myLoc.beInfo()) {
			myLoc.infoT(
				mn,
				"uniqueId {0}, relationAttribute {1}, active company concept {2}, company context {3}",
				new Object[] {
					uniqueId,
					IPrincipal.DEFAULT_RELATION_NAMESPACE + DOT + IPrincipal.PRINCIPAL_RELATION_MEMBER_ATTRIBUTE,
					ACTIVE_COMPANY_ATRRIBUTE_NAME,
					myCompanyId });
		}
		boolean result;
		result =
			UMFactory.getPrincipalFactory().isPrincipalAttributeModifiable(uniqueId, IPrincipal.DEFAULT_RELATION_NAMESPACE, IPrincipal.PRINCIPAL_RELATION_MEMBER_ATTRIBUTE);
		if (myLoc.beDebug()) {
			if (result) {
				myLoc.debugT(mn, "principal is modifyable");
			} else {
				myLoc.debugT(mn, "principal is not modifyable");
			}
		}
		String privateType =
			getPrivatePrincipalTypeIdentifier(
				getPrincipalType(uniqueId));
		if (result) {
			UMEPermission perm =
				new UMEPermission(
					privateType,
					UMEPermission.ACTION_EDIT,
					IPrincipal.DEFAULT_RELATION_NAMESPACE + DOT + IPrincipal.PRINCIPAL_RELATION_MEMBER_ATTRIBUTE,
					uniqueId,
					myCompanyId);
			result = InternalUMFactory.loggedInUserHasPermission(perm);
			if (myLoc.beDebug()) {
				if (result) {
					myLoc.debugT(
						mn,
						"Logged in user {0} has permission {1}, principal is assignable",
						new Object[] {
							UMFactory.getAuthenticator().getLoggedInUser(),
							perm });
				} else {
					myLoc.debugT(
						mn,
						"Logged in user {0} has not permission {1}, principal is not assignable",
						new Object[] {
							UMFactory.getAuthenticator().getLoggedInUser(),
							perm });
				}
			}
		}
		if (result && IPrincipalDatabag.ROLE_TYPE.equals(privateType)) {
			try {
				checkAclForRole(uniqueId, false);
			} catch (PrincipalNotAccessibleException e) {
				result = false;
			}
			if (myLoc.beDebug()) {
				if (result) {
					myLoc.debugT(
						mn,
						"ACL check was successful, principal is assignable");
				} else {
					myLoc.debugT(
						mn,
						"ACL check was not successful, principal is not assignable");
				}
			}
		}
		return result;
	}
		finally
		{
			if (mTxSupport) InternalUMFactory.setHookInRunningTransaction(false);
		}
	}
	
	public IPrincipalMaint getMutablePrincipal(String uniqueId, String namespace, String name)
		throws UMException {
		try
		{
			if (mTxSupport) InternalUMFactory.setHookInRunningTransaction(mTxSupport);
			
		final String mn =
			"public IPrincipalMaint getMutablePrincipal(String uniqueId)";
		if (myLoc.beInfo()) {
			myLoc.infoT(
				mn,
				"uniqueId {0}, active company concept {1}, company context {2}",
				new Object[] {
					uniqueId,
					ACTIVE_COMPANY_ATRRIBUTE_NAME,
					myCompanyId });
		}
		String privateType =
			getPrivatePrincipalTypeIdentifier(
				getPrincipalType(uniqueId));
		UMEPermission perm =
			new UMEPermission(
				privateType,
				UMEPermission.ACTION_EDIT,
				namespace + DOT + name,
				uniqueId,
				myCompanyId);
		InternalUMFactory.checkPermissionOfLoggedInUser(perm);
		if (myLoc.beDebug()) {
			myLoc.debugT(
				mn,
				"Logged in user {0} has permission {1}, proceeding",
				new Object[] {
					UMFactory.getAuthenticator().getLoggedInUser(),
					perm });
		}
		return UMFactory.getPrincipalFactory().getMutablePrincipal(uniqueId);
	}
		finally
		{
			if (mTxSupport) InternalUMFactory.setHookInRunningTransaction(false);
		}
	}
	
	/*
	 * Methods from other factories
	 */

	public void commitUser(IUserMaint userMaint, IUserAccount accountMaint)
		throws UMException {
		try
		{
			if (mTxSupport) InternalUMFactory.setHookInRunningTransaction(mTxSupport);
			
		final String mn =
			"public void commitUser(IUserMaint userMaint, IUserAccount accountMaint)";
		if (myLoc.beInfo()) {
			myLoc.infoT(
				mn,
				"UserMaint {0}, accountMaint {1}, active company concept {2}, company context {3}",
				new Object[] {
					userMaint,
					accountMaint,
					ACTIVE_COMPANY_ATRRIBUTE_NAME,
					myCompanyId });
		}
		String action = UMEPermission.ACTION_CREATE;
		String uniqueId = userMaint.getUniqueID();
		if (uniqueId != null) {
			action = UMEPermission.ACTION_EDIT;
		}
		UMEPermission perm =
			new UMEPermission(
				IPrincipalDatabag.USER_TYPE,
				action,
				UMEPermission.COM_SAP_SECURITY_CORE_USERMANAGEMENT_UNIQUEID,
				uniqueId,
				myCompanyId);
		InternalUMFactory.checkPermissionOfLoggedInUser(perm);
		if (myLoc.beDebug()) {
			myLoc.debugT(
				mn,
				"Logged in user {0} has permission {1}, proceeding",
				new Object[] {
					UMFactory.getAuthenticator().getLoggedInUser(),
					perm });
		}
		UMFactory.getUserFactory().commitUser(userMaint, accountMaint);
	}
		finally
		{
			if (mTxSupport) InternalUMFactory.setHookInRunningTransaction(false);
		}
	}

	public ISearchResult searchUsers(
		IUserSearchFilter userSearchFilter,
		IUserAccountSearchFilter userAccountSearchFilter)
		throws UMException {
		try
		{
			if (mTxSupport) InternalUMFactory.setHookInRunningTransaction(mTxSupport);
			
		final String mn =
			"searchUsers(IUserSearchFilter userSearchFilter, IUserAccountSearchFilter userAccountSearchFilter)";
		if (myLoc.beInfo()) {
			myLoc.infoT(
				mn,
				"userSearchFilter {0}, userAccountSearchFilter {1}, active company concept {2}, company context {3}",
				new Object[] {
					userSearchFilter,
					userAccountSearchFilter,
					ACTIVE_COMPANY_ATRRIBUTE_NAME,
					myCompanyId });
		}
		UMEPermission perm =
			new UMEPermission(
				IPrincipalDatabag.USER_TYPE,
				UMEPermission.ACTION_READ,
				UMEPermission.COM_SAP_SECURITY_CORE_USERMANAGEMENT_UNIQUEID,
				null,
				myCompanyId);
		InternalUMFactory.checkPermissionOfLoggedInUser(perm);
		if (myLoc.beDebug()) {
			myLoc.debugT(
				mn,
				"Logged in user {0} has permission {1}, proceeding",
				new Object[] {
					UMFactory.getAuthenticator().getLoggedInUser(),
					perm });
		}
		switch (myUsedCompanyConcept) {
			case CC_BPO :
				if (null != myCompanyId) {
					userSearchFilter.setSearchAttribute(
						IPrincipal.DEFAULT_NAMESPACE,
						ATTR_BPO,
						myCompanyId,
						ISearchAttribute.EQUALS_OPERATOR,
						false);
					userAccountSearchFilter.setSearchAttribute(
						IPrincipal.DEFAULT_NAMESPACE,
						ATTR_BPO,
						myCompanyId,
						ISearchAttribute.EQUALS_OPERATOR,
						false);
				}
				break;
			case CC_TPD :
				if (null != myCompanyId) {
					userSearchFilter.setSearchAttribute(
						IPrincipal.DEFAULT_NAMESPACE,
						ATTR_TPD,
						myCompanyId,
						ISearchAttribute.EQUALS_OPERATOR,
						false);
				}
				break;
			default :
				//nothing todo if company concept is disabled
				break;
		}
		if (myLoc.beDebug()) {
			myLoc.debugT(
				mn,
				"userSearchFilter {0}, userAccountSearchFilter {1}",
				new Object[] { userSearchFilter, userAccountSearchFilter });
		}
		return UMFactory.getUserFactory().searchUsers(
			userSearchFilter,
			userAccountSearchFilter);
	}
		finally
		{
			if (mTxSupport) InternalUMFactory.setHookInRunningTransaction(false);
		}
	}

	public ISearchResult searchUnapprovedUsers(
		IUserSearchFilter userSearchFilter,
		IUserAccountSearchFilter userAccountSearchFilter)
		throws UMException {
		try
		{
			if (mTxSupport) InternalUMFactory.setHookInRunningTransaction(mTxSupport);
			
		final String mn =
			"public ISearchResult searchUnapprovedUsers(IUserSearchFilter userSearchFilter, IUserAccountSearchFilter userAccountSearchFilter)";
		if (myLoc.beInfo()) {
			myLoc.infoT(
				mn,
				"userSearchFilter {0}, accountMaint {1}, active company concept {2}, company context {3}",
				new Object[] {
					userSearchFilter,
					userAccountSearchFilter,
					ACTIVE_COMPANY_ATRRIBUTE_NAME,
					myCompanyId });
		}
		UMEPermission perm =
			new UMEPermission(
				IPrincipalDatabag.USER_TYPE,
				UMEPermission.ACTION_READ,
				UMEPermission.COM_SAP_SECURITY_CORE_USERMANAGEMENT_UNIQUEID,
				null,
				myCompanyId);
		InternalUMFactory.checkPermissionOfLoggedInUser(perm);
		if (myLoc.beDebug()) {
			myLoc.debugT(
				mn,
				"Logged in user {0} has permission {1}, proceeding",
				new Object[] {
					UMFactory.getAuthenticator().getLoggedInUser(),
					perm });
		}
		switch (myUsedCompanyConcept) {
			case CC_BPO :
				//no unapproved users in BPO
				break;
			case CC_TPD :
				if (null != myCompanyId) {
					userSearchFilter.setSearchAttribute(
						IPrincipal.DEFAULT_NAMESPACE,
						APPROVAL_REQUEST_COMPANYID,
						myCompanyId,
						ISearchAttribute.EQUALS_OPERATOR,
						false);
				} else {
					userSearchFilter.setSearchAttribute(
						IPrincipal.DEFAULT_NAMESPACE,
						APPROVAL_REQUEST_COMPANYID,
						WILDCARD,
						ISearchAttribute.LIKE_OPERATOR,
						false);
				}
				break;
			default :
				//nothing todo if company concept is disabled
				break;
		}
		if (myLoc.beDebug()) {
			myLoc.debugT(
				mn,
				"userSearchFilter {0}, userAccountSearchFilter {1}",
				new Object[] { userSearchFilter, userAccountSearchFilter });
		}
		return UMFactory.getUserFactory().searchUsers(
			userSearchFilter,
			userAccountSearchFilter);
	}
		finally
		{
			if (mTxSupport) InternalUMFactory.setHookInRunningTransaction(false);
		}
	}

	public IUserMaint newUser(String uniqueName) throws UMException {
		try
		{
			if (mTxSupport) InternalUMFactory.setHookInRunningTransaction(mTxSupport);
			
		final String mn = "public IUserMaint newUser(String uniqueName)";
		if (myLoc.beInfo()) {
			myLoc.infoT(
				mn,
				"uniqueName {0}, active company concept {1}, company context {2}",
				new Object[] {
					uniqueName,
					ACTIVE_COMPANY_ATRRIBUTE_NAME,
					myCompanyId });
		}
		UMEPermission perm =
			new UMEPermission(
				IPrincipalDatabag.USER_TYPE,
				UMEPermission.ACTION_CREATE,
				UMEPermission.COM_SAP_SECURITY_CORE_USERMANAGEMENT_UNIQUEID,
				null,
				myCompanyId);
		InternalUMFactory.checkPermissionOfLoggedInUser(perm);
		if (myLoc.beDebug()) {
			myLoc.debugT(
				mn,
				"Logged in user {0} has permission {1}, proceeding",
				new Object[] {
					UMFactory.getAuthenticator().getLoggedInUser(),
					perm });
		}
		IUserMaint user = UMFactory.getUserFactory().newUser(uniqueName);
		switch (myUsedCompanyConcept) {
			case CC_BPO :
				if (null != myCompanyId && !COMPANIES_ALL.equals(myCompanyId)) {
					user.setTransientAttribute(
						IPrincipal.DEFAULT_NAMESPACE,
						ATTR_BPO,
						myCompanyId);
				}
				break;
			case CC_TPD :
				if (null != myCompanyId) {
					user.setAttribute(
						IPrincipal.DEFAULT_NAMESPACE,
						ATTR_TPD,
						new String[] { myCompanyId });
				} else {
					user.setAttribute(
						IPrincipal.DEFAULT_NAMESPACE,
						ATTR_TPD,
						new String[] { EMPTY });
				}
				break;
			default :
				//nothing todo if company concept is disabled
				break;
		}
		if (myLoc.beDebug()) {
			myLoc.debugT(mn, "user {0}", new Object[] { user });
		}
		return user;
	}
		finally
		{
			if (mTxSupport) InternalUMFactory.setHookInRunningTransaction(false);
		}
	}

	public IUserMaint newServiceUser(String uniqueName) throws UMException {
		try
		{
			if (mTxSupport) InternalUMFactory.setHookInRunningTransaction(mTxSupport);
			
		final String mn = "public IUserMaint newServiceUser(String uniqueName)";
		if (myLoc.beInfo()) {
			myLoc.infoT(
				mn,
				"uniqueName {0}, active company concept {1}, company context {2}",
				new Object[] {
					uniqueName,
					ACTIVE_COMPANY_ATRRIBUTE_NAME,
					myCompanyId });
		}
		UMEPermission perm =
			new UMEPermission(
				IPrincipalDatabag.USER_TYPE,
				UMEPermission.ACTION_CREATE,
				UMEPermission.COM_SAP_SECURITY_CORE_USERMANAGEMENT_UNIQUEID,
				null,
				myCompanyId);
		InternalUMFactory.checkPermissionOfLoggedInUser(perm);
		if (myLoc.beDebug()) {
			myLoc.debugT(
				mn,
				"Logged in user {0} has permission {1}, proceeding",
				new Object[] {
					UMFactory.getAuthenticator().getLoggedInUser(),
					perm });
		}
		perm =
			new UMEPermission(
				IPrincipalDatabag.USER_TYPE,
				UMEPermission.ACTION_CREATE,
				UMEPermission.$SERVICEUSER$_SERVICEUSER_ATTRIBUTE,
				null,
				myCompanyId);
		InternalUMFactory.checkPermissionOfLoggedInUser(perm);
		if (myLoc.beDebug()) {
			myLoc.debugT(
				mn,
				"Logged in user {0} has permission {1}, proceeding",
				new Object[] {
					UMFactory.getAuthenticator().getLoggedInUser(),
					perm });
		}
		UMFactory.getServiceUserFactory().createServiceUser(uniqueName);
		String uniqueId = UMFactory.getServiceUserFactory().getServiceUser(uniqueName).getUniqueID();
		IUserMaint user = UMFactory.getUserFactory().getMutableUser(uniqueId);
		//no company concept needed, because service users are always global
		if (myLoc.beDebug()) {
			myLoc.debugT(mn, "user {0}", new Object[] { user });
		}
		return user;
	}
		finally
		{
			if (mTxSupport) InternalUMFactory.setHookInRunningTransaction(false);
		}
	}

	public IGroup newGroup(String uniqueName) throws UMException {
		try
		{
			if (mTxSupport) InternalUMFactory.setHookInRunningTransaction(mTxSupport);
			
		final String mn = "public IGroup newGroup(String uniqueName)";
		if (myLoc.beInfo()) {
			myLoc.infoT(
				mn,
				"uniqueName {0}, active company concept {1}, company context {2}",
				new Object[] {
					uniqueName,
					ACTIVE_COMPANY_ATRRIBUTE_NAME,
					myCompanyId });
		}
		UMEPermission perm =
			new UMEPermission(
				IPrincipalDatabag.GROUP_TYPE,
				UMEPermission.ACTION_CREATE,
				UMEPermission.COM_SAP_SECURITY_CORE_USERMANAGEMENT_UNIQUEID,
				null,
				myCompanyId);
		InternalUMFactory.checkPermissionOfLoggedInUser(perm);
		if (myLoc.beDebug()) {
			myLoc.debugT(
				mn,
				"Logged in user {0} has permission {1}, proceeding",
				new Object[] {
					UMFactory.getAuthenticator().getLoggedInUser(),
					perm });
		}
		IGroup group = UMFactory.getGroupFactory().newGroup(uniqueName);
		switch (myUsedCompanyConcept) {
			case CC_BPO :
				if (null != myCompanyId && !COMPANIES_ALL.equals(myCompanyId)) {
					((AbstractPrincipal) group).setTransientAttribute(
						IPrincipal.DEFAULT_NAMESPACE,
						ATTR_BPO,
						myCompanyId);
				}
				break;
			case CC_TPD :
				//not setting any attribute if TPD
				break;
			default :
				//nothing todo if company concept is disabled
				break;
		}
		if (myLoc.beDebug()) {
			myLoc.debugT(mn, "group {0}", new Object[] { group });
		}
		return group;
	}
		finally
		{
			if (mTxSupport) InternalUMFactory.setHookInRunningTransaction(false);
		}
	}

	public IRole newRole(String uniqueName) throws UMException {
		try
		{
			if (mTxSupport) InternalUMFactory.setHookInRunningTransaction(mTxSupport);
			
		final String mn = "public IRole newRole(String uniqueName)";
		if (myLoc.beInfo()) {
			myLoc.infoT(
				mn,
				"uniqueName {0}, active company concept {1}, company context {2}",
				new Object[] {
					uniqueName,
					ACTIVE_COMPANY_ATRRIBUTE_NAME,
					myCompanyId });
		}
		UMEPermission perm =
			new UMEPermission(
				IPrincipalDatabag.ROLE_TYPE,
				UMEPermission.ACTION_CREATE,
				UMEPermission.COM_SAP_SECURITY_CORE_USERMANAGEMENT_UNIQUEID,
				null,
				myCompanyId);
		InternalUMFactory.checkPermissionOfLoggedInUser(perm);
		if (myLoc.beDebug()) {
			myLoc.debugT(
				mn,
				"Logged in user {0} has permission {1}, proceeding",
				new Object[] {
					UMFactory.getAuthenticator().getLoggedInUser(),
					perm });
		}
		IRole role = UMFactory.getRoleFactory().newRole(uniqueName);
		switch (myUsedCompanyConcept) {
			case CC_BPO :
				if (null != myCompanyId && !COMPANIES_ALL.equals(myCompanyId)) {
					((AbstractPrincipal) role).setTransientAttribute(
						IPrincipal.DEFAULT_NAMESPACE,
						ATTR_BPO,
						myCompanyId);
				}
				break;
			case CC_TPD :
				//not setting any attribute if TPD
				break;
			default :
				//nothing todo if company concept is disabled
				break;
		}
		if (myLoc.beDebug()) {
			myLoc.debugT(mn, "role {0}", new Object[] { role });
		}
		return role;
	}
		finally
		{
			if (mTxSupport) InternalUMFactory.setHookInRunningTransaction(false);
		}
	}

	public IUserAccount newUserAccount(String uniqueName) throws UMException {
		try
		{
			if (mTxSupport) InternalUMFactory.setHookInRunningTransaction(mTxSupport);
			
		final String mn = "public IUserAccount newUserAccount(String uniqueId)";
		if (myLoc.beInfo()) {
			myLoc.infoT(
				mn,
				"uniqueName {0}, active company concept {1}, company context {2}",
				new Object[] {
					uniqueName,
					ACTIVE_COMPANY_ATRRIBUTE_NAME,
					myCompanyId });
		}
		UMEPermission perm =
			new UMEPermission(
				IPrincipalDatabag.USER_TYPE,
				UMEPermission.ACTION_CREATE,
				UMEPermission.COM_SAP_SECURITY_CORE_USERMANAGEMENT_UNIQUEID,
				null,
				myCompanyId);
		InternalUMFactory.checkPermissionOfLoggedInUser(perm);
		if (myLoc.beDebug()) {
			myLoc.debugT(
				mn,
				"Logged in user {0} has permission {1}, proceeding",
				new Object[] {
					UMFactory.getAuthenticator().getLoggedInUser(),
					perm });
		}
		IUserAccount account =
			UMFactory.getUserAccountFactory().newUserAccount(uniqueName);
		switch (myUsedCompanyConcept) {
			case CC_BPO :
				if (null != myCompanyId && !COMPANIES_ALL.equals(myCompanyId)) {
					((AbstractPrincipal) account).setTransientAttribute(
						IPrincipal.DEFAULT_NAMESPACE,
						ATTR_BPO,
						myCompanyId);
				}
				break;
			case CC_TPD :
				//not setting any attribute if TPD
				break;
			default :
				//nothing todo if company concept is disabled
				break;
		}
		if (myLoc.beDebug()) {
			myLoc.debugT(mn, "account {0}", new Object[] { account });
		}
		return account;
	}
		finally
		{
			if (mTxSupport) InternalUMFactory.setHookInRunningTransaction(false);
		}
	}

	public void deleteUser(String uniqueId) throws UMException {
		try
		{
			if (mTxSupport) InternalUMFactory.setHookInRunningTransaction(mTxSupport);
			
		final String mn = "public void deleteUser(String uniqueId)";
		if (myLoc.beInfo()) {
			myLoc.infoT(
				mn,
				"uniqueId {0}, active company concept {1}, company context {2}",
				new Object[] {
					uniqueId,
					ACTIVE_COMPANY_ATRRIBUTE_NAME,
					myCompanyId });
		}
		String loggedInUserId = UMFactory.getAuthenticator().getLoggedInUser().getUniqueID();
		if (loggedInUserId.equals(uniqueId)){
			if (myLoc.beInfo()){
				myLoc.infoT(
					mn,
					"User to be deleted {0} equals logged in user {1}; " + 
					"logged in user is not able to delete him-/herself",
					new Object[]{uniqueId, loggedInUserId});
			}
			return;
		}
		UMEPermission perm =
			new UMEPermission(
				IPrincipalDatabag.USER_TYPE,
				UMEPermission.ACTION_DELETE,
				UMEPermission.COM_SAP_SECURITY_CORE_USERMANAGEMENT_UNIQUEID,
				uniqueId,
				myCompanyId,
				loggedInUserId);
		InternalUMFactory.checkPermissionOfLoggedInUser(perm);
		if (myLoc.beDebug()) {
			myLoc.debugT(
				mn,
				"Logged in user {0} has permission {1}, proceeding",
				new Object[] {
					UMFactory.getAuthenticator().getLoggedInUser(),
					perm });
		}
		UMFactory.getUserFactory().deleteUser(uniqueId);
	}
		finally
		{
			if (mTxSupport) InternalUMFactory.setHookInRunningTransaction(false);
		}
	}

	public void deleteRole(String uniqueId) throws UMException {
		try
		{
			if (mTxSupport) InternalUMFactory.setHookInRunningTransaction(mTxSupport);
			
		final String mn = "public void deleteRole(String uniqueId)";
		if (myLoc.beInfo()) {
			myLoc.infoT(
				mn,
				"uniqueId {0}, active company concept {1}, company context {2}",
				new Object[] {
					uniqueId,
					ACTIVE_COMPANY_ATRRIBUTE_NAME,
					myCompanyId });
		}
		UMEPermission perm =
			new UMEPermission(
				IPrincipalDatabag.ROLE_TYPE,
				UMEPermission.ACTION_DELETE,
				UMEPermission.COM_SAP_SECURITY_CORE_USERMANAGEMENT_UNIQUEID,
				uniqueId,
				myCompanyId);
		InternalUMFactory.checkPermissionOfLoggedInUser(perm);
		if (myLoc.beDebug()) {
			myLoc.debugT(
				mn,
				"Logged in user {0} has permission {1}, proceeding",
				new Object[] {
					UMFactory.getAuthenticator().getLoggedInUser(),
					perm });
		}
		UMFactory.getRoleFactory().deleteRole(uniqueId);
	}
		finally
		{
			if (mTxSupport) InternalUMFactory.setHookInRunningTransaction(false);
		}
	}

	public void deleteGroup(String uniqueId) throws UMException {
		final String mn = "public void deleteGroup(String uniqueId)";
		try
		{
			if (mTxSupport) InternalUMFactory.setHookInRunningTransaction(mTxSupport);
			
		if (myLoc.beInfo()) {
			myLoc.infoT(
				mn,
				"uniqueId {0}, active company concept {1}, company context {2}",
				new Object[] {
					uniqueId,
					ACTIVE_COMPANY_ATRRIBUTE_NAME,
					myCompanyId });
		}
		UMEPermission perm =
			new UMEPermission(
				IPrincipalDatabag.GROUP_TYPE,
				UMEPermission.ACTION_DELETE,
				UMEPermission.COM_SAP_SECURITY_CORE_USERMANAGEMENT_UNIQUEID,
				uniqueId,
				myCompanyId);
		InternalUMFactory.checkPermissionOfLoggedInUser(perm);
		if (myLoc.beDebug()) {
			myLoc.debugT(
				mn,
				"Logged in user {0} has permission {1}, proceeding",
				new Object[] {
					UMFactory.getAuthenticator().getLoggedInUser(),
					perm });
		}
		UMFactory.getGroupFactory().deleteGroup(uniqueId);
	}
		finally
		{
			if (mTxSupport) InternalUMFactory.setHookInRunningTransaction(false);
		}
	}

	public void cleanupPrincipalDatabag(String uniqueId)
		throws PersistenceException, UMException {
		try
		{
			if (mTxSupport) InternalUMFactory.setHookInRunningTransaction(mTxSupport);
			
		final String mn =
			"public void cleanupPrincipalDatabag(String uniqueId)";
		if (myLoc.beInfo()) {
			myLoc.infoT(
				mn,
				"uniqueId {0}, active company concept {1}, company context {2}",
				new Object[] {
					uniqueId,
					ACTIVE_COMPANY_ATRRIBUTE_NAME,
					myCompanyId });
		}
		String privateType =
			getPrivatePrincipalTypeIdentifier(
				getPrincipalType(uniqueId));
		String loggedInUserId = UMFactory.getAuthenticator().getLoggedInUser().getUniqueID();
		if (loggedInUserId.equals(uniqueId)){
			if (myLoc.beInfo()){
				myLoc.infoT(
					mn,
					"User to be deleted {0} equals logged in user {1}; " +
					"logged in user is not able to cleanup him-/herself",
					new Object[]{uniqueId, loggedInUserId});
			}
			return;
		}
		UMEPermission perm =
			new UMEPermission(
				privateType,
				UMEPermission.ACTION_DELETE,
				UMEPermission.COM_SAP_SECURITY_CORE_USERMANAGEMENT_UNIQUEID,
				uniqueId,
				myCompanyId,
				loggedInUserId);
		InternalUMFactory.checkPermissionOfLoggedInUser(perm);
		if (myLoc.beDebug()) {
			myLoc.debugT(
				mn,
				"Logged in user {0} has permission {1}, proceeding",
				new Object[] {
					UMFactory.getAuthenticator().getLoggedInUser(),
					perm });
		}
		PrincipalDatabagFactory.getInstance().cleanupPrincipalDatabag(uniqueId);
	}
		finally
		{
			if (mTxSupport) InternalUMFactory.setHookInRunningTransaction(false);
		}
	}

	public IUserAccount getMutableUserAccount(String uniqueId)
		throws UMException {
		try
		{
			if (mTxSupport) InternalUMFactory.setHookInRunningTransaction(mTxSupport);
			
		final String mn =
			"public IUserAccount getMutableUserAccount(String uniqueId)";
		if (myLoc.beInfo()) {
			myLoc.infoT(
				mn,
				"uniqueId {0}, active company concept {1}, company context {2}",
				new Object[] {
					uniqueId,
					ACTIVE_COMPANY_ATRRIBUTE_NAME,
					myCompanyId });
		}
		UMEPermission perm =
			new UMEPermission(
				IPrincipalDatabag.USER_TYPE,
				UMEPermission.ACTION_EDIT,
				UMEPermission.COM_SAP_SECURITY_CORE_USERMANAGEMENT_UNIQUEID,
				uniqueId,
				myCompanyId);
		InternalUMFactory.checkPermissionOfLoggedInUser(perm);
		if (myLoc.beDebug()) {
			myLoc.debugT(
				mn,
				"Logged in user {0} has permission {1}, proceeding",
				new Object[] {
					UMFactory.getAuthenticator().getLoggedInUser(),
					perm });
		}
		return UMFactory.getUserAccountFactory().getMutableUserAccount(
			uniqueId);
	}
		finally
		{
			if (mTxSupport) InternalUMFactory.setHookInRunningTransaction(false);
		}
	}

	public IRole getRole(String uniqueId, AttributeList attributeList)
		throws UMException {
		try
		{
			if (mTxSupport) InternalUMFactory.setHookInRunningTransaction(mTxSupport);
			
		final String mn =
			"public IRole getRole(String uniqueId, AttributeList attributeList)";
		if (myLoc.beInfo()) {
			myLoc.infoT(
				mn,
				"uniqueId {0}, attributeList {1}, active company concept {2}, company context {3}",
				new Object[] {
					uniqueId,
					attributeList,
					ACTIVE_COMPANY_ATRRIBUTE_NAME,
					myCompanyId });
		}
		String action = UMEPermission.ACTION_READ;
		UMEPermission perm =
			new UMEPermission(
				IPrincipalDatabag.ROLE_TYPE,
				action,
				UMEPermission.COM_SAP_SECURITY_CORE_USERMANAGEMENT_UNIQUEID,
				uniqueId,
				myCompanyId);
		InternalUMFactory.checkPermissionOfLoggedInUser(perm);
		if (myLoc.beDebug()) {
			myLoc.debugT(
				mn,
				"Logged in user {0} has permission {1}, proceeding",
				new Object[] {
					UMFactory.getAuthenticator().getLoggedInUser(),
					perm });
		}
		return UMFactory.getRoleFactory().getRole(uniqueId, attributeList);
	}
		finally
		{
			if (mTxSupport) InternalUMFactory.setHookInRunningTransaction(false);
		}
	}

	/*
	 * Methods from IPrincipalFactory
	 */

	public String getPrincipalType(String uniqueId) throws UMException {
		try
		{
			if (mTxSupport) InternalUMFactory.setHookInRunningTransaction(mTxSupport);
        
		if (JmxActionFactoryWrapper.isAction(uniqueId)) {
       		return JmxActionFactoryWrapper.IACTION;
        }
		return UMFactory.getPrincipalFactory().getPrincipalType(uniqueId);
	}
		finally
		{
			if (mTxSupport) InternalUMFactory.setHookInRunningTransaction(false);
		}
	}

	public IPrincipal getPrincipal(String uniqueId) throws UMException {
		try
		{
			if (mTxSupport) InternalUMFactory.setHookInRunningTransaction(mTxSupport);
			
		final String mn = "public IPrincipal getPrincipal(String uniqueId)";
		if (myLoc.beInfo()) {
			myLoc.infoT(
				mn,
				"uniqueId {0}, active company concept {1}, company context {2}",
				new Object[] {
					uniqueId,
					ACTIVE_COMPANY_ATRRIBUTE_NAME,
					myCompanyId });
		}
		String privateType =
			getPrivatePrincipalTypeIdentifier(
				getPrincipalType(uniqueId));
		UMEPermission perm =
			new UMEPermission(
				privateType,
				UMEPermission.ACTION_READ,
				UMEPermission.COM_SAP_SECURITY_CORE_USERMANAGEMENT_UNIQUEID,
				uniqueId,
				myCompanyId);
		InternalUMFactory.checkPermissionOfLoggedInUser(perm);
		if (myLoc.beDebug()) {
			myLoc.debugT(
				mn,
				"Logged in user {0} has permission {1}, proceeding",
				new Object[] {
					UMFactory.getAuthenticator().getLoggedInUser(),
					perm });
		}
		return UMFactory.getPrincipalFactory().getPrincipal(uniqueId);
	}
		finally
		{
			if (mTxSupport) InternalUMFactory.setHookInRunningTransaction(false);
		}
	}

	public IPrincipal getPrincipal(
		String uniqueId,
		AttributeList attributeList)
		throws UMException {
		try
		{
			if (mTxSupport) InternalUMFactory.setHookInRunningTransaction(mTxSupport);
			
		final String mn =
			"public IPrincipal getPrincipal(String uniqueId, AttributeList attributeList)";
		if (myLoc.beInfo()) {
			myLoc.infoT(
				mn,
				"uniqueId {0}, attributeList {1}, active company concept {2}, company context {3}",
				new Object[] {
					uniqueId,
					attributeList,
					ACTIVE_COMPANY_ATRRIBUTE_NAME,
					myCompanyId });
		}
		String privateType =
			getPrivatePrincipalTypeIdentifier(
				getPrincipalType(uniqueId));
		UMEPermission perm =
			new UMEPermission(
				privateType,
				UMEPermission.ACTION_READ,
				UMEPermission.COM_SAP_SECURITY_CORE_USERMANAGEMENT_UNIQUEID,
				uniqueId,
				myCompanyId);
		InternalUMFactory.checkPermissionOfLoggedInUser(perm);
		if (myLoc.beDebug()) {
			myLoc.debugT(
				mn,
				"Logged in user {0} has permission {1}, proceeding",
				new Object[] {
					UMFactory.getAuthenticator().getLoggedInUser(),
					perm });
		}
		return UMFactory.getPrincipalFactory().getPrincipal(
			uniqueId,
			attributeList);
	}
		finally
		{
			if (mTxSupport) InternalUMFactory.setHookInRunningTransaction(false);
		}
	}

	public boolean isPrincipalModifiable(String uniqueId) throws UMException {
		try
		{
			if (mTxSupport) InternalUMFactory.setHookInRunningTransaction(mTxSupport);
			
		final String mn =
			"public boolean isPrincipalModifiable(String uniqueId)";
		if (myLoc.beInfo()) {
			myLoc.infoT(
				mn,
				"uniqueId {0}, active company concept {1}, company context {2}",
				new Object[] {
					uniqueId,
					ACTIVE_COMPANY_ATRRIBUTE_NAME,
					myCompanyId });
		}
		boolean result =
			UMFactory.getPrincipalFactory().isPrincipalModifiable(uniqueId);
		if (myLoc.beDebug()) {
			if (result) {
				myLoc.debugT(mn, "principal is modifyable");
			} else {
				myLoc.debugT(mn, "principal is not modifyable");
			}
		}
		String privateType = getPrivatePrincipalTypeIdentifier(
			getPrincipalType(uniqueId));
		if (result) {
			UMEPermission perm =
				new UMEPermission(
					privateType,
					UMEPermission.ACTION_EDIT,
					UMEPermission.COM_SAP_SECURITY_CORE_USERMANAGEMENT_UNIQUEID,
					uniqueId,
					myCompanyId);
			result = InternalUMFactory.loggedInUserHasPermission(perm);
			if (myLoc.beDebug()) {
				if (result) {
					myLoc.debugT(
						mn,
						"Logged in user {0} has permission {1}, principal is modifyable",
						new Object[] {
							UMFactory.getAuthenticator().getLoggedInUser(),
							perm });
				} else {
					myLoc.debugT(
						mn,
						"Logged in user {0} has not permission {1}, principal is not modifyable",
						new Object[] {
							UMFactory.getAuthenticator().getLoggedInUser(),
							perm });
				}
			}
		}
		return result;
	}
		finally
		{
			if (mTxSupport) InternalUMFactory.setHookInRunningTransaction(false);
		}
	}

	public boolean isPrincipalDeletable(String uniqueId) throws UMException {
		try
		{
			if (mTxSupport) InternalUMFactory.setHookInRunningTransaction(mTxSupport);
			
		final String mn =
			"public boolean isPrincipalDeletable(String uniqueId)";
		if (myLoc.beInfo()) {
			myLoc.infoT(
				mn,
				"uniqueId {0}, active company concept {1}, company context {2}",
				new Object[] {
					uniqueId,
					ACTIVE_COMPANY_ATRRIBUTE_NAME,
					myCompanyId });
		}
		boolean result = true;
		//checking if current logged in user is the same user, which should be deleted
		//deleting own user should not work
		IUser currentUser = UMFactory.getAuthenticator().getLoggedInUser();
		if (currentUser != null && currentUser.getUniqueID().equals(uniqueId)){
			result = false;
		}
		if (result){
			result =
				UMFactory.getPrincipalFactory().isPrincipalDeletable(uniqueId);
			if (myLoc.beDebug()) {
				if (result) {
					myLoc.debugT(mn, "principal is deleteable");
				} else {
					myLoc.debugT(mn, "principal is not deleteable");
				}
			}			
		}
		String privateType =
			getPrivatePrincipalTypeIdentifier(
				getPrincipalType(uniqueId));
		if (result) {
			UMEPermission perm =
				new UMEPermission(
					privateType,
					UMEPermission.ACTION_DELETE,
					UMEPermission.COM_SAP_SECURITY_CORE_USERMANAGEMENT_UNIQUEID,
					uniqueId,
					myCompanyId);
			result = InternalUMFactory.loggedInUserHasPermission(perm);
			if (myLoc.beDebug()) {
				if (result) {
					myLoc.debugT(
						mn,
						"Logged in user {0} has permission {1}, principal is deleteable",
						new Object[] {
							UMFactory.getAuthenticator().getLoggedInUser(),
							perm });
				} else {
					myLoc.debugT(
						mn,
						"Logged in user {0} has not permission {1}, principal is not deleteable",
						new Object[] {
							UMFactory.getAuthenticator().getLoggedInUser(),
							perm });
				}
			}
			if (result && IPrincipalDatabag.ROLE_TYPE.equals(privateType)) {
				try {
					checkAclForRole(uniqueId, false);
				} catch (PrincipalNotAccessibleException e) {
					result = false;
				}
				if (myLoc.beDebug()) {
					if (result) {
						myLoc.debugT(
							mn,
							"ACL check was successful, principal is deleteable");
					} else {
						myLoc.debugT(
							mn,
							"ACL check was not successful, principal is not deleteable");
					}
				}
			}
		}
		return result;
	}
		finally
		{
			if (mTxSupport) InternalUMFactory.setHookInRunningTransaction(false);
		}
	}

	public boolean isPrincipalAttributeModifiable(
		String uniqueId,
		String namespace,
		String name)
		throws UMException {
		try
		{
			if (mTxSupport) InternalUMFactory.setHookInRunningTransaction(mTxSupport);
			
		String type =
			getPrivatePrincipalTypeIdentifier(
				getPrincipalType(uniqueId));
		return isPrincipalAttributeModifiable(type, uniqueId, namespace, name);
	}
		finally
		{
			if (mTxSupport) InternalUMFactory.setHookInRunningTransaction(false);
		}
	}
	
	public boolean isPrincipalAttributeModifiable(
		String principalType,
		String uniqueId,
		String namespace,
		String name)
		throws UMException {
		try
		{
			if (mTxSupport) InternalUMFactory.setHookInRunningTransaction(mTxSupport);
			
		final String mn =
			"public boolean isPrincipalAttributeModifiable(String principalType, String uniqueId, String namespace, String name)";
		IUser currentUser = UMFactory.getAuthenticator().getLoggedInUser();
		if (myLoc.beInfo()) {
			myLoc.infoT(
				mn,
				"principalType {0}, uniqueId {1}, namespace {2}, name {3}, active company concept {4}, company context {5}",
				new Object[] {
					principalType,
					uniqueId,
					namespace,
					name,
					ACTIVE_COMPANY_ATRRIBUTE_NAME,
					myCompanyId });
		}

		boolean result = false;
		String privateType = getPrivatePrincipalTypeIdentifier(principalType);
		if (JmxActionFactoryWrapper.ACTION_TYPE.equals(privateType)){
			result = JmxActionFactoryWrapper.isActionAttributeModifiable(
						uniqueId, namespace, name);
		} else {
			switch (myUsedCompanyConcept) {
			case CC_BPO : 
				if (name.equals(ATTR_TPD) && namespace.equals(IPrincipal.DEFAULT_NAMESPACE)){
					result = false;
				} else {
					result =
					UMFactory.getPrincipalFactory().isPrincipalAttributeModifiable(
						uniqueId,
						namespace,
						name);
				}
				break;
			default :
				result =
					UMFactory.getPrincipalFactory().isPrincipalAttributeModifiable(
						uniqueId,
						namespace,
						name);			
				break;
			}
			if (myLoc.beDebug()) {
				if (result) {
					myLoc.debugT(mn, "principal attribute is modifyable");
				} else {
					myLoc.debugT(mn, "principal attribute is not modifyable");
				}
			}			
		}
		
		if (result) {
			UMEPermission perm =
				new UMEPermission(
					privateType,
					UMEPermission.ACTION_EDIT,
					namespace + DOT + name,
					uniqueId,
					myCompanyId);
			result = InternalUMFactory.loggedInUserHasPermission(perm);
			if (myLoc.beDebug()) {
				if (result) {
					myLoc.debugT(
						mn,
						"Logged in user {0} has permission {1}, principal attribute is modifyable",
						new Object[] {
							currentUser,
							perm });
				} else {
					myLoc.debugT(
						mn,
						"Logged in user {0} has not permission {1}, principal attribute is not modifyable",
						new Object[] {
							currentUser,
							perm });
				}
			}
		}
		if (result && IPrincipalDatabag.ROLE_TYPE.equals(privateType)) {
			try {
				checkAclForRole(uniqueId, false);
			} catch (PrincipalNotAccessibleException e) {
				result = false;
			}
			if (myLoc.beDebug()) {
				if (result) {
					myLoc.debugT(
						mn,
						"ACL check was successful, principal attribute is modifyable");
				} else {
					myLoc.debugT(
						mn,
						"ACL check was not successful, principal attribute is not modifyable");
				}
			}
		}
		if (result
			&& name.equals(ILoginConstants.LOGON_PWD_ALIAS)
			&& namespace.equals(IPrincipal.DEFAULT_NAMESPACE)
			&& IPrincipalDatabag.ACCOUNT_TYPE.equals(privateType)){
			if (!UMFactory.getSecurityPolicy().getPasswordChangeAllowed()){
				IUserAccount[] currentAccounts = currentUser.getUserAccounts();
				if (currentAccounts != null && currentAccounts.length > 0){
					if (uniqueId.equals(currentAccounts[0].getUniqueID())){
						UMEPermission perm =
							new UMEPermission(
								privateType,
								UMEPermission.ACTION_EDIT,
								namespace + DOT + name,
								null,
								myCompanyId);
						if (!InternalUMFactory.loggedInUserHasPermission(perm)){
							result = false;	
						}
					}
				}
			}
		}
		return result;
	}
		finally
		{
			if (mTxSupport) InternalUMFactory.setHookInRunningTransaction(false);
		}
	}
	
	public boolean isPrincipalAttributeCreateable(
		String principalType,
		IPrincipal principal,
		String namespace,
		String name) throws UMException{
		try
		{
			if (mTxSupport) InternalUMFactory.setHookInRunningTransaction(mTxSupport);
			
		final String mn =
			"public boolean isPrincipalAttributeCreateable(String principalType, String namespace, String name)";
		if (myLoc.beInfo()) {
			myLoc.infoT(
				mn,
				"principalType {0}, namespace {1}, name {2}, active company concept {3}, company context {4}",
				new Object[] {
					principalType,
					namespace,
					name,
					ACTIVE_COMPANY_ATRRIBUTE_NAME,
					myCompanyId });
		}
		if (ATTR_TPD.equals(name) && IPrincipal.DEFAULT_NAMESPACE.equals(namespace)){
			return false;
		}
		boolean result = false;
		switch (myUsedCompanyConcept) {
			case CC_BPO : 
				if (name.equals(ATTR_TPD) && namespace.equals(IPrincipal.DEFAULT_NAMESPACE)){
					result = false;
				} else {
					result =
					UMFactory.getPrincipalFactory().isPrincipalAttributeModifiable(
						principal,
						namespace,
						name);
				}
				break;
			default :
				result =
					UMFactory.getPrincipalFactory().isPrincipalAttributeModifiable(
						principal,
						namespace,
						name);			
				break;
		}

		if (myLoc.beDebug()) {
			if (result) {
				myLoc.debugT(mn, "principal attribute is modifyable");
			} else {
				myLoc.debugT(mn, "principal attribute is not modifyable");
			}
		}
		String privateType = getPrivatePrincipalTypeIdentifier(principalType);
		if (result){
			UMEPermission perm =
				new UMEPermission(
					privateType,
					UMEPermission.ACTION_CREATE,
					namespace + DOT + name,
					null,
					myCompanyId);
			result = InternalUMFactory.loggedInUserHasPermission(perm);
			if (myLoc.beDebug()) {
				if (result) {
					myLoc.debugT(
						mn,
						"Logged in user {0} has permission {1}, principal attribute is createable",
						new Object[] {
							UMFactory.getAuthenticator().getLoggedInUser(),
							perm });
				} else {
					myLoc.debugT(
						mn,
						"Logged in user {0} has not permission {1}, principal attribute is not createable",
						new Object[] {
							UMFactory.getAuthenticator().getLoggedInUser(),
							perm });
				}
			}			
		}
		//if user has to be created, check datasource itself
//		if (result && IPrincipalDatabag.USER_TYPE.equals(privateType)
//					&& IPrincipal.DEFAULT_NAMESPACE.equals(namespace)
//					&& UNIQUEID.equals(name)) {
//			result = UMFactory.getUserFactory().isUserCreationPossible();
//		}
		//removed, for calling isPrincipalAttributeCreateable, a dummy user has already be created
		return result;
	}
		finally
		{
			if (mTxSupport) InternalUMFactory.setHookInRunningTransaction(false);
		}
	}

	public IPrincipal[] getPrincipals(String[] uniqueIds)
		throws NoSuchPrincipalException {
		try
		{
			if (mTxSupport) InternalUMFactory.setHookInRunningTransaction(mTxSupport);
			
		final String mn =
			"public IPrincipal[] getPrincipals(String[] uniqueIds)";
		if (myLoc.beInfo()) {
			myLoc.infoT(
				mn,
				"active company concept {0}, company context {1}",
				new Object[] { ACTIVE_COMPANY_ATRRIBUTE_NAME, myCompanyId });
		}
		try {
			for (int i = 0; i < uniqueIds.length; i++) {
				if (myLoc.beDebug()) {
					myLoc.debugT(
						mn,
						"uniqueId {0}",
						new Object[] { uniqueIds[i] });
				}
				String privateType =
					getPrivatePrincipalTypeIdentifier(
						getPrincipalType(
							uniqueIds[i]));
				UMEPermission perm =
					new UMEPermission(
						privateType,
						UMEPermission.ACTION_READ,
						UMEPermission
							.COM_SAP_SECURITY_CORE_USERMANAGEMENT_UNIQUEID,
						uniqueIds[i],
						myCompanyId);
				InternalUMFactory.checkPermissionOfLoggedInUser(perm);
				if (myLoc.beDebug()) {
					myLoc.debugT(
						mn,
						"Logged in user {0} has permission {1}, proceeding",
						new Object[] {
							UMFactory.getAuthenticator().getLoggedInUser(),
							perm });
				}
			}
		} catch (UMException ume) {
			throw new NoSuchPrincipalException(ume);
		}
		return UMFactory.getPrincipalFactory().getPrincipals(uniqueIds);
	}
		finally
		{
			if (mTxSupport) InternalUMFactory.setHookInRunningTransaction(false);
		}
	}

	public IPrincipal[] getPrincipals(
		String[] uniqueIds,
		AttributeList attributeList)
		throws NoSuchPrincipalException {
		try
		{
			if (mTxSupport) InternalUMFactory.setHookInRunningTransaction(mTxSupport);
			
		final String mn =
			"public IPrincipal[] getPrincipals(String[] uniqueIds, AttributeList attributeList)";
		if (myLoc.beInfo()) {
			myLoc.infoT(
				mn,
				"attributeList {0}, active company concept {1}, company context {2}",
				new Object[] {
					attributeList,
					ACTIVE_COMPANY_ATRRIBUTE_NAME,
					myCompanyId });
		}
		try {
			for (int i = 0; i < uniqueIds.length; i++) {
				if (myLoc.beDebug()) {
					myLoc.debugT(
						mn,
						"uniqueId {0}",
						new Object[] { uniqueIds[i] });
				}
				String privateType =
					getPrivatePrincipalTypeIdentifier(
						getPrincipalType(
							uniqueIds[i]));
				UMEPermission perm =
					new UMEPermission(
						privateType,
						UMEPermission.ACTION_READ,
						UMEPermission
							.COM_SAP_SECURITY_CORE_USERMANAGEMENT_UNIQUEID,
						uniqueIds[i],
						myCompanyId);
				InternalUMFactory.checkPermissionOfLoggedInUser(perm);
				if (myLoc.beDebug()) {
					myLoc.debugT(
						mn,
						"Logged in user {0} has permission {1}, proceeding",
						new Object[] {
							UMFactory.getAuthenticator().getLoggedInUser(),
							perm });
				}
			}
		} catch (UMException ume) {
			throw new NoSuchPrincipalException(ume);
		}
		return UMFactory.getPrincipalFactory().getPrincipals(
			uniqueIds,
			attributeList);
	}
		finally
		{
			if (mTxSupport) InternalUMFactory.setHookInRunningTransaction(false);
		}
	}

	public void deletePrincipal(String uniqueId) throws UMException {
		final String mn = "public void deletePrincipal(String uniqueId)";
		try
		{
			if (mTxSupport) InternalUMFactory.setHookInRunningTransaction(mTxSupport);
			
		if (myLoc.beInfo()) {
			myLoc.infoT(
				mn,
				"uniqueId {0}, active company concept {1}, company context {2}",
				new Object[] {
					uniqueId,
					ACTIVE_COMPANY_ATRRIBUTE_NAME,
					myCompanyId });
		}
		String privateType =
			getPrivatePrincipalTypeIdentifier(
				getPrincipalType(uniqueId));
		UMEPermission perm =
			new UMEPermission(
				privateType,
				UMEPermission.ACTION_DELETE,
				UMEPermission.COM_SAP_SECURITY_CORE_USERMANAGEMENT_UNIQUEID,
				uniqueId,
				myCompanyId);
		InternalUMFactory.checkPermissionOfLoggedInUser(perm);
		if (myLoc.beDebug()) {
			myLoc.debugT(
				mn,
				"Logged in user {0} has permission {1}, proceeding",
				new Object[] {
					UMFactory.getAuthenticator().getLoggedInUser(),
					perm });
		}
		if (IPrincipalDatabag.ROLE_TYPE.equals(privateType)) {
			try {
				checkAclForRole(uniqueId, false);
			} catch (PrincipalNotAccessibleException e) {
				throw new AccessControlException("no authorisation");
			}
			if (myLoc.beDebug()) {
				myLoc.debugT(
					mn,
					"ACL check was successful, principal will be deleted");
			}
		}
		UMFactory.getPrincipalFactory().deletePrincipal(uniqueId);
	}
		finally
		{
			if (mTxSupport) InternalUMFactory.setHookInRunningTransaction(false);
		}
	}

	public void deletePrincipals(String[] uniqueIds) throws UMException {
		try
		{
			if (mTxSupport) InternalUMFactory.setHookInRunningTransaction(mTxSupport);
			
		final String mn = "public void deletePrincipals(String[] uniqueIds)";
		if (myLoc.beInfo()) {
			myLoc.infoT(
				mn,
				"active company concept {0}, company context {1}",
				new Object[] { ACTIVE_COMPANY_ATRRIBUTE_NAME, myCompanyId });
		}
		for (int i = 0; i < uniqueIds.length; i++) {
			if (myLoc.beDebug()) {
				myLoc.debugT(mn, "uniqueId {0}", new Object[] { uniqueIds[i] });
			}
			String privateType =
				getPrivatePrincipalTypeIdentifier(
					getPrincipalType(
						uniqueIds[i]));
			UMEPermission perm =
				new UMEPermission(
					privateType,
					UMEPermission.ACTION_DELETE,
					UMEPermission.COM_SAP_SECURITY_CORE_USERMANAGEMENT_UNIQUEID,
					uniqueIds[i],
					myCompanyId);
			InternalUMFactory.checkPermissionOfLoggedInUser(perm);
			if (myLoc.beDebug()) {
				myLoc.debugT(
					mn,
					"Logged in user {0} has permission {1}, proceeding",
					new Object[] {
						UMFactory.getAuthenticator().getLoggedInUser(),
						perm });
			}
			if (IPrincipalDatabag.ROLE_TYPE.equals(privateType)) {
				try {
					checkAclForRole(uniqueIds[i], false);
				} catch (PrincipalNotAccessibleException e) {
					throw new AccessControlException("no authorisation");
				}
				if (myLoc.beDebug()) {
					myLoc.debugT(
						mn,
						"ACL check was successful, principal will be deleted");
				}
			}
		}
		UMFactory.getPrincipalFactory().deletePrincipals(uniqueIds);
	}
		finally
		{
			if (mTxSupport) InternalUMFactory.setHookInRunningTransaction(false);
		}
	}

	public ISearchResult searchPrincipals(IPrincipalSearchFilter searchFilter)
		throws UMException {
		try
		{
			if (mTxSupport) InternalUMFactory.setHookInRunningTransaction(mTxSupport);
			
		final String mn =
			"public ISearchResult searchPrincipals(IPrincipalSearchFilter searchFilter)";
		if (myLoc.beInfo()) {
			myLoc.infoT(
				mn,
				"searchFilter {0}, active company concept {1}, company context {2}",
				new Object[] {
					searchFilter,
					ACTIVE_COMPANY_ATRRIBUTE_NAME,
					myCompanyId });
		}
		String privateType =
			getPrivatePrincipalTypeIdentifier(
				searchFilter.getSearchPrincipalType());
		UMEPermission perm =
			new UMEPermission(
				privateType,
				UMEPermission.ACTION_READ,
				UMEPermission.COM_SAP_SECURITY_CORE_USERMANAGEMENT_UNIQUEID,
				null,
				myCompanyId);
		InternalUMFactory.checkPermissionOfLoggedInUser(perm);
		if (myLoc.beDebug()) {
			myLoc.debugT(
				mn,
				"Logged in user {0} has permission {1}, proceeding",
				new Object[] {
					UMFactory.getAuthenticator().getLoggedInUser(),
					perm });
		}
		switch (myUsedCompanyConcept) {
			case CC_BPO :
				if (null != myCompanyId) {
					if (IPrincipalDatabag.USER_TYPE.equals(privateType)
						|| IPrincipalDatabag.ACCOUNT_TYPE.equals(privateType)) {
						searchFilter.setSearchAttribute(
							IPrincipal.DEFAULT_NAMESPACE,
							ATTR_BPO,
							myCompanyId,
							ISearchAttribute.EQUALS_OPERATOR,
							false);
					} else if (IPrincipalDatabag.GROUP_TYPE.equals(privateType)) {
						searchFilter.setSearchAttribute(
							IPrincipal.DEFAULT_NAMESPACE,
							CompanyGroups.COMPANY_ATTRIBUTE,
							myCompanyId,
							ISearchAttribute.EQUALS_OPERATOR,
							false);
					} else if (IPrincipalDatabag.ROLE_TYPE.equals(privateType)) {
						searchFilter.setSearchAttribute(
							IPrincipal.DEFAULT_NAMESPACE,
							Role.NAMESPACE_ATTRIBUTE,
							myCompanyId,
							ISearchAttribute.EQUALS_OPERATOR,
							false);
					}
				}
				break;
			case CC_TPD :
				if (myCompanyId != null) {
					if (IPrincipalDatabag.USER_TYPE.equals(privateType)) {
						searchFilter.setSearchAttribute(
							IPrincipal.DEFAULT_NAMESPACE,
							ATTR_TPD,
							myCompanyId,
							ISearchAttribute.EQUALS_OPERATOR,
							false);
					/* if searched type equals groups, it has to be checked if the logged in user is allowed to change the company
					 * attribute of a user --> group mapping of a user.
					 */
					} else if (IPrincipalDatabag.GROUP_TYPE.equals(privateType)) {
						perm =
							new UMEPermission(
								IPrincipalDatabag.USER_TYPE,
								UMEPermission.ACTION_READ,
								UMEPermission
									.COM_SAP_SECURITY_CORE_USERMANAGEMENT_UNIQUEID,
								null,
								null);
						boolean result =
							InternalUMFactory.loggedInUserHasPermission(perm);
						if (!result) {
							if (myLoc.beDebug()) {
								myLoc.debugT(
									mn,
									"Logged in user {0} has not permission {1}, proceeding",
									new Object[] {
										UMFactory
											.getAuthenticator()
											.getLoggedInUser(),
										perm });
							}
							String companyGroupsName;
							if (EMPTY.equals(myCompanyId)){
								companyGroupsName = CompanyGroups.GUEST_COMPANY_GROUP_ID;
							} else {
								companyGroupsName = myCompanyId;
							}
							searchFilter.setSearchAttribute(
								IPrincipal.DEFAULT_NAMESPACE,
								ATTR_TPD,
								companyGroupsName,
								ISearchAttribute.EQUALS_OPERATOR,
								false);
						}
					}
				}
				break;
			default :
				//nothing todo if company concept is disabled
				break;
		}
		if (myLoc.beDebug()) {
			myLoc.debugT(mn, "searchFilter {0}", new Object[] { searchFilter });
		}
		return UMFactory.getPrincipalFactory().searchPrincipals(searchFilter);
	}
		finally
		{
			if (mTxSupport) InternalUMFactory.setHookInRunningTransaction(false);
		}
	}

	public IPrincipalMaint getMutablePrincipal(String uniqueId)
		throws UMException {
		try
		{
			if (mTxSupport) InternalUMFactory.setHookInRunningTransaction(mTxSupport);
			
			return getMutablePrincipal(uniqueId, IPrincipal.DEFAULT_NAMESPACE, UNIQUEID);
	}
		finally
		{
			if (mTxSupport) InternalUMFactory.setHookInRunningTransaction(false);
		}
	}

	public ISearchResult simplePrincipalSearch(
		String searchCriteria,
		String principalType,
		int mode,
		boolean caseSensitive,
		CompositeData[] searchAttributes)
		throws OpenDataException, UMException {
		return simplePrincipalSearchByDatasources(searchCriteria,
				principalType, null, mode, caseSensitive, searchAttributes, null, null);
	}

	public ISearchResult simplePrincipalSearchByDatasources(
			String searchCriteria,
			String principalType,
			String[] requestDatasourceIds,
			int mode,
			boolean caseSensitive,
			CompositeData[] searchAttributes,
			Locale locale,
			List<IJmxMessage> additionalMessages)
			throws OpenDataException, UMException {
		try
		{
			if (mTxSupport) InternalUMFactory.setHookInRunningTransaction(mTxSupport);
			
		final String mn =
			"public ISearchResult simplePrincipalSearch(String searchCriteria, String principalType, int mode, boolean caseSensitive, Map searchAttributes)";
		if (myLoc.beInfo()) {
			myLoc.infoT(
				mn,
				"searchCriteria {0}, principalType {1}, mode {2}, caseSensitive {3}, active company concept {4}, company context {5}",
				new Object[] {
					searchCriteria,
					principalType,
					new Integer(mode),
					new Boolean(caseSensitive),
					ACTIVE_COMPANY_ATRRIBUTE_NAME,
					myCompanyId });
		}
		String privateType = getPrivatePrincipalTypeIdentifier(principalType);
		UMEPermission perm =
			new UMEPermission(
				privateType,
				UMEPermission.ACTION_READ,
				UMEPermission.COM_SAP_SECURITY_CORE_USERMANAGEMENT_UNIQUEID,
				null,
				myCompanyId);
		InternalUMFactory.checkPermissionOfLoggedInUser(perm);
		if (myLoc.beDebug()) {
			myLoc.debugT(
				mn,
				"Logged in user {0} has permission {1}, proceeding",
				new Object[] {
					UMFactory.getAuthenticator().getLoggedInUser(),
					perm });
		}
		/*
		 * Special internal Map for simple search in order to
		 * pass company and check attributes to simple search
		 */
		Map<String, Object> simpleSearchMap = new HashMap<String, Object>();
		switch (myUsedCompanyConcept) {
			case CC_BPO :
				if (null != myCompanyId) {
					simpleSearchMap.put(ATTR_BPO, myCompanyId);
				}
				break;
			case CC_TPD :
				if (null != myCompanyId) {
					if (IPrincipalDatabag.USER_TYPE.equals(privateType)) {
						simpleSearchMap.put(ATTR_TPD, myCompanyId);
					/* if searched type equals groups, it has to be checked if the logged in user is allowed to change the company
					 * attribute of a user --> group mapping of a user.
					 */
					} else if (IPrincipalDatabag.GROUP_TYPE.equals(privateType)) {
						perm =
							new UMEPermission(
								IPrincipalDatabag.USER_TYPE,
								UMEPermission.ACTION_READ,
								UMEPermission
									.COM_SAP_SECURITY_CORE_USERMANAGEMENT_UNIQUEID,
								null,
								null);
						boolean result =
							InternalUMFactory.loggedInUserHasPermission(perm);
						if (!result) {
							if (myLoc.beDebug()) {
								myLoc.debugT(
									mn,
									"Logged in user {0} has not permission {1}, proceeding",
									new Object[] {
										UMFactory
											.getAuthenticator()
											.getLoggedInUser(),
										perm });
							}
							String companyGroupsName;
							if (EMPTY.equals(myCompanyId)){
								companyGroupsName = CompanyGroups.GUEST_COMPANY_GROUP_ID;
							} else {
								companyGroupsName = myCompanyId;
							}
							simpleSearchMap.put(ATTR_TPD, companyGroupsName);
						}
					}
				}
				break;
			default :
				//nothing todo if company concept is disabled
				break;
		}
		if (IPrincipalDatabag.ROLE_TYPE.equals(privateType) && UME_JMX_SEARCH_SIMPLE_ROLE_ACCESS__CHECK){
			//acl check for simple principal search in order to hide the pcd roles which are not assignable for the current user
			UMEEntityAccessPermission pcdPerm = new UMEEntityAccessPermission(UMEEntityAccessPermission.NAME_ROLE_PCD, UMEEntityAccessPermission.ACTION_VIEW);
			if (!InternalUMFactory.loggedInUserHasPermission(pcdPerm)){
				if (myLoc.beInfo()){
					myLoc.infoT(mn, "Adding transient attribute search flag for PCD role check");
				}
				simpleSearchMap.put(IPrincipalSearchFilter.CHECK_ACCESS, TRUE);	
			}
			//Check if logged in user has appropriate permission. If not, hide UME roles
			UMEEntityAccessPermission umePerm = new UMEEntityAccessPermission(UMEEntityAccessPermission.NAME_ROLE_UME, UMEEntityAccessPermission.ACTION_VIEW);
			if (!InternalUMFactory.loggedInUserHasPermission(umePerm)){
				if (myLoc.beInfo()){
					myLoc.infoT(mn, "Adding transient attribute search flag for UME role hide");
				}
				simpleSearchMap.put(IPrincipalSearchFilter.HIDE_UME_ROLES, TRUE);	
			}
		}
		/* evaluate, which datasources should be searched for
		 */
		Set<String> datasourceSet = evaluateDatasourcesToSearchFor(
				requestDatasourceIds, privateType, locale, additionalMessages);
		if (datasourceSet != null){
			simpleSearchMap.put(IPrincipal.DATASOURCE,
					(String[]) datasourceSet
							.toArray(new String[datasourceSet
									.size()]));			
		}
		ISearchResult result = UMFactory.getPrincipalFactory()
				.simplePrincipalSearch(searchCriteria,
						principalType, mode, caseSensitive,
						simpleSearchMap);
		/*
		 * It is possible to search only for roles and groups, which have a modifyable relation attribute only.
		 * This is used for the assignment to users; only roles and groups are visible which are able to be assigned.
		 */
		if (searchAttributes != null && searchAttributes.length > 0 && searchAttributes[0] != null){
			IJmxAttribute[] jmxSearchAttributes = JmxAttribute.generateJmxAttributes(searchAttributes);
			for (int i = 0; i < jmxSearchAttributes.length; i++) {
				if (IPrincipal.DEFAULT_RELATION_NAMESPACE
					.equals(jmxSearchAttributes[i].getNamespace())
					&& IPrincipal.PRINCIPAL_RELATION_MEMBER_ATTRIBUTE
							.equals(jmxSearchAttributes[i].getName())
					&& jmxSearchAttributes[i].getModifyable() == true) {
					PersistenceCollection coll = new PersistenceCollection();
					while (result.hasNext()){
						String uniqueId = (String)result.next();
						try {
							if (this
								.isPrincipalAttributeModifiable(
									uniqueId,
									IPrincipal.DEFAULT_RELATION_NAMESPACE,
									IPrincipal.PRINCIPAL_RELATION_MEMBER_ATTRIBUTE)) {
								coll.add(uniqueId);
							}
						} catch (NoSuchPrincipalException e) {
							if (myLoc.beInfo()){
								myLoc.traceThrowableT(Severity.INFO, mn, e);
							}
						} catch (PrincipalNotAccessibleException e){
							if (myLoc.beInfo()){
								myLoc.traceThrowableT(Severity.INFO, mn, e);
							}
						}
					}
					result = new PrincipalIteratorImpl(
						coll,
						PrincipalIteratorImpl.ITERATOR_TYPE_UNIQUEIDS_NOT_CHECKED);
				}
			}
		}
		return result;
	} finally {
			if (mTxSupport)
				InternalUMFactory.setHookInRunningTransaction(false);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.sap.security.api.IConfigurable#setTxSupport(boolean)
	 */
	public void setTxSupport(boolean txSupport) {
		mTxSupport = txSupport;
	}

}
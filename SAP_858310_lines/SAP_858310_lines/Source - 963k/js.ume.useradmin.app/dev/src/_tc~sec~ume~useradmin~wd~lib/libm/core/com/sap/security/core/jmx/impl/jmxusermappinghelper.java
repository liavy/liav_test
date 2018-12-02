package com.sap.security.core.jmx.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.OpenDataException;

import com.sap.security.api.AuthenticationFailedException;
import com.sap.security.api.IGroup;
import com.sap.security.api.IPrincipal;
import com.sap.security.api.IPrincipalFactory;
import com.sap.security.api.IRole;
import com.sap.security.api.IUser;
import com.sap.security.api.NoSuchUserException;
import com.sap.security.api.UMException;
import com.sap.security.api.UMFactory;
import com.sap.security.api.logon.ILoginConstants;
import com.sap.security.api.umap.IUserMapping;
import com.sap.security.api.umap.IUserMappingConverter;
import com.sap.security.api.umap.IUserMappingData;
import com.sap.security.api.umap.MultipleHitException;
import com.sap.security.api.umap.NoLogonDataAvailableException;
import com.sap.security.api.umap.system.ExceptionInImplementationException;
import com.sap.security.api.umap.system.ISystemLandscapeObject;
import com.sap.security.api.umap.system.ISystemLandscape;
import com.sap.security.core.InternalUMFactory;
import com.sap.security.core.jmx.IJmxAttribute;
import com.sap.security.core.jmx.IJmxEntity;
import com.sap.security.core.jmx.IJmxMapEntry;
import com.sap.security.core.jmx.IJmxServer;
import com.sap.security.core.jmx.IJmxState;
import com.sap.security.core.umap.imp.UserMappingUtils;
import com.sap.security.core.util.UMEPermission;
import com.sap.security.core.util.imp.Util;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

// TODO UserMappingDataImp: Better change behaviour to read and decrypt data
//      not before the first call to enrich(). This will make it possible to
//      get an IUserMappingData object and use methods like isReadOnly() and
//      others that are called rather often e.g. by getUserMappingSystems()
//      without the need to read and decrypt the data each time (which would
//      probably have a bad impact on performance)
// TODO Verify even non-master-system mapping data by opening a test connection
//      using the connector service?
// TODO Never transfer the mapped password to the JMX client.

/**
 * @author Heiko Ettelbrueck (d034567)
 */
public class JmxUserMappingHelper {

    private static final Location LOCATION = Location.getLocation(JmxUserMappingHelper.class);

    private static final String PASSWORD_DUMMY = "********";

    private static final String SYSTEM_NAME_SEPARATOR = ":";

    private static final IUserMapping _userMapping = UMFactory.getUserMapping();

    public static IJmxEntity[] getUserMappingSystems(String principalUniqueId, Locale locale)
    throws OpenDataException, ExceptionInImplementationException, UMException {
        final String method = "getUserMappingSystems(String)";

        if(LOCATION.beInfo()) {
            LOCATION.infoT(method,
                "Received request to determine all systems available for user mapping " +
                "for principal \"{0}\".",
                new Object[] { principalUniqueId } );
        }

        // Permission check: Is this an administrator or an end-user?
        String principalType = CompanyPrincipalFactory.getPrivatePrincipalTypeIdentifier(
            UMFactory.getPrincipalFactory().getPrincipalType(principalUniqueId));

        IUser editingUser = UMFactory.getAuthenticator().getLoggedInUser();
        String editingUserID = editingUser.getUniqueID();

        UMEPermission permission = new UMEPermission(principalType,
            UMEPermission.ACTION_DELETE,
            UMEPermission.COM_SAP_SECURITY_CORE_USERMANAGEMENT_UNIQUEID,
            principalUniqueId, null, editingUserID
        );

        boolean inAdminContext = editingUser.hasPermission(permission);

        IPrincipal principal = UMFactory.getPrincipalFactory().getPrincipal(principalUniqueId);

        if(LOCATION.beInfo()) {
            LOCATION.infoT(method,
                "The current user is {0} (editing principal {1}). According to user " +
                "permissions, the request for user mapping systems is performed as {2}.",
                new Object[] {
                    UserMappingUtils.getLoggingStringForPrincipal(editingUser),
                    UserMappingUtils.getLoggingStringForPrincipal(principal),
                    inAdminContext ? "administrator" : "end-user"
                });
        }

        IJmxEntity[] systemEntities;

        // Handle case that there is no registered system landscape wrapper
        // (e.g. AS Java without Enterprise Portal)
        List<ISystemLandscape> landscapes = UMFactory.getSystemLandscapeFactory().getAllLandscapes();
        if(landscapes.size() > 0) {
            // Iterate over all registered system landscapes and collect all systems
            // that should be displayed in the UI.
            List<IJmxEntity> systemEntitiesList = new ArrayList<IJmxEntity>();

            Iterator<ISystemLandscape> landscapesIterator = landscapes.iterator();
            while(landscapesIterator.hasNext()) {
                ISystemLandscape currentLandscape = landscapesIterator.next();

                Enumeration aliasesEnum = currentLandscape.getAllAliases(principal);

                // The result for each landscape is a list containing all systems from
                // that landscape which should be displayed on the UI.
                List<IJmxEntity> relevantSystemsFromCurrentLandscape = new ArrayList<IJmxEntity>();

                while(aliasesEnum.hasMoreElements()) {
                    String systemAlias = (String) aliasesEnum.nextElement();

                    ISystemLandscapeObject currentSystem = null;
                    try {
                        currentSystem = currentLandscape.getSystemByAlias(systemAlias);
                    }
                    catch(ExceptionInImplementationException e) {
                        LOCATION.traceThrowableT(Severity.ERROR, method, e);
                    }

                    if(currentSystem == null) {
                        InternalUMFactory.CATEGORY.warningT(LOCATION, method,
                            "While scanning system landscape \"{0}\" for systems available for " +
                            "user mapping for principal {1}, system alias \"{2}\" was listed, but " +
                            "the related system object could not be retrieved.\nThe system " +
                            "will not be shown in the user mapping maintenance UI.",
                            new Object[] {
                                currentLandscape.getType(),
                                UserMappingUtils.getLoggingStringForPrincipal(principal),
                                systemAlias
                            } );
                        // TODO Pass this error to the UI in some way?
                        continue;
                    }

                    // Use the master system only if the principal is a user.
                    boolean isCurrentSystemMasterSystem = UserMappingUtils.isMasterSystem(currentSystem);
                    if(isCurrentSystemMasterSystem) {
                        if(! (principal instanceof IUser)) {
                            if(LOCATION.beInfo()) {
                                LOCATION.infoT(method,
                                    "System {0} is the SAP reference system, but the current " +
                                    "principal {1} is not a user. User mappings for the SAP " +
                                    "reference system can only be maintained for users. " +
                                    "Skipping this system.",
                                    new Object[] {
                                        UserMappingUtils.getLoggingStringForSystem(currentSystem),
                                        UserMappingUtils.getLoggingStringForPrincipal(principal)
                                    } );
                            }
                            continue;
                        }
                        else {
                            if(LOCATION.beInfo()) {
                                LOCATION.infoT(method,
                                    "System {0} is the SAP reference system. As the current " +
                                    "principal {1} is a user, going on checking this system " +
                                    "(note: only users, but no other principals, can be mapped " +
                                    "to backend users in the SAP reference system).",
                                    new Object[] {
                                        UserMappingUtils.getLoggingStringForSystem(currentSystem),
                                        UserMappingUtils.getLoggingStringForPrincipal(principal)
                                    } );
                            }
                        }
                    }
                    else {
                        if(LOCATION.beInfo()) {
                            LOCATION.infoT(method,
                                "System {0} is not the SAP reference system. Going on checking " +
                                "this system for principal {1}.",
                                new Object[] {
                                    UserMappingUtils.getLoggingStringForSystem(currentSystem),
                                    UserMappingUtils.getLoggingStringForPrincipal(principal)
                                } );
                        }
                    }

                    // Filter according to logon method?
                    // (Reference system: only method "SAPLOGONTICKET",
                    //  other systems   : only method "UIDPW")
                    String logonMethod = currentSystem.getLogonMethod();

                    // If there's no "logonmethod" attribute at all, assume "UIDPW"
                    // (because the attribute is usually missing for systems that
                    //  simply don't support any method other than plain UIDPW)
                    if(logonMethod == null) {
                        if(LOCATION.beInfo()) {
                            LOCATION.infoT(method,
                                "There is no logon method maintained for system {0}. Assuming " +
                                "\"{1}\" as default. Going on checking this system for principal {2}.",
                                new Object[] {
                                    UserMappingUtils.getLoggingStringForSystem(currentSystem),
                                    ILoginConstants.SSO_JCO_LOGON_METHOD_UIDPW,
                                    UserMappingUtils.getLoggingStringForPrincipal(principal)
                                } );
                        }
                        logonMethod = ILoginConstants.SSO_JCO_LOGON_METHOD_UIDPW;
                    }

                    // In case of logon ticket, display the system only if it's the reference
                    // system (if not, the mapping wouldn't be effective anyway)
                    if(ILoginConstants.SSO_JCO_LOGON_METHOD_TICKET.equals(logonMethod)) {
                        if(! isCurrentSystemMasterSystem) {
                            if(LOCATION.beInfo()) {
                                LOCATION.infoT(method,
                                    "Logon method of system {0} is \"{1}\", but the system is not " +
                                    "the SAP reference system. User mapping data for systems " +
                                    "other than the SAP reference system will not be effective " +
                                    "if the logon method is set this way. Hiding the system " +
                                    "for principal {2}.",
                                    new Object[] {
                                        UserMappingUtils.getLoggingStringForSystem(currentSystem),
                                        ILoginConstants.SSO_JCO_LOGON_METHOD_TICKET,
                                        UserMappingUtils.getLoggingStringForPrincipal(principal)
                                    });
                            }
                            continue;
                        }
                        else {
                            if(LOCATION.beInfo()) {
                                LOCATION.infoT(method,
                                    "Logon method of system {0} is \"{1}\" and the system is the " +
                                    "SAP reference system. Going on checking this system for " +
                                    "principal {2}.",
                                    new Object[] {
                                        UserMappingUtils.getLoggingStringForSystem(currentSystem),
                                        ILoginConstants.SSO_JCO_LOGON_METHOD_TICKET,
                                        UserMappingUtils.getLoggingStringForPrincipal(principal)
                                    });
                            }
                        }
                    }
                    // If the logon method is neither logon ticket nor UIDPW, a user mapping
                    // wouldn't be effective --> don't display
                    else if(! ILoginConstants.SSO_JCO_LOGON_METHOD_UIDPW.equals(logonMethod)) {
                        if(LOCATION.beInfo()) {
                            LOCATION.infoT(method,
                                "Logon method of system {0} is neither \"{1}\" nor \"{2}\", so " +
                                "possibly existing user mapping data for this system would " +
                                "not be effective. Skipping this system for principal {3}.",
                                new Object[] {
                                    UserMappingUtils.getLoggingStringForSystem(currentSystem),
                                    ILoginConstants.SSO_JCO_LOGON_METHOD_TICKET,
                                    ILoginConstants.SSO_JCO_LOGON_METHOD_UIDPW,
                                    UserMappingUtils.getLoggingStringForPrincipal(principal)
                                });
                        }
                        continue;
                    }

                    // Use all systems that have the correct "usermappingtype"
                    String mappingType = (String)
                        currentSystem.getAttribute(IUserMapping.UMAP_USERMAPPING_TYPE);

                    if(mappingType != null) {
                        if(inAdminContext) {
                            if(mappingType.indexOf(IUserMapping.UMAP_USERMAPPING_TYPE_ADMIN) == -1) {
                                if(LOCATION.beInfo()) {
                                    LOCATION.infoT(method,
                                        "System {0} is not available for user mapping for " +
                                        "administrators, but the request asks for systems " +
                                        "for which administrators may define user mapping " +
                                        "data. Skipping this system for principal {1}.",
                                        new Object[] {
                                            UserMappingUtils.getLoggingStringForSystem(currentSystem),
                                            UserMappingUtils.getLoggingStringForPrincipal(principal)
                                        } );
                                }
                                continue;
                            }
                            else {
                                if(LOCATION.beInfo()) {
                                    LOCATION.infoT(method,
                                        "System {0} is available for user mapping for " +
                                        "administrators, which matches the request for " +
                                        "systems for which administrators may define user " +
                                        "mapping data. Going on checking this system for " +
                                        "principal {1}.",
                                        new Object[] {
                                            UserMappingUtils.getLoggingStringForSystem(currentSystem),
                                            UserMappingUtils.getLoggingStringForPrincipal(principal)
                                        } );
                                }
                            }
                        }
                        else { // -> in end-user context
                            if(mappingType.indexOf(IUserMapping.UMAP_USERMAPPING_TYPE_USER) == -1) {
                                if(LOCATION.beInfo()) {
                                    LOCATION.infoT(method,
                                        "System {0} is not available for user mapping for " +
                                        "end-users, but the request asks for systems for which " +
                                        "end-users may define user mapping data. Skipping this " +
                                        "system for principal {1}.",
                                        new Object[] {
                                            UserMappingUtils.getLoggingStringForSystem(currentSystem),
                                            UserMappingUtils.getLoggingStringForPrincipal(principal)
                                        } );
                                }
                                continue;
                            }
                            else {
                                if(LOCATION.beInfo()) {
                                    LOCATION.infoT(method,
                                        "System {0} is available for user mapping for " +
                                        "end-users, which matches the request for systems " +
                                        "for which end-users may define user mapping data. " +
                                        "Going on checking this system for principal {1}.",
                                        new Object[] {
                                            UserMappingUtils.getLoggingStringForSystem(currentSystem),
                                            UserMappingUtils.getLoggingStringForPrincipal(principal)
                                        } );
                                }
                            }
                        }
                    }
                    else { // --> mappingType is invalid
                        if(LOCATION.beInfo()) {
                            LOCATION.infoT(method,
                                "The user mapping type (attribute \"{0}\") of system {1} is empty. " +
                                "Cannot determine whether this system should be available for " +
                                "user mapping for administrators, end-users or both. Skipping " +
                                "this system for principal {2}.",
                                new Object[] {
                                    IUserMapping.UMAP_USERMAPPING_TYPE,
                                    UserMappingUtils.getLoggingStringForSystem(currentSystem),
                                    UserMappingUtils.getLoggingStringForPrincipal(principal)
                                } );
                        }
                        continue;
                    }

                    // If we reach this code, the system is available for user mapping
                    // in the current scenario.
                    if(LOCATION.beInfo()) {
                        LOCATION.infoT(method,
                            "Adding system {0} to the list of systems available for user " +
                            "mapping for principal {1}.",
                            new Object[] {
                                UserMappingUtils.getLoggingStringForSystem(currentSystem),
                                UserMappingUtils.getLoggingStringForPrincipal(principal)
                            } );
                    }

                    // The system entity is specified by a qualified name which contains the landscape type.
                    String qualifiedSystemName = getQualifiedSystemName(currentLandscape, currentSystem);
                    JmxEntity currentEntity = new JmxEntity();
                    currentEntity.setUniqueId(qualifiedSystemName);

                    // TODO Adjust initial list size...
                    List<JmxAttribute> currentAttributes = new ArrayList<JmxAttribute>(5);

                    // TODO When adding additional attributes, adjust internal class JmxSystemEntityComparator
                    //      which expects that the display name is contained in the first attribute.
                    currentAttributes.add(new JmxAttribute(
                        IJmxServer.UMAP_NAMESP_SYSTEM,
                        IJmxServer.UMAP_ATTR_DISPLAY_NAME,
                        getDisplayNameForSystem(currentLandscape, currentSystem, locale)
                    ));

                    currentAttributes.add(new JmxAttribute(
                        IJmxServer.UMAP_NAMESP_SYSTEM,
                        IJmxServer.UMAP_ATTR_CRYPTO_STATUS,
                        _userMapping.checkCryptoConfiguration(currentSystem)
                    ));

                    currentAttributes.add(new JmxAttribute(
                        IJmxServer.UMAP_NAMESP_SYSTEM,
                        IJmxServer.UMAP_ATTR_IS_REFERENCE_SYSTEM,
                        isCurrentSystemMasterSystem
                    ));

                    currentAttributes.add(new JmxAttribute(
                        IJmxServer.UMAP_NAMESP_SYSTEM,
                        IJmxServer.UMAP_ATTR_HAS_MAPPING_DATA,
                        _userMapping.existsMappingData(currentSystem, principal)
                    ));

                    currentAttributes.add(new JmxAttribute(
                        IJmxServer.UMAP_NAMESP_SYSTEM,
                        IJmxServer.UMAP_ATTR_FIELDS,
                        (String) currentSystem.getAttribute(IUserMapping.UMAP_USERMAPPING_FIELDS)
                    ));

                    currentEntity.setAttributes(currentAttributes);
                    relevantSystemsFromCurrentLandscape.add(currentEntity);
                } // Iteration over all systems from the current landscape.
                
                systemEntitiesList.addAll(relevantSystemsFromCurrentLandscape);
            } // Iteration over all registered system landscapes.

            // Sort all system entities.
            Collections.sort(systemEntitiesList, JmxSystemEntityComparator.getInstance());

            // Create the result array.
            systemEntities = systemEntitiesList.toArray(new IJmxEntity[systemEntitiesList.size()]);

            if(LOCATION.beInfo()) {
                String systemsList;
                if(systemEntities.length > 0) {
                    String[] systemIDs = new String[systemEntities.length];
                    for(int j = 0; j < systemEntities.length; j++) {
                        systemIDs[j] = systemEntities[j].getUniqueId();
                    }
                    systemsList = Util.getArrayContentsAsString(systemIDs);
                }
                else {
                    systemsList = "No systems";
                }
                LOCATION.infoT(method,
                    "All systems available for principal {0} in {1} context: {2}",
                    new Object[] {
                        UserMappingUtils.getLoggingStringForPrincipal(principal),
                        inAdminContext ? "administrator" : "end-user",
                        systemsList
                    } );
            }
        }
        else {
            systemEntities = new IJmxEntity[0];
            LOCATION.infoT(method,
                "No systems available for user mapping because no system landscape " +
                "wrapper has been registered for UME.\n" +
                "Current principal: {0}",
                new Object[] {
                    UserMappingUtils.getLoggingStringForPrincipal(principal)
                } );
        }

        return systemEntities;
    }

    public static IJmxEntity getUserMappingData(String principalUniqueId, String systemId)
    throws OpenDataException, ExceptionInImplementationException, UMException,
    NoLogonDataAvailableException {
        final String method = "getUserMappingData(String, String)";

        if(LOCATION.beInfo()) {
            LOCATION.infoT(method,
                "Received request to retrieve user mapping data for principal \"{0}\" and " +
                "system \"{1}\".",
                new Object[] { principalUniqueId, systemId } );
        }

        IPrincipal principal = UMFactory.getPrincipalFactory().getPrincipal(principalUniqueId);
        ISystemLandscapeObject system = getSystemByQualifiedName(systemId);

        IUserMappingData mappingData =
            _userMapping.getUserMappingData(system, principal);
        Map<String, String> logonData = new HashMap<String, String>(2);
        try {
            mappingData.enrich(logonData);

            // Special handling of password: Replace _existing_ password
            // by "*" characters
            // (because it will never be necessary as cleartext in the UI)
            if(logonData.containsKey(IUserMappingData.UMAP_PASSWORD)) {
                logonData.put(IUserMappingData.UMAP_PASSWORD, PASSWORD_DUMMY);
            }
        }
        catch(NoLogonDataAvailableException e) {
            // Different handling depending on whether there's simply no data at
            // all or whether there was some kind of error
            switch(e.getReason()) {
                case NoLogonDataAvailableException.REASON_NO_DATA :
                    if(LOCATION.beInfo()) {
                        LOCATION.infoT(method,
                            "No user mapping data available for principal {0} and system {1}.",
                        new Object[] {
                            UserMappingUtils.getLoggingStringForPrincipal(principal),
                            UserMappingUtils.getLoggingStringForSystem(system)
                        } );
                    }
                    break;
                // Handling of real errors (i.e. the following reason codes) is
                // performed on the UI side, so it's sufficient to have a default
                // handler here:
                case NoLogonDataAvailableException.REASON_UNKNOWN :
                case NoLogonDataAvailableException.REASON_CRYPTO_MISMATCH :
                case NoLogonDataAvailableException.REASON_MULTIPLE_MAPPING :
                case NoLogonDataAvailableException.REASON_POLICY_FILES_MISSING :
                case NoLogonDataAvailableException.REASON_KEY_CHANGED :
                case NoLogonDataAvailableException.REASON_NO_TICKET :
                case NoLogonDataAvailableException.REASON_NO_CERTIFICATE :
                case NoLogonDataAvailableException.REASON_NO_KEY :
                default :
                    InternalUMFactory.CATEGORY.warningT(LOCATION, method,
                        "There was an error while reading user mapping data for " +
                        "principal {0} and system {1}.",
                        new Object[] {
                            UserMappingUtils.getLoggingStringForPrincipal(principal),
                            UserMappingUtils.getLoggingStringForSystem(system)
                        } );
                    LOCATION.traceThrowableT(Severity.ERROR, method, e);
                    throw e;
            }
        }

        JmxEntity mappingEntity = new JmxEntity();
        mappingEntity.setUniqueId(getUniqueIdForMapping(principal, systemId));
        mappingEntity.setModifyable(! mappingData.isReadOnly());

        // TODO Adjust initial list size...
        List<JmxAttribute> mappingAttributes = new ArrayList<JmxAttribute>(2 + logonData.size() + 1);

        mappingAttributes.add(new JmxAttribute(
            IJmxServer.UMAP_NAMESP_SYSTEM,
            IJmxServer.UMAP_ATTR_CRYPTO_STATUS,
            _userMapping.checkCryptoConfiguration(system)
        ));

        IPrincipal sourceOfIndirectMapping = mappingData.getSourceOfIndirectMapping();
        if(sourceOfIndirectMapping != null) {
            mappingAttributes.add(new JmxAttribute(
                IJmxServer.UMAP_NAMESP_MAPPING_DETAILS,
                IJmxServer.UMAP_ATTR_INDIRECT_MAPPING_SRC,
                sourceOfIndirectMapping.getUniqueID()
            ));

            mappingAttributes.add(new JmxAttribute(
                IJmxServer.UMAP_NAMESP_MAPPING_DETAILS,
                IJmxServer.UMAP_ATTR_INDIRECT_MAPPING_SRC_DISPLAY,
                sourceOfIndirectMapping.getDisplayName()
            ));

            String principalType = null;
            if(sourceOfIndirectMapping instanceof IUser) {
                principalType = IPrincipalFactory.IUSER;
            }
            else if(sourceOfIndirectMapping instanceof IGroup) {
               principalType = IPrincipalFactory.IGROUP;
            }
            else if(sourceOfIndirectMapping instanceof IRole) {
               principalType = IPrincipalFactory.IROLE;
            }
            // Unexpected principal type
            // This should not happen as user mapping only supports users,
            // groups and roles, but no other principal types
            else {
                if(LOCATION.beInfo()) {
                    LOCATION.infoT(method,
                        "The source principal of the inherited user mapping (for " +
                        "principal {0} and system {1}) has an unexpected type. It is " +
                        "not a usual UME user, group or role. The implementation " +
                        "class is: {2}",
                    new Object[] {
                        UserMappingUtils.getLoggingStringForSystem(system),
                        UserMappingUtils.getLoggingStringForPrincipal(principal),
                        sourceOfIndirectMapping.getClass().getName()
                    });
                }
            }

            if(principalType != null) {
                mappingAttributes.add(new JmxAttribute(
                    IJmxServer.UMAP_NAMESP_MAPPING_DETAILS,
                    IJmxServer.UMAP_ATTR_INDIRECT_MAPPING_SRC_TYPE,
                    principalType
                ));
            }
        }

        Iterator logonDataIterator = logonData.entrySet().iterator();
        while(logonDataIterator.hasNext()) {
            Map.Entry entry = (Map.Entry) logonDataIterator.next();

            mappingAttributes.add(new JmxAttribute(
                IJmxServer.UMAP_NAMESP_LOGON_DATA,
                (String) entry.getKey(),
                (String) entry.getValue()
            ));
        }

        // Check inverse mapping data (to see whether there are multiple mappings
        // to the same backend user, which could cause problems when a logon ticket
        // arrives and only contains an ABAP user ID --> more than one UME user
        // matches)
        // Check only for users (no inverse mapping for other principal types)
        // and only if there's any mapping data for the current system
        if(principal instanceof IUser && logonData.size() > 0) {
            String[] inverseMappings;
            String mappedUser = logonData.get(IUserMappingData.UMAP_USER);
            try {
                String localUserID = _userMapping.getInverseMappingData(mappedUser, system);
                inverseMappings = new String[] { localUserID };
            }
            catch(MultipleHitException e) {
                // Determine all users being mapped to this backend user.
                inverseMappings = e.getUserNames();
                InternalUMFactory.CATEGORY.infoT(LOCATION, method,
                    "User {0} is not the only user mapped to backend user ID \"{1}\" for " +
                    "system {2}. This can lead to problems when evaluating SAP logon " +
                    "tickets issued by ABAP systems and trying to determine the mapped " +
                    "UME user.\n" +
                    "All local users mapped to backend user \"{1}\": {3}",
                    new Object[] {
                        UserMappingUtils.getLoggingStringForPrincipal(principal),
                        mappedUser,
                        UserMappingUtils.getLoggingStringForSystem(system),
                        Util.getArrayContentsAsString(inverseMappings),
                    }
                );
                LOCATION.traceThrowableT(Severity.DEBUG, method, e);
            }
            catch(UMException e) {
                inverseMappings = null;
                InternalUMFactory.CATEGORY.warningT(LOCATION, method,
                    "An error occured while trying to retrieve the local user(s) being " +
                    "mapped to user \"{0}\" in backend system {1}.",
                    new Object[] {
                        mappedUser,
                        UserMappingUtils.getLoggingStringForSystem(system)
                    } );
                LOCATION.traceThrowableT(Severity.ERROR, method, e);
            }

            if(inverseMappings != null) {
                for(int i = 0; i < inverseMappings.length; i++) {
                    mappingAttributes.add(new JmxAttribute(
                        IJmxServer.UMAP_NAMESP_INVERSE_MAPPING,
                        Integer.toString(i),
                        inverseMappings[i]
                    ));
                }
            }
        }

        mappingEntity.setAttributes(mappingAttributes);

        if(LOCATION.beInfo()) {
            LOCATION.infoT(method,
                "Successfully created JMX entity object containing user mapping data " +
                "for principal {0} and system {1}.",
                new Object[] {
                    UserMappingUtils.getLoggingStringForPrincipal(principal),
                    UserMappingUtils.getLoggingStringForSystem(system)
                } );
        }

        return mappingEntity;
    }

    public static void storeUserMappingData(String principalUniqueId, String systemId,
    CompositeData[] logonData)
    throws OpenDataException, UMException, ExceptionInImplementationException, AuthenticationFailedException {
        final String method = "storeUserMappingData(String, String, CompositeData[]";

        if(LOCATION.beInfo()) {
            LOCATION.infoT(method,
                "Received request to store user mapping data for principal \"{0}\" and " +
                "system \"{1}\".",
                new Object[] { principalUniqueId, systemId } );
        }

        IJmxMapEntry[] mapEntries = new IJmxMapEntry[logonData.length];
        for(int i = 0; i < mapEntries.length; i++) {
            mapEntries[i] = new JmxMapEntry(logonData[i]);
        }
        Map logonDataMap = JmxUtils.convertJmxMapEntriesToMap(mapEntries);

        IPrincipal principal =
            UMFactory.getPrincipalFactory().getPrincipal(principalUniqueId);
        ISystemLandscapeObject system = getSystemByQualifiedName(systemId);

        IUserMappingData mappingData =
            _userMapping.getUserMappingData(system, principal);

        // Handle the case that some data was changed, but the mapped password
        // has not been touched
        // -> password is still "********"
        // -> replace it with the real password again
        if(PASSWORD_DUMMY.equals(logonDataMap.get(IUserMappingData.UMAP_PASSWORD))) {
            if(LOCATION.beInfo()) {
                LOCATION.infoT(method,
                    "Mapped password in user mapping data for principal {0} and " +
                    "system {1} has not been changed in the UI (i.e. it still consists " +
                    "of \"*\" characters only). Checking for existing password which " +
                    "will be reused.",
                    new Object[] {
                        UserMappingUtils.getLoggingStringForPrincipal(principal),
                        UserMappingUtils.getLoggingStringForSystem(system)
                    } );
            }

            Map<Object, Object> oldLogonData = new HashMap<Object, Object>(logonDataMap.size());

            try {
                mappingData.enrich(oldLogonData);

                logonDataMap.put(IUserMappingData.UMAP_PASSWORD,
                    oldLogonData.get(IUserMappingData.UMAP_PASSWORD)
                );
            }
            catch(NoLogonDataAvailableException e) {
                // Different handling depending on whether there's simply no data at
                // all or whether there was some kind of error
                switch(e.getReason()) {
                    case NoLogonDataAvailableException.REASON_NO_DATA :
                        if(LOCATION.beInfo()) {
                            LOCATION.infoT(method,
                                "No existing user mapping data available for " +
                                "principal {0} and system {1}.",
                                new Object[] {
                                    UserMappingUtils.getLoggingStringForPrincipal(principal),
                                    UserMappingUtils.getLoggingStringForSystem(system)
                                } );
                        }
                        break;
                    // Handling of real errors (i.e. the following reason codes) is
                    // performed on the UI side, so it's sufficient to have a default
                    // handler here:
                    case NoLogonDataAvailableException.REASON_UNKNOWN :
                    case NoLogonDataAvailableException.REASON_CRYPTO_MISMATCH :
                    case NoLogonDataAvailableException.REASON_MULTIPLE_MAPPING :
                    case NoLogonDataAvailableException.REASON_POLICY_FILES_MISSING :
                    case NoLogonDataAvailableException.REASON_KEY_CHANGED :
                    case NoLogonDataAvailableException.REASON_NO_TICKET :
                    case NoLogonDataAvailableException.REASON_NO_CERTIFICATE :
                    case NoLogonDataAvailableException.REASON_NO_KEY :
                    default :
                        InternalUMFactory.CATEGORY.warningT(LOCATION, method,
                            "There was an error while saving user mapping data for " +
                            "principal {0} and system {1}.",
                            new Object[] {
                                UserMappingUtils.getLoggingStringForPrincipal(principal),
                                UserMappingUtils.getLoggingStringForSystem(system)
                            } );
                        LOCATION.traceThrowableT(Severity.ERROR, method, e);
                        throw e;
                    // Never throw an exception here! -> Perhaps overwriting existing
                    // mapping data by new data fixes the problem.
                }
            }
        }

        // Actually save the credentials. This may include verification of the credentials.
        mappingData.saveLogonData(logonDataMap);

        if(LOCATION.beInfo()) {
            LOCATION.infoT(method,
                "Successfully saved user mapping data for principal {0} and system {1}.",
                new Object[] {
                    UserMappingUtils.getLoggingStringForPrincipal(principal),
                    UserMappingUtils.getLoggingStringForSystem(system)
                } );
        }
    }

    public static void clearUserMappingData(String principalUniqueId, String systemId)
    throws UMException, ExceptionInImplementationException {
        final String method = "clearUserMappingData(String, String)";

        if(LOCATION.beInfo()) {
            LOCATION.infoT(method,
                "Received request to clear user mapping data for principal \"{0}\" and " +
                "system \"{1}\".",
                new Object[] { principalUniqueId, systemId } );
        }

        IPrincipal principal =
            UMFactory.getPrincipalFactory().getPrincipal(principalUniqueId);
        ISystemLandscapeObject system = getSystemByQualifiedName(systemId);
        IUserMappingData mappingData =
            _userMapping.getUserMappingData(system, principal);
        mappingData.saveLogonData(null);

        if(LOCATION.beInfo()) {
            LOCATION.infoT(method,
                "Successfully cleared user mapping data for principal \"{0}\" and system \"{1}\".",
                new Object[] { principalUniqueId, systemId } );
        }
    }

    public static IJmxEntity[] getAvailableUserMappingConverters()
    throws OpenDataException {
        final String method = "getAvailableUserMappingConverters()";

        LOCATION.infoT(method,
            "Received request to retrieve available converters for user mapping data.");

        IUserMappingConverter[] allConverters = _userMapping.getAvailableConverters();
        JmxEntity[] allConvertersTypes = new JmxEntity[allConverters.length];

        IUserMappingConverter currentConverter       = null;
        JmxEntity             currentConverterEntity = null;

        for(int i = 0; i < allConverters.length; i++) {
            currentConverter       = allConverters[i];
            currentConverterEntity = new JmxEntity();

            currentConverterEntity.setUniqueId(currentConverter.getType());
            currentConverterEntity.setModifyable(currentConverter.isConversionPossible());

            // Add conversion status information
            Map currentStatus = currentConverter.getConversionStatus();

            List<JmxAttribute> currentAttributes = new ArrayList<JmxAttribute>(currentStatus.size());
            Iterator currentStatusIterator = currentStatus.entrySet().iterator();
            while(currentStatusIterator.hasNext()) {
                Map.Entry currentEntry = (Map.Entry) currentStatusIterator.next();

                // A cast without checking is safe here because the key is always a String
                String name = (String) currentEntry.getKey();

                // For the value, we require that toString() returns a reasonable String
                // representation of the value object
                String value = currentEntry.getValue().toString();

                currentAttributes.add(new JmxAttribute(IJmxServer.UMAP_NAMESP_CONV, name, value));
            }
            currentConverterEntity.setAttributes(currentAttributes);

            allConvertersTypes[i] = currentConverterEntity;
        }

        if(LOCATION.beInfo()) {
            String converterList;
            if(allConvertersTypes.length > 0) {
                String[] converterNames = new String[allConvertersTypes.length];
                for(int i = 0; i < allConvertersTypes.length; i++) {
                    converterNames[i] = allConvertersTypes[i].getUniqueId();
                }
                converterList = Util.getArrayContentsAsString(converterNames);
            }
            else {
                converterList = "No converters";
            }
            LOCATION.infoT(method,
                "All available user mapping data converters: {0}",
                new Object[] { converterList } );
        }

        return allConvertersTypes;
    }

    public static void startUserMappingConversion(String converterType)
    throws UMException {
        final String method = "startUserMappingConversion(String)";

        if(LOCATION.beInfo()) {
            LOCATION.infoT(method,
                "Received request to start conversion of user mapping data using " +
                "converter \"{0}\".",
                new Object[] { converterType } );
        }

        IUserMappingConverter converter = getUserMappingConverter(converterType);
        converter.startConversion();

        if(LOCATION.beInfo()) {
            LOCATION.infoT(method, "User mapping data conversion using converter \"{0}\" " +
                "successfully started.",
                new Object[] { converterType } );
        }
    }

    public static void startUserMappingConversion(String converterType,
    int numberOfThreads) throws UMException {
        final String method = "startUserMappingConversion(String, int)";

        if(LOCATION.beInfo()) {
            LOCATION.infoT(method,
                "Received request to start conversion of user mapping data using " +
                "converter \"{0}\". The requests explicitely asks for usage of {1} " +
                "parallel threads.",
                new Object[] { converterType, Integer.toString(numberOfThreads) } );
        }

        IUserMappingConverter converter = getUserMappingConverter(converterType);
        converter.startConversion(numberOfThreads);

        if(LOCATION.beInfo()) {
            LOCATION.infoT(method,
                "User mapping data conversion using converter \"{0}\" successfully started " +
                "using {1} threads.",
                new Object[] { converterType, Integer.toString(numberOfThreads) } );
        }
    }

    public static void resetUserMappingConversionStatus(String converterType) {
        final String method = "resetUserMappingConversionStatus(String)";

        if(LOCATION.beInfo()) {
            LOCATION.infoT(method,
                "Received request to reset conversion status information of user mapping " +
                "data using converter \"{0}\".",
                new Object[] { converterType } );
        }

        IUserMappingConverter converter = getUserMappingConverter(converterType);
        converter.resetStatus();

        if(LOCATION.beInfo()) {
            LOCATION.infoT(method,
                "Status of user mapping data converter \"{0}\" has been reset.",
                new Object[] { converterType } );
        }
    }

    public static String[] getSAPReferenceSystemCandidates() throws ExceptionInImplementationException {
        Map<ISystemLandscapeObject, ISystemLandscape> candidates = getSAPReferenceSystemCandidatesInternal();
        if(candidates == null) {
            // This has already been traced in getSAPReferenceSystemCandidatesInternal().
            return null;
        }

        // Prepare result array.
        String[] candidateSystemIDs = new String[candidates.size()];

        // Iterate over all candidate systems and fill the result array.
        Iterator<Entry<ISystemLandscapeObject, ISystemLandscape>> candidateIterator
            = candidates.entrySet().iterator();
        int currentResultIndex = 0;
        while(candidateIterator.hasNext()) {
            Entry<ISystemLandscapeObject, ISystemLandscape> currentEntry = candidateIterator.next();
            ISystemLandscape currentLandscape = currentEntry.getValue();
            ISystemLandscapeObject  currentSystem    = currentEntry.getKey  ();

            String currentSystemID = getQualifiedSystemName(currentLandscape, currentSystem);
            candidateSystemIDs[currentResultIndex++] = currentSystemID;
        }

        // Finally sort all candidate systems by their unique IDs (= system landscape type + system alias)
        Arrays.sort(candidateSystemIDs);

        return candidateSystemIDs;
    }

    public static IJmxEntity[] getSAPReferenceSystemCandidates2(Locale locale)
    throws ExceptionInImplementationException, OpenDataException {
        Map<ISystemLandscapeObject, ISystemLandscape> candidates = getSAPReferenceSystemCandidatesInternal();
        if(candidates == null) {
            // This has already been traced in getSAPReferenceSystemCandidatesInternal().
            return null;
        }

        // Prepare result array.
        IJmxEntity[] candidateEntities = new IJmxEntity[candidates.size()];

        // Iterate over all candidate systems and fill the result array.
        Iterator<Entry<ISystemLandscapeObject, ISystemLandscape>> candidateIterator
            = candidates.entrySet().iterator();
        int currentResultIndex = 0;
        while(candidateIterator.hasNext()) {
            Entry<ISystemLandscapeObject, ISystemLandscape> currentEntry = candidateIterator.next();
            ISystemLandscape currentLandscape = currentEntry.getValue();
            ISystemLandscapeObject  currentSystem    = currentEntry.getKey  ();

            String currentSystemID = getQualifiedSystemName(currentLandscape, currentSystem);

            // TODO When adding additional attributes, adjust internal class JmxSystemEntityComparator
            //      when expects that the display name is contained in the first attribute.
            IJmxAttribute[] currentAttributes = new IJmxAttribute[] {
                new JmxAttribute(
                    IJmxServer.UMAP_NAMESP_SYSTEM,
                    IJmxServer.UMAP_ATTR_DISPLAY_NAME,
                    getDisplayNameForSystem(currentLandscape, currentSystem, locale)
                ) };

            JmxEntity currentEntity = new JmxEntity();
            currentEntity.setUniqueId(currentSystemID);
            currentEntity.setAttributes(currentAttributes);

            candidateEntities[currentResultIndex++] = currentEntity;
        }

        // Finally sort all candidate systems by their unique IDs (= system landscape type + system alias)
        Arrays.sort(candidateEntities, JmxSystemEntityComparator.getInstance());

        return candidateEntities;
    }

    // TODO What the purpose of the "systemType" parameter which is NEVER USED!
    public static IJmxEntity getUserByInverseUserMapping(
        String userId, String systemAlias, String systemType, IJmxState state)
    throws ExceptionInImplementationException, OpenDataException, UMException {
        String method = "getUserByInverseUserMapping";
        if(userId != null) {
            if(systemAlias != null) {
                ISystemLandscape landscape =
                    UMFactory.getSystemLandscapeFactory().getLandscape(ISystemLandscape.TYPE_ENTERPRISE_PORTAL);
                if(landscape != null) {
                    ISystemLandscapeObject system = landscape.getSystemByAlias(systemAlias);
                    if(system != null) {
                        String uniqueId = UMFactory.getUserMapping().getInverseMappingData(userId, system);
                        if(uniqueId != null) {
                            return JmxSearchHelper.getAllEntityDetails(uniqueId, null, state);
                        }
                    }
                }
            }

            String uniqueId = UMFactory.getUserFactory().getUserByLogonID(userId).getUniqueID();
            if(uniqueId != null) {
                return JmxSearchHelper.getAllEntityDetails(uniqueId, null, state);
            }
        }

        String error = MessageFormat.format(
            "A user with userId \"{0}\" in system \"{1}\" (system type \"{2}\") cannot be found.",
            new Object[] { userId, systemAlias, systemType } );
        LOCATION.errorT(method, error);
        throw new NoSuchUserException(error);
    }

    /***************************************************************************
     * Private utility methods
     **************************************************************************/

    private static Map<ISystemLandscapeObject, ISystemLandscape> getSAPReferenceSystemCandidatesInternal()
    throws ExceptionInImplementationException {
        final String method = "getSAPReferenceSystemCandidatesInternal()";

        List<ISystemLandscape> landscapes = UMFactory.getSystemLandscapeFactory().getAllLandscapes();
        if(landscapes.size() == 0){
            LOCATION.infoT(method,
                "There are no system landscapes currently registered in UME. " +
                "Cannot provide any candidates for SAP reference system.");

            return null;
        }
        else {
            // Iterate over all registered landscapes and collect all potential SAP master systems.
            Map<ISystemLandscapeObject, ISystemLandscape> candidates =
                new HashMap<ISystemLandscapeObject, ISystemLandscape>();

            Iterator<ISystemLandscape> landscapesIterator = landscapes.iterator();
            while(landscapesIterator.hasNext()) {
                ISystemLandscape currentLandscape = landscapesIterator.next();

                Enumeration systemsAliases = currentLandscape.getAllAliases();
                while(systemsAliases.hasMoreElements()){
                    String currentSystemAlias = (String) systemsAliases.nextElement();

                    try {
                        ISystemLandscapeObject currentSystem = currentLandscape.getSystemByAlias(currentSystemAlias);

                        if(currentSystem != null) {
                            String currentSystemType = (String) currentSystem.getAttribute(IUserMapping.UMAP_SYSTEM_TYPE);

                            // Only R/3, BW and CRM systems are potential SAP reference systems
                            if(currentSystemType != null
                            && (   currentSystemType.equals("SAP_R3")
                                || currentSystemType.equals("SAP_BW")
                                || currentSystemType.equals("SAP_CRM")
                               )
                            ) {
                                String currentLogonMethod = currentSystem.getLogonMethod();
                                if(ILoginConstants.SSO_JCO_LOGON_METHOD_TICKET.equals(currentLogonMethod)) {
                                    candidates.put(currentSystem, currentLandscape);

                                    if(LOCATION.beInfo()) {
                                        LOCATION.infoT(method,
                                        "Adding system {0} to the list of SAP reference system candidates.",
                                            new Object[] {
                                                UserMappingUtils.getLoggingStringForSystem(currentSystem)
                                            } );
                                    }
                                }
                                else {
                                    if(LOCATION.beDebug()) {
                                        LOCATION.debugT(method,
                                            "System {0} is not a SAP reference system candidate because " +
                                            "its logon method is not \"{1}\", but \"{2}\".",
                                            new Object[] {
                                                UserMappingUtils.getLoggingStringForSystem(currentSystem),
                                                ILoginConstants.SSO_JCO_LOGON_METHOD_TICKET,
                                                currentLogonMethod
                                            } );
                                    }
                                }
                            }
                            else {
                                if(LOCATION.beDebug()) {
                                    LOCATION.debugT(method,
                                        "System {0} is not a SAP reference system candidate because " +
                                        "its system type \"{1}\" is none of SAP_R3, SAP_BW and SAP_CRM.",
                                        new Object[] {
                                            UserMappingUtils.getLoggingStringForSystem(currentSystem),
                                            currentSystemType
                                        } );
                                }
                            }
                        }
                        else { // system object is null!
                            InternalUMFactory.CATEGORY.warningT(LOCATION, method,
                                "The System with alias \"{0}\" cannot be displayed in the list of " +
                                "potential SAP reference systems for UME. It could not be " +
                                "retrieved from system landscape \"{1}\".",
                                new Object[] {
                                    currentSystemAlias,
                                    currentLandscape.getType()
                                } );
                        }
                    }
                    catch(ExceptionInImplementationException e) {
                        InternalUMFactory.CATEGORY.warningT(LOCATION, method,
                            "The System with alias \"{0}\" cannot be displayed in the list of " +
                            "potential SAP reference systems for UME. There was error " +
                            "retrieving it from system landscape \"{1}\".",
                            new Object[] {
                                currentSystemAlias,
                                currentLandscape.getType()
                            } );
                        LOCATION.traceThrowableT(Severity.ERROR, method, e);
                    }
                }
            }

            return candidates;
        }
    }

    private static String getQualifiedSystemName(ISystemLandscape landscape, ISystemLandscapeObject system) {
        return landscape.getType() + SYSTEM_NAME_SEPARATOR + system.getAlias();
    }

    private static ISystemLandscapeObject getSystemByQualifiedName(String qualifiedSystemName)
    throws ExceptionInImplementationException {
        final String method = "getSystemByQualifiedName(String)";

        int separatorIndex = qualifiedSystemName.indexOf(SYSTEM_NAME_SEPARATOR);
        if(separatorIndex < 1) {
            LOCATION.errorT(method, "Received invalid qualified system name \"{0}\" from " +
                "UME UI / JMX client. The name must consist of the system landscape type " +
                "responsible for the system, the separator character \"{1}\" and the " +
                "system alias.",
                new Object[] { qualifiedSystemName, SYSTEM_NAME_SEPARATOR } );
            throw new ExceptionInImplementationException("Invalid system name: The landscape type prefix is missing.");
        }

        String landscapeType = qualifiedSystemName.substring(0, separatorIndex    );
        String systemAlias   = qualifiedSystemName.substring(   separatorIndex + 1);

        ISystemLandscape landscape = UMFactory.getSystemLandscapeFactory().getLandscape(landscapeType);
        if(landscape == null) {
            InternalUMFactory.CATEGORY.infoT(LOCATION, method,
                "Cannot retrieve the requested system \"{0}\" from system landscape \"{1}\" " +
                "because the system landscape is no longer registered in UME. Maybe the service " +
                "or application responsible for that system landscape has been stopped.",
                new Object[] { systemAlias, landscapeType } );
            throw new ExceptionInImplementationException("The specified system landscape is " +
                "currently not registered at UME.");
        }

        ISystemLandscapeObject system = null;
        ExceptionInImplementationException exception = null;
        try {
            system = landscape.getSystemByAlias(systemAlias);
        }
        catch(ExceptionInImplementationException e) {
            LOCATION.traceThrowableT(Severity.ERROR, method, e);
            exception = e;
        }
        if(system == null) {
            InternalUMFactory.CATEGORY.infoT(LOCATION, method,
                "Potential inconsistency found: The system landscape \"{0}\" " +
                "plugged into UME could not retrieve the system object bound to " +
                "system alias \"{0}\". This is no problem if the system has simply " +
                "been deleted in the meanwhile.",
                new Object[] { landscapeType, systemAlias } );
            throw new ExceptionInImplementationException(
                "The system cannot be retrieved from the system landscape.", exception);
        }

        return system;
    }

    private static String getDisplayNameForSystem(ISystemLandscape landscape,
    ISystemLandscapeObject system, Locale locale) {
        String landscapeDisplayName = landscape.getDisplayName(locale);
        String systemDisplayName    = system.getAlias();

        StringBuilder displayName = new StringBuilder(landscapeDisplayName.length() + systemDisplayName.length() + 1);
        displayName.append(landscapeDisplayName).append(':').append(systemDisplayName);

        return displayName.toString();
    }

    private static String getUniqueIdForMapping(IPrincipal principal, String qualifiedSystemName) {
        return MessageFormat.format("{0}/{1}",
            new Object[] {
                principal.getUniqueID(),
                qualifiedSystemName
            }
        );
    }

    private static IUserMappingConverter getUserMappingConverter(String converterType) {
        IUserMappingConverter[] allConverters =
            _userMapping.getAvailableConverters();

        if(converterType == null) {
            return null;
        }

        IUserMappingConverter converter = null;
        for(int i = 0; i < allConverters.length; i++) {
            if(converterType.equals(allConverters[i].getType())) {
                converter = allConverters[i];
                break;
            }
        }

        return converter;
    }

    /**
     * Comparator to compare system entities as IJmxEntity instances based on
     * their display names.
     */
    private static class JmxSystemEntityComparator implements Comparator<IJmxEntity> {

        private static JmxSystemEntityComparator _instance = null;

        public static JmxSystemEntityComparator getInstance() {
            if(_instance == null) {
                _instance = new JmxSystemEntityComparator();
            }

            return _instance;
        }

        public int compare(IJmxEntity systemEntity1, IJmxEntity systemEntity2) {
            // TODO Adjust array index 0 when adding new attributes to reference system candidates.
            String displayName1 = systemEntity1.getAttributes()[0].getValue();
            String displayName2 = systemEntity2.getAttributes()[0].getValue();

            return displayName1.compareTo(displayName2);
        }
        
    }

}

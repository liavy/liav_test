package com.sap.archtech.initservice;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.MarshalException;

import javax.xml.namespace.QName;

import com.sap.archtech.initservice.types.ActionEntry;
import com.sap.archtech.initservice.types.AssignedActionEntry;
import com.sap.archtech.initservice.types.BusinessserviceEntry;
import com.sap.archtech.initservice.types.DescriptionEntry;
import com.sap.archtech.initservice.types.PermissionEntry;
import com.sap.archtech.initservice.types.RoleEntry;
import com.sap.engine.services.webservices.jaxrpc.encoding.XMLMarshaller;
import com.sap.engine.services.webservices.jaxrpc.exceptions.TypeMappingException;
import com.sap.security.api.UMException;
import com.sap.security.core.role.imp.xml.XMLServiceRepository;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

/**
 * The <code>ActionsDescriptorBuilder</code> class provides utility methods for generation and deployment
 * of an "actions.xml" descriptor file at runtime.
 */
class ActionsDescriptorBuilder
{
  private static final Category cat = Category.getCategory(Category.APPS_COMMON_ARCHIVING, "Initialization Service");
  private static final Location loc = Location.getLocation("com.sap.archtech.initservice");
  
  // Permission class for Archiving Connector access
  private static final String ARCHCONN_PERMISSION_CLASS = "com.sap.archtech.archconn.mbeans.ConnectorAccessPermission";
  private static final String ARCHCONN_PREFIX = "ARCH_CO_";  
  // Location of "types.xml" (generated from XML Schema "ActionsSchema.xsd")
  private static final String TYPES_XML_LOCATION = "com/sap/archtech/initservice/types/types.xml";
  // Name of deployment unit
  private static final String DEPLOY_COMP_NAME = "sap.com~tc~archtech~ume_roles.xml";

  // Constants for non-archiving-set-specific actions
  private static final String ACTION_VIEW = "VIEW";
  private static final String ACTION_OVERALL = "OverallArchiving";
  // Constants for archiving-set-specific actions
  private static final String ACTION_ARCHIVE = "ARCHIVE_";
  private static final String ACTION_CONFIGURE = "CONFIG_";
  private static final String ACTION_ORGANIZE = "ORGANIZE_";
  // Constants for Action descriptions
  private static final String DESCR_VIEW = "Readonly access to the NWA Archiving Cockpit";
  private static final String DESCR_OVERALL = "Summary of all archiving-related actions in all Archiving Sets";
  private static final String DESCR_ARCHIVE = "Execute archiving sessions for Archiving Set ";
  private static final String DESCR_CONFIGURE = "Modify the properties of Archiving Set ";
  private static final String DESCR_ORGANIZE = "Execute additional organizational tasks for Archiving Set ";
  // Constants for Permission names
  private static final String PERM_VIEW = "view";
  private static final String PERM_ARCHIVE = "archive";
  private static final String PERM_CONFIGURE = "configure";
  private static final String PERM_ORGANIZE = "organize";
  private static final String PERM_ALL = "*";
  // Constants for Role names
  private static final String ROLE_ARCHSUPERADMIN = "SAP_ARCH_SUPERADMIN";
  private static final String DESCR_ARCHSUPERADMIN = "Archiving Super Administrator";
  private static final String ROLE_J2EEADMIN = "Administrator";
  private static final String ROLE_ARCHREADONLY = "SAP_ARCH_CO_VIEW";
  private static final String DESCR_ARCHREADONLY = "Java Archiving Cockpit, read-only";
  // Constants for Archiving Set names
  private static final String ARCHSET_ALL = "*";
  
  private ActionsDescriptorBuilder()
  {
  }
  
  /**
   * Build an "actions.xml" descriptor file for the given Archiving Sets. 
   * @param archSetNames Names of all Archiving Sets registered in the given system.
   * @return A byte stream containing the serialized "actions.xml"
   * @throws ActionsBuilderException Thrown if XML creation or serialization failed
   */
  static InputStream buildActionsDescriptor(String[] archSetNames) throws ActionsBuilderException
  {
    if(archSetNames == null)
    {
      throw new IllegalArgumentException("Array of Archiving Sets must not be \"null\"!");
    }
    // create Java representation of "actions.xml"
    BusinessserviceEntry bsElement = new BusinessserviceEntry();
    ActionEntry[] archconnAccessEntries = createAllActionElements(archSetNames, ARCHCONN_PREFIX, ARCHCONN_PERMISSION_CLASS);
    bsElement.setACTION(archconnAccessEntries);
    bsElement.setROLE(createAllRoleElements());
    // perform Java-to-XML marshalling
    return getSerializedXML(bsElement);
  }
  
  static void deployActions(InputStream actionsStream) throws ActionsBuilderException
  {
    try
    {
      XMLServiceRepository.deployActionsXMLFile(DEPLOY_COMP_NAME, actionsStream);
    }
    catch(UMException e)
    {
      cat.logThrowableT(Severity.ERROR, loc, "Deployment of \"actions.xml\" failed", e);
      throw new ActionsBuilderException(e);
    }
  }
  
  private static DescriptionEntry createDescriptionElement(String descrValue)
  {
    if(descrValue == null)
    {
      throw new IllegalArgumentException("Value for \"Description\" element must not be \"null\"!");
    }
    DescriptionEntry descrEntry = new DescriptionEntry();
    descrEntry.setLOCALE("en");
    descrEntry.setVALUE(descrValue);
    return descrEntry;
  }
  
  private static PermissionEntry createPermissionElement(String permissionName, String archSetName, String permissionClassName)
  {
    if(permissionName == null)
    {
      throw new IllegalArgumentException("Name of Permission must not be \"null\"!");
    }
    if(archSetName == null)
    {
      throw new IllegalArgumentException("Name of Archiving Set must not be \"null\"!");
    }
    PermissionEntry permissionEntry = new PermissionEntry();
    permissionEntry.setCLASS(permissionClassName);
    permissionEntry.setNAME(permissionName);
    permissionEntry.setVALUE(archSetName);
    return permissionEntry;
  }
  
  private static ActionEntry createActionEntry(String actionName, String descrValue, String permissionName, String archSetName, String permissionClassName)
  {
    if(actionName == null || "".equals(actionName))
    {
      throw new IllegalArgumentException("Name of Action must not be empty!");
    }
    ActionEntry actionEntry = new ActionEntry();
    actionEntry.setDESCRIPTION(createDescriptionElement(descrValue));
    actionEntry.setPERMISSION(createPermissionElement(permissionName, archSetName, permissionClassName));
    actionEntry.setNAME(actionName);
    return actionEntry;
  }
  
  private static AssignedActionEntry createAssignedActionElement(String actionName)
  {
    if(actionName == null || "".equals(actionName))
    {
      throw new IllegalArgumentException("Name of Action must not be empty!");
    }
    AssignedActionEntry assignedActionEntry = new AssignedActionEntry();
    assignedActionEntry.setNAME(actionName);
    return assignedActionEntry;
  }
  
  private static RoleEntry createRoleElement(String roleName, String descrValue, String[] actionNames)
  {
    if(roleName == null || "".equals(roleName))
    {
      throw new IllegalArgumentException("Name of Role must not be empty!");
    }
    if(actionNames == null || actionNames.length == 0)
    {
      throw new IllegalArgumentException("There must be at least one Action that is to be assigned to the Role!");
    }
    RoleEntry roleElement = new RoleEntry();
    roleElement.setDESCRIPTION(createDescriptionElement(descrValue));
    roleElement.setNAME(roleName);
    AssignedActionEntry[] actionEntries = new AssignedActionEntry[actionNames.length];
    for(int i = 0, n = actionNames.length; i < n; i++)
    {
      actionEntries[i] = createAssignedActionElement(actionNames[i]);
    }
    roleElement.setASSIGNEDACTION(actionEntries);
    return roleElement;
  }
  
  private static ActionEntry[] createAllActionElements(String[] archSetNames, String actionNamePrefix, String permissionClassName)
  {
    ActionEntry[] actionEntries = new ActionEntry[2 + 3*archSetNames.length];
    actionEntries[0] = createActionEntry(actionNamePrefix+ACTION_OVERALL, DESCR_OVERALL, PERM_ALL, ARCHSET_ALL, permissionClassName);
    actionEntries[1] = createActionEntry(actionNamePrefix+ACTION_VIEW, DESCR_VIEW, PERM_VIEW, ARCHSET_ALL, permissionClassName);
    
    String actionName = null;
    String descr = null;
    for(int i = 0, n = archSetNames.length; i < n; i++)
    {
      // Action "archive"
      actionName = new StringBuilder(actionNamePrefix).append(ACTION_ARCHIVE).append(archSetNames[i]).toString();
      descr = new StringBuilder(DESCR_ARCHIVE).append(archSetNames[i]).toString();
      actionEntries[2+i*3] = createActionEntry(actionName, descr, PERM_ARCHIVE, archSetNames[i], permissionClassName);
      // Action "configure"
      actionName = new StringBuilder(actionNamePrefix).append(ACTION_CONFIGURE).append(archSetNames[i]).toString();
      descr = new StringBuilder(DESCR_CONFIGURE).append(archSetNames[i]).toString();
      actionEntries[3+i*3] = createActionEntry(actionName, descr, PERM_CONFIGURE, archSetNames[i], permissionClassName);
      // Action "organize"
      actionName = new StringBuilder(actionNamePrefix).append(ACTION_ORGANIZE).append(archSetNames[i]).toString();
      descr = new StringBuilder(DESCR_ORGANIZE).append(archSetNames[i]).toString();
      actionEntries[4+i*3] = createActionEntry(actionName, descr, PERM_ORGANIZE, archSetNames[i], permissionClassName);
    }
    return actionEntries;
  }
  
  private static RoleEntry[] createAllRoleElements()
  {
    String[] superArchAdminRoleActions = new String[2];
    superArchAdminRoleActions[0] = ARCHCONN_PREFIX + ACTION_VIEW;
    superArchAdminRoleActions[1] = ARCHCONN_PREFIX + ACTION_OVERALL;
    
    String[] superAdminRoleActions = new String[2];
    superAdminRoleActions[0] = ARCHCONN_PREFIX + ACTION_VIEW;
    superAdminRoleActions[1] = ARCHCONN_PREFIX + ACTION_OVERALL;
    
    String[] archReadonlyRoleActions = new String[1];
    archReadonlyRoleActions[0] = ARCHCONN_PREFIX + ACTION_VIEW;
    
    // create Role elements
    RoleEntry[] roleElements = new RoleEntry[3];
    roleElements[0] = createRoleElement(ROLE_ARCHSUPERADMIN, DESCR_ARCHSUPERADMIN, superArchAdminRoleActions);
    roleElements[1] = createRoleElement(ROLE_J2EEADMIN, ROLE_J2EEADMIN, superAdminRoleActions);
    roleElements[2] = createRoleElement(ROLE_ARCHREADONLY, DESCR_ARCHREADONLY, archReadonlyRoleActions);
    
    return roleElements;
  }
  
  private static InputStream getSerializedXML(BusinessserviceEntry bsElement) throws ActionsBuilderException
  {
    if(bsElement == null)
    {
      throw new IllegalArgumentException("XML root element \"BUSINESSSERVICE\" is missing!");
    }
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    try
    {
      XMLMarshaller marshaller = getInitializedXMLMarshaller(ActionsDescriptorBuilder.class.getClassLoader());
      QName xmlRootElement = new QName(/*"http://sap.com/tc/archtech/initservice/actions",*/ "BUSINESSSERVICE");
      return createXMLStream(marshaller, xmlRootElement, outputStream, bsElement);
    }
    catch(Throwable t)
    {
      //$JL-EXC$	    	
      cat.logThrowableT(Severity.ERROR, loc, "Serialization of \"actions.xml\" failed", t);
      throw new ActionsBuilderException(t);
    }
    finally
    {
      try
      {
        outputStream.close();
      }
      catch(IOException e)
      {
        cat.logThrowableT(Severity.WARNING, loc, "Closing the XML stream failed", e);
      }
    }
  }
  
  private static XMLMarshaller getInitializedXMLMarshaller(ClassLoader currentClassLoader) throws TypeMappingException
  {
    // open InputStream on "types.xml"
    InputStream typesXml = getXMLTypes4Marshalling(TYPES_XML_LOCATION, currentClassLoader);
    XMLMarshaller marshaller = new XMLMarshaller();
    marshaller.init(typesXml, currentClassLoader);
    return marshaller;
  }
  
  private static InputStream getXMLTypes4Marshalling(String location, ClassLoader classLoader)
  {
    InputStream typesXml = classLoader.getResourceAsStream(location);
    if(typesXml == null)
    {
      // could not find "types.xml"
      throw new AssertionError(new StringBuilder("Could not find \"types.xml\" at location \"")
          						.append(location)
          						.append("\".")
          						.toString());
    }
    return typesXml;
  }
  
  private static ByteArrayInputStream createXMLStream(XMLMarshaller marshaller, QName elementName, ByteArrayOutputStream outputStream, BusinessserviceEntry bsElement)
  throws MarshalException
  {
    outputStream.reset();
    // serialize BusinessserviceEntry to OutputStream
    marshaller.marshal(bsElement, elementName, outputStream);
    return new ByteArrayInputStream(outputStream.toByteArray());
  }
}

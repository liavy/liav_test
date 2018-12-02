package com.sap.engine.services.webservices.server.deploy.migration.ws;

/**
 * Class that contains all necessary string constants.
 * Company: Sap Labs Sofia 
 * @author aneta-a
 */
public class MigrationConstants {
    //interface mapping
	public static final String IMPLEMENTATION_ID  = "implementation-id";
	public static final String JAVACLASS_IMPLLINK = "javaclass-impllink";
	public static final String JAVA_CLASS         = "java-class";
	public static final String SERVLET_NAME         = "servlet-name";
	public static final String OMIT_PART_WRAPPER  = "omit-part-wrapper";
	public static final String SOAP_ACTION        = "soapAction";
    public static final String ATTACHMENT_PARTS   = "attachment-parts";
    public static final String CONTENT_TYPE       = "content-type";
    public static final String NAMESPACE          = "namespace";
	public static final String CONTENT_TRANSFER_ENCODING = "content-transfer-encoding";
	//NW04 data structure
	public static final String CONFIGURATION_FILE       = "configurations.xml";
	public static final String MAPPING_FILE             = "mappings.xml";
	public static final String META_INF                 = "META-INF"; 
	public static final String BINDINGS                 = "bindings";
	public static final String PORTTYPES                = "porttypes";
	public static final String TYPES                    = "types";
	public static final String WEBSERVICES_J2EE_ENGINE  = "webservices-j2ee-engine.xml";
	
  //exception constants
  public static final String UNSUCCESSFUL_MIGRATION   = "webservices_5100";//Unsuccessful migration for application {0} by WebServicesMigrator. The application can't be started.
  public static final String OPERATION_NAME_MISSING   = "webservices_5101";//Operation name is missing from operation-configuration in ws-deployment-descriptor.xml.
  public static final String SECURITY_ROLES           = "webservices_5102";//Exception occurred while trying to get security-roles properties!
  public static final String PROBLEM_LOAD_INTERFACE   = "webservices_5103";//Unable to load interface mapping data in mappings.xml.
  public static final String PROBLEM_PARAMETER_MAPPEDTYPEREFERENCE = "webservices_5104";//Problem with getting elements of element 'Parameter.MappedTypeReference'. Root clause is: {0}
  public static final String PARSING_PROBLEM          = "webservices_5105";//Parsing ws-deployment-descriptor, Vitrual Interfaces and WebService Definitions failed. The application will not be started.
  public static final String SAVE_MAPPINGSFILE        = "webservices_5106";//Saving mappings.xml file failed. The application will not be started.
  public static final String SAVE_CONFIGURATIONSFILE  = "webservices_5107";//Saving configurations.xml file failed. The application will not be started.
  public static final String SAVE_WEBSERVICESJ2EEENGINEFILE       = "webservices_5108";//Saving webservices-j2ee-engine.xml file failed. The application will not be started.
  public static final String LOAD_VIINTERFACE         = "webservices_5109";//Can't load Virtual Interface file from {0}.
  public static final String LOAD_WSD                 = "webservices_5110";//Can't load Webservice Definition file from {0}.

}


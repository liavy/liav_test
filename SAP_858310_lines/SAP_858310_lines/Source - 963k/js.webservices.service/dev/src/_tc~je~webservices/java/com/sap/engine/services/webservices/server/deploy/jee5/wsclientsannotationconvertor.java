	package com.sap.engine.services.webservices.server.deploy.jee5;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;

import javax.xml.namespace.QName;

import org.xml.sax.SAXException;

import com.sap.engine.lib.descriptors5.appclient.ApplicationClientType;
import com.sap.engine.lib.descriptors5.ejb.EjbJarType;
import com.sap.engine.lib.descriptors5.ejb.EnterpriseBeansType;
import com.sap.engine.lib.descriptors5.ejb.MessageDrivenBeanType;
import com.sap.engine.lib.descriptors5.ejb.SessionBeanType;
import com.sap.engine.lib.descriptors5.javaee.FullyQualifiedClassType;
import com.sap.engine.lib.descriptors5.javaee.JndiNameType;
import com.sap.engine.lib.descriptors5.javaee.PortComponentRefType;
import com.sap.engine.lib.descriptors5.javaee.ServiceRefType;
import com.sap.engine.lib.descriptors5.javaee.XsdAnyURIType;
import com.sap.engine.lib.descriptors5.javaee.XsdQNameType;
import com.sap.engine.lib.descriptors5.javaee.XsdStringType;
import com.sap.engine.lib.descriptors5.web.ServletType;
import com.sap.engine.lib.descriptors5.web.WebAppType;
import com.sap.engine.lib.descriptors5.web.WebAppVersionType;
import com.sap.engine.lib.processor.SchemaProcessor;
import com.sap.engine.services.webservices.webservices630.server.deploy.util.IOUtil;
import com.sap.engine.services.webservices.webservices630.server.deploy.util.jar.JarUtil;
import com.sap.lib.javalang.annotation.AnnotationRecord;
import com.sap.lib.javalang.annotation.AnnotationRecord.NamedMember;
import com.sap.lib.javalang.annotation.impl.AnnotationNamedMember;
import com.sap.lib.javalang.element.ClassInfo;
import com.sap.lib.javalang.element.FieldInfo;
import com.sap.lib.javalang.element.MethodInfo;
import com.sap.lib.javalang.file.FileInfo;
import com.sap.lib.javalang.file.FolderInfo;
import com.sap.lib.javalang.tool.ReadResult;
import com.sap.engine.lib.descriptors5.web.WebAppType.Choice1.JndiEnvironmentRefsGroup;
import com.sap.engine.lib.descriptors5.ejb.*;


/**
 * Title: NewWSClientsAnnotationConvertor 
 * Description: Converts @WebServiiceRef and @WebServiceClient annotations into <service-ref> descriptor 
 * adding  it in the corresponding description file of the client (web application client, ejb module ot web module) 
 * in order to use the j2ee converter after words   
 * 
 * @author Yulkyar Myudzhelit
 * @version
 */
public class WSClientsAnnotationConvertor {
	private String tmpPath;
	private String archiveName ;
    private ReadResult parsedAnnotations;
    private Map<String, List<AnnotationRecord>> classLevelAnnots = null;
    private String moduleType ;
    
    private static final String META_INF = "META-INF";
	private static final String WEB_INF = "WEB-INF";
	private static final String WS_CLIENTS_DEPLOYMENT_DESCRIPTORS_DIR = "ws-clients-descriptors";
	private static final String LIB = "lib";
	private static final String GEN_OBJECT = "java.lang.Object";
    // Annotations
    private static final String WEB_SERVICE_REF = "javax.xml.ws.WebServiceRef";
    private static final String WEB_SERVICE_REFS = "javax.xml.ws.WebServiceRefs";
	private static final String WEB_SERVICE_CLIENT = "javax.xml.ws.WebServiceClient";
	private final static String STATELESS = "javax.ejb.Stateless";  
	private final static String STATEFUL =  "javax.ejb.Stateful";  
	private final static String ENTITY =  "javax.ejb.Entity";
	private final static String MESSAGE_DRIVEN =  "javax.ejb.MessageDriven";
	private static final String ISERVICE = "javax.xml.ws.Service";
	// ejb type
	private final static String SESSION_EJB =  "Session";  
	private final static String MSGDR_EJB =  "MessageDriven";  
	private final static String ENTITY_EJB =  "Entity";
	// module type
	private static final String SERVLET_CLIENT = "servlet";
	private static final String EJB_CLIENT = "ejb";	
	private static final String APP_CLIENT = "appClient";	
	// descriptor names
	private static final String WEB_XML_FILE = "web.xml";
	private static final String EJB_JAR_FILE = "ejb-jar.xml";
	private static final String APP_CLIENT_FILE = "application-client.xml";
	// descriptor versions
	private static final String WEB_SERVICE_WEB_APP_VERSION = "2.5";
	private static final String WEB_SERVICE_EJB_VERSION = "3.0";
	private static final String APPCLIENT_VERSION = "5";
	// Deployment descriptors 
    private com.sap.engine.lib.descriptors5.web.WebAppType webXmlType;
    private com.sap.engine.lib.descriptors5.ejb.EjbJarType ejbJarXmlType;
    private com.sap.engine.lib.descriptors5.appclient.ApplicationClientType appClientType;
    // module processor
    private SchemaProcessor descriptorProcessor5;
    // to store the collected clients
    private Hashtable<String, Hashtable<String, ServiceRefType>> moduleServRefs;
    private Hashtable<String, EnterpriseBeansType.Choice1> ejbChoice1Elements; 
    private String ALL_IN_ONE = "allinone";
    
    ArrayList <String> incorrectCTSWS = new ArrayList<String>();
    {
    	incorrectCTSWS.add("WSEjbOverrideWSRefHCWithDDsTestClnt_client.jar");
    	incorrectCTSWS.add("WSEjbOverrideWSRefHCWithDDsTestClnt_web.war");
    	incorrectCTSWS.add("WSEjbOverrideWSRefWithDDsTestClnt_client.jar");
    }
	/**
     * NewWSClientsAnnotationConvertor constructor
     * 
     * @param tmpPath - path to war/jar archives location
     * @param parsedAnnotations - ReadResult data   
     * @param archiveName - archive name
     */	
	public WSClientsAnnotationConvertor(String tmpPath, String archiveName, ReadResult parsedAnnotations){
		this.tmpPath = tmpPath;
		this.parsedAnnotations = parsedAnnotations;
		this.archiveName = archiveName;
		this.moduleServRefs = new Hashtable<String, Hashtable<String, ServiceRefType>>();
	}
	
	/**
     * Converts the WS client annotations do deployment descriptors
     * 
     * @throws Exception 
     * 
     * @param tmpPath - path to war/jar archives location
     * @param parsedAnnotations - ReadResult data   
     * @param archiveName - archive name
	 * @throws WSClientAnnotConversionException, Exception 
     */
	public void convertWSClientAnnotations() throws ConversionException {
		if(!isEligibleForConversion()) {
			return;
		}
		// check the client type
		determineModuleType();
		// deserialize the web.xml file
		initializeDescriptor(archiveName, moduleType);
		try {
			// - check archive type
			if(moduleType.equals(SERVLET_CLIENT)){
				// store wsRefs in a hashmap
				// - find WSRef annotations 
				// - combine with WebServcieClient
				// - for each couple create 	
				collectServletWSRef();
				
				// do not process if no WebServiceRef /WebServiceRefs annotations found
				if(moduleServRefs == null ||moduleServRefs.size() == 0){
					return;
				}
				
				// - add the service-ref descriptor in the web.xml
				addClientsToWebDescriptor();
				
				// - add the descriptor to the original archive
				buildJar(webXmlType, WEB_XML_FILE, tmpPath, archiveName, IOUtil.getFileNameWithoutExt(archiveName) + "_wscl" + IOUtil.getFileExtension(archiveName));
				
			} else if(moduleType.equals(EJB_CLIENT)){
				collectEjbWSRef();
				
				// do not process if no WebServiceRef /WebServiceRefs annotations found
				if(moduleServRefs == null ||moduleServRefs.size() == 0){
					return;
				}
				
				// - add the service-ref descriptor in the web.xml
				addClientsToEjbDesriptor();
				
				// - add the descriptor to the original archive
				buildJar(ejbJarXmlType,EJB_JAR_FILE, tmpPath, archiveName, IOUtil.getFileNameWithoutExt(archiveName) + "_wscl" + IOUtil.getFileExtension(archiveName));
			} else {
				collectWSRef();
				
				// do not process if no WebServiceRef /WebServiceRefs annotations found
				if(moduleServRefs == null ||moduleServRefs.size() == 0){
					return;
				}
				
				// - add the service-ref descriptor in the application_client.xml
				addClientsToAppClientDescriptor();
				
				// - add the descriptor to the original archive
				buildJar(appClientType,APP_CLIENT_FILE, tmpPath, archiveName, IOUtil.getFileNameWithoutExt(archiveName) + "_wscl" + IOUtil.getFileExtension(archiveName));
			}
		}  catch (IOException e) {
			throw new ConversionException("webservices_5206", new String[]{archiveName, moduleType}, e );
		} catch (Exception e) {
			throw new ConversionException("webservices_5200", new String[]{archiveName, moduleType}, e );
		}
	}
	
	/**
	 * Verifies if the module:
	 * - is in the list of processed files of the ReadResult of the deployed application 
	 * - has any @WebServiceClient annotations
	 * 
	 * @return boolean isEligible  
	 */
	private boolean isEligibleForConversion(){
		boolean isConversionEligible = false;
		FileInfo[] applFolders = parsedAnnotations.getProcessedFiles();
		FolderInfo rootAppFolder = null;
		FileInfo [] processedFilesList = null;
		
		// Only root files and folders are presented in the returned array
		// so we get the ear/sda root folder in order to get its processed files
		if (applFolders[0] !=  null) {
			rootAppFolder = (FolderInfo) applFolders[0];
			processedFilesList = rootAppFolder.getFiles();
		}
		Map<String, List<AnnotationRecord>> annotations = null; 
		FileInfo curFile = null; 
		String curFileName =  null;
		if (processedFilesList != null && processedFilesList.length > 0 ) {
			for (int i = 0; i < processedFilesList.length; i++) {
				curFile = processedFilesList[i];
				curFileName = curFile.getName();
				
				//Check only if war or jar
				if(!archiveName.endsWith(".jar") && ! archiveName.endsWith(".war")) {
					continue;
				}	
				
				// Check if the module is in the list 
				// of processed files of the ReadResult of the deployed application
				if(archiveName.endsWith(".jar")) {
					if(!archiveName.equals(curFileName)) {
						continue;
					}
				}	
				if(archiveName.endsWith(".war")) {
			      int extIndex = archiveName.lastIndexOf(".war");
			      String moduleName2  = archiveName.substring(0, extIndex) + ".extracted.war";
			      if(!moduleName2.equals(curFileName)) {
			        continue;
			      }
			    }
				
				// Check if the module has any @WebServiceClient annotations
				annotations = curFile.getClassLevelAnnotations();
			
		    	if(annotations != null && !annotations.isEmpty() && annotations.get(WEB_SERVICE_CLIENT) != null) {
		    		// save class level annotations for later reuse;
		    		classLevelAnnots = annotations;
		    		
		    		isConversionEligible = true;
		    	}		
		    	break;
			}
		}
		
		return isConversionEligible;
	}
	
	private void determineModuleType() throws ConversionException{
		if(archiveName.endsWith(".war")){
			moduleType = SERVLET_CLIENT;
		} else if( archiveName.endsWith(".jar")) {
		    if(classLevelAnnots.get(STATELESS) != null 
		    		|| classLevelAnnots.get(STATEFUL) != null 
		    		|| classLevelAnnots.get(MESSAGE_DRIVEN) != null 
		    		|| classLevelAnnots.get(ENTITY) != null) {
		    	moduleType = EJB_CLIENT;
		    } else {
		    	JarFile archive = null;
		    	
		    	try {
					archive = new JarFile( tmpPath + File.separator + archiveName );
					if(archive.getEntry(META_INF + "/" + EJB_JAR_FILE) != null) {
			    		moduleType = EJB_CLIENT;
			    	} else {
				    	moduleType = APP_CLIENT;
				    }
				} catch (IOException e) {
					throw new ConversionException("webservices_5201", new String[]{tmpPath + File.separator + archiveName, archiveName}, e );
				} finally {
					try {
						if(archive != null)
							archive.close();	
					} catch (IOException e) {
						// $JL-EXC$
					}
				}	
		    }
		}
	}
	
	/**
	 * Deserializes the corresponding deployment descriptor if exists
	 * 
	 * @param archiveName 
	 * @throws ConversionException 
	 */
	
	private void initializeDescriptor( String archiveName, String clientType) throws ConversionException {
		// deserialize web descriptor
		JarFile archive = null;
		ZipEntry deployDescr =  null;
		InputStream webServiceEntryAsInputStreamTmp = null;
		
		try {
			archive = new JarFile( tmpPath + File.separator + archiveName );
		} catch (IOException e) {
			throw new ConversionException("webservices_5201", new String[]{tmpPath + File.separator + archiveName, archiveName }, e );
		}
		try{
			// descriptor depending on the archive
			Hashtable<String, ServiceRefType[]> wsRefsHT = null;
			if(clientType.equals(SERVLET_CLIENT)){
				this.descriptorProcessor5 = com.sap.engine.lib.processor.impl.WebProcessor5.getInstance();
				deployDescr = archive.getEntry(WEB_INF + "/" + WEB_XML_FILE);
				
				if (deployDescr != null) {
					webServiceEntryAsInputStreamTmp = archive.getInputStream(deployDescr); 
			        webXmlType = (WebAppType) descriptorProcessor5 .parse(webServiceEntryAsInputStreamTmp);
			        wsRefsHT = getServiceRefs(webXmlType);
				} else {
					webXmlType =  new com.sap.engine.lib.descriptors5.web.WebAppType();
					WebAppVersionType webAppVersionType = new WebAppVersionType(WEB_SERVICE_WEB_APP_VERSION);
					webXmlType.setVersion(webAppVersionType);
				}
				
			} else if (clientType.equals(EJB_CLIENT)){
				this.descriptorProcessor5  = com.sap.engine.lib.processor.impl.EjbProcessor5.getInstance();
				deployDescr = archive.getEntry(META_INF + "/" + EJB_JAR_FILE);
				
				if (deployDescr != null) {
					webServiceEntryAsInputStreamTmp = archive.getInputStream(deployDescr); 
					ejbJarXmlType = (EjbJarType)descriptorProcessor5.parse(webServiceEntryAsInputStreamTmp);
					wsRefsHT = getServiceRefs(ejbJarXmlType);
				} else {
					ejbJarXmlType =  new EjbJarType();
					ejbJarXmlType.setVersion(WEB_SERVICE_EJB_VERSION);
				}
			} else {
				this.descriptorProcessor5  = com.sap.engine.lib.processor.impl.AppclientProcessor5.getInstance();
				deployDescr = archive.getEntry(META_INF + "/" + APP_CLIENT_FILE);
				
				if (deployDescr != null) {
					webServiceEntryAsInputStreamTmp = archive.getInputStream(deployDescr); 
					appClientType = (ApplicationClientType)descriptorProcessor5.parse(webServiceEntryAsInputStreamTmp);
					wsRefsHT = getServiceRefs(appClientType);
				} else {
					appClientType =  new ApplicationClientType();
					appClientType.setVersion(APPCLIENT_VERSION);
				}
			}
			
			// check for duplicate service ref jndi names
			String duplicatingJndiName = null;
			duplicatingJndiName = hasDuplicateServiceRefNames(wsRefsHT);
			if( duplicatingJndiName!= null){
				throw new ConversionException("webservices_5210", new String[]{duplicatingJndiName, deployDescr.getName(),archiveName, moduleType}, null);
			}
		} catch (IOException e) {
			throw new ConversionException("webservices_5204", new String[]{archiveName, moduleType}, e );
		} catch (SAXException e) {
			throw new ConversionException("webservices_5205", new String[]{deployDescr.getName(), archiveName, moduleType}, e );
		} finally {
			try {
				if(webServiceEntryAsInputStreamTmp != null)
					webServiceEntryAsInputStreamTmp.close();
			} catch (IOException e) {
				// $JL-EXC$
			}		
			try {
				if(archive != null)
					archive.close();	
			} catch (IOException e) {
				// $JL-EXC$
			}
		}
	}
	/**
	 * Gets the service refs from WebAppType decriptor as a hashtable:
	 * HashTable:
	 * 		key: (String)archiveName
	 * 		value: HashTable :
	 * 					key: (String)servRefName 
	 * 					value: (ServiceRefType) service-ref object
	 * @param dDescr
	 * @return
	 */
	private Hashtable<String, ServiceRefType[]> getServiceRefs(WebAppType dDescr){
		if(dDescr == null ){
			return null;
		}
		WebAppType.Choice1[] choiseArr = dDescr.getChoiceGroup1();
		if (choiseArr != null) {
			for (WebAppType.Choice1 choice1 : choiseArr) {
				JndiEnvironmentRefsGroup jndiRefsGr = choice1.getJndiEnvironmentRefsGroupGroup();
				if (jndiRefsGr != null) {
					 ServiceRefType[] serviceRefArr =  jndiRefsGr.getServiceRef();
					if(serviceRefArr != null) {
						Hashtable<String, ServiceRefType[]> serviceRefsTable = new Hashtable<String, ServiceRefType[]>();
						serviceRefsTable.put(archiveName, serviceRefArr);
						return serviceRefsTable;
					}
				} 
			}
		}
		return null;
	}
	
	/**
	 * Gets the service refs from EjbJarType decriptor as a hashtable:
	 * HashTable:
	 * 		key: (String)beanName
	 * 		value: HashTable :
	 * 					key: (String)servRefName 
	 * 					value: (ServiceRefType) service-ref object
	 * @param dDescr
	 * @return
	 */
	private Hashtable<String, ServiceRefType[]> getServiceRefs(EjbJarType dDescr){
		if(dDescr == null ){
			return null;
		}
		EnterpriseBeansType enterpriseBeans = dDescr.getEnterpriseBeans();
		
		if(enterpriseBeans == null){
			return null;
		}
		EnterpriseBeansType.Choice1[] choises =  enterpriseBeans.getChoiceGroup1();
		
		if (choises != null) {
			Hashtable<String, ServiceRefType[]> serviceRefsTable = new Hashtable<String, ServiceRefType[]>();
			for (EnterpriseBeansType.Choice1 choice1 : choises) {
				 if(choice1 == null) {
					  continue;
				 }
				 SessionBeanType sessionB = choice1.getSession();
				 if(sessionB != null){
					 ServiceRefType[] serviceRefArr =  sessionB.getServiceRef();
					 if (serviceRefArr != null && serviceRefArr.length > 0) {
						 serviceRefsTable.put(sessionB.getEjbName().get_value(), serviceRefArr);
					 }
					 continue;
				 }
				 
				 MessageDrivenBeanType messageDrivenB = choice1.getMessageDriven();
				 if(messageDrivenB != null){
					 ServiceRefType[] serviceRefArr =  messageDrivenB.getServiceRef();
					 
					 if (serviceRefArr != null && serviceRefArr.length > 0) {
						 serviceRefsTable.put(messageDrivenB.getEjbName().get_value(), serviceRefArr);
					 }
				 } 
			}
			if(serviceRefsTable.size() > 0 ){
				return serviceRefsTable;
			}
		}
		return null;
	}
	
	/**
	 * Gets the service refs from ApplicationClientType decriptor as a hashtable:
	 * HashTable:
	 * 		key: (String)archiveName
	 * 		value: HashTable :
	 * 					key: (String)servRefName 
	 * 					value: (ServiceRefType) service-ref object
	 * @param dDescr
	 * @return
	 */
	private  Hashtable<String, ServiceRefType[]> getServiceRefs(ApplicationClientType dDescr){
		if(dDescr == null ){
			return null;
		}
		ServiceRefType[] serviceRefArr = dDescr.getServiceRef();
		if(serviceRefArr != null) {
			Hashtable<String, ServiceRefType[]> serviceRefsTable = new Hashtable<String, ServiceRefType[]>();
			serviceRefsTable.put(archiveName, serviceRefArr);
			return serviceRefsTable;
		}
		return null;
	}
	
	/**
	 * Checks if the initial DD has duplicate service ref names
	 * 
	 * @param serviceRefHT
	 * @return hasDuplicateName  
	 */
	private String hasDuplicateServiceRefNames(Hashtable<String,ServiceRefType[]> serviceRefHT) {
		ServiceRefType[] serviceRefArr = null;
		if(serviceRefHT != null) {
			for (Iterator<String> iterator = serviceRefHT.keySet().iterator(); iterator.hasNext();) {
				String container = iterator.next();
				serviceRefArr = serviceRefHT.get(container);
				if (serviceRefArr !=  null ) {
					ArrayList<String> jndiNames = new ArrayList<String>();
					for (int i = 0; i < serviceRefArr.length; i++) {
						JndiNameType jndiName = serviceRefArr[i].getServiceRefName();
						String jndiStrName = jndiName.get_value();
						if(jndiNames.indexOf(jndiStrName)< 0 ) {
							jndiNames.add(jndiStrName);
						} else {
							return jndiStrName;
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 *  store the service ref <code>srT</code> in the <code>moduleServRefs</code> structure 
	 *  in order to be added later in DD
	 * 
	 * @param servRefName
	 * @param srT
	 * @param groupName bean name if ejb client, else null
	 * @throws ConversionException f the service ref name is duplicated
	 */
	private void storeServiceRef(String servRefName, ServiceRefType srT, String groupName) throws ConversionException{
		if (groupName == null) {
			groupName = ALL_IN_ONE;
		}
		Hashtable<String, ServiceRefType> servRefArr = null;
		servRefArr = moduleServRefs.get(groupName);
		if(servRefArr == null){
			servRefArr = new Hashtable<String, ServiceRefType>();
			moduleServRefs.put(groupName, servRefArr);
		}
		if(servRefArr.get(servRefName) == null){
			servRefArr.put(servRefName, srT);
		} else {
			throw new ConversionException("webservices_5211", new String[]{servRefName, archiveName, moduleType}, null);
		}
	}
	
	private void getWSRefsAnnotOfClass(ClassInfo ci, String beanName) throws ConversionException{
		AnnotationRecord wsRefs = ci.getAnnotation(WEB_SERVICE_REFS);
		if (wsRefs != null) {
			NamedMember valueMember =  wsRefs.getMember("value");
			AnnotationNamedMember[] wsRefAnnotMembersArr = (AnnotationNamedMember[]) valueMember.getMemberArrayValue();
			ClassInfo clClass = null;
			for(int s = 0; s < wsRefAnnotMembersArr.length; s ++){
				AnnotationRecord wsRefAnnot = (AnnotationRecord) wsRefAnnotMembersArr[s].getMemberValue();
				clClass = parsedAnnotations.getClass(wsRefAnnot.getMember("type").getStringValue());
				
				// not found or not in the currant archive
				if (clClass == null || !isWSClientInArchive(clClass)) {
					continue;
				}
				AnnotationRecord wsClientAnn = (AnnotationRecord)clClass.getAnnotation(WEB_SERVICE_CLIENT);
				// No WebServiceClient annotation found for the Service
				if(wsClientAnn == null) {
					continue;
				}
				ServiceRefType srT;
				srT = constructServiceRefDesc(wsRefAnnot, wsClientAnn, wsRefAnnot.getMember("type").getStringValue());
				storeServiceRef(wsRefAnnot.getMember("name").getStringValue(), srT, beanName);
			}
		}
	}
	
	private void getWSRefAnnotOfClass(ClassInfo ci, String beanName) throws ConversionException {
		FieldInfo[] fields = ci.getFields();
		ClassInfo clientClass = null;
		for (int i = 0; i < fields.length; i++) {
			FieldInfo fi = fields[i]; 
			AnnotationRecord wsRefAnn = null;
			if ( (wsRefAnn = fi.getAnnotation(WEB_SERVICE_REF)) != null) {
				
				// found a WebServiceRef
				String wsRefName = null;
				String wsType = fi.getType();
				NamedMember wsRefNameMember = wsRefAnn.getMember("name");
				if(wsRefNameMember != null){
					wsRefName = wsRefNameMember.getStringValue();
				} else {
					// FIXME Choose a better name, discuss with Dida
					wsRefName = ci.getName();
				}
				 
				// get the corresponding WebServiceClient 
				// which is located in the Service class 
				// (given by "value" property of @WebServiceRef or the java class type of the annotated member) 
				NamedMember serviceClassMember = wsRefAnn.getMember("value");
				String serviceClass = null;
				if (serviceClassMember != null && !(serviceClassMember.getStringValue().equals(GEN_OBJECT)) && !serviceClassMember.getStringValue().equals(ISERVICE)) {
					serviceClass = serviceClassMember.getStringValue();
				} else {
					serviceClass = wsType; 
				}
				clientClass = parsedAnnotations.getClass(serviceClass);
				
				// not found or not in the currant archive
				if (clientClass == null || !isWSClientInArchive(clientClass)) {
					continue;
				}
				AnnotationRecord wsClientAnn = (AnnotationRecord)clientClass.getAnnotation(WEB_SERVICE_CLIENT) ;
				// No WebServiceClient annotation found for the Service
				if(wsClientAnn == null) {
					continue;
				}
				ServiceRefType srT;
				srT = constructServiceRefDesc(wsRefAnn, wsClientAnn, fi.getType());
				storeServiceRef(wsRefName, srT, beanName);
			}
		}
	}
	
	/**
	 * check if the WebServiceClient annotation is in the same module
	 */
	private boolean isWSClientInArchive (ClassInfo ci){
		boolean isWSClientInArchive = false;
		
		List<AnnotationRecord> moduleWsClients =  classLevelAnnots.get(WEB_SERVICE_CLIENT);
		for (Iterator<AnnotationRecord> iterator = moduleWsClients.iterator(); iterator.hasNext();) {
			AnnotationRecord clAnnot = iterator.next();
			String javaType = ((ClassInfo)clAnnot.getOwner()).getName();
			
			if(javaType.equals(ci.getName())){
				isWSClientInArchive = true;
				break;
			}
		}
		return isWSClientInArchive;
	}
	
	/**
	 *  Finds the @WEbServiceRef annotations 
	 *  and add the constructed <code>ServiceRefType</code> corresponding to the annotation
	 *  to the <code>moduleServRefs</code>   
	 * 
	 * @param ci
	 * @param beanName
	 * @throws ConversionException 
	 */
	private void collectServRefDescOfClass (ClassInfo ci,  String beanName) throws ConversionException {
		
		if(ci == null || ci.getName().equals(Object.class.getName())){
			return ;
		}
		
		// check for @WebServiceRefs 
		getWSRefsAnnotOfClass(ci, beanName);
		
		// check for @WebServiceRef 
		getWSRefAnnotOfClass(ci, beanName);
		
		// check the ancestor classes for @WebServicesRef/@WebServicesRefs annotations
		ClassInfo pci = ci.getSuperclass();
		collectServRefDescOfClass(pci, beanName);
	}
	
	/**
	 *  Prepare ServiceRefType
	 *  
	 *  @param wsRefAnn AnnotationRecord for the @WebServiceRef annotation 
	 *  @param wsClientAnn AnnotationRecord for the @WebServiceRef annotation
	 *  @throws ConversionException 
	 *  
	 */
	private ServiceRefType constructServiceRefDesc (AnnotationRecord wsRefAnn, AnnotationRecord wsClientAnn, String fieldClassName) throws ConversionException{
		
		// create ServiceRefType
		ServiceRefType srT = new ServiceRefType();
		
		// Set service-ref-name
		JndiNameType jndiName = new JndiNameType();
		NamedMember wsRefNameMember = wsRefAnn.getMember("name");
		String wsRefName; 
		if(wsRefNameMember != null){
			wsRefName = wsRefNameMember.getStringValue();
		} else {
			wsRefName = fieldClassName;
		}
		 
		jndiName.set_value(wsRefName);
		srT.setServiceRefName(jndiName);
		
		// set service-ref-type
		// @WebServiceRef.type (by spec)
		NamedMember refType = wsRefAnn.getMember("type");
		if( refType != null) {
			com.sap.engine.lib.descriptors5.javaee.FullyQualifiedClassType servRefType = new com.sap.engine.lib.descriptors5.javaee.FullyQualifiedClassType();
			
			if( !refType.getStringValue().equals(GEN_OBJECT) && !refType.getStringValue().equals(ISERVICE)){
				servRefType.set_value(refType.getStringValue());
			} else {
				servRefType.set_value(fieldClassName);
			}
			
			srT.setServiceRefType(servRefType);
		} 
		
		// Set service-interface
		NamedMember refValue = wsRefAnn.getMember("value");
		String servInterfaceClass = null;
		
		// CTS webservices12\ejb\descriptors\WSEjbOverrideWSRefWithDDsTest
		// webservices12\ejb\descriptors\WSEjbOverrideWSRefHCWithDDs are not correct: 
		// injects service but both 'type' and 'value' are given
		// -> special treatment
		
		if (refValue != null && !incorrectCTSWS.contains(archiveName)) {
			// @WebServiceRef.value
			servInterfaceClass =  refValue.getStringValue();
		} else if(refType != null && !refType.getStringValue().equals(GEN_OBJECT) && !refType.getStringValue().equals(ISERVICE)){
			// OR
			// @WebServiceRef.type if @WebServiceRef.value not specified
			servInterfaceClass = refType.getStringValue();
		} else {
			// OR
			// get the classtype of the member field
			servInterfaceClass = fieldClassName;
		}
		com.sap.engine.lib.descriptors5.javaee.FullyQualifiedClassType fullyClassType = new com.sap.engine.lib.descriptors5.javaee.FullyQualifiedClassType();
		fullyClassType.set_value(servInterfaceClass);
		srT.setServiceInterface(fullyClassType);
		
		// set port-component-ref > service-endpoint-interface type
		// when @WebServiceRef.value is specified 
		if (refValue != null && refType != null && !incorrectCTSWS.contains(archiveName)) {
			PortComponentRefType port = new PortComponentRefType();
			FullyQualifiedClassType seiClass = new FullyQualifiedClassType();
			seiClass.set_value(refType.getStringValue());
			port.setServiceEndpointInterface(seiClass);
			com.sap.engine.lib.descriptors5.javaee.PortComponentRefType[] portArr = new PortComponentRefType[]{port};
			srT.setPortComponentRef(portArr);
		} 
		
		// Set wsdl-file
		NamedMember wsdlLocation = wsRefAnn.getMember("wsdlLocation");
		if (wsdlLocation == null) {
			wsdlLocation = wsClientAnn.getMember("wsdlLocation");
		}
		if (wsdlLocation != null) {
			String wsdl = null;
			try { 
				
				wsdl = wsdlLocation.getStringValue();
				XsdAnyURIType xsdA = new XsdAnyURIType();
				URI uri = null;
				uri = new URI(wsdl);
				xsdA.set_value(uri);
				srT.setWsdlFile(xsdA);
			} catch (URISyntaxException e) {
				throw new ConversionException("webservices_5202", new String[] {wsdl, wsRefName}, e);
			}
		} 
		
		// set mappedName if any 
		NamedMember mappedName = wsRefAnn.getMember("mappedName");
		if (mappedName != null) {
			XsdStringType xsdMapped = new XsdStringType();
			xsdMapped.set_value(mappedName.getStringValue());
			srT.setMappedName(xsdMapped);
		}
		
		// set QName
		XsdQNameType xsdQName = new XsdQNameType();
		NamedMember tNamespace =  wsClientAnn.getMember("targetNamespace");
		String strNamespace = "";
		if (tNamespace != null) {
			strNamespace =  tNamespace.getStringValue();
		} 
		QName qName = new QName(strNamespace, wsClientAnn.getMember("name").getStringValue());
		xsdQName.set_value(qName);
		srT.setServiceQname(xsdQName);
		
		return srT;
	}
	
	/**
	 * Collects the service-ref info from all the servlet classes 
	 * declared in the web.xml of the web module
	 * and stock them in <code>moduleServRefs</code> table 
	 * 
	 * @throws ConversionException
	 */
	private void collectServletWSRef() throws ConversionException{

		// get servlet classes from web.xml if any
		WebAppType.Choice1[] choises = webXmlType.getChoiceGroup1();
		
		if (choises  == null || choises.length <= 0) {
			return;
		}
		ArrayList<ClassInfo> servletClasses = new ArrayList<ClassInfo>();
		ClassInfo ci = null;
		
		for (WebAppType.Choice1 choice : choises) {
			ServletType servlet = choice.getServlet();
			if(servlet != null) {
				ServletType.Choice1 servletOrJsp = servlet.getChoiceGroup1();
				FullyQualifiedClassType fullClNameObj = servletOrJsp.getServletClass();
				if (fullClNameObj != null) {
					String fullClName = fullClNameObj.get_value();
					ci =  parsedAnnotations.getClass(fullClName);
					servletClasses.add(ci);
				}
			}
		}
		
		if(servletClasses.size() <= 0) {
			return;
		}
		
		// check each of servlet classes from web.xml for service ref
		for (Iterator<ClassInfo> iterator = servletClasses.iterator(); iterator.hasNext();) {
			ClassInfo servletClass = iterator.next();
			// search WSRef Annotatations in currant class 
			// and add it to the WebServiceRef collection
			collectServRefDescOfClass(servletClass, null );
		}
	}

	/**
	 * Collects the service-ref info from all the class files in the archive
	 * and stock them in <code>moduleServRefs</code> table 
	 * 
	 * @throws ConversionException 
	 */
	private void collectWSRef() throws ConversionException{
		Map<String,ClassInfo> allClassesMap = parsedAnnotations.getClasses();
		Collection<ClassInfo> allClasses = allClassesMap.values();
		
		// search in every class and store the classes need to be checked for 
		// WebServiceRef/WebServiceRefs annotations
		ArrayList<ClassInfo> checkClassList = new ArrayList<ClassInfo> ();
		for (Iterator<ClassInfo> iterator = allClasses.iterator(); iterator.hasNext();) {
			ClassInfo ci = iterator.next();
			String className = ci.getName();
			
			//filter classes in the current module
			File archive = new File( tmpPath + File.separator + archiveName );
			
			// for appclient modules only
			// check if the class contains main method
			if(moduleType.equals(APP_CLIENT))
			{
				if(!isAppClientClass(ci))
				{
					continue;
				}
			}
			// in the processed module
			if (!inArchive(archive, className)) {
				continue;
			}
			
			if(false){
				continue;
			}
			checkClassList.add(ci);
		}
		for (Iterator<ClassInfo> iterator = checkClassList.iterator(); iterator.hasNext();) {
			ClassInfo classToCheck =  iterator.next();
			
			// search WSRef Annotation at field level of currant class 
			// and add it to the WebServiceRef collection
			collectServRefDescOfClass(classToCheck, null );
		}
	}
	
	/**
	 * Collects the service-ref info from the classes annotated with kind of ejb annotaion
	 * ( @Stateful, @Stateless, @Message, @Entity)
	 * and stock them in <code>moduleServRefs</code> table 
	 * @throws ConversionException 
s	 */
	private void collectEjbWSRef() throws ConversionException {
		
		// Process EJBs declared in dd only
		processEjbInDD();
		
		String[] validEjbAnnots = new String[] {STATELESS, STATEFUL, MESSAGE_DRIVEN};
		ClassInfo ci = null;
		
		// check the ejbs with @Stateless, @Statefull,@MessageDriven annotations, without description in the ejb-jar.xml
		for (int i = 0; i < validEjbAnnots.length; i++) {
			List<AnnotationRecord> ejbAnnotsList = classLevelAnnots.get(validEjbAnnots[i]);
			if (ejbAnnotsList != null) {
				for (AnnotationRecord curAnnot : ejbAnnotsList) {
					String javaType = ((ClassInfo)curAnnot.getOwner()).getName();
					
					NamedMember beanNameMember = curAnnot.getMember("name");
					String beanName= null;
					if (beanNameMember != null) {
						beanName = beanNameMember.getStringValue();
					} else {
						int extIndex = javaType.lastIndexOf(".");
						beanName = javaType.substring(extIndex +1);;
					} 
					
					// search WSRef/WSRefs Annotation for the currant class 
					// and add it to the WebServiceRef collection
					ci = parsedAnnotations.getClass(javaType);
					collectServRefDescOfClass(ci, beanName);
				}
			}
		}
	}
	
	/**
	 * Process EJBs declared in ejb-jar.xml only
	 * 
	 * @throws ConversionException
	 */
	private void processEjbInDD () throws ConversionException{
		ClassInfo ci = null;
		EnterpriseBeansType enterprBeans = null;
		enterprBeans = ejbJarXmlType.getEnterpriseBeans();
		Hashtable<String, String> ejbInDD = new Hashtable<String, String>();
		if( enterprBeans != null){
			EnterpriseBeansType.Choice1[] eBChoise = enterprBeans.getChoiceGroup1();
			EjbClassType ejbCl = null;
			EjbNameType ejbName = null;
			for (EnterpriseBeansType.Choice1 choise : eBChoise) {
				SessionBeanType sessBean = choise.getSession();
				MessageDrivenBeanType msgDrBean = choise.getMessageDriven();
				if(sessBean != null) {
					ejbCl = sessBean.getEjbClass();
					ejbName = sessBean.getEjbName();
				} else if(msgDrBean != null) {
					ejbCl = msgDrBean.getEjbClass();
					ejbName = msgDrBean.getEjbName();
				}
				if(ejbCl != null && ejbName != null){
					ejbInDD.put(ejbCl.get_value(), ejbName.get_value());
				}
			}
			// check ejb classes declared in the ejb-jar.xml and not annotated
			for (Iterator<String> iterator = ejbInDD.keySet().iterator(); iterator.hasNext();) {
				String ejbClName = iterator.next();
				ci = parsedAnnotations.getClass(ejbClName);
				
				if(ci.getAnnotation(STATEFUL) == null && ci.getAnnotation(STATELESS) == null && ci.getAnnotation(MESSAGE_DRIVEN) == null){
					collectServRefDescOfClass(ci, ejbInDD.get(ejbClName));
				}
			}
		}
	}
	
	/**
	 * Constructs a Choice1 type object to use in WebXmlFile construction
	 * 
	 * @param servRefs
	 * @return wsRefsChoice WebAppType.Choice1 ready to be set to the WebXmlFile
	 */
	private WebAppType.Choice1 prepareWebChoice1( Hashtable<String, ServiceRefType> servRefs){
		
		WebAppType.Choice1 wsRefsChoice = new WebAppType.Choice1();
		
		JndiEnvironmentRefsGroup jndiEnvironmentRefsGroup = new JndiEnvironmentRefsGroup();
		
		ServiceRefType[] serviceRefs = new ServiceRefType[servRefs.size()];
		
		int i = 0;
		for (Iterator<ServiceRefType> iterator = servRefs.values().iterator(); iterator.hasNext() ;) {
			ServiceRefType serviceRef = (ServiceRefType) iterator.next();		
			serviceRefs[i] = serviceRef;
			i++;
		}
		jndiEnvironmentRefsGroup.setServiceRef(serviceRefs);
		wsRefsChoice.setJndiEnvironmentRefsGroupGroup(jndiEnvironmentRefsGroup);
		return wsRefsChoice;
	}
	
	/**
	 * Constructs the EjbChoice1 elements to use while constructing the EjbJarType 
	 * 
	 * @param beanName String
	 * @param beanType String
	 * @param srTArr ServiceRefType[] 
	 * @param javaClass String
	 */
	private void prepareEjbChoice1Elems(String beanName, String beanType, ServiceRefType[] srTArr, String javaClass){
		EnterpriseBeansType.Choice1 choiceElem = null;
		if (ejbChoice1Elements == null) {
			ejbChoice1Elements = new Hashtable<String, EnterpriseBeansType.Choice1>(); 
		}
		if(!ejbChoice1Elements.containsKey(beanName)){
			choiceElem = new EnterpriseBeansType.Choice1();
			ejbChoice1Elements.put(beanName, choiceElem);
		}
		// check ejb type 
		if (SESSION_EJB.equals(beanType)) {
			SessionBeanType sessionEjb = new SessionBeanType();
			//set ejb name
			EjbNameType beanNmT = new EjbNameType();
			beanNmT.set_value(beanName);
			sessionEjb.setEjbName(beanNmT);
			choiceElem.setSession(sessionEjb);
			
			// set ejb class
			EjbClassType beanClT = new EjbClassType();
			beanClT.set_value(javaClass);
			sessionEjb.setEjbClass(beanClT);
			
			//set service refs
			sessionEjb.setServiceRef(srTArr);
			choiceElem.setSession(sessionEjb);
		}
		if (MSGDR_EJB.equals(beanType)) {
			MessageDrivenBeanType msgEjb = new MessageDrivenBeanType();
			//set ejb name
			EjbNameType beanNmT = new EjbNameType();
			beanNmT.set_value(beanName);
			msgEjb.setEjbName(beanNmT);
			choiceElem.setMessageDriven(msgEjb);
			
			// set ejb class
			EjbClassType beanClT = new EjbClassType();
			beanClT.set_value(javaClass);
			msgEjb.setEjbClass(beanClT);
			
			//set serviceRef
			msgEjb.setServiceRef(srTArr);
			choiceElem.setMessageDriven(msgEjb);
		}		
		ejbChoice1Elements.put(beanName, choiceElem);
	}

	/**
	 * Add/Update the collected service client refs 
	 * in the web.xml DD
	 * 
	 */
	private void addClientsToWebDescriptor() {
		if (moduleServRefs.get(ALL_IN_ONE) == null || !(moduleServRefs.get(ALL_IN_ONE).size() > 0)) {
			return;
		}
		Set<String> serviceRefNames =  moduleServRefs.get(ALL_IN_ONE).keySet();
		WebAppType.Choice1[] curChoicesInDD =  webXmlType.getChoiceGroup1();

		// Update the current DD
		if (curChoicesInDD != null || curChoicesInDD.length > 0) {
			WebAppType.Choice1.JndiEnvironmentRefsGroup jndiRefsGr = null;
			ArrayList<WebAppType.Choice1.JndiEnvironmentRefsGroup> jndiRefsArr = new ArrayList<WebAppType.Choice1.JndiEnvironmentRefsGroup>();
			
			// collect jndi refs groups from DD
			for (WebAppType.Choice1 choice : curChoicesInDD) {
				
				jndiRefsGr = choice.getJndiEnvironmentRefsGroupGroup();  
				
				if(jndiRefsGr != null) {
					jndiRefsArr.add(jndiRefsGr);
				}
			}
			
			// check in in JNDI Refs group for Service Refs described also with annotation
			for (Iterator<JndiEnvironmentRefsGroup> iterator  = jndiRefsArr.iterator(); iterator .hasNext();) {
				JndiEnvironmentRefsGroup jndiRGrInDD = iterator.next();
				ServiceRefType[]sRefTs = jndiRGrInDD.getServiceRef();
				
				if (sRefTs != null && sRefTs.length > 0) {
					for (int i = 0; i < sRefTs.length; i++) {
						ServiceRefType curServRefInDD = sRefTs[i];
						String curServRefNameInDDName = curServRefInDD.getServiceRefName().get_value();
						// found a service ref described in DD and in Annotation
						if (serviceRefNames.contains(curServRefNameInDDName)) {
				
							ServiceRefType curServRefInAnnot =  moduleServRefs.get(ALL_IN_ONE).get(curServRefNameInDDName);  
							
							// update service ref in DD with additional info from annotations
							updateServRef(curServRefInDD, curServRefInAnnot);
							
							moduleServRefs.get(ALL_IN_ONE).remove(curServRefNameInDDName);			
						}
					}
				}
			}
			
			// There are still service-refs not added  in the DD
			if (moduleServRefs.get(ALL_IN_ONE).size() > 0) {
				WebAppType.Choice1 newChoice1 = prepareWebChoice1(moduleServRefs.get(ALL_IN_ONE));
				WebAppType.Choice1[] updatedChoices = new WebAppType.Choice1[curChoicesInDD.length+1];

				// collect choices in one
				System.arraycopy(curChoicesInDD, 0, updatedChoices, 0, curChoicesInDD.length);
				System.arraycopy(new WebAppType.Choice1[]{newChoice1}, 0 , updatedChoices, curChoicesInDD.length, 1);
				
				//add to descriptor
				webXmlType.setChoiceGroup1(updatedChoices);
			}
		}
	}
	
	/**
	 * Add service client refs to ejb-jar descriptor object of type EjbJarType
	 */
	/**
	 * Add service client refs to ejb-jar descriptor object of type EjbJarType
	 */
	private void addClientsToEjbDesriptor (){
		
		// if there are any @WebServiceRef collected 
		Set<String> annotatedEjbNames = moduleServRefs.keySet();
		if(annotatedEjbNames == null ){
			return;
		}
		
		// get enterprise-beans tag if exist
		// or create a new one
		EnterpriseBeansType entepriseBeans = ejbJarXmlType.getEnterpriseBeans();
		if(entepriseBeans == null){
			entepriseBeans = new EnterpriseBeansType();
		}
		
		// get the ejbs already declared in the  ejb-jar if any
		EnterpriseBeansType.Choice1[] beansChoices = entepriseBeans.getChoiceGroup1();
		if (beansChoices == null) {
			beansChoices = new EnterpriseBeansType.Choice1[]{new EnterpriseBeansType.Choice1()};
		}
		
		String processedEjbName = null;
		//iterate over the ejb-s declared in the ejb-jar.xml
		for (int i = 0; i < beansChoices.length; i++) {
			
			EnterpriseBeansType.Choice1 curChoice = beansChoices[i];
			curChoice.getContent();			
			if (curChoice.isSetSession()) {
				
				SessionBeanType sessionBean =  curChoice.getSession();
				if (sessionBean != null) {
					processedEjbName = sessionBean.getEjbName().get_value();
					
					if (annotatedEjbNames. contains(processedEjbName)) {
						ServiceRefType[] servRefsInDD = sessionBean.getServiceRef();
						ServiceRefType[] updatesServRefs = updateServRefArr(servRefsInDD, processedEjbName);
						
						if(updatesServRefs != null) {
							sessionBean.setServiceRef(updatesServRefs);
						}
					}
				}
				continue;
			} else if (curChoice.isSetMessageDriven()) {
				
				// Message-Driven Beans
				// Session beans
				MessageDrivenBeanType messageDrivenBean =  curChoice.getMessageDriven();
				
				if (messageDrivenBean != null) {
					processedEjbName = messageDrivenBean.getEjbName().get_value();
					if (annotatedEjbNames. contains(processedEjbName)) {
						ServiceRefType[] servRefsInDD = messageDrivenBean.getServiceRef();
						ServiceRefType[] updatesServRefs = updateServRefArr(servRefsInDD, processedEjbName);
						
						if(updatesServRefs != null) {
							messageDrivenBean.setServiceRef(updatesServRefs);
						}
					}
				}
			} else {
				//keep it as it is
			}
		}
		if(processedEjbName != null) {
			
			// remove the bean from list of ejbs to be added in the ejb-jar
			moduleServRefs.remove(processedEjbName);
		} 
		
		// add the rest of the service-refs from ejbs not described yet in the ejb-jar xml 
		if(!moduleServRefs.isEmpty() ) {
			
			// temp Choices array to store the rest of the service refs grpuped by ejb name
			EnterpriseBeansType.Choice1[] tmpBeanChoices = new EnterpriseBeansType.Choice1[beansChoices.length + moduleServRefs.size()];
			if (beansChoices.length > 0 ) {
				System.arraycopy(beansChoices, 0, tmpBeanChoices, 0, beansChoices.length);
			}
			int i = beansChoices.length;
			for (Iterator<String> iterator2 = moduleServRefs.keySet().iterator(); iterator2.hasNext();) {
				String ejbName = iterator2.next();
				tmpBeanChoices[i]  =  new EnterpriseBeansType.Choice1();
				
				// use only session beans
				SessionBeanType sessionEjb = new SessionBeanType();
				
				//set ejb name
				EjbNameType beanNmT = new EjbNameType();
				beanNmT.set_value(ejbName);
				sessionEjb.setEjbName(beanNmT);
				tmpBeanChoices[i].setSession(sessionEjb);
				
				//set service refs
				Hashtable <String, ServiceRefType> servRefsInEJB = moduleServRefs.get(ejbName);
				sessionEjb.setServiceRef(toArray(servRefsInEJB));
				tmpBeanChoices[i].setSession(sessionEjb);
				i++;
			}
			entepriseBeans.setChoiceGroup1(tmpBeanChoices);
			ejbJarXmlType.setEnterpriseBeans(entepriseBeans);
		}
	}
	
	/**
	 * Update or add the service refs for the given ejb 
	 * 
	 * @param ddServRefArr
	 * @param ejbName
	 * @return the service ref array to be set to the ejb and null if there is no need of update
	 */
	private  ServiceRefType[] updateServRefArr(ServiceRefType[] ddServRefArr, String ejbName){
		ServiceRefType[] updatedServRefs = null;
		int ddServRefArrSize = 0;
		//check if the ejb in the ejb-jar contains service refs from annotations
		if ( moduleServRefs.get(ejbName) == null) {
			return null;
		}
		
		// update the service ref in the initil DD
		Set<String> serviceRefInAnnotNames =  moduleServRefs.get(ejbName).keySet();
		String servRefInDDName = null;
		
		// update the service refs if any in the dd
		if (ddServRefArr != null) {
			ddServRefArrSize = ddServRefArr.length;
			for (int j = 0; j < ddServRefArr.length; j++) {
				servRefInDDName = ddServRefArr[j].getServiceRefName().get_value();
				if(serviceRefInAnnotNames.contains(servRefInDDName)) {
					//update
					updateServRef(ddServRefArr[j], moduleServRefs.get(ejbName).get(servRefInDDName));
					//remove
					moduleServRefs.get(ejbName).remove(servRefInDDName);
				}
			}
		} 
		
		// add the servcice refs not present in the initial DD
		if(moduleServRefs.get(ejbName) != null && moduleServRefs.get(ejbName).size() >0 ) {
			//concatenate service-refs
			ServiceRefType[] newServRefsInAnnot = toArray(moduleServRefs.get(ejbName));
			ServiceRefType[] tmpServRefs = new ServiceRefType[newServRefsInAnnot.length + ddServRefArrSize];
			
			if(ddServRefArr != null){
				System.arraycopy(ddServRefArr, 0, tmpServRefs, 0, ddServRefArrSize);
			}
			System.arraycopy(newServRefsInAnnot, 0, tmpServRefs, ddServRefArrSize, newServRefsInAnnot.length);
			
			updatedServRefs = tmpServRefs;
		}
		
		return updatedServRefs;
	}
	
	/**
	 * Add the collected service client refs to the web descriptor object of type ApplicationClientType
	 */
	private void addClientsToAppClientDescriptor() {
		if (moduleServRefs.get(ALL_IN_ONE) == null || moduleServRefs.get(ALL_IN_ONE).isEmpty()) {
			return;
		}
		Set<String> serviceRefNames =  moduleServRefs.get(ALL_IN_ONE).keySet();
		
		// check for service-refs in the DD
		ServiceRefType[] servRefsInDD = appClientType.getServiceRef();
		if (servRefsInDD != null && servRefsInDD.length > 0) {
			
			//update service-refs described in the DD 
			//if more info is provided by annotations
			for (int j = 0; j < servRefsInDD.length; j++) {
				ServiceRefType curServRefInDD = servRefsInDD[j];
				String curServRefNameInDDName = curServRefInDD.getServiceRefName().get_value();
				// found a service ref described in DD and in Annotation
				if (serviceRefNames.contains(curServRefNameInDDName)) {
					ServiceRefType curServRefInAnnot =  moduleServRefs.get(ALL_IN_ONE).get(curServRefNameInDDName);  
					// update service ref in DD with additional info from annotations
					updateServRef(curServRefInDD, curServRefInAnnot);
					moduleServRefs.get(ALL_IN_ONE).remove(curServRefNameInDDName);			
				}
			}
		}
				
		// There are still service-refs not added  in the DD
		if (moduleServRefs.get(ALL_IN_ONE).size() > 0) {
			ServiceRefType[] servRefsInAnnArr = new ServiceRefType[moduleServRefs.get(ALL_IN_ONE).size()];
			int k = 0;
			for (Iterator<ServiceRefType> iterator = moduleServRefs.get(ALL_IN_ONE).values().iterator(); iterator.hasNext();) {
				ServiceRefType ref = iterator.next();
				servRefsInAnnArr[k] = ref;
			}
			ServiceRefType[]  updatedServRefsArr = new ServiceRefType[servRefsInDD.length + servRefsInAnnArr.length];
			
			// collect service-refs in one
			System.arraycopy(servRefsInDD, 0, updatedServRefsArr, 0, servRefsInDD.length);
			System.arraycopy(servRefsInAnnArr, 0 , updatedServRefsArr, servRefsInDD.length,  servRefsInAnnArr.length);
			
			// update  descriptor
			appClientType.setServiceRef(updatedServRefsArr);
		}
	}
	
	/**
	 * Checks if the class belongs to the processed module
	 * 
	 * @param fileToCheck
	 * @param clName String - the class name with the package path 
	 * (ex: com.sap.engine.services.webservices.server.deploy.jee5.NewWSClientsAnnotationConvertor)
	 * @throws ConversionException 
	 * 
	 */
	private boolean inArchive(File fileToCheck, String clName) throws ConversionException {
		boolean inArchive = false;
		String fileNameReplaced = clName.replace(".", "/");
		JarFile archive = null;
		try {
			archive = new JarFile (fileToCheck);
			String relativeClassFile = "";
			if(archive.getName().endsWith(".war")){
				relativeClassFile = WEB_INF+"/classes/"+fileNameReplaced+".class";
			} else {
				relativeClassFile = fileNameReplaced+".class";
			}
			if(archive.getEntry(relativeClassFile) != null) {
				inArchive = true;
			}
			// check the  LIB directory of extracted WAR 		
			if(!inArchive && archive.getName().endsWith(".war")){
				
				int extIndex = archiveName.lastIndexOf(".war");
			    String extractedModuleName  = archiveName.substring(0, extIndex) + ".extracted.war";
				String extractedWarLib = this.tmpPath + "/"+ extractedModuleName + "/" + WEB_INF + "/" + LIB;
				
				File dir = new File(extractedWarLib);
				if (dir.isDirectory()){					
					inArchive = checkLibDir(dir,clName);
				}
			}
			
		} catch (IOException e) {
			throw new ConversionException("webservices_5201", new Object[] {fileToCheck.getName() , clName}, e );
		} finally {
			try {
				if(archive != null) {
						archive.close();
				}
			} catch (IOException e) {
				// $JL-EXC$3
			}
		}
		return inArchive;
	}
	
	/**
	 *  Check whether current file exist in jars located in  WEB-INF/lib 
	 *  (uses the extracted war folder)
	 *  
	 * @param dir File - contains directory pathname
	 * @param className String - fully qualified class name
	 * 
	 * @return fileExist boolean - return whether file exist or not
	 * 
	 * @throws IOException 
	 * @throws ZipException 
	 * @throws ConversionException 
	 */		
	 private boolean checkLibDir( File dir, String className) throws  ConversionException{
		 boolean fileExist = false;
		 String[] children = dir.list();
		 File libJars = null;
		 for(int i=0; i< children.length; i++){
			 String curChild = dir.getAbsolutePath()+ File.separator +children[i];
			 libJars = new File(curChild);
			 
			 if(!libJars.isDirectory()){
				 fileExist = inArchive(libJars, className);
				 if(fileExist){
					 break;
				 }
			 }
		 }
		 return fileExist;
	 }
	 
	 private boolean isAppClientClass(ClassInfo classInfo) {     
		MethodInfo[] mainMethodInfoes = classInfo.getMethod("main");      
		if(mainMethodInfoes == null || mainMethodInfoes.length == 0) {
	      return false; 	
	    }   	  
		return true; 
	  }
	 
	 private void updateServRef(ServiceRefType ddServRef, ServiceRefType annotServRef){
		 
		// Update service-interface if generic in DD
		if(ddServRef.getServiceInterface() == null || ddServRef.getServiceInterface().get_value().equals(ISERVICE)) {
			ddServRef.setServiceInterface(annotServRef.getServiceInterface());
		}
		
		// Add wsdlFile if not described in DD
		if (ddServRef.getWsdlFile() == null && annotServRef.getWsdlFile() != null) {
			ddServRef.setWsdlFile(annotServRef.getWsdlFile());
		}
		
		// Add mappedName if not described in DD 
		if (ddServRef.getMappedName() == null && annotServRef.getMappedName() != null) {
			ddServRef.setMappedName(annotServRef.getMappedName());
		}
		
		// Add QName if not described in DD 
		// Add mappedName if not described in DD 
		if (ddServRef.getServiceQname() == null && annotServRef.getServiceQname() != null) {
			ddServRef.setServiceQname(annotServRef.getServiceQname());
		}
	 }
	 /**
	  * Make array from the values of hashtable
	  *  
	  * @param hashT
	  * @return null if no elements 
	  */
	 private ServiceRefType[] toArray(Hashtable<String , ServiceRefType> hashT) {
			ServiceRefType[] servRefsInEJBArr = null;
			if(hashT != null || !(hashT.size() > 0)){
				servRefsInEJBArr = new ServiceRefType[hashT.size()];
				hashT.values().toArray(servRefsInEJBArr);
				return servRefsInEJBArr;
				
			} else {
				return null;
			}
		}
	
	 /**
	  * 
	  * Build Jar from given Jar file adding deploy descriptors
	  * @param descriptorObj descriptor object to be serialized(ApplicationClientType, EjbJArType or WebAppType object) 
	  * @param webServiceClientDescriptor - name web service client deploy descriptor (web.xml, ejb-jar.xml or application-client.xml) 
	  * @param tmpPath - path to source war/jar archives location
	  * @param moduleRelativeFileUri - name of source jar/war archive
	  * @param newArchiveFileName - name of new built jar/war archive
	  * @throws IOException 
	  * 
	  */
	 private void buildJar(Object descriptorObj, String webServiceClientDescriptor, String tmpPath, String moduleRelativeFileUri,  String newArchiveFileName) throws IOException{
		 String webServicesClientXmlDir = null;
		 JarFile srcArchiveJarFile = null;
		 File archiveFileNew = null;
		 FileOutputStream out = null;
		 JarOutputStream jarOut = null;
		 
		 try {
			 if(moduleType.equals(SERVLET_CLIENT)) {
				 webServicesClientXmlDir = WEB_INF;
			 } else {
				 webServicesClientXmlDir = META_INF;
			 }
			 
			 srcArchiveJarFile = new JarFile(tmpPath + File.separator + moduleRelativeFileUri);   
			 archiveFileNew = new File(tmpPath + File.separator + newArchiveFileName);
	         out = new FileOutputStream(archiveFileNew); 
	         jarOut = new JarOutputStream(out);
	         
			 JarUtil.copyEntries(srcArchiveJarFile, new String[0], new String[0], new String[0],new String[]{webServicesClientXmlDir + "/" + webServiceClientDescriptor},  false, jarOut);
			 try { 
				  jarOut.putNextEntry(new JarEntry(webServicesClientXmlDir + "/" + webServiceClientDescriptor));
				  this.descriptorProcessor5 .build(descriptorObj, jarOut);	  	 
				} finally {
				  try {
			        jarOut.closeEntry();  
			      } catch(Exception e) {
			        // $JL-EXC$		
			      }	  
			    } 
	    } finally {
	      try {
	        if(srcArchiveJarFile != null) {
	        	srcArchiveJarFile.close();	
	        }  
	      } catch(Exception e) {
	        // $JL-EXC$	   
	      }     
	      try {
	        if(jarOut != null) {
	        	jarOut.close(); 	
	        }  
	      } catch(Exception e) {
	        // $JL-EXC$	   
	      }
	      try {
	        if(out != null) {
	          out.close();	
	        }  
	      } catch(Exception e) {
	        // $JL-EXC$	   
	      }  
	      
	    } 
	 }
}
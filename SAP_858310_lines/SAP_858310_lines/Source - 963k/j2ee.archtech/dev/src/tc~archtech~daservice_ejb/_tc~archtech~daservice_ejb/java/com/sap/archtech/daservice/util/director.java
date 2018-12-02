package com.sap.archtech.daservice.util;

import java.util.ArrayList;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.sap.sld.api.builder.DataQueue;
import com.sap.sld.api.builder.DeltaElement;
import com.sap.sld.api.builder.GenericInputData;
import com.sap.sld.api.builder.InvalidDataException;
import com.sap.sld.api.builder.MappingUtility;
import com.sap.sld.api.builder.SynchronousDirector;
import com.sap.sld.api.wbem.cim.CIMNamespace;
import com.sap.sld.api.wbem.cim.CIMType;
import com.sap.sld.api.wbem.cim.ElementName;
import com.sap.sld.api.wbem.client.WBEMClient;
import com.sap.sld.api.wbem.exception.CIMException;
import com.sap.sld.api.wbem.sap.SLDElementNames;
import com.sap.sldserv.SldApplicationServiceInterface;

public class Director {

	public static final String ENGINE_CREATION_CLASS_NAME = "SAP_XMLDataArchivingServer";
	public static final String ARCHIVE_CREATION_CLASS_NAME = "SAP_ArchiveStore";
	public static final String ASSOCIATION_CREATION_CLASS_NAME = "SAP_ArchiveStoreForXMLDAS";
	public static final ElementName[] KEYPROP_NAMES = {
			SLDElementNames.P_CreationClassName, SLDElementNames.P_Name };
	public static final String[] ENGINE_PROP_NAMES = {
			SLDElementNames.P_Caption.toString(),
			SLDElementNames.P_ApplicationName.toString(),
			SLDElementNames.P_ApplicationType.toString(),
			SLDElementNames.P_Description.toString(),
			SLDElementNames.P_NameFormat.toString(),
			SLDElementNames.P_SystemCreationClassName.toString(),
			SLDElementNames.P_SystemName.toString(),
			SLDElementNames.P_URL.toString() };
	public static final String[] ARCHIVE_PROP_NAMES = {
			SLDElementNames.P_Caption.toString(),
			SLDElementNames.P_ArchiveStore.toString(),
			SLDElementNames.P_Description.toString(),
			SLDElementNames.P_DestinationName.toString(),
			SLDElementNames.P_NameFormat.toString(),
			SLDElementNames.P_StoreType.toString(),
			SLDElementNames.P_UnixRoot.toString(),
			SLDElementNames.P_WebDAVRoot.toString(),
			SLDElementNames.P_WindowsRoot.toString(),
			SLDElementNames.P_XMLDASName.toString(),
			SLDElementNames.P_ILMConformanceClass.toString(),
			SLDElementNames.P_IsDefaultArchiveStore.toString() };
	public static final String[] ARCHIVE_PROP_TYPES = new String[] {
			CIMType.NAME_STRING, CIMType.NAME_STRING, CIMType.NAME_STRING,
			CIMType.NAME_STRING, CIMType.NAME_STRING, CIMType.NAME_STRING,
			CIMType.NAME_STRING, CIMType.NAME_STRING, CIMType.NAME_STRING,
			CIMType.NAME_STRING, CIMType.NAME_UINT16, CIMType.NAME_BOOLEAN };
	public static final String[] ARCHIVE_PROP_RESET_DEFAULT_NAMES = { "IsDefaultArchiveStore" };
	public static final String[] ARCHIVE_PROP_RESET_DEFAULT_TYPES = new String[] { CIMType.NAME_BOOLEAN };

	public void insertSldInstance(String engineName, String[] enginePropValues,
			String archiveName, String[] archivePropValues)
			throws CIMException, InvalidDataException, NamingException {

		// Create WBEMClient
		Context context = new InitialContext();
		SldApplicationServiceInterface serviceInterface = (SldApplicationServiceInterface) (context
				.lookup(SldApplicationServiceInterface.KEY));
		WBEMClient cimClient = serviceInterface.getWbemClient();

		// Get Object Server And CIM Name Space
		ArrayList<DeltaElement> inputObjList = new ArrayList<DeltaElement>();
		DeltaElement engine, archive, association;
		ElementName objectServer = cimClient.getObjectServer();
		CIMNamespace cimNamespace = cimClient.getTargetNamespace();

		// Build Engine Delta Element
		String[] engineKeyValues = new String[] { ENGINE_CREATION_CLASS_NAME,
				engineName };
		engine = MappingUtility.createDeltaElement(objectServer, cimNamespace,
				new ElementName(ENGINE_CREATION_CLASS_NAME), KEYPROP_NAMES,
				engineKeyValues, null, null);
		MappingUtility.changeValues(engine, ENGINE_PROP_NAMES,
				enginePropValues, null, null);
		inputObjList.add(engine);

		// Build Archive Delta Element
		String[] archiveKeyValues = new String[] { ARCHIVE_CREATION_CLASS_NAME,
				archiveName };
		archive = MappingUtility.createDeltaElement(objectServer, cimNamespace,
				new ElementName(ARCHIVE_CREATION_CLASS_NAME), KEYPROP_NAMES,
				archiveKeyValues, null, null);
		MappingUtility.changeValues(archive, ARCHIVE_PROP_NAMES,
				archivePropValues, ARCHIVE_PROP_TYPES, null);
		inputObjList.add(archive);

		// Build Association Delta Element
		association = MappingUtility.createDeltaElement(objectServer,
				cimNamespace, ASSOCIATION_CREATION_CLASS_NAME, engine,
				SLDElementNames.ROLE_Antecedent.toString(), archive,
				SLDElementNames.ROLE_Dependent.toString());
		inputObjList.add(association);

		// Create Synchronous Director And Builder Factory
		SynchronousDirector director = new SynchronousDirector();
		BuilderFactory builderFactory = new BuilderFactory();

		// Add CIM Client And Builder Factory To Director
		director.addCIMClient(cimClient);
		director.addBuilders(builderFactory);

		// Build Input Data Queue
		DataQueue queue = new DataQueue();
		GenericInputData data = new GenericInputData(inputObjList, null);
		queue.add(data);
		director.setDataQueue(queue);

		// Synchronize Data
		director.distributeData();
	}

	public void updateSldInstance(String archiveName, String[] archivePropValues)
			throws CIMException, InvalidDataException, NamingException {

		// Create WBEMClient
		Context context = new InitialContext();
		SldApplicationServiceInterface serviceInterface = (SldApplicationServiceInterface) (context
				.lookup(SldApplicationServiceInterface.KEY));
		WBEMClient cimClient = serviceInterface.getWbemClient();

		// Get Object Server And CIM Name Space
		ArrayList<DeltaElement> inputObjList = new ArrayList<DeltaElement>();
		DeltaElement archive;
		ElementName objectServer = cimClient.getObjectServer();
		CIMNamespace cimNamespace = cimClient.getTargetNamespace();

		// Build Archive Delta Element
		String[] archiveKeyValues = new String[] { ARCHIVE_CREATION_CLASS_NAME,
				archiveName };
		archive = MappingUtility.createDeltaElement(objectServer, cimNamespace,
				new ElementName(ARCHIVE_CREATION_CLASS_NAME), KEYPROP_NAMES,
				archiveKeyValues, null, null);
		MappingUtility.changeValues(archive, ARCHIVE_PROP_NAMES,
				archivePropValues, ARCHIVE_PROP_TYPES, null);
		inputObjList.add(archive);

		// Create Synchronous Director And Builder Factory
		SynchronousDirector director = new SynchronousDirector();
		BuilderFactory builderFactory = new BuilderFactory();

		// Add CIM Client And Builder Factory To Director
		director.addCIMClient(cimClient);
		director.addBuilders(builderFactory);

		// Build Input Data Queue
		DataQueue queue = new DataQueue();
		GenericInputData data = new GenericInputData(inputObjList, null);
		queue.add(data);
		director.setDataQueue(queue);

		// Synchronize Data
		director.distributeData();
	}

	public void updateResetDefaultSldInstance(String archiveName,
			String[] archivePropValues) throws CIMException,
			InvalidDataException, NamingException {

		// Create WBEMClient
		Context context = new InitialContext();
		SldApplicationServiceInterface serviceInterface = (SldApplicationServiceInterface) (context
				.lookup(SldApplicationServiceInterface.KEY));
		WBEMClient cimClient = serviceInterface.getWbemClient();

		// Get Object Server And CIM Name Space
		ArrayList<DeltaElement> inputObjList = new ArrayList<DeltaElement>();
		DeltaElement archive;
		ElementName objectServer = cimClient.getObjectServer();
		CIMNamespace cimNamespace = cimClient.getTargetNamespace();

		// Build Archive Delta Element
		String[] archiveKeyValues = new String[] { ARCHIVE_CREATION_CLASS_NAME,
				archiveName };
		archive = MappingUtility.createDeltaElement(objectServer, cimNamespace,
				new ElementName(ARCHIVE_CREATION_CLASS_NAME), KEYPROP_NAMES,
				archiveKeyValues, null, null);
		MappingUtility.changeValues(archive, ARCHIVE_PROP_RESET_DEFAULT_NAMES,
				archivePropValues, ARCHIVE_PROP_RESET_DEFAULT_TYPES, null);
		inputObjList.add(archive);

		// Create Synchronous Director And Builder Factory
		SynchronousDirector director = new SynchronousDirector();
		BuilderFactory builderFactory = new BuilderFactory();

		// Add CIM Client And Builder Factory To Director
		director.addCIMClient(cimClient);
		director.addBuilders(builderFactory);

		// Build Input Data Queue
		DataQueue queue = new DataQueue();
		GenericInputData data = new GenericInputData(inputObjList, null);
		queue.add(data);
		director.setDataQueue(queue);

		// Synchronize Data
		director.distributeData();
	}

	public void deleteSldInstance(String engineName, String archiveName)
			throws CIMException, InvalidDataException, NamingException {

		// Create WBEMClient
		Context context = new InitialContext();
		SldApplicationServiceInterface serviceInterface = (SldApplicationServiceInterface) (context
				.lookup(SldApplicationServiceInterface.KEY));
		WBEMClient cimClient = serviceInterface.getWbemClient();

		// Get Object Server And CIM Name Space
		ArrayList<DeltaElement> inputObjList = new ArrayList<DeltaElement>();
		DeltaElement engine, archive;
		ElementName objectServer = cimClient.getObjectServer();
		CIMNamespace cimNamespace = cimClient.getTargetNamespace();

		// Build Engine Delta Element
		if (engineName != null) {
			String[] engineKeyValues = new String[] {
					ENGINE_CREATION_CLASS_NAME, engineName };
			engine = MappingUtility.createDeltaElement(objectServer,
					cimNamespace, new ElementName(ENGINE_CREATION_CLASS_NAME),
					KEYPROP_NAMES, engineKeyValues, null, null);
			inputObjList.add(engine);
		}

		// Build Archive Delta Element
		if (archiveName != null) {
			String[] archiveKeyValues = new String[] {
					ARCHIVE_CREATION_CLASS_NAME, archiveName };
			archive = MappingUtility.createDeltaElement(objectServer,
					cimNamespace, new ElementName(ARCHIVE_CREATION_CLASS_NAME),
					KEYPROP_NAMES, archiveKeyValues, null, null);
			inputObjList.add(archive);
		}

		// Create Synchronous Director And Builder Factory
		SynchronousDirector director = new SynchronousDirector();
		BuilderFactory builderFactory = new BuilderFactory();

		// Add CIM Client And Builder Factory To Director
		director.addCIMClient(cimClient);
		director.addBuilders(builderFactory);

		// Build Input Data Queue
		DataQueue queue = new DataQueue();
		GenericInputData data = new GenericInputData(null, inputObjList);
		queue.add(data);
		director.setDataQueue(queue);

		// Synchronize Data
		director.distributeData();
	}
}
/*
 * Copyright (c) 2004 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.webservices.server.deploy.migration.wsclients;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import com.sap.engine.services.webservices.espbase.configuration.BindingData;
import com.sap.engine.services.webservices.espbase.configuration.ConfigurationFactory;
import com.sap.engine.services.webservices.espbase.configuration.ConfigurationRoot;
import com.sap.engine.services.webservices.espbase.configuration.InterfaceData;
import com.sap.engine.services.webservices.espbase.configuration.InterfaceDefinition;
import com.sap.engine.services.webservices.espbase.configuration.InterfaceDefinitionCollection;
import com.sap.engine.services.webservices.espbase.configuration.OperationData;
import com.sap.engine.services.webservices.espbase.configuration.PropertyListType;
import com.sap.engine.services.webservices.espbase.configuration.PropertyType;
import com.sap.engine.services.webservices.espbase.configuration.Service;
import com.sap.engine.services.webservices.espbase.configuration.ServiceCollection;
import com.sap.engine.services.webservices.espbase.configuration.ServiceData;
import com.sap.engine.services.webservices.espbase.configuration.Variant;
import com.sap.engine.services.webservices.espbase.mappings.ImplementationLink;
import com.sap.engine.services.webservices.espbase.mappings.InterfaceMapping;
import com.sap.engine.services.webservices.espbase.mappings.MappingFactory;
import com.sap.engine.services.webservices.espbase.mappings.MappingRules;
import com.sap.engine.services.webservices.espbase.mappings.ServiceMapping;
import com.sap.engine.services.webservices.jaxrpc.exceptions.LogicalPortException;
import com.sap.engine.services.webservices.jaxrpc.exceptions.TypeMappingException;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.lpapi.FeatureType;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.lpapi.GlobalFeatures;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.lpapi.LocalFeatures;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.lpapi.LogicalPortFactory;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.lpapi.LogicalPortType;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.lpapi.LogicalPorts;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.lpapi.OperationType;


/**
 * Copyright (c) 2004, SAP-AG
 * @author Boyan Slavov
 */
public class LPortsConvertor {

	
	/** Convert the xml file with path lports to the files with path configuration and mappings 
	 * @param lports path to file conforming to the schema com\sap\engine\services\webservices\jaxrpc\wsdl2java\lpapi\LPSchema.xsd
	 * @param configuration path to file conforming to the schema com\sap\engine\services\webservices\espbase\configuration\configuration.xsd
	 * @param mappings path to file conforming to the schema tc\je\webservices_lib\_comp\src\packages\com\sap\engine\services\webservices\espbase\mappings\mapping.xsd
	 * @throws LogicalPortException
	 * @throws TypeMappingException
	 * @throws IOException
	 */
	public static void convert(String lports, String configuration, String mappings)
		throws LogicalPortException, TypeMappingException, IOException {
		LogicalPortFactory factory = new LogicalPortFactory();
		ConfigurationRoot cr = new ConfigurationRoot();
		MappingRules rules = new MappingRules();
		
		convert(factory.loadLogicalPorts(lports), cr, rules);
		ConfigurationFactory.save(cr, configuration);
		MappingFactory.save(rules, mappings);
	}

	/** Transfer contained information from LogicalPorts to ConfigurationRoot and MappingRules
	 * @param ports
	 */
	public static void convert(LogicalPorts ports, ConfigurationRoot root, MappingRules rules) {

		ServiceCollection rt = new ServiceCollection();
		root.setRTConfig(rt);

		Service s = new Service();
		Service[] ss = new Service[] { s };
		rt.setService(ss);

		ServiceData sd = null;
		//s.setId();
		s.setName(ports.getName());
		sd = new ServiceData();
		s.setServiceData(sd);

		LogicalPortType[] types = ports.getLogicalPort();

		if (types != null) {

			InterfaceDefinitionCollection dt =
				new InterfaceDefinitionCollection();
			root.setDTConfig(dt);

			InterfaceDefinition id = new InterfaceDefinition();
			InterfaceDefinition[] ids = new InterfaceDefinition[]{id};
			id.setId("???");
			id.setName("???");
			id.setType(0);

			Variant[] vars = new Variant[types.length];
			BindingData[] bds = new BindingData[types.length];
			Map mappedBindings = new HashMap();
			
			for (int i = 0; i < types.length; i++) {
				
				LogicalPortType type = types[i];
				Variant var = new Variant();
				vars[i] = var;

				//type.getStubName()

				InterfaceData idata = new InterfaceData();
				var.setInterfaceData(idata);
				var.setName(type.getName());
//				idata.setDescription(descs);
//				desc = descs[0];
//				desc.setLocale("");
//				desc.setLongText("");
//				desc.setShortText("");
//				idata.setName("");
//				idata.setNamespace("");
				LocalFeatures lf = type.getLocalFeatures();
				if (lf != null) {
					idata.setOperation(convert(lf.getOperation()));
				}
				
//				OperationData op = ops[0];
//				op.setName("");
//				op.setPropertyList(pls);
//				PropertyListType pl = pls[0];
//				pl.setProperty(prop);
//				pl.setSelected(null);

				GlobalFeatures features = type.getGlobalFeatures();
				if (features != null)
					idata.setPropertyList(convert(features.getFeature()));

//				idata.setUDDIEntity(ues);
//				ue = ues[0];
//				ue.setSubscribtionKey("");
//				ue.setType(0);
//				ue.setUddiKey("");
//				ue.setUDDIServer(us);
//				us.setInquiryURL("");
//				us.setSIDName("");

				BindingData bd = new BindingData();
				bds[i] = bd;
				convert(type, bd, mappedBindings);

			}

			id.setVariant(vars);			
			dt.setInterfaceDefinition(ids);
			sd.setBindingData(bds);
			rules.setInterface((InterfaceMapping[])mappedBindings.values().toArray(new InterfaceMapping[mappedBindings.size()]));

			ServiceMapping sm = new ServiceMapping();
			rules.setService( new ServiceMapping[]{sm});
			//sm.setEndpoint();
			ImplementationLink implLink = new ImplementationLink();
			if (ports.getImplementationName() != null)
				implLink.setSIImplName(ports.getImplementationName());
			sm.setImplementationLink(implLink);
			//sm.setProperty();
			sm.setSIName(ports.getInterfaceName());

			//sd.setDomainId("");
			//sd.setName(ports.getName());
			//sd.setNamespace("");
			//pls = null;
			//sd.setPropertyList(pls);
			//ues = null;
			//sd.setUDDIEntity(ues);
			s.setType(null);
		}
	}

	/**
	 * @param type
	 * @return
	 */
	private static String getInterfaceId(LogicalPortType type) {
		return type.getName() + "--" + type.getInterfaceName();
	}

	/**
	 * @param type
	 * @param s
	 */
	private static void convert(LogicalPortType type, BindingData bd, Map mappedBindings) {
		//bd.setActive(null);
		bd.setBindingName(type.getBindingName());
		bd.setBindingNamespace(type.getBindingUri());
		//bd.setEditable();
		//bd.setGroupConfigId("");
		//bd.setInterfaceId(getInterfaceId(type));
		bd.setName(type.getName());
		LocalFeatures lf = type.getLocalFeatures();
		if (lf != null) {
			bd.setOperation(convert(lf.getOperation()));
		}
		GlobalFeatures features = type.getGlobalFeatures();
		if (features != null)
			bd.setPropertyList(convert(features.getFeature()));
		//ues = null;
		//bd.setUDDIEntity(ues);
		bd.setUrl(type.getEndpoint());
		bd.setVariantName(type.getName());


		if (!mappedBindings.containsKey(type.getBindingName())) {
			InterfaceMapping im = new InterfaceMapping();
			mappedBindings.put(type.getBindingName(), im);

			im.setBindingQName(new QName(null, type.getBindingName()));
			
			ImplementationLink link = new ImplementationLink();
			if (type.getStubName() != null)
				link.setStubName(type.getStubName());
			im.setImplementationLink(link);

			//im.setOperation();
			//im.setPortType();
			//im.setProperty();
			im.setSEIName(type.getInterfaceName());
		}
	}

	private static OperationData[] convert(OperationType[] ops) {
		OperationData[] ods = new OperationData[ops.length];
		
		for (int i = 0; i < ods.length; i++) {
		
			OperationType op = ops[i];
			OperationData od = new OperationData();
			ods[i] = od;
			od.setName(op.getName());
			//op.getMechanismType();
			od.setPropertyList(convert(op.getFeature()));
		
		}
		return ods;
	}

	/**
	 * @param types
	 * @return
	 */
	private static PropertyListType[] convert(FeatureType[] fs) {
		if (fs == null)
			return null;

		ArrayList propertyLists = new ArrayList(fs.length);

		for (int j = 0; j < fs.length; j++) {
			FeatureType f = fs[j];
			PropertyListType pl = new PropertyListType();
			
			if (f.getProperty() != null && f.getProperty().length > 0) {
				propertyLists.add(pl);
				pl.setProperty(convert(f.getProperty(), f.getName()));
			}
			//pl.setSelected();
		}
		PropertyListType[] pls = new PropertyListType[propertyLists.size()];
		propertyLists.toArray(pls);
		return pls;
	}

	/**
	 * @param types
	 * @return
	 */
	private static PropertyType[] convert(
		com
			.sap
			.engine
			.services
			.webservices
			.jaxrpc
			.wsdl2java
			.lpapi
			.PropertyType[] srcs,
		String ns) {
		if (srcs == null)
			return null;

		PropertyType[] dsts = new PropertyType[srcs.length];

		for (int i = 0; i < dsts.length; i++) {
			PropertyType dst = new PropertyType();
			dsts[i] = dst;
			com
				.sap
				.engine
				.services
				.webservices
				.jaxrpc
				.wsdl2java
				.lpapi
				.PropertyType src =
				srcs[i];

			dst.set_value(src.getValue());
			dst.setName(src.getName());
			dst.setNamespace(ns);
			// src.getProperty() // hierarchical properties???
		}
		return dsts;
	}

}

package com.sap.archtech.xmldasinit;

import java.util.Iterator;
import java.util.Set;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.sap.archtech.daservice.mbeans.SAP_ITSAMXMLDataArchivingServerWrapper;
import com.sap.archtech.daservice.mbeans.SAP_ITSAMXMLDataArchivingServer_Impl;

/**
 * The class <code>MBeanInitializer</code> is responsible for the
 * initialization of the XMLDAS Administration MBeans.
 */
class MBeanInitializer {

	public final static String J2EE_CLUSTER_QUERY = "*:type=SAP_ITSAMJ2eeCluster,cimclass=SAP_ITSAMJ2eeCluster,*";
	public final static String XMLDAS_OBJECT_NAME_BEGINNING = ":cimclass=SAP_ITSAMXMLDataArchivingServer,version=1.0,type=SAP_ITSAMJ2eeCluster.SAP_ITSAMXMLDataArchivingServer,SAP_ITSAMJ2eeCluster.Name=";
	public final static String XMLDAS_OBJECT_NAME_ENDING = ",SAP_ITSAMJ2eeCluster.CreationClassName=SAP_ITSAMJ2eeCluster,SAP_ITSAMXMLDataArchivingServer.Name=SAP_ITSAMXMLDataArchivingServer,SAP_ITSAMXMLDataArchivingServer.CreationClassName=SAP_ITSAMXMLDataArchivingServer";

	void initMBeans() throws NamingException, MalformedObjectNameException,
			InstanceAlreadyExistsException, MBeanRegistrationException,
			NotCompliantMBeanException {
		MBeanServer mbs = this.getMBeanServer();
		SAP_ITSAMXMLDataArchivingServerWrapper mbean = new SAP_ITSAMXMLDataArchivingServerWrapper(
				new SAP_ITSAMXMLDataArchivingServer_Impl());
		ObjectName objectName = this.getObjectName(mbs);
		if (!mbs.isRegistered(objectName))
			mbs.registerMBean(mbean, objectName);
	}

	void destroyMBeans() throws NamingException, MalformedObjectNameException,
			MBeanRegistrationException, InstanceNotFoundException {
		MBeanServer mbs = this.getMBeanServer();
		ObjectName objectName = this.getObjectName(mbs);
		mbs.unregisterMBean(objectName);
	}

	String infoMBeans() throws NamingException, MalformedObjectNameException,
			InstanceNotFoundException, IntrospectionException,
			ReflectionException {
		MBeanServer mbs = this.getMBeanServer();
		ObjectName objectName = this.getObjectName(mbs);
		MBeanInfo mbeanInfo = mbs.getMBeanInfo(objectName);
		if ((mbs.isRegistered(objectName)))
			return mbeanInfo.getDescription() + " is one of the "
					/*+ mbs.getMBeanCount()*/ + " registered MBeans.";// see CSN 0766325 2007 -> getMBeanCount() is regarded as "dangerous performance killer"
		else
			return "SAP_ITSAMXMLDataArchivingServer MBean is not registered.";
	}

	private ObjectName getObjectName(MBeanServer mbs)
			throws MalformedObjectNameException {
		Set<ObjectName> set = mbs.queryNames(new ObjectName(J2EE_CLUSTER_QUERY), null);
		Iterator<ObjectName> iterator = set.iterator();
		String clusterName = null;
		if (iterator.hasNext()) {
			ObjectName objectName = iterator.next();
			clusterName = (objectName
					.getKeyProperty("SAP_ITSAMJ2eeCluster.Name"));
		}
		if (clusterName != null)
			return new ObjectName(XMLDAS_OBJECT_NAME_BEGINNING + clusterName
					+ XMLDAS_OBJECT_NAME_ENDING);
		else
			return null;
	}

	private MBeanServer getMBeanServer() throws NamingException {
		return (MBeanServer) new InitialContext().lookup("jmx");
	}
}

package com.sap.engine.services.ts.mbeans;

import java.util.Properties;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;
import com.sap.tc.logging.SimpleLogger;

public class TransactionServiceMBeansRegistrator {

	private static final Location location = Location
			.getLocation(TransactionServiceMBeansRegistrator.class
					.getName());

	public static void registerTransactionServiceMBeans(MBeanServer jmx, int nodeId) {
		String clusterMBeanName = null;
		String nodeMBeanName = null;
		try {
			SAP_ITSAMTransactionServiceManagementCluster_Impl clusterMbeanImpl =
				new SAP_ITSAMTransactionServiceManagementCluster_Impl(jmx);
			SAP_ITSAMTransactionServiceManagementClusterWrapper clusterMbeanWrapper =
				new SAP_ITSAMTransactionServiceManagementClusterWrapper(clusterMbeanImpl);

			SAP_ITSAMTransactionServiceManagementNode_Impl nodeMbeanImpl =
				new SAP_ITSAMTransactionServiceManagementNode_Impl();
			SAP_ITSAMTransactionServiceManagementNodeWrapper nodeMbeanWrapper =
				new SAP_ITSAMTransactionServiceManagementNodeWrapper(nodeMbeanImpl);

			String[] mBeanNames = getMBeanNames(jmx, nodeId);
			clusterMBeanName = mBeanNames[0];
			nodeMBeanName = mBeanNames[1];

			try {
				jmx.unregisterMBean(new ObjectName(nodeMBeanName));
			} catch (Exception e) {
				// $JL-EXC$
			} finally {
				try {
					jmx.unregisterMBean(new ObjectName(clusterMBeanName));
				} catch (Exception e) {
					// $JL-EXC$
				}
			}

			jmx.registerMBean(nodeMbeanWrapper, new ObjectName(nodeMBeanName));
			jmx.registerMBean(clusterMbeanWrapper, new ObjectName(clusterMBeanName));

		} catch (Exception e) {
			SimpleLogger.traceThrowable(Severity.ERROR, location,
					"Cannot register transaction service management beans. "
							+ "The transaction UI might not work.", e);

			try {
				if (null != nodeMBeanName) {
					jmx.unregisterMBean(new ObjectName(nodeMBeanName));
				}
			} catch (Exception e1) {
				// $JL-EXC$
			} finally {
				try {
					if (null != clusterMBeanName) {
						jmx.unregisterMBean(new ObjectName(clusterMBeanName));
					}
				} catch (Exception e1) {
					// $JL-EXC$
				}
			}
		}
	}

	public static void unregisterTransactionServiceMBeans(MBeanServer jmx, int nodeId) {
		String clusterMBeanName = null;
		try {
			String[] mBeanNames = getMBeanNames(jmx, nodeId);
			clusterMBeanName = mBeanNames[0];
			String nodeMBeanName = mBeanNames[1];

			jmx.unregisterMBean(new ObjectName(nodeMBeanName));
		} catch (Exception e) {
			// $JL-EXC$
		} finally {
			try {
				if (null!=clusterMBeanName) {
					jmx.unregisterMBean(new ObjectName(clusterMBeanName));
				}
			} catch (Exception e) {
				// $JL-EXC$
			}
		}
	}

	private static String[] getMBeanNames(MBeanServer jmx, int nodeId) throws Exception {
		Set<ObjectName> result = jmx.queryNames(
				new ObjectName("*:*,cimclass=SAP_ITSAMJ2eeCluster"), null);
		ObjectName j2eeCluster = result.iterator().next();
		result = jmx.queryNames(
				new ObjectName("*:*,cimclass=SAP_ITSAMJ2eeNode,SAP_J2EEClusterNode="+ nodeId), null);
		ObjectName j2eeNode = result.iterator().next();

		Properties sysProperties = System.getProperties();
		String sysName = sysProperties.getProperty("SAPSYSTEMNAME");
		String dbHost = sysProperties.getProperty("j2ee.dbhost");

		String clusterMBeanName = "com.sap.default:cimclass=SAP_ITSAMTransactionServiceManagementCluster,version=3.3,"
				+ "SAP_ITSAMJ2eeCluster.CreationClassName=SAP_ITSAMJ2eeCluster,"
				+ "SAP_ITSAMJ2eeCluster.Name=" + sysName+ ".SystemName." + dbHost
				+ ",SAP_ITSAMTransactionServiceManagementCluster.SystemName=SAP_ITSAMTransactionServiceManagementCluster,"
				+ "SAP_ITSAMTransactionServiceManagementCluster.CreationClassName=SAP_ITSAMTransactionServiceManagementCluster,"
				+ "SAP_ITSAMTransactionServiceManagementCluster.Name=SAP_ITSAMTransactionServiceManagementCluster," 
				+ "SAP_ITSAMTransactionServiceManagementCluster.SystemCreationClassName=SAP_ITSAMTransactionServiceManagementCluster,"
				+ "type=SAP_ITSAMJ2eeCluster.SAP_ITSAMTransactionServiceManagementCluster";

		String nodeMBeanName = "com.sap.default:cimclass=SAP_ITSAMTransactionServiceManagementNode,version=3.3,"
				+ "type=SAP_ITSAMJ2eeCluster.SAP_ITSAMJ2eeInstance.SAP_ITSAMJ2eeNode.SAP_ITSAMTransactionServiceManagementNode,"
				+ "SAP_ITSAMTransactionServiceManagementNode.ElementName=SAP_ITSAMTransactionServiceManagementNode,"
				+ "SAP_ITSAMTransactionServiceManagementNode.SystemName=SAP_ITSAMTransactionServiceManagementNode,"
				+ "SAP_ITSAMTransactionServiceManagementNode.CreationClassName=SAP_ITSAMTransactionServiceManagementNode,"
				+ "SAP_ITSAMTransactionServiceManagementNode.Name=SAP_ITSAMTransactionServiceManagementNode,"
				+ "SAP_ITSAMTransactionServiceManagementNode.SystemCreationClassName=SAP_ITSAMTransactionServiceManagementNode"

				+ ",SAP_ITSAMJ2eeCluster.Name="
				+ j2eeCluster.getKeyProperty("SAP_ITSAMJ2eeCluster.Name")

				+ ",SAP_ITSAMJ2eeCluster.CreationClassName="
				+ j2eeCluster.getKeyProperty("SAP_ITSAMJ2eeCluster.CreationClassName")

				+ ",SAP_ITSAMJ2eeInstance.J2eeInstanceID="
				+ j2eeNode.getKeyProperty("SAP_ITSAMJ2eeInstance.J2eeInstanceID")

				+ ",SAP_ITSAMJ2eeInstance.CreationClassName="
				+ j2eeNode.getKeyProperty("SAP_ITSAMJ2eeInstance.CreationClassName")

				+ ",SAP_ITSAMJ2eeInstance.Name="
				+ j2eeNode.getKeyProperty("SAP_ITSAMJ2eeInstance.Name")

				+ ",SAP_ITSAMJ2eeNode.CreationClassName="
				+ j2eeNode.getKeyProperty("SAP_ITSAMJ2eeNode.CreationClassName")

				+ ",SAP_ITSAMJ2eeNode.Name="
				+ j2eeNode.getKeyProperty("SAP_ITSAMJ2eeNode.Name")

				+ ",SAP_J2EEClusterNode="
				+ j2eeNode.getKeyProperty("SAP_J2EEClusterNode");

		return new String[] { clusterMBeanName, nodeMBeanName };
	}
}

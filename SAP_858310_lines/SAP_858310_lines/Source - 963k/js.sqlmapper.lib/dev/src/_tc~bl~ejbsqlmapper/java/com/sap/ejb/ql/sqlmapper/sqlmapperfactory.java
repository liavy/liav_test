package com.sap.ejb.ql.sqlmapper;

import com.sap.ejb.ql.sqlmapper.SQLMapper;
import com.sap.ejb.ql.sqlmapper.SQLMappingException;
import com.sap.ejb.ql.sqlmapper.general.SQLMapperImplementation;
import com.sap.ejb.ql.sqlmapper.general.SysLog;
import com.sap.ejb.ql.sqlmapper.general.DevTrace;

import com.sap.engine.interfaces.ejb.orMapping.CommonORMapping;

import com.sap.tc.logging.Location;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * SQL mapper factory creates SQL mappers for given OR bean mappings.
 * The actual implementation chosen for the <code>SQLMapper</code> instance is defined
 * by default property <code>com.sap.ejb.ql.sqlmapper.SQLMapper</code>. Default poperties
 * are set by default properties file <code>sqlm.properties</code> and local properties
 * file <code>sqlmlocal.properties</code>.
 * <p></p>
 * An OR mapping is bound permanently to an <code>SQLMapper</code>. Thus you might
 * want to create only one <code>SQLMapper</code> instance per OR mapping, as
 * <code>SQLMapper</code> implementations might use OR mapping caches for increased
 * performance.
 * </p><p>
 * <code>SQLMapperFactory</code> accepts a class as valid implementation of interface
 * <code>SQLMapper</code> if and only if that class extends <code>SQLMapperImplementation</code>.
 * Current default for <code>com.sap.ejb.ql.sqlmapper.SQLMapper</code> is 
 * <code>com.sap.ejb.ql.sqlmapper.common.CommonSQLMapper</code>.
 * </p><p>
 * Copyright (c) 2002-2004, SAP-AG
 * @author Rainer Schweigkoffer, Dirk Debertin
 * @version 1.1
 * @see SQLMapper
 * @see com.sap.ejb.ql.sqlmapper.general.SQLMapperImplementation
 * @see com.sap.engine.interfaces.ejb.orMapping.CommonORMapping
 * @see com.sap.engine.interfaces.ejb.orMapping.SchemaORMapping
 */

public class SQLMapperFactory {
        private static final Location loc = Location.getLocation(SQLMapperFactory.class);
        private static final String createSQLMapper = "createSQLMapper";
        private static final String setDefaultProperties = "setDefaultProperties";
        private static final String mergeLocalProperties = "mergeLocalProperties";
        private static final String createSQLMapperParms[] 
                                           = { "orMapping", "runtimeProperties" };

	// Name of file containing SQLMapper default properties 
	private static final String defaultFileName = "sqlm.properties";
	// Name of optional file adding/replacing additional default properties 
	private static final String localFileName = "sqlmlocal.properties";
	// Joined SQLMapper properties 
	private static Properties defaultProperties = null;

	private static String SQLMapperImpl = null;
	private static Class SQLMapperClass = null;

	// prevent class from instantiation
	private SQLMapperFactory() {
	}

	/**
	 * Creates an <code>SQLMapper</code> instance for a given OR mapping.
         * The actual implementation chosen for the <code>SQLMapper</code> instance is defined
         * by default property <code>com.sap.ejb.ql.sqlmapper.SQLMapper</code>.
	 * <p></p>
	 * @param orMapping
	 * 		OR mapping of an ejb abstract schema.
	 * @return
	 * 		a <code>SQLMapper</code> instance.
	 * @throws SQLMappingException
	 * 		if no <code>SQLMapper</code> instance can be created.
	 */
	public static SQLMapper createSQLMapper(CommonORMapping orMapping)
		throws SQLMappingException {
		return createSQLMapper(orMapping, null);
	}

	/**
         * Creates an <code>SQLMapper</code> instance for a given OR mapping.
         * The actual implementation chosen for the <code>SQLMapper</code> instance is defined
         * by default property <code>com.sap.ejb.ql.sqlmapper.SQLMapper</code>.
	 * <p></p>
	 * @param orMapping
	 *     	OR mapping of an ejb abstract schema.
	 * @param runtimeProperties
	 *     	Runtime properties for the SQLMapper to be created.<BR>
	 * 		<b>Note</b> that runtime properties take precedence over
	 * 		<code>SQLMapperFactory</code>'s internal default properties.
	 * 		It is, however, not allowed to set property <code>com.sap.ejb.ql.sqlmapper.SQLMapper</code>
	 * 		as runtime property.
	 * @return
	 * 		a <code>SQLMapper</code> instance.
	 * @throws SQLMappingException
	 * 		if no <code>SQLMapper</code> instance can be created.
	 */
	public static SQLMapper createSQLMapper(
		CommonORMapping orMapping,
		Properties runtimeProperties)
		throws SQLMappingException {

                if ( DevTrace.isOnDebugLevel(loc) )
                {
                  Object inputValues[] = { orMapping, runtimeProperties };
                  DevTrace.entering(loc, createSQLMapper, createSQLMapperParms, inputValues);
                }

		synchronized (defaultFileName) {
			if (defaultProperties == null) {
				setDefaultProperties();
				SQLMapperImpl =
					defaultProperties.getProperty(
						SQLMapperFactory.class.getPackage().getName()
							+ ".SQLMapper");

				if (SQLMapperImpl == null) {
					defaultProperties = null;
                                        DevTrace.exitingWithException(loc, createSQLMapper);
					throw new SQLMappingException(
						"No SQLMapper implementation defined.",
						"Property "
							+ SQLMapperFactory.class.getPackage().getName()
							+ "SQLMapper"
							+ "has neither been defined in "
							+ defaultFileName
							+ " nor in "
							+ localFileName
							+ " file.",
						"EJB005");
				}
			}
		}

		Properties properties = new Properties(defaultProperties);

		if (runtimeProperties != null) {
                        DevTrace.displayProperties(loc, "runtime properties", 
                                                        runtimeProperties);

			String rtSQLMapperImpl =
				runtimeProperties.getProperty(
					SQLMapperFactory.class.getPackage().getName()
						+ ".SQLMapper");
			if (rtSQLMapperImpl != null) {
                                DevTrace.exitingWithException(loc, createSQLMapper);
				throw new SQLMappingException(
					"Illegal attempt to override SQLMapper "
						+ "implementation at runtime call.",
					"An attempt was made to override SQL mapper "
						+ "implementation, defined as "
						+ SQLMapperImpl
						+ " within property files "
						+ defaultFileName
						+ " and "
						+ localFileName
						+ ", by runtime property "
						+ " value "
						+ rtSQLMapperImpl
						+ ". This is illegal "
						+ " and an error of the calling side.",
					"EJB013");
			}
			properties.putAll(runtimeProperties);
		}

                SysLog.displayProperties(loc, properties);

		SQLMapper sqlMapper;

		try {
			if (SQLMapperClass == null) {
				SQLMapperClass =
					SQLMapperFactory.class.getClassLoader().loadClass(
						SQLMapperImpl);
			}
			SQLMapperImplementation sqlMapperImpl =
				(SQLMapperImplementation) SQLMapperClass.newInstance();
                        SysLog.info(loc, "SQLMapper implementation is " + SQLMapperImpl);
			sqlMapperImpl.setProperties(properties);
			sqlMapperImpl.setORMapping(orMapping);
			sqlMapper = (SQLMapper) sqlMapperImpl;
		} catch (Exception ex) {
                        DevTrace.exitingWithException(loc, createSQLMapper);
			throw new SQLMappingException(
				"SQL Mapper Implementation "
					+ SQLMapperImpl
					+ " could not be loaded.",
				"Loading of "
					+ SQLMapperImpl
					+ " has failed."
					+ " Please check settings for property "
					+ SQLMapperFactory.class.getPackage().getName()
					+ "SQLMapper"
					+ " in files "
					+ defaultFileName
					+ " and (if present)"
					+ localFileName
					+ ". See also attached exception.",
				"EJB007",
				ex);
		}

                DevTrace.exiting(loc, createSQLMapper, sqlMapper);
		return sqlMapper;
	}

        /**
         * Retrieves expected name for local properties file.
         * </p><p>
         * @return
         *    name of local properties file.
         */
        // method used by detailed message of an SQLMappingException within Common implementation
        // of SQLMapper
        public static String getLocalFileName()
        {
          return localFileName;
        }

	/**
	 * Loads default properties from default file name and calls mergeLocalProperties()
	 * to merge with local properties. Default properties file is expected within same
	 * package as this factory.
	 * <p></p>
	 * @throws SQLMappingException
	 * 		if default proeprties can not be loaded.
	 */
	private static void setDefaultProperties() throws SQLMappingException {

                DevTrace.entering(loc, setDefaultProperties, null, null);

		/* Load mandatory default properties from file <defaultFileName> */
		defaultProperties = new Properties();
                if ( SQLMapperFactory.class.getPackage() == null )
                {
                  DevTrace.exitingWithException(loc, setDefaultProperties);
                  throw new SQLMappingException("Deficient JVM encountered.",
                          "The JVM currently used does not implement the getPackage method"
                          + " of class Class. Please kindly replace JVM currently used"
                          + " by a more reasonable implementation.",
                          "EJB015");
                }
		String defaultPath =
			SQLMapperFactory.class.getPackage().getName().replace('.', '/')
				+ "/"
				+ defaultFileName;
		InputStream in =
			SQLMapperFactory.class.getClassLoader().getResourceAsStream(
				defaultPath);
		if (in == null) {
                        DevTrace.exitingWithException(loc, setDefaultProperties);
			throw new SQLMappingException(
				"Default properties file " + defaultPath + " not found.",
				"The default properties file should be contained"
					+ " in the SQL Mapper archive. Please recheck your .jar file.",
				"EJB009");
		}
		try {
			defaultProperties.load(in);
		} catch (IOException ex) {
                        DevTrace.exitingWithException(loc, setDefaultProperties);
			throw new SQLMappingException(
				"Failed to load properties file " + defaultPath + ".",
				"Please check the attached IOException.",
				"EJB011",
				ex);
		} finally {
			try {
				in.close();
			} catch (IOException ex) {
                                // $JL-EXC$ as JLin is not satisfied with evaluating
                                // the exception's message only below
                                // see internal CSS 0120031469 0000684920 2004

                                SysLog.catching(loc, ex.getMessage());
			}
		}

                DevTrace.displayProperties(loc, "default properties", defaultProperties);

		/* Merge with optional properties from file <localFileName> */
		mergeLocalProperties();

                DevTrace.exiting(loc, setDefaultProperties);
                return;
	}

	/**
	 * Merges properties from local properties file into default properties.
	 * Local file is expected within class path or local working directory
	 * and ignored if not found. Mind that local properties take precedence
	 * over default properties.
	 */
	private static void mergeLocalProperties() {

                DevTrace.entering(loc, mergeLocalProperties, null, null);

		InputStream in = null;

		try {

			/* Search in the class path */
			in =
				SQLMapperFactory.class.getClassLoader().getResourceAsStream(
					localFileName);
			if (in == null) {
				/* Search in the working directory */
				in = new FileInputStream(localFileName);
			}
			if (in != null) {
				Properties props = new Properties();
				props.load(in);
                                DevTrace.displayProperties(loc, "local properties", props);

				/* Add/replace properties */
				defaultProperties.putAll(props);
			}
		} catch (IOException ex) {
                                // $JL-EXC$ as JLin is not satisfied with evaluating
                                // the exception's message only below
                                // see internal CSS 0120031469 0000684920 2004

                                SysLog.catching(loc, ex.getMessage());
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException ex) {
                                // $JL-EXC$ as JLin is not satisfied with evaluating
                                // the exception's message only below
                                // see internal CSS 0120031469 0000684920 2004

                                        SysLog.catching(loc, ex.getMessage());
				}
			}
                        DevTrace.exiting(loc, mergeLocalProperties);
		}
	}

}

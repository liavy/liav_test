package com.sap.ejb.ql.sqlmapper.general;


import com.sap.ejb.ql.sqlmapper.SQLMapper;
import com.sap.ejb.ql.sqlmapper.SQLMappingException;
import com.sap.ejb.ql.sqlmapper.SQLMapperFactory;
import com.sap.ejb.ql.sqlmapper.general.DevTrace;

import com.sap.engine.interfaces.ejb.orMapping.CommonORMapping;

import com.sap.tc.logging.Location;

import java.util.Properties;

/**
 * Any class extending abstract class <code>SQLMapperImplementation</code>
 * is considered a valid implementation of the <code>SQLMapper</code>
 * interface by class <code>SQLMapperFactory</code>.
 * </p><p>
 * Consequently, class <code>SQLMapperImplementation</code> implements
 * interface <code>SQLMapper</code>. It furthermore offers two
 * methods for setting of properties and underlying OR mapping, which
 * are used by <code>SQLMapperFactory</code> at creation of a
 * <code>SQLMapper</code> instance.
 * <p></p>
 * Copyright (c) 2002-2004, SAP-AG
 * @author Rainer Schweigkoffer, Dirk Debertin
 * @version 1.0
 * @see com.sap.ejb.ql.sqlmapper.SQLMapper
 * @see com.sap.ejb.ql.sqlmapper.SQLMapperFactory
 */


public abstract class SQLMapperImplementation implements SQLMapper {
	/**
	 * Runtime properties.
	 */
	protected Properties properties;

	/**
	 * Underlying OR mapping.
	 */
	protected CommonORMapping orMapping;

        private static final Location loc = Location.getLocation(SQLMapperImplementation.class);
        private static final String setProperties = "setProperties";
        private static final String setORMapping  = "setORMapping";
        private static final String setPropertiesParms[] = { "props" };
        private static final String setORMappingParms[]  = { "orMapping" };

	/**
	 * Default constructor - to be invoked by <code>SQLMapperFactory</code> only.
	 */
	public SQLMapperImplementation () {
	}

	/**
	 * Sets the <code>SQLMapper</code>'s instance's runtime properties.
         * This method may only be called once during the life time of
         * an <code>SQLMapper</code> instance.
	 * <p></p>
	 * @param props
	 * 		runtime properties.
	 * @throws SQLMappingException
	 * 		if method is invoked more than once.
	 */
	public final void setProperties(Properties props)
		throws SQLMappingException {

                if ( DevTrace.isOnDebugLevel(loc) )
                {
                  Object inputValues[] = { props };
                  DevTrace.entering(loc, setProperties, setPropertiesParms, inputValues);
                }

		if (this.properties != null) {
			String Implementation =
				this.properties.getProperty(
					SQLMapperFactory.class.getPackage().getName()
						+ ".SQLMapper");

                        DevTrace.exitingWithException(loc, setProperties);
			throw new SQLMappingException(
				"Attempt to overwrite SQLMapper properties.",
				"It was attempted to set SQL Mapper properties, though "
					+ "these properties have already been set. This is an error "
					+ "from the calling side. Called implementation is "
					+ Implementation
					+ ".",
				"EJB001");
		}

		this.properties = props;

                DevTrace.exiting(loc, setProperties);
                return;
	}

	/**
	 * Sets the underlying OR mapping for <code>SQLMapper</code> instance.
         * This method may only be called once during the life time of
         * an <code>SQLMapper</code> instance.
	 * <p></p>
	 * 
	 * @param orMapping
	 * 		an <code>ORMapping</code>.
	 * @throws SQLMappingException
         *              if method is invoked more than once.
         * @see com.sap.engine.interfaces.ejb.orMapping.CommonORMapping
         * @see com.sap.engine.interfaces.ejb.orMapping.SchemaORMapping
	 */
	public final void setORMapping(CommonORMapping orMapping)
		throws SQLMappingException {
                if ( DevTrace.isOnDebugLevel(loc) )
                {
                  Object inputValues[]  = { orMapping };
                  DevTrace.entering(loc, setORMapping, setORMappingParms, inputValues);
                }

		if (this.orMapping != null) {
			String Implementation =
				this.properties.getProperty(
					SQLMapperFactory.class.getPackage().getName()
						+ ".SQLMapper");

                        DevTrace.exitingWithException(loc, setORMapping);
			throw new SQLMappingException(
				"Attempt to overwrite SQLMapper's OR mapping.",
				"It was attempted to set SQL Mapper's OR mapping, though "
					+ "this mapping has already been set. his is an error "
					+ "from the calling side. Called implementation is "
					+ Implementation
					+ ".",
				"EJB003");
		}

		this.orMapping = orMapping;

                DevTrace.exiting(loc, setORMapping);
                return;	
        }

}

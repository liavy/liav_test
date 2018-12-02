package com.sap.ejb.ql.sqlmapper.common;

import com.sap.ejb.ql.sqlmapper.SQLMappingException;
import com.sap.ejb.ql.sqlmapper.common.LiteralValue;

import com.sap.tc.logging.Location;
import com.sap.ejb.ql.sqlmapper.general.DevTrace;

/**
 * This class offers static functions for mapping literal values
 * from one type to another. Mapping is done with respect to a given 
 * jdbc type. 
 * </p><p>
 * For the moment only mapping of literal values of type boolean has been implemented.
 * Currently supported jdbc types to be mapped to are <code>java.sql.BIT</code>
 * and <code>java.sql.SMALLINT</code>.
 * <p></p>
 * Copyright (c) 2002-2003, SAP-AG
 * @author Rainer Schweigkoffer, Dirk Debertin
 * @version 1.0
 */

public class LiteralValueMapper {

        private static final Location loc = Location.getLocation(LiteralValueMapper.class);
        private static final String mapBoolean = "mapBoolean";
        private static final String mapBooleanParms[] = { "literal", "jdbcType" };
        
        // to prevent accidental instantiation
	private LiteralValueMapper() {
	}

	/**
	 * Maps a boolean literal value to a numeric literal value.
	 * The mapping from boolean to numeric is only supported
	 * for the underlying jdbc types BIT and SMALLINT for the 
	 * boolean literal value. In case of an other
	 * jdbc type, an SQLMappingException is thrown. Also if
	 * the literal value is not of type boolean, 
	 * an SQLMappingException is thrown,
	 * because a mapping is not existing for literal value
	 * types other than boolean.
	 */
	static LiteralValue mapBoolean(LiteralValue literal, int jdbcType)
		throws SQLMappingException {
                if ( DevTrace.isOnDebugLevel(loc) )
                {
                  Object inputValues[] = { literal, new Integer(jdbcType) };
                  DevTrace.entering(loc, mapBoolean, mapBooleanParms, inputValues);
                }

                LiteralValue result;
		if (literal.isBoolean()) {
			if ((jdbcType == java.sql.Types.BIT)
				|| (jdbcType == java.sql.Types.SMALLINT)) {
				result = new LiteralValue(
					  literal.getValue().equalsIgnoreCase("TRUE") ? "1" : "0",
			   		  false,
					  true,
                                          false,
					  false);
                                DevTrace.exiting(loc, mapBoolean, result);
                                return result;
			}

                        DevTrace.exitingWithException(loc, mapBoolean);
			throw new SQLMappingException(
				"Unsupported mapping for abstract bean type boolean.",
				"You have mapped a bean field of type boolean to a jdbc "
					+ "type that is not supported for this mapping. "
					+ "jdbc type number is "
					+ jdbcType
					+ ". Please kindly "
					+ "check your or-mapping for proper mapping of boolean bean fields.",
				"CSM069");
		}

                DevTrace.exitingWithException(loc, mapBoolean);
		throw new SQLMappingException(
			"Literal to be mapped is not boolean.",
			"An attempt was made to apply boolean type mapping to "
				+ "literal that is not of boolean type. This is an internal "
				+ "programming error of the sql mapper. Literal Value is "
				+ literal.toString()
				+ ".",
			"CSM071");
	}
}

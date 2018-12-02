package com.sap.ejb.ql.sqlmapper.common;

/**
 * Defines a literal value via a String representation and
 * flags that indicate the type of the literal value.
 * There currently exist five different literal value types:
 * String literals, boolean literals, integer literals, decimal
 * literals and float literals.
 * <p></p>
 * Copyright (c) 2002-2003, SAP-AG
 * @author Rainer Schweigkoffer, Dirk Debertin
 * @version 1.0
 */

public class LiteralValue {
	private String value;
	private boolean isBoolean;
	private boolean isNumeric;
        private boolean isDecimal;
	private boolean isFloaty;

	LiteralValue(
		String value,
		boolean isBoolean,
		boolean isNumeric,
                boolean isDecimal,
		boolean isFloaty) {
			
		this.value = value;
		this.isBoolean = isBoolean;
		this.isNumeric = isNumeric;
                this.isDecimal = isDecimal;
		this.isFloaty = isFloaty;
	}

	LiteralValue(String value, boolean isBoolean, boolean isNumeric) {
		this(value, isBoolean, isNumeric, false, false);
	}

	String getValue() {
		return this.value;
	}

	boolean isBoolean() {
		return this.isBoolean;
	}

	boolean isNumeric() {
		return this.isNumeric;
	}

        boolean isDecimal()
        {
          return this.isDecimal;
        }

	boolean isFloaty() {
		return this.isFloaty;
	}

	/**
	 * Gets a String representation of this LiteralValue object.
	 * <p></p>
	 * @return
	 * 		a <code>String</code> representation of this object.
	 */
	public String toString() {
		StringBuffer strBuf = new StringBuffer(this.value);
		if (this.isBoolean || this.isNumeric || this.isFloaty) {
			strBuf.append("(");
			if (this.isBoolean) {
				strBuf.append(" boolean ");
			}
			if (this.isNumeric) {
				strBuf.append(" numeric ");
			}
                        if ( this.isDecimal )
                        {
                          strBuf.append(" decimal ");
                        }
			if (this.isFloaty) {
				strBuf.append(" floaty ");
			}
			strBuf.append(")");
		}

		return strBuf.toString();
	}
}


package com.sap.sapcontrol;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;


/**
 * <p>Java class for RESTRICTION-TYPE.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="RESTRICTION-TYPE">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="SAPControl-RESTRICT-NONE"/>
 *     &lt;enumeration value="SAPControl-RESTRICT-INT"/>
 *     &lt;enumeration value="SAPControl-RESTRICT-FLOAT"/>
 *     &lt;enumeration value="SAPControl-RESTRICT-INTRANGE"/>
 *     &lt;enumeration value="SAPControl-RESTRICT-FLOATRANGE"/>
 *     &lt;enumeration value="SAPControl-RESTRICT-ENUM"/>
 *     &lt;enumeration value="SAPControl-RESTRICT-BOOL"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlEnum
public enum RESTRICTIONTYPE {

    @XmlEnumValue("SAPControl-RESTRICT-BOOL")
    SAP_CONTROL_RESTRICT_BOOL("SAPControl-RESTRICT-BOOL"),
    @XmlEnumValue("SAPControl-RESTRICT-ENUM")
    SAP_CONTROL_RESTRICT_ENUM("SAPControl-RESTRICT-ENUM"),
    @XmlEnumValue("SAPControl-RESTRICT-FLOAT")
    SAP_CONTROL_RESTRICT_FLOAT("SAPControl-RESTRICT-FLOAT"),
    @XmlEnumValue("SAPControl-RESTRICT-FLOATRANGE")
    SAP_CONTROL_RESTRICT_FLOATRANGE("SAPControl-RESTRICT-FLOATRANGE"),
    @XmlEnumValue("SAPControl-RESTRICT-INT")
    SAP_CONTROL_RESTRICT_INT("SAPControl-RESTRICT-INT"),
    @XmlEnumValue("SAPControl-RESTRICT-INTRANGE")
    SAP_CONTROL_RESTRICT_INTRANGE("SAPControl-RESTRICT-INTRANGE"),
    @XmlEnumValue("SAPControl-RESTRICT-NONE")
    SAP_CONTROL_RESTRICT_NONE("SAPControl-RESTRICT-NONE");
    private final String value;

    RESTRICTIONTYPE(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static RESTRICTIONTYPE fromValue(String v) {
        for (RESTRICTIONTYPE c: RESTRICTIONTYPE.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v.toString());
    }

}

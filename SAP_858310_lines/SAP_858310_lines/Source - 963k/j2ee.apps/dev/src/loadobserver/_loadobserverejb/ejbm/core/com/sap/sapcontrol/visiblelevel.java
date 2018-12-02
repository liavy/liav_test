
package com.sap.sapcontrol;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;


/**
 * <p>Java class for VISIBLE-LEVEL.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="VISIBLE-LEVEL">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="SAPControl-UNKNOWN"/>
 *     &lt;enumeration value="SAPControl-OPERATOR"/>
 *     &lt;enumeration value="SAPControl-EXPERT"/>
 *     &lt;enumeration value="SAPControl-DEVELOPER"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlEnum
public enum VISIBLELEVEL {

    @XmlEnumValue("SAPControl-DEVELOPER")
    SAP_CONTROL_DEVELOPER("SAPControl-DEVELOPER"),
    @XmlEnumValue("SAPControl-EXPERT")
    SAP_CONTROL_EXPERT("SAPControl-EXPERT"),
    @XmlEnumValue("SAPControl-OPERATOR")
    SAP_CONTROL_OPERATOR("SAPControl-OPERATOR"),
    @XmlEnumValue("SAPControl-UNKNOWN")
    SAP_CONTROL_UNKNOWN("SAPControl-UNKNOWN");
    private final String value;

    VISIBLELEVEL(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static VISIBLELEVEL fromValue(String v) {
        for (VISIBLELEVEL c: VISIBLELEVEL.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v.toString());
    }

}

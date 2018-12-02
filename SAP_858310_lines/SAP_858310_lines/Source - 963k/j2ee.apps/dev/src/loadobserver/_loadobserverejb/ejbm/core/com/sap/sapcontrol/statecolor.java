
package com.sap.sapcontrol;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;


/**
 * <p>Java class for STATE-COLOR.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="STATE-COLOR">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="SAPControl-GRAY"/>
 *     &lt;enumeration value="SAPControl-GREEN"/>
 *     &lt;enumeration value="SAPControl-YELLOW"/>
 *     &lt;enumeration value="SAPControl-RED"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlEnum
public enum STATECOLOR {

    @XmlEnumValue("SAPControl-GRAY")
    SAP_CONTROL_GRAY("SAPControl-GRAY"),
    @XmlEnumValue("SAPControl-GREEN")
    SAP_CONTROL_GREEN("SAPControl-GREEN"),
    @XmlEnumValue("SAPControl-RED")
    SAP_CONTROL_RED("SAPControl-RED"),
    @XmlEnumValue("SAPControl-YELLOW")
    SAP_CONTROL_YELLOW("SAPControl-YELLOW");
    private final String value;

    STATECOLOR(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STATECOLOR fromValue(String v) {
        for (STATECOLOR c: STATECOLOR.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v.toString());
    }

}

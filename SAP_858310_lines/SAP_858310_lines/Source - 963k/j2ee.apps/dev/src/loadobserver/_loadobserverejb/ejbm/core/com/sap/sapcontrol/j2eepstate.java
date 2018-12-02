
package com.sap.sapcontrol;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;


/**
 * <p>Java class for J2EE-PSTATE.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="J2EE-PSTATE">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="SAPControl-J2EE-STOPPED"/>
 *     &lt;enumeration value="SAPControl-J2EE-STARTING"/>
 *     &lt;enumeration value="SAPControl-J2EE-CORE-RUNNING"/>
 *     &lt;enumeration value="SAPControl-J2EE-RUNNING"/>
 *     &lt;enumeration value="SAPControl-J2EE-STOPPING"/>
 *     &lt;enumeration value="SAPControl-J2EE-MAINTENANCE"/>
 *     &lt;enumeration value="SAPControl-J2EE-UNKNOWN"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlEnum
public enum J2EEPSTATE {

    @XmlEnumValue("SAPControl-J2EE-CORE-RUNNING")
    SAP_CONTROL_J_2_EE_CORE_RUNNING("SAPControl-J2EE-CORE-RUNNING"),
    @XmlEnumValue("SAPControl-J2EE-MAINTENANCE")
    SAP_CONTROL_J_2_EE_MAINTENANCE("SAPControl-J2EE-MAINTENANCE"),
    @XmlEnumValue("SAPControl-J2EE-RUNNING")
    SAP_CONTROL_J_2_EE_RUNNING("SAPControl-J2EE-RUNNING"),
    @XmlEnumValue("SAPControl-J2EE-STARTING")
    SAP_CONTROL_J_2_EE_STARTING("SAPControl-J2EE-STARTING"),
    @XmlEnumValue("SAPControl-J2EE-STOPPED")
    SAP_CONTROL_J_2_EE_STOPPED("SAPControl-J2EE-STOPPED"),
    @XmlEnumValue("SAPControl-J2EE-STOPPING")
    SAP_CONTROL_J_2_EE_STOPPING("SAPControl-J2EE-STOPPING"),
    @XmlEnumValue("SAPControl-J2EE-UNKNOWN")
    SAP_CONTROL_J_2_EE_UNKNOWN("SAPControl-J2EE-UNKNOWN");
    private final String value;

    J2EEPSTATE(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static J2EEPSTATE fromValue(String v) {
        for (J2EEPSTATE c: J2EEPSTATE.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v.toString());
    }

}

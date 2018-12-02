
package com.sap.sapcontrol;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;


/**
 * <p>Java class for StartStopOption.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="StartStopOption">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="SAPControl-ALL-INSTANCES"/>
 *     &lt;enumeration value="SAPControl-SCS-INSTANCES"/>
 *     &lt;enumeration value="SAPControl-DIALOG-INSTANCES"/>
 *     &lt;enumeration value="SAPControl-ABAP-INSTANCES"/>
 *     &lt;enumeration value="SAPControl-J2EE-INSTANCES"/>
 *     &lt;enumeration value="SAPControl-PRIORITY-LEVEL"/>
 *     &lt;enumeration value="SAPControl-TREX-INSTANCES"/>
 *     &lt;enumeration value="SAPControl-ENQREP-INSTANCES"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlEnum
public enum StartStopOption {

    @XmlEnumValue("SAPControl-ABAP-INSTANCES")
    SAP_CONTROL_ABAP_INSTANCES("SAPControl-ABAP-INSTANCES"),
    @XmlEnumValue("SAPControl-ALL-INSTANCES")
    SAP_CONTROL_ALL_INSTANCES("SAPControl-ALL-INSTANCES"),
    @XmlEnumValue("SAPControl-DIALOG-INSTANCES")
    SAP_CONTROL_DIALOG_INSTANCES("SAPControl-DIALOG-INSTANCES"),
    @XmlEnumValue("SAPControl-ENQREP-INSTANCES")
    SAP_CONTROL_ENQREP_INSTANCES("SAPControl-ENQREP-INSTANCES"),
    @XmlEnumValue("SAPControl-J2EE-INSTANCES")
    SAP_CONTROL_J_2_EE_INSTANCES("SAPControl-J2EE-INSTANCES"),
    @XmlEnumValue("SAPControl-PRIORITY-LEVEL")
    SAP_CONTROL_PRIORITY_LEVEL("SAPControl-PRIORITY-LEVEL"),
    @XmlEnumValue("SAPControl-SCS-INSTANCES")
    SAP_CONTROL_SCS_INSTANCES("SAPControl-SCS-INSTANCES"),
    @XmlEnumValue("SAPControl-TREX-INSTANCES")
    SAP_CONTROL_TREX_INSTANCES("SAPControl-TREX-INSTANCES");
    private final String value;

    StartStopOption(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static StartStopOption fromValue(String v) {
        for (StartStopOption c: StartStopOption.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v.toString());
    }

}

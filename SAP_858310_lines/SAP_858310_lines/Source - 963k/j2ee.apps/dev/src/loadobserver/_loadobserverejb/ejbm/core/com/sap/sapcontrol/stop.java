
package com.sap.sapcontrol;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Stop element declaration.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;element name="Stop">
 *   &lt;complexType>
 *     &lt;complexContent>
 *       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *         &lt;sequence>
 *           &lt;element name="softtimeout" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *           &lt;element name="IsSystemStop" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;/sequence>
 *       &lt;/restriction>
 *     &lt;/complexContent>
 *   &lt;/complexType>
 * &lt;/element>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "softtimeout",
    "isSystemStop"
})
@XmlRootElement(name = "Stop")
public class Stop {

    @XmlElement(defaultValue = "0")
    protected int softtimeout;
    @XmlElement(name = "IsSystemStop", defaultValue = "0")
    protected int isSystemStop;

    /**
     * Gets the value of the softtimeout property.
     * 
     */
    public int getSofttimeout() {
        return softtimeout;
    }

    /**
     * Sets the value of the softtimeout property.
     * 
     */
    public void setSofttimeout(int value) {
        this.softtimeout = value;
    }

    /**
     * Gets the value of the isSystemStop property.
     * 
     */
    public int getIsSystemStop() {
        return isSystemStop;
    }

    /**
     * Sets the value of the isSystemStop property.
     * 
     */
    public void setIsSystemStop(int value) {
        this.isSystemStop = value;
    }

}

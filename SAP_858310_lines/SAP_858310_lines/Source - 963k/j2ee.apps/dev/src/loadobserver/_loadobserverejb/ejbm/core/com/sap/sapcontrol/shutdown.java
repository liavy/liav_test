
package com.sap.sapcontrol;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Shutdown element declaration.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;element name="Shutdown">
 *   &lt;complexType>
 *     &lt;complexContent>
 *       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *         &lt;sequence>
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
    "isSystemStop"
})
@XmlRootElement(name = "Shutdown")
public class Shutdown {

    @XmlElement(name = "IsSystemStop", defaultValue = "0")
    protected int isSystemStop;

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

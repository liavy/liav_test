
package com.sap.sapcontrol;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for EnqRemoveLocks element declaration.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;element name="EnqRemoveLocks">
 *   &lt;complexType>
 *     &lt;complexContent>
 *       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *         &lt;sequence>
 *           &lt;element name="lock" type="{urn:SAPControl}ArrayOfEnqLock" minOccurs="0"/>
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
    "lock"
})
@XmlRootElement(name = "EnqRemoveLocks")
public class EnqRemoveLocks {

    @XmlElementRef(name = "lock", type = JAXBElement.class)
    protected JAXBElement<ArrayOfEnqLock> lock;

    /**
     * Gets the value of the lock property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfEnqLock }{@code >}
     *     
     */
    public JAXBElement<ArrayOfEnqLock> getLock() {
        return lock;
    }

    /**
     * Sets the value of the lock property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfEnqLock }{@code >}
     *     
     */
    public void setLock(JAXBElement<ArrayOfEnqLock> value) {
        this.lock = ((JAXBElement<ArrayOfEnqLock> ) value);
    }

}

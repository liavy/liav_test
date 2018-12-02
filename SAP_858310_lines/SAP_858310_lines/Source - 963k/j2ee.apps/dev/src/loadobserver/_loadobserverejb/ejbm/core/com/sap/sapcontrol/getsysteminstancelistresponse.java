
package com.sap.sapcontrol;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for GetSystemInstanceListResponse element declaration.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;element name="GetSystemInstanceListResponse">
 *   &lt;complexType>
 *     &lt;complexContent>
 *       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *         &lt;sequence>
 *           &lt;element name="instance" type="{urn:SAPControl}ArrayOfSAPInstance" minOccurs="0"/>
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
    "instance"
})
@XmlRootElement(name = "GetSystemInstanceListResponse")
public class GetSystemInstanceListResponse {

    @XmlElementRef(name = "instance", type = JAXBElement.class)
    protected JAXBElement<ArrayOfSAPInstance> instance;

    /**
     * Gets the value of the instance property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfSAPInstance }{@code >}
     *     
     */
    public JAXBElement<ArrayOfSAPInstance> getInstance() {
        return instance;
    }

    /**
     * Sets the value of the instance property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfSAPInstance }{@code >}
     *     
     */
    public void setInstance(JAXBElement<ArrayOfSAPInstance> value) {
        this.instance = ((JAXBElement<ArrayOfSAPInstance> ) value);
    }

}

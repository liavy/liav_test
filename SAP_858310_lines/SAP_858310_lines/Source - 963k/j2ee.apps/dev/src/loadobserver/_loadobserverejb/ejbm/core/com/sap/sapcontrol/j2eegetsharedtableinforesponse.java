
package com.sap.sapcontrol;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for J2EEGetSharedTableInfoResponse element declaration.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;element name="J2EEGetSharedTableInfoResponse">
 *   &lt;complexType>
 *     &lt;complexContent>
 *       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *         &lt;sequence>
 *           &lt;element name="jsf" type="{urn:SAPControl}ArrayOfJ2EESharedTableInfo" minOccurs="0"/>
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
    "jsf"
})
@XmlRootElement(name = "J2EEGetSharedTableInfoResponse")
public class J2EEGetSharedTableInfoResponse {

    @XmlElementRef(name = "jsf", type = JAXBElement.class)
    protected JAXBElement<ArrayOfJ2EESharedTableInfo> jsf;

    /**
     * Gets the value of the jsf property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfJ2EESharedTableInfo }{@code >}
     *     
     */
    public JAXBElement<ArrayOfJ2EESharedTableInfo> getJsf() {
        return jsf;
    }

    /**
     * Sets the value of the jsf property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfJ2EESharedTableInfo }{@code >}
     *     
     */
    public void setJsf(JAXBElement<ArrayOfJ2EESharedTableInfo> value) {
        this.jsf = ((JAXBElement<ArrayOfJ2EESharedTableInfo> ) value);
    }

}

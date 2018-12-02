
package com.sap.sapcontrol;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for GetVersionInfoResponse element declaration.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;element name="GetVersionInfoResponse">
 *   &lt;complexType>
 *     &lt;complexContent>
 *       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *         &lt;sequence>
 *           &lt;element name="version" type="{urn:SAPControl}ArrayOfInstanceVersionInfo" minOccurs="0"/>
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
    "version"
})
@XmlRootElement(name = "GetVersionInfoResponse")
public class GetVersionInfoResponse {

    @XmlElementRef(name = "version", type = JAXBElement.class)
    protected JAXBElement<ArrayOfInstanceVersionInfo> version;

    /**
     * Gets the value of the version property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfInstanceVersionInfo }{@code >}
     *     
     */
    public JAXBElement<ArrayOfInstanceVersionInfo> getVersion() {
        return version;
    }

    /**
     * Sets the value of the version property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfInstanceVersionInfo }{@code >}
     *     
     */
    public void setVersion(JAXBElement<ArrayOfInstanceVersionInfo> value) {
        this.version = ((JAXBElement<ArrayOfInstanceVersionInfo> ) value);
    }

}

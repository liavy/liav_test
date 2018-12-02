
package com.sap.sapcontrol;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for GetAccessPointListResponse element declaration.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;element name="GetAccessPointListResponse">
 *   &lt;complexType>
 *     &lt;complexContent>
 *       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *         &lt;sequence>
 *           &lt;element name="accesspoint" type="{urn:SAPControl}ArrayOfAccessPoint" minOccurs="0"/>
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
    "accesspoint"
})
@XmlRootElement(name = "GetAccessPointListResponse")
public class GetAccessPointListResponse {

    @XmlElementRef(name = "accesspoint", type = JAXBElement.class)
    protected JAXBElement<ArrayOfAccessPoint> accesspoint;

    /**
     * Gets the value of the accesspoint property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfAccessPoint }{@code >}
     *     
     */
    public JAXBElement<ArrayOfAccessPoint> getAccesspoint() {
        return accesspoint;
    }

    /**
     * Sets the value of the accesspoint property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfAccessPoint }{@code >}
     *     
     */
    public void setAccesspoint(JAXBElement<ArrayOfAccessPoint> value) {
        this.accesspoint = ((JAXBElement<ArrayOfAccessPoint> ) value);
    }

}


package com.sap.sapcontrol;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for GetAlertsResponse element declaration.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;element name="GetAlertsResponse">
 *   &lt;complexType>
 *     &lt;complexContent>
 *       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *         &lt;sequence>
 *           &lt;element name="RootTidName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *           &lt;element name="alert" type="{urn:SAPControl}ArrayOfAlert"/>
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
    "rootTidName",
    "alert"
})
@XmlRootElement(name = "GetAlertsResponse")
public class GetAlertsResponse {

    @XmlElementRef(name = "RootTidName", type = JAXBElement.class)
    protected JAXBElement<String> rootTidName;
    @XmlElement(required = true)
    protected ArrayOfAlert alert;

    /**
     * Gets the value of the rootTidName property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getRootTidName() {
        return rootTidName;
    }

    /**
     * Sets the value of the rootTidName property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setRootTidName(JAXBElement<String> value) {
        this.rootTidName = ((JAXBElement<String> ) value);
    }

    /**
     * Gets the value of the alert property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfAlert }
     *     
     */
    public ArrayOfAlert getAlert() {
        return alert;
    }

    /**
     * Sets the value of the alert property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfAlert }
     *     
     */
    public void setAlert(ArrayOfAlert value) {
        this.alert = value;
    }

}


package com.sap.sapcontrol;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ICMGetConnectionListResponse element declaration.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;element name="ICMGetConnectionListResponse">
 *   &lt;complexType>
 *     &lt;complexContent>
 *       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *         &lt;sequence>
 *           &lt;element name="connection" type="{urn:SAPControl}ArrayOfICMConnection" minOccurs="0"/>
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
    "connection"
})
@XmlRootElement(name = "ICMGetConnectionListResponse")
public class ICMGetConnectionListResponse {

    @XmlElementRef(name = "connection", type = JAXBElement.class)
    protected JAXBElement<ArrayOfICMConnection> connection;

    /**
     * Gets the value of the connection property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfICMConnection }{@code >}
     *     
     */
    public JAXBElement<ArrayOfICMConnection> getConnection() {
        return connection;
    }

    /**
     * Sets the value of the connection property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfICMConnection }{@code >}
     *     
     */
    public void setConnection(JAXBElement<ArrayOfICMConnection> value) {
        this.connection = ((JAXBElement<ArrayOfICMConnection> ) value);
    }

}

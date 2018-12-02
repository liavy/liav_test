
package com.sap.sapcontrol;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ICMGetProxyConnectionListResponse element declaration.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;element name="ICMGetProxyConnectionListResponse">
 *   &lt;complexType>
 *     &lt;complexContent>
 *       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *         &lt;sequence>
 *           &lt;element name="connection" type="{urn:SAPControl}ArrayOfICMProxyConnection" minOccurs="0"/>
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
@XmlRootElement(name = "ICMGetProxyConnectionListResponse")
public class ICMGetProxyConnectionListResponse {

    @XmlElementRef(name = "connection", type = JAXBElement.class)
    protected JAXBElement<ArrayOfICMProxyConnection> connection;

    /**
     * Gets the value of the connection property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfICMProxyConnection }{@code >}
     *     
     */
    public JAXBElement<ArrayOfICMProxyConnection> getConnection() {
        return connection;
    }

    /**
     * Sets the value of the connection property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfICMProxyConnection }{@code >}
     *     
     */
    public void setConnection(JAXBElement<ArrayOfICMProxyConnection> value) {
        this.connection = ((JAXBElement<ArrayOfICMProxyConnection> ) value);
    }

}

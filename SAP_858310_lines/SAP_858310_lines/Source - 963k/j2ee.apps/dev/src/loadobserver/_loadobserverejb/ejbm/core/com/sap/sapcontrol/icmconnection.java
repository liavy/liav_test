
package com.sap.sapcontrol;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ICMConnection complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ICMConnection">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="conid" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="protocol" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="role" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="requesttype" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="peer-address" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="peer-port" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="local-address" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="local-port" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="proc-timeout" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="keepalive-timeout" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="connection-time" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="nihdl" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ICMConnection", propOrder = {
    "conid",
    "protocol",
    "role",
    "requesttype",
    "peerAddress",
    "peerPort",
    "localAddress",
    "localPort",
    "procTimeout",
    "keepaliveTimeout",
    "connectionTime",
    "nihdl"
})
public class ICMConnection {

    @XmlElementRef(name = "conid", type = JAXBElement.class)
    protected JAXBElement<String> conid;
    @XmlElementRef(name = "protocol", type = JAXBElement.class)
    protected JAXBElement<String> protocol;
    @XmlElementRef(name = "role", type = JAXBElement.class)
    protected JAXBElement<String> role;
    @XmlElementRef(name = "requesttype", type = JAXBElement.class)
    protected JAXBElement<String> requesttype;
    @XmlElementRef(name = "peer-address", type = JAXBElement.class)
    protected JAXBElement<String> peerAddress;
    @XmlElement(name = "peer-port")
    protected int peerPort;
    @XmlElementRef(name = "local-address", type = JAXBElement.class)
    protected JAXBElement<String> localAddress;
    @XmlElement(name = "local-port")
    protected int localPort;
    @XmlElement(name = "proc-timeout")
    protected int procTimeout;
    @XmlElement(name = "keepalive-timeout")
    protected int keepaliveTimeout;
    @XmlElementRef(name = "connection-time", type = JAXBElement.class)
    protected JAXBElement<String> connectionTime;
    protected int nihdl;

    /**
     * Gets the value of the conid property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getConid() {
        return conid;
    }

    /**
     * Sets the value of the conid property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setConid(JAXBElement<String> value) {
        this.conid = ((JAXBElement<String> ) value);
    }

    /**
     * Gets the value of the protocol property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getProtocol() {
        return protocol;
    }

    /**
     * Sets the value of the protocol property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setProtocol(JAXBElement<String> value) {
        this.protocol = ((JAXBElement<String> ) value);
    }

    /**
     * Gets the value of the role property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getRole() {
        return role;
    }

    /**
     * Sets the value of the role property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setRole(JAXBElement<String> value) {
        this.role = ((JAXBElement<String> ) value);
    }

    /**
     * Gets the value of the requesttype property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getRequesttype() {
        return requesttype;
    }

    /**
     * Sets the value of the requesttype property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setRequesttype(JAXBElement<String> value) {
        this.requesttype = ((JAXBElement<String> ) value);
    }

    /**
     * Gets the value of the peerAddress property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getPeerAddress() {
        return peerAddress;
    }

    /**
     * Sets the value of the peerAddress property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setPeerAddress(JAXBElement<String> value) {
        this.peerAddress = ((JAXBElement<String> ) value);
    }

    /**
     * Gets the value of the peerPort property.
     * 
     */
    public int getPeerPort() {
        return peerPort;
    }

    /**
     * Sets the value of the peerPort property.
     * 
     */
    public void setPeerPort(int value) {
        this.peerPort = value;
    }

    /**
     * Gets the value of the localAddress property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getLocalAddress() {
        return localAddress;
    }

    /**
     * Sets the value of the localAddress property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setLocalAddress(JAXBElement<String> value) {
        this.localAddress = ((JAXBElement<String> ) value);
    }

    /**
     * Gets the value of the localPort property.
     * 
     */
    public int getLocalPort() {
        return localPort;
    }

    /**
     * Sets the value of the localPort property.
     * 
     */
    public void setLocalPort(int value) {
        this.localPort = value;
    }

    /**
     * Gets the value of the procTimeout property.
     * 
     */
    public int getProcTimeout() {
        return procTimeout;
    }

    /**
     * Sets the value of the procTimeout property.
     * 
     */
    public void setProcTimeout(int value) {
        this.procTimeout = value;
    }

    /**
     * Gets the value of the keepaliveTimeout property.
     * 
     */
    public int getKeepaliveTimeout() {
        return keepaliveTimeout;
    }

    /**
     * Sets the value of the keepaliveTimeout property.
     * 
     */
    public void setKeepaliveTimeout(int value) {
        this.keepaliveTimeout = value;
    }

    /**
     * Gets the value of the connectionTime property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getConnectionTime() {
        return connectionTime;
    }

    /**
     * Sets the value of the connectionTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setConnectionTime(JAXBElement<String> value) {
        this.connectionTime = ((JAXBElement<String> ) value);
    }

    /**
     * Gets the value of the nihdl property.
     * 
     */
    public int getNihdl() {
        return nihdl;
    }

    /**
     * Sets the value of the nihdl property.
     * 
     */
    public void setNihdl(int value) {
        this.nihdl = value;
    }

}


package com.sap.sapcontrol;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ICMProxyConnection complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ICMProxyConnection">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="conid" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="role" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="peer-address" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="peer-port" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="local-address" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="local-port" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="status" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="nihdl" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="socket" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="partner" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="internal-convid" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="external-convid" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="snc-context-length" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="failover-status" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="disconnect-time" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="objectid" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="tid-uid-mode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ICMProxyConnection", propOrder = {
    "conid",
    "role",
    "peerAddress",
    "peerPort",
    "localAddress",
    "localPort",
    "status",
    "nihdl",
    "socket",
    "partner",
    "internalConvid",
    "externalConvid",
    "sncContextLength",
    "failoverStatus",
    "disconnectTime",
    "objectid",
    "tidUidMode"
})
public class ICMProxyConnection {

    @XmlElementRef(name = "conid", type = JAXBElement.class)
    protected JAXBElement<String> conid;
    @XmlElementRef(name = "role", type = JAXBElement.class)
    protected JAXBElement<String> role;
    @XmlElementRef(name = "peer-address", type = JAXBElement.class)
    protected JAXBElement<String> peerAddress;
    @XmlElement(name = "peer-port")
    protected int peerPort;
    @XmlElementRef(name = "local-address", type = JAXBElement.class)
    protected JAXBElement<String> localAddress;
    @XmlElement(name = "local-port")
    protected int localPort;
    @XmlElementRef(name = "status", type = JAXBElement.class)
    protected JAXBElement<String> status;
    protected int nihdl;
    protected int socket;
    @XmlElementRef(name = "partner", type = JAXBElement.class)
    protected JAXBElement<String> partner;
    @XmlElementRef(name = "internal-convid", type = JAXBElement.class)
    protected JAXBElement<String> internalConvid;
    @XmlElementRef(name = "external-convid", type = JAXBElement.class)
    protected JAXBElement<String> externalConvid;
    @XmlElementRef(name = "snc-context-length", type = JAXBElement.class)
    protected JAXBElement<Integer> sncContextLength;
    @XmlElementRef(name = "failover-status", type = JAXBElement.class)
    protected JAXBElement<String> failoverStatus;
    @XmlElementRef(name = "disconnect-time", type = JAXBElement.class)
    protected JAXBElement<String> disconnectTime;
    @XmlElementRef(name = "objectid", type = JAXBElement.class)
    protected JAXBElement<String> objectid;
    @XmlElementRef(name = "tid-uid-mode", type = JAXBElement.class)
    protected JAXBElement<String> tidUidMode;

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
     * Gets the value of the status property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getStatus() {
        return status;
    }

    /**
     * Sets the value of the status property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setStatus(JAXBElement<String> value) {
        this.status = ((JAXBElement<String> ) value);
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

    /**
     * Gets the value of the socket property.
     * 
     */
    public int getSocket() {
        return socket;
    }

    /**
     * Sets the value of the socket property.
     * 
     */
    public void setSocket(int value) {
        this.socket = value;
    }

    /**
     * Gets the value of the partner property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getPartner() {
        return partner;
    }

    /**
     * Sets the value of the partner property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setPartner(JAXBElement<String> value) {
        this.partner = ((JAXBElement<String> ) value);
    }

    /**
     * Gets the value of the internalConvid property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getInternalConvid() {
        return internalConvid;
    }

    /**
     * Sets the value of the internalConvid property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setInternalConvid(JAXBElement<String> value) {
        this.internalConvid = ((JAXBElement<String> ) value);
    }

    /**
     * Gets the value of the externalConvid property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getExternalConvid() {
        return externalConvid;
    }

    /**
     * Sets the value of the externalConvid property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setExternalConvid(JAXBElement<String> value) {
        this.externalConvid = ((JAXBElement<String> ) value);
    }

    /**
     * Gets the value of the sncContextLength property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public JAXBElement<Integer> getSncContextLength() {
        return sncContextLength;
    }

    /**
     * Sets the value of the sncContextLength property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public void setSncContextLength(JAXBElement<Integer> value) {
        this.sncContextLength = ((JAXBElement<Integer> ) value);
    }

    /**
     * Gets the value of the failoverStatus property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getFailoverStatus() {
        return failoverStatus;
    }

    /**
     * Sets the value of the failoverStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setFailoverStatus(JAXBElement<String> value) {
        this.failoverStatus = ((JAXBElement<String> ) value);
    }

    /**
     * Gets the value of the disconnectTime property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getDisconnectTime() {
        return disconnectTime;
    }

    /**
     * Sets the value of the disconnectTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setDisconnectTime(JAXBElement<String> value) {
        this.disconnectTime = ((JAXBElement<String> ) value);
    }

    /**
     * Gets the value of the objectid property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getObjectid() {
        return objectid;
    }

    /**
     * Sets the value of the objectid property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setObjectid(JAXBElement<String> value) {
        this.objectid = ((JAXBElement<String> ) value);
    }

    /**
     * Gets the value of the tidUidMode property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getTidUidMode() {
        return tidUidMode;
    }

    /**
     * Sets the value of the tidUidMode property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setTidUidMode(JAXBElement<String> value) {
        this.tidUidMode = ((JAXBElement<String> ) value);
    }

}


package com.sap.sapcontrol;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for J2EEClusterMsg complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="J2EEClusterMsg">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="service" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="count" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="length" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="avg-length" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="max-length" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="count-p2p-msg" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="count-p2p-request" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="count-p2p-reply" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="count-broadcast-msg" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="count-broadcast-request" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "J2EEClusterMsg", propOrder = {
    "service",
    "id",
    "count",
    "length",
    "avgLength",
    "maxLength",
    "countP2PMsg",
    "countP2PRequest",
    "countP2PReply",
    "countBroadcastMsg",
    "countBroadcastRequest"
})
public class J2EEClusterMsg {

    @XmlElementRef(name = "service", type = JAXBElement.class)
    protected JAXBElement<String> service;
    @XmlElementRef(name = "id", type = JAXBElement.class)
    protected JAXBElement<String> id;
    protected long count;
    protected long length;
    @XmlElement(name = "avg-length")
    protected long avgLength;
    @XmlElement(name = "max-length")
    protected long maxLength;
    @XmlElement(name = "count-p2p-msg")
    protected long countP2PMsg;
    @XmlElement(name = "count-p2p-request")
    protected long countP2PRequest;
    @XmlElement(name = "count-p2p-reply")
    protected long countP2PReply;
    @XmlElement(name = "count-broadcast-msg")
    protected long countBroadcastMsg;
    @XmlElement(name = "count-broadcast-request")
    protected long countBroadcastRequest;

    /**
     * Gets the value of the service property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getService() {
        return service;
    }

    /**
     * Sets the value of the service property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setService(JAXBElement<String> value) {
        this.service = ((JAXBElement<String> ) value);
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setId(JAXBElement<String> value) {
        this.id = ((JAXBElement<String> ) value);
    }

    /**
     * Gets the value of the count property.
     * 
     */
    public long getCount() {
        return count;
    }

    /**
     * Sets the value of the count property.
     * 
     */
    public void setCount(long value) {
        this.count = value;
    }

    /**
     * Gets the value of the length property.
     * 
     */
    public long getLength() {
        return length;
    }

    /**
     * Sets the value of the length property.
     * 
     */
    public void setLength(long value) {
        this.length = value;
    }

    /**
     * Gets the value of the avgLength property.
     * 
     */
    public long getAvgLength() {
        return avgLength;
    }

    /**
     * Sets the value of the avgLength property.
     * 
     */
    public void setAvgLength(long value) {
        this.avgLength = value;
    }

    /**
     * Gets the value of the maxLength property.
     * 
     */
    public long getMaxLength() {
        return maxLength;
    }

    /**
     * Sets the value of the maxLength property.
     * 
     */
    public void setMaxLength(long value) {
        this.maxLength = value;
    }

    /**
     * Gets the value of the countP2PMsg property.
     * 
     */
    public long getCountP2PMsg() {
        return countP2PMsg;
    }

    /**
     * Sets the value of the countP2PMsg property.
     * 
     */
    public void setCountP2PMsg(long value) {
        this.countP2PMsg = value;
    }

    /**
     * Gets the value of the countP2PRequest property.
     * 
     */
    public long getCountP2PRequest() {
        return countP2PRequest;
    }

    /**
     * Sets the value of the countP2PRequest property.
     * 
     */
    public void setCountP2PRequest(long value) {
        this.countP2PRequest = value;
    }

    /**
     * Gets the value of the countP2PReply property.
     * 
     */
    public long getCountP2PReply() {
        return countP2PReply;
    }

    /**
     * Sets the value of the countP2PReply property.
     * 
     */
    public void setCountP2PReply(long value) {
        this.countP2PReply = value;
    }

    /**
     * Gets the value of the countBroadcastMsg property.
     * 
     */
    public long getCountBroadcastMsg() {
        return countBroadcastMsg;
    }

    /**
     * Sets the value of the countBroadcastMsg property.
     * 
     */
    public void setCountBroadcastMsg(long value) {
        this.countBroadcastMsg = value;
    }

    /**
     * Gets the value of the countBroadcastRequest property.
     * 
     */
    public long getCountBroadcastRequest() {
        return countBroadcastRequest;
    }

    /**
     * Sets the value of the countBroadcastRequest property.
     * 
     */
    public void setCountBroadcastRequest(long value) {
        this.countBroadcastRequest = value;
    }

}

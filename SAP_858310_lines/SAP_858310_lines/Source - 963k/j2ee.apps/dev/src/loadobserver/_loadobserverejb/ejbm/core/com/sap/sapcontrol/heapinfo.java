
package com.sap.sapcontrol;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for HeapInfo complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="HeapInfo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="processname" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="type" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="size" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="commitSize" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="maxUsedSize" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="initialSize" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="maxSize" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="dispstatus" type="{urn:SAPControl}STATE-COLOR"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HeapInfo", propOrder = {
    "processname",
    "type",
    "size",
    "commitSize",
    "maxUsedSize",
    "initialSize",
    "maxSize",
    "dispstatus"
})
public class HeapInfo {

    @XmlElementRef(name = "processname", type = JAXBElement.class)
    protected JAXBElement<String> processname;
    @XmlElementRef(name = "type", type = JAXBElement.class)
    protected JAXBElement<String> type;
    protected long size;
    protected long commitSize;
    protected long maxUsedSize;
    protected long initialSize;
    protected long maxSize;
    @XmlElement(required = true)
    protected STATECOLOR dispstatus;

    /**
     * Gets the value of the processname property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getProcessname() {
        return processname;
    }

    /**
     * Sets the value of the processname property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setProcessname(JAXBElement<String> value) {
        this.processname = ((JAXBElement<String> ) value);
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setType(JAXBElement<String> value) {
        this.type = ((JAXBElement<String> ) value);
    }

    /**
     * Gets the value of the size property.
     * 
     */
    public long getSize() {
        return size;
    }

    /**
     * Sets the value of the size property.
     * 
     */
    public void setSize(long value) {
        this.size = value;
    }

    /**
     * Gets the value of the commitSize property.
     * 
     */
    public long getCommitSize() {
        return commitSize;
    }

    /**
     * Sets the value of the commitSize property.
     * 
     */
    public void setCommitSize(long value) {
        this.commitSize = value;
    }

    /**
     * Gets the value of the maxUsedSize property.
     * 
     */
    public long getMaxUsedSize() {
        return maxUsedSize;
    }

    /**
     * Sets the value of the maxUsedSize property.
     * 
     */
    public void setMaxUsedSize(long value) {
        this.maxUsedSize = value;
    }

    /**
     * Gets the value of the initialSize property.
     * 
     */
    public long getInitialSize() {
        return initialSize;
    }

    /**
     * Sets the value of the initialSize property.
     * 
     */
    public void setInitialSize(long value) {
        this.initialSize = value;
    }

    /**
     * Gets the value of the maxSize property.
     * 
     */
    public long getMaxSize() {
        return maxSize;
    }

    /**
     * Sets the value of the maxSize property.
     * 
     */
    public void setMaxSize(long value) {
        this.maxSize = value;
    }

    /**
     * Gets the value of the dispstatus property.
     * 
     * @return
     *     possible object is
     *     {@link STATECOLOR }
     *     
     */
    public STATECOLOR getDispstatus() {
        return dispstatus;
    }

    /**
     * Sets the value of the dispstatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link STATECOLOR }
     *     
     */
    public void setDispstatus(STATECOLOR value) {
        this.dispstatus = value;
    }

}

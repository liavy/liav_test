
package com.sap.sapcontrol;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for TaskHandlerQueue complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="TaskHandlerQueue">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Typ" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Now" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="High" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="Max" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="Writes" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="Reads" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TaskHandlerQueue", propOrder = {
    "typ",
    "now",
    "high",
    "max",
    "writes",
    "reads"
})
public class TaskHandlerQueue {

    @XmlElementRef(name = "Typ", type = JAXBElement.class)
    protected JAXBElement<String> typ;
    @XmlElement(name = "Now")
    protected int now;
    @XmlElement(name = "High")
    protected int high;
    @XmlElement(name = "Max")
    protected int max;
    @XmlElement(name = "Writes")
    protected int writes;
    @XmlElement(name = "Reads")
    protected int reads;

    /**
     * Gets the value of the typ property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getTyp() {
        return typ;
    }

    /**
     * Sets the value of the typ property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setTyp(JAXBElement<String> value) {
        this.typ = ((JAXBElement<String> ) value);
    }

    /**
     * Gets the value of the now property.
     * 
     */
    public int getNow() {
        return now;
    }

    /**
     * Sets the value of the now property.
     * 
     */
    public void setNow(int value) {
        this.now = value;
    }

    /**
     * Gets the value of the high property.
     * 
     */
    public int getHigh() {
        return high;
    }

    /**
     * Sets the value of the high property.
     * 
     */
    public void setHigh(int value) {
        this.high = value;
    }

    /**
     * Gets the value of the max property.
     * 
     */
    public int getMax() {
        return max;
    }

    /**
     * Sets the value of the max property.
     * 
     */
    public void setMax(int value) {
        this.max = value;
    }

    /**
     * Gets the value of the writes property.
     * 
     */
    public int getWrites() {
        return writes;
    }

    /**
     * Sets the value of the writes property.
     * 
     */
    public void setWrites(int value) {
        this.writes = value;
    }

    /**
     * Gets the value of the reads property.
     * 
     */
    public int getReads() {
        return reads;
    }

    /**
     * Sets the value of the reads property.
     * 
     */
    public void setReads(int value) {
        this.reads = value;
    }

}

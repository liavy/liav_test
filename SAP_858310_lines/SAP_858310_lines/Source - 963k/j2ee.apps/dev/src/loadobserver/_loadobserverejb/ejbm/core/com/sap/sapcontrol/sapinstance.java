
package com.sap.sapcontrol;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SAPInstance complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SAPInstance">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="hostname" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="instanceNr" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="httpPort" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="httpsPort" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="startPriority" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="features" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
@XmlType(name = "SAPInstance", propOrder = {
    "hostname",
    "instanceNr",
    "httpPort",
    "httpsPort",
    "startPriority",
    "features",
    "dispstatus"
})
public class SAPInstance {

    @XmlElementRef(name = "hostname", type = JAXBElement.class)
    protected JAXBElement<String> hostname;
    protected int instanceNr;
    protected int httpPort;
    protected int httpsPort;
    @XmlElementRef(name = "startPriority", type = JAXBElement.class)
    protected JAXBElement<String> startPriority;
    @XmlElementRef(name = "features", type = JAXBElement.class)
    protected JAXBElement<String> features;
    @XmlElement(required = true)
    protected STATECOLOR dispstatus;

    /**
     * Gets the value of the hostname property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getHostname() {
        return hostname;
    }

    /**
     * Sets the value of the hostname property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setHostname(JAXBElement<String> value) {
        this.hostname = ((JAXBElement<String> ) value);
    }

    /**
     * Gets the value of the instanceNr property.
     * 
     */
    public int getInstanceNr() {
        return instanceNr;
    }

    /**
     * Sets the value of the instanceNr property.
     * 
     */
    public void setInstanceNr(int value) {
        this.instanceNr = value;
    }

    /**
     * Gets the value of the httpPort property.
     * 
     */
    public int getHttpPort() {
        return httpPort;
    }

    /**
     * Sets the value of the httpPort property.
     * 
     */
    public void setHttpPort(int value) {
        this.httpPort = value;
    }

    /**
     * Gets the value of the httpsPort property.
     * 
     */
    public int getHttpsPort() {
        return httpsPort;
    }

    /**
     * Sets the value of the httpsPort property.
     * 
     */
    public void setHttpsPort(int value) {
        this.httpsPort = value;
    }

    /**
     * Gets the value of the startPriority property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getStartPriority() {
        return startPriority;
    }

    /**
     * Sets the value of the startPriority property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setStartPriority(JAXBElement<String> value) {
        this.startPriority = ((JAXBElement<String> ) value);
    }

    /**
     * Gets the value of the features property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getFeatures() {
        return features;
    }

    /**
     * Sets the value of the features property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setFeatures(JAXBElement<String> value) {
        this.features = ((JAXBElement<String> ) value);
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

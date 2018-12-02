
package com.sap.sapcontrol;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for J2EEApplicationAlias complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="J2EEApplicationAlias">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="AppName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Alias" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="TotalRequests" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="AppActive" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="IgnoreCookie" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "J2EEApplicationAlias", propOrder = {
    "appName",
    "alias",
    "totalRequests",
    "appActive",
    "ignoreCookie"
})
public class J2EEApplicationAlias {

    @XmlElementRef(name = "AppName", type = JAXBElement.class)
    protected JAXBElement<String> appName;
    @XmlElementRef(name = "Alias", type = JAXBElement.class)
    protected JAXBElement<String> alias;
    @XmlElement(name = "TotalRequests")
    protected int totalRequests;
    @XmlElementRef(name = "AppActive", type = JAXBElement.class)
    protected JAXBElement<String> appActive;
    @XmlElementRef(name = "IgnoreCookie", type = JAXBElement.class)
    protected JAXBElement<String> ignoreCookie;

    /**
     * Gets the value of the appName property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getAppName() {
        return appName;
    }

    /**
     * Sets the value of the appName property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setAppName(JAXBElement<String> value) {
        this.appName = ((JAXBElement<String> ) value);
    }

    /**
     * Gets the value of the alias property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getAlias() {
        return alias;
    }

    /**
     * Sets the value of the alias property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setAlias(JAXBElement<String> value) {
        this.alias = ((JAXBElement<String> ) value);
    }

    /**
     * Gets the value of the totalRequests property.
     * 
     */
    public int getTotalRequests() {
        return totalRequests;
    }

    /**
     * Sets the value of the totalRequests property.
     * 
     */
    public void setTotalRequests(int value) {
        this.totalRequests = value;
    }

    /**
     * Gets the value of the appActive property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getAppActive() {
        return appActive;
    }

    /**
     * Sets the value of the appActive property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setAppActive(JAXBElement<String> value) {
        this.appActive = ((JAXBElement<String> ) value);
    }

    /**
     * Gets the value of the ignoreCookie property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getIgnoreCookie() {
        return ignoreCookie;
    }

    /**
     * Sets the value of the ignoreCookie property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setIgnoreCookie(JAXBElement<String> value) {
        this.ignoreCookie = ((JAXBElement<String> ) value);
    }

}

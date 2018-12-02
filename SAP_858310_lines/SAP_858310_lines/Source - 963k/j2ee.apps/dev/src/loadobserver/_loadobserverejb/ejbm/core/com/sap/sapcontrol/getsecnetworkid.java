
package com.sap.sapcontrol;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for GetSecNetworkId element declaration.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;element name="GetSecNetworkId">
 *   &lt;complexType>
 *     &lt;complexContent>
 *       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *         &lt;sequence>
 *           &lt;element name="service-ip" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *           &lt;element name="service-port" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *           &lt;element name="version" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *           &lt;element name="challenge" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
    "serviceIp",
    "servicePort",
    "version",
    "challenge"
})
@XmlRootElement(name = "GetSecNetworkId")
public class GetSecNetworkId {

    @XmlElement(name = "service-ip", required = true)
    protected String serviceIp;
    @XmlElement(name = "service-port")
    protected int servicePort;
    protected int version;
    @XmlElementRef(name = "challenge", type = JAXBElement.class)
    protected JAXBElement<String> challenge;

    /**
     * Gets the value of the serviceIp property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getServiceIp() {
        return serviceIp;
    }

    /**
     * Sets the value of the serviceIp property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setServiceIp(String value) {
        this.serviceIp = value;
    }

    /**
     * Gets the value of the servicePort property.
     * 
     */
    public int getServicePort() {
        return servicePort;
    }

    /**
     * Sets the value of the servicePort property.
     * 
     */
    public void setServicePort(int value) {
        this.servicePort = value;
    }

    /**
     * Gets the value of the version property.
     * 
     */
    public int getVersion() {
        return version;
    }

    /**
     * Sets the value of the version property.
     * 
     */
    public void setVersion(int value) {
        this.version = value;
    }

    /**
     * Gets the value of the challenge property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getChallenge() {
        return challenge;
    }

    /**
     * Sets the value of the challenge property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setChallenge(JAXBElement<String> value) {
        this.challenge = ((JAXBElement<String> ) value);
    }

}

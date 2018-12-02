
package com.sap.sapcontrol;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ABAPAcknowledgeAlerts element declaration.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;element name="ABAPAcknowledgeAlerts">
 *   &lt;complexType>
 *     &lt;complexContent>
 *       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *         &lt;sequence>
 *           &lt;element name="R3Client" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *           &lt;element name="R3User" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *           &lt;element name="R3Password" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *           &lt;element name="Aid" type="{urn:SAPControl}ArrayOfString"/>
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
    "r3Client",
    "r3User",
    "r3Password",
    "aid"
})
@XmlRootElement(name = "ABAPAcknowledgeAlerts")
public class ABAPAcknowledgeAlerts {

    @XmlElement(name = "R3Client", required = true)
    protected String r3Client;
    @XmlElement(name = "R3User", required = true)
    protected String r3User;
    @XmlElement(name = "R3Password", required = true)
    protected String r3Password;
    @XmlElement(name = "Aid", required = true)
    protected ArrayOfString aid;

    /**
     * Gets the value of the r3Client property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getR3Client() {
        return r3Client;
    }

    /**
     * Sets the value of the r3Client property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setR3Client(String value) {
        this.r3Client = value;
    }

    /**
     * Gets the value of the r3User property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getR3User() {
        return r3User;
    }

    /**
     * Sets the value of the r3User property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setR3User(String value) {
        this.r3User = value;
    }

    /**
     * Gets the value of the r3Password property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getR3Password() {
        return r3Password;
    }

    /**
     * Sets the value of the r3Password property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setR3Password(String value) {
        this.r3Password = value;
    }

    /**
     * Gets the value of the aid property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfString }
     *     
     */
    public ArrayOfString getAid() {
        return aid;
    }

    /**
     * Sets the value of the aid property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfString }
     *     
     */
    public void setAid(ArrayOfString value) {
        this.aid = value;
    }

}

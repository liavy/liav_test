
package com.sap.sapcontrol;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for AnalyseLogFiles element declaration.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;element name="AnalyseLogFiles">
 *   &lt;complexType>
 *     &lt;complexContent>
 *       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *         &lt;sequence>
 *           &lt;element name="starttime" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *           &lt;element name="endtime" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *           &lt;element name="severity-level" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *           &lt;element name="maxentries" type="{http://www.w3.org/2001/XMLSchema}int"/>
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
    "starttime",
    "endtime",
    "severityLevel",
    "maxentries"
})
@XmlRootElement(name = "AnalyseLogFiles")
public class AnalyseLogFiles {

    @XmlElementRef(name = "starttime", type = JAXBElement.class)
    protected JAXBElement<String> starttime;
    @XmlElementRef(name = "endtime", type = JAXBElement.class)
    protected JAXBElement<String> endtime;
    @XmlElement(name = "severity-level", defaultValue = "2")
    protected int severityLevel;
    @XmlElement(defaultValue = "10000")
    protected int maxentries;

    /**
     * Gets the value of the starttime property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getStarttime() {
        return starttime;
    }

    /**
     * Sets the value of the starttime property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setStarttime(JAXBElement<String> value) {
        this.starttime = ((JAXBElement<String> ) value);
    }

    /**
     * Gets the value of the endtime property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getEndtime() {
        return endtime;
    }

    /**
     * Sets the value of the endtime property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setEndtime(JAXBElement<String> value) {
        this.endtime = ((JAXBElement<String> ) value);
    }

    /**
     * Gets the value of the severityLevel property.
     * 
     */
    public int getSeverityLevel() {
        return severityLevel;
    }

    /**
     * Sets the value of the severityLevel property.
     * 
     */
    public void setSeverityLevel(int value) {
        this.severityLevel = value;
    }

    /**
     * Gets the value of the maxentries property.
     * 
     */
    public int getMaxentries() {
        return maxentries;
    }

    /**
     * Sets the value of the maxentries property.
     * 
     */
    public void setMaxentries(int value) {
        this.maxentries = value;
    }

}


package com.sap.sapcontrol;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ABAPReadSyslogResponse element declaration.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;element name="ABAPReadSyslogResponse">
 *   &lt;complexType>
 *     &lt;complexContent>
 *       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *         &lt;sequence>
 *           &lt;element name="log" type="{urn:SAPControl}ArrayOfSyslogEntry" minOccurs="0"/>
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
    "log"
})
@XmlRootElement(name = "ABAPReadSyslogResponse")
public class ABAPReadSyslogResponse {

    @XmlElementRef(name = "log", type = JAXBElement.class)
    protected JAXBElement<ArrayOfSyslogEntry> log;

    /**
     * Gets the value of the log property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfSyslogEntry }{@code >}
     *     
     */
    public JAXBElement<ArrayOfSyslogEntry> getLog() {
        return log;
    }

    /**
     * Sets the value of the log property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfSyslogEntry }{@code >}
     *     
     */
    public void setLog(JAXBElement<ArrayOfSyslogEntry> value) {
        this.log = ((JAXBElement<ArrayOfSyslogEntry> ) value);
    }

}

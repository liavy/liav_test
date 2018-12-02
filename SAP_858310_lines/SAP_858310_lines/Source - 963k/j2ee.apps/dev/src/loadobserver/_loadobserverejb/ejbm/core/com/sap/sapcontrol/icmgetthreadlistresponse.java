
package com.sap.sapcontrol;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ICMGetThreadListResponse element declaration.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;element name="ICMGetThreadListResponse">
 *   &lt;complexType>
 *     &lt;complexContent>
 *       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *         &lt;sequence>
 *           &lt;element name="thread" type="{urn:SAPControl}ArrayOfICMThread" minOccurs="0"/>
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
    "thread"
})
@XmlRootElement(name = "ICMGetThreadListResponse")
public class ICMGetThreadListResponse {

    @XmlElementRef(name = "thread", type = JAXBElement.class)
    protected JAXBElement<ArrayOfICMThread> thread;

    /**
     * Gets the value of the thread property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfICMThread }{@code >}
     *     
     */
    public JAXBElement<ArrayOfICMThread> getThread() {
        return thread;
    }

    /**
     * Sets the value of the thread property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfICMThread }{@code >}
     *     
     */
    public void setThread(JAXBElement<ArrayOfICMThread> value) {
        this.thread = ((JAXBElement<ArrayOfICMThread> ) value);
    }

}

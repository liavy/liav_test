
package com.sap.sapcontrol;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ABAPGetWPTableResponse element declaration.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;element name="ABAPGetWPTableResponse">
 *   &lt;complexType>
 *     &lt;complexContent>
 *       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *         &lt;sequence>
 *           &lt;element name="workprocess" type="{urn:SAPControl}ArrayOfWorkProcess" minOccurs="0"/>
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
    "workprocess"
})
@XmlRootElement(name = "ABAPGetWPTableResponse")
public class ABAPGetWPTableResponse {

    @XmlElementRef(name = "workprocess", type = JAXBElement.class)
    protected JAXBElement<ArrayOfWorkProcess> workprocess;

    /**
     * Gets the value of the workprocess property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfWorkProcess }{@code >}
     *     
     */
    public JAXBElement<ArrayOfWorkProcess> getWorkprocess() {
        return workprocess;
    }

    /**
     * Sets the value of the workprocess property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfWorkProcess }{@code >}
     *     
     */
    public void setWorkprocess(JAXBElement<ArrayOfWorkProcess> value) {
        this.workprocess = ((JAXBElement<ArrayOfWorkProcess> ) value);
    }

}

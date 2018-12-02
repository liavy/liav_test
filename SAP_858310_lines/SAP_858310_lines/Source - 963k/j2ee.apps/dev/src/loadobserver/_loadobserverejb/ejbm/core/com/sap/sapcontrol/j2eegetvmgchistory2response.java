
package com.sap.sapcontrol;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for J2EEGetVMGCHistory2Response element declaration.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;element name="J2EEGetVMGCHistory2Response">
 *   &lt;complexType>
 *     &lt;complexContent>
 *       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *         &lt;sequence>
 *           &lt;element name="gc" type="{urn:SAPControl}ArrayOfGCInfo2" minOccurs="0"/>
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
    "gc"
})
@XmlRootElement(name = "J2EEGetVMGCHistory2Response")
public class J2EEGetVMGCHistory2Response {

    @XmlElementRef(name = "gc", type = JAXBElement.class)
    protected JAXBElement<ArrayOfGCInfo2> gc;

    /**
     * Gets the value of the gc property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfGCInfo2 }{@code >}
     *     
     */
    public JAXBElement<ArrayOfGCInfo2> getGc() {
        return gc;
    }

    /**
     * Sets the value of the gc property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfGCInfo2 }{@code >}
     *     
     */
    public void setGc(JAXBElement<ArrayOfGCInfo2> value) {
        this.gc = ((JAXBElement<ArrayOfGCInfo2> ) value);
    }

}

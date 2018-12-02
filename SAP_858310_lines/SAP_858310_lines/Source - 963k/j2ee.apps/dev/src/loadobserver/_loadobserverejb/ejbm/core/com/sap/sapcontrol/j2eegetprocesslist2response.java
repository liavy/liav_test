
package com.sap.sapcontrol;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for J2EEGetProcessList2Response element declaration.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;element name="J2EEGetProcessList2Response">
 *   &lt;complexType>
 *     &lt;complexContent>
 *       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *         &lt;sequence>
 *           &lt;element name="process" type="{urn:SAPControl}ArrayOfJ2EEProcess2" minOccurs="0"/>
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
    "process"
})
@XmlRootElement(name = "J2EEGetProcessList2Response")
public class J2EEGetProcessList2Response {

    @XmlElementRef(name = "process", type = JAXBElement.class)
    protected JAXBElement<ArrayOfJ2EEProcess2> process;

    /**
     * Gets the value of the process property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfJ2EEProcess2 }{@code >}
     *     
     */
    public JAXBElement<ArrayOfJ2EEProcess2> getProcess() {
        return process;
    }

    /**
     * Sets the value of the process property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfJ2EEProcess2 }{@code >}
     *     
     */
    public void setProcess(JAXBElement<ArrayOfJ2EEProcess2> value) {
        this.process = ((JAXBElement<ArrayOfJ2EEProcess2> ) value);
    }

}

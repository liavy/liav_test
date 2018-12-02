
package com.sap.sapcontrol;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for J2EEGetVMHeapInfoResponse element declaration.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;element name="J2EEGetVMHeapInfoResponse">
 *   &lt;complexType>
 *     &lt;complexContent>
 *       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *         &lt;sequence>
 *           &lt;element name="heap" type="{urn:SAPControl}ArrayOfHeapInfo" minOccurs="0"/>
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
    "heap"
})
@XmlRootElement(name = "J2EEGetVMHeapInfoResponse")
public class J2EEGetVMHeapInfoResponse {

    @XmlElementRef(name = "heap", type = JAXBElement.class)
    protected JAXBElement<ArrayOfHeapInfo> heap;

    /**
     * Gets the value of the heap property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfHeapInfo }{@code >}
     *     
     */
    public JAXBElement<ArrayOfHeapInfo> getHeap() {
        return heap;
    }

    /**
     * Sets the value of the heap property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfHeapInfo }{@code >}
     *     
     */
    public void setHeap(JAXBElement<ArrayOfHeapInfo> value) {
        this.heap = ((JAXBElement<ArrayOfHeapInfo> ) value);
    }

}

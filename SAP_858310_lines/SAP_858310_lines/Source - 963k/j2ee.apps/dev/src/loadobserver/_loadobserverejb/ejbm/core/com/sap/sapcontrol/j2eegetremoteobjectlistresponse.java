
package com.sap.sapcontrol;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for J2EEGetRemoteObjectListResponse element declaration.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;element name="J2EEGetRemoteObjectListResponse">
 *   &lt;complexType>
 *     &lt;complexContent>
 *       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *         &lt;sequence>
 *           &lt;element name="remoteobject" type="{urn:SAPControl}ArrayOfJ2EERemoteObject" minOccurs="0"/>
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
    "remoteobject"
})
@XmlRootElement(name = "J2EEGetRemoteObjectListResponse")
public class J2EEGetRemoteObjectListResponse {

    @XmlElementRef(name = "remoteobject", type = JAXBElement.class)
    protected JAXBElement<ArrayOfJ2EERemoteObject> remoteobject;

    /**
     * Gets the value of the remoteobject property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfJ2EERemoteObject }{@code >}
     *     
     */
    public JAXBElement<ArrayOfJ2EERemoteObject> getRemoteobject() {
        return remoteobject;
    }

    /**
     * Sets the value of the remoteobject property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfJ2EERemoteObject }{@code >}
     *     
     */
    public void setRemoteobject(JAXBElement<ArrayOfJ2EERemoteObject> value) {
        this.remoteobject = ((JAXBElement<ArrayOfJ2EERemoteObject> ) value);
    }

}

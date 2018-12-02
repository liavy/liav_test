﻿
package com.sap.sapcontrol;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for J2EEGetComponentListResponse element declaration.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;element name="J2EEGetComponentListResponse">
 *   &lt;complexType>
 *     &lt;complexContent>
 *       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *         &lt;sequence>
 *           &lt;element name="component" type="{urn:SAPControl}ArrayOfJ2EEComponentInfo" minOccurs="0"/>
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
    "component"
})
@XmlRootElement(name = "J2EEGetComponentListResponse")
public class J2EEGetComponentListResponse {

    @XmlElementRef(name = "component", type = JAXBElement.class)
    protected JAXBElement<ArrayOfJ2EEComponentInfo> component;

    /**
     * Gets the value of the component property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfJ2EEComponentInfo }{@code >}
     *     
     */
    public JAXBElement<ArrayOfJ2EEComponentInfo> getComponent() {
        return component;
    }

    /**
     * Sets the value of the component property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfJ2EEComponentInfo }{@code >}
     *     
     */
    public void setComponent(JAXBElement<ArrayOfJ2EEComponentInfo> value) {
        this.component = ((JAXBElement<ArrayOfJ2EEComponentInfo> ) value);
    }

}

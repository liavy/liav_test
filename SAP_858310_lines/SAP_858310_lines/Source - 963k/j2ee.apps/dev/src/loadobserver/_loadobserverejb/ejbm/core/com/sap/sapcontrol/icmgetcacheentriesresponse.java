
package com.sap.sapcontrol;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ICMGetCacheEntriesResponse element declaration.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;element name="ICMGetCacheEntriesResponse">
 *   &lt;complexType>
 *     &lt;complexContent>
 *       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *         &lt;sequence>
 *           &lt;element name="entry" type="{urn:SAPControl}ArrayOfICMCacheEntry" minOccurs="0"/>
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
    "entry"
})
@XmlRootElement(name = "ICMGetCacheEntriesResponse")
public class ICMGetCacheEntriesResponse {

    @XmlElementRef(name = "entry", type = JAXBElement.class)
    protected JAXBElement<ArrayOfICMCacheEntry> entry;

    /**
     * Gets the value of the entry property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfICMCacheEntry }{@code >}
     *     
     */
    public JAXBElement<ArrayOfICMCacheEntry> getEntry() {
        return entry;
    }

    /**
     * Sets the value of the entry property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfICMCacheEntry }{@code >}
     *     
     */
    public void setEntry(JAXBElement<ArrayOfICMCacheEntry> value) {
        this.entry = ((JAXBElement<ArrayOfICMCacheEntry> ) value);
    }

}

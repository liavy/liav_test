
package com.sap.sapcontrol;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ParameterRestriction complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ParameterRestriction">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="type" type="{urn:SAPControl}RESTRICTION-TYPE"/>
 *         &lt;element name="int-min" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="int-max" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="float-min" type="{http://www.w3.org/2001/XMLSchema}double" minOccurs="0"/>
 *         &lt;element name="float-max" type="{http://www.w3.org/2001/XMLSchema}double" minOccurs="0"/>
 *         &lt;element name="valuemap" type="{urn:SAPControl}ArrayOfString" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ParameterRestriction", propOrder = {
    "type",
    "intMin",
    "intMax",
    "floatMin",
    "floatMax",
    "valuemap"
})
public class ParameterRestriction {

    @XmlElement(required = true)
    protected RESTRICTIONTYPE type;
    @XmlElementRef(name = "int-min", type = JAXBElement.class)
    protected JAXBElement<Long> intMin;
    @XmlElementRef(name = "int-max", type = JAXBElement.class)
    protected JAXBElement<Long> intMax;
    @XmlElementRef(name = "float-min", type = JAXBElement.class)
    protected JAXBElement<Double> floatMin;
    @XmlElementRef(name = "float-max", type = JAXBElement.class)
    protected JAXBElement<Double> floatMax;
    @XmlElementRef(name = "valuemap", type = JAXBElement.class)
    protected JAXBElement<ArrayOfString> valuemap;

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link RESTRICTIONTYPE }
     *     
     */
    public RESTRICTIONTYPE getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link RESTRICTIONTYPE }
     *     
     */
    public void setType(RESTRICTIONTYPE value) {
        this.type = value;
    }

    /**
     * Gets the value of the intMin property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Long }{@code >}
     *     
     */
    public JAXBElement<Long> getIntMin() {
        return intMin;
    }

    /**
     * Sets the value of the intMin property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Long }{@code >}
     *     
     */
    public void setIntMin(JAXBElement<Long> value) {
        this.intMin = ((JAXBElement<Long> ) value);
    }

    /**
     * Gets the value of the intMax property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Long }{@code >}
     *     
     */
    public JAXBElement<Long> getIntMax() {
        return intMax;
    }

    /**
     * Sets the value of the intMax property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Long }{@code >}
     *     
     */
    public void setIntMax(JAXBElement<Long> value) {
        this.intMax = ((JAXBElement<Long> ) value);
    }

    /**
     * Gets the value of the floatMin property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Double }{@code >}
     *     
     */
    public JAXBElement<Double> getFloatMin() {
        return floatMin;
    }

    /**
     * Sets the value of the floatMin property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Double }{@code >}
     *     
     */
    public void setFloatMin(JAXBElement<Double> value) {
        this.floatMin = ((JAXBElement<Double> ) value);
    }

    /**
     * Gets the value of the floatMax property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Double }{@code >}
     *     
     */
    public JAXBElement<Double> getFloatMax() {
        return floatMax;
    }

    /**
     * Sets the value of the floatMax property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Double }{@code >}
     *     
     */
    public void setFloatMax(JAXBElement<Double> value) {
        this.floatMax = ((JAXBElement<Double> ) value);
    }

    /**
     * Gets the value of the valuemap property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfString }{@code >}
     *     
     */
    public JAXBElement<ArrayOfString> getValuemap() {
        return valuemap;
    }

    /**
     * Sets the value of the valuemap property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfString }{@code >}
     *     
     */
    public void setValuemap(JAXBElement<ArrayOfString> value) {
        this.valuemap = ((JAXBElement<ArrayOfString> ) value);
    }

}

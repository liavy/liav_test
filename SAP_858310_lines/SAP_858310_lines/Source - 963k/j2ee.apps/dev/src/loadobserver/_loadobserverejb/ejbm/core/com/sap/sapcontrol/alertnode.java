
package com.sap.sapcontrol;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for AlertNode complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AlertNode">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="parent" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="ActualValue" type="{urn:SAPControl}STATE-COLOR"/>
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Time" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="AnalyseTool" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="VisibleLevel" type="{urn:SAPControl}VISIBLE-LEVEL"/>
 *         &lt;element name="HighAlertValue" type="{urn:SAPControl}STATE-COLOR"/>
 *         &lt;element name="AlDescription" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="AlTime" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Tid" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AlertNode", propOrder = {
    "name",
    "parent",
    "actualValue",
    "description",
    "time",
    "analyseTool",
    "visibleLevel",
    "highAlertValue",
    "alDescription",
    "alTime",
    "tid"
})
public class AlertNode {

    @XmlElementRef(name = "name", type = JAXBElement.class)
    protected JAXBElement<String> name;
    protected int parent;
    @XmlElement(name = "ActualValue", required = true)
    protected STATECOLOR actualValue;
    @XmlElementRef(name = "description", type = JAXBElement.class)
    protected JAXBElement<String> description;
    @XmlElementRef(name = "Time", type = JAXBElement.class)
    protected JAXBElement<String> time;
    @XmlElementRef(name = "AnalyseTool", type = JAXBElement.class)
    protected JAXBElement<String> analyseTool;
    @XmlElement(name = "VisibleLevel", required = true)
    protected VISIBLELEVEL visibleLevel;
    @XmlElement(name = "HighAlertValue", required = true)
    protected STATECOLOR highAlertValue;
    @XmlElementRef(name = "AlDescription", type = JAXBElement.class)
    protected JAXBElement<String> alDescription;
    @XmlElementRef(name = "AlTime", type = JAXBElement.class)
    protected JAXBElement<String> alTime;
    @XmlElementRef(name = "Tid", type = JAXBElement.class)
    protected JAXBElement<String> tid;

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setName(JAXBElement<String> value) {
        this.name = ((JAXBElement<String> ) value);
    }

    /**
     * Gets the value of the parent property.
     * 
     */
    public int getParent() {
        return parent;
    }

    /**
     * Sets the value of the parent property.
     * 
     */
    public void setParent(int value) {
        this.parent = value;
    }

    /**
     * Gets the value of the actualValue property.
     * 
     * @return
     *     possible object is
     *     {@link STATECOLOR }
     *     
     */
    public STATECOLOR getActualValue() {
        return actualValue;
    }

    /**
     * Sets the value of the actualValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link STATECOLOR }
     *     
     */
    public void setActualValue(STATECOLOR value) {
        this.actualValue = value;
    }

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setDescription(JAXBElement<String> value) {
        this.description = ((JAXBElement<String> ) value);
    }

    /**
     * Gets the value of the time property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getTime() {
        return time;
    }

    /**
     * Sets the value of the time property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setTime(JAXBElement<String> value) {
        this.time = ((JAXBElement<String> ) value);
    }

    /**
     * Gets the value of the analyseTool property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getAnalyseTool() {
        return analyseTool;
    }

    /**
     * Sets the value of the analyseTool property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setAnalyseTool(JAXBElement<String> value) {
        this.analyseTool = ((JAXBElement<String> ) value);
    }

    /**
     * Gets the value of the visibleLevel property.
     * 
     * @return
     *     possible object is
     *     {@link VISIBLELEVEL }
     *     
     */
    public VISIBLELEVEL getVisibleLevel() {
        return visibleLevel;
    }

    /**
     * Sets the value of the visibleLevel property.
     * 
     * @param value
     *     allowed object is
     *     {@link VISIBLELEVEL }
     *     
     */
    public void setVisibleLevel(VISIBLELEVEL value) {
        this.visibleLevel = value;
    }

    /**
     * Gets the value of the highAlertValue property.
     * 
     * @return
     *     possible object is
     *     {@link STATECOLOR }
     *     
     */
    public STATECOLOR getHighAlertValue() {
        return highAlertValue;
    }

    /**
     * Sets the value of the highAlertValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link STATECOLOR }
     *     
     */
    public void setHighAlertValue(STATECOLOR value) {
        this.highAlertValue = value;
    }

    /**
     * Gets the value of the alDescription property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getAlDescription() {
        return alDescription;
    }

    /**
     * Sets the value of the alDescription property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setAlDescription(JAXBElement<String> value) {
        this.alDescription = ((JAXBElement<String> ) value);
    }

    /**
     * Gets the value of the alTime property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getAlTime() {
        return alTime;
    }

    /**
     * Sets the value of the alTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setAlTime(JAXBElement<String> value) {
        this.alTime = ((JAXBElement<String> ) value);
    }

    /**
     * Gets the value of the tid property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getTid() {
        return tid;
    }

    /**
     * Sets the value of the tid property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setTid(JAXBElement<String> value) {
        this.tid = ((JAXBElement<String> ) value);
    }

}

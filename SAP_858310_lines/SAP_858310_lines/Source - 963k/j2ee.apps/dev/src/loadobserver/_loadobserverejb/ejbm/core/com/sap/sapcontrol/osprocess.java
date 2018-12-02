
package com.sap.sapcontrol;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for OSProcess complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OSProcess">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="dispstatus" type="{urn:SAPControl}STATE-COLOR"/>
 *         &lt;element name="textstatus" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="starttime" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="elapsedtime" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="pid" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OSProcess", propOrder = {
    "name",
    "description",
    "dispstatus",
    "textstatus",
    "starttime",
    "elapsedtime",
    "pid"
})
public class OSProcess {

    @XmlElementRef(name = "name", type = JAXBElement.class)
    protected JAXBElement<String> name;
    @XmlElementRef(name = "description", type = JAXBElement.class)
    protected JAXBElement<String> description;
    @XmlElement(required = true)
    protected STATECOLOR dispstatus;
    @XmlElementRef(name = "textstatus", type = JAXBElement.class)
    protected JAXBElement<String> textstatus;
    @XmlElementRef(name = "starttime", type = JAXBElement.class)
    protected JAXBElement<String> starttime;
    @XmlElementRef(name = "elapsedtime", type = JAXBElement.class)
    protected JAXBElement<String> elapsedtime;
    protected int pid;

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
     * Gets the value of the dispstatus property.
     * 
     * @return
     *     possible object is
     *     {@link STATECOLOR }
     *     
     */
    public STATECOLOR getDispstatus() {
        return dispstatus;
    }

    /**
     * Sets the value of the dispstatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link STATECOLOR }
     *     
     */
    public void setDispstatus(STATECOLOR value) {
        this.dispstatus = value;
    }

    /**
     * Gets the value of the textstatus property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getTextstatus() {
        return textstatus;
    }

    /**
     * Sets the value of the textstatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setTextstatus(JAXBElement<String> value) {
        this.textstatus = ((JAXBElement<String> ) value);
    }

    /**
     * Gets the value of the starttime property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getStarttime() {
        return starttime;
    }

    /**
     * Sets the value of the starttime property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setStarttime(JAXBElement<String> value) {
        this.starttime = ((JAXBElement<String> ) value);
    }

    /**
     * Gets the value of the elapsedtime property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getElapsedtime() {
        return elapsedtime;
    }

    /**
     * Sets the value of the elapsedtime property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setElapsedtime(JAXBElement<String> value) {
        this.elapsedtime = ((JAXBElement<String> ) value);
    }

    /**
     * Gets the value of the pid property.
     * 
     */
    public int getPid() {
        return pid;
    }

    /**
     * Sets the value of the pid property.
     * 
     */
    public void setPid(int value) {
        this.pid = value;
    }

}

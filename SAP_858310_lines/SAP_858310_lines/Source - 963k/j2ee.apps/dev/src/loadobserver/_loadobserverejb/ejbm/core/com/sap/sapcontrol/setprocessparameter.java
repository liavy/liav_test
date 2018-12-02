
package com.sap.sapcontrol;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SetProcessParameter element declaration.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;element name="SetProcessParameter">
 *   &lt;complexType>
 *     &lt;complexContent>
 *       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *         &lt;sequence>
 *           &lt;element name="processtype" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *           &lt;element name="pid" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *           &lt;element name="parameter" type="{urn:SAPControl}ArrayOfSetProfileParameter"/>
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
    "processtype",
    "pid",
    "parameter"
})
@XmlRootElement(name = "SetProcessParameter")
public class SetProcessParameter {

    @XmlElementRef(name = "processtype", type = JAXBElement.class)
    protected JAXBElement<String> processtype;
    @XmlElement(defaultValue = "-1")
    protected int pid;
    @XmlElement(required = true)
    protected ArrayOfSetProfileParameter parameter;

    /**
     * Gets the value of the processtype property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getProcesstype() {
        return processtype;
    }

    /**
     * Sets the value of the processtype property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setProcesstype(JAXBElement<String> value) {
        this.processtype = ((JAXBElement<String> ) value);
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

    /**
     * Gets the value of the parameter property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfSetProfileParameter }
     *     
     */
    public ArrayOfSetProfileParameter getParameter() {
        return parameter;
    }

    /**
     * Sets the value of the parameter property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfSetProfileParameter }
     *     
     */
    public void setParameter(ArrayOfSetProfileParameter value) {
        this.parameter = value;
    }

}

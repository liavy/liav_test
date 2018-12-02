
package com.sap.sapcontrol;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for J2EEProcess complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="J2EEProcess">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="telnetPort" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="pid" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="type" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="restart" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="exitCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="state" type="{urn:SAPControl}J2EE-PSTATE"/>
 *         &lt;element name="statetext" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="startTime" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="elapsedTime" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="restartCount" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="errorCount" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="cpu" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="debug" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "J2EEProcess", propOrder = {
    "telnetPort",
    "name",
    "pid",
    "type",
    "restart",
    "exitCode",
    "state",
    "statetext",
    "startTime",
    "elapsedTime",
    "restartCount",
    "errorCount",
    "cpu",
    "debug"
})
public class J2EEProcess {

    protected int telnetPort;
    @XmlElementRef(name = "name", type = JAXBElement.class)
    protected JAXBElement<String> name;
    protected int pid;
    @XmlElementRef(name = "type", type = JAXBElement.class)
    protected JAXBElement<String> type;
    @XmlElementRef(name = "restart", type = JAXBElement.class)
    protected JAXBElement<String> restart;
    @XmlElementRef(name = "exitCode", type = JAXBElement.class)
    protected JAXBElement<String> exitCode;
    @XmlElement(required = true)
    protected J2EEPSTATE state;
    @XmlElementRef(name = "statetext", type = JAXBElement.class)
    protected JAXBElement<String> statetext;
    @XmlElementRef(name = "startTime", type = JAXBElement.class)
    protected JAXBElement<String> startTime;
    @XmlElementRef(name = "elapsedTime", type = JAXBElement.class)
    protected JAXBElement<String> elapsedTime;
    protected int restartCount;
    protected int errorCount;
    @XmlElementRef(name = "cpu", type = JAXBElement.class)
    protected JAXBElement<String> cpu;
    @XmlElementRef(name = "debug", type = JAXBElement.class)
    protected JAXBElement<String> debug;

    /**
     * Gets the value of the telnetPort property.
     * 
     */
    public int getTelnetPort() {
        return telnetPort;
    }

    /**
     * Sets the value of the telnetPort property.
     * 
     */
    public void setTelnetPort(int value) {
        this.telnetPort = value;
    }

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
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setType(JAXBElement<String> value) {
        this.type = ((JAXBElement<String> ) value);
    }

    /**
     * Gets the value of the restart property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getRestart() {
        return restart;
    }

    /**
     * Sets the value of the restart property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setRestart(JAXBElement<String> value) {
        this.restart = ((JAXBElement<String> ) value);
    }

    /**
     * Gets the value of the exitCode property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getExitCode() {
        return exitCode;
    }

    /**
     * Sets the value of the exitCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setExitCode(JAXBElement<String> value) {
        this.exitCode = ((JAXBElement<String> ) value);
    }

    /**
     * Gets the value of the state property.
     * 
     * @return
     *     possible object is
     *     {@link J2EEPSTATE }
     *     
     */
    public J2EEPSTATE getState() {
        return state;
    }

    /**
     * Sets the value of the state property.
     * 
     * @param value
     *     allowed object is
     *     {@link J2EEPSTATE }
     *     
     */
    public void setState(J2EEPSTATE value) {
        this.state = value;
    }

    /**
     * Gets the value of the statetext property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getStatetext() {
        return statetext;
    }

    /**
     * Sets the value of the statetext property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setStatetext(JAXBElement<String> value) {
        this.statetext = ((JAXBElement<String> ) value);
    }

    /**
     * Gets the value of the startTime property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getStartTime() {
        return startTime;
    }

    /**
     * Sets the value of the startTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setStartTime(JAXBElement<String> value) {
        this.startTime = ((JAXBElement<String> ) value);
    }

    /**
     * Gets the value of the elapsedTime property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getElapsedTime() {
        return elapsedTime;
    }

    /**
     * Sets the value of the elapsedTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setElapsedTime(JAXBElement<String> value) {
        this.elapsedTime = ((JAXBElement<String> ) value);
    }

    /**
     * Gets the value of the restartCount property.
     * 
     */
    public int getRestartCount() {
        return restartCount;
    }

    /**
     * Sets the value of the restartCount property.
     * 
     */
    public void setRestartCount(int value) {
        this.restartCount = value;
    }

    /**
     * Gets the value of the errorCount property.
     * 
     */
    public int getErrorCount() {
        return errorCount;
    }

    /**
     * Sets the value of the errorCount property.
     * 
     */
    public void setErrorCount(int value) {
        this.errorCount = value;
    }

    /**
     * Gets the value of the cpu property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getCpu() {
        return cpu;
    }

    /**
     * Sets the value of the cpu property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setCpu(JAXBElement<String> value) {
        this.cpu = ((JAXBElement<String> ) value);
    }

    /**
     * Gets the value of the debug property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getDebug() {
        return debug;
    }

    /**
     * Sets the value of the debug property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setDebug(JAXBElement<String> value) {
        this.debug = ((JAXBElement<String> ) value);
    }

}

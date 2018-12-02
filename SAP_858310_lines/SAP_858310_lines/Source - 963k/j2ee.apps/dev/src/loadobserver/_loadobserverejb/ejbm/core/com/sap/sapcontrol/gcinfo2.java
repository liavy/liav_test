
package com.sap.sapcontrol;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for GCInfo2 complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GCInfo2">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="processname" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="type" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="reason" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="startTime" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="duration" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="cpuTime" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="objBytesBefore" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="objBytesAfter" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="objBytesFreed" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="clsBytesBefore" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="clsBytesAfter" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="clsBytesFreed" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="heapSize" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="unloadedClasses" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="pageFaults" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="dispstatus" type="{urn:SAPControl}STATE-COLOR"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GCInfo2", propOrder = {
    "processname",
    "type",
    "reason",
    "startTime",
    "duration",
    "cpuTime",
    "objBytesBefore",
    "objBytesAfter",
    "objBytesFreed",
    "clsBytesBefore",
    "clsBytesAfter",
    "clsBytesFreed",
    "heapSize",
    "unloadedClasses",
    "pageFaults",
    "dispstatus"
})
public class GCInfo2 {

    @XmlElementRef(name = "processname", type = JAXBElement.class)
    protected JAXBElement<String> processname;
    @XmlElementRef(name = "type", type = JAXBElement.class)
    protected JAXBElement<String> type;
    @XmlElementRef(name = "reason", type = JAXBElement.class)
    protected JAXBElement<String> reason;
    @XmlElementRef(name = "startTime", type = JAXBElement.class)
    protected JAXBElement<String> startTime;
    protected int duration;
    protected int cpuTime;
    protected long objBytesBefore;
    protected long objBytesAfter;
    protected long objBytesFreed;
    protected long clsBytesBefore;
    protected long clsBytesAfter;
    protected long clsBytesFreed;
    protected long heapSize;
    protected int unloadedClasses;
    protected long pageFaults;
    @XmlElement(required = true)
    protected STATECOLOR dispstatus;

    /**
     * Gets the value of the processname property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getProcessname() {
        return processname;
    }

    /**
     * Sets the value of the processname property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setProcessname(JAXBElement<String> value) {
        this.processname = ((JAXBElement<String> ) value);
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
     * Gets the value of the reason property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getReason() {
        return reason;
    }

    /**
     * Sets the value of the reason property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setReason(JAXBElement<String> value) {
        this.reason = ((JAXBElement<String> ) value);
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
     * Gets the value of the duration property.
     * 
     */
    public int getDuration() {
        return duration;
    }

    /**
     * Sets the value of the duration property.
     * 
     */
    public void setDuration(int value) {
        this.duration = value;
    }

    /**
     * Gets the value of the cpuTime property.
     * 
     */
    public int getCpuTime() {
        return cpuTime;
    }

    /**
     * Sets the value of the cpuTime property.
     * 
     */
    public void setCpuTime(int value) {
        this.cpuTime = value;
    }

    /**
     * Gets the value of the objBytesBefore property.
     * 
     */
    public long getObjBytesBefore() {
        return objBytesBefore;
    }

    /**
     * Sets the value of the objBytesBefore property.
     * 
     */
    public void setObjBytesBefore(long value) {
        this.objBytesBefore = value;
    }

    /**
     * Gets the value of the objBytesAfter property.
     * 
     */
    public long getObjBytesAfter() {
        return objBytesAfter;
    }

    /**
     * Sets the value of the objBytesAfter property.
     * 
     */
    public void setObjBytesAfter(long value) {
        this.objBytesAfter = value;
    }

    /**
     * Gets the value of the objBytesFreed property.
     * 
     */
    public long getObjBytesFreed() {
        return objBytesFreed;
    }

    /**
     * Sets the value of the objBytesFreed property.
     * 
     */
    public void setObjBytesFreed(long value) {
        this.objBytesFreed = value;
    }

    /**
     * Gets the value of the clsBytesBefore property.
     * 
     */
    public long getClsBytesBefore() {
        return clsBytesBefore;
    }

    /**
     * Sets the value of the clsBytesBefore property.
     * 
     */
    public void setClsBytesBefore(long value) {
        this.clsBytesBefore = value;
    }

    /**
     * Gets the value of the clsBytesAfter property.
     * 
     */
    public long getClsBytesAfter() {
        return clsBytesAfter;
    }

    /**
     * Sets the value of the clsBytesAfter property.
     * 
     */
    public void setClsBytesAfter(long value) {
        this.clsBytesAfter = value;
    }

    /**
     * Gets the value of the clsBytesFreed property.
     * 
     */
    public long getClsBytesFreed() {
        return clsBytesFreed;
    }

    /**
     * Sets the value of the clsBytesFreed property.
     * 
     */
    public void setClsBytesFreed(long value) {
        this.clsBytesFreed = value;
    }

    /**
     * Gets the value of the heapSize property.
     * 
     */
    public long getHeapSize() {
        return heapSize;
    }

    /**
     * Sets the value of the heapSize property.
     * 
     */
    public void setHeapSize(long value) {
        this.heapSize = value;
    }

    /**
     * Gets the value of the unloadedClasses property.
     * 
     */
    public int getUnloadedClasses() {
        return unloadedClasses;
    }

    /**
     * Sets the value of the unloadedClasses property.
     * 
     */
    public void setUnloadedClasses(int value) {
        this.unloadedClasses = value;
    }

    /**
     * Gets the value of the pageFaults property.
     * 
     */
    public long getPageFaults() {
        return pageFaults;
    }

    /**
     * Sets the value of the pageFaults property.
     * 
     */
    public void setPageFaults(long value) {
        this.pageFaults = value;
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

}

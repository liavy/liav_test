
package com.sap.sapcontrol;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for J2EECache2 complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="J2EECache2">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="owner" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="processname" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="type" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="size" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="attrSize" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="keysSize" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="cachedObjects" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="usedObjects" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="puts" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="gets" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="hits" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="changes" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="removes" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="evictions" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="instanceInvalidations" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="clusterInvalidations" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="updateTime" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
@XmlType(name = "J2EECache2", propOrder = {
    "description",
    "owner",
    "processname",
    "type",
    "size",
    "attrSize",
    "keysSize",
    "cachedObjects",
    "usedObjects",
    "puts",
    "gets",
    "hits",
    "changes",
    "removes",
    "evictions",
    "instanceInvalidations",
    "clusterInvalidations",
    "updateTime",
    "dispstatus"
})
public class J2EECache2 {

    @XmlElementRef(name = "description", type = JAXBElement.class)
    protected JAXBElement<String> description;
    @XmlElementRef(name = "owner", type = JAXBElement.class)
    protected JAXBElement<String> owner;
    @XmlElementRef(name = "processname", type = JAXBElement.class)
    protected JAXBElement<String> processname;
    @XmlElementRef(name = "type", type = JAXBElement.class)
    protected JAXBElement<String> type;
    protected long size;
    protected long attrSize;
    protected long keysSize;
    protected int cachedObjects;
    protected int usedObjects;
    protected int puts;
    protected int gets;
    protected int hits;
    protected int changes;
    protected int removes;
    protected int evictions;
    protected int instanceInvalidations;
    protected int clusterInvalidations;
    @XmlElementRef(name = "updateTime", type = JAXBElement.class)
    protected JAXBElement<String> updateTime;
    @XmlElement(required = true)
    protected STATECOLOR dispstatus;

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
     * Gets the value of the owner property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getOwner() {
        return owner;
    }

    /**
     * Sets the value of the owner property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setOwner(JAXBElement<String> value) {
        this.owner = ((JAXBElement<String> ) value);
    }

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
     * Gets the value of the size property.
     * 
     */
    public long getSize() {
        return size;
    }

    /**
     * Sets the value of the size property.
     * 
     */
    public void setSize(long value) {
        this.size = value;
    }

    /**
     * Gets the value of the attrSize property.
     * 
     */
    public long getAttrSize() {
        return attrSize;
    }

    /**
     * Sets the value of the attrSize property.
     * 
     */
    public void setAttrSize(long value) {
        this.attrSize = value;
    }

    /**
     * Gets the value of the keysSize property.
     * 
     */
    public long getKeysSize() {
        return keysSize;
    }

    /**
     * Sets the value of the keysSize property.
     * 
     */
    public void setKeysSize(long value) {
        this.keysSize = value;
    }

    /**
     * Gets the value of the cachedObjects property.
     * 
     */
    public int getCachedObjects() {
        return cachedObjects;
    }

    /**
     * Sets the value of the cachedObjects property.
     * 
     */
    public void setCachedObjects(int value) {
        this.cachedObjects = value;
    }

    /**
     * Gets the value of the usedObjects property.
     * 
     */
    public int getUsedObjects() {
        return usedObjects;
    }

    /**
     * Sets the value of the usedObjects property.
     * 
     */
    public void setUsedObjects(int value) {
        this.usedObjects = value;
    }

    /**
     * Gets the value of the puts property.
     * 
     */
    public int getPuts() {
        return puts;
    }

    /**
     * Sets the value of the puts property.
     * 
     */
    public void setPuts(int value) {
        this.puts = value;
    }

    /**
     * Gets the value of the gets property.
     * 
     */
    public int getGets() {
        return gets;
    }

    /**
     * Sets the value of the gets property.
     * 
     */
    public void setGets(int value) {
        this.gets = value;
    }

    /**
     * Gets the value of the hits property.
     * 
     */
    public int getHits() {
        return hits;
    }

    /**
     * Sets the value of the hits property.
     * 
     */
    public void setHits(int value) {
        this.hits = value;
    }

    /**
     * Gets the value of the changes property.
     * 
     */
    public int getChanges() {
        return changes;
    }

    /**
     * Sets the value of the changes property.
     * 
     */
    public void setChanges(int value) {
        this.changes = value;
    }

    /**
     * Gets the value of the removes property.
     * 
     */
    public int getRemoves() {
        return removes;
    }

    /**
     * Sets the value of the removes property.
     * 
     */
    public void setRemoves(int value) {
        this.removes = value;
    }

    /**
     * Gets the value of the evictions property.
     * 
     */
    public int getEvictions() {
        return evictions;
    }

    /**
     * Sets the value of the evictions property.
     * 
     */
    public void setEvictions(int value) {
        this.evictions = value;
    }

    /**
     * Gets the value of the instanceInvalidations property.
     * 
     */
    public int getInstanceInvalidations() {
        return instanceInvalidations;
    }

    /**
     * Sets the value of the instanceInvalidations property.
     * 
     */
    public void setInstanceInvalidations(int value) {
        this.instanceInvalidations = value;
    }

    /**
     * Gets the value of the clusterInvalidations property.
     * 
     */
    public int getClusterInvalidations() {
        return clusterInvalidations;
    }

    /**
     * Sets the value of the clusterInvalidations property.
     * 
     */
    public void setClusterInvalidations(int value) {
        this.clusterInvalidations = value;
    }

    /**
     * Gets the value of the updateTime property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getUpdateTime() {
        return updateTime;
    }

    /**
     * Sets the value of the updateTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setUpdateTime(JAXBElement<String> value) {
        this.updateTime = ((JAXBElement<String> ) value);
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

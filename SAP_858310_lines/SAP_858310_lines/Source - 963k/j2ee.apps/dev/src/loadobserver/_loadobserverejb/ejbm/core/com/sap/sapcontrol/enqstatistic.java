
package com.sap.sapcontrol;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for EnqStatistic element declaration.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;element name="EnqStatistic">
 *   &lt;complexType>
 *     &lt;complexContent>
 *       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *         &lt;sequence>
 *           &lt;element name="owner-now" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *           &lt;element name="owner-high" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *           &lt;element name="owner-max" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *           &lt;element name="owner-state" type="{urn:SAPControl}STATE-COLOR"/>
 *           &lt;element name="arguments-now" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *           &lt;element name="arguments-high" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *           &lt;element name="arguments-max" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *           &lt;element name="arguments-state" type="{urn:SAPControl}STATE-COLOR"/>
 *           &lt;element name="locks-now" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *           &lt;element name="locks-high" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *           &lt;element name="locks-max" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *           &lt;element name="locks-state" type="{urn:SAPControl}STATE-COLOR"/>
 *           &lt;element name="enqueue-requests" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *           &lt;element name="enqueue-rejects" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *           &lt;element name="enqueue-errors" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *           &lt;element name="dequeue-requests" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *           &lt;element name="dequeue-errors" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *           &lt;element name="dequeue-all-requests" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *           &lt;element name="cleanup-requests" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *           &lt;element name="backup-requests" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *           &lt;element name="reporting-requests" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *           &lt;element name="compress-requests" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *           &lt;element name="verify-requests" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *           &lt;element name="lock-time" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *           &lt;element name="lock-wait-time" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *           &lt;element name="server-time" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *           &lt;element name="replication-state" type="{urn:SAPControl}STATE-COLOR"/>
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
    "ownerNow",
    "ownerHigh",
    "ownerMax",
    "ownerState",
    "argumentsNow",
    "argumentsHigh",
    "argumentsMax",
    "argumentsState",
    "locksNow",
    "locksHigh",
    "locksMax",
    "locksState",
    "enqueueRequests",
    "enqueueRejects",
    "enqueueErrors",
    "dequeueRequests",
    "dequeueErrors",
    "dequeueAllRequests",
    "cleanupRequests",
    "backupRequests",
    "reportingRequests",
    "compressRequests",
    "verifyRequests",
    "lockTime",
    "lockWaitTime",
    "serverTime",
    "replicationState"
})
@XmlRootElement(name = "EnqStatistic")
public class EnqStatistic {

    @XmlElement(name = "owner-now")
    protected int ownerNow;
    @XmlElement(name = "owner-high")
    protected int ownerHigh;
    @XmlElement(name = "owner-max")
    protected int ownerMax;
    @XmlElement(name = "owner-state", required = true)
    protected STATECOLOR ownerState;
    @XmlElement(name = "arguments-now")
    protected int argumentsNow;
    @XmlElement(name = "arguments-high")
    protected int argumentsHigh;
    @XmlElement(name = "arguments-max")
    protected int argumentsMax;
    @XmlElement(name = "arguments-state", required = true)
    protected STATECOLOR argumentsState;
    @XmlElement(name = "locks-now")
    protected int locksNow;
    @XmlElement(name = "locks-high")
    protected int locksHigh;
    @XmlElement(name = "locks-max")
    protected int locksMax;
    @XmlElement(name = "locks-state", required = true)
    protected STATECOLOR locksState;
    @XmlElement(name = "enqueue-requests")
    protected long enqueueRequests;
    @XmlElement(name = "enqueue-rejects")
    protected long enqueueRejects;
    @XmlElement(name = "enqueue-errors")
    protected long enqueueErrors;
    @XmlElement(name = "dequeue-requests")
    protected long dequeueRequests;
    @XmlElement(name = "dequeue-errors")
    protected long dequeueErrors;
    @XmlElement(name = "dequeue-all-requests")
    protected long dequeueAllRequests;
    @XmlElement(name = "cleanup-requests")
    protected long cleanupRequests;
    @XmlElement(name = "backup-requests")
    protected long backupRequests;
    @XmlElement(name = "reporting-requests")
    protected long reportingRequests;
    @XmlElement(name = "compress-requests")
    protected long compressRequests;
    @XmlElement(name = "verify-requests")
    protected long verifyRequests;
    @XmlElement(name = "lock-time")
    protected double lockTime;
    @XmlElement(name = "lock-wait-time")
    protected double lockWaitTime;
    @XmlElement(name = "server-time")
    protected double serverTime;
    @XmlElement(name = "replication-state", required = true)
    protected STATECOLOR replicationState;

    /**
     * Gets the value of the ownerNow property.
     * 
     */
    public int getOwnerNow() {
        return ownerNow;
    }

    /**
     * Sets the value of the ownerNow property.
     * 
     */
    public void setOwnerNow(int value) {
        this.ownerNow = value;
    }

    /**
     * Gets the value of the ownerHigh property.
     * 
     */
    public int getOwnerHigh() {
        return ownerHigh;
    }

    /**
     * Sets the value of the ownerHigh property.
     * 
     */
    public void setOwnerHigh(int value) {
        this.ownerHigh = value;
    }

    /**
     * Gets the value of the ownerMax property.
     * 
     */
    public int getOwnerMax() {
        return ownerMax;
    }

    /**
     * Sets the value of the ownerMax property.
     * 
     */
    public void setOwnerMax(int value) {
        this.ownerMax = value;
    }

    /**
     * Gets the value of the ownerState property.
     * 
     * @return
     *     possible object is
     *     {@link STATECOLOR }
     *     
     */
    public STATECOLOR getOwnerState() {
        return ownerState;
    }

    /**
     * Sets the value of the ownerState property.
     * 
     * @param value
     *     allowed object is
     *     {@link STATECOLOR }
     *     
     */
    public void setOwnerState(STATECOLOR value) {
        this.ownerState = value;
    }

    /**
     * Gets the value of the argumentsNow property.
     * 
     */
    public int getArgumentsNow() {
        return argumentsNow;
    }

    /**
     * Sets the value of the argumentsNow property.
     * 
     */
    public void setArgumentsNow(int value) {
        this.argumentsNow = value;
    }

    /**
     * Gets the value of the argumentsHigh property.
     * 
     */
    public int getArgumentsHigh() {
        return argumentsHigh;
    }

    /**
     * Sets the value of the argumentsHigh property.
     * 
     */
    public void setArgumentsHigh(int value) {
        this.argumentsHigh = value;
    }

    /**
     * Gets the value of the argumentsMax property.
     * 
     */
    public int getArgumentsMax() {
        return argumentsMax;
    }

    /**
     * Sets the value of the argumentsMax property.
     * 
     */
    public void setArgumentsMax(int value) {
        this.argumentsMax = value;
    }

    /**
     * Gets the value of the argumentsState property.
     * 
     * @return
     *     possible object is
     *     {@link STATECOLOR }
     *     
     */
    public STATECOLOR getArgumentsState() {
        return argumentsState;
    }

    /**
     * Sets the value of the argumentsState property.
     * 
     * @param value
     *     allowed object is
     *     {@link STATECOLOR }
     *     
     */
    public void setArgumentsState(STATECOLOR value) {
        this.argumentsState = value;
    }

    /**
     * Gets the value of the locksNow property.
     * 
     */
    public int getLocksNow() {
        return locksNow;
    }

    /**
     * Sets the value of the locksNow property.
     * 
     */
    public void setLocksNow(int value) {
        this.locksNow = value;
    }

    /**
     * Gets the value of the locksHigh property.
     * 
     */
    public int getLocksHigh() {
        return locksHigh;
    }

    /**
     * Sets the value of the locksHigh property.
     * 
     */
    public void setLocksHigh(int value) {
        this.locksHigh = value;
    }

    /**
     * Gets the value of the locksMax property.
     * 
     */
    public int getLocksMax() {
        return locksMax;
    }

    /**
     * Sets the value of the locksMax property.
     * 
     */
    public void setLocksMax(int value) {
        this.locksMax = value;
    }

    /**
     * Gets the value of the locksState property.
     * 
     * @return
     *     possible object is
     *     {@link STATECOLOR }
     *     
     */
    public STATECOLOR getLocksState() {
        return locksState;
    }

    /**
     * Sets the value of the locksState property.
     * 
     * @param value
     *     allowed object is
     *     {@link STATECOLOR }
     *     
     */
    public void setLocksState(STATECOLOR value) {
        this.locksState = value;
    }

    /**
     * Gets the value of the enqueueRequests property.
     * 
     */
    public long getEnqueueRequests() {
        return enqueueRequests;
    }

    /**
     * Sets the value of the enqueueRequests property.
     * 
     */
    public void setEnqueueRequests(long value) {
        this.enqueueRequests = value;
    }

    /**
     * Gets the value of the enqueueRejects property.
     * 
     */
    public long getEnqueueRejects() {
        return enqueueRejects;
    }

    /**
     * Sets the value of the enqueueRejects property.
     * 
     */
    public void setEnqueueRejects(long value) {
        this.enqueueRejects = value;
    }

    /**
     * Gets the value of the enqueueErrors property.
     * 
     */
    public long getEnqueueErrors() {
        return enqueueErrors;
    }

    /**
     * Sets the value of the enqueueErrors property.
     * 
     */
    public void setEnqueueErrors(long value) {
        this.enqueueErrors = value;
    }

    /**
     * Gets the value of the dequeueRequests property.
     * 
     */
    public long getDequeueRequests() {
        return dequeueRequests;
    }

    /**
     * Sets the value of the dequeueRequests property.
     * 
     */
    public void setDequeueRequests(long value) {
        this.dequeueRequests = value;
    }

    /**
     * Gets the value of the dequeueErrors property.
     * 
     */
    public long getDequeueErrors() {
        return dequeueErrors;
    }

    /**
     * Sets the value of the dequeueErrors property.
     * 
     */
    public void setDequeueErrors(long value) {
        this.dequeueErrors = value;
    }

    /**
     * Gets the value of the dequeueAllRequests property.
     * 
     */
    public long getDequeueAllRequests() {
        return dequeueAllRequests;
    }

    /**
     * Sets the value of the dequeueAllRequests property.
     * 
     */
    public void setDequeueAllRequests(long value) {
        this.dequeueAllRequests = value;
    }

    /**
     * Gets the value of the cleanupRequests property.
     * 
     */
    public long getCleanupRequests() {
        return cleanupRequests;
    }

    /**
     * Sets the value of the cleanupRequests property.
     * 
     */
    public void setCleanupRequests(long value) {
        this.cleanupRequests = value;
    }

    /**
     * Gets the value of the backupRequests property.
     * 
     */
    public long getBackupRequests() {
        return backupRequests;
    }

    /**
     * Sets the value of the backupRequests property.
     * 
     */
    public void setBackupRequests(long value) {
        this.backupRequests = value;
    }

    /**
     * Gets the value of the reportingRequests property.
     * 
     */
    public long getReportingRequests() {
        return reportingRequests;
    }

    /**
     * Sets the value of the reportingRequests property.
     * 
     */
    public void setReportingRequests(long value) {
        this.reportingRequests = value;
    }

    /**
     * Gets the value of the compressRequests property.
     * 
     */
    public long getCompressRequests() {
        return compressRequests;
    }

    /**
     * Sets the value of the compressRequests property.
     * 
     */
    public void setCompressRequests(long value) {
        this.compressRequests = value;
    }

    /**
     * Gets the value of the verifyRequests property.
     * 
     */
    public long getVerifyRequests() {
        return verifyRequests;
    }

    /**
     * Sets the value of the verifyRequests property.
     * 
     */
    public void setVerifyRequests(long value) {
        this.verifyRequests = value;
    }

    /**
     * Gets the value of the lockTime property.
     * 
     */
    public double getLockTime() {
        return lockTime;
    }

    /**
     * Sets the value of the lockTime property.
     * 
     */
    public void setLockTime(double value) {
        this.lockTime = value;
    }

    /**
     * Gets the value of the lockWaitTime property.
     * 
     */
    public double getLockWaitTime() {
        return lockWaitTime;
    }

    /**
     * Sets the value of the lockWaitTime property.
     * 
     */
    public void setLockWaitTime(double value) {
        this.lockWaitTime = value;
    }

    /**
     * Gets the value of the serverTime property.
     * 
     */
    public double getServerTime() {
        return serverTime;
    }

    /**
     * Sets the value of the serverTime property.
     * 
     */
    public void setServerTime(double value) {
        this.serverTime = value;
    }

    /**
     * Gets the value of the replicationState property.
     * 
     * @return
     *     possible object is
     *     {@link STATECOLOR }
     *     
     */
    public STATECOLOR getReplicationState() {
        return replicationState;
    }

    /**
     * Sets the value of the replicationState property.
     * 
     * @param value
     *     allowed object is
     *     {@link STATECOLOR }
     *     
     */
    public void setReplicationState(STATECOLOR value) {
        this.replicationState = value;
    }

}

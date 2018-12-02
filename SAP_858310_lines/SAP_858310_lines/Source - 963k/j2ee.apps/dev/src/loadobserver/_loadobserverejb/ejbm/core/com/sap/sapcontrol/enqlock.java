
package com.sap.sapcontrol;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for EnqLock complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="EnqLock">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="lock-name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="lock-arg" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="lock-mode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="owner" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="owner-vb" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="use-count-owner" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="use-count-owner-vb" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="client" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="user" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="transaction" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="object" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="backup" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EnqLock", propOrder = {
    "lockName",
    "lockArg",
    "lockMode",
    "owner",
    "ownerVb",
    "useCountOwner",
    "useCountOwnerVb",
    "client",
    "user",
    "transaction",
    "object",
    "backup"
})
public class EnqLock {

    @XmlElementRef(name = "lock-name", type = JAXBElement.class)
    protected JAXBElement<String> lockName;
    @XmlElementRef(name = "lock-arg", type = JAXBElement.class)
    protected JAXBElement<String> lockArg;
    @XmlElementRef(name = "lock-mode", type = JAXBElement.class)
    protected JAXBElement<String> lockMode;
    @XmlElementRef(name = "owner", type = JAXBElement.class)
    protected JAXBElement<String> owner;
    @XmlElementRef(name = "owner-vb", type = JAXBElement.class)
    protected JAXBElement<String> ownerVb;
    @XmlElement(name = "use-count-owner")
    protected int useCountOwner;
    @XmlElement(name = "use-count-owner-vb")
    protected int useCountOwnerVb;
    @XmlElementRef(name = "client", type = JAXBElement.class)
    protected JAXBElement<String> client;
    @XmlElementRef(name = "user", type = JAXBElement.class)
    protected JAXBElement<String> user;
    @XmlElementRef(name = "transaction", type = JAXBElement.class)
    protected JAXBElement<String> transaction;
    @XmlElementRef(name = "object", type = JAXBElement.class)
    protected JAXBElement<String> object;
    protected boolean backup;

    /**
     * Gets the value of the lockName property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getLockName() {
        return lockName;
    }

    /**
     * Sets the value of the lockName property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setLockName(JAXBElement<String> value) {
        this.lockName = ((JAXBElement<String> ) value);
    }

    /**
     * Gets the value of the lockArg property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getLockArg() {
        return lockArg;
    }

    /**
     * Sets the value of the lockArg property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setLockArg(JAXBElement<String> value) {
        this.lockArg = ((JAXBElement<String> ) value);
    }

    /**
     * Gets the value of the lockMode property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getLockMode() {
        return lockMode;
    }

    /**
     * Sets the value of the lockMode property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setLockMode(JAXBElement<String> value) {
        this.lockMode = ((JAXBElement<String> ) value);
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
     * Gets the value of the ownerVb property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getOwnerVb() {
        return ownerVb;
    }

    /**
     * Sets the value of the ownerVb property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setOwnerVb(JAXBElement<String> value) {
        this.ownerVb = ((JAXBElement<String> ) value);
    }

    /**
     * Gets the value of the useCountOwner property.
     * 
     */
    public int getUseCountOwner() {
        return useCountOwner;
    }

    /**
     * Sets the value of the useCountOwner property.
     * 
     */
    public void setUseCountOwner(int value) {
        this.useCountOwner = value;
    }

    /**
     * Gets the value of the useCountOwnerVb property.
     * 
     */
    public int getUseCountOwnerVb() {
        return useCountOwnerVb;
    }

    /**
     * Sets the value of the useCountOwnerVb property.
     * 
     */
    public void setUseCountOwnerVb(int value) {
        this.useCountOwnerVb = value;
    }

    /**
     * Gets the value of the client property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getClient() {
        return client;
    }

    /**
     * Sets the value of the client property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setClient(JAXBElement<String> value) {
        this.client = ((JAXBElement<String> ) value);
    }

    /**
     * Gets the value of the user property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getUser() {
        return user;
    }

    /**
     * Sets the value of the user property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setUser(JAXBElement<String> value) {
        this.user = ((JAXBElement<String> ) value);
    }

    /**
     * Gets the value of the transaction property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getTransaction() {
        return transaction;
    }

    /**
     * Sets the value of the transaction property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setTransaction(JAXBElement<String> value) {
        this.transaction = ((JAXBElement<String> ) value);
    }

    /**
     * Gets the value of the object property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getObject() {
        return object;
    }

    /**
     * Sets the value of the object property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setObject(JAXBElement<String> value) {
        this.object = ((JAXBElement<String> ) value);
    }

    /**
     * Gets the value of the backup property.
     * 
     */
    public boolean isBackup() {
        return backup;
    }

    /**
     * Sets the value of the backup property.
     * 
     */
    public void setBackup(boolean value) {
        this.backup = value;
    }

}

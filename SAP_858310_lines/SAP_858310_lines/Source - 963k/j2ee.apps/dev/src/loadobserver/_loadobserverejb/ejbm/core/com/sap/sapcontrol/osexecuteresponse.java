
package com.sap.sapcontrol;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for OSExecuteResponse element declaration.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;element name="OSExecuteResponse">
 *   &lt;complexType>
 *     &lt;complexContent>
 *       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *         &lt;sequence>
 *           &lt;element name="exitcode" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *           &lt;element name="pid" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *           &lt;element name="lines" type="{urn:SAPControl}ArrayOfString"/>
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
    "exitcode",
    "pid",
    "lines"
})
@XmlRootElement(name = "OSExecuteResponse")
public class OSExecuteResponse {

    protected int exitcode;
    protected int pid;
    @XmlElement(required = true)
    protected ArrayOfString lines;

    /**
     * Gets the value of the exitcode property.
     * 
     */
    public int getExitcode() {
        return exitcode;
    }

    /**
     * Sets the value of the exitcode property.
     * 
     */
    public void setExitcode(int value) {
        this.exitcode = value;
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
     * Gets the value of the lines property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfString }
     *     
     */
    public ArrayOfString getLines() {
        return lines;
    }

    /**
     * Sets the value of the lines property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfString }
     *     
     */
    public void setLines(ArrayOfString value) {
        this.lines = value;
    }

}

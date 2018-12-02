
package com.sap.sapcontrol;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for GetAlertTreeResponse element declaration.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;element name="GetAlertTreeResponse">
 *   &lt;complexType>
 *     &lt;complexContent>
 *       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *         &lt;sequence>
 *           &lt;element name="tree" type="{urn:SAPControl}ArrayOfAlertNode" minOccurs="0"/>
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
    "tree"
})
@XmlRootElement(name = "GetAlertTreeResponse")
public class GetAlertTreeResponse {

    @XmlElementRef(name = "tree", type = JAXBElement.class)
    protected JAXBElement<ArrayOfAlertNode> tree;

    /**
     * Gets the value of the tree property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfAlertNode }{@code >}
     *     
     */
    public JAXBElement<ArrayOfAlertNode> getTree() {
        return tree;
    }

    /**
     * Sets the value of the tree property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfAlertNode }{@code >}
     *     
     */
    public void setTree(JAXBElement<ArrayOfAlertNode> value) {
        this.tree = ((JAXBElement<ArrayOfAlertNode> ) value);
    }

}

package com.sap.engine.services.webservices.jaxrpc.exceptions;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Iterator;
import javax.xml.namespace.QName;
import javax.xml.soap.Detail;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.transform.TransformerException;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import com.sap.engine.lib.xml.parser.ParserException;
import com.sap.engine.lib.xml.parser.tokenizer.XMLTokenReader;
import com.sap.engine.lib.xml.parser.tokenizer.XMLTokenReaderFactory;
import com.sap.engine.services.webservices.espbase.xi.XIFrameworkConstants;
import com.sap.engine.services.webservices.tools.SharedTransformers;

/**
 * Extents javax.xml.rpc.soap.SOAPFaultException to handle the non serializable
 * content.
 * 
 * @author I056242
 * 
 */
public class SOAPFaultException extends javax.xml.rpc.soap.SOAPFaultException {

  private static final String SET_ACCESSIBLE_METHOD = "setAccessible";

  private static final String DETAIL_FIELD = "detail";

  private static final long serialVersionUID = -3033696793697460656L;

  /** Non serializable data.  */
  private transient Detail detail;

  public SOAPFaultException(QName faultcode, String faultstring, String faultactor, Detail faultdetail) {
    super(faultcode, faultstring, faultactor, faultdetail);

    this.detail = faultdetail;

    clearNonSerParentFields();
  }

  /**
   * Overload to return the child member. The parent member is nulled due to
   * serialization reasons.
   */
  public Detail getDetail() {
    return detail;
  }

  /**
   * Clear the non serializable parent fields.
   */
  private void clearNonSerParentFields() {
    try {

      Field detailField = javax.xml.rpc.soap.SOAPFaultException.class.getDeclaredField(DETAIL_FIELD);

      Method method = detailField.getClass().getMethod(SET_ACCESSIBLE_METHOD, new Class[] { boolean.class });
      method.invoke(detailField, new Object[] { Boolean.TRUE });

      // Null the parent field.
      detailField.set(this, null);
    } catch (Exception e) {
      throw new RuntimeException("Can not clear non serializable private member of the parent.", e);
    }
  }

  /**
   * Transforms <code>node</code> to String using SharedTransformer
   * 
   * @param node
   *          Node
   * @return String
   */

  private String transform(Node node) {
    String elementString;
    try {
      elementString = SharedTransformers.transform(SharedTransformers.DEFAULT_TRANSFORMER, node);
    } catch (TransformerException e) {
      // gives a clue with node name if exception occurs during transformation
      elementString = "<" + node.getNodeName() + "...";
    }
    return elementString;
  }

  private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {

    String faultElementString = (String) ois.readObject();

    try {
      // Create a reader from the string element.
      byte[] faultByteArray = faultElementString.getBytes(XIFrameworkConstants.XI_MESSAGE_ENCODING);
      ByteArrayInputStream dataInputStream = new ByteArrayInputStream(faultByteArray);
      XMLTokenReader xmlReader = createXMLTokenReader(dataInputStream);

      detail = loadSOAPDetail(xmlReader);
    } catch (Exception e) {
      throw new IOException("Can not deserialize the fault detail element.");
    }

    ois.defaultReadObject();
  }

  private void writeObject(ObjectOutputStream out) throws IOException {
    // Convert the node to serializable string.
    String stringDetail = transform(detail);

    out.writeObject(stringDetail);

    out.defaultWriteObject();
  }

  /**
   * 
   * @param reader
   * @return
   * @throws SOAPException
   * @throws ParserException
   */
  private Detail loadSOAPDetail(XMLTokenReader reader) throws SOAPException, ParserException {

    SOAPFactory factory = SOAPFactory.newInstance();

    Detail detailNode = factory.createDetail();

    Document document = detailNode.getOwnerDocument();

    SOAPElement element = (SOAPElement) reader.getDOMRepresentation(document);
    NamedNodeMap attributes = element.getAttributes();

    for (int j = 0; j < attributes.getLength(); j++) {
      Attr attrib = (Attr) attributes.item(j);
      attrib = (Attr) detailNode.getOwnerDocument().importNode(attrib, false);
      detailNode.setAttributeNodeNS(attrib);
    }

    for (Iterator<Node> elements = element.getChildElements(); elements.hasNext();) {
      Node next = elements.next();
      if (next instanceof SOAPElement) {
        detailNode.addChildElement((SOAPElement) next);
      }
      if (next instanceof javax.xml.soap.Text) {
        detailNode.addTextNode(((javax.xml.soap.Text) next).getValue());
      }
    }

    reader.next();
    reader.passChars();
    return detailNode;
  }

  /**
   * Create reader from byte[].
   * @param dataInputStream
   * @return
   * @throws ParserException
   */
  private XMLTokenReader createXMLTokenReader(ByteArrayInputStream dataInputStream) throws ParserException {
    XMLTokenReader xmlReader = XMLTokenReaderFactory.newInstance(dataInputStream);
    xmlReader.begin();
    xmlReader.moveToNextElementStart();
    return xmlReader;
  }

}

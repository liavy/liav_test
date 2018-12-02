package com.sap.engine.services.webservices.espbase.client.dynamic.types.impl;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.sap.engine.services.webservices.espbase.client.dynamic.util.Util;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DAnnotationItem;


public abstract class DAnnotationItemImpl implements DAnnotationItem {

  private String source;
  private NodeList content;
  
  public String getSource() {
    return(source);
  }

  public void setSource(String source) {
    this.source = source;
  } 
  
  public NodeList getContent() {
    return(content);
  }

  public void setContent(NodeList content) {
    this.content = content;
  }
  
  public abstract void initToStringBuffer(StringBuffer toStringBuffer, String offset);
  
  public void initToStringBuffer_DAnnotationItem(StringBuffer toStringBuffer, String offset) {
    Util.initToStringBuffer_ObjectValue(toStringBuffer, offset, "source : ", source);
    initToStringBuffer_Content(toStringBuffer, offset);
  }
  
  private void initToStringBuffer_Content(StringBuffer toStringBuffer, String offset) {
    Util.initToStringBuffer_AppendCReturnOffsetAndId(toStringBuffer, offset, "content");
    for(int i = 0; i < content.getLength(); i++) {
      Node node = content.item(i);
      initToStringBuffer_Node(toStringBuffer, offset + Util.TO_STRING_OFFSET, node);
    }
  }
  
  private void initToStringBuffer_Node(StringBuffer toStringBuffer, String offset, Node node) {
    if(node instanceof Text) {
      initToStringBuffer_Text(toStringBuffer, offset, (Text)node);
    } else if(node instanceof Element) {
      initToStringBuffer_Element(toStringBuffer, offset, (Element)node);
    }
  }
  
  private void initToStringBuffer_Text(StringBuffer toStringBuffer, String offset, Text text) {
    String value = text.getNodeValue();
    if(!isWhiteSpacesOnly(value)) {
      toStringBuffer.append("\n");
      toStringBuffer.append(offset + Util.TO_STRING_OFFSET);
      toStringBuffer.append(value.trim());
    }
  }
  
  private boolean isWhiteSpacesOnly(String value) {
    for(int i = 0; i < value.length(); i++) {
      char ch = value.charAt(i);
      if(ch != '\r' && ch != '\t' && ch != '\n') {
        return(false);
      }
    }
    return(true);
  }
  
  private void initToStringBuffer_Element(StringBuffer toStringBuffer, String offset, Element element) {
    toStringBuffer.append("\n");
    toStringBuffer.append(offset);
    toStringBuffer.append("<");
    toStringBuffer.append(element.getNodeName());
    initToStringBuffer_Attributes(toStringBuffer, element);
    toStringBuffer.append(">");
    initToStringBuffer_ChildNodes(toStringBuffer, offset + Util.TO_STRING_OFFSET, element);
    toStringBuffer.append("\n");
    toStringBuffer.append(offset);
    toStringBuffer.append("</");
    toStringBuffer.append(element.getNodeName());
    toStringBuffer.append(">");
  }
  
  private void initToStringBuffer_ChildNodes(StringBuffer toStringBuffer, String offset, Element element) {
    NodeList nodeList = element.getChildNodes();
    for(int i = 0; i < nodeList.getLength(); i++) {
      Node node = nodeList.item(i);
      initToStringBuffer_Node(toStringBuffer, offset + Util.TO_STRING_OFFSET, node);
    }
  }
  
  private void initToStringBuffer_Attributes(StringBuffer toStringBuffer, Element element) {
    NamedNodeMap namedNodeMap = element.getAttributes();
    for(int i = 0; i < namedNodeMap.getLength(); i++) {
      Attr attr = (Attr)(namedNodeMap.item(i));
      initToStringBuffer_Attr(toStringBuffer, attr);
    }
  }
  
  private void initToStringBuffer_Attr(StringBuffer toStringBuffer, Attr attr) {
    toStringBuffer.append(" ");
    toStringBuffer.append(attr.getNodeName());
    toStringBuffer.append("='");
    toStringBuffer.append(attr.getValue());
    toStringBuffer.append("'");
  }
}

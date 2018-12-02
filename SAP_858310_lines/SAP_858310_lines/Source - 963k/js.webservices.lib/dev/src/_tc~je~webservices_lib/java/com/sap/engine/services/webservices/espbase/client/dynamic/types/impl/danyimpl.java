/**
 * 
 */
package com.sap.engine.services.webservices.espbase.client.dynamic.types.impl;

import com.sap.engine.services.webservices.espbase.client.dynamic.util.Util;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DAny;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DOccurrenceable;

/**
 * @author dimitar-v
 *
 */
public class DAnyImpl extends DFieldImpl implements DAny, DOccurrenceable {

  private int minOccurs;
  private int maxOccurs;
  
  protected DAnyImpl() {
    super();
    minOccurs = 0;
    maxOccurs = 0;
  }
	
  public int getMinOccurs() {
  	return minOccurs;
  }
  
  public int getMaxOccurs() {		
  	return maxOccurs;
  }
  
  public void setMaxOccurs(int maxOccurs) {
  	this.maxOccurs = maxOccurs;
  }
  
  public void setMinOccurs(int minOccurs) {
  	this.minOccurs = minOccurs;
  }

  public String toString() {
    StringBuffer toStringBuffer = new StringBuffer();
    initToStringBuffer(toStringBuffer, "");
    return(toStringBuffer.toString());
  }
  
  public void initToStringBuffer(StringBuffer toStringBuffer, String offset) {
    Util.initToStringBuffer_AppendOffsetAndId(toStringBuffer, offset, "DAny");
    initToStringBuffer_DField(toStringBuffer, offset + Util.TO_STRING_OFFSET);
    Util.initToStringBuffer_DOccurrenceable(toStringBuffer, offset + Util.TO_STRING_OFFSET, this);
  }
}

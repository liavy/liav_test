package com.sap.engine.services.webservices.espbase.client.dynamic.types.impl;

import com.sap.engine.services.webservices.espbase.client.dynamic.util.Util;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DSimpleType;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.Facet;

public class DSimpleTypeImpl extends DBaseTypeImpl implements DSimpleType{

  private Facet[] facets; 
	
  protected DSimpleTypeImpl() {
    super();
    facets = new Facet[0];
  }
  
  public Facet[] getFacets() {
    return(facets);
  }
	
  protected void setFacets(Facet[] facets) {
    this.facets = facets;
  }
  
  public void setType(int type) {
    this.type = type;
  }
  
  public Facet getFacet(String facetName) {
    for(int i = 0; i < facets.length; i++) {
      Facet facet = facets[i];
      if(facet.getName().equals(facetName)) {
        return(facet);
      }
    }
    return(null);
  }

  public String getFacetValue(String facetName) {
    Facet facet = getFacet(facetName);
    return(facet == null ? null : facet.getValue());
  }
    
  public String toString() {
    StringBuffer toStringBuffer = new StringBuffer();
    initToStringBuffer(toStringBuffer, "");
    return(toStringBuffer.toString());
  }
  
  public void initToStringBuffer(StringBuffer toStringBuffer, String offset) {
    Util.initToStringBuffer_AppendOffsetAndId(toStringBuffer, offset, "DSimpleType");
    initToStringBuffer_DBaseType(toStringBuffer, offset + Util.TO_STRING_OFFSET);
    initToStringBuffer_Facets(toStringBuffer, offset + Util.TO_STRING_OFFSET);
  }
  
  public void initToStringBuffer_Facets(StringBuffer toStringBuffer, String offset) {
    if(facets.length != 0) {
      Util.initToStringBuffer_AppendCReturnOffsetAndId(toStringBuffer, offset, "facets");
      for(int i = 0; i < facets.length; i++) {
        Facet facet = facets[i];
        toStringBuffer.append("\n");
        facet.initToStringBuffer(toStringBuffer, offset + Util.TO_STRING_OFFSET);
      }
    }
  }
}

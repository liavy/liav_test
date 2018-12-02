package com.sap.engine.services.webservices.espbase.client.jaxws.metadata;

import javax.jws.soap.SOAPBinding;

public class SOAPBindingConfiguration {
  
  private SOAPBinding.ParameterStyle parameterStyle;
  private SOAPBinding.Style style;
  private SOAPBinding.Use use;
  
  protected SOAPBindingConfiguration() {
    parameterStyle = SOAPBinding.ParameterStyle.WRAPPED;
    style = SOAPBinding.Style.DOCUMENT;
    use = SOAPBinding.Use.LITERAL;
  }
  
  protected SOAPBinding.ParameterStyle getParameterStyle() {
    return(parameterStyle);
  }

  protected void setParameterStyle(SOAPBinding.ParameterStyle parameterStyle) {
    this.parameterStyle = parameterStyle;
  }

  protected SOAPBinding.Style getStyle() {
    return(style);
  }

  protected void setStyle(SOAPBinding.Style style) {
    this.style = style;
  }

  protected SOAPBinding.Use getUse() {
    return(use);
  }

  protected void setUse(SOAPBinding.Use use) {
    this.use = use;
  }
  
  protected boolean isRpcLiteral() {
    return(style.equals(SOAPBinding.Style.RPC) && use.equals(SOAPBinding.Use.LITERAL));
  }
  
  protected boolean isDocumentBare() {
    return(style.equals(SOAPBinding.Style.DOCUMENT) && use.equals(SOAPBinding.Use.LITERAL) && parameterStyle.equals(SOAPBinding.ParameterStyle.BARE));
  }
  
  protected boolean isDocumentWrapped() {
    return(style.equals(SOAPBinding.Style.DOCUMENT) && use.equals(SOAPBinding.Use.LITERAL) && parameterStyle.equals(SOAPBinding.ParameterStyle.WRAPPED));
  }
}

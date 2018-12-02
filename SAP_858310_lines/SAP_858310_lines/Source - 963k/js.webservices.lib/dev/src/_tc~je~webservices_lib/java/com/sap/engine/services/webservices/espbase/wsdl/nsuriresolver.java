package com.sap.engine.services.webservices.espbase.wsdl;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

/**
 * <p>An object that implements this interface that can be called by the processor
 * to turn a namespace used in xsl:import into a Source object.
 */
public interface NSURIResolver extends URIResolver {
   
  /**
   * Called by the processor when it encounters
   * an xsl:include, xsl:import, or document() function and the 
   * object can not be resolved by the its relative path.
   * (javax.xml.transform.URIResolver.resolve(String href, String base) 
   * has returned null)
   * 
   *    
   * @param tartgetNamespace of the imported schema.
   *
   * @return A Source object, or null if the namespace cannot be resolved.
   *   
   * @throws TransformerException if an error occurs when trying to
   * resolve the URI.
   */
  public Source resolveByNS(String tartgetNamespace)
      throws TransformerException;

}

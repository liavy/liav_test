package com.sap.sl.util.components.xml.api;

import com.sap.sl.util.components.api.ComponentElementIF;
import com.sap.sl.util.loader.Loader;

/**
 * @author d030435
 *
 */

public abstract class ComponentElementXMLizerFactoryIF {
  private static ComponentElementXMLizerFactoryIF INSTANCE = null;
  private final static String FACTORY_IMPL_CLASS="com.sap.sl.util.components.xml.impl.ComponentElementXMLizerFactory";
  
  /**
   * Gets the ComponentElementXMLizerFactory instance
   * If you want a special @see java.lang.ClassLoader to be
   * used for the loading of the class use
   * @see com.sap.sl.util.loader.Loader.setClassloader
   *
   *@return    The ComponentElementXMLizerFactory instance
   */
  public static ComponentElementXMLizerFactoryIF getInstance() {
    if (null == ComponentElementXMLizerFactoryIF.INSTANCE) {
      ComponentElementXMLizerFactoryIF.INSTANCE=(ComponentElementXMLizerFactoryIF)Loader.getInstanceOfClass(FACTORY_IMPL_CLASS);
    }
    return INSTANCE;
  }
  /**
   *  get instance of a ComponentElementXMLizer
   *
   *@return    The ComponentElementXMLizer instance
   */
  public abstract ComponentElementXMLizerIF createComponentXMLizerElement(ComponentElementIF component);          
  public abstract ComponentElementXMLizerIF createComponentXMLizerElementFromXML(String xmlelement) throws ComponentElementXMLizerException; 
}

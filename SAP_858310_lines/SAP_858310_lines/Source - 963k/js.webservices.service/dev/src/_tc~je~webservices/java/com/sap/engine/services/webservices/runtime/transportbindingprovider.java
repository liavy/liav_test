package com.sap.engine.services.webservices.runtime;

import com.sap.engine.interfaces.webservices.runtime.TransportBinding;
import com.sap.engine.interfaces.webservices.runtime.component.ComponentInstantiationException;

/**
 * Title: TransportBindingProvider
 * Description: This interfaces specifies a method for obtaining server transport binding instances.
 * Copyright: Copyright (c) 2004
 * Company: Sap Labs Sofia
 * 
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public interface TransportBindingProvider {

  public TransportBinding getTransportBinding(String trBindingId) throws ComponentInstantiationException;

}

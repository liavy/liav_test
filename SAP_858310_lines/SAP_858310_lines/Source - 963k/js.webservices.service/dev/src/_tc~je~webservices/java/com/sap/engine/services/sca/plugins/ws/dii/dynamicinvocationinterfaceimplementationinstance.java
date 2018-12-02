package com.sap.engine.services.sca.plugins.ws.dii;

import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.Map;

import com.sap.engine.interfaces.sca.assembly.IServiceArtifact;
import com.sap.engine.interfaces.sca.config.ESBConfigurationFactory;
import com.sap.engine.interfaces.sca.config.binding.WSConfigurationObject;
import com.sap.engine.interfaces.sca.runtime.ImplementationInstance;
import com.sap.engine.interfaces.sca.wire.Message;
import com.sap.engine.interfaces.sca.wire.Operation;
import com.sap.engine.interfaces.sca.wire.OperationContext;
import com.sap.engine.interfaces.sca.wire.Parameter;
import com.sap.engine.interfaces.sca.wire.Result;
import com.sap.engine.services.sca.plugins.ws.config.ConfigurationDestinationInfo;
import com.sap.engine.services.webservices.espbase.wsdas.OperationConfig;
import com.sap.engine.services.webservices.espbase.wsdas.WSDAS;
import com.sap.engine.services.webservices.espbase.wsdas.WSDASFactory;
import commonj.sdo.DataObject;
import commonj.sdo.helper.HelperContext;

/**
 * 
 * @author I044263
 *
 */
public class DynamicInvocationInterfaceImplementationInstance implements ImplementationInstance {

  private DynamicInvocationInterfaceImplementationFactory iFactory = null;
  private WSDASFactory iWsdasFactory = null;
  
  /**
   * Ctor.
   * @param aObject
   * @param aService
   * @param aFactory
   */
  public DynamicInvocationInterfaceImplementationInstance(Object aObject, IServiceArtifact aService, DynamicInvocationInterfaceImplementationFactory aFactory) {
    iFactory 		= aFactory;		
    iWsdasFactory 	= WSDASFactory.newInstance();
  }
  
  public void release() {
    
  }

  public Result accept(Message aMessage) {
    Throwable appError = null;
    Object result = null;
    
    try {
      // Get HelperContext in case of provider helper context.
      HelperContext hc = aMessage.getHelperContext();
      if (hc == null)
	throw new NullPointerException("HelperContext can not be null!");

      // Operation to call.
      Operation operation = aMessage.getOperation();
      
      // Create destination.
      ConfigurationDestinationInfo destInfo = iFactory.getDestinationInfo();
      ESBConfigurationFactory esbFactory = ESBConfigurationFactory.newInstance();
      WSConfigurationObject destination = (WSConfigurationObject) esbFactory.getConfiguration(	destInfo.iAppName, 
	  											destInfo.iCompositeAddress, 
	  											destInfo.iComponentAddress.getLocalPart(),
	  											destInfo.iReferenceAddress.getLocalPart(),
	  											destInfo.iBindingType);
      
      // Create WSDAS.
      WSDAS wsdas = iWsdasFactory.createWSDAS(destination.getApplicationName(), destination.getServiceReferenceId(), hc);
        
      // Set parameters and invoke operation.
      OperationConfig opCfg = wsdas.getOperationCfg(operation.getName());
      
      if (operation.getInputWrapperProperty() != null) {
	if (!(aMessage.getValues().length == 1))
	  throw new IllegalArgumentException("Incorrect number of data parameters.");
	
	Object sdoData = aMessage.getValues()[0].getValue();
	if (!(sdoData instanceof DataObject))
	  throw new IllegalArgumentException("Input data not in SDO format");
	
	// Set input parameter data.
	opCfg.setInputParamValue(operation.getInputWrapperProperty().getLocalPart(), sdoData);
      } else {
	for (int i = 0; i < operation.getParameters().size(); ++ i) {
	  Parameter p = operation.getParameters().get(i);
	  if (p.getPropertyUri() != null) {
	    opCfg.setInputParamValue(p.getPropertyUri().getLocalPart(), aMessage.getValues()[i].getValue());
	  } else {
	    opCfg.setInputParamValue(p.getName(), aMessage.getValues()[i].getValue());
	  }
	}
      }

      // Invoke WS operation.
      if (aMessage.isExist(OperationContext.GALAXY_MAP))
	wsdas.invokeOperation(opCfg, (Map) aMessage.getValue(OperationContext.GALAXY_MAP));
      else
	wsdas.invokeOperation(opCfg);
      
      // Get result from invocation.
      if (operation.getOutputWrapperProperty() != null) {
	DataObject wrapper = (DataObject)opCfg.getOutputParamValue(operation.getOutputWrapperProperty().getLocalPart());
	result = wrapper;
      } else {
	for (int i = 0; i < operation.getResults().size(); ++ i) {
	  Parameter p = operation.getResults().get(i);
	  if (p.getPropertyUri() != null) {
	    result = opCfg.getOutputParamValue(p.getPropertyUri().getLocalPart());
	  } else {
	    result = opCfg.getOutputParamValue(p.getName());
	  }
	}
      }
    } catch (InvocationTargetException ite) {
      appError = ite;
    } catch (RemoteException re) {
      appError = re;
    } catch (Exception e) {
      appError = e;
    }

    // Pass result to MB runtime.
    return new Result(result, appError, appError == null);
  }

}

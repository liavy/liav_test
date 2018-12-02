package com.sap.engine.services.sca.plugins.ws;

import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import com.sap.engine.interfaces.sca.assembly.IServiceArtifact;
import com.sap.engine.interfaces.sca.assembly.ITarget;
import com.sap.engine.interfaces.sca.config.ESBConfigurationFactory;
import com.sap.engine.interfaces.sca.config.binding.WSConfigurationObject;
import com.sap.engine.interfaces.sca.runtime.ImplementationInstance;
import com.sap.engine.interfaces.sca.wire.Message;
import com.sap.engine.interfaces.sca.wire.Operation;
import com.sap.engine.interfaces.sca.wire.OperationContext;
import com.sap.engine.interfaces.sca.wire.Parameter;
import com.sap.engine.interfaces.sca.wire.Result;
import com.sap.engine.interfaces.sca.wire.Value;

import com.sap.engine.services.sca.plugins.ws.config.ConfigurationDestinationInfo;
import com.sap.engine.services.sca.plugins.ws.tools.wsdas.WsdasFactoryWrapper;
import com.sap.engine.services.sca.plugins.ws.tools.wsdas.WsdasFactoryWrapper.WsdasFactoryMode;
import com.sap.engine.services.webservices.espbase.wsdas.OperationConfig;
import com.sap.engine.services.webservices.espbase.wsdas.WSDAS;

import commonj.sdo.DataObject;
import commonj.sdo.Property;
import commonj.sdo.helper.HelperContext;

public class WebServiceImplementationInstance implements ImplementationInstance {
  
  private WebServiceImplementationFactory 	iFactory	= null;
	
  /**
   * Ctor.
   * @param aObject
   * @param aService
   * @param aFactory
   */
  public WebServiceImplementationInstance(Object aObject, IServiceArtifact aService, WebServiceImplementationFactory aFactory) {    
    iFactory = aFactory;
  }

  public boolean isCurrent() {
    return false;
  }

  public void release() {
    //
  }

  public Result accept(Message aMessage) {
    Throwable appError = null;
    Map<String, Object> result = null;
    Object ret = null;
    
    try {
      // Get HelperContext if passed via message or get from assembly.
      HelperContext hc = aMessage.getHelperContext();
      if (hc == null)
	hc = iFactory.getHelperContext();
      if (hc == null)
	throw new NullPointerException("Could not get HelperContext neither from message nor from assembly.");

      // Create WSDAS object.
      WSDAS wsdas = this.initWsdas(hc);
      
      // Operation to call.
      Operation operation = aMessage.getOperation();
        
      // Set parameters and invoke operation.
      OperationConfig opCfg = wsdas.getOperationCfg(operation.getName());
      
      QName op = operation.getInputWrapperProperty();
      if (op != null) {
	Value[] values = aMessage.getValues();
	
	if (operation.isInputWrapped())
	  // Case input is wrapped. Input data in SDO format
	  opCfg.setInputParamValue(operation.getInputWrapperProperty().getLocalPart(), values[0].getValue());
	else {
	  // Case input is not wrapped. Input data not in SDO format.
    	  Property wrapperProperty = hc.getTypeHelper().getOpenContentProperty(op.getNamespaceURI(), op.getLocalPart());
    	  if (wrapperProperty == null)
    	    throw new NullPointerException("Could not get wrapper property: " + operation.getInputWrapperProperty());
 
    	  DataObject wrapper = hc.getDataFactory().create(wrapperProperty.getType());
    	  List<Parameter> params = operation.getParameters();
    	  for (int i = 0; i < params.size(); ++ i)
    	    wrapper.set(params.get(i).getName(), aMessage.getValues()[i].getValue());
  
    	  opCfg.setInputParamValue(wrapperProperty.getName(), wrapper);	  
	}
      } else {
	List<Parameter> params = operation.getParameters();
	for (int i = 0; i < params.size(); ++ i) {
	  Parameter p = params.get(i);
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
	// Document literal style.
	String elementName = aMessage.getValue(OperationContext.RETURN_ELEMENT_NAME) == null ? operation.getOutputWrapperProperty().getLocalPart() : (String) aMessage.getValue(OperationContext.RETURN_ELEMENT_NAME);
	DataObject wrapper = (DataObject) opCfg.getOutputParamValue(elementName);	
	if (operation.isInputWrapped()) {
	  result = new Hashtable<String, Object>(5);
	  result.put(elementName, wrapper);	  
	  ret = result;
	}	  
	else
	  ret = wrapper.get(operation.getResults().get(0).getName());
      } else {
	// RPC literal style.
	String userElementName = (String) aMessage.getValue(OperationContext.RETURN_ELEMENT_NAME); 
	int results = operation.getResults().size();
	if (results > 0 && operation.isInputWrapped())
	  result = new Hashtable<String, Object>(5);
	
	for (int i = 0; i < results; ++ i) {
	  Parameter p = operation.getResults().get(i);
	  if (p.getPropertyUri() != null) {
	    String elementName = userElementName == null ? p.getPropertyUri().getLocalPart() : userElementName;
	    
	    if (operation.isInputWrapped()) {
	      result.put(elementName, opCfg.getOutputParamValue(elementName));
	      ret = result;
	    } else
	      ret = opCfg.getOutputParamValue(elementName);
	  } else {
	    String elementName = userElementName == null ? p.getName() : userElementName;
	    
	    if (operation.isInputWrapped()) {
	      result.put(elementName, opCfg.getOutputParamValue(elementName));
	      ret = result;
	    } else
	      ret = opCfg.getOutputParamValue(elementName);
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
    return new Result(ret, appError, appError == null);
  }

  public Object getConversationID() {
    throw new UnsupportedOperationException("Not supported method.");
  }

  public ITarget getTarget() {
    throw new UnsupportedOperationException("Not supported method.");
  }

  public void setConversationID(Object aId) {
    throw new UnsupportedOperationException("Not supported method.");
  }
  
  private WSDAS initWsdas(HelperContext aContext) throws Exception {
    ConfigurationDestinationInfo destInfo = iFactory.getDestinationInfo();
    ESBConfigurationFactory esbFactory = ESBConfigurationFactory.newInstance();
    WSConfigurationObject destination = (WSConfigurationObject) esbFactory.getConfiguration(	destInfo.iAppName, 
	  											destInfo.iCompositeAddress, 
	  											destInfo.iComponentAddress.getLocalPart(),
	  											destInfo.iReferenceAddress.getLocalPart(),
	  											destInfo.iBindingType);

    return WsdasFactoryWrapper.getInstance(WsdasFactoryMode.CacheEnabled).createWsdas(destination, aContext);
  }
}

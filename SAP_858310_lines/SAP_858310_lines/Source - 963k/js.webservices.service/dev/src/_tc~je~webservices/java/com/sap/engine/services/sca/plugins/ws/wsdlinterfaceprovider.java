package com.sap.engine.services.sca.plugins.ws;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;

import org.osoa.sca.sdo.Interface;
import org.osoa.sca.sdo.WsdlPortType;

import com.sap.engine.interfaces.sca.SCAResolver;
import com.sap.engine.interfaces.sca.wire.InterfaceMetadata;
import com.sap.engine.interfaces.sca.wire.NamedInterfaceMetadata;
import com.sap.engine.interfaces.sca.wire.Operation;
import com.sap.sdo.api.impl.SapHelperProvider;
import com.sap.sdo.api.types.SapProperty;
import com.sap.sdo.api.types.SapType;
import com.sap.sdo.api.util.URINamePair;
import com.sap.engine.services.sca.plugins.ws.sdo.das.DeploytimeWsdlProvider;
import commonj.sdo.DataObject;
import commonj.sdo.helper.HelperContext;
import commonj.sdo.impl.HelperProvider;

public class WsdlInterfaceProvider {
  
  public InterfaceMetadata generateMetadata(Interface aInterface, SCAResolver aResolver, HelperContext aHelperCtx) {
      return this.generateWsdlPortTypeMetadata(aInterface, aResolver, aHelperCtx);
  }
  
  private InterfaceMetadata generateWsdlPortTypeMetadata(Interface aInterface, SCAResolver aResolver, HelperContext aHelperCtx) {
    WsdlPortType wsdlInterface = (WsdlPortType) aInterface;

    URINamePair unp = parsePortType(wsdlInterface.getInterface());
    String scdlLocation = (String)((DataObject) wsdlInterface).get(WebServicePluginConstants.COMPOSITE_LOCATION_ATTR_NAME);
    if (scdlLocation == null)
      // Return empty interface metadata in case of location attribute is missing.
      return (NamedInterfaceMetadata) HelperProvider.getDefaultContext().getDataFactory().create(NamedInterfaceMetadata.class);
    
    DeploytimeWsdlProvider wsdlProvider = new DeploytimeWsdlProvider(unp.getURI(), unp.getName(), scdlLocation, aResolver, aHelperCtx);
    InterfaceMetadata ret = wsdlProvider.getInterfaceMetaData();
    HelperContext containerContext = ((SapType)((DataObject) ret).getType()).getHelperContext();
		
    if (wsdlInterface.getCallbackInterface() != null) {
      unp = parsePortType(wsdlInterface.getCallbackInterface());
      wsdlProvider = new DeploytimeWsdlProvider(unp.getURI(), unp.getName(), scdlLocation, aResolver, aHelperCtx);				
      InterfaceMetadata cb = wsdlProvider.getInterfaceMetaData();
      for (Operation o: cb.getOperations()) {
	ret.getCallbackOperations().add((Operation) containerContext.getCopyHelper().copy((DataObject) o));
      }
    }
    
    HelperContext tmp = SapHelperProvider.getNewContext();
		
    // tmp.getCopyHelper().copy(wsdlProvider.getTypes().get(0))
    if(wsdlProvider.getTypes().size() > 0) {
      tmp = ((SapType) wsdlProvider.getTypes().get(0)).getHelperContext();
    } else {
      if(wsdlProvider.getProperties().size() > 0) {
	tmp = ((SapProperty) wsdlProvider.getProperties().get(0)).getHelperContext();
      }
    }
    
    //tmp.getTypeHelper().define(wsdlProvider.getTypes());
    //for (Property p: wsdlProvider.getProperties()) {
    //tmp.getTypeHelper().defineOpenContentProperty(p.getContainingType().getURI(), (DataObject)p);
    //}
    
    StringWriter w = new StringWriter();
    SapHelperProvider.serializeContexts(Collections.singletonList(tmp), w);
    ((DataObject) ret).set("typeStr", w.toString());
    ret.setOriginal((Interface) containerContext.getCopyHelper().copy((DataObject) aInterface));
    
    return ret;    
  }
  
  private URINamePair parsePortType(String aUri) {
    if (aUri == null || aUri.length() == 0) {
      return null;
    }
    
    URINamePair unp = URINamePair.fromStandardSdoFormat(aUri);
		
    if (!unp.getName().startsWith(WebServicePluginConstants.WSDL_INTERFACE_STR)) {
      throw new RuntimeException("Cannot parse interface.wsdl: " + aUri);
    }
    
    return new URINamePair(unp.getURI(), unp.getName().substring(WebServicePluginConstants.WSDL_INTERFACE_STR.length(), unp.getName().length() - 1));
  }

  public void enrichContext(InterfaceMetadata aMetadata, HelperContext aHelperCtx) {
    if (!(aMetadata.getOriginal() instanceof WsdlPortType)) 
      return; 
    
    DataObject obj = (DataObject) aMetadata;
    if (obj.isSet("typeStr")) {
      String typeStr = obj.getString("typeStr");
      this.deserializeInto(new StringReader(typeStr), aHelperCtx);
    }    
  }
  
  /**
   * @deprecated
   */
  private void deserializeInto(Reader aReader, HelperContext aHelperCtx) {
    try {
      Method m = SapHelperProvider.class.getMethod("deserializeContextInto", new Class[]{Reader.class, HelperContext.class});
      m.invoke(null, new Object[]{aReader, aHelperCtx});
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    } catch (NoSuchMethodException e) {
      throw new RuntimeException("This version of interface.wsdl needs the 'deserializeContextInto' method.  Please update your version of SDO");
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    }	
  }
}

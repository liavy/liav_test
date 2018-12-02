package com.sap.engine.services.sca.plugins.ws.sdo.das;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import javax.xml.namespace.QName;

import org.w3.www.ns.wsdl.DescriptionType;
import org.w3.www.ns.wsdl.ImportType;
import org.w3.www.ns.wsdl.InterfaceOperationType;
import org.w3.www.ns.wsdl.InterfaceType;
import org.xmlsoap.schemas.wsdl.TBinding;
import org.xmlsoap.schemas.wsdl.TDefinitions;
import org.xmlsoap.schemas.wsdl.TPortType;

import com.sap.engine.interfaces.sca.SCAResolver;
import com.sap.engine.interfaces.sca.assembly.AssemblyException;
import com.sap.engine.interfaces.sca.wire.NamedInterfaceMetadata;
import com.sap.engine.interfaces.sca.wire.Operation;
import com.sap.engine.interfaces.sca.wire.Parameter;

import commonj.sdo.DataObject;
import commonj.sdo.Property;
import commonj.sdo.Type;
import commonj.sdo.helper.DataFactory;
import commonj.sdo.helper.HelperContext;
import commonj.sdo.impl.HelperProvider;

public class DeploytimeWsdlProvider extends AbstractWsdlProvider {
  
  private String iLocString = null;
  private String iNamespaceFromScdl = null;
  private String iPortTypeStr = null;
  private String iScdlLocation = null;
  private DataObject iWsdl = null;
  
  /**
   * Ctor.
   * @param wsdlUrlString
   * @param portName
   * @param resolver
   * @param ctx
   */
  public DeploytimeWsdlProvider(String aWsdlUrl, String aPortName, String aScdlLocation, SCAResolver aResolver, HelperContext aContext) {
    super(aResolver, aContext);
    
    iNamespaceFromScdl = aWsdlUrl;
    iPortTypeStr = aPortName;
    iScdlLocation = aScdlLocation;
  }  
  
  public DataObject getWsdlDataObject() {
    if (iWsdl == null) {
      initializeTypes();
      URL location = iResolver.getWsdlURL(iScdlLocation);
      if (location == null) {
	throw new RuntimeException(new AssemblyException("WSDL " + iNamespaceFromScdl + '#' + iPortTypeStr + " not found."));
      }
      
      iLocString = location.toString();
      
      InputStream is;
      try {
	is = location.openStream();
	iWsdl = iWsdlHelper.readWsdl(iLocString, is);
      } catch (IOException e) {
	throw new RuntimeException("Could not load wsdl for " + iNamespaceFromScdl, e);
      }
      iWsdlHelper.getDefinedTypes(iLocString, iResolver);
    }
			
    return iWsdl;
  }
  
  public String getServiceLocation() {
    throw new RuntimeException("Could not retrieve service location");
  }
  
  public List<Type> getTypes() {
    return iWsdlHelper.getDefinedTypes(iLocString, iResolver);
  }
  
  public List<Property> getProperties() {
    return iWsdlHelper.getDefinedProps(iLocString, iResolver);
  }
  
  @Override
  protected TBinding getServiceBinding(TDefinitions aDefinitions) {
    QName unp = null;
    if (!iNamespaceFromDefs.equals(iNamespaceFromScdl))
      unp = new QName(iNamespaceFromScdl, iPortTypeStr);
    else 
      unp = new QName(iNamespaceFromDefs, iPortTypeStr);
    
    TBinding ret = null;
    for (TBinding binding: aDefinitions.getBinding()) {
      if (unp.equals(binding.getTBindingType())) {
	ret = binding;
	break;
      }
    }
    
    if (ret != null) {
      return ret;
    }
    
    ret = (TBinding)DataFactory.INSTANCE.create(TBinding.class);
    ret.setTBindingType(unp);
    
    return ret;
  }
  
  public TPortType getPortType() {
    if (iServiceBinding == null) {
      loadService();
    }
    
    return iPortType;
  }
  
  public QName getPortName() {
    return null;
  }
  
  public QName getServiceName() {
    return null;
  }
  
  public TDefinitions getWsdl() {
    return (TDefinitions) getWsdlDataObject();
  }
  
  @Override
  public NamedInterfaceMetadata getInterfaceMetaData() {
    if (getWsdlDataObject() instanceof TDefinitions) {
      return super.getInterfaceMetaData();
    }
    
    NamedInterfaceMetadata ret = (NamedInterfaceMetadata)getContainerDataFactory().create(NamedInterfaceMetadata.class);

    for (InterfaceOperationType op: getInterface().getOperation()) {
      Operation m = (Operation)getContainerDataFactory().create(Operation.class);
      ret.getOperations().add(m);
      m.setName(op.getName());
      getStyle(op).createMessages(op, m);
    }
    
    return ret;
  }
  
  private Wsdl2Style getStyle(InterfaceOperationType aOperationType) {
    if ("http://www.w3.org/ns/wsdl/style/rpc".equals(aOperationType.getStyle())) {
      return new RpcStyle();
    }
    
    return null;
  }
  
  private static interface Wsdl2Style {
    public void createMessages(InterfaceOperationType op, Operation m);
  }
  
  private static class RpcStyle implements Wsdl2Style {
    public void createMessages(InterfaceOperationType aOperationType, Operation aOperation) {
      if (aOperationType.getInput().size()>0) {
	QName elementString = aOperationType.getInput().get(0).getElement();
	aOperation.setInputMessageUri(elementString);
	Type wrapperType = getWrapperType(elementString);
	for (int i=0; i<wrapperType.getProperties().size(); i++) {
	  Property prop = (Property)wrapperType.getProperties().get(i);
	  Parameter p = (Parameter)((DataObject) aOperation).createDataObject("parameters");
	  p.setName(prop.getName());
	  if (prop.isOpenContent()) {
	    p.setPropertyUri(new QName(prop.getContainingType().getURI(),prop.getName()));
	  }
	  p.setTypeUri(new QName(prop.getType().getURI(),prop.getType().getName()));
	  // TODO: Multivalues
	  //p.setMany(prop.isMany());
	}
      }
      
      if ("http://www.w3.org/ns/wsdl/in-out".equals(aOperationType.getPattern())) {
	QName elementString = aOperationType.getOutput().get(0).getElement();
	aOperation.setOutputMessageUri(elementString);
	Type wrapperType = getWrapperType(elementString);
	
	for (int i=0; i<wrapperType.getProperties().size(); i++) {
	  Property prop = (Property)wrapperType.getProperties().get(i);
	  Parameter p = (Parameter)((DataObject) aOperation).createDataObject("results");
	  p.setName(prop.getName());
	  if (prop.isOpenContent()) {
	    p.setPropertyUri(new QName(prop.getContainingType().getURI(),prop.getName()));
	  }
	  
	  p.setTypeUri(new QName(prop.getType().getURI(),prop.getType().getName()));
	  // TODO: Multivalues
	  //p.setMany(prop.isMany());
	}					
      }
    }
    
    private Type getWrapperType(QName aQname) {
      Property p = HelperProvider.getDefaultContext().getTypeHelper().getOpenContentProperty(aQname.getNamespaceURI(), aQname.getLocalPart());
      if (p == null) {
	throw new RuntimeException("element " + aQname + " not found");
      }
      
      return p.getType();
    }
  }

  private InterfaceType getInterface() {
    DescriptionType wsdl = (DescriptionType)getWsdlDataObject();
    return recurseForInterface(wsdl);
  }
  
  private InterfaceType recurseForInterface(DescriptionType aWsdl) {
    for (InterfaceType i: aWsdl.getInterface()) {
      if (i.getName().equals(iPortTypeStr)) {
	return i;
      }
    }
    
    for (ImportType imp: ((DescriptionType) aWsdl).getImport()) {
      DescriptionType wsdl2 = (DescriptionType) iWsdlHelper.getWsdl(imp.getLocation(), iResolver);
      InterfaceType i = recurseForInterface(wsdl2);
      if (i != null) {
	return i;
      }
    }
    
    return null;
  }
  
  protected String getWsdlLocation() {
    return iLocString;
  }
}

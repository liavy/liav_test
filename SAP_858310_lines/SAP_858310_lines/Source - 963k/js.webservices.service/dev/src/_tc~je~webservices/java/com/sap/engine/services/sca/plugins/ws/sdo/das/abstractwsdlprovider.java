package com.sap.engine.services.sca.plugins.ws.sdo.das;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.xmlsoap.schemas.wsdl.TBinding;
import org.xmlsoap.schemas.wsdl.TBindingOperation;
import org.xmlsoap.schemas.wsdl.TBindingOperationFault;
import org.xmlsoap.schemas.wsdl.TBindingOperationMessage;
import org.xmlsoap.schemas.wsdl.TDefinitions;
import org.xmlsoap.schemas.wsdl.TFault;
import org.xmlsoap.schemas.wsdl.TImport;
import org.xmlsoap.schemas.wsdl.TMessage;
import org.xmlsoap.schemas.wsdl.TOperation;
import org.xmlsoap.schemas.wsdl.TPart;
import org.xmlsoap.schemas.wsdl.TPortType;
import org.xmlsoap.schemas.wsdl.soap.TAddress;
import org.xmlsoap.schemas.wsdl.soap.TBody;

import com.sap.engine.interfaces.sca.SCAResolver;
import com.sap.engine.interfaces.sca.wire.NamedInterfaceMetadata;
import com.sap.engine.interfaces.sca.wire.Operation;
import com.sap.engine.lib.xml.parser.URLLoader;
import com.sap.sdo.api.helper.MappingSchemaResolver;
import com.sap.sdo.api.helper.SapXsdHelper;
import com.sap.sdo.api.types.SapProperty;
import com.sap.sdo.api.util.URINamePair;

import commonj.sdo.DataObject;
import commonj.sdo.Property;
import commonj.sdo.Type;
import commonj.sdo.helper.DataFactory;
import commonj.sdo.helper.HelperContext;
import commonj.sdo.helper.TypeHelper;
import commonj.sdo.helper.XMLHelper;
import commonj.sdo.helper.XSDHelper;
import commonj.sdo.impl.HelperProvider;

public abstract class AbstractWsdlProvider implements WsdlProvider {
  
  // TODO:  To increase efficiency, these could maybe be associated with an assembly...  
  protected String iNamespaceFromDefs = null;
  protected String iPackageName = null;
  protected TBinding iServiceBinding = null;
  protected TPortType iPortType = null;  
  protected Map<String, TOperation> iNameToOperation = null;
  protected Map<String, TBindingOperation> iNameToBindingOperation = null;
  protected final SCAResolver iResolver;
  protected final WsdlHelper iWsdlHelper;
  
  private boolean iLoaded;	
  private HelperContext iContext = null;  
  private Map<QName, TMessage> iNameToMessage = null;
  private Map<String, WebServiceStyle> iInputStyleMap = null;  
  private Map<String, WebServiceStyle> iOutputStyleMap = null;
  private Map<String, Map<String, WebServiceStyle>> iFaultNameMapMap = null;
    
  public enum Direction {
    INPUT, 
    OUTPUT, 
    FAULT
  }
  
  /**
   * Ctor.
   * @param aResolver
   * @param aHelperCtx
   */
  public AbstractWsdlProvider(SCAResolver aResolver, HelperContext aHelperCtx) {
    super();

    iResolver = aResolver;
    iContext = aHelperCtx;
    
    iLoaded = false;
    iWsdlHelper = new WsdlHelper(aHelperCtx);
    iNameToOperation = new HashMap<String, TOperation>(8);
    iNameToBindingOperation = new HashMap<String, TBindingOperation>(8);
    iNameToMessage = new HashMap<QName, TMessage>(8);
    iInputStyleMap = new HashMap<String, WebServiceStyle>(8);
    iOutputStyleMap = new HashMap<String,WebServiceStyle>(8);
    iFaultNameMapMap = new HashMap<String, Map<String,WebServiceStyle>>(8);
  } 
  
  protected abstract String getWsdlLocation();  
  protected abstract TBinding getServiceBinding(TDefinitions def);
  
  public XMLHelper getXmlHelper() {
    return iContext.getXMLHelper();
  }
  
  public HelperContext getHelperContext() {
    return iContext;
  }
  
  protected void initializeTypes() {
    ((MappingSchemaResolver)((SapXsdHelper) iContext.getXSDHelper()).peekResolver()).defineSchemaLocationMapping("http://schemas.xmlsoap.org/wsdl/", "org/xmlsoap/schemas/wsdl/wsdl.xsd");
    
    TypeHelper helper = iContext.getTypeHelper();
    helper.getType(org.xmlsoap.schemas.wsdl.soap.TBinding.class);
    helper.getType(org.xmlsoap.schemas.wsdl.soap.TOperation.class);
    helper.getType(org.xmlsoap.schemas.wsdl.soap.TBody.class);
    helper.getType(org.xmlsoap.schemas.wsdl.soap.TBinding.class);
    helper.getType(TAddress.class);
  }

  public void loadService() {
    TDefinitions definitions = getWsdl();

    iNamespaceFromDefs = definitions.getTargetNamespace();
    iServiceBinding = getServiceBinding(definitions);
    
    for (TBindingOperation op: iServiceBinding.getOperation())
      iNameToBindingOperation.put(op.getName(), op);
   
    
    QName unp = iServiceBinding.getTBindingType();
    try {
      this.recurseForPortType(definitions, unp);
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
    
    if (iPortType == null)
      throw new RuntimeException("PortType " + unp + " not found in WSDL");
    
    try {
      this.recurseForMessageNames(definitions);
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
    
    
    for (TOperation op: iPortType.getOperation())
      iNameToOperation.put(op.getName(), op);
    
    iLoaded = true;
  }
  
  protected void recurseForMessageNames(TDefinitions definitions) throws IOException {
    for (TMessage message: definitions.getMessage()) {
      iNameToMessage.put(new QName(definitions.getTargetNamespace(), message.getName()), message);
    }
    
    for (TImport imp: definitions.getImport()) {
      TDefinitions def = (TDefinitions) iWsdlHelper.getWsdl(this.calculateUriPath(imp.getLocation(), this.getWsdlLocation()), iResolver);
      this.recurseForMessageNames(def);
    }
  }

  protected void recurseForPortType(TDefinitions definitions, QName unp) throws IOException {
    if (definitions.getTargetNamespace().equals(unp.getNamespaceURI())) {
      for (TPortType portType: definitions.getPortType()) {
	if (portType.getName().equals(unp.getLocalPart())) {
	  iPortType = portType;
	  break;
	}
      }
    }
    
    if (iPortType == null) {
      for (TImport imp: definitions.getImport()) {
	TDefinitions def = (TDefinitions) iWsdlHelper.getWsdl(this.calculateUriPath(imp.getLocation(), this.getWsdlLocation()), iResolver);
	this.recurseForPortType(def, unp);
      }
    }
  }
  
  protected boolean loaded() {
    return iLoaded;
  }
  
  public TypeHelper getTypeHelper() {
	return iContext.getTypeHelper();
  }
  
  public DataFactory getDataFactory() {
    return iContext.getDataFactory();
  }
  
  public XSDHelper getXsdHelper() {
    return iContext.getXSDHelper();
  }
  
  private TOperation findOperation(String name) {
    if (!loaded()) {
      loadService();
    }
    
    TOperation op = iNameToOperation.get(name);
    if (op == null) {
      throw new RuntimeException("Could not find operation for method name: "+name);
    }
    return op;
  }

  private TBindingOperation findBindingOperation(String name) {
    if (!loaded()) {
      loadService();
    }
    
    return iNameToBindingOperation.get(name);
  }

  public String getSoapAction(String name) {
    org.xmlsoap.schemas.wsdl.soap.TOperation soapOp = getSoapOperation(name);
    if (soapOp != null) {
      return soapOp.getSoapAction();
    }
    
    return getWsdl().getTargetNamespace() + name;
  }

  private org.xmlsoap.schemas.wsdl.soap.TOperation getSoapOperation(String aName) {
    org.xmlsoap.schemas.wsdl.soap.TOperation soapOp = null;
    
    DataObject op = (DataObject) findBindingOperation(aName);
    if (op == null) {
      return null;
    }
    
    Type t = iContext.getTypeHelper().getType(org.xmlsoap.schemas.wsdl.soap.TOperation.class);
    Property soapOperationProperty = iContext.getTypeHelper().getOpenContentProperty(t.getURI(), "operation");    
    List l = op.getList(soapOperationProperty);
    if (l.size() > 0)
      soapOp =  (org.xmlsoap.schemas.wsdl.soap.TOperation) l.get(0);    
    
    return soapOp;
  }

  private org.xmlsoap.schemas.wsdl.soap.TBinding getSoapBinding() {
    DataObject op = (DataObject) iServiceBinding;
    Type t = iContext.getTypeHelper().getType(org.xmlsoap.schemas.wsdl.soap.TBinding.class);
    Property soapBindingProperty = iContext.getTypeHelper().getOpenContentProperty(t.getURI(), "binding");
    List<org.xmlsoap.schemas.wsdl.soap.TBinding> soapBindings = op.getList(soapBindingProperty);
    if (soapBindings.size() < 1) {
      return null;
    }
    
    return soapBindings.get(0);
  }

  private TBody getSoapBody(String name, Direction dir, String faultName) {    
    TBindingOperation op = findBindingOperation(name);
    if (op == null) {
      return null;
    }
        
    TBindingOperationMessage message;
    switch (dir) {
    case INPUT:
      message = op.getInput();
      break;
    case OUTPUT:
      message = op.getOutput();
      break;
    case FAULT:
      for (TBindingOperationFault fault: op.getFault()) {
	if (faultName.equals(fault.getName())) {
	  Type t = iContext.getTypeHelper().getType(org.xmlsoap.schemas.wsdl.soap.TFault.class);
	  iContext.getTypeHelper().getType(org.xmlsoap.schemas.wsdl.soap.TFaultRes.class);
	  Property soapBodyProperty = iContext.getTypeHelper().getOpenContentProperty(t.getURI(), "fault");
	  return (TBody)((DataObject)fault).getList(soapBodyProperty).get(0);	
	}
      }
    default:
      return null;
    }
    
    if (message == null)
      return null;
    
    Type t = iContext.getTypeHelper().getType(TBody.class);
    Property soapBodyProperty = iContext.getTypeHelper().getOpenContentProperty(t.getURI(), "body");
    TBody soapBody = (TBody)((DataObject)message).getList(soapBodyProperty).get(0);
    
    return soapBody;
  }

  private String getOperationStyle(String name) {
    org.xmlsoap.schemas.wsdl.soap.TOperation op = getSoapOperation(name);    
    if (op != null && ((DataObject) op).isSet("style")) {
	return op.getStyle();
    }
    
    org.xmlsoap.schemas.wsdl.soap.TBinding binding = this.getSoapBinding();
    if (binding != null) {
      return binding.getStyle();
    }
    
    return "document";
  }

  private String getUse(String name, Direction dir, String faultName) {
    // TODO:
    if (dir == Direction.FAULT && faultName==null) {
      return "literal";
    }
    
    TBody op = this.getSoapBody(name, dir, faultName);
    if (op != null) {
      return op.getUse();
    }
    
    return "literal";
  }

  private String getBodyNamespace(String name, Direction dir, String faultName) {
    TBody op = getSoapBody(name, dir, faultName);
    if (op != null && op.getNamespace()!=null) {
      return op.getNamespace();
    
    }
    return "noNamespace";
  }

  public List<MessageDescr> getPropertiesOrTypes(String name, Direction dir, String faultName) {
    TOperation op = findOperation(name);
    
    TMessage message;
    switch (dir) {
    	case INPUT:
    	  message = iNameToMessage.get(op.getInput().getMessage());
    	  break;
    	case OUTPUT:
    	  message = iNameToMessage.get(op.getOutput().getMessage());
    	  break;
    	case FAULT:
    	default:
    	  List<TFault> faultMessages = op.getFault();

    	  message = null;
    
    	  for (TFault f: faultMessages) {
    	    if (faultName.equals(f.getName())) {
    	      message = iNameToMessage.get(f.getMessage());
    	    }
    	  }
 
    	  if (message == null) {
    	    throw new RuntimeException("Operation " + name + " does not have fault " + faultName);
    	  }
    }
		
    List<MessageDescr> ret = new ArrayList(message.getPart().size());
    for (TPart part: message.getPart()) {
      MessageDescr d = new MessageDescr();
      d.name = part.getName();
      ret.add(d);
      
      if (((DataObject)part).isSet("element")) {
	QName unp = part.getElement();
	Property p = iContext.getTypeHelper().getOpenContentProperty(unp.getNamespaceURI(), unp.getLocalPart());
	if (p == null) {
	  throw new RuntimeException("GlobalProperty not defined: "+part.getElement());
	}
	
	d.typeOrProperty = p;
      } else {
	URINamePair unp = new URINamePair(part.getTPartType());
	if (URINamePair.SCHEMA_URI.equals(unp.getURI())) {
	  unp = ((SapXsdHelper)SapXsdHelper.INSTANCE).getSdoName(unp);
	}
	Type t = iContext.getTypeHelper().getType(unp.getURI(), unp.getName());
	if (t == null) {
	  throw new RuntimeException("Type not defined: "+part.getTPartType());
	}
	
	d.typeOrProperty = t;
      }
    }
    
    return ret;
  }

  public synchronized WebServiceStyle getStyle(String name, Direction dir, String faultName) {
    Map<String,WebServiceStyle> map;
    
    switch (dir) {
    	case INPUT:
    	  map = iInputStyleMap;
    	  break;
    	case OUTPUT:
    	  map = iOutputStyleMap;
    	  break;
    	default:
    	  if (faultName == null) {
    	    faultName = "";
    	  }
    	
    	  map = iFaultNameMapMap.get(faultName);
    	  if (map == null) {
    	    map = new HashMap<String,WebServiceStyle>();
    	    iFaultNameMapMap.put(faultName, map);
    	  }
    }
    
    WebServiceStyle ret = map.get(name);
    if (ret == null) {
      ret = this.computeStyle(name, dir, faultName);
      map.put(name, ret);
    }
    
    return ret;
  }

  private WebServiceStyle computeStyle(String aName, Direction aDir, String aFaultName) {
    String opStyle = this.getOperationStyle(aName);
    String encoding = this.getUse(aName, aDir, aFaultName);
    if ("document".equals(opStyle)) {
      if ("encoded".equals(this.getUse(aName, aDir, aFaultName))) {
	throw new RuntimeException("style document/encoded not supported");
      }
      
      if (this.isWrappable(this.findOperation(aName))) {
	return new WrappedDocumentLiteralStyle(this);
      } else {
	return new DocumentLiteralStyle(this);
      }
    }
    
    if ("rpc".equals(opStyle)) {
      if ("literal".equals(encoding)) {
	return new RpcLiteralStyle(this, this.getBodyNamespace(aName, aDir, aFaultName), aName, aDir, aFaultName);
      } else {
	throw new RuntimeException("style rpc/encoded not supported");
      }
    }
    
    return null;
  }
  
  private boolean isWrappableMethod(TMessage aMessage, String aName) {
    if (aMessage == null) {
      return false;
    }
    
    if (aMessage.getPart().size()!=1) {
      return false;
    }
    
    QName element = aMessage.getPart().get(0).getElement();
    if (element == null || element.getNamespaceURI() == null || element.getLocalPart().length() == 0) {
      return false;
    }
    
    Property gp = iContext.getTypeHelper().getOpenContentProperty(element.getNamespaceURI(), element.getLocalPart());
    if (gp.getType().isDataType()) {
      return false;
    }
    
    for (Property p: (List<Property>)gp.getType().getProperties()) {
      if (!((SapProperty)p).isXmlElement()) {
	return false;
      }
    }
    
    return true;
  }
  
  private boolean isWrappable(TOperation aOperation) {
    if (aOperation == null || aOperation.getInput()==null || aOperation.getOutput() == null) {
      return false;
    }
    
    TMessage message = iNameToMessage.get(aOperation.getInput().getMessage());
    if (!isWrappableMethod(message, aOperation.getName())) {
      return false;
    }
    
    message = iNameToMessage.get(aOperation.getOutput().getMessage());
    if (!isWrappableMethod(message, aOperation.getName() + "Response")) {
      return false;
    }
    
    return true;
  }

  public Set<FaultDescription> getFaults() {
    Set<FaultDescription> ret = new HashSet<FaultDescription>();
    for (TOperation op: iNameToOperation.values()) {
      ret.addAll(getFaults(op));
    }
    
    return ret;
  }

  public Set<FaultDescription> getFaults(TOperation aOperation) {
    Set<FaultDescription> ret = new HashSet<FaultDescription>();
    for (TFault fault: aOperation.getFault()) {
      TMessage message = iNameToMessage.get(fault.getMessage());
      TPart part = message.getPart().get(0);
      
      Type type;
      if (part.getElement() != null && part.getElement().getNamespaceURI()!=null && part.getElement().getLocalPart().length()>0) {
	QName unp = part.getElement();
	Property p = iContext.getTypeHelper().getOpenContentProperty(unp.getNamespaceURI(), unp.getLocalPart());
	if (p == null) {
	  throw new RuntimeException("Could not find global property: "+part.getElement());
	}
	
	type = p.getType();
      } else {
	QName unp = part.getTPartType();
	type = iContext.getTypeHelper().getType(unp.getNamespaceURI(), unp.getLocalPart());					
      }
      
      WebServiceStyle style = this.getStyle(aOperation.getName(), Direction.FAULT, fault.getName());
      ret.add(new FaultDescription(new QName(getNamespace(), message.getName()),style.getFaultType(type, message)));
    }
    
    return ret;
  }
  
  protected String getNamespace() {
    if (iNamespaceFromDefs == null) {
      loadService();
    }
    
    return iNamespaceFromDefs;
  }
  
  public String getExceptionFromDetailType(String aOperationName, Type aType) {
    if (iPackageName == null) {
      return null;
    }
    
    for (FaultDescription fault: getFaults(findOperation(aOperationName))) {
      if (aType == fault.getDetailType()) {
	return iPackageName + "." + fault.getExceptionUNP().getLocalPart();
      }
    }
    
    throw new RuntimeException("No exception identified for detail type " + aType);
  }
  
  public DataFactory getContainerDataFactory() {
    return HelperProvider.getDefaultContext().getDataFactory();
  }
  
  public NamedInterfaceMetadata getInterfaceMetaData() {
    NamedInterfaceMetadata ret = (NamedInterfaceMetadata) getContainerDataFactory().create(NamedInterfaceMetadata.class);

    for (TOperation op: getPortType().getOperation()) {
      WebServiceStyle inputStyle = this.getStyle(op.getName(), Direction.INPUT, null);
      WebServiceStyle outputStyle = this.getStyle(op.getName(), Direction.OUTPUT, null);
      
      Operation m = (Operation) this.getContainerDataFactory().create(Operation.class);      
      m.setName(op.getName());
      m.setInputMessageUri(op.getInput().getMessage());
      if(op.getOutput() != null)
	m.setOutputMessageUri(op.getOutput().getMessage());
      else
	m.setOutputMessageUri(null);

      // Add newly created operation.
      ret.getOperations().add(m);
      
      // Add results and arguments(parameters).
      if(op.getOutput() != null)
	outputStyle.setResult(m, op, iNameToMessage.get(op.getOutput().getMessage()), iContext);
      else
	outputStyle.setResult(m, op, null, iContext);
      inputStyle.setArguments(m, op, iNameToMessage.get(op.getInput().getMessage()), iContext);
      //m.getExceptions().addAll(provider.getFaults(op));
    }
    return ret;	
  }
  
  private String calculateUriPath (String aRelativeUri, String aBaseUri) throws IOException {
    URL rootURL = URLLoader.fileOrURLToURL(null, aBaseUri);
    URL locationURL = URLLoader.fileOrURLToURL(rootURL, aRelativeUri);
    
    return locationURL.toString();
  }
}
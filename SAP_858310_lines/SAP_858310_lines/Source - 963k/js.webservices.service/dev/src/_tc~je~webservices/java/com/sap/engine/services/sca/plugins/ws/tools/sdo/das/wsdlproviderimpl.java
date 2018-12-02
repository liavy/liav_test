package com.sap.engine.services.sca.plugins.ws.tools.sdo.das;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.xml.namespace.QName;

import org.xmlsoap.schemas.wsdl.TBinding;
import org.xmlsoap.schemas.wsdl.TDefinitions;
import org.xmlsoap.schemas.wsdl.TPort;
import org.xmlsoap.schemas.wsdl.TPortType;
import org.xmlsoap.schemas.wsdl.TService;
import org.xmlsoap.schemas.wsdl.soap.TAddress;

import com.sap.engine.interfaces.sca.SCAResolver;
import com.sap.engine.services.sca.plugins.ws.sdo.das.AbstractWsdlProvider;
import com.sap.engine.services.sca.plugins.ws.sdo.das.WsdlProvider;

import commonj.sdo.DataObject;
import commonj.sdo.Property;
import commonj.sdo.Type;
import commonj.sdo.helper.HelperContext;
import commonj.sdo.helper.TypeHelper;
import commonj.sdo.helper.XSDHelper;

public class WsdlProviderImpl extends AbstractWsdlProvider implements WsdlProvider {
	final String _wsdlUrlString;
	final String _serviceName;
	final String _portName;
	public TPortType getPortType() {
		if (_servicePort == null) {
			loadService();
		}
		return iPortType;
	}

	public TDefinitions getWsdl() {
		initializeTypes();
		// TODO:  Add support for WSDL 2.0
		return (TDefinitions) iWsdlHelper.getWsdl(_wsdlUrlString, iResolver);
	}
	private static final Property ADDRESS_PROPERTY = lookupAddressProperty();

	private static Property lookupAddressProperty() {
		Type addressType = TypeHelper.INSTANCE.getType(TAddress.class);
		return XSDHelper.INSTANCE.getGlobalProperty(addressType.getURI(),"address",true);
	}
	public String getServiceLocation() {
		DataObject port = (DataObject)_servicePort;
		if (port.getList(ADDRESS_PROPERTY).size()==1) {
			TAddress address = (TAddress)port.getList(ADDRESS_PROPERTY).get(0);
			if (address != null) {
				return address.getLocation();
			}
		}
		if (_wsdlUrlString.endsWith("?WSDL")) {
			return _wsdlUrlString.substring(0,_wsdlUrlString.length()-5);
		}
		throw new RuntimeException("Could not retrieve service location");
	}
	public List<Type> getTypes() {
		return iWsdlHelper.getDefinedTypes(_wsdlUrlString, iResolver);
	}
	public List<Property> getProperties() {
		return iWsdlHelper.getDefinedProps(_wsdlUrlString, iResolver);
	} 
	public QName getPortName() {
		return new QName(iNamespaceFromDefs, _portName);
	}

	public QName getServiceName() {
		return new QName(iNamespaceFromDefs, _serviceName);
	}
	public URL getWsdlUrl() {
		try {
			loadService();
			return new URL(getServiceLocation()+"?wsdl");
		} catch (MalformedURLException e) {
			throw new RuntimeException("",e);
		}		
	}
	public WsdlProviderImpl(String wsdlUrlString, String serviceName, String portName, String myPackageName, SCAResolver resolver, HelperContext ctx) {
		super(resolver,ctx);
		_wsdlUrlString = wsdlUrlString;
		_serviceName = serviceName;
		_portName = portName;
		iPackageName = myPackageName;
	}
	private TService _service; 
	TPort _servicePort;

	protected TBinding getServiceBinding(TDefinitions definitions) {
		for (TService service: definitions.getService()) {
			if (_serviceName.equals(service.getName())) {
				_service = service;
				break;
			}
		}
		if (_service == null) {
			throw new RuntimeException("Service "+_serviceName+" not found in WSDL "+_wsdlUrlString);
		}
		for (TPort port: _service.getPort()) {
			if (_portName.equals(port.getName())) {
				_servicePort = port;
				break;
			}
		}
		if (_servicePort == null) {
			throw new RuntimeException("Port "+_portName+" not found in WSDL for service "+_serviceName);
		}
		QName unp = _servicePort.getBinding();
		for (TBinding binding: definitions.getBinding()) {
			if (binding.getName().equals(unp.getLocalPart())
				&& definitions.getTargetNamespace().equals(unp.getNamespaceURI())
					) {
				iServiceBinding = binding;
				break;
			}
		}
		if (iServiceBinding == null) {
			throw new RuntimeException("Binding "+_servicePort.getBinding()+" not found in WSDL");
		}
		return iServiceBinding;
	}
	
	protected String getWsdlLocation() {
	   return null;
	 }
}

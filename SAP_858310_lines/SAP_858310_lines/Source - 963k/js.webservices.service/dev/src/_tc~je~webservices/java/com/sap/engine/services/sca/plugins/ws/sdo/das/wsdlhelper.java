package com.sap.engine.services.sca.plugins.ws.sdo.das;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3.www.ns.wsdl.DescriptionType;
import org.w3.www.ns.wsdl.ImportType;
import org.xmlsoap.schemas.wsdl.TDefinitions;
import org.xmlsoap.schemas.wsdl.TImport;

import com.sap.engine.interfaces.sca.SCAResolver;
import com.sap.engine.lib.xml.util.BASE64Encoder;
import com.sap.sdo.api.helper.SapXmlDocument;
import com.sap.sdo.api.helper.SapXmlHelper;

import commonj.sdo.DataObject;
import commonj.sdo.Property;
import commonj.sdo.Type;

import commonj.sdo.helper.HelperContext;
import commonj.sdo.helper.XMLDocument;

public class WsdlHelper {
  
  private final HelperContext _ctx;
  public Map<String, DataObject> _urlToDef = new HashMap<String, DataObject>();
  public Map<String, List<Type>> _urlToType = new HashMap<String, List<Type>>();
  public Map<String, List<Property>> _urlToProps = new HashMap<String, List<Property>>();
  
  public WsdlHelper(HelperContext ctx) {
    super();
    ctx.getTypeHelper().getType(TDefinitions.class);
    _ctx = ctx;
  }
  
  public DataObject readWsdl(String url, InputStream is) throws IOException {
    _ctx.getTypeHelper().getType(TDefinitions.class);
    
    Map options = new HashMap();
    options.put(SapXmlHelper.OPTION_KEY_DEFINE_SCHEMAS, SapXmlHelper.OPTION_VALUE_FALSE);
    XMLDocument doc = _ctx.getXMLHelper().load(is, url, options);
			
    _urlToType.put(url, _ctx.getTypeHelper().define(((SapXmlDocument)doc).getDefinedTypes()));
    List<Property> props = new ArrayList<Property>();
    _urlToProps.put(url, props);
    for (Map.Entry<String, List<DataObject>> uriEntry: ((SapXmlDocument)doc).getDefinedProperties().entrySet()) {
      for (DataObject item: uriEntry.getValue()) {
	props.add(_ctx.getTypeHelper().defineOpenContentProperty(uriEntry.getKey(), item));
      }
    }
    
    return doc.getRootObject();
  }
  
  public DataObject getWsdl(String url, SCAResolver resolver) {
    DataObject ret = _urlToDef.get(url);
    if (ret == null) {
      ret = loadWsdl(url,resolver);
    }
    
    return ret;
  }
  
  public List<Type> getDefinedTypes(String url, SCAResolver resolver) {
    List<Type> ret = _urlToType.get(url);
    if (ret == null) {
      List<Property> props = new ArrayList<Property>();
      DataObject wsdl = loadWsdl(url,resolver);
      
      if (wsdl instanceof TDefinitions) {
	for (TImport imp: ((TDefinitions)wsdl).getImport()) {
	  ret.addAll(getDefinedTypes(imp.getLocation(),resolver));
	  props.addAll(getDefinedProps(imp.getLocation(),resolver));
	}
      } else {
	for (ImportType imp: ((DescriptionType)wsdl).getImport()) {
	  ret.addAll(getDefinedTypes(imp.getLocation(),resolver));
	  props.addAll(getDefinedProps(imp.getLocation(),resolver));
	}				
      }
      
      _urlToType.put(url,ret);
      _urlToProps.put(url,props);
    }
    
    return ret;
  }
  
  public List<Property> getDefinedProps(String url, SCAResolver resolver) {
    List<Property> ret = _urlToProps.get(url);
    if (ret == null) {
      getDefinedTypes(url, resolver);
      ret = _urlToProps.get(url);
    }
    
    return ret;
  }
  
  private DataObject loadWsdl(String url, SCAResolver resolver) {
    if (isRemote(url)) {
      return loadWsdlOverHttp(url);
    }
    
    try {
      try {
	return readWsdl(url, new URL(url).openStream());
      } catch (Exception e) {
	return readWsdl(url, resolver.getResource(url).openStream());
      }      
    } catch (IOException e) {
      throw new RuntimeException("",e);
    } 
  }
  
  private boolean isRemote(String url) {
    // TODO:  Not very robust
    return url.startsWith("http:");
  }
  
  private DataObject loadWsdlOverHttp(String url) {
    URL address = null;
    
    try {
      address = new URL(url);
      HttpURLConnection connection = (HttpURLConnection)address.openConnection();
      connection.setRequestMethod("GET");
      connection.setDoOutput(false);
      
      String encoding = new String(BASE64Encoder.encode("anzeiger:display".getBytes())); //$JL-I18N$
      connection.addRequestProperty("Authorization", "Basic " + encoding);
      connection.addRequestProperty("Content-Type", "text/xml; charset=utf-8");
      connection.connect();
      
      int code = connection.getResponseCode();
      if (code != HttpURLConnection.HTTP_OK) {
	throw new RuntimeException("HTTP " + code + " " + connection.getResponseMessage());
      }
      
      InputStream is = connection.getInputStream();
      return readWsdl(url, is);
    } catch (MalformedURLException e) {
      throw new RuntimeException("",e);
    } catch (ProtocolException e) {
      throw new RuntimeException("",e);
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(url);
    }
  }
}

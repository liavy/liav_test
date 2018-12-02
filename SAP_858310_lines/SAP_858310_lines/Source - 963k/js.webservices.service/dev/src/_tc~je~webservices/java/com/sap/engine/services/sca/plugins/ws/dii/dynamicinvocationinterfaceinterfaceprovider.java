package com.sap.engine.services.sca.plugins.ws.dii;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import com.sap.sca.sdo.DiiInterface;
import com.sap.sdo.api.SdoTypeMetaData;

import org.osoa.sca.sdo.Interface;

import com.sap.engine.interfaces.sca.SCAResolver;
import com.sap.engine.interfaces.sca.assembly.AssemblyException;
import com.sap.engine.interfaces.sca.wire.InterfaceMetadata;
import com.sap.engine.interfaces.sca.wire.Operation;
import com.sap.engine.interfaces.sca.wire.Parameter;
import com.sap.lib.javalang.annotation.AnnotationRecord;
import com.sap.lib.javalang.element.ClassInfo;
import com.sap.lib.javalang.element.MethodInfo;
import com.sap.lib.javalang.tool.ReadResult;

import commonj.sdo.DataObject;
import commonj.sdo.Type;
import commonj.sdo.helper.CopyHelper;
import commonj.sdo.helper.DataFactory;
import commonj.sdo.helper.HelperContext;

/**
 * 
 * @author I044263
 *
 */
public class DynamicInvocationInterfaceInterfaceProvider {

  /**
   * Ctor.
   */
  public DynamicInvocationInterfaceInterfaceProvider() {
    
  }
  
  public void enrichContext(InterfaceMetadata aMetadata, HelperContext aContext) throws AssemblyException {
    try {
	Method method = aContext.getClass().getMethod("getClassLoader", new Class[]{});
	Object result = method.invoke(aContext, new Object[]{});
	ClassLoader cl = (ClassLoader) result;
	
	Class iface = cl.loadClass(((DiiInterface) aMetadata.getOriginal()).getInterface());
	
	for (Operation op: aMetadata.getOperations()) {
	  Class[] ps = new Class[op.getParameters().size()];
		
	  int k = -1;
	  for (Parameter p: op.getParameters()) {
	    ++ k;
	    String className = p.getClassName();			
		  
	    PrimitiveTypeInfo info = PrimitiveTypeInfo.get(className);
	    if (info == null) {
	      ps[k] = cl.loadClass(className);
	    } else {
	      ps[k] = info.clazz;
	    }			 
	  }
		
	  op.setMethod(iface.getMethod(op.getName(), ps));
	}		
    } catch (IllegalAccessException iae) {
	throw new AssemblyException("Can not enrich context!", iae);
    } catch (NoSuchMethodException nsme) {
	throw new AssemblyException("Can not enrich context!", nsme);
    } catch (InvocationTargetException ite) {
	throw new AssemblyException("Can not enrich context!", ite);
    } catch (ClassNotFoundException cnfe) {
	throw new AssemblyException("Can not enrich context!", cnfe);
    }    
  }
  
  public InterfaceMetadata generateMetadata(Interface aInterface, SCAResolver aResolver, HelperContext aHelper) throws AssemblyException {    
    DiiInterface wsdlInterface = (DiiInterface) aInterface;
    
    ReadResult annotations = aResolver.getAnnotations();
    ClassInfo classInfo = annotations.getClass(wsdlInterface.getInterface());
    
    InterfaceMetadata ret = (InterfaceMetadata) DataFactory.INSTANCE.create(InterfaceMetadata.class);
    ret.setOriginal((Interface) CopyHelper.INSTANCE.copy((DataObject) aInterface));

    MethodInfo[] methods = classInfo.getMethods();
    for (int i = 0; i < methods.length; ++ i) {
	Operation op = (Operation) DataFactory.INSTANCE.create(Operation.class);
	op.setName(methods[i].getName());
	
	try {
	  this.addParameters(op.getParameters(), methods[i].getGenericParameters(), aHelper, annotations);
	} catch (ClassNotFoundException cnfe) {
	  throw new AssemblyException("Can not add parameter to operation with name: " + op.getName(), cnfe);
	}
	
	ret.getOperations().add(op);
    }    
    
    return ret;    
  }
  
  private void addParameters(List<Parameter> aParameters, String[] aParameterTypes, HelperContext aHelper, ReadResult aAnnotations) throws ClassNotFoundException {
    for (int i = 0; i < aParameterTypes.length; ++ i) {
      String type = aParameterTypes[i];
      
      this.introspectParameter(aParameters, aHelper, aAnnotations, type);
    }
  }
  
  private void introspectParameter(List<Parameter> aParameters, HelperContext aHelper, ReadResult aAnnotations, String aType) throws ClassNotFoundException {
    aParameters.add(this.createParameter(aHelper, aAnnotations, aType));
  }
  
  private Parameter createParameter(HelperContext aHelper, ReadResult aAnnotations, String aType) throws ClassNotFoundException {
    Parameter p = (Parameter) DataFactory.INSTANCE.create(Parameter.class);
    p.setClassName(aType.startsWith("javax.xml.ws.Holder") ? "javax.xml.ws.Holder" :  aType);
    p.setTypeUri(aType.startsWith("javax.xml.ws.Holder") ? new QName("javax.xml.ws.Holder") : this.getSdoType(aHelper, aType, aAnnotations));
    
    return p;
  }
  
  private static class TypeInfo {
    /** "base type" used in various descriptors (see JVMS section 4.3.2) */
    public String baseTypeString;

    /** name of corresponding wrapper class */
    public String className;    	
    public Class clazz;
    }  
  
  /**
   * A PrimitiveTypeInfo object contains assorted information about
   * a primitive type in its public fields.  The struct for a particular
   * primitive type can be obtained using the static "get" method.
   */
  private static class PrimitiveTypeInfo extends TypeInfo {
    private static Map<String,PrimitiveTypeInfo> table = new HashMap<String,PrimitiveTypeInfo>();
    static {
      add(byte.class, "byte");
      add(char.class, "char");
      add(double.class, "double");
      add(float.class, "float");
      add(int.class, "int");
      add(long.class, "long");
      add(short.class, "short");
      add(boolean.class, "boolean");
    }

    private static void add(Class aPrimitiveClass, String aName) {
      PrimitiveTypeInfo ret = new PrimitiveTypeInfo(aPrimitiveClass, aName);
      table.put(aName, ret);
    }

    private PrimitiveTypeInfo(Class aPrimitiveClass, String aName) {
      assert aPrimitiveClass.isPrimitive();

      baseTypeString = Array.newInstance(aPrimitiveClass, 0).getClass().getName().substring(1);
      className = aName;
      clazz = aPrimitiveClass;
    }

    public static PrimitiveTypeInfo get(String aClass) {
      return table.get(aClass);
    }
  } 
  
  private QName getSdoType(HelperContext aHelper, String aJavaType, ReadResult aAnnotations) throws ClassNotFoundException {	
    if ("int".equals(aJavaType))
      return this.getQName(aHelper.getTypeHelper().getType(int.class));
      
    if ("boolean".equals(aJavaType))
      return this.getQName(aHelper.getTypeHelper().getType(boolean.class));

    if ("double".equals(aJavaType))
      return this.getQName(aHelper.getTypeHelper().getType(double.class));

    if ("long".equals(aJavaType))
      return this.getQName(aHelper.getTypeHelper().getType(long.class));

    if ("short".equals(aJavaType))
      return this.getQName(aHelper.getTypeHelper().getType(short.class));
      
    try {
      Class clazz = Thread.currentThread().getContextClassLoader().loadClass(aJavaType);
      return this.getQName(aHelper.getTypeHelper().getType(clazz));
    } catch (ClassNotFoundException e) {
      if (aAnnotations == null)
	throw e;
	
      ClassInfo clz = aAnnotations.getClass(aJavaType);
      if (clz != null) {
	int p = clz.getName().lastIndexOf('.');
	String pn;
	String cn;
	if (p < 0) {
	  pn = "";
	  cn = clz.getName();
	} else {
	  pn = clz.getName().substring(0, p);
	  cn = clz.getName().substring(p + 1);
	}
	
	String uri = pn;
	AnnotationRecord metaData = clz.getAnnotation(SdoTypeMetaData.class.getName());
	if (metaData != null && 
	    metaData.getMember("uri") != null && 
	    metaData.getMember("uri").getStringValue().length() > 0) {
	  uri = metaData.getMember("uri").getStringValue();
          }
	
	if (metaData != null && 
	    metaData.getMember("sdoName") != null && 
	    metaData.getMember("sdoName").getStringValue().length() > 0) {
	  cn =  metaData.getMember("sdoName").getStringValue();
	}
	return new QName(uri,cn);
      }
	
      throw e;
    }
  }
	
  private QName getQName(Type aType) {
    return new QName(aType.getURI(), aType.getName());
  }	
}

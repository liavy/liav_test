package com.sap.engine.services.sca.plugins.ws.tools.sdo.das;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import com.sap.engine.interfaces.sca.wire.NamedInterfaceMetadata;
import com.sap.engine.interfaces.sca.wire.Operation;
import com.sap.engine.interfaces.sca.wire.Parameter;
import com.sap.sdo.api.helper.InterfaceGenerator;
import com.sap.sdo.api.helper.SapTypeHelper;
import com.sap.sdo.api.types.SapProperty;
import com.sap.sdo.api.util.URINamePair;
import com.sap.engine.services.sca.plugins.ws.sdo.das.FaultDescription;

import commonj.sdo.Type;
import commonj.sdo.helper.DataFactory;
import commonj.sdo.helper.TypeHelper;
import commonj.sdo.impl.HelperProvider;

public class BusinessInterfaceGenerator {
	private static final String STUB = "Stub";
	public static final String INDENT = "    ";
	public static final String INDENT2 = INDENT+INDENT;
	public static final String INDENT3 = INDENT2+INDENT;
	public static final String INDENT4 = INDENT3+INDENT;
	public static final String INDENT5 = INDENT4+INDENT;

	public interface ConstructorGenerator {
		void write(String name, Writer implWriter) throws IOException;
	}
	private InterfaceGenerator _visitor;
	private final String _packageName;
	private final File _directory;
	private List<String> _classNames;
	private String _serviceUri;
	public BusinessInterfaceGenerator(String packageName, String directoryPath, String namespace) {

		InterfaceGenerator _visitor = ((SapTypeHelper)HelperProvider.getDefaultContext().getTypeHelper()).createInterfaceGenerator(directoryPath);

		// = new JavaGeneratorVisitor(packageName, directoryPath, HelperProvider.getDefaultContext());
		if (packageName != null) {
			_packageName = packageName;
			_visitor.addPackage(namespace, packageName);
		} else {
			_packageName = _visitor.getPackage(namespace);
		}
		_directory = new File(directoryPath+File.separatorChar+
            _packageName.replace('.',File.separatorChar));
		_directory.mkdirs();
		if (!_directory.isDirectory()) {
			throw new IllegalArgumentException("Directory "+_directory.getPath()+" could not be created.");
		}
	}
	public void generateTypes(List<Type> types) throws IOException {
		_classNames = _visitor.generate(types);
		
	}
	public void generate(NamedInterfaceMetadata descr, ConstructorGenerator constructorGenerator) throws FileNotFoundException, IOException {
		Writer writer = createWriter(_directory, descr.getName(), "");
        Writer implWriter = createWriter(_directory, descr.getName(), STUB);
		writer.write("package "+_packageName+";\n\n");
		writer.write("import java.util.List;\n");
		implWriter.write("package "+_packageName+";\n\n");
		implWriter.write("import com.sap.engine.interfaces.sca.runtime.SCAProxy;\n");
		implWriter.write("import com.sap.engine.interfaces.sca.wire.InterfaceMetadata;\n");
		implWriter.write("import com.sap.engine.interfaces.sca.wire.Message;\n");
		implWriter.write("import com.sap.engine.interfaces.sca.wire.Operation;\n");
		implWriter.write("import com.sap.engine.interfaces.sca.wire.Result;\n");
		implWriter.write("import com.sap.engine.interfaces.sca.wire.Value;\n");
		implWriter.write("import commonj.sdo.DataObject;\n");
		implWriter.write("import commonj.sdo.helper.TypeHelper;\n");
		
		writer.write("\npublic interface "+descr.getName()+" {\n");
		implWriter.write("\npublic class "+descr.getName()+STUB+" implements "+descr.getName()+" {\n");	
		implWriter.write(INDENT+"static {\n");
		for (String c: _classNames) {
			if (!c.startsWith("org.xmlsoap.schemas")) {
				implWriter.write(INDENT2+"TypeHelper.INSTANCE.getType("+c+".class);\n");
			}
		}
		implWriter.write(INDENT+"}\n");
		implWriter.write(INDENT+"private final SCAProxy _invoker;\n");
		implWriter.write(INDENT+"private final InterfaceMetadata _imd;\n");
		constructorGenerator.write(descr.getName()+STUB, implWriter);
		for (Operation m: descr.getOperations()) {
			writer.write(INDENT);
			implWriter.write(INDENT+"public ");
			if (m.getResults().size() < 1) {
				writer.write("void");
				implWriter.write("void");
			} else {
				writeType(writer, m.getResults().get(0),false);
				writeType(implWriter, m.getResults().get(0),false);
			}
			writer.write(' '+m.getName()+'(');
			implWriter.write(' '+m.getName()+'(');
			boolean first = true;
			for (Parameter p: m.getParameters()) {
				if (!first) {
					writer.write(", ");
					implWriter.write(", ");
				}
				first = false;
				writeType(writer,p,false);
				writeType(implWriter,p,false);
				writer.write(" "+p.getName());
				implWriter.write(" "+p.getName());
			}
			writer.write(")");
			implWriter.write(")");

			writer.write(";\n");
			implWriter.write(" {\n");
			implWriter.write(INDENT2+"Value[] params = new Value["+m.getParameters().size()+"];\n");
			int i=0;
			for (Parameter p: m.getParameters()) {
				implWriter.write(INDENT2+"params["+(i++)+"] = new Value("+p.getName()+");\n");
			}
			implWriter.write(INDENT2+"Operation op = (Operation)((DataObject)_imd).get(\"operations[name='"+m.getName()+"']\");\n");
			implWriter.write(INDENT2+"Message req = new Message(op, params);\n");
			implWriter.write(INDENT2+"try {\n");
			implWriter.write(INDENT3+"Result resp = _invoker.accept(req);\n");
			implWriter.write(INDENT3+"if (resp.getException() != null) {\n");
//			for (FaultDescription t: m.getExceptions()) {
//				implWriter.write(INDENT4+"if (resp.getException() instanceof ");
//				implWriter.write(getFullyQualifiedName(t));
//				implWriter.write(") {\n");
//				implWriter.write(INDENT5+"throw (");
//				implWriter.write(getFullyQualifiedName(t));
//				implWriter.write(") resp.getException();\n");
//				implWriter.write(INDENT4+"}\n");
//			}
			implWriter.write(INDENT4+"throw new com.sap.sdo.das.common.DasRuntimeException(resp.getException());\n");
			implWriter.write(INDENT3+"}\n");
			if (m.getResults().size() > 0) {
				implWriter.write(INDENT3+"return (");
				writeType(implWriter, m.getResults().get(0), true);
				implWriter.write(") resp.getValue();\n");
			}
			implWriter.write(INDENT2+"} catch (Exception e) {\n");
			implWriter.write(INDENT3+"throw new RuntimeException(\"\",e);\n");			
			implWriter.write(INDENT2+"}\n");
			implWriter.write(INDENT+"}\n");
		}
		writer.write("}\n");
		writer.flush();
		writer.close();
		implWriter.write("}\n");
		implWriter.flush();
		implWriter.close();
	}
	private String getFullyQualifiedName(FaultDescription exType) {
		return _visitor.getPackage(exType.getExceptionUNP().getNamespaceURI())
		 	+ '.' + NameConverter.CONVERTER.toClassName(exType.getExceptionUNP().getLocalPart());
	}
	private Writer createWriter(File directory, String name, String additional) throws FileNotFoundException {
		Writer writer;
		final File javaFile = new File(directory.toString()+File.separator+name+additional+".java");
		try {
			writer = new PrintWriter(javaFile);
		} catch (FileNotFoundException e) {
			throw e;
		}
		return writer;
	}
	private void writeType(Writer writer, Parameter param, boolean pConvertToObject) throws IOException {
		boolean convertToObject = pConvertToObject;
		// TODO
//		if (isMany) {
//			writer.write("java.util.List<");
//			convertToObject = true;
//		}
		QName typeUnp = param.getTypeUri();
		Type type = TypeHelper.INSTANCE.getType(typeUnp.getNamespaceURI(), typeUnp.getLocalPart());
		String typeName;
		if (type.isDataType()) {
			Class clazz = type.getInstanceClass();
			if (clazz.isPrimitive() && convertToObject) {
				//clazz = ((JavaSimpleType)TypeHelper.INSTANCE.getType(clazz)).getNillableType().getInstanceClass();
			}
			typeName = NameConverter.CONVERTER.normalizeClassname(clazz.getName());
		} else if (type == getDataObjectType()) {
			typeName = "Object";
		} else {
			typeName = NameConverter.CONVERTER.toClassName(type.getName());
			if (!type.getURI().equals(_serviceUri)) {
				typeName = _visitor.getPackage(type.getURI())+"."+typeName;
			}
		}
		writer.write(typeName);
//		if (isMany) {
//			writer.write('>');
//		}
	}
	private Type getDataObjectType() {
		return TypeHelper.INSTANCE.getType(URINamePair.MODELTYPE_URI, "DataObject");
	}
	public void generateExceptions(Set<FaultDescription> faults) throws IOException {
        for (FaultDescription fault: faults) {
        	File directory = _visitor.getDirectory(fault.getExceptionUNP().getNamespaceURI());
        	Writer faultWriter = createWriter(directory, fault.getExceptionUNP().getLocalPart(), "");
        	String packageName = _visitor.getPackage(fault.getExceptionUNP().getNamespaceURI());
        	faultWriter.write("package "+packageName+";\n\n");
        	faultWriter.write("\npublic class "+fault.getExceptionUNP().getLocalPart()+" extends com.sap.sdo.das.ws.WebServiceException {\n");
        	for (SapProperty p: (List<SapProperty>)fault.getDetailType().getProperties()) {
        		String javaName = p.getJavaName();
        		if (javaName == null) {
        			javaName = p.getName();
        		}
        		faultWriter.write(INDENT+"private final ");
        		writeType(faultWriter, makeParameter(p), true);
        		faultWriter.write(" _"+javaName+";\n");
        		faultWriter.write(INDENT+"public ");
        		writeType(faultWriter, makeParameter(p), false);
        		faultWriter.write(" get"+NameConverter.CONVERTER.toPropertyName(javaName)+"() {\n");
        		faultWriter.write(INDENT2+"return _"+javaName+";\n");
        		faultWriter.write(INDENT+"}\n");
        	}
        	String detailClassName = _visitor.getFullClassName(fault.getDetailType());
        	faultWriter.write(INDENT+"public "+fault.getExceptionUNP().getLocalPart()+'('+detailClassName+" detail) {\n");
        	faultWriter.write(INDENT2+"commonj.sdo.DataObject detailDo = (commonj.sdo.DataObject)detail;\n");
        	for (SapProperty p: (List<SapProperty>)fault.getDetailType().getProperties()) {
        		String javaName = p.getJavaName();
        		if (javaName == null) {
        			javaName = p.getName();
        		}
        		faultWriter.write(INDENT2+"_"+javaName+" = (");
        		writeType(faultWriter, makeParameter(p), true);
        		faultWriter.write(") detailDo.get(\""+p.getName()+"\");\n");
        	}
    		faultWriter.write(INDENT+"}\n");
        	faultWriter.write("}\n");
        	faultWriter.flush();
        	faultWriter.close();
        }
	}
	private Parameter makeParameter(SapProperty p) {
		Parameter ret = (Parameter)DataFactory.INSTANCE.create(Parameter.class);
		ret.setName(p.getName());
		ret.setTypeUri(new QName(p.getType().getURI(), p.getName()));
		return ret;
	}
	public String getPackageName() {
		return _packageName;
	}
}

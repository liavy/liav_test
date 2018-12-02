/*
 * Copyright (c) 2004 by SAP AG, Walldorf., http://www.sap.com All rights
 * reserved. This software is the confidential and proprietary information of
 * SAP AG, Walldorf. You shall not disclose such Confidential Information and
 * shall use it only in accordance with the terms of the license agreement you
 * entered into with SAP.
 */
package com.sap.engine.services.webservices.jaxws.j2w;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.sap.engine.lib.xml.dom.DOM;
import com.sap.engine.services.webservices.espbase.mappings.ParameterMapping;
import com.sap.engine.services.webservices.jaxrpc.schema2java.SchemaMappingConstants;
import com.sap.engine.services.webservices.jaxrpc.util.PackageBuilder;
import com.sap.engine.services.webservices.tools.SharedDocumentBuilders;
import com.sun.xml.bind.api.JAXBRIContext;

/**
 * @author Dimitar Velichkov (I033362) dimitar.velichkov@sap.com
 */
public class JaxWsSchemaGenerator {

  private ArrayList<Class<?>>              types;
  private ArrayList<BeanGenerator>         beans;
  private String                           tNs;
  private Package                          pkg;
  private HashMap<ParameterMapping, QName> paramToElemMap;
  private DOMSource[]                      sch;
  private Class<?>[]                       classes;
  private Set<String>                      beanClasses = new HashSet();
  
  public JaxWsSchemaGenerator(ArrayList<Class<?>> types, ArrayList<BeanGenerator> beans, HashMap<ParameterMapping, QName> paramToElemMap, Package pkg, String tNs) {

    if (types == null || beans == null || paramToElemMap == null || pkg == null || tNs == null) {
      throw new IllegalArgumentException();
    }

    this.types = types;
    this.beans = beans;
    this.pkg = pkg;
    this.tNs = tNs;
    this.paramToElemMap = paramToElemMap;

  }

  private void addBeansForCompile(String sourcePath) throws JaxWsInsideOutException {

    for (BeanGenerator bg : beans) {

      try {
        // generate the bean source into a memory buffer
        bg.generate(sourcePath);
        beanClasses.add(bg.getBeanClassName());
      } catch (IOException e1) {
        throw new JaxWsInsideOutException(JaxWsInsideOutException.BEAN_GENERATION_ERROR, e1);
      }
    }

  }

  /**
   * Clean the compiler's work dir
   * 
   * @param compilerPath
   * @param filter
   * @param deleteDirs
   *          set to true if directories should also be deleted
   */
  private void cleanCompilerDir(File compilerPath, FileFilter filter, boolean deleteDirs) {

    File[] currentFiles = compilerPath.listFiles(filter);
    if (currentFiles != null) {
      for (File f : currentFiles) {

        if (f.isDirectory()) {
          cleanCompilerDir(f, filter, deleteDirs);
        } else {
          f.delete();
        }

      }

      if (deleteDirs) {
        compilerPath.delete();
      }
    }
  }

  /**
   * Generates a package-info.java file, which holds default package->schema
   * namespace mapping information for types that have no annotations and would
   * otherwise be put by JAXB into a schema with no target namespace. NOT
   * RELIABLE!
   * 
   * @throws JaxWsInsideOutException
   */
  private void generatePackageInfo(String sourcePath, String outP) throws JaxWsInsideOutException {
    OutputStreamWriter pkgInfWr = null;
    File pkgInfoFile;
    File outDir = null;
    try {
      // File outDir = new File(this.sourcePath + File.separator +
      // pkg.getName().replaceAll("\\.", "\\" + File.separator));
      outDir = new File(sourcePath).getParentFile();
      if (!outDir.exists()) {
        outDir.mkdirs();
      }

      String newLine = System.getProperty("line.separator");
      pkgInfoFile = new File(outDir.getAbsolutePath() + File.separator + "package-info.java");
      pkgInfWr = new OutputStreamWriter(new FileOutputStream(pkgInfoFile), "UTF-8");
      pkgInfWr.write("@" + XmlSchema.class.getName() + "(namespace = \"" + tNs + "\")");
      pkgInfWr.write("package " + pkg.getName() + ";" + newLine);

    } catch (Exception ex) {
      throw new JaxWsInsideOutException("Error writing \"package-info.java\" file!", ex);
    } finally {
      if (pkgInfWr != null) {
        try {
          pkgInfWr.close();
        } catch (IOException e) {
          // $JL-EXC$
        }
      }
    }

    File outPath;

    // neccessary for CTS, which places pre-compiled beans in a directory
    // created by appending the full pacakge name of the SEI package to the
    // generated classes directory. package-info should be placed there, and NOT
    // in the package-name-derived subdirectory of "generated_classes"!
    // E.g.:
    // Pre-compiled beans are placed in by CTS in
    // D:\Develop\InQMy\CTS\50_DEV\classes_vi_built\com\sun\ts\tests\jaxws\mapping\j2wmapping\document\literal\wrapped
    // package-info.class should be placed THERE, and NOT in
    // D:\Develop\InQMy\CTS\50_DEV\classes_vi_built\com\sun\ts\tests\jaxws\mapping\j2wmapping\document\literal\wrapped\generated_classes\com\sun\ts\tests\jaxws\mapping\j2wmapping\document\literal\wrapped
    String sepRegEx = System.getProperty("os.name").contains("Windows") ? "\\" + File.separator : File.separator;
    int pkgNameStart = outDir.getAbsolutePath().indexOf(pkg.getName().replaceAll("\\.", sepRegEx));
    if (pkgNameStart == -1) {
      outPath = new File(outP);
    } else {
      outPath = new File(outDir.getAbsolutePath().substring(0, pkgNameStart));
    }

    PackageBuilder pb = new PackageBuilder();
    pb.setOutputPath(outPath);
    try {
      pb.compile("", outDir);
    } catch (Exception e) {
      throw new JaxWsInsideOutException("Error compiling package-info.java in " + outDir.getAbsolutePath());
    }

  }

  /**
   * If beans were compiled, load them using special classloader, add them to
   * "types" array so that they can be passed to JAXB
   * 
   * @throws JaxWsInsideOutException
   */
  private void loadBeans(String outPath, ClassLoader serverClassLoader) throws JaxWsInsideOutException {

    // load all compiled bean classes using the ldr. Necessary for JAXB and
    // schema generation
    ClassLoader parent = this.getClass().getClassLoader();
    if(serverClassLoader != null){
      parent = serverClassLoader; 
    }
    JaxWsBeanClassLoader ldr = new JaxWsBeanClassLoader(parent, outPath, beanClasses);

    for (BeanGenerator bg : beans) {
      try {

        // don't attempt to load class if no bean was generated for the
        // exception!
        if ((bg instanceof FaultBeanGenerator && ((FaultBeanGenerator) bg).isExceptionCompliant()) ||
            isJaxWSSchemaBuiltInType(bg.getBeanClassName())) {
          continue;
        } else {
          types.add(ldr.loadClass(bg.getBeanClassName()));
        }
      } catch (ClassNotFoundException e) {
        throw new JaxWsInsideOutException(JaxWsInsideOutException.BEAN_CLASSLOAD_ERROR, e, bg.getBeanClassName());
      }
    }

    //cleanBareBeans();
  }

  private boolean isJaxWSSchemaBuiltInType(String className){
    return SchemaMappingConstants.JAVA_TO_SCHEMA_JAXB.get(className) != null;
  }
  
  private void compile(String sourcePath, String outPath, String serverClassPath) throws JaxWsInsideOutException {

    PackageBuilder pb = new PackageBuilder();
    pb.setOutputPath(new File(outPath));

    String classPath = "";
    if(classPath != null){
      classPath = serverClassPath;
    }
    try {
      pb.compile(classPath, new File(sourcePath));
    } catch (Exception e) {
      throw new JaxWsInsideOutException(JaxWsInsideOutException.BEAN_COMPILATION_ERROR, e);
    }

  }

  /**
   * Generate the schemas for the classes that were passed in
   * 
   * @throws JAXBException
   * @throws JaxWsInsideOutException
   */
  public void genJaxbMappings(String sourcePath, String outPath, String serverClassPath, ClassLoader ldr) throws JAXBException, JaxWsInsideOutException {
    genJaxbMappings(sourcePath, outPath, serverClassPath, ldr, true);
  }
  
  /**
   * Generate the schemas for the classes that were passed in
   * 
   * @throws JAXBException
   * @throws JaxWsInsideOutException
   */
  public void genJaxbMappings(String sourcePath, String outPath, String serverClassPath, ClassLoader ldr, boolean genSchemas) throws JAXBException, JaxWsInsideOutException {

     //generatePackageInfo();

    if (beans.size() > 0) {
      addBeansForCompile(sourcePath);
    }

    compile(sourcePath, outPath, serverClassPath);

    if (beans.size() > 0) {
      loadBeans(outPath, ldr);
    }
    
    this.classes = types.toArray(new Class<?>[types.size()]);
    if (genSchemas) {
      // Properties specific to SUN JAXB RI
      HashMap<String,String> props = new HashMap<String,String>(); 
      props.put(JAXBRIContext.DEFAULT_NAMESPACE_REMAP,this.tNs);
      JAXBContext ctx = null;
      try {
        ctx = JAXBContext.newInstance((Class[]) classes,props);
      } catch (JAXBException origE) {
        //needed .toString() of the exception to be called, since it returns detailed info.
        JAXBException newE = new JAXBException(origE.toString(), origE);
        throw newE;
      }
  
      // store each generated schema here
    
      final HashMap<String, DOMResult> nsToSchemaMap = new HashMap<String, DOMResult>();
  
      try {
        ctx.generateSchema(new SchemaOutputResolver() {
  
          public Result createOutput(String nsURI, String fileName) throws IOException {
  
            DOMResult dr = new DOMResult();          
            dr.setSystemId("");  
            nsToSchemaMap.put(nsURI, dr);
            return dr;
  
          }
  
        });
      } catch (IOException e) {
        throw new JaxWsInsideOutException(JaxWsInsideOutException.JAXB_ERROR, e);
      }
  
      removeBadDefNsImport(nsToSchemaMap.values());
      
      // create global elements in schema for headers and doc-bare types
      createGlobalElements(nsToSchemaMap);
          
      Collection<DOMResult> schema = nsToSchemaMap.values();
      sch = new DOMSource[schema.size()];
  
      int i = 0;
      for (DOMResult dr : schema) {
        sch[i++] = new DOMSource(((Document) dr.getNode()).getDocumentElement());
      }
    } else {
      sch = new DOMSource[0];
    }
  }

  /**
   * JAXB RI 2.0 puts an invalid "<xs:import schemaLocation=".">" element because the DOMResult systemID is set to the empty string. 
   * According to the doc, if the systemID is NOT set, i.e., null, the import element will not be generated, however the RI throws an exception when the 
   * system ID is NOT set. So we set it to the empty string, but afterwards we have to remove the bad import elements
   * @param schemas
   * @throws JaxWsInsideOutException
   */
  private void removeBadDefNsImport(Collection<DOMResult> schemas) throws JaxWsInsideOutException{
    
    for(DOMResult dr : schemas){
     
      Document schemaRoot = (Document)dr.getNode();
      NodeList nl = schemaRoot.getElementsByTagNameNS(XMLConstants.W3C_XML_SCHEMA_NS_URI, "import");
      for(int i = 0; i < nl.getLength(); ++i){
        Element el = (Element)nl.item(i);
        el.removeAttribute("schemaLocation");
        if (el.getAttribute("namespace").length() == 0) {
          el.setAttribute("namespace", "");
        }
//        String schemaLoc = el.getAttribute("schemaLocation");        
//        if(schemaLoc != null && schemaLoc.equals(".")){     
//          el.getParentNode().removeChild(el);          
//        }                
      }      
    }        
  }
  
  /**
   * @param nsToSchemaMap
   * @throws JaxWsInsideOutException
   */
  private void createGlobalElements(HashMap<String, DOMResult> nsToSchemaMap) throws JaxWsInsideOutException {

    int ctr = 0;
    for (ParameterMapping pm : paramToElemMap.keySet()) {

      QName elementName = pm.getSchemaQName();
      QName type = paramToElemMap.get(pm);
      String elUri = elementName.getNamespaceURI();

      DOMResult dr = (DOMResult) nsToSchemaMap.get(elUri);
      Document schemaRoot;

      // create a fresh schema for this namespace
      if (dr == null) {
        try {
          schemaRoot = SharedDocumentBuilders.newDocument();
        } catch (RuntimeException x) {
          throw new JaxWsInsideOutException("Error creating document builder for schema generation!", x);
        }
        Element schemaEl = schemaRoot.createElementNS(XMLConstants.W3C_XML_SCHEMA_NS_URI, "xs:schema");
        schemaEl.setAttribute("targetNamespace", elUri);
        schemaRoot.appendChild(schemaEl);

        nsToSchemaMap.put(elUri, new DOMResult(schemaRoot));
      } else {
        schemaRoot = (Document) nsToSchemaMap.get(elUri).getNode();
      }

      List possibleEl = DOM.getChildElementsByTagNameNS(schemaRoot.getDocumentElement(), XMLConstants.W3C_XML_SCHEMA_NS_URI, "element");
      Element found = null;
      for (int i = 0; i < possibleEl.size(); ++i) {
        Element possFound = (Element) possibleEl.get(i);
        Attr elType = possFound.getAttributeNode("type");
        Attr name = possFound.getAttributeNode("name");

        if (name != null && elType != null && type != null) {
          String typeVal = elType.getValue();
          typeVal = typeVal.indexOf(':') > 0 ? typeVal.substring(typeVal.indexOf(':') + 1) : typeVal;
          if (name.getValue().equals(elementName.getLocalPart()) && typeVal.equals(type.getLocalPart())) {
            found = possFound;
            break;
          }
        }
      }
      if (found == null) {
        createGlobalElement(elementName, schemaRoot, type, ctr);
      }
      ctr++;
    }

  }

  private void createGlobalElement(QName elementName, Document schemaRoot, QName type, int ctr) {
    Element newEl = schemaRoot.createElementNS(XMLConstants.W3C_XML_SCHEMA_NS_URI, "xs:element");
    newEl.setAttribute("name", elementName.getLocalPart());
    
    
//    if type is null, create an "empty" element like so: 
//    <xs:element name="helloString2">
//     <xs:complexType/>
//    </xs:element>
//    This is used when we have doc-bare and no input or output parameters
    if (type == null) {

      Element cplxType = schemaRoot.createElementNS(XMLConstants.W3C_XML_SCHEMA_NS_URI, "complexType");
      Element seq = schemaRoot.createElementNS(XMLConstants.W3C_XML_SCHEMA_NS_URI, "sequence");
      cplxType.appendChild(seq);
      newEl.appendChild(cplxType);
      
    } else {
      newEl.setAttribute("nillable", "true");
      Attr nsAttr = schemaRoot.createAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, XMLConstants.XMLNS_ATTRIBUTE + ":" + "ctr" + ctr);
      nsAttr.setValue(type.getNamespaceURI());
      newEl.setAttributeNode(nsAttr);
      newEl.setAttribute("type", "ctr" + ctr + ":" + type.getLocalPart());
      
    }
    
    schemaRoot.getDocumentElement().appendChild(newEl);
    
  }

  /**
   * @return Returns the classes.
   */
  public Class<?>[] getUsedClasses() {
    return classes;
  }

  /**
   * @return Returns the sch.
   */
  public DOMSource[] getSchemaSources() {
    return sch;
  }

}

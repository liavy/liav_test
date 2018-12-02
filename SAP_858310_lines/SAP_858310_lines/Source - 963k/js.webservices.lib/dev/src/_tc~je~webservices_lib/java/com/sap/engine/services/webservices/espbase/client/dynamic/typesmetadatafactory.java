package com.sap.engine.services.webservices.espbase.client.dynamic;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.transform.Source;

import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import com.sap.engine.lib.schema.components.Loader;
import com.sap.engine.lib.schema.components.Schema;
import com.sap.engine.lib.schema.components.SchemaLoaderFactory;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.impl.MetadataLoader;
import com.sap.engine.services.webservices.jaxrpc.encoding.ExtendedTypeMapping;
import com.sap.engine.services.webservices.jaxrpc.encoding.TypeMappingRegistryImpl;
import com.sap.engine.services.webservices.jaxrpc.schema2java.SchemaToJavaConfig;
import com.sap.engine.services.webservices.jaxrpc.schema2java.SchemaToJavaGeneratorException;
import com.sap.engine.services.webservices.jaxrpc.schema2java.SchemaToJavaGeneratorNew;

public class TypesMetadataFactory {

  private Loader xsdLoader;
  
  private final static TypesMetadataFactory singletonFactory = new TypesMetadataFactory();
  
  private TypesMetadataFactory() {
    xsdLoader = SchemaLoaderFactory.create();
  }
  
  public static TypesMetadataFactory newInstance() {
    return(singletonFactory);
  }
  
  private Loader initXSDLoader(TypesFactoryConfig typesFactoryConfig) {
    xsdLoader.setEntityResolver(typesFactoryConfig.getEntityResolver());
    xsdLoader.setUriResolver(typesFactoryConfig.getURIResolver());
    return(xsdLoader);
  }
  
  public ExtendedTypeMapping createTypes(String xsdURI, TypesFactoryConfig typesFactoryConfig) throws Exception {
    return(createTypes(initXSDLoader(typesFactoryConfig).load(xsdURI), typesFactoryConfig));
  }

  public ExtendedTypeMapping createTypes(String[] xsdURIs, TypesFactoryConfig typesFactoryConfig) throws Exception {
    return(createTypes(initXSDLoader(typesFactoryConfig).load(xsdURIs), typesFactoryConfig));
  }

  public ExtendedTypeMapping createTypes(Node xsdNode, TypesFactoryConfig typesFactoryConfig) throws Exception {
    return(createTypes(initXSDLoader(typesFactoryConfig).load(xsdNode), typesFactoryConfig));
  }
  
  public ExtendedTypeMapping createTypes(Node[] xsdNodes, TypesFactoryConfig typesFactoryConfig) throws Exception {
    return(createTypes(initXSDLoader(typesFactoryConfig).load(xsdNodes), typesFactoryConfig));
  }
  
  public ExtendedTypeMapping createTypes(Source xsdSource, TypesFactoryConfig typesFactoryConfig) throws Exception {
    return(createTypes(initXSDLoader(typesFactoryConfig).load(xsdSource), typesFactoryConfig));
  }
  
  public ExtendedTypeMapping createTypes(Source[] xsdSources, TypesFactoryConfig typesFactoryConfig) throws Exception {
    return(createTypes(initXSDLoader(typesFactoryConfig).load(xsdSources), typesFactoryConfig));
  }

  public ExtendedTypeMapping createTypes(InputStream xsdInputStream, TypesFactoryConfig typesFactoryConfig) throws Exception {
    return(createTypes(initXSDLoader(typesFactoryConfig).load(xsdInputStream), typesFactoryConfig));
  }

  public ExtendedTypeMapping createTypes(InputStream[] xsdInputStreams, TypesFactoryConfig typesFactoryConfig) throws Exception {
    return(createTypes(initXSDLoader(typesFactoryConfig).load(xsdInputStreams), typesFactoryConfig));
  }

  public ExtendedTypeMapping createTypes(InputSource xsdInputSource, TypesFactoryConfig typesFactoryConfig) throws Exception {
    return(createTypes(initXSDLoader(typesFactoryConfig).load(xsdInputSource), typesFactoryConfig));
  }

  public ExtendedTypeMapping createTypes(InputSource[] xsdInputSources, TypesFactoryConfig typesFactoryConfig) throws Exception {
    return(createTypes(initXSDLoader(typesFactoryConfig).load(xsdInputSources), typesFactoryConfig));
  }

  public ExtendedTypeMapping createTypes(File xsdFile, TypesFactoryConfig typesFactoryConfig) throws Exception {
    return(createTypes(initXSDLoader(typesFactoryConfig).load(xsdFile), typesFactoryConfig));
  }

  public ExtendedTypeMapping createTypes(File[] xsdFiles, TypesFactoryConfig typesFactoryConfig) throws Exception {
    return(createTypes(initXSDLoader(typesFactoryConfig).load(xsdFiles), typesFactoryConfig));
  }

  public ExtendedTypeMapping createTypes(Object xsdObj, TypesFactoryConfig typesFactoryConfig) throws Exception {
    return(createTypes(initXSDLoader(typesFactoryConfig).load(xsdObj), typesFactoryConfig));
  }

  public ExtendedTypeMapping createTypes(Object[] xsdObjs, TypesFactoryConfig typesFactoryConfig) throws Exception {
    return(createTypes(initXSDLoader(typesFactoryConfig).load(xsdObjs), typesFactoryConfig));
  }
  
  public ExtendedTypeMapping createTypes(Schema schema, TypesFactoryConfig typesFactoryConfig) throws Exception {
    initOutputDir(typesFactoryConfig);
    TypeMappingRegistryImpl typeMappingRegistry = loadTypesMetadata(schema, typesFactoryConfig);
    deleteOutputDir(typesFactoryConfig);
    return((ExtendedTypeMapping)(typeMappingRegistry.getDefaultTypeMapping()));
  }
  
  private void initOutputDir(TypesFactoryConfig typesFactoryConfig) throws IOException {
    String outputDirPath = typesFactoryConfig.getOutputDir();
    if(outputDirPath == null) {
      throw new IllegalArgumentException("Output directory is not configured.");
    }
    File outputDir = new File(outputDirPath);
    if(!outputDir.exists()) {
      boolean outputDirIsCreatedSuccessfull = outputDir.mkdirs();
      if(!outputDirIsCreatedSuccessfull) {
        throw new IllegalArgumentException("Output directory " + outputDir.getCanonicalPath() + " can not be created.");
      }
    }
  }
  
  private void deleteOutputDir(TypesFactoryConfig typesFactoryConfig) {
    deleteDirContent(new File(typesFactoryConfig.getOutputDir()));
  }
  
  private void deleteDirContent(File dirFile) {
    File[] files = dirFile.listFiles();
    if(files != null) {
      for(int i = 0; i < files.length; i++) {
        File file = files[i];
        if(file.isFile()) {
          file.delete();
        } else if(file.isDirectory()) {
          deleteDirContent(file);
        }
      }
    }
    dirFile.delete();
  }
  
  private TypeMappingRegistryImpl loadTypesMetadata(Schema schema, TypesFactoryConfig typesFactoryConfig) throws Exception {
    SchemaToJavaGeneratorNew schemaToJavaGenerator = new SchemaToJavaGeneratorNew();
    
    SchemaToJavaConfig schemaToJavaCfg = new SchemaToJavaConfig();
    schemaToJavaCfg.setBasePackageName("");
    schemaToJavaCfg.setOutputDir(new File(typesFactoryConfig.getOutputDir()));
    schemaToJavaCfg.setUseGenericContent(true);
    schemaToJavaCfg.setSchema(schema);
    schemaToJavaCfg.setGenerationMode(SchemaToJavaConfig.LOAD_MODE);
    
    schemaToJavaGenerator.generateAll(schemaToJavaCfg);
    
    schemaToJavaCfg.setGenerationMode(SchemaToJavaConfig.FRAMEWORK_MODE);
    //schemaToJavaCfg.setCompile(true);
    //schemaToJavaCfg.setAdditionalClassPath(typesFactoryConfig.getAdditionalClasspath());
    schemaToJavaCfg.getUriToPackageMapping().clear();
    
    schemaToJavaGenerator.generateAll(schemaToJavaCfg);
    
    TypeMappingRegistryImpl typeMappingRegistry = createTypeMappingRegistry(schemaToJavaCfg);
    return(typeMappingRegistry);
  }
  
  private TypeMappingRegistryImpl createTypeMappingRegistry(SchemaToJavaConfig schemaToJavaConfig) throws Exception {
    ByteArrayOutputStream typeSetByteArrayOutput = new ByteArrayOutputStream();
    schemaToJavaConfig.getTypeSet().saveSettings(typeSetByteArrayOutput);
    ByteArrayInputStream typeMappingRegInput = new ByteArrayInputStream(typeSetByteArrayOutput.toByteArray());
    SerializationFRMClassLoader serializationFRMClassLoader = new SerializationFRMClassLoader(getClass().getClassLoader(), schemaToJavaConfig.getOutputDir().getCanonicalPath()); 
    TypeMappingRegistryImpl typeMappingRegistry = new TypeMappingRegistryImpl(typeMappingRegInput, serializationFRMClassLoader);
    MetadataLoader.loadMetaData(typeMappingRegistry, schemaToJavaConfig.getSchema());
    return(typeMappingRegistry);
  }
}

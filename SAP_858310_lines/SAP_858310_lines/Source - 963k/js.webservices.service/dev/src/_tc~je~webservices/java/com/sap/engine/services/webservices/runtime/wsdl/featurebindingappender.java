/**
 * Created by IntelliJ IDEA.
 * User: dimitar-an
 * Date: Nov 20, 2002
 * Time: 1:05:16 PM
 * To change this template use Options | File Templates.
 */
package com.sap.engine.services.webservices.runtime.wsdl;

import com.sap.engine.services.webservices.runtime.definition.FeatureInfo;
import com.sap.engine.services.webservices.runtime.definition.OperationDefinitionImpl;
import com.sap.engine.services.webservices.runtime.definition.ServiceEndpointDefinition;
import com.sap.engine.services.webservices.wsdl.*;

import java.util.ArrayList;
import java.util.Enumeration;

public class FeatureBindingAppender {

  private static final String FEATURE_BASE  =  "_runtime-feature_";
  private static final String FEATURE_NS_PREFIX  =  "fns";
  private static final String TARGETNS_PREFIX  =  "tns";

  /**
   * Updates the definitions with the binding features as well as their corresponding
   * feture declarations.
   *
   * @param definitions  the Object to be updated
   * @param endpoints    endpoints which bindings are defined in the definitions parameter
   *
   * @return updated definitions
   * @throws com.sap.engine.services.webservices.wsdl.WSDLException  if any exception occurs
   */
  public static WSDLDefinitions appendFeatures(WSDLDefinitions definitions, ServiceEndpointDefinition[] endpoints) throws WSDLException {

    WSDLBinding[] bindings;
    com.sap.engine.interfaces.webservices.runtime.OperationDefinition[] operations;
    SAPUseFeature[] useFeatures;
    WSDLBindingOperation bndOperation;

    String tnsPref = setTNSPrefix(definitions);
    int nsNom = 0;

    for (int i = 0; i < endpoints.length; i++) {
      bindings = getBindings(definitions, endpoints[i].getWsdlBindingName().getLocalPart());

      for (int j = 0; j < bindings.length; j++) {
        //adding <sap:Feature> declaration
        nsNom = appendSAPFeatures(endpoints[i], definitions, bindings[j].getName(), nsNom);

        //adding <useSapFeature> for global features
        useFeatures = createUseFeatures(bindings[j].getName(), null, (FeatureInfo[])endpoints[i].getFeaturesChain(), tnsPref);
        if (useFeatures != null) {
          for (int f = 0; f < useFeatures.length; f++) {
            bindings[j].addUseFeature(useFeatures[f]);
          }
        }

        //adding <useSapFeature> for operations featrues
        operations = endpoints[i].getOperations();
        for (int op = 0; op < operations.length; op++) {
          useFeatures = createUseFeatures(bindings[j].getName(), operations[op].getOperationName(), ((OperationDefinitionImpl) operations[op]).getFeaturesChain(), tnsPref);
          bndOperation = bindings[j].getOperation(operations[op].getOperationName());

          if (bndOperation == null) {
            continue;
            //throw new WSDLException("Could not find operation '" + operations[op] + "' in binding '" + bindings[j] + "'");
          }

          //adding features in operation
          if (useFeatures != null) {
            for (int f = 0; f < useFeatures.length; f++) {
              bndOperation.addUseFeatire(useFeatures[f]);
            }
          }
        } //end operations' for
      } //end bindings' for
    } //end endpoints's for

    return definitions;
  }

  /**
   * Creating and adding SAPFeature objects to the definitions
   */
  private static int appendSAPFeatures(ServiceEndpointDefinition endpoint, WSDLDefinitions def, String bindingName, int fnsNom) {

    SAPFeature sapFeature;
    String fLable;
    FeatureInfo[] features;

    features = (FeatureInfo[])endpoint.getFeaturesChain();

    //if clobal features are available
    if (features != null) {
      //trversing global features
      for (int j = 0; j < features.length; j++) {
        fLable = getFeatureLable(bindingName, null, j);

        sapFeature = new SAPFeature();
        sapFeature.setName(fLable);
        sapFeature.setUri(features[j].getFeatureName());

        //adding a prefix for feature uri
        String prefix = getPrefixForUri(def, features[j].getFeatureName());
        if (prefix == null) {
          prefix = FEATURE_NS_PREFIX + (fnsNom++);
        }
        def.addAdditionalAttribute("xmlns:" + prefix, features[j].getFeatureName());

        def.addFeature(loadProperties(sapFeature, features[j].getConfiguration(), prefix));
      }
    }

    //traversing operations
    com.sap.engine.interfaces.webservices.runtime.OperationDefinition[] operations = endpoint.getOperations();
    for (int j = 0; j < operations.length; j++) {
      features = ( (OperationDefinitionImpl) operations[j]).getFeaturesChain();

      //the operation has no features specified
      if (features == null || features.length == 0) {
        continue;
      }

      //traversing features
      for (int f = 0; f < features.length; f++) {
        fLable = getFeatureLable(bindingName, operations[j].getOperationName(), f);

        sapFeature = new SAPFeature();
        sapFeature.setName(fLable);
        sapFeature.setUri(features[f].getFeatureName());

        //adding a prefix for feature uri
        String prefix = getPrefixForUri(def, features[f].getFeatureName());
        if (prefix == null) {
          prefix = FEATURE_NS_PREFIX + (fnsNom++);
        }
        def.addAdditionalAttribute("xmlns:" + prefix, features[f].getFeatureName());

        def.addFeature(loadProperties(sapFeature, features[f].getConfiguration(), prefix));
      }
    }

    return fnsNom;
  }

  public static SAPFeature loadProperties(SAPFeature feature, com.sap.engine.interfaces.webservices.runtime.Config cnf, String fNSPrefix) {

    com.sap.engine.interfaces.webservices.runtime.PropertyDescriptor[] fProperties = cnf.getProperties();
    SAPProperty sapProperty;
    SAPOption sapOption;

    //traversing properties
    for (int p = 0; p < fProperties.length; p++) {
      //if it is supported by the SAP WSDL
      if (fProperties[p].hasSimpleContent() || fProperties[p].hasValueAttrib()) {
        sapProperty = new SAPProperty();
        sapProperty.setQname(new com.sap.engine.lib.xml.util.QName(fNSPrefix, fProperties[p].getPropertyDescriptorName(), feature.getUri()));
        sapOption = new SAPOption();
        if (fProperties[p].hasSimpleContent()) {
          sapOption.setValue(fProperties[p].getSimpleContent());
        } else {
          sapOption.setValue(fNSPrefix + ":" + fProperties[p].getValue());
        }

        sapProperty.addOption(sapOption);
        feature.addProperty(sapProperty);
      }
    }

    return feature;
  }

  private static String getFeatureLable(String bindingName, String operationName, int nom) {
    if (operationName == null) { //global feature
      return bindingName + FEATURE_BASE + nom;
    }
    //operation feature
    return bindingName + "_" + operationName + FEATURE_BASE + nom;
  }

  private static SAPUseFeature[] createUseFeatures(String bindingName, String operationName, FeatureInfo[] features, String tnsPref) {

    if (features == null || features.length == 0) {
      return null;
    }

    SAPUseFeature useF;
    SAPUseFeature res[] = new SAPUseFeature[features.length];

    for (int i = 0; i < res.length; i++) {
      useF = new SAPUseFeature();
      useF.setFeatureQName(new com.sap.engine.lib.xml.util.QName(tnsPref, getFeatureLable(bindingName, operationName, i), features[i].getFeatureName()));
      res[i] = useF;
    }

    return res;
  }

  private static WSDLBinding[] getBindings(WSDLDefinitions definitions, String bindingName) {
    WSDLBinding tmpBinding;
    ArrayList resultBindings = new ArrayList();
    ArrayList bindings = definitions.getBindings();

    for (int i = 0; i < bindings.size(); i++) {
      tmpBinding = (WSDLBinding) bindings.get(i);

      if (tmpBinding.getName().equals(bindingName) || tmpBinding.getName().equals(bindingName + "_document")) {
        resultBindings.add(tmpBinding);
      }
    }

    return (WSDLBinding[]) resultBindings.toArray(new WSDLBinding[resultBindings.size()]);
  }

  public static String getPrefixForUri(WSDLDefinitions def, String ns) {
    Enumeration en = def.getAdditionalAttributes().keys();
    String tmp;

    while (en.hasMoreElements()) {
      tmp = (String) en.nextElement();

      if (tmp.startsWith("xmlns:") && def.getAdditionalAttributes().get(tmp).equals(ns)) {
        return tmp.substring("xmlns:".length());
      }
    }

    return null;
  }

  public static String setTNSPrefix(WSDLDefinitions def) {
    String trgNS = def.getAdditionalAttrValue("targetNamespace"); //in case it is porttype
    if (trgNS == null) { //in case it is binding
      trgNS = def.targetNamespace;
    }
    String pref = getPrefixForUri(def, trgNS);
    if (pref == null) { // no prefix is mapped
      def.addAdditionalAttribute("xmlns:" + TARGETNS_PREFIX, trgNS);
      pref = TARGETNS_PREFIX;
    }

    return pref;
  }
}

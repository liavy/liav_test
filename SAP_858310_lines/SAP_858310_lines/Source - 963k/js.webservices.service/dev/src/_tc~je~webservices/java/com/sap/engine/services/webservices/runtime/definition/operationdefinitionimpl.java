package com.sap.engine.services.webservices.runtime.definition;

import com.sap.engine.interfaces.webservices.runtime.*;

import java.util.Properties;

/**
 * Copyright (c) 2002 by SAP Labs Sofia.,
 * All rights reserved.
 *
 * Description:
 * @author       Dimiter Angelov
 * @version      1.0
 */

public class OperationDefinitionImpl implements OperationDefinition {

  private String uniqueOperationName = null;
  private String javaOperationName;
  private String operationName;
  private FeatureInfo[] featuresChain;

  private boolean isExposed = true;

  private Config generalConfig;
  private Config inputConfig;
  private Config outputConfig;

  private String description;

  private ParameterNode[] inputParams;
  private ParameterNode[] outputParams;
  private Fault[] faults;

  private Key[] keys = null;

  //for WSD Sasho use
  private String inputConfigDefaultNS;
  private String outputConfigDefaultNS;

  public OperationDefinitionImpl() {

  }

  public String getUniqueOperationName() {
    return uniqueOperationName;
  }

  public void setUniqueOperationName(String uniqueOperationName) {
    this.uniqueOperationName = uniqueOperationName;
  }

  public void setJavaOperationName(String javaOperationName) {
    this.javaOperationName = javaOperationName;
  }

  public String getJavaOperationName() {
    return javaOperationName;
  }

  public void setOperationName(String operationName)  {
    this.operationName = operationName;
  }

  public String getOperationName() {
    return operationName;
  }

  public Feature[] getFeatures() {
    return this.featuresChain;
  }

  public void setInputParameters(ParameterNode[] inputParams) {
    this.inputParams = inputParams;
  }

  public ParameterNode[] getInputParameters() {
    return this.inputParams;
  }

  public void setOutputParameters(ParameterNode[] outputParams) {
    this.outputParams = outputParams;
  }

  public ParameterNode[] getOutputParameters() {
    return this.outputParams;
  }

  public String getDescription() {
    return this.description;
  }

  public Feature getFeature(String featureName) {
    if (featureName == null) {
      throw new IllegalArgumentException("Feature name could not be 'null'");
    }
    for (int i = 0; i < featuresChain.length; i++) {
      if (featuresChain[i].getFeatureName().equals(featureName)) {
        return featuresChain[i];
      }
    }
    return null;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setFeaturesChain(FeatureInfo[] featuresChain) {
    this.featuresChain = featuresChain;
  }

  public FeatureInfo[] getFeaturesChain() {
    return  featuresChain;
  }

  public boolean isExposed() {
    return isExposed;
  }

  public void setExposed(boolean exposed) {
    isExposed = exposed;
  }

  public void setGeneralConfiguration(Config generalConfig) {
    this.generalConfig = generalConfig;
  }

  public void setGeneralConfiguration(Properties properties) {
    this.generalConfig = new ConfigImpl(properties);
  }

  public Config getGeneralConfiguration() {
    return this.generalConfig;
  }

  public void setInputOperationConfiguration(Config config) {
    this.inputConfig = config;
  }

  public void setInputOperationConfiguration(Properties properties) {
    this.inputConfig = new ConfigImpl(properties);
  }

  public Config getInputConfiguration() {
    return this.inputConfig;
  }

  public void setOutputOperationConfiguration(Config config) {
    this.outputConfig = config;
  }

  public void setOutputOperationConfiguration(Properties properties) {
    this.outputConfig = new ConfigImpl(properties);
  }

  public Config getOutputConfiguration() {
    return this.outputConfig;
  }

  public void setFaults(Fault[] faults) {
    this.faults = faults;
  }

  public Fault[] getFaults() {
    return this.faults;
  }

  public void setKeys(Key[] keys) {
    this.keys = keys;
  }

  public Key[] getKeys() {
    return keys;
  }

  public String getInputConfigDefaultNS() {
    return inputConfigDefaultNS;
  }

  public void setInputConfigDefaultNS(String inputConfigDefaultNS) {
    this.inputConfigDefaultNS = inputConfigDefaultNS;
  }

  public String getOutputConfigDefaultNS() {
    return outputConfigDefaultNS;
  }

  public void setOutputConfigDefaultNS(String outputConfigDefaultNS) {
    this.outputConfigDefaultNS = outputConfigDefaultNS;
  }

  public String toString() {
    String result = "";
    result += "operation name: '" + operationName + "'\n";
    result += "javaOperation name: '" + javaOperationName + "'\n";
    if (keys != null) result += "Keys: \n" + toString(keys) + "\n";
    if (inputParams != null) result += "InputParams: \n" + toString(inputParams) + "\n";
    if (outputParams != null) result += "OutputParams: \n" + toString(outputParams) + "\n";
    if (faults != null) result += "Faults: \n" + faults.toString() + "\n";
    return result;
  }

  public String toString(Object[] objArray) {
    String result = "";
    if (objArray == null) return result;
    int length = objArray.length;
    for (int i = 0; i < length; i++)
      result += "element[" + i + "] = " + objArray[i].toString();
    return result;
  }

}
package com.sap.engine.services.userstore.filter;

import com.sap.engine.interfaces.security.userstore.spi.FilterPassword;
import com.sap.engine.interfaces.security.userstore.spi.FilterUsername;

import com.sap.engine.frame.core.configuration.Configuration;
import com.sap.engine.services.userstore.exceptions.BaseSecurityException;

public class FilterStorage {

  public static final String FILTER_CONFIG_PATH = "filter";
  public static final String USER_FILTER_CONFIG_PATH = FILTER_CONFIG_PATH + "/username";
  public static final String PASSWORD_FILTER_CONFIG_PATH = FILTER_CONFIG_PATH + "/password";

  public final static String CASE_SENSITIVITY_PROP = "CASE_SENSITIVITY";
  public final static String DIGITS_USAGE_PROP = "DIGITS_USAGE";
  public final static String MAXIMUM_LENGTH_PROP = "MAXIMUM_LENGTH";
  public final static String MINIMUM_LENGTH_PROP = "MINIMUM_LENGTH";
  public final static String SPACES_USAGE_PROP = "SPACES_USAGE";

  public FilterStorage() {
  }

  public FilterUsername getFilterUsername(Configuration configuration) {
    try {
      return new FilterUsernameImpl(configuration, false);
    } catch (Exception ex) {
      throw new BaseSecurityException(BaseSecurityException.GET_FILTER_USERNAME_ERROR, ex);
    }
  }

  public FilterPassword getFilterPassword(Configuration configuration) {
    try {
      return new FilterPasswordImpl(configuration, false);
    } catch (Exception ex) {
      throw new BaseSecurityException(BaseSecurityException.GET_FILTER_PASSWORD_ERROR, ex);
    }
  }

  public void setFilterPassword(FilterPassword filter, Configuration configuration) {
    try {
      configuration.modifyConfigEntry(CASE_SENSITIVITY_PROP, new Integer(filter.getRestriction(FilterUsername.CASE_SENSITIVITY)));
      configuration.modifyConfigEntry(DIGITS_USAGE_PROP, new Integer(filter.getRestriction(FilterUsername.DIGITS_USAGE)));
      configuration.modifyConfigEntry(MAXIMUM_LENGTH_PROP, new Integer(filter.getRestriction(FilterUsername.MAXIMUM_LENGTH)));
      configuration.modifyConfigEntry(MINIMUM_LENGTH_PROP, new Integer(filter.getRestriction(FilterUsername.MINIMUM_LENGTH)));
      configuration.modifyConfigEntry(SPACES_USAGE_PROP, new Integer(filter.getRestriction(FilterUsername.SPACES_USAGE)));
    } catch (Exception ex) {
      throw new BaseSecurityException(BaseSecurityException.SET_FILTER_PASSWORD_ERROR, ex);
    }
  }

  public void setFilterUsername(FilterUsername filter, Configuration configuration) {
    try {
      configuration.modifyConfigEntry(CASE_SENSITIVITY_PROP, new Integer(filter.getRestriction(FilterUsername.CASE_SENSITIVITY)));
      configuration.modifyConfigEntry(DIGITS_USAGE_PROP, new Integer(filter.getRestriction(FilterUsername.DIGITS_USAGE)));
      configuration.modifyConfigEntry(MAXIMUM_LENGTH_PROP, new Integer(filter.getRestriction(FilterUsername.MAXIMUM_LENGTH)));
      configuration.modifyConfigEntry(MINIMUM_LENGTH_PROP, new Integer(filter.getRestriction(FilterUsername.MINIMUM_LENGTH)));
      configuration.modifyConfigEntry(SPACES_USAGE_PROP, new Integer(filter.getRestriction(FilterUsername.SPACES_USAGE)));
    } catch (Exception ex) {
      throw new BaseSecurityException(BaseSecurityException.SET_FILTER_USERNAME_ERROR, ex);
    }
  }
}

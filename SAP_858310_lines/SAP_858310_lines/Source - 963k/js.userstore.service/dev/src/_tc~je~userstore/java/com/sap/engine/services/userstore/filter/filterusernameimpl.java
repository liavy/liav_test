package com.sap.engine.services.userstore.filter;

import com.sap.engine.interfaces.security.userstore.spi.FilterUsername;
import com.sap.engine.frame.core.configuration.Configuration;

public class FilterUsernameImpl implements FilterUsername {
  private int case_sensitivity = USAGE_ALLOWED;
  private int digits_usage = USAGE_ALLOWED;
  private int maximum_length = 20;
  private int minimum_length = 1;
  private int spaces_allowed = USAGE_ALLOWED;

  /**
   *  Constructor
   *
   */
  public FilterUsernameImpl() {

  }

  public FilterUsernameImpl(Configuration configuration, boolean writeAccess) throws Exception {
    if (writeAccess) {
      configuration.addConfigEntry(FilterStorage.CASE_SENSITIVITY_PROP, new Integer(case_sensitivity));
      configuration.addConfigEntry(FilterStorage.DIGITS_USAGE_PROP, new Integer(digits_usage));
      configuration.addConfigEntry(FilterStorage.MAXIMUM_LENGTH_PROP, new Integer(maximum_length));
      configuration.addConfigEntry(FilterStorage.MINIMUM_LENGTH_PROP, new Integer(minimum_length));
      configuration.addConfigEntry(FilterStorage.SPACES_USAGE_PROP, new Integer(spaces_allowed));
    } else {
      case_sensitivity = ((Integer) configuration.getConfigEntry(FilterStorage.CASE_SENSITIVITY_PROP)).intValue();
      digits_usage = ((Integer) configuration.getConfigEntry(FilterStorage.DIGITS_USAGE_PROP)).intValue();
      maximum_length = ((Integer) configuration.getConfigEntry(FilterStorage.MAXIMUM_LENGTH_PROP)).intValue();
      minimum_length = ((Integer) configuration.getConfigEntry(FilterStorage.MINIMUM_LENGTH_PROP)).intValue();
      spaces_allowed = ((Integer) configuration.getConfigEntry(FilterStorage.SPACES_USAGE_PROP)).intValue();
    }
  }

  /**
   *  Converts the encoded password filter to a PasswordFilter instance.
   *
   * @param  data  the encoding of the filter.
   */
  public FilterUsername deserialize(byte[] data) {
    FilterUsername filter = new FilterUsernameImpl();
    for (int i = 0; i < 5; i++) {
      filter.setRestriction(i, data[i]);
    }
    return filter;
  }

  /**
   *  Tests a password for the restrictions. The method returns silently on a
   * correct password.
   *
   * @param   username  String
   *
   */

  public boolean filterUsername(String username) {
    if ((username == null)  || (username.length() > maximum_length) || (username.length() < minimum_length)) {
      return false;
    }

    if (digits_usage == USAGE_FORBIDDEN) {
      for (int i = 0; i < username.length(); i++) {
        if (Character.isDigit(username.charAt(i))) {
          return false;
        }
      }
    } else if (digits_usage == USAGE_MANDATORY) {
      boolean isOK = false;
      for (int i = 0; i < username.length(); i++) {
        if (Character.isDigit(username.charAt(i))) {
          isOK = true;
          break;
        }
      }
      if (!isOK) {
        return false;
      }
    }

    if (spaces_allowed == USAGE_FORBIDDEN) {
      for (int i = 0; i < username.length(); i++) {
        if (Character.isWhitespace(username.charAt(i))) {
          return false;
        }
      }
    } else if (spaces_allowed == USAGE_MANDATORY) {
      boolean isOK = false;
      for (int i = 0; i < username.length(); i++) {
        if (Character.isWhitespace(username.charAt(i))) {
          isOK = true;
          break;
        }
      }
      if (!isOK) {
        return false;
      }
    }
    return true;
  }

  /**
   *  Generates a random password with the restrictions.
   *
   * @return   a char[] for password
   */

  public String generateUsername() {
    java.util.Random rand = new java.util.Random();
    int length;
    int a = (int)'a';
    int no = (int)'0';

    while ((length = rand.nextInt(maximum_length)) < minimum_length) {
    }

    char[] ch = new char[length];
    int pos1 = -1;
    int pos2 = -1;

    if (digits_usage == USAGE_MANDATORY) {
      pos1 = rand.nextInt(length-1);
      ch[pos1] = (char)(rand.nextInt(10) + no);
    }

    if (spaces_allowed == USAGE_MANDATORY) {
      while((pos2 = rand.nextInt(length-1)) != pos1) {
        ch[pos2] = ' ';
      }
    }

    for (int i = 0; i < length; i++) {
      if (i != pos1 && i != pos2) {
        ch[i] = (char)(rand.nextInt(25) + a);
      }
    }

    return new String(ch);
  }

  /**
   *  Returns the restriction for the passwords.
   *
   * @param  restriction   the restriction type ( SPACES_USAGE, MAXIMUM_LENGTH, etc.).
   *
   * @return  the type of the restriction ( USAGE_ALLOWED, 16, etc.).
   */
  public int getRestriction(int restriction) {
    switch (restriction) {
      case CASE_SENSITIVITY: {
        return case_sensitivity;
      }
      case  DIGITS_USAGE: {
        return digits_usage;
      }
      case  MAXIMUM_LENGTH: {
        return maximum_length;
      }
      case  MINIMUM_LENGTH: {
        return minimum_length;
      }
      case  SPACES_USAGE: {
        return spaces_allowed;
      }
      default: return -1;
    }
  }

  /**
   *  Converts the password filter to a byte array.
   *
   * @return  the encoding of the filter.
   */

  public byte[] serialize() {
    return new byte[] {(byte) case_sensitivity, (byte) digits_usage, (byte) maximum_length, (byte) minimum_length, (byte) spaces_allowed};
  }

  /**
   *  Specifies a restriction for the passwords.
   *
   * @param  restriction  the restriction type ( SPACES_USAGE, MAXIMUM_LENGTH, etc.).
   * @param  usage        the restriction value ( USAGE_ALLOWED, 16, etc.).
   */

  public void setRestriction(int restriction, int usage) {
    switch (restriction) {
      case  CASE_SENSITIVITY: {
        if (usage >= 0 && usage <= 4) {
          this.case_sensitivity = usage;
        }
        break;
      }
      case  DIGITS_USAGE: {
        if (usage >= 0 && usage <= 4) {
          digits_usage = usage;
        }
        break;
      }
      case  MAXIMUM_LENGTH: {
        if (usage <= 255 && usage >= minimum_length) {
          maximum_length = usage;
        }
        break;
      }
      case  MINIMUM_LENGTH: {
        if (usage >= 0 && usage <= maximum_length) {
          minimum_length = usage;
        }
        break;
      }
      case  SPACES_USAGE: {
        if (usage >= 0 && usage <= 4) {
          spaces_allowed = usage;
        }
      }
    }
  }

  /**
   *  Returns an array of printable strings describing the restriction. Each String
   * is a short description of the restriction with constant equal to the array index.
   * The array should be at least of lengh 5 because the use of the above declared
   * restrictions is mandatory.
   *
   * @return  the restriction types information.
   */

  public String[] getRestrictionsInfo() {
    return new String[] {
      "case_sensitive", "digits_usage", "maximum_length",
      "minimum_length", "spaces_usage"};
  }

  /**
   *  Returns an array of printable strings describing the valid values of restrictions.
   * Each String is a short description of the usage with constant equal to the array index.
   * The array should be at least of lengh 4 because the use of the above declared
   * restrictions is mandatory.
   *
   * @return  the restriction types information.
   */
  public String[] getUsageInfo() {
    return new String[]{"usage_allowed" , "usage_forbidden", "usage_ignored", "usage_mandatory"};
  }

  public int hashCode() {
    //The method added for JLin Prio1 compatibility. It used to work with 
    // return super.hashCode()
    // so if you need performance - uncomment previous line (and comment the following one) 
    return getClass().getName().hashCode();
  }

  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }

    FilterUsername filter = null;
    try {
      filter = (FilterUsername) obj;
    } catch (Exception e) {
      return false;
    }

    for (int i = 0; i < 5; i++) {
      if (filter.getRestriction(i) != this.getRestriction(i)) {
        return false;
      }
    }
    return true;
  }

}

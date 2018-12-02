/*
 * Created on Apr 23, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.sap.engine.interfaces.webservices.uddi;

import java.io.Serializable;

/**
 * @author Administrator
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class UserAccount implements Serializable {
  private String username = "";
  private String fullName = "";
  private String language = "";
  private String phone = "";
  private String email = "";
  private String address = "";
  private int level = UDDIServerAdmin.NOT_VALID_USER;
  
  /**
   * @return
   */
  public String getFullName() {
    return fullName;
  }

  /**
   * @return
   */
  public String getLanguage() {
    return language;
  }

  /**
   * @return
   */
  public int getLevel() {
    return level;
  }

  /**
   * @return
   */
  public String getPhone() {
    return phone;
  }

  /**
   * @return
   */
  public String getUsername() {
    return username;
  }

  /**
   * @param string
   */
  public void setFullName(String string) {
    fullName = string;
  }

  /**
   * @param string
   */
  public void setLanguage(String string) {
    language = string;
  }

  /**
   * @param i
   */
  public void setLevel(int i) {
    level = i;
  }

  /**
   * @param string
   */
  public void setPhone(String string) {
    phone = string;
  }

  /**
   * @param string
   */
  public void setUsername(String string) {
    username = string;
  }
  
  
  /**
   * @return
   */
  public String getAddress() {
    return address;
  }

  /**
   * @return
   */
  public String getEmail() {
    return email;
  }

  /**
   * @param string
   */
  public void setAddress(String string) {
    address = string;
  }

  /**
   * @param string
   */
  public void setEmail(String string) {
    email = string;
  }

}

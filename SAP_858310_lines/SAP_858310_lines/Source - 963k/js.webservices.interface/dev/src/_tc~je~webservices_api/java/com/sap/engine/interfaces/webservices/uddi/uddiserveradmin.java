/*
 * Created on Apr 22, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.sap.engine.interfaces.webservices.uddi;

/**
 * @author Administrator
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public interface UDDIServerAdmin {
  public static final String JNDI_NAME = "UDDI";
  
  public static final int NOT_VALID_USER = -1;
  public static final int LEVEL_PUB_TIER1 = 1;
  public static final int LEVEL_PUB_TIERN = 2;
  public static final int LEVEL_ADMIN = 3; 
   
  /**
   * Register an UDDIServer inside the J2EE Engine.
   * This method has to be invoked on the startup of the UDDI Server Application
   * @param server The UDDI Server itself
   */
  public void registerUDDIServer(UDDIServer server);
  
  /**
   * Unregister the registerred UDDI Server
   * This method has to be invoked on the stopping of the UDDI Server Application 
   */
  public void unregisterUDDIServer();
  
  /**
   * @return The UDDI Server that has been registered or <null> if no server has been registered yet
   */
  public UDDIServer getUDDIServer();
  
  /**
   * A method for getting the level of the user
   * @param username
   * @return LEVEL_PUB_TIER1 if the user has rights to publish 1 business entity,
   * LEVEL_PUB_TIERN if the user has rights to publish 5 business entities, 
   * LEVEL_ADMIN if the user has UDDI admin rights,
   * NOT_VALID_USER if the username that is specified is not used for the UDDI at all. 
   */
  public int getUserLevel(String username);
  
  /**
   * A method for validating if the specified user with the specified password 
   * is a valid user for the UDDI
   * @param username The username
   * @param password The password. Due to security reasons, passwords should not be stored as strings, but as char arrays 
   * @return true if the specified user is a valid one. 
   */
  public boolean isValidUser(String username, char[] password);
  
  /**
   * A method for creating a new user
   * @param username The username
   * @param password The password. Due to security reasons, passwords should not be stored as strings, but as char arrays
   * @param level The level of the user
   * @throws UserExistsException If the specified username already exists in the user store
   */
  public void createNewUser(String username, char[] password, int level) throws UserExistsException;

  /**
   * A method for deleting a user from the user store
   * @param username The Username
   * @throws UserDoesNotExistException If the specified user does not exist 
   */
  public void deleteUser(String username) throws UserDoesNotExistException;
  
  /**
   * A method for changing the current level of a user
   * @param username The username
   * @param level The new level of the user
   * @throws UserDoesNotExistsException If the specified username does not exist in the user store
   */
  public void changeUserLevel(String username, int level) throws UserDoesNotExistException;
  
  /**
   * A method for getting all the users with the specified level.
   * @param level The level
   * @return An array of usernames
   */
  public String[] getUsersOfLevel(int level);
}

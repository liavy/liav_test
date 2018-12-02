/**
 * Property of SAP AG, Walldorf
 * (c) Copyright SAP AG, Walldorf, 2000-2002.
 * All rights reserved.
 */
package com.sap.engine.interfaces.shell;

/**
 * Main interface of the shell interface. Enables users to add and
 * to remove commands.
 */
public interface ShellInterface {

  /**
   * Registers service's commands that will be accessible through the shell
   *
   * @param  commands  Array or commands
   * @return  Handle of the group of commands
   */
  public int registerCommands(Command[] commands);


  /**
   * Unregisters the commands from the group with a given handle
   *
   * @param  id   Handle of the command group
   */
  public void unregisterCommands(int id);

}


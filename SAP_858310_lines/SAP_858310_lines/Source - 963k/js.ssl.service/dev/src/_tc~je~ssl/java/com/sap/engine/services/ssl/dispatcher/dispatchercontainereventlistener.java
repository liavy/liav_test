package com.sap.engine.services.ssl.dispatcher;

import com.sap.engine.frame.container.event.ContainerEventListener;
import com.sap.engine.frame.container.registry.ObjectRegistry;
import com.sap.engine.interfaces.shell.ShellInterface;
import com.sap.engine.interfaces.shell.Command;

import java.util.Properties;

public class DispatcherContainerEventListener implements ContainerEventListener {

  private int commandsId = -1;
  private ShellInterface shell = null;
  private Command[] commands = null;


  private static final String SHELL_INTERFACE_NAME = "shell";

  public DispatcherContainerEventListener(Command[] commands, ObjectRegistry registry) {
    this.commands = commands;
    Object impl = registry.getProvidedInterface(SHELL_INTERFACE_NAME);
    if (impl != null) {
      interfaceAvailable(SHELL_INTERFACE_NAME, impl);
    }
  }

  public void containerStarted() {
  }

  public void beginContainerStop() {
  }

  public void serviceStarted(String serviceName, Object serviceInterface) {
  }

  public void serviceNotStarted(String serviceName) {
  }

  public void beginServiceStop(String serviceName) {
  }

  public void serviceStopped(String serviceName) {
  }

  public void interfaceAvailable(String interfaceName, Object interfaceImpl) {
    if (interfaceName.equals(SHELL_INTERFACE_NAME)) {
      shell = (ShellInterface) interfaceImpl;
      commandsId = shell.registerCommands(commands);
    }
  }

  public void interfaceNotAvailable(String interfaceName) {
    if (interfaceName.equals(SHELL_INTERFACE_NAME)) {
      if ((shell != null) && (commandsId >= 0)) {
        shell.unregisterCommands(commandsId);
        commandsId = -1;
      }
    }
  }

  public void markForShutdown(long timeout) {
  }

  public boolean setServiceProperty(String key, String value) {
    return false;
  }

  public boolean setServiceProperties(Properties serviceProperties) {
    return false;
  }

  public void stop() {
    if ((shell != null) && (commandsId >= 0)) {
      shell.unregisterCommands(commandsId);
      commandsId = -1;
    }
  }

}


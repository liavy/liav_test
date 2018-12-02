package com.sap.tests.utils;

import com.sap.engine.interfaces.transaction.RMContainer;
import com.sap.engine.interfaces.transaction.RMContainerRegistry;
import com.sap.engine.services.ts.recovery.RMContainerRegistryImpl;

public class Registrator implements Runnable {
	
	private static RMContainerRegistry rmContainerRegistry = null;
	private String name = null;
	private RMContainer c = null;
	private int val = 0;
	private boolean registered = true;
	private boolean unregistered = true;
	private static String[] names = null;
	private static RMContainer[] containers = null;
	
	public Registrator(String name, RMContainer c, int val, RMContainerRegistry r) {
		rmContainerRegistry = r;
		this.name = name;
		this.c = c;
		this.val = val;
	}

	public void run() {
		switch(val) {
			case 1: register(name, c); break;
			case 2: unregister(name); break;
			default: System.out.println("Error");
		}
	}
	
	public void register(String name, RMContainer c) {
		rmContainerRegistry.registerRMContainer(name, c);
	}
	
	public void unregister(String name) {
		rmContainerRegistry.unregisterRMContainer(name);
	}
	
	/**
	 * Method which checks if everything is correctly registed into RMContainerRegistry
	 * @param name
	 * @param c
	 */
	public void checkRegister(String name, RMContainer c) {
		RMContainerRegistryImpl impl = (RMContainerRegistryImpl)rmContainerRegistry;
		synchronized (rmContainerRegistry) {
			for(int i = 0; i < names.length; i++) {
				if(impl.getRMContainerByName(names[i]) != containers[i]) { /// or equals
					registered = false;
					break;
				}
			}
		}
	}
	
	/**
	 * Method which checks if everything is correctly unregistered from RMContainerRegistry
	 * @param name
	 */
	public void checkUnregister(String name) {
		RMContainerRegistryImpl impl = (RMContainerRegistryImpl)rmContainerRegistry;
		synchronized (rmContainerRegistry) {
			for(int i = 0; i < names.length; i++) {
				if(impl.getRMContainerByName(names[i]) != null) {
					unregistered = false;
					break;
				}
			}
		}
	}
	
	
	
	/**
	 * @return the names
	 */
	public String[] getNames() {
		return names;
	}

	/**
	 * @return the containers
	 */
	public static RMContainer[] getContainers() {
		return containers;
	}

	/**
	 * @param names the names to set
	 */
	public static void setNames(String[] names1) {
		Registrator.names = names1;
	}

	/**
	 * @param containers the containers to set
	 */
	public static void setContainers(RMContainer[] containers) {
		Registrator.containers = containers;
	}

	/**
	 * @return the checkRegister
	 */
	public boolean isRegistered() {
		return registered;
	}

	/**
	 * @return the checkUnregister
	 */
	public boolean isUnregistered() {
		return unregistered;
	}

	/**
	 * @param checkRegister the checkRegister to set
	 */
	public void setRegistered(boolean checkRegister) {
		this.registered = checkRegister;
	}

	/**
	 * @param checkUnregister the checkUnregister to set
	 */
	public void setUnregistered(boolean checkUnregister) {
		this.unregistered = checkUnregister;
	}
	

}

package com.sap.tests;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


import static junit.framework.Assert.*;

import com.sap.engine.interfaces.transaction.RMContainer;
import com.sap.engine.interfaces.transaction.RMContainerRegistry;
import com.sap.engine.services.ts.recovery.RMContainerRegistryImpl;
import com.sap.tests.utils.MyRMContainer;
import com.sap.tests.utils.Registrator;

public class ContainerRegistryTest {
	
	private static RMContainerRegistry rmContainerRegistry = null;
	private static String[] names = null;
	private static RMContainer[] containers = null;
	private static int count = 5;
	
	@BeforeClass
	public static void init() {
		rmContainerRegistry = new RMContainerRegistryImpl();
		containers = new MyRMContainer[count];
		names = new String[count];
		for(int i = 0 ; i < count; i++) {
			names[i] = "MyContainer" + i;
			containers[i] = new MyRMContainer();
		}
	}
	
	@Test
	public void testRegister() throws InterruptedException {
		RMContainerRegistryImpl impl = (RMContainerRegistryImpl)rmContainerRegistry;
		
		Registrator[] reg = new Registrator[count];
		for(int i  = 0 ; i < count; i++) {
			reg[i] = new Registrator(names[i], containers[i], 1, rmContainerRegistry);
			new Thread(reg[i]).start();
			Thread.sleep(1000);
		}
		
	
//		for(int i = 0; i < count; i++) {
//			assertEquals(containers[i], impl.getRMContainerByName(names[i]));
//		}
		
		for(int i = 0; i < count; i++) {
			reg[i].setNames(names);
			reg[i].setContainers(containers);
			reg[i].checkRegister(names[i], containers[i]);
			assertEquals(true, reg[i].isRegistered());
		}
		
	}
	
	@Test
	public void testUnregister() throws InterruptedException {
//		RMContainerRegistryImpl impl = (RMContainerRegistryImpl)rmContainerRegistry;
		
		Registrator[] reg = new Registrator[count];
		for(int i  = 0 ; i < count; i++) {
			reg[i] = new Registrator(names[i], containers[i], 2, rmContainerRegistry);
			new Thread(reg[i]).start();
			Thread.sleep(1000);
		}
	
//		for(int i = 0; i < count; i++) {
//			assertEquals(null, impl.getRMContainerByName(names[i]));
//		}
		
		for(int i = 0; i < count; i++) {
			reg[i].setNames(names);
			reg[i].setContainers(containers);
			reg[i].checkUnregister(names[i]);
			assertEquals(true, reg[i].isUnregistered());
		}

	}
	
	@AfterClass
	public static void finallize() {
		rmContainerRegistry = null;
		names = null;
		containers = null;
	}
	
	

}

package com.sap.tests.utils.standaloneTMTests.oneAndTwoPhase;

import javax.transaction.Synchronization;

public class DummySynchronizationListener implements Synchronization{

	public void afterCompletion(int arg0) {
		System.out.println("AfterCompletion called.");
	}

	public void beforeCompletion() {
		System.out.println("BeforeCompletion called.");
	}

}

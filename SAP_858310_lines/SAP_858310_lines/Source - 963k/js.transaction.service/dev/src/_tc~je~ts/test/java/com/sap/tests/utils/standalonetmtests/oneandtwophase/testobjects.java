package com.sap.tests.utils.standaloneTMTests.oneAndTwoPhase;

import java.util.concurrent.atomic.AtomicInteger;

public class TestObjects {

	public static AtomicInteger start = new AtomicInteger(0);
	public static AtomicInteger end = new AtomicInteger(0);
	public static AtomicInteger prepare = new AtomicInteger(0);
	public static AtomicInteger commit = new AtomicInteger(0);
	public static AtomicInteger rollback = new AtomicInteger(0);
	public static int startFlag = Integer.MIN_VALUE;
	
}

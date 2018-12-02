package com.sap.dictionary.database.temptests;

import java.io.File;

public class TestFiles {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		File ff = 
			new File("C:\\eclipseProjects30\\tableDefinition1");
		System.out.println("is directory? " + ff.isDirectory());
		System.out.println("is file? " + ff.isFile());
		if (ff.isDirectory()) {
			String[] list = ff.list();
			for (int i = 0; i < list.length; i++) {
	      System.out.println(list[i]);
      }
		}
			

	}

}

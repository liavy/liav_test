/*
 * Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *
 *  History:
 *  Date        ID                      Description
 *  ----------  ----------------------  ----------------------------------------------
 *  2003/01/18  Balu Sthanikam, Oracle  Created
 *                        
 *
 * Generate customer names by pairing Car Manufacturer names and 
 * names from an array.
 * @author Balu Sthanikam
 */

package org.spec.jappserver.driver;

/**
 * Generates customer names.
 */
public class CustHelper {

	static final String names[] = { "Aca", "Adamson", "Appajodu", "Arnold",
                                    "Barnes", "Basler", "Beer", "Bhaskar",
                                    "Brebner", "Buch", "Butera", "Carole",
                                    "Chak", "Chapman", "Chen", "Chen", "Coha",
                                    "Daly", "Dauria", "Dixit", "Donley", "Eric",
                                    "Evan", "Fisher", "Gombold", "Gray",
                                    "Harlan", "Hemant", "Hogstrom", "Honghua",
                                    "Jacob", "Jason", "Judy", "Kalyan",
                                    "Khawaja", "Kounev", "Krishnamurthy",
                                    "Lemerre", "Loen", "Marlow", "Mensah",
                                    "Mueller", "Nick", "Nigam", "Park",
                                    "Pierantoni", "Prasad", "Pyda", "Raj",
                                    "Rajiv", "Ramesh", "Realmuto", "Ricardo",
                                    "Rick", "Russell", "Rye", "Sam", "Satyajit",
                                    "Schoof", "Scot", "Silverman", "Smith",
                                    "Spyker", "Stan", "Stancox", "Stecher",
                                    "Steven", "Sthanikam", "Troop", "Weicker",
                                    "Wulf" , "Zeier" };

    /**
     * Get the idx'th name in the array. Starts from 0.
     * @param idx The index
     * @return The name
     * @throws Exception If any exception occurred
     */
	public static String getName(int idx) throws Exception {

		// get the year
		if (idx < 0) 
			throw new Exception("Ilegal index specified for Last name:" + idx );

		idx = idx % names.length;

		return new String(names[idx]);
	}

    /**
     * Gets the total number of unique names we have.
     * @return Total number of unique names
     */
	public static int getNumNames() {
		return names.length;
	}

	/**
     * Test main method for this class.
     * @param args The command line arguments
     * @throws Exception An error occurred
     */
    public static void main(String args[]) throws Exception {

		System.out.println("number pf cars = " + CustHelper.getNumNames());

		for(int i = 0; i < 288; i++) 
			System.out.println(i + " th name:" + CustHelper.getName(i));
	}
}

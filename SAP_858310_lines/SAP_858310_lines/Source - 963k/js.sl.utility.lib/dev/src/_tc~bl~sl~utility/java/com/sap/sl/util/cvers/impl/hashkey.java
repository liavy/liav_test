package com.sap.sl.util.cvers.impl;

import com.sap.sl.util.logging.api.SlUtilLogger;

/**
 *  The central class for providing a hash value
 *  used as DB key field for vendor and name of
 *  a component
 *
 *@author     md
 *@created    01. März 2004
 *@version    1.0
 */

public class HashKey {

	private static final SlUtilLogger log = SlUtilLogger.getLogger(HashKey.class.getName());

	/**
	 * Test Test...
	 */
	public static void main(String[] args) {
		String val[] ={
		 "Hallo",
		 "Marco",
		 "Alexander",
		 "Gero",
		 "Michael",
		 "1Michael",
		 "2Michael",
		 "3Michael",
		 "4Michael",
		 "5Michael",
		 "ichbauehiereineganzlangeKomponentediemöglichstvielPlatzbeabspruchensollumdieLängedesHashWerteszutesten",
		};
		for (int i = 0; i < val.length; i++) {
			System.out.println("Value: "+val[i]+" key "+ String.valueOf( defaultHashFunction(val[i]) ));	//$JL-SYS_OUT_ERR$
		}
	}

	/**
	 * Default hash function for a string
	 * 
	 * @param hashKey	String to be hashed 
	 * @return hashVal	The hash integer of the input String
	 */
	static int defaultHashFunction (String hashKey) {
		log.debug("hashKey: " + hashKey);
		char array[] = hashKey.toCharArray();
		int len = array.length;
		int hashVal=0;

		for(int i=0; i < len; i++)
			hashVal = (hashVal<<5) - hashVal + array[i];
		log.debug("hashVal: " + hashVal);
		return hashVal;
	}

	/**
	 * Default hash function for two strings, which will be concatenated
	 * 
	 * @param hashKey1	String to be hashed
	 * @param hashKey2	String to be hashed
	 * @return hashVal	The hash integer of the input String
	 */
	static int defaultHashFunction (String hashKey1, String hashKey2) {
		String hashKey = hashKey1.concat(hashKey2);
		log.debug("concatenated hashKey: " + hashKey);
		return defaultHashFunction(hashKey);
	}

}


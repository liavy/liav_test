package com.sap.dictionary.database.veris;

import java.io.InputStream;
import java.util.Properties;

public class TempTest {
	private static final String propFilename = "com/sap/sql/jdbc/internal/context.properties";
	//private static final String propFilename = "com/sap/sql/jdbc/internal/SapContext.class";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			new TempTest().loadProperties();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public TempTest() {
	}
	
	private void loadProperties() throws Exception {
		InputStream in = null;

   
        Properties prop = new Properties();
        in = getClass().getClassLoader().getResourceAsStream(propFilename);
        if (in == null) {
            throw new Exception("Properties file '" + propFilename
                    + "' not found");
        }
        prop.load(in);
        String url = prop.getProperty("provider.url");
        if (url == null) {
            throw new Exception(
                    "Property 'provider.url' not defined in properties file '"
                    + propFilename + "'");
        }
    
}

	}



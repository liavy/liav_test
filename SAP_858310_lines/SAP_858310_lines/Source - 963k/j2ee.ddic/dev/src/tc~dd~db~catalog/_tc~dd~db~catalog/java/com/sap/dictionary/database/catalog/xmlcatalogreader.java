package com.sap.dictionary.database.catalog;

import com.sap.sql.catalog.*;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.io.*;
import com.sap.dictionary.database.dbs.*;

/**
 * Ueberschrift:   Tests zu jdbpersistency
 * Beschreibung:
 * Copyright:     Copyright (c) 2001
 * Organisation:
 * @author
 * @version 1.0
 */

  public class XmlCatalogReader implements CatalogReader {
    private ClassLoader loader = null;
    private Connection con = null;
	private DbFactory factory = null;
	private static final Location loc = Location.getLocation(XmlCatalogReader.class);
    private static final Category cat = Category.getCategory(Category.SYS_DATABASE,Logger.CATEGORY_NAME);

	   /**
       * Constructs an XML catalog reader with regards to the class loader
       * specified. This means, that the getTable() method will look
       * for a suitable .gdbtable file within this classloader's classpath.
       * 
       * @param loader a class loader
       */
    public XmlCatalogReader(ClassLoader loader) {
        this.loader = loader;
        factory = new DbFactory(Database.ARTIFICIAL);
    }
    

    /**
     * Constructs an XML catalog reader from a given path of directories or 
     * jar files seprarated by path separator
     * 
     * @param path the path
     * @exception java.sql.SQLException if no catalog reader can 
     * be constructed from the given path
     */
  public XmlCatalogReader(String path) throws SQLException {
    loader = getClassLoader(path);
    if (loader == null) {
           throw new SQLException(
                "no catalog reader can be constructed from the path: " + path);
    }
    factory = new DbFactory(Database.ARTIFICIAL);
  }

  public XmlCatalogReader(Connection con) {
    this.con = con;
    try {
    	factory = new DbFactory(con);
    	factory.switchOffAdjustBasicTables();
    } catch (JddException e) {
    	throw JddRuntimeException.createInstance(e,cat,Severity.ERROR,loc);
    }
  }

  public Table getTable(String schemaName, String tableName)
        throws SQLException {
    Table gs = null;
    try {
      if (con != null) {
      	gs  =  new DbGeneralStructure(tableName,con,"dbs",factory);
      }
      else {
        gs  =  new DbGeneralStructure(tableName,loader,factory);
      }  
      if (tableName.compareTo(gs.getName()) != 0) return null;
    }
    catch (JddException ex) {
      //ex.printStackTrace();
      if (ex.getExType() == ExType.SQL_ERROR)
        throw new SQLException(ex.getMessage());
      else if (ex.getExType() == ExType.XML_ERROR) 
        throw new IllegalStateException(ex.getMessage()); 
      else if (ex.getExType() == ExType.NOT_ON_DB)
        return null;  
      else  
        return null;
    }
    return gs;
  }

  public Table getTable(String tableName)
        throws SQLException {
    Table gs = null;
    try {
      if (con != null) {
      	gs  =  new DbGeneralStructure(tableName,con,"dbs",factory);
      }
      else {
        gs = new DbGeneralStructure(tableName,loader,factory);
      }  
      if (tableName.compareTo(gs.getName()) != 0) return null;
    }
    catch (JddException ex) {
      if (ex.getExType() == ExType.SQL_ERROR)
        throw new SQLException(ex.getMessage());
      else if (ex.getExType() == ExType.XML_ERROR) 
        throw new IllegalStateException(ex.getMessage());   
      else if (ex.getExType() == ExType.NOT_ON_DB)
        return null;
      else return null;  
    }
    return gs;
  }

  public boolean existsTable(String tableName)
        throws SQLException {
    return (getTable(tableName) != null);
  }

  public boolean existsTable(String schemaName, String tableName)
        throws SQLException {
    return (getTable(schemaName,tableName) != null);
  }

  public boolean isLogicalCatalogReader() {return true;}
  
      private ClassLoader getClassLoader(String path) {
        ArrayList arrayList = new ArrayList();
        URL[] urls;
        if (path != null) {

            StringTokenizer tokenizer =
                new StringTokenizer(path, java.io.File.pathSeparator);
            while (tokenizer.hasMoreTokens()) {
                String name = tokenizer.nextToken();
                File file = new File(name);
                if (file.exists()) {
                    try {
                        try {
                            file = file.getCanonicalFile();
                        } catch (IOException ex) {
                            throw new IllegalStateException("if the file exists, it must have a canonical path");
                        }

                        arrayList.add(file.toURL());
                    } catch (java.net.MalformedURLException ex) {
                        throw new IllegalStateException("if the file exists, the URL cannot be malformed");
                    }
                } else {
                    // ignore illegal path elements
                }
            }
        }

        urls = new URL[arrayList.size()];
        urls = (URL[]) arrayList.toArray(urls);

        if (urls.length == 0) {
            return null;
        } else {
            return new MyClassLoader(urls);
        }
    }
    
    class MyClassLoader extends URLClassLoader {

        public MyClassLoader(URL[] arg0) {
            super(arg0, null);
        }

        public InputStream getResourceAsStream(String name) {
            URL url = getResource(name);
            if (url != null) {
                try {
                    URLConnection urlConnection = url.openConnection();
                    urlConnection.setUseCaches(false);
                    return urlConnection.getInputStream();
                } catch (IOException e) {
                    return null;
                }
            }
            return null;
        }
    }
    

}
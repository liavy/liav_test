package com.sap.sl.util.dbaccess.impl;

import java.io.IOException;

import com.sap.sl.util.jarsl.api.JarSLIF;

/*
 * Created on 28.06.2004
 * @author d000706
 *
 * creates a file status object
 */
class FileLengthReader
{
  /**
   *  The TCSFactory instance (singleton)
   */
  public static FileLengthReader instance = null;

  private int reading_an_old_file;
  
  private FileLengthReader()
  {
    this.reset();
  }

  /**
   *  Gets the FileLengthReader instance
   *
   *@return    The FileLengthReader instances
   */
  public final static synchronized FileLengthReader getInstance()
  {
    if (instance == null) {
      instance = new FileLengthReader();
    }
    return instance;
  }
  
  public int read_length(JarSLIF jarsl, boolean bereadyforoldfile) throws IOException
  {
    int len = 0;
    
    if (!bereadyforoldfile)
      len = jarsl.getIntData(); // this is a filed which was always exported with a preceeding Int length
    else
    {  
      // unfortunately, ua changed the behavior of the Clobhandler in an incompatibe way on 19.02.2004.
      // Previously, the length information was written as Long, but now it is written as Int.
     
      switch (reading_an_old_file)
      {
        case 0:
          len = jarsl.getIntData();
          break;
        case 1:
          len = (int) jarsl.getLongData();
          break;
        case -1:
          len = jarsl.getIntData();
          if (len == 0)
          {
            len = jarsl.getIntData();
            reading_an_old_file = 1;  // true
          }
          else
            reading_an_old_file = 0;    // false
            
          break;
      }
    }
    return len;
  }
  
  public void reset()
  {
    reading_an_old_file = -1; // initialized with -1 which means "unknown"
  }
}

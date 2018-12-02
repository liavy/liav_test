package com.sap.sl.util.jarsl.api;

import java.io.IOException;

/**
 * @author d030435
 */

public interface JarSLFileStreamHandlerIF {
  /**
   * This method adds a file to the resulting archive. The file content is passed via a stream
   * and the file name is a parameter of this method. It is important that the corresponding .close()
   * methods are used and that only one file stream is open per time. 
   * @param archname file name of the added file in the resulting archive
   * @return JarSLOutputStream object
   * @throws JarSLException
   */
  public abstract JarSLOutputStreamIF addFile(String archname) throws IOException, JarSLException;
  /**
   * This method adds a file and its parent directory names to the resulting archive. The file content 
   * is passed via a stream and the file name is a parameter of this method. It is important that the 
   * corresponding .close() methods are used and that only one file stream is open per time. 
   * @param archname file name of the added file in the resulting archive
   * @return JarSLOutputStream object
   * @throws JarSLException
   */
  public abstract JarSLOutputStreamIF addFileWithParentDirectories(String archname) throws IOException, JarSLException;
  /**
   * Closes the jarsl stream handler and finishes the archives creation process 
   * @throws IOException
   */
  public abstract void close() throws IOException; 
}

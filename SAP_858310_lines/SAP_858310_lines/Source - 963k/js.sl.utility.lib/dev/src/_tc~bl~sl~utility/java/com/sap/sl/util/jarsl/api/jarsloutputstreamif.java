package com.sap.sl.util.jarsl.api;

import java.io.IOException;

/**
 * @author d030435
 */

public interface JarSLOutputStreamIF {
  /**
   * writes b.length bytes to this output stream. 
   * @param b the data
   * @param off the start offset in the data
   * @param len the number of bytes to write
   * @throws IOException
   */
  public abstract void write(byte[] b, int off, int len) throws IOException;
  /**
   * closes the output stream as well as the stream being filtered
   * @throws IOException
   */
  public abstract void close() throws IOException;
}

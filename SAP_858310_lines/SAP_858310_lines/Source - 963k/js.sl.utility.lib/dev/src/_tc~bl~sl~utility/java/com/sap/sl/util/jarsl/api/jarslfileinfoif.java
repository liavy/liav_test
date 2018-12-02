package com.sap.sl.util.jarsl.api;

/**
 * @author d030435
 * This class encapsulates the java.io.File class.
 */

public interface JarSLFileInfoIF {
  public abstract String getName();
  public abstract String getPath();
  public abstract String getParent();
  public abstract boolean isDirectory();
  public abstract boolean exists();
  public abstract boolean isFile();
  public abstract boolean canRead();
}
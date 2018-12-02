package com.sap.archtech.archconn.util;

import java.net.URISyntaxException;

/**
 * This is a very simplistic version of a URI which is
 * based on {@link java.net.URI}. This URI implementation had been 
 * introduced when Java 1.4 was not supported yet.
 * In the meantime the implementation has been changed by delegating 
 * all method invocations to the appropriate methods of {@link java.net.URI}.
 * We only need a small subset of the functionality offered in {@link java.net.URI}.
 * 
 * URIs are immutable and contain only a path.
 */
public class URI
{
  private final java.net.URI jdkUri;
  
  // Hide default constructor
  private URI()
  {
    throw new AssertionError("Default constructor of com.sap.archtech.archconn.util.URI must not be called!");
  } 

  // For internal use only
  private URI(java.net.URI jdkUri)
  {
    if(jdkUri == null)
    {
      throw new AssertionError("Given instance of java.net.URI has unexpected value \"null\"!");
    }
    this.jdkUri = jdkUri;
  }
  
  /**
   * @see java.net.URI#URI(String) 
   */
  public URI(String str)
  {
    try
    {
      jdkUri = new java.net.URI(str);
    }
    catch(URISyntaxException e)
    {
      throw new IllegalArgumentException("Cannot parse " + str + " into a URI; check the documentation of java.net.URI for details", e);
    }
  }

  /**
   * @see java.net.URI#toString()
   */
  public String toString()
  {
    return jdkUri.toString();
  }

  /**
   * @see java.net.URI#normalize()
   */
  public URI normalize()
  {
    return new URI(jdkUri.normalize());
  }

  /**
   * @see java.net.URI#relativize(java.net.URI)
   */
  public URI relativize(URI uri)
  {
    if(uri == null)
    {
      throw new NullPointerException("Given URI is null");
    }
    java.net.URI relativizedJdkUri = jdkUri.relativize(uri.jdkUri);
    return new URI(relativizedJdkUri);
  }

  /**
   * @see java.net.URI#resolve(java.net.URI)
   */
  public URI resolve(URI uri)
  {
    if(uri == null)
    {
      throw new NullPointerException("Given URI is null");
    }
    java.net.URI resolvedJdkUri = jdkUri.resolve(uri.jdkUri);
    return new URI(resolvedJdkUri);
  }

  /**
   * @see java.net.URI#resolve(String)
   */
  public URI resolve(String str)
  {
    if(str == null)
    {
      throw new NullPointerException("Given URI string is null");
    }
    java.net.URI resolvedJdkUri = jdkUri.resolve(str);
    return new URI(resolvedJdkUri);
  }
  
  /**
   * @see java.net.URI#equals(Object)
   */
  public boolean equals(Object other)
  {
    if(other instanceof URI)
    {
      URI otherUri = (URI)other;
      return jdkUri == otherUri.jdkUri; 
    }
    return false;
  }
  
  /**
   * @see java.net.URI#hashCode()
   */
  public int hashCode()
  {
    return jdkUri.hashCode();
  }
}

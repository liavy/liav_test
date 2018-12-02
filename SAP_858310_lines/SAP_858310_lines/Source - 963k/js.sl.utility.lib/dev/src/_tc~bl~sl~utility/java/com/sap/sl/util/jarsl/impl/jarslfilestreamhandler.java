package com.sap.sl.util.jarsl.impl;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Stack;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.zip.CRC32;
import com.sap.sl.util.jarsl.api.Conv;
import com.sap.sl.util.jarsl.api.JarSLException;
import com.sap.sl.util.jarsl.api.JarSLFileStreamHandlerIF;
import com.sap.sl.util.jarsl.api.JarSLOutputStreamIF;

public class JarSLFileStreamHandler implements JarSLFileStreamHandlerIF {
  private JarOutputStream jos=null;
  private JarSLOutputStream jarslos=null;
  private Hashtable addeddirs=new Hashtable();
  JarSLFileStreamHandler(JarOutputStream jos) {
    this.jos=jos;
  }
  JarSLFileStreamHandler(JarOutputStream jos, Hashtable addeddirs) {
    this.jos=jos;
    this.addeddirs=new Hashtable(addeddirs);
  }
  public JarSLOutputStreamIF addFile(String _archname) throws JarSLException, IOException {
    return _add(_archname,false);
  }
  public JarSLOutputStreamIF addFileWithParentDirectories(String _archname) throws IOException, JarSLException {
    return _add(_archname,true);
  }
  private JarSLOutputStreamIF _add(String _archname, boolean withparentdirs) throws JarSLException, IOException {
    String archname=Conv.pathConvJar(_archname);
    if (archname.endsWith("/")) {
      throw new JarSLException("It is not allowed to add directories by using the JarSLFileStreamHandler.addFile method.");
    }
    if (jarslos!=null && jarslos.isOpen()) {
      throw new JarSLException("Another JarSLOutput stream is still open, it is not allowed to add more than one file at the same time.");
    }
    if (jos==null) {
      throw new JarSLException("Cannot add more files to the archive, because the JarSLFileStream handler was closed.");
    }
    if (withparentdirs) {
      addParentDirs(archname);
    }
    jos.putNextEntry(new JarEntry(archname));
    jarslos=new JarSLOutputStream(jos);
    return jarslos;
  }
  public void close() throws IOException {
    if (jos!=null) {
      jos.close();
      jos=null;
    }
  }
  private void addParentDirs(String entry) throws JarSLException, IOException {
    Stack directories=new Stack();
    int slashpos=entry.length();
    String dir=null;
    JarEntry je=null;
    while ((slashpos=entry.lastIndexOf((int)'/',slashpos-1))!=-1) {
      dir=entry.substring(0,slashpos+1);
      if (addeddirs.get(dir)!=null) {
        continue;
      }
      directories.push(dir);
    }
    while (!directories.isEmpty()) {
      dir=(String)directories.pop();
      if (addeddirs.get(dir)==null) {
        addeddirs.put(dir,dir);
        //
        je=new JarEntry(dir);
        je.setSize(0);
        je.setMethod(JarEntry.STORED);
        je.setCrc(new CRC32().getValue());
        jos.putNextEntry(je);
        //
      }
    }
  }
}

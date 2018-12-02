package com.sap.sl.util.jarsl.impl;

/**
 * Title: JarSL
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company: SAP AG
 * @author  Ralf Belger
 * @version 1.0
 */

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.CRC32;

import com.sap.sl.util.jarsl.api.ConstantsIF;
import com.sap.sl.util.jarsl.api.Conv;
import com.sap.sl.util.jarsl.api.JarSLException;
import com.sap.sl.util.jarsl.api.JarSLFileStreamHandlerIF;
import com.sap.sl.util.jarsl.api.JarSLIF;
import com.sap.sl.util.jarsl.api.JarSLManifestException;
import com.sap.sl.util.jarsl.api.JarSLManifestIF;
import com.sap.sl.util.jarsl.api.ManifestEntryIF;
import com.sap.sl.util.logging.api.SlUtilLogger;

final class JarSL implements ConstantsIF, JarSLIF {
  /**
    * DEF_jarslversion should be updated with every change in com.sap.sl.util.jarsl
    */
  static private String DEF_jarslversion="20090728.1000";
  private static final SlUtilLogger log = SlUtilLogger.getLogger(JarSL.class.getName());

  private Vector filelist;      /* this vector contains the files which are in the actual archive resp. which will be added to the actual archive */
  private String jarfilename;   /* name of the actual archive */
  private String rootdir;       /* relative referce directory for extraction and recursive file addition to the archive */
  private boolean changed;      /* some changes to the actual archive occured, so it must be new created before termination */
  private boolean filelistread; /* the file content of the archive was read */
  private boolean manifestread; /* the manifest of the archive was read */
  private ManifestSL manifestsd, manifestex; /* these two objects contain the MANIFEST.MF and the SAP_MANIFEST.MF content */
  private ObjectOutputStream oossf=null;
  private String oossffilename=null;
  private String oossfarchname=null;
  private ObjectInputStream oissf=null;
  private String jarfilename_conv=null;

  /**
   * This method returns the current jarSL version      
   * @return String the jarSL version
   */
  static String getJarSLVersion() {
    return DEF_jarslversion;
  }
  /**
   * creates a JarSL instance
   * @param jarfilename
   * @param rootdir
   */
 	JarSL(String jarfilename, String rootdir) {
    log.entering("constructor: "+jarfilename+" , "+rootdir);
    this.jarfilename=Conv.pathConvJar(jarfilename);
    this.rootdir=Conv.pathConvJar(rootdir);
    jarfilename_conv=Conv.pathConv(jarfilename);
    filelist=new Vector();
    manifestsd=new ManifestSL(JARSL_MANIFEST);    /* standard manifest */
    manifestex=new ManifestSL(JARSL_SAP_MANIFEST);/* ext. manifest */
    changed=false;
    filelistread=false;
    manifestread=false;
    log.exiting("constructor");
  }
  /**
   * returns the name of the archive
   */
  public File getArchiveName() {
    return new File(jarfilename);
  }
  /**
   * calculates the md5 fingerprint of the actual archive
   */
  public String calcFingerprintFromArchive() {
    return (new FingerPrint(jarfilename_conv)).calcMD5();
  }
  /**
   * returns every manifest entry which has the given attribute value pair entry in the SAP_MANIFEST.MF
   */
  public String[] getEntryListOfGivenAttributeValuePair(String attribute, String value) {
    if (!manifestread) { /* in case the manifest was not read before, read it */
      try {
        manifestsd.readAllAttributes(jarfilename);
        manifestex.readAllAttributes(jarfilename);
        manifestread=true;
      }
      catch (JarSLManifestException e) {
        manifestread=false;
        return null;
      }
    }
    return manifestex.getEntryListOfGivenAttributeValuePair(attribute,value);
  }
  /**
   * returns every entry contained in the SAP_MANIFEST.MF (only once)
   */
  public String[] getFilelistFromManifest() {
    if (!manifestread) { /* in case the manifest was not read before, read it */
      try {
        manifestsd.readAllAttributes(jarfilename);
        manifestex.readAllAttributes(jarfilename);
        manifestread=true;
      }
      catch (JarSLManifestException e) {
        manifestread=false;
        return null;
      }
    }
    return manifestex.getEntryListFromManifest();
  }
  /**
   * returns every archive entry contained in the SAP_MANIFEST.MF (only once)
   */
  public String[] getArchiveEntriesFromManifest() {
    if (!manifestread) { /* in case the manifest was not read before, read it */
      try {
        manifestsd.readAllAttributes(jarfilename);
        manifestex.readAllAttributes(jarfilename);
        manifestread=true;
      }
      catch (JarSLManifestException e) {
        manifestread=false;
        return null;
      }
    }
    return manifestex.getArchiveEntryListFromManifest();
  }
  /**
   * This method extracts a single file from the archive and returns it as InputStrean.
   * The returned stream must be closed by the caller.
   */
  public InputStream extractSingleFileAsStream(String sfname) throws JarSLException {
    String sfname_convjar=Conv.pathConvJar(sfname);
    String localplattform=System.getProperty("os.name")+System.getProperty("os.arch");
    if (sfname_convjar.endsWith("/")) {
      throw new JarSLException("It is not possible to extract the directory "+sfname+".");
    }
    if (!localplattform.toLowerCase().startsWith("Windows".toLowerCase())) {
      //    ==> due to SUN bug 4705373 the usage of JarFile causes an out_of_memory error on linux 
      JarEntry je;
      JarInputStream jis=null;
      boolean rc=false;
      try {
        jis=new JarInputStream(new BufferedInputStream(new FileInputStream(jarfilename_conv),65536));
        while ((je=jis.getNextJarEntry())!=null && !rc) {
          if (Conv.pathConvJar(je.getName()).compareTo(sfname_convjar)==0) {
            rc=true;
          }
        }
        if (!rc) {
          throw new JarSLException("File "+sfname+" cannot be extracted from "+jarfilename_conv+".");
        }
      }
      catch (JarSLException e) {
        if (jis!=null) {
          try {
            jis.close();
          }
          catch(IOException e1) {
           // $JL-EXC$
          }
          jis=null;
        }
        throw e;
      }
      catch (Exception e) {
        if (jis!=null) {
          try {
            jis.close();
          }
          catch(IOException e1) {
           // $JL-EXC$
          }
          jis=null;
        }
        throw new JarSLException(e.getMessage());
      }
      return jis;
    }
    else {
      JarFile jf=null;
      JarEntry je;
      InputStream is=null;
      try {
        jf=new JarFile(jarfilename_conv);
        je=jf.getJarEntry(sfname_convjar);
        if (je==null) {
          throw new JarSLException("File "+sfname+" cannot be extracted from "+jarfilename_conv+".");
        }
        else {
          is=jf.getInputStream(je);
        }
      }
      catch (JarSLException e) {
        if (is!=null) {
          try {
            is.close();
          }
          catch(IOException e1) {
           // $JL-EXC$
          }
          is=null;
        }
        throw e;
      }
      catch (Exception e) {
        if (is!=null) {
          try {
            is.close();
          }
          catch(IOException e1) {
            // $JL-EXC$
          }
          is=null;
        }
        throw new JarSLException(e.getMessage());
      }
      return is;
    }   
  }
	/**
	 * extracts a single file form the archive and returns it as ByteArrayInputStrean
	 */
	public ByteArrayInputStream extractSingleFileAsByteArray(String sfname) {
    String sfname_convjar=Conv.pathConvJar(sfname);
		String localplattform=System.getProperty("os.name")+System.getProperty("os.arch");
		if (!localplattform.toLowerCase().startsWith("Windows".toLowerCase())) {
			//		==> due to SUN bug 4705373 the usage of JarFile causes an out_of_memory error on linux 
			byte[] buff=new byte[65536];
			int len;
			JarEntry je;
			JarInputStream jis=null;
			ByteArrayOutputStream baos=null;
			ByteArrayInputStream bais=null;
			boolean rc=false;
			try {
				jis=new JarInputStream(new BufferedInputStream(new FileInputStream(jarfilename_conv),65536));
				while ((je=jis.getNextJarEntry())!=null && !rc) {
					if (Conv.pathConvJar(je.getName()).compareTo(sfname_convjar)==0) {
						if (!sfname_convjar.endsWith("/")) {
							baos=new ByteArrayOutputStream();
							while ((len=jis.read(buff))>=0) {
								baos.write(buff,0,len);
							}
							bais=new ByteArrayInputStream(baos.toByteArray());
							baos.close();
							baos=null;
						}
						rc=true;
					}
				}
				if (rc==false && sfname_convjar.compareTo(JARSL_METAINF_MANIFEST)==0 && jis.getManifest()!=null) {   /* extract the standard manifest */
					baos=new ByteArrayOutputStream();
					jis.getManifest().write(baos);
					bais=new ByteArrayInputStream(baos.toByteArray());
					baos.close();
					baos=null;
					rc=true;
				}
				jis.close();
				jis=null;
			}
			catch (Exception e) {
				if (bais!=null) {
					try {
						bais.close();
					}
					catch(IOException e1) {
           // $JL-EXC$
					}
					bais=null;
				}
			}
			finally {
				try {  
					if (baos!=null) {
						baos.close();
					}
				}
				catch (IOException e) {
       		// $JL-EXC$
				}
				try {
					if (jis!=null) {
						jis.close();
					}
				}
				catch (IOException e) {
					// $JL-EXC$
				}
			}
			return bais;
		}
		else {
			byte[] buff=new byte[65536];
			int len;
			JarFile jf=null;
			InputStream is=null;
			ByteArrayOutputStream baos=null;
			ByteArrayInputStream bais=null;
			JarEntry je=null;
			boolean rc=false;
			try {
				jf=new JarFile(jarfilename_conv);
				try {
				  je=jf.getJarEntry(sfname_convjar);
				  if (je==null) {
				    is=null;
				  }
				  else {
				    is=jf.getInputStream(je);
				  }
				}
				catch (IOException e) {
					is=null;
				}
				if (is!=null) {
					rc=true;
					if (!sfname_convjar.endsWith("/")) {
						baos=new ByteArrayOutputStream();
						while ((len=is.read(buff))>=0) {
							baos.write(buff,0,len);
						}
						bais=new ByteArrayInputStream(baos.toByteArray());
						baos.close();
						baos=null;
					}
				}
				if (rc==false && sfname_convjar.compareTo(JARSL_METAINF_MANIFEST)==0 && jf.getManifest()!=null) {   /* extract the standard manifest */
					baos=new ByteArrayOutputStream();
					jf.getManifest().write(baos);
					bais=new ByteArrayInputStream(baos.toByteArray());
					baos.close();
					baos=null;
					rc=true;
				}
				is.close();
				is=null;
				jf.close();
				jf=null;
			}
			catch (Exception e) {
				if (bais!=null) {
					try {
						bais.close();
					}
					catch(IOException e1) {
  					// $JL-EXC$
					}
					bais=null;
				}
			}
			finally {
				try {  
					if (baos!=null) {
						baos.close();
					}
				}
				catch (IOException e) {
					// $JL-EXC$
				}
				try {
					if (is!=null) {
						is.close();
					}
				}
				catch (IOException e) {
					// $JL-EXC$
				}
				try {
					if (jf!=null) {
						jf.close();
					}
				}
				catch (IOException e) {
					// $JL-EXC$
				}
			}
			return bais;
		}
	}
  /**
   * extracts a single file form the archive and writes it to targetname or
   * (if targetname == null) to the correct position relative to root
   */
  public boolean extractSingleFile(String sfname,Vector errorTexts) {
    String[] sfnames=new String[1];
    sfnames[0]=sfname;
    return extractSingleFiles(sfnames,null,errorTexts);
  }
  public boolean extractSingleFile(String sfname, String targetname,Vector errorTexts) {
    String[] sfnames=new String[1];
    String[] targetnames=new String[1];
    sfnames[0]=sfname;
    targetnames[0]=targetname;
    return extractSingleFiles(sfnames,targetnames,errorTexts);
  }
  /**
   * extracts single files form the archive and writes it to the given targetnames or
   * (if targetnames == null) to the correct position relative to root
   */
  public boolean extractSingleFiles(String[] sfnames,Vector errorTexts) {
    return extractSingleFiles(sfnames,null,errorTexts);
  }
  public boolean extractSingleFiles(String[] sfnames, String[] targetnames,Vector errorTexts) {
    log.entering("extractSingleFiles");
    log.debug("called sfnames: ");
    for (int i=0; sfnames!=null && i<sfnames.length; ++i) {
      log.debug("==> "+i+". "+sfnames[i]);
    }
    log.debug("called targetnames: ");
    for (int i=0; targetnames!=null && i<targetnames.length; ++i) {
      log.debug("==> "+i+". "+targetnames[i]);
    }
    byte[] buff=new byte[65536];
    int len;
    JarEntry je;
    FileLoc name;
    String namepath_conv=null;
    String path;
    JarInputStream jis=null;
    BufferedOutputStream bos=null;
    boolean rc=true;
    boolean finished=false;
    boolean[] extracted=new boolean[sfnames.length];
    for (int i=0; i<sfnames.length; ++i) {
      extracted[i]=false;
    }
    if (targetnames!=null && sfnames.length!=targetnames.length) {
      if (null != errorTexts) {
        errorTexts.add("Wrong call of method 'extractSingleFiles'.");
      }
      log.debug("Wrong call of method 'extractSingleFiles'.");
      log.exiting("extractSingleFiles: false");
      return false;
    }
    try {
      log.debug("open input stream for "+jarfilename_conv);
      jis=new JarInputStream(new BufferedInputStream(new FileInputStream(jarfilename_conv),65536));
      while ((je=jis.getNextJarEntry())!=null && !finished) {  
        int place=-1;
        log.debug("got "+je.getName()+" as next jar entry");
        for (int i=0; i<sfnames.length && place==-1; ++i) {
          if (extracted[i]==false && Conv.pathConvJar(je.getName()).compareTo(Conv.pathConvJar(sfnames[i]))==0) {
            place=i;
            extracted[i]=true;
            finished=true;
            for (int j=0; j<extracted.length; ++j) {
              finished&=extracted[j];
            }
          }
        }
        if (place!=-1) {
          if (targetnames==null || targetnames[place]==null) {
            name=new FileLoc(rootdir,Conv.pathConvJar(je.getName()));
            namepath_conv=Conv.pathConv(name.pathname());
          }
          else {
            name=new FileLoc("",Conv.pathConvJar(targetnames[place]));
            namepath_conv=Conv.pathConv(name.pathname());
          }
          if ((path=((new File(namepath_conv)).getParent()))!=null) {
            log.debug("build directory structure for "+path+" on filesystem");
            if ((new File(path)).mkdirs()) {
              log.debug("succeeded");
            }
            else {
              log.debug("failed");
            } 
          }
          if (!name.pathname().endsWith("/")) {
            log.debug("open output stream for "+namepath_conv);
            bos=new BufferedOutputStream(new FileOutputStream(namepath_conv),65536);
            while ((len=jis.read(buff))>=0) {
              bos.write(buff,0,len);
            }
            log.debug("output stream writting finished");
            bos.close();
            bos=null;
          }
          else {        
            log.debug("build directory structure for "+namepath_conv+" on filesystem");
            if ((new File(namepath_conv)).mkdirs()) {
              log.debug("succeeded");
            }
            else {
              log.debug("failed");
            }     
          }
          (new File(namepath_conv)).setLastModified(je.getTime());
          log.debug("modification time of "+namepath_conv+" set to "+je.getTime());
          if (targetnames==null) {
            addFile(name.base(),name.fname(),null); /* register the extracted file to the filelist */
          }
        }
      }
      if (finished==false) {
        for (int i=0; i<extracted.length; ++i) {
          if (extracted[i]==false) {
            if (Conv.pathConvJar(sfnames[i]).compareTo(JARSL_METAINF_MANIFEST)==0 && jis.getManifest()!=null) {   /* extract the standard manifest */
              log.debug("the standard manifest is extracted now");
              if (targetnames==null || targetnames[i]==null) {
                name=new FileLoc(rootdir,JARSL_METAINF_MANIFEST);
                namepath_conv=Conv.pathConv(name.pathname());
              }
              else {
                name=new FileLoc("",Conv.pathConvJar(targetnames[i]));
                namepath_conv=Conv.pathConv(name.pathname());
              }
              if ((path=((new File(namepath_conv).getParent())))!=null) {
                log.debug("build directory structure for "+path+" on filesystem");
                if((new File(path)).mkdirs()) {
                  log.debug("succeeded");
                }
                else {
                  log.debug("failed");
                }    
              }
              log.debug("open output stream for "+namepath_conv);
              bos=new BufferedOutputStream (new FileOutputStream(namepath_conv));
              jis.getManifest().write(bos);
              log.debug("output stream writting finished");
              bos.close();
              bos=null;
            }
            else {
              rc=false;
              log.debug("Cannot find the file "+Conv.pathConvJar(sfnames[i])+" in the archive.");
              if (null != errorTexts) {
                errorTexts.add("Cannot find the file "+Conv.pathConvJar(sfnames[i])+" in the archive.");
              }
            }
          }
        }
      }
      jis.close();
      jis=null;
    }
    catch (Exception e) {
      log.fatal(e);
      if (null != errorTexts) {
        errorTexts.add("Exception occured during extractSingleFile: " + e.getMessage());
      }
      rc=false;
    }
    finally {
      try {  
        if (bos!=null) {
          bos.close();
        }
      }
      catch (IOException e) {
        log.error(e);
      }
      try {
        if (jis!=null) {
          jis.close();
        }
      }
      catch (IOException e) {
        log.error(e);
      }
    }
    log.exiting("extractSingleFiles: rc ="+rc);
    return rc;
  }
  /**
   * makes a simple copy of the actual archive
   */
  public boolean copyArchive(String targetname) {
    boolean rc=false;
    String path;
    byte[] buff=new byte[65536];
    int len;
    BufferedInputStream bis=null;
    BufferedOutputStream bos=null;
    try {
      bis=new BufferedInputStream(new FileInputStream(jarfilename_conv),65536);
      FileLoc name=new FileLoc("",Conv.pathConvJar(targetname));
      if (name.pathname().toLowerCase().equals(jarfilename.toLowerCase())) {
        return false;
      }
      if ((path=((new File(Conv.pathConv(name.pathname()))).getParent()))!=null) {
        (new File(path)).mkdirs();
      }
      bos=new BufferedOutputStream (new FileOutputStream(Conv.pathConv(name.pathname())),65536);
      while ((len=bis.read(buff))>=0) {
        bos.write(buff,0,len);
      }
      bos.close();
      bos=null;
      bis.close();
      bis=null;
      rc=true;
    }
    catch (Exception e) {
      rc=false;
    }
    finally {
      try {  
        if (bos!=null) {
          bos.close();
        }
      }
      catch (IOException e) {
        // $JL-EXC$
      }
      try {
        if (bis!=null) {
          bis.close();
        }
      }
      catch (IOException e) {
        // $JL-EXC$
      }
    }
    return rc;
  }
  /**
   * This method opens the archive and extracts it relative to the given rootdirectory.
   * Also the included files are added to the filelist and the two manifests are read.
   * In case of errors, false is returned.
   */
  public boolean extract() {
    return extract(null);
  }
  /**
   * This method opens the archive and extracts it relative to the given rootdirectory.
   * Also the included files are added to the filelist and the two manifests are read.
   * In case of errors, messages are added to the vector errTexts of String's,
   * and false is returned. If deleterootdirectory is true then the whole rootdir is deleted
   * at first.
   */
  public boolean extract(Vector errTexts) {
  	return extract(errTexts,false);
  }
  public boolean extract(Vector errTexts, boolean deleterootdirectory) {
    log.entering("extract: delete root dir ="+(deleterootdirectory?"true":"false"));
    byte[] buff=new byte[65536];
    int len;
    JarEntry je;
    String path;
    FileLoc name = null;
    String namepath_conv=null;
    JarInputStream jis=null;
    BufferedOutputStream bos=null;
    boolean rc=true;
    try {
      filelist.removeAllElements(); /* clear the filelist */
      if (deleterootdirectory && rootdir!=null && !rootdir.equals("") && !rootdir.equals(" ")) {
        log.debug("perform deletion of rootdir ("+rootdir+")");
        deleteFile(new File(rootdir));
      }
      //
      log.debug("open input stream for "+jarfilename_conv);
      jis=new JarInputStream(new BufferedInputStream(new FileInputStream(jarfilename_conv),65536));
      while ((je=jis.getNextJarEntry())!=null) {
        log.debug("got "+je.getName()+" as next jar entry");
        name=new FileLoc(rootdir,Conv.pathConvJar(je.getName()));
        namepath_conv=Conv.pathConv(name.pathname());
        filelist.addElement(name);
        if ((path=((new File(namepath_conv)).getParent()))!=null) {
          log.debug("build directory structure for "+path+" on filesystem");
          if ((new File(path)).mkdirs()) {
            log.debug("succeeded");
          }
          else {
            log.debug("failed");
          }     
        }
        if (!name.pathname().endsWith("/")) {  /* is it a directory */
          log.debug("open output stream for "+namepath_conv);
          bos=new BufferedOutputStream(new FileOutputStream(namepath_conv),65536);
          while ((len=jis.read(buff))>=0) {
            bos.write(buff,0,len);
          }
          log.debug("output stream writting finished");
          bos.close();
          bos=null;
        }
        else {
          log.debug("build directory structure for "+namepath_conv+" on filesystem");
          if ((new File(namepath_conv)).mkdirs()) {
            log.debug("succeeded");
          }
          else {
            log.debug("failed");
          }     
        }
        (new File(namepath_conv)).setLastModified(je.getTime());
        log.debug("modification time of "+namepath_conv+" set to "+je.getTime());
      }
      if (jis.getManifest()!=null) {   /* extract the standard manifest */
        log.debug("the standard manifest is extracted now");
        log.debug("build directory structure for "+Conv.pathConv(rootdir+"/"+JARSL_METAINF)+" on filesystem");
        if((new File(Conv.pathConv(rootdir+"/"+JARSL_METAINF))).mkdirs()) {
          log.debug("succeeded");
        }
        else {
          log.debug("failed");
        }    
        log.debug("open output stream for "+Conv.pathConv(rootdir+"/"+JARSL_METAINF_MANIFEST));
        bos=new BufferedOutputStream(new FileOutputStream(Conv.pathConv(rootdir+"/"+JARSL_METAINF_MANIFEST)));
        jis.getManifest().write(bos);
        log.debug("output stream writting finished");
        bos.close();
        bos=null;
        if (filelist.contains(new FileLoc(rootdir,JARSL_METAINF_MANIFEST))==false) {
          filelist.addElement(new FileLoc(rootdir,JARSL_METAINF_MANIFEST));
        }
      }
      jis.close();
      jis=null;
      log.debug("start reading of standard manifest");
      manifestsd.readAllAttributes(rootdir); /* read standard manifest to memory */
      log.debug("reading finished");
      log.debug("start reading of ext. manifest");
      manifestex.readAllAttributes(rootdir); /* read ext. manifest to memory */
      log.debug("reading finished");
      filelistread=true;
      manifestread=true;
    }
    catch (Exception e) {
      log.fatal(e);
      if (errTexts != null) {
        /* fill errTexts */
        if (name != null) {
          errTexts.add("Error: Could not extract file " + name.fname() +
                       " from archive " + jarfilename);
        } else {
          errTexts.add("Error: Could not extract all files from archive " +
                       jarfilename);
        }
        errTexts.add("Additional error message is:");
        errTexts.add(e.getMessage());
      } // if errTexts
      filelist.removeAllElements();
      manifestsd.removeAllElements();
      manifestex.removeAllElements();
      filelistread=false;
      manifestread=false;
      changed=false;
      rc=false;
    }
    finally {
      try {  
        if (bos!=null) {
          bos.close();
        }
      }
      catch (IOException e) {
        log.error(e);
      }
      try {
        if (jis!=null) {
          jis.close();
        }
      }
      catch (IOException e) {
        log.error(e);
      }
      log.exiting("extract: rc ="+rc);
    }
    return rc;
  }
  /**
   * scans the directory structur beginning with rootdir and adds the located files to the
   * filelist, also the two manifests are read (if they are found at the correct location)
   */
  public boolean addAllFiles() {
    filelist.removeAllElements();
    changed=true;
    try {
      getFileList(rootdir);
    }
    catch (IOException e1) {
      return false;
    }   /* fills the filelist recursive starting from roodir */     
    filelistread=true;
    try {
      manifestsd.readAllAttributes(rootdir);
      manifestex.readAllAttributes(rootdir);
      manifestread=true;
      return true;
    }
    catch (JarSLManifestException e) {
      manifestread=false;
      return false;
    }
  }
  /**
   * This methods write the files contained in filelist to the archive. Also the two
   * manifests are created from the two manifest objects and added to the new archive.
   */
  public boolean create(Vector errorTexts) {
    return create(null,true,true,true,errorTexts);
  }
  public boolean create(boolean compress,Vector errorTexts) {
    return create(null,compress,true,true,errorTexts);
  }
  public boolean create(boolean compress, boolean writemftodisk, boolean writesapmanifest, Vector errorTexts) {
      return create(null,compress,writemftodisk,writesapmanifest,errorTexts);
  }
  public boolean create(String filename, boolean compress, boolean writemftodisk, boolean writesapmanifest, Vector errorTexts) {
    JarSLFileStreamHandlerIF jfsh=null;
    try {
      jfsh=create(null,filename,compress,writemftodisk,writesapmanifest);
      jfsh.close();
      jfsh=null;
      if (filename==null) {
        changed=false;
      }
      return true;
    }
    catch (Exception e) {
      if (errorTexts != null) {
        errorTexts.add(e.getMessage());
      }
      return false;
    }
    finally {
      try {  
        if (jfsh!=null) {
          jfsh.close();
        }
      }
      catch (IOException e) {
        // $JL-EXC$
      }
    }
  }
  /**
   * This methods write the files contained in filelist to the given output stream by using
   * the archive format. Also the two manifests are created from the two manifest objects and
   * added to the stream. The given stream must be closed by the caller.
   */
  public boolean createAsStream(OutputStream ostream) {
    return createAsStream(ostream,true,true,null);
  }
  public boolean createAsStream(OutputStream ostream, boolean compress, Vector errorTexts) {
    return createAsStream(ostream,compress,true,errorTexts);
  }
  public boolean createAsStream(OutputStream ostream, boolean compress, boolean writesapmanifest, Vector errorTexts) {
    JarSLFileStreamHandlerIF jfsh=null;
    try {
      jfsh=create(ostream,null,compress,false,writesapmanifest);
      jfsh.close();
      jfsh=null;
      return true;
    }
    catch (Exception e) {
      if (errorTexts != null) {
        errorTexts.add(e.getMessage());
      }
      return false;
    }
    finally {
      try {  
        if (jfsh!=null) {
          jfsh.close();
        }
      }
      catch (IOException e) {
        // $JL-EXC$
      }
    }
  }
  //
  /**
   * This methods write the files contained in filelist to the archive. Also the two
   * manifests are created from the two manifest objects and added to the new archive.
   * In contrast to the create method it is possible to add additional files via stream to the
   * resulting archive (see JarSLFileStreamHandler). As soon as the JarSLFileStream handler is closed
   * the resulting archive is built. The resulting archive is always compressed.
   */
  public JarSLFileStreamHandlerIF createWithAdditionalFileStreams() throws JarSLException, IOException {
    return createWithAdditionalFileStreams(null,true,true);
  }
  public JarSLFileStreamHandlerIF createWithAdditionalFileStreams(boolean writemftodisk, boolean writesapmanifest)throws JarSLException, IOException {
    return createWithAdditionalFileStreams(null,writemftodisk,writesapmanifest);
  }
  public JarSLFileStreamHandlerIF createWithAdditionalFileStreams(String filename, boolean writemftodisk, boolean writesapmanifest) throws JarSLException, IOException {
    boolean tmp_changed=changed; 
    changed=true;
    try {
      return create(null,filename,true,writemftodisk,writesapmanifest);
    }
    finally {
      changed=tmp_changed;
    }
  }
  /**
   * This methods write the files contained in filelist to the given output stream by using
   * the archive format. Also the two manifests are created from the two manifest objects and
   * added to stream. The given stream must be closed by the caller.
   * In contrast to the create method it is possible to add additional files via stream to the
   * resulting archive (see JarSLFileStreamHandler). As soon as the JarSLFileStream handler is 
   * closed the resulting archive is built. The resulting archive is always compressed.
   */
  public JarSLFileStreamHandlerIF createAsStreamWithAdditionalFileStreams(OutputStream ostream) throws JarSLException, IOException {
    return createAsStreamWithAdditionalFileStreams(ostream,true);
  }
  public JarSLFileStreamHandlerIF createAsStreamWithAdditionalFileStreams(OutputStream ostream, boolean writesapmanifest) throws JarSLException, IOException {
    boolean tmp_changed=changed; 
    changed=true;
    try {
      return create(ostream,null,true,false,writesapmanifest);
    }
    finally {
      changed=tmp_changed;
    }
  }
  private JarSLFileStreamHandlerIF create(OutputStream ostream, String filename, boolean compress, boolean writemftodisk, boolean writesapmanifest) throws IOException, JarSLException {
    JarOutputStream jos=null;
    ByteArrayInputStream bais=null;
    InputStream is=null;
    Hashtable addeddirs=new Hashtable();
    if (filename!=null || changed || ostream!=null) {
      try {
        byte[] buff=new byte[65536];
        int len;
        String path;
        if (writeAttribute("",ATTJARSLVERSION,DEF_jarslversion)==false) {
          throw new JarSLException("Could not write attribute (,"+ATTJARSLVERSION+","+DEF_jarslversion+")");
        }
        writeAttribute("",ATTCOMPRESS,compress?"true":"false");
        if (writemftodisk) { /* the two manifests are created and written to the file-system */
          if (!manifestsd.createManifest(rootdir) || !manifestex.createManifest(rootdir)) {
            throw new JarSLException("Could not write manifests to file system.");
          }
        }
        removeDir("/");
        removeDir(JARSL_METAINF);
        removeFile(JARSL_METAINF_SAP_MANIFEST);
        removeFile(JARSL_METAINF_MANIFEST);
        addFile(JARSL_METAINF_SAP_MANIFEST,0,null);
        addDir(JARSL_METAINF,0,null);
        bais=manifestsd.createManifest(); /* the standard manifest is created and returned as ByteArrayInputStream */
        Manifest mf=null;
        try {
          if (bais==null || (mf=new Manifest(bais))==null) {
          throw new Exception("jump");
          }
        }
        catch (Exception e) {
          throw new JarSLException("Could not create Manifest from ByteArrayInputStream (1).");
        }
        bais.close();
        bais=null;
        if (ostream!=null) {
          jos=new JarOutputStream(new BufferedOutputStream(ostream,65536),mf);
        }
        else {
          if ((path=((new File(Conv.pathConv(filename!=null?filename:jarfilename))).getParent()))!=null) {
            (new File(path)).mkdirs();
          }
          jos=new JarOutputStream(new BufferedOutputStream(new FileOutputStream(Conv.pathConv(filename!=null?filename:jarfilename)),65536),mf);
        }
        if (compress) {
          jos.setMethod(JarOutputStream.DEFLATED);
        }
        else {
          jos.setMethod(JarOutputStream.STORED);
        }
        FileLoc nextfile;
        String nextfilepath_conv=null;
        JarEntry je;
        String jarentryname;
        long emptycrc=new CRC32().getValue();
        if (writesapmanifest==false) {
            removeFile(JARSL_METAINF_SAP_MANIFEST);
        }
        for (int i=0;i<filelist.size();++i) {
          nextfile=(FileLoc)filelist.elementAt(i);
          nextfilepath_conv=Conv.pathConv(nextfile.pathname());
          if (nextfile.fname().compareTo(JARSL_METAINF_MANIFEST)!=0) { /* standard manifest is already in the archive, see constructor */
            if (nextfile.archfilename()!=null) {
              jarentryname=nextfile.archfilename();
            }
            else {
              jarentryname=nextfile.fname();
            }
            je=new JarEntry(jarentryname);
            if (nextfile.pathname().endsWith("/")) { /* a directory is added */
              if (addeddirs.get(jarentryname)==null) {
                addeddirs.put(jarentryname,jarentryname);
              }
              je.setSize(0);
              je.setMethod(JarEntry.STORED);
              je.setCrc(emptycrc);
              try {
                File fod=new File(nextfilepath_conv);
                if (fod.isDirectory()) {
                  je.setTime(fod.lastModified());
                }
              }
              catch (Exception e) {
                // $JL-EXC$
              }
            }
            else {
              if (!compress) {
                long size=0;
                CRC32 crc=new CRC32();
                if (writemftodisk || nextfile.fname().compareTo(JARSL_METAINF_SAP_MANIFEST)!=0) {
                  is=new BufferedInputStream(new FileInputStream(nextfilepath_conv),65536);
                }
                else {
                  is=manifestex.createManifest(); /* ext. manifest is created and returned as ByteArrayInputStream */
                  if (is==null) {
                    throw new JarSLException("Could not create Manifest from ByteArrayInputStream (2).");
                  }
                }
                while ((len=is.read(buff))>0) {
                  size+=len;
                  crc.update(buff,0,len);
                }
                is.close();
                is=null;
                je.setSize(size);
                je.setCrc(crc.getValue());
              }
            }
            if (writemftodisk || nextfile.fname().compareTo(JARSL_METAINF_SAP_MANIFEST)!=0) {
              je.setTime((new File(nextfilepath_conv)).lastModified());
            }
            jos.putNextEntry(je);
            if (nextfile.pathname().endsWith("/")==false) {
              if (writemftodisk || nextfile.fname().compareTo(JARSL_METAINF_SAP_MANIFEST)!=0) {
                is=new BufferedInputStream(new FileInputStream(nextfilepath_conv),65536);
              }
              else {
                is=manifestex.createManifest(); /* ext. manifest is created and returned as ByteArrayInputStream */
                if (is==null) {
                  throw new JarSLException("Could not create Manifest from ByteArrayInputStream (3).");
                }
              }
              while ((len=is.read(buff))>=0) {
                jos.write(buff,0,len);
              }
              is.close();
              is=null;
            }
          }
          if (nextfile.deleteIt()==true) {
            File file=new File(nextfilepath_conv);
            file.delete();
          }
        }
      }
      catch (IOException e) {
        try {  
          if (jos!=null) {
            jos.close();
          }
        }
        catch (IOException ee) {
          // $JL-EXC$
        }
        throw e;
      }
      catch (JarSLException e) {
        try {  
          if (jos!=null) {
            jos.close();
          }
        }
        catch (IOException ee) {
          // $JL-EXC$
        }
        throw e;
      }
      finally {
        try {  
          if (bais!=null) {
            bais.close();
          }
        }
        catch (IOException e) {
          // $JL-EXC$
        }
        try {
          if (is!=null) {
            is.close();
          }
        }
        catch (IOException e) {
          // $JL-EXC$
        }
      }
    }
    return new JarSLFileStreamHandler(jos,addeddirs);
  }
  /**
   * removes a file from the filelist
   */
  public boolean removeFile(String filename) {
    return removeFile(rootdir,filename);
  }
  public boolean removeFile(String lroot, String filename) {
    FileLoc fl=new FileLoc(Conv.pathConvJar(lroot),Conv.pathConvJar(filename));
    FileLoc fl2;
    if (filelistread && filelist.contains(fl)==true) {
      fl2=(FileLoc)filelist.elementAt(filelist.indexOf(fl));
      if (fl2.deleteIt()==true) {
        (new File(fl2.pathname())).delete();
      }
      filelist.removeElementAt(filelist.indexOf(fl));
      changed=true;
      return true;
    }
    else {
      return false;
    }
  }
  /**
   * removes a directory from the filelist
   */
  public boolean removeDir(String dirname) {
    return removeDir(rootdir,dirname);
  }
  public boolean removeDir(String lroot, String dirname) {
    lroot=Conv.pathConvJar(lroot);
    dirname=Conv.pathConvJar(dirname);
    if (!dirname.endsWith("/")) {
      dirname+="/";
    }
    FileLoc fl=new FileLoc(lroot,dirname);
    FileLoc fl2;
    if (filelistread && filelist.contains(fl)==true) {
      fl2=(FileLoc)filelist.elementAt(filelist.indexOf(fl));
      if (fl2.deleteIt()==true) {
        (new File(fl2.pathname())).delete();
      }
      filelist.removeElementAt(filelist.indexOf(fl));
      changed=true;
      return true;
    }
    else {
      return false;
    }
  }
  /**
   * This method opens the entry named with entryname within the archive. The file is
   * read with the methods get...Data() and closed with the method closeSingleArchiveFile().
   */
  public boolean openSingleArchiveFile(String entryname, Vector errorTexts) {
    if (oissf==null) {
      if (!extractSingleFile(entryname,errorTexts)) {
        errorTexts.add("Error during extraction of "+entryname+" from the archive "+jarfilename);
        return false;
      }
      FileLoc name=new FileLoc(rootdir,Conv.pathConvJar((new File(entryname)).getName()));
//      ByteArrayInputStream bais=extractSingleFileAsByteArray(entryname);
//      if (bais==null) {
//        if (null != errorTexts) {
//          errorTexts.add("Error during extraction of "+entryname+" from the archive "+jarfilename);
//        }
//        return false;
//      }
      try {
        oissf=new ObjectInputStream(new FileInputStream(Conv.pathConv(name.pathname())));
        return true;
      }
      catch (Exception e) {
        if (null != errorTexts) {
          errorTexts.add("Error during extraction of "+entryname+". Stream is corrupted");
        }
        if (oissf!=null) {
          try {
            oissf.close();
          }
          catch (IOException e1) {
            // $JL-EXC$
          }
          oissf=null;
        }
        return false;
      }
    }
    else {
      if (null != errorTexts) {
        errorTexts.add("Error during extraction of "+entryname+". Stream already open");
      }
      return false;
    }
  }
  public boolean closeSingleArchiveFile(Vector errorTexts) {
    if (oissf==null) {
      if (null != errorTexts) {
        errorTexts.add("Error during closing the file. Stream is not open");
      }
      return false;
    }
    else {
      try {
        oissf.close();
        oissf=null;
        return true;
      }
      catch (Exception e) {
        if (null != errorTexts) {
          errorTexts.add("Error during closing the file. Cannot close it");
        }
        oissf=null;
        return false;
      }
    }
  }
  public int getIntData() throws IOException {
    if (oissf==null) {
      throw new IOException();
    }
    else {
      return oissf.readInt();
    }
  }
  public boolean getBooleanData() throws IOException {
    if (oissf==null) {
      throw new IOException();
    }
    else {
      return oissf.readBoolean();
    }
  }
  public long getLongData() throws IOException {
    if (oissf==null) {
      throw new IOException();
    }
    else {
      return oissf.readLong();
    }
  }
  public double getDoubleData() throws IOException {
    if (oissf==null) {
      throw new IOException();
    }
    else {
      return oissf.readDouble();
    }
  }
  public float getFloatData() throws IOException {
    if (oissf==null) {
      throw new IOException();
    }
    else {
      return oissf.readFloat();
    }
  }
  public short getShortData() throws IOException {
    if (oissf==null) {
      throw new IOException();
    }
    else {
      return oissf.readShort();
    }
  }
  public char getCharData() throws IOException {
    if (oissf==null) {
      throw new IOException();
    }
    else {
      return oissf.readChar();
    }
  }
  public byte getByteData() throws IOException {
    if (oissf==null) {
      throw new IOException();
    }
    else {
      return oissf.readByte();
    }
  }
  public int getData(byte[] data, int off, int len) throws IOException {
    if (oissf==null) {
      throw new IOException();
    }
    else {
      int readbytes=0;
      while (readbytes < len) {
        readbytes += oissf.read(data,readbytes+off,len-readbytes);
      }
      return readbytes;
    }
  }
  public int getData(byte[] data) throws IOException {
    if (oissf==null) {
      throw new IOException();
    }
    else {
      return this.getData(data,0,data.length);
    }
  }
  public Object getObjectData() throws ClassNotFoundException,IOException {
    if (oissf==null) {
      throw new IOException();
    }
    else {
      return oissf.readObject();
    }
  }
  /**
   * This method creates the file named with filename. The file is
   * filled with the methods put...Data() and closed with the method closeFile().
   */
  public boolean createFile(String filename,String archname,Vector errorTexts) {
    if (oossf==null) {
      oossffilename=filename;
      oossfarchname=archname;
      filename=Conv.pathConv(filename);
      try {
        if ((new File(filename).getParentFile())!=null) {
          (new File(filename).getParentFile()).mkdirs();
        }
        (new File(filename)).delete();
        oossf=new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(Conv.pathConv(filename)),65536));
      }
      catch (Exception e) {
        if (null != errorTexts) {
          errorTexts.add("Error during creation of the file "+filename+". Cannot create it.");
        }
        if (oossf!=null) {
          try {
            oossf.close();
          }
          catch (IOException e1) {
            // $JL-EXC$
          }
          oossf=null;
        }
        return false;
      }
    }
    else {
      if (null != errorTexts) {
        errorTexts.add("Error during creation of the file "+filename+". Stream already open");
      }
      return false;
    }
    return true;
  }
  public boolean putIntData(int in) {
    if (oossf==null) {
      return false;
    }
    else {
      try {
        oossf.writeInt(in);
        return true;
      }
      catch (Exception e) {
        return false;
      }
    }
  }
  public boolean putBooleanData(boolean in) {
    if (oossf==null) {
      return false;
    }
    else {
      try {
        oossf.writeBoolean(in);
        return true;
      }
      catch (Exception e) {
        return false;
      }
    }
  }
  public boolean putLongData(long in) {
    if (oossf==null) {
      return false;
    }
    else {
      try {
        oossf.writeLong(in);
        return true;
      }
      catch (Exception e) {
        return false;
      }
    }
  }
  public boolean putDoubleData(double in) {
    if (oossf==null) {
      return false;
    }
    else {
      try {
        oossf.writeDouble(in);
        return true;
      }
      catch (Exception e) {
        return false;
      }
    }
  }
  public boolean putFloatData(float in) {
    if (oossf==null) {
      return false;
    }
    else {
      try {
        oossf.writeFloat(in);
        return true;
      }
      catch (Exception e) {
        return false;
      }
    }
  }
  public boolean putShortData(short in) {
    if (oossf==null) {
      return false;
    }
    else {
      try {
        oossf.writeShort(in);
        return true;
      }
      catch (Exception e) {
        return false;
      }
    }
  }
  public boolean putCharsData(String in) {
    if (oossf==null) {
      return false;
    }
    else {
      try {
        oossf.writeChars(in);
        return true;
      }
      catch (Exception e) {
        return false;
      }
    }
  }
  public boolean putCharData(int in) {
    if (oossf==null) {
      return false;
    }
    else {
      try {
        oossf.writeChar(in);
        return true;
      }
      catch (Exception e) {
        return false;
      }
    }
  }
  public boolean putByteData(int in) {
    if (oossf==null) {
      return false;
    }
    else {
      try {
        oossf.writeByte(in);
        return true;
      }
      catch (Exception e) {
        return false;
      }
    }
  }
  public boolean putBytesData(String in) {
    if (oossf==null) {
      return false;
    }
    else {
      try {
        oossf.writeBytes(in);
        return true;
      }
      catch (Exception e) {
        return false;
      }
    }
  }
  public boolean putData(byte[] data, int off, int len) {
    if (oossf==null) {
      return false;
    }
    else {
      try {
        oossf.write(data,off,len);
      }
      catch (Exception e) {
        return false;
      }
    }
    return true;
  }
  public boolean putObjectData(Object in) {
    if (oossf==null) {
      return false;
    }
    else {
      try {
        oossf.writeObject(in);
        return true;
      }
      catch (Exception e) {
        return false;
      }
    }
  }
  /**
   * The new created file is closed and added to the filelist. When the first argument of the
   * method call is true then the file will be deleted after it was added to the archive
   */
  public boolean closeFile(boolean deleteit, Vector errorTexts) {
    if (oossf==null) {
      if (null != errorTexts) {
        errorTexts.add("Error during closing the file. Stream is not open");
      }
      return false;
    }
    else {
      try {
        oossf.close();
        oossf=null;
        return addFile("",oossffilename,oossfarchname,errorTexts,deleteit);
      }
      catch (Exception e) {
        if (null != errorTexts) {
          errorTexts.add("Error during closing the file. Cannot close it");
        }
        oossf=null;
        return false;
      }
    }
  }
  /**
   * adds a file to the filelist. If archname is not equal null, it represents the name of the
   * added file in the archive, if it is null then the archive name is equal to the filename.
   */
  public boolean addFile(String filename,int place,Vector errorTexts) {
    return addFile(rootdir,filename,null,place,errorTexts,false);
  }
  public boolean addFile(String filename,Vector errorTexts) {
    return addFile(rootdir,filename,null,errorTexts,false);
  }
  public boolean addFile(String filename,Vector errorTexts, boolean deleteit) {
    return addFile(rootdir,filename,null,errorTexts,deleteit);
  }
  public boolean addFile(String lroot, String filename,Vector errorTexts) {
    return addFile(lroot,filename,null,errorTexts,false);
  }
  public boolean addFile(String lroot, String filename, String archname,Vector errorTexts) {
    return addFile(lroot,filename,archname,errorTexts,false);
  }
  public boolean addFile(String lroot, String filename, String archname, Vector errorTexts,boolean deleteit) {
    return addFile(lroot,filename,archname,-1,errorTexts,deleteit);
  }
  public boolean addFile(String lroot, String filename, String archname,int place, Vector errorTexts,boolean deleteit) {
    FileLoc name=new FileLoc(Conv.pathConvJar(lroot),Conv.pathConvJar(filename),Conv.pathConvJar(archname),deleteit);
    if (filelist.contains(name)==false) {
      if (place!=-1 && filelist.size()>place) {
        filelist.insertElementAt(name,place);
      }
      else {
        filelist.addElement(name);
      }
      changed=true;
      filelistread=true;
      manifestread=true;
    }
    else {
      FileLoc _name=null;
      for (int i=0; _name==null && filelist!=null && i<filelist.size(); ++i) {
        if (filelist.elementAt(i).equals(name)) {
          _name=(FileLoc)filelist.elementAt(i);
        }
      }
      if (null != errorTexts) {
        if (_name!=null) {
          errorTexts.add("Error adding file "+name.fname()+" to archive. File "+ _name.fname()+" was already added before ("+name.pathname()+"<->"+_name.pathname()+")");
        }
        else {
          errorTexts.add("Error adding file "+name.fname()+" to archive. File was already added before.");
        }
      }
      return false;
    }
    File file = new File(Conv.pathConv(name.pathname()));
    if (file.canRead()==false) {
      if (null != errorTexts)
        errorTexts.add("Error adding file to archive. Can't read file: "+file.getAbsolutePath());
      return false;
    }
    return true;
  }
  /**
   * adds a directory to the filelist
   */
  public boolean addDir(String dirname, int place, Vector errorTexts) {
    return addDir(rootdir,dirname,place,errorTexts);
  }
  public boolean addDir(String dirname, Vector errorTexts) {
    return addDir(rootdir,dirname,-1,errorTexts);
  }
  public boolean addDir(String lroot, String dirname, Vector errorTexts) {
    return addDir(lroot,dirname,-1,errorTexts);
  }
  public boolean addDir(String lroot, String dirname, int place, Vector errorTexts) {
    lroot=Conv.pathConvJar(lroot);
    dirname=Conv.pathConvJar(dirname);
    if (!dirname.endsWith("/")) {
      dirname+="/";
    }
    FileLoc dir=new FileLoc(lroot,dirname);
    if (filelist.contains(dir)==false) {
      if (place!=-1 && filelist.size()>place) {
        filelist.insertElementAt(dir,place);
      }
      else {
        filelist.addElement(dir);
      }
      changed=true;
      filelistread=true;
      manifestread=true;
      return true;
    }
    else {
      FileLoc _dir=null;
      for (int i=0; _dir==null && filelist!=null && i<filelist.size(); ++i) {
        if (filelist.elementAt(i).equals(dir)) {
          _dir=(FileLoc)filelist.elementAt(i);
        }
      }
      if (null != errorTexts) {
        if (_dir!=null) {
          errorTexts.add("Error adding directory "+dir.fname()+" to archive. Directory "+ _dir.fname()+" was already added before ("+dir.pathname()+"<->"+_dir.pathname()+")");
        }
        else {
          errorTexts.add("Error adding directory "+dir.fname()+" to archive. Directory was already added before.");
        }
      }
      return false;
    }
  }
  /**
   * reads the attributes's value of the given entry from the manifest
   */
  public String readAttribute(String entry, String attribute) {
    return readAttribute(entry,attribute,null);
  }
  public String readAttribute(String entry, String attribute, Vector error) {
    log.entering("readAttribute: "+entry+" , "+attribute);
    boolean found=false;
    String value=null;
    boolean rc=true;
    if (!manifestread) {
      log.debug("manifest attrubutes must be read");
      try {
        rc=manifestsd.readAllAttributes(jarfilename);
        log.debug("rc of manifest read = "+rc);
        rc=manifestex.readAllAttributes(jarfilename);
        log.debug("rc of sapmanifest read = "+rc);
        manifestread=true;
      }
      catch (JarSLManifestException e) {
        log.debug("cannot read mfs",e);
        if (error!=null) {
          error.add(e.getMessage());
        }
        log.exiting("readAttribute: cannor read mfs");
        return null;
      }
    }
    if (entry.endsWith("/") || entry.endsWith("\\")) {
      found=true;
    }
    if (found==false && ManifestSL.isBothAttribute(attribute)) {
      log.debug("using both attribute access");
      value=manifestex.readAttribute(entry,attribute);
      if (value==null) {
        log.debug("attribute is not contained in sapmanifest");
        value=manifestsd.readAttribute(entry,attribute);
      }
    }
    else if (found==false && ManifestSL.isStandardAttribute(attribute)) {
      log.debug("using standard attribute access");
      value=manifestsd.readAttribute(entry,attribute);
    }
    else {
      log.debug("using ext attribute access");
      value=manifestex.readAttribute(entry,attribute);
    }
    log.exiting("readAttribute: rc="+value);
    return value;
  }
  /**
   * writes a attribute value pair for the given entry into the manifest
   */
  public boolean writeAttribute(String entry, String attribute, String value, boolean archiveentry) {
    boolean rv;
    boolean found=false;
    if (entry.endsWith("/") || entry.endsWith("\\")) {
      found=true;
    }
    if (found==false && ManifestSL.isStandardAttribute(attribute)) {
      rv=manifestsd.writeAttribute(entry,attribute,value,archiveentry);
    }
    else {
      rv=manifestex.writeAttribute(entry,attribute,value,archiveentry);
    }
    if (rv) {
    	filelistread=true;
      manifestread=true;
      changed=true;
    }
    return rv;
  }
  public boolean writeAttribute(String entry, String attribute, String value) {
    boolean rv;
    boolean found=false;
    if (entry.endsWith("/") || entry.endsWith("\\")) {
      found=true;
    }
    if (found==false && ManifestSL.isStandardAttribute(attribute)) {
      rv=manifestsd.writeAttribute(entry,attribute,value);
    }
    else {
      rv=manifestex.writeAttribute(entry,attribute,value);
    }
    if (rv) {
    	filelistread=true;
      manifestread=true;
      changed=true;
    }
    return rv;
  }
  /**
   * writes the attributes of the JarSLManifest object into the manifest
   */
  public boolean writeAttributesFromJarSLManifest(JarSLManifestIF jarslmf) {
    boolean rc=true;
    manifestsd=new ManifestSL(JARSL_MANIFEST);    /* standard manifest */
    manifestex=new ManifestSL(JARSL_SAP_MANIFEST);/* ext. manifest */
    changed=true;
    filelistread=true;
    manifestread=true;
    ManifestEntryIF[] mfentries=jarslmf.getAllManifestEntries();
    for (int i=0; mfentries!=null && i<mfentries.length && rc; ++i) {
      rc=writeAttribute(mfentries[i].getEntryName(),mfentries[i].getAttributeName(),mfentries[i].getAttributeValue(),mfentries[i].isArchiveEntry());
    }
    mfentries=jarslmf.getAllSapManifestEntries();
    for (int i=0; mfentries!=null && i<mfentries.length && rc; ++i) {
      rc=writeAttribute(mfentries[i].getEntryName(),mfentries[i].getAttributeName(),mfentries[i].getAttributeValue(),mfentries[i].isArchiveEntry());
    }
    return rc;
  }
  /**
   * removes the given attribute from the manifest
   */
  public boolean deleteAttribute(String entry, String attribute) {
    boolean rv;
    boolean found=false;
    if (!filelistread) {
      return false;
    }
    if (entry.endsWith("/") || entry.endsWith("\\")) {
      found=true;
    }
    if (found==false && ManifestSL.isStandardAttribute(attribute)) {
      rv=manifestsd.deleteAttribute(entry,attribute);
    }
    else {
      rv=manifestex.deleteAttribute(entry,attribute);
    }
    if (rv) {
      changed=true;
    }
    return rv;
  }
  /**
   * returns all entries form the manifest
   * @return ManifestEntryIF array
   */
  public ManifestEntryIF[] getAllManifestEntries() {
    if (!manifestread) {
      try {
        manifestsd.readAllAttributes(jarfilename);
        manifestex.readAllAttributes(jarfilename);
        manifestread=true;
      }
      catch (JarSLManifestException e) {
        manifestread=false;
        return null;
      }
    }
    return manifestsd.returnAllManifestEntries();
  }
  /**
   * returns all entries form the sap_manifest
   * @return ManifestEntryIF array
   */
  public ManifestEntryIF[] getAllSapManifestEntries() {
    if (!manifestread) {
      try {
        manifestsd.readAllAttributes(jarfilename);
        manifestex.readAllAttributes(jarfilename);
        manifestread=true;
      }
      catch (JarSLManifestException e) {
        manifestread=false;
        return null;
      }
    }
    return manifestex.returnAllManifestEntries();
  }
  /**
   * returns the corresponding manifest as stream
   */
  public InputStream getManifestAsStream(Vector errorTexts) {
    InputStream result=null;
    if (!manifestread) {
      try {
        manifestsd.readAllAttributes(jarfilename);
        manifestex.readAllAttributes(jarfilename);
        manifestread=true;
      }
      catch (JarSLManifestException e) {
        manifestread=false;
        if (errorTexts!=null) {
          errorTexts.add("Could not create manifest: "+e.getMessage());
        }
        return null;
      }
    }
    result=manifestsd.createManifest();
    if (result==null && errorTexts!=null) {
      errorTexts.add("Could not create manifest.");
    }
    return result;
  }
  /**
   * returns the corresponding sap_manifest as stream
   */
  public InputStream getSapManifestAsStream(Vector errorTexts) {
    InputStream result=null;
    if (!manifestread) {
      try {
        manifestsd.readAllAttributes(jarfilename);
        manifestex.readAllAttributes(jarfilename);
        manifestread=true;
      }
      catch (JarSLManifestException e) {
        manifestread=false;
        if (errorTexts!=null) {
          errorTexts.add("Could not create sap_manifest: "+e.getMessage());
        }
        return null;
      }
    }
    else if (changed) {
      deleteAttribute("",ATTJARSLVERSION);
      if (writeAttribute("",ATTJARSLVERSION,DEF_jarslversion)==false) {
        if (errorTexts!=null) {
          errorTexts.add("Could not write attribute to sapmanifest.");
        }
        return null;
      }
      deleteAttribute("",ATTSAPMANIFESTORIGIN);
      if (writeAttribute("",ATTSAPMANIFESTORIGIN,"jarsl(getSapManifestAsStream)")==false) {
        if (errorTexts!=null) {
          errorTexts.add("Could not write attribute to sapmanifest.");
        }
        return null;
      }  
    }
    result=manifestex.createManifest();
    if (result==null && errorTexts!=null) {
      errorTexts.add("Could not create sap_manifest.");
    }
    return result;
  }
//  /**
//   * This method writes an entry for every file contained in the filelist to the manifest
//   * 
//   */
//  public boolean createEntryAttributes() {
//    FileLoc nextfile;
//    String entryname;
//    if (!filelistread) {
//      return false;
//    }
//    for (int i=0; i<filelist.size(); ++i) {
//      nextfile=(FileLoc)filelist.elementAt(i);
//      if (nextfile.pathname().endsWith("/")==false) { /* do not calculate the fingerprint from a directory */
//        if (nextfile.archfilename()!=null) {
//          entryname=nextfile.archfilename();
//        }
//        else {
//          entryname=nextfile.fname();
//        }
//        if (writeAttribute(entryname,ATTCONTENT," ")==false) {
//          return false;
//        }
//      }
//    }
//    return true;
//  }
  /**
   * This method creates a md5 fingerprint from every file contained in the filelist and
   * writes it to the manifest. (attribute: md5_fingerprint)
   */
  public boolean createFingerPrintAttribute() {
    String fingerprint;
    FileLoc nextfile;
    String entryname;
    if (!filelistread) {
      return false;
    }
    for (int i=0; i<filelist.size(); ++i) {
      nextfile=(FileLoc)filelist.elementAt(i);
      if (nextfile.pathname().endsWith("/")==false) { /* do not calculate the fingerprint from a directory */
        fingerprint=(new FingerPrint(Conv.pathConv(nextfile.pathname()))).calcMD5();
        if (nextfile.archfilename()!=null) {
          entryname=nextfile.archfilename();
        }
        else {
          entryname=nextfile.fname();
        }
        if (fingerprint==null || writeAttribute(entryname,ATTJARSLFINGERPRINT,fingerprint)==false) {
          return false;
        }
      }
    }
    return true;
  }
  /**
   * This method checks if the current archive is uptodate
   */
  public boolean isCurrentArchiveUpToDate(long mintime) {
    boolean makenew=false;
    File oldjarsl=this.getArchiveName();
    long lastmodified=oldjarsl.lastModified();
    JarSL tmpjarsl=new JarSL(oldjarsl.getPath(),"");
    String[] content=tmpjarsl.getJarFileList();
    Vector contentv=new Vector();
    int jump=0;
    if (content==null) {
      return false;
    }
    for (int i=0; i<content.length; ++i) {
      if (!(content[i].equals(JARSL_METAINF+"/") || content[i].equals(JARSL_METAINF+"\\") || content[i].equals(JARSL_METAINF_MANIFEST) || content[i].equals(JARSL_METAINF_SAP_MANIFEST))) {
        contentv.addElement(content[i]);
      }
    }
    if (lastmodified<mintime) {
      makenew=true;
    }
    for (int i=0; i<filelist.size() && !makenew; ++i) {
      String name=((FileLoc)filelist.elementAt(i)).fname();
      if (((FileLoc)filelist.elementAt(i)).archfilename()!=null) {
        name=((FileLoc)filelist.elementAt(i)).archfilename();
      }
      if (name.equals(JARSL_METAINF+"/") || name.equals(JARSL_METAINF+"\\") || name.equals(JARSL_METAINF_MANIFEST) || name.equals(JARSL_METAINF_SAP_MANIFEST)) {
        ++jump;
        continue;
      }
      if (!contentv.contains(name)) {
        makenew=true;
      }
      else {
        if (new File(((FileLoc)filelist.elementAt(i)).pathname()).lastModified()>lastmodified) {
          makenew=true;
        }
      }
    }
    if (contentv.size()!=filelist.size()-jump) {
      makenew=true;
    }
    return !makenew;
  }
  /**
   * This method returns the files contained in the corresponding archive
   */
  public String[] getJarFileList() {
    String[] files=null;
    if (filelistread) { /* uses the filelist to create the list */
      files=new String[filelist.size()];
      for (int i=0; i<filelist.size(); ++i) {
        files[i]=new String(((FileLoc)filelist.elementAt(i)).fname());
      }
    }
    else { /* reads the archive to create the list */
      Vector filesv=new Vector();
      JarInputStream jis=null;
      JarEntry je;
      FileLoc name;
      try {
        jis=new JarInputStream(new BufferedInputStream(new FileInputStream(jarfilename_conv),65536));
        while ((je=jis.getNextJarEntry())!=null) {
          name=new FileLoc(rootdir,je.getName());
          filesv.addElement(name);
        }
        if (jis.getManifest()!=null) {
          if (filesv.contains(new FileLoc(rootdir,JARSL_METAINF_MANIFEST))==false) {
            filesv.addElement(new FileLoc(rootdir,JARSL_METAINF_MANIFEST));
          }
        }
        jis.close();
        jis=null;
      }
      catch (Exception e) {
        filesv.removeAllElements();
      }
      finally {
        try {
          if (jis!=null) {
            jis.close();
          }
        }
        catch (IOException e) {
          // $JL-EXC$
        }
      }
      if (filesv.size()>0) {
        files=new String[filesv.size()];
        for (int i=0; i<filesv.size(); ++i) {
          files[i]=new String(((FileLoc)filesv.elementAt(i)).fname());
        }
      }
    }
    return files;
  }
//  /**
//   * The two manifests of every archive contained in the filelist is read and added
//   * to the current manifest. 
//   */
//  public boolean addArchiveManifestsToManifest() {
//    return addArchiveManifestsToManifest(null);
//  }
//  public boolean addArchiveManifestsToManifest(Vector errorTexts) {
//    boolean archive;
//    boolean rc=true;
//    FileLoc file;
//    ManifestSL mfsd=new ManifestSL(JARSL_MANIFEST);
//    ManifestSL mfex=new ManifestSL(JARSL_SAP_MANIFEST);
//    for (int i=0; i<filelist.size(); ++i) {
//      archive=false;
//      Vector subErrors = new Vector(2);
//      file=(FileLoc)filelist.elementAt(i); 
//      try {
//        JarInputStream jis=new JarInputStream(new BufferedInputStream(new FileInputStream(Conv.pathConv(file.pathname()))));
//        if (jis.getManifest()!=null || jis.getNextJarEntry()!=null) {
//          archive=true;
//        }
//        jis.close();
//      }
//      catch (Exception e) {
//      }
//      if (archive) {     
////          if (file.fname().toLowerCase().endsWith(JarSL.archiveends[j])) {
////          archive=true; /* we found an archive */
//        if (!mfsd.readAllAttributes(file.pathname(),subErrors)) {  /* reads the standard manifest of the found archive */
//          if (null != errorTexts) {
//            errorTexts.add("Could not read manifest " + file.pathname());
//            Iterator iter=subErrors.iterator();
//            if (iter != null) {
//              errorTexts.add("\n   Details:\n");
//              while (iter.hasNext()) {
//                errorTexts.add("      "+(String)iter.next());
//              }
//            }
//          }
//        }
//        mfex.readAllAttributes(file.pathname());         /* reads the ext. manifest of the found archive */
//        // now the two manifests are added to the current manifest
//        if (file.archfilename()==null) {
//          if (!manifestex.addManifest(mfsd,file.fname()+"/") || !manifestex.addManifest(mfex,file.fname()+"/")) { E R R O R because "/" is only allowed for deployarchives
//            if (null != errorTexts) {
//              errorTexts.add("Could not add one of the sub-manifests of "+file.fname()+"to the manifest.");
//              rc = false;
//            } else {
//              return false;
//            }
//          }
//        }
//        else {
//          if (!manifestex.addManifest(mfsd,file.archfilename()+"/") || !manifestex.addManifest(mfex,file.archfilename()+"/")) {
//            if (null != errorTexts) {
//              errorTexts.add("Could not add one of the sub-manifests of "+file.archfilename()+"to the manifest.");
//              rc = false;
//            } else {
//              return false;
//            }
//          }
//        }
//      }
//    //}
//    }
//    return rc;
//  }
  /**
   * This method scans the directory structure beginning with the first argument as path and
   * adds the found files to the filelist. With the second argument it is possible to specify
   * a base-directory to which the found files are added to the archive relatively.
   */
  private void getFileList(String files) throws IOException {
    getFileList(files,files);
  }
  private void getFileList(String files, String basedir) throws IOException {
    files=Conv.pathConvJar(new File(files).getCanonicalPath());
    basedir=Conv.pathConvJar(new File(basedir).getCanonicalPath());
    File df=new File(Conv.pathConv(files));
    while (files.endsWith("/")) {
      files=files.substring(0,files.length()-1);
    }
    while (basedir.endsWith("/")) {
      basedir=basedir.substring(0,basedir.length()-1);
    }
    if (df.isDirectory()) {
      if (files.compareTo(basedir)!=0) {
        filelist.add(new FileLoc(basedir,files.substring(basedir.length()+1)+"/"));
      }
      String[] dflist=df.list();
      for (int i=0; dflist!=null && i<dflist.length; ++i) {
        if (df.getPath().endsWith(File.separator)) {
          dflist[i]=Conv.pathConvJar(df.getPath()+dflist[i]);
        }
        else {
          dflist[i]=Conv.pathConvJar(df.getPath()+File.separator+dflist[i]);
        }
        if ((new File(Conv.pathConv(dflist[i])).isDirectory())) {
          getFileList(dflist[i],basedir);
        }
        else {
          filelist.add(new FileLoc(basedir,dflist[i].substring(basedir.length()+1)));
        }
      }
    }
    else {
      if (df.canRead()) {
        filelist.add(new FileLoc(basedir,files.substring(basedir.length()+1)));
      }
    }
  }
  /**
  * Delete this File, even if is a not empty directory
  */
  private void deleteFile(File file) {
    if ((file != null) && file.exists()) {
      if (file.isDirectory()) {
        File[] subFiles = file.listFiles();
        for (int i=0; subFiles!=null && i<subFiles.length; ++i) { 
          deleteFile(subFiles[i]);
        }
      }
      file.delete();
    }
  }
  /**
   * This method calculates a MD5 fingerprint from a given file
   * @param String filename             the name of the file to be created  
   * @return String                     the MD5 fingerprint
   */
  static String getMD5FingerPrintFromGivenFile(String filename) {
    return new FingerPrint(Conv.pathConv(filename)).calcMD5();
  }
  // static I/O methods 
  /**
   * This method writes a file from a given InputStream
   * @param InputStream in              the InputStream
   * @param String filename             the name of the file to be created  
   * @param Vector errorTexts           contains the error message as String objects       
   * @return boolean                    true: file created; false: file not created, see errorTexts
   */
  static boolean writeFileFromInputStream(InputStream in, String filename, Vector errorTexts) {
    log.entering("writeFileFromInputStream: "+filename);
    BufferedOutputStream buffout=null;
    boolean rc=true;
    try {		
  		filename=Conv.pathConv(filename);
      if ((new File(filename).getParentFile())!=null) {
      	(new File(filename).getParentFile()).mkdirs();
      }
      (new File(filename)).delete();
  		buffout=new BufferedOutputStream(new FileOutputStream(filename),65536);
  		int len;
      int tlen=0;
  		byte[] buff=new byte[65536];
      log.debug("start of file writing");    
  		while ((len=in.read(buff))>=0) {
        tlen+=len;
    		buffout.write(buff,0,len);
    	}
      log.debug(tlen+" bytes were written");
    	buffout.close();
      buffout=null;
  	}
  	catch (Exception e) {
      log.fatal("Error creating file "+filename+" from InputStream",e);
  		errorTexts.add("Error creating file "+filename+" from InputStream: "+e.getMessage());
  		rc=false;
  	} 	
    finally {
      try {
        if (in!=null) {
          in.close();
        }
      }
      catch (IOException e) {
        // $JL-EXC$
      }
      try {
        if (buffout!=null) {
          buffout.close();
        }
      }
      catch (IOException e) {
        // $JL-EXC$
      }
    }
    log.exiting("writeFileFromInputStream");
    return rc;
  }
  /**
   * This method returns an OutputStream for a opened file with given filename
   * @param String filename             the name of the file to be created  
   * @param Vector errorTexts           contains the error message as String objects
   * @return OutputStream
   */
  static OutputStream writeFile(String filename, Vector errorTexts) {
    log.entering("writeFile: "+filename);
    OutputStream os=null;
    try {
      filename=Conv.pathConv(filename);
      if ((new File(filename).getParentFile())!=null) {
        (new File(filename).getParentFile()).mkdirs();
      }
      (new File(filename)).delete();
      os=new FileOutputStream(filename);
    }
    catch (Exception e) {
      log.fatal("Error creating file "+filename,e);
      errorTexts.add("Error creating file "+filename +": "+e.getMessage());
      try {
        if (os!=null) {
          os.close();
        }
      }
      catch (Exception e1) {
        // $JL-EXC$
      }
      os=null;
    }
    log.exiting("writeFile"); 
    return os;
  }
  /**
   * This method opens a given file and returns an InputStream
   * @param String filename             the name of the file to be read
   * @param Vector errorTexts           contains the error message as String objects
   * @return InputStream
   */
  static InputStream readFileAsInputStream(String filename, Vector errorTexts) {
    log.entering("readFileAsInputStream: "+filename);
  	try {
  		return new FileInputStream(Conv.pathConv(filename));
  	}
  	catch (Exception e) {
      log.fatal("Error reading file "+filename,e);
  		errorTexts.add("Error reading file "+filename+": "+e.getMessage());
  		return null;
  	} 	
    finally {
      log.exiting("readFileAsInputStream");
    }
  }  
  /**
   * This method returns the length of the corresponding file
   * @param String filename             the name of the file
   * @param Vector errorTexts           contains the error message as String objects
   * @return long                       the file length
   */
  static long getFileLength(String filename, Vector errorTexts) {
    log.entering("getFileLength: "+filename);
  	try {
  		return (new File(Conv.pathConv(filename))).length();
  	}
  	catch (Exception e) {
      log.fatal("Cannot get file-length from file "+filename,e);
  		errorTexts.add("Cannot get file-length from file "+filename+": "+e.getMessage());
  		return -1;
  	} 
    finally {
      log.exiting("getFileLength");
    }
  }
  /**
   * This method creates the given directory, including necessary subdirectories
   * @param String directory            the name of the directory to be created
   */
  static void createDirectory(String directory) {
    log.entering("createDirectory: "+directory);
    (new File(Conv.pathConv(directory))).mkdirs();
    log.exiting("createDirectory");
  }
  /**
   * This method checks if the given file exists
   * @param String filename             the name of the file
   * @return boolean                    true: file exists; false: file does not exist
   */
  static boolean checkFileExistence(String filename) {
    log.entering("checkFileExistence: "+filename);
    boolean exists=false;
    exists=(new File(Conv.pathConv(filename))).exists();
    log.exiting("checkFileExistence: "+exists);
    return exists;
  }
  /**
   * This method deletes a given file or directory
   * @param String filedirname          the name of the file or directory to be deleted
   */
  static void deleteFileOrDirectory(String filedirname) {
    log.entering("deleteFileOrDirectory: "+filedirname);
    JarSL jarsl=new JarSL("","");
    jarsl.deleteFile(new File(filedirname));
    log.exiting("deleteFileOrDirectory");
  }
  /**
   * This method scans a given directory for archives and returns their names in a
   * string array.
   * @param String filename             the name of the directory
   * @return String[]                   array list of found archives or NULL if directory does not contain any archives
   */
  static String[] getArchivesFromDirectory(String directory) {
    return getArchivesFromDirectory(directory,false);
  }
  static String[] getArchivesFromDirectory(String directory, boolean recursive) {
    log.entering("getArchivesFromDirectory: "+directory+ " / recursive = "+(recursive?"true":"false"));
    Vector foundarcs=new Vector();
    String[] result=null;
    try {
      getArchivesFromDirectory(directory,recursive,foundarcs);
      result=(String[])foundarcs.toArray(new String[0]);
    }
    catch (JarSLInternalException e) {
      log.debug(e);
      result=null;
    }
    if (result!=null) {
      log.exiting("getArchivesFromDirectory: found "+result.length+" archives");
    }
    else {
      log.exiting("getArchivesFromDirectory: null");
    }
    return result;
  }
  private static void getArchivesFromDirectory(String directory, boolean recursive, Vector foundarcs) throws JarSLInternalException {
    File df=new File(Conv.pathConv(directory));
    if (!df.exists()) {
      throw new JarSLInternalException("directory ("+directory+") does not exist on filesystem");
    }
    else if (!df.canRead()) {
      throw new JarSLInternalException("directory ("+directory+") cannot be read from filesystem");
    }
    else if (!df.isDirectory()) {
      throw new JarSLInternalException(directory+" is not a directory"); 
    }
    String[] dflist=df.list();
    for (int i=0; dflist!=null && i<dflist.length; ++i) {
      dflist[i]=Conv.pathConvJar(df.getPath()+File.separator+dflist[i]);
      log.debug("found file "+dflist[i]);
      if (!(new File(Conv.pathConv(dflist[i])).isDirectory())) {
        boolean isarchive=false; 
        JarInputStream jis=null;   
        try {
          jis=new JarInputStream(new BufferedInputStream(new FileInputStream(Conv.pathConv(dflist[i]))));
          if (jis.getManifest()!=null || jis.getNextJarEntry()!=null) {
            isarchive=true;
            log.debug(dflist[i]+" is archive");
          }
          jis.close();
          jis=null;
        }
        catch (Exception e) {
          throw new JarSLInternalException("an exception occurred during directory reading: "+e.getMessage());
        }   
        finally {
          try {
            if (jis!=null) {
              jis.close();
            }
          }
          catch (IOException e) {
            // $JL-EXC$
          }
        }      
        if (isarchive) {
          foundarcs.addElement(dflist[i]);
        }
      }
      else if (recursive) {
        getArchivesFromDirectory(dflist[i],recursive,foundarcs);       
      }
    } 
  }
  /**
   * This method renames a given file
   * @param String sourcefilename       the name of the file to be renamed
   * @param String targetfilename       the new file name
   * @return boolean                    true or false
   */
  static boolean renameFile(String sourcefilename, String targetfilename) {
    log.entering("renameFile: "+sourcefilename+", "+targetfilename);
    File source=new File(sourcefilename);
    File target=new File(targetfilename);
    boolean renamedone=false;
    if (target.isDirectory()) {
      target.mkdirs();
    }
    else {
      (new File(target.getParent())).mkdirs();
    }
    try {
      if (source.renameTo(target)) {
        log.debug("file renamed");
        renamedone=true;
      }
    }
    catch (Throwable e) {
	  // $JL-EXC$
    }
    if (!renamedone) {
      JarSL jarsl=new JarSL(source.getPath(),"");
      if (jarsl.copyArchive(target.getPath())) {
        log.debug("file copied");
        if (source.delete()) {
          log.debug("source file deleted");
          renamedone=true;
        }
      }
    } 
    log.exiting("renameFile: "+renamedone);
    return renamedone;
  } 
}
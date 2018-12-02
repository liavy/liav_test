package com.sap.sl.util.jarsl.impl;

/**
 * Title:
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
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Vector;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import com.sap.sl.util.jarsl.api.ConstantsIF;
import com.sap.sl.util.jarsl.api.Conv;
import com.sap.sl.util.jarsl.api.JarSLManifestException;
import com.sap.sl.util.jarsl.api.ManifestEntryIF;
import com.sap.sl.util.logging.api.SlUtilLogger;

final class ManifestSL implements ConstantsIF {
  private static final SlUtilLogger log = SlUtilLogger.getLogger(ManifestSL.class.getName());
  
  private Vector manifest;      /* every manifest entry is added to this vector */
  private String manifestname;  /* name of the manifest */
  ManifestSL(String manifestname) {
    this.manifestname=manifestname;
    manifest=new Vector();
  }
  /**
   * clears the manifest
   */
  boolean removeAllElements() {
    manifest.removeAllElements();
    return true;
  }
  ByteArrayInputStream createManifest() {
    return createManifest(null,false);
  }
  boolean createManifest(String rootdir) {
    if (createManifest(rootdir,true)==null) {
      return false;
    }
    else {
      return true;
    }
  }
  /**
   * This method creates the manifest file with the content given by the corresponding manifest vector.
   * If the second argument is true then the created manifest is written to the file-system
   * (rootdir+"/META-INF/"+manifestname). Also the method returns the manifest as ByteArrayInputStream.
   */
  private ByteArrayInputStream createManifest(String rootdir, boolean filewrite) {
    ManifestEntry mfe;
    if (readAttribute("",ATTMANIFESTVERSION)==null) {
      if (writeAttribute("",ATTMANIFESTVERSION,"1.0")==false) {
        return null;
      }
    }
    ByteArrayOutputStream baos=null;
    ByteArrayInputStream bais=null;
    BufferedOutputStream bos=null;
    DataOutputStream dosf=null;
    DataOutputStream dosa=null;
    try {
      if (filewrite) {
        (new File(Conv.pathConv(rootdir+"/"+JARSL_METAINF+"/"+manifestname))).delete();      // gefaehrlich
        (new File(Conv.pathConv(rootdir+"/"+JARSL_METAINF))).mkdirs();
        bos=new BufferedOutputStream(new FileOutputStream(Conv.pathConv(rootdir+"/"+JARSL_METAINF+"/"+manifestname)));    // write it to the file-system
      }
      boolean namewritten;
      baos=new ByteArrayOutputStream();
      dosa=new DataOutputStream(baos);  // ArrayStream
      if (filewrite) {
        dosf=new DataOutputStream(bos);       // file-system
      }
      Vector entries=new Vector();
      String name;
      entries.addElement("");
      // attributes belonging to the same entry are grouped together
      for (int i=0;i<manifest.size();++i) {
        if (!entries.contains(((ManifestEntry)manifest.elementAt(i)).getEntryName())) {
          entries.addElement(((ManifestEntry)manifest.elementAt(i)).getEntryName());
        }
      }
      for (int i=0;i<entries.size();++i) {
        name=(String)entries.elementAt(i);
        namewritten=false;
        for (int ii=0; ii<manifest.size();++ii) {
          mfe=(ManifestEntry)manifest.elementAt(ii);
          if (mfe.getEntryName().compareTo(name)==0) {
            if (!namewritten) {
              if (name.compareTo("")!=0) {
                dosa.writeBytes(ATTNAME+name);
                dosa.writeByte('\n');
                if (filewrite) {
                  dosf.writeBytes(ATTNAME+name);
                  dosf.writeByte('\n');
                }
              }
              namewritten=true;
            }
            dosa.writeBytes(mfe.getAttributeName()+": "+mfe.getValueToWrite());
            dosa.writeByte('\n');
            if (filewrite) {
              dosf.writeBytes(mfe.getAttributeName()+": "+mfe.getValueToWrite());
              dosf.writeByte('\n');
            }
          }
        }
        if (namewritten) {
          dosa.writeBytes("");
          dosa.writeByte('\n');
          if (filewrite) {
            dosf.writeBytes("");
            dosf.writeByte('\n');
          }
        }
      }
      if (filewrite) {
        dosf.close();
        dosf=null;
        bos.close();
        bos=null;
      }
      dosa.close();
      dosa=null;
      bais=new ByteArrayInputStream(baos.toByteArray());
      baos.close();
      baos=null;
    }
    catch (Exception e) {
      if (bais!=null) {
        try {
          bais.close();
        }
        catch (IOException e1) {
          // $JL-EXC$
        }   
      }
      bais=null;
    }
    finally {
      try {  
        if (dosf!=null) {
          dosf.close();
        }
      }
      catch (IOException e) {
        // $JL-EXC$
      }
      try {
        if (bos!=null) {
          bos.close();
        }
      }
      catch (IOException e) {
        // $JL-EXC$
      }
      try {
        if (dosa!=null) {
          dosa.close();
        }
      }
      catch (IOException e) {
        // $JL-EXC$
      }
      try {
        if (baos!=null) {
          baos.close();
        }
      }
      catch (IOException e) {
        // $JL-EXC$
      }
    }
    return bais;
  }
  boolean readAllAttributes(ByteArrayInputStream bais) throws JarSLManifestException {
    return readAllAttributes(bais,null,null);
  }
  boolean readAllAttributes(String inputname) throws JarSLManifestException {
    return readAllAttributes(null,inputname,null);
  }
  boolean readAllAttributes(ByteArrayInputStream bais,Vector errorTexts) throws JarSLManifestException {
    return readAllAttributes(bais,null,errorTexts);
  }
  boolean readAllAttributes(String inputname,Vector errorTexts) throws JarSLManifestException {
    return readAllAttributes(null,inputname,errorTexts);
  }
  /**
   * This method fills the manifest vector by reading the corresponding manifest file from
   * the given InputStream which must represent the manifest file. The stream is not closed here.
   */
  boolean readAllAttributesFromManifestInputStream(InputStream manifeststream, Vector errorTexts) throws JarSLManifestException {
    Attributes attr;
    Object[] attributes;
    Object[] entries;
    Map mp;
    Manifest mf=null;
    removeAllElements();
    if (manifest==null) {
      throw new JarSLManifestException("no manifest is defined");
    }
    BufferedInputStream bis=null;
    try {
      bis=new BufferedInputStream(manifeststream);
      mf=new Manifest(bis);
      if (mf==null && errorTexts!=null) {
        errorTexts.add("could not create java.util.jar.Manifest from given stream");
      }
      bis.close();
      bis=null;
    }
    catch (Exception e) {
      throw new JarSLManifestException("received exception: "+e.getMessage());
    }
    finally {
      try {
        if (bis!=null) {
          bis.close();
        }
      }
      catch (IOException e) {
        // $JL-EXC$
      }
    }
    /* now the manifest entries and attributes are extracted and added to the manifest vector */
    if (mf!=null) {
      attr=mf.getMainAttributes();
      attributes=attr.keySet().toArray();
      for (int i=0; i<attributes.length; ++i) {
        manifest.addElement(new ManifestEntry(attributes[i].toString(),attr.getValue(attributes[i].toString()),""));
      }
      mp=mf.getEntries();
      entries=mp.keySet().toArray();
      for (int i=0; i<entries.length; ++i) {
        attr=mf.getAttributes(entries[i].toString());
        attributes=attr.keySet().toArray();
        for (int ii=0; ii<attributes.length; ++ii) {
          manifest.addElement(new ManifestEntry(attributes[ii].toString(),attr.getValue(attributes[ii].toString()),entries[i].toString()));
        }
      }
      return true;
    }
    else {
      return false;
    }
  }
  /**
   * This method fills the manifest vector by reading the corresponding manifest file from
   * the file (second argument is the name of the file) or by reading it from the given
   * ByteArrayInputStream (first argument) which must represent an archive file.
   */
  private boolean readAllAttributes(ByteArrayInputStream bais, String inputname) throws JarSLManifestException {
    return  readAllAttributes(bais,inputname,null);
  }
	private boolean readAllAttributes(ByteArrayInputStream bais, String inputname,Vector errorTexts) throws JarSLManifestException {
    log.entering("readAllAttributes: "+inputname);
		String localplattform=System.getProperty("os.name")+System.getProperty("os.arch");
    log.debug("localplatform = "+localplattform);
    String inputname_conv=Conv.pathConv(inputname);
		if (!localplattform.toLowerCase().startsWith("Windows".toLowerCase())) {
			//		==> due to SUN bug 4705373 the usage of JarFile causes an out_of_memory error on linux 
      log.debug("no direct manifest access");
			Attributes attr;
			Object[] attributes;
			Object[] entries;
			Map mp;
			Manifest mf=null;
			removeAllElements();
			if (bais==null && inputname==null) {
        log.debug("no parameters given");
        throw new JarSLManifestException("no parameters given");
			}
			JarInputStream jis=null;
			BufferedInputStream bis=null;
			try {
				if (bais!=null) {
          log.debug("reading from bytearrayinputstream");
					if (manifestname.compareTo(JARSL_MANIFEST)==0) {
            log.debug("looking for "+JARSL_MANIFEST);
						jis=new JarInputStream(bais);
						mf=jis.getManifest();
						if (mf==null) {
              log.debug("rc of .getManifest() is null");
							JarEntry je;
							while ((je=jis.getNextJarEntry())!=null && je.getName().compareTo(JARSL_METAINF+"/"+manifestname)!=0);
							if (je!=null){
                log.debug("entry name is "+je.getName());
								mf=new Manifest(jis);
                if (mf==null) {
                  log.debug("could not create java.util.jar.Manifest from JarInputStream (created from ByteArrayInputStream)."+
                  " (debuginfo: manifestname="+manifestname+",JARSL_METAINF="+JARSL_METAINF+")");
                  if (null!=errorTexts) {
                    errorTexts.add("could not create java.util.jar.Manifest from JarInputStream (created from ByteArrayInputStream)."+
                    " (debuginfo: manifestname="+manifestname+",JARSL_METAINF="+JARSL_METAINF+")");
                  }
								}
							}
							else {
                log.debug("no entry \""+JARSL_METAINF+"/"+manifestname+"\" found."+
                " (debuginfo: manifestname="+manifestname+",JARSL_METAINF="+JARSL_METAINF+")");
								if (null != errorTexts) {
									errorTexts.add("no entry \""+JARSL_METAINF+"/"+manifestname+"\" found."+
									" (debuginfo: manifestname="+manifestname+",JARSL_METAINF="+JARSL_METAINF+")");
								}
							}
						}
						jis.close();
						jis=null;
					}
					else {
						JarEntry je;
						jis=new JarInputStream(bais);
						while ((je=jis.getNextJarEntry())!=null && je.getName().compareTo(JARSL_METAINF+"/"+manifestname)!=0);
						if (je!=null){
              log.debug("entry name is "+je.getName());
							mf=new Manifest(jis);
              if (mf==null) {
                log.debug("could not create java.util.jar.Manifest from JarInputStream (created from ByteArrayInputStream)."+
                " (debuginfo: manifestname="+manifestname+",JARSL_METAINF="+JARSL_METAINF+")");
                if (null!=errorTexts) {
                  errorTexts.add("could not create java.util.jar.Manifest from JarInputStream (created from ByteArrayInputStream)."+
                  " (debuginfo: manifestname="+manifestname+",JARSL_METAINF="+JARSL_METAINF+")");
                }
              }
						}
						else {
              log.debug("no entry \""+JARSL_METAINF+"/"+manifestname+"\" found."+
              " (debuginfo: manifestname="+manifestname+",JARSL_METAINF="+JARSL_METAINF+")");
							if (null != errorTexts) {
								errorTexts.add("no entry \""+JARSL_METAINF+"/"+manifestname+"\" found."+
								" (debuginfo: manifestname="+manifestname+",JARSL_METAINF="+JARSL_METAINF+")");
							}
						}
						jis.close();
						jis=null;
					}
				}
				else {
          log.debug("check if second argument is an archive file name");
					// is the second argument a archive file name
					boolean archive=false;
          if (!new File(inputname_conv).isDirectory() && !archive) {
						try {
							jis=new JarInputStream(new BufferedInputStream(new FileInputStream(inputname_conv)));
							if (jis.getManifest()!=null || jis.getNextJarEntry()!=null) {
								archive=true;
							}
							jis.close();
							jis=null;
						}
						catch (Exception e) {
              log.debug("exception during archive check: ",e);
							try {
								 if (jis!=null) {
									 jis.close();
									 jis=null;
								 }
							}
							catch (IOException ee) {
							 // $JL-EXC$
							}
							throw new JarSLManifestException("exception during archive check: "+e.getMessage());
						}
					}
					if (archive) {
            log.debug("found an archive");
						if (manifestname.compareTo(JARSL_MANIFEST)==0) {
              log.debug("looking for "+JARSL_MANIFEST);
							jis=new JarInputStream(new BufferedInputStream(new FileInputStream(inputname_conv)));
							mf=jis.getManifest();
							if (mf==null) {
                log.debug("rc of .getManifest() is null");
								JarEntry je;
								while ((je=jis.getNextJarEntry())!=null && je.getName().compareTo(JARSL_METAINF+"/"+manifestname)!=0);
								if (je!=null){
                  log.debug("entry name is "+je.getName());
									mf=new Manifest(jis);
									if (mf==null) {
                    log.debug("could not create java.util.jar.Manifest from file "+inputname+"."+
                    " (debuginfo: manifestname="+manifestname+",JARSL_METAINF="+JARSL_METAINF+")");
                    if (null!=errorTexts) {
										  errorTexts.add("could not create java.util.jar.Manifest from file "+inputname+"."+
										  " (debuginfo: manifestname="+manifestname+",JARSL_METAINF="+JARSL_METAINF+")");
                    }
									}
								}
								else {
                  log.debug("no entry \""+JARSL_METAINF+"/"+manifestname+"\" found in file "+inputname+"."+
                  " (debuginfo: manifestname="+manifestname+",JARSL_METAINF="+JARSL_METAINF+")");
									if (null != errorTexts) {
										errorTexts.add("no entry \""+JARSL_METAINF+"/"+manifestname+"\" found in file "+inputname+"."+
										" (debuginfo: manifestname="+manifestname+",JARSL_METAINF="+JARSL_METAINF+")");
									}
								}
							}
							jis.close();
							jis=null;
						}
						else {
							JarEntry je;
							jis=new JarInputStream(new BufferedInputStream(new FileInputStream(inputname_conv)));
							while ((je=jis.getNextJarEntry())!=null && je.getName().compareTo(JARSL_METAINF+"/"+manifestname)!=0);
							if (je!=null){
                log.debug("entry name is "+je.getName());
								mf=new Manifest(jis);
								if (mf==null) {
                  log.debug("could not create java.util.jar.Manifest from file "+inputname+"."+
                  " (debuginfo: manifestname="+manifestname+",JARSL_METAINF="+JARSL_METAINF+")");
                  if (null != errorTexts) {
									 errorTexts.add("could not create java.util.jar.Manifest from file "+inputname+"."+
									 " (debuginfo: manifestname="+manifestname+",JARSL_METAINF="+JARSL_METAINF+")");
                  }
								}
							}
							else {
                log.debug("no entry \""+JARSL_METAINF+"/"+manifestname+"\" found in file "+inputname+"."+
                " (debuginfo: manifestname="+manifestname+",JARSL_METAINF="+JARSL_METAINF+")");
								if (null != errorTexts) {
									errorTexts.add("no entry \""+JARSL_METAINF+"/"+manifestname+"\" found in file "+inputname+"."+
									" (debuginfo: manifestname="+manifestname+",JARSL_METAINF="+JARSL_METAINF+")");
								}
							}
							jis.close();
							jis=null;
						}
					}
					else {
            log.debug("no archive was found");
            if (new File(Conv.pathConv(inputname+"/"+JARSL_METAINF+"/"+manifestname)).exists()) {
  						bis=new BufferedInputStream(new FileInputStream(Conv.pathConv(inputname+"/"+JARSL_METAINF+"/"+manifestname)));
  						mf=new Manifest(bis);
  						if (mf==null) {
                log.debug("Could not create java.util.jar.Manifest from file \"" + inputname+"/"+JARSL_METAINF+"/"+manifestname + "\"");
                if (null != errorTexts) {
  							  errorTexts.add("Could not create java.util.jar.Manifest from file \"" + inputname+"/"+JARSL_METAINF+"/"+manifestname + "\"");
                }
              }
  						bis.close();
  						bis=null;
            }
            else {
              log.debug(Conv.pathConv(inputname+"/"+JARSL_METAINF+"/"+manifestname)+" does not exist");
              mf=null;
            }
					}
				}
			}
			catch (Exception e) {
        log.debug("received exception",e);
        throw new JarSLManifestException("received exception: "+e.getMessage());
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
				try {
					if (bis!=null) {
						bis.close();
					}
				}
				catch (IOException e) {
					// $JL-EXC$
				}
			}
			/* now the manifest entries and attributes are extracted and added to the manifest vector */
			if (mf!=null) {
        log.debug("manifest entries and attributes are extracted and added to the manifest vector");
				attr=mf.getMainAttributes();
				attributes=attr.keySet().toArray();
				for (int i=0; i<attributes.length; ++i) {
					manifest.addElement(new ManifestEntry(attributes[i].toString(),attr.getValue(attributes[i].toString()),""));
				}
				mp=mf.getEntries();
				entries=mp.keySet().toArray();
				for (int i=0; i<entries.length; ++i) {
					attr=mf.getAttributes(entries[i].toString());
					attributes=attr.keySet().toArray();
					for (int ii=0; ii<attributes.length; ++ii) {
						manifest.addElement(new ManifestEntry(attributes[ii].toString(),attr.getValue(attributes[ii].toString()),entries[i].toString()));
					}
				}
        log.exiting("readAllAttributes: true");
				return true;
			}
			else {
        log.exiting("readAllAttributes: false");
				return false;
			}
		}
		else {
      log.debug("direct manifest access");
			Attributes attr;
			Object[] attributes;
			Object[] entries;
			Map mp;
			Manifest mf=null;
			removeAllElements();
			if (bais==null && inputname==null) {
        log.debug("no parameters given");
        throw new JarSLManifestException("no parameters given");
			}
			JarInputStream jis=null;
			BufferedInputStream bis=null;
			JarFile jf=null;
			InputStream is=null;
			try {
				if (bais!=null) {
          log.debug("reading from bytearrayinputstream");
					if (manifestname.compareTo(JARSL_MANIFEST)==0) {
            log.debug("looking for "+JARSL_MANIFEST);
						jis=new JarInputStream(bais);
						mf=jis.getManifest();
						if (mf==null) {
              log.debug("rc of .getManifest() is null");
							JarEntry je;
							while ((je=jis.getNextJarEntry())!=null && je.getName().compareTo(JARSL_METAINF+"/"+manifestname)!=0);
							if (je!=null){
                log.debug("entry name is "+je.getName());
								mf=new Manifest(jis);
								if (mf==null) {
                  log.debug("could not create java.util.jar.Manifest from JarInputStream (created from ByteArrayInputStream)."+
                  " (debuginfo: manifestname="+manifestname+",JARSL_METAINF="+JARSL_METAINF+")");
                  if (null != errorTexts) {
									  errorTexts.add("could not create java.util.jar.Manifest from JarInputStream (created from ByteArrayInputStream)."+
									  " (debuginfo: manifestname="+manifestname+",JARSL_METAINF="+JARSL_METAINF+")");
                  }
								}
							}
							else {
                log.debug("no entry \""+JARSL_METAINF+"/"+manifestname+"\" found."+
                " (debuginfo: manifestname="+manifestname+",JARSL_METAINF="+JARSL_METAINF+")");
								if (null != errorTexts) {
									errorTexts.add("no entry \""+JARSL_METAINF+"/"+manifestname+"\" found."+
									" (debuginfo: manifestname="+manifestname+",JARSL_METAINF="+JARSL_METAINF+")");
								}
							}
						}
						jis.close();
						jis=null;
					}
					else {
						JarEntry je;
						jis=new JarInputStream(bais);
						while ((je=jis.getNextJarEntry())!=null && je.getName().compareTo(JARSL_METAINF+"/"+manifestname)!=0);
						if (je!=null){
              log.debug("entry name is "+je.getName());
							mf=new Manifest(jis);
							if (mf==null) {
                log.debug("could not create java.util.jar.Manifest from JarInputStream (created from ByteArrayInputStream)."+
                " (debuginfo: manifestname="+manifestname+",JARSL_METAINF="+JARSL_METAINF+")");
                if (null != errorTexts) {
								  errorTexts.add("could not create java.util.jar.Manifest from JarInputStream (created from ByteArrayInputStream)."+
								  " (debuginfo: manifestname="+manifestname+",JARSL_METAINF="+JARSL_METAINF+")");
                }
							}
						}
						else {
              log.debug("no entry \""+JARSL_METAINF+"/"+manifestname+"\" found."+
              " (debuginfo: manifestname="+manifestname+",JARSL_METAINF="+JARSL_METAINF+")");
							if (null != errorTexts) {
								errorTexts.add("no entry \""+JARSL_METAINF+"/"+manifestname+"\" found."+
								" (debuginfo: manifestname="+manifestname+",JARSL_METAINF="+JARSL_METAINF+")");
							}
						}
						jis.close();
						jis=null;
					}
				}
				else {
					// is the second argument a archive file name
          log.debug("check if second argument is an archive file name");
					boolean archive=false;
          if (!new File(inputname_conv).isDirectory() && !archive) {
						try {
							jis=new JarInputStream(new BufferedInputStream(new FileInputStream(inputname_conv)));
							if (jis.getManifest()!=null || jis.getNextJarEntry()!=null) {
								archive=true;
							}
							jis.close();
							jis=null;
						}
						catch (Exception e) {
              log.debug("exception during archive check: ",e);
							try {
								 if (jis!=null) {
									 jis.close();
									 jis=null;
								 }
							}
							catch (IOException ee) {
							 // $JL-EXC$
							}
              throw new JarSLManifestException("exception during archive check: "+e.getMessage());
						}
					}
					if (archive) {
            log.debug("found an archive");
            JarEntry jey=null;
						jf=new JarFile(inputname_conv);
						if (manifestname.compareTo(JARSL_MANIFEST)==0) {
              log.debug("looking for "+JARSL_MANIFEST);
							mf=jf.getManifest();
						}
						if (mf==null) {
              log.debug("rc of .getManifest() is null");
							try {
                jey=jf.getJarEntry(JARSL_METAINF+"/"+manifestname);
                if (jey!=null) {
								  is=jf.getInputStream(jey);
                }
                else {
                  is=null;
                }
							}
							catch (IOException e) {
                log.debug("received exception: ",e);
                throw new JarSLManifestException("received exception: "+e.getMessage());
							}
							if (is!=null) {
								mf=new Manifest(is);  
								is.close();
								is=null;       
							}
							else {
                log.debug("no entry \""+JARSL_METAINF+"/"+manifestname+"\" found in file "+inputname+"."+
                " (debuginfo: manifestname="+manifestname+",JARSL_METAINF="+JARSL_METAINF+")");
								if (null != errorTexts) {
									errorTexts.add("no entry \""+JARSL_METAINF+"/"+manifestname+"\" found in file "+inputname+"."+
									" (debuginfo: manifestname="+manifestname+",JARSL_METAINF="+JARSL_METAINF+")");
								}
							}
							if (mf==null) {
                log.debug("could not create java.util.jar.Manifest from file "+inputname+"."+
                " (debuginfo: manifestname="+manifestname+",JARSL_METAINF="+JARSL_METAINF+")");
                if (null != errorTexts) {
								  errorTexts.add("could not create java.util.jar.Manifest from file "+inputname+"."+
								  " (debuginfo: manifestname="+manifestname+",JARSL_METAINF="+JARSL_METAINF+")");
                }
							}
						}
						jf.close();
						jf=null;
					}
					else {
            if (new File(Conv.pathConv(inputname+"/"+JARSL_METAINF+"/"+manifestname)).exists()) {
  						bis=new BufferedInputStream(new FileInputStream(Conv.pathConv(inputname+"/"+JARSL_METAINF+"/"+manifestname)));
  						mf=new Manifest(bis);
  						if (mf==null) {
                log.debug("Could not create java.util.jar.Manifest from file \"" + inputname+"/"+JARSL_METAINF+"/"+manifestname + "\"");
                if (null != errorTexts) {
  							  errorTexts.add("Could not create java.util.jar.Manifest from file \"" + inputname+"/"+JARSL_METAINF+"/"+manifestname + "\"");
                }
              }
  						bis.close();
  						bis=null;
            }
            else {
              log.debug(Conv.pathConv(inputname+"/"+JARSL_METAINF+"/"+manifestname)+" does not exist");
              mf=null;
            }
					}
				}
			}
			catch (Exception e) {
        log.debug("received exception",e);
        throw new JarSLManifestException("received exception: "+e.getMessage());
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
				try {
					if (bis!=null) {
						bis.close();
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
			/* now the manifest entries and attributes are extracted and added to the manifest vector */
			if (mf!=null) {
        log.debug("manifest entries and attributes are extracted and added to the manifest vector");
				attr=mf.getMainAttributes();
				attributes=attr.keySet().toArray();
				for (int i=0; i<attributes.length; ++i) {
					manifest.addElement(new ManifestEntry(attributes[i].toString(),attr.getValue(attributes[i].toString()),""));
				}
				mp=mf.getEntries();
				entries=mp.keySet().toArray();
				for (int i=0; i<entries.length; ++i) {
					attr=mf.getAttributes(entries[i].toString());
					attributes=attr.keySet().toArray();
					for (int ii=0; ii<attributes.length; ++ii) {
						manifest.addElement(new ManifestEntry(attributes[ii].toString(),attr.getValue(attributes[ii].toString()),entries[i].toString()));
					}
				}
        log.exiting("readAllAttributes: true");
				return true;
			}
			else {
        log.exiting("readAllAttributes: false");
				return false;
			}
		}
	}
  private String localPathConvJar(String input) {
    if (input==null || !input.endsWith("\\")) {
      return Conv.pathConvJar(input);
    }
    else {
      String tmp=input.substring(0,input.length()-1);
      return Conv.pathConvJar(tmp)+"\\";
    }
  }
  /**
   * returns the value of the given attribute and entry. If the corresponding entry was not
   * found in the manifest vector then null is returned.
   */
  String readAttribute(String entry, String attribute) {
    log.entering("readAttribute: "+entry+" , "+attribute);
    ManifestEntry mfe;
    String returnvalue=null;
    if (entry!=null && !entry.equals("")) {
      entry=localPathConvJar(entry);
    }
    for (int i=0; i<manifest.size() && returnvalue==null; ++i) {
      mfe=(ManifestEntry)manifest.elementAt(i);
      if (mfe.getAttributeName().compareTo(attribute)==0 && mfe.getEntryName().compareTo(entry)==0) {
        returnvalue=mfe.getAttributeValue();
      }
    }
    log.exiting("readAttribute: rc="+returnvalue);
    return returnvalue;
  }
  boolean writeAttribute(String entry, String attribute, String value) {
    boolean archiveentry=false;
    if (entry.endsWith("/")) {
      archiveentry=true;
    }
    return writeAttribute(entry,attribute,value,archiveentry);
  }
  /**
   * adds or overwrites a manifest entry defined by the given entry, attribute pair with
   * the given value
   */
  boolean writeAttribute(String entry, String attribute, String value, boolean archiveentry) {
    boolean found=false;
    ManifestEntry mfe;
    if (value==null) {
      // null values are not allowed
      return false;
    }
    if (entry!=null && !entry.equals("")) {
      entry=localPathConvJar(entry);
    }
    for (int i=0; i<manifest.size() && !found; ++i) {
      mfe=(ManifestEntry)manifest.elementAt(i);
      if (mfe.getEntryName().compareToIgnoreCase(entry)==0) {
        if (mfe.getEntryName().compareTo(entry)!=0) {
          // duplicate entry in manifest with different cases
          return false;
        }
        else if (mfe.getAttributeName().compareToIgnoreCase(attribute)==0) {
          if (mfe.getAttributeName().compareTo(attribute)!=0) {
            // duplicate attribute in manifest with different cases
            return false;
          }
          else {
            found=true;
            if (mfe.getAttributeValue().compareTo(value)!=0 || mfe.isArchiveEntry()!=archiveentry) {
             mfe.setAttributeValue(value);
             mfe.setArchiveEntry(archiveentry);
            }
          }
        }
      }
    }
    if (!found) {
      if (attribute.compareTo(ATTMANIFESTVERSION)==0) {
        manifest.insertElementAt(new ManifestEntry(attribute,value,entry,archiveentry),0);
      }
      else {
        manifest.addElement(new ManifestEntry(attribute,value,entry,archiveentry));
      }
    }
  return true;
  }
  /**
   * deletes a manifest entry
   */
  boolean deleteAttribute(String entry, String attribute) {
    boolean found=false;
    ManifestEntry mfe;
    if (entry!=null && !entry.equals("")) {
      entry=localPathConvJar(entry);
    }
    for (int i=0; i<manifest.size() && !found; ++i) {
      mfe=(ManifestEntry)manifest.elementAt(i);
      if (mfe.getAttributeName().compareTo(attribute)==0 && mfe.getEntryName().compareTo(entry)==0) {
        found=true;
        manifest.removeElementAt(i);
      }
    }
    if (found) {
      return true;
    }
    else {
      return false;
    }
  }
  /**
   * This method returns all manifest entries
   * @return ManifestEntryIF array
   */
  ManifestEntryIF[] returnAllManifestEntries() {
    return (ManifestEntryIF[])manifest.toArray(new ManifestEntryIF[0]);
  }
  /**
   * This method inserts the content of the given manifest object to the actual
   * manifest. Also it is possible to add a prefix string to the respective entry
   * name before it is written to the actual manifest.
   */
  boolean addManifest(ManifestSL mf, String prefix) {
    ManifestEntry mfe;
    boolean rc=true;
    for (int i=0; i<mf.manifest.size(); ++i) {
      mfe=(ManifestEntry)mf.manifest.elementAt(i);
      rc&=writeAttribute(prefix+mfe.getEntryName(),mfe.getAttributeName(),mfe.getAttributeValue(),true);
    }
    return rc;
  }
  /**
   * returns a list of entries which have the given attribute value manifest entry
   */
  String[] getEntryListOfGivenAttributeValuePair(String attribute, String value) {
    Vector entry=new Vector();
    String[] entrylist=getEntryListFromManifest(true);
    String rv;
    for (int i=0; i<entrylist.length; ++i) {
      rv=readAttribute(entrylist[i],attribute);
      if (rv!=null && rv.compareTo(value)==0) {
        entry.addElement(entrylist[i]);
      }
    }
    String[] list=new String[entry.size()];
    for (int i=0; i<entry.size(); ++i) {
      list[i]=(String)entry.elementAt(i);
    }
    return list;
  }
  String[] getEntryListFromManifest() {
    return getEntryListFromManifest(false);
  }
  /**
   * Every manifest entry is added to a list (only once) and the created list is returned.
   * If the argument is false then no main-entry and no archive entry which ends with "/" is
   * added to the list.
   */
  String[] getEntryListFromManifest(boolean wholelist) {
    Vector entry=new Vector();
    boolean archive;
    for (int i=0; i<manifest.size(); ++i) {
      archive=false;
      if (((ManifestEntry)manifest.elementAt(i)).getEntryName().endsWith("/")==true) {
        archive=true;
      }
      if ( wholelist || archive==false) {
        if ((wholelist || ((ManifestEntry)manifest.elementAt(i)).getEntryName().compareTo("")!=0) && !entry.contains(((ManifestEntry)manifest.elementAt(i)).getEntryName())) {
          entry.addElement(((ManifestEntry)manifest.elementAt(i)).getEntryName());
        }
      }
    }
    String[] list=new String[entry.size()];
    for (int i=0; i<entry.size(); ++i) {
      list[i]=(String)entry.elementAt(i);
    }
    return list;
  }
  /**
   * Every manifest archive entry which ends with "/" is added to a list (only once)
   * and the created list is returned.
   */
  String[] getArchiveEntryListFromManifest() {
    Vector entry=new Vector();
    boolean archive;
    for (int i=0; i<manifest.size(); ++i) {
      archive=false;
      if (((ManifestEntry)manifest.elementAt(i)).getEntryName().endsWith("/")==true) {
        archive=true;
      }
      if (archive) {
        if ((((ManifestEntry)manifest.elementAt(i)).getEntryName().compareTo("")!=0) && !entry.contains(((ManifestEntry)manifest.elementAt(i)).getEntryName())) {
          entry.addElement(((ManifestEntry)manifest.elementAt(i)).getEntryName());
        }
      }
    }
    String[] list=new String[entry.size()];
    for (int i=0; i<entry.size(); ++i) {
      list[i]=((String)entry.elementAt(i)).substring(0,((String)entry.elementAt(i)).length()-1);
    }
    return list;
  }
  static boolean isStandardAttribute(String attribute) {
    boolean rc=true;
    for (int i=0; i<exattributes.length && rc; ++i) {
      if (exattributes[i].compareToIgnoreCase(attribute)==0) {
        rc=false;
      }
    }
    return rc;
  }
  static boolean isBothAttribute(String attribute) {
    boolean rc=false;
    for (int i=0; i<bothattributes.length && !rc; ++i) {
      if (bothattributes[i].compareToIgnoreCase(attribute)==0) {
        rc=true;
      }
    }
    return rc;
  }
  public static void main(String[] args) {
    System.out.println("Reading Manifest of \""+args[0]+"\"...");
    Manifest mf = null;
    JarInputStream jis=null;
    try {
      jis=new JarInputStream(new BufferedInputStream(new FileInputStream(Conv.pathConv(args[0]))));
      mf=jis.getManifest();
      jis.close();
      jis=null;
    }
    catch (Exception e) {
      System.out.println("Caught Exception: " + e.getMessage());
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
      System.out.println("...ready");
    }
    if (null == mf) {
      System.out.println("No Manifest found.");
    }
    else {
      System.out.println("Found Manifest:");
      try {
        mf.write(System.out) ;
      }
      catch (java.io.IOException ioe) {
        System.out.println("Could not print out Manifest: "+ ioe.getMessage());
      }
    }
  }
}
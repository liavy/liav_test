/*
 * Copyright (c) 2004 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.webservices.espbase.attachment.impl;

import java.io.File;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.rmi.server.UID;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import com.sap.engine.services.webservices.espbase.attachment.Attachment;
import com.sap.localization.LocalizableTextFormatter;
import com.sap.localization.LocalizationException;
import com.sap.tc.logging.Location;
/**
 * Container for <code>Attachment</code> objects.
 * @author I024065
 *
 */
public class AttachmentContainer {
  
  private static final Location LOC = Location.getLocation(AttachmentContainer.class);
  
  private Set atts = new HashSet(); //contains all attachment objects
  private Hashtable cidAtts = new Hashtable(); //maps attachment object by its 'cid' URLs
  /**
   * Creates and returns an empty attachment object, but with set unique 'Content-Id' value.
   */
  public static Attachment createAttachmentWithUniqueCID() {
    Attachment a = createAttachment();
    try {
      String cid = new UID().toString(); 
      cid = cid.replace(':', '-'); //since '-' is not escaped by the .encoded() method below.
                                   //This is necessary since SUN's RI of JAXWS does not decode
                                   //attachment's 'Content-ID' content and uses it as is, while
                                   //it decodes the 'swaref' references.
                                   //By using sequnce into which no character needs escaping,
                                   //this issue is workarounded.
      a.setContentId(URLEncoder.encode(cid, "utf-8"));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return a;
  }
  /**
   * Creates and returns an empty attachment object.
   */
  public static Attachment createAttachment() {
    Attachment a = new AttachmentImpl();
    return a;
  }
  /**
   * Adds the attachment to the internal set. 
   * @param att attachment to be added
   * @return the 'cid' url for the added attachment, base on its 'Content-Id' value.
   * @exception NullpointerException if the parameter is null.
   * @exception IlleagalStateException if internal table already contains attachment with 'Content-Id' equal to <code>att</code> one.
   */  
  public synchronized String addAttachment(Attachment att) {
    if (LOC.beDebug()) {
      LOC.debugT("addAttachment(): adding attachment " + att);
    }
    try {
      if (att == null) {
        throw new NullPointerException(LocalizableTextFormatter.formatString(ResourceAccessor.getResourceAccessor(), ResourceAccessor.INVALID_PARAMETER_NULL));
      }
      String cid = att.getContentId();
      if (cid != null) {
        cid = "cid:" + cid;
        if (cidAtts.containsKey(cid)) {
          throw new IllegalStateException(LocalizableTextFormatter.formatString(ResourceAccessor.getResourceAccessor(), ResourceAccessor.ATTACHMENTE_IS_ALREADY_PRESENT, new Object[]{cid}));
        }
        cidAtts.put(cid, att); //add the the cid table
      }
      atts.add(att);
      return cid;
    } catch (LocalizationException lE) {
      throw new RuntimeException(lE);
    }
  }
  /**
   * Gets attachment by <code>cid</code>.
   * @param cid url. It is in the from "cid:xxxx". See RFC2392 for further details.
   * @return attachment object which 'Content-Id' is equal to <code>cid</code>, or null if none is found.
   * @exception NullpointerException if the parameter is null.
   */  
  public synchronized Attachment getAttachment(String cid) {
    try {
      if (cid == null) {
        throw new NullPointerException(LocalizableTextFormatter.formatString(ResourceAccessor.getResourceAccessor(), ResourceAccessor.INVALID_PARAMETER_NULL));
      }
      final String cid_scheme = "cid:";
      if (! cid.startsWith(cid_scheme)) {
        throw new IllegalArgumentException(LocalizableTextFormatter.formatString(ResourceAccessor.getResourceAccessor(), ResourceAccessor.INVALID_CID_URL));
      }
    } catch (LocalizationException lE) {
      throw new RuntimeException(lE);
    }
    return (Attachment) cidAtts.get(cid);
  }
  /**
   * 
   * @return Set containing all Attachments object holded this container.
   */
  public synchronized Set getAttachments() {
    return Collections.unmodifiableSet(atts);
  }
  /**
   * Removes attachment with 'Content-Id' equal to <code>cid</code>.
   * @param cid url. It is in the from "cid:xxxx". See RFC2392 for further details.
   * @return the removed attachment or null if attachment with such 'Content-Id' is not present.
   */
  public synchronized Attachment removeAttachment(String cid) {
    Attachment a = (Attachment) cidAtts.remove(cid);
    if (a != null) {
      atts.remove(a);
    }
    return a;
  }
  /**
   * Removes attachment object from this container.
   * @param a object to be removed
   * @return true if the container contains such object.
   */
  public synchronized boolean removeAttachment(Attachment a) {
    if (LOC.beDebug()) {
      LOC.debugT("removeAttachment(): attachment " + a);
    }
    boolean res = atts.remove(a);
    if (res) {
      String cid = a.getContentId();
      if (cid != null) {
        cidAtts.remove(cid);
      }
    }
    return res;
  }  
  /**
   * Adds all attachments from this container instance into <code>ac</code>.
   * @param ac
   */
  public synchronized void putAll(AttachmentContainer ac) {
    Iterator itr = this.atts.iterator();
    while (itr.hasNext()) {
      ac.addAttachment((Attachment) itr.next());
    }
  }
  
  public synchronized void clear() {
    LOC.debugT("clear()");
    this.atts.clear();
    this.cidAtts.clear();
  }
  
  public String toString() {
    return this.atts.toString();
  }
  
  public static void main(String[] args) throws Exception {    
    File f = new File("test file");
//    System.out.println(URLDecoder.decode(f.toURI().toString(), "utf-8"));
    String encString = URLEncoder.encode("t+ t  a", "utf-8");
    System.out.println(encString);
    System.out.println(URLDecoder.decode(encString, "utf-8"));
     
//    MimeMultipart mmp = new MimeMultipart();
//    MimeBodyPart mbp = new MimeBodyPart();
//    mbp.setContentID(URLEncoder.encode("some content-id%", "utf-8"));
//    mmp.addBodyPart(mbp);
//    mmp.writeTo(System.out);
    
//    Attachment a = AttachmentContainer.createAttachment();
//    a.setContentId("<somecid>");
//    a.setContentType("text/plain");
//    //a.setDataHandler(null);
//    AttachmentContainer ac = new AttachmentContainer();
//    ac.addAttachment(a);
//    System.out.println(ac.getAttachment("<somecid>"));
  }
}

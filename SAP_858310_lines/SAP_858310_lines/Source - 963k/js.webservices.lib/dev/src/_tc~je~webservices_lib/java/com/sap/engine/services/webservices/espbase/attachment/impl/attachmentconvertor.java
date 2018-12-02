package com.sap.engine.services.webservices.espbase.attachment.impl;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.activation.DataHandler;
import javax.imageio.ImageIO;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

import com.sap.engine.services.webservices.espbase.attachment.Attachment;

public class AttachmentConvertor {

  private static final String IMAGE_FORMAT_NAME = "jpeg";
  
  public static final byte[] convertToByteArray(Attachment attachment) throws Exception {
    Object contentObject = attachment.getContentObject();
    return(convertToByteArray(contentObject));
  }
  
  private static final byte[] convertToByteArray(Object content) throws Exception {
    if(content instanceof byte[]) {
      return((byte[])content);
    }
    if(content instanceof String) {
      return(((String)content).getBytes()); //$JL-I18N$
    }
    if(content instanceof Image) {
      return(convertImageToByteArray((Image)content));
    }
    if(content instanceof Source) {
      return(convertSourceToByteArray((Source)content));
    }
    if(content instanceof DataHandler) {
      return(convertDataHandlerToByteArray((DataHandler)content));
    }
    return(null);
  }
  
  private static final byte[] convertDataHandlerToByteArray(DataHandler dataHandler) throws Exception {
    byte[] dataHandlerBytes = convertToByteArray(dataHandler.getContent());
    if(dataHandlerBytes == null) {
      dataHandlerBytes = readDataHandlerInputStream(dataHandler);
    }
    return(dataHandlerBytes);
  }
  
  private static final byte[] readDataHandlerInputStream(DataHandler dataHandler) throws IOException {
    InputStream inputStream = dataHandler.getInputStream();
    if(inputStream == null) {
      inputStream = dataHandler.getDataSource().getInputStream(); 
    }
    byte[] dataHadnlerBytes = new byte[inputStream.available()];
    int readBytes = 0;
    while(readBytes < dataHadnlerBytes.length) {
      readBytes += inputStream.read(dataHadnlerBytes, readBytes, dataHadnlerBytes.length); 
    }
    return(dataHadnlerBytes);
  }

  private static final byte[] convertSourceToByteArray(Source source) throws Exception {
    ByteArrayOutputStream byteArrayOutput = null;
    try {
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = transformerFactory.newTransformer();
      StreamResult streamResult = new StreamResult(byteArrayOutput); 
      transformer.transform(source, streamResult);
      byteArrayOutput.flush();
      return(byteArrayOutput.toByteArray());
    } finally {
      if(byteArrayOutput != null) {
        byteArrayOutput.close();
      }
    }
  }
  
  private static final byte[] convertImageToByteArray(Image image) throws IOException {
    ByteArrayOutputStream imageByteArrayOutput = null;
    Graphics imageGraphics = null;
    try {
      BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_3BYTE_BGR);
      imageGraphics = bufferedImage.createGraphics();
      imageGraphics.drawImage(image, 0, 0, null);
      imageByteArrayOutput = new ByteArrayOutputStream();
      ImageIO.write(bufferedImage, IMAGE_FORMAT_NAME, imageByteArrayOutput);
      imageByteArrayOutput.flush();
      return(imageByteArrayOutput.toByteArray());
    } finally {
      if(imageByteArrayOutput != null) {
        imageByteArrayOutput.close();
      }
      if(imageGraphics != null) {
        imageGraphics.dispose();
      }
    }
  }
}

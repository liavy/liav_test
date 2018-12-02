package com.sap.sl.util.sduwrite.api;

import java.io.IOException;

import com.sap.sl.util.sduread.api.IllFormattedSduManifestException;
import com.sap.sl.util.sduread.api.SdaFile;

/**
 * An abstract SDU file writer. SDU is short for Software Delivery Unit.
 * 
 * @author d030435
 */

public interface SduWriter {
  /**
   * Creates a SCA that is based on the given manifest files and the SDAs to be contained. It is checked if the manifest files
   * fit to the SDAs.
   * @param targetPathOfScaFile the name of the SCA to be created
   * @param pathNameOfManifestFile the path to the SDU manifest file
   * @param pathNameOfSapManifestFile the path to the SDU sap_manifest file
   * @param containedSdas the SDAs to be packed
   * @throws IllFormattedSduFileException
   * @throws IOException
   */
  public abstract void createScaFile(String targetPathOfScaFile, String pathNameOfManifestFile, String pathNameOfSapManifestFile, SdaFile[] containedSdas) throws SduWriterException, IllFormattedSduManifestException, IOException;
}
package com.sap.engine.services.textcontainer.module.test;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.sap.engine.services.textcontainer.deployment.test.TextComponentProcessorDeploymentDummy;
import com.sap.engine.services.textcontainer.module.TextComponent;

class dummy {
    public static void main(String[] args) {
		try {
			String sArchive = args.length>0 ? args[0] : "C:/javalabor/text/appl1.ear";
			File sTextModuleTemp = extractZipEntryToTempFile( 
				sArchive, "textmodule.txtar", "textmodul", "txtar.zip"
			);
			TextComponent oTextComponent = new TextComponent(sTextModuleTemp);
			oTextComponent.load( new TextComponentProcessorDeploymentDummy("com.sap/testtext") );
	    } catch (Exception e) {
	    	System.err.println(e.getMessage());
	    	e.printStackTrace();
	    }    	
    }

    /** extracts a file from Zipfile and stores it in temporary file */
    public static File extractZipEntryToTempFile( 
    		String sZipFile, String sFileName, String sTempPrefix, String sTempSuffix 
	) throws Exception {
		File fZipFile = new File( sZipFile );
		ZipFile oZipFile = new ZipFile( fZipFile );
	    ZipEntry oZipEntry = oZipFile.getEntry(sFileName);
	    InputStream oInputStream = oZipFile.getInputStream(oZipEntry);
		int nFileLen = oInputStream.available();
	    File fTemp = File.createTempFile( sTempPrefix, sTempSuffix);
	    String sTemp = fTemp.getPath();
		RandomAccessFile oTemp = new RandomAccessFile(sTemp, "rw");
		byte[] abFileContent = new byte[nFileLen];
		int nFileRead = 0;
		while( nFileRead<nFileLen ) {
			int nFileReadStep = oInputStream.read(abFileContent,nFileRead,nFileLen-nFileRead);
			nFileRead += nFileReadStep;
		}
		oInputStream.close();
		oTemp.write(abFileContent);
		oTemp.close();
		fTemp.deleteOnExit();
		return fTemp;
    }
}

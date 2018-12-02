package com.sap.sl.util.jarsl.impl;

import java.io.File;
import com.sap.sl.util.jarsl.api.JarSLFileInfoIF;

/**
 * @author d030435
 * This class encapsulates the java.io.File class.
 */

final class JarSLFileInfo implements JarSLFileInfoIF {
	private File file=null;
	JarSLFileInfo(String filename) {
		file=new File(filename);
	}
	public String getName() {
		return file.getName();
	}
	public String getPath() {
		return file.getPath();
	}
	public String getParent() {
		return file.getParent();
	}
	public boolean isDirectory() {
		return file.isDirectory();
	}
	public boolean exists() {
		return file.exists();
	}
	public boolean isFile() {
		return file.isFile();
	}
	public boolean canRead() {
		return file.canRead();
	}
}

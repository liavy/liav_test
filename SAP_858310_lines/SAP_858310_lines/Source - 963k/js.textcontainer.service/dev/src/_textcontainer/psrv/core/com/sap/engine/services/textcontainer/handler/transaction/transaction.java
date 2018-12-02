package com.sap.engine.services.textcontainer.handler.transaction;

import java.io.File;
import java.io.IOException;

import com.sap.engine.frame.core.configuration.Configuration;
import com.sap.engine.services.content.handler.api.ContentTransaction;
import com.sap.engine.services.textcontainer.handler.TextContainerHandler;
import com.sap.engine.services.textcontainer.handler.util.ContentFileUtils;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

public abstract class Transaction implements ContentTransaction
{

	protected static final String TEST_STR = "test";
	protected static final String VERSION_STR = "version";
	protected String workDirPath;
	protected Configuration mySubCfg = null;
	protected final long timeStamp = System.currentTimeMillis();
	protected final long threadId = Thread.currentThread().getId();

	protected File getContentFile(File[] files)
	{
		String workDirPath = getWorkDirPath();
		
		for(File _f : files)
		{
			// do not use ZipFile because of the natvie OO
			// should we provide FileUtil centrally for all?
			try
			{
				ContentFileUtils.extractJar(_f.getCanonicalPath(), workDirPath);
			}
			catch (IOException e)
			{
    	    	CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "getContentFile", e);
    	    	throw new RuntimeException("IOException", e);
			}
		}
		
		File cntntFile = null;
		File workDir = new File(workDirPath);		
		String[] fls = workDir.list();
		for(String _fname : fls)
		{
			if (_fname.endsWith("textmodule.txtar"))
			{
				cntntFile = new File(workDirPath + File.separator + _fname);
				
				break;
			}
		}
		
		if (cntntFile == null)
			throw new RuntimeException("could not find textmodule.txtar");
		
		return cntntFile;
	}

	protected void removeWorkDir()
	{
		String workDirPath = getWorkDirPath();

		File workDir = new File(workDirPath);

		deleteDirRecursively(workDir);
	}

	/**
	 * Deletes the content of a directory recursively
	 *
	 * @param f is a <code>File</code> object, method works only if f
	 *          represents a directory
	 */
	private void deleteDirRecursively(File f)
	{
		if ((f == null) || (!f.exists()) || (!f.isDirectory()))
	 		return;

		// Get files and subdirectories:
	 	File[] files = f.listFiles();

	 	if (files == null)
	 		return;

	 	int l = files.length;

	 	// Loop over files and subdirectories:
	 	for (int i = 0; i < l; i++)
	 	{
	 		if (files[i].isDirectory())
	 			deleteDirRecursively(files[i]);

	 		// in case that file[i] is a directory it is empty now,
	 		// so we can delete it
	 		// in case it is a file we can delete it anyway
			try
			{
		 		files[i].delete();
			}
			catch (Exception e)
			{
    	    	CATEGORY.logThrowableT(Severity.WARNING, LOCATION, "deleteDirRecursively", e);
			}
	 	}

	 	// f is now an empty directory
		try
		{
		 	f.delete();
		}
		catch (Exception e)
		{
	    	CATEGORY.logThrowableT(Severity.WARNING, LOCATION, "deleteDirRecursively", e);
		}
	 }

	/**
	 * @return  Returns the workDirPath.
	 * @uml.property  name="workDirPath"
	 */
	protected String getWorkDirPath()
	{
		return TextContainerHandler.getInstance().getWorkDirPath() + File.separator + String.valueOf(timeStamp) + String.valueOf(threadId);
	}

	// Logging:
	private static final Location LOCATION = Location.getLocation("com.sap.engine.services.textcontainer.handler.transaction.Transaction");
	private static final Category CATEGORY = Category.SYS_SERVER;
}

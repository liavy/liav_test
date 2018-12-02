package com.sap.engine.services.textcontainer.handler.util;

import java.util.Iterator;
import java.util.Vector;

import com.sap.sl.util.jarsl.api.JarSLFactory;
import com.sap.sl.util.jarsl.api.JarSLIF;

public class ContentFileUtils
{
	
	/**
	 * Extracts Jar(Zip) file to the specified path 
	 * @param sdaFilePath jar or zip file to be extracted
	 * @param parentPath place to extract the archive
	 * @return true on success
	 * @throws CFSStorageException
	 */
	public static void extractJar(String fileToExtract, String targetPath)
	{
		try
		{
			JarSLFactory slFactory = JarSLFactory.getInstance();
			JarSLIF jarSl = slFactory.createJarSL(fileToExtract,targetPath);
			Vector exceptions = new Vector();
			boolean success= jarSl.extract(exceptions);
			StringBuffer buffer = null;
			if (!exceptions.isEmpty())
			{
				buffer = new StringBuffer();
				for (Iterator iter = exceptions.iterator(); iter.hasNext();)
				{
					String msg = (String) iter.next();
					buffer.append(msg).append(System.getProperty("line.separator"));
				}
			}
			if (!success)
			{
				String msg = "'" + fileToExtract + "' could not unpacked successfully.";
				if (buffer==null)
				{
					throw new RuntimeException(msg);
				}
				else
				{
					throw new RuntimeException(msg
							+ "Collected messages:"
							+ System.getProperty("line.separator")
							+ buffer.toString());
				}
			}
		}
		catch(Error err)
		{
			throw err;
		}
	}

	/**
	 * retrieves array of files in given folder and all subfolders
	 * @param folderPath path from where to get all files
	 * @return
	 */
/*
	public static File[] getDirFiles(File folderPath)
	{
		Collection list = new ArrayList();
		getDirFiles(folderPath,list);
		File[] ret = new File[list.size()];
		return (File[])list.toArray(ret);
	}
	
	private static Collection getDirFiles(File dir, Collection files)
	{
		File[] dirFiles = dir.listFiles();
		if ((dir.isFile()) || (dirFiles == null) || (dirFiles.length == 0))
		{
			files.add(dir);
		}
		else
		{
			for (int i = 0; i < dirFiles.length; i++)
			{
				getDirFiles(dirFiles[i], files);
			}
		}
		return files;
	}
*/

	/**
	 * Splits the file path by file separators.the result is array with 
	 * sequenced list of distinct folders pointing from root to the given file
	 * @param filePath
	 * @return
	 */
/*
	public static String [] getFilePathsAsArray(String filePath)
	{	  
		return getFilePathsAsArray(filePath, true);
	}	
  
	public static String [] getFilePathsAsArray(String filePath, boolean fileNameIncluded)
	{
		if (filePath == null)
		{
			throw new NullPointerException("Argument 'relFilePath' could not be null!");
		}

		if (filePath.trim().equals(""))
		{
			return new String [0];
		}
	  
		String preparedRelFilPath = prepareArchiveFilePath(filePath);
		StringTokenizer strTokenizer = new StringTokenizer(preparedRelFilPath, "/");
		Collection filePaths = new ArrayList();
		while (strTokenizer.hasMoreTokens())
		{
			String path = strTokenizer.nextToken();
			if ((fileNameIncluded) || (strTokenizer.hasMoreTokens()))
			{
				filePaths.add(path);	
			}
		}
  
		final String [] filePathsArr = new String[filePaths.size()];
		return (String []) filePaths.toArray(filePathsArr);
	}
*/

	/**
	 * Justy replaces all back slashes with forward slashes
	 * @param filePath
	 * @return
	 */
/*
	public static String prepareArchiveFilePath(String filePath)
	{
		if (filePath == null)
		{
			return null;
		}
		else
		{
			String result = filePath.replace('\\','/');
			if (result.startsWith("./"))
			{
				result = result.substring(2);
			}
			return result;
		}
	}
	
	public static String getCanonicalPath(String filePath)
	{
	  return getCanonicalPath(new File(filePath));
	}
	
	public static String getCanonicalPath(File file)
	{
		try 
		{
			return file.getCanonicalPath();
		}
		catch (IOException e)
		{
			//$JL-EXC$
			String path = file.getAbsolutePath();
			return path;
		}
	}
*/
}

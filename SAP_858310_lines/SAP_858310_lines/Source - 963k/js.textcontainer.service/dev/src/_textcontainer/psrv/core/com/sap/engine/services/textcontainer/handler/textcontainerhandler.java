package com.sap.engine.services.textcontainer.handler;

import com.sap.engine.services.content.handler.api.ContentHandler;
import com.sap.engine.services.content.handler.api.ContentHandlerInfo;

public class TextContainerHandler implements ContentHandler
{

	private static TextContainerHandler instance = null;
	private String workDirPath; 	
	private ContentHandlerInfo info = new TextContainerHandlerInfo(); 

	public static TextContainerHandler getInstance()
	{
		if (instance == null)
			throw new RuntimeException();

		return instance;
	}

	public static TextContainerHandler getInstance(String workingDirPath)
	{
		if (instance == null)
			instance = new TextContainerHandler(workingDirPath);

		return instance;
	}

	protected TextContainerHandler(String workingDirPath)
	{
		workDirPath = workingDirPath;
	}

	public ContentHandlerInfo getInfo()
	{
		return info;
	}

	public String getWorkDirPath()
	{
		return workDirPath;
	}

}

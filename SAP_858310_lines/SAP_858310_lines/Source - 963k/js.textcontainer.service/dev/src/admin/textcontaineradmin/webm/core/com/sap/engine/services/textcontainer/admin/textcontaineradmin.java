package com.sap.engine.services.textcontainer.admin;

import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;


/**
 * @author d029702
 */
public class TextContainerAdmin implements TextContainerAdminMBean
{

	public void retrieve(String destination)
	{
		try
		{
			AdministrationTool.retrieve(destination, true, true, false, false);
		}
		catch (Exception e)
		{
    		CATEGORY.logThrowableT(Severity.FATAL, LOCATION, "Exception", e);
		}
	}

	// Logging:
	private static final Location LOCATION = Location.getLocation("com.sap.engine.services.textcontainer.admin.TextContainerAdmin");
	private static final Category CATEGORY = Category.APPLICATIONS;

}

package com.sap.engine.services.ts;

import java.util.Properties;
import com.sap.engine.frame.ServiceException;
import com.sap.engine.frame.container.runtime.RuntimeConfiguration;


public class ServicePropertyChanger extends RuntimeConfiguration {

	@Override
	public void updateProperties(Properties properties) throws ServiceException {
		
        TransactionServiceFrame.initializeServiceProperties(properties, true);

	}

}

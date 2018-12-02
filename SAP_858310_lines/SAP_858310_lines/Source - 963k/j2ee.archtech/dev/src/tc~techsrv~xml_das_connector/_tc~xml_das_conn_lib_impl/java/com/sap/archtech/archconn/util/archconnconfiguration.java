package com.sap.archtech.archconn.util;

import com.sap.archtech.archconn.servicereg.IConfigurationService;
import com.sap.archtech.archconn.servicereg.ServiceRegistry;

/**
 * @author d025792
 *
 * Holds all the parameter for the archiving connector. Also acts as an
 * Observer. Method configurationChanged() is called if the Configuration
 * has changed. Used by ArchConfigProviderSingle.
 */
class ArchconnConfiguration
{
   private static final String HTTPCLIENT_LIB_ID = "DSDTR";

   private final IConfigurationService configService;

   ArchconnConfiguration()
   {
     configService = ServiceRegistry.getInstance().getConfigurationService();
     configService.loadArchconnConfig(true);
   }

   String getArchClientLib()
   {
      return HTTPCLIENT_LIB_ID;
   }

   String getArchDestination()
   {
      return configService.getArchDestination();
   }

   int getConnTimeout()
   {
      return configService.getConnTimeout();
   }

   int getExpTimeout()
   {
      return configService.getExpTimeout();
   }

   int getReadTimeout()
   {
      return configService.getReadTimeout();
   }

   int getUpdateFreq()
   {
      return configService.getUpdateFreq();
   }
}

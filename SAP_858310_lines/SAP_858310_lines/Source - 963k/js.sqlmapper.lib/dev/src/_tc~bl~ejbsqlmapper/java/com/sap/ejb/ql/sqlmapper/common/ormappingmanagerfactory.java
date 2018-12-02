package com.sap.ejb.ql.sqlmapper.common;

import com.sap.engine.interfaces.ejb.orMapping.CommonORMapping;
import com.sap.engine.interfaces.ejb.orMappingDescriptors.SchemaModel;

import com.sap.ejb.ql.sqlmapper.SQLMappingException;
import com.sap.ejb.ql.sqlmapper.general.DevTrace;
import com.sap.ejb.ql.sqlmapper.common.ORMappingManager;
import com.sap.ejb.ql.sqlmapper.common.DescriptorBasedORMappingManager;

import com.sap.tc.logging.Location;

/**
 * Creates appropriate <code>ORMappingManager</code>s for given
 * <code>CommonORMapping</code>s depending on the actual implementation
 * of the <code>CommonORMapping</code> interface.
 * </p><p>
 * Copyright (c) 2005, SAP-AG
 * @author Rainer Schweigkoffer, Dirk Debertin
 * @version 1.0
 */
public class ORMappingManagerFactory
{
   private static final Location loc = Location.getLocation(ORMappingManagerFactory.class);
   private static final String createORMappingManager = "createORMappingManager";
   private static final String[] createORMappingManagerParms = { "orMapping" };

   /**
    * Creates an <code>ORMappingManager</code> instance for a given <code>CommonORMapping</code>.
    * The <code>ORMappingManagerFactory</code> determines the actual implementation of
    * the <code>CommonORMapping</code> interface and tries to instantiate an appropriate
    * implementation of the <code>ORMappingManager</code> interface.
    * <p></p>
    * @param orMapping
    *      OR mapping of an ejb abstract schema.
    * @return
    *              an <code>ORMappingManager</code> instance.
    * @throws SQLMappingException
    *              if no <code>ORMappingManager</code> instance can be created.
    */
   static ORMappingManager createORMappingManager(CommonORMapping orMapping)
      throws SQLMappingException
   {

     if ( DevTrace.isOnDebugLevel(loc) )
     {
       Object inputValues[] = { orMapping };
       DevTrace.entering(loc, createORMappingManager, createORMappingManagerParms, inputValues);
     } 

     ORMappingManager orMappingManager;

     if (orMapping instanceof SchemaModel )
     {
       orMappingManager = new DescriptorBasedORMappingManager((SchemaModel) orMapping);
       DevTrace.exiting(loc, createORMappingManager, orMappingManager);
       return orMappingManager;
     }

     DevTrace.exitingWithException(loc, createORMappingManager);
     throw new SQLMappingException("Unsupported OR mapping API encountered.",
                                   "An instance of class "
                                   + orMapping.getClass().getName()
                                   + " has been provided as OR mapping. However,"
                                   + " that class is not known to the SQL mapper."
                                   + " This is an internal programming error in the"
                                   + " ejb service. Please kindly open up a problem"
                                   + " ticket for SAP on component BC-JAS-EJB.",
                                   "CSM155");
   }
}

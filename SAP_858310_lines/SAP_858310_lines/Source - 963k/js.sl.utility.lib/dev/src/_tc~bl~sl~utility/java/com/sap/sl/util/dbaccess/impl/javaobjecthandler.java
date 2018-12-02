package com.sap.sl.util.dbaccess.impl;

class JavaObjectHandler extends ObjectHandler
{
   JavaObjectHandler(String name)
   {
      super(name);
      this.type = java.sql.Types.JAVA_OBJECT;
   }
}

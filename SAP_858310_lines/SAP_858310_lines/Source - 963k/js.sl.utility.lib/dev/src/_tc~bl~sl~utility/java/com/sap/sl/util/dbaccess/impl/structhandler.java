package com.sap.sl.util.dbaccess.impl;


class StructHandler extends ObjectHandler
{
   StructHandler(String name)
   {
      super(name);
      this.type =  java.sql.Types.STRUCT;
   }
}


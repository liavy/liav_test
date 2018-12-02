package org.spec.jappserver.mfg;

import java.io.Serializable;

public class BomPK implements Serializable
{
   private static final long serialVersionUID = 1L;

   protected String assemblyId;
   protected String componentId;
   protected int lineNo;

   /**
    * Default constructor.
    */
   public BomPK()
   {
   }

   /**
    * Default constructor.
    */
   public BomPK(String assemblyId, String componentId, int lineNo)
   {
      this.assemblyId = assemblyId;
      this.componentId = componentId;
      this.lineNo = lineNo;
   }

   public boolean equals(Object other)
   {
      if (other == this)
      {
          return true;
      }

      if (!(other instanceof BomPK))
      {
          return false;
      }

      BomPK otherKey = ((BomPK) other);
      return assemblyId.equals(otherKey.assemblyId)
            && componentId.equals(otherKey.componentId)
            && lineNo == otherKey.lineNo;
   }

   public int hashCode()
   {
      return 31*(31*assemblyId.hashCode() + componentId.hashCode()) + lineNo;
   }

   public String getAssemblyId ()
   {
      return assemblyId;
   }

   public void setAssemblyId (String assemblyId)
   {
      this.assemblyId = assemblyId;
   }

   public String getComponentId ()
   {
      return componentId;
   }

   public void setComponentId (String componentId)
   {
      this.componentId = componentId;
   }

   public int getLineNo ()
   {
      return lineNo;
   }

   public void setLineNo (int lineNo)
   {
      this.lineNo = lineNo;
   }
}

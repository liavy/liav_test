package com.sap.archtech.archconn.values;

import java.io.Serializable;
import java.util.Collection;

/**
 * Describes one parameter in a
 * @link com.sap.archtech.archconn.values.SelectClause
 * 
 * @author d025792
 */
public class SelectParam implements Comparable<SelectParam>, Serializable
{

   /**
    * versions of this class must declare this SUID to allow
    * serialization between different versions
    */
   private static final long serialVersionUID = -315225510704145946L;

   private final int pos;
   private final String type;
   private final Serializable value;

   public SelectParam()
   {
  	 pos = 0;
  	 type = null;
  	 value = null;
   }

   /**
    * @deprecated Use {@link #SelectParam(int, String, Serializable)}
    */
   public SelectParam(int pos, String type, Object value)
   {
     this(pos, type, (Serializable)value);
   }

   public SelectParam(int pos, String type, Serializable value)
   {
     if(pos < 0)
     {
       throw new IllegalArgumentException("The position of a parameter inside a SELECT clause must not be less than 0!");
     }
     this.pos = pos;
     this.type = type;
     this.value = value;
   }

   /**
    * Instances of this class are sorted according to the position in the {@link SelectClause} they belong to.
    */
   public int compareTo(SelectParam o)
   {
      return pos - o.getPos();
   }

   /**
    * Two instances of this class are considered as equal if the results of {@link #getPos()} are equal
    * for both instances. This guarantees consistency between {@link #compareTo(Object)} and {@link #equals(Object)}.<br>
    * <b>Note: </b>If you want to put several instances of this class into a {@link Collection}, make sure they belong to the 
    * same instance of {@link SelectClause}. 
    */
   public boolean equals(Object other)
   {
     if(other instanceof SelectParam)
     {
       SelectParam otherParam = (SelectParam)other;
       return pos == otherParam.pos;
     }
     return false;
   }
   
   /**
    * Two instances of this class are considered as equal if the results of {@link #getPos()} are equal
    * for both instances. This guarantees consistency between {@link #compareTo(Object)} and {@link #equals(Object)}.<br>
    * Therefore the hash code of a certain instance is equal to the result of {@link #getPos()}.
    * <b>Note: </b>If you want to put several instances of this class into a {@link Collection}, make sure they belong to the 
    * same instance of {@link SelectClause}.
    */
   public int hashCode()
   {
     return pos;
   }
   
   /**
    * @return position of select parameter
    */
   public int getPos()
   {
      return pos;
   }

   /**
    * @return value of select parameter
    */
   public Object getValue()
   {
      return value;
   }

   /**
    * @return type of select parameter
    */
   public String getType()
   {
      return type;
   }

}

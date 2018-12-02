package org.spec.jappserver.orders;

import java.io.Serializable;

public class OrderLinePK implements Serializable
{
   private static final long serialVersionUID = 1L;

   protected int orderLineId;
   protected int orderId;

   /**
    * Default constructor.
    */
   public OrderLinePK()
   {
   }

   /**
    * Default constructor.
    */
   public OrderLinePK(int ordLineId, int ordId)
   {
      orderLineId = ordLineId;
      orderId = ordId;
   }

   /**
    * Persistent field.
    *
    * @return orderline identifier
    */
   public int getOrderLineId ()
   {
      return orderLineId;
   }

   /**
    * Persistent field.
    *
    * @return order identifier
    */
   public int getOrderId ()
   {
      return orderId;
   }

   /**
    * Persistent field.
    *
    * @param i orderline identifier
    */
   public void setOrderLineId (int i)
   {
      orderLineId = i;
   }

   /**
    * Persistent field.
    *
    * @param i order identifier
    */
   public void setOrderId (int i)
   {
      orderId = i;
   }

   public boolean equals(Object other)
   {
      if (other == this)
      {
          return true;
      }

      if (!(other instanceof OrderLinePK))
      {
          return false;
      }

      OrderLinePK otherKey = ((OrderLinePK) other);
      return orderLineId == otherKey.orderLineId && orderId == otherKey.orderId;
   }

   public int hashCode()
   {
      return 31*orderId + orderLineId;
   }
}

/*
 * Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *
 * This source code is provided as is, without any express or implied warranty.
 *
 *  History:
 *  Date        ID, Company             Description
 *  ----------  ----------------        ----------------------------------------------
 *  2003/01/01  John Stecher, IBM       Created for SPECjAppServer2004
 *  2003/03/26  John Stecher, IBM       Added Big Decimal Support
 *  2003/04/30  John Stecher, IBM       moved to helper package
 *  2003/06/28  John Stecher, IBM       Removed unnecessary imports
 */

package org.spec.jappserver.corp.helper;

import java.io.Serializable;
import java.math.BigDecimal;

public class CustomerInventoryDataBean implements Serializable, Comparable<CustomerInventoryDataBean> {
    private String          vehicle;
    private BigDecimal      totalCost;
    private int         quantity;
    private Integer         customerID;
    private Integer     id;
    /**
     * Returns the customerID.
     * @return int
     */
    public Integer getCustomerID() {
        return customerID;
    }

    /**
     * Returns the quantity.
     * @return int
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Returns the totalCost.
     * @return double
     */
    public BigDecimal getTotalCost() {
        return totalCost;
    }

    /**
     * Returns the vehicle.
     * @return ItemsDataBean
     */
    public String getVehicle() {
        return vehicle;
    }

    /**
     * Sets the customerID.
     * @param customerID The customerID to set
     */
    public void setCustomerID(Integer customerID) {
        this.customerID = customerID;
    }

    /**
     * Sets the quantity.
     * @param quantity The quantity to set
     */
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    /**
     * Sets the totalCost.
     * @param totalCost The totalCost to set
     */
    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }

    /**
     * Sets the vehicle.
     * @param vehicle The vehicle to set
     */
    public void setVehicle(String vehicle) {
        this.vehicle = vehicle;
    }

    /**
     * Returns the id.
     * @return Integer
     */
    public Integer getId() {
        return id;
    }

    /**
     * Sets the id.
     * @param id The id to set
     */
    public void setId(Integer id) {
        this.id = id;
    }

    public boolean equals (Object o1) {
        CustomerInventoryDataBean cdb = (CustomerInventoryDataBean) o1;
        if( cdb.customerID.intValue() == customerID.intValue() ) return true;
        else return false;
    }

   public int compareTo (CustomerInventoryDataBean o)
   {
      if( id < o.id ) {
          return -1;
      } else if( id == o.id ) {
          return 0;
      } else {
          return 1;
      }
   }

   public int hashCode()
   {
      return customerID.intValue();
   }
}

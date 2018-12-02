/*
* Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
*               All rights reserved.
*
* This source code is provided as is, without any express or implied warranty.
*
*  History:
*  Date        ID, Company               Description
*  ----------  ----------------          ----------------------------------------------
*  2003/01/01  John Stecher, IBM         Created for SPECjAppServer2004
*  2003/03/26  John Stecher, IBM         Implemented Big Decimal Changes
*  2003/04/20  John Stecher, IBM         Moved class to helper package
*  2003/12/04  Samuel Kounev, Darmstadt  Added category field.
*/

package org.spec.jappserver.orders.helper;

import java.math.BigDecimal;

public class ItemsDataBean implements java.io.Serializable {
    String      name;
    BigDecimal  price;
    String      description;
    BigDecimal  discount;
    int	        category;
    String      id;

    /**
     * Returns the description.
     * @return String
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the discount.
     * @return float
     */
    public BigDecimal getDiscount() {
        return discount;
    }

    /**
     * Returns the name.
     * @return String
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the price.
     * @return double
     */
    public BigDecimal getPrice() {
        return price;
    }
    
    /**
     * Returns the category.
     * @return int
     */
    public int getCategory() {
        return category;
    }

    /**
     * Sets the description.
     * @param description The description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Sets the discount.
     * @param discount The discount to set
     */
    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    /**
     * Sets the name.
     * @param name The name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the price.
     * @param price The price to set
     */
    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    /**
     * Returns the id.
     * @return String
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the id.
     * @param id The id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Sets the category.
     * @param int category 
     */
    public void setCategory(int category) {
        this.category = category;
    }

}

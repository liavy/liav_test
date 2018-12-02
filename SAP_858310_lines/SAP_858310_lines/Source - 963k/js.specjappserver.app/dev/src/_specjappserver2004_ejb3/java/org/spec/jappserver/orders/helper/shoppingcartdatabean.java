/*
* Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
*               All rights reserved.
*
* This source code is provided as is, without any express or implied warranty.
*
*  History:
*  Date         ID, Company               Description
*  ----------   ----------------          ----------------------------------------------
*  2003/01/01   John Stecher, IBM         Created for SPECjAppServer2004
*  2003/03/26   John Stecher, IBM         Implemented Big Decimal Changes
*  2003/06/28   John Stecher, IBM         Removed unnecessary imports
*  2003/08/15   Samuel Kounev, Darmstadt  Optimized BigDecimal creation from int - eliminated 
*                                         conversion of integer to String and parsing.
*/

package org.spec.jappserver.orders.helper;


import java.math.BigDecimal;

public class ShoppingCartDataBean implements java.io.Serializable {
    private String          vehicle;
    private int         quantity;
    private Integer     cartID;
    private String          itemID;
    private String          description;
    private BigDecimal      discount;
    private BigDecimal      price;

    /**
     * Returns the customerID.
     * @return int
     */
    public Integer getCartID() {
        return cartID;
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
        return getDiscountedPrice().multiply(new BigDecimal(quantity));
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
    public void setCartID(Integer cartID) {
        this.cartID = cartID;
    }

    /**
     * Sets the quantity.
     * @param quantity The quantity to set
     */
    public void setQuantity(int quantity) {
        this.quantity = quantity;
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
    public String getId() {
        return itemID;
    }

    /**
     * Sets the id.
     * @param id The id to set
     */
    public void setId(String id) {
        this.itemID = id;
    }

    /**
     * Returns the description.
     * @return String
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the discount.
     * @return double
     */
    public BigDecimal getDiscount() {
        return discount;
    }

    /**
     * Returns the itemID.
     * @return String
     */
    public String getItemID() {
        return itemID;
    }

    /**
     * Returns the MSRP price.
     * @return double
     */
    public BigDecimal getMSRP() {
        return price;
    }
    
    /**
     * Returns the discounted price.
     * @return double
     */
    public BigDecimal getDiscountedPrice() {
        return price.multiply(new BigDecimal("1.00").subtract(discount)).setScale(2, BigDecimal.ROUND_DOWN);
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
     * Sets the itemID.
     * @param itemID The itemID to set
     */
    public void setItemID(String itemID) {
        this.itemID = itemID;
    }

    /**
     * Sets the price.
     * @param price The price to set
     */
    public void setMSRP(BigDecimal price) {
        this.price = price;
    }

}

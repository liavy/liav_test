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
 *  2003/03/26  John Stecher, IBM       Added Big Decimal support
 *  2003/04/30  John Stecher, IBM       moved to helper package
 */

package org.spec.jappserver.corp.helper;

import java.math.BigDecimal;

public class CustomerDataBean implements java.io.Serializable {

    private int accountID;
    private java.sql.Date accountCreated;
    private BigDecimal balance;
    private String creditRating;
    private BigDecimal creditLimit;
    private String firstName;
    private String lastName;

    /**
     * Returns the accountCreated.
     * @return java.sql.Date
     */
    public java.sql.Date getAccountCreated() {
        return accountCreated;
    }

    /**
     * Returns the accountID.
     * @return int
     */
    public int getAccountID() {
        return accountID;
    }

    /**
     * Returns the balance.
     * @return double
     */
    public BigDecimal getBalance() {
        return balance;
    }

    /**
     * Returns the creditLimit.
     * @return double
     */
    public BigDecimal getCreditLimit() {
        return creditLimit;
    }

    /**
     * Returns the creditRating.
     * @return String
     */
    public String getCreditRating() {
        return creditRating;
    }

    /**
     * Sets the accountCreated.
     * @param accountCreated The accountCreated to set
     */
    public void setAccountCreated(java.sql.Date accountCreated) {
        this.accountCreated = accountCreated;
    }

    /**
     * Sets the accountID.
     * @param accountID The accountID to set
     */
    public void setAccountID(int accountID) {
        this.accountID = accountID;
    }

    /**
     * Sets the balance.
     * @param balance The balance to set
     */
    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    /**
     * Sets the creditLimit.
     * @param creditLimit The creditLimit to set
     */
    public void setCreditLimit(BigDecimal creditLimit) {
        this.creditLimit = creditLimit;
    }

    /**
     * Sets the creditRating.
     * @param creditRating The creditRating to set
     */
    public void setCreditRating(String creditRating) {
        this.creditRating = creditRating;
    }   

    public String toString() {
        return "\n\tAccount Data for account: " + getAccountID()
        + "\n\t\t   accountCreated:" + getAccountCreated()
        + "\n\t\t  balance:" + getBalance()
        + "\n\t\t    creditRating:" + getCreditRating()
        + "\n\t\t creditLimit:" + getCreditLimit()          
        ;
    }

    public String toHTML() {
        return "<BR>Account Data for account: <B>" + getAccountID() + "</B>"
        + "<LI>accountCreated:" + getAccountCreated() + "</LI>"
        + "<LI>balance:" + getBalance() + "</LI>"
        + "<LI>creditRating:" + getCreditRating() + "</LI>"
        + "<LI>creditLimit:" + getCreditLimit() + "</LI>"           
        ;
    }   

    /**
     * Returns the firstName.
     * @return String
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Returns the lastName.
     * @return String
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets the firstName.
     * @param firstName The firstName to set
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Sets the lastName.
     * @param lastName The lastName to set
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

}

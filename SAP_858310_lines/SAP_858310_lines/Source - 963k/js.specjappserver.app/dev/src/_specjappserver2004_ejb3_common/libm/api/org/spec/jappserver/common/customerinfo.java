/*
 * Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *
 * This source code is provided as is, without any express or implied warranty.
 *
 *  History:
 *  Date        ID, Company             Description
 *  ----------  ----------------        ----------------------------------------------
 *  2002/03/22  ramesh, SUN Microsystem Created
 *  2002/04/12  Matt Hogstrom, IBM      Conversion from ECperf 1.1 to SPECjAppServer2001
 *  2002/07/10  Russel Raymundo, BEA    Conversion from SPECjAppServer2001 to
 *                                      SPECjAppServer2002 (EJB2.0).
 *  2003/01/01  John Stecher, IBM       Modifed for SPECjAppServer2004
 *  2003/06/17  John Stecher, IBM       Added Big Decimal to this container class
 */

package org.spec.jappserver.common;


import java.io.Serializable;
import java.math.BigDecimal;


/**
 * This class is the CustomerInfo object passed to the addCustomer
 * method of the OrderCustomer beans
 *
 * @author Shanti Subramanyam
 */
public class CustomerInfo implements Serializable {

    /**
     * Constructor CustomerInfo
     *
     *
     * @param first
     * @param last
     * @param address
     * @param contact
     * @param credit
     * @param creditLimit
     * @param balance
     * @param YtdPayment
     *
     */
    public CustomerInfo(String first, String last, Address address,
                        String contact, String credit, BigDecimal creditLimit,
                        BigDecimal balance, BigDecimal YtdPayment) {

        this.firstName   = first;
        this.lastName    = last;
        this.address     = address;
        this.contact     = contact;
        this.since       = Util.getCurrentDateRoundToDay();
        this.credit      = credit;
        this.creditLimit = creditLimit;
        this.balance     = balance;
        this.YtdPayment  = YtdPayment;
    }

    public Integer       customerId;
    public String        firstName;
    public String        lastName;
    public Address       address;
    public String        contact;
    public String        credit;
    public BigDecimal    creditLimit;
    public java.sql.Date since;
    public BigDecimal    balance;
    public BigDecimal    YtdPayment;
}


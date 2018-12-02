﻿/*
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
 */

package org.spec.jappserver.common;


import java.io.Serializable;


/**
 * This class describes the Address fields used in the various tables
 *
 * @author Shanti Subramanyam
 */
public class Address implements Serializable {

    public String street1;
    public String street2;
    public String city;
    public String state;
    public String country;
    public String zip;
    public String phone;

    /**
     * Constructor Address
     *
     *
     */
    public Address() {
    }

    /**
     * Constructor Address
     *
     *
     * @param street1
     * @param street2
     * @param city
     * @param state
     * @param country
     * @param zip
     * @param phone
     *
     */
    public Address(String street1, String street2, String city, String state,
                   String country, String zip, String phone) {

        this.street1 = street1;
        this.street2 = street2;
        this.city    = city;
        this.state   = state;
        this.country = country;
        this.zip     = zip;
        this.phone   = phone;
    }

    /**
     * Method validate
     *
     *
     * @throws InvalidInfoException
     *
     */
    public void validate() throws InvalidInfoException {

        int i;

        // Check if zip and phone are numeric
        for( i = 0; i < zip.length(); i++ ) {
            if( (zip.charAt(i) < '0') || (zip.charAt(i) > '9') ) {
                throw new InvalidInfoException("Invalid zip in address: "
                                               + zip);
            }
        }

        for( i = 0; i < phone.length(); i++ ) {
            if( (phone.charAt(i) < '0') || (phone.charAt(i) > '9') ) {
                throw new InvalidInfoException("Invalid phone in address: "
                                               + phone);
            }
        }
    }
}


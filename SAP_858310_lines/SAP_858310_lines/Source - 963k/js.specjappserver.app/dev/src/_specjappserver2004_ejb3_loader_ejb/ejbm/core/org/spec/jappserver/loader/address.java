/*
 * Copyright (c) 2001-2007 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *
 * This source code is provided as is, without any express or implied warranty.
 *
 *  History:
 *  Date        ID, Company               Description
 *  ----------  -----------------------   ---------------------------------------------------------------
 *  2001        Shanti Subramanyam, SUN   Created
 *  2002/04/12  Matt Hogstrom, IBM        Conversion from ECperf 1.1 to SPECjAppServer2001
 *  2002/07/10  Russell Raymundo, BEA     Conversion from SPECjAppServer2001 to 
 *                                        SPECjAppServer2002 (EJB2.0).
 *  2007/10/02  Bernhard Riedhofer, SAP   Integration of loader into SPECjAppServer2007 application
 */
package org.spec.jappserver.loader;

/*
 * Generates random data for an address.
 */
class Address {
    
    static final class RandomGenerator {

        private final RandNum r;

        RandomGenerator() {
            r = new RandNum();
        }
        
        RandomGenerator(final long seed) {
            r = new RandNum(seed);
        }
        
        Address createAdress() {
            return new Address(r);
        }
    }

    private final String contact;
    private final String phone;
    private final String zip;
    private final String country;
    private final String state;
    private final String city;
    private final String street2;
    private final String name;
    private final String street1;

    private Address(final RandNum r) {
        name = r.makeAString(8, 16);
        street1 = r.makeAString(10, 20);
        street2 = r.makeAString(10, 20);
        city = r.makeAString(10, 20);
        state = r.makeAString(2, 2);
        country = r.makeAString(3, 10);
        zip = r.makeNString(4, 4) + "11111";
        phone = r.makeNString(12, 16);
        contact = r.makeAString(10, 25);
    }

    String getStreet1() {
        return street1;
    }

    String getName() {
        return name;
    }

    String getStreet2() {
        return street2;
    }

    String getCity() {
        return city;
    }

    String getState() {
        return state;
    }

    String getCountry() {
        return country;
    }

    String getZip() {
        return zip;
    }

    String getPhone() {
        return phone;
    }

    String getContact() {
        return contact;
    }
}

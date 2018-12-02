/*
 * Copyright (c) 2001-2007 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 * This source code is provided as is, without any express or implied warranty.
 *
 *  History:
 *  Date        ID                          Description
 *  ----------  ------------------------    ----------------------------------------------------------------
 *  2003/01/18  Balu                        Created
 *  2007/10/02  Bernhard Riedhofer, SAP     Integration of loader into SPECjAppServer2007 application
 */
package org.spec.jappserver.loader;

/*
 * Generate customer names by pairing Car Manufacturer names and 
 * names from an array.
 */
class CustHelper {
    private static final String names[] = { "Aca", "Adamson", "Appajodu", "Arnold", "Barnes", "Basler", "Beer", "Bhaskar",
            "Brebner", "Buch", "Butera", "Carole", "Chak", "Chapman", "Chen", "Chen", "Coha", "Daly", "Dauria", "Dixit",
            "Donley", "Eric", "Evan", "Fisher", "Gombold", "Gray", "Harlan", "Hemant", "Hogstrom", "Honghua", "Jacob", "Jason",
            "Judy", "Kalyan", "Khawaja", "Kounev", "Krishnamurthy", "Lemerre", "Loen", "Marlow", "Mensah", "Mueller", "Nick",
            "Nigam", "Park", "Pierantoni", "Prasad", "Pyda", "Raj", "Rajiv", "Ramesh", "Realmuto", "Ricardo", "Rick",
            "Russell", "Rye", "Sam", "Satyajit", "Schoof", "Scot", "Silverman", "Smith", "Spyker", "Stan", "Stancox",
            "Stecher", "Steven", "Sthanikam", "Troop", "Weicker", "Wulf", "Zeier" };

    // get the idx'th name in the array. starts from 0.
    static String getName(int idx) {
        if (idx < 0) {
            throw new IllegalArgumentException("Ilegal index specified for Last name:" + idx);
        }
        idx = idx % names.length;
        return new String(names[idx]);
    }

    // return total number of unique names we have.
    static int getNumNames() {
        return names.length;
    }
}

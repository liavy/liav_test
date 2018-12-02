/*
 * Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *
 * This source code is provided as is, without any express or implied warranty.
 *
 *  History:
 *  Date        ID, Company                Description
 *  ----------  ------------------------   ---------------------------------------------------------
 *  2001/../..  Shanti Subramanyam, SUN    Created.
 *  2002/04/12  Matt Hogstrom, IBM         Conversion from ECperf 1.1 to SPECjAppServer2001.
 *  2002/07/10  Russel Raymundo, BEA       Conversion from SPECjAppServer2001 to
 *                                         SPECjAppServer2002 (EJB2.0).
 *  2003/11/25  Tom Daly, SUN              Added getPart(int start, int end) to allow selection of a
 *                                         range of itemids. This is required to support categories.
 *  2004/01/09  Samuel Kounev, Darmstadt   Modified to generate longer partId strings (char(20)).
 *
 */

 
package org.spec.jappserver.driver;

/**
 * This class generates a random part number.
 *
 * @author Shanti Subramanyam
 */
public class RandPart {
	static final int A = 63;
	RandNum r;
	int PGS = 1;
	int numItems;

    /**
     * Constructs the RandPart object.
     * @param r The random number generator
     * @param numItems The number of items to select from
     * @param scale The scale
     */
    RandPart(RandNum r, int numItems, int scale) {
		this.r = r;
		this.numItems = numItems;
		this.PGS = scale;
	}

        
        /**
         * This method generates a random part id to use for access to the
         * item table in the orders database but does so within the range of
         * ids that fall inside the current category.
         * @param start Start of the range
         * @param end End of the range
         * @return Part id
         */
        public String getPart(int start , int end) {
        /*
         * part_id is a combination of 5 numeric, 5 alpha,5 numeric
         * For manufactured parts, the part_id is of the form
         * <scale>"MITEM"<n> where n ranges from 1-10. (There are
         * 10 manufactured parts per PG)
         */
            
            int p3 = r.random(start, end);
            String p_id = prt1(1) + "MITEM" + prt3(p3);
            return(p_id);
        }      
        
        
        /**
         * This method generates a random part id to use for access
         * to the item table in the orders database.
         *
         * @return String part id
         */
        public String getPart() {
        /*
         * part_id is a combination of 5 numeric, 5 alpha,5 numeric
         * For manufactured parts, the part_id is of the form
         * <scale>"MITEM"<n> where n ranges from 1-10. (There are
         * 10 manufactured parts per PG)
         */
            
            int p3 = r.random(1, numItems);
            String p_id = prt1(1) + "MITEM" + prt3(p3);
            return(p_id);
        }

    String prt1(int i) {
        Integer j = new Integer(i);
        if (i < 10) 
            return ("0000" + j.toString());
        else if (i < 100)
            return ("000" + j.toString());
        else if (i < 1000)
            return ("00" + j.toString());
        else if (i < 10000)
            return ("0" + j.toString());
        else
            return (j.toString());
    }
    
    String prt3(int i) {
        Integer j = new Integer(i);
        if (i < 10)
            return("000000000" + j.toString());
        else if (i < 100)
            return("00000000" + j.toString());
        else if (i < 1000)
            return("0000000" + j.toString());
        else if (i < 10000)
            return("000000" + j.toString());
        else if (i < 100000)
            return("00000" + j.toString());
        else if (i < 1000000)
            return("0000" + j.toString());
        else if (i < 10000000)
            return("000" + j.toString());
        else if (i < 100000000)
            return("00" + j.toString());
        else if (i < 1000000000)
            return("0" + j.toString());
        else
            return(j.toString());
    }

}



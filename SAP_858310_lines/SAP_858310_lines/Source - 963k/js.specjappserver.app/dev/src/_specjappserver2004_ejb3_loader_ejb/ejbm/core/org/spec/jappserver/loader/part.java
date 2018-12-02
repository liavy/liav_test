/*
 * Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 * This source code is provided as is, without any express or implied warranty.
 *
 *  History:
 *  Date        ID, Company               Description
 *  ----------  ------------------------  ---------------------------------------------
 *  2001/../..  Shanti Subramanyam, SUN   Created
 *  2002/04/12  Matt Hogstrom, IBM        Conversion from ECperf 1.1 to SPECjAppServer2001
 *  2002/07/10  Russel Raymundo, BEA      Conversion from SPECjAppServer2001 to
 *                                        SPECjAppServer2002 (EJB2.0).
 *  2003/11/06  Samuel Kounev, Darmstadt  Modified database scaling rules (see osgjava-5681).
 *  2003/11/25  Samuel Kounev, Darmstadt  Modified database scaling rules as per osgjava-5891.
 *  2004/01/09  Samuel Kounev, Darmstadt  Modified to generate longer partId strings (char(20)).
 *  2007/10/02  Bernhard Riedhofer, SAP   Integration of loader into SPECjAppServer2007 application
 */
package org.spec.jappserver.loader;

/*
 * Generates random data for a part.
 */
class Part {

    /*
     * The safety stock is just to prevent us from depleting the inventory. Since we do not actually couple the component level
     * demand (in BOM) with the pDemand, we can run into a low pDemand and a high count in BOM which will deplete the inventory
     * fairly quick. Changing the safety stock is performance neutral.
     */
    private static final double SAFETY_STOCK = 2000.0;

    String pId;
    String pName;
    String pDesc;
    double pCost;
    private final String pUnit;
    private final String pRev;
    private final int pLeadTime;
    private final double pDemand;
    private final int pPlanner;

    private static String randomN4A2String(final RandNum rand) {
        return rand.makeNString(4, 4) + rand.makeAString(2, 2);
    }

    static String getPartId(final int scale, String str, final int id) {
        return Helper.toXDigitString(scale, 5) + str + Helper.toXDigitString(id, 10);
    }
    
    public Part(final RandNum r) {
        pUnit = r.makeAString(2, 10);
        pRev = randomN4A2String(r);
        pLeadTime = r.random(2, 10);
        // New pDemand unit is in components/sec.
        // The avg depletion rate is 0.69825 ~ 0.7
        pDemand = r.drandom(0.2d, 1.2d);
        pPlanner = r.random(1, 100000);
    }

    String getId() {
        return pId;
    }

    int getType() {
        return 0;
    }

    int getInd() {
        return 1;
    }

    String getName() {
        return pName;
    }

    String getDesc() {
        return pDesc;
    }

    double getCost() {
        return pCost;
    }

    double getPrice() {
        return 0.0;
    }

    String getUnit() {
        return pUnit;
    }

    String getRev() {
        return pRev;
    }

    int getLeadTime() {
        return pLeadTime;
    }

    double getDemand() {
        return pDemand;
    }

    int getLowMark() {
        return (int) Math.ceil(pDemand * pLeadTime * (1.0 + SAFETY_STOCK));
    }

    int getContainerSize() {
        // Planned reordering every 10000 sec.
        return (int) Math.ceil(pDemand * 10000);
    }

    int getPlanner() {
        return pPlanner;
    }
}

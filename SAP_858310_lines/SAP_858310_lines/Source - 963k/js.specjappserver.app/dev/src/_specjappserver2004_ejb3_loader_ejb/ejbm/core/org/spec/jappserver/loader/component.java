/*
 * Copyright (c) 2001-2007 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *
 * This source code is provided as is, without any express or implied warranty.
 *
 *  History:
 *  Date        ID, Company               Description
 *  ----------  -----------------------   ---------------------------------------------------------------
 *  2007/10/02  Bernhard Riedhofer, SAP   Created, integration of loader into SPECjAppServer2007 application
 */
package org.spec.jappserver.loader;

import java.util.ArrayList;
import java.util.List;

/*
 * Generates random data for a component.
 */
final class Component extends Part {

    public static final int MAX_COMPONENTS_PER_ASSEMBLY = 15;
    public static final int MAX_LINES_PER_PURCHASED_ORDER = 9;
    
    private static final String PARTS[] = { "BARAA", "OUGHT", "ABLEA", "PRIAA", "PRESA", "ESEAA",
        "ANTIA", "CALLY", "ATION", "EINGA", "IRESE", "QUIET", "LINES", "NAILS", "SCREW" };

    static final class RandomGenerator {

        private final RandNum r;
        private int componentNumber;
        private CardDeck usedParts;
        
        RandomGenerator(final long seed, int startComponentNumber) {
            r = new RandNum(seed);
            componentNumber = startComponentNumber;
            usedParts = new CardDeck(0, PARTS.length - 1, r.nextLong());
        }

        List<Part> createAssemblyComponents (final int scale) {
            int cnt = r.random(5, MAX_COMPONENTS_PER_ASSEMBLY);
            List<Part> result = new ArrayList<Part>(cnt);
            for (int i = 0; i < cnt; i++) {
                result.add(createComponent(scale));
            }
            return result;
        }
        
        private Component createComponent(final int scale) {
            Component result = new Component(r, scale, componentNumber, PARTS[usedParts.nextCard()]);
            componentNumber++;
            return result;
        }
    }

    public Component(final RandNum r, final int scale, final int partNo, final String partName) {
        super(r);
        pId = Part.getPartId(scale, partName, partNo);
        pName = r.makeAString(5, 10);
        pDesc = r.makeAString(30, 100);
        pCost = r.drandom(.10, 100.00);
    }

    @Override
    int getInd() {
        return 2;
    }
}

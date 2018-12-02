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

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

/*
 * This file creates and maintains a card deck with specified number of items.
 * Note that this currently only works for integers.
 */
class CardDeck {
    private final RandNum r;
    private ArrayList<Integer> unusedDeck;
    private ArrayList<Integer> usedDeck;

    /*
     * Constructor
     * Initialize card deck.
     */
    CardDeck(int start, int end, long seed) {
        r = new RandNum(seed);
        final int range = end - start + 1;
        unusedDeck = new ArrayList<Integer>(range);
        usedDeck = new ArrayList<Integer>(range);
        for (int i = start; i <= end; i++) {
            unusedDeck.add(new Integer(i));
        }
    }

    Set<Integer> getUnusedDeck()
    {
        TreeSet<Integer> result = new TreeSet<Integer>();
        result.addAll(unusedDeck);
        return result;
    }
    
    Set<Integer> getUsedDeck()
    {
        TreeSet<Integer> result = new TreeSet<Integer>();
        result.addAll(usedDeck);
        return result;
    }
    
    /**
     * Select a random number from our deck.
     */
    int nextCard() {
        if (unusedDeck.isEmpty()) {
            ArrayList<Integer> tmp = unusedDeck;
            unusedDeck = usedDeck;
            usedDeck = tmp;
        }

        // After the selection of a card the bucket of the selected card in the array list is empty.
        // Due to performance reasons we do not move all the elements behind the selected bucket.
        // Instead we move only the last element.
        final int indexOfLastElement = unusedDeck.size() - 1;
        final Integer lastCard = unusedDeck.remove(indexOfLastElement);

        final int selectedIndex = r.random(0, indexOfLastElement);
        final Integer selectedCard = selectedIndex == indexOfLastElement ? lastCard : unusedDeck.set(selectedIndex, lastCard);

        usedDeck.add(selectedCard);
        return selectedCard.intValue();
    }
}

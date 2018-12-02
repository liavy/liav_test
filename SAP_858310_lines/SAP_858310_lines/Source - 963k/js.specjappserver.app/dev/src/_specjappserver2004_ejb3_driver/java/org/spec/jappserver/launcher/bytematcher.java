/*
 * Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *
 * This source code is provided as is, without any express or implied warranty.
 *
 *  History:
 *  Date        ID, Company               Description
 *  ----------  -----------------------   ---------------------------------------------------------------
 *  2001        Akara Sucharitakul, SUN   Created
 *  2002/04/12  Matt Hogstrom, IBM        Conversion from ECperf 1.1 to SPECjAppServer2001
 *  2002/07/10  Russell R., BEA           Conversion from SPECjAppServer2001 to 
 *                                        SPECjAppServer2002 (EJB2.0).
 *
 * $Id: ByteMatcher.java,v 1.2 2004/02/17 17:16:03 skounev Exp $
 */

package org.spec.jappserver.launcher;

/**
 * ByteMatcher matches a string to discrete buffers.
 * @author Akara Sucharitakul
 */
public class ByteMatcher {

    byte[] matchSequence;
    byte[] partialSequence;

    /**
     * Constructs a new ByteMatcher.
     * @param matchString The string to search for.
     */
    public ByteMatcher(String matchString) {
        matchSequence = matchString.getBytes();
    }

    /**
     * Match a buffer with a certain length.
     * @param buffer The buffer to be matched against
     * @param len    The buffer length
     * @return       true if match string is part of buffer, false otherwise
     */
    public boolean match(byte[] buffer, int len) {

        // Try to match partialSequence with start of buffer.
        if (partialSequence != null && match(buffer, len, 0, partialSequence))
            return true;

        // Scan the buffer and try to match all possible offsets.
        for (int i = 0; i < len; i++) {
            if (match(buffer, len, i, matchSequence))
                return true;
            // If partially matched end, just stop scanning.
            if (partialSequence != null)
                return false;
        }
        return false;
    }

    /**
     * Match a buffer at a certain porision to a byte sequence.
     * @param buffer The buffer
     * @param len    The length of the buffer
     * @param offset The offset in the buffer to be matched
     * @param seq    The byte sequence to be matched
     * @return       true if match succeeds, false otherwise
     */
    private boolean match(byte[] buffer, int len, int offset, byte[] seq) {

        partialSequence = null;

        if (offset > len)
            throw new ArrayIndexOutOfBoundsException("Offset " + offset + 
                                                     " out of bounds!");
        else if (offset == len)
            return false;

        for (int i = 0; i < seq.length; i++) {
            if (offset + i == len) {
                partialSequence = new byte[seq.length - i];
                System.arraycopy(seq, i, partialSequence, 0, seq.length - i);
                return false;
            }
            if (buffer[offset + i] != seq[i])
                return false;
        }
        return true;
    }
}

package org.spec.jappserver.loader;

import java.util.Random;

final class RandNum {

    private static final char[] alpha = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
        'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e',
        'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };

    private final Random r;
    
    RandNum() {
        r = new Random();
    }

    RandNum(final long seed) {
        r = new Random(seed);
    }

    long nextLong() {
        return r.nextLong();
    }

    /**
     * Selects a random number uniformly distributed between x and y, inclusively, with a mean of (x+y)/2.
     */
    int random(int x, int y) {
        int n = r.nextInt();
        return x + Math.abs(n % (y - x + 1));
    }

    /**
     * Selects a long random number uniformly distributed between x and y, inclusively, with a mean of (x+y)/2.
     */
    long lrandom(long x, long y) {
        long n = r.nextLong();
        return x + Math.abs(n % (y - x + 1));
    }

    /**
     * @see #lrandom(long, long), but returns double.
     */
    double drandom(double x, double y) {
        return (x + (r.nextDouble() * (y - x)));
    }

    /**
     * Generates a random string of alphanumeric characters of random length of mininum x, maximum y and mean (x+y)/2.
     */
    String makeAString(int x, int y) {
        final int len; /* len of string */
        if (x == y) {
            len = x;
        } else {
            len = random(x, y);
        }
        char[] buffer = new char[len];
        for (int i = 0; i < len; i++) {
            int j = Math.abs(random(0, 60));
            buffer[i] = alpha[j];
        }
        return new String(buffer);
    }

    /**
     * Generates a random string of numeric characters of random length of mininum x, maximum y and mean (x+y)/2.
     */
    String makeNString(int x, int y) {
        final int len;
        if (x == y) {
            len = x;
        } else {
            len = random(x, y);
        }
        final StringBuffer str = new StringBuffer(len);
        for (int i = 0; i < len; i++) {
            str.append(random(0, 9));
        }
        return str.toString();
    }
}

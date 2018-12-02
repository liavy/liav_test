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

import java.sql.Date;
import java.util.Set;

import org.spec.jappserver.common.util.DateTimeNormalizer;

/*
 * Generates random data for a cutomer.
 */
class Customer {

    private static final String names[] = { "Aca", "Adamson", "Alan", "Appajodu", "Arnold", "Barnes", "Basler", "Beer",
            "Bernhard", "Bhaskar", "Brebner", "Buch", "Butera", "Carole", "Chak", "Chapman", "Chen", "Chen", "Clebert",
            "Coha", "Daly", "Dauria", "Dixit", "Donley", "Eric", "Evan", "Fisher", "Gombold", "Gray", "Harlan",
            "Hemant", "Hogstrom", "Honghua", "Ireland", "Jacob", "Jason", "Judy", "Kalyan", "Khawaja", "Kai", "Kounev",
            "Krishnamurthy", "Lemerre", "Loen", "Narayan", "Marlow", "Mensah", "Mueller", "Nick", "Nigam", "Park",
            "Pierantoni", "Prasad", "Pyda", "Raj", "Rajiv", "Ramesh", "Realmuto", "Ricardo", "Rick", "Riedhofer",
            "Rob", "Robert", "Russell", "Rye", "Sachs", "Sam", "Samuel", "Saraswathy", "Satyajit", "Schoof", "Scot",
            "Silverman", "Smith", "Spyker", "Stan", "Stancox", "Stecher", "Steve", "Steven", "Sthanikam", "Suconic",
            "Tom", "Troop", "Wisniewski", "Weicker", "Wulf", "Zeier" };
     
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

    static Set<Integer> getBadCreditCustomers(int startCustomerId, int numCustomers, int everyXthWithBadCredit,
            SeedGenerator seedGen) {
        long customerCreditSeed = seedGen.getCustomerCreditSeed(startCustomerId / numCustomers);
        final CardDeck custDeck = new CardDeck(startCustomerId, startCustomerId + numCustomers - 1, customerCreditSeed);
        final int numBadCredit = (numCustomers - 1) / everyXthWithBadCredit;
        for (int i = 0; i < numBadCredit; i++) {
            custDeck.nextCard();
        }
        return custDeck.getUsedDeck();
    }

    static final class RandomGenerator {

        private final RandNum r;
        private final Address.RandomGenerator addressGenerator;

        RandomGenerator() {
            r = new RandNum();
            addressGenerator = new Address.RandomGenerator();
        }

        Customer createCustomer() {
            return new Customer(r, addressGenerator);
        }
    }

    private final String cFirst;
    private final String cLast;
    private final Address cAddress;
    private final Date cSince;
    private double cBalance;
    private final double cYtdPayment;
    private String cCredit;
    private double cCreditLimit;
    private final String cContact;
    
    Customer(RandNum r, Address.RandomGenerator addressGenerator) {
        cFirst = Customer.getName(r.random(0, Customer.getNumNames() - 1));
        cLast = CarNameGen.getMfr(r.random(0, CarNameGen.getNumMfrs() - 1));
        cAddress = addressGenerator.createAdress();
        long now = System.currentTimeMillis();
        long sinceStart = now - 7l * 365 * 24 * 60 * 60 * 1000;
        long millis = DateTimeNormalizer.normalizeSqlDateMillis(r.lrandom(sinceStart, now)); // Range is back ~7 yrs.
        cSince = new Date(millis);
        cBalance = r.random(0, 25000);
        if (cBalance < 500.0) {
            cBalance = 0.0;
        }
        cYtdPayment = r.random(0, 350000);
        /*
         * Credit limit should be large enough to encompass all
         * orders. A regular customer order has max qty of 20 and
         * large order has 200. The max price of a car is 50,000 =>
         * the balance should be >= 200*50000 = 10,000,000
         * cCreditLimit[i] = rand.drandom(300000, 3000000);
         */
        cCredit = "GC";
        cCreditLimit = r.drandom(10000000, 50000000);
        cContact = r.makeAString(10, 25);
    }

    void setBadCredit()
    {
        cCredit = "BC";
        cCreditLimit = 0.0;
    }

    String getFirst() {
        return cFirst;
    }

    String getLast() {
        return cLast;
    }

    Address getAddress() {
        return cAddress;
    }

    Date getSince() {
        return cSince;
    }

    double getBalance() {
        return cBalance;
    }

    double getYtdPayment() {
        return cYtdPayment;
    }

    String getCredit() {
        return cCredit;
    }

    double getCreditLimit() {
        return cCreditLimit;
    }

    String getContact() {
        return cContact;
    }
}

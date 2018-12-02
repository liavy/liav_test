/*
 * Copyright (c) 2001-2007 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *
 * This source code is provided as is, without any express or implied warranty.
 *
 *  History:
 *  Date        ID, Company               Description
 *  ----------  ------------------------  -------------------------------------------------------------------
 *  2001/../..  Shanti Subramanyam, SUN   Created
 *  2002/04/12  Matt Hogstrom, IBM        Conversion from ECperf 1.1 to SPECjAppServer2001
 *  2002/07/10  Russel Raymundo, BEA      Conversion from SPECjAppServer2001 to
 *                                        SPECjAppServer2002 (EJB2.0).
 *  2003/01/24  BSthanikam, Oracle        Change customer names that are populated in
 *                                        db(o_customer).Also changed the part names to be
 *                                        car names.
 *  2003/02/03  BSthanikam, Oracle        Load c_inventory table.
 *  2003/03/21  BSthanikam, Oracle        Since O_Customer tbl is removed, do not send the
 *                                        data to the orders pipe. Generate description for
 *                                        the Cars. Change price of the Assemblies to
 *                                        correspond to that of cars. Increase the Balance of
 *                                        customers to take care of the higer cost of assemblies now.
 *  2003/03/24  BSthanikam, Oracle        Load U_sequences with the row for inventory.
 *  2003/04/03  Russel Raymundo, BEA      Explicitly specified table columns names for
 *                                        inserts.  Change Sequence Table nextnum for
 *                                        inventory to be one greater than number loaded.
 *                                        Enabled Distributed Database loading.
 *  2003/06/10  Samuel Kounev, Darmstadt  Fixed bug found by Evan Ireland (Sybase)
 *                                        c_customerinventory --> C_customerinventory; S_ID --> s_id
 *  2003/07/28  Samuel Kounev, Darmstadt  Removed C_rule table.
 *  2003/08/28  John Stecher,   IBM       Modified the loader to correctly load for discounted vehicles price
 *  2003/09/09  Samuel Kounev, Darmstadt  Fixed bug in generating total cost values for the
 *                                        customer_inventory table (osgjava-5487).
 *  2003/11/06  Samuel Kounev, Darmstadt  Modified database scaling rules (see osgjava-5681).
 *  2003/11/06  Samuel Kounev, Darmstadt  Corrected pDemand computation as per osgjava-5843.
 *  2003/11/25  Samuel Kounev, Darmstadt  Modified database scaling rules as per osgjava-5891.
 *  2004/01/06  Balu Sthanikam, Oracle    Wrap around car names after exhausting them, osgjava-6209,6230.
 *  2004/01/07  Akara Sucharitakul, Sun   Implemented new pDemand model (as per osgjava-6255):
 *                                        Changed the demand, loMark, and hiMark calculation.
 *                                        Cleaned up unused variables and import statements.
 *  2004/01/09  Samuel Kounev, Darmstadt  Adjusted getIdx to new partId format (char(20)).
 *  2004/01/13  Rafay Khawaja, Borland    Re-arranged code, and added some threads for cocurrent 
 *                                        loading where possible.
 *  2004/02/20  Rafay Khawaja, Borland    Addressed issue with assigning correct categories to 
 *                                        ranges of ItemIds when loading using multiple threads.
 *  2007/10/02  Bernhard Riedhofer, SAP   Integration of loader into SPECjAppServer2007 application
 */
package org.spec.jappserver.loader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.jms.JMSException;
import javax.naming.NamingException;

/*
 * Loads the tables of corporate domain.
 */
class LoadCorp extends Load {

    static final int EVERY_XTH_CUSTOMER_WITH_BAD_CREDIT = 10;
    static final int MAX_INVENTORY_PER_CUSTOMER = 10;

    private static final String CUSTOMER_TABLE_NAME = "C_CUSTOMER";
    private static final String CUSTOMERINVENTORY_TABLE_NAME = "C_CUSTOMERINVENTOR";
    private static final String PART_TABLE_NAME = "C_PARTS";
    private static final String SITE_TABLE_NAME = "C_SITE";
    private static final String SUPPLIER_TABLE_NAME = "C_SUPPLIER";

    static Map<Integer, Double> assemblyPrices;
    static int threadsUsingAssemblyPrices = 0;
    
    static synchronized void useAssemblyPrices(int numAssemblies, int numAssembliesPerThread, SeedGenerator seedGen) {
        if (threadsUsingAssemblyPrices == 0) {
            assemblyPrices = Assembly.getAssemblyPrices(numAssemblies, numAssembliesPerThread, seedGen);
        }
        threadsUsingAssemblyPrices++;
    }

    // Free it for GC if there are no more users.
    // We want to use only one copy per VM.
    // This technique is needed since the threads could be executed on more than one VM.
    static synchronized void releaseAssemblyPrices() {
        threadsUsingAssemblyPrices--;
        if (threadsUsingAssemblyPrices == 0) {
            assemblyPrices = null;
        }
    }

    LoadCorp(ActionMessage message) {
        super(message);
        message.setDomain(LoadMessage.CORPORATE_DOMAIN);
        dbKey = DatabaseHelper.CORPDB_KEY;
    }

    @Override
    void addActionMessages(TreeSet<ActionMessage> actionMessages) throws CloneNotSupportedException {
        addActionMessages(actionMessages, CUSTOMER_TABLE_NAME, numCustomers);
        addActionMessages(actionMessages, CUSTOMERINVENTORY_TABLE_NAME, numCustomers);
        addActionMessages(actionMessages, PART_TABLE_NAME, numAssemblies);
        addActionMessages(actionMessages, SITE_TABLE_NAME, LOAD_IN_ONE_SECTION);
        addActionMessages(actionMessages, SUPPLIER_TABLE_NAME, LOAD_IN_ONE_SECTION);
    }

    @Override
    void loadSequences() throws SQLException, NamingException, JMSException {
        loadSequence("customer", numCustomers + 1);
        loadSequence("inventory", MAX_INVENTORY_PER_CUSTOMER * numCustomers + 1);
    }

    @Override
    boolean loadTableSection(final String[] tables, final int start, final int num) throws SQLException, NamingException, JMSException {
        String strTables = MessageHelper.getCommaSeperatedList(tables);
        if (strTables.equals(CUSTOMER_TABLE_NAME)) {
            return loadCustomer(start, num);
        } else if (strTables.equals(CUSTOMERINVENTORY_TABLE_NAME)) {
            return loadCustomerInventory(start, num);
        } else if (strTables.equals(PART_TABLE_NAME)) {
            return loadParts(start, num);
        } else if (strTables.equals(SITE_TABLE_NAME)) {
            return loadSite0();
        } else if (strTables.equals(SUPPLIER_TABLE_NAME)) {
            return loadSupplier0();
        } else {
            throw new IllegalArgumentException("Table " + strTables + " not found.");
        }
    }

    private boolean loadCustomer(final int start, final int num)
            throws SQLException, NamingException, JMSException {
        final int end = start + num;
        final Connection dbConnection = getConnection();
        try
        {
            final PreparedStatement cs = dbConnection
                    .prepareStatement("insert into C_CUSTOMER (C_ID, C_FIRST, C_LAST, "
                            + "C_STREET1, C_STREET2, C_CITY, C_STATE, " + "C_COUNTRY, C_ZIP, C_PHONE, "
                            + "C_CONTACT, C_SINCE, C_BALANCE, C_CREDIT, C_CREDIT_LIMIT, C_YTD_PAYMENT) "
                            + "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
            try {
                Customer.RandomGenerator customerGen = new Customer.RandomGenerator();
                Set<Integer> badCreditCustomers = Customer.getBadCreditCustomers(start, num,
                        EVERY_XTH_CUSTOMER_WITH_BAD_CREDIT, seedGen);
                int batchCount = 0;
                final MessageHelper.InterruptChecker ic = new MessageHelper.InterruptChecker();
                for (int cid = start; cid < end; cid++) {
                    if (ic.isInterrupted()) {
                        return false;
                    }
                    Customer cust = customerGen.createCustomer();
                    if (badCreditCustomers.contains(cid)) {
                        cust.setBadCredit();
                    }
                    Address address = cust.getAddress();
                    cs.setInt(1, cid);
                    cs.setString(2, cust.getFirst());
                    cs.setString(3, cust.getLast());
                    cs.setString(4, address.getStreet1());
                    cs.setString(5, address.getStreet2());
                    cs.setString(6, address.getCity());
                    cs.setString(7, address.getState());
                    cs.setString(8, address.getCountry());
                    cs.setString(9, address.getZip());
                    cs.setString(10, address.getPhone());
                    cs.setString(11, cust.getContact());
                    cs.setDate(12, cust.getSince());
                    cs.setDouble(13, cust.getBalance());
                    cs.setString(14, cust.getCredit());
                    cs.setDouble(15, cust.getCreditLimit());
                    cs.setDouble(16, cust.getYtdPayment());
                    batchCount = DatabaseHelper.addAndExecuteBatchIfFull(dbConnection, batchCount, cs);
                }
                DatabaseHelper.executeBatch(dbConnection, batchCount, cs); // load remaining rows
            } finally {
                cs.close();
            }
        } finally {
            dbConnection.close();
        }
        return true;
    }

    private boolean loadCustomerInventory(final int start, final int num) throws SQLException, NamingException, JMSException {
        final int end = start + num;
        final Connection dbConnection = getConnection();
        try
        {
            final PreparedStatement cs = dbConnection.prepareStatement("insert into C_CUSTOMERINVENTOR"
                    + " (CI_ID, CI_ITEMID, CI_QUANTITY, CI_CUSTOMERID, CI_VALUE)" + " values (?,?,?,?,?)");
            try {
                int customerInventorId = MAX_INVENTORY_PER_CUSTOMER * (start - 1) + 1;
                int batchCount = 0;
                useAssemblyPrices(numAssemblies, Helper.getNumPerThread(numAssemblies, parallelism), seedGen);
                try {
                    final MessageHelper.InterruptChecker ic = new MessageHelper.InterruptChecker();
                    for (int customerId = start; customerId < end; customerId++) {
                        if (ic.isInterrupted()) {
                            return false;
                        }
                        // each customer has 5 ... MAX_INVENTORY_PER_CUSTOMER cars in inventory
                        final int numCars = r.random(5, MAX_INVENTORY_PER_CUSTOMER);
                        for (int k = 0; k < numCars; k++) {
                            final int assemblyNum = r.random(1, numAssemblies);
                            final String pId = Assembly.getAssemblyId(DatabaseHelper.CUR_BATCH, assemblyNum);
                            final int pQty = r.random(1, 5);
                            final double value = assemblyPrices.get(assemblyNum).doubleValue() * pQty * r.drandom(0.3, 1);
                            cs.setInt(1, customerInventorId++);
                            cs.setString(2, pId);
                            cs.setInt(3, pQty);
                            cs.setInt(4, customerId);
                            cs.setDouble(5, value);
                            batchCount = DatabaseHelper.addAndExecuteBatchIfFull(dbConnection, batchCount, cs);
                        }
                    }
                } finally {
                    releaseAssemblyPrices();
                }
                DatabaseHelper.executeBatch(dbConnection, batchCount, cs); // load remaining rows
            } finally {
                cs.close();
            }
        } finally {
            dbConnection.close();
        }
        return true;
    }

    private boolean loadParts(final int start, final int num) throws SQLException, NamingException, JMSException {
        final int end = start + num;
        final Connection dbConnection = getConnection();
        try
        {
            final PreparedStatement cs = dbConnection
                    .prepareStatement("insert into C_PARTS (P_ID, P_NAME, P_DESC, P_REV, P_UNIT, "
                            + "P_COST, P_PRICE, P_PLANNER, P_TYPE, P_IND) "
                            + "values(?,?,?,?,?,?,?,?,?,?)");
            try {
                int assemblyBlock = start/num;
                int startComponentNumber = (start - 1)*Component.MAX_COMPONENTS_PER_ASSEMBLY + 1;
                final Assembly.RandomGenerator assemblyGen = new Assembly.RandomGenerator(seedGen
                        .getAssemblySeed(assemblyBlock), start);
                final Component.RandomGenerator componentGen = new Component.RandomGenerator(seedGen
                        .getComponentSeed(assemblyBlock), startComponentNumber);
                int batchCount = 0;
                final MessageHelper.InterruptChecker ic = new MessageHelper.InterruptChecker();
                for (int j = start; j < end; j++) {
                    if (ic.isInterrupted()) {
                        return false;
                    }
                    Assembly assembly = assemblyGen.createAssembly(DatabaseHelper.CUR_BATCH);
                    final List<Part> parts = componentGen.createAssemblyComponents(DatabaseHelper.CUR_BATCH);
                    parts.add(assembly);
                    for (final Part part : parts) {
                        cs.setString(1, part.getId());
                        cs.setString(2, part.getName());
                        cs.setString(3, part.getDesc());
                        cs.setString(4, part.getRev());
                        cs.setString(5, part.getUnit());
                        cs.setDouble(6, part.getCost());
                        cs.setDouble(7, part.getPrice());
                        cs.setInt(8, part.getPlanner());
                        cs.setInt(9, part.getType());
                        cs.setInt(10, part.getInd());
                        batchCount = DatabaseHelper.addAndExecuteBatchIfFull(dbConnection, batchCount, cs);
                    }
                }
                DatabaseHelper.executeBatch(dbConnection, batchCount, cs); // load remaining rows
            } finally {
                cs.close();
            }
        } finally {
            dbConnection.close();
        }
        return true;
    }

    private boolean loadSite0() throws SQLException, NamingException, JMSException {
        final Connection dbConnection = getConnection();
        try
        {
            final PreparedStatement cs = dbConnection.prepareStatement("insert into C_SITE"
                    + " (SITE_ID, SITE_NAME, SITE_STREET1, SITE_STREET2, SITE_CITY, SITE_STATE, SITE_COUNTRY, SITE_ZIP)"
                    + " values (?,?,?,?,?,?,?,?)");
            try {
                final Address.RandomGenerator generator = new Address.RandomGenerator(seedGen.getSiteAddressSeed());
                int batchCount = 0;
                final MessageHelper.InterruptChecker ic = new MessageHelper.InterruptChecker();
                for (int sid = 1; sid <= Helper.NUM_SITES; sid++) {
                    if (ic.isInterrupted()) {
                        return false;
                    }
                    final Address adr = generator.createAdress();
                    cs.setInt(1, sid);
                    cs.setString(2, adr.getName());
                    cs.setString(3, adr.getStreet1());
                    cs.setString(4, adr.getStreet2());
                    cs.setString(5, adr.getCity());
                    cs.setString(6, adr.getState());
                    cs.setString(7, adr.getCountry());
                    cs.setString(8, adr.getZip());
                    batchCount = DatabaseHelper.addAndExecuteBatchIfFull(dbConnection, batchCount, cs);
                }
                DatabaseHelper.executeBatch(dbConnection, batchCount, cs); // load remaining rows
            } finally {
                cs.close();
            }
        } finally {
            dbConnection.close();
        }
        return true;
    }

    private boolean loadSupplier0() throws SQLException, NamingException, JMSException {
        final Connection dbConnection = getConnection();
        try
        {
            final PreparedStatement cs = dbConnection
                    .prepareStatement("insert into C_SUPPLIER"
                            + " (SUPP_ID, SUPP_NAME, SUPP_STREET1, SUPP_STREET2, SUPP_CITY, SUPP_STATE, SUPP_COUNTRY, SUPP_ZIP, SUPP_PHONE, SUPP_CONTACT)"
                            + " values (?,?,?,?,?,?,?,?,?,?)");
            try {
                final Address.RandomGenerator addressGenerator = new Address.RandomGenerator(seedGen.getSupplierAddressSeed());
                int batchCount = 0;
                final MessageHelper.InterruptChecker ic = new MessageHelper.InterruptChecker();
                for (int sid = 1; sid <= Helper.NUM_SUPPLIERS; sid++) {
                    if (ic.isInterrupted()) {
                        return false;
                    }
                    final Address adr = addressGenerator.createAdress();
                    cs.setInt(1, sid);
                    cs.setString(2, adr.getName());
                    cs.setString(3, adr.getStreet1());
                    cs.setString(4, adr.getStreet2());
                    cs.setString(5, adr.getCity());
                    cs.setString(6, adr.getState());
                    cs.setString(7, adr.getCountry());
                    cs.setString(8, adr.getZip());
                    cs.setString(9, adr.getPhone());
                    cs.setString(10, adr.getContact());
                    batchCount = DatabaseHelper.addAndExecuteBatchIfFull(dbConnection, batchCount, cs);
                }
                DatabaseHelper.executeBatch(dbConnection, batchCount, cs); // load remaining rows
            } finally {
                cs.close();
            }
        } finally {
            dbConnection.close();
        }
        return true;
    }
}

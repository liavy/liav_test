/*
 * Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 * This source code is provided as is, without any express or implied warranty.
 *
 *  History:
 *  Date        ID, Company               Description
 *  ----------  ------------------------  ------------------------------------------------------
 *  2002/03/24  ramesh, SUN Microsystem   Created
 *  2001/09/17  Matt Hogstrom, IBM        Modified the customer selection to avoid
 *                                        creating orders for customers with bad credit.
 *  2001/11/06  Matt Hogstrom, IBM        Modified  SQLDate to use a timestmp instead
 *                                        of a java.sql.Date to match the actual table
 *                                        column type.
 *  2002/04/12  Matt Hogstrom, IBM        Conversion from ECperf 1.1 to SPECjAppServer2001*
 *  2002/07/10  Russel Raymundo, BEA      Conversion from SPECjAppServer2001 to
 *                                        SPECjAppServer2002 (EJB2.0).
 *  2003/04/03  Russel Raymundo, BEA      Explicitly specified table columns names for
 *                                        inserts. Enabled Distributed Database loading.
 *  2003/03/21  BSthanikam, Oracle        Do not load O_Customer table. Change loading of
 *                                        o_orderline to load ol_total_value column too.
 *  2003/05/29  Samuel Kounev, Darmstadt  Changed to make sure all large orderlines are
 *                                        created with status 3 (completed).
 *  2003/11/06  Samuel Kounev, Darmstadt  Modified database scaling rules (see osgjava-5681).
 *  2003/11/24  Samuel Kounev, Darmstadt  Modified to populate new i_category column of the
 *                                        o_item table (see osgjava-6001).
 *  2003/11/25  Samuel Kounev, Darmstadt  Modified database scaling rules as per osgjava-5891.
 *  2004/01/13  Rafay Khawaja, Borland    Re-arranged code, and added some threads for cocurrent
 *                                        loading where possible.
 *  2004/02/20  Rafay Khawaja, Borland    Addressed issue with assigning correct categories to
 *                                        ranges of ItemIds when loading using multiple threads.
 *  2007/10/02  Bernhard Riedhofer, SAP   Created, integration of loader into SPECjAppServer2007 application
 */
package org.spec.jappserver.loader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import javax.jms.JMSException;
import javax.naming.NamingException;

/*
 * Loads the tables of orders domain.
 */
class LoadOrds extends Load {
    
    private static final int ORDER_SIZE = 5;
    private static final int ITEMS_PER_CATEGORY = 200;

    private static final String ITEM_TABLE_NAME = "O_ITEM";
    private static final String[] ORDERS_TABLE_NAMES = new String[] {"O_ORDERS", "O_ORDERLINE"};

    static Set<Integer> badCreditCustomers;
    static int threadsUsingBadCreditCustomers = 0;
    
    static synchronized void useBadCreditCustomers(int numCustomers, SeedGenerator seedGen) {
        if (threadsUsingBadCreditCustomers == 0) {
            badCreditCustomers = Customer.getBadCreditCustomers(1, numCustomers,
                    LoadCorp.EVERY_XTH_CUSTOMER_WITH_BAD_CREDIT, seedGen);
        }
        threadsUsingBadCreditCustomers++;
    }

    // Free it for GC if there are no more users.
    // We want to use only one copy per VM.
    // This technique is needed since the threads could be executed on more than one VM.
    static synchronized void releaseBadCreditCustomers() {
        threadsUsingBadCreditCustomers--;
        if (threadsUsingBadCreditCustomers == 0) {
            badCreditCustomers = null;
        }
    }
    
    LoadOrds(ActionMessage message) {
        super(message);
        message.setDomain(LoadMessage.ORDER_DOMAIN);
        dbKey = DatabaseHelper.ORDS_KEY;
    }

    @Override
    void addActionMessages(TreeSet<ActionMessage> actionMessages) throws CloneNotSupportedException {
        addActionMessages(actionMessages, ITEM_TABLE_NAME, numAssemblies);
        addActionMessages(actionMessages, ORDERS_TABLE_NAMES, numOrders);
    }

    @Override
    void loadSequences() throws SQLException, NamingException, JMSException {
        loadSequence("order", numOrders + 1);
    }

    @Override
    boolean loadTableSection(final String[] tables, final int start, final int num) throws SQLException, NamingException, JMSException {
        String strTables = MessageHelper.getCommaSeperatedList(tables);
        if (strTables.equals(ITEM_TABLE_NAME)) {
            return loadItem(start, num);
        } else if (strTables.equals(MessageHelper.getCommaSeperatedList(ORDERS_TABLE_NAMES))) {
            return loadOrders(start, num);
        } else {
            throw new IllegalArgumentException("Table " + strTables + " not found.");
        }
    }

    private boolean loadItem(final int start, final int num) throws SQLException, NamingException, JMSException {
        final int end = start + num;
        final Connection dbConnection = getConnection();
        try
        {
            final PreparedStatement cs = dbConnection.prepareStatement("insert into O_ITEM"
                    + " (I_ID, I_NAME, I_DESC, I_PRICE, I_DISCOUNT, I_CATEGORY)" + " values (?,?,?,?,?,?)");
            try {
                int assemblyBlock = start/num;
                final Assembly.RandomGenerator assemblyGen = new Assembly.RandomGenerator(seedGen
                        .getAssemblySeed(assemblyBlock), start);
                int batchCount = 0;
                final MessageHelper.InterruptChecker ic = new MessageHelper.InterruptChecker();
                for (int j = start; j < end; j++) {
                    if (ic.isInterrupted()) {
                        return false;
                    }
                    Assembly assembly = assemblyGen.createAssembly(DatabaseHelper.CUR_BATCH);
                    final String pId = assembly.getId();
                    final float iDiscount = (float) (r.drandom(0.00, 0.70));
                    final String idString = pId.substring(10);
                    final int idFromStr = Integer.parseInt(idString);
                    final int iCategory = (idFromStr - 1) / ITEMS_PER_CATEGORY;
                    cs.setString(1, pId);
                    cs.setString(2, assembly.getName());
                    cs.setString(3, assembly.getDesc());
                    cs.setDouble(4, assembly.getPrice());
                    cs.setFloat(5, iDiscount);
                    cs.setInt(6, iCategory);
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

    private boolean loadOrders(final int start, final int num)
            throws SQLException, NamingException, JMSException {
        final int end = start + num;
        final Connection dbConnection = getConnection();
        try
        {
            final String insertOrderStmtTxt = "insert into O_ORDERS"
                    + " (O_ID, O_C_ID, O_OL_CNT, O_DISCOUNT, O_TOTAL, O_STATUS, O_ENTRY_DATE, O_SHIP_DATE)"
                    + " values (?,?,?,?,?,?,?,?)";
            final String insertOrderLineStmtTxt = "insert into O_ORDERLINE"
                    + " (OL_ID, OL_O_ID, OL_I_ID, OL_QTY, OL_TOTAL_VALUE, OL_MSRP, OL_STATUS, OL_SHIP_DATE)"
                    + " values (?,?,?,?,?,?,?,?)";
            final PreparedStatement insertOrderStatement = dbConnection
                  .prepareStatement(insertOrderStmtTxt);
            try {
                final PreparedStatement insertOrderLineStatement = dbConnection
                      .prepareStatement(insertOrderLineStmtTxt);
                try {
                    useBadCreditCustomers(numCustomers, seedGen);
                    try {
                        return loadOrders0(start, end, numCustomers, numItems, dbConnection, insertOrderStatement,
                                insertOrderLineStatement);
                    } finally {
                        releaseBadCreditCustomers();
                    }
                } finally {
                    insertOrderLineStatement.close();
                }
            } finally {
                insertOrderStatement.close();
            }
        } finally {
            dbConnection.close();
        }
    }

    private void generateSortedItemIds(final String[] itemIds, final int count, final int numItems) {
        for (int k = 0; k < count; k++) {
            do {
                itemIds[k] = Assembly.getAssemblyId(DatabaseHelper.CUR_BATCH, r.random(1, numItems));
                int l;
                for (l = 0; l < k; l++) {
                    if (itemIds[k].equals(itemIds[l])) {
                        break;
                    }
                }
                if (l == k) {
                    break;
                }
            } while (true);
        }
        Arrays.sort(itemIds, 0, count);
    }

    private boolean loadOrders0(final int start, final int end, final int numCustomers, final int numItems,
            final Connection dbConnection,
            final PreparedStatement insertOrderStatement,
            final PreparedStatement insertOrderLineStatement) throws SQLException, NamingException, JMSException {
        final PreparedStatement[] preparedStatements = new PreparedStatement[] {
                insertOrderStatement, insertOrderLineStatement};
        final String olIid[] = new String[ORDER_SIZE];
        int batchCount = 0;
        final MessageHelper.InterruptChecker ic = new MessageHelper.InterruptChecker();
        for (int oId = start; oId < end; oId++) {
            if (ic.isInterrupted()) {
                return false;
            }
            int cID;
            do {
                cID = r.random(1, numCustomers);
            } while (badCreditCustomers.contains(cID));
            final int oCid = cID;
            final int oOlcnt = r.random(1, ORDER_SIZE);
            double oTotal = 0.0;
            final int oStatus = r.random(1, 3);
            double MSRPOrderTotal = 0.0;
            
            generateSortedItemIds(olIid, oOlcnt, numItems);
            
            for (int k = 0; k < oOlcnt; k++) {
                final int olQty;
                if (r.random(1, 100) <= 10) {
                    olQty = r.random(1, 99);
                } else {
                    olQty = r.random(1, 9);
                }
                final int olStatus;
                if (olQty > 20) {
                    olStatus = 3;
                } else {
                    olStatus = oStatus;
                }
                final double olValue = r.drandom(10000.00, 20000.00);
                final double olMSRP = olValue + r.drandom(5000.00, 20000.00);
                final double olTotalValue = olValue * olQty;
                MSRPOrderTotal += olMSRP * olQty;
                oTotal += olTotalValue;

                insertOrderLineStatement.setInt(1, k + 1);
                insertOrderLineStatement.setInt(2, oId);
                insertOrderLineStatement.setString(3, olIid[k]);
                insertOrderLineStatement.setInt(4, olQty);
                insertOrderLineStatement.setDouble(5, olTotalValue);
                insertOrderLineStatement.setDouble(6, olMSRP);
                insertOrderLineStatement.setInt(7, olStatus);
                insertOrderLineStatement.setDate(8, olStatus == 3 ? Helper.NOW_DATE : null);
                insertOrderLineStatement.addBatch();
            }
            insertOrderLineStatement.executeBatch();
            final double oDiscount = (1.00 - (oTotal / MSRPOrderTotal)) * 100;
            // now create order row
            insertOrderStatement.setInt(1, oId);
            insertOrderStatement.setInt(2, oCid);
            insertOrderStatement.setInt(3, oOlcnt);
            insertOrderStatement.setDouble(4, oDiscount);
            insertOrderStatement.setDouble(5, oTotal);
            insertOrderStatement.setInt(6, oStatus);
            insertOrderStatement.setTimestamp(7, Helper.NOW_TIMESTAMP);
            insertOrderStatement.setDate(8, oStatus == 3 ? Helper.NOW_DATE : null);
            insertOrderStatement.addBatch();
            batchCount++;
            batchCount = DatabaseHelper.executeBatchIfFull(dbConnection, batchCount, preparedStatements);
        }
        DatabaseHelper.executeBatch(dbConnection, batchCount, preparedStatements);
        return true;
    }
}

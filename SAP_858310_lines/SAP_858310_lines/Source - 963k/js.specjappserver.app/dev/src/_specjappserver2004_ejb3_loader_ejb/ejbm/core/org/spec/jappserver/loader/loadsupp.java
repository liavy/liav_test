/*
 * Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 * This source code is provided as is, without any express or implied warranty.
 *
 *  History:
 *  Date        ID, Company               Description
 *  ----------  ------------------------  ---------------------------------------------------------------
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
 *  2003/11/06  Samuel Kounev, Darmstadt  Removed property numAssemblies, since it is not used.
 *  2004/01/13  Rafay Khawaja, Borland    Re-arranged code, and added threads for concurrent
 *                                        loading where possible.
 *  2004/01/24  Rafay Khawaja, Borland    Fix for a multi-threaded mis-behavior due to PO.getPONumber() 
 *                                        instead of PO.getNextJ()
 *  2007/10/02  Bernhard Riedhofer, SAP   Created, integration of loader into SPECjAppServer2007 application
 *
 * Note (Shanti): This program reads off part numbers from the Supp database for
 *                simplicity, but in reality there must be a way to have different
 *                part numbers between the company & supplier database
 *                We load only the customer and parts tables initially.
 *
 */
package org.spec.jappserver.loader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.TreeSet;

import javax.jms.JMSException;
import javax.naming.NamingException;

import org.spec.jappserver.common.util.DateTimeNormalizer;

/*
 * Loads the tables of supplier domain.
 */
class LoadSupp extends Load {

    private static final String SITE_TABLE_NAME = "S_SITE";
    private static final String SUPPLIER_TABLE_NAME = "S_SUPPLIER";
    private static final String[] SUPPLIER_TABLE_NAMES = new String[] {"S_COMPONENT", "S_SUPP_COMPONENT", "S_PURCHASE_ORDER", "S_PURCHASE_ORDERLI"};

    LoadSupp(ActionMessage message) {
        super(message);
        message.setDomain(LoadMessage.SUPPLIER_DOMAIN);
        dbKey = DatabaseHelper.SUPP_KEY;
    }

    @Override
    void addActionMessages(TreeSet<ActionMessage> actionMessages) throws CloneNotSupportedException {
        addActionMessages(actionMessages, SITE_TABLE_NAME, LOAD_IN_ONE_SECTION);
        addActionMessages(actionMessages, SUPPLIER_TABLE_NAME, LOAD_IN_ONE_SECTION);
        addActionMessages(actionMessages, SUPPLIER_TABLE_NAMES, numAssemblies);
    }

    @Override
    void loadSequences() throws SQLException, NamingException, JMSException {
        loadSequence("purchaseorder", Component.MAX_COMPONENTS_PER_ASSEMBLY * numAssemblies + 1);
    }

    @Override
    boolean loadTableSection(final String[] tables, final int start, final int num) throws SQLException, NamingException, JMSException {
        String strTables = MessageHelper.getCommaSeperatedList(tables);
        if (strTables.equals(SITE_TABLE_NAME)) {
            return loadSite0();
        } else if (strTables.equals(SUPPLIER_TABLE_NAME)) {
            return loadSupplier0();
        } else if (strTables.equals(MessageHelper.getCommaSeperatedList(SUPPLIER_TABLE_NAMES))) {
            return loadComponent(start, num);
        } else {
            throw new IllegalArgumentException("Table " + strTables + " not found.");
        }
    }

    private boolean loadSite0() throws SQLException, NamingException, JMSException {
        final Connection dbConnection = getConnection();
        try {
            final PreparedStatement cs = dbConnection.prepareStatement("insert into S_SITE"
                    + " (SITE_ID, SITE_NAME, SITE_STREET1, SITE_STREET2, SITE_CITY, SITE_STATE, SITE_COUNTRY, SITE_ZIP)"
                    + " values(?,?,?,?,?,?,?,?)");
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
                    .prepareStatement("insert into S_SUPPLIER"
                            + " (SUPP_ID, SUPP_NAME, SUPP_STREET1, SUPP_STREET2, SUPP_CITY, SUPP_STATE, SUPP_COUNTRY, SUPP_ZIP, SUPP_PHONE, SUPP_CONTACT)"
                            + " values(?,?,?,?,?,?,?,?,?,?)");
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
            dbConnection.commit();
        } finally {
            dbConnection.close();
        }
        return true;
    }

    private boolean loadComponent(final int start, final int num) throws SQLException, NamingException, JMSException {
        final int end = start + num;
        final Connection dbConnection = getConnection();
        try
        {
            final PreparedStatement cs = dbConnection.prepareStatement("insert into S_COMPONENT"
                    + " (COMP_ID, COMP_NAME, COMP_DESC, COMP_UNIT, COMP_COST, QTY_ON_ORDER, QTY_DEMANDED, LEAD_TIME, CONTAINER_SIZE)"
                    + " values (?,?,?,?,?,0,0,?,?)");
            try {
                final PreparedStatement insertSupplierComponents = dbConnection
                        .prepareStatement("insert into S_SUPP_COMPONENT"
                                + " (SC_P_ID, SC_SUPP_ID, SC_PRICE, SC_QTY, SC_DISCOUNT, SC_DEL_DATE)"
                                + " values (?,?,?,?,?,?)");
                try {
                    final PreparedStatement ps = dbConnection.prepareStatement("insert into S_PURCHASE_ORDER"
                            + " (PO_NUMBER, PO_SUPP_ID, PO_SITE_ID)"
                            + " values(?,?,?)");
                    try {
                        final PreparedStatement pos = dbConnection.prepareStatement("insert into S_PURCHASE_ORDERLI"
                                + " (POL_NUMBER, POL_PO_ID, POL_P_ID, POL_QTY, POL_BALANCE, POL_DELDATE, POL_MESSAGE)"
                                + " values(?,?,?,?,?,?,?)");
                        try {
                            final PreparedStatement[] preparedStatements = new PreparedStatement[] {
                                    cs, insertSupplierComponents, ps, pos};
                            int assemblyBlock = start/num;
                            int startComponentNumber = (start - 1)*Component.MAX_COMPONENTS_PER_ASSEMBLY + 1;
                            final Component.RandomGenerator componentGen = new Component.RandomGenerator(seedGen
                                    .getComponentSeed(assemblyBlock), startComponentNumber);
                            final CardDeck supplierDeck = new CardDeck(1, Helper.NUM_SUPPLIERS, r.nextLong());
                            PurchaseOrder purchaseOrder = new PurchaseOrder(ps, pos, startComponentNumber);
                            int batchCount = 0;
                            final MessageHelper.InterruptChecker ic = new MessageHelper.InterruptChecker();
                            for (int j = start; j < end; j++) {
                                if (ic.isInterrupted()) {
                                    return false;
                                }
                                final List<Part> parts = componentGen.createAssemblyComponents(DatabaseHelper.CUR_BATCH);
                                for (final Part part : parts) {
                                    final String pId = part.getId();
                                    final int leadTime = part.getLeadTime();
                                    cs.setString(1, pId);
                                    cs.setString(2, part.getName());
                                    cs.setString(3, part.getDesc());
                                    cs.setString(4, part.getUnit());
                                    cs.setDouble(5, part.getCost());
                                    cs.setInt(6, leadTime);
                                    cs.setInt(7, part.getContainerSize());
                                    cs.addBatch();
                                    loadSupplierComponent(pId, supplierDeck, part.getCost(), leadTime, dbConnection,
                                            insertSupplierComponents);
                                    purchaseOrder.load(pId);
                                    batchCount++;
                                    batchCount = DatabaseHelper.executeBatchIfFull(dbConnection, batchCount, preparedStatements);
                                }
                            }
                            // load remaining rows
                            DatabaseHelper.executeBatch(dbConnection, batchCount, preparedStatements);
                        } finally {
                            pos.close();;
                        }
                    } finally {
                        ps.close();;
                    }
                } finally {
                    insertSupplierComponents.close();
                }
            } finally {
                cs.close();
            }
        } finally {
            dbConnection.close();
        }
        return true;
    }

    private void loadSupplierComponent(final String pId, final CardDeck supplierDeck, final double pCost, final int lead_time,
            final Connection dbConnection, final PreparedStatement insertSupplierComponents)
            throws SQLException {
        for (int i = 0; i < Helper.NUM_SUPPLIERS; i++) {
            final double scPrice = pCost;
            final int scQty = r.random(10, 20);
            final double scDiscount = r.drandom(0.0, 0.50);
            int scDelivery = r.random(1, lead_time);
            insertSupplierComponents.setString(1, pId);
            insertSupplierComponents.setInt(2, supplierDeck.nextCard());
            insertSupplierComponents.setDouble(3, scPrice);
            insertSupplierComponents.setInt(4, scQty);
            insertSupplierComponents.setDouble(5, scDiscount);
            insertSupplierComponents.setInt(6, scDelivery);
            insertSupplierComponents.addBatch();
        }
    }

    private static class PurchaseOrder {

        private final RandNum r = new RandNum();

        private PreparedStatement ps;
        private PreparedStatement pos;
        
        private boolean newPo = true;
        private int poNumber;
        private int polNumber = 1;
        private int polCount;

        PurchaseOrder(PreparedStatement ps, PreparedStatement pos, int startPoNumber) throws SQLException {
            this.ps = ps;
            this.pos = pos;
            poNumber = startPoNumber - 1;
        }

        private void load(String pId) throws SQLException {
            // Create a purchase order for 10% of the parts
            if (r.random(1, 100) > 10) {
                return;
            }
            if (newPo) {
                poNumber++;
                final int poSiteId = r.random(1, Helper.NUM_SITES);
                final int poSuppId = r.random(1, Helper.NUM_SUPPLIERS);
                ps.setInt(1, poNumber);
                ps.setInt(2, poSuppId);
                ps.setInt(3, poSiteId);
                ps.addBatch();
                polCount = r.random(1, Component.MAX_LINES_PER_PURCHASED_ORDER);
                newPo = false;
                polNumber = 0;
            }
            final int polQty = r.random(1, 10000);
            final String polMessage = r.makeAString(25, 100);
            final long millis = DateTimeNormalizer.normalizeSqlDateMillis(System.currentTimeMillis() + 90 * 24 * 60 * 60000);
            final java.sql.Date polDelDate = new java.sql.Date(millis);
            polNumber++;
            pos.setInt(1, polNumber);
            pos.setInt(2, poNumber);
            pos.setString(3, pId);
            pos.setInt(4, polQty);
            pos.setDouble(5, 0.0);
            pos.setDate(6, polDelDate);
            pos.setString(7, polMessage);
            pos.addBatch();
            newPo = polNumber == polCount;
        }
    }
}

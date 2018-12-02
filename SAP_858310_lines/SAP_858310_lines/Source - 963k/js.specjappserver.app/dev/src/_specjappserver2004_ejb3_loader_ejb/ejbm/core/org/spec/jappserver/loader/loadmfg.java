/*
 * Copyright (c) 2001-2007 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *
 *  History:
 *  Date        ID, Company               Description
 *  ----------  ------------------------  -----------------------------------------------------------------
 *  2001/../..  Shanti Subramanyam, SUN   Created
 *  2001/../..  Matt Hogstrom, IBM        Modified startDate to use java.sql.Timestamp instead
 *                                        of java.sql.Date for DB vendors that don't map these types.
 *  2002/04/12  Matt Hogstrom, IBM        Conversion from ECperf 1.1 to SPECjAppServer2001
 *  2003/04/03  Russel Raymundo, BEA      Explicitly specified table columns names for
 *                                        inserts. Enabled Distributed Database loading.
 *  2002/07/10  Russel Raymundo, BEA      Conversion from SPECjAppServer2001 to
 *                                        SPECjAppServer2002 (EJB2.0).
 *  2003/11/06  Samuel Kounev, Darmstadt  Modified database scaling rules (see osgjava-5681).
 *  2003/11/25  Samuel Kounev, Darmstadt  Modified database scaling rules as per osgjava-5891.
 *  2004/01/07  Akara Sucharitakul, Sun   Implemented new pDemand model (see osgjava-6255):
 *                                        Changed the initial inventory loading to random(loMark, hiMark).
 *                                        Cleaned up unused variables and import statements.
 *  2004/01/13  Rafay Khawaja, Borland    Re-arranged code, and added some threads for cocurrent 
 *                                        loading where possible.
 *  2004/02/20  Rafay Khawaja, Borland    Addressed issue with assigning correct categories to 
 *                                        ranges of ItemIds when loading using multiple threads.
 *  2007/10/02  Bernhard Riedhofer, SAP   Integration of loader into SPECjAppServer2007 application
 *
 *  Note (Shanti): The Corp database must be loaded first.
 *                 The Mfg database is loaded by extracting data out of the Corp
 *                 db. This program reads from a named pipe that is written to by
 *                 the LoadCorp program.
 */
package org.spec.jappserver.loader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.TreeSet;

import javax.jms.JMSException;
import javax.naming.NamingException;

/*
 * Loads the tables of mfg domain.
 */
class LoadMfg extends Load {

    private static final String[] MFG_TABLE_NAMES = new String[] {"M_PARTS", "M_INVENTORY", "M_BOM", "M_WORKORDER", "M_LARGEORDER"};

    LoadMfg(ActionMessage message) {
        super(message);
        message.setDomain(LoadMessage.MANUFACTURER_DOMAIN);
        dbKey = DatabaseHelper.MFG_KEY;
    }

    @Override
    void addActionMessages(TreeSet<ActionMessage> actionMessages) throws CloneNotSupportedException {
        addActionMessages(actionMessages, MFG_TABLE_NAMES, numAssemblies);
    }

    @Override
    void loadSequences() throws SQLException, NamingException, JMSException {
        loadSequence("workorder", numAssemblies*Component.MAX_COMPONENTS_PER_ASSEMBLY + 1);
        loadSequence("largeorder", 1);
    }

    @Override
    boolean loadTableSection(final String[] tables, final int start, final int num) throws SQLException, NamingException, JMSException {
        String strTables = MessageHelper.getCommaSeperatedList(tables);
        if (strTables.equals(MessageHelper.getCommaSeperatedList(MFG_TABLE_NAMES))) {
            return loadAllMfg(start, num);
        } else {
            throw new IllegalArgumentException("Tables " + strTables + " not found.");
        }
    }

    private boolean loadAllMfg(final int start, final int num) throws SQLException, NamingException, JMSException {
        final int end = start + num;
        final Connection dbConnection = getConnection();
        try
        {
            final PreparedStatement bomStatement = dbConnection.prepareStatement("insert into M_BOM"
                    + " (B_COMP_ID, B_ASSEMBLY_ID, B_LINE_NO, B_QTY, B_OPS, B_OPS_DESC)"
                    + " values (?,?,?,?,?,?)");
            try {
                final PreparedStatement wos = dbConnection
                        .prepareStatement("insert into M_WORKORDER"
                                + " (WO_NUMBER, WO_O_ID, WO_OL_ID, WO_STATUS, WO_ASSEMBLY_ID, WO_ORIG_QTY, WO_COMP_QTY, WO_DUE_DATE, WO_START_DATE)"
                                + " values (?,?,?,?,?,?,?,?,?)");
                try {
                    final PreparedStatement inventoryStatement = dbConnection.prepareStatement("insert into M_INVENTORY"
                            + " (IN_P_ID, IN_QTY, IN_ORDERED, IN_LOCATION, IN_ACC_CODE, IN_ACT_DATE)"
                            + " values (?,?,?,?,?,?)");
                    try {
                        final PreparedStatement partStatement = dbConnection.prepareStatement("insert into M_PARTS"
                                + " (P_ID, P_NAME, P_DESC, P_REV, P_PLANNER, P_TYPE, P_IND, P_LOMARK, P_HIMARK)"
                                + " values (?,?,?,?,?,?,?,?,?)");
                        try {
                            final PreparedStatement[] preparedStatements = new PreparedStatement[] { partStatement, inventoryStatement, wos,
                                    bomStatement };
                            int assemblyBlock = start/num;
                            int startComponentNumber = (start - 1)*Component.MAX_COMPONENTS_PER_ASSEMBLY + 1;
                            final Assembly.RandomGenerator assemblyGen = new Assembly.RandomGenerator(seedGen
                                    .getAssemblySeed(assemblyBlock), start);
                            final Component.RandomGenerator componentGen = new Component.RandomGenerator(seedGen
                                    .getComponentSeed(assemblyBlock), startComponentNumber);
                            int batchCount = 0;
                            int bomLineNo = 1;
                            int woNumber = startComponentNumber;
                            final MessageHelper.InterruptChecker ic = new MessageHelper.InterruptChecker();
                            for (int j = start; j < end; j++) {
                                if (ic.isInterrupted()) {
                                    return false;
                                }
                                Assembly assembly = assemblyGen.createAssembly(DatabaseHelper.CUR_BATCH);
                                final List<Part> parts = componentGen.createAssemblyComponents(DatabaseHelper.CUR_BATCH);
                                parts.add(assembly);
                                for (final Part part : parts) {
                                    final String pId = part.getId();
                                    final int pType = part.getType();
                                    final int pLoMark = part.getLowMark();
                                    final int pHiMark = pLoMark + part.getContainerSize();
                                    // Load parts table
                                    partStatement.setString(1, pId);
                                    partStatement.setString(2, part.getName());
                                    partStatement.setString(3, part.getDesc());
                                    partStatement.setString(4, part.getRev());
                                    partStatement.setInt(5, part.getPlanner());
                                    partStatement.setInt(6, pType);
                                    partStatement.setInt(7, part.getInd());
                                    partStatement.setInt(8, pLoMark);
                                    partStatement.setInt(9, pHiMark);
                                    partStatement.addBatch();
                                    if (pType == 0) {
                                        // This is a component - insert it into BOM table
                                        bomStatement.setString(1, pId);
                                        bomStatement.setString(2, assembly.getId());
                                        bomStatement.setInt(3, bomLineNo++);
                                        bomStatement.setInt(4, r.random(1, 20));
                                        bomStatement.setInt(5, r.random(1, 20));
                                        bomStatement.setString(6, r.makeAString(50, 100));
                                        bomStatement.addBatch();
                                    } else {
                                        // This is an assembly - insert it into workorder table
                                        wos.setInt(1, woNumber++);
                                        wos.setInt(2, 0);
                                        wos.setInt(3, 0);
                                        wos.setInt(4, 1); // OPEN
                                        wos.setString(5, pId);
                                        final int woOrigQty = r.random(1, 1000);
                                        wos.setInt(6, woOrigQty);
                                        wos.setInt(7, r.random(1, woOrigQty));
                                        wos.setDate(8, null);
                                        wos.setTimestamp(9, new java.sql.Timestamp(System.currentTimeMillis()));
                                        wos.addBatch();
                                        // store the assembly for the components which follow
                                        bomLineNo = 1; // reset line number
                                    }
                                    // insert into inventory table
                                    inventoryStatement.setString(1, pId);
                                    inventoryStatement.setInt(2, r.random(pLoMark, pHiMark));
                                    inventoryStatement.setInt(3, 0);
                                    inventoryStatement.setString(4, r.makeAString(20, 20));
                                    inventoryStatement.setInt(5, 0);
                                    inventoryStatement.setDate(6, Helper.NOW_DATE);
                                    inventoryStatement.addBatch();
                                    batchCount++;
                                    batchCount = DatabaseHelper.executeBatchIfFull(dbConnection, batchCount, preparedStatements);
                                }
                            }
                            // load remaining rows
                            DatabaseHelper.executeBatch(dbConnection, batchCount, preparedStatements);
                        } finally {
                            partStatement.close();
                        }
                    } finally {
                        inventoryStatement.close();
                    }
                } finally {
                    wos.close();
                }
            } finally {
                bomStatement.close();
            }
        } finally {
            dbConnection.close();
        }
        return true;
    }
}

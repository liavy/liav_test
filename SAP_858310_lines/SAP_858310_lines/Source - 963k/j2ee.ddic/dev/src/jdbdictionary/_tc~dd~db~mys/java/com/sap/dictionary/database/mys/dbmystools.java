package com.sap.dictionary.database.mys;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import com.sap.dictionary.database.dbs.DbFactory;
import com.sap.dictionary.database.dbs.DbTools;
import com.sap.dictionary.database.dbs.ExType;
import com.sap.dictionary.database.dbs.JddException;
import com.sap.dictionary.database.dbs.Logger;
import com.sap.sql.NativeSQLAccess;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Category;


/**
 * Ueberschrift:  misceleanous functions 
 * Beschreibung:
 * Copyright:     Copyright (c) 2001
 * Organisation:
 * @author        Hakan Kuecuekyilmaz, <hakan at mysql dot com>, 2005-07-10
 * @version 1.0
 */

public class DbMysTools extends DbTools {

    private static Location loc = Logger.getLocation("mys.DbMysTable");
    private static final Category cat = Category.getCategory(Category.SYS_DATABASE, Logger.CATEGORY_NAME);

    public DbMysTools(DbFactory factory) {
        super(factory);
    }

    /**
     * Renames a table on the database. If no exception is send, the table could
     * be renamed.
     * 
     * @param sourceName -
     *            current name of table
     * @param destinationName -
     *            new name of table
     * @exception JddException -
     *                The following error-situations should be distinguished by
     *                the exception's ExType: ExType.NOT_ON_DB: Source-table
     *                does not exist on database ExType.EXISTS_ON_DB:
     *                Destination table already exists. Every other error should
     *                be send with ExType.SQL_ERROR or ExType.OTHER.
     */
    public void renameTable(String sourceName, String destinationName)
            throws JddException {

        loc.entering("renameTable");
        Connection con = getFactory().getConnection();

        try {
            Statement dstmt = NativeSQLAccess.createNativeStatement(con);
            dstmt.execute("RENAME TABLE `" + sourceName + "` TO `" + destinationName + "`");
            dstmt.close();
        } catch (SQLException sqlex) {
            int errcode = sqlex.getErrorCode();
            ExType xt;

            Object[] arguments = { sourceName, destinationName, sqlex.getMessage() };
            loc.errorT("renameTable({0},{1}) failed: {2}", arguments);
            loc.exiting();

            // map errorcode to NOT_ON_DB, EXISTS_ON_DB, SQL_ERROR
            switch (errcode) {
            case 1017:
                xt = ExType.NOT_ON_DB;
                break;
            case 1050:
            	xt = ExType.EXISTS_ON_DB;
            	break;
            default:
                xt = ExType.SQL_ERROR;
                break;
            }

            throw new JddException(xt, sqlex.getMessage());
        } catch (Exception ex) {
            Object[] arguments = { sourceName, destinationName, ex.getMessage() };
            loc.errorT("renameTable({0},{1}) failed: {2}", arguments);
            loc.exiting();

            throw new JddException(ExType.OTHER, ex.getMessage());
        }

        Object[] arguments = { sourceName, destinationName };
        loc.infoT("renameTable: renamed {0} to {1}", arguments);
        loc.exiting();

        return;
    }
}

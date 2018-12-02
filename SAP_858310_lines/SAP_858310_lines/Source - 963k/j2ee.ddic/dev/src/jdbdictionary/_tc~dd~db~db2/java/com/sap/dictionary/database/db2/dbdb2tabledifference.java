package com.sap.dictionary.database.db2;

import com.sap.dictionary.database.dbs.*;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Category;

/**
 * Title: Analysis of table and view changes: DB2/390 specific classes
 * Description: DB2/390 specific analysis of table and view changes. Tool to
 * deliver Db2/390 specific database information. Copyright: Copyright (c) 2001
 * Company: SAP AG
 * 
 * @author Burkhard Diekmann
 * @version 1.0
 */

public class DbDb2TableDifference extends DbTableDifference {

	// db specific parameters
	private boolean alterPriqty = false;

	private boolean alterSecqty = false;

	private boolean alterPctfree = false;

	private boolean alterBufferpool = false;

	private boolean alterFreepage = false;

	private boolean alterLockrule = false;

	private boolean alterLockmax = false;

	private boolean alterPagesize = false;

	private boolean alterSegsize = false;

	private boolean alterCloserule = false;

	private boolean alterDefine = false;

	private boolean alterCompress = false;

	private boolean alterMaxrows = false;

	private boolean alterMemclust = false;

	private boolean alterGbpcache = false;

	private boolean addLobView = false; // view for lob table if missing

	private static Location loc = Logger
			.getLocation("db2.DbDb2TableDifference");

	private static Category cat = Category.getCategory(Category.SYS_DATABASE,
			Logger.CATEGORY_NAME);

	Action tblspAction = Action.NOTHING;

	public DbDb2TableDifference(DbTable refTable, DbTable cmpTable) {
		super(refTable, cmpTable);

		// Returns Action what has to be done for this table:
		// Possible values: See class com.sap.jdd.dbs.Action

		DbDb2Table db2refTable = (DbDb2Table) (refTable);
		DbDb2Table db2cmpTable = (DbDb2Table) (cmpTable);

	}

	public DbObjectSqlStatements getDdlStatements(String tableName,
			DbTable tableForStorageInfo) throws Exception {

		DbObjectSqlStatements stmts = new DbObjectSqlStatements(tableName);

		if (((getAction() == Action.NOTHING) || (getAction() == Action.ALTER))
				&& (tblspAction == Action.ALTER)) {
			stmts.merge(((DbDb2Table) getTarget())
					.getDdlStatementsforAlterTsp());
		}

		stmts.merge(super.getDdlStatements(tableName, tableForStorageInfo));

		return stmts;
	}

	public void diffLobView(DbDb2Table refTable) {
		loc.entering("diffLobView");
		if (refTable.isLobViewMissing() == true) {
			Object[] arguments = { refTable.getName() };
			cat
					.infoT(
							loc,
							"diffLobView {0}: alter necessary: view to hide rowid field missing.",
							arguments);
			mergeAction(Action.ALTER);
			addLobView = true;
		}
		loc.exiting();
	}

	public void diffPageSize(DbDb2Table refTable, DbDb2Table cmpTable) {
		loc.entering("diffPageSize");
		// if new pagesize exceeds old pagesize
		// we need to convert
		if (cmpTable.getTsAttr().getPageSize() > refTable.getTsAttr()
				.getPageSize()) {
			Object[] arguments = { refTable.getName(),
					new Integer(refTable.getTsAttr().getPageSize()),
					new Integer(cmpTable.getTsAttr().getPageSize()) };
			cat
					.infoT(
							loc,
							"diffPageSize {0}: conversion necessary: original page size {1} smaller than target page size {2}",
							arguments);
			mergeAction(Action.CONVERT);
			tblspAction = Action.CONVERT;
		}
		loc.exiting();
	}

	public void diffDbSpecificContent(DbDb2Table refTable, DbDb2Table cmpTable) { // $JL-EXC$_
		if (!refTable.equalsSpecificContent(cmpTable)) {
			mergeAction(Action.CONVERT);
			tblspAction = Action.CONVERT;
		}
	}

	public void diffSpecificContent(DbDb2Table refTable, DbDb2Table cmpTable) { // $JL-EXC$_

		loc.entering("diffSpecificContent");

		boolean fexp = false;
		if ((getAction() != null) && (getAction() != Action.NOTHING)
				&& (getAction() != Action.ALTER))
			return;

		/*
		 * do not alter bufferpool, gbpcache, maxrows because the tablespace
		 * must be stopped first:
		 * 
		 * if( 0 != refTable.getGbpcache().compareTo(cmpTable.getGbpcache()) )
		 * alterGbpcache = true; if( 0 !=
		 * refTable.getBufferpool().compareTo(cmpTable.getBufferpool()) )
		 * alterBufferpool = true; if( refTable.getMaxrows() !=
		 * cmpTable.getMaxrows() ) alterMaxrows = true;
		 */

		DbDb2TsAttr refTsAttr = refTable.getTsAttr();
		DbDb2PartAttr refPartAttr = refTsAttr.getFirstPart();
		DbDb2TsAttr cmpTsAttr = cmpTable.getTsAttr();
		DbDb2PartAttr cmpPartAttr = cmpTsAttr.getFirstPart();

		if (refPartAttr.getPriQty() != cmpPartAttr.getPriQty()) {
			Object[] arguments = { refTable.getName(),
					new Integer(refPartAttr.getPriQty()),
					new Integer(cmpPartAttr.getPriQty()) };
			cat
					.infoT(
							loc,
							" (diffSpecificContent {0}): alter tablespace: original primary quantity ({1}) differs from target primary quantity ({2}).",
							arguments);
			alterPriqty = true;
		}
		if (refPartAttr.getSecQty() != cmpPartAttr.getSecQty()) {
			Object[] arguments = { refTable.getName(),
					new Integer(refPartAttr.getSecQty()),
					new Integer(cmpPartAttr.getSecQty()) };
			cat
					.infoT(
							loc,
							" (diffSpecificContent {0}): alter tablespace: original secondary quantity ({1}) differs from target secondary quantity ({2}).",
							arguments);
			alterSecqty = true;
		}

		if (refPartAttr.getFreePage() != cmpPartAttr.getFreePage()) {
			Object[] arguments = { refTable.getName(),
					new Integer(refPartAttr.getFreePage()),
					new Integer(cmpPartAttr.getFreePage()) };
			cat
					.infoT(
							loc,
							" (diffSpecificContent {0}): alter tablespace: original freepage ({1}) differs from target freepage ({2}).",
							arguments);
			alterFreepage = true;
		}
		if (refPartAttr.getPctFree() != cmpPartAttr.getPctFree()) {
			Object[] arguments = { refTable.getName(),
					new Integer(refPartAttr.getPctFree()),
					new Integer(cmpPartAttr.getPctFree()) };
			cat
					.infoT(
							loc,
							" (diffSpecificContent {0}): alter tablespace: original pctfree ({1}) differs from target pctfree ({2}).",
							arguments);
			alterPctfree = true;
		}
		if (0 != refPartAttr.getCompress().compareTo(cmpPartAttr.getCompress())) {
			Object[] arguments = { refTable.getName(),
					refPartAttr.getCompress(), cmpPartAttr.getCompress() };
			cat
					.infoT(
							loc,
							" (diffSpecificContent {0}): alter tablespace: original compress ({1}) differs from target compress ({2}).",
							arguments);
			alterCompress = true;
		}
		if (0 != refTsAttr.getLockRule().compareTo(cmpTsAttr.getLockRule())) {
			Object[] arguments = { refTable.getName(), refTsAttr.getLockRule(),
					cmpTsAttr.getLockRule() };
			cat
					.infoT(
							loc,
							" (diffSpecificContent {0}): alter tablespace: original lockrule ({1}) differs from target lockrule ({2}).",
							arguments);
			alterLockrule = true;
		}
		if (0 != refTsAttr.getClose().compareTo(cmpTsAttr.getClose())) {
			Object[] arguments = { refTable.getName(), refTsAttr.getClose(),
					cmpTsAttr.getClose() };
			cat
					.infoT(
							loc,
							" (diffSpecificContent {0}): alter tablespace: original closerule ({1}) differs from target closerule ({2}).",
							arguments);
			alterCloserule = true;
		}
		if (refTsAttr.getLockMax() != cmpTsAttr.getLockMax()) {
			Object[] arguments = { refTable.getName(),
					new Integer(refTsAttr.getLockMax()),
					new Integer(cmpTsAttr.getLockMax()) };
			cat
					.infoT(
							loc,
							" (diffSpecificContent {0}): alter tablespace: original lockmax ({1}) differs from target lockmax ({2}).",
							arguments);
			alterLockmax = true;
		}

		if (alterPriqty || alterSecqty || alterPctfree || alterBufferpool
				|| alterFreepage || alterLockrule || alterLockmax
				|| alterCloserule || alterCompress || alterGbpcache) {
			mergeAction(Action.ALTER);
			tblspAction = Action.ALTER;
		}

		if (0 != refPartAttr.getDefine().compareTo(cmpPartAttr.getDefine())) {
			alterDefine = true;

			boolean dropCreate = true;
			try {

				dropCreate = (refTable.existsData() == false);
			} catch (Exception ex) {
				fexp = true; // avoid lint errors
			}

			if (dropCreate) {

				mergeAction(Action.DROP_CREATE);
				tblspAction = Action.DROP_CREATE;
			}
		}

		if (!refTable.equalsSpecificContent(cmpTable)) {
			mergeAction(Action.DROP_CREATE);
			tblspAction = Action.DROP_CREATE;
		}
		loc.exiting();
	}

	public Action getTblspAction() {
		return tblspAction;
	}

	public boolean getLobViewMissing() {
		return addLobView;
	}

}
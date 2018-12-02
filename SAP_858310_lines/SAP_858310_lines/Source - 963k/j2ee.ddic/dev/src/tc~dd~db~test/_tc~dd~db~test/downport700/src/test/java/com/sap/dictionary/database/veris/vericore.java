package com.sap.dictionary.database.veris;

import java.util.*;
import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.*;
import java.sql.*;

import junit.framework.Assert;

import org.xml.sax.InputSource;
import com.sap.dictionary.database.dbs.*;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;
import com.sap.tc.logging.StreamLog;

/**
 * @author d019347
 * 
 *         To change this generated comment edit the template variable
 *         "typecomment": Window>Preferences>Java>Templates. To enable and
 *         disable the creation of type comments go to
 *         Window>Preferences>Java>Code Generation.
 */
public class VeriCore {
	private static final Location loc0 = Location
	    .getLocation("com.sap.dictionary.database");
	private static final Location loc = Location
	    .getLocation("com.sap.dictionary.database.veris");
	private static final String TABLE = "table";
	private static final String COLUMNS = "columns";
	private static final String COLUMN = "column";
	private static final String INDEXES = "indexes";
	private static final String INDEX = "index";
	private static final String PRIMARY_KEY = "primaryKey";
	private final String TPREFIX = "TMP_VERI";
	private ArrayList mapsfrom = new ArrayList();
	private ArrayList mapsto = new ArrayList();
	private HashSet colnames0 = new HashSet();
	private ArrayList colmaps0 = new ArrayList();
	private PrintStream out = null;
	private PrintStream preout = null;
	private String filename = null;
	private ByteArrayOutputStream infoText = null;
	private StreamLog tempLog = null;
	public static final TimeZone TIME_ZONE_GMT = TimeZone.getTimeZone("GMT");
	public static final DateFormat TIME_FORMAT = DateFormat.getTimeInstance(
	    DateFormat.MEDIUM, Locale.GERMAN);
	public static final DateFormat DATE_FORMAT = DateFormat.getDateInstance(
	    DateFormat.MEDIUM, Locale.GERMAN);
	public static final DateFormat TIMESTAMP_FORMAT = new SimpleDateFormat(
	    "dd.MM.yyyy H:mm:ss.SSS", Locale.GERMAN);
	public static final DecimalFormat DECIMAL_FORMAT = (DecimalFormat) NumberFormat
	    .getInstance(Locale.US);
	static {
		// DATE_FORMAT.setTimeZone(TIME_ZONE_GMT);
		DATE_FORMAT.setLenient(true);
		TIME_FORMAT.setTimeZone(TIME_ZONE_GMT);
		TIME_FORMAT.setLenient(true);
		TIMESTAMP_FORMAT.setTimeZone(TIME_ZONE_GMT);
		TIMESTAMP_FORMAT.setLenient(true);
		DECIMAL_FORMAT.applyPattern("#,##0.###############################");
	}

	public VeriCore(String filename, PrintStream out) {
		this.out = out;
		this.filename = filename;
	}

	public void init(XmlMap vmaps) {
		mapsfrom = new ArrayList();
		mapsto = new ArrayList();
		colnames0 = new HashSet();
		colmaps0 = new ArrayList();

		XmlMap map0 = vmaps.getXmlMap("Dbtable");

		// build colmaps0 and colnames0
		XmlMap colsmap0 = map0.getXmlMap("columns");
		XmlMap cmap = null;
		for (int i = 0; !(cmap = colsmap0.getXmlMap("column", i)).isEmpty(); i++) {
			colnames0.add(cmap.getString("name"));
			colmaps0.add(cmap);
		}

		// set correct names
		XmlMap vmap = null;
		XmlMap indexes = null;
		XmlMap ind = null;
		String tname = null;
		int i1 = 0;
		for (int i = 0; !(vmap = vmaps.getXmlMap("Dbtable", i)).isEmpty(); i++) {

			// out.println((XmlMap)mapsfrom.get(0));
			// out.println(vmap);
			tname = TPREFIX + new DecimalFormat("000").format(i1);
			i1++;
			// out.println(tname);
			XmlMap map = (XmlMap) map0.clone();
			map.put("name", tname);
			XmlMap prkey = (XmlMap) map.getXmlMap("primary-key").clone();
			if (prkey != null && !prkey.isEmpty()) {
				map.put("primary-key", prkey);
				prkey.put("tabname", tname);
			}
			indexes = (XmlMap) map.getXmlMap("indexes").clone();
			map.put("indexes", indexes);
			ind = null;
			for (int j = 0; !(ind = indexes.getXmlMap("index", j)).isEmpty(); j++) {
				XmlMap in = (XmlMap) ind.clone();
				indexes.put("index" + (j == 0 ? "" : "" + j), in);
				String iname = tname + in.getString("name").substring(4);
				in.put("name", iname);
				in.put("tabname", tname);
			}
			XmlMap tabmap = new XmlMap();
			tabmap.put("Dbtable", map);
			mapsfrom.add(tabmap);
			if (i == 0) {
				mapsto.add(tabmap);
				continue;
			}
			if (i != 0) {
				vmap.put("name", tname);
				indexes = (XmlMap) vmap.getXmlMap("indexes");
				for (int j = 0; !(ind = indexes.getXmlMap("index", j)).isEmpty(); j++) {
					String iname = tname + ind.getString("name").substring(4);
					ind.put("name", iname);
					ind.put("tabname", tname);
				}
			}
		}
		// out.println((XmlMap)mapsfrom.get(0));
		// out.println("-------------------------------------");
		// out.println((XmlMap)mapsfrom.get(1));
		// out.println("-------------------------------------");
		// out.println(colnames0);
		// out.println("-------------------------------------");
		// out.println(colmaps0);
		// out.println("-------------------------------------");

		XmlMap originDelta = null;
		for (int i = 1; !(vmap = vmaps.getXmlMap("Dbtable", i)).isEmpty(); i++) {
			XmlMap basic = (XmlMap) mapsfrom.get(i);
			if (vmap.getBoolean("origin")) {
				if (originDelta != null)
					mapsfrom.set(i, makeResult(basic, originDelta));
				originDelta = vmap;
			} else if (originDelta != null) {
				mapsfrom.set(i, makeResult(basic, originDelta));
				// originDelta.remove("origin");
			}
			mapsto.add(makeResult(basic, vmap));
		}

	}

	public XmlMap makeResult(XmlMap origin, XmlMap delta) {
		XmlMap cmap = null;
		HashSet indnames0 = new HashSet();
		ArrayList indmaps0 = new ArrayList();
		HashSet delcols = new HashSet();
		XmlMap addcols = new XmlMap();
		HashMap modcols = new HashMap();
		HashSet delinds = new HashSet();
		XmlMap addinds = new XmlMap();
		HashMap modinds = new HashMap();
		XmlMap cvmap = (XmlMap) delta.clone();
		cvmap.remove("name");
		cvmap.remove("columns");
		// cvmap.remove("primary-key");
		cvmap.remove("indexes");
		Set topattr = cvmap.entrySet();
		// out.println("-------------------------------------");
		// out.println(topattr);

		// prepare delta-columns
		XmlMap cols = delta.getXmlMap("columns");
		XmlMap col = null;
		String addPoint = null;
		ArrayList addchunk = null;
		for (int j = 0; !(col = cols.getXmlMap("column", j)).isEmpty(); j++) {
			// out.println("COLUMN***" + col);
			// out.println("-------------------------------------");
			String name = col.getString("name");
			if (col.getString("delete") != null) {
				delcols.add(name);
				addPoint = name;
				continue;
			}
			if (!colnames0.contains(name)) {
				if (addcols.containsKey(addPoint)) {
					addchunk.add(col);
				} else {
					addchunk = new ArrayList();
					addchunk.add(col);
					addcols.put(addPoint, addchunk);
				}
				continue;
			}
			// modify case
			modcols.put(name, col.entrySet());
			addPoint = name;
		}
		// out.println("DELCOLUMNS***" + delcols);
		// out.println("-------------------------------------");
		// out.println("MODCOLUMNS***" + modcols);
		// out.println("-------------------------------------");
		// out.println("ADDCOLUMNS***" + addcols);
		// out.println("-------------------------------------");

		// prepare delta-indexes
		XmlMap inds0 = (origin).getXmlMap("Dbtable").getXmlMap("indexes");
		XmlMap ind = null;
		for (int j = 0; !(ind = inds0.getXmlMap("index", j)).isEmpty(); j++) {
			indmaps0.add(ind);
			indnames0.add(ind.getString("name"));
		}
		XmlMap inds = delta.getXmlMap("indexes");
		ind = null;
		for (int j = 0; !(ind = inds.getXmlMap("index", j)).isEmpty(); j++) {
			// out.println("INDEX***" + ind);
			// out.println("-------------------------------------");
			String name = ind.getString("name");
			if (ind.getString("delete") != null) {
				delinds.add(name);
				addPoint = name;
				continue;
			}
			if (!indnames0.contains(name)) {
				if (addinds.containsKey(addPoint)) {
					addchunk.add(ind);
				} else {
					addchunk = new ArrayList();
					addchunk.add(ind);
					addinds.put(addPoint, addchunk);
				}
				continue;
			}
			// modify case
			modinds.put(name, ind.entrySet());
			addPoint = name;
		}
		// out.println("DELINDEXES***" + delinds);
		// out.println("-------------------------------------");
		// out.println("MODINDEXES***" + modinds);
		// out.println("-------------------------------------");
		// out.println("ADDINDEXES***" + addinds);
		// out.println("-------------------------------------");

		// build result-columns
		ArrayList rescols = new ArrayList();
		XmlMap colmaps = new XmlMap();
		if (addcols.containsKey(null)) {
			rescols.addAll((ArrayList) (addcols.get(null)));
		}
		for (int j = 0; j < colmaps0.size(); j++) {
			cmap = (XmlMap) colmaps0.get(j);
			String name = cmap.getString("name");
			XmlMap cm = (XmlMap) cmap.clone();
			if (modcols.containsKey(name)) {
				Set dltcol = (Set) modcols.get(name);
				Iterator iter = dltcol.iterator();
				while (iter.hasNext()) {
					Map.Entry me = (Map.Entry) iter.next();
					cm.put(me.getKey(), me.getValue());
				}
				rescols.add(cm);
			} else if (!delcols.contains(name))
				rescols.add(cm);
			if (addcols.containsKey(name)) {
				rescols.addAll((ArrayList) addcols.get(name));
			}
		}
		for (int j = 0; j < rescols.size(); j++) {
			String pos = String.valueOf(j + 1);
			cmap = (XmlMap) rescols.get(j);
			cmap.put("position", "" + pos);
			colmaps.put("column" + (j == 0 ? "" : "" + j), cmap);
		}
		// out.println("RESULTCOLUMNS***" + colmaps);
		// out.println("-------------------------------------");

		// build result-indexes
		ArrayList resinds = new ArrayList();
		XmlMap indmaps = new XmlMap();
		if (addinds.containsKey(null)) {
			resinds.addAll((ArrayList) (addinds.get(null)));
		}
		XmlMap imap = null;
		for (int j = 0; j < indmaps0.size(); j++) {
			imap = (XmlMap) indmaps0.get(j);
			String name = imap.getString("name");
			XmlMap im = (XmlMap) imap.clone();
			if (modinds.containsKey(name)) {
				Set dltind = (Set) modinds.get(name);
				Iterator iter = dltind.iterator();
				while (iter.hasNext()) {
					Map.Entry me = (Map.Entry) iter.next();
					im.put(me.getKey(), me.getValue());
				}
				resinds.add(im);
			} else if (!delinds.contains(name))
				resinds.add(im);
			if (addinds.containsKey(name)) {
				resinds.addAll((ArrayList) addinds.get(name));
			}
		}
		for (int j = 0; j < resinds.size(); j++) {
			imap = (XmlMap) resinds.get(j);
			indmaps.put("index" + (j == 0 ? "" : "" + j), imap);
		}
		// out.println("RESULTINDEXES***" + indmaps);
		// out.println("-------------------------------------");

		// build result table
		XmlMap tabmap = (XmlMap) (origin).clone();
		XmlMap topmaps = (XmlMap) tabmap.getXmlMap("Dbtable").clone();
		tabmap.put("Dbtable", topmaps);
		String tabname = topmaps.getXmlMap("primary-key").getString("tabname");
		Iterator iter = topattr.iterator();
		while (iter.hasNext()) {
			Map.Entry me = (Map.Entry) iter.next();
			topmaps.put(me.getKey(), me.getValue());
		}
		if (topmaps.getXmlMap("primary-key").getString("delete") != null)
			topmaps.remove("primary-key");
		topmaps.getXmlMap("primary-key").put("tabname", tabname);
		topmaps.put("columns", colmaps);
		topmaps.put("indexes", indmaps);
		return tabmap;
	}

	public boolean actionCheck(String object, String name, String expectedAction,
	    DbTableDifference diff) throws Exception {
		if ((COLUMN.equalsIgnoreCase(object) || INDEX.equalsIgnoreCase(object))
		    && (name == null || name.trim().length() == 0)) {
			fail("veri >>>>>>>      expected actions: name for object " + object
			    + " is not given");
			return false;
		}
		Action expected = null;
		Action real = null;
		if (isEmpty(expectedAction))
			expected = Action.NOTHING;
		else
			expected = Action.getInstance(expectedAction);
		if (isEmpty(object) || object.equalsIgnoreCase(TABLE)) {
			object = TABLE;
			if (diff == null)
				real = Action.NOTHING;
			else
				real = diff.getAction();
		} else if (object.equalsIgnoreCase(COLUMNS)) {
			if (diff == null || diff.getColumnsDifference() == null)
				real = Action.NOTHING;
			else
				real = diff.getColumnsDifference().getAction();
		} else if (object.equalsIgnoreCase(COLUMN)) {
			if (diff == null || diff.getColumnsDifference() == null)
				real = Action.NOTHING;
			else {
				DbColumnsDifference.MultiIterator iter = diff.getColumnsDifference()
				    .iterator();
				boolean found = false;
				while (iter.hasNext()) {
					DbColumnDifference coldif = iter.next();
					String colname = null;
					if (coldif.getOrigin() == null)
						colname = coldif.getTarget().getName();
					else
						colname = coldif.getOrigin().getName();
					if (colname.equalsIgnoreCase(name)) {
						real = coldif.getAction();
						found = true;
						break;
					}
				}
				if (!found) {
					fail("veri >>>>>>>      expected actions: name " + name
					    + " for object " + object + " is wrong");
					return false;
				}
			}
		} else if (object.equalsIgnoreCase(INDEXES)) {
			real = diff.getIndexesDifference() == null ? Action.NOTHING
			    : Action.DROP_CREATE;
		} else if (object.equalsIgnoreCase(INDEX)) {
			if (diff == null || diff.getIndexesDifference() == null)
				real = Action.NOTHING;
			else {
				Iterator iter = diff.getIndexesDifference().iterator();
				boolean found = false;
				while (iter.hasNext()) {
					DbIndexDifference indexdif = (DbIndexDifference) iter.next();
					String indexname = null;
					if (indexdif.getOrigin() == null)
						indexname = indexdif.getTarget().getName();
					else
						indexname = indexdif.getOrigin().getName();
					if (indexname.equalsIgnoreCase(name)) {
						real = indexdif.getAction();
						found = true;
						break;
					}
				}
				if (!found) {
					fail("veri >>>>>>>      expected actions: name " + name
					    + " for object " + object + " is wrong");
					return false;
				}
			}
		} else if (object.equalsIgnoreCase(PRIMARY_KEY)) {
			real = diff.getPrimaryKeyDifference() == null ? Action.NOTHING
			    : Action.DROP_CREATE;
		} else {
			fail("veri >>>>>>>      expected actions: object " + object + " is wrong");
			return false;
		}
		if (real == null)
			real = Action.NOTHING;
		if (real == expected)
			return true;
		String error = "veri >>>>>>>      action for object " + object + " ";
		if (!isEmpty(name))
			error += name + " ";
		error += "is " + real + " but expected " + expected;
		fail(error);
		return false;

	}

	public static boolean isEmpty(String str) {
		if (str == null || str.trim().length() == 0)
			return true;
		return false;
	}

	public static boolean eq(String str1, String str2) {
		if (str1 == null && str2 == null)
			return true;
		else if (str1 == null)
			return false;
		else if (str2 == null)
			return false;
		return (str1.equalsIgnoreCase(str2));
	}

	public boolean rtcheck(DbFactory factory, DbTable target) throws Exception {
		boolean isRtcheckOk = true;
		XmlExtractor extractor = new XmlExtractor();
		DbRuntimeObjects runtimeObjects = DbRuntimeObjects.getInstance(factory);
		String tabname = target.getName();
		String str = null;
		DbTableDifference diff = null;

		try {
			DbTable tab1 = factory.makeTable();
			tab1.setCommonContentViaXml(runtimeObjects.get(tabname));
			tab1.replaceSpecificContent(target);
			DbDeploymentInfo di = target.getDeploymentInfo();
			if (di == null) {
				target.setDeploymentInfo(new DbDeploymentInfo());
				di = target.getDeploymentInfo();
			}
			boolean prold = di.positionIsRelevant();
			di.setPositionIsRelevant(true);
			diff = target.compareTo(tab1);
			di.setPositionIsRelevant(prold);
			if (diff != null) {
				fail("veri >>>>>>> (clob) target not reached");
				return false;
			}
		} catch (Exception ex) {
			fail("veri >>>>>>> xml (clob) read exception:");
			ex.printStackTrace();
			isRtcheckOk = false;
		}

		return isRtcheckOk;
	}

	public void modify(DbFactory factory, Action[] actions, int[] incl, int[] excl) {
		DbModificationController modController = new DbModificationController(
		    factory, false, null);
		IDbDeployObjects deployTables = modController.getDeployTables();
		println("\nM O D I F Y");
		for (int i = 0; i < mapsto.size(); i++) {
			if (!isIncluded(i, incl, excl))
				continue;
			if (actions[i] == Action.REFUSE)
				continue;
			XmlMap mapto = (XmlMap) mapsto.get(i);
			String tabname = mapto.getXmlMap("Dbtable").getString("name")
			    .toUpperCase();
			println("   add to modify " + tabname);
			deployTables.put(tabname, DbTools.currentTime(), mapto);
			// begin temp - delete after the experiment
			// if (actions[i] == Action.CONVERT) {
			// DbRuntimeObjects runtimeObjects =
			// DbRuntimeObjects.getInstance(factory);
			// runtimeObjects.remove(tabname);
			// }
			// end temp - delete after the experiment
		}
		modController.switchOnTrace(Severity.ERROR);
		int dbsSeverity = modController.distribute();
		modController.switchOffTrace();
		if (DbsSeverity.WARNING < dbsSeverity)
			fail("veri >>>>>>>      Modification failed. P R O T O C O L:\n "
			    + modController.getInfoText());
	}

	public static boolean isIncluded(int i, int[] incl, int[] excl) {
		if (incl != null) {
			for (int j = 0; j < incl.length; j++)
				if (incl[j] == i) {
					return true;
				}
			return false;
		} else if (excl != null) {
			for (int j = 0; j < excl.length; j++)
				if (excl[j] == i) {
					return false;
				}
			return true;
		}
		return true;
	}

	public static boolean isIncluded(String dbvers, String interval) {
		interval = interval.trim();
		boolean minincl = false;
		boolean maxincl = false;
		boolean minexcl = false;
		boolean maxexcl = false;
		String min = null;
		String max = null;
		StringBuffer sb = null;
		char[] cs = interval.toCharArray();
		for (int i = 0; i < cs.length; i++) {
			if (cs[i] == '(') {
				minexcl = true;
				sb = new StringBuffer();
				continue;
			} else if (cs[i] == '[') {
				minincl = true;
				sb = new StringBuffer();
				continue;
			} else if (cs[i] == ',') {
				min = sb.toString();
				sb = new StringBuffer();
				continue;
			} else if (cs[i] == ')') {
				max = sb.toString();
				maxexcl = true;
				break;
			} else if (cs[i] == ']') {
				max = sb.toString();
				maxincl = true;
				break;
			}
			if (sb != null)
				sb.append(cs[i]);
		}
		if (isEmpty(min) && isEmpty(max))
			return dbvers.equalsIgnoreCase(interval);
		if (!isEmpty(min)) {
			int res = dbvers.compareToIgnoreCase(min);
			if (res < 0)
				return false;
			if (res == 0 && minexcl)
				return false;
		}
		if (!isEmpty(max)) {
			int res = dbvers.compareToIgnoreCase(max);
			if (res > 0)
				return false;
			if (res == 0 && maxexcl)
				return false;
		}
		return true;
	}

	public static HashMap getContent(XmlMap inmap, DbTable table)
	    throws Exception {
		// System.out.println(inmap);
		HashMap res = new HashMap();
		XmlMap columns = inmap.getXmlMap("columns");
		XmlMap nextColumn = null;
		for (int i = 0; !(nextColumn = columns.getXmlMap("column"
		    + (i == 0 ? "" : "" + i))).isEmpty(); i++) {
			XmlMap lines = nextColumn.getXmlMap("data-lines");
			if (lines.size() == 0)
				continue;
			String name = nextColumn.getString("name");
			DbColumn column = table.getColumns().getColumn(name);
			Object[] columnContent = getColumnContent(lines, column.getJavaSqlType(),
			    column.getLength());
			res.put(name, columnContent);
		}
		return res;
	}

	public static Object[] getColumnContent(XmlMap lines, int type, long len)
	    throws Exception {
		Object[] res = new Object[lines.size()];
		String line = null;
		for (int i = 0; i < res.length; i++) {
			line = lines.getString("data-line" + (i == 0 ? "" : "" + i));
			if (line.trim().equalsIgnoreCase("null")) {
				continue;
			}
			switch (type) {
			case Types.VARCHAR:
			case Types.CLOB:
				res[i] = line;
				break;
			case Types.BINARY:
				res[i] = convertToByteArray(line, len);
				break;
			case Types.LONGVARBINARY:
			case Types.BLOB:
				res[i] = convertToByteArray(line, 0);
				break;
			case Types.DATE:
				res[i] = new java.sql.Date(DATE_FORMAT.parse(line).getTime());
				break;
			case Types.TIME:
				res[i] = new java.sql.Time(TIME_FORMAT.parse(line).getTime());
				break;
			case Types.TIMESTAMP:
				res[i] = new java.sql.Timestamp(TIMESTAMP_FORMAT.parse(line).getTime());
				break;
			case Types.SMALLINT:
				res[i] = new Short(line);
				break;
			case Types.INTEGER:
				res[i] = new Integer(line);
				break;
			case Types.BIGINT:
				res[i] = new Long(line);
				break;
			case Types.DECIMAL:
				res[i] = new BigDecimal(line);
				break;
			case Types.REAL:
				res[i] = new Float(line);
				break;
			case Types.DOUBLE:
				res[i] = new Double(line);
				break;
			}

		}

		return res;
	}

	public static byte[] convertToByteArray(String line, long len) {
		int ilen = new Long(len).intValue();
		byte[] res = null;
		if (len == 0)
			res = new byte[line.length() / 2];
		else
			res = new byte[ilen];
		Integer tmpI = null;
		int tmpi = 0;
		for (int i = 0; i <= line.length(); i = i + 2) {
			if (i == 0)
				continue;
			tmpI = Integer.decode("#" + line.substring(i - 2, i));
			tmpi = tmpI.intValue();
			res[i / 2 - 1] = tmpi > Byte.MAX_VALUE ? new Integer(tmpi - 256)
			    .byteValue() : tmpI.byteValue();
		}
		return res;
	}

	public void insertContent(Connection con, DbTable table, HashMap content)
	    throws Exception {
		PreparedStatement insPstmt = null;
		String templ = "INSERT INTO \"" + table.getName() + "\" (";
		String templ1 = "";
		DbColumns cols = table.getColumns();
		DbColumnIterator iter = cols.iterator();
		while (iter.hasNext()) {
			DbColumn col = iter.next();
			templ += "\"" + col.getName() + "\""
			    + (iter.hasNext() ? "," : ") values (");
			templ1 += "?" + (iter.hasNext() ? "," : ")");
		}
		templ += templ1;
		// try {
		insPstmt = con.prepareStatement(templ);
		int row = 0;
		while (true) {
			iter = cols.iterator();
			int pos = 0;
			boolean found = false;
			while (iter.hasNext()) {
				pos++;
				DbColumn column = iter.next();
				Object[] colContents = (Object[]) content.get(column.getName());
				int colContentsLen = (colContents == null ? 0 : colContents.length);
				if (colContentsLen > row) {
					found = true;
					if (colContents[row] == null)
						insPstmt.setNull(pos, column.getJavaSqlType());
					else
						insPstmt.setObject(pos, colContents[row]);
				} else {
					VeriTools.setColumnValue(insPstmt, column, pos, 0);
				}
			}
			insPstmt.executeUpdate();
			if (!found)
				break;
			row++;
		}
		if (!con.getAutoCommit())
			con.commit();
		insPstmt.close();
		// } catch (SQLException e) {
		// fail("veri >>>>>>> table " + table.getName() + " unexpected error");
		// e.printStackTrace();
		// isResultOk = false;
		// }
	}

	public boolean checkContent(Connection con, DbTable table, HashMap content)
	    throws Exception {
		DbColumns cols = table.getColumns();
		DbColumnIterator iter = cols.iterator();
		// try {
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT * FROM \"" + table.getName()
		    + "\"");
		System.out.println(table.getName());
		int row = 0;
		while (rs.next()) {
			iter = cols.iterator();
			while (iter.hasNext()) {
				DbColumn column = iter.next();
				String name = column.getName();
				Object[] colContents = (Object[]) content.get(name);
				int colContentsLen = (colContents == null ? 0 : colContents.length);
				int type = column.getJavaSqlType();
				if (colContentsLen <= row)
					continue;
				if (colContents[row] == null && column.isNotNull())
					colContents[row] = column.getDefaultObject();
				if (colContents[row] instanceof byte[]) {
					byte[] ob1a = (byte[]) colContents[row];
					byte[] ob2a = (byte[]) rs.getBytes(name);
					for (int i = 0; i < ob2a.length; i++) {
						if (i >= ob1a.length && ob2a[i] != 0) {
							fail("data loss: byte array is too long. " + (i-1) +
									" elements are expected");
							return false;
						}
						if (i < ob1a.length && ob1a[i] != ob2a[i]) {
							fail("data loss: element " + i + " of the byte array is " +
									ob2a[i]  + " but expected " + ob1a[i]);
							return false;
						}
					}
					continue;
				}
				Object ob1 = colContents[row];
				Object ob2 = null;
				if (ob1 instanceof String)
					ob2 = rs.getString(name);
				else
					ob2 = rs.getObject(name);
				if (ob1 != ob2 && (ob1 == null || !ob1.equals(ob2))) {
					fail("data loss: content is " + ob2 + " but expected " + ob1);
					return false;
				}
				continue;
			}
			row++;
		}
		stmt.close();
		return true;
		// } catch (Exception ex) {
		// return false;
		// }
	}

	public boolean exec(Connection con) {
		return exec(con, true);
	}

	public boolean exec(Connection con, boolean breakIfError) {
		return exec(con, breakIfError, null, null);
	}

	public boolean exec(Connection con, boolean breakIfError, HashMap incl,
	    HashMap excl) {
		switchOnTrace();
		InputStream stream = VeriCore.class.getResourceAsStream(filename);
		XmlExtractor extractor = new XmlExtractor();
		XmlMap verifile = extractor.map(new InputSource(stream));
		XmlMap inverifile = verifile.getXmlMap("veri");
		if (inverifile.isEmpty()) {
			inverifile = verifile;
		}
		XmlMap vmaps = null;
		boolean resultIsOk = true;
		int[] aincl = null;
		int[] aexcl = null;
		for (int i = 0; !(vmaps = inverifile.getXmlMap("veritables", i)).isEmpty(); i++) {
			init(vmaps);
			if (incl != null)
				aincl = (int[]) incl.get(new Integer(i));
			if (excl != null)
				aexcl = (int[]) excl.get(new Integer(i));
			resultIsOk &= exec2(con, breakIfError, aincl, aexcl);
			if (!resultIsOk && breakIfError)
				return false;
		}
		switchOffTrace();
		return resultIsOk;
	}

	public boolean exec2(Connection con, boolean breakIfError, int[] incl,
	    int[] excl) {
		boolean isResultOk = true;
		DbFactory factory = null;
		DbObjectSqlStatements stmts = null;
		DbTableDifference diff = null;
		DbColumnsDifference cdiff = null;
		String tabname = null;
		Action action0 = null;
		Action clmnsAction0 = null;
		Action indxsAction0 = null;
		Action prkeyAction0 = null;
		Action action = null;
		Action clmnsAction = null;
		Action indxsAction = null;
		Action prkeyAction = null;
		try {
			println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$ "
			    + Database.getDatabase(con).getAbbreviation() + " "
			    + filename.substring(10, filename.indexOf('.'))
			    + " $$$$$$$$$$$$$$$$$$$$$$$$$$$$");
			factory = new DbFactory(con);

			DbModificationController modController = new DbModificationController(
			    factory, false, null);
			IDbDeployObjects deployTables = modController.getDeployTables();
			DbRuntimeObjects ro = DbRuntimeObjects.getInstance(factory);
			HashMap[] contents = new HashMap[mapsfrom.size()];
			Action[] actions = new Action[mapsfrom.size()];
			DbTable[] fromsViaDb = new DbTable[mapsfrom.size()];
			for (int i = 0; i < mapsfrom.size(); i++) {
				XmlMap mapfrom = (XmlMap) mapsfrom.get(i);
				XmlMap mapto = (XmlMap) mapsto.get(i);
				// System.exit(1);
				XmlMap inmapfrom = mapfrom.getXmlMap("Dbtable");
				XmlMap inmapto = mapto.getXmlMap("Dbtable");
				DbTable tabfrom = factory.makeTable();
				DbTable tabfromViaXml = tabfrom;
				DbTable tabto = factory.makeTable();
				DbTable tabres = factory.makeTable();
				tabfrom.setCommonContentViaXml(mapfrom);
				tabfrom.setSpecificContentViaXml(mapfrom);
				tabname = tabfrom.getName();
				tabto.setCommonContentViaXml(mapto);
				tabfrom.setSpecificContentViaXml(mapfrom);
				// out.println("*********************************************");
				println("-------------------------------------");
				if (i == 0 || inmapto.getBoolean("origin"))
					println("*ORIGIN* Table " + tabname);
				else
					println("Table " + tabname);
				println("   "
				    + inmapto.getXmlMap("properties").getXmlMap("description")
				        .getString("description"));
				if (!isIncluded(i, incl, excl)) {
					println("   *******SKIP************");
					continue;
				}
				XmlMap config = inmapto.getXmlMap("config");
				if (!config.isEmpty()) {
					Iterator it = config.entrySet().iterator();
					while (it.hasNext()) {
						Map.Entry pair = (Map.Entry) it.next();
						println("   " + pair.getKey() + "=" + pair.getValue());
					}
				}
				// out.println("TABFROM***" + tabfrom);
				// out.println("TABFROM***" + mapsfrom.get(i));
				// out.println("-------------------------------------");
				// out.println("TABTO***" + tabto);
				// out.println("TABTO***" + mapsto.get(i));
				// out.println("-------------------------------------");
				// out.println("***********************************************");

				// drop table
				deployTables.reset();
				deployTables.put(tabname, DbTools.currentTime(), Action.DROP, mapfrom);
				modController.distribute();
				// stmts = tabfrom.getDdlStatementsForDrop();
				// try {
				// stmts.execute(con);
				// ro.remove(tabfrom.getName());
				// factory.getTools().commit();
				// // Invalidate entry for name in table-buffer
				// factory.getTools().invalidate(tabfrom.getName());
				// } catch (Exception ex) {
				// }

				// consistency check of the database specifica
				if (!tabfrom.checkSpecificContent()) {
					fail("veri >>>>>>>      xml definition of table is not consistent");
					if (breakIfError)
						return false;
					isResultOk = false;
					continue;
				}

				// create source("from") table
				deployTables.reset();
				deployTables
				    .put(tabname, DbTools.currentTime(), Action.CREATE, mapfrom);
				modController.distribute();
				// stmts = tabfrom.getDdlStatementsForCreate();
				// if (stmts != null)
				// if (!stmts.execute(con)) {
				// out
				// .println("veri >>>>>>> " + tabname
				// + " create error");
				// if (breakIfError)
				// return false;
				// isResultOk = false;
				// continue;
				// } else {
				// ro.putTable(tabname, mapfrom);
				// factory.getTools().commit();
				// }

				// read source("from") table from database
				tabfrom = factory.makeTable(tabname);
				tabfrom.setCommonContentViaDb(factory);
				tabfrom.setSpecificContentViaDb();
				fromsViaDb[i] = tabfrom;

				// compare the XML and DB-versions of the table
				if (!tabfromViaXml.equalsSpecificContent(tabfrom)) {
					fail("veri >>>>>>>      xml definition of table is not equal the db definition");
					if (breakIfError)
						return false;
					isResultOk = false;
				}

				// compare source(from) with target(to)
				tabto.replaceSpecificContent(tabfrom); // changes of specific content
				// are not allowed
				if (tabto == null || tabfrom == null || tabto.getColumns() == null
				    || tabfrom.getColumns() == null)
					diff = null;
				else
					diff = tabfrom.compareTo(tabto);
				if (diff == null)
					actions[i] = null;
				else
					actions[i] = diff.getAction();
				String cdbname = Database.getDatabase(con).getAbbreviation();
				String cdbvers = factory.getEnvironment().getDatabaseVersion();
				XmlMap eamap = inmapto.getXmlMap("expected-actions");
				Collection col = eamap.values();
				String[][] eas = new String[col.size()][5];
				Iterator iter = eamap.values().iterator();
				for (int j = 0; j < eas.length; j++) {
					XmlMap ea = (XmlMap) iter.next();
					eas[j][0] = ea.getString("name");
					eas[j][1] = ea.getString("object");
					eas[j][2] = ea.getString("object-name");
					eas[j][3] = ea.getString("dbs-name");
					eas[j][4] = ea.getString("dbs-version");
				}
				String dbname = null;
				String dbvers = null;
				for (int j = 0; j < eas.length; j++) {
					dbname = eas[j][3];
					dbvers = eas[j][4];
					if (!isEmpty(dbname) && !cdbname.equalsIgnoreCase(dbname))
						continue;
					if (!isEmpty(dbname) && !isEmpty(dbvers)
					    && !isIncluded(cdbvers, dbvers))
						continue;
					if (isEmpty(dbname)) {
						boolean found = false;
						for (int k = 0; k < eas.length; k++) {
							if (k != j && eq(eas[j][1], eas[k][1])
							    && eq(eas[j][2], eas[k][2])
							    && cdbname.equalsIgnoreCase(eas[k][3])
							    && (isEmpty(eas[k][4]) || isIncluded(cdbvers, eas[k][4]))) {
								found = true;
								break;
							}
						}
						if (found)
							continue;
					}
					if (!isEmpty(dbname) && isEmpty(dbvers)) {
						boolean found = false;
						for (int k = 0; k < eas.length; k++) {
							if (k != j && eq(eas[j][1], eas[k][1])
							    && eq(eas[j][2], eas[k][2])
							    && cdbname.equalsIgnoreCase(eas[k][3])
							    && isIncluded(cdbvers, eas[k][4])) {
								found = true;
								break;
							}
						}
						if (found)
							continue;
					}
					if (!actionCheck(eas[j][1], eas[j][2], eas[j][0], diff)) {
						if (breakIfError)
							return false;
						isResultOk = false;
					} else {
						String str = "   action " + eas[j][0] + " for ";
						if (!isEmpty(eas[j][1]))
							str += eas[j][1];
						else
							str += "table";
						if (!isEmpty(eas[j][2]))
							str += " " + eas[j][1];
						println(str);
					}
				}

				// fill content into from-table
				if (inmapto.getString("nodata") == null) {
					if (tabfrom != null && tabfrom.getColumns() != null) {
						contents[i] = getContent(inmapfrom, tabfromViaXml);
						insertContent(con, tabfromViaXml, contents[i]);
					}
				}
			}

			modify(factory, actions, incl, excl);

			for (int i = 0; i < mapsfrom.size(); i++) {
				if (!isIncluded(i, incl, excl))
					continue;
				if (actions[i] == Action.REFUSE) {
					XmlMap mapfrom = (XmlMap) mapsfrom.get(i);
					DbTable tabfrom = factory.makeTable();
					tabname = tabfrom.getName();
					// clear database (drop table)
					deployTables.reset();
					deployTables
					    .put(tabname, DbTools.currentTime(), Action.DROP, mapfrom);
					modController.distribute();
					continue;
				}
				XmlMap mapfrom = (XmlMap) mapsfrom.get(i);
				XmlMap mapto = (XmlMap) mapsto.get(i);
				XmlMap inmapto = mapto.getXmlMap("Dbtable");
				DbTable tabfrom = factory.makeTable();
				DbTable tabto = factory.makeTable();
				DbTable tabres = factory.makeTable();
				tabfrom.setCommonContentViaXml(mapfrom);
				tabname = tabfrom.getName();
				tabto.setCommonContentViaXml(mapto);

				// check content
				Action predefinedAction = tabto.getDeploymentInfo()
				    .getPredefinedAction();
				if (actions[i] != Action.DROP_CREATE
				    && predefinedAction != Action.DROP_CREATE
				    && inmapto.getString("nodata") == null) {
					boolean ok = checkContent(con, tabto, contents[i]);
					if (!ok) {
						fail("veri >>>>>>> table" + tabname + " lost data");
						if (breakIfError)
							return false;
						isResultOk = false;
					}
					// Statement stmt = con.createStatement();
					// try {
					// ResultSet rs = stmt.executeQuery("SELECT * FROM \""
					// + tabto.getName() + "\"");
					// if (!rs.next()) {
					// fail("veri >>>>>>> table" + tabname + " lost data");
					// if (breakIfError)
					// return false;
					// isResultOk = false;
					// }
					// byte[] by = rs.getBytes("F1");
					// System.out.print("\n" + tabto.getName() + ": " + by.length + ":");
					// for (int j = 0; j < by.length; j++) {
					// System.out.print(by[j] + ",");
					// }
					// stmt.close();
					// } catch (Exception ex) {
					// ex.printStackTrace();
					// if (breakIfError)
					// return false;
					// isResultOk = false;
					// }
				}

				// read table from database
				tabres = factory.makeTable(tabname);
				tabres.setCommonContentViaDb(factory);
				tabres.setSpecificContentViaDb();

				// compare source and result database specific content
				if (!tabto.specificForce()) {
					if (!fromsViaDb[i].equalsSpecificContent(tabres)) {
						fail("veri >>>>>>>      specific content is changed after table adjusting");
						if (breakIfError)
							return false;
						isResultOk = false;
					}
				} else {
					tabto.setSpecificContentViaXml(mapto);
					if (!tabto.equalsSpecificContent(tabres)) {
						fail("veri >>>>>>>      specific content is not correct after table adjusting");
						if (breakIfError)
							return false;
						isResultOk = false;
					}
				}

				// compare target with database
				DbTable tempto = tabto;
				if (!tabto.specificForce())
					tempto.replaceSpecificContent(fromsViaDb[i]);
				diff = tabres.compareTo(tempto);
				if (diff != null) {
					fail("veri >>>>>>> target not reached " + tabname);
					if (breakIfError)
						return false;
					isResultOk = false;
				}

				// runtime object check
				try {
					boolean isRuntimeObjectOk = rtcheck(factory, tabres);
					if (isRuntimeObjectOk == false) {
						fail("veri >>>>>>> error in runtime object");
						if (breakIfError)
							return false;
						isResultOk = false;
					}
				} catch (Exception ex) {
					fail("veri >>>>>>> error in runtime object:");
					ex.printStackTrace();
					if (breakIfError)
						return false;
					isResultOk = false;
				}

				// clear database (drop table)
				deployTables.reset();
				deployTables.put(tabname, DbTools.currentTime(), Action.DROP, mapfrom);
				modController.distribute();
				// stmts = tabfrom.getDdlStatementsForDrop();
				// try {
				// stmts.execute(con);
				// ro.remove(tabfrom.getName());
				// // Invalidate entry for name in table-buffer
				// factory.getTools().invalidate(tabfrom.getName());
				// } catch (Exception ex) {
				// }
				// throw new Exception("1234567890");

			}
		} catch (Exception ex) {
			fail("veri >>>>>>> table " + tabname + " unexpected error " + ex );
			//ex.printStackTrace();
			isResultOk = false;
		}

		println("============================================");
		// out.println("============================================");
		// out.println("============================================");
		return isResultOk;
	}

	public void fail(String mess) {
		if (out != null) {
			out.println(mess);
			loc.errorT(mess);
		} else {
			Assert.fail(mess);
		}
	}

	public void println(String mess) {
		if (out != null) {
			out.println(mess);
			loc.infoT(mess);
			// } else {
			// Assert.fail(mess);
		}
	}

	public void print(String mess) {
		if (out != null) {
			out.print(mess);
			loc.infoT(mess);
			// } else {
			// Assert.fail(mess);
		}
	}

	public void setOut(PrintStream stream) {
		// System.out.println("bevor: " + (out == null? "empty":out));
		// if (out != null)
		// out.println("System");
		// System.out.println("after: " + (stream == null? "empty":stream));
		// if (stream != null)
		// stream.println("System");
		preout = out;
		out = stream;
	}

	public void resetOut() {
		// System.out.println("reset to: " + (preout == null? "empty":preout));
		out = preout;
		// if (out != null)
		// out.println("System");
	}

	public void switchOnTrace() {
		if (tempLog != null)
			loc0.removeLog(tempLog);
		infoText = new ByteArrayOutputStream(128);
		tempLog = new StreamLog(infoText, new DbTraceFormatter());
		loc0.setEffectiveSeverity(Severity.INFO);
		loc0.setName("temp");
		loc0.addLog(tempLog);
	}

	public void switchOffTrace() {
		if (tempLog == null)
			return;
		tempLog.close();
		loc0.removeLog(tempLog);
		tempLog = null;

	}

	public String getInfoText() {
		if (infoText != null)
			return infoText.toString();
		return null;
	}

}

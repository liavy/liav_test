package com.sap.archtech.daservice.commands;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import javax.servlet.http.HttpServletResponse;

import com.sap.archtech.archconn.values.IndexPropValues;
import com.sap.archtech.daservice.data.ColumnData;
import com.sap.archtech.daservice.data.DasResponse;
import com.sap.archtech.daservice.data.TechnicalIndexData;
import com.sap.archtech.daservice.exceptions.GeneralConflictException;
import com.sap.archtech.daservice.exceptions.MissingParameterException;
import com.sap.archtech.daservice.exceptions.NoSuchDBObjectException;
import com.sap.guid.GUIDFormatException;
import com.sap.guid.GUIDGeneratorFactory;
import com.sap.guid.IGUIDGenerator;
import com.sap.tc.logging.Severity;

public class IndexGetMethod extends MasterMethod {
	private final static String CHECK_URI1 = "SELECT colid FROM bc_xmla_col WHERE uri = ?";
	private final static String CHECK_URI2 = "SELECT resid FROM bc_xmla_res WHERE colid = ? AND delstatus IN ('Y', 'N', 'P') AND resname = ?";
	private final static String IDX_GET = "SELECT indexTable, indexId FROM bc_xmla_index_dict WHERE indexName = ?";
	private final static String COL_GET = "SELECT * FROM bc_xmla_index_cols WHERE indexId = ?";

	private final static IGUIDGenerator guidGen = GUIDGeneratorFactory
			.getInstance().createGUIDGenerator();

	private final Connection connection;
	private final String index_name;
	private final String uri;
	private final boolean isCalledByJavaArchConn;

	public IndexGetMethod(Connection connection, HttpServletResponse response,
			String index_name, String uri, String useragent) {
		this.connection = connection;
		this.response = response;
		this.index_name = index_name == null ? "" : index_name.trim()
				.toLowerCase();
		this.uri = uri == null ? "" : uri.trim().toLowerCase();
		if ("SAPArchivingConnector".equals(useragent)) {
			isCalledByJavaArchConn = true;
		} else {
			isCalledByJavaArchConn = false;
		}
	}

	public boolean execute() throws IOException {
		response.setContentType(MasterMethod.contentType);
		response.setHeader("service_message", "see message body");
		response.setHeader("index_name", index_name);

		PreparedStatement checkUri_1 = null;
		PreparedStatement getIdx = null;
		PreparedStatement getCols = null;
		PreparedStatement checkUri_2 = null;
		BufferedWriter bwout = null;
		try {
			checkParams();
			// get resource name (= last URI part)
			int lastSlashPos = uri.lastIndexOf("/");
			String coll = uri.substring(0, lastSlashPos);
			String resName = uri.substring(lastSlashPos + 1, uri.length());
			// check if resource exists in this collection
			// Note: For the following two DB-statements a JOIN would work. We
			// encountered
			// DB problems with this JOIN. On SAPDB, the JOIN takes a very long
			// time.
			// Therefore, two statements are used here.
			checkUri_1 = connection.prepareStatement(CHECK_URI1);
			checkUri_1.setString(1, coll);
			checkUri_2 = connection.prepareStatement(CHECK_URI2);// param
																	// "colid"
																	// is the
																	// result of
																	// the
																	// "checkUri_1"
																	// query
			checkUri_2.setString(2, resName);
			long resId = checkResource(coll, resName, checkUri_1, checkUri_2);
			// check if index exists
			getIdx = connection.prepareStatement(IDX_GET);
			getIdx.setString(1, index_name);
			TechnicalIndexData tid = checkIndex(getIdx);
			// get all columns of the index
			getCols = connection.prepareStatement(COL_GET);
			getCols.setLong(1, tid.getIndexId());
			ArrayList<ColumnData> columnNames = getColumns(tid, getCols);
			bwout = new BufferedWriter(new OutputStreamWriter(response
					.getOutputStream(), "UTF8"));
			if (isCalledByJavaArchConn) {
				writeJavaResponse(columnNames, resId, tid);
			} else {
				writeABAPResponse(columnNames, resId, tid, bwout);
			}

			writeStatus(bwout, HttpServletResponse.SC_OK, "Ok");
			bwout.flush();
			return true;
		} catch (SQLException sqlex) {
			reportStreamError(DasResponse.SC_SQL_ERROR, "INDEX GET: "
					+ sqlex.getMessage(), sqlex, bwout);
			return false;
		} catch (NoSuchDBObjectException nsdbex) {
			reportStreamError(DasResponse.SC_DOES_NOT_EXISTS, "INDEX GET: "
					+ nsdbex.getMessage(), bwout);
			return false;
		} catch (MissingParameterException mspex) {
			reportStreamError(DasResponse.SC_PARAMETER_MISSING, "INDEX GET: "
					+ mspex.getMessage(), bwout);
			return false;
		} catch (GeneralConflictException gcex) {
			reportStreamError(DasResponse.SC_PARAMETER_SYNTAX_INCORRECT,
					"INDEX GET: " + gcex.getMessage(), bwout);
			return false;
		} catch (GUIDFormatException e) {
			reportStreamError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"INDEX GET: " + e.getMessage(), bwout);
			return false;
		} catch (IOException ioex) {
			bwout.flush();
			MasterMethod.cat.errorT(loc, getStackTrace(ioex));
			throw new IOException(ioex.toString());
		} finally {
			if (bwout != null) {
				try {
					bwout.close();
				} catch (IOException e) {
					cat.logThrowableT(Severity.WARNING, loc, "INDEX GET: "
							+ e.getMessage(), e);
				}
			}
			if (checkUri_1 != null) {
				try {
					checkUri_1.close();
				} catch (SQLException sqlex) {
					cat.logThrowableT(Severity.WARNING, loc, "INDEX GET: "
							+ sqlex.getMessage(), sqlex);
				}
			}
			if (getIdx != null) {
				try {
					getIdx.close();
				} catch (SQLException sqlex) {
					cat.logThrowableT(Severity.WARNING, loc, "INDEX GET: "
							+ sqlex.getMessage(), sqlex);
				}
			}
			if (getCols != null) {
				try {
					getCols.close();
				} catch (SQLException sqlex) {
					cat.logThrowableT(Severity.WARNING, loc, "INDEX GET: "
							+ sqlex.getMessage(), sqlex);
				}
			}
			if (checkUri_2 != null) {
				try {
					checkUri_2.close();
				} catch (SQLException sqlex) {
					cat.logThrowableT(Severity.WARNING, loc, "INDEX GET: "
							+ sqlex.getMessage(), sqlex);
				}
			}
		}
	}

	private void checkParams() throws MissingParameterException,
			GeneralConflictException {
		// check if parameters are empty
		if ("".equals(index_name)) {
			throw new MissingParameterException("No index name specified");
		}
		if ("".equals(uri)) {
			throw new MissingParameterException("No URI specified");
		}
		if (uri.contains("//") || uri.contains("\\") || !uri.startsWith("/")
				|| uri.endsWith("/") || uri.lastIndexOf("/") == 0) {
			// URI must start with a "/", must not end with a "/" and must
			// contain at least one further "/" (representing the resource)
			throw new GeneralConflictException("URI " + uri
					+ " does not meet specifications");
		}
	}

	private long checkResource(final String coll, final String resName,
			final PreparedStatement checkUri_1,
			final PreparedStatement checkUri_2) throws SQLException,
			NoSuchDBObjectException {
		ResultSet rs1 = null;
		ResultSet rs5 = null;
		try {
			rs1 = checkUri_1.executeQuery();
			if (!rs1.next()) {
				throw new NoSuchDBObjectException("INDEX GET: Collection "
						+ coll + " does not exist");
			}
			long colId = rs1.getLong("colid");

			checkUri_2.setLong(1, colId);
			rs5 = checkUri_2.executeQuery();
			if (!rs5.next()) {
				throw new NoSuchDBObjectException("INDEX GET: Resource " + uri
						+ " does not exist");
			}
			long resId = rs5.getLong("resid");
			return resId;
		} finally {
			if (rs1 != null) {
				try {
					rs1.close();
				} catch (SQLException e) {
					cat.logThrowableT(Severity.WARNING, loc, "INDEX GET: "
							+ e.getMessage(), e);
				}
			}
			if (rs5 != null) {
				try {
					rs5.close();
				} catch (SQLException e) {
					cat.logThrowableT(Severity.WARNING, loc, "INDEX GET: "
							+ e.getMessage(), e);
				}
			}
		}
	}

	private TechnicalIndexData checkIndex(final PreparedStatement getIdx)
			throws SQLException, NoSuchDBObjectException {
		ResultSet rs2 = null;
		try {
			rs2 = getIdx.executeQuery();
			if (!rs2.next()) {
				throw new NoSuchDBObjectException("INDEX GET: Index "
						+ index_name + " does not exists");
			}
			TechnicalIndexData tid = new TechnicalIndexData(rs2
					.getString("indexTable"), rs2.getLong("indexId"));
			return tid;
		} finally {
			if (rs2 != null) {
				try {
					rs2.close();
				} catch (SQLException e) {
					cat.logThrowableT(Severity.WARNING, loc, "INDEX GET: "
							+ e.getMessage(), e);
				}
			}
		}
	}

	private ArrayList<ColumnData> getColumns(final TechnicalIndexData tid,
			final PreparedStatement getCols) throws SQLException {
		ArrayList<ColumnData> columnNames = new ArrayList<ColumnData>();
		ResultSet rs3 = null;
		try {
			rs3 = getCols.executeQuery();
			while (rs3.next()) {
				columnNames.add(new ColumnData(rs3.getString("propName"), "",
						rs3.getString("jdbcType")));
			}
			return columnNames;
		} finally {
			if (rs3 != null) {
				try {
					rs3.close();
				} catch (SQLException e) {
					cat.logThrowableT(Severity.WARNING, loc, "INDEX GET: "
							+ e.getMessage(), e);
				}
			}
		}
	}

	private void writeJavaResponse(final ArrayList<ColumnData> columnNames,
			final long resId, final TechnicalIndexData tid)
			throws SQLException, IOException, GUIDFormatException {
		// construct IndexPropValues and serialize them
		Statement selectIdxTable = null;
		ObjectOutputStream objst = null;
		ArrayList<IndexPropValues> ipvList = new ArrayList<IndexPropValues>();
		try {
			IndexPropValues ipv = null;
			selectIdxTable = connection.createStatement();
			ResultSet rs4 = selectIdxTable.executeQuery("SELECT * FROM "
					+ tid.getTablename() + " WHERE resId = " + resId);
			String lctype = null;
			String lcname = null;
			Serializable colValue = null;
			while (rs4.next()) {
				// the index table may contain several property sets per
				// resource
				ipv = new IndexPropValues(index_name);
				ipvList.add(ipv);
				for (ColumnData column : columnNames) {
					lcname = column.getCName();
					lctype = column.getJdbcType();
					// fetch values according to allowed types
					if ("integer".equals(lctype)) {
						colValue = rs4.getInt(lcname);
						if (!rs4.wasNull()) {
							ipv.putProp(lcname, (Integer) colValue);
						}
					} else if ("smallint".equals(lctype)) {
						colValue = rs4.getShort(lcname);
						if (!rs4.wasNull()) {
							ipv.putProp(lcname, (Short) colValue);
						}
					} else if ("varchar".equals(lctype)) {
						colValue = rs4.getString(lcname);
						if (!rs4.wasNull()) {
							ipv.putProp(lcname, (String) colValue);
						}
					} else if (lctype.startsWith("real")) {
						colValue = rs4.getFloat(lcname);
						if (!rs4.wasNull()) {
							ipv.putProp(lcname, (Float) colValue);
						}
					} else if (lctype.startsWith("double")) {
						colValue = rs4.getDouble(lcname);
						if (!rs4.wasNull()) {
							ipv.putProp(lcname, (Double) colValue);
						}
					} else if (lctype.startsWith("bigint")) {
						colValue = rs4.getLong(lcname);
						if (!rs4.wasNull()) {
							ipv.putProp(lcname, (Long) colValue);
						}
					} else if (lctype.startsWith("timestamp")) {
						colValue = rs4.getTimestamp(lcname);
						if (!rs4.wasNull()) {
							ipv.putProp(lcname, (Timestamp) colValue);
						}
					} else if ("binary".equals(lctype)) {
						colValue = rs4.getBytes(lcname);
						if (!rs4.wasNull()) {
							ipv.putProp(lcname, colValue != null ? guidGen
									.createGUID((byte[]) colValue) : null);
						}
					}
				}
			}
			rs4.close();
			// write list of IndexPropValues to ObjectOutputStream()
			objst = new ObjectOutputStream(response.getOutputStream());
			objst.writeObject(ipvList);
			objst.flush();
		} finally {
			if (objst != null) {
				try {
					objst.close();
				} catch (IOException e) {
					cat.logThrowableT(Severity.WARNING, loc, "INDEX GET: "
							+ e.getMessage(), e);
				}
			}
			if (selectIdxTable != null) {
				try {
					selectIdxTable.close();
				} catch (SQLException e) {
					cat.logThrowableT(Severity.WARNING, loc, "INDEX GET: "
							+ e.getMessage(), e);
				}
			}
		}
	}

	private void writeABAPResponse(final ArrayList<ColumnData> columnNames,
			final long resId, final TechnicalIndexData tid,
			final BufferedWriter bwout) throws SQLException, IOException,
			GUIDFormatException {
		// Get all index attributes and their values
		Statement selectIdxTable = null;
		try {
			selectIdxTable = connection.createStatement();
			ResultSet rs4 = selectIdxTable.executeQuery("SELECT * FROM "
					+ tid.getTablename() + " WHERE resId = " + resId);
			String lctype = null;
			String lcname = null;
			String colValue = null;
			while (rs4.next()) {
				for (ColumnData column : columnNames) {
					lcname = column.getCName();
					lctype = column.getJdbcType();
					// fetch values according to allowed types
					if ("integer".equals(lctype)) {
						colValue = String.valueOf(rs4.getInt(lcname));
					} else if ("smallint".equals(lctype)) {
						colValue = String.valueOf(rs4.getShort(lcname));
					} else if ("varchar".equals(lctype)) {
						colValue = rs4.getString(lcname);
					} else if (lctype.startsWith("real")) {
						colValue = String.valueOf(rs4.getFloat(lcname));
					} else if (lctype.startsWith("double")) {
						colValue = String.valueOf(rs4.getDouble(lcname));
					} else if (lctype.startsWith("bigint")) {
						colValue = String.valueOf(rs4.getLong(lcname));
					} else if (lctype.startsWith("timestamp")) {
						Timestamp ts = rs4.getTimestamp(lcname);
						if (ts != null) {
							sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
							colValue = sdf.format(new Date(ts.getTime()
									+ (ts.getNanos() / 1000000)));
						}
					} else if ("binary".equals(lctype)) {
						byte[] guidBytes = rs4.getBytes(lcname);
						colValue = guidBytes != null ? guidGen.createGUID(
								guidBytes).toHexString() : null;
					}

					if (colValue != null && !rs4.wasNull()) {
						bwout.write(lcname + ":" + colValue);
						// add an CR/LF platform independent
						bwout.write(13);
						bwout.write(10);
					}
				}
			}
			rs4.close();
		} finally {
			if (selectIdxTable != null) {
				try {
					selectIdxTable.close();
				} catch (SQLException e) {
					cat.logThrowableT(Severity.WARNING, loc, "INDEX GET: "
							+ e.getMessage(), e);
				}
			}
		}
	}
}

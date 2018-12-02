package com.sap.archtech.daservice.commands;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sap.archtech.archconn.values.IndexPropValues;
import com.sap.archtech.daservice.data.CollectionData;
import com.sap.archtech.daservice.data.ColumnData;
import com.sap.archtech.daservice.data.DasResponse;
import com.sap.archtech.daservice.data.ResourceData;
import com.sap.archtech.daservice.data.Sapxmla_Config;
import com.sap.archtech.daservice.ejb.ArchStoreConfigLocalHome;
import com.sap.archtech.daservice.exceptions.ArchStoreConfigException;
import com.sap.archtech.daservice.exceptions.MissingParameterException;
import com.sap.archtech.daservice.exceptions.NoMoreDBObjectsException;
import com.sap.archtech.daservice.exceptions.NoSuchDBObjectException;
import com.sap.archtech.daservice.exceptions.WrongArgumentException;
import com.sap.archtech.daservice.exceptions.WrongChecksumException;
import com.sap.archtech.daservice.storage.XmlDasGet;
import com.sap.archtech.daservice.storage.XmlDasGetRequest;
import com.sap.archtech.daservice.storage.XmlDasGetResponse;
import com.sap.engine.frame.core.locking.LockException;
import com.sap.engine.frame.core.locking.TechnicalLockException;
import com.sap.engine.services.applocking.TableLocking;
import com.sap.guid.GUIDFormatException;
import com.sap.guid.GUIDGeneratorFactory;
import com.sap.guid.IGUIDGenerator;
import com.sap.tc.logging.Severity;

public class PickMethod extends MasterMethod {

	private final static String SEL_COL_TAB = "SELECT colId, storeId FROM BC_XMLA_COL WHERE uri = ?";
	private final static String SEL_RES_DEL = "SELECT * FROM BC_XMLA_RES WHERE delstatus = 'N' and colId = ? and restype <> 'XSD'";
	private final static String UPD_RES_DEL = "UPDATE BC_XMLA_RES SET delstatus = ? WHERE resId = ? AND delstatus = ?";
	private final static String GET_IDX_TAB = "SELECT indexTable, indexId FROM bc_xmla_index_dict WHERE indexName = ?";
	private final static String COL_GET = "SELECT * FROM bc_xmla_index_cols WHERE indexId = ?";
	private final static String UPD_RES_DEL2 = "UPDATE BC_XMLA_RES SET delstatus = ? WHERE resId = ?";

	private final static IGUIDGenerator guidGen = GUIDGeneratorFactory.getInstance().createGUIDGenerator();
	
	private Connection con;
	private HttpServletRequest request;
	private String uri;
	private TableLocking tlock;
	private ArchStoreConfigLocalHome beanLocalHome;

	public PickMethod(HttpServletResponse response, HttpServletRequest request,
			Connection con, String uri, TableLocking tlock,
			ArchStoreConfigLocalHome beanLocalHome) {
		this.response = response;
		this.request = request;
		this.con = con;
		this.uri = uri.toLowerCase();
		this.tlock = tlock;
		this.beanLocalHome = beanLocalHome;
	}

	public boolean execute() throws IOException {

		CollectionData coldat;
		PreparedStatement pst1 = null;
		PreparedStatement pst2 = null;
		PreparedStatement pst3 = null;
		PreparedStatement pst4 = null;
		PreparedStatement pst5 = null;
		PreparedStatement pst7 = null;
		Statement st6 = null;
		ResourceData pickedres = null;
		int respicked;
		boolean nolock = true;
		boolean catchup = true;
		HashMap<String, Long> pkMap = new HashMap<String, Long>();
		String confirmkey = request.getHeader("confirmdelkey");
		long delkey;
		int delcount;
		BufferedOutputStream bw = null;
		boolean returnxmldoc = false;
		int readsuccess = 0;
		boolean status = false;

		try {
			this.response.setHeader("service_message", "see message body");
			response.setContentType(MasterMethod.contentType);
			bw = new BufferedOutputStream(response.getOutputStream());

			pst1 = con.prepareStatement(SEL_COL_TAB);
			pst2 = con.prepareStatement(SEL_RES_DEL);
			pst3 = con.prepareStatement(UPD_RES_DEL);
			pst4 = con.prepareStatement(GET_IDX_TAB);
			pst5 = con.prepareStatement(COL_GET);
			pst7 = con.prepareStatement(UPD_RES_DEL2);
			st6 = con.createStatement();
			// check collection
			coldat = checkCollection(uri, pst1);
			// optional: confirm deletion - set delstatus from P to Y
			if (confirmkey != null) {
				delkey = Long.valueOf(confirmkey).longValue();
				pst7.setString(1, "Y");
				pst7.setLong(2, delkey);
				delcount = pst7.executeUpdate();
				if (delcount != 1)
					throw new NoSuchDBObjectException(
							"Resource key provided with this PICK request could not be found");
			}

			while (catchup) {

				/*
				 * Pick a resource - Select one resource with delstatus == N -
				 * lock this resource - update delstatus to P
				 */
				while (nolock) {
					pst2.setMaxRows(1);
					pst2.setLong(1, coldat.getcolId());
					ResultSet rs2 = pst2.executeQuery();
					if (!rs2.next())
						throw new NoMoreDBObjectsException(
								"No more resource to pick");
					pickedres = new ResourceData(rs2.getLong("RESID"), rs2
							.getString("RESNAME"),
							rs2.getString("FINGERPRINT"), rs2
									.getInt("PACKLENGTH"), rs2
									.getLong("OFFSET"), rs2
									.getString("PACKNAME"), rs2
									.getString("ISPACKED"));
					rs2.close();

					pkMap.clear();
					pkMap.put("RESID", new Long(pickedres.getResId()));

					try {
						tlock.lock(TableLocking.LIFETIME_TRANSACTION, con,
								"BC_XMLA_RES", pkMap,
								TableLocking.MODE_EXCLUSIVE_NONCUMULATIVE);
						nolock = false;
					} catch (LockException loex) {

						// $JL-EXC$
						loc.debugT("PICK: LockException for resource "
								+ pickedres.getResId());
					}
				} // while (nolock)
				pst3.setString(1, "P");
				pst3.setLong(2, pickedres.getResId());
				pst3.setString(3, "N");
				respicked = pst3.executeUpdate();
				if (respicked > 1)
					throw new SQLException("PICK: Picked " + respicked
							+ " resources!");
				if (respicked == 1)
					catchup = false;
				/*
				 * if (respicked == 0) repeat;
				 * 
				 * The UPDATE asks if delstatus == 'N'. The UPDATE may fail
				 * (does nothing) in the following parallel condition:
				 * 
				 * T1 -------SELECT resource x
				 * ---------------------------------------------- Lock
				 * Resource--------- T2 ------------- SELECT resource x -- Lock
				 * resource ----- Commit (unlock) -----------------------
				 * 
				 * For T1, the DELSTATUS of resource x is now 'Y' and not 'N'.
				 * To prevent this situation, we have to reread after setting
				 * the lock. Instead of doing the SELECT again, we use the
				 * UPDATE and evaluate the number of updated rows.
				 */
			} // while (catch up)

			this.response.setHeader("reskey", Long.toString(pickedres
					.getResId()));

			// Check request body and return property index value or complete
			// document
			String indexname = request.getHeader("index_name");
			if (indexname == null) {
				returnxmldoc = true;
				this.response.setHeader("returntype", "xmldoc");
			} else {
				this.response.setHeader("returntype", "propvalues");
				this.response.setHeader("index_name", indexname);
				this.getPropVal(indexname, pickedres.getResId(), pst4, pst5,
						st6);
			}

			// Read XML doc, check finger print and return it if no index info
			// was provided
			// Read it silently (Option NODELIVER) and check finger print if
			// index info was provided
			// Get Archive Store Configuration Data
			Sapxmla_Config sac = this.getArchStoreConfigObject(beanLocalHome,
					coldat.getStoreId());

			if (pickedres.getIsPacked().equalsIgnoreCase("Y")) {
				// Offset Is Only Considered If IsPacked Is Set
				if (pickedres.getOffset() <= 0) {
					this.reportStreamError(DasResponse.SC_WRONG_OFFSET,
							"PICK: Offset " + pickedres.getOffset()
									+ " is wrong", bw);
					return false;
				}
				XmlDasGetRequest getRequest = new XmlDasGetRequest(sac, bw,
						this.uri + pickedres.getPackName().trim(), pickedres
								.getOffset(), pickedres.getPackLength(),
						pickedres.getFpdb(), returnxmldoc ? "DELIVER"
								: "NODELIVER", "NO", null);
				XmlDasGet get = new XmlDasGet(getRequest);
				XmlDasGetResponse getResponse = get.execute();
				readsuccess = getResponse.getStatusCode();
			} else {
				XmlDasGetRequest getRequest = new XmlDasGetRequest(sac, bw,
						this.uri + pickedres.getResName().trim(), 0, 0,
						pickedres.getFpdb(), returnxmldoc ? "DELIVER"
								: "NODELIVER", "NO", null);
				XmlDasGet get = new XmlDasGet(getRequest);
				XmlDasGetResponse getResponse = get.execute();
				readsuccess = getResponse.getStatusCode();
			}

			if (readsuccess == DasResponse.SC_CHECKSUM_INCORRECT)
				throw new WrongChecksumException("Wrong checksum");

			this.writeStatus(bw, HttpServletResponse.SC_OK, "Ok");
			bw.flush();
			status = true;
		} catch (ArchStoreConfigException ascex) {
			this.reportStreamError(DasResponse.SC_CONFIG_INCONSISTENT, "PICK: "
					+ ascex.getMessage(), ascex, bw);
		} catch (WrongArgumentException waex) {
			this.reportStreamError(DasResponse.SC_PARAMETER_SYNTAX_INCORRECT,
					"PICK: " + waex.getMessage(), waex, bw);
		} catch (SQLException sqlex) {
			this.reportStreamError(DasResponse.SC_SQL_ERROR, "PICK: "
					+ sqlex.getMessage(), sqlex, bw);
		} catch (NoSuchDBObjectException nsdbex) {
			this.reportStreamError(DasResponse.SC_DOES_NOT_EXISTS, "PICK: "
					+ nsdbex.getMessage(), nsdbex, bw);
		} catch (MissingParameterException msex) {
			this.reportStreamError(DasResponse.SC_PARAMETER_MISSING, "PICK: "
					+ msex.getMessage(), msex, bw);
		} catch (TechnicalLockException tclex) {
			this.reportStreamError(
					HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "PICK: "
							+ tclex.getMessage(), tclex, bw);
		} catch (WrongChecksumException wcex) {
			this.reportStreamError(DasResponse.SC_CHECKSUM_INCORRECT, "PICK: "
					+ wcex.getMessage(), wcex, bw);
		} catch (NoMoreDBObjectsException nmdbex) {

			// $JL-EXC$
			/*
			 * Although we throw an exception here, we have to commit the
			 * updates! Finding no more elements to pick is a 'normal' end of a
			 * PICK request! In this case, the body is empty and it is not a
			 * streamed response. No logging takes place.
			 * 
			 */
			this.response.setStatus(DasResponse.SC_NO_MORE_OBJECTS);
			String newmessage = "PICK: ".concat(nmdbex.getMessage());
			newmessage = newmessage.replace('\n', ' ');
			this.response.setHeader("service_message", newmessage);
			status = true;
		} catch(GUIDFormatException e){
			reportStreamError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "PICK: " + e.getMessage(), bw);
		} finally {
			try {
				if (pst1 != null)
					pst1.close();
				if (pst2 != null)
					pst2.close();
				if (pst3 != null)
					pst3.close();
				if (pst4 != null)
					pst4.close();
				if (pst5 != null)
					pst5.close();
				if (st6 != null)
					st6.close();
				if (pst7 != null)
					pst7.close();
			} catch (SQLException sqlex) {
				cat.logThrowableT(Severity.WARNING, loc, "PICK: "
						+ sqlex.getMessage(), sqlex);
			}
			if (bw != null)
				bw.close();
		}
		return status;
	}

	private void getPropVal(String indexname, long resId,
			PreparedStatement pst4, PreparedStatement pst5, Statement st6)
			throws SQLException, NoSuchDBObjectException, IOException, GUIDFormatException {
		ArrayList<ColumnData> cNames = new ArrayList<ColumnData>();
		String tableName;
		long indexId;
		String lctype;
		String lcname;
		
		// check if index exists
		pst4.setString(1, indexname);
		ResultSet rs4 = pst4.executeQuery();
		if (!rs4.next())
			throw new NoSuchDBObjectException("Index " + indexname
					+ " does not exist");

		tableName = rs4.getString("indexTable");
		indexId = rs4.getLong("indexId");
		rs4.close();

		// get all columns of the Index
		pst5.setLong(1, indexId);
		ResultSet rs5 = pst5.executeQuery();
		while (rs5.next()) {
			ColumnData cData = new ColumnData(rs5.getString("propName"), "",
					rs5.getString("jdbcType"));
			cNames.add(cData);
		}
		rs5.close();

		// Get all index attributes and their values

		ResultSet rs6 = st6.executeQuery("SELECT * FROM " + tableName
				+ " WHERE resId = " + resId);
		Serializable colValue = null;
		ArrayList<IndexPropValues> ipvList = new ArrayList<IndexPropValues>();
		IndexPropValues ipv = null;
		while (rs6.next()) {
			// the index table may contain several property sets per resource
			ipv = new IndexPropValues(indexname);
			ipvList.add(ipv);
			for (int k = 0; k < cNames.size(); k++) {
				lcname = ((ColumnData) cNames.get(k)).getCName();
				lctype = ((ColumnData) cNames.get(k)).getJdbcType();
				// fetch values according to allowed types
				if (lctype.equals("integer"))
				{
					colValue = rs6.getInt(lcname);
					if(!rs6.wasNull())
					{
						ipv.putProp(lcname, (Integer)colValue);
					}
				}
				else if (lctype.equals("smallint"))
				{
					colValue = rs6.getShort(lcname);
					if(!rs6.wasNull())
					{
						ipv.putProp(lcname, (Short)colValue);
					}
				}
				else if (lctype.equals("varchar"))
				{
					colValue = rs6.getString(lcname);
					if(!rs6.wasNull())
					{
						ipv.putProp(lcname, (String)colValue);
					}
				}
				else if (lctype.startsWith("real"))
				{
					colValue = rs6.getFloat(lcname);
					if(!rs6.wasNull())
					{
						ipv.putProp(lcname, (Float)colValue);
					}
				}
				else if (lctype.startsWith("double"))
				{
					colValue = rs6.getDouble(lcname);
					if(!rs6.wasNull())
					{
						ipv.putProp(lcname, (Double)colValue);
					}
				}
				else if (lctype.startsWith("bigint"))
				{
					colValue = rs6.getLong(lcname);
					if(!rs6.wasNull())
					{
						ipv.putProp(lcname, (Long)colValue);
					}
				}
				else if (lctype.startsWith("timestamp"))
				{
					colValue = rs6.getTimestamp(lcname);
					if(!rs6.wasNull())
					{
						ipv.putProp(lcname, (Timestamp)colValue);
					}
				}
				else if("binary".equals(lctype))
				{
					colValue = rs6.getBytes(lcname);
					if(!rs6.wasNull())
					{
						ipv.putProp(lcname, colValue != null ? guidGen.createGUID((byte[])colValue) : null);
					}
				}
			}
		}
		rs6.close();
		ObjectOutputStream objst = new ObjectOutputStream(this.response
				.getOutputStream());
		objst.writeObject(ipvList);
		objst.flush();
	}
}
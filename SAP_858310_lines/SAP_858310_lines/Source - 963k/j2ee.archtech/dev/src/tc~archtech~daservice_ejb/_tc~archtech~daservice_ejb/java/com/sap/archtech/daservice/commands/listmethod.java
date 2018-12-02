package com.sap.archtech.daservice.commands;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sap.archtech.daservice.data.CollectionData;
import com.sap.archtech.daservice.data.DasResponse;
import com.sap.archtech.daservice.exceptions.GeneralConflictException;
import com.sap.tc.logging.Severity;

public class ListMethod extends MasterMethod {

	// BC_XMLA_COL
	private final static String GET_COLL = "SELECT colId, uri, creationUser, creationTime, colType, frozen FROM BC_XMLA_COL WHERE uri = ?";
	private final static String GET_CHILD_COLLS = "SELECT colId, uri, creationUser, creationTime, colType, frozen FROM BC_XMLA_COL WHERE parentColId = ?";
	private final static String GET_COLL_COUNT_NONRECURSIVE = "SELECT COUNT(*) AS rowcount FROM BC_XMLA_COL WHERE parentcolid = ?";
	private final static String GET_COLL_COUNT_RECURSIVE = "SELECT COUNT(*) AS rowcount FROM BC_XMLA_COL WHERE uri LIKE ";
	// BC_XMLA_RES
	private final static String GET_RES_WHERE_COLID = "SELECT * FROM BC_XMLA_RES WHERE colId = ?";
	private final static String GET_COUNT_RES_WHERE_COLID = "SELECT COUNT(*) AS rowcount FROM BC_XMLA_RES WHERE colId = ?";
	private final static String GET_COUNT_RES_RECURSIVE = "SELECT COUNT(*) AS rowcount FROM BC_XMLA_RES WHERE colid IN (SELECT colid FROM BC_XMLA_COL WHERE uri LIKE ";
	private final static String AND_CLAUSE_RESTYPE = " AND resType IN ";
	private final static String AND_CLAUSE_DELSTATUS = " AND delstatus = 'Y'";
	private final static String AND_CLAUSE_ADK_RES = " AND resname LIKE '%.adk'";
	private final static String AND_CLAUSE_NON_ADK_RES = " AND resname NOT LIKE '%.adk'";
	// JOIN BC_XMLA_RES
	private final static String GET_RES_JOIN_PART_1 = "SELECT * FROM BC_XMLA_RES AS r LEFT OUTER JOIN ";
	private final static String GET_RES_JOIN_PART_2 = " AS i ON r.resId = i.resId WHERE r.colId = ?";
	private final static String AND_CLAUSE_JOIN_RESTYPE = " AND r.resType IN ";
	private final static String AND_CLAUSE_JOIN_DELSTATUS = " AND r.delstatus = 'Y'";
	private final static String AND_CLAUSE_JOIN_ADK_RES = " AND r.resname LIKE '%.adk'";
	private final static String AND_CLAUSE_JOIN_NON_ADK_RES = " AND r.resname NOT LIKE '%.adk'";
	// Index table
	private final static String IDX_GET = "SELECT indexTable, indexId FROM bc_xmla_index_dict WHERE indexName = ?";
	private final static String IDX_GET_PROP = "SELECT jdbctype FROM bc_xmla_index_cols WHERE indexId = ? AND PropName = ?";
	
	
	private Connection connection;
	private ArrayList<String> resTypeList;
	private String apath;
	private String range;
	private String recursive;
	private String type;
	private String propertyName;
	private String indexName;
	private boolean doReturnProperty = false; // for ESA indexing tool
	private boolean del_only = false; // deliver only resources deleted in the original system
	private final boolean provideResourceData;
	private PreparedStatement selectChildColls;
	private final boolean provideNrOfHits;

	public ListMethod(Connection connection, HttpServletResponse response,
			HttpServletRequest request, ArrayList<String> resTypeList,
			String apath, String range, String recursive, String type,
			String index_name, String property_name, String provide_resourcedata, String provide_nrOfHits) throws IOException {
		this.connection = connection;
		this.resTypeList = new ArrayList<String>(resTypeList);
		this.resTypeList.add("ALL_XML");
		this.resTypeList.add("ADK");
		this.resTypeList.add("NON_ADK_BIN");
		this.response = response;
		this.apath = apath.toLowerCase();
		this.range = range;
		this.recursive = recursive;
		this.type = type;
		if ((index_name != null) && (property_name != null)) {
			this.doReturnProperty = true;
			this.indexName = index_name.trim().toLowerCase();
			this.propertyName = property_name.trim().toLowerCase();
		}

		if ("SAPArchivingConnector".equals(request.getHeader("User-Agent"))) {
			if ("Y".equalsIgnoreCase(request.getHeader("del_only")))
				this.del_only = true;
		}
		if(provide_resourcedata != null)
		{
			if("N".equalsIgnoreCase(provide_resourcedata))
			{
				if("RES".equalsIgnoreCase(this.range))
				{
					provideResourceData = false;
					doReturnProperty = false;
				}
				else
				{
					// "provideResourceData" ignored if "COL" or "ALL" is requested
					provideResourceData = true;
				}
			}
			else
			{
				provideResourceData = true;
			}
		}
		else
		{
			provideResourceData = true;
		}
		if(provide_nrOfHits != null)
		{
			if("Y".equalsIgnoreCase(provide_nrOfHits))
			{
				provideNrOfHits = true;
			}
			else
			{
				provideNrOfHits = false;
			}
		}
		else
		{
			provideNrOfHits = false;
		}
	}

	public boolean execute() throws IOException {

		CollectionData startColl = null;
		int intRange;
		boolean hasFilter = true;
		
		response.setContentType(MasterMethod.contentType);
		response.setHeader("service_message", "see message body");
		response.setHeader("has_resources", "N");// default value (may be overwritten below)
		BufferedWriter pwout = new BufferedWriter(new OutputStreamWriter(
				response.getOutputStream(), "UTF8"));

		// ------------------- Parameter checks
		// check if archive path exists and get the colId, otherwise return an
		// error
		if (apath == null || apath.equals("")) {
			this.reportStreamError(DasResponse.SC_PARAMETER_MISSING,
					"LIST: Archive path must be specified", pwout);
			return false;
		}

		if (!(this.apath.indexOf("//") == -1)
				|| !(this.apath.indexOf("\\") == -1)
				|| !this.apath.startsWith("/") || !this.apath.endsWith("/")) {
			this.reportStreamError(DasResponse.SC_PARAMETER_SYNTAX_INCORRECT,
					"LIST: Archive path " + this.apath
							+ " does not meet specifications", pwout);
			return false;
		}

		// check if range is ok
		if (this.range == null)
			this.range = "ALL";
		String rangeU = this.range.toUpperCase();
		if (!(rangeU.equals("ALL") || rangeU.equals("COL") || rangeU
				.equals("RES"))) {
			this
					.reportStreamError(
							DasResponse.SC_KEYWORD_UNKNOWN,
							"LIST: list_range "
									+ this.range
									+ " not supported in LIST; specify ALL, RES or COL",
							pwout);
			return false;
		}

		// set intRange for switch-case-statement later - not very nice
		if (rangeU.equals("ALL"))
			intRange = 1;
		else if (rangeU.equals("COL"))
			intRange = 2;
		else
			intRange = 3;

		// check recursive flag
		if (this.recursive == null)
			this.recursive = "N";
		if (!(this.recursive.equalsIgnoreCase("Y") || this.recursive
				.equalsIgnoreCase("N"))) {
			this.reportStreamError(DasResponse.SC_KEYWORD_UNKNOWN,
					"LIST: Recursive Setting " + this.recursive
							+ " not supported in LIST; specify Y or N", pwout);
			return false;
		}
		
		StringBuilder selectRes = null;
		try {
			// check if type is allowed and set hasFilter flag
			if(type != null) 
			{
				if(!resTypeList.contains(type.toUpperCase())) 
				{
					// type not supported
					reportStreamError(DasResponse.SC_RESOURCE_TYPE_NOT_SUPPORTED,	"LIST: Type " + type + " not supported; specify another type", pwout);
					return false;
				}
			} 
			else
			{
				hasFilter = false;
			}
			// create SELECT for resource retrieval
			if(doReturnProperty) 
			{
				// JOIN with index table required
				if(indexName.equals("") || propertyName.equals("")) 
				{
					reportStreamError(DasResponse.SC_PARAMETER_SYNTAX_INCORRECT, "LIST: If provided, neither index name nor property name may be blank", pwout);
					return false;
				}
				String indexTabName = getIndexTab(indexName, propertyName);
				selectRes = new StringBuilder(GET_RES_JOIN_PART_1)
																				.append(indexTabName)
																				.append(GET_RES_JOIN_PART_2)
																				.append(del_only ? AND_CLAUSE_JOIN_DELSTATUS : "");
			} 
			else 
			{
				selectRes = new StringBuilder(GET_RES_WHERE_COLID)
																				.append(del_only ? AND_CLAUSE_DELSTATUS : "");
			}
			if(hasFilter)
			{
				addTypeFilterClause(selectRes, doReturnProperty);
			}
			selectChildColls = connection.prepareStatement(GET_CHILD_COLLS);
			
		} catch (SQLException sqlex) {
			this.reportStreamError(DasResponse.SC_SQL_ERROR, "LIST: "
					+ sqlex.getMessage(), sqlex, pwout);
			return false;
		} catch (GeneralConflictException ex) {

			// $JL-EXC$
			this.reportStreamError(
					DasResponse.SC_WRONG_PROPERTY_OR_INDEX_IN_LIST, ex
							.getMessage(), pwout);
			return false;
		}

		// cut last slash if apath is not root ("/")
		String oldpath = apath;
		if (!"/".equals(apath))
			apath = apath.substring(0, apath.length() - 1);

		try {
			startColl = this.getColl(apath);
			if (startColl == null) {
				this.reportStreamError(DasResponse.SC_DOES_NOT_EXISTS,
						"LIST: Archive path " + oldpath + " does not exist",
						pwout);
				return false;
			}

			int nrOfHits = 0;
			boolean isRecursive = recursive.equalsIgnoreCase("Y");
			switch (intRange) {
			case 1:
				if(provideNrOfHits)
				{
					nrOfHits += getNumberOfSubCollections(isRecursive ? apath : null, isRecursive ? null : startColl.getcolId(), pwout);
					nrOfHits += getNumberOfSubResources(isRecursive ? apath : null, isRecursive ? null : startColl.getcolId(), del_only, hasFilter, pwout);
					response.setHeader("nr_of_hits", Integer.toString(nrOfHits));
				}
				executeRequest4ALL(selectRes.toString(), isRecursive, hasFilter, startColl, pwout);
				break;
			case 2:
				if(provideNrOfHits)
				{
					nrOfHits += getNumberOfSubCollections(isRecursive ? apath : null, isRecursive ? null : startColl.getcolId(), pwout);
					response.setHeader("nr_of_hits", Integer.toString(nrOfHits));
				}
				executeRequest4COL(isRecursive, startColl, pwout);
				break;
			case 3:
				if(provideNrOfHits)
				{
					nrOfHits += getNumberOfSubResources(isRecursive ? apath : null, isRecursive ? null : startColl.getcolId(), del_only, hasFilter, pwout);
					response.setHeader("nr_of_hits", Integer.toString(nrOfHits));
				}
				executeRequest4RES(selectRes.toString(), isRecursive, hasFilter, del_only, startColl, pwout);
				break;
			default:
				// can't be reached
				break;
			}
		} catch (SQLException sqlex) {
			this.reportStreamError(DasResponse.SC_SQL_ERROR, "LIST: "
					+ sqlex.getMessage(), sqlex, pwout);
			return false;
		}

		// Write status code and message at the end of the body
		this.writeStatus(pwout, HttpServletResponse.SC_OK, "Ok");
		this.cleanup(pwout);
		return true;
	}

	private CollectionData getColl(String collection) throws SQLException {
		CollectionData startColl = null;

		PreparedStatement pst4 = connection.prepareStatement(GET_COLL);
		try
		{
			pst4.setString(1, collection);
			ResultSet res4 = pst4.executeQuery();
			if(!res4.next())
			{
				return null;
			}
			else
			{
				startColl = new CollectionData(res4.getLong("colId"), res4
					.getString("URI"), res4.getString("creationUser"), res4
					.getTimestamp("creationTime"), res4.getString("colType"),
					res4.getString("frozen"));
			}
			res4.close();
			return startColl;
		}
		finally
		{
			if(pst4 != null)
			{
				pst4.close();	
			}
		}
	}

	private void writeCollection(CollectionData cdata, BufferedWriter pwout)	throws IOException {
		pwout.write(cdata.getColURI() + ";");
		pwout.write("COL" + ";");
		pwout.write(getUTCString(cdata.getCDate()) + ";");
		pwout.write(cdata.getCUser() + ";");
		pwout.write(cdata.getFrozen() + ";");
		pwout.write(cdata.getColType());
		// add an CR/LF platform independent
		pwout.write(13);
		pwout.write(10);
	}

	private void writeResources(final String sqlSelect, long colId, CollectionData coll, BufferedWriter pwout) throws IOException {
		PreparedStatement selectStmt = null;
		try {
			String propValue;
			selectStmt = connection.prepareStatement(sqlSelect);
			selectStmt.setLong(1, colId);
			ResultSet rs2 = selectStmt.executeQuery();
			while (rs2.next()) {
				pwout.write(coll.getColURI() + "/");
				pwout.write(rs2.getString("resName") + ";");
				pwout.write(rs2.getString("resType") + ";");
				pwout.write(getUTCString(rs2.getTimestamp("creationTime"))
						+ ";");
				pwout.write(rs2.getString("creationUser") + ";");
				pwout.write(rs2.getLong("resLength") + ";");
				pwout.write(rs2.getString("checkStatus") + ";");
				pwout.write(rs2.getString("isPacked"));
				if (doReturnProperty) {
					pwout.write(";");
					if ((propValue = rs2.getString(propertyName)) != null)
						pwout.write(propValue);
				}
				// add an CR/LF platform independent
				pwout.write(13);
				pwout.write(10);
			}
			rs2.close();
		} catch (SQLException sqlex) {
			this.reportStreamError(DasResponse.SC_SQL_ERROR, "LIST: "
					+ sqlex.getMessage(), sqlex, pwout);
		} finally {
			if(selectStmt != null)
			{
				try {
					selectStmt.close();
				} catch (SQLException sqlex) {
					cat.logThrowableT(Severity.WARNING, loc, "LIST: "	+ sqlex.getMessage(), sqlex);
				}
			}
		}
	}

	private int getNumberOfSubCollections(String parentCollUri, Long parentCollID, BufferedWriter pwout) throws IOException 
	{
		Statement selectStmt = null;
		ResultSet rs = null;
		try 
		{
			if(parentCollUri != null)
			{
				// recursive search
				selectStmt = connection.createStatement();
				rs = selectStmt.executeQuery(new StringBuilder(GET_COLL_COUNT_RECURSIVE).append("'").append(parentCollUri).append("%'").toString());
			}
			else if(parentCollID != null)
			{
				// non-recursive search
				selectStmt = connection.prepareStatement(GET_COLL_COUNT_NONRECURSIVE);
				((PreparedStatement)selectStmt).setLong(1, parentCollID);
				rs = ((PreparedStatement)selectStmt).executeQuery();
			}
			else
			{
				throw new IllegalArgumentException("Either \"parentCollUri\" or \"parentCollID\" must be non-null!");
			}
			if(rs.next())
			{
				return rs.getInt("rowcount");
			}
			return 0;
		} 
		catch(SQLException sqlex) 
		{
			reportStreamError(DasResponse.SC_SQL_ERROR, "LIST: " + sqlex.getMessage(), sqlex, pwout);
			return 0;
		}
		finally
  	{
  		if(rs != null)
  		{
  			try
  			{
  				rs.close();
  			}
  			catch(SQLException e)
  			{
  				cat.logThrowableT(Severity.WARNING, loc, "Closing a ResultSet failed", e);
  			}
  		}
  		if(selectStmt != null)
  		{
  			try
  			{
  				selectStmt.close();
  			}
  			catch(SQLException e)
  			{
  				cat.logThrowableT(Severity.WARNING, loc, "Closing a SQL Statement failed", e);
  			}
  		}
  	}
	}

	private int getNumberOfSubResources(String parentCollUri, Long parentCollID, final boolean onlyDeletedRes, final boolean isTypeFilterSet, BufferedWriter pwout) throws IOException 
	{
		PreparedStatement selectStmt = null;
		ResultSet rs = null;
		try 
		{
			if(parentCollUri != null)
			{
				// recursive search
				StringBuilder resourceSelect = new StringBuilder(GET_COUNT_RES_RECURSIVE)
																												.append("'").append(parentCollUri).append("%')")
																												.append(onlyDeletedRes ? AND_CLAUSE_DELSTATUS : "");
				if(isTypeFilterSet)
				{
					addTypeFilterClause(resourceSelect, false);
				}
				selectStmt = connection.prepareStatement(resourceSelect.toString());
			}
			else if(parentCollID != null)
			{
				// non-recursive search
				StringBuilder resourceSelect = new StringBuilder(GET_COUNT_RES_WHERE_COLID)
																												.append(onlyDeletedRes ? AND_CLAUSE_DELSTATUS : "");
				if(isTypeFilterSet)
				{
					addTypeFilterClause(resourceSelect, false);
				}
				selectStmt = connection.prepareStatement(resourceSelect.toString());
				selectStmt.setLong(1, parentCollID);
			}
			else
			{
				throw new IllegalArgumentException("Either \"parentCollUri\" or \"parentCollID\" must be non-null!");
			}	
			rs = selectStmt.executeQuery();
			if(rs.next())
			{
				return rs.getInt("rowcount");
			}
			return 0;
		} 
		catch(SQLException sqlex) 
		{
			reportStreamError(DasResponse.SC_SQL_ERROR, "LIST: " + sqlex.getMessage(), sqlex, pwout);
			return 0;
		}
		finally
  	{
  		if(rs != null)
  		{
  			try
  			{
  				rs.close();
  			}
  			catch(SQLException e)
  			{
  				cat.logThrowableT(Severity.WARNING, loc, "Closing a ResultSet failed", e);
  			}
  		}
  		if(selectStmt != null)
  		{
  			try
  			{
  				selectStmt.close();
  			}
  			catch(SQLException e)
  			{
  				cat.logThrowableT(Severity.WARNING, loc, "Closing a SQL Statement failed", e);
  			}
  		}
  	}
	}

	private HashMap<Long, CollectionData> getImmediateChildColls(long collId)
			throws SQLException {
		HashMap<Long, CollectionData> imColls = new HashMap<Long, CollectionData>();
		ResultSet res4;

		selectChildColls.setLong(1, collId);
		res4 = selectChildColls.executeQuery();
		while (res4.next())
			imColls.put(Long.valueOf(res4.getLong("colId")), new CollectionData(
					res4.getLong("colId"), res4.getString("URI"), res4
							.getString("creationUser"), res4
							.getTimestamp("creationTime"), res4
							.getString("colType"), res4.getString("frozen")));
		res4.close();

		return imColls;
	}

	public HashMap<Long, CollectionData> getAllChildColls(long collId)
			throws SQLException {
		HashMap<Long, CollectionData> inter = new HashMap<Long, CollectionData>();
		ResultSet res4;

		selectChildColls.setLong(1, collId);
		res4 = selectChildColls.executeQuery();
		while (res4.next())
			inter.put(Long.valueOf(res4.getLong("colId")), new CollectionData(res4
					.getLong("colId"), res4.getString("URI"), res4
					.getString("creationUser"), res4
					.getTimestamp("creationTime"), res4.getString("colType"),
					res4.getString("frozen")));
		res4.close();

		if (inter.isEmpty())
			return inter;
		else {
			// build an ArrayList with all key Values
			ArrayList<Object> atemp = new ArrayList<Object>();
			for (Long collID : inter.keySet())
				atemp.add(collID);
			// iterate over the ArrayList and call this method recursively for
			// each element
			for (int j = 0; j < atemp.size(); j++)
				inter.putAll(this.getAllChildColls(((Long) atemp.get(j))
						.longValue()));
		}
		return inter;
	}

	private void cleanup(BufferedWriter pwout) throws IOException {
		try {
			this.selectChildColls.close();
		} catch (SQLException sqlex) {
			cat.logThrowableT(Severity.WARNING, loc, "LIST: "
					+ sqlex.getMessage(), sqlex);
		}
		if (pwout != null) {
			pwout.flush();
			pwout.close();
		}
	}

	private String getIndexTab(String iName, String pName) throws SQLException,
			GeneralConflictException {
		// check if index exists
		PreparedStatement pst = this.connection.prepareStatement(IDX_GET);
		pst.setString(1, iName);
		ResultSet rs = pst.executeQuery();
		if (!rs.next()) {
			rs.close();
			throw new GeneralConflictException("LIST: Index " + iName
					+ " does not exist");
		}
		String iTab = rs.getString("indexTable");
		long indexId = rs.getLong("indexId");
		rs.close();
		pst.close();

		PreparedStatement pst1 = this.connection.prepareStatement(IDX_GET_PROP);
		pst1.setLong(1, indexId);
		pst1.setString(2, pName);
		ResultSet rs1 = pst1.executeQuery();
		if (!rs1.next()) {
			rs1.close();
			throw new GeneralConflictException("LIST: Index " + iName
					+ " does not contain property " + pName);
		}
		String jdbcType = rs1.getString("jdbcType");
		rs1.close();
		pst1.close();

		if (!jdbcType.toUpperCase().startsWith("VARCHAR"))
			throw new GeneralConflictException("LIST: Property " + pName
					+ " must be of JDBC type VARCHAR");

		return iTab;
	}
	
	private void executeRequest4ALL(final String sqlSelect, final boolean isRecursive, final boolean isTypeFilterSet, final CollectionData startColl, final BufferedWriter pwout)
	throws SQLException, IOException
	{
		// get all relevant collections - observe recursive flag
		HashMap<Long, CollectionData> allColls = null;
		if(isRecursive)
		{
			allColls = getAllChildColls(startColl.getcolId());
		}
		else
		{
			allColls = getImmediateChildColls(startColl.getcolId());
		}
		// print all resources belonging to the startColl
		writeResources(sqlSelect, startColl.getcolId(), startColl,	pwout);
		// print everything else
		for(Entry<Long, CollectionData> collEntry : allColls.entrySet())
		{
			// write each collection to the output stream
			writeCollection(collEntry.getValue(), pwout);
			// if recursive is specified, print all resources belonging to this collection
			if(isRecursive) 
			{
				writeResources(sqlSelect, collEntry.getKey(), collEntry.getValue(), pwout);
			}
		}
	}
	
	private void executeRequest4COL(final boolean isRecursive, final CollectionData startColl, final BufferedWriter pwout)
	throws SQLException, IOException
	{
		// get all relevant collections - observe recursive flag
		HashMap<Long, CollectionData> allColls = null;
		if(isRecursive)
		{
			allColls = getAllChildColls(startColl.getcolId());
		}
		else
		{
			allColls = getImmediateChildColls(startColl.getcolId());
		}
		for(CollectionData collData : allColls.values())
		{
			writeCollection(collData, pwout);
		}
	}
	
	private void executeRequest4RES(final String sqlSelect, final boolean isRecursive, final boolean isTypeFilterSet, final boolean deletedResOnly, final CollectionData startColl, final BufferedWriter pwout)
	throws SQLException, IOException
	{
		if(!provideResourceData)
		{
			//*** do not return meta data
			// check if there are resources below "startColl" 
			boolean hasResources = getNumberOfSubResources(null, Long.valueOf(startColl.getcolId()), deletedResOnly, isTypeFilterSet, pwout) > 0 ? true : false;
			if(!hasResources && isRecursive)
			{
				// no resource so far? -> check collections recursively until at least one resource is found
				hasResources = hasResourcesRecursively(startColl.getcolId(), deletedResOnly, pwout, isTypeFilterSet);
			}
			response.setHeader("has_resources", hasResources ? "Y" : "N");
		}
		else
		{
			//*** return meta data in response stream
			// print all resources belonging to the startColl
			writeResources(sqlSelect, startColl.getcolId(), startColl, pwout);
			// if recursive is specified, print everything else
			if(isRecursive)
			{
				HashMap<Long, CollectionData> allColls = getAllChildColls(startColl.getcolId());
				for(Entry<Long, CollectionData> collEntry : allColls.entrySet())
				{
					writeResources(sqlSelect, collEntry.getKey(), collEntry.getValue(), pwout);
				}
			}
		}
	}
	
	private boolean hasResourcesRecursively(final long startCollID, final boolean deletedResOnly, final BufferedWriter pwout, final boolean isTypeFilterSet) 
	throws SQLException, IOException
	{
		// get child collections
		HashSet<Long> inter = new HashSet<Long>();
		ResultSet res4;
		selectChildColls.setLong(1, startCollID);
		res4 = selectChildColls.executeQuery();
		while(res4.next())
		{
			inter.add(Long.valueOf(res4.getLong("colId")));
		}
		res4.close();

		if(inter.isEmpty())
		{
			return false;
		}
		
		for(Long childCollID : inter)
		{
			int nrOfResources = getNumberOfSubResources(null, Long.valueOf(childCollID.longValue()), deletedResOnly, isTypeFilterSet, pwout);
			if(nrOfResources > 0)
			{
				return true;
			}
			if(hasResourcesRecursively(childCollID.longValue(), deletedResOnly, pwout, isTypeFilterSet))
			{
				return true;
			}
		}
		return false;
	}
	
	private void addTypeFilterClause(final StringBuilder resourceSelect, final boolean isIdxTableJoin)
	{
		resourceSelect.append(isIdxTableJoin ? AND_CLAUSE_JOIN_RESTYPE : AND_CLAUSE_RESTYPE);
		// add type(s) to SQL select statement (IN clause)
		if("ALL_XML".equals(type))
		{
			resourceSelect.append("('XML','XSL','XSD')");
		}
		else if("ADK".equals(type))
		{
			resourceSelect.append("('BIN')").append(isIdxTableJoin ? AND_CLAUSE_JOIN_ADK_RES : AND_CLAUSE_ADK_RES);
		}
		else if("NON_ADK_BIN".equals(type))
		{
			resourceSelect.append("('BIN')").append(isIdxTableJoin ? AND_CLAUSE_JOIN_NON_ADK_RES : AND_CLAUSE_NON_ADK_RES);
		}
		else
		{
			resourceSelect.append("('").append(type).append("')");
		}
	}
}

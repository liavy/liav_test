package com.sap.archtech.daservice.commands;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sap.archtech.archconn.values.SelectClause;
import com.sap.archtech.archconn.values.SelectParam;
import com.sap.archtech.daservice.data.CollectionData;
import com.sap.archtech.daservice.data.DasResponse;
import com.sap.archtech.daservice.exceptions.BodyFormatException;
import com.sap.archtech.daservice.exceptions.MissingParameterException;
import com.sap.archtech.daservice.exceptions.NoSuchDBObjectException;
import com.sap.archtech.daservice.exceptions.WrongArgumentException;
import com.sap.guid.GUIDFormatException;
import com.sap.guid.GUIDGeneratorFactory;
import com.sap.guid.IGUIDGenerator;
import com.sap.tc.logging.Severity;

public class SelectMethod extends MasterMethod 
{
	private final static String GET_INDEX = "SELECT indexTable, indexid FROM BC_XMLA_INDEX_DICT WHERE indexName = ?";
	private final static String GET_COLL = "SELECT * FROM BC_XMLA_COL WHERE uri = ?";
	private final static String GET_CHILD_COLLS = "SELECT colId, uri, creationUser, creationTime, colType, frozen FROM BC_XMLA_COL WHERE parentColId = ?";
	private final static String SEL_IDX_COL = "SELECT propname FROM BC_XMLA_INDEX_COLS WHERE indexid = ?";
	
	private final static String INDEXPREFIX_MULTI = "BCGEN01_XMLA";
	private final static String RESERVED_SEPARATOR = "|";
	
	private final static IGUIDGenerator guidGen = GUIDGeneratorFactory.getInstance().createGUIDGenerator();
	
	private static enum ScenarioEnum
	{
		INDEX_COMPL,
		INDEX_WHERE,
		SYSTEM_WHERE,
		INDEX_SYSTEM_COMPL,
		INDEX_SYSTEM_WHERE;
	}
	
	private static Set<String> forbiddenWords = new HashSet<String>();
	static
	{
		forbiddenWords.add("where");
		forbiddenWords.add("select");
		forbiddenWords.add("union");
		forbiddenWords.add("join");
		forbiddenWords.add("insert");
		forbiddenWords.add("update");
		forbiddenWords.add("delete");
		forbiddenWords.add("create");
		forbiddenWords.add("alter");
		forbiddenWords.add("drop");
		forbiddenWords.add("truncate");
		forbiddenWords.add(";");// may be used in malicious code to separate SQL statements in INDEX_WHERE or SYSTEM_WHERE clause
		forbiddenWords.add(RESERVED_SEPARATOR);
	}

	private final Connection con;
	private final HttpServletRequest request;
	private String coll;
	private final String recursive;
	private final int maxhits;
	private final boolean isCalledByJavaArchConn;
	private final boolean del_only; // deliver only resources deleted in the original system

	public SelectMethod(Connection con, HttpServletResponse response,
			HttpServletRequest request, String coll, String recursive,
			String maxhits) throws IOException 
	{
		this.con = con;
		this.response = response;
		this.request = request;
		this.coll = coll;
		this.recursive = recursive == null ? "N" : recursive;
		
		if(maxhits != null)
		{
			this.maxhits = Integer.parseInt(maxhits);
		}
		else
		{
			this.maxhits = 0;
		}
		if("SAPArchivingConnector".equals(request.getHeader("User-Agent"))) 
		{
			this.isCalledByJavaArchConn = true;
			if ("Y".equalsIgnoreCase(request.getHeader("del_only")))
			{
				this.del_only = true;
			}
			else
			{
				this.del_only = false;
			}
		}
		else
		{
			this.isCalledByJavaArchConn = false;
			this.del_only = false;
		}
	}

	public boolean execute() throws IOException 
	{
		response.setContentType(MasterMethod.contentType);
		response.setHeader("service_message", "see message body");

		BufferedWriter bwout = new BufferedWriter(new OutputStreamWriter(response.getOutputStream(), "UTF8"));
		PreparedStatement getChildColls = null;
		PreparedStatement finalQuery = null;
		try 
		{
			CollectionData startColl = checkParams();
			long startCollID = startColl.getcolId();
			getChildColls = con.prepareStatement(GET_CHILD_COLLS);
			// parse request body
			// cut the last slash in the collection string - relevant for later output
			// note: this has to be done AFTER the checkParams() call
			if (!"/".equals(coll))
			{
				coll = coll.substring(0, coll.length() - 1).toLowerCase();
			}
			SelectClause selectClause = null;
			if(isCalledByJavaArchConn)
			{
				selectClause = parseJavaSelectBody();
			}
			else
			{
				selectClause = parseABAPSelectBody();
			}
			// check selectClause if it contains malicious code (SQL injection)
			checkSelectClause(selectClause);
			// get the "real" index name
			ScenarioEnum scenario = getScenario(selectClause);
			String tcIndexName = null;
			long[] indexId = new long[1];
			if(scenario != ScenarioEnum.SYSTEM_WHERE)
			{
				tcIndexName = getTechnicalIndexName(selectClause.getIndexname(), indexId);
			}
			// create the SQL Statement
			finalQuery = calculateStatement(scenario, selectClause, tcIndexName, indexId[0]);
			// output results
			if(recursive.equalsIgnoreCase("N"))
			{
				printResOfCol(startCollID, new CollectionData(startCollID, coll, null, null, null, null), finalQuery,	bwout);
			}
			else 
			{
				// calculate all relevant collections
				HashMap<Long, CollectionData> colMap = new HashMap<Long, CollectionData>();
				getAllChildColls(colMap, startCollID, getChildColls);
				// add the start Collection
				colMap.put(startCollID, new CollectionData(startCollID, coll, null, null, null, null));
				// calculate all qualified resources for all collections
				for(Entry<Long, CollectionData> colMapEntry : colMap.entrySet()) 
				{
					printResOfCol(colMapEntry.getKey(), colMapEntry.getValue(), finalQuery, bwout);
				}
			}

			writeStatus(bwout, HttpServletResponse.SC_OK, "Ok");
			bwout.flush();
			
			return true;
		} 
		catch(SQLException sqlex) 
		{
			reportStreamError(DasResponse.SC_SQL_ERROR, "SELECT: "	+ sqlex.getMessage(), sqlex, bwout);
			return false;
		} 
		catch(WrongArgumentException waex) 
		{
			reportStreamError(DasResponse.SC_PARAMETER_SYNTAX_INCORRECT, "SELECT: " + waex.getMessage(), waex, bwout);
			return false;
		} 
		catch(NoSuchDBObjectException nsex) 
		{
			reportStreamError(DasResponse.SC_DOES_NOT_EXISTS, "SELECT: " + nsex.getMessage(), nsex, bwout);
			return false;
		} 
		catch(MissingParameterException mpex) 
		{
			reportStreamError(DasResponse.SC_PARAMETER_MISSING, "SELECT: " + mpex.getMessage(), mpex, bwout);
			return false;
		} 
		catch(BodyFormatException bfex) 
		{
			reportStreamError(DasResponse.SC_BODY_FORMAT_CORRUPT, "SELECT: " + bfex.getMessage(), bfex, bwout);
			return false;
		} 
		catch(ClassNotFoundException cnfex) 
		{
			reportStreamError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "SELECT: " + cnfex.getMessage(), cnfex, bwout);
			return false;
		} 
		finally 
		{
			if(getChildColls != null)
			{
				try
				{
					getChildColls.close();
				}
				catch(SQLException sqlex) 
				{
					cat.logThrowableT(Severity.WARNING, loc, "SELECT: "	+ sqlex.getMessage(), sqlex);
				}
			}
			if(finalQuery != null)
			{
				try
				{
					finalQuery.close();
				}
				catch(SQLException sqlex) 
				{
					cat.logThrowableT(Severity.WARNING, loc, "SELECT: "	+ sqlex.getMessage(), sqlex);
				}
			}
			if(bwout != null)
			{
				bwout.close();
			}
		}
	}

	private CollectionData checkParams() throws WrongArgumentException, SQLException,	NoSuchDBObjectException, MissingParameterException 
	{
		// check recursive flag
		if(!(recursive.equalsIgnoreCase("Y") || recursive.equalsIgnoreCase("N"))) 
		{
			throw new MissingParameterException("Recursive setting " + recursive + " not supported in SELECT; specify Y or N");
		}
		
		PreparedStatement getColl = null;
		try
		{
			CollectionData coldat = null;
			// check collection
			getColl = con.prepareStatement(GET_COLL);
			coldat = checkCollection(coll, getColl);
			return coldat;
		}
		finally
		{
			if(getColl != null)
			{
				try
				{
					getColl.close();
				}
				catch(SQLException sqlex) 
				{
					cat.logThrowableT(Severity.WARNING, loc, "SELECT: "	+ sqlex.getMessage(), sqlex);
				}
			}
		}
	}

	private void printResOfCol(final long colId, final CollectionData coll, final PreparedStatement finalQuery, final BufferedWriter pwout) throws IOException 
	{
		ResultSet rs1 = null;
		try 
		{
			finalQuery.setLong(1, colId);
			if(maxhits > 0)
			{
				finalQuery.setMaxRows(maxhits);
			}

			rs1 = finalQuery.executeQuery();
			while(rs1.next()) 
			{
				pwout.write(coll.getColURI() + "/");
				pwout.write(rs1.getString("resName") + ";");
				pwout.write(rs1.getString("resType") + ";");
				pwout.write(getUTCString(rs1.getTimestamp("creationTime")) + ";");
				pwout.write(rs1.getString("creationUser") + ";");
				pwout.write(rs1.getLong("resLength") + ";");
				pwout.write(rs1.getString("checkStatus") + ";");
				pwout.write(rs1.getString("isPacked"));
				// add an CR/LF platform independent
				pwout.write(13);
				pwout.write(10);
			}
		} 
		catch(SQLException sqlex) 
		{
			reportStreamError(DasResponse.SC_SQL_ERROR, "SELECT: " + sqlex.getMessage(), sqlex, pwout);
		}
		finally
		{
			if(rs1 != null)
			{
				try
				{
					rs1.close();
				}
				catch(SQLException e)
				{
					cat.logThrowableT(Severity.WARNING, loc, "SELECT: "	+ e.getMessage(), e);
				}
			}
		}
	}

	private void getAllChildColls(final HashMap<Long, CollectionData> colMap, final long parentCollID,	final PreparedStatement getChildColls) throws SQLException 
	{
		ResultSet result = null;
		HashSet<Long> collIdSet = new HashSet<Long>();
		try
		{
			getChildColls.setLong(1, parentCollID);
			result = getChildColls.executeQuery();
			Long childCollID = null;
			while(result.next())
			{
				childCollID = result.getLong("colId");
				collIdSet.add(childCollID);
				colMap.put(childCollID, new CollectionData(
					childCollID, result.getString("URI"), result.getString("creationUser"), 
					result.getTimestamp("creationTime"), result.getString("colType"), result.getString("frozen")));
			}
		}
		finally
		{
			if(result != null)
			{
				try
				{
					result.close();
				}
				catch(SQLException e)
				{
					cat.logThrowableT(Severity.WARNING, loc, "SELECT: "	+ e.getMessage(), e);
				}
			}
		}
		
		if(collIdSet.isEmpty())
		{
			// no result for the given "startCollID"
			return;
		}
		else 
		{
			// call this method recursively for each collection ID 
			for(Long nextParentCollID : collIdSet)
			{
				getAllChildColls(colMap, nextParentCollID, getChildColls);
			}
		}
	}

	private SelectClause parseABAPSelectBody() throws IOException, BodyFormatException 
	{
		BufferedReader br = null;
		try 
		{
			br = new BufferedReader(new InputStreamReader(request.getInputStream(), "UTF-8"));
			// 1. read everything line by line in an ArrayList
			ArrayList<String> givenSelectBody = new ArrayList<String>();
			String line = null;
			while((line = br.readLine()) != null)
			{
				givenSelectBody.add(line);
			}
			// 2. Determine if a parameter-part is attached (new version)
			int nrOfFirstParamLine = 0;
			boolean isParamListAttached = false;
			for(String selectBodyLine : givenSelectBody)
			{
				nrOfFirstParamLine++;
				if(selectBodyLine.length() == 0)
				{
					// see spec: the parameter list must be headed by an empty line in the request stream
					isParamListAttached = true;
					break;
				}
			}
			// 3. Determine selection clause
			SelectClause selectClause = null;
			int selectBodySizeWithoutParams = isParamListAttached ? nrOfFirstParamLine - 1 : givenSelectBody.size();
			switch(selectBodySizeWithoutParams)
			{
			case 1:
				selectClause = new SelectClause(getIndexName(givenSelectBody.get(0).trim()), null, null);
				break;

			case 2:
				if(givenSelectBody.get(0).startsWith("INDEX"))
				{
					selectClause = new SelectClause(getIndexName(givenSelectBody.get(0).trim()), givenSelectBody.get(1), null);
				}
				else if(givenSelectBody.get(0).startsWith("SYSTEM"))
				{
					selectClause = new SelectClause(null, null, givenSelectBody.get(1));
				}
				else
				{
					throw new BodyFormatException("Specification of SELECT-request wrong, no SelectClause can be determined");
				}
				break;

			case 3:
				selectClause = new SelectClause(getIndexName(givenSelectBody.get(0).trim()), null, givenSelectBody.get(2));
				break;

			case 4:
				selectClause = new SelectClause(getIndexName(givenSelectBody.get(0).trim()), givenSelectBody.get(1), givenSelectBody.get(3));
				break;

			default:
				throw new BodyFormatException("Specification of SELECT-request wrong, no SelectClause can be determined");
			}
			// 4. add param type and value (SelectParam)
			if(isParamListAttached)
			{
				String paramLine = null;
				String paramType = null;
				String paramValue = null;
				int paramdelimiter = 0;
				for(int i = nrOfFirstParamLine; i < givenSelectBody.size(); i++) 
				{
					paramLine = givenSelectBody.get(i);
					paramdelimiter = paramLine.indexOf("=");
					paramType = paramLine.substring(0, paramdelimiter).toLowerCase();
					paramValue = paramLine.substring(paramdelimiter + 1, paramLine.length());
					if(paramdelimiter == -1)
					{	
						throw new BodyFormatException("Error parsing parameter list. No '=' found.");
					}
					if("string".equals(paramType))
					{
						selectClause.setString(i - nrOfFirstParamLine + 1, paramValue);
					}
					else if("short".equals(paramType))
					{
						selectClause.setShort(i - nrOfFirstParamLine + 1, Short.valueOf(paramValue));
					}
					else if("int".equals(paramType))
					{
						selectClause.setInt(i - nrOfFirstParamLine + 1, Integer.valueOf(paramValue));
					}
					else if("long".equals(paramType))
					{
						selectClause.setLong(i - nrOfFirstParamLine + 1 , Long.valueOf(paramValue));
					}
					else if("float".equals(paramType))
					{
						selectClause.setFloat(i - nrOfFirstParamLine + 1, Float.valueOf(paramValue));
					}
					else if("double".equals(paramType))
					{
						selectClause.setDouble(i - nrOfFirstParamLine + 1, Double.valueOf(paramValue));
					}
					else if("timestamp".equals(paramType)) 
					{
						try 
						{
							selectClause.setTimestamp(i - nrOfFirstParamLine + 1, new Timestamp(sdf.parse((String)paramValue).getTime()));
						} 
						catch(ParseException paex) 
						{
							throw new BodyFormatException("Problem parsing date string");
						}
					} 
					else if("binary".equals(paramType))
					{
						try
						{
							selectClause.setGuid(i - nrOfFirstParamLine + 1, guidGen.parseHexGUID(paramValue));
						}
						catch(GUIDFormatException e)
						{
							throw new BodyFormatException("Problem parsing GUID string: " + e.getMessage());
						}
					}
					else
					{
						throw new BodyFormatException("Type " + paramType	+ " not supported!");
					}	
				}
			}
			
			return selectClause;
		} 
		finally 
		{
			if(br != null) 
			{
				br.close();
			}
		}
	}

	private SelectClause parseJavaSelectBody() throws IOException, ClassNotFoundException, BodyFormatException 
	{
		// get SelectClause instance from request stream
		ObjectInputStream ois = null;
		try
		{
			ois = new ObjectInputStream(request.getInputStream());
			return (SelectClause)ois.readObject();
		}
		finally
		{
			if(ois != null)
			{
				ois.close();
			}
		}
	}

	private ScenarioEnum getScenario(final SelectClause selectClause) throws BodyFormatException
	{
		String indexName = selectClause.getIndexname();
		String indexWhere = selectClause.getIndexclause();
		String systemWhere = selectClause.getSystemclause();
		ScenarioEnum scenario = null;
		if(indexName != null)
		{
			if(indexWhere != null)
			{
				if(systemWhere != null)
				{
					scenario = ScenarioEnum.INDEX_SYSTEM_WHERE;
				}
				else
				{
					scenario = ScenarioEnum.INDEX_WHERE;
				}
			}
			else
			{
				if(systemWhere != null)
				{
					scenario = ScenarioEnum.INDEX_SYSTEM_COMPL;
				}
				else
				{
					scenario = ScenarioEnum.INDEX_COMPL;
				}
			}
		}
		else
		{
			if(indexWhere == null)
			{
				if(systemWhere != null)
				{
					scenario = ScenarioEnum.SYSTEM_WHERE;
				}
			}
		}
		if(scenario == null)
		{
			throw new BodyFormatException("Specification of SELECT-request wrong, no scenario can be determined");
		}
		return scenario;		
	}
	
	private String getIndexName(final String indexNameLine) throws BodyFormatException 
	{
		int delim = indexNameLine.indexOf(" ");
		if(delim == -1)
		{
			throw new BodyFormatException("Index name is missing");
		}
		return indexNameLine.substring(delim + 1, indexNameLine.length());
	}

	private String getTechnicalIndexName(final String indexName, final long[] outParam_IndexId) throws SQLException, NoSuchDBObjectException 
	{
		PreparedStatement getIndex = null;
		ResultSet result = null;
		try
		{
			getIndex = con.prepareStatement(GET_INDEX);
			getIndex.setString(1, indexName.trim().toLowerCase());
			result = getIndex.executeQuery();
			if(!result.next())
			{
				throw new NoSuchDBObjectException("Index " + indexName + " does not exist");
			}
			String tcindex = result.getString("indexTable");
			outParam_IndexId[0] = result.getLong("indexid");
			return tcindex;
		}
		finally
		{
			if(result != null)
			{
				try
				{
					result.close();
				}
				catch(SQLException e)
				{
					cat.logThrowableT(Severity.WARNING, loc, "SELECT: "	+ e.getMessage(), e);
				}
			}
			if(getIndex != null)
			{
				try
				{
					getIndex.close();
				}
				catch(SQLException e)
				{
					cat.logThrowableT(Severity.WARNING, loc, "SELECT: "	+ e.getMessage(), e);
				}
			}
		}
	}

	private PreparedStatement calculateStatement(final ScenarioEnum scenario, final SelectClause selectClause, final String tcIndexName, final long indexId)
	throws SQLException, BodyFormatException, IOException 
	{
		StringBuilder sttext = null;
		String indexWhereClause = null;
		boolean isMultiValSupported = tcIndexName != null ? tcIndexName.startsWith(INDEXPREFIX_MULTI) : false;
		switch(scenario) 
		{
		case INDEX_COMPL:
			sttext = new StringBuilder("SELECT DISTINCT * FROM BC_XMLA_RES r, ")
				.append(tcIndexName)
				.append(" i WHERE r.colId = ? AND r.resId = i.resId");
			if(del_only)
			{
				sttext.append(" AND r.delstatus = 'Y'");
			}
			break;
		case INDEX_WHERE:
			indexWhereClause = selectClause.getIndexclause();
			if(isMultiValSupported)
			{
				ArrayList<String> columnNames = getIndexPropertyNames(indexId, tcIndexName);
				indexWhereClause = extendIndexWhereClause(indexWhereClause, columnNames);
			}
			sttext = new StringBuilder("SELECT DISTINCT * FROM BC_XMLA_RES WHERE colId = ? AND resId IN (SELECT DISTINCT resId FROM ")
				.append(tcIndexName)
				.append(" WHERE ")
				.append(indexWhereClause)
				.append(')');
			if(del_only)
			{
				sttext.append(" AND delstatus = 'Y'");
			}
			break;
		case SYSTEM_WHERE:
			sttext = new StringBuilder("SELECT DISTINCT * FROM BC_XMLA_RES WHERE colId = ? AND (")
				.append(selectClause.getSystemclause())
				.append(')');
			if(del_only)
			{
				sttext.append(" AND delstatus = 'Y'");
			}
			break;
		case INDEX_SYSTEM_COMPL:
			sttext = new StringBuilder("SELECT DISTINCT * FROM BC_XMLA_RES r, ")
				.append(tcIndexName)
				.append(" i WHERE r.colId = ? AND r.resId = i.resId AND (")
				.append(selectClause.getSystemclause())
				.append(')');
			if(del_only)
			{
				sttext.append(" AND r.delstatus = 'Y'");
			}
			break;
		case INDEX_SYSTEM_WHERE:
			indexWhereClause = selectClause.getIndexclause();
			if(isMultiValSupported)
			{
				ArrayList<String> columnNames = getIndexPropertyNames(indexId, tcIndexName);
				indexWhereClause = extendIndexWhereClause(indexWhereClause, columnNames);
			}
			sttext = new StringBuilder("SELECT DISTINCT * FROM BC_XMLA_RES WHERE colId = ? AND resId IN (SELECT DISTINCT resId FROM ")
				.append(tcIndexName)
				.append(" WHERE ")
				.append(indexWhereClause)
				.append(") AND (")
				.append(selectClause.getSystemclause())
				.append(')');
			if(del_only)
			{
				sttext.append(" AND delstatus = 'Y'");
			}
			break;
		default:
			String errMsg = "SELECT: invalid select scenario " + scenario.toString();
			cat.errorT(loc,	errMsg);
			throw new BodyFormatException(errMsg);
		}
		loc.infoT(sttext.toString());
		
		PreparedStatement fps = null;
		// Note: The statement is created from a non-constant string; hence, it is vulnerable for an SQL injection attack
		// Therefore, the statement is checked in an subsequent step (see "checkCalculatedSqlStatement()" method)
		fps = con.prepareStatement(sttext.toString());
		int statementParamPos = 2;// first position ("colId") will be set later
		TreeSet<SelectParam> paramlist = selectClause.getParamlist();
		for(SelectParam slpm : paramlist) 
		{
			setStatementParams(fps, slpm, statementParamPos);
			statementParamPos++;
		}
		return fps;
	}

	private void setStatementParams(final PreparedStatement fps, final SelectParam slpm, final int statementParamPos) 
	throws SQLException, BodyFormatException 
	{
		String paramType = slpm.getType();
		Object paramValue = slpm.getValue();
		if("binary".equals(paramType))
		{
			try
			{
				fps.setBytes(statementParamPos, guidGen.parseHexGUID((String)paramValue).toBytes());
			}
			catch(GUIDFormatException e)
			{
				throw new BodyFormatException("Problem parsing GUID string: " + e.getMessage());
			}
		}
		else
		{
			fps.setObject(statementParamPos, paramValue);
		}
	}
	
	private void checkSelectClause(final SelectClause selectClause) throws SQLException
	{
		String indexWhere = selectClause.getIndexclause();
		String systemWhere = selectClause.getSystemclause();
		for(String forbiddenWord : forbiddenWords)
		{
			if(indexWhere != null)
			{
				if(indexWhere.toLowerCase().contains(forbiddenWord))
				{
					throw new SQLException("The index WHERE clause \"" + indexWhere + "\" contains the forbidden word \"" + forbiddenWord + "\"");
				}
			}
			if(systemWhere != null)
			{
				if(systemWhere.toLowerCase().contains(forbiddenWord))
				{
					throw new SQLException("The system WHERE clause \"" + systemWhere + "\" contains the forbidden word \"" + forbiddenWord + "\"");
				}

			}
		}
	}
	
	private String extendIndexWhereClause(final String origIndexWhereClause, final ArrayList<String> columnNames)
	{
		// copy original WHERE clause and replace "and" and "or" by "|"
		// This approach assumes:
		// 1) only "and" and "or" are used as SQL conjunctions
		// 2) "|" is never contained in any WHERE clause
		String tmp = origIndexWhereClause.toLowerCase();
		tmp = tmp.replace(" and ", RESERVED_SEPARATOR);
		tmp = tmp.replace(" or ", RESERVED_SEPARATOR);
		// split in tokens
		StringTokenizer tokenizer = new StringTokenizer(tmp, RESERVED_SEPARATOR);
		String origPart = null;
		StringBuilder modifiedPart = null;
		String propName = null;
		HashMap<String, String> modified2origParts = new HashMap<String, String>();
		int posOfColName = 0;
		int lastPosOfColName = 0;
		while(tokenizer.hasMoreTokens())
		{
			origPart = tokenizer.nextToken().trim();
			modifiedPart = new StringBuilder(origPart);
			// check which of the columnNames is contained in the given part
			// Note: both sides of the SQL clause "origPart" may contain columnNames (e.g. "col_1 <> col_2")
			// => if so, do not modify the "origPart"
			propName = null;
			lastPosOfColName = -1;
			for(String colName : columnNames)
			{
				posOfColName = modifiedPart.indexOf(colName);
				if(posOfColName != -1)
				{
					// colName found
					if(lastPosOfColName == -1)
					{
						// first colName found in this origPart 
						propName = colName;
						lastPosOfColName = posOfColName;
					}
					else
					{
						// colName has been found before in this orgPart
						// -> do not modify this origPart
						propName = null;
						break;
					}
				}
			}
			if(propName != null)
			{
				// check if "is null" or "is not null" is contained -> if not, add "is not null"
				if(modifiedPart.indexOf("is null") == -1 && modifiedPart.indexOf("is not null") == -1)
				{
					modifiedPart.insert(lastPosOfColName, new StringBuilder(propName).append(" is not null and ").toString());
					modifiedPart.insert(0, '(');
					modifiedPart.append(')');
					modified2origParts.put(origPart, modifiedPart.toString());
				}
			}
		}
		// replace original parts by their modifications
		String result = origIndexWhereClause.toLowerCase();
		for(Entry<String, String> entry : modified2origParts.entrySet())
		{
			result = result.replace(entry.getKey(), entry.getValue());
		}
		return result;
	}
	
	private ArrayList<String> getIndexPropertyNames(final long indexId, final String indexTable) throws SQLException, IOException
	{
		// get index property meta data
		PreparedStatement selIdxCol = null;
		ResultSet result = null;
		ArrayList<String> propNames = new ArrayList<String>();
		try
		{
			selIdxCol = con.prepareStatement(SEL_IDX_COL);
			selIdxCol.setLong(1, indexId);
			result = selIdxCol.executeQuery();
			int nrOfProperties = 0;
			while(result.next()) 
			{
				propNames.add(result.getString("propname").toLowerCase());
				nrOfProperties++;
			}
			if(nrOfProperties == 0) 
			{
				reportError(DasResponse.SC_DOES_NOT_EXISTS,	"SELECT: No entry for index table "	+ indexTable + " in table BC_XMLA_INDEX_COLS");
			}
			return propNames;
		}
		finally
		{
			if(result != null)
			{
				try
				{
					result.close();
				}
				catch(SQLException sqlex)
				{
					cat.logThrowableT(Severity.WARNING, loc, "SELECT: " + sqlex.getMessage(), sqlex);
				}
			}
			if(selIdxCol != null)
			{
				try
				{
					selIdxCol.close();
				}
				catch(SQLException sqlex)
				{
					cat.logThrowableT(Severity.WARNING, loc, "INDEX INSERT: " + sqlex.getMessage(), sqlex);
				}
			}
		}
	}
}
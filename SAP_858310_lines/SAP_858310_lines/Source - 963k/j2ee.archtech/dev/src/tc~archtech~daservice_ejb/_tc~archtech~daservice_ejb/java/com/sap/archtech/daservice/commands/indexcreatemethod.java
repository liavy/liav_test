package com.sap.archtech.daservice.commands;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.sap.archtech.daservice.data.ColumnData;
import com.sap.archtech.daservice.data.DasResponse;
import com.sap.archtech.daservice.exceptions.ConfigInconsistentException;
import com.sap.archtech.daservice.exceptions.GeneralConflictException;
import com.sap.archtech.daservice.exceptions.MissingParameterException;
import com.sap.archtech.daservice.exceptions.PropertyForbiddenException;
import com.sap.archtech.daservice.exceptions.WrongArgumentException;
import com.sap.archtech.daservice.util.IdProvider;
import com.sap.dictionary.database.dbs.JddException;
import com.sap.dictionary.database.opentools.DbTableOpenTools;
import com.sap.dictionary.database.opentools.OpenTools;
import com.sap.guid.IGUID;
import com.sap.tc.logging.Severity;

public class IndexCreateMethod extends MasterMethod 
{
	private final static int MAXVARCHARSIZE = 339;
	private final static String MAXNNUMBER = "000000";
	private final static String INDEXPREFIX = "BCGEN00_XMLA";
	private final static String INDEXPREFIX_MULTI = "BCGEN01_XMLA";
	private final static String SECONDARYINDEXPREFIX = "BCGEN00_XML";
	private final static String SECONDARYINDEXPREFIX_MULTI = "BCGEN01_XML";
	private final static String INS_DICT = "INSERT INTO BC_XMLA_INDEX_DICT(indexId, indexName, indexTable, hasMANDT) VALUES (?, ?, ?, ?)";
	private final static String INS_COLS = "INSERT INTO BC_XMLA_INDEX_COLS(indexColId, propName, jdbcType, vlength, indexId) VALUES (?, ?, ?, ?, ?)";
	private final static String GET_IDX = "SELECT * FROM BC_XMLA_INDEX_DICT WHERE indexName = ?";
	private final static String INS_MAXIDS = "INSERT INTO BC_XMLA_MAXIDS(tablename, maxid) VALUES (?, ?)";
	
	private final static HashMap<String, String> typeConversionMap = new HashMap<String, String>();
	static
	{
		typeConversionMap.put("VARCHAR", "string");
		typeConversionMap.put("SMALLINT", "short");
		typeConversionMap.put("INTEGER", "integer");
		typeConversionMap.put("BIGINT", "long");
		typeConversionMap.put("REAL", "float");
		typeConversionMap.put("DOUBLE", "double");
		typeConversionMap.put("TIMESTAMP", "timestamp");
		typeConversionMap.put("BINARY", "binary");
	}
	
	private static final String SECINDEX_ALIAS_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	
	private final Connection connection;
	private final HttpServletRequest request;
	private final String index_name;
	private final IdProvider idProv;
	private final HashMap<String, Integer> dbColumnLimits;
	private final String multiValSupport;
	private final HashSet<String> nocNames;
	
	private String hasMandt;
	// outer map: key = alias of secondary index, value = inner map (sorted map with key = position in secondary index, value = column name)
	private HashMap<Character, TreeMap<Integer, String>> secondaryIndexDefinition;
	
	public IndexCreateMethod(Connection con, IdProvider idProv,
			HttpServletResponse response, HttpServletRequest request,
			String index_name, HashMap<String, Integer> dbColumnLimits, String multiValSupport) 
	{
		this.connection = con;
		this.response = response;
		this.request = request;
		this.index_name = index_name == null ? "" : index_name.trim().toLowerCase();
		this.idProv = idProv;
		this.dbColumnLimits = dbColumnLimits;
		secondaryIndexDefinition = null;//lazily initialized
		hasMandt = "N";
		this.multiValSupport = multiValSupport == null ? "" : multiValSupport.trim().toUpperCase();
		nocNames = new HashSet<String>();
		nocNames.add("RESID");
		nocNames.add("RESNAME");
		nocNames.add("RESTYPE");
		nocNames.add("RESLENGTH");
		nocNames.add("CREATIONTIME");
		nocNames.add("CREATIONUSER");
		nocNames.add("PACKNAME");
		nocNames.add("OFFSET");
		nocNames.add("PACKLENGTH");
		nocNames.add("PACKTIME");
		nocNames.add("PACKUSER");
		nocNames.add("CHECKSTATUS");
		nocNames.add("ISPACKED");
		nocNames.add("FINGERPRINT");
		nocNames.add("COLID");
		if("Y".equals(this.multiValSupport))
		{
			nocNames.add("COUNTER");
		}
	}

	public boolean execute() throws IOException 
	{
		response.setContentType("text/xml");

		PreparedStatement getIdx = null;
		try 
		{
			// check parameters syntactically
			checkParams();
			// parse body
			boolean isMultiValSupported = "Y".equals(multiValSupport);
			ArrayList<ColumnData> columnData = parseRequestBody(isMultiValSupported);
			// check if index name exists
			getIdx = connection.prepareStatement(GET_IDX);
			getIdx.setString(1, index_name);
			checkIndexExists(getIdx);
			// generate index table name
			long indexDictId = idProv.getId("BC_XMLA_INDEX_DICT");
			StringBuilder tableNameBuf = new StringBuilder(MAXNNUMBER).append(indexDictId);
			String indexSuffix = tableNameBuf.substring(tableNameBuf.length()- MAXNNUMBER.length(), tableNameBuf.length());
			String tableName = new StringBuilder(isMultiValSupported ? INDEXPREFIX_MULTI : INDEXPREFIX).append(indexSuffix).toString();
			// generate XML-description of the index table
			Document indexTableXML = generateTableXML(tableName, indexSuffix, columnData, isMultiValSupported);
			// serialize table generation XML
			ByteArrayOutputStream bos = serializeIndexTableXML(indexTableXML);
			// create table with JDDIC
			createTable(tableName, new ByteArrayInputStream(bos.toByteArray()));
			// update index dictionary
			updateIndexDict(indexDictId, tableName);
			// update index_cols table
			updateIndexCols(indexDictId, columnData);
			if(isMultiValSupported)
			{
				// update MAXIDS table
				updateMaxIdsTable(tableName);
			}
			// set response header
			response.setHeader("service_message", "Ok");
			return true;
		} 
		catch(ConfigInconsistentException coinex) 
		{
			reportError(DasResponse.SC_CONFIG_INCONSISTENT, "INDEX CREATE: " + coinex.getMessage(), coinex);
			return false;
		} 
		catch(MissingParameterException msex)
		{
			reportError(DasResponse.SC_PARAMETER_MISSING, "INDEX CREATE: " + msex.getMessage(), msex);
			return false;
		} 
		catch(IllegalArgumentException ilex) 
		{
			reportError(DasResponse.SC_PARAMETER_SYNTAX_INCORRECT, "INDEX CREATE: " + ilex.getMessage(), ilex);
			return false;
		} 
		catch(PropertyForbiddenException pfex) 
		{
			reportError(DasResponse.SC_PROPERTY_NAME_FORBIDDEN, "INDEX CREATE: " + pfex.getMessage(), pfex);
			return false;
		}
		catch(GeneralConflictException gcex) 
		{
			reportInfo(HttpServletResponse.SC_CONFLICT, "INDEX CREATE: " + gcex.getMessage());
			return false;
		} 
		catch(JddException jddex) 
		{
			reportError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INDEX CREATE: " + jddex.getMessage(), jddex);
			return false;
		} 
		catch(WrongArgumentException waex) 
		{
			reportError(DasResponse.SC_PARAMETER_SYNTAX_INCORRECT, "INDEX CREATE: " + waex.getMessage(), waex);
			return false;
		} 
		catch(IOException ioex) 
		{
			reportError(DasResponse.SC_IO_ERROR, "INDEX CREATE: "	+ ioex.getMessage(), ioex);
			return false;
		} 
		catch(SQLException sqlex) 
		{
			// For parallel running IndexCreate commands: Do an additional check
			// and test if the index already exists (created by someone else in
			// the meantime)
			try 
			{
				getIdx.setString(1, index_name);
				checkIndexExists(getIdx);
				reportError(DasResponse.SC_SQL_ERROR, "INDEX CREATE: "	+ sqlex.getMessage(), sqlex);
			} 
			catch(SQLException sqlex2) 
			{
				reportError(DasResponse.SC_SQL_ERROR, "INDEX CREATE: "	+ sqlex2.getMessage(), sqlex2);
			} 
			catch(GeneralConflictException gcex2) 
			{
				reportInfo(HttpServletResponse.SC_CONFLICT, "INDEX CREATE: " + gcex2.getMessage());
			}
			return false;
		} 
		finally 
		{
			if(getIdx != null)
			{
				try
				{
					getIdx.close();
				}
				catch(SQLException e)
				{
					cat.logThrowableT(Severity.WARNING, loc, "INDEX CREATE: " + e.getMessage(), e);
				}
			}
		}
	}

	private void checkParams() throws ConfigInconsistentException, MissingParameterException 
	{
		if(dbColumnLimits.isEmpty())
		{
			throw new ConfigInconsistentException("Unable to read database meta data information from init");
		}
		if("".equals(index_name))
		{
			throw new MissingParameterException("No index name specified");
		}
		if(index_name.length() > dbColumnLimits.get("index_dict.indexName"))
		{
			throw new IllegalArgumentException("Index name can be only " + dbColumnLimits.get("index_dict.indexName") + " characters long!");
		}
		if(!"".equals(multiValSupport))
		{
			if(!"Y".equals(multiValSupport) && !"N".equals(multiValSupport))
			{
				throw new IllegalArgumentException("Request header field \"multi_val_support\" has unsupported value \"" + multiValSupport + "\"");
			}
		}
	}

	private ArrayList<ColumnData> parseRequestBody(final boolean isMultiValSupported) 
	throws IOException, MissingParameterException, PropertyForbiddenException, WrongArgumentException 
	{
		BufferedReader br = null;
		try
		{
			br = new BufferedReader(new InputStreamReader(request.getInputStream(), "UTF-8"));
			String line = null;
			String columnName = null;
			String columnType = null;
			String javaType = null;
			int length = 0;
			ArrayList<ColumnData> columnList = new ArrayList<ColumnData>();
			StringTokenizer st = null;
			while((line = br.readLine()) != null) 
			{
				length = 0;
				st = new StringTokenizer(line, ":");
				columnName = st.nextToken().toUpperCase();
				columnType = st.nextToken().toUpperCase();
				if(st.hasMoreTokens()) 
				{
					parseSecIndexDefinition(columnName, st.nextToken().toUpperCase());
				}
				if(checkColumnName(columnName))
				{
					if(columnName.equalsIgnoreCase("MANDT"))
					{
						hasMandt = "Y";
					}
				}
				else
				{
					throw new PropertyForbiddenException("Property name " + columnName + " not allowed here or property name too long");
				}
				if(columnType.toUpperCase().startsWith("VARCHAR")) 
				{
					length = Integer.parseInt(columnType.substring(columnType.indexOf("(") + 1, columnType.indexOf(")")));
					if(length > MAXVARCHARSIZE || length < 1)
					{
						throw new IllegalArgumentException("Length of " + columnName + " must be between 1 and " + MAXVARCHARSIZE);
					}
					columnType = "VARCHAR";
				}
				else if(columnType.toUpperCase().equals("BINARY"))
				{
					length = IGUID.NUMBYTES;
				}
				javaType = typeConversionMap.get(columnType);
				if(javaType == null)
				{
					throw new IllegalArgumentException("Column type "	+ columnType + " not supported");
				}
				columnList.add(new ColumnData(columnName, javaType, columnType,	length));
			}
			if(columnList.isEmpty())
			{
				throw new MissingParameterException("No index properties specified");
			}

			// add RESID entry
			columnList.add(0, new ColumnData("RESID", "long", "BIGINT", 0));
			// if multi-valued properties are supported, then add "COUNTER" column
			if(isMultiValSupported)
			{
				columnList.add(1, new ColumnData("COUNTER", "long", "BIGINT", 0));
			}
			return columnList;
		}
		finally
		{
			if(br != null)
			{
				br.close();
			}
		}
	}

	private boolean checkColumnName(final String columnName) 
	{
		if(columnName.length() > dbColumnLimits.get("index_cols.propName"))
		{
			return false;
		}
		if(nocNames.contains(columnName.toUpperCase()))
		{	
			return false;
		}
		return true;
	}

	private void checkIndexExists(final PreparedStatement getIdx) throws SQLException, GeneralConflictException 
	{
		ResultSet rs3 = getIdx.executeQuery();
		try
		{
			if(rs3.next()) 
			{
				throw new GeneralConflictException("An index with this name already exists");
			}
		}
		finally
		{
			rs3.close();
		}
	}

	private Document generateTableXML(final String tableName, final String indexSuffix, final ArrayList<ColumnData> columnData, final boolean isMultiValSupported) 
	throws ConfigInconsistentException 
	{
		Document indexTableXML = null;
		try 
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			indexTableXML = builder.newDocument();
		} 
		catch(ParserConfigurationException pacex) 
		{
			throw new ConfigInconsistentException("Failed to create the table generation XML: " + pacex.getMessage());
		} 
		// root element of table generation XML 
		Element dbTableElem = indexTableXML.createElement("Dbtable");
		dbTableElem.setAttribute("name", tableName.toUpperCase().trim());
		dbTableElem.setAttribute("creation-date", new java.util.Date().toString());
		// sub element "properties" with "author" and "description"
		Element properties = indexTableXML.createElementNS(null, "properties");
		Element author = addTextNodeElem(indexTableXML, "author", "DAservice"); 
		Element description = addTextNodeElem(indexTableXML, "description", "Property index table auto-generated by SAP XML Data Archiving Service"); 
		description.setAttribute("language", "EN");
		properties.appendChild(author);
		properties.appendChild(description);
		// (empty) sub element "predefined-action"
		Element preDefActionElem = indexTableXML.createElementNS(null, "predefined-action");
		// (empty) sub element "position-is-relevant"
		Element posIsRelevantElem = indexTableXML.createElementNS(null, "position-is-relevant");
		// sub element "deployment-status"
		Element deployStatusElem = indexTableXML.createElementNS(null, "deployment-status");
		// add sub elements to root
		dbTableElem.appendChild(properties);
		dbTableElem.appendChild(preDefActionElem);
		dbTableElem.appendChild(posIsRelevantElem);
		dbTableElem.appendChild(deployStatusElem);
		
		// sub element "columns"
		Element columns = indexTableXML.createElementNS(null, "columns");
		Element columnElem = null;
		int colPosition = 0;
		String colName = null;
		for(ColumnData column : columnData) 
		{
			// add "column" elements
			columnElem = indexTableXML.createElementNS(null, "column");
			colName = column.getCName();
			columnElem.setAttributeNS(null, "name", colName);
			columnElem.appendChild(addTextNodeElem(indexTableXML, "position", String.valueOf(colPosition + 1)));
			columnElem.appendChild(addTextNodeElem(indexTableXML, "ddtyp", column.getCType()));
			columnElem.appendChild(addTextNodeElem(indexTableXML, "java-sql-type", column.getJdbcType()));
			columnElem.appendChild(addTextNodeElem(indexTableXML, "length", String.valueOf(column.getLength())));
			columnElem.appendChild(addTextNodeElem(indexTableXML, "decimals", null));
			// if multi-valued properties are supported, then all columns are nullable except the primary key columns ("RESID", "COUNTER")
			if("RESID".equals(colName) || "COUNTER".equals(colName))
			{
				columnElem.appendChild(addTextNodeElem(indexTableXML, "is-not-null", "true"));
			}
			else
			{
				columnElem.appendChild(addTextNodeElem(indexTableXML, "is-not-null", isMultiValSupported ? "false" : "true"));
			}
			columns.appendChild(columnElem);
			colPosition++;
		}
		dbTableElem.appendChild(columns);
		
		// sub element "primary-key" with "tabname" and "columns" ("RESID" and (if required) "COUNTER" are primary key fields)
		Element primKeyElem = indexTableXML.createElementNS(null, "primary-key");
		primKeyElem.appendChild(addTextNodeElem(indexTableXML, "tabname", tableName));
		Element primKeyColumns = indexTableXML.createElementNS(null, "columns");
		primKeyColumns.appendChild(addTextNodeElem(indexTableXML, "column", "RESID"));
		if(isMultiValSupported)
		{
			primKeyColumns.appendChild(addTextNodeElem(indexTableXML, "column", "COUNTER"));
		}
		primKeyElem.appendChild(primKeyColumns);
		dbTableElem.appendChild(primKeyElem);
		// sub element "indexes" (one "index" element per secondary index definition)
		if(secondaryIndexDefinition != null) 
		{
			Element secIndexesElem = indexTableXML.createElementNS(null, "indexes");
			Element secIndexElem = null;
			Element secPropertiesElem = null;
			Element secColumnsElem = null;
			Element secPropDescriptionElem = null;
			Element secIndexColumnElem = null;
			TreeMap<Integer, String> colName2SecIdxPos = null;
			for(Entry<Character, TreeMap<Integer, String>> secIndexAlias : secondaryIndexDefinition.entrySet())
			{
				// "index" element
				secIndexElem = indexTableXML.createElementNS(null, "index");
				secIndexElem.setAttributeNS(null, "name", new StringBuilder(isMultiValSupported ? SECONDARYINDEXPREFIX_MULTI : SECONDARYINDEXPREFIX).append(indexSuffix).append(secIndexAlias.getKey()).toString());
				// each "index" element contains:
				// 1. "tabname"
				secIndexElem.appendChild(addTextNodeElem(indexTableXML, "tabname", tableName));
				// 2. "properties" (with "author" and "description")
				secPropertiesElem = indexTableXML.createElementNS(null, "properties");
				secPropertiesElem.appendChild(addTextNodeElem(indexTableXML, "author", ""));
				secPropDescriptionElem = addTextNodeElem(indexTableXML, "description", "");
				secPropDescriptionElem.setAttributeNS(null, "language", "");
				secPropertiesElem.appendChild(secPropDescriptionElem);
				secIndexElem.appendChild(secPropertiesElem);
				// 3. "is-unique"
				secIndexElem.appendChild(addTextNodeElem(indexTableXML, "is-unique", "false"));
				// 4. "deployment-status"
				secIndexElem.appendChild(addTextNodeElem(indexTableXML, "deployment-status", ""));
				// 5. "columns"
				secColumnsElem = indexTableXML.createElementNS(null, "columns");
				colName2SecIdxPos = secIndexAlias.getValue();
				for(Entry<Integer, String> colName2PosEntry : colName2SecIdxPos.entrySet())
				{
					// iterate through the sorted positions and create a "column" entry for each
					// add "column" elements (each with "name" and "is-descending")
					secIndexColumnElem = indexTableXML.createElementNS(null, "column");
					secIndexColumnElem.appendChild(addTextNodeElem(indexTableXML, "name", colName2PosEntry.getValue()));
					secIndexColumnElem.appendChild(addTextNodeElem(indexTableXML, "is-descending",	"false"));
					secColumnsElem.appendChild(secIndexColumnElem);
				}
				secIndexElem.appendChild(secColumnsElem);
			}
			secIndexesElem.appendChild(secIndexElem);
			dbTableElem.appendChild(secIndexesElem);
		}
		// add root element to XML document
		indexTableXML.appendChild(dbTableElem);
		return indexTableXML;
	}

	private ByteArrayOutputStream serializeIndexTableXML(final Document indexTableXML)
	throws IOException
	{
		// serialize table generation XML
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		Result result = new StreamResult(bos);
		BufferedReader br = null;
		try 
		{
			Source source = new DOMSource(indexTableXML);
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.transform(source, result);
			bos.flush();
			// log transformed index schema XML document
			ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
			InputStreamReader isr = new InputStreamReader(bis, "UTF-8");
			br = new BufferedReader(isr);
			String s = "";
			StringBuilder sb = new StringBuilder();
			while((s = br.readLine()) != null)
			{
				sb.append(s);
			}
			loc.infoT(sb.toString());

			return bos;
		} 
		catch(TransformerException tfex) 
		{
			throw new IOException("Serialization of table generation XML failed: "	+ tfex.getMessage());
		}
		finally
		{
			if(br != null)
			{
				br.close();
			}
		}
	}
	
	private Element addTextNodeElem(final Document indexTableXML, final String elemName, String elemText) 
	{
		if(elemText == null) 
		{
			elemText = "";
		}
		// create the text node element
		Element textNodeElem = indexTableXML.createElementNS(null, elemName);
		// create the text node
		Node textNode = indexTableXML.createTextNode(elemText);
		// append text node to element
		textNodeElem.appendChild(textNode);
		return textNodeElem;
	}

	private void createTable(final String tableName, final ByteArrayInputStream bis) throws JddException, SQLException 
	{
		OpenTools dyndict = new DbTableOpenTools(connection);
		boolean result = dyndict.createTable(tableName, bis);
		if(!result)
		{
			throw new SQLException("Error creating index table " + tableName);
		}
	}

	private void updateIndexDict(final long indexId, final String tabName) throws SQLException 
	{
		PreparedStatement insertDict = null;
		try
		{
			insertDict = connection.prepareStatement(INS_DICT);
			insertDict.setLong(1, indexId);
			insertDict.setString(2, index_name);
			insertDict.setString(3, tabName);
			insertDict.setString(4, hasMandt);
			insertDict.executeUpdate();
		}
		finally
		{
			if(insertDict != null)
			{
				try 
				{
					insertDict.close();
				}
				catch(SQLException sqlex) 
				{
					cat.logThrowableT(Severity.WARNING, loc, "INDEX CREATE: " + sqlex.getMessage(), sqlex);
				}
			}
		}
	}

	private void updateIndexCols(final long indexDictId, final ArrayList<ColumnData> columnData) 
	throws SQLException 
	{
		long indexColId = 0;
		String columnName = null;
		PreparedStatement insertCols = null;
		try
		{
			insertCols = connection.prepareStatement(INS_COLS);
			for(ColumnData column : columnData) 
			{
				columnName = column.getCName().toLowerCase();
				// "resid" and "counter" are automatically generated for every index. Don't write to BC_XMLA_INDEX_COLS
				if(!"resid".equals(columnName) && !"counter".equals(columnName)) 
				{
					indexColId = idProv.getId("BC_XMLA_INDEX_COLS");
					insertCols.setLong(1, indexColId);
					insertCols.setString(2, (columnName));
					insertCols.setString(3, (column.getJdbcType().toLowerCase()));
					insertCols.setInt(4, column.getLength());
					insertCols.setLong(5, indexDictId);
					insertCols.executeUpdate();
				}
			}
		}
		finally
		{
			if(insertCols != null)
			{
				try 
				{
					insertCols.close();
				}
				catch(SQLException sqlex) 
				{
					cat.logThrowableT(Severity.WARNING, loc, "INDEX CREATE: " + sqlex.getMessage(), sqlex);
				}
			}
		}
	}

	private void updateMaxIdsTable(final String indexTableName) throws SQLException 
	{
		PreparedStatement insertMaxIds = null;
		try
		{
			insertMaxIds = connection.prepareStatement(INS_MAXIDS);
			insertMaxIds.setString(1, indexTableName);
			insertMaxIds.setLong(2, 0);
			insertMaxIds.executeUpdate();
		}
		finally
		{
			if(insertMaxIds != null)
			{
				try 
				{
					insertMaxIds.close();
				}
				catch(SQLException sqlex) 
				{
					cat.logThrowableT(Severity.WARNING, loc, "INDEX CREATE: " + sqlex.getMessage(), sqlex);
				}
			}
		}
	}

	private void parseSecIndexDefinition(final String columnName, final String secIndexParam)	throws WrongArgumentException 
	{
		// extract sec index aliases and positions for the given column name
		StringTokenizer tokenizer = new StringTokenizer(secIndexParam, ".");
		String aliasAndPos = null;
		Character alias = 0;
		Integer secIdxPos = 0;
		TreeMap<Integer, String> colName2SecIdxPos = null;
		while(tokenizer.hasMoreTokens()) 
		{
			aliasAndPos = tokenizer.nextToken();
			alias = aliasAndPos.charAt(0);
			if(SECINDEX_ALIAS_CHARS.indexOf(alias) == -1)
			{
				throw new WrongArgumentException("Secondary index definition does not meet specifications: alias must be one character out of \"" + SECINDEX_ALIAS_CHARS + "\"");
			}
			if(aliasAndPos.length() == 1) 
			{
				// no position given
				secIdxPos = 0;
			}
			else if(aliasAndPos.length() == 2) 
			{
				try 
				{
					// position = second character
					secIdxPos = Integer.valueOf(aliasAndPos.substring(1));
				} 
				catch(NumberFormatException nfex) 
				{
					// $JL-EXC$
					throw new WrongArgumentException("Secondary index definition does not meet specifications: index position must be a one digit number");
				}
			} 
			else 
			{
				throw new WrongArgumentException("Secondary index definition does not meet specifications: index position must be a one digit number");
			}
			
			if(secondaryIndexDefinition == null)
			{
				secondaryIndexDefinition = new HashMap<Character, TreeMap<Integer, String>>(SECINDEX_ALIAS_CHARS.length());
			}
			if(!secondaryIndexDefinition.containsKey(alias))
			{
				// first occurrence of "alias"
				colName2SecIdxPos = new TreeMap<Integer, String>();
				secondaryIndexDefinition.put(alias, colName2SecIdxPos);
			}
			else
			{
				colName2SecIdxPos = secondaryIndexDefinition.get(alias);
				if(colName2SecIdxPos.containsKey(secIdxPos))
				{
					// the given index position is already occupied by another column
					throw new WrongArgumentException("Secondary index definition does not meet specifications: index position already occupied by column \"" + colName2SecIdxPos.get(secIdxPos) + "\"");
				}
			}
			colName2SecIdxPos.put(secIdxPos, columnName);
		}
	}
}
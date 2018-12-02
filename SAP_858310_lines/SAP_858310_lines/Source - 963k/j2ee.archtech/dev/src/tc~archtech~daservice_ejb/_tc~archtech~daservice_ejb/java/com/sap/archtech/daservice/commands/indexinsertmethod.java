package com.sap.archtech.daservice.commands;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Map.Entry;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sap.archtech.archconn.values.IndexPropValues;
import com.sap.archtech.daservice.data.DasResponse;
import com.sap.archtech.daservice.exceptions.BodyFormatException;
import com.sap.archtech.daservice.exceptions.InvalidNameException;
import com.sap.archtech.daservice.util.IdProvider;
import com.sap.guid.GUIDFormatException;
import com.sap.guid.GUIDGeneratorFactory;
import com.sap.guid.IGUIDGenerator;
import com.sap.tc.logging.Severity;

public class IndexInsertMethod extends MasterMethod {
	private final static int READBUFFER = 1024;
	private final static String INDEX_SPLIT_TOKEN = "#";
	private final static String INDEX_ATTRIBUTE_VALUE_TOKEN = "=";
	private final static String SEL_COL_TAB = "SELECT COLID FROM BC_XMLA_COL WHERE URI = ?";
	private final static String SEL_RES_TAB = "SELECT RESID FROM BC_XMLA_RES WHERE COLID = ? AND DELSTATUS IN ('Y', 'N', 'P') AND RESNAME = ?";
	private final static String SEL_IDX_DIC = "SELECT INDEXID, INDEXTABLE, HASMANDT FROM BC_XMLA_INDEX_DICT WHERE INDEXNAME = ?";
	private final static String SEL_IDX_COL = "SELECT PROPNAME, JDBCTYPE FROM BC_XMLA_INDEX_COLS WHERE INDEXID = ?";
	private final static String SEL_COL_IDX = "SELECT * FROM BC_XMLA_COL_INDEX WHERE INDEXID = ? AND COLID = ?";
	private final static String INS_COL_IDX = "INSERT INTO BC_XMLA_COL_INDEX (INDEXID, COLID) VALUES (?, ?)";

	private final static String INDEXPREFIX_MULTI = "BCGEN01_XMLA";

	private final static IGUIDGenerator guidGen = GUIDGeneratorFactory
			.getInstance().createGUIDGenerator();

	private final HttpServletRequest request;
	private final Connection connection;
	private final String index_count;
	private final IdProvider idProvider;

	private String archive_path;
	private String resource_name;

	public IndexInsertMethod(HttpServletRequest request,
			HttpServletResponse response, Connection connection,
			IdProvider idProvider, String archive_path, String resource_name,
			String index_count) {
		this.request = request;
		this.response = response;
		this.connection = connection;
		this.archive_path = archive_path;
		this.resource_name = resource_name;
		this.index_count = index_count;
		this.idProvider = idProvider;
	}

	public boolean execute() throws IOException {
		if (!checkParams()) {
			return false;
		}
		// *** get collection and resource from database
		// adjust archive path for further processing
		archive_path = archive_path.substring(0, archive_path.length() - 1);
		Long collID = null;
		Long resID = null;
		try {
			// get collection ID
			collID = getCollectionID();
			if (collID == null) {
				return false;
			}
			// get resource ID
			resID = getResourceID(collID);
			if (resID == null) {
				return false;
			}
		} catch (SQLException e) {
			reportError(
					DasResponse.SC_SQL_ERROR,
					"INDEX INSERT: Database problem occurred when trying to get collection and resource for archive path "
							+ archive_path + ": " + e.toString(), e);
			return false;
		}
		// *** extract index data from HTTP request body
		String[] indexName = new String[] { "" };
		ArrayList<IndexDataContainer> indexData = new ArrayList<IndexDataContainer>();
		try {
			if (index_count == null) {
				// "index_count" not set -> get index data from HTTP request
				// body
				if (!extractIndexDataFromHttpRequest(request.getInputStream(),
						resID, indexData, indexName)) {
					return false;
				}
			} else {
				// deserialize IndexPropValues from HTTP request body
				if (!deserializeIndexPropValues(request.getInputStream(),
						resID, indexData, indexName)) {
					return false;
				}
			}
		} catch (ParseException pex) {
			reportError(DasResponse.SC_WRONG_TIMESTAMP_FORMAT,
					"INDEX INSERT: A timestamp property of index "
							+ indexName[0] + " has wrong format: "
							+ pex.toString(), pex);
			return false;
		} catch (NumberFormatException nfex) {
			reportError(DasResponse.SC_WRONG_INDEX_PROPERTY_TYPE,
					"INDEX INSERT: While casting the properties of index "
							+ indexName[0]
							+ " a NumberFormatException occurred: "
							+ nfex.toString(), nfex);
			return false;
		} catch (SQLException sqlex) {
			reportError(DasResponse.SC_SQL_ERROR,
					"INDEX INSERT: While preparing index " + indexName[0]
							+ " an SQLException occurred: " + sqlex.toString(),
					sqlex);
			return false;
		}
		// *** insert index data into Database
		try {
			persistIndexData(indexData, collID);
		} catch (SQLException sqlex) {
			reportError(DasResponse.SC_SQL_ERROR, "INDEX INSERT: "
					+ sqlex.toString(), sqlex);
			return false;
		} catch (BodyFormatException e) {
			reportError(DasResponse.SC_BODY_FORMAT_CORRUPT,
					"INDEX INSERT: While preparing index " + indexName[0]
							+ " a GUIDFormatException occurred: "
							+ e.toString(), e);
			return false;
		}
		// *** set response header fields
		response.setContentType("text/xml");
		response.setHeader("service_message", "Ok");
		return true;
	}

	private boolean checkParams() throws IOException {
		// Check Request Header "archive_path"
		if (archive_path == null) {
			reportError(DasResponse.SC_PARAMETER_MISSING,
					"INDEX INSERT: ARCHIVE_PATH missing in request header");
			return false;
		} else {
			try {
				isValidName(archive_path, true);
			} catch (InvalidNameException inex) {
				reportError(DasResponse.SC_INVALID_CHARACTER, "INDEX INSERT: "
						+ inex.getMessage());
				return false;
			}
			archive_path = archive_path.toLowerCase();
			if (archive_path.contains("//") || !archive_path.startsWith("/")
					|| !archive_path.endsWith("/") || archive_path.length() < 3) {
				reportError(
						DasResponse.SC_PARAMETER_SYNTAX_INCORRECT,
						"INDEX INSERT: Value "
								+ archive_path
								+ "of request header ARCHIVE_PATH does not meet specifications");
				return false;
			}
		}

		// Check Request Header "resource_name"
		if (resource_name == null) {
			reportError(DasResponse.SC_PARAMETER_MISSING,
					"INDEX INSERT: RESOURCE_NAME missing in request header");
			return false;
		} else {
			try {
				isValidName(resource_name, false);
			} catch (InvalidNameException inex) {
				reportError(DasResponse.SC_INVALID_CHARACTER, "INDEX INSERT: "
						+ inex.getMessage());
				return false;
			}
			resource_name = resource_name.toLowerCase();
		}

		return true;
	}

	private boolean extractIndexDataFromHttpRequest(
			final ServletInputStream requestInputStream, final Long resID,
			final ArrayList<IndexDataContainer> indexData,
			final String[] outParam_indexName) throws IOException,
			ParseException, SQLException {
		boolean furtherLoop = true;
		ByteArrayOutputStream baos = null;
		byte[] buffer = null;
		int nrOfBytes4Copy = 0;
		do {
			try {
				baos = new ByteArrayOutputStream();
				buffer = new byte[READBUFFER];
				nrOfBytes4Copy = 0;
				// copy each index data block to one byte array
				while ((nrOfBytes4Copy = requestInputStream.readLine(buffer, 0,
						READBUFFER)) > 0) {
					baos.write(buffer, 0, nrOfBytes4Copy);
					if (nrOfBytes4Copy >= 2
							&& buffer[nrOfBytes4Copy - 2] == 0x0D
							&& buffer[nrOfBytes4Copy - 1] == 0x0A) {
						// reached end of index data block ("0D0A")
						break;
					}
				}
				if (baos.size() <= 2) {
					// reached end of request input stream
					furtherLoop = false;
				} else {
					fillIndexDataContainerFromIndexDataBytes(
							baos.toByteArray(), resID, indexData,
							outParam_indexName);
				}
			} finally {
				if (baos != null) {
					baos.close();
				}
			}
		} while (furtherLoop);

		// there should be at least one index in the request body
		if (indexData.size() == 0) {
			reportError(DasResponse.SC_INDEX_PROPERTY_MISSING,
					"INDEX INSERT: Request contains no index data");
			return false;
		}
		return true;
	}

	private boolean deserializeIndexPropValues(
			final ServletInputStream requestInputStream, final Long resID,
			final ArrayList<IndexDataContainer> indexData,
			final String[] outParam_indexName) throws IOException {
		ObjectInputStream ois = null;
		try {
			int indexCount = Integer.parseInt(index_count);
			IndexPropValues propValues = null;
			for (int i = 0; i < indexCount; i++) {
				ois = new ObjectInputStream(requestInputStream);
				propValues = (IndexPropValues) ois.readObject();
				fillIndexDataContainerFromIndexPropValues(propValues, resID,
						indexData, outParam_indexName);
			}
			// there should be at least one index in the request body
			if (indexData.size() == 0) {
				reportError(DasResponse.SC_INDEX_PROPERTY_MISSING,
						"INDEX INSERT: Request contains no index data");
				return false;
			}
			return true;
		} catch (NumberFormatException nfex) {
			MasterMethod.cat
					.errorT(
							loc,
							"INDEX INSERT: Value "
									+ index_count
									+ " of request header INDEX_COUNT does not meet specifications: "
									+ getStackTrace(nfex));
			// $JL-EXC$
			reportError(
					DasResponse.SC_KEYWORD_UNKNOWN,
					"INDEX INSERT: Value "
							+ index_count
							+ " of request header INDEX_COUNT does not meet specifications: "
							+ nfex.toString());
			return false;
		} catch (ClassNotFoundException cnfex) {
			MasterMethod.cat.errorT(loc,
					"INDEX INSERT: An error occurred during index data deserialization: "
							+ getStackTrace(cnfex));
			reportError(DasResponse.SC_BODY_FORMAT_CORRUPT,
					"INDEX INSERT: An error occurred during index data deserialization: "
							+ cnfex.toString());
			return false;
		} catch (Exception ex) {
			MasterMethod.cat.errorT(loc,
					"INDEX INSERT: An error occurred during index data deserialization: "
							+ getStackTrace(ex));
			reportError(DasResponse.SC_BODY_FORMAT_CORRUPT,
					"INDEX INSERT: An error occurred during index data deserialization: "
							+ ex.toString());
			return false;
		} finally {
			if (ois != null) {
				ois.close();
			}
		}
	}

	private Long getCollectionID() throws SQLException, IOException {
		PreparedStatement selColTab = null;
		ResultSet result = null;
		try {
			selColTab = connection.prepareStatement(SEL_COL_TAB);
			selColTab.setString(1, archive_path.trim());
			result = selColTab.executeQuery();
			if (result.next()) {
				return result.getLong("COLID");
			} else {
				int lastSlashNum = archive_path.lastIndexOf("/");
				int strLen = archive_path.length();
				if (lastSlashNum != -1 && lastSlashNum < strLen) {
					reportError(DasResponse.SC_DOES_NOT_EXISTS,
							"INDEX INSERT: Collection "
									+ archive_path.substring(lastSlashNum + 1,
											strLen) + " does not exist");
				} else {
					reportError(DasResponse.SC_DOES_NOT_EXISTS,
							"INDEX INSERT: Collection does not exist");
				}
				return null;
			}
		} finally {
			if (result != null) {
				try {
					result.close();
				} catch (SQLException sqlex) {
					cat.logThrowableT(Severity.WARNING, loc, "INDEX INSERT: "
							+ sqlex.getMessage(), sqlex);
				}
			}
			if (selColTab != null) {
				try {
					selColTab.close();
				} catch (SQLException sqlex) {
					cat.logThrowableT(Severity.WARNING, loc, "INDEX INSERT: "
							+ sqlex.getMessage(), sqlex);
				}
			}
		}
	}

	private Long getResourceID(final Long collID) throws SQLException,
			IOException {
		PreparedStatement selResTab = null;
		ResultSet result = null;
		try {
			selResTab = connection.prepareStatement(SEL_RES_TAB);
			selResTab.setLong(1, collID);
			selResTab.setString(2, resource_name.trim());
			result = selResTab.executeQuery();
			if (result.next()) {
				return result.getLong("RESID");
			} else {
				reportError(DasResponse.SC_DOES_NOT_EXISTS,
						"INDEX INSERT: Resource " + resource_name.trim()
								+ " does not exist");
				return null;
			}
		} finally {
			if (result != null) {
				try {
					result.close();
				} catch (SQLException sqlex) {
					cat.logThrowableT(Severity.WARNING, loc, "INDEX INSERT: "
							+ sqlex.getMessage(), sqlex);
				}
			}
			if (selResTab != null) {
				try {
					selResTab.close();
				} catch (SQLException sqlex) {
					cat.logThrowableT(Severity.WARNING, loc, "INDEX INSERT: "
							+ sqlex.getMessage(), sqlex);
				}
			}
		}
	}

	private boolean fillIndexDataContainerFromIndexDataBytes(
			final byte[] indexDataBytes, final Long resId,
			final ArrayList<IndexDataContainer> indexDataList,
			final String[] outParam_indexName) throws SQLException,
			IOException, ParseException {
		// cut 0D0A if necessary
		byte[] indexDataBytes_Cut = indexDataBytes;
		if (indexDataBytes.length >= 2
				&& indexDataBytes[indexDataBytes.length - 2] == 0x0D
				&& indexDataBytes[indexDataBytes.length - 1] == 0x0A) {
			indexDataBytes_Cut = new byte[indexDataBytes.length - 2];
			System.arraycopy(indexDataBytes, 0, indexDataBytes_Cut, 0,
					indexDataBytes.length - 2);
		}
		String indexLine = new String(indexDataBytes_Cut, "UTF-8");
		boolean hasMandtAttr = indexLine.toUpperCase().contains("#MANDT");
		StringTokenizer tokenizer = new StringTokenizer(indexLine,
				INDEX_SPLIT_TOKEN);
		outParam_indexName[0] = tokenizer.nextToken().toLowerCase().trim();
		// get index table meta data from database
		Long[] indexId = new Long[] { Long.valueOf(0) };
		String[] indexTable = new String[] { "" };
		String[] hasMandt = new String[] { "" };
		if (!getIndexTableMetaData(outParam_indexName[0], indexId, indexTable,
				hasMandt)) {
			return false;
		}
		boolean isMultiValSupported = indexTable[0].toUpperCase().startsWith(
				INDEXPREFIX_MULTI);
		// check if MANDT property is correctly set
		if (hasMandt[0].equalsIgnoreCase("Y") && !hasMandtAttr) {
			reportError(DasResponse.SC_CLIENT_MISSING,
					"INDEX INSERT: Property MANDT missing for client-dependent index "
							+ outParam_indexName[0]);
			return false;
		}
		// get index property meta data
		HashMap<String, PropValueAndJdbcType> propertyDefMap = new HashMap<String, PropValueAndJdbcType>();
		int nrOfProperties = getIndexPropertiesMetaData(propertyDefMap,
				new HashMap<String, Serializable>(0), indexId[0], indexTable[0]);
		if (nrOfProperties == 0) {
			return false;
		}
		// fill property values
		String propNameAndValue = null;
		String propName = null;
		String propValue = null;
		String jdbcType = null;
		int posOfNameValueSeparator = 0;
		PropValueAndJdbcType propValAndJdbcType = null;
		int propValueCounter = 0;
		while (tokenizer.hasMoreTokens()) {
			propNameAndValue = tokenizer.nextToken();
			posOfNameValueSeparator = propNameAndValue
					.indexOf(INDEX_ATTRIBUTE_VALUE_TOKEN);
			if (posOfNameValueSeparator != -1) {
				// get property name
				propName = propNameAndValue.substring(0,
						posOfNameValueSeparator).toLowerCase().trim();
				// check if JDBC type is defined for the given property
				propValAndJdbcType = propertyDefMap.get(propName);
				jdbcType = propValAndJdbcType != null ? propValAndJdbcType
						.getJdbcType() : null;
				if (jdbcType == null) {
					reportError(DasResponse.SC_DOES_NOT_EXISTS,
							"INDEX INSERT: Property " + propName
									+ " does not exist in index "
									+ outParam_indexName[0]);
					return false;
				}
				// get property value
				propValue = propNameAndValue.substring(
						posOfNameValueSeparator + 1, propNameAndValue.length());
				if (propValue.length() == 0 && !jdbcType.startsWith("VARCHAR")) {
					// empty value only allowed for "VARCHAR" properties
					reportError(DasResponse.SC_BODY_FORMAT_CORRUPT,
							"INDEX INSERT: Property " + propName + " of index "
									+ outParam_indexName[0] + " has no value");
					return false;
				}
				// add properties to map
				if (jdbcType.startsWith("VARCHAR")) {
					// empty strings are not supported
					if ("".equals(propValue)) {
						propValue = null;
					}
					propValAndJdbcType.setPropValue(propValue);
					propValueCounter++;
				} else if (jdbcType.startsWith("SMALLINT")) {
					propValAndJdbcType.setPropValue(Short.valueOf(propValue));
					propValueCounter++;
				} else if (jdbcType.startsWith("INTEGER")) {
					propValAndJdbcType.setPropValue(Integer.valueOf(propValue));
					propValueCounter++;
				} else if (jdbcType.startsWith("BIGINT")) {
					propValAndJdbcType.setPropValue(Long.valueOf(propValue));
					propValueCounter++;
				} else if (jdbcType.startsWith("REAL")) {
					propValAndJdbcType.setPropValue(Float.valueOf(propValue));
					propValueCounter++;
				} else if (jdbcType.startsWith("DOUBLE")) {
					propValAndJdbcType.setPropValue(Double.valueOf(propValue));
					propValueCounter++;
				} else if (jdbcType.startsWith("TIMESTAMP")) {
					SimpleDateFormat sdf = propValue.length() < 20 ? new SimpleDateFormat(
							"yyyy-MM-dd HH:mm:ss z")
							: new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS z");
					Date date = sdf.parse(new StringBuilder(propValue).append(
							" GMT").toString());
					propValAndJdbcType.setPropValue(new Timestamp(date
							.getTime()));
					propValueCounter++;
				} else if (jdbcType.startsWith("BINARY")) {
					propValAndJdbcType.setPropValue(propValue);
					propValueCounter++;
				} else {
					reportError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
							"INDEX INSERT: Index table " + indexTable[0]
									+ " has no property of type " + jdbcType);
					return false;
				}
			} else {
				reportError(DasResponse.SC_BODY_FORMAT_CORRUPT,
						"INDEX INSERT: Error while inserting entry into index table "
								+ indexTable[0] + ", due to missing \'"
								+ INDEX_ATTRIBUTE_VALUE_TOKEN + "\' token");
				return false;
			}
		}

		// check if all index properties have been passed (only in case of
		// missing multi-value support)
		if (!isMultiValSupported && (propValueCounter != nrOfProperties)) {
			reportError(DasResponse.SC_INDEX_PROPERTY_MISSING,
					"INDEX INSERT: Passed properties for index "
							+ outParam_indexName[0] + " are not complete");
			return false;
		}
		// add RESID column
		propertyDefMap.put("resid", new PropValueAndJdbcType(resId, "BIGINT"));
		// calculate value for "counter" column
		if (isMultiValSupported) {
			propertyDefMap.put("counter", new PropValueAndJdbcType(idProvider
					.getId(indexTable[0].toUpperCase()), "BIGINT"));
		}
		// add index data object to ArrayList
		indexDataList.add(new IndexDataContainer(indexId[0], indexTable[0],
				propertyDefMap));
		return true;
	}

	private boolean fillIndexDataContainerFromIndexPropValues(
			final IndexPropValues indexPropValues, final Long resId,
			final ArrayList<IndexDataContainer> indexData,
			final String[] indexName) throws SQLException, IOException,
			ParseException {
		// get index name
		indexName[0] = indexPropValues.getIndexname();
		HashMap<String, Serializable> propValuesMap = indexPropValues
				.getPropertyValues();
		// get index table meta data from database
		Long[] indexId = new Long[] { Long.valueOf(0) };
		String[] indexTable = new String[] { "" };
		String[] hasMandt = new String[] { "" };
		if (!getIndexTableMetaData(indexName[0], indexId, indexTable, hasMandt)) {
			return false;
		}
		boolean isMultiValSupported = indexTable[0].toUpperCase().startsWith(
				INDEXPREFIX_MULTI);
		// check if MANDT property is correctly set
		if (hasMandt[0].equalsIgnoreCase("Y")
				&& !propValuesMap.containsKey("mandt")) {
			reportError(DasResponse.SC_CLIENT_MISSING,
					"INDEX INSERT: Property MANDT missing for client-dependent index "
							+ indexName[0]);
			return false;
		}
		// get meta data of index properties
		HashMap<String, PropValueAndJdbcType> propertyDefMap = new HashMap<String, PropValueAndJdbcType>();
		int nrOfProperties = getIndexPropertiesMetaData(propertyDefMap,
				propValuesMap, indexId[0], indexTable[0]);
		if (nrOfProperties == 0) {
			return false;
		}
		// check if all index properties have been passed (only in case of
		// missing multi-value support)
		if (!isMultiValSupported && (propValuesMap.size() != nrOfProperties)) {
			reportError(DasResponse.SC_INDEX_PROPERTY_MISSING,
					"INDEX INSERT: Passed properties for index " + indexName[0]
							+ " are not complete");
			return false;
		}

		// if necessary convert index property values into correct type
		// note: must iterate over a copy of "propertyDefMap" because some
		// entries in that map may become modified
		// during the iteration!
		String propName = null;
		String propType = null;
		Serializable propVal = null;
		HashMap<String, PropValueAndJdbcType> propertyDefMap_Copy = new HashMap<String, PropValueAndJdbcType>(
				propertyDefMap);
		Set<Entry<String, PropValueAndJdbcType>> propertyDefMap_CopyEntries = propertyDefMap_Copy
				.entrySet();
		for (Entry<String, PropValueAndJdbcType> propertyDefEntry : propertyDefMap_CopyEntries) {
			propName = propertyDefEntry.getKey();
			propType = propertyDefEntry.getValue().getJdbcType();
			propVal = propertyDefEntry.getValue().getPropValue();
			if (isMultiValSupported && propVal == null) {
				// no property value contained in the request -> allowed in case
				// of multi-value support
				continue;
			}
			if (propType.startsWith("VARCHAR")) {
				if (!(propVal instanceof String)) {
					reportError(DasResponse.SC_WRONG_INDEX_PROPERTY_TYPE,
							"INDEX INSERT: Property " + propName
									+ " of index table " + indexTable[0]
									+ " has a wrong type");
					return false;
				} else if ("".equals((String) propVal)) {
					// empty strings are not supported
					propertyDefMap.put(propName, new PropValueAndJdbcType(null,
							propType));
				}
			} else if (propType.startsWith("SMALLINT")) {
				if (propVal instanceof String) {
					propertyDefMap.put(propName, new PropValueAndJdbcType(Short
							.valueOf((String) propVal), propType));
				} else if (!(propVal instanceof Short)) {
					reportError(DasResponse.SC_WRONG_INDEX_PROPERTY_TYPE,
							"INDEX INSERT: Property " + propName
									+ " of index table " + indexTable[0]
									+ " has a wrong type");
					return false;
				}
			} else if (propType.startsWith("INTEGER")) {
				if (propVal instanceof String) {
					propertyDefMap.put(propName, new PropValueAndJdbcType(
							Integer.valueOf((String) propVal), propType));
				} else if (!(propVal instanceof Integer)) {
					reportError(DasResponse.SC_WRONG_INDEX_PROPERTY_TYPE,
							"INDEX INSERT: Property " + propName
									+ " of index table " + indexTable[0]
									+ " has a wrong type");
					return false;
				}
			} else if (propType.startsWith("BIGINT")) {
				if (propVal instanceof String) {
					propertyDefMap.put(propName, new PropValueAndJdbcType(Long
							.valueOf((String) propVal), propType));
				} else if (!(propVal instanceof Long)) {
					reportError(DasResponse.SC_WRONG_INDEX_PROPERTY_TYPE,
							"INDEX INSERT: Property " + propName
									+ " of index table " + indexTable[0]
									+ " has a wrong type");
					return false;
				}
			} else if (propType.startsWith("REAL")) {
				if (propVal instanceof String) {
					propertyDefMap.put(propName, new PropValueAndJdbcType(Float
							.valueOf((String) propVal), propType));
				} else if (!(propVal instanceof Float)) {
					reportError(DasResponse.SC_WRONG_INDEX_PROPERTY_TYPE,
							"INDEX INSERT: Property " + propName
									+ " of index table " + indexTable[0]
									+ " has a wrong type");
					return false;
				}
			} else if (propType.startsWith("DOUBLE")) {
				if (propVal instanceof String) {
					propertyDefMap.put(propName, new PropValueAndJdbcType(
							Double.valueOf((String) propVal), propType));
				} else if (!(propVal instanceof Double)) {
					reportError(DasResponse.SC_WRONG_INDEX_PROPERTY_TYPE,
							"INDEX INSERT: Property " + propName
									+ " of index table " + indexTable[0]
									+ " has a wrong type");
					return false;
				}
			} else if (propType.startsWith("TIMESTAMP")) {
				if (propVal instanceof String) {
					SimpleDateFormat sdf = ((String) propVal).length() < 20 ? new SimpleDateFormat(
							"yyyy-MM-dd HH:mm:ss z")
							: new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS z");
					propertyDefMap.put(propName, new PropValueAndJdbcType(
							new Timestamp(sdf
									.parse(((String) propVal) + " GMT")
									.getTime()), propType));
				} else if (!(propVal instanceof Timestamp)) {
					reportError(DasResponse.SC_WRONG_INDEX_PROPERTY_TYPE,
							"INDEX INSERT: Property " + propName
									+ " of index table " + indexTable[0]
									+ " has a wrong type");
					return false;
				}
			} else if (propType.startsWith("BINARY")) {
				if (!(propVal instanceof String)) {
					reportError(DasResponse.SC_WRONG_INDEX_PROPERTY_TYPE,
							"INDEX INSERT: Property " + propName
									+ " of index table " + indexTable[0]
									+ " has a wrong type");
					return false;
				}
			} else {
				reportError(DasResponse.SC_WRONG_INDEX_PROPERTY_TYPE,
						"INDEX INSERT: Property " + propName
								+ " of index table " + indexTable[0]
								+ " has a wrong type");
				return false;
			}
		}
		// add RESID column
		propertyDefMap.put("resid", new PropValueAndJdbcType(resId, "BIGINT"));
		// calculate value for "counter" column
		if (isMultiValSupported) {
			propertyDefMap.put("counter", new PropValueAndJdbcType(idProvider
					.getId(indexTable[0].toUpperCase()), "BIGINT"));
		}
		// add index data object to ArrayList
		indexData.add(new IndexDataContainer(indexId[0], indexTable[0],
				propertyDefMap));
		return true;
	}

	private boolean getIndexTableMetaData(final String indexName,
			final Long[] outParam_indexId, final String[] outParam_indexTable,
			final String[] outParam_hasMandt) throws SQLException, IOException {
		PreparedStatement selIdxDic = null;
		ResultSet result = null;
		try {
			selIdxDic = connection.prepareStatement(SEL_IDX_DIC);
			selIdxDic.setString(1, indexName);
			result = selIdxDic.executeQuery();
			if (result.next()) {
				outParam_indexId[0] = result.getLong("INDEXID");
				outParam_indexTable[0] = result.getString("INDEXTABLE");
				outParam_hasMandt[0] = result.getString("HASMANDT");
				return true;
			} else {
				reportError(DasResponse.SC_DOES_NOT_EXISTS,
						"INDEX INSERT: Index " + indexName + " does not exist");
				return false;
			}
		} finally {
			if (result != null) {
				try {
					result.close();
				} catch (SQLException sqlex) {
					cat.logThrowableT(Severity.WARNING, loc, "INDEX INSERT: "
							+ sqlex.getMessage(), sqlex);
				}
			}
			if (selIdxDic != null) {
				try {
					selIdxDic.close();
				} catch (SQLException sqlex) {
					cat.logThrowableT(Severity.WARNING, loc, "INDEX INSERT: "
							+ sqlex.getMessage(), sqlex);
				}
			}
		}
	}

	private int getIndexPropertiesMetaData(
			final HashMap<String, PropValueAndJdbcType> propertyDefMap,
			final HashMap<String, Serializable> propValuesMap,
			final long indexId, final String indexTable) throws SQLException,
			IOException {
		// get index property meta data
		PreparedStatement selIdxCol = null;
		ResultSet result = null;
		try {
			selIdxCol = connection.prepareStatement(SEL_IDX_COL);
			selIdxCol.setLong(1, indexId);
			result = selIdxCol.executeQuery();
			int nrOfProperties = 0;
			String propName = null;
			boolean noValuesAvailable = propValuesMap.isEmpty();
			while (result.next()) {
				propName = result.getString("PROPNAME").toLowerCase();
				if (noValuesAvailable) {
					propertyDefMap.put(propName, new PropValueAndJdbcType(
							result.getString("JDBCTYPE").toUpperCase()));
				} else {
					// Note: propValuesMap does not necessarily contain an entry
					// for each propName (in case of multi-value support)
					propertyDefMap.put(propName, new PropValueAndJdbcType(
							propValuesMap.get(propName), result.getString(
									"JDBCTYPE").toUpperCase()));
				}
				nrOfProperties++;
			}
			if (nrOfProperties == 0) {
				reportError(DasResponse.SC_DOES_NOT_EXISTS,
						"INDEX INSERT: No entry for index table " + indexTable
								+ " in table BC_XMLA_INDEX_COLS");
			}
			return nrOfProperties;
		} finally {
			if (result != null) {
				try {
					result.close();
				} catch (SQLException sqlex) {
					cat.logThrowableT(Severity.WARNING, loc, "INDEX INSERT: "
							+ sqlex.getMessage(), sqlex);
				}
			}
			if (selIdxCol != null) {
				try {
					selIdxCol.close();
				} catch (SQLException sqlex) {
					cat.logThrowableT(Severity.WARNING, loc, "INDEX INSERT: "
							+ sqlex.getMessage(), sqlex);
				}
			}
		}
	}

	private void persistIndexData(
			final ArrayList<IndexDataContainer> indexDataList, final Long collID)
			throws SQLException, IOException, BodyFormatException {
		PreparedStatement insert = null;
		PreparedStatement checkSelect = null;
		PreparedStatement selColIdx = null;
		PreparedStatement insColIdx = null;
		ResultSet result = null;
		try {
			// insert all indexes into index table
			long idxId = 0;
			String indexTableName = null;
			String propName = null;
			Serializable propValue = null;
			String propType = null;
			boolean isMultiValSupported = false;
			StringBuilder checkSelectSql = null;
			StringBuilder insertSql = null;
			boolean isInsertRequired = true;
			HashMap<Integer, PropValueAndJdbcType> statementValues = null;
			int placeHolderCounter = 0;
			boolean isParamAdded = false;
			for (IndexDataContainer idxData : indexDataList) {
				isInsertRequired = true;
				// get index name
				idxId = idxData.getIndexID();
				indexTableName = idxData.getIndexTable();
				isMultiValSupported = indexTableName.toUpperCase().startsWith(
						INDEXPREFIX_MULTI);
				// check if BC_COL_INDEX entry already exists
				selColIdx = connection.prepareStatement(SEL_COL_IDX);
				selColIdx.setLong(1, idxId);
				selColIdx.setLong(2, collID);
				result = selColIdx.executeQuery();
				// insert BC_COL_INDEX entry if it does not exist
				if (!result.next()) {
					insColIdx = connection.prepareStatement(INS_COL_IDX);
					insColIdx.setLong(1, idxId);
					insColIdx.setLong(2, collID);
					insColIdx.executeUpdate();
				}
				// in case of multi-value support: check if insertion is
				// necessary
				if (isMultiValSupported) {
					placeHolderCounter = 0;
					statementValues = new HashMap<Integer, PropValueAndJdbcType>();
					checkSelectSql = new StringBuilder("SELECT 1 FROM ")
							.append(indexTableName.toUpperCase()).append(
									" WHERE ");
					isParamAdded = false;
					for (Entry<String, PropValueAndJdbcType> propertyDef : idxData
							.getPropertyDefMap().entrySet()) {
						propName = propertyDef.getKey();
						if ("counter".equals(propName)) {
							// exclude "counter" field from WHERE clause
							continue;
						}
						if (isParamAdded) {
							checkSelectSql.append(" AND ");
						}
						checkSelectSql.append(propName);
						isParamAdded = true;
						propValue = propertyDef.getValue().getPropValue();
						if (propValue == null) {
							checkSelectSql.append(" IS NULL ");
						} else {
							checkSelectSql.append(" = ?");
							statementValues.put(Integer
									.valueOf(++placeHolderCounter), propertyDef
									.getValue());
						}
					}
					// create, fill and execute SELECT statement
					checkSelect = connection.prepareStatement(checkSelectSql
							.toString());
					for (Entry<Integer, PropValueAndJdbcType> checkSelectValue : statementValues
							.entrySet()) {
						propValue = checkSelectValue.getValue().getPropValue();
						propType = checkSelectValue.getValue().getJdbcType();
						if ("BINARY".equals(propType)) {
							try {
								checkSelect.setBytes(checkSelectValue.getKey()
										.intValue(), guidGen.parseHexGUID(
										(String) propValue).toBytes());
							} catch (GUIDFormatException e) {
								throw new BodyFormatException(
										"Problem parsing GUID string: "
												+ e.getMessage());
							}
						} else {
							checkSelect.setObject(checkSelectValue.getKey()
									.intValue(), propValue);
						}
					}
					result = checkSelect.executeQuery();
					if (result.next()) {
						// INSERT not necessary
						isInsertRequired = false;
					}
				}
				if (isInsertRequired) {
					// create INSERT string
					placeHolderCounter = 0;
					statementValues = new HashMap<Integer, PropValueAndJdbcType>();
					insertSql = new StringBuilder("INSERT INTO ").append(
							indexTableName.toUpperCase()).append(" (");
					isParamAdded = false;
					for (Entry<String, PropValueAndJdbcType> propertyDef : idxData
							.getPropertyDefMap().entrySet()) {
						propName = propertyDef.getKey();
						if (isParamAdded) {
							insertSql.append(", ");
						}
						insertSql.append(propName);
						isParamAdded = true;
						statementValues.put(Integer
								.valueOf(++placeHolderCounter), propertyDef
								.getValue());
					}
					insertSql.append(") VALUES (");
					for (int i = 0; i < placeHolderCounter; i++) {
						if (i > 0) {
							insertSql.append(", ");
						}
						insertSql.append("?");
					}
					insertSql.append(")");
					// create, fill and execute INSERT statement
					// Note: There is no SQL injection danger here because the
					// value of "indexTableName" has already been checked in
					// "getIndexTableMetaData()"
					insert = connection.prepareStatement(insertSql.toString());
					for (Entry<Integer, PropValueAndJdbcType> insertValue : statementValues
							.entrySet()) {
						propValue = insertValue.getValue().getPropValue();
						if (propValue != null) {
							propType = insertValue.getValue().getJdbcType();
							if ("BINARY".equals(propType)) {
								try {
									insert.setBytes(insertValue.getKey()
											.intValue(), guidGen.parseHexGUID(
											(String) propValue).toBytes());
								} catch (GUIDFormatException e) {
									throw new BodyFormatException(
											"Problem parsing GUID string: "
													+ e.getMessage());
								}
							} else {
								insert.setObject(insertValue.getKey()
										.intValue(), propValue);
							}
						} else {
							// property value not contained in the request ->
							// allowed for multi-value support
							insert.setNull(insertValue.getKey().intValue(),
									Types.NULL);
						}
					}
					insert.executeUpdate();
				}
			}
		} finally {
			if (result != null) {
				try {
					result.close();
				} catch (SQLException sqlex) {
					cat.logThrowableT(Severity.WARNING, loc, "INDEX INSERT: "
							+ sqlex.getMessage(), sqlex);
				}
			}
			if (insert != null) {
				try {
					insert.close();
				} catch (SQLException sqlex) {
					cat.logThrowableT(Severity.WARNING, loc, "INDEX INSERT: "
							+ sqlex.getMessage(), sqlex);
				}
			}
			if (checkSelect != null) {
				try {
					checkSelect.close();
				} catch (SQLException sqlex) {
					cat.logThrowableT(Severity.WARNING, loc, "INDEX INSERT: "
							+ sqlex.getMessage(), sqlex);
				}
			}
			if (selColIdx != null) {
				try {
					selColIdx.close();
				} catch (SQLException sqlex) {
					cat.logThrowableT(Severity.WARNING, loc, "INDEX INSERT: "
							+ sqlex.getMessage(), sqlex);
				}
			}
			if (insColIdx != null) {
				try {
					insColIdx.close();
				} catch (SQLException sqlex) {
					cat.logThrowableT(Severity.WARNING, loc, "INDEX INSERT: "
							+ sqlex.getMessage(), sqlex);
				}
			}
		}
	}

	private static final class PropValueAndJdbcType {
		private Serializable propValue;
		private final String jdbcType;

		PropValueAndJdbcType(Serializable propValue, String jdbcType) {
			this.propValue = propValue;
			this.jdbcType = jdbcType;
		}

		PropValueAndJdbcType(String jdbcType) {
			this(null, jdbcType);
		}

		Serializable getPropValue() {
			return propValue;
		}

		void setPropValue(Serializable propValue) {
			this.propValue = propValue;
		}

		String getJdbcType() {
			return jdbcType;
		}
	}

	private static final class IndexDataContainer {
		private final long indexID;
		private final String indexTable;
		private final HashMap<String, PropValueAndJdbcType> propertyDefMap;

		IndexDataContainer(long indexID, String indexTable,
				HashMap<String, PropValueAndJdbcType> propertyDefMap) {
			this.indexID = indexID;
			this.indexTable = indexTable;
			this.propertyDefMap = propertyDefMap;
		}

		long getIndexID() {
			return indexID;
		}

		String getIndexTable() {
			return indexTable;
		}

		HashMap<String, PropValueAndJdbcType> getPropertyDefMap() {
			return propertyDefMap;
		}
	}
}
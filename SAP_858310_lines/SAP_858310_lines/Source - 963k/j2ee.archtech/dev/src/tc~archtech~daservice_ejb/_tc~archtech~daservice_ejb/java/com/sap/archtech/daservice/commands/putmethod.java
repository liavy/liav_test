package com.sap.archtech.daservice.commands;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Map.Entry;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

import com.sap.archtech.archconn.values.IndexPropValues;
import com.sap.archtech.daservice.data.DasResponse;
import com.sap.archtech.daservice.data.IndexData;
import com.sap.archtech.daservice.data.ResourceData;
import com.sap.archtech.daservice.data.Sapxmla_Config;
import com.sap.archtech.daservice.ejb.ArchStoreConfigLocalHome;
import com.sap.archtech.daservice.exceptions.ArchStoreConfigException;
import com.sap.archtech.daservice.exceptions.BodyFormatException;
import com.sap.archtech.daservice.exceptions.InvalidNameException;
import com.sap.archtech.daservice.storage.ParserErrorHandler;
import com.sap.archtech.daservice.storage.XmlDasDelete;
import com.sap.archtech.daservice.storage.XmlDasDeleteRequest;
import com.sap.archtech.daservice.storage.XmlDasDeleteResponse;
import com.sap.archtech.daservice.storage.XmlDasGet;
import com.sap.archtech.daservice.storage.XmlDasGetRequest;
import com.sap.archtech.daservice.storage.XmlDasGetResponse;
import com.sap.archtech.daservice.storage.XmlDasMaster;
import com.sap.archtech.daservice.storage.XmlDasPut;
import com.sap.archtech.daservice.storage.XmlDasPutRequest;
import com.sap.archtech.daservice.storage.XmlDasPutResponse;
import com.sap.archtech.daservice.util.IdProvider;
import com.sap.archtech.daservice.util.XmlSchemaProvider;
import com.sap.engine.frame.core.locking.LockException;
import com.sap.engine.frame.core.locking.TechnicalLockException;
import com.sap.engine.services.applocking.TableLocking;
import com.sap.guid.GUIDFormatException;
import com.sap.guid.GUIDGeneratorFactory;
import com.sap.guid.IGUIDGenerator;
import com.sap.security.core.server.vsi.api.Instance;
import com.sap.security.core.server.vsi.api.VSIFilterInputStream;
import com.sap.security.core.server.vsi.api.exception.VSIServiceException;
import com.sap.security.core.server.vsi.api.exception.VirusInfectionException;
import com.sap.security.core.server.vsi.api.exception.VirusScanException;

public class PutMethod extends MasterMethod {

	private final static int MAXCOLLECTIONURILENGTH = 255;
	private final static int MAXRESOURCENAMELENGTH = 100;
	private final static int READBUFFER = 1024;
	private final static String INDEX_SPLIT_TOKEN = "#";
	private final static String INDEX_ATTRIBUTE_VALUE_TOKEN = "=";
	private final static String SEL_COL_TAB = "SELECT * FROM BC_XMLA_COL WHERE URI = ?";
	private final static String SEL_COL_TBL = "SELECT COLID FROM BC_XMLA_COL WHERE URI = ?";
	private final static String SEL_RES_CHK = "SELECT RESID FROM BC_XMLA_RES WHERE COLID = ? AND DELSTATUS IN ('Y', 'N', 'P') AND RESNAME = ?";
	private final static String SEL_RES_SCH = "SELECT * FROM BC_XMLA_RES WHERE COLID = ? AND RESTYPE = 'XSD' ORDER BY RESID";
	private final static String INS_RES_PAR = "INSERT INTO BC_XMLA_RES (RESID, RESNAME, RESTYPE, RESLENGTH, CREATIONTIME, CREATIONUSER, CHECKSTATUS, ISPACKED, COLID) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
	private final static String INS_RES_TAB = "INSERT INTO BC_XMLA_RES (RESID, RESNAME, RESTYPE, RESLENGTH, CREATIONTIME, CREATIONUSER, CHECKSTATUS, ISPACKED, FINGERPRINT, COLID) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	private final static String UPD_RES_TAB = "UPDATE BC_XMLA_RES SET RESLENGTH = ?, CHECKSTATUS = ?, FINGERPRINT = ? WHERE RESID = ?";
	private final static String SEL_IDX_DIC = "SELECT INDEXID, INDEXTABLE, HASMANDT FROM BC_XMLA_INDEX_DICT WHERE INDEXNAME = ?";
	private final static String SEL_IDX_COL = "SELECT PROPNAME, JDBCTYPE FROM BC_XMLA_INDEX_COLS WHERE INDEXID = ?";
	private final static String SEL_COL_IDX = "SELECT * FROM BC_XMLA_COL_INDEX WHERE INDEXID = ? AND COLID = ?";
	private final static String INS_COL_IDX = "INSERT INTO BC_XMLA_COL_INDEX (INDEXID, COLID) VALUES (?, ?)";

	private final static String INDEXPREFIX_MULTI = "BCGEN01_XMLA";

	private final static IGUIDGenerator guidGen = GUIDGeneratorFactory
			.getInstance().createGUIDGenerator();

	private HttpServletRequest request;
	private Connection connection;
	private IdProvider idProvider;
	private TableLocking tlock;
	private ArchStoreConfigLocalHome beanLocalHome;
	private ArrayList<String> resTypeList;
	private String type;
	private String archive_path;
	private String resource_name;
	private String user;
	private String mode;
	private String check_level;
	private String index_count;
	private String checksum;
	private Timestamp dateTime;
	private String filename_win;
	private String filename_unx;

	public PutMethod(HttpServletRequest request, HttpServletResponse response,
			Connection connection, IdProvider idProvider, TableLocking tlock,
			ArchStoreConfigLocalHome beanLocalHome,
			ArrayList<String> resTypeList, String type, String archive_path,
			String resource_name, String user, String mode, String check_level,
			String index_count, String checksum, Timestamp dateTime,
			String filename_win, String filename_unx) {
		this.request = request;
		this.response = response;
		this.connection = connection;
		this.idProvider = idProvider;
		this.tlock = tlock;
		this.beanLocalHome = beanLocalHome;
		this.resTypeList = resTypeList;
		this.type = type;
		this.archive_path = archive_path;
		this.resource_name = resource_name;
		this.user = user;
		this.mode = mode;
		this.check_level = check_level;
		this.index_count = index_count;
		this.checksum = checksum;
		this.dateTime = dateTime;
		this.filename_win = filename_win;
		this.filename_unx = filename_unx;
	}

	public boolean execute() throws IOException {

		// Variables
		boolean autoNaming = false;
		boolean abapRequest = false;

		int hits = 0;
		int index_count_int = 0;

		long colId = 0;
		long resId = 0;
		long storeId = 0;
		long indexId = 0;
		long httpBodyLength = 0;

		String colType = "";
		String indexTable = "";
		String isFrozen = "";
		String validateStatus = "_";
		String digest = "";
		String indexName = "";

		ArrayList<Object> indexList = new ArrayList<Object>();
		ArrayList<IndexData> indexData = new ArrayList<IndexData>();
		IndexData idxData = null;
		ArrayList<String> propList = null;
		HashMap<String, Serializable> propValuesMap = null;
		HashMap<String, String> jdbcMap = null;

		ServletInputStream sis = null;
		VSIFilterInputStream vis_sis = null;
		ObjectInputStream ois = null;
		FileInputStream fis = null;
		StringTokenizer indexStringToken = null;
		PreparedStatement pst = null;
		PreparedStatement pst0 = null;
		PreparedStatement pst1 = null;
		PreparedStatement pst2 = null;
		PreparedStatement pst3 = null;
		PreparedStatement pst4 = null;
		PreparedStatement pst5 = null;
		PreparedStatement pst6 = null;
		PreparedStatement pst7 = null;
		PreparedStatement pst8 = null;
		PreparedStatement pst9 = null;
		ResultSet result = null;
		Sapxmla_Config sac = null;
		Instance vsInstance = null;

		// Check Request Header "type"
		if (this.type == null) {
			this.reportError(DasResponse.SC_PARAMETER_MISSING,
					"PUT: TYPE missing from request header");
			return false;
		} else if (!resTypeList.contains(this.type.toUpperCase())) {
			this
					.reportError(
							DasResponse.SC_RESOURCE_TYPE_NOT_SUPPORTED,
							"PUT: Value "
									+ this.type
									+ " of request header TYPE does not meet specifications");
			return false;
		} else
			this.type = this.type.toLowerCase();

		// Check Request Header "archive_path"
		if (this.archive_path == null) {
			this.reportError(DasResponse.SC_PARAMETER_MISSING,
					"PUT: ARCHIVE_PATH missing from request header");
			return false;
		} else {
			try {
				this.isValidName(this.archive_path, true);
			} catch (InvalidNameException inex) {

				// $JL-EXC$
				this.reportError(DasResponse.SC_INVALID_CHARACTER, "PUT: "
						+ inex.getMessage());
				return false;
			}
			this.archive_path = this.archive_path.toLowerCase();
			if (!(this.archive_path.indexOf("//") == -1)
					|| !this.archive_path.startsWith("/")
					|| !this.archive_path.endsWith("/")
					|| this.archive_path.length() < 3) {
				this
						.reportError(
								DasResponse.SC_PARAMETER_SYNTAX_INCORRECT,
								"PUT: Value "
										+ this.archive_path
										+ " of request header ARCHIVE_PATH does not meet specifications");
				return false;
			}
		}

		// Check Request Header "resource_name"
		if (this.resource_name == null) {
			this.reportError(DasResponse.SC_PARAMETER_MISSING,
					"PUT: RESOURCE_NAME missing from request header");
			return false;
		} else {
			try {
				this.isValidName(this.resource_name, false);
			} catch (InvalidNameException inex) {

				// $JL-EXC$
				this.reportError(DasResponse.SC_INVALID_CHARACTER, "PUT: "
						+ inex.getMessage());
				return false;
			}
			this.resource_name = this.resource_name.toLowerCase();

			// Check If Auto Naming Is Selected
			if (this.resource_name.length() == 0) {
				if (this.type.equalsIgnoreCase("XML")
						|| this.type.equalsIgnoreCase("BIN")) {
					autoNaming = true;
				} else {
					this
							.reportError(HttpServletResponse.SC_FORBIDDEN,
									"PUT: Auto naming is only allowed for types XML and BIN");
					return false;
				}
			} else {
				if (this.resource_name.startsWith("_")) {
					this
							.reportError(DasResponse.SC_INVALID_CHARACTER,
									"PUT: Underscore as first character is reserved for XML DAS autonaming");
					return false;
				}
			}

			// Check If Resource Name Length Exceeds Maximum Length
			if ((this.resource_name.length()) > MAXRESOURCENAMELENGTH) {
				this.reportError(DasResponse.SC_PARAMETER_SYNTAX_INCORRECT,
						"PUT: Length of resource name " + this.resource_name
								+ " exceeds limit of " + MAXRESOURCENAMELENGTH);
				return false;
			}

			// Check If Resource Name Ends With A Dot
			if ((this.resource_name.endsWith("."))) {
				this
						.reportError(
								DasResponse.SC_INVALID_CHARACTER,
								"PUT: Value "
										+ this.resource_name
										+ " of request header RESOURCE_NAME does not meet specifications");
				return false;
			}
		}

		// Check Request Header "user"
		if ((this.user == null) || (this.user.length() == 0)) {
			this.reportError(DasResponse.SC_PARAMETER_MISSING,
					"PUT: USER missing from request header");
			return false;
		}

		// Check Request Header "mode"
		if (this.mode == null)
			this.mode = "STORE";
		else if (!((this.mode.equalsIgnoreCase("STORE")) || (this.mode
				.equalsIgnoreCase("NOSTORE")))) {
			this.reportError(DasResponse.SC_KEYWORD_UNKNOWN, "PUT: Value "
					+ this.mode
					+ " of request header MODE does not meet specifications");
			return false;
		}

		// Check Request Header "check_level"
		if (this.check_level == null)
			this.check_level = "NO";
		else if (!((this.check_level.equalsIgnoreCase("NO"))
				|| (this.check_level.equalsIgnoreCase("PARSE")) || (this.check_level
				.equalsIgnoreCase("VALIDATE")))) {
			this
					.reportError(
							DasResponse.SC_KEYWORD_UNKNOWN,
							"PUT: Value "
									+ this.check_level
									+ " of request header CHECK_LEVEL does not meet specifications");
			return false;
		}
		if (this.check_level.equalsIgnoreCase("VALIDATE")
				&& !this.type.equals("xml")) {
			this.reportError(HttpServletResponse.SC_FORBIDDEN,
					"PUT: Only type XML can be validated");
			return false;
		}
		if (this.type.equals("bin")
				&& this.check_level.equalsIgnoreCase("PARSE")) {
			this.reportError(HttpServletResponse.SC_FORBIDDEN,
					"PUT: Only types XML, XSD and XSL can be parsed");
			return false;
		}

		// Check Request Header "index_count"
		if (this.index_count == null) {
			abapRequest = true;
		} else {
			abapRequest = false;
			try {
				index_count_int = Integer.parseInt(this.index_count);
			} catch (NumberFormatException nfex) {

				// $JL-EXC$
				this
						.reportError(
								DasResponse.SC_KEYWORD_UNKNOWN,
								"PUT: Value "
										+ this.index_count
										+ " of request header INDEX_COUNT does not meet specifications");
				return false;
			}
		}

		// Check Request Header "checksum"
		if (this.checksum == null) {
			this.checksum = "N";
		} else if (!(this.checksum.equalsIgnoreCase("Y") || this.checksum
				.equalsIgnoreCase("N"))) {
			this
					.reportError(
							DasResponse.SC_KEYWORD_UNKNOWN,
							"PUT: Value "
									+ this.checksum
									+ " of request header CHECKSUM does not meet specifications");
			return false;
		}

		// Get Virus Scan Instance
		try {
			if (MasterMethod.vsiService != null)
				vsInstance = MasterMethod.vsiService
						.getInstance("archiving_DAS");
		} catch (Exception ex) {
			// $JL-EXC$
			vsInstance = null;
		}

		// Check Request Headers "filename_win" And "filename_unx"
		if ((this.filename_win != null) || (this.filename_unx != null)) {
			String filename = "";

			// Unix Operation System
			if (System.getProperty("file.separator").startsWith("/")) {
				if (this.filename_unx == null) {
					this.reportError(DasResponse.SC_IO_ERROR,
							"PUT: Unix file name is missing for Windows file name "
									+ this.filename_win);
					return false;
				} else {
					filename = this.filename_unx;
				}
			}

			// Windows Operating System
			else {
				if (this.filename_win == null) {
					this.reportError(DasResponse.SC_IO_ERROR,
							"PUT: Windows file name is missing for Unix file name "
									+ this.filename_unx);
					return false;
				} else {

					filename = this.filename_win;
				}
			}

			// Check If Source File Exists, Is A File And Is Readable
			File f = new File(filename);
			if (!f.exists()) {
				this.reportError(DasResponse.SC_IO_ERROR, "PUT: File "
						+ filename + " does not exist");
				return false;
			}
			if (!f.isFile()) {
				this.reportError(DasResponse.SC_IO_ERROR, "PUT: File "
						+ filename + " is a directory");
				return false;
			}
			if (!f.canRead()) {
				this.reportError(DasResponse.SC_IO_ERROR, "PUT: File "
						+ filename + " is not readable");
				return false;
			}

			// Get Source File Stream
			fis = new FileInputStream(filename);

			// Do Virus Scan Of Source File If Selected
			if (vsInstance != null) {
				try {

					// Perform Virus Scan
					if (!vsInstance.scanFile(filename)) {
						this.reportError(DasResponse.SC_VIRUS_SCAN_ERROR,
								"PUT: Virus Scan for source file " + filename
										+ " failed");
						return false;
					}
				} catch (VirusScanException vsex) {

					// VirusScanException Occurred
					this.reportError(DasResponse.SC_VIRUS_SCAN_ERROR,
							"PUT: Virus Scan for source file " + filename
									+ " failed: VirusScanException occurred: "
									+ vsex.getLocalizedMessage());
					return false;
				} catch (VirusInfectionException viex) {

					// VirusInfectionException Occurred
					this
							.reportError(
									DasResponse.SC_VIRUS_SCAN_ERROR,
									"PUT: Virus Scan for source file "
											+ filename
											+ " failed: VirusInfectionException occurred: "
											+ viex.getLocalizedMessage());
					return false;
				} catch (VSIServiceException vsisex) {

					// VSIServiceException Occurred
					this.reportError(DasResponse.SC_VIRUS_SCAN_ERROR,
							"PUT: Virus Scan for source file " + filename
									+ " failed: VSIServiceException occurred: "
									+ vsisex.getLocalizedMessage());
					return false;
				} catch (Exception ex) {

					// Exception Occurred
					this.reportError(DasResponse.SC_VIRUS_SCAN_ERROR,
							"PUT: Virus Scan for source file " + filename
									+ " failed: Exception occurred: "
									+ ex.getLocalizedMessage());
					return false;
				} finally {
					if (MasterMethod.vsiService != null)
						MasterMethod.vsiService.releaseInstance(vsInstance);
				}
			}
		}

		// Get Servlet Input Stream
		sis = request.getInputStream();

		// Get Index Data From HTTP Body
		if (abapRequest == true) // ABAP Request
		{

			ByteArrayOutputStream baos = null;
			int size = 0;
			boolean furtherLoop = true;
			try {
				do {
					baos = new ByteArrayOutputStream();
					byte[] buffer = new byte[READBUFFER];
					int counter = 0;
					do {
						counter = sis.readLine(buffer, 0, READBUFFER);
						baos.write(buffer, 0, counter);
					} while (!(buffer[counter - 1] == 10)); // line feed
					size = baos.size();
					if (size == 2) // end of index data
					{
						furtherLoop = false;
					} else {
						byte[] idxBuffer = new byte[size];
						idxBuffer = baos.toByteArray();
						indexList.add(new ByteArrayInputStream(idxBuffer));
					}
					baos.close();
				} while (furtherLoop);
			} catch (Exception ex) {
				MasterMethod.cat.errorT(loc,
						"PUT: An error occurred during index data deserialization: "
								+ getStackTrace(ex));
				this.reportError(DasResponse.SC_BODY_FORMAT_CORRUPT,
						"PUT: An error occurred during index data deserialization: "
								+ ex.toString());
				return false;
			}
		}

		// Java Request
		else {

			// Get Index Data From HTTP Body
			try {
				if (index_count_int > 0) {
					for (int i = 0; i < index_count_int; i++) {
						ois = new ObjectInputStream(sis);
						indexList.add((IndexPropValues) ois.readObject());
					}
				}
			} catch (ClassNotFoundException cnfex) {
				MasterMethod.cat.errorT(loc,
						"PUT: An error occurred during index data deserialization: "
								+ getStackTrace(cnfex));
				this.reportError(DasResponse.SC_BODY_FORMAT_CORRUPT,
						"PUT: An error occurred during index data deserialization: "
								+ cnfex.toString());
				return false;
			} catch (Exception ex) {
				MasterMethod.cat.errorT(loc,
						"PUT: An error occurred during index data deserialization: "
								+ getStackTrace(ex));
				this.reportError(DasResponse.SC_BODY_FORMAT_CORRUPT,
						"PUT: An error occurred during index data deserialization: "
								+ ex.toString());
				return false;
			}
		}

		// Get Resource Id And Do Auto Naming If Necessary
		try {
			if (this.mode.equalsIgnoreCase("STORE")) {

				// Get New Resource Id
				resId = this.idProvider.getId("BC_XMLA_RES");

				// Generate Auto Name
				if (autoNaming == true) {
					this.resource_name = "_" + resId + "." + this.type;
				}
			}
		} catch (SQLException sqlex) {
			this.reportError(DasResponse.SC_SQL_ERROR,
					"PUT: While resource autonaming an SQL error occurred: "
							+ sqlex.toString(), sqlex);
			return false;
		}

		// Do Virus Scan Of Servlet Stream If Selected
		if ((vsInstance != null)
				&& (!((this.filename_win != null) || (this.filename_unx != null)))) {
			try {

				// Create VSI Filter Input Stream
				vis_sis = vsiService.createVSIStream(sis);

				// Perform Virus Scan
				if (!vsInstance.scanStream(this.resource_name, vis_sis, resId)) {
					this.reportError(DasResponse.SC_VIRUS_SCAN_ERROR,
							"PUT: Virus Scan of source stream for resource "
									+ this.resource_name + " failed");
					return false;
				}
			} catch (VirusScanException vsex) {

				// VirusScanException Occurred
				this.reportError(DasResponse.SC_VIRUS_SCAN_ERROR,
						"PUT: Virus Scan of source stream for resource "
								+ this.resource_name
								+ " failed: VirusScanException occurred: "
								+ vsex.getLocalizedMessage());
				return false;
			} catch (VirusInfectionException viex) {

				// VirusInfectionException Occurred
				this.reportError(DasResponse.SC_VIRUS_SCAN_ERROR,
						"PUT: Virus Scan of source stream for resource "
								+ this.resource_name
								+ " failed: VirusInfectionException occurred: "
								+ viex.getLocalizedMessage());
				return false;
			} catch (VSIServiceException vsisex) {

				// VSIServiceException Occurred
				this.reportError(DasResponse.SC_VIRUS_SCAN_ERROR,
						"PUT: Virus Scan of source stream for resource "
								+ this.resource_name
								+ " failed: VSIServiceException occurred: "
								+ vsisex.getLocalizedMessage());
				return false;
			} catch (Exception ex) {

				// Exception Occurred
				this.reportError(DasResponse.SC_VIRUS_SCAN_ERROR,
						"PUT: Virus Scan of source stream for resource "
								+ this.resource_name
								+ " failed: Exception occurred: "
								+ ex.getLocalizedMessage());
				return false;
			} finally {
				if (MasterMethod.vsiService != null)
					MasterMethod.vsiService.releaseInstance(vsInstance);
			}
		}

		// Prepare Index Data For Insertion
		boolean errorOccurred = false;
		boolean isMultiValSupported = false;
		try {

			// Extract Index Data From HTTP Body
			for (int m = 0; m < indexList.size(); m++) {

				if (errorOccurred)
					break;

				// Create New IndexData Object
				idxData = new IndexData();
				propList = new ArrayList<String>();
				jdbcMap = new HashMap<String, String>();
				indexName = "";
				String hasMandt = "";

				// Case Distinction
				if (abapRequest == true) // ABAP Request
				{

					// Get Index Instance
					BufferedReader br = new BufferedReader(
							new InputStreamReader(
									(ByteArrayInputStream) indexList.get(m),
									"UTF-8"));
					String indexLine = br.readLine();
					int hasMandtAttr = indexLine.toUpperCase()
							.indexOf("#MANDT");
					indexStringToken = new StringTokenizer(indexLine,
							INDEX_SPLIT_TOKEN);
					br.close();
					indexName = indexStringToken.nextToken().toLowerCase()
							.trim();
					propValuesMap = new HashMap<String, Serializable>();

					// Get Corresponding Index Table
					pst1 = connection.prepareStatement(SEL_IDX_DIC);
					pst1.setString(1, indexName);
					result = pst1.executeQuery();
					hits = 0;
					while (result.next()) {
						indexId = result.getLong("INDEXID");
						indexTable = result.getString("INDEXTABLE");
						hasMandt = result.getString("HASMANDT");
						hits++;
					}
					result.close();
					pst1.close();
					if (hits == 0) {
						this.reportError(DasResponse.SC_DOES_NOT_EXISTS,
								"PUT: Index " + indexName + " does not exist");
						errorOccurred = true;
						break;
					}

					isMultiValSupported = indexTable
							.startsWith(INDEXPREFIX_MULTI);
					// Check If Mandant Setting Is Correct
					if ((hasMandt.equalsIgnoreCase("Y") == true)
							&& (hasMandtAttr == -1)) {
						this.reportError(DasResponse.SC_CLIENT_MISSING,
								"PUT: Property MANDT missing for client-dependent index "
										+ indexName);
						errorOccurred = true;
						break;
					}

					// Get Index Meta Data
					pst2 = connection.prepareStatement(SEL_IDX_COL);
					pst2.setLong(1, indexId);
					result = pst2.executeQuery();
					hits = 0;
					String propName = "";
					while (result.next()) {
						propName = result.getString("PROPNAME").toLowerCase();
						propList.add(propName);
						jdbcMap.put(propName, result.getString("JDBCTYPE")
								.toUpperCase());
						hits++;
					}
					result.close();
					pst2.close();
					if (hits == 0) {
						this.reportError(DasResponse.SC_DOES_NOT_EXISTS,
								"PUT: No entry for index table " + indexTable
										+ " in table BC_XMLA_INDEX_COLS");
						errorOccurred = true;
						break;
					}

					// Get All Index Parts
					while (indexStringToken.hasMoreTokens()) {
						String indexToken = indexStringToken.nextToken();
						if (indexToken.indexOf(INDEX_ATTRIBUTE_VALUE_TOKEN) != -1) {

							// Get Property Name
							String attributeName = indexToken
									.substring(
											0,
											indexToken
													.indexOf(INDEX_ATTRIBUTE_VALUE_TOKEN))
									.toLowerCase().trim();

							// Get And Check JDBC Type
							String jdbcType = jdbcMap.get(attributeName);
							if (jdbcType == null) {
								this.reportError(
										DasResponse.SC_DOES_NOT_EXISTS,
										"PUT: Property " + attributeName
												+ " does not exist in index "
												+ indexName);
								errorOccurred = true;
								break;
							}

							// Get Property Value
							String attributeValue = indexToken
									.substring(
											indexToken
													.indexOf(INDEX_ATTRIBUTE_VALUE_TOKEN) + 1,
											indexToken.length());

							// Check If Property Value Exists
							if (attributeValue.length() == 0
									&& !jdbcType.startsWith("VARCHAR")) {
								this.reportError(
										DasResponse.SC_BODY_FORMAT_CORRUPT,
										"PUT: Property " + attributeName
												+ " of index " + indexName
												+ " has no value");
								errorOccurred = true;
								break;
							}

							// Check IF Property Value Format Of TIMESTAMP JDBC
							// Type Is Valid
							if (jdbcType.startsWith("VARCHAR")) {
								// empty strings are not supported
								if ("".equals(attributeValue)) {
									attributeValue = null;
								}
								propValuesMap
										.put(attributeName, attributeValue);
							} else if (jdbcType.startsWith("SMALLINT")) {
								propValuesMap.put(attributeName, new Short(
										attributeValue));
							} else if (jdbcType.startsWith("INTEGER")) {
								propValuesMap.put(attributeName, new Integer(
										attributeValue));
							} else if (jdbcType.startsWith("BIGINT")) {
								propValuesMap.put(attributeName, new Long(
										attributeValue));
							} else if (jdbcType.startsWith("REAL")) {
								propValuesMap.put(attributeName, new Float(
										attributeValue));
							} else if (jdbcType.startsWith("DOUBLE")) {
								propValuesMap.put(attributeName, new Double(
										attributeValue));
							} else if (jdbcType.startsWith("TIMESTAMP")) {
								SimpleDateFormat sdf;
								if (attributeValue.length() < 20)
									sdf = new SimpleDateFormat(
											"yyyy-MM-dd HH:mm:ss z");
								else
									sdf = new SimpleDateFormat(
											"yyyy-MM-dd HH:mm:ss.SSS z");
								java.util.Date date = sdf.parse(attributeValue
										+ " GMT");
								propValuesMap.put(attributeName, new Timestamp(
										date.getTime()));
							} else if (jdbcType.startsWith("BINARY")) {
								propValuesMap
										.put(attributeName, attributeValue);
							} else {
								this
										.reportError(
												HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
												"PUT: Index table "
														+ indexTable
														+ " has no property with "
														+ jdbcType + " type");
								errorOccurred = true;
								break;
							}
						} else {
							this.reportError(
									DasResponse.SC_BODY_FORMAT_CORRUPT,
									"PUT: Error while inserting entry into index table "
											+ indexTable
											+ ", due to missing \'"
											+ INDEX_ATTRIBUTE_VALUE_TOKEN
											+ "\' token");
							errorOccurred = true;
							break;
						}
					} // end while
					if (errorOccurred)
						break;

					// check if all index properties have been passed (only in
					// case of missing multi-value support)
					if (!isMultiValSupported && (propValuesMap.size() != hits)) {
						this.reportError(DasResponse.SC_INDEX_PROPERTY_MISSING,
								"PUT: Passed properties for index " + indexName
										+ " are not complete");
						errorOccurred = true;
						break;
					}
				}

				// Java Request
				else {

					// Get Index Data Set
					IndexPropValues indexPropValues = (IndexPropValues) indexList
							.get(m);
					indexName = indexPropValues.getIndexname();
					propValuesMap = indexPropValues.getPropertyValues();

					// Get Corresponding Index Table
					pst1 = connection.prepareStatement(SEL_IDX_DIC);
					pst1.setString(1, indexName);
					result = pst1.executeQuery();
					hits = 0;
					while (result.next()) {
						indexId = result.getLong("INDEXID");
						indexTable = result.getString("INDEXTABLE");
						hasMandt = result.getString("HASMANDT");
						hits++;
					}
					result.close();
					pst1.close();
					if (hits == 0) {
						this.reportError(DasResponse.SC_DOES_NOT_EXISTS,
								"PUT: Index " + indexName + " does not exist");
						errorOccurred = true;
						break;
					}

					isMultiValSupported = indexTable.toUpperCase().startsWith(
							INDEXPREFIX_MULTI);
					// Check If Mandant Setting Is Correct
					if ((hasMandt.equalsIgnoreCase("Y"))
							&& !(propValuesMap.containsKey("mandt"))) {
						this.reportError(DasResponse.SC_CLIENT_MISSING,
								"PUT: Property MANDT missing for client-dependent index "
										+ indexName);
						errorOccurred = true;
						break;
					}

					// Get Index Meta Data
					pst2 = connection.prepareStatement(SEL_IDX_COL);
					pst2.setLong(1, indexId);
					result = pst2.executeQuery();
					hits = 0;
					while (result.next()) {
						String propName = result.getString("PROPNAME")
								.toLowerCase();
						propList.add(propName);
						jdbcMap.put(propName, result.getString("JDBCTYPE")
								.toUpperCase());
						hits++;
					}
					result.close();
					pst2.close();
					if (hits == 0) {
						this.reportError(DasResponse.SC_DOES_NOT_EXISTS,
								"PUT: No entry for index table " + indexTable
										+ " in table BC_XMLA_INDEX_COLS");
						errorOccurred = true;
						break;
					}

					// check if all index properties have been passed (only in
					// case of missing multi-value support)
					if (!isMultiValSupported && (propValuesMap.size() != hits)) {
						this.reportError(DasResponse.SC_INDEX_PROPERTY_MISSING,
								"PUT: Passed properties for index " + indexName
										+ " are not complete");
						errorOccurred = true;
						break;
					}

					// If Necessary Convert Index Property Values Into Correct
					// Type
					for (int k = 0; k < propList.size(); k++) {
						String pName = propList.get(k);
						String pType = jdbcMap.get(pName);
						Serializable obj = propValuesMap.get(pName);
						if (isMultiValSupported && obj == null) {
							// no property value contained in the request ->
							// allowed in case of multi-value support
							continue;
						}

						if (pType.startsWith("VARCHAR")) {
							if (!(obj instanceof String)) {
								this
										.reportError(
												DasResponse.SC_WRONG_INDEX_PROPERTY_TYPE,
												"PUT: Property " + pName
														+ " of index table "
														+ indexTable
														+ " has a wrong type");
								errorOccurred = true;
								break;
							} else if ("".equals((String) obj)) {
								// empty strings are not supported
								propValuesMap.put(pName, null);
							}
						} else if (pType.startsWith("SMALLINT")) {
							if (obj instanceof String)
								propValuesMap.put(pName, Short
										.valueOf(((String) obj)));
							else if (!(obj instanceof Short)) {
								this
										.reportError(
												DasResponse.SC_WRONG_INDEX_PROPERTY_TYPE,
												"PUT: Property " + pName
														+ " of index table "
														+ indexTable
														+ " has a wrong type");
								errorOccurred = true;
								break;
							}
						} else if (pType.startsWith("INTEGER")) {
							if (obj instanceof String)
								propValuesMap.put(pName, Integer
										.valueOf(((String) obj)));
							else if (!(obj instanceof Integer)) {
								this
										.reportError(
												DasResponse.SC_WRONG_INDEX_PROPERTY_TYPE,
												"PUT: Property " + pName
														+ " of index table "
														+ indexTable
														+ " has a wrong type");
								errorOccurred = true;
								break;
							}
						} else if (pType.startsWith("BIGINT")) {
							if (obj instanceof String)
								propValuesMap.put(pName, Long
										.valueOf(((String) obj)));
							else if (!(obj instanceof Long)) {
								this
										.reportError(
												DasResponse.SC_WRONG_INDEX_PROPERTY_TYPE,
												"PUT: Property " + pName
														+ " of index table "
														+ indexTable
														+ " has a wrong type");
								errorOccurred = true;
								break;
							}
						} else if (pType.startsWith("REAL")) {
							if (obj instanceof String)
								propValuesMap.put(pName, Float
										.valueOf(((String) obj)));
							else if (!(obj instanceof Float)) {
								this
										.reportError(
												DasResponse.SC_WRONG_INDEX_PROPERTY_TYPE,
												"PUT: Property " + pName
														+ " of index table "
														+ indexTable
														+ " has a wrong type");
								errorOccurred = true;
								break;
							}
						} else if (pType.startsWith("DOUBLE")) {
							if (obj instanceof String)
								propValuesMap.put(pName, Double
										.valueOf(((String) obj)));
							else if (!(obj instanceof Double)) {
								this
										.reportError(
												DasResponse.SC_WRONG_INDEX_PROPERTY_TYPE,
												"PUT: Property " + pName
														+ " of index table "
														+ indexTable
														+ " has a wrong type");
								errorOccurred = true;
								break;
							}
						} else if (pType.startsWith("TIMESTAMP")) {
							if (obj instanceof String) {
								SimpleDateFormat sdf;
								if (((String) obj).length() < 20)
									sdf = new SimpleDateFormat(
											"yyyy-MM-dd HH:mm:ss z");
								else
									sdf = new SimpleDateFormat(
											"yyyy-MM-dd HH:mm:ss.SSS z");
								propValuesMap.put(pName, new Timestamp(sdf
										.parse(((String) obj) + " GMT")
										.getTime()));
							} else if (!(obj instanceof Timestamp)) {
								this
										.reportError(
												DasResponse.SC_WRONG_INDEX_PROPERTY_TYPE,
												"PUT: Property " + pName
														+ " of index table "
														+ indexTable
														+ " has a wrong type");
								errorOccurred = true;
								break;
							}
						} else if (pType.startsWith("BINARY")) {
							if (!(obj instanceof String)) {
								this
										.reportError(
												DasResponse.SC_WRONG_INDEX_PROPERTY_TYPE,
												"PUT: Property " + pName
														+ " of index table "
														+ indexTable
														+ " has a wrong type");
								errorOccurred = true;
								break;
							}
						} else {
							this.reportError(
									DasResponse.SC_WRONG_INDEX_PROPERTY_TYPE,
									"PUT: Property " + pName
											+ " of index table " + indexTable
											+ " has a wrong type");
							errorOccurred = true;
							break;
						}
					} // end for
					if (errorOccurred)
						break;
				} // end if

				// Add RESID Column
				propList.add("resid");
				propValuesMap.put("resid", new Long(resId));
				jdbcMap.put("resid", "BIGINT");
				// calculate value for "counter" column
				if (isMultiValSupported) {
					propList.add("counter");
					propValuesMap.put("counter", idProvider.getId(indexTable
							.toUpperCase()));
					jdbcMap.put("counter", "BIGINT");
				}
				// Set Index Data
				idxData.setId(indexId);
				idxData.setName(indexTable);
				idxData.setPropList(propList);
				idxData.setPropValuesMap(propValuesMap);
				idxData.setJdbcMap(jdbcMap);

				// Add Index Data Object To ArrayList
				indexData.add(idxData);
			} // end for
		} catch (ParseException pex) {
			this.reportError(DasResponse.SC_WRONG_TIMESTAMP_FORMAT,
					"PUT: A timestamp property of index " + indexName
							+ " has wrong format: " + pex.toString(), pex);
			errorOccurred = true;
		} catch (NumberFormatException nfex) {
			this.reportError(DasResponse.SC_WRONG_INDEX_PROPERTY_TYPE,
					"PUT: While casting the properties of index " + indexName
							+ " an NumberFormatException occurred: "
							+ nfex.toString(), nfex);
			errorOccurred = true;
		} catch (SQLException sqlex) {
			this.reportError(DasResponse.SC_SQL_ERROR,
					"PUT: While preparing index " + indexName
							+ " an SQLException occurred: " + sqlex.toString(),
					sqlex);
			errorOccurred = true;
		} catch (Exception ex) {
			this.reportError(HttpServletResponse.SC_CONFLICT,
					"PUT: While preparing index " + indexName
							+ " an Exception occurred: " + ex.toString(), ex);
			errorOccurred = true;
		} finally {
			try {
				if (pst1 != null)
					pst1.close();
				if (pst2 != null)
					pst2.close();
			} catch (SQLException sqlex) {
				this.reportError(DasResponse.SC_SQL_ERROR, "PUT: "
						+ sqlex.toString(), sqlex);
				errorOccurred = true;
			}
		}
		if (errorOccurred)
			return false;

		// Put Resource On Archive Store
		try {

			// Adjust Archive Path For Further Processing
			this.archive_path = this.archive_path.substring(0,
					this.archive_path.length() - 1);

			// Check If Collection Exists
			pst3 = connection.prepareStatement(SEL_COL_TAB);
			pst3.setString(1, this.archive_path.trim());
			result = pst3.executeQuery();
			hits = 0;
			while (result.next()) {
				colId = result.getLong("COLID");
				isFrozen = result.getString("FROZEN");
				storeId = result.getLong("STOREID");
				colType = result.getString("COLTYPE");
				hits++;
			}
			result.close();
			pst3.close();
			if (hits == 0) {
				int lastSlashNum = this.archive_path.lastIndexOf("/");
				int strLen = this.archive_path.length();
				if ((lastSlashNum != -1) && (lastSlashNum < strLen))
					this.reportError(DasResponse.SC_DOES_NOT_EXISTS,
							"PUT: Collection "
									+ this.archive_path.substring(
											lastSlashNum + 1, strLen)
									+ " does not exist");
				else
					this.reportError(DasResponse.SC_DOES_NOT_EXISTS,
							"PUT: Collection does not exist");
				errorOccurred = true;
			}

			// Check If Collection Is Frozen
			if (!errorOccurred) {
				if (!isFrozen.equalsIgnoreCase("N")) {
					int lastSlashNum = this.archive_path.lastIndexOf("/");
					int strLen = this.archive_path.length();
					if ((lastSlashNum != -1) && (lastSlashNum < strLen))
						this.reportError(DasResponse.SC_COLLECTION_FROZEN,
								"PUT: Collection "
										+ this.archive_path.substring(
												lastSlashNum + 1, strLen)
										+ " is frozen");
					else
						this.reportError(DasResponse.SC_COLLECTION_FROZEN,
								"PUT: Collection is frozen");
					errorOccurred = true;
				}
			}

			// Check If Collection Is A Home Or Application Collection
			if (!errorOccurred) {
				if (!(colType.equalsIgnoreCase("H") || colType
						.equalsIgnoreCase("A"))) {
					int lastSlashNum = this.archive_path.lastIndexOf("/");
					int strLen = this.archive_path.length();
					if ((lastSlashNum != -1) && (lastSlashNum < strLen))
						this
								.reportError(
										DasResponse.SC_SYSTEM_COLLECTION,
										"PUT: Collection "
												+ this.archive_path.substring(
														lastSlashNum + 1,
														strLen)
												+ " is not a home or application collection");
					else
						this
								.reportError(DasResponse.SC_SYSTEM_COLLECTION,
										"PUT: Collection is not a home or application collection");
					errorOccurred = true;
				}
			}

			// Check If Archive Store Is Assigned
			if (!errorOccurred) {
				if (storeId == 0) {
					int lastSlashNum = this.archive_path.lastIndexOf("/");
					int strLen = this.archive_path.length();
					if ((lastSlashNum != -1) && (lastSlashNum < strLen))
						this
								.reportError(
										DasResponse.SC_STORE_NOT_ASSIGNED,
										"PUT: Collection "
												+ this.archive_path.substring(
														lastSlashNum + 1,
														strLen)
												+ " is either not yet assigned or not uniquely assigned to an archive store");
					else
						this
								.reportError(
										DasResponse.SC_SYSTEM_COLLECTION,
										"PUT: Collection is either not yet assigned or not uniquely assigned to an archive store");
					errorOccurred = true;
				}
			}

			// Set 'Logical' Exclusive Table Lock
			if (!errorOccurred) {
				try {
					HashMap<String, Object> primKeyMap = new HashMap<String, Object>();
					primKeyMap.put("COLID", new Long(colId));
					primKeyMap.put("RESNAME", new String(this.resource_name
							.trim()));
					tlock.lock(TableLocking.LIFETIME_TRANSACTION, connection,
							"BC_XMLA_LOCKING", primKeyMap,
							TableLocking.MODE_EXCLUSIVE_NONCUMULATIVE);
				} catch (LockException lex) {
					int lastSlashNum = this.archive_path.lastIndexOf("/");
					int strLen = this.archive_path.length();
					if ((lastSlashNum != -1) && (lastSlashNum < strLen))
						this.reportError(HttpServletResponse.SC_CONFLICT,
								"PUT: The lock for the collection "
										+ this.archive_path.substring(
												lastSlashNum + 1, strLen)
										+ " and resource " + this.resource_name
										+ " cannot be granted", lex);
					else
						this.reportError(HttpServletResponse.SC_CONFLICT,
								"PUT: The lock for the collection id " + colId
										+ " and resource " + this.resource_name
										+ " cannot be granted", lex);
					errorOccurred = true;
				} catch (TechnicalLockException tlex) {
					int lastSlashNum = this.archive_path.lastIndexOf("/");
					int strLen = this.archive_path.length();
					if ((lastSlashNum != -1) && (lastSlashNum < strLen))
						this
								.reportError(HttpServletResponse.SC_CONFLICT,
										"PUT: A technical error occurred while locking collection "
												+ this.archive_path.substring(
														lastSlashNum + 1,
														strLen)
												+ " and resource "
												+ this.resource_name, tlex);
					else
						this.reportError(HttpServletResponse.SC_CONFLICT,
								"PUT: A technical error occurred while locking collection id "
										+ colId + " and resource "
										+ this.resource_name, tlex);
					errorOccurred = true;
				}
			}

			// Check If A Collection With Identical Name As Resource Exists
			if (!errorOccurred) {
				if ((this.archive_path.length() + 1 + this.resource_name
						.length()) <= MAXCOLLECTIONURILENGTH) {
					pst0 = connection.prepareStatement(SEL_COL_TBL);
					pst0.setString(1, this.archive_path.trim() + "/"
							+ this.resource_name.trim());
					result = pst0.executeQuery();
					hits = 0;
					while (result.next())
						hits++;
					result.close();
					pst0.close();
					if (hits != 0) {
						this
								.reportError(
										HttpServletResponse.SC_CONFLICT,
										"PUT: Name "
												+ this.resource_name
												+ " already being used for an existing collection");
						errorOccurred = true;
					}
				}
			}

			// When No Auto Naming Check If Resource Exists In Table BC_XMLA_RES
			if (!errorOccurred) {
				if (autoNaming == false) {
					if (mode.equalsIgnoreCase("STORE")) {
						try {

							// Insert New Entry Into Table BC_XMLA_RES
							pst4 = connection.prepareStatement(INS_RES_PAR);
							pst4.setLong(1, resId);
							pst4.setString(2, this.resource_name.trim());
							pst4.setString(3, this.type.toUpperCase().trim());
							pst4.setInt(4, 0);
							pst4.setTimestamp(5, dateTime);
							pst4.setString(6, this.user);
							pst4.setString(7, "_");
							pst4.setString(8, "N");
							pst4.setLong(9, colId);
							pst4.executeUpdate();
							pst4.close();
						} catch (SQLException sqlex) {

							// Check If Resource Already Exists In Table
							// BC_XMLA_RES
							pst5 = connection.prepareStatement(SEL_RES_CHK);
							pst5.setLong(1, colId);
							pst5.setString(2, this.resource_name.trim());
							result = pst5.executeQuery();
							hits = 0;
							while (result.next())
								hits++;
							result.close();
							pst5.close();
							if (hits != 0) {
								this.reportError(
										HttpServletResponse.SC_CONFLICT,
										"PUT: Resource "
												+ this.resource_name.trim()
												+ " already exists");
								errorOccurred = true;
							} else {
								throw sqlex;
							}
						}
					} else {

						// Check If Resource Already Exists
						pst5 = connection.prepareStatement(SEL_RES_CHK);
						pst5.setLong(1, colId);
						pst5.setString(2, this.resource_name.trim());
						result = pst5.executeQuery();
						hits = 0;
						while (result.next())
							hits++;
						result.close();
						pst5.close();
						if (hits != 0) {
							this.reportError(HttpServletResponse.SC_CONFLICT,
									"PUT: Resource "
											+ this.resource_name.trim()
											+ " already exists");
							errorOccurred = true;
						}
					}
				}
			}

			Schema schema = null;
			if (!errorOccurred) {
				// Get Archive Store Configuration Data
				sac = this.getArchStoreConfigObject(beanLocalHome, storeId);

				// Check If A XML Schema Object Already Exists In Schema Cache
				if (type.equalsIgnoreCase("XSD")
						&& XmlSchemaProvider.containsSchemaObject(colId))
					XmlSchemaProvider.removeSchemaObject(colId);

				// Obtain XML Schema Object
				ArrayList<ResourceData> schemaList = new ArrayList<ResourceData>();
				if (this.check_level.equalsIgnoreCase("VALIDATE")) {

					// Check If XML Schema Object Is Already In Schema Pool
					schema = XmlSchemaProvider.getSchemaObject(colId);

					// Get XML Schema From Archive Store And Add To Schema Pool
					if (schema == null) {

						// Get All XML Schemas Entries From Table BC_XMLA_RES
						pst6 = connection.prepareStatement(SEL_RES_SCH);
						pst6.setLong(1, colId);
						result = pst6.executeQuery();
						hits = 0;
						while (result.next()) {
							if (result.getString("ISPACKED").equalsIgnoreCase(
									"Y"))
								schemaList.add(new ResourceData(result
										.getLong("RESID"), this.archive_path
										.trim()
										+ "/" + result.getString("PACKNAME"),
										null, result.getInt("PACKLENGTH"),
										result.getLong("OFFSET"), result
												.getString("PACKNAME"), "Y"));
							else
								schemaList.add(new ResourceData(result
										.getLong("RESID"), this.archive_path
										.trim()
										+ "/" + result.getString("RESNAME"),
										null, 0, 0, null, "N"));
							hits++;
						}
						result.close();
						pst6.close();
						if (hits == 0) {
							int lastSlashNum = this.archive_path
									.lastIndexOf("/");
							int strLen = this.archive_path.length();
							if ((lastSlashNum != -1) && (lastSlashNum < strLen))
								this
										.reportError(
												DasResponse.SC_DOES_NOT_EXISTS,
												"PUT: Collection "
														+ this.archive_path
																.substring(
																		lastSlashNum + 1,
																		strLen)
														+ " contains no XML Schema for validation");
							else
								this
										.reportError(
												DasResponse.SC_DOES_NOT_EXISTS,
												"PUT: Collection contains no XML Schema for validation");
							errorOccurred = true;
						}

						// Get All XML Schemas From Archive Store
						Source[] schemaFiles = null;
						if (!errorOccurred) {
							schemaFiles = new StreamSource[schemaList.size()];
							Iterator<ResourceData> schemaIter = schemaList
									.iterator();
							int schemaNumber = 0;
							while (schemaIter.hasNext()) {
								ByteArrayOutputStream schemaContainer = new ByteArrayOutputStream();
								ResourceData resourceData = schemaIter.next();
								XmlDasGetRequest getRequest = null;
								if (resourceData.getIsPacked()
										.equalsIgnoreCase("Y"))
									getRequest = new XmlDasGetRequest(sac,
											schemaContainer, resourceData
													.getResName(), resourceData
													.getOffset(), resourceData
													.getPackLength(), null,
											"DELIVER", "NO", null);
								else
									getRequest = new XmlDasGetRequest(sac,
											schemaContainer, resourceData
													.getResName(), 0, 0, null,
											"DELIVER", "NO", null);
								XmlDasGet get = new XmlDasGet(getRequest);
								XmlDasGetResponse getResponse = get.execute();
								if (!((getResponse.getStatusCode() == HttpServletResponse.SC_OK) || (getResponse
										.getStatusCode() == HttpServletResponse.SC_PARTIAL_CONTENT))) {
									if (getResponse.getException() == null)
										this
												.reportError(
														DasResponse.SC_IO_ERROR,
														"PUT: Error while getting XML Schema "
																+ resourceData
																		.getResName()
																		.substring(
																				resourceData
																						.getResName()
																						.lastIndexOf(
																								"/") + 1,
																				resourceData
																						.getResName()
																						.length())
																+ " from archive store; Response: "
																+ getResponse
																		.getStatusCode()
																+ " "
																+ getResponse
																		.getReasonPhrase());
									else
										this
												.reportError(
														DasResponse.SC_IO_ERROR,
														"PUT: Error while getting XML Schema "
																+ resourceData
																		.getResName()
																		.substring(
																				resourceData
																						.getResName()
																						.lastIndexOf(
																								"/") + 1,
																				resourceData
																						.getResName()
																						.length())
																+ " from archive store; Response: "
																+ getResponse
																		.getStatusCode()
																+ " "
																+ getResponse
																		.getReasonPhrase(),
														getResponse
																.getException());
									errorOccurred = true;
									break;
								}
								schemaFiles[schemaNumber] = new StreamSource(
										new ByteArrayInputStream(
												schemaContainer.toByteArray()));
								schemaNumber++;
							}
						}
						if (!errorOccurred) {
							try {

								// Create Schema Object
								SchemaFactory schemaFactory = SchemaFactory
										.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
								schemaFactory
										.setErrorHandler(new ParserErrorHandler());
								schema = schemaFactory.newSchema(schemaFiles);

								// Add Schema Object To Schema Pool
								if (!XmlSchemaProvider.addSchemaObject(colId,
										schema))
									schema = XmlSchemaProvider
											.getSchemaObject(colId);
							} catch (SAXException sex) {
								this.reportError(
										HttpServletResponse.SC_CONFLICT,
										"PUT: Error while creating schema object: "
												+ sex.toString(), sex);
								errorOccurred = true;
							}
						}
					}
				}
			}

			XmlDasPutResponse putResponse = null;
			if (!errorOccurred) {

				// Put Resource To Storage System
				XmlDasPutRequest putRequest = null;

				// File Input Stream
				if ((this.filename_win != null) || (this.filename_unx != null)) {
					putRequest = new XmlDasPutRequest(sac, fis,
							this.archive_path.trim() + "/"
									+ this.resource_name.trim(), this.type,
							this.mode, this.check_level, schema);
				}

				// Servlet Input Stream
				else {

					// Virus Scan
					if (vsInstance != null)
						putRequest = new XmlDasPutRequest(sac, vis_sis,
								this.archive_path.trim() + "/"
										+ this.resource_name.trim(), this.type,
								this.mode, this.check_level, schema);

					// No Virus Scan
					else
						putRequest = new XmlDasPutRequest(sac, sis,
								this.archive_path.trim() + "/"
										+ this.resource_name.trim(), this.type,
								this.mode, this.check_level, schema);
				}
				XmlDasPut put = new XmlDasPut(putRequest);
				putResponse = put.execute();

				// Get Response Status Code From Storage System
				int responseCode = putResponse.getStatusCode();
				httpBodyLength = putResponse.getContentLength();
				if (responseCode != HttpServletResponse.SC_CREATED) {
					if (responseCode == HttpServletResponse.SC_OK
							|| responseCode == HttpServletResponse.SC_NO_CONTENT)
						MasterMethod.cat
								.errorT(
										loc,
										"PUT: Resource "
												+ this.resource_name.trim()
												+ " already existed on storage system and was overwritten");
					else if (responseCode == HttpServletResponse.SC_FORBIDDEN)
						MasterMethod.cat
								.errorT(
										loc,
										"PUT: Resource "
												+ this.resource_name.trim()
												+ " already existed on storage system - it is now consistently created in XML DAS");
					else {

						// Report Error Message
						if (responseCode == DasResponse.SC_CHECK_FAILED) {
							if (putResponse.getException() == null)
								this
										.reportError(
												DasResponse.SC_CHECK_FAILED,
												"PUT: Error while validating / parsing resource "
														+ this.resource_name
																.trim()
														+ "; response code from storage system : "
														+ putResponse
																.getStatusCode()
														+ " "
														+ putResponse
																.getReasonPhrase()
														+ " "
														+ putResponse
																.getEntityBody());
							else
								this
										.reportError(
												DasResponse.SC_CHECK_FAILED,
												"PUT: Error while validating / parsing resource "
														+ this.resource_name
																.trim()
														+ "; response code from storage system : "
														+ putResponse
																.getStatusCode()
														+ " "
														+ putResponse
																.getReasonPhrase()
														+ " "
														+ putResponse
																.getEntityBody(),
												putResponse.getException());
						} else {
							if (putResponse.getException() == null)
								this
										.reportError(
												DasResponse.SC_IO_ERROR,
												"PUT: Error while creating resource "
														+ this.resource_name
																.trim()
														+ "; response code from storage system : "
														+ putResponse
																.getStatusCode()
														+ " "
														+ putResponse
																.getReasonPhrase()
														+ " "
														+ putResponse
																.getEntityBody());
							else
								this
										.reportError(
												DasResponse.SC_IO_ERROR,
												"PUT: Error while creating resource "
														+ this.resource_name
																.trim()
														+ "; response code from storage system : "
														+ putResponse
																.getStatusCode()
														+ " "
														+ putResponse
																.getReasonPhrase()
														+ " "
														+ putResponse
																.getEntityBody(),
												putResponse.getException());
						}

						// Delete Possibly Created Resource
						XmlDasDeleteRequest deleteRequest = new XmlDasDeleteRequest(
								sac, this.archive_path.trim() + "/"
										+ this.resource_name.trim(), "RES");
						XmlDasDelete delete = new XmlDasDelete(deleteRequest);
						XmlDasDeleteResponse deleteResponse = delete.execute();
						if (!(deleteResponse.getStatusCode() == HttpServletResponse.SC_OK
								|| deleteResponse.getStatusCode() == HttpServletResponse.SC_ACCEPTED || deleteResponse
								.getStatusCode() == HttpServletResponse.SC_NO_CONTENT)) {
							if (deleteResponse.getStatusCode() == DasResponse.SC_MULTI_STATUS) {
								String entityBody = deleteResponse
										.getEntityBody();
								if (entityBody == null)
									entityBody = "";
								if (entityBody.startsWith(String
										.valueOf(HttpServletResponse.SC_OK))
										|| entityBody
												.startsWith(String
														.valueOf(HttpServletResponse.SC_ACCEPTED))
										|| entityBody
												.startsWith(String
														.valueOf(HttpServletResponse.SC_NO_CONTENT)))
									MasterMethod.cat
											.infoT(
													loc,
													"PUT: Resource "
															+ this.resource_name
																	.trim()
															+ " deleted successfully after creation failure");
								else
									MasterMethod.cat
											.infoT(
													loc,
													"PUT: Error while deleting resource "
															+ this.resource_name
																	.trim()
															+ ": Archive store returned following response: "
															+ deleteResponse
																	.getStatusCode()
															+ " "
															+ deleteResponse
																	.getReasonPhrase()
															+ " "
															+ deleteResponse
																	.getEntityBody());
							} else {
								if (deleteResponse.getException() == null)
									MasterMethod.cat
											.infoT(
													loc,
													"PUT: Error while deleting resource "
															+ this.resource_name
																	.trim()
															+ "; response code from storage system : "
															+ deleteResponse
																	.getStatusCode()
															+ " "
															+ deleteResponse
																	.getReasonPhrase()
															+ " "
															+ deleteResponse
																	.getEntityBody());
								else
									MasterMethod.cat
											.infoT(
													loc,
													"PUT: Error while deleting resource "
															+ this.resource_name
																	.trim()
															+ "; response code from storage system : "
															+ deleteResponse
																	.getStatusCode()
															+ " "
															+ deleteResponse
																	.getReasonPhrase()
															+ " "
															+ deleteResponse
																	.getEntityBody()
															+ " "
															+ deleteResponse
																	.getException()
																	.toString());
							}
						} else {
							MasterMethod.cat
									.infoT(
											loc,
											"PUT: Resource "
													+ this.resource_name.trim()
													+ " deleted successfully after creation failure");
						}

						// Set Error Occurred Flag
						errorOccurred = true;
					}
				}
			}

			if (!errorOccurred) {

				// Get MessageDigest
				digest = putResponse.getCheckSum();

				// Set Check Level
				if (this.check_level.equalsIgnoreCase("PARSE"))
					validateStatus = "W";
				else if (this.check_level.equalsIgnoreCase("VALIDATE"))
					validateStatus = "V";
			}
		} catch (ArchStoreConfigException ascex) {
			this.reportError(DasResponse.SC_CONFIG_INCONSISTENT, "PUT: "
					+ ascex.getMessage(), ascex);
			errorOccurred = true;
		} catch (IOException ioex) {
			this.reportError(DasResponse.SC_IO_ERROR,
					"PUT: " + ioex.toString(), ioex);
			errorOccurred = true;
		} catch (SQLException sqlex) {
			this.reportError(DasResponse.SC_SQL_ERROR, "PUT: "
					+ sqlex.toString(), sqlex);
			errorOccurred = true;
		} catch (Exception ex) {
			this.reportError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"PUT: " + ex.toString(), ex);
			errorOccurred = true;
		} finally {
			try {
				if (sis != null)
					sis.close();
				if (fis != null)
					fis.close();
				if (vis_sis != null)
					vis_sis.close();
			} catch (IOException ioex) {
				this.reportError(DasResponse.SC_IO_ERROR, "PUT: "
						+ ioex.toString(), ioex);
				errorOccurred = true;
			}
			try {
				if (pst0 != null)
					pst0.close();
				if (pst3 != null)
					pst3.close();
				if (pst4 != null)
					pst4.close();
				if (pst5 != null)
					pst5.close();
				if (pst6 != null)
					pst6.close();
			} catch (SQLException sqlex) {
				this.reportError(DasResponse.SC_SQL_ERROR, "PUT: "
						+ sqlex.toString(), sqlex);
				errorOccurred = true;
			}
		}
		if (errorOccurred)
			return false;

		boolean status = false;
		PreparedStatement checkSelect = null;
		try {
			if (this.mode.equalsIgnoreCase("STORE")) {

				// Insert All Indexes Into Index Table
				int placeHolderCounter = 0;
				StringBuilder checkSelectSql = null;
				StringBuilder insertSql = null;
				boolean isParamAdded = false;
				String propName = null;
				Serializable propValue = null;
				String propType = null;
				HashMap<Integer, Entry<String, Serializable>> statementValues = null;
				boolean isInsertRequired = true;
				for (int m = 0; m < indexData.size(); m++) {

					isInsertRequired = true;
					// Get Index Name And Id
					idxData = indexData.get(m);
					long idxId = idxData.getId();
					String indexTableName = idxData.getName();

					// Check If BC_COL_INDEX Entry Already Exists
					pst7 = connection.prepareStatement(SEL_COL_IDX);
					pst7.setLong(1, idxId);
					pst7.setLong(2, colId);
					result = pst7.executeQuery();
					hits = 0;
					while (result.next())
						hits++;
					result.close();
					pst7.close();

					// Insert BC_COL_INDEX Entry If It Does Not Exists
					if (hits == 0) {
						pst8 = connection.prepareStatement(INS_COL_IDX);
						pst8.setLong(1, idxId);
						pst8.setLong(2, colId);
						pst8.executeUpdate();
						pst8.close();
					}

					// in case of multi-value support: check if insertion is
					// necessary
					if (isMultiValSupported) {
						placeHolderCounter = 0;
						statementValues = new HashMap<Integer, Entry<String, Serializable>>();
						checkSelectSql = new StringBuilder("SELECT 1 FROM ")
								.append(indexTableName.toUpperCase()).append(
										" WHERE ");
						isParamAdded = false;
						for (Entry<String, Serializable> propValueEntry : idxData
								.getPropValuesMap().entrySet()) {
							propName = propValueEntry.getKey();
							if ("counter".equals(propName)) {
								// exclude "counter" field from WHERE clause
								continue;
							}
							if (isParamAdded) {
								checkSelectSql.append(" AND ");
							}
							checkSelectSql.append(propName);
							isParamAdded = true;
							propValue = propValueEntry.getValue();
							if (propValue == null) {
								checkSelectSql.append(" IS NULL ");
							} else {
								checkSelectSql.append(" = ?");
								statementValues.put(Integer
										.valueOf(++placeHolderCounter),
										propValueEntry);
							}
						}
						// create, fill and execute SELECT statement
						checkSelect = connection
								.prepareStatement(checkSelectSql.toString());
						for (Entry<Integer, Entry<String, Serializable>> checkSelectValue : statementValues
								.entrySet()) {
							propValue = checkSelectValue.getValue().getValue();
							propType = idxData.getJdbcMap().get(
									checkSelectValue.getValue().getKey());
							if ("BINARY".equals(propType)) {
								try {
									checkSelect.setBytes(checkSelectValue
											.getKey().intValue(), guidGen
											.parseHexGUID((String) propValue)
											.toBytes());
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
						checkSelect.close();
					}

					if (isInsertRequired) {
						// create INSERT string
						placeHolderCounter = 0;
						statementValues = new HashMap<Integer, Entry<String, Serializable>>();
						insertSql = new StringBuilder("INSERT INTO ").append(
								indexTableName.toUpperCase()).append(" (");
						isParamAdded = false;
						for (Entry<String, Serializable> propValueEntry : idxData
								.getPropValuesMap().entrySet()) {
							propName = propValueEntry.getKey();
							if (isParamAdded) {
								insertSql.append(", ");
							}
							insertSql.append(propName);
							isParamAdded = true;
							statementValues.put(Integer
									.valueOf(++placeHolderCounter),
									propValueEntry);
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
						// Note: There is no SQL injection danger here because
						// the value of "indexTableName" has already been
						// checked in "getIndexTableMetaData()"
						pst = connection.prepareStatement(insertSql.toString());
						for (Entry<Integer, Entry<String, Serializable>> insertValue : statementValues
								.entrySet()) {
							propValue = insertValue.getValue().getValue();
							if (propValue != null) {
								propType = idxData.getJdbcMap().get(
										insertValue.getValue().getKey());
								if ("BINARY".equals(propType)) {
									try {
										pst.setBytes(insertValue.getKey()
												.intValue(), guidGen
												.parseHexGUID(
														(String) propValue)
												.toBytes());
									} catch (GUIDFormatException e) {
										throw new BodyFormatException(
												"Problem parsing GUID string: "
														+ e.getMessage());
									}
								} else {
									pst.setObject(insertValue.getKey()
											.intValue(), propValue);
								}
							} else {
								// property value not contained in the request
								// -> allowed for multi-value support
								pst.setNull(insertValue.getKey().intValue(),
										Types.NULL);
							}
						}
						pst.executeUpdate();
						pst.close();
					}
				} // end for

				// Insert New Resource Entry Into Table BC_XMLA_RES
				if (autoNaming == false) {
					pst9 = connection.prepareStatement(UPD_RES_TAB);
					pst9.setLong(1, httpBodyLength);
					pst9.setString(2, validateStatus.trim());
					pst9.setString(3, digest.trim());
					pst9.setLong(4, resId);
				} else {
					pst9 = connection.prepareStatement(INS_RES_TAB);
					pst9.setLong(1, resId);
					pst9.setString(2, this.resource_name.trim());
					pst9.setString(3, this.type.toUpperCase().trim());
					pst9.setLong(4, httpBodyLength);
					pst9.setTimestamp(5, dateTime);
					pst9.setString(6, this.user);
					pst9.setString(7, validateStatus.trim());
					pst9.setString(8, "N");
					pst9.setString(9, digest.trim());
					pst9.setLong(10, colId);
				}
				pst9.executeUpdate();
				pst9.close();

				// Set Response Header Fields
				response.setHeader("resource_name", this.resource_name.trim());
			}

			String completeArchivePath = this.archive_path.trim() + "/";

			// GET verifying Check Sum
			if (this.checksum.equalsIgnoreCase("Y")
					&& this.mode.equalsIgnoreCase("STORE")) {
				this.connection.commit();

				String uri = completeArchivePath + this.resource_name.trim();

				if (this.verify(sac, uri, digest.trim())) {
					this.response.setHeader("service_message", "Ok");
					this.response.setStatus(XmlDasMaster.SC_OK);
				} else {
					if (undoStore(sac, uri))
						undoDB(resId, indexData);
				}
			} else
				// end verify
				// Method Was Successful
				this.response.setHeader("service_message", "Ok");

			// Set Response Header Fields
			response.setContentType("text/xml");
			// Now always return path (not just in STORE mode)
			response.setHeader("archive_path", completeArchivePath);
			// Plus archive store
			response.setHeader("archive_store", sac.archive_store.trim());

			// Set status always to TRUE to commit in any case (outside as
			// usual)
			status = true;

		} catch (SQLException sqlex) {

			// Delete Already Stored Resource Because Of An SQL Exception
			// Occurred
			String errorMessage = "";
			try {
				XmlDasDeleteRequest deleteRequest = new XmlDasDeleteRequest(
						sac, this.archive_path.trim() + "/"
								+ this.resource_name.trim(), "RES");
				XmlDasDelete delete = new XmlDasDelete(deleteRequest);
				XmlDasDeleteResponse deleteResponse = delete.execute();
				if (!((deleteResponse.getStatusCode() == HttpServletResponse.SC_OK) || (deleteResponse
						.getStatusCode() == HttpServletResponse.SC_NO_CONTENT))) {
					if (deleteResponse.getStatusCode() == DasResponse.SC_MULTI_STATUS) {
						String entityBody = deleteResponse.getEntityBody();
						if (entityBody == null)
							entityBody = "";
						if (!(entityBody.startsWith(String
								.valueOf(HttpServletResponse.SC_OK))
								|| entityBody
										.startsWith(String
												.valueOf(HttpServletResponse.SC_ACCEPTED)) || entityBody
								.startsWith(String
										.valueOf(HttpServletResponse.SC_NO_CONTENT))))
							throw new IOException(deleteResponse
									.getStatusCode()
									+ " "
									+ deleteResponse.getReasonPhrase()
									+ " " + deleteResponse.getEntityBody());
					} else {
						throw new IOException(deleteResponse.getStatusCode()
								+ " " + deleteResponse.getReasonPhrase() + " "
								+ deleteResponse.getEntityBody());
					}
				}
			} catch (Exception ex) {

				// $JL-EXC$
				errorMessage = ex.toString();
			}
			if (errorMessage.length() == 0)
				this.reportError(DasResponse.SC_SQL_ERROR, "PUT: Resource "
						+ this.resource_name.trim() + " was not archived: "
						+ sqlex.toString(), sqlex);
			else
				this.reportError(DasResponse.SC_SQL_ERROR, "PUT: Resource "
						+ this.resource_name.trim()
						+ " was stored, but cannot be accessed by XMLDAS: "
						+ sqlex.toString()
						+ " IOException when deleting "
						+ this.resource_name.trim()
						+ " in "
						+ XmlDasMaster.getPhysicalPath(sac, this.archive_path
								.trim()) + ": " + errorMessage, sqlex);
		} catch (Exception exc) {

			// Delete Already Stored Resource Because Of An Exception Occurred
			String errorMessage = "";
			try {
				XmlDasDeleteRequest deleteRequest = new XmlDasDeleteRequest(
						sac, this.archive_path.trim() + "/"
								+ this.resource_name.trim(), "RES");
				XmlDasDelete delete = new XmlDasDelete(deleteRequest);
				XmlDasDeleteResponse deleteResponse = delete.execute();
				if (!((deleteResponse.getStatusCode() == HttpServletResponse.SC_OK) || (deleteResponse
						.getStatusCode() == HttpServletResponse.SC_NO_CONTENT))) {
					if (deleteResponse.getStatusCode() == DasResponse.SC_MULTI_STATUS) {
						String entityBody = deleteResponse.getEntityBody();
						if (entityBody == null)
							entityBody = "";
						if (!(entityBody.startsWith(String
								.valueOf(HttpServletResponse.SC_OK))
								|| entityBody
										.startsWith(String
												.valueOf(HttpServletResponse.SC_ACCEPTED)) || entityBody
								.startsWith(String
										.valueOf(HttpServletResponse.SC_NO_CONTENT))))
							throw new IOException(deleteResponse
									.getStatusCode()
									+ " "
									+ deleteResponse.getReasonPhrase()
									+ " " + deleteResponse.getEntityBody());
					} else {
						throw new IOException(deleteResponse.getStatusCode()
								+ " " + deleteResponse.getReasonPhrase() + " "
								+ deleteResponse.getEntityBody());
					}
				}
			} catch (Exception ex) {

				// $JL-EXC$
				errorMessage = ex.toString();
			}
			if (errorMessage.length() == 0)
				this.reportError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						"PUT: Resource " + this.resource_name.trim()
								+ " was not archived: " + exc.toString(), exc);
			else
				this
						.reportError(
								HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
								"PUT: Resource "
										+ this.resource_name.trim()
										+ " was stored, but cannot be accessed by XMLDAS: "
										+ exc.toString()
										+ " IOException when deleting "
										+ this.resource_name.trim()
										+ " in "
										+ XmlDasMaster.getPhysicalPath(sac,
												this.archive_path.trim())
										+ ": " + errorMessage, exc);
		} finally {
			try {
				if (pst != null)
					pst.close();
				if (pst7 != null)
					pst7.close();
				if (pst8 != null)
					pst8.close();
				if (pst9 != null)
					pst9.close();
			} catch (SQLException sqlex) {
				this.reportError(DasResponse.SC_SQL_ERROR, "PUT: "
						+ sqlex.toString(), sqlex);
				status = false;
			}
		}
		return status;
	}

	private boolean verify(Sapxmla_Config sac, String uriParam,
			String checksumFromPut) {
		boolean status = false;
		XmlDasGetResponse getResponse = null;
		int statusCodeFromGet;

		// Get Resource From Storage System
		XmlDasGetRequest getRequest = new XmlDasGetRequest(sac, null, uriParam,
				0, 0, checksumFromPut, "NODELIVER", "NO", null);
		XmlDasGet get = new XmlDasGet(getRequest);
		getResponse = get.execute();
		statusCodeFromGet = getResponse.getStatusCode();

		try {
			if (!((statusCodeFromGet == HttpServletResponse.SC_OK) || (statusCodeFromGet == HttpServletResponse.SC_PARTIAL_CONTENT)))
				this
						.reportError(
								statusCodeFromGet,
								"PUT (verifying GET): Error while accessing resource; response from archive store: "
										+ statusCodeFromGet
										+ " "
										+ getResponse.getReasonPhrase()
										+ " "
										+ getResponse.getEntityBody());
			else if (getResponse.isChecksumIdentical())
				status = true;
			else
				this.reportError(DasResponse.SC_CHECKSUM_INCORRECT,
						"PUT (verifying GET) Check sum " + checksumFromPut
								+ " not matched by resource " + uriParam);

		} catch (IOException ioex) {
			try {
				this.reportError(DasResponse.SC_IO_ERROR,
						"PUT (verifying GET): " + ioex.toString(), ioex);
				return status;
			} catch (IOException ioex2) {

				// $JL-EXC$
				return status;
			}
		}

		return status;
	}

	private boolean undoStore(Sapxmla_Config sac, String uri) {
		boolean status = true;

		XmlDasDeleteRequest deleteRequest = new XmlDasDeleteRequest(sac, uri,
				"RES");
		XmlDasDelete delete = new XmlDasDelete(deleteRequest);
		XmlDasDeleteResponse deleteResponse = delete.execute();

		try {
			if (!(deleteResponse.getStatusCode() == HttpServletResponse.SC_OK
					|| deleteResponse.getStatusCode() == HttpServletResponse.SC_ACCEPTED || deleteResponse
					.getStatusCode() == HttpServletResponse.SC_NO_CONTENT)) {
				if (deleteResponse.getStatusCode() == DasResponse.SC_MULTI_STATUS) {
					String entityBody = deleteResponse.getEntityBody();
					if (entityBody == null)
						entityBody = "";
					if (!(entityBody.startsWith(String
							.valueOf(HttpServletResponse.SC_OK))
							|| entityBody.startsWith(String
									.valueOf(HttpServletResponse.SC_ACCEPTED)) || entityBody
							.startsWith(String
									.valueOf(HttpServletResponse.SC_NO_CONTENT))))
						throw new IOException(deleteResponse.getStatusCode()
								+ " " + deleteResponse.getReasonPhrase() + " "
								+ deleteResponse.getEntityBody());
				} else {
					throw new IOException(deleteResponse.getStatusCode() + " "
							+ deleteResponse.getReasonPhrase() + " "
							+ deleteResponse.getEntityBody());
				}
			}
		} catch (IOException ioex) {
			status = false;
			try {
				this
						.reportError(
								DasResponse.SC_IO_ERROR,
								"PUT: No access to resource "
										+ uri
										+ " Exception during DELETE following failed verification: ",
								ioex);
			} catch (IOException ioex2) {

				// $JL-EXC$
				return false;
			}
		}
		return status;
	}

	private boolean undoDB(long resId, ArrayList<IndexData> indexData) {
		boolean status = false;
		int hits = 0;

		final String DEL_RES_TAB = "DELETE FROM BC_XMLA_RES WHERE RESID = ?";
		PreparedStatement pst10 = null;
		PreparedStatement pst11 = null;

		try {
			pst10 = this.connection.prepareStatement(DEL_RES_TAB);
			pst10.setLong(1, resId);
			hits = pst10.executeUpdate();
			pst10.close();

			if (hits != 1) {
				this
						.reportError(
								DasResponse.SC_CHECKSUM_INCORRECT,
								"PUT: Error while deleting resource ID "
										+ resId
										+ " from table BC_XMLA_RES during undo after failed verification");
			}

			// Get Index Names
			for (int m = 0; m < indexData.size(); m++) {
				IndexData idxData = indexData.get(m);
				String indexTableName = idxData.getName().trim();

				// Delete All Possible Entries In All Index Tables
				pst11 = this.connection.prepareStatement("DELETE FROM "
						+ indexTableName + " WHERE RESID = ?");
				pst11.setLong(1, resId);
				pst11.executeUpdate();
				pst11.close();
			}

			return true;

		} catch (SQLException sqlex) {
			try {
				this.reportError(DasResponse.SC_CHECKSUM_INCORRECT,
						"PUT: SQL error while deleting database (ResID "
								+ resId
								+ ") during undo after failed verification: "
								+ sqlex.toString(), sqlex);
				if (pst10 != null)
					pst10.close();
				if (pst11 != null)
					pst11.close();
				return status;
			} catch (Exception withoutex) {

				// $JL-EXC$
				return status;
			}
		} catch (Exception ex) {
			try {
				this.reportError(DasResponse.SC_CHECKSUM_INCORRECT,
						"PUT: System error while deleting database (ResID "
								+ resId
								+ ") during undo after failed verification: "
								+ ex.toString(), ex);
				if (pst10 != null)
					pst10.close();
				if (pst11 != null)
					pst11.close();
				return status;
			} catch (Exception withoutex) {

				// $JL-EXC$
				return status;
			}
		}
	}
}
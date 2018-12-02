package com.sap.engine.services.ts.facades.crypter;

import static com.sap.engine.services.ts.tlog.db.DBTableAndColumnNames.RM_PROPS;
import static com.sap.engine.services.ts.tlog.db.DBTableAndColumnNames.RM_PROPS_PROPERTY_TYPE;
import static com.sap.engine.services.ts.tlog.db.DBTableAndColumnNames.RM_PROPS_PROPERTY_VALUE;
import static com.sap.engine.services.ts.tlog.db.DBTableAndColumnNames.RM_PROPS_SECURE_PROPERTY;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.sap.engine.services.ts.tlog.TLog;
import com.sap.engine.services.ts.tlog.TLogIOException;
import com.sap.engine.services.ts.tlog.TLogReaderWriter;
import com.sap.engine.services.ts.tlog.fs.FSTLog;
import com.sap.engine.services.ts.tlog.fs.FSTLogReaderWriter;
import com.sap.engine.services.ts.tlog.fs.RMEntryRecord;
import com.sap.engine.services.ts.tlog.fs.RMRecord;
import com.sap.engine.services.ts.tlog.util.TLogLocking;
import com.sap.engine.services.ts.utils.ByteArrayUtils;

public abstract class AbstractCrypter {

	/**
	 * Select all resource manager's properties for all transaction logs of
	 * specified type. Parameter - the type of the properties.
	 */
	private static final String GET_ALL_RM_PROPERTIES_STATEMENT = "SELECT "
			+ RM_PROPS_PROPERTY_VALUE + " FROM " + RM_PROPS + " WHERE "
			+ RM_PROPS_PROPERTY_TYPE + " = ?";


	/**
	 * Encrypt a given String
	 * 
	 * @param str
	 *            the String to encrypt
	 * @return the encrypted String
	 */
	public byte[] encryptString(String str) throws CrypterException {
		// String to byte[]
		byte[] bytes = ByteArrayUtils.convertStringToByteArray(str);
		// encrypt byte[]
		return encryptBytes(bytes);
	}

	/**
	 * Decrypt a given String
	 * 
	 * @param str
	 *            the String to decrypt
	 * @return the decrypted String
	 */
	public String decryptString(byte[] bytes) throws CrypterException {
		// decrypt byte[]
		bytes = decryptBytes(bytes);
		// return byte[] to String
		return ByteArrayUtils.convertByteArrayToString(bytes);
	}

	/**
	 * Reencrypt all entries in a DBTLog
	 * 
	 * @param con
	 *            Connection to the DB where the DBTLog is located
	 */
	public void reencryptDBTLog(Connection con) throws CrypterException {
		try {
			con.setAutoCommit(false);

			// select all security properties
			PreparedStatement stmn = con.prepareStatement(
					GET_ALL_RM_PROPERTIES_STATEMENT,
					ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
			stmn.setBytes(1, new byte[] { RM_PROPS_SECURE_PROPERTY });
			ResultSet res = stmn.executeQuery();

			// reencrypt and update the selected properties
			try {
				while (res.next()) {
					byte[] value = res.getBytes(1);
					value = reencryptBytes(value);
					res.updateBytes(1, value);
					res.updateRow();
				}
			} catch (CrypterException e) {
				con.rollback();
				con.setAutoCommit(true);
				throw e;
			}

			// commit all
			con.commit();
			stmn.close();
			con.setAutoCommit(true);
		} catch (SQLException e) {
			try {
				con.rollback();
				con.setAutoCommit(true);
			} catch (SQLException e1) {
				throw new CrypterException("SQL exception.", e1);
			}
			throw new CrypterException("SQL exception.", e);
		}
	}
	
	/**
	 * Reencrypt all entries in a FSTLog
	 * 
	 * @param directory - represent directory in which we should reencrypt all tlog files
	 */
	public void reencryptFSTLog(String directory, TLogLocking locking,
			int bufferCapacity, int maxFileSize) throws CrypterException {
		TLogReaderWriter writer = null;
		TLog tlog = null;

		writer = new FSTLogReaderWriter(
				new File(directory), 
				locking, 
				null,
				bufferCapacity,
				maxFileSize);
		while(true) {
			try {
				tlog = writer.lockOrphanedTLog();
			} catch (TLogIOException e) {
				throw new CrypterException("TlogIOException occured during lockOrphanedTlog()", e);
			}
			if(tlog == null) {
				break;
			}
			FSTLog log = (FSTLog) tlog;
			Map<Integer, RMEntryRecord> newRMStorage = new HashMap<Integer, RMEntryRecord>();
			Iterator<Entry<Integer, RMEntryRecord>> it = log.getRMEntries().entrySet().iterator();
			while(it.hasNext()) {
				Entry<Integer, RMEntryRecord> tmp = it.next();
				int num = tmp.getKey();
				RMRecord rec = tmp.getValue();
				
				RMEntryRecord record = (RMEntryRecord)rec;
				byte[] secureProperties = record.getSecure();
				try {
					secureProperties = reencryptBytes(secureProperties);
				} catch (CrypterException e) {
					throw e;
				}
				record.setSecure(secureProperties);
				newRMStorage.put(num, record);
			}
			
			log.setRMEntries(newRMStorage);//just to be sure
			
			try {
				log.rotateRM();
			} catch (TLogIOException e) {
				throw new CrypterException("TLogIOException occured during rotateRM()", e);
			}
			try {
				tlog.close();
			} catch (TLogIOException e) {
				throw new CrypterException(e);
			}
		}
	}

	/**
	 * Reencrypt the current TLog used by the transaction manager.
	 * 
	 * Note: Must be called only offline.
	 */
	public static void reencryptTLog() {
		// check that it's online otherwise throw Exception
	}


	/**
	 * Decrypt a given byte[]
	 * 
	 * @param bytes
	 *            the bytes to decrypt
	 * @return the decrypted byte[]
	 */
	public abstract byte[] decryptBytes(byte[] bytes) throws CrypterException;

	/**
	 * Encrypt a given byte[]
	 * 
	 * @param bytes
	 *            the byte[] to encrypt
	 * @return the encrypted byte[]
	 */
	public abstract byte[] encryptBytes(byte[] bytes) throws CrypterException;

	/**
	 * Reencrypt a given byte[]
	 * 
	 * @param bytes
	 *            the byte[] to reencrypt
	 * @return the reencrypted byte[]
	 */
	public abstract byte[] reencryptBytes(byte[] bytes) throws CrypterException;

}

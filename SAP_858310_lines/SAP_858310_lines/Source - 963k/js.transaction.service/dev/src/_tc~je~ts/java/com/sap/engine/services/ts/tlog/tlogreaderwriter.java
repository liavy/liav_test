package com.sap.engine.services.ts.tlog;

import com.sap.engine.services.ts.utils.TLogVersion;



/**
 * 
 * This interface will be implemented from modules that are responsible to physical TLog storages. 
 * There are 2 known modules one for file system and one for DBMS TLogs.
 * 
 * @author I024163
 */
public interface TLogReaderWriter {

	/**
	 * Creates new TLog with specified TLogVersion. Created TLog is opened and ready for use.
	 * TLog is automatically locked so other processes are not able to read or write into it.    
	 * 
	 * @param TLogVersion unique array with 15 bytes. 
	 * @return new TLog instance
	 * @throws TlogIOException when unexpected IOException or SQLException occurred.	
	 * @throws TLogAlreadyExistException when TLog with specified TLogVersion already exist.
	 */
	public TLog createNewTLog(byte[] TLogVersion) throws TLogIOException, TLogAlreadyExistException;

	/**
	 * 
	 * Return TLog which exist and is not locked. TLog is automatically locked
	 * so other processes are not able to read or write into it.
	 * 
	 * @return null if there are no logs or all TLogs are already locked.	
	 * @throws TlogIOException when unexpected IOException or SQLException occurred.
	 */
	public TLog lockOrphanedTLog() throws TLogIOException;

	/**
	 * Return an InboundTLog with the specified TLog Version. If the InboundTLog
	 * already exist it will be locked and returned. If it doesn't exist it will
	 * be created. If the InboundTLog is already locked then an runtime
	 * exception will be thrown.
	 * 
	 * @return InboundTLog with the specified TLogVersion
	 * @throws TLogIOException
	 *             when unexpected IOException or SQLException occurred.
	 */
	public InboundTLog getInboundTLog(TLogVersion tLogVersion) throws TLogIOException;

}

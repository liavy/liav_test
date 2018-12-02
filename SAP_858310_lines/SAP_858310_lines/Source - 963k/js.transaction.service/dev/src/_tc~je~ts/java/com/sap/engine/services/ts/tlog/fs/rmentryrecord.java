package com.sap.engine.services.ts.tlog.fs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Properties;

import com.sap.engine.services.ts.TransactionServiceFrame;
import com.sap.engine.services.ts.facades.crypter.CrypterException;
import com.sap.engine.services.ts.tlog.TLogIOException;

public class RMEntryRecord extends RMRecord implements Cloneable {
	
	private int rmID; // 4 bytes
	
	private byte[] name; // int nameLen;(4 bytes)
	
	private byte[] containerName; // int containerNameLen;(4 bytes)
	
	private byte[] secure; // int secLen;(4 bytes) 
	
	private byte[] nonSecure; // int nonSecLen;(4 bytes)
	
	private final byte type = RM_ENTRY_RECORD_TYPE;
	
	//Empty Constructor
	public RMEntryRecord() {
	}
	
	// Constructor
	public RMEntryRecord(int rmID, byte[] name, byte[] containerName, Properties secure,
			Properties nonSecure) throws TLogIOException {
		super();
		this.rmID = rmID;
		this.name = name;
		this.containerName = containerName;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			secure.store(out, null);
		} catch (IOException e) {
			throw new TLogIOException("cannot load sec props", e);
		}
		this.secure = out.toByteArray();
		
		ByteArrayOutputStream out1 = new ByteArrayOutputStream();
		try {
			nonSecure.store(out1, null);
		} catch (IOException e) {
			throw new TLogIOException("cannot load non-sec props", e);
		}
		this.nonSecure = out1.toByteArray();
	}

	public RMEntryRecord decrypt() throws TLogIOException {
		byte[] arr = null;
		try {
			arr = TransactionServiceFrame.getCrypter().decryptBytes(secure);
		} catch (CrypterException e) {
			throw new TLogIOException("Decrypt exception", e);
		}
		
		this.secure = arr;
		return this;
	}
	
	public RMEntryRecord encrypt() throws TLogIOException {
		byte[] arr = null;
		try {
			arr = TransactionServiceFrame.getCrypter().encryptBytes(secure);
		} catch (CrypterException e) {
			throw new TLogIOException("Encrypt exception", e);
		}
		
		this.secure = arr;
		return this;
	}
	
	@Override
	public RMEntryRecord read(ByteBuffer buf) throws TLogIOException {
		return read(buf, true);
	}

	public RMEntryRecord read(ByteBuffer buf, boolean decrypt) throws TLogIOException {
		int rmid = buf.getInt();
		
		int nameLen = buf.getInt();
		byte[] name = new byte[nameLen];
		for (int i = 0; i < name.length; i++) {
			name[i] = buf.get();
		}
		
		int containerNameLen = buf.getInt();
		byte[] containerName = new byte[containerNameLen];
		for (int i = 0; i < containerName.length; i++) {
			containerName[i] = buf.get();
		}
		
		int nonSecurePropsLen = buf.getInt();
		byte[] nonSecure = new byte[nonSecurePropsLen];
		for (int i = 0; i < nonSecure.length; i++) {
			nonSecure[i] = buf.get();
		}
				
		int securePropsLen = buf.getInt();
		byte[] secure = new byte[securePropsLen];
		buf.get(secure);
		if(decrypt) {
			try {
				secure = TransactionServiceFrame.getCrypter().decryptBytes(secure);
			} catch (CrypterException e) {
				throw new TLogIOException("FS TLog cannot decrypt security properties.", e);
			}
		}
				
		this.containerName = containerName;
		this.name = name;
		this.rmID = rmid;
		this.secure = secure;
		this.nonSecure = nonSecure;
		
		return this;
	}

	@Override
	public RMEntryRecord write(ByteBuffer buf) throws TLogIOException {
		return write(buf, true);
	}
	
	public RMEntryRecord write(ByteBuffer buf, boolean encrypt) throws TLogIOException {
		int start = buf.position();
		
		buf.putInt(0); // reserved for length of record
		
		buf.putLong(0); // reserved for checksum - crc32
		
		buf.put(type); // record type
		
		buf.putInt(rmID); // resource manager id
		
		buf.putInt(name.length); // resource manager name length of byte array
		buf.put(name); // resource manager name (byte array)
		
		buf.putInt(containerName.length); // resource manager container name length
		buf.put(containerName); // resource manager container name (byte array)
		
		// secure props
		buf.putInt(nonSecure.length);
		buf.put(nonSecure);
		
		if(encrypt) {
			try {
				secure = TransactionServiceFrame.getCrypter().encryptBytes(secure);
			} catch (CrypterException e) {
				throw new TLogIOException("FS TLog cannot encrypt security properties.", e);
			}
		}
		//non-secure props
		buf.putInt(secure.length);
		buf.put(secure);
		
		int end = buf.position();
		
		Record.completeData(buf, start, end);
		
		return this;
	}
	
	/**
	 * @return the name
	 */
	public byte[] getName() {
		return name;
	}

	/**
	 * @return the secure
	 */
	public byte[] getSecure() {
		return secure;
	}
	
	public Properties getSecureProperties() throws TLogIOException {
		Properties res = new Properties();
		try {
			res.load(new ByteArrayInputStream(secure));
		} catch (IOException e) {
			throw new TLogIOException ("Cannot load secure props", e);
		}
		return res;
	}

	/**
	 * @return the nonSecure
	 */
	public byte[] getNonSecure() {
		return nonSecure;
	}
	
	public Properties getNonSecureProperties() throws TLogIOException {
		Properties res = new Properties();
		try {
			res.load(new ByteArrayInputStream(nonSecure));
		} catch (IOException e) {
			throw new TLogIOException ("Cannot load non-secure props", e);
		}
		return res;
	}

	/**
	 * @param secure the secure to set
	 */
	public void setSecure(Properties secure) throws TLogIOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			secure.store(out, null);
		} catch (IOException e) {
			throw new TLogIOException("cannot load sec props", e);
		}
		this.secure = out.toByteArray();
	}
	
	public void setSecure(byte[] secure) {
		this.secure = secure;
	}


	/**
	 * @return the containerName
	 */
	public byte[] getContainerName() {
		return containerName;
	}
	
	@Override
	public RMEntryRecord clone() throws CloneNotSupportedException {
		RMEntryRecord res = null;
		try {
			res = new RMEntryRecord(rmID, name, containerName, this.getSecureProperties(), this.getNonSecureProperties());
			res.secure = this.secure;
		} catch (TLogIOException e) {
			CloneNotSupportedException ex = new CloneNotSupportedException();
			ex.initCause(e);
			throw ex;
		}
		return res;
	}
	
	public int getRMID() {
		return rmID;
	}
}

package com.sap.engine.services.ts.facades.crypter;

import com.sap.security.core.server.secstorefs.DecryptionFailedException;
import com.sap.security.core.server.secstorefs.SecStoreFS;
import com.sap.security.core.server.secstorefs.SecStoreFSException;

public class ServerCrypter extends AbstractCrypter {

	private static final SecStoreFS SEC_STORE_FS = new SecStoreFS();

	@Override
	public byte[] decryptBytes(byte[] bytes) throws CrypterException {
		try {
			return SEC_STORE_FS.decrypt(bytes);
		} catch (SecStoreFSException e) {
			throw new CrypterException("Decryption exception.", e);
		} catch (DecryptionFailedException e) {
			throw new CrypterException("Decryption exception.", e);
		}
	}

	@Override
	public byte[] encryptBytes(byte[] bytes) throws CrypterException {
		try {
			return SEC_STORE_FS.encrypt(bytes);
		} catch (SecStoreFSException e) {
			throw new CrypterException("Encryption exception.", e);
		}
	}

	@Override
	public byte[] reencryptBytes(byte[] bytes) throws CrypterException {
		try {
			return SEC_STORE_FS.reencryptBytes(bytes);
		} catch (SecStoreFSException e) {
			throw new CrypterException("Encryption exception.", e);
		} catch (DecryptionFailedException e) {
			throw new CrypterException("Encryption exception.", e);
		}
	}

}

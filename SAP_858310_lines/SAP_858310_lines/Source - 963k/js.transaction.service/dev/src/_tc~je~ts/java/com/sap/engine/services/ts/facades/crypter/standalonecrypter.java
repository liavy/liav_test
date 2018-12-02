package com.sap.engine.services.ts.facades.crypter;

public class StandaloneCrypter extends AbstractCrypter {

	@Override
	public byte[] decryptBytes(byte[] bytes) throws CrypterException {
		return bytes;
	}

	@Override
	public byte[] encryptBytes(byte[] bytes) throws CrypterException {
		return bytes;
	}

	@Override
	public byte[] reencryptBytes(byte[] bytes) throws CrypterException {
		return bytes;
	}

}

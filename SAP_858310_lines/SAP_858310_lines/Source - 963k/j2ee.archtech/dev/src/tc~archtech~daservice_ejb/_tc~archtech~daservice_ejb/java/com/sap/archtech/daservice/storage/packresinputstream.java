package com.sap.archtech.daservice.storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import javax.servlet.http.HttpServletResponse;

import com.sap.archtech.daservice.data.ResourceData;
import com.sap.archtech.daservice.data.Sapxmla_Config;

/**
 * The intention of this class is to provide a resource from the archive in the
 * AXML-format in a stream-like manner. To do so the complete resource is read
 * into memory and packed upon the first call of one of the read()-methods.
 */
public class PackResInputStream extends InputStream {

	private final static int COMPRESSION_METHOD = java.util.zip.Deflater.BEST_COMPRESSION;
	private final static byte[] MIB = { 0x00, 0x6A };

	private ResourceData res;
	private Sapxmla_Config sac;
	private String coll;
	private boolean firstread = true;
	private InputStream zipres;

	public PackResInputStream(String coll, ResourceData res, Sapxmla_Config sac) {
		super();
		this.coll = coll;
		this.res = res;
		this.sac = sac;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#read()
	 */
	public int read() throws IOException {
		if (firstread) {
			this.pack_res();
			this.firstread = false;
		}
		return zipres.read();
	}

	public void close() throws IOException {
		if (zipres != null)
			zipres.close();
	}

	private void pack_res() throws IOException {
		ByteArrayOutputStream zipheader;
		ByteArrayOutputStream zipbody;
		DeflaterOutputStream zipstream;
		Deflater defla;
		int success;
		int length;
		long offset;

		zipbody = new ByteArrayOutputStream();
		defla = new Deflater(COMPRESSION_METHOD, true);
		zipstream = new DeflaterOutputStream(zipbody, defla);
		zipheader = new ByteArrayOutputStream();

		// As OutputStream for the XmlDasGetRequest-object, take a
		// DeflaterOutputStream
		// As a result the zipped document is available in the
		// zipbody-ByteArrayOutputStream
		XmlDasGetRequest getRequest = new XmlDasGetRequest(sac, zipstream,
				this.coll.concat(this.res.getResName()), 0, 0, null, "DELIVER",
				"NO", null);
		XmlDasGet get = new XmlDasGet(getRequest);
		XmlDasGetResponse getResponse = get.execute();
		success = getResponse.getStatusCode();

		zipstream.finish();
		zipstream.close();
		defla.end();

		if (success != HttpServletResponse.SC_OK)
			throw new IOException(String.valueOf(success) + ": "
					+ getResponse.getReasonPhrase());

		// calculate length of packed resource, write packHeader in
		// zipheader-ByteArrayOutputStream
		// and update offset with length of header
		length = zipbody.size();

		// break further packing when packed document is larger than 2GB
		if (length < 0)
			throw new IOException("The packed resource is larger than 2 GB");

		offset = writeDocHeader(zipheader, this.res.getResName(), this.res
				.getResType(), length);

		// add header and body together and provide one single InputStream
		this.zipres = new SequenceInputStream(new ByteArrayInputStream(
				zipheader.toByteArray()), new ByteArrayInputStream(zipbody
				.toByteArray()));

		// update packLength and offset
		this.res.setLength(length);
		this.res.setOffset(offset);
	}

	private long writeDocHeader(ByteArrayOutputStream bos, String resName,
			String resType, int zipLength) throws IOException {
		bos.write(MIB);
		byte[] resTypeBA = resType.getBytes("UTF-8");
		bos.write(resTypeBA);
		bos.write(resName.length());
		byte[] resNameBA = resName.getBytes("UTF-8");
		bos.write(resNameBA);
		bos.write(toFourBytes(zipLength));

		return MIB.length + resTypeBA.length + 1 + resNameBA.length + 4;
	}

	private byte[] toFourBytes(int n) {
		byte[] b = new byte[4];
		b[3] = (byte) (n);
		n >>>= 8;
		b[2] = (byte) (n);
		n >>>= 8;
		b[1] = (byte) (n);
		n >>>= 8;
		b[0] = (byte) (n);
		return b;
	}
}

package com.sap.archtech.daservice.storage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class InputStreamWrapperReadWrite extends InputStream {

	private InputStream in;
	private OutputStream out;
	private long contentLength;

	public long getContentLength() {
		return contentLength;
	}

	public InputStreamWrapperReadWrite(InputStream in, OutputStream out) {
		this.in = in;
		this.out = out;
	}

	public int read() throws IOException {
		int len2 = in.read();
		if (len2 > -1) {
			out.write(len2);
			this.contentLength++;
		}
		return len2;
	}

	public int read(byte[] buf, int off, int len) throws IOException {
		int len2 = in.read(buf, off, len);
		if (len2 > -1) {
			out.write(buf, off, len2);
			this.contentLength += len2;
		}
		return len2;
	}

	public int read(byte buf[]) throws IOException {
		int len2 = in.read(buf);
		if (len2 > -1) {
			out.write(buf);
			this.contentLength += len2;
		}
		return len2;
	}

	public void close() throws IOException {
		if (in != null)
			in.close();
	}

	// public void closeAll() throws IOException {
	// if (in != null)
	// in.close();
	// if (out != null)
	// out.close();
	// }

}

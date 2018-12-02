package com.sap.archtech.daservice.storage;

import java.io.IOException;
import java.io.InputStream;

public class InputStreamWrapperReadOnly extends InputStream {

	private InputStream in;
	private long contentLength;

	public long getContentLength() {
		return contentLength;
	}

	public InputStreamWrapperReadOnly(InputStream in) {
		this.in = in;
	}

	public int read() throws IOException {
		int r = in.read();
		if (r > -1)
			this.contentLength++;
		return r;
	}

	public int read(byte[] buf, int off, int len) throws IOException {
		int len2 = in.read(buf, off, len);
		if (len2 > -1)
			this.contentLength += len2;
		return len2;
	}

	public int read(byte buf[]) throws IOException {
		int len2 = in.read(buf);
		if (len2 > -1)
			this.contentLength += len2;
		return len2;
	}

	public void close() throws IOException {
		if (in != null)
			in.close();
	}
}

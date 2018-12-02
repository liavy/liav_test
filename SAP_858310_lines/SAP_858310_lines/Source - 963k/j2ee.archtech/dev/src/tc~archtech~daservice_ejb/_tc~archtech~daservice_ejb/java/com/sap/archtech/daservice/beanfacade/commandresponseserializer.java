package com.sap.archtech.daservice.beanfacade;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;

import javax.servlet.ServletOutputStream;

/**
 * The <code>CommandResponseSerializer</code> is used for the serialization of
 * the data resulting from the execution of those XMLDAS commands exposed in the
 * Session Bean Facade. Although it does not deal with servlets in any way, it
 * is derived from the <code>ServletOutputStream</code> because this is
 * required by the XMLDAS commands.
 */
class CommandResponseSerializer extends ServletOutputStream {
	private static final char SEPARATOR = 0x1F;

	private ByteArrayOutputStream responseStream;
	private byte[] responseStreamData;
	private Object deserializedObject;
	private String protocolMsg;
	private int responseStreamStatusCode;

	CommandResponseSerializer() {
		super();
		responseStream = null;
		responseStreamData = new byte[0];
		deserializedObject = null;
		protocolMsg = null;
		responseStreamStatusCode = -1;
	}

	public void write(int b) throws IOException {
		if (responseStream == null) {
			responseStream = new ByteArrayOutputStream();
		}
		responseStream.write(b);
	}

	public void close() throws IOException {
		byte[] newBytes = closeByteArrayOutputStream(responseStream);
		if (responseStreamData.length == 0) {
			// stream content has not been stored so far
			responseStreamData = new byte[newBytes.length];
			System.arraycopy(newBytes, 0, responseStreamData, 0,
					newBytes.length);
		} else {
			// append
			byte[] oldBytes = new byte[responseStreamData.length];
			// keep existing content
			System.arraycopy(responseStreamData, 0, oldBytes, 0,
					oldBytes.length);
			responseStreamData = new byte[oldBytes.length + newBytes.length];
			// append new content
			System.arraycopy(oldBytes, 0, responseStreamData, 0,
					oldBytes.length);
			System.arraycopy(newBytes, 0, responseStreamData, oldBytes.length,
					newBytes.length);
		}
	}

	Object getSerializedObjectData(int responseHeaderStatus)
			throws IOException, ClassNotFoundException {
		splitResponseStream(responseHeaderStatus);
		return deserializedObject;
	}

	String getProtocolMessage(int responseHeaderStatus) throws IOException,
			ClassNotFoundException {
		splitResponseStream(responseHeaderStatus);
		return protocolMsg;
	}

	int getResponseStreamStatus(int responseHeaderStatus) throws IOException,
			ClassNotFoundException {
		splitResponseStream(responseHeaderStatus);
		return responseStreamStatusCode;
	}

	private byte[] closeByteArrayOutputStream(
			final ByteArrayOutputStream byteArrayOutputStream)
			throws IOException {
		byte[] storedBytes = new byte[0];
		if (byteArrayOutputStream != null) {
			if (byteArrayOutputStream.size() > 0) {
				storedBytes = byteArrayOutputStream.toByteArray();
			}
			byteArrayOutputStream.close();
		}
		return storedBytes;
	}

	private void splitResponseStream(int responseHeaderStatus)
			throws IOException, ClassNotFoundException {
		if (deserializedObject != null && protocolMsg != null
				&& responseStreamStatusCode != -1) {
			// Response stream has been split before
			return;
		}
		if (responseStreamData.length == 0) {
			// Nothing has been serialized
			return;
		}
		ByteArrayInputStream byteIS = new ByteArrayInputStream(
				responseStreamData);
		ObjectInputStream objectIS = null;
		BufferedReader reader = null;
		try {
			if (responseHeaderStatus == 200) {
				objectIS = new ObjectInputStream(byteIS);
				// get object from response stream (always the first part of the
				// stream)
				deserializedObject = objectIS.readObject();
			}
			// get status code and protocol message from response stream
			reader = new BufferedReader(new InputStreamReader(byteIS, "UTF-8"));
			String allTheRest = reader.readLine();
			if (allTheRest != null && allTheRest.length() >= 5) {
				String stCode = null;
				if (allTheRest.charAt(0) == SEPARATOR) {
					// cut SEPARATOR and protocol part
					stCode = allTheRest.substring(1, 4);
					responseStreamStatusCode = Integer.parseInt(stCode);
					// cut SEPARATOR and status code (3 char + blank)
					protocolMsg = allTheRest.substring(5);
				} else {
					// cut protocol part
					stCode = allTheRest.substring(0, 3);
					responseStreamStatusCode = Integer.parseInt(stCode);
					// cut status code (3 char + blank)
					protocolMsg = allTheRest.substring(4);
				}
			} else {
				responseStreamStatusCode = responseHeaderStatus;
				protocolMsg = "";
			}
		} finally {
			if (objectIS != null) {
				objectIS.close();
			}
			if (reader != null) {
				reader.close();
			}
		}
	}
}

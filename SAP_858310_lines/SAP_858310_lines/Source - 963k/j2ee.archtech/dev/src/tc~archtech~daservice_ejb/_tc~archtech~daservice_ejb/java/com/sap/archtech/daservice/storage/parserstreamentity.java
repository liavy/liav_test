package com.sap.archtech.daservice.storage;

import java.io.IOException;
import java.io.InputStream;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.sap.archtech.daservice.exceptions.SAXParseExceptionExtended;
import com.tssap.dtr.client.lib.protocol.IRequestEntity;
import com.tssap.dtr.client.lib.protocol.IRequestStream;

public class ParserStreamEntity implements IRequestEntity {
	
	private InputStream is;
	private long contentLength = -1L;
	private String contentType;
	private long sentContentLength;

	public ParserStreamEntity(InputStream is, String contentType) {
		this.is = is;
		this.contentType = contentType;
	}

	public void write(IRequestStream destination) throws IOException {
		InputStreamWrapperReadWrite iswrw = null;
		try {
			iswrw = new InputStreamWrapperReadWrite(is, destination.asStream());
			XMLReader parser = XMLReaderFactory.createXMLReader();
			parser.setErrorHandler(new ParserErrorHandler());
			parser.parse(new InputSource(iswrw));
			this.sentContentLength = iswrw.getContentLength();
		} catch (Exception ex) {
			iswrw.close();
			throw new SAXParseExceptionExtended(ex.getMessage());
		}
	}

	public long getSentContentLength() {
		return sentContentLength;
	}

	public long getContentLength() {
		return contentLength;
	}

	public String getContentMD5() {
		return null;
	}

	public String getContentType() {
		return this.contentType;
	}

	public String getEntityType() {
		return null;
	}

	public void reset() {
	}

	public boolean supportsReset() {
		return false;
	}
}

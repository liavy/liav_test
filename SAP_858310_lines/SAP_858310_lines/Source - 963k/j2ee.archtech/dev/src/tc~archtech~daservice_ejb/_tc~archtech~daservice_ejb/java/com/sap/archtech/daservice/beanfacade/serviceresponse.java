package com.sap.archtech.daservice.beanfacade;

/**
 * The <code>ServiceResponse</code> class represents a Value Object holding
 * the command response data required by the methods of the Session Bean Facade.
 */
public class ServiceResponse {
	private final int statusCode;
	private final String serviceMessage;
	private final String protocolMessage;
	private final String xmldasName;
	private final String xmldasRelease;

	ServiceResponse(int statusCode, String serviceMessage,
			String protocolMessage) {
		this(statusCode, serviceMessage, protocolMessage, "", "");
	}

	ServiceResponse(int statusCode, String serviceMessage,
			String protocolMessage, String xmldasName, String xmldasRelease) {
		this.statusCode = statusCode;
		this.serviceMessage = serviceMessage;
		this.protocolMessage = protocolMessage;
		this.xmldasName = xmldasName;
		this.xmldasRelease = xmldasRelease;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public String getServiceMessage() {
		return serviceMessage;
	}

	public String getProtocolMessage() {
		return protocolMessage;
	}

	public String getXmldasName() {
		return xmldasName;
	}

	public String getXmldasRelease() {
		return xmldasRelease;
	}
}

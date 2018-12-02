package com.sap.archtech.daservice.beanfacade;

/**
 * The <code>PackStatusResponse</code> class represents a Value Object holding
 * the <code>ServiceResponse</code> and the status created by the <i>PACK</i>
 * XMLDAS command.
 * 
 */
public class PackStatusResponse {
	private final ServiceResponse serviceResponse;
	private final String packStatus;

	PackStatusResponse(ServiceResponse serviceResponse, String packStatus) {
		this.serviceResponse = new ServiceResponse(serviceResponse
				.getStatusCode(), serviceResponse.getServiceMessage(),
				serviceResponse.getProtocolMessage());
		this.packStatus = packStatus;
	}

	public ServiceResponse getServiceResponse() {
		return new ServiceResponse(serviceResponse.getStatusCode(),
				serviceResponse.getServiceMessage(), serviceResponse
						.getProtocolMessage());
	}

	public String getPackStatus() {
		return packStatus;
	}
}

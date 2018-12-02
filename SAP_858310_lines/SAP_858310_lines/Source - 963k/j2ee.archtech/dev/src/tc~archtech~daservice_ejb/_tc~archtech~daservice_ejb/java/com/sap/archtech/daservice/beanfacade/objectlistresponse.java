package com.sap.archtech.daservice.beanfacade;

import java.util.Collections;
import java.util.List;

/**
 * The <code>ObjectListResponse</code> class represents a Value Object holding
 * the <code>ServiceResponse</code> created by an XMLDAS command and a list of
 * objects also created by that command.
 */
public class ObjectListResponse {
	private final ServiceResponse serviceResponse;
	private final List<Object> objectList;

	ObjectListResponse(ServiceResponse serviceResponse, List<Object> objectList) {
		this.serviceResponse = new ServiceResponse(serviceResponse
				.getStatusCode(), serviceResponse.getServiceMessage(),
				serviceResponse.getProtocolMessage());
		this.objectList = Collections.unmodifiableList(objectList);
	}

	public ServiceResponse getServiceResponse() {
		return new ServiceResponse(serviceResponse.getStatusCode(),
				serviceResponse.getServiceMessage(), serviceResponse
						.getProtocolMessage());
	}

	public List<Object> getObjectList() {
		return Collections.unmodifiableList(objectList);
	}
}

package com.sap.engine.services.servlets_jsp.server.runtime.client.preservation.release;

import com.sap.engine.services.httpserver.interfaces.HttpParameters;
import com.sap.engine.services.servlets_jsp.server.ServiceContext;
import com.sap.engine.services.servlets_jsp.server.runtime.client.preservation.PreservationEntity;

//supplies new request and response objects into the pool without recycling old ones
public class NewStrategy implements ReleaseStrategy{
	transient private ServiceContext serviceContext;
	
	public NewStrategy(ServiceContext context){
		this.serviceContext = context;
	}
	
	public void release(PreservationEntity entity) {
		if (location.beDebug()){
			location.debugT("@@@@@@-Release "+this.toString()+" "+entity.getId());
		}
		HttpParameters http = entity.getRequest().getHttpParameters();
		
		serviceContext.getPoolContext().returnNewRequest();
		serviceContext.getPoolContext().returnNewResponse();
	
		http.justNew();
	}

	
	
	

	public String toString(){
    	return "NewStrategy";
    }

}

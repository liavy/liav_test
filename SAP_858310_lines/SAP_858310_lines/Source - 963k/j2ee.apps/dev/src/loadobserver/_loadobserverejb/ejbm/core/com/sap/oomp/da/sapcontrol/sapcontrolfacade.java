package com.sap.oomp.da.sapcontrol;


import com.sap.oomp.da.PEData;
import com.sap.sapcontrol.*;
import com.sap.sapcontrol.wsclient.SAPControlPortType;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Observable;

public class SAPControlFacade extends Observable{
    private SAPControlPortType port;
    private String previous = "";
    private String process = "";
    private int time = 0;
    
    public SAPControlFacade(SAPControlPortType port){
        this.port = port;
    }
    
    public SAPControlFacade(SAPControlPortType sap_control_port,
			String server_process) {
		this.port = sap_control_port;
		this.process = server_process;
	}

	public StringBuilder getGCInfo(){
        
        StringBuilder result = new StringBuilder();
        ArrayOfGCInfo info = port.j2EEGetVMGCHistory();
        if (info == null || info.getItem() == null || info.getItem().size() == 0){
            return null;
        }
        
        
        int ejb_sessions_count = port.j2EEGetEJBSessionList().getItem().size();
        long ejb_sessions_size = 0;
        List<J2EEEJBSession> e_sessions = port.j2EEGetEJBSessionList().getItem();
        if (e_sessions != null){
            for (J2EEEJBSession s: e_sessions){
                if (s.getSize() > -1){
                    ejb_sessions_size+= s.getSize();
                }
            }
        }
        List<J2EEWebSession> w_sessions = port.j2EEGetWebSessionList().getItem();
        long web_sessions_size = 0;
        if (w_sessions != null){
            for (J2EEWebSession s: w_sessions){
                if (s.getSize() > -1){
                    web_sessions_size+= s.getSize();
                }
            }
        }

        
        int web_sessions_count = port.j2EEGetWebSessionList().getItem().size();
        
        List<GCInfo> list = info.getItem();
        Collections.sort(list, new Comparator<GCInfo>() {
            public int compare(GCInfo o1, GCInfo o2) {
                return o1.getStartTime().getValue().compareTo(o2.getStartTime().getValue());
            }
        });
        int size = list.size();
        int index = 0;
        for (; index < size; index++){
            GCInfo gc = list.get(index);
            if (!process.equals("") && !process.equals(gc.getProcessname().getValue())){
            	continue;
            }
            if ( (previous.compareTo(gc.getStartTime().getValue()) < 0) ||
                    ((previous.compareTo(gc.getStartTime().getValue()) == 0) && (time != gc.getDuration()))  ){
            	result.append('\t').append("process:").append('\t').append(gc.getProcessname().getValue());
                result.append('\t').append("time:").append('\t').append(gc.getStartTime().getValue());
                result.append('\t').append("objectsfreed:").append('\t').append(gc.getObjBytesFreed());
                result.append('\t').append("heap:").append('\t').append(gc.getHeapSize());
                result.append('\t').append("duration:").append('\t').append(gc.getDuration());
                result.append('\t').append("gctype:").append('\t').append(gc.getType().getValue());
                result.append('\t').append("web sessions:").append('\t').append(web_sessions_count);
                result.append('\t').append("web sessions size:").append('\t').append(web_sessions_size);
                result.append('\t').append("ejb sessions:").append('\t').append(ejb_sessions_count);
                result.append('\t').append("ejb sessions size:").append('\t').append(ejb_sessions_size);
                result.append('\n');
                this.setChanged();
                this.notifyObservers(new PESAPControlData(gc,ejb_sessions_count,web_sessions_count));
                previous = gc.getStartTime().getValue();
                time = gc.getDuration();
            }     
        }
        
        return result;
    }
    
    
    

}

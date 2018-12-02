package com.sap.oomp.da.sapcontrol;



import com.sap.oomp.da.PEData;
import com.sap.sapcontrol.GCInfo;

public class PESAPControlData extends PEData{
    private GCInfo gc;
    private int sessions;
    
    public PESAPControlData(GCInfo gc, int ejb, int web){
        this.gc = gc;
        sessions = ejb + web;
    }
    
    public String getDate(){
        return gc.getStartTime().getValue();
    }

    public int getSessions() {
        return this.sessions;
    }

    public long getHeap() {
        return gc.getHeapSize();
    }

    public int getGCSpentTime() {
        return gc.getDuration();
    }

    public long getGarbage() {
        return gc.getObjBytesFreed();
    }

}

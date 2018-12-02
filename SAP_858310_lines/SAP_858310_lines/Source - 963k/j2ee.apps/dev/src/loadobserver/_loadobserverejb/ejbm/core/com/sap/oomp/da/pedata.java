package com.sap.oomp.da;

public class  PEData {
    protected String date;
    protected int sessions;
    protected long heap;
    protected int gctime;
    protected long garbage;
    
    
    public PEData(){};
    
    public PEData(String date,  long heap, long garbage, int gctime, int sessions){
            this.date  = date;
            this.sessions = sessions;
            this.heap = heap;
            this.gctime = gctime;
            this.garbage = garbage;
    };

    public String getDate() {
        return date;
    }

    public long getGarbage() {
        return garbage;
    }

    public int getGctime() {
        return gctime;
    }

    public long getHeap() {
        return heap;
    }

    public int getSessions() {
        return sessions;
    }
    
    
    
}

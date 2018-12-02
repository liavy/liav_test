package com.sap.oomp.da;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;


public class GCInfoCollector implements Observer{
    private ArrayList<PEData> current_list = new ArrayList<PEData>(100);
    private final Object lock = new Object();
    private int index = 0;

    public void update(Observable o, Object arg) {
        synchronized(lock){
            if (arg instanceof PEData){
                PEData data = (PEData)arg;
                if (index < 100){
                	current_list.add(data);
                	index++;
                }else{
                	current_list.remove(0);
                	current_list.add(data);
                }
            }
        }    
    }
    
    
    public PEData getAggregatedData(){
         long heapsize = 0;
         long garbage = 0;
         int sessions = 0;
         String date = "";
            synchronized(lock){
                Iterator<PEData> iter = current_list.iterator();
                while (iter.hasNext()){
                    PEData data = iter.next();
                    heapsize = heapsize + data.getHeap();
                    garbage = garbage + data.getGarbage();
                    sessions = sessions + data.getSessions();
                }
                
                if (current_list.size() > 0){
                    int size = current_list.size();
                    heapsize=heapsize/size;
                    garbage=garbage/size;
                    sessions = sessions/size;
                    date = current_list.get(size-1).getDate();
                    current_list.clear();
                    index = 0;
                    return new PEData(date, heapsize, garbage, 0, sessions);
                }    
            }
        return null;
    }
    
    
    
    
}

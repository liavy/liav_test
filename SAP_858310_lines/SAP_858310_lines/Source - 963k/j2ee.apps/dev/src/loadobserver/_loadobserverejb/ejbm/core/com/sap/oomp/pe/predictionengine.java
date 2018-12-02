package com.sap.oomp.pe;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import com.sap.log.LogContext;
import com.sap.oomp.da.GCInfoCollector;
import com.sap.oomp.da.PEData;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

public class PredictionEngine {
	static final long now = System.currentTimeMillis();
	Object lock = new Object();
	
	GCInfoCollector info ;
	long maxHeapSize = 1024 *1024 * 1024;
	int last = -1;
	static final int queue_lenght = 10;
	ArrayList<LoadData> queue = new ArrayList<LoadData>(queue_lenght); 
	SimpleDateFormat dformat = new SimpleDateFormat("yyyy MM dd HH:mm:ss");
	String date = "";
	
	
	
	//interpolation over 10 aggregated results
	
	
	public PredictionEngine(GCInfoCollector collector, long maxHeapSize) {
		this.info = collector;
		this.maxHeapSize = maxHeapSize;
	}


	public int getSystemLoad(){
		synchronized (lock){
		PEData data =  info.getAggregatedData();
		if (data == null){
			return last;
		}
		if (LogContext.getPredictionEngineLocation().beDebug()){
			String tmp = java.text.MessageFormat.format("Aggregated Prediction Engine Data: {0} heap {1} garbage {2}",
					new Object[]{data.getDate(),data.getHeap(),data.getGarbage()});
			LogContext.getCategory().logT(Severity.DEBUG, LogContext.getPredictionEngineLocation(),tmp);
		}
		
		long delta =  data.getHeap() - data.getGarbage();
		double res = (delta*100)/maxHeapSize;
		Double d = new Double(res);
		last = d.intValue();
		if(queue.size()>queue_lenght){
			queue.remove(0);
		}
		Date date = null;
		try {
			date = dformat.parse(data.getDate());
		} catch (ParseException e) {
			LogContext.getCategory().logThrowableT(Severity.WARNING, LogContext.getPredictionEngineLocation(), "Cannot parse predication date",e);
		}
		
		
		queue.add(new LoadData(date.getTime()-now,last));
		
		//based on queue values we derive an approximation
		int result = last;
		
		//load percentage
		long sum_load = 0;
		long sum_time = 0;
		long sum_load_mul_time = 0;
		long sum_square_load = 0;
		long sum_square_time = 0;
		
		
		StringBuilder dump_builder = null;
		if (queue.size() > 1){
			if (LogContext.getPredictionEngineLocation().beDebug()){
				LogContext.getCategory().logT(Severity.DEBUG, LogContext.getPredictionEngineLocation(),"Prediction Engine dump queue:");
				 dump_builder= new StringBuilder();
				 dump_builder.append("\n");
			}
			Iterator<LoadData> iter = queue.iterator();
			
			
			while (iter != null && iter.hasNext()){
				LoadData d1 = iter.next();
				dump_builder.append(d1.getTime()).append("\t").append(d1.getLoad()).append("\n");
				sum_load = sum_load+d1.getLoad();
				sum_time = sum_time + d1.getTime();
				sum_load_mul_time = sum_load_mul_time + (d1.getLoad()*d1.getTime());
				sum_square_time = sum_square_time + (d1.getTime()*d1.getTime());
			}
			int n = queue.size();
			if (LogContext.getPredictionEngineLocation().beDebug()){
				LogContext.getCategory().logT(Severity.DEBUG, LogContext.getPredictionEngineLocation(),"PE queue:"+dump_builder.toString());
				LogContext.getCategory().logT(Severity.DEBUG, LogContext.getPredictionEngineLocation(),"Sum time:"+sum_time);
				LogContext.getCategory().logT(Severity.DEBUG, LogContext.getPredictionEngineLocation(),"Sum load:"+sum_load);
				LogContext.getCategory().logT(Severity.DEBUG, LogContext.getPredictionEngineLocation(),"Sum sq_time:"+sum_square_time);
				LogContext.getCategory().logT(Severity.DEBUG, LogContext.getPredictionEngineLocation(),"Sum time_mul_load:"+sum_load_mul_time);
			}
			long del = n*sum_square_time - (sum_time*sum_time);
			double b = (double)((sum_square_time*sum_load) - (sum_time*sum_load_mul_time))/del;
			double a = (double)((n*sum_load_mul_time) - (sum_load*sum_time))/del;
            
			//check for sign
			double y1 = a*1 +b;
			double y2 = a*2 +b;
			if (y1 < y2){
				if (LogContext.getPredictionEngineLocation().beDebug()){
					LogContext.getCategory().logT(Severity.DEBUG, LogContext.getPredictionEngineLocation(),"Prediction Engine increasing curve");
				}
			}else{
				if (LogContext.getPredictionEngineLocation().beDebug()){
					LogContext.getCategory().logT(Severity.DEBUG, LogContext.getPredictionEngineLocation(),"Prediction Engine decreasing curve");
				}	
			}
			double pred = (100-b)/a;
			long pred_time = new Double(pred).longValue()+now;
			Date dd = new Date(pred_time);
			if (pred_time < System.currentTimeMillis()){
				this.date = "";
			}else{
				this.date = dd.toString();
			}
			if (LogContext.getPredictionEngineLocation().beDebug()){
				Double tmp_a = new Double(a);
				Double tmp_b = new Double(b);
				String tmp = java.text.MessageFormat.format("Prediction Engine a: {0}", new Object[]{tmp_a});
				LogContext.getCategory().logT(Severity.DEBUG, LogContext.getPredictionEngineLocation(),tmp);
				tmp = java.text.MessageFormat.format("Prediction Engine last b: {0}", new Object[]{tmp_b});
				LogContext.getCategory().logT(Severity.DEBUG, LogContext.getPredictionEngineLocation(),tmp);
				tmp = java.text.MessageFormat.format("Prediction Engine pred time: {0}", new Object[]{dd});
				LogContext.getCategory().logT(Severity.DEBUG, LogContext.getPredictionEngineLocation(),tmp);
			}
			
		}
		
		if (LogContext.getPredictionEngineLocation().beDebug()){
			String tmp = java.text.MessageFormat.format("Prediction Engine last aggregation: {0}", new Object[]{last});
			LogContext.getCategory().logT(Severity.DEBUG, LogContext.getPredictionEngineLocation(),tmp);
			tmp = java.text.MessageFormat.format("Prediction Engine last approximation: {0}", new Object[]{result});
			LogContext.getCategory().logT(Severity.DEBUG, LogContext.getPredictionEngineLocation(),tmp);
		}
		}
		return last;
		
	}

	
	public String getPreditcionDate(){
		synchronized (lock){
			return this.date;
		}	
	}
}

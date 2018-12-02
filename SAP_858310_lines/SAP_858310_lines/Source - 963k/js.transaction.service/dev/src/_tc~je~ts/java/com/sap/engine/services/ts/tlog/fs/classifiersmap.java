package com.sap.engine.services.ts.tlog.fs;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.sap.engine.services.ts.TransactionServiceFrame;
import com.sap.engine.services.ts.tlog.TLogFullException;


public class ClassifiersMap {
	
	private Map<String, Integer> map = new HashMap<String, Integer>(TransactionServiceFrame.maxTransactionClassifiers);
	private Set<Integer> ids = new TreeSet<Integer>();
	
	private int count = 1;
	
	protected void add(String s, int i) throws TLogFullException {
		synchronized (map) {
			if(count >= TransactionServiceFrame.maxTransactionClassifiers) {
				throw new TLogFullException("There is no room for more transaction classifiers.");
			}
			map.put(s, i);
			ids.add(i);
			count++;
		}
	}
	
	public int add(String s) throws TLogFullException {
		if(!containsClassifier(s)) {
			synchronized (map) {
				if(!containsClassifier(s)) {
					if(count >= TransactionServiceFrame.maxTransactionClassifiers) {
						throw new TLogFullException("There is no room for more transaction classifiers.");
					}
					int tmp = count++;
					map.put(s, tmp);
					ids.add(tmp);
					return tmp;
				}
			}
		}
		return -1;
	}	
	
	
	/**
	 * Checks if classifier is in storage.
	 * @param String - classifier
	 * @return True if classifier is in storage, false - otherwise.
	 */
	public boolean containsClassifier(String s) {
		synchronized (map) {
			return map.containsKey(s);
		}
	}
	
	/**
	 * Checks if id is in storage.
	 * @param int - id
	 * @return True if id is in storage, false - otherwise.
	 */
	public boolean containsId(int s) {
		synchronized (map) {
			return ids.contains(s);
		}
	}
	
	/**
	 * Method which returns classifier for given id.
	 * @param int - id
	 * @return Classifier associated with this id 
	 */
	public String getClassifier(int id) {
		synchronized (map) {
			Iterator<Map.Entry<String, Integer>> it = map.entrySet().iterator();
			while(it.hasNext()) {
				Map.Entry<String, Integer> entry = it.next();
				if(entry.getValue() == id) {
					return entry.getKey();
				}
			}
		}
		return null;
	}
	
	/**
	 * Method which returns the id for given classifier.
	 * @param String - classifier
	 * @return Id associated with this classifier 
	 */
	public int getId(String s) {
		if(!containsClassifier(s)) {
			return -1;
		}
		return map.get(s);
	}
	
	public boolean isEmpty() {
		synchronized (map) {
			return map.isEmpty();
		}
	}

}
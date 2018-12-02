package com.sap.archtech.daservice.util;

import java.io.InputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.ejb.FinderException;

import com.sap.archtech.daservice.ejb.PackStatusDBLocal;

/**
 * 
 * This class is used to merge a set of InputStreams into one InputStream using
 * SequenceInputStream(Enumeration e). It is used for PACK. The special feature
 * of this implementation of the Enumeration interface is that nextElement()
 * deletes the enumeration-entry before the current element. This is necessary
 * because otherwise all packed resources of one PACK-Request would stay in
 * memory. This implementation of Enumeration is based on an ArrayList.
 * 
 */
public class PackResInStreamEnumeration implements Enumeration<InputStream> {

	private ArrayList<InputStream> list;
	private boolean first;
	private int commitcount;
	private int packedres;
	private PackStatusDBLocal pAccess;

	public PackResInStreamEnumeration(int commitcount, PackStatusDBLocal pAccess)
			throws FinderException {
		list = new ArrayList<InputStream>();
		this.commitcount = commitcount;
		first = true;
		this.pAccess = pAccess;
		this.packedres = 0;
	}

	public boolean hasMoreElements() {
		if (list.size() > 1)
			return true;
		else
			return false;
	}

	public InputStream nextElement() {
		// if it is the first call of this method,
		// do not delete anything
		if (first)
			first = false;
		// otherwise, delete one element
		else {
			list.remove(0);
			// count only every second entry
			if ((list.size() % 2) == 0)
				packedres++;
		}

		// check State, update table
		if ((list.size() % commitcount) == 0)
			this.pAccess.updPackStatus(packedres, new Timestamp(System
					.currentTimeMillis()), "RUNNING");

		// always return the element at the top of the list
		return list.get(0);
	}

	public void addElement(InputStream is) {
		list.add(is);
	}
}

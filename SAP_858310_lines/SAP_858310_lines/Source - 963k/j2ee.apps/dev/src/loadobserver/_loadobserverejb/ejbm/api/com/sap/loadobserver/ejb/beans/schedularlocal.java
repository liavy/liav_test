/**
 * 
 */
package com.sap.loadobserver.ejb.beans;

import javax.ejb.Local;

/**
 * @author I032870
 *
 */
@Local
public interface SchedularLocal {

	public void schedule(long interval,int port);
	public void unschedule();
}

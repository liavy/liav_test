package com.sap.engine.core.thread.execution;

import java.util.Collection;

/**
 * Interface describing the functionality that a RejectionPolicy handler implementation must provide.
 * 
 * @author Elitsa Pancheva
 */
public interface RejectionPolicy {

  public void handle(Runnable r);

  public void handle(Collection r);

}

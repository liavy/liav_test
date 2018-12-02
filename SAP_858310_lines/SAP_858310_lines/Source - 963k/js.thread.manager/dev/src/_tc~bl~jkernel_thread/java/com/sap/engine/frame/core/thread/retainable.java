package com.sap.engine.frame.core.thread;

/**
 * Marker interface used in ThreadContext implementation to identify the ContextObjects that should be transfered from parent to child thread
 * even in case clean child thread is requested.
 * Usually such ContextObjects keep statistics information which has no impact on the execution flow, i.e. they are not part of any 
 * request/response processing.
 * Such ContextObjects should implement Retainable to notify the ThreadContext that they should survive clean thread inheritance. 
 *
 * NOTE: This feature should be used with care. If the ContextObject functionality is not appropriate to be used in clean threads it may bring 
 * hard to debug runtime problems. You need AS Java Server Runtime architectural approval in order to use Retainable. 
 * 
 * @author Elitsa Pancheva
 * @version 720
 */
public interface Retainable {

}

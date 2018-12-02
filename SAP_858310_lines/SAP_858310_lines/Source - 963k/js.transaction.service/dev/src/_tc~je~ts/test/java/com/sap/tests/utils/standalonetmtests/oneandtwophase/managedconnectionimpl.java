package com.sap.tests.utils.standaloneTMTests.oneAndTwoPhase;

import java.io.PrintWriter;
import java.util.ArrayList;

import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ManagedConnectionMetaData;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;

class ManagedConnectionImpl implements ManagedConnection {

	/**
	 * Request info
	 */
	private ConnectionRequestInfo request;
	/**
	 * listeners for this connection
	 */
	private ArrayList<ConnectionEventListener> listeners = null;
	/**
	 * log writer instance
	 */
	private PrintWriter logWriter;
	/**
	 * real connections
	 */
	private ArrayList<Object> conn = new ArrayList<Object>();
	/**
	 * flag indicating if the physical connection to the ResourceManager is destroyed
	 */
	private boolean destroyed = false;
	/**
	 * Synchronization lock for destroying
	 */
	private Object lock = new Object();
	/**
	 * ManagedConnectionFactory instance
	 */
	private DummyManagedConnectionFactory mcf;
	/**
	 * XAResource instance
	 */
	private DummyXAResource xares;
	
	
	/**
	 * Constructor
	 * Creates new ManagedConnection from given parameters
	 * @param mcf - ManagedConnectionFactory
	 * @param request - ConnectionRequestInfo
	 */
	public ManagedConnectionImpl(DummyManagedConnectionFactory mcf, ConnectionRequestInfo request) {
		this.mcf = mcf;
		this.request = request;
		listeners = new ArrayList<ConnectionEventListener>();
	}	
	/**
	 * Returns XAResource instance of ManagedConnection
	 * @return XAResource
	 * @throws ResourceException
	 */
	public XAResource getXAResource() throws ResourceException {
		xares = new DummyXAResource();
		return xares;
	}
	/**
	 * Set XAResource instance with the given parameter
	 * @param XAResource
	 */
	public void setXAResource(DummyXAResource xares) {
		this.xares = xares;
	}
	/**
	 * Checks if ManagedConnection is in transaction
	 * @return true if XAResource is null ot false otherwise
	 */
	public boolean isInTransaction() {
		return (xares != null);
	}
	/**
	 * Adds a connection event listener to the ManagedConnection instance
	 * @param listener - a new ConnectionEventListener to be registered
	 */
	public void addConnectionEventListener(ConnectionEventListener listener) { 
		synchronized (listeners) {
			listeners.add(listener);
		}
	}
	/**
	 * Removes a connection event listener from ManagedConnection instance
	 * @param listener - already registered connection event listener to be removed
	 */
	public void removeConnectionEventListener(ConnectionEventListener l) { 
		synchronized (listeners) {
			listeners.remove(l);
		}
	}
	/**
	 * Used by the container to change the association of an application-level connection 
	 * handle with a ManagedConneciton instance. The container should find the right 
	 * ManagedConnection instance and call the associateConnection method.
	 * @param Object - connection instance
	 * @throws ResourceException
	 */
	public void associateConnection(Object connection) throws ResourceException {
//		if (connection instanceof TestConnectionImplementation) {
//			((TestConnectionImplementation) connection).mc = this;
//			synchronized (conn) {
//				conn.add(connection);
//			}
//		} else
//			throw new ResourceException("the type of connection is unapplicable");
	}
	/**
	 * Application server calls this method to force any cleanup on the ManagedConnection instance
	 * @throws ResourceException
	 */
	public void cleanup() throws ResourceException { 
		synchronized (conn) {
			for(Object i : conn) {
//				TestConnectionImplementation temp = (TestConnectionImplementation) i;
//				temp.mc = null;
			}
			conn.clear();
			xares = null;
		}
	}
	/**
	 * Destroys the physical connection to the underlying resource manager
	 * @throws ResourceException
	 */
	public void destroy() throws ResourceException { 
		synchronized (lock) {
			destroyed = true;
		}
		cleanup();
	}
	/**
	 * Creates a new connection handle for the underlying physical connection represented 
	 * by the ManagedConnection instance. The ManagedConnection uses the Subject and additional 
	 * ConnectionRequest Info (which is specific to resource adapter) to set the state of the physical connection.
	 * @throws ResourceException
	 */
	public Object getConnection(Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException { 
		if (destroyed) {
			throw new ResourceException("Already closed!");
		}
//		TestConnectionImplementation temp = new TestConnectionImplementation(this);
//		synchronized (conn) {
//			conn.add(temp);
//		}
		return null;
	}
	/**
	 * Returns LocalTransaction instance
	 * @throws ResourceException
	 */
	public LocalTransaction getLocalTransaction() throws ResourceException { 
		throw new NotSupportedException("Local transaction not supported");
	}
	/**
	 * Sets the log writer for this ManagedConnection instance
	 * @throws ResourceException
	 */
	public void setLogWriter(PrintWriter out) throws ResourceException {
		logWriter = out;
	}
	/**
	 * Gets the log writer for this ManagedConnection instance
	 * @throws ResourceException
	 */
	public PrintWriter getLogWriter() throws ResourceException {
		return logWriter;
	}
	/**
	 * Gets the metadata information for this connection's underlying EIS resource manager instance
	 * @throws ResourceException
	 */
	public ManagedConnectionMetaData getMetaData() throws ResourceException {
		throw new ResourceException("Not supported method");
	}
	/**
	 * Returns given ManagedConnectionFactory instance
	 * @return ManagedConnectionFactory
	 */
	public ManagedConnectionFactory getManagedConnectionFactory() {
		return mcf;
	}	
	/**
	 * Returns ConnectionRequestInfo instance
	 * @return ConnectionRequestInfo
	 */
	protected ConnectionRequestInfo getRequestInfo() {
		return request;
	}

}
package com.sap.tests.utils.standaloneTMTests.oneAndTwoPhase;

import java.io.PrintWriter;
import java.util.Set;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ManagedConnectionMetaData;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;

public class DummyManagedConnectionFactory implements ManagedConnectionFactory{

	private static final long serialVersionUID = 406740113085198051L;

	public Object createConnectionFactory() throws ResourceException {	
		System.out.println("createConnectionFactory() is called");
		return new DummyConnectionFactoryImpl(this, null);
	}

	public Object createConnectionFactory(ConnectionManager connectionManager)	throws ResourceException {
		System.out.println("createConnectionFactory("+connectionManager+") is called");
		return new DummyConnectionFactoryImpl(this, connectionManager);
	}

	public ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo connectionRequestInfo) throws ResourceException {
		System.out.println("createManagedConnection("+subject+", "+connectionRequestInfo+") is called");		
		return new ManagedConnectionImpl(this, null);
	}

	public PrintWriter getLogWriter() throws ResourceException {
		System.out.println("getLogWriter() is called");
		return null;
	}

	public ManagedConnection matchManagedConnections(Set set, Subject subject, ConnectionRequestInfo connectionRequestInfo) throws ResourceException {
		System.out.println("createManagedConnection("+set+", "+subject+" ,"+connectionRequestInfo+") is called");
		return null;
	}

	public void setLogWriter(PrintWriter arg0) throws ResourceException {		
		System.out.println("setLogWriter() is called");
	}

}

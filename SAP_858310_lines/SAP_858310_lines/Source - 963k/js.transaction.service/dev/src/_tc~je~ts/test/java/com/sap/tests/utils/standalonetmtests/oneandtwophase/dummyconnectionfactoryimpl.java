package com.sap.tests.utils.standaloneTMTests.oneAndTwoPhase;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.ConnectionFactory;
import javax.resource.cci.ConnectionSpec;
import javax.resource.cci.RecordFactory;
import javax.resource.cci.ResourceAdapterMetaData;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ManagedConnectionFactory;

public class DummyConnectionFactoryImpl implements ConnectionFactory {
	
	
	private ConnectionManager connectionManager = null;
	private Reference reference = null;
	private ManagedConnectionFactory managedConnectionFactory = null;
	
	public DummyConnectionFactoryImpl(ManagedConnectionFactory mcf, ConnectionManager new_connectionManager){
		System.out.println("new DummyConnectionFactoryImpl("+new_connectionManager+") is called on CF");		
	 	this.connectionManager = new_connectionManager;
	 	managedConnectionFactory = mcf;
	}
	

	public Connection getConnection() throws ResourceException {
		System.out.println("getConnection()is called on CF");		
		return (Connection)connectionManager.allocateConnection(managedConnectionFactory, null);
	}

	public Connection getConnection(ConnectionSpec connectionSpec) throws ResourceException {
		System.out.println("getConnection("+connectionSpec+") is called on CF");		
		return null;
	}

	public ResourceAdapterMetaData getMetaData() throws ResourceException {
		System.out.println("getMetaData()is called on CF");	
		return null;
	}

	public RecordFactory getRecordFactory() throws ResourceException {
		System.out.println("getRecordFactory()is called on CF");
		return null;
	}

	public void setReference(Reference new_reference) {
		System.out.println("setReference("+new_reference+") is called on CF");
		this.reference = new_reference;
	}

	public Reference getReference() throws NamingException {
		System.out.println("getReference()is called on CF");
		return reference;
	}

}

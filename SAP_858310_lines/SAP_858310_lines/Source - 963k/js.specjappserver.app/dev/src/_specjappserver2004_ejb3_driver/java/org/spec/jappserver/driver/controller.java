/*
 * Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *
 * This source code is provided as is, without any express or implied warranty.
 *
 *  History:
 *  Date        ID, Company               Description
 *  ----------  ----------------          ---------------------------------------------------------------
 *  2001        Shanti Subrmanyam, SUN    Created
 *  2002/04/12  Matt Hogstrom, IBM        Conversion from ECperf 1.1 to SPECjAppServer2001
 *  2002/07/10  Russell R., BEA           Conversion from SPECjAppServer2001 to 
 *                                        SPECjAppServer2002 (EJB2.0).
 *
 *  $Id: Controller.java,v 1.5 2004/02/17 17:15:28 skounev Exp $
 */

package org.spec.jappserver.driver;

import java.rmi.Remote;
import java.rmi.RemoteException;


/**
 * The methods in this interface are the public face of Controller.
 * The Controller is the single remote object that runs on the master
 * machine and with which all other instances of remote servers register.
 * A remote reference to any remote service is obtained by the GUI as 
 * well as the Engine through the controller. Using the controller
 * eliminates the need to run rmiregistry on multiple machines. There
 * is only one remote server object (the Controller) that is known
 * by rmiregistry running on the master machine. Once a reference to
 * the Controller is obtained by the client, it should use the
 * 'getReference' method to obtain a reference to any type of
 * remote server.
 *
 * @author Shanti Subrmanyam
 */
public interface Controller extends Remote {

    /**
     * Register service with Controller.
     * The service name is of the form <name>@<host>
     * For example, a CmdAgent will register itself as CmdAgent@<host>
     * so all CmdAgents on different machiens can be uniquely
     * identified by name.
     * @param name public name of service
     * @param service Remote reference to service
     * @throws RemoteException An error occurred calling the remote method
     */
    public void register(String name, Remote service) throws RemoteException;

    /**
     * Register service with Controller.
     * The service name is of the form <name>@<host>
     * For example, a CmdAgent will register itself as CmdAgent@<host>
     * so all CmdAgents on different machiens can be uniquely
     * identified by name.
     * @param type of service
     * @param name of service
     * @param service Remote reference to service
     * @throws RemoteException An error occurred calling the remote method
     */
    public void register(String type, String name, Remote service) throws RemoteException;

    /**
     * Unregister service from Controller.
     * The controller removes this service from its list and clients
     * can no longer access it. This method is typically called when
     * the service exits.
     * @param name Public name of service
     * @throws RemoteException An error occurred calling the remote method
     */
    public void unregister(String name) throws RemoteException;

    /**
     * Unregister service from Controller.
     * The controller removes this service from its list and clients
     * can no longer access it. This method is typically called when
     * the service exits.
     * @param type Type of service
     * @param name Public name of service
     * @throws RemoteException An error occurred calling the remote method
     */
    public void unregister(String type, String name) throws RemoteException;

    /**
     * Get reference to service from Controller.
     * The controller searches in its list of registered services
     * and returns a remote reference to the requested one.
     * The service name is of the form <name>@<host>
     * @param name Public name of service
     * @return remote reference
     * @throws RemoteException An error occurred calling the remote method
     */
    public Remote getService(String name) throws RemoteException;

    /**
     * Get reference to service from Controller.
     * The controller searches in its list of registered services
     * and returns a remote reference to the requested one.
     * The service name is of the form <name>@<host>
     * @param type Type of service
     * @param name Name of service
     * @return remote reference
     * @throws RemoteException An error occurred calling the remote method
     */
    public Remote getService(String type, String name) throws RemoteException;

    /**
     * Get all references to a type of services from Controller.
     * The controller searches in its list of registered services
     * and returns all  remote references to the requested type.
     * The service name is of the form <name>@<host>
     * @param type of service
     * @return remote references
     * @throws RemoteException An error occurred calling the remote method
     */
    public Remote[] getServices(String type) throws RemoteException;

    /**
     * Get the number of registered Services of a type.
     * @param type of service
     * @return int number of registered services
     * @throws RemoteException An error occurred calling the remote method
     */
    public int getNumServices(String type) throws RemoteException;

}

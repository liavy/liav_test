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
 *  2002/05/12  Matt Hogstrom, IBM        Added syncronization code to avoid race conditions in register.
 *  2002/04/12  Matt Hogstrom, IBM        Conversion from ECperf 1.1 to SPECjAppServer2001
 *  2002/07/10  Russell R., BEA           Conversion from SPECjAppServer2001 to
 *                                        SPECjAppServer2002 (EJB2.0).
 *
 * $Id: ControllerImpl.java,v 1.5 2004/02/17 17:15:28 skounev Exp $
 */

package org.spec.jappserver.driver;

import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.RMISecurityManager;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * This class implements the Controller interface
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
public class ControllerImpl extends UnicastRemoteObject implements Controller {

    private Hashtable servicesTable = new Hashtable();

    ControllerImpl() throws RemoteException {
        super();

        // This code was to check if AllByName returns all other names
        // for the machine
        /*      try {
        InetAddress[] allIPs = InetAddress.getAllByName("echodo");
        for (int i = 0; i < allIPs.length; i++) {
        String printHost = allIPs[i].getHostAddress();
        System.out.println("Host " + i + " = " + printHost);
        }
        }
        catch (java.net.UnknownHostException uhe) {
        System.out.println("ControllerImpl constructor: " + uhe.getMessage());
        System.exit(-1);
        } */
    }


    /**
     * Register service with Controller.
     * The service name is of the form <name>@<host>
     * For example, a CmdAgent will register itself as CmdAgent@<host>
     * so all CmdAgents on different machiens can be uniquely
     * identified by name.
     * @param name Public name of service
     * @param service Remote reference to service
     */
    public synchronized void register(String name, Remote service) {
        // First check if service already exists
        Remote r = (Remote) servicesTable.get(name);
        if (r != null)
            unregister(name);
        System.out.println("Controller: Registering " + name +
                " on machine " + getCaller());
        servicesTable.put(name, service);
    }

    /**
     * register service with Controller
     * The service name is of the form <name>@<host>
     * For example, a CmdAgent will register itself as CmdAgent@<host>
     * so all CmdAgents on different machiens can be uniquely
     * identified by name.
     * @param type Type of service
     * @param name Name of service
     * @param service Remote reference to service
     */
    public synchronized void register(String type, String name,
                                      Remote service) {
        // First check if the type of service exists
        Hashtable h = (Hashtable) servicesTable.get(type);

        if (h == null) {
            h = new Hashtable();
            servicesTable.put(type, h);
        }

        // check if service already exists
        Remote r = (Remote) h.get(name);
        if (r != null)
            unregister(type, name);
        System.out.println("Controller: Registering " + name +
                " on machine " + getCaller());
        h.put(name, service);
    }

    /**
     * unregister service from Controller
     * The controller removes this service from its list and clients
     * can no longer access it. This method is typically called when
     * the service exits.
     * @param name public name of service
     */
    public synchronized void unregister(String name) {
        servicesTable.remove(name);
    }

    /**
     * unregister service from Controller
     * The controller removes this service from its list and clients
     * can no longer access it. This method is typically called when
     * the service exits.
     * @param type Type of service
     * @param name Public name of service
     */
    public synchronized void unregister(String type, String name) {
        // First check if the type of service exists
        Hashtable h = (Hashtable) servicesTable.get(type);

        if (h == null) {
            Debug.println(
                    "Controller.unregister : Cannot find Service type : " +
                    type);
        } else {
            h.remove(name);
        }
    }


    /**
     * get reference to service from Controller
     * The controller searches in its list of registered services
     * and returns a remote reference to the requested one.
     * The service name is of the form <name>@<host>
     * @param name Public name of service
     * @return remote reference
     */
    public synchronized Remote getService(String name) {
        Remote r = (Remote) servicesTable.get(name);
        return(r);
    }

    /**
     * get reference to service from Controller
     * The controller searches in its list of registered services
     * and returns a remote reference to the requested one.
     * The service name is of the form <name>@<host>
     * @param type Type of service
     * @param name Public name of service
     * @return remote reference
     */
    public synchronized Remote getService(String type, String name) {
        Remote r = null;
        // First check if the type of service exists
        Hashtable h = (Hashtable) servicesTable.get(type);

        if (h == null)
        {
            Debug.println(
                    "Controller.getService : Cannot find Service type : " +
                    type);
        }
        else
        {
            r = (Remote) h.get(name);
        }
        return(r);
    }

    /**
     * get all references to a type of services from Controller
     * The controller searches in its list of registered services
     * and returns all  remote references to the requested type.
     * The service name is of the form <name>@<host>
     * @param type of service
     * @return remote references
     */
    public synchronized Remote[] getServices(String type) {
        Remote[] r = null;
        // First check if the type of service exists
        Hashtable h = (Hashtable) servicesTable.get(type);

        if (h == null)
        {
            Debug.println(
                    "Controller.getServices : Cannot find Service type : " +
                    type);
        }
        else
        {
            r = new Remote[h.size()];
            Enumeration _enum = h.elements();
            int i = 0;
            while (_enum.hasMoreElements())
                r[i++] = (Remote) _enum.nextElement();
        }
        return(r);
    }

    /**
     * Get the number of registered Services of a type.
     * @param type of service
     * @return int number of registered services
     */
    public synchronized int getNumServices(String type) {
        // First check if the type of service exists
        Hashtable h = (Hashtable) servicesTable.get(type);
        int i = 0;
        if (h == null)
        {
            Debug.println(
                    "Controller.getNumServices : Cannot find Service type : " +
                    type);
        }
        else
        {
            i = h.size();
        }
        return i;
    }

    // Get the caller
    private String getCaller() {
        String s = null;

        try {
            s = getClientHost();
        } catch (Exception e) {
           e.printStackTrace();
            Debug.println(e.getMessage());
        }

        return s;
    }

    /**
     * Registration for RMI serving.
     * @param argv The command line arguments
     */
    public static void main(String [] argv) {

        String s = null;
        System.setSecurityManager (new RMISecurityManager());

        try {
            Controller c = new ControllerImpl();
            String host = (InetAddress.getLocalHost()).getHostName();
            s = "//" + host + "/" + "Controller";
            Naming.bind(s , c);
            System.out.println("Binding controller to " + s);
        } catch (Exception e) {
            e.printStackTrace();
            try
            {
                Naming.unbind(s);
            }
            catch (Exception ei)
            {
               e.printStackTrace();
            }
            System.exit(-1);
        }
    }
}




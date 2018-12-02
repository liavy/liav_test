/*
 * Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *
 * This source code is provided as is, without any express or implied warranty.
 *
 *  History:
 *  Date        ID, Company               Description
 *  ----------  -----------------------   ---------------------------------------------------------------
 *  2001        Akara Sucharitakul, SUN   Created
 *  2002/04/12  Matt Hogstrom, IBM        Conversion from ECperf 1.1 to SPECjAppServer2001
 *  2002/07/10  Russell R., BEA           Conversion from SPECjAppServer2001 to
 *                                        SPECjAppServer2002 (EJB2.0).
 *
 * $Id: Environment.java,v 1.2 2004/02/17 17:16:03 skounev Exp $
 */

package org.spec.jappserver.launcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

/**
 * This class creates and provides access to the environment
 * needed to run SPECjAppServer from a java program. It requires the
 * system property "specjappserver.home" to be set and will read the
 * environment from the env file specified in the config
 * directory.<br>
 * If an environment is needed from the parent shell, it
 * has to be passed into the java command line using
 * -Denvironment.[VAR]=$[VAR].
 * @author Akara Sucharitakul
 * @see Launcher
 */
public class Environment {

    Properties envProp;

    /**
     * Constructor reads the environment from the config directory.
     * @exception IOException specjappserver.home not defined or cannot
     *                        read config/env files
     */
    public Environment() throws IOException {
        /* Read the environment file */
        final File envFile = new File(new File("config"), "netweaver.env");
        BufferedReader reader = new BufferedReader(new FileReader(envFile));
        envProp = new Properties();


        // Populate environment from System property starting with "environment."

        for (Enumeration propNames = System.getProperties().propertyNames();
             propNames.hasMoreElements();) {

            String propName = (String) propNames.nextElement();
            if (propName.startsWith("environment."))
                envProp.setProperty(propName.substring("environment.".length()),
                    System.getProperty(propName));
        }

        // Populate environment from env file
        String line = reader.readLine();
        while (line != null) {
        	int mark = line.indexOf('#');
            if (mark >= 0)
                line = line.substring(0, mark).trim();

            mark = line.indexOf('=');

            if (mark > 0) {
                String propName = line.substring(0, mark).trim();
                String propVal  = line.substring(mark + 1).trim();
                set(propName, propVal);
            }
            line = reader.readLine();
        }
    }

    /**
     * Substitutes shell variables with valid values.
     * Currently we only support Unix style shell variables
     * DOS/Win32 style can be added later.
     * This is tricky since we do not have endings, so the
     * current algorithm is rather rudimentary. If needed,
     * we have to come up with some better algorithms.
     * @param propVal The environment string befor substitution
     * @return The substituted environment string
     */
    private String substitute(String propVal) {
       int mark = propVal.indexOf('$');
        if (mark >= 0) {
            StringBuffer propValBuf = new StringBuffer(propVal);
            do {
                String shellVar = null;
                String shellVal = null;
                ++mark;
                if (mark >= propVal.length())
                {
                   return propVal;
                }
                if (propVal.charAt(mark) == '{') {
                    /* With enclosing brackets, we just find the next
                     * closing bracket and deal with the whole content.
                     */
                    int endMark = propVal.indexOf('}', mark + 1);
                    if (endMark < 0)
                        return propVal;

                    shellVar = propVal.substring(mark + 1, endMark);
                    shellVal = envProp.getProperty(shellVar);

                    if (shellVal == null)
                        propValBuf.replace(mark - 1, endMark + 1, "");
                    else
                        propValBuf.replace(mark - 1, endMark + 1, shellVal);

                } else {
                    /* Without enclosing brackets we don't know the exact
                     * ending so the code has to try to match the longest
                     * possible match. This code is quite tricky
                     */
                    char[] varBuf = new char[propVal.length() - mark];
                    propVal.getChars(mark, propVal.length(), varBuf, 0);
                    int len = 0;
                    for (; len < varBuf.length; len++)
                        if (!(varBuf[len] >= '0' && varBuf[len] <= '9') &&
                            !(varBuf[len] >= 'a' && varBuf[len] <= 'z') &&
                            !(varBuf[len] >= 'A' && varBuf[len] <= 'Z') &&
                            (varBuf[len] != '_')) {
                            shellVar = new String(varBuf, 0, len);
                            break;
                        }
                    if (shellVar == null)
                        shellVar = new String(varBuf, 0, varBuf.length);
                    int maxVarLen = shellVar.length();
                    do {
                        shellVal = envProp.getProperty(shellVar);
                        if (shellVal != null)
                            break;
                        shellVar = new String(varBuf, 0, --len);
                    } while (len > 0);
                    if (shellVal == null)
                        propValBuf.replace(mark - 1, mark + maxVarLen, "");
                    else
                        propValBuf.replace(mark - 1, mark + shellVar.length(),
                                           shellVal);
                }
                propVal = propValBuf.toString();
                mark = propVal.indexOf('$');
            } while (mark >= 0);
        }
        return propVal;
    }

    /**
     * Provides an array of Strings representing the environment
     * suitable for use in the Launcher.exec(), Launcher.bgExec()
     * and Runtime.exec() calls.
     * @return environment in a String array
     */
    public String[] getList() {
        String[] envList = new String[envProp.size()];
        int i = 0;
        for (Enumeration e = envProp.propertyNames(); e.hasMoreElements();) {
            String envName = (String) e.nextElement();
            envList[i] = envName + '=' + envProp.getProperty(envName);
            i++;
        }
        return envList;
    }

    /**
     * Returns the environment represented by a variable.
     * @param  propName The environment variable
     * @return The value of the variable
     */
    public String get(String propName) {
        return envProp.getProperty(propName);
    }

    /**
     * Returns the environment represented by a variable.
     * @param  propName The environment variable
     * @defaultProp default value if not found
     * @return The value of the variable
     */
    public String get(String propName,  String defaultProp) {
        return envProp.getProperty(propName, defaultProp);
    }

    /**
     * Sets an environment variable.
     * @param propName Variable name
     * @param propValue The variable's value
     */
    public void set(String propName, String propValue) {

        int spcIdx;

        /* Take off any quotes */
        if ((propValue.startsWith("\"") && propValue.endsWith("\"")) ||
            (propValue.startsWith("'")  && propValue.endsWith("'")))
            propValue = propValue.substring(1, propValue.length() - 1);

         /* If not quoted, the value is only to the first space.
         * The rest is ignored.
         */
        else if ((spcIdx = propValue.indexOf(' ')) >= 0)
            propValue = propValue.substring(0, spcIdx);

        /* Substitute the shell variable */
        propValue = substitute(propValue);

        if ( propName.equalsIgnoreCase("CLASSPATH")) {
       	   String	ps = System.getProperty("path.separator");
           
           if ( ps.equals(":")) {
        	  String oldClasspath = propValue;
              propValue = propValue.replace(';',':');
              
          	  System.out.println("Classpath environment variable changed.");
          	  System.out.println("Old ="+oldClasspath+"\nNew="+propValue);
        	}
        }
        /* Then push the environment */
        envProp.setProperty(propName, propValue);
    }

    public static void main(String[] args) {
        try {
            Environment env = new Environment();
            String[] list = env.getList();
            for (int i = 0; i < list.length; i++)
                System.out.println(list[i]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

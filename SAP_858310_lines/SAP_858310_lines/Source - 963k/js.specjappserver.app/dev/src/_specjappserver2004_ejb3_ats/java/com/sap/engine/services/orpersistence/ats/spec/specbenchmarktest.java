package com.sap.engine.services.orpersistence.ats.spec;

import java.util.Properties;

import com.sap.ats.Test;
import com.sap.ats.env.DeployEnvironment;
import com.sap.ats.env.HttpEnvironment;
import com.sap.ats.env.LogEnvironment;
import com.sap.ats.env.system.EnvironmentFactory;
// import com.sap.engine.services.orpersistence.ats.Resources;

@SuppressWarnings("static-access")
public class SpecBenchmarkTest implements Test {
    private LogEnvironment log;

    private static final String DDIC = "res/SPECjAppServer2004_EJB3_ddic.sda";
    private static final String CONTENT = "res/SPECjAppServer2004_EJB3_content.sda";
    private static final String EAR = "res/SPECjAppServer2004_EJB3_ear.sda";
    private static final String BROWSE_REQUEST = "res/browse_request.txt";
    private static final String PROPERTY_HTTP_REQUESTS = "requests_file";

    public int prepare() throws Exception {
        log = (LogEnvironment) EnvironmentFactory.getEnvironment(EnvironmentFactory.LOG);
        try {

            DeployEnvironment deploy = (DeployEnvironment) EnvironmentFactory.getEnvironment(EnvironmentFactory.DEPLOY);

            log.log("Deploying database tables " + DDIC + "...");
            deploy.deployEar(DDIC, false);

            log.log("Deploying content " + CONTENT + "...");
            deploy.deployEar(CONTENT, false);

            log.log("Deploying application " + EAR + "...");
            deploy.deployEar(EAR, false);

            log.log("OK");

            return Test.PASSED;
        } catch (Exception exc) {
            log.log(exc);
            return Test.NOT_APPLICABLE;
        }
    }

    public int requests() throws Exception {
        Properties props = new Properties();
        props.put(PROPERTY_HTTP_REQUESTS, BROWSE_REQUEST);
        HttpEnvironment loginAndBrowse = (HttpEnvironment) EnvironmentFactory.getEnvironment(props, EnvironmentFactory.HTTP);
        return loginAndBrowse.check();
    }
}
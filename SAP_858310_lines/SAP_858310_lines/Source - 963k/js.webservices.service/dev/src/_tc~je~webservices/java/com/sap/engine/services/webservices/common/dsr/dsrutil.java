package com.sap.engine.services.webservices.common.dsr;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import com.sap.jdsr.passport.DSRPassport;
import com.sap.jdsr.statistics.DSRIRecord;
import com.sap.jdsr.statistics.reader.DsrDataReader;
import com.sap.jdsr.writer.DsrIPassport;
import com.sap.jmx.ObjectNameFactory;
import com.sap.jmx.remote.JmxConnectionFactory;

public class DSRUtil {
  public static class Passport {
    private String passport;

    private String transId;

    private String rootId;

    public Passport(String passport, String transId, String rootId) {
      this.passport = passport;
      this.transId = transId;
      this.rootId = rootId;
    }

    public String getPassport() {
      return passport;
    }

    public String getTransId() {
      return transId;
    }

    public String getRootId() {
      return rootId;
    }
  } 

  private MbeanServerDsrWrapper mBeanServerWrapper;

  private String transId;

  private String rootId;

  private String connId;

  private int connectionCounter = 0;

  private String[] dsrRoots;

  private Method logMethod;

  private Object log;

  public DSRUtil(Properties connectionProperties, String sid, Object log, String logMethod) throws Exception {
    MBeanServerConnection mbeanServer = JmxConnectionFactory
    .getMBeanServerConnection(JmxConnectionFactory.PROTOCOL_ENGINE_P4,
        connectionProperties);
    mBeanServerWrapper = new MbeanServerDsrWrapper(mbeanServer, sid);

    if (log != null){
      this.log = log;
      this.logMethod = log.getClass().getMethod(logMethod, String.class);
    }
    String[] roots = mBeanServerWrapper.getDsrRootDirectories();
    if (roots == null) {
      log("could not find DSR root folder");
      throw new RuntimeException("Could not find DSR root folder");
    }

    dsrRoots = roots;

    log("OK");
    log(dsrRoots.length + " dsrRoots found");
    for (String r : roots){
      log("dsrRoot: [" + r + "]");
    }
  }

  public Passport generateHexPassport() throws Exception{
    return generateHexPassport(32);
  }

  private Passport generateHexPassport(int length) throws Exception{
    transId = generateRandomHexString(length);
    rootId = generateRandomHexString(length);
    connId = generateRandomHexString(length);

    byte[] rootIdBytes = hexToByteArray(rootId);
    byte[] connIdBytes = hexToByteArray(connId);
    String sysId = "MyTestSys";
    String user = "MyUser";
    String prevSys = "PrevSysId";
    String action = "MyTestAction";
    String client = "012";

    DsrIPassport pass = new DSRPassport(3, 0, sysId.getBytes(), 1, user //$JL-I18N$
        .getBytes(), action.getBytes(), 1, prevSys.getBytes(), transId //$JL-I18N$
        .getBytes(), client.getBytes(), 1, rootIdBytes, connIdBytes, //$JL-I18N$
        connectionCounter);
    String hexPass = byteArrayToHex(pass.getNetPassport());

    log("Passport generated: [" + hexPass + "]");
    log("TransId: [" + transId + "]");
    log("RootId: [" + rootId + "]");
    log("ConnId: [" + connId + "]\n\n");

    return new Passport(hexPass, transId, rootId);
  }

  private String generateRandomHexString(int symbols) {
    StringBuilder sb = new StringBuilder(symbols);
    int index;
    for (int i = 0; i < symbols; i++) {
      index = (int) (HEXCHARS.length * Math.random());
      if (index >= HEXCHARS.length) {
        index = HEXCHARS.length / 2;
      }
      sb.append(HEXCHARS[index]);
    }

    return sb.toString().toLowerCase(Locale.ENGLISH);
  }

  public void cleanAllDSRs() throws Exception {
    mBeanServerWrapper.clean();
  }

  public void flush() throws Exception {
    mBeanServerWrapper.flush();
  }

  public void passportCheck(List<DSRIRecord> recs, String CLIENT_NAME,
      String SERVICE_NAME) throws Exception{
    //int i = 0;
    int clientRecords = 0;
    int serviceRecords = 0;
    String action;
    for (DSRIRecord dr : recs) {
      action = dr.getMainRecord().getAction();
      if (action == null) {
        continue;
      }

      /*log("action: [" + dr.getMainRecord().getAction() + "]");
      log("connection  counter: ["
          + dr.getPassportRecord().getConnectionCounter());
      log("client number: [" + dr.getPassportRecord().getClientNumber() + "]");
      log("connIdHex: [" + dr.getPassportRecord().getConnectionIdHex() + "]");
      log("service: [" + dr.getPassportRecord().getService() + "]");
      log("subRecordCert [" + dr.getPassportRecord().getSubRecordCert() + "]");
      log("connID [" + dr.getPassportRecord().getConnectionIdHex() + "]");
      log("start time: [" + dr.getMainRecord().getStartTime() + "]");
      Date d = new Date(dr.getMainRecord().getStartTime());
      log("start time: [" + d.getHours() + ":" + d.getMinutes() + ":"
          + d.getSeconds() + "]");
      log("service type: [" + dr.getMainRecord().getServiceType() + "]");
      log("sent bytes: [" + dr.getMainRecord().getSentBytes() + "]");

      log("\n\n");*/

      if (CLIENT_NAME.equals(action)) {
        clientRecords++;
      } else if (SERVICE_NAME.equals(action)) {
        serviceRecords++;
      }
    }
    log(clientRecords + " client records, " + serviceRecords
        + " service records");
  }

  public static String byteArrayToHex(byte[] byteArray) {
    String hex = null;
    char lowChar, highChar;
    int halfByte;
    if (byteArray != null) {
      char[] charArray = new char[byteArray.length * 2];
      for (int i = 0, j = 0; i < byteArray.length; i++) {
        j = 2 * i;
        halfByte = ((byteArray[i] & 0xF0) >> 4);
        highChar = HEXCHARS[halfByte];
        charArray[j] = highChar;
        halfByte = ((byteArray[i] & 0x0F));
        lowChar = HEXCHARS[halfByte];
        charArray[j + 1] = lowChar;
      }
      hex = new String(charArray);
    }
    return hex;
  }

  public byte[] hexToByteArray(String _hex) {

    String hex = _hex.toUpperCase(Locale.ENGLISH);
    int l = hex.length();
    int len = l / 2;
    int count = 0;
    int base = 0;
    int position = 0;
    byte[] result = new byte[len];
    char[] chars = hex.toCharArray();

    for (int i = 0; i < l; i++) {
      int index = indexOfHex(chars[i]);
      if (index < 0)
        return null;
      if (position == 0) {
        base = index;
        position++;
      } else {
        base <<= 4;
        base |= index;
        result[count++] = (byte) base;
        position = 0;
      }
    }
    if (position != 0)
      return null;

    return result;
  }

  private int indexOfHex(char hex) {
    for (int i = 0; i < 16; i++) {
      if (hex == HEXCHARS[i])
        return i;
    }
    return -1;
  }

  public List<DSRIRecord> searchRecords(boolean searchRootId,
      boolean searchConnId, String componentToSkip) throws Exception {
    List<DSRIRecord> res = new ArrayList<DSRIRecord>();

    for (String r : dsrRoots){
      log("\nSearching dsrRoot [" + r + "] ");
      String[] comps = DsrDataReader.getAllAvailableComponents(r);
      log("Found " + comps.length + " components\n");

      if (comps == null) {
        log("Components are null");
        return null;
      }

      for (int i = 0; i < comps.length; i++) {
        log("\nLooking into component " + i + ": " + comps[i]);
        if (componentToSkip != null && comps[i].indexOf(componentToSkip) > -1){
          continue;
        }
        DsrDataReader reader = new DsrDataReader(r, comps[i], 1L,
            Long.MAX_VALUE, null, false);
        DSRIRecord rec = reader.getNextRecord();
        String ti = null;
        boolean found = false; // if records found in this component, don't search further
        while (rec != null) {
          ti = rec.getMainRecord().getTransId();
          //log("Next record TransID: " + ti);
          if (ti == null){
            continue;
          }
          if (ti.equalsIgnoreCase(transId)) {
            log("Transaction ID found");
            if (searchRootId) {
              log("Check passport record");
              DsrIPassport pass = rec.getPassportRecord();
              if (pass == null) {
                log("Passport is null");
                rec = reader.getNextRecord();
                continue;
              }
              if (rootId.equalsIgnoreCase(pass.getRootContextIdHex())) {
                log("Root Context ID found");
                found = true;
              } else {
                log("Root Context ID does not match");
                rec = reader.getNextRecord();
                continue;
              }
              if (searchConnId) {
                found = found || false;
                if (connId.equalsIgnoreCase(pass.getConnectionIdHex())) {
                  log("Connection ID found");
                } else {
                  log("Connection ID does not match");
                  rec = reader.getNextRecord();
                  continue;
                }
                log("Connection counter in pass: [" + pass.getConnectionCounter()
                    + "]");
                if (pass.getConnectionCounter() == connectionCounter) {
                  log("Connection counter found");
                  found = true;
                } else {
                  log("Connection counter does not match");
                  rec = reader.getNextRecord();
                  continue;
                }
              }
            }

            res.add(rec);
          }

          rec = reader.getNextRecord();
        }
        /*if (found){
          log("Passport found, search stopped");
          break;
        }*/
      }
    }
    return res;
  }

  private void log(String s) throws InvocationTargetException, IllegalAccessException{
    if (logMethod == null || log == null){
      return;
    }
    logMethod.invoke(log, s);
  }

  private static final char[] HEXCHARS = { '0', '1', '2', '3', '4', '5', '6',
    '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

  static class MbeanServerDsrWrapper {

    private MBeanServerConnection mbeanServer;

    private Set clusters;

    private String[] clusterNames;

    private ObjectName[] dsrServMBean;

    public MbeanServerDsrWrapper(MBeanServerConnection serv, String systemName)
    throws Exception {
      this.mbeanServer = serv;
      ObjectName clusterMBeans = new ObjectName(
      "com.sap.default:j2eeType=SAP_J2EEClusterNode,*");
      clusters = mbeanServer.queryMBeans(clusterMBeans, null);

      clusterNames = new String[clusters.size()];
      Iterator itr = clusters.iterator(); 
      for (int i = 0; i < clusterNames.length; i++) {
        clusterNames[i] = ((ObjectInstance) itr.next()).getObjectName()
        .getKeyProperty("name");
      }

      dsrServMBean = new ObjectName[clusterNames.length];
      for (int i = 0; i < dsrServMBean.length; i++) {
        dsrServMBean[i] = ObjectNameFactory.getNameForServerChildPerNode(
            "Activity", "DSR", clusterNames[i], systemName);
      }
    }

    public ObjectName[] getDsrMbeanObjects() {
      return dsrServMBean;
    }

    public Object[] invoke(String method, Object[] arg3, String[] arg4)
    throws Exception {
      Object[] res = new Object[clusterNames.length];
      for (int i = 0; i < clusterNames.length; i++) {
        res[i] = mbeanServer.invoke(dsrServMBean[i], method, arg3, arg4);
      }
      return res;
    }

    public String[] getDsrRootDirectories() throws Exception {
      String[] dirs = new String[clusterNames.length];
      for (int i = 0; i < clusterNames.length; i++) {
        dirs[i] = (String) mbeanServer.invoke(dsrServMBean[i], "getRootDir",
            null, null);
      }
      return dirs;
    }

    public void flush() throws Exception {
      this.invoke("flush", null, null);
    }

    public void clean() throws Exception {
      this.invoke("clean", null, null);
    }

    public void setTraceMode(boolean mode) throws Exception {
      Object[] values = new Object[] { new Boolean(mode) };
      String[] params = new String[] { Boolean.class.getName() };
      this.invoke("setTraceMode", values, params);
    }
  }
}

/*class ATSPropertiesCollector {
  private LogEnvironment logEnv;

  private final boolean beLog;

  private String sid;

  private final Properties connectionProperties;

  public ATSPropertiesCollector(Properties testProps, LogEnvironment logEnv) {
    beLog = (this.logEnv = logEnv) != null;

    log("System properties acquired:\n");
    sid = testProps.getProperty("system_name");
    log("system ID: [" + sid + "]");
    String user = testProps.getProperty("server_user");
    log("user: [" + user + "]");
    String password = testProps.getProperty("server_password");
    // logEnv.log("password: []");

    String host = testProps.getProperty("server_host");
    log("host [" + host + "]");
    String p4Port = testProps.getProperty("p4normal_port");
    log("p4Port [" + p4Port + "]");
    String httpPort = testProps.getProperty("http_port");
    logEnv.log("httpPort: [" + httpPort + "]");

    connectionProperties = new Properties();
    connectionProperties.setProperty(Context.INITIAL_CONTEXT_FACTORY,
        "com.sap.engine.services.jndi.InitialContextFactoryImpl");
    connectionProperties.setProperty(Context.PROVIDER_URL, host + ":" + p4Port);
    connectionProperties.setProperty(Context.SECURITY_PRINCIPAL, user);
    connectionProperties.setProperty(Context.SECURITY_CREDENTIALS, password);
  }

  private void log(String s) {
    if (beLog) {
      logEnv.log(s);
    }
  }

  public String getSID() {
    return sid;
  }

  public LogEnvironment getLogEnvironment() {
    return logEnv;
  }

  public Properties getConnectionProperties() {
    return connectionProperties;
  }
}*/



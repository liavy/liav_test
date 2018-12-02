package org.spec.jappserver.driver.event;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.spec.jappserver.driver.DealerEntry;
import org.spec.jappserver.driver.PlannedLine;
import org.spec.jappserver.driver.http.HttpRequestData;
import org.spec.jappserver.driver.http.SJASHttpException;

/*
 * Add to jvm parameters in instance.properties:
 * -Xeprof:time_on=sigusr2,time_slice=sigusr2
 * disable bootstrap in JC-profile:
 * jstartup/bootstrap = no
 */
public class EprofEventHandler
      implements EventHandler
{
   private static Runtime sRuntime = Runtime.getRuntime();

   private static List<String> getVMPids() throws IOException
   {
      List<String> vmPids = new ArrayList<String>();
      Process p = sRuntime.exec(new String[] {"ps", "-efx"}); // works on HP-UX only
      BufferedReader r = new BufferedReader(new InputStreamReader(p
            .getInputStream()));
      while (true)
      {
         String line = r.readLine();
         if (line == null)
         {
            break;
         }
         if (line.indexOf("jlaunch") != -1
                 || (line.indexOf("jstart") != -1 && line.indexOf("nodeName") != -1)
                 || (isClientProfiling() && line.indexOf("java") != -1 && line.indexOf("") != -1))
         {
            String[] words = line.trim().split("\\s+");
            vmPids.add(words[1]);
//            System.out.println(line);
//            System.out.println(words[1]);
         }
      }
      r.close();
      return vmPids;
   }

   private static void sendSignal(long waitAfterKill)
   {
      try
      {
         for (String vmPid : getVMPids())
         {
//            System.out.println("Sending kill -s SIGUSR2 to process " + vmPid);
            sRuntime.exec(new String[] {"kill", "-s", "SIGUSR2", vmPid}); // works on HP-UX only
         }
         Thread.sleep(waitAfterKill);
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }
   
   public void rampUpStart ()
   {
//      sendSignal();
   }

   public void rampUpEnd ()
   {
//      sendSignal();
   }

   public void steadyStateStart ()
   {
      sendSignal(0);
   }

   public void steadyStateEnd ()
   {
      sendSignal(0);
   }

   public void rampDownStart ()
   {
   }

   public void rampDownEnd ()
   {
   }

   static boolean isProfilingEnabled = false;
   static String requestNamePrefix;
   static String requestName;
   static int counter = 0;

   private static String padZeros(String str, int length) {
       return "0000000000".substring(0, length - str.length()) + str;
   }
   
   private static String removeWhiteSpaces(String str) {
       StringBuffer result = new StringBuffer();
       for (int i = 0; i < str.length(); i++) {
           char c = str.charAt(i);
           if (c != ' ') {
               result.append(c);
           }
       }
       return result.toString();
   }
   
   private static String getName(String reqName) {
       counter++;  // increase counter even if we do not profile
       
       if (requestNamePrefix == null) {
           return null;
       }

       StringBuffer result = new StringBuffer();
       result.append(padZeros("" + counter, 5));
       result.append("_");
       if (requestNamePrefix.length() != 0) {
           result.append(requestNamePrefix);
           result.append("_");
       }
       result.append(reqName);
       return result.toString();
   }
   
   private static String getName(HttpRequestData req) {
       StringBuffer result = new StringBuffer();
       String[] addedKeys = new String[] {"action", "browse", "quantity"};
       for (String str : addedKeys) {
           String param = req.getParam(str);
           if (param != null) {
               result.append(str + "-" + param + "_");
           }
       }

/*       
       Enumeration keys = req.getParamKeys();
       while(keys.hasMoreElements()){
          String key = (String)keys.nextElement();
          String value = req.getParam(key);
          result.append(key + "=" + value);
          if (keys.hasMoreElements()) {
              result.append(",");
          }
       }
*/

       result.deleteCharAt(result.length() - 1);
       return removeWhiteSpaces(result.toString());
   }
   
   /*
    * Setting requestNamePrefix to null means no profiling.
    */
   public static void setRequestNamePrefix (String requestNamePrefix) {
       EprofEventHandler.requestNamePrefix = requestNamePrefix;
   }

   private static boolean isClientProfiling() {
       return requestNamePrefix.equals("RMI") || requestNamePrefix.equals("WS");
   }

   public static void enableProfiling(boolean isEnabled) {
       isProfilingEnabled = isEnabled;
   }

   public static void startProfiling(String reqName)
   {
      requestName = getName(reqName);
      if (isProfilingEnabled && requestName != null)
      {
          sendSignal(5000);
          System.out.println("Profiling " + requestName + " ...");
      }
   }

   public static void startProfiling(HttpRequestData req)
   {
      startProfiling(getName(req));
   }

   public static void endProfiling() {
      if (isProfilingEnabled && requestName != null)
      {
         sendSignal(5000);
         moveEprofFile(System.getProperty("appserverdir"));
         if (isClientProfiling()) {
             moveEprofFile(System.getProperty("clientdir"));
         }
      }
   }

   private static void moveEprofFile(String dir) {
       try {
           String toEprofDir = dir + File.separatorChar + "eprofs";
           String eprofTargetFileName = toEprofDir + File.separatorChar + requestName + ".eprof";
        
           // wait and find eprof file
           FilenameFilter filter = new FilenameFilter() {
               public boolean accept(File dir, String name) {
                   return name.endsWith(".eprof");
               }
           };
           String[] eprofFiles;
           do {
               eprofFiles = (new File(dir)).list(filter);
           } while (eprofFiles == null || eprofFiles.length == 0);
           if (eprofFiles.length != 1) {
               throw new RuntimeException("More than one *.eprof file in directory " + dir);
           }
           String eprofFile = dir + File.separatorChar + eprofFiles[0];
        
           Thread.sleep(7000);

           // move eprof file into eprofs directory
           if ((new File(eprofTargetFileName).exists())) {
               sRuntime.exec(new String[] {"rm", toEprofDir + File.separatorChar + "*.eprof"});
           } else {
               sRuntime.exec(new String[] {"mkdir", "-p", toEprofDir});
               sRuntime.exec(new String[] {"mv", eprofFile, eprofTargetFileName});
        //           System.out.println("Moved eprof file from " + eprofFile + " to " + eprofTargetFileName);
        //           System.out.println();
           }
       } catch (Exception e) {
           e.printStackTrace();
       }
   }

   public static void main (String[] args) throws Exception {
       // do not retry with other customer in case of InsufficientCreditException!
       // we need reporducability even for first request which hits the server!
       int customerId = 4;
       
       DealerEntry dealer = new DealerEntry();
       PlannedLine plannedLine = new PlannedLine();
       
       System.out.println("Executing first requests directly after engine start ...");
       setRequestNamePrefix("HTTP");
       dealer.executeDealerHTTPRequests(customerId);
       EprofEventHandler.enableProfiling(true);
       plannedLine.executePlannedLineRMICalls();

       int max = 1000;
       System.out.println("Executing load " + max + " times ...");
       for (int i = 1; i <= max; i++) {
           System.out.print(i + " ");
           setRequestNamePrefix(null);
           dealer.executeDealerHTTPRequests(customerId);
           EprofEventHandler.enableProfiling(false);
           plannedLine.executePlannedLineRMICalls();
       }

       System.out.println("Executing requests after some load ...");
       setRequestNamePrefix("HTTP");
       dealer.executeDealerHTTPRequests(customerId);
       enableProfiling(true);
       plannedLine.executePlannedLineRMICalls();
       enableProfiling(false);
   }
}

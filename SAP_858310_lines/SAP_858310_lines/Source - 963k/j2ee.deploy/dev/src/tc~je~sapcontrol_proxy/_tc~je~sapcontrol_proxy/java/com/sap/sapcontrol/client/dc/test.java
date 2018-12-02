package com.sap.sapcontrol.client.dc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Properties;

import com.sap.sapcontrol.client.dc.util.DCUtils;

public class Test {

  /**
   * @param args
   */
  public static void main(String[] args) {
    // singleCallTest();
    // threadTest();
    // threadTestWithInput(args[0]);
    fullTest(args);
  }

  private static void singleCallTest() {
    FileWriter fw = null;
    try {
      File output = new File("." + File.separator + "deploy.X.log");

      if (!output.exists()) {
        if (!output.createNewFile()) {
          System.out.println("Could not create file "
              + output.getAbsolutePath());
        }
      } else {
        if (output.delete()) {
          if (!output.createNewFile()) {
            System.out.println("Could not create file "
                + output.getAbsolutePath());
          }
        } else {
          System.out.println("Could not delete file "
              + output.getAbsolutePath());
        }
      }
      // custom connection parameters
//    Properties props = new Properties();
//    props.put(DCClient.ENDPOINT_ADDRESS_PROPERTY, "http://localhost:56613/SAPControl.cgi");
//    DCClient dcSAPCntrl = new DCClient(props);
      DCClient dcSAPCntrl = new DCClient();
      dcSAPCntrl.setDebug(true);

      fw = new FileWriter(output, true);

      dcSAPCntrl.readLogFileContinuous("work", "deploy\\.\\d+\\.log", fw);
      // dcSAPCntrl.readLogFile("work",
      // DCUtils.getFileNameRegex(DCUtils.DEPLOY_X_LOG_REGEX),null);//fw);
      // dcSAPCntrl.readLogFile(new String[]{"work", "a", "b"},
      // DCUtils.getFilePathRegex(DCUtils.DEPLOY_X_LOG_REGEX),fw);

    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } finally {
      try {
        if (fw != null) {
          fw.flush();
          fw.close();
        }
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

  private static void fullTest(String[] args) {
    boolean printInstanceProps = false;
    boolean isDebug = false;
    long timeout = 0;
    for (int i = 0; i < args.length; i++) {
      if (args[i].equals("-ip")) {
        printInstanceProps = true;
      } else if (args[i].equals("-debug")) {
        isDebug = true;
      } else if (args[i].startsWith("-timeout=")) {
        timeout = Long.parseLong(args[i].substring("-timeout=".length() + 1));
      } else if (args[i].startsWith("-timeout=")) {
        timeout = Long.parseLong(args[i].substring("-timeout=".length() + 1));
      }
    }
    File log = new File("." + File.separator + "log.txt");
    if (log.exists()) {
      log.delete();
    }
    if (!log.exists()) {
      try {
        log.createNewFile();
        FileOutputStream fos = new FileOutputStream(log);
        PrintStream ps = new PrintStream(fos);
        System.setOut(ps);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    FileWriter fw = null;
    try {
      File output = new File("." + File.separator + "deploy.X.log");

      if (!output.exists()) {
        if (!output.createNewFile()) {
          System.out.println("Could not create file "
              + output.getAbsolutePath());
        }
      } else {
        if (output.delete()) {
          if (!output.createNewFile()) {
            System.out.println("Could not create file "
                + output.getAbsolutePath());
          }
        } else {
          System.out.println("Could not delete file "
              + output.getAbsolutePath());
        }
        
      }
      // custom connection parameters
//      Properties props = new Properties();
//      props.put(DCClient.ENDPOINT_ADDRESS_PROPERTY, "http://localhost:56613/SAPControl.cgi");
//      DCClient dcSAPCntrl = new DCClient(props);
      DCClient dcSAPCntrl = new DCClient();
      dcSAPCntrl.setStartOnUpdate(false);
      if (isDebug) {
        dcSAPCntrl.setDebug(true);
      }
      if (printInstanceProps) {
        dcSAPCntrl.printInstanceProperties();
      }
      fw = new FileWriter(output, true);
      dcSAPCntrl.readLogFile("work", DCUtils
          .getFileNameRegex(DCUtils.DEPLOY_X_LOG_REGEX), fw);

      // thread test
      if (timeout <= 0) {
        dcSAPCntrl.readLogFileContinuous("work", DCUtils
            .getFileNameRegex(DCUtils.DEPLOY_X_LOG_REGEX), fw);
      } else {
        dcSAPCntrl.readLogFileContinuous("work", DCUtils
            .getFileNameRegex(DCUtils.DEPLOY_X_LOG_REGEX), fw, timeout);
      }
      try {
        Thread.sleep(1600000);
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      dcSAPCntrl.stopLogReading();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } finally {
      try {
        if (fw != null) {
          fw.flush();
          fw.close();
        }
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

  private static void threadTest() {
    // custom connection parameters
//  Properties props = new Properties();
//  props.put(DCClient.ENDPOINT_ADDRESS_PROPERTY, "http://localhost:56613/SAPControl.cgi");
//  DCClient dcSAPCntrl = new DCClient(props);
    DCClient dcSAPCntrl = new DCClient();
    dcSAPCntrl.setDebug(true);
    long waitTime = 12000;
    dcSAPCntrl.readLogFileContinuous("work", DCUtils
        .getFileNameRegex(DCUtils.DEPLOY_X_LOG_REGEX), null, 1000);
    dcSAPCntrl.readLogFileContinuous("work", DCUtils
        .getFileNameRegex(DCUtils.DEPLOY_X_LOG_REGEX), null, 1000);
    dcSAPCntrl.readLogFileContinuous("work", DCUtils
        .getFileNameRegex(DCUtils.DEPLOY_X_LOG_REGEX), null, 1000);

    dcSAPCntrl.readLogFileContinuous("work", DCUtils
        .getFileNameRegex(DCUtils.DEPLOY_X_TRC_REGEX), null, 1000);
    dcSAPCntrl.readLogFileContinuous("work", DCUtils
        .getFileNameRegex(DCUtils.DEPLOY_X_TRC_REGEX), null, 1000);
    dcSAPCntrl.readLogFileContinuous("work", DCUtils
        .getFileNameRegex(DCUtils.DEPLOY_X_TRC_REGEX), null, 1000);
    try {
      Thread.sleep(waitTime);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    dcSAPCntrl.stopLogReading(DCUtils
        .getFileNameRegex(DCUtils.DEPLOY_X_TRC_REGEX));
    try {
      Thread.sleep(waitTime);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    System.out.println("Register new continuous...");
    dcSAPCntrl.readLogFileContinuous("work", DCUtils
        .getFileNameRegex(DCUtils.DEPLOY_X_LOG_REGEX), null, 1000);
    dcSAPCntrl.readLogFileContinuous("work", DCUtils
        .getFileNameRegex(DCUtils.DEPLOY_X_LOG_REGEX), null, 1000);

    dcSAPCntrl.readLogFileContinuous("work", DCUtils
        .getFileNameRegex(DCUtils.DEPLOY_X_TRC_REGEX), null, 1000);
    dcSAPCntrl.readLogFileContinuous("work", DCUtils
        .getFileNameRegex(DCUtils.DEPLOY_X_TRC_REGEX), null, 1000);
    try {
      Thread.sleep(waitTime);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    dcSAPCntrl.stopLogReading(DCUtils
        .getFileNameRegex(DCUtils.DEPLOY_X_LOG_REGEX));
    try {
      Thread.sleep(waitTime);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    dcSAPCntrl.stopLogReading();
    try {
      Thread.sleep(waitTime);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    System.out.println("FINISHED");
  }

  private static void threadTestWithInput(String sEngineWorkDirPath) {
    final File f0 = new File(sEngineWorkDirPath, "dcclient.0.log");
    PrintStream ps0 = null;
//    if (f0.exists()) {
//      f0.delete();
//    }
    if (!f0.exists()) {
      try {
        f0.createNewFile();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    final File f1 = new File(sEngineWorkDirPath, "dcclient.0.trc");
    PrintStream ps1 = null;
//    if (f1.exists()) {
//      f1.delete();
//    }
    if (!f1.exists()) {
      try {
        f1.createNewFile();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    FileWriter fw0 = null;
    File output0 = new File("." + File.separator + "dcclient.X.log");
    try {
      if (!output0.exists()) {
        if (!output0.createNewFile()) {
          System.out.println("Could not create file "
              + output0.getAbsolutePath());
        }
      } else {
        if (output0.delete()) {
          if (!output0.createNewFile()) {
            System.out.println("Could not create file "
                + output0.getAbsolutePath());
          }
        } else {
          System.out.println("Could not delete file "
              + output0.getAbsolutePath());
        }
      }

      fw0 = new FileWriter(output0, true);
    } catch (IOException e) {
      e.printStackTrace();
    }
    FileWriter fw1 = null;
    File output1 = new File("." + File.separator + "dcclient.X.trc");
    try {
      if (!output1.exists()) {
        if (!output1.createNewFile()) {
          System.out.println("Could not create file "
              + output1.getAbsolutePath());
        }
      } else {
        if (output1.delete()) {
          if (!output1.createNewFile()) {
            System.out.println("Could not create file "
                + output1.getAbsolutePath());
          }
        } else {
          System.out.println("Could not delete file "
              + output1.getAbsolutePath());
        }
      }

      fw1 = new FileWriter(output1, true);
    } catch (IOException e) {
      e.printStackTrace();
    }
    Runnable rbl0 = new Runnable() {
      public void run() {
        try {
          FileOutputStream fos0 = new FileOutputStream(f0);
          PrintStream ps0 = new PrintStream(fos0);
          boolean bool = true;
          while (bool) {
            ps0.print('0');
            Thread.sleep(100);
          }
          
        } catch (FileNotFoundException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } catch (InterruptedException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }

      }
    };
    Thread t0 = new Thread(rbl0);
    t0.start();
    Runnable rbl1 = new Runnable() {
      public void run() {
        try {
          FileOutputStream fos1 = new FileOutputStream(f1);
          PrintStream ps1 = new PrintStream(fos1);
          boolean bool = true;
          while (bool) {
            ps1.print('1');
            Thread.sleep(100);
          }
          
        } catch (FileNotFoundException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } catch (InterruptedException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }

      }
    };
    Thread t1 = new Thread(rbl1);
    t1.start();

    long waitTime = 12000;
    try {
      Thread.sleep(waitTime);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    // custom connection parameters
//  Properties props = new Properties();
//  props.put(DCClient.ENDPOINT_ADDRESS_PROPERTY, "http://localhost:56613/SAPControl.cgi");
//  DCClient dcSAPCntrl = new DCClient(props);
    DCClient dcSAPCntrl = new DCClient();
    dcSAPCntrl.setDebug(true);
    dcSAPCntrl.setStartOnUpdate(false);
    
    String sDCClientLogRegex = "dcclient\\.\\d\\.log";
    String sDCClientTrcRegex = "dcclient\\.\\d\\.trc";//"dcclient\\.\\d+\\.log";
    
    dcSAPCntrl
        .readLogFileContinuous("work", sDCClientLogRegex, fw0, 1000);
    dcSAPCntrl
        .readLogFileContinuous("work", sDCClientLogRegex, fw0, 1000);
    dcSAPCntrl
        .readLogFileContinuous("work", sDCClientLogRegex, fw0, 1000);

    dcSAPCntrl
        .readLogFileContinuous("work", sDCClientTrcRegex, fw0, 1000);
    dcSAPCntrl
        .readLogFileContinuous("work", sDCClientTrcRegex, fw1, 1000);
    dcSAPCntrl
        .readLogFileContinuous("work", sDCClientTrcRegex, fw1, 1000);
    try {
      Thread.sleep(waitTime);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    dcSAPCntrl.stopLogReading(sDCClientTrcRegex);
    try {
      Thread.sleep(waitTime);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    System.out.println("Register new continuous...");
    dcSAPCntrl
        .readLogFileContinuous("work", sDCClientLogRegex, fw0, 1000);
    dcSAPCntrl
        .readLogFileContinuous("work", sDCClientLogRegex, fw0, 1000);

    dcSAPCntrl
        .readLogFileContinuous("work", sDCClientTrcRegex, fw1, 1000);
    dcSAPCntrl
        .readLogFileContinuous("work", sDCClientTrcRegex, fw1, 1000);
    try {
      Thread.sleep(waitTime);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    dcSAPCntrl.stopLogReading(sDCClientLogRegex);
    try {
      Thread.sleep(waitTime);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    dcSAPCntrl.stopLogReading();
    try {
      Thread.sleep(waitTime);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    try{
      fw0.flush();
      fw0.close();
      fw1.flush();
      fw1.close();
      
    } catch (Exception e) {
      e.printStackTrace();
    }
    System.out.println("FINISHED");
  }
}

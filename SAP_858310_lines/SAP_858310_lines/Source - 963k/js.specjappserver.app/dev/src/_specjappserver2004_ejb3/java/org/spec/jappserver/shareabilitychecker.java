package org.spec.jappserver;

import java.util.List;

import com.sap.engine.tools.sharecheck.SessionSerializationReport;
import com.sap.engine.tools.sharecheck.SessionSerializationReportFactory;

/*
 * Not in Config class since JBoss does not know classes SessionXX used by this class.
 */
public class ShareabilityChecker
{
   public static void checkSharability(Object obj)
   {
      System.err.println("Checking sharability...");
       try
       {
           SessionSerializationReport report =
               SessionSerializationReportFactory.getInstance().createSerializationReport(
                       obj,
                       SessionSerializationReport.NON_SERIALIZABLE_CLASS |
                       SessionSerializationReport.CUSTOM_SERIALIZATION_CLASS |
                       SessionSerializationReport.NON_SERIALIZABLE_PARENT |
                       SessionSerializationReport.HAS_TRANSIENT_FIELDS |
                       SessionSerializationReport.SERIAL_PERSISTENT_FIELD |
                       SessionSerializationReport.NON_TRIVIAL_FINALIZER,
                       SessionSerializationReportFactory.CLASS_LEVEL,
                       null);
           try
           {
               List or = report.getFullObjectReport();
               if (or!=null)
               {
                   System.err.println("objects: "+or.toString());
               }
           }
           catch (Exception e)
           {
              System.err.println("no object report: "+e.getMessage());
           }
           try
           {
               List cr = report.getClassReport();
               if (cr!=null)
               {
                  System.err.println("classes: "+cr.toString());
               }
           }
           catch (Exception e)
           {
              System.err.println("no classes report: "+e.getMessage());
           }
           System.err.println("session-size:"+report.getSessionSizeInBytes());
       }
       catch (Exception e)
       {
          e.printStackTrace();
          System.err.println("report error: "+e.getMessage());
       }
   }
}

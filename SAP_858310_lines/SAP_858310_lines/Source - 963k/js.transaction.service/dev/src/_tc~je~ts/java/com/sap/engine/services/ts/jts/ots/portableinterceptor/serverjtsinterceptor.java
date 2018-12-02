package com.sap.engine.services.ts.jts.ots.PortableInterceptor;

import org.omg.CORBA.Any;
import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.ORB;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CosTSPortability.Receiver;
import org.omg.CosTransactions.PropagationContext;
import org.omg.CosTransactions.PropagationContextHelper;
import org.omg.CosTransactions.PropagationContextHolder;
import org.omg.IOP.Codec;
import org.omg.IOP.ServiceContext;
import org.omg.IOP.CodecPackage.FormatMismatch;
import org.omg.IOP.CodecPackage.InvalidTypeForEncoding;
import org.omg.IOP.CodecPackage.TypeMismatch;
import org.omg.PortableInterceptor.ServerRequestInfo;
import org.omg.PortableInterceptor.ServerRequestInterceptor;

import com.sap.engine.services.ts.Log;
import com.sap.engine.services.ts.jts.TransactionServiceImpl;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;
import com.sap.tc.logging.SimpleLogger;

public class ServerJTSInterceptor extends org.omg.CORBA.LocalObject implements ServerRequestInterceptor {//$JL-SER$

  private static final Location LOCATION = Location.getLocation(ServerJTSInterceptor.class);

  public String name = null;
  private Receiver receiver = null;
  private Codec codec = null;
  private int jtsId = 0;
  private static int idCounter = 0;
  private static Object syncObj = new Object();

  public ServerJTSInterceptor(Codec codec) {
    this.receiver = TransactionServiceImpl.getReceiver();
    this.codec = codec;
    this.name = "jts";
  }

  public String name() {
    return name;
  }

  public void destroy() {

  }

  public void receive_request_service_contexts(ServerRequestInfo sri) {

    if (LOCATION.beLogged(Severity.DEBUG)) {
//      LOCATION.logT(Severity.DEBUG, "ServerJTSInterceptor.receive_request_service_contexts({0})", new Object[]{Log.objectToString(sri)});
      SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000236", "ServerJTSInterceptor.receive_request_service_contexts({0})", new Object[]{Log.objectToString(sri)});
    }

    if (receiver != null) {
      synchronized(syncObj) {
        jtsId = idCounter++;
      }
      ServiceContext sc = null;

      try {
        sc = sri.get_request_service_context(0);
      } catch (BAD_PARAM ex){
      	//$JL-EXC$
      }

      if (sc != null) {
        Any any = null;
        try {
          TypeCode typeCode = PropagationContextHelper.type();
          any = codec.decode_value(sc.context_data, typeCode);
        } catch (TypeMismatch e) {
          if (LOCATION.beLogged(Severity.ERROR)) {
//            LOCATION.traceThrowableT(Severity.ERROR, "ServerJTSInterceptor.receive_request_service_contexts", e);
            SimpleLogger.traceThrowable(Severity.ERROR,LOCATION,e, "ASJ.trans.000049", "ServerJTSInterceptor.receive_request_service_contexts");
          }
          throw new INTERNAL(0, CompletionStatus.COMPLETED_YES);
        } catch (FormatMismatch e) {
          if (LOCATION.beLogged(Severity.ERROR)) {
//            LOCATION.traceThrowableT(Severity.ERROR, "ServerJTSInterceptor.receive_request_service_contexts", e);
            SimpleLogger.traceThrowable(Severity.ERROR,LOCATION,e, "ASJ.trans.000050", "ServerJTSInterceptor.receive_request_service_contexts");
          }
          throw new INTERNAL(0, CompletionStatus.COMPLETED_YES);
        }
        PropagationContext prContext = PropagationContextHelper.extract(any);
        receiver.received_request(jtsId, prContext);
      }
    }
  }

  public void receive_request(ServerRequestInfo sri) {
    //     System.out.println(">>>> Interceptor receive_request! ");
  }

  public void send_reply(ServerRequestInfo sri) {
    if (LOCATION.beLogged(Severity.DEBUG)) {
//      LOCATION.logT(Severity.DEBUG, "ServerJTSInterceptor.send_reply({0})", new Object[]{Log.objectToString(sri)});
      SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000237", "ServerJTSInterceptor.send_reply({0})", new Object[]{Log.objectToString(sri)});
    }
    if (receiver != null) {
      PropagationContextHolder prContext = new PropagationContextHolder();
      receiver.sending_reply(sri.request_id(), prContext);

      if (prContext.value != null) {
        Any any = ORB.init().create_any();
        PropagationContextHelper.insert(any, prContext.value);
        byte[] ctxData = null;
        try {
          ctxData = codec.encode_value(any);
        } catch (InvalidTypeForEncoding e) {
          if (LOCATION.beLogged(Severity.ERROR)) {
//            LOCATION.traceThrowableT(Severity.ERROR, "ServerJTSInterceptor.send_reply", e);
            SimpleLogger.traceThrowable(Severity.ERROR,LOCATION,e, "ASJ.trans.000051", "ServerJTSInterceptor.send_reply");
          }
          throw new INTERNAL(e.toString());
        }
        ServiceContext sc = new ServiceContext(0, ctxData);
        sri.add_reply_service_context(sc, false);
      }
    }
  }

  public void send_exception(ServerRequestInfo sri) {

  }

  public void send_other(ServerRequestInfo sri) {

  }

}


package com.sap.engine.services.ts.jts.ots.PortableInterceptor;

import com.sap.engine.services.ts.jts.TransactionServiceImpl;
import com.sap.engine.services.ts.Log;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;
import com.sap.tc.logging.SimpleLogger;

import org.omg.CORBA.Any;
import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.Environment;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.INVALID_TRANSACTION;
import org.omg.CORBA.LocalObject;
import org.omg.CORBA.ORB;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CosTSPortability.Sender;
import org.omg.CosTransactions.PropagationContext;
import org.omg.CosTransactions.PropagationContextHelper;
import org.omg.CosTransactions.PropagationContextHolder;
import org.omg.IOP.Codec;
import org.omg.IOP.CodecPackage.FormatMismatch;
import org.omg.IOP.CodecPackage.InvalidTypeForEncoding;
import org.omg.IOP.CodecPackage.TypeMismatch;
import org.omg.IOP.ServiceContext;
import org.omg.PortableInterceptor.ClientRequestInfo;
import org.omg.PortableInterceptor.ClientRequestInterceptor;

public class ClientJTSInterceptor extends LocalObject implements ClientRequestInterceptor {//$JL-SER$

  private static final Location LOCATION = Location.getLocation(ClientJTSInterceptor.class);

  public String name = null;
  private Sender sender = null;
  private Codec codec = null;

  public ClientJTSInterceptor(Codec codec) {
    this.sender = TransactionServiceImpl.getSender();
    this.codec = codec;
    this.name = "jts";
  }

  public String name() {
    return name;
  }

  public void send_request(ClientRequestInfo cri) {

    if (LOCATION.beLogged(Severity.DEBUG)) {
//      LOCATION.logT(Severity.DEBUG, "ClientJTSInterceptor.send_request({0})", new Object[]{Log.objectToString(cri)});
      SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000232", "ClientJTSInterceptor.send_request({0})", new Object[]{Log.objectToString(cri)});
    }

    if (sender != null) {
      PropagationContextHolder prContext = new PropagationContextHolder();
      sender.sending_request(cri.request_id(), prContext);

      if (prContext.value != null) {
        Any any = ORB.init().create_any();
        PropagationContextHelper.insert(any, prContext.value);
        byte[] ctxData = null;
        try {
          ctxData = codec.encode_value(any);
        } catch (InvalidTypeForEncoding e) {
          if (LOCATION.beLogged(Severity.DEBUG)) {
            LOCATION.traceThrowableT(Severity.DEBUG, "Full stacktrace: ", e);
          }
          throw new INTERNAL();
        }
        ServiceContext sc = new ServiceContext(0, ctxData);
        cri.add_request_service_context(sc, false);
      }
    }
  }

  public void destroy() {

  }

  public void send_poll(ClientRequestInfo cri) {

  }

  public void receive_reply(ClientRequestInfo cri) {

    if (LOCATION.beLogged(Severity.DEBUG)) {
//      LOCATION.logT(Severity.DEBUG, "ClientJTSInterceptor.receive_reply({0})", new Object[]{Log.objectToString(cri)});
      SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000233", "ClientJTSInterceptor.receive_reply({0})", new Object[]{Log.objectToString(cri)});
    }

    ServiceContext sc = null;
    try {
      sc = cri.get_reply_service_context(0);
    } catch (BAD_PARAM ex) {
    	//$JL-EXC$
    }

    if (sender != null && sc != null) {
      Any any = null;
      try {
        TypeCode typeCode = PropagationContextHelper.type();
        any = codec.decode_value(sc.context_data, typeCode);
      } catch (TypeMismatch e) {
        if (LOCATION.beLogged(Severity.ERROR)) {
//          LOCATION.traceThrowableT(Severity.ERROR, "ClientJTSInterceptor.receive_reply", e);
          SimpleLogger.traceThrowable(Severity.ERROR,LOCATION,e, "ASJ.trans.000040", "ClientJTSInterceptor.receive_reply");
        }
        throw new INTERNAL(0, CompletionStatus.COMPLETED_YES);
      } catch (FormatMismatch e) {
        if (LOCATION.beLogged(Severity.ERROR)) {
//          LOCATION.traceThrowableT(Severity.ERROR, "ClientJTSInterceptor.receive_reply", e);
          SimpleLogger.traceThrowable(Severity.ERROR,LOCATION,e, "ASJ.trans.000041", "ClientJTSInterceptor.receive_reply");
        }
        throw new INTERNAL(0, CompletionStatus.COMPLETED_YES);
      }
      PropagationContext prContext = PropagationContextHelper.extract(any);
      try {
        sender.received_reply(cri.request_id(), prContext, new EnvironmentImpl());
      } catch (org.omg.CORBA.WrongTransaction ex) {
        if (LOCATION.beLogged(Severity.ERROR)) {
//          LOCATION.traceThrowableT(Severity.ERROR, "ClientJTSInterceptor.receive_reply", ex);
          SimpleLogger.traceThrowable(Severity.ERROR,LOCATION,ex, "ASJ.trans.000042", "ClientJTSInterceptor.receive_reply");
        }
        throw new INVALID_TRANSACTION(0, CompletionStatus.COMPLETED_YES);
      }
    }
  }

  public void receive_exception(ClientRequestInfo cri) {

    if (LOCATION.beLogged(Severity.DEBUG)) {
//      LOCATION.logT(Severity.DEBUG, "ClientJTSInterceptor.receive_exception({0})", new Object[]{Log.objectToString(cri)});
      SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000234", "ClientJTSInterceptor.receive_exception({0})", new Object[]{Log.objectToString(cri)});
    }

    ServiceContext sc = null;
    try {
      sc = cri.get_reply_service_context(0);
    } catch (BAD_PARAM ex) {
    	//$JL-EXC$
    }

    if (sender != null && sc != null) {
      Any any = null;
      try {
        TypeCode typeCode = PropagationContextHelper.type();
        any = codec.decode_value(sc.context_data, typeCode);
      } catch (TypeMismatch e) {
        if (LOCATION.beLogged(Severity.ERROR)) {
//          LOCATION.traceThrowableT(Severity.ERROR, "ClientJTSInterceptor.receive_exception", e);
          SimpleLogger.traceThrowable(Severity.ERROR,LOCATION,e, "ASJ.trans.000044", "ClientJTSInterceptor.receive_exception");
        }
        throw new INTERNAL(0, CompletionStatus.COMPLETED_YES);
      } catch (FormatMismatch e) {
        if (LOCATION.beLogged(Severity.ERROR)) {
          LOCATION.traceThrowableT(Severity.ERROR, "ClientJTSInterceptor.receive_exception", e);
          SimpleLogger.traceThrowable(Severity.ERROR,LOCATION,e, "ASJ.trans.000045", "ClientJTSInterceptor.receive_exception");
        }
        throw new INTERNAL(0, CompletionStatus.COMPLETED_YES);
      }
      PropagationContext prContext = PropagationContextHelper.extract(any);
      Environment env = new EnvironmentImpl();
      String repId = cri.received_exception_id();
      SystemException exception = null;
      CompletionStatus completionStatus = null;

      // check if the exception is a system or user exception
      // the default assumption is an user exception
      if (repId.startsWith("IDL:omg.org/CORBA/")) {
        exception = new SystemException("", 0, CompletionStatus.COMPLETED_MAYBE){};
        Any any_ex = cri.received_exception();
        org.omg.CORBA.portable.InputStream strm = any_ex.create_input_stream();
        strm.read_string(); // read and discard repId
        int minorCode = strm.read_long();
        completionStatus = CompletionStatus.from_int(strm.read_long());

        if (repId.equals("IDL:omg.org/CORBA/UNKNOWN:1.0") && minorCode == 1) {
          exception = null;
        }
      }

      if (exception == null) {
        // if user exception, CompletionStatus is always YES.
        completionStatus = CompletionStatus.COMPLETED_YES;
      }

      env.exception(exception);
      try {
        sender.received_reply(cri.request_id(), prContext, env);
      } catch (org.omg.CORBA.WrongTransaction ex) {
        if (LOCATION.beLogged(Severity.ERROR)) {
//          LOCATION.traceThrowableT(Severity.ERROR, "ClientJTSInterceptor.receive_exception", ex);
          SimpleLogger.traceThrowable(Severity.ERROR,LOCATION,ex, "ASJ.trans.000046", "ClientJTSInterceptor.receive_exception");
        }
        throw new INVALID_TRANSACTION(0, CompletionStatus.COMPLETED_YES);
      }
    }
  }

  public void receive_other(ClientRequestInfo cri) {

  }

}


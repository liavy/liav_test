package com.sap.engine.services.ts.jts.ots.PortableInterceptor;

import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.INTERNAL;
import org.omg.IOP.Codec;
import org.omg.IOP.CodecFactory;
import org.omg.IOP.CodecFactoryPackage.UnknownEncoding;
import org.omg.IOP.ENCODING_CDR_ENCAPS;
import org.omg.IOP.Encoding;
import org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName;
import org.omg.PortableInterceptor.ORBInitializer;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;
import com.sap.tc.logging.SimpleLogger;
import com.sap.engine.services.ts.Log;

public class JTSInitializer extends org.omg.CORBA.LocalObject implements ORBInitializer {

  static final long serialVersionUID = 6083023213963149606L;
  private static final Location LOCATION = Location.getLocation(JTSInitializer.class);

  public JTSInitializer() {

  }

  /**
   * This method is called during ORB initialization.
   * @param info object that provides initialization attributes
   *        and operations by which interceptors are registered.
   */
  public void pre_init(org.omg.PortableInterceptor.ORBInitInfo info) {

  }

  /**
   * This method is called during ORB initialization.
   * @param info object that provides initialization attributes
   *        and operations by which interceptors are registered.
   */
  public void post_init(org.omg.PortableInterceptor.ORBInitInfo info) {

    if (LOCATION.beLogged(Severity.DEBUG)) {
//      LOCATION.logT(Severity.DEBUG, "JTSInitializer.post_init({0})", new Object[]{Log.objectToString(info)});
      SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000235", "JTSInitializer.post_init({0})", new Object[]{Log.objectToString(info)});
    }

    CodecFactory codecFactory = info.codec_factory();
    Encoding enc = new Encoding(ENCODING_CDR_ENCAPS.value, (byte)1, (byte)2);
    Codec codec = null;
    try {
      codec = codecFactory.create_codec(enc);
    } catch (UnknownEncoding e) {
      if (LOCATION.beLogged(Severity.ERROR)) {
//        LOCATION.traceThrowableT(Severity.ERROR, "JTSInitializer.post_init", e);
        SimpleLogger.traceThrowable(Severity.ERROR,LOCATION,e, "ASJ.trans.000047", "JTSInitializer.post_init");
      }
      throw new INTERNAL(0, CompletionStatus.COMPLETED_NO);
    }
    // register the interceptors.
    try {
      info.add_client_request_interceptor(new ClientJTSInterceptor(codec));
      info.add_server_request_interceptor(new ServerJTSInterceptor(codec));
    } catch (DuplicateName e) {
      if (LOCATION.beLogged(Severity.ERROR)) {
//        LOCATION.traceThrowableT(Severity.ERROR, "JTSInitializer.post_init", e);
        SimpleLogger.traceThrowable(Severity.ERROR,LOCATION,e, "ASJ.trans.000048", "JTSInitializer.post_init");
      }

      throw new INTERNAL(0, CompletionStatus.COMPLETED_NO);
    }
  }

}


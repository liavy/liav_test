package com.sap.engine.services.ssl.exception;

import com.sap.localization.ResourceAccessor;
import com.sap.localization.LocalizableTextFormatter;
import com.sap.localization.LocalizationException;
import com.sap.localization.LocalizableText;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

import java.util.Locale;


/**
 *
 *
 *
 *
 * @author Ilia Kacarov
 * @author Ekaterina Zheleva
 */
public class SSLResourceAccessor extends ResourceAccessor {
  public static Category category = null;
  public static Location location = null;

  private static final String BUNDLE_NAME = "com.sap.engine.services.ssl.exception.SSLResourceBundle";
  private final static String SUFIX_DESCRIPTION = "_descr";
  private final static String POSTFIX_CONSEQUENCES = "_cnsq";
  private final static String POSTFIX_COUNTERMEASURES = "_cntrmsr";

  /**
   * all SSL log recores will have this structure: <br>
   *   Source: {0}\r\nDescription: {1}\r\nConsequences: {2}\r\nCountermeasures:{3}
   */
  private static final String GENERAL_LOG_TEMPLATE="ssl_general_log";

  private static ResourceAccessor resourceAccessor = null;
  private static Locale currentLocale = Locale.getDefault();


  private SSLResourceAccessor() {
    super(BUNDLE_NAME);
    category = Category.getCategory("/System/Security/SSL");
    location = Location.getLocation("com.sap.engine.services.ssl");
  }

  public static synchronized ResourceAccessor getResourceAccessor() {
    if (resourceAccessor == null) {
      resourceAccessor = new SSLResourceAccessor();
    }
    return resourceAccessor;
  }


  ///////////////////   helper methods
  private static final String getDescriptionKey(String key) {
    return key + SUFIX_DESCRIPTION;
  }
  private static final String getConsequencesKey(String key) {
    return key + POSTFIX_CONSEQUENCES;
  }
  private static final String getCountermeasuresKey(String key) {
    return key + POSTFIX_COUNTERMEASURES;
  }

  public static String getMessage(String key) {
    String result = key;
    try {
      result = (new LocalizableTextFormatter(resourceAccessor, key, null)).format();
    } catch (LocalizationException e) {
      result = resourceAccessor.getMessageText(currentLocale, key);
    }
    return result;
  }

  public static LocalizableText getLocalizableMessage(String key, Object[] params) {
    return (new LocalizableTextFormatter(resourceAccessor, key, params));
  }

  public static String getMessage(String key, Object[] params) {
    String result = key;
    try {
      result = (new LocalizableTextFormatter(resourceAccessor, key, params)).format();
    } catch (LocalizationException e) {
      result = resourceAccessor.getMessageText(currentLocale, key);
    }
    return result;
  }

  public static String getDescription(String key, Object[] params) {
    key = getDescriptionKey(key);
    String result = key;
    try {
      result = (new LocalizableTextFormatter(resourceAccessor, key, params)).format();
    } catch (LocalizationException e) {
      result = resourceAccessor.getMessageText(currentLocale, key);
    }
    return result;
  }

  public static String getConsequences(String key, Object[] params) {
    key = getConsequencesKey(key);
    String result = key;
    try {
      result = (new LocalizableTextFormatter(resourceAccessor, key, params)).format();
    } catch (LocalizationException e) {
      result = resourceAccessor.getMessageText(currentLocale, key);
    }
    return result;
  }

  public static String getCountermeasures(String key, Object[] params) {
    key = getCountermeasuresKey(key);
    String result = key;
    try {
      result = (new LocalizableTextFormatter(resourceAccessor, key, params)).format();
    } catch (LocalizationException e) {
      result = resourceAccessor.getMessageText(currentLocale, key);
    }
    return result;
  }


  //////////////////////////////////////// logs

  /**
     * Shortcut for log(severity, source, description, consequences, countermeasures).<br>
     *
     * It is usefull, when the messages 'description', 'consequences' and 'countermeasures'
     * don't have parameters.
     * <p/>
     * description     := getDescription(messageId, null) <br>
     * consequences    := getConsequences(messageId, null) <br>
     * countermeasures := getCountermeasures(messageId, null) <br>
     * Properties with the above keys must exist in the used resource bundle properties file.<br>
     * <BR>
     * @param severity  Accepted values: {Severity.INFO, .., Severity.FATAL}. All other will be ignored.
     * @param source    Information about the location or source(An answer to the question "Where did it happen?")
     * @param messageId the key to the respective message bodies in .properties file. The additional keys for
     *                  description, consequences and countermeasures must exist.
     */
    public static void log(int severity, Object source, String messageId) {
      log(severity, source, getDescription(messageId, null), getConsequences(messageId, null), getCountermeasures(messageId, null));
    }

    /**
     * Shortcut for log(severity, source, description, consequences, countermeasures).<br>
     *<br>
     * It is usefull, when the messages 'description', 'consequences' and 'countermeasuresare'
     * do have parameters.
     * <p/>
     * description     := getDescription(messageId, params)<br>
     * consequences    := getConsequences(messageId, params)<br>
     * countermeasures := getCountermeasures(messageId, params)<br>
     * Properties with the above keys must exist in the used resource bundle properties file.<br>
     *<br>
     * @param severity  Accepted values: {Severity.INFO, .., Severity.FATAL}. All other will be ignored.
     * @param source    Information about the location or source(An answer to the question "Where did it happen?")
     * @param messageId the key to the respective message bodies in .properties file. The additional keys for
     *                  description, consequences and countermeasures must exist!!!!
     * @param params    TextFormatter parameters for the actual description, consequences and countermeasures message
     *                  bodies. May be null, if there is no need for such parameters.
     */
    public static void log(int severity, Object source, String messageId, Object[] params) {
      log(severity, source, getDescription(messageId, params), getConsequences(messageId, params), getCountermeasures(messageId, params));
    }

    /**
     *
     * @param severity        Accepted values: {Severity.INFO, .., Severity.FATAL}. All other will be ignored.
     *
     * @param source          Information about the location or source(An answer to the question "Where did it happen?")
     *
     * @param description     Short description of the error, event or status info
     *                        (A brief answer to the question "What has happened?" respectively "What status is reached?",
     *                        including a brief in-formation about the object directly involved
     *                        [e.g. a document number or a property key etc.], if appropriate).
     *
     * @param consequences    Information about the consequences (in case of error messages and partly also in case of event
     *                        notifications [warnings]).This is necessary, if there are future impacts on hardware or software components.
     *
     * @param countermeasures Information about possible countermeasures (in case of error messages and event
     *                        notifications [warnings]) to solve the problems reason. This normally includes also a description,
     *                        of who (which addressee/role) should perform countermeasures. If solving the problem is complicated and
     *                        requires a detailed description, the referencing of external information should be considered.
     */
    public static void log(int severity, Object source, String description, String consequences, String countermeasures) {
      if (location == null || category == null) {
        return;
      }
      switch (severity) {
        case Severity.INFO: {
          category.infoT(location,
                         getMessage(GENERAL_LOG_TEMPLATE, new Object[]{source,description, consequences,countermeasures}),
                         (Object[]) null);
          break;
        }
        case Severity.WARNING: {
          category.warningT(location,
                            getMessage(GENERAL_LOG_TEMPLATE, new Object[]{source,description, consequences,countermeasures}),
                            (Object[]) null);
          break;
        }
        case Severity.ERROR: {
          category.errorT(location,
                          getMessage(GENERAL_LOG_TEMPLATE, new Object[]{source,description, consequences,countermeasures}),
                          (Object[]) null);
          break;
        }
        case Severity.FATAL: {
          category.fatalT(location,
                          getMessage(GENERAL_LOG_TEMPLATE, new Object[]{source,description, consequences,countermeasures}),
                          (Object[]) null);
          break;
        }
        default: {
        }
      }
    }


    ////////////////////////////////////////////////////////////// traces


    /**
     *
     *
     *
     * @param severity  Accepted values: {Severity.DEBUG, Severity.PATH, Severity.INFO,
     *                                    Severity.WARNING, Severity.ERROR, Severity.FATAL}
     * @param msg  The actual message to be traced - no parameters allowed!
     */
    public static void trace(int severity, String msg) {
      if (location == null) {
        return;
      }
      switch (severity) {
        case Severity.PATH: {
          //$JL-SEVERITY_TEST$
          location.pathT(msg);
          break;
        }
        case Severity.DEBUG: {
          //$JL-SEVERITY_TEST$
          location.debugT(msg);
          break;
        }
        case Severity.INFO: {
          //$JL-SEVERITY_TEST$
          location.infoT(msg);
          break;
        }
        case Severity.WARNING: {
          //$JL-SEVERITY_TEST$
          location.warningT(msg);
          break;
        }
        case Severity.ERROR: {
          //$JL-SEVERITY_TEST$
          location.errorT(msg);
          break;
        }
        case Severity.FATAL: {
          //$JL-SEVERITY_TEST$
          location.fatalT(msg);
          break;
        }
        default: {
        }
      }
    }

    /**
     *  This is shortcut to trace(severity, getMessage(key, parameters)); <br>
     *
     *
     * @param severity   accepted values: {Severity.DEBUG, Severity.PATH, Severity.INFO,
     *                                    Severity.WARNING, Severity.ERROR, Severity.FATAL}
     * @param key        key to the actual message bodie from resource bundle file
     * @param parameters parameters for actual message body. May be null, if there is no need
     *                   for such parameters
     */
    public static void trace(int severity, String key, Object[] parameters) {
      trace(severity, getMessage(key, parameters));
    }

    /**
     *
     *
     * @param severity Accepted values: {Severity.DEBUG, Severity.PATH, Severity.INFO,
     *                                   Severity.WARNING, Severity.ERROR, Severity.FATAL}
     * @param msg      Free text message - no formatting symbols
     * @param exc      traced Throwable object - must not be null
     */
    public static void traceThrowable(int severity, String msg, Throwable exc) {
      if (location == null) {
        return;
      }
      if (exc != null) {
        location.traceThrowableT(severity, msg, exc);
      } else {
        trace(severity, msg);
      }
    }


    /**
     *
     *
     * @param severity Accepted values: {Severity.DEBUG, Severity.PATH, Severity.INFO,
     *                                   Severity.WARNING, Severity.ERROR, Severity.FATAL}
     * @param key      key to the formatted message from the resource bundle, not the
     *                 plain text!
     * @param params   paramapeters to the formatted message. May be null, if the message does not
     *                 need such params.
     * @param exc      traced Throwable object - must not be null
     */
    public static void traceThrowable(int severity, String key, Object[] params, Throwable exc) {
      if (location == null) {
        return;
      }
      if (exc != null) {
        location.traceThrowableT(severity, getMessage(key, params), exc);
      } else {
        trace(severity, key, params);
      }
    }

}

package com.sap.transaction;

import com.sap.exception.BaseExceptionInfo;
import com.sap.exception.IBaseException;
import com.sap.localization.LocalizableText;
import com.sap.localization.LocalizableTextFormatter;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;
import java.util.TimeZone;

/**
 * A <code>TxException</code> is thrown to indicate that the transaction
 * manager has encountered an unexpected error situation. Most likely, an
 * exception of this type is raised when a call to the underlying JTA
 * transaction manager has failed. In this case, the caught JTA exception is
 * packed as "cause" attribute into the <code>TxException</code>.
 */
public class TxException extends Exception {


    public TxException(){
    }
    
    public TxException(String msg) {
        super(msg);
    }
    
}

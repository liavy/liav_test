/*
 * Copyright (c) 2002 by SAP Labs Bulgaria AG.,
 * url: http://www.saplabs.bg
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Sofia AG.
 */
package com.sap.engine.services.servlets_jsp.jspparser_api.exception;

import java.io.File;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;
import java.util.TimeZone;

import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.Position;
import com.sap.exception.BaseExceptionInfo;
import com.sap.exception.IBaseException;
import com.sap.localization.LocalizableText;
import com.sap.localization.LocalizableTextFormatter;
import com.sap.localization.LocalizationException;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;

/**
 * Exception for parsing errors
 * @author Violeta Uzunova
 * @version 6.30
 */
public class JspParseException extends Exception implements IBaseException {
  private BaseExceptionInfo exceptionInfo = null;

  private static final String ERROR_IN = "jsp_parser_0001";
  public static final String EXPECTING_INSTEAD_OF = "jsp_parser_0002";
  public static final String EXPECTING_JSP_TAG_WHILE_READ_END_OF_FILE = "jsp_parser_0003";
  public static final String EXPECTING_CHAR_WHILE_READ_END_OF_FILE = "jsp_parser_0004";
  public static final String IMPLICIT_PARSING_ERROR = "jsp_parser_0005";
  public static final String EXPECTING_CHAR_OR_CHAR_WHILE_READ_END_OF_FILE = "jsp_parser_0006";
  public static final String EXPECTING_CHAR_OR_CHAR = "jsp_parser_0007";
  public static final String ELEMENT_DOES_NOT_TAKE_ANY_ATTRIBUTES = "jsp_parser_0008";
  public static final String IMPLICIT_PARSING_ERROR_PARSING_ELEMENT_IS_NOT_VALID_FOR_TOKEN = "jsp_parser_0009";
  public static final String UNRECOGNIZED_DIRECTIVE_NAME = "jsp_parser_0010";
  public static final String RETHROW_EXCEPTION = "jsp_parser_0011";
  public static final String ELEMENT_HAS_NONE_EMPTY_BODY = "jsp_parser_0012";
  public static final String ACTION_DOES_NOT_HAVE_CORRECT_BODY = "jsp_parser_0013";
  public static final String ELEMENT_DOES_NOT_HAVE_CORRECT_BODY = "jsp_parser_0014";
  public static final String ACTION_MUST_HAVE_AT_LEAST_TWO_SPECIFIED_ATTRIBUTES = "jsp_parser_0015";
  public static final String JSP_USEBEAN_DUPLICATED_ID_ATTRIBUTE = "jsp_parser_0016";
  public static final String JSP_USEBEAN_NEEDS_A_TYPE_ATTRIBUTE = "jsp_parser_0017";
  public static final String METHOD_MUST_RETURN_BOOLEAN = "jsp_parser_0018";
  public static final String CANNOT_RESOLVE_BEAN_CLASS = "jsp_parser_0019";
  public static final String CANNOT_FIND_FIELD_IN_BEAN_CLASS = "jsp_parser_0020";
  public static final String UNEXPECTED_ELEMENT_IN_THE_BODY_OF_ELEMENT = "jsp_parser_0021";
  public static final String DIRECTIVE_MUST_HAVE_EXCACTY_ONE_SPECIFIED_ATTRIBUTE = "jsp_parser_0022";
  public static final String MISSING_ATTRIBUTE_IN_DIRECTIVE = "jsp_parser_0023";
  public static final String ERROR_IN_LOADING_SUPERCLASS_OF_THE_SERVLET_THE_CLASS_IS = "jsp_parser_0024";
  public static final String JSP_PAGE_MUSR_BE_INSTANCE_OF_HTTPJSPPAGE_HTTPJSPPAGE_IS_NOT_ASSIGNABLE_FROM_CLASS = "jsp_parser_0025";
  public static final String INCORRECT_TO_SET_AUTOFLUSH_FALSE_AND_BUFFER_NONE = "jsp_parser_0026";
  public static final String DIRECTIVE_MUST_HAVE_AT_LEAST_ONE_SPECIFIED_ATTRIBUTE = "jsp_parser_0027";
  public static final String ATTRIBUTE_ALREADY_SPECIFED_IN_DIRECTIVE = "jsp_parser_0028";
  public static final String INVALID_VALUE_FOR_ATTRIBUTE_IN_DIRECTIVE = "jsp_parser_0029";
  public static final String UNRECOGNIZED_ATTRIBUTE_IN_DIRECTIVE = "jsp_parser_0030";
  public static final String INVALID_VALUE_FOR_ATTRIBUTE_IN_DIRECTIVE_VALUE_IS_RESERVED = "jsp_parser_0031";
  public static final String ERROR_IN_PARSING_TAGLIB_TAG_IN_WEBXML_OR_TLD_FILE_OF_THE_TAGLIB_LIBRARY = "jsp_parser_0032";
  public static final String REACHED_END_OF_FILE_BEFORE_END_OF_TAG = "jsp_parser_0033";
  public static final String IO_ERROR_IN_FILE = "jsp_parser_0034";
  public static final String IO_FILE_ERROR_WHILE_TRYING_TO_READ_FILE = "jsp_parser_0035";
  public static final String UNSUPPORTED_ENCODING = "jsp_parser_0036";
  public static final String JSP_TAG_IS_NOT_CLOSED = "jsp_parser_0037";
  public static final String UNRECOGNIZED_TAG_LIBRARY_PREFIX = "jsp_parser_0038";
  public static final String UNRECOGNIZED_TAG_NAME = "jsp_parser_0039";
  public static final String ATTRIBUTE_NOT_FOUND_IN_TAG_LIBRARY_FOR_THE_TAG = "jsp_parser_0040";
  public static final String ATTRIBUTE_IS_REQUIRED_BUT_WAS_NOT_FOUND_IN_THE_TAG_OF_THE_ACTION = "jsp_parser_0041";
  public static final String MUST_HAVE_NO_ATTRIBUTES = "jsp_parser_0042";
  public static final String ACTRION_MUST_HAVE_EXACTLY_ONE_SPECIFIED_ATTRIBUTE = "jsp_parser_0043";
  public static final String UNRECOGNIZED_ATTRIBUTE_IN_ACTION = "jsp_parser_0044";
  public static final String ACTION_MUST_HAVE_EXACTLY_TWO_SPECIFIED_ATTRIBUTE = "jsp_parser_0045";
  public static final String ATTRIBUTE_ALREADY_SPECIFED_IN_ACTION = "jsp_parser_0046";
  public static final String ACTION_MUST_HAVE_ONLY_ONE_OR_TWO_SPECIFIED_ATTRIBUTES = "jsp_parser_0047";
  public static final String INVALID_VALUE_FOR_ATTRIBUTE_IN_ACTION = "jsp_parser_0048";
  public static final String MISSING_ATTRIBUTE_IN_ACTION = "jsp_parser_0049";
  public static final String IMPLICIT_PARSING_EXCEPTION_MISSING = "jsp_parser_0050";
  public static final String ELEMENT_MUST_HAVE_EXACTLY_TWO_SPECIFIED_ATTRIBUTE = "jsp_parser_0051";
  public static final String ATTRIBUTE_ALREADY_SPECIFED_IN_ELEMENT = "jsp_parser_0052";
  public static final String UNRECOGNIZED_ATTRIBUTE_IN_ELEMENT = "jsp_parser_0053";
  public static final String ERROR_PARSING_PLUGIN = "jsp_parser_0054";
  public static final String ACTION_MUST_HAVE_AT_LEAST_THREE_SPECIFIED_ATTRIBUTES = "jsp_parser_0055";
  public static final String MISSING_ATTRIBUTE_OR_ATTRIBUTE_IN_ACTION = "jsp_parser_0056";
  public static final String IMPLICIT_PARSING_EXCEPTION_MISSING_CLOSING_TAG_FOR = "jsp_parser_0057";
  public static final String ATTRIBUTE_OR_ATTRIBUTE_SHOULD_NOT_BE_SPECIFED_IN_ACTION_WHEN_THE_VALUE_OF_ATTRIBUTE_IS_STAR = "jsp_parser_0058";
  public static final String ATTRIBUTE_AND_ATTRIBUTE_CANNOT_BE_SPECIFED_TOGETHER_IN_ACTION = "jsp_parser_0059";
  public static final String DUBLICATED_ATTRIBUTE_VALUE_IN_ACTION = "jsp_parser_0060";
  public static final String IMPLICIT_PARSING_ERROR_ACTION_NOT_VERIFIED = "jsp_parser_0061";
  public static final String ACTION_MUST_HAVE_AT_LEAST_ONE_SPECIFIED_ATTRIBUTE = "jsp_parser_0062";
  public static final String EXPECTING_IDENTIFIER_WHILE_READING_END_OF_FILE = "jsp_parser_0063";
  public static final String CANNOT_INIT_BEANINFO_FOR_THE_TAG_HANDLER = "jsp_parser_0064";
  public static final String CANNOT_INIT_TAGBEGINGENERATOR = "jsp_parser_0065";
  public static final String ATTRIBUTE_IS_MISSING = "jsp_parser_0066";
  public static final String INVALID_ATTRIBUTE = "jsp_parser_0067";
  public static final String UNABLE_TO_FIND_METHOD_FOR_ATTRIBUTE = "jsp_parser_0068";
  public static final String UNKNOWN_VALUE_OF_CHARACTER = "jsp_parser_0069";
  public static final String UNKNOWN_CLASS_NAME = "jsp_parser_0070";
  public static final String WRONG_ARRAY_PARAMETER = "jsp_parser_0071";
  public static final String CANNOT_PARSE_CUSTOM_TAG = "jsp_parser_0072";
  public static final String NOT_IMPLEMENTED = "jsp_parser_0073";
  public static final String NO_ATTRIBUTE = "jsp_parser_0074";
  public static final String CANNOT_GET_CANNONICAL_JSP_PATH = "jsp_parser_0075";
  public static final String CANNOT_INCLUDE_FILE = "jsp_parser_0076";
  public static final String WRONG_START_OR_END_INDEXES = "jsp_parser_0077";
  public static final String CANNOT_OBTAIN_PARSER = "jsp_parser_0078";
  public static final String CHAR_EXCPECTED = "jsp_parser_0079";
  public static final String UNABLE_TO_PARSE_JSP_CANNOT_READ_THE_FILE = "jsp_parser_0080";
  public static final String TAGLIBVALIDATOR_RETURN_ERRORS_FOR_TAGLIB = "jsp_parser_0081";
  public static final String DIFFERENT_ENCODINGS_PROPERTY_BODY = "jsp_parser_0083";
  public static final String DIFFERENT_ENCODINGS_PROPERTY_XML = "jsp_parser_0084";
  public static final String DIFFERENT_ENCODINGS_XML_BODY = "jsp_parser_0085";
  public static final String ERROR_IN_PARSING_TAGLIB_TAG_IN_JSP_CANNOT_RESOLVE_URI = "jsp_parser_0086";
  public static final String ERROR_IN_PARSING_JSP_FILE = "jsp_parser_0087";
  public static final String ERROR_NON_NULL_TEI_AND_VAR_SUBELEMENTS = "jsp_parser_0088";
  public static final String ERROR_TEI_VALIDATION_ERROR = "jsp_parser_0089";
  public static final String NEITHER_URI_NOR_TAGDIR_PRESENT = "jsp_parser_0090";
  public static final String ERROR_IN_PARSING_TAGLIB_TAG_IN_JSP_CANNOT_RESOLVE_TAGDIR = "jsp_parser_0091";
  public static final String MISSING_ATTRIBUTES_IN_DIRECTIVE = "jsp_parser_0092";
  public static final String ATTRIBUTE_CANNOT_BE_SPECIFIED_WHEN_OTHER_ATTRIBUTE_IS_ALSO_SPECIFIED = "jsp_parser_0093";
  public static final String ATTRIBUTE_DUPLICATE_NAME = "jsp_parser_0094";
  public static final String VARIABLE_DUPLICATE_NAME = "jsp_parser_0095";
  public static final String VARIABLE_NAME_CONFLICTS_WITH_ATTRIBUTE_NAME = "jsp_parser_0096";
  public static final String VARIABLE_ALIAS_CONFLICTS_WITH_ATTRIBUTE_NAME = "jsp_parser_0097";
  public static final String VARIABLE_DUPLICATE_NAMEFROMATTRIBUTE = "jsp_parser_0098";
  public static final String VARIABLE_DUPLICATE_ALIAS = "jsp_parser_0099";
  public static final String ATTRIBUTE_WITH_NAMEFROMATTRIBUTE_NOT_DEFINED = "jsp_parser_0100";
  public static final String VARIABLE_NAME_CONFLICTS_WITH_DYNAMIC_ATTRIBUTES = "jsp_parser_0101";
  public static final String ATTRIBUTE_NAME_CONFLICTS_WITH_DYNAMIC_ATTRIBUTES = "jsp_parser_0102";
  public static final String ATTRIBUTE_NAME_CONFLICTS_WITH_VARIABLE_NAME = "jsp_parser_0103";
  public static final String ATTRIBUTE_CANNOT_BE_SPECIFIED_WHEN_OTHER_ATTRIBUTE_IS_TRUE = "jsp_parser_0104";
  public static final String ERROR_IN_PARSING_TAGLIB_TAG_IN_JSP_CANNOT_RESOLVE_URI_JSP11 = "jsp_parser_0105";
  public static final String TAGDIR_MUST_START_WITH_WEBINF_TAGS = "jsp_parser_0106";
  public static final String JSP_INVOKE_CANNOT_CONTAIN = "jsp_parser_0107";
  public static final String JSP_INVOKE_SHOULD_CONTAIN = "jsp_parser_0108";
  public static final String JSP_ACTION_CANNOT_BE_USED_IN_JSP = "jsp_parser_0109";
	public static final String PARSER_CANNOT_GENERATE_OUTPUT_WITH_ENCODING = "jsp_parser_0110";
  public static final String SCRIPTING_ELEMENTS_ARE_NOT_ALLOWED_HERE = "jsp_parser_0111";
	public static final String CREATING_NEW_ELEMENT_FAILED = "jsp_parser_0112";
  public static final String CANNOT_BE_USED_IN_JSP_SYNTAX = "jsp_parser_0113";
	public static final String NEITHER_SCRIPTING_NOR_SUBELEMENTS_ARE_ALOWED = "jsp_parser_0114";
  public static final String SCRIPTING_ELEMENTS_ARE_NOT_ALLOWED_IN_JSPATTRIBUTE = "jsp_parser_0115";
  public static final String MUST_BE_SUBELEMENT_OF_STANDARD_OR_CUSTOM_ACTION = "jsp_parser_0116";
  public static final String TAG_REQUIRE_EMPTY_BODY = "jsp_parser_0117";
	public static final String TLD_NOT_FOUND = "jsp_parser_0118";
	public static final String USE_BEAN_CLASS_IS_NOT_ASSIGNABLE_TO_TYPE = "jsp_parser_0119";
	public static final String ATTRIBUTE_CAN_ACCEPT_ONLY_STATIC_VALUES = "jsp_parser_0120";
	public static final String ELEMENT_CONTAINS_JSP_ATTRIBUTE_WITHOUT_JSP_BODY = "jsp_parser_0121";
	public static final String ERROR_PARSING_TAGFILE = "jsp_parser_0122";
	public static final String CANNOT_GENERATE_ECLIPSE_DEBUG = "jsp_parser_0123";
	public static final String CANNOT_CLOSE_FILE = "jsp_parser_0124";
	public static final String ATTRIBUTE_VALUES_MUST_BE_QUOTED= "jsp_parser_0125";
	public static final String PREFIX_ALREADY_DEFINED= "jsp_parser_0126";
	public static final String DIRECTIVE_SHOULD_HAVE_AT_LEAST_ATTRIBUTES = "jsp_parser_0127";
	public static final String WRONG_ATTRIBUTE_FOR_VARIABLE_FROM_ATTRIBUTE = "jsp_parser_0128";
  public static final String PAGEENCODING_CANNOT_BE_USED_IN_TAG_FILES_XWM_SYNTAX = "jsp_parser_0129";
  public static final String ACTION_CAN_BE_USED_ONLY_PLUGIN_ACTION = "jsp_parser_0130";
  public static final String JSP_ROOT_CAN_ONLY_APPEAR_AS_ROOT_ELEMENT = "jsp_parser_0131";
  public static final String TAG_LIBRARY_NOT_FOUND = "jsp_parser_0132";
  public static final String UNKNOWN_ELEMENT_TYPE = "jsp_parser_0133";
  public static final String DIRECTIVE_SHOULD_HAVE_EMPTY_BODY = "jsp_parser_0134";
  public static final String PREFIX_ALREADY_USED = "jsp_parser_0135";
  public static final String PREFIX_RESERVED = "jsp_parser_0136";
  public static final String ELEMENT_MUST_BE_USED_ONLY_IN = "jsp_parser_0137";
  public static final String ELEMENT_NOT_FOUND = "jsp_parser_0138";
  public static final String ELEMENT_CANNOT_BE_USED_IN_TAG_FILES = "jsp_parser_0139";
  public static final String SIMPLE_TAG_CANNOT_HAVE_JSP_BODY_CONTENT = "jsp_parser_0140";
  public static final String INVALID_TAG_FILE_PATH = "jsp_parser_0141";
  public static final String TAG_FILE_NOT_FOUND = "jsp_parser_0142";
  public static final String TAG_FILE_HAS_DUPLICATE_NAME = "jsp_parser_0143";
  public static final String DEFERRED_SYNTAX_CANNOT_BE_USED_IN_TEMPLATE_TEXT = "jsp_parser_0145";
  public static final String JSP_DOCUMENT_CANNOT_REDEFINE_PREFIX = "jsp_parser_0146";
  public static final String INVALID_VERSION_FOR_IMPLICIT_TLD = "jsp_parser_0147";
  public static final String ELEMENTS_NOT_ALLOWED_FOR_IMPLICIT_TLD = "jsp_parser_0148";
  public static final String DEFERRED_NOT_ALLOWED_FOR_TAG_ATTRIBUTE = "jsp_parser_0149";
  public static final String DUPLICATED_JSP_OUTPUT_WITH_DIFFERENT_ATTRIBUTE_VALUE = "jsp_parser_0150";
  public static final String DIFFERENT_ENCODINGS_PROPERTY_BOM = "jsp_parser_0151";
  public static final String DIFFERENT_ENCODINGS_ATTRIBUTE_BOM = "jsp_parser_0152";
  public static final String ILLEGAL_METHOD_SIGNATURE = "jsp_parser_0153";
  public static final String ILLEGAL_VALUE_FOR_METHOD_SIGNATURE = "jsp_parser_0154";
  public static final String ATTRIBUTES_IN_TAGFILE_CANNOT_BE_USED_FOR_JSP_VERSION = "jsp_parser_0155";
  public static final String COMPILATION_LIBRARY_NOT_FOUND = "jsp_parser_0156";
  public static final String FUNCTION_NOT_FOUND = "jsp_parser_0157";
  public static final String EL_HAS_NO_CLOSE_BRACKET = "jsp_parser_0158";
  public static final String ATTRIBUTE_CANNOT_ACCEPT_RUNTIME_EXPRESSIONS = "jsp_parser_0159";
  public static final String ERROR_PARSING_IMPLICIT_TLD = "jsp_parser_0160";

  private String fileName = null;
  private Position pos = null;

  private String messageNumber = null;

  /**
   * Constructs a new JspParseException exception with given message.
   * @param msg text for the exception
   */
  public JspParseException(String msg) {
    super();
    messageNumber = msg;
    LocalizableTextFormatter formater = new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(), msg);
    exceptionInfo = new BaseExceptionInfo(WebResourceAccessor.location, formater, this, null);
  }

  /**
   * Constructs a new JspParseException exception with given message and parameters.
   * @param msg text for the exception
   * @param parameters array with parametters correspondign to bundle
   */
  public JspParseException(String msg, Object [] parameters) {
    super();
    messageNumber = msg;
    LocalizableTextFormatter formater = new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(), msg, parameters);
    exceptionInfo = new BaseExceptionInfo( WebResourceAccessor.location, formater, this, null);
  }

  /**
   * Constructs a new JspParseException exception with given message and causing exception.
   * @param msg text for the exception
   * @param linkedException the real couse of this parsing exception
   */
  public JspParseException(String msg, Throwable linkedException) {
    super();
    messageNumber = msg;
    LocalizableTextFormatter formater = new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(), msg);
    exceptionInfo = new BaseExceptionInfo(WebResourceAccessor.location, formater, this, linkedException);
  }

  /**
   * Constructs a new JspParseException exception with given message, parameters and causing exception.
   * @param msg text for the exception
   * @param parameters array with parametters corresponding to bundle
   * @param linkedException the real couse of this parsing exception
   */
  public JspParseException(String msg, Object [] parameters, Throwable linkedException) {
    super();
    messageNumber = msg;
    LocalizableTextFormatter formater = new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(), msg, parameters);
    exceptionInfo = new BaseExceptionInfo(WebResourceAccessor.location, formater, this, linkedException);
  }

  /**
   *Constructs a new ParseException exception.
   *
   * @param   msg  message of the exception
   * @param   pos  the position where the error occured
   */
  public JspParseException(String msg, String fileName, Position pos) {
    this(msg);
    this.pos = pos;
    this.fileName = fileName;
  }

  /**
   *Constructs a new ParseException exception.
   *
   * @param   msg  message of the exception
   * @param   parameters  parameters of exception message
   * @param   pos  the position where the error occured
   */
  public JspParseException(String msg, Object [] parameters, String fileName, Position pos) {
    this(msg, parameters);
    this.pos = pos;
    this.fileName = fileName;
  }

  /**
   *Constructs a new ParseException exception.
   *
   * @param   msg  message of the exception
   * @param   parameters  parameters of exception message
   * @param   pos  the position where the error occured
   * @param   linkedException  root cause of this exception
   */
  public JspParseException(String msg, Object [] parameters, String fileName, Position pos, Throwable linkedException) {
    this(msg, parameters, linkedException);
    this.pos = pos;
    this.fileName = fileName;
  }

  public String getMessage() {
    return getLocalizedMessage();
  }

  public Throwable initCause(Throwable cause) {
    return exceptionInfo.initCause(cause);
  }

  public Throwable getCause() {
    return exceptionInfo.getCause();
  }

  public LocalizableText getLocalizableMessage() {
    return exceptionInfo.getLocalizableMessage();
  }

  public String getLocalizedMessage() {
    if (fileName == null || pos == null) {
      return exceptionInfo.getLocalizedMessage();
    } else {
      return exceptionInfo.getLocalizedMessage() + "\r\n" + formatPosition();
    }
  }

  public String getLocalizedMessage(Locale loc) {
    if (fileName == null || pos == null) {
      return exceptionInfo.getLocalizedMessage(loc);
    } else {
      return exceptionInfo.getLocalizedMessage(loc) + "\r\n" + formatPosition();
    }
  }

  public String getLocalizedMessage(TimeZone timeZone) {
    if (fileName == null || pos == null) {
      return exceptionInfo.getLocalizedMessage(timeZone);
    } else {
      return exceptionInfo.getLocalizedMessage(timeZone) + "\r\n" + formatPosition();
    }
  }

  public String getLocalizedMessage(Locale loc, TimeZone timeZone) {
    if (fileName == null || pos == null) {
      return exceptionInfo.getLocalizedMessage(loc, timeZone);
    } else {
      return exceptionInfo.getLocalizedMessage(loc, timeZone) + "\r\n" + formatPosition();
    }
  }

  public String getNestedLocalizedMessage() {
    return exceptionInfo.getNestedLocalizedMessage();
  }

  public String getNestedLocalizedMessage(Locale loc) {
    return exceptionInfo.getNestedLocalizedMessage(loc);
  }

  public String getNestedLocalizedMessage(TimeZone timeZone) {
    return exceptionInfo.getNestedLocalizedMessage(timeZone);
  }

  public String getNestedLocalizedMessage(Locale loc, TimeZone timeZone) {
    return exceptionInfo.getNestedLocalizedMessage(loc, timeZone);
  }

  public void finallyLocalize() {
    exceptionInfo.finallyLocalize();
  }

  public void finallyLocalize(Locale loc) {
    exceptionInfo.finallyLocalize(loc);
  }

  public void finallyLocalize(TimeZone timeZone) {
    exceptionInfo.finallyLocalize(timeZone);
  }

  public void finallyLocalize(Locale loc, TimeZone timeZone) {
    exceptionInfo.finallyLocalize(loc, timeZone);
  }

  public String getSystemStackTraceString() {
    StringWriter s = new StringWriter();
    super.printStackTrace(new PrintWriter(s));
    return s.toString();
  }

  public String getStackTraceString() {
    return exceptionInfo.getStackTraceString();
  }

  public String getNestedStackTraceString() {
    return exceptionInfo.getNestedStackTraceString();
  }

  public void printStackTrace() {
    exceptionInfo.printStackTrace();
  }

  public void printStackTrace(PrintStream s) {
    exceptionInfo.printStackTrace(s);
  }

  public void printStackTrace(PrintWriter s) {
    exceptionInfo.printStackTrace(s);
  }

  /**
	 * Setter method for logging information.
	 *
	 * @param cat logging category
	 * @param severity logging severity
	 * @param loc logging location
	 * @deprecated
	 */
  public void setLogSettings(Category cat, int severity, Location loc) {
    //exceptionInfo.setLogSettings(cat, severity, loc);
  }

  /**
	 * Logs the exception message.
	 * @deprecated
	 */
	public void log() {
		//exceptionInfo.log();
	}

  /**
   * Creates info string from position where the error has occured and localized it
   *
   * @return  formated info
   */
  private String formatPosition() {
    if (fileName == null || pos == null) {
      return "";
    }
    try {
      fileName = fileName.replace('\\', File.separatorChar);
      Object[] params = new Object[]{fileName, pos.getLine() + "",  pos.getLinePos() + ""};
      LocalizableTextFormatter formater = new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(), ERROR_IN, params);
      return formater.format();
    } catch (LocalizationException e) {
      return "";
    }
  }

  private Object writeReplace() {
    StringWriter stringWriter = new StringWriter();
    printStackTrace(new PrintWriter(stringWriter, true));
    return new Exception(stringWriter.toString());
  }

  public String getMessageNumber() {
    return messageNumber;
  }
}

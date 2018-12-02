package com.sap.engine.frame.client;

import com.sap.exception.BaseException;
import com.sap.localization.LocalizableText;
import com.sap.localization.LocalizableTextFormatter;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.engine.frame.core.thread.ThreadResourceAccessor;


public class ClientException extends BaseException {

  private Throwable linkedException;

  /**
   * Constructs an exception with the specified root cause.
   *
   * @param rootCause throwable object which caused this exception
   */
  public ClientException(Throwable rootCause) {
    super(rootCause);
    linkedException = rootCause;
  }

  /**
   * Constructs an exception with a localizable text message.
   *
   * @param text  - localizable text message
   */
  public ClientException(LocalizableText text) {
    super(text);
  }

  /**
   * Constructs an exception with a localizable text message and the specified root cause, which caused this exception.
   *
   * @param text  - localizable text message
   * @param rootCause - throwable object, which caused this exception
   */
  public ClientException(LocalizableText text, Throwable rootCause) {
    super(text, rootCause);
    linkedException = rootCause;
  }

  /**
   * Constructs an exception with a localizable text message and the specified root cause, which caused this exception.
   *
   * @param category - logging category
   * @param severity - logging severity
   * @param location - logging location
   * @param text  - localizable text message
   * @param rootCause - throwable object, which caused this exception
   */
  public ClientException(Category category, int severity, Location location, LocalizableText text, Throwable rootCause) {
    super(category, severity, location, text, rootCause);
    linkedException = rootCause;
  }

  /**
   * Constructs an exception with a localizable text message and the specified root cause, which caused this exception.
   *
   * @param resourceId - the resource id of the localizable text
   * @param parameters - the parameters of the localizable text
   * @param rootCause - throwable object, which caused this exception
   */
  public ClientException(String resourceId, Object[] parameters, Exception rootCause) {
    super(new LocalizableTextFormatter(ThreadResourceAccessor.getInstance(), resourceId, parameters), rootCause);
    linkedException = rootCause;
  }

  /**
   * Constructors a new ServiceException with detailed message.
   *
   * @param  message  Detail message of the exception.
   * @deprecated
   */
  public ClientException(String message) {
    super(new LocalizableTextFormatter(ThreadResourceAccessor.getInstance(), message));
  }

  /**
   * Constructors a new ServiceException and links the real exception to it.
   *
   * @param  message  Detail message of the exception.
   * @param  linkedException  This is the real exception that has appeared during service work.
   * @deprecated
   */
  public ClientException(String message, Throwable linkedException) {
    super(new LocalizableTextFormatter(ThreadResourceAccessor.getInstance(), message), linkedException);
    this.linkedException = linkedException;
  }

  /**
   * Returns the linked exception.
   *
   * @return the exception.
   * @deprecated
   */
  public final Throwable getLinkedException() {
    return linkedException;
  }

  /**
   * Sets a linked exception.
   *
   * @param  linkedException  This is the real exception that has appeared during service work.
   * @deprecated
   */
  public final void setLinkedException(Throwable linkedException) {
    this.linkedException = linkedException;
  }

}


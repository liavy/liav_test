package com.sap.security.core.jmx;

import javax.management.openmbean.CompositeData;

public interface IJmxMessage extends CompositeData {

	public static final String LOCALIZEDMESSAGE = "LocalizedMessage";

	public static final String TYPE = "Type";

	public static final String LIFETIME = "LifeTime";

	public static final String CATEGORY = "Category";

	public static final String TIMESTAMP = "TimeStamp";

	public static final String MESSAGE = "Message";
	
	public static final String GUID = "Guid";

	/**
	 * Returns the localized message text in the locale of the rendered UI. If
	 * this is not possible, a localized generic text is returned. The generic
	 * text includes the clear text message ({@see IJmxMessage#getMessage()})
	 * which is not a translated text and not related to the given locale
	 * object.
	 * 
	 * @return The localized message.
	 */
	public String getLocalizedMessage();

	/**
	 * Returns the type of the message. Possible types are:
	 * {@link IJmxServer#MESSAGE_TYPE_ERROR},{@link IJmxServer#MESSAGE_TYPE_WARNING},{@link IJmxServer#MESSAGE_TYPE_INFO},{@link IJmxServer#MESSAGE_TYPE_SUCCESS}
	 * {@link IJmxServer#MESSAGE_TYPE_ERROR} means: The message text explains an
	 * error. {@link IJmxServer#MESSAGE_TYPE_WARNING} means: The message text
	 * explains a warning. {@link IJmxServer#MESSAGE_TYPE_INFO} means: The
	 * message text is informational. {@link IJmxServer#MESSAGE_TYPE_SUCCESS}
	 * means: The message text explains a successfully completed operation. This
	 * type is not in use.
	 * 
	 * @return The type of the message.
	 */
	public int getType();

	/**
	 * Returns the life time of the message. Possible life times are:
	 * {@link IJmxServer#MESSAGE_LIFETIME_PERMANENT},{@link IJmxServer#MESSAGE_LIFETIME_ONCE},{@link IJmxServer#MESSAGE_LIFETIME_ONCE_TRX}
	 * {@link IJmxServer#MESSAGE_LIFETIME_PERMANENT} means: Message is valid
	 * until explicitly deleted. This is the default for all messages.
	 * {@link IJmxServer#MESSAGE_LIFETIME_ONCE} means: Message is valid only
	 * once. After it has been displayed once, it should not appear again.
	 * {@link IJmxServer#MESSAGE_LIFETIME_ONCE_TRX} means: Message is only valid
	 * in the current transaction. This life time is not in use.
	 * 
	 * @return The life time of the message.
	 */
	public int getLifeTime();

	/**
	 * Returns the category of the message. Possible life times are:
	 * {@link IJmxServer#MESSAGE_CATEGORY_OBJECT},{@link IJmxServer#MESSAGE_CATEGORY_PROCESS}
	 * {@link IJmxServer#MESSAGE_CATEGORY_OBJECT} means: The message is related
	 * to a specific IPrincipal object.
	 * {@link IJmxServer#MESSAGE_CATEGORY_PROCESS} means: The message is realted
	 * to a process (like search).
	 * 
	 * @return The category of the message.
	 */
	public int getCategory();

	/**
	 * Returns the {@link String} object that represents the time when the
	 * message was added to the message buffer.
	 * 
	 * @return The time when the message was added to the buffer.
	 */
	public String getTimeStamp();

	/**
	 * Returns a not localized clear text which might be assigned to the
	 * message, or <code>null</code>. This message is used to enrich the
	 * localized generic message ({@see IJmxMessage#getLocalizedMessage()}.
	 * 
	 * @return The not localized clear text which is assigned to this message or
	 *         <code>null</code>
	 */
	public String getMessage();

	/**
	 * Returns a generated unique ID to identify the message.
	 * This GUID can be used in order to trace or log messages on client and server side.
	 * @return The generated GUID
	 */
	public String getGuid();
	
}

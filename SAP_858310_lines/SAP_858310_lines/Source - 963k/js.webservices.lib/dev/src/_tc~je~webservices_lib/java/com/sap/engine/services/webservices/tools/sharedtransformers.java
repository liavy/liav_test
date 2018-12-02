/*
 * Copyright (c) 2005 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.webservices.tools;

import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Node;

import com.sap.engine.lib.logging.LoggingHelper;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.MsgObject;
import com.sap.tc.logging.Severity;

/**
 * This class provides static methods for transforming DOM to String
 * 
 * @author i046525
 * @date 2008-3-17
 */
public class SharedTransformers {

	// Default transformer instance constant
	public static final int DEFAULT_TRANSFORMER = 0;

	private static final int MAX_THREAD_NUMBER_PER_TRANSFORMER = 3;
	
	// denotes how many Transformer types are currently supported.
	private static final int TRANSFORMERS_COUNT = 1;

	// Array of Transformers with length the defined number of Transformer types
	// The index of the Transformer corresponds to Transformer constant.
	public static final Transformer[] TRANSFORMERS = new Transformer[TRANSFORMERS_COUNT];

	// Array of int containing the number of threads which are waiting for
	// transforming or transforming at the moment.
	private static final int[] THREAD_COUNT = new int[TRANSFORMERS_COUNT];

	// Array of objects used for thread safe counting of the threads using the
	// corresponding transformer instance.
	// The index of the object corresponds to Transformer constant.
	private static final Object[] COUNTER_LOCK_OBJECTS = new Object[TRANSFORMERS_COUNT];

	private static final Location LOCATION = Location
			.getLocation(SharedTransformers.class);

	static {
		// initializing counter lock object
		COUNTER_LOCK_OBJECTS[DEFAULT_TRANSFORMER] = new Object();
		// initializing Transformer
		try {
			TransformerFactory tf = TransformerFactory.newInstance();
			TRANSFORMERS[DEFAULT_TRANSFORMER] = tf.newTransformer();
			TRANSFORMERS[DEFAULT_TRANSFORMER].setOutputProperty(
					OutputKeys.INDENT, "yes");
			TRANSFORMERS[DEFAULT_TRANSFORMER].setOutputProperty(
					OutputKeys.OMIT_XML_DECLARATION, "yes");
		} catch (Exception e) {
			throw new Error(e);
		}
	}

	/**
	 * Transforms <code>source</code> in <code>result</code> using the
	 * DefaultTransformer
	 * 
	 * @param transformerId int
	 * @param source Source
	 * @param result Result
	 * 
	 * @throws TransformerException
	 */
	public static void transform(int transformerId,Source source, Result result)
			throws TransformerException {
		threadEnters(transformerId);
		try {
			synchronized (TRANSFORMERS[transformerId]) {
				TRANSFORMERS[transformerId].transform(source, result);
			}
		} finally {
			threadLeaves(transformerId);
		}
	}

	/**
	 * Transforms <code>node</code> to String
	 * 
	 * @param tranformerId int
	 * @param node Node
	 * @throws TransformerException
	 */
	public static String transform(int transformerId, Node node)
			throws TransformerException {

		threadEnters(transformerId);
		try {
			Source source = new DOMSource(node);
			StreamResult result = new StreamResult(new StringWriter());
			synchronized (TRANSFORMERS[transformerId]) {
				TRANSFORMERS[transformerId].transform(source, result);
			}
			String nodeAsString = result.getWriter().toString();
			return nodeAsString;
		} finally {
			threadLeaves(transformerId);
		}

	}

	/**
	 * Increases thread counter for the specific <code>transformerId</code>.
	 * 
	 * @param transformerId int
	 */
	private static void threadEnters(int transformerId) {
		if (transformerId >= COUNTER_LOCK_OBJECTS.length) {
			throw new IllegalArgumentException("Invalid transformer id '"
					+ transformerId + "'");
		}
		synchronized (COUNTER_LOCK_OBJECTS[transformerId]) {
			if (THREAD_COUNT[transformerId] > MAX_THREAD_NUMBER_PER_TRANSFORMER) {
				Category.SYSTEM.warningT(LOCATION,
						"There are too many waiting threads, '"
								+ THREAD_COUNT[transformerId] + "', "
								+ "for transformer '" + transformerId + "'");
			}
			THREAD_COUNT[transformerId]++;
		}
	}

	/**
	 * Decreases thread counter for the specific <code>transformerId</code>.
	 * 
	 * @param transformerId int
	 */
	private static void threadLeaves(int transformerId) {
		synchronized (COUNTER_LOCK_OBJECTS[transformerId]) {
			THREAD_COUNT[transformerId]--;
		}
	}

}

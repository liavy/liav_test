package com.sap.archtech.daservice.ejb;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class VerifyIntegrityMessages {
	
	private static final String BUNDLE_NAME = "com.sap.archtech.daservice.ejb.translate.VerifyIntegrityTranslation"; //$NON-NLS-1$
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
			.getBundle(BUNDLE_NAME);

	private VerifyIntegrityMessages() {
	}

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}

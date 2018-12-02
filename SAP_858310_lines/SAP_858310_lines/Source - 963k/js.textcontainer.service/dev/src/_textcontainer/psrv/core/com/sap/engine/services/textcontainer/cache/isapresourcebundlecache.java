package com.sap.engine.services.textcontainer.cache;

import com.sap.engine.services.textcontainer.TextContainerException;

public interface ISAPResourceBundleCache
{

	SAPResourceBundleCacheObject get(String key) throws TextContainerException;

	void put(String key, SAPResourceBundleCacheObject value) throws TextContainerException;

	void remove(String key) throws TextContainerException;

	void clear() throws TextContainerException;

	void dispose() throws TextContainerException;

}

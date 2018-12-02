package com.sap.security.core.jmx.impl;

import com.sap.security.api.ISearchResult;
import com.sap.security.core.InternalUMFactory;
import com.sap.security.core.util.cache.ICache;

public class SearchResultCache {
	//TODO handle cache: delete cache when not needed
	//when calculate for a search was executed before the get, the cache will
	// be cleaned correctly
	//when get was performed without a calculate, the search result resists in
	// the cache and has to
	//be removed manually by cancelSearchRequest(String guid)

	private static ICache myCache = InternalUMFactory.getCache(
			InternalUMFactory.DEFAULT_CACHE, 100, 300, "JmxServer");

	public static void addSearchResult(String guid, ISearchResult searchResult) {
		myCache.put(guid, searchResult);
	}

	public static ISearchResult getSearchResult(String guid) {
		ISearchResult searchResult = null;
		Object o = myCache.get(guid);
		if (o != null) {
			myCache.invalidate(guid);
			searchResult = (ISearchResult) o;
		}
		return searchResult;
	}
	
	public static void invalidateSearchResult(String guid){
		myCache.invalidate(guid);		
	}

}

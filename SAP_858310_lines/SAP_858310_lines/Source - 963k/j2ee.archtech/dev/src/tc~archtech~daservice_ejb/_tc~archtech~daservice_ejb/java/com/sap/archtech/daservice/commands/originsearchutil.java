package com.sap.archtech.daservice.commands;

import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

class OriginSearchUtil 
{
	private OriginSearchUtil(){}
	
	static Set<String> createAncestors(String collectionUri, String ancestorRootUri)
	{
		// create all ancestors in the archive hierarchy of a given collection URI and sort them in ascending order 
		int ancestorLength = ancestorRootUri.length();
		if(ancestorLength > 1 && ancestorRootUri.endsWith("/"))
		{
			// cut trailing "/" (if ancestor is not "/")
			ancestorRootUri = ancestorRootUri.substring(0, ancestorRootUri.length() - 1);
		}
		Set<String> ancestors = new TreeSet<String>();
		StringBuilder tmp = new StringBuilder(ancestorRootUri);
		// ancestor root is also an ancestor
		ancestors.add(tmp.toString());
		// cut the root from the URI
		String restUri = ancestorLength > 1 ? collectionUri.substring(ancestorRootUri.length() + 1, collectionUri.length()) : collectionUri.substring(ancestorRootUri.length(), collectionUri.length());
		// create ancestor from each URI part
		StringTokenizer tokenizer = new StringTokenizer(restUri, "/");
		String uriPart = null;
		while(tokenizer.hasMoreTokens()) 
		{
			uriPart = tokenizer.nextToken();
			if(tmp.length() > 1)
			{
				tmp.append('/');
			}
			tmp.append(uriPart); 
			ancestors.add(tmp.toString());
		}
		// omit the last part (it's the given URI itself)
		ancestors.remove(tmp.toString());
		return ancestors;
	}
}

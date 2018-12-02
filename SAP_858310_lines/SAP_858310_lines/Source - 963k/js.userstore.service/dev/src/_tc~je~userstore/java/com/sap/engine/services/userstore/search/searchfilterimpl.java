/**
 * Copyright:    2002 by SAP AG
 * Company:      SAP AG, http://www.sap.com
 *
 * This software is the confidential and proprietary information
 * of SAP AG. You shall not disclose such Confidential Information
 * and shall use it only in accordance with the terms of the license
 * agreement you entered into with SAP.
 */
package com.sap.engine.services.userstore.search;

import com.sap.engine.interfaces.security.userstore.context.SearchFilter;
import java.util.Iterator;
import java.util.Vector;

/**
 * SearchFilter represents the search criteria, which can be used to tighter
 * specify the collection of objects returned by a search request.
 *
 * A search filter without any search attributes will lead to a search result
 * containing all objects. Every search attribute restricts the result set
 * to the objects matching this criterium. All the search attributes of a search
 * filter are combined using logical AND. Entries matching the search filter
 * therefore have to match all the search attributes.
 *
 * @author  Ekaterina Zheleva
 * @version 6.40
 *
 */
public class SearchFilterImpl implements SearchFilter {
  static final long serialVersionUID = -7288706300790635109L;

  private int resultSize = 0;
  private Vector searchAttributes = null;

  public SearchFilterImpl() {
    searchAttributes = new Vector();
  }


	/**
	 * Removes all search attributes
	 */
	public void clear() {
    resultSize = 0;
    searchAttributes = new Vector();
  }

	/**
	 * Get the list of SearchAttributes
	 *
	 * @return Iterator Iterator with SearchAttributes
	 */
	public Iterator getSearchAttributes() {
    return searchAttributes.iterator();
  }

	/**
	 * Set the value of a attribute to match in the search.
	 *
 	 * <p>NOTE: When MaxResultSize is set, this method can only be used, if the
 	 *          search filter does not already contain a search attribute.
	 *
	 * @param attribute The name of the attribute
	 * @param value The value to match
	 * @param operator Use constants defined in {@link com.sap.engine.interfaces.security.userstore.context.SearchAttribute}
	 */
	public void setSearchAttribute(String attribute, String value, int operator) {
    searchAttributes.addElement(new SearchAttributeImpl(attribute, value, operator));
  }

	/**
	 * Set the maxium size of the search result set. For an unlimited search result you have
	 * to specify a limit of '0' or smaller.
	 *
	 * <p>NOTE: This method can only be used, if the search filter does not contain
	 *   		more than one search attribute.
	 *
	 * @param resultSize Set the maximum result set size
	 */
	public void setMaxSearchResultSize(int resultSize) {
    this.resultSize = resultSize;
  }

	/**
	 * Get the maximum size of the search result set
	 *
	 * @return int the max search result size
	 */
	public int getMaxSearchResultSize() {
    return resultSize;
  }
}

package com.sap.archtech.archconn.values;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * The <code>LegalHoldValues</code> class is used in the <i>LEGALHOLD</i> commands to represent the assignment of Legal Hold cases
 * to URIs of archived resources/collections.
 */

public class LegalHoldValues implements Serializable
{
  private static final long serialVersionUID = 42L;

  private final HashMap<String, String[]> legalHoldValues;

  /**
   * Create a <code>LegalHoldValues</code> instance mapping a single Legal Hold cases to a set of given URIs.
   * Used by the LEGALHOLDADD command.
   * @param uris The URIs having the same Legal Hold case assigned.
   * @param legalHoldCase The Legal Hold case
   * @throws IllegalArgumentException Thrown if <code>uris</code> or <code>legalHoldCase</code> is <code>null</code> or empty
   */
  public LegalHoldValues(Set<String> uris, String legalHoldCase)
  {
    if(uris == null || uris.size() == 0)
    {
      throw new IllegalArgumentException("Missing URI(s)!");
    }
    if(legalHoldCase == null || "".equals(legalHoldCase))
    {
      throw new IllegalArgumentException("Missing Legal Hold case!");
    }

    legalHoldValues = new HashMap<String, String[]>(uris.size());
    String[] caseArr = new String[]{legalHoldCase};
    for(String uri : uris)
    {
      legalHoldValues.put(uri, caseArr);
    }
  }

  /**
   * Create a <code>LegalHoldValues</code> instance mapping a set of Legal Hold cases to one single URI.
   * Used by the LEGALHOLDGET command.
   * @param uri The URI having the Legal Hold cases assigned.
   * @param legalHoldCases The set of Legal Hold case
   * @throws IllegalArgumentException Thrown if <code>uri</code> is <code>null</code>
   */
  public LegalHoldValues(String uri, Set<String> legalHoldCases)
  {
    if(uri == null || "".equals(uri))
    {
      throw new IllegalArgumentException("Missing URI!");
    }

    legalHoldValues = new HashMap<String, String[]>(1);
    if(legalHoldCases == null || legalHoldCases.size() == 0)
    {
      legalHoldValues.put(uri, new String[0]);
    }
    else
    {
      String[] caseArr = legalHoldCases.toArray(new String[legalHoldCases.size()]);
      String[] copy = new String[caseArr.length];
      System.arraycopy(caseArr, 0, copy, 0, caseArr.length);
      legalHoldValues.put(uri, copy);
    }
  }

  /**
   * Get all assignments of Legal Hold cases to URIs that are contained in this instance.
   * @return An unmodifiable map containing the Legal Hold case assignments
   */
  public Map<String, String[]> getAllLegalHoldCases()
  {
    return Collections.unmodifiableMap(legalHoldValues);
  }

  /**
   * Get the Legal Hold cases held by this instance for one specific URI.
   * @param uri URI of the resource/collection
   * @return The Legal Hold cases of the URI. May be an empty array.
   */
  public String[] getLegalHoldCases(String uri)
  {
    String[] legalHoldCases = legalHoldValues.get(uri);
    if(legalHoldCases == null)
    {
      return new String[0];
    }
    return legalHoldCases;
  }
}

package com.sap.engine.services.userstore.search;

import com.sap.engine.interfaces.security.userstore.context.SearchAttribute;

import java.util.StringTokenizer;
import java.util.NoSuchElementException;


public class SearchUtil {

  static boolean isLike(String attributeValue, String node) {
    StringTokenizer tokenizer = new StringTokenizer(attributeValue, SearchAttribute.WILDCARD_MANY);
    String token = null;
    String extoken = null;
    String nexttoken= null;
    if (tokenizer.hasMoreTokens()) {
      token = tokenizer.nextToken();
    } else {
      return true;
    }

    int pos = node.indexOf(token);
    if (pos == -1 || (!attributeValue.startsWith(SearchAttribute.WILDCARD_MANY) && pos > 0)) {
      return false;
    }

    while (tokenizer.hasMoreTokens()) {
      extoken = token;
      if (nexttoken != null) {
        token = nexttoken;
      } else {
        token = tokenizer.nextToken();
      }
      try {
        nexttoken = tokenizer.nextToken();
      } catch (NoSuchElementException nsee) {
        pos = node.lastIndexOf(token, node.length());
        if (pos == -1) {
          return false;
        }
        break;
      }
      pos = node.indexOf(token, pos + extoken.length());
      if (pos == -1) {
        return false;
      }

      if (!tokenizer.hasMoreTokens()) {
        extoken = token;
        token = nexttoken;
        pos = node.lastIndexOf(token, node.length());
        if (pos == -1) {
          return false;
        }
      }
    }

    if (!attributeValue.endsWith(SearchAttribute.WILDCARD_MANY) && (pos + token.length() != node.length())) {
      return false;
    }
    return true;
  }
}

package com.sap.engine.services.servlets_jsp.server.deploy.util.jacc;

import com.sap.engine.services.httpserver.lib.ParseUtils;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebIllegalStateException;

import java.io.File;

public class UrlPattern {
  public static final int PATTERN_EXACT = 1; /// like "/my/dir/file1.txt"
  public static final int PATTERN_EXTENSION = 2; // like "*.jsp"
  public static final int PATTERN_PREFIX = 3; // like "/dir/*"
  public static final int PATTERN_DEFAULT = 4; // for default servlet

  public static final String SLASH = "/";
  protected static final String slashStar = "/*";
  private static final String starDot = "*.";

  private String pattern = null;
  private int patternType = -1;

  public UrlPattern(String pattern) {
    this.pattern = canonicalize(pattern);
    patternType = getPatternType(pattern);
  }

  public String getPattern() {
    return pattern;
  }

  int getPatternType() {
    return patternType;
  }

  public static String canonicalize(String urlPatternValue) {
    int patternType = getPatternType(urlPatternValue);
    switch (patternType) {
      case PATTERN_DEFAULT: {
        return SLASH;
      }
      case PATTERN_EXACT: {
        urlPatternValue = ParseUtils.canonicalize(urlPatternValue).replace(File.separatorChar, ParseUtils.separatorChar);
        if (!urlPatternValue.startsWith(ParseUtils.separator)) {
          return ParseUtils.separatorChar + urlPatternValue;
        } else {
          return urlPatternValue;
        }
      }
      case PATTERN_EXTENSION: {
        return urlPatternValue;
      }
      case PATTERN_PREFIX: {
        urlPatternValue = ParseUtils.canonicalize(urlPatternValue).replace(File.separatorChar, ParseUtils.separatorChar);
        if (!urlPatternValue.startsWith(ParseUtils.separator)) {
          return ParseUtils.separatorChar + urlPatternValue;
        } else {
          return urlPatternValue;
        }
      }
      default: {
        throw new WebIllegalStateException(WebIllegalStateException.ILLEGAL_URL_PATTERN_TYPE, new Object[]{"" + patternType});
      }
    }
  }

  /**
   * Performs qualification check comparison, i.e. if some pattern is contained in other one.
   * @return  - the value <code>0</code> if the second urlPattern is equal to the first UrlPattern; 
   *          - a value less than <code>0</code> if first UrlPattern is less than the second urlPattern argument; 
   *          - and a value greater than <code>0</code> if first UrlPattern is greater than second urlPattern argument.
   *          - If the two patterns are not comparable - returns "-2".
   *          Greater means higher priority (more specific pattern): compare("/test/*","/*") = 1
   */
  public static int compare(UrlPattern first, UrlPattern second) {
    if (first.getPattern().equals(second.getPattern())) {
      return 0;
    }
    switch (first.getPatternType()) {
      case PATTERN_EXACT: {
        switch (second.getPatternType()) {
          case PATTERN_EXACT: {
            return -2; //not compareable
          }
          case PATTERN_PREFIX: {
            if (second.getPattern().equals(first.getPattern() + slashStar)) {
              return 1;
            } else {
              return -2;
            }
          }
          case PATTERN_EXTENSION: {
            if (first.getPattern().endsWith(second.getPattern().substring(1))) {
              return 1;
            } else {
              return -2;
            }
          }
          case PATTERN_DEFAULT: {
            return 1;
          }
          default: {
            throw new IllegalArgumentException();//todo - exception lib
          }
        }
      }

      case PATTERN_PREFIX: {
        switch (second.getPatternType()) {
          case PATTERN_EXACT: {
            if (first.getPattern().equals(second.getPattern() + slashStar)) {
              return 2;
            } else {
              return -2;
            }
          }
          case PATTERN_PREFIX: {
            if (first.getPattern().length() > second.getPattern().length()) {
              if (first.getPattern().startsWith(second.getPattern().substring(0, second.getPattern().length() - 1))) {
                return 1;
              } else {
                return -2;
              }
            } else {
              if (second.getPattern().startsWith(first.getPattern().substring(0, first.getPattern().length() - 1))) {
                return 2;
              } else {
                return -2;
              }
            }
          }
          case PATTERN_EXTENSION: {
            return 1;
          }
          case PATTERN_DEFAULT: {
            if (first.getPattern().equals(UrlPattern.slashStar)) {
              return -2;
            } else {
              return 1;
            }
          }
          default: {
            throw new IllegalArgumentException();//todo - exception lib
          }
        }
      }

      case PATTERN_EXTENSION: {
        switch (second.getPatternType()) {
          case PATTERN_EXACT: {
            if (second.getPattern().endsWith(first.getPattern().substring(1))) {
              return 2;
            } else {
              return -2;
            }
          }
          case PATTERN_PREFIX: {
            return 2;
          }
          case PATTERN_EXTENSION: {
            return -2;
          }
          case PATTERN_DEFAULT: {
            return 1;
          }
          default: {
            throw new IllegalArgumentException();//todo - exception lib
          }
        }
      }

      case PATTERN_DEFAULT: {
        switch (second.getPatternType()) {
          case PATTERN_EXACT: {
            return 2;
          }
          case PATTERN_PREFIX: {
            if (second.getPattern().equals(UrlPattern.slashStar)) {
              return -2;
            } else {
              return 2;
            }
          }
          case PATTERN_EXTENSION: {
            return 2;
          }
          case PATTERN_DEFAULT: {
            return 0;
          }
          default: {
            throw new IllegalArgumentException();//todo - exception lib
          }
        }
      }
      default: {
        throw new IllegalArgumentException();//todo - exception lib
      }
    }
  }

  private static int getPatternType(String pattern) {
    if (pattern.startsWith(SLASH) && pattern.endsWith(slashStar)) {
      return PATTERN_PREFIX;
    } else if (pattern.startsWith(starDot)) {
      return PATTERN_EXTENSION;
    } else if (pattern.equals(SLASH)) {
      return PATTERN_DEFAULT;
    } else {
      return PATTERN_EXACT;
    }
  }
}

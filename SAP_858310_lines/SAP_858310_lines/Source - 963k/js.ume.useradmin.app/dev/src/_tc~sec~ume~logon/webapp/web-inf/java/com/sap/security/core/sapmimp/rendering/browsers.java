package com.sap.security.core.sapmimp.rendering;

import javax.servlet.http.HttpServletRequest;


/**
 * we use MSIE5 as default...
 * see also com.sapportals.htmlb.rendering.PageContext
 *
 * what about NN7?
 */
public class Browsers {
  public final static String VERSIONSTRING = "$Id: //engine/js.ume.useradmin.app/dev/src/_tc~sec~ume~logon/webapp/WEB-INF/java/com/sap/security/core/sapmimp/rendering/Browsers.java#1 $ from $DateTime: 2008/09/17 17:08:55 $ ($Change: 217715 $)";

  public final static String DEFAULT      = "ie5";
  public final static String NETSCAPE_6   = "nn6";
  public final static String NETSCAPE_7   = "nn7";
  public final static String MSIE5        = "ie5";
  public final static String MSIE6        = "ie6";

  public static String getStyleSheet(HttpServletRequest httpServletRequest) {
    // Try to determine User-Agent
    String userAgent = httpServletRequest.getHeader("User-Agent");
  
    if (userAgent != null) {
      userAgent = userAgent.toUpperCase();
      String userAgentUP = userAgent.toUpperCase();
      int msie_start_pos = userAgentUP.indexOf("MSIE");
      
      if ((msie_start_pos >= 0) && (userAgent.indexOf("OPERA") < 0)) {
        // extract MSIE part (avoids confusion with 'Windows 5.0' part):
        int msie_end_pos = userAgentUP.indexOf(";", msie_start_pos);
        String msie_part = (msie_end_pos >= 0) 
                            ? userAgentUP.substring(msie_start_pos, msie_end_pos) 
                            : userAgentUP.substring(msie_start_pos);
        
        if (msie_part.indexOf("5.01") >= 0) {
          return MSIE5; //setBrowserId(BrowserType.MSIE5); // 5.01? Differences??
        } else if (msie_part.indexOf("5.0") >= 0) {
          return MSIE5; //setBrowserId(BrowserType.MSIE5);
        } else if (msie_part.indexOf("5.5") >= 0) {
          return MSIE5; //setBrowserId(BrowserType.MSIE55);
        } else if (msie_part.indexOf("6.0") >= 0) {
          return MSIE6; //setBrowserId(BrowserType.MSIE6);
        } else {
          return DEFAULT; //setBrowserId(BrowserType.MSIE4);
        }
      } else if (userAgent.indexOf("OPERA") >= 0) {
        return NETSCAPE_6; //(BrowserType.NETSCAPE_6);
      } else if (userAgent.indexOf("MOZILLA/5.0") >= 0) {
        return NETSCAPE_6; //setBrowserId(BrowserType.NETSCAPE_6);
      } else {
        return DEFAULT; //setBrowserId(BrowserType.DEFAULT);
      }
    } 
      
    return DEFAULT; //setBrowserId(BrowserType.DEFAULT);
  }

  private Browsers() {
    //
  }
}
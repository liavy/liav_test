package com.sap.engine.services.servlets_jsp.chain.impl;

import com.sap.engine.services.httpserver.chain.BaseFilterConfig;
import com.sap.engine.services.httpserver.chain.ChainComposer;
import com.sap.engine.services.httpserver.chain.Filter;
import com.sap.engine.services.httpserver.chain.FilterException;
import com.sap.engine.services.servlets_jsp.filters.ApplicationSelector;
import com.sap.engine.services.servlets_jsp.filters.DSRWebContainerFilter;
import com.sap.engine.services.servlets_jsp.filters.ServletSelector;

public class ChainComposerImpl extends ChainComposer {
  public ChainComposerImpl() throws FilterException {    
    Filter filter = new ApplicationSelector(this);
    filter.init(new BaseFilterConfig("Application Selector"));
    filters.add(filter);

    filter = new ServletSelector();
    filter.init(new BaseFilterConfig("Servlet Selector"));
    filters.add(filter);
    
    filter = new DSRWebContainerFilter();
    filter.init(new BaseFilterConfig("DSR Filter for Instrumentaion of the Web Conatiner service"));
    filters.add(filter);
    
  }
}

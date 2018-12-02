package com.sap.engine.services.servlets_jsp.lib.jspruntime;

import javax.servlet.jsp.tagext.JspFragment;
import javax.servlet.jsp.tagext.JspTag;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.JspContext;

import com.sap.engine.services.servlets_jsp.server.application.InjectionWrapper;
import com.sap.engine.services.servlets_jsp.server.application.ServletContextImpl;
import com.sap.engine.services.servlets_jsp.server.lib.StringUtils;

/**
 * Created by IntelliJ IDEA.
 * User: mladen-m
 * Date: 2004-7-14
 * Time: 17:54:44
 * To change this template use File | Settings | File Templates.
 */
public abstract class JspFragmentBase
  extends JspFragment {

  protected int index;
  protected JspContext jspContext;
  protected PageContextImpl _jspx_pageContext;
  /**
   * Used runtime by the JSP for injection
   */
  protected InjectionWrapper _jspx_resourceInjector = null;
  protected JspTag parentTag;

  public JspFragmentBase(int index, JspContext jspContext, JspTag parent)
  {
    this.index = index;
    this.jspContext = jspContext;
    this.parentTag = parent;

    _jspx_pageContext = null;
    if (jspContext instanceof PageContext)
    {
      _jspx_pageContext = (PageContextImpl)jspContext;
      if (StringUtils.greaterThan(((ServletContextImpl)_jspx_pageContext.getServletContext()).getApplicationContext().getWebApplicationConfiguration().getWebAppVersion(), 2.4)) {
        _jspx_resourceInjector = ((ServletContextImpl)_jspx_pageContext.getServletContext()).getApplicationContext().getInjectionWrapper();
      }
    }
    
  }

  public JspContext getJspContext() {
    return this.jspContext;
  }

  public JspTag getParentTag() {
    return this.parentTag;
  }
}

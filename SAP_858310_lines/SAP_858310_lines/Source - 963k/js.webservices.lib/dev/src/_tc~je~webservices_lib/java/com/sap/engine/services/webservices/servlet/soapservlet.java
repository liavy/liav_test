package com.sap.engine.services.webservices.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sap.engine.interfaces.webservices.runtime.ServletDispatcher;

public class SoapServlet extends HttpServlet {

  private static final String WS_DISPATCHER = "/wsContext/" + ServletDispatcher.NAME;
  
  protected Context ctx;

  private ServletDispatcher dispatcher;

  public void start() throws Exception {
    Properties p = new Properties();
    p.put("domain", "true");
    ctx = new InitialContext(p);
    dispatcher = (ServletDispatcher) ctx.lookup(WS_DISPATCHER);
  }

  private void throwException(Exception e) throws ServletException, IOException {
    if (e instanceof ServletException) {
      throw (ServletException) e;
    } else if (e instanceof IOException) {
      throw (IOException) e;
    }
    StringWriter w = new StringWriter();
    PrintWriter pw = new PrintWriter(w);
    e.printStackTrace(pw);
    pw.flush();
    throw new ServletException(w.toString());
  }
     

  public void init() throws ServletException {
    try {
      start();
    } catch (Exception e) {
      StringWriter w = new StringWriter();
      PrintWriter pw = new PrintWriter(w);
      e.printStackTrace(pw);
      pw.flush();
      throw new UnavailableException(w.toString(), 0);
    }
  }

  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    try {
      dispatcher.doPost(request, response, this);
    } catch (Exception e) {
      throwException(e);
    }
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    try {
      dispatcher.doGet(request, response, this);
    } catch (Exception e) {
      throwException(e);
    }
  }

  protected void doHead(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    try {
      dispatcher.doHead(request, response, this);
    } catch (Exception e) {
      throwException(e);
    }
  }

}

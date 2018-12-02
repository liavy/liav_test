/*
 * Created on 2004-6-25
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.sap.engine.services.servlets_jsp.jspparser_api.jspparser;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.jsp.tagext.TagInfo;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

import com.sap.engine.services.servlets_jsp.jspparser_api.JspPageInterface;
import com.sap.engine.services.servlets_jsp.jspparser_api.exception.JspParseException;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.exceptions.SAXBuilderException;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.Attribute;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.DebugInfo;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.Element;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.ElementCollection;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.Indentifier;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.JspElement;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.JspIncludeDirective;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.JspTag;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.OutputCommentElement;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.Position;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.TemplateData;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.xmlsyntax.RootTag;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.taglib.tagfiles.TagFilesUtil;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.deploy.descriptor.TagLibDescriptor;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;


/**
 * @author ivo-s
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class SAXTreeBuilder extends DefaultHandler implements LexicalHandler {

  public static final String URI_EMPTY = "";
  public static final String JSP = "jsp";
  public static final String URI_JSP = "http://java.sun.com/JSP/Page";
  /**
   * http://www.w3.org/2000/xmlns/
   * The prefix xmlns: was specified as a syntactic device for declaring namespaces,
   * but was not itself associated with any namespace name by the Jan 1999 namespaces specification.
   * But in some processing contexts, e.g. DOM, it is useful to represent all XML attributes as (namespace name, local name) pairs.
   * For this purpose, the namespace name http://www.w3.org/2000/xmlns/ is assigned.
   * Note that you must use the reserved prefix xmlns: when declaring namespaces.
   * It does not work to declare another prefix with this namespace name and then try to use it for namespace declarations.
   * The attribute name xmlns, which is used to declare the default namespace, is also hereby associated with this namespace name.
   */
  public static final String URI_XMLNS = "http://www.w3.org/2000/xmlns/";

  /**
   * http://www.w3.org/TR/REC-xml-names/#defaulting
   * A default namespace declaration applies to all unprefixed element names within its scope.
   * Default namespace declarations do not apply directly to attribute names; the interpretation of unprefixed attributes is determined by the element on which they appear.
   * If there is a default namespace declaration in scope, the expanded name corresponding to an unprefixed element
   * name has the URI of the default namespace as its namespace name.
   * If there is no default namespace declaration in scope, the namespace name has no value.
   * The namespace name for an unprefixed attribute name always has no value.
   * The attribute value in a default namespace declaration MAY be empty. This has the same effect, within the scope of the declaration, of there being no default namespace.
   */
  public static final String URI_XHTML = "http://www.w3.org/1999/xhtml"; // Cust CSN 687884 2007

  public static final String XMLNS_PREFIX = "xmlns:";
  public static final String XMLNS = "xmlns";

  public static final String URN_JSP_TLD = "urn:jsptld:";
  public static final String URN_JSP_TAGDIR = "urn:jsptagdir:";

  protected static final String JSP_ROOT          = "root";
  protected static final String JSP_TEXT          = "text";
  protected static final String JSP_DECLARATION   = "declaration";
  protected static final String JSP_SCRIPTLET      = "scriptlet";
  protected static final String JSP_EXPRESSION    = "expression";
  protected static final String JSP_USE_BEAN      = "useBean";
  protected static final String JSP_SET_PROPERTY  = "setProperty";
  protected static final String JSP_GET_PROPERTY  = "getProperty";
  protected static final String JSP_INCLUDE       = "include";
  protected static final String JSP_FORWARD       = "forward";
  protected static final String JSP_PARAM         = "param";
  protected static final String JSP_PLUGIN        = "plugin";
  protected static final String JSP_PARAMS        = "params";
  protected static final String JSP_FALLBACK      = "fallback";
  protected static final String JSP_ATTRIBUTE     = "attribute";
  protected static final String JSP_BODY          = "body";
  protected static final String JSP_INVOKE        = "invoke";
  protected static final String JSP_DO_BODY       = "doBody";
  protected static final String JSP_ELEMENT       = "element";
  protected static final String JSP_OUTPUT        = "output";

  protected static final String JSP_DIRECTIVE_PAGE      = "directive.page";
  protected static final String JSP_DIRECTIVE_INCLUDE   = "directive.include";
  protected static final String JSP_DIRECTIVE_TAG       = "directive.tag";
  protected static final String JSP_DIRECTIVE_ATTRIBUTE = "directive.attribute";
  protected static final String JSP_DIRECTIVE_VARIABLE  = "directive.variable";


  private static final Integer EC_EMPTY      = new Integer(-1);

  private static final int EC_DOCUMENT      = 0;
  private static final int EC_ROOT          = 1;
  private static final int EC_TEXT          = 2;
  private static final int EC_DECLARATION   = 3;
  private static final int EC_SCRIPLET      = 4;
  private static final int EC_EXPRESSION    = 5;
  private static final int EC_USE_BEAN      = 6;
  private static final int EC_SET_PROPERTY  = 7;
  private static final int EC_GET_PROPERTY  = 8;
  private static final int EC_INCLUDE       = 9;
  private static final int EC_FORWARD       = 10;
  private static final int EC_PARAM         = 11;
  private static final int EC_PLUGIN        = 12;
  private static final int EC_PARAMS        = 13;
  private static final int EC_FALLBACK      = 14;
  private static final int EC_ATTRIBUTE     = 15;
  private static final int EC_BODY          = 16;
  private static final int EC_INVOKE        = 17;
  private static final int EC_DO_BODY       = 18;
  private static final int EC_ELEMENT       = 19;
  private static final int EC_OUTPUT        = 20;

  private static final int EC_DIRECTIVE_PAGE      = 21;
  private static final int EC_DIRECTIVE_INCLUDE   = 22;
  private static final int EC_DIRECTIVE_TAG       = 23;
  private static final int EC_DIRECTIVE_ATTRIBUTE = 24;
  private static final int EC_DIRECTIVE_VARIABLE  = 25;

  private static final int EC_TEMPLATE_DATA       = 26;
  private static final int EC_CUSTOM_TAG          = 27;

  private static final Integer[] contextIds = new Integer[] {
    new Integer(EC_DOCUMENT      ),
    new Integer(EC_ROOT          ),
    new Integer(EC_TEXT          ),
    new Integer(EC_DECLARATION   ),
    new Integer(EC_SCRIPLET      ),
    new Integer(EC_EXPRESSION    ),
    new Integer(EC_USE_BEAN      ),
    new Integer(EC_SET_PROPERTY  ),
    new Integer(EC_GET_PROPERTY  ),
    new Integer(EC_INCLUDE       ),
    new Integer(EC_FORWARD       ),
    new Integer(EC_PARAM         ),
    new Integer(EC_PLUGIN        ),
    new Integer(EC_PARAMS        ),
    new Integer(EC_FALLBACK      ),
    new Integer(EC_ATTRIBUTE     ),
    new Integer(EC_BODY          ),
    new Integer(EC_INVOKE        ),
    new Integer(EC_DO_BODY       ),
    new Integer(EC_ELEMENT       ),
    new Integer(EC_OUTPUT        ),
    new Integer(EC_DIRECTIVE_PAGE      ),
    new Integer(EC_DIRECTIVE_INCLUDE   ),
    new Integer(EC_DIRECTIVE_TAG       ),
    new Integer(EC_DIRECTIVE_ATTRIBUTE ),
    new Integer(EC_DIRECTIVE_VARIABLE  ),

    new Integer(EC_TEMPLATE_DATA  ),
    new Integer(EC_CUSTOM_TAG  ),
  };

  /* JSP parser */
  private JspPageInterface parser;
  /* parsing context identifier stack */
  private List<Integer> contextStack;
  /* parsing element stack */
  private List<Element> elementStack;
  /* current parsing element */
  private Element currentElement;
  /* current context */
  private Integer currentContext;
  /* current position */
  private Position currentPosition;
  /* document locator */
  private Locator locator;
  /* document start position */
  private Position rootStartPosition;
  /* document end position */
  private Position rootEndPosition;
  /* log location */
  private static Location location = LogContext.getLocationJspParser();;

  private int countLevel = 0;
  /**
   * Flag indicating that all elements in the body of tagdependent custom tag, should be treated as TemplateData
   */
  private String tagdependentQName = null;

  private String tagdependentQNameSaved = null;

  private boolean rootNamespaceSet = false;
  /**
   * Constructs new SAX Handler that belongs to the specified ParserImpl.
   * All parsed elements will be assigned to this parser instance.
   * @param parser jsp parser
   */
  public SAXTreeBuilder(JspPageInterface parser) {
    //assert parser != null;
    this.parser = parser;
    this.contextStack = new ArrayList<Integer>();
    this.elementStack = new ArrayList<Element>();
    this.currentContext = EC_EMPTY;
  }

    /**
    * @see org.xml.sax.ext.LexicalHandler#startDTD(String name, String publicId, String systemId);
    */
   public void startDTD(String name, String publicId, String systemId) throws SAXException {
     //nothing to do
   }

   /**
    * @see org.xml.sax.ext.LexicalHandler#endDTD();
    */
   public void endDTD() throws SAXException {
     //nothing to do
   }

   /**
    * @see org.xml.sax.ext.LexicalHandler#startCDATA();
    */
   public void startEntity (String name) throws SAXException {
     //nothing to do
   }

   /**
    * @see org.xml.sax.ext.LexicalHandler#startCDATA();
    */
   public void endEntity (String name) throws SAXException {
     //nothing to do
   }

   /**
    * @see org.xml.sax.ext.LexicalHandler#startCDATA();
    */
   public void startCDATA () throws SAXException {
     //nothing to do
   }

   /**
    * @see org.xml.sax.ext.LexicalHandler#endCDATA();
    */
   public void endCDATA ()throws SAXException {
     //nothing to do
   }

   /**
    * @see org.xml.sax.ext.LexicalHandler#comment(char ch[], int start, int length);
    */
   public void comment (char ch[], int start, int length) throws SAXException {
     //assert locator != null;

     if (location.bePath()) {
       Object[] args = new Object[] {new String(ch,start,length)};
       location.pathT("SAXTreeBuilder.comment [{0}] ",args);
     }
     //comment is skipped before root tag
     if( countLevel == 0 ){
       return;
     }
     // create some debugInfo
     Position lastPosition = currentPosition;
     currentPosition = new Position(locator.getLineNumber(),locator.getColumnNumber());
     DebugInfo debugInfo = new DebugInfo(lastPosition,currentPosition,parser.currentFileName(), parser.getWebContainerParameters().isJspDebugSupport());

     // first check context
     switch (currentContext.intValue()) {
       case EC_OUTPUT              :
       case EC_DIRECTIVE_PAGE      :
       case EC_DIRECTIVE_INCLUDE   :
       case EC_DIRECTIVE_TAG       :
       case EC_DIRECTIVE_ATTRIBUTE :
       case EC_DIRECTIVE_VARIABLE  :
         // TODO exception
         throw new SAXBuilderException(JspParseException.DIRECTIVE_SHOULD_HAVE_EMPTY_BODY);
       default :
         break;
     }

     StringBuffer sb = new StringBuffer(length + 7);
     sb.append("<!--");
     sb.append(ch,start,length);
     sb.append("-->");

     // create template data
     TemplateData templateData = new TemplateData(sb.toString(), false);
     templateData.debugInfo = debugInfo;
     templateData.parser = parser;

     // create output comment
     OutputCommentElement ocel = new OutputCommentElement();
     ocel.elements = new Element[]{templateData};
     ocel.debugInfo = debugInfo;
     ocel.parser = parser;

     // append templated data to the parsed tree
     // if this is the first element - skip it
     if (currentElement instanceof ElementCollection ) {
       Element[] newElements = SAXJspElementFactory.addElement(currentElement.elements,ocel, tagdependentQName != null);
       currentElement.elements = newElements;
     } else
     if (currentElement instanceof JspTag) {
       Element element = ((JspTag)currentElement).getBody();
       //assert element instanceof ElementCollection;
       Element[] newElements = SAXJspElementFactory.addElement(element.elements,ocel, tagdependentQName != null);
       element.elements = newElements;

     } else
     if (currentElement instanceof JspElement) {
       Element element = ((JspElement)currentElement).getBody();
       //assert element instanceof ElementCollection;
			 Element[] newElements = SAXJspElementFactory.addElement(element.elements,ocel, tagdependentQName != null);
			 element.elements = newElements;
     }

   }

  /**
   * @see org.xml.sax.helpers.DefaultHandler#setDocumentLocator(Locator locator);
   */
  public void setDocumentLocator (Locator locator) {
    if (location.bePath()) {
      Object[] args = new Object[] {locator};
      location.pathT("SAXTreeBuilder.setDocumentLocator {0} ",args);
    }
    this.locator = locator;
  }

  /**
   * @see org.xml.sax.helpers.DefaultHandler#startDocument();
   */
  public void startDocument() throws SAXException {
    //assert currentContext == EC_EMPTY;
    //assert locator != null;

    if (location.bePath()) {
      Object[] args = new Object[] {parser.currentFileName()};
      location.pathT("SAXTreeBuilder.startDocument {0} ",args);
    }

    push(EC_DOCUMENT);
    // create root collection
    currentElement = new ElementCollection(parser);
    // create some debug info
    currentPosition = new Position(locator.getLineNumber(),locator.getColumnNumber());
    rootStartPosition = currentPosition;
    //System.out.println("startDocument at (l:"+locator.getLineNumber()+",c:"+locator.getColumnNumber()+")");
  }

  /**
   * @see org.xml.sax.helpers.DefaultHandler#endDocument();
   */
  public void endDocument() throws SAXException {
    //assert locator != null;

    pop(EC_DOCUMENT);

    // assert the stack operations were correct
    //assert currentContext == EC_EMPTY;

    // create some debug info
    rootEndPosition = new Position(locator.getLineNumber(),locator.getColumnNumber());

    if (location.bePath()) {
      Object[] args = new Object[] {parser.currentFileName()};
      location.pathT("SAXTreeBuilder.endDocument {0} ",args);
    }
  }

  /**
   * @see org.xml.sax.helpers.DefaultHandler#startPrefixMapping(String prefix, String uri);
   */
  public void startPrefixMapping(String prefix, String uri) throws SAXException {

    if (location.bePath()) {
      Object[] args = new Object[] {prefix,uri};
      location.pathT("SAXTreeBuilder.startPrefixMapping prefix={0} uri={1}",args);
    }

    if (URI_JSP.equals(uri)) {
      if( !rootNamespaceSet ) {
        // this is the first set of JSP namespace so put is as default
        parser.getRootElement()._default_prefix.pop();
        rootNamespaceSet = true;
      }
      parser.getRootElement()._default_prefix.push(prefix);
      parser.getRootElement()._default_prefix_tag_start = "<" + prefix + ":";
      parser.getRootElement()._default_prefix_tag_end = "</" + prefix + ":";
      parser.getRootElement().JSP_ID = " " + prefix + ":id";
      // the URI is for the well known jsp standard actions
      return;
    }
    try {
      String tldPath = null;
      String tagDir = null;
      // the URI is for a templateLibrary
      if (uri.startsWith(URN_JSP_TAGDIR)) {
        tagDir = uri.substring(URN_JSP_TAGDIR.length());
      } else if (uri.startsWith(URN_JSP_TLD)) {
        tldPath = uri.substring(URN_JSP_TLD.length());
      } else {
        tldPath = uri;
      }
      parser.getRootElement().getJspTaglibDirective().registerTaglib(tldPath, tagDir, prefix, parser);
    } catch(JspParseException ex) {
      throw new SAXBuilderException(JspParseException.RETHROW_EXCEPTION,new Object[]{ex.getLocalizedMessage()}, ex);
    }
  }

  /**
   * @see org.xml.sax.helpers.DefaultHandler#endPrefixMapping(String uri);
   */
  public void endPrefixMapping(String prefix) throws SAXException {
    if( parser.getRootElement()._default_prefix.peek().equals(prefix) ) {
      if( parser.getRootElement()._default_prefix.size() > 1 ) {
        // remove it only if this is not the default
        // see startPrefixMapping()
        parser.getRootElement()._default_prefix.pop();
        parser.getRootElement()._default_prefix_tag_start = "<" + parser.getRootElement()._default_prefix.peek() + ":";
        parser.getRootElement()._default_prefix_tag_end = "</" + parser.getRootElement()._default_prefix.peek() + ":";
        parser.getRootElement().JSP_ID = " " + parser.getRootElement()._default_prefix.peek() + ":id";
      }
    } else {
      //According to JSP 10.3.4 taglib directive adds to global namespace.
      // while nested namespaces are not cumulative
      TagLibDescriptor tld= (TagLibDescriptor)parser.getTaglibs().remove(prefix);
      if( parser.getTaglibs().get(prefix) == null ) {
        // as two TLDs can have same prefixes , we shouldn't remove the prefixes if there is still one TLD in scope
        if( tld != null ) {
          // tld could be null if the namespace is not valid and Taglibdirective haven't created tag lib descriptor object
          String tagPrefix = null;
          if( prefix.equals("") ) {
            tagPrefix = "<" + prefix;
          } else {
            tagPrefix = "<" + prefix + ":";
          }
          if( tld.getTags() != null ) {
            for (int i = 0; i < tld.getTags().length; i++) {
              TagInfo tagInfo = tld.getTags()[i];
              String startIndend =  tagPrefix+ tagInfo.getTagName();
              parser.getRootElement().unregisterParsingElement(startIndend);
            }
          }
          parser.getRootElement().removeTagLibPrefix(tagPrefix);
        }
      }
    }
  }

  /**
   * @see org.xml.sax.helpers.DefaultHandler#startElement(String uri, String name,String qName, Attributes attributes);
   */
  public void startElement (String uri, String name,String qName, Attributes attributes) throws SAXException {
    //assert locator != null;
    countLevel++;
    if (location.bePath()) {
      Object[] args = new Object[] {uri,name,qName};
      location.pathT("SAXTreeBuilder.startElement uri={0} name={1} qName={3} ",args);
    }

    //create some debugInfo
    Position lastPosition = currentPosition;
    currentPosition = new Position(locator.getLineNumber(),locator.getColumnNumber());
    DebugInfo debugInfo = new DebugInfo(lastPosition,currentPosition,parser.currentFileName(), parser.getWebContainerParameters().isJspDebugSupport());

    // if there is no namespace specified then the element is considered to be a template data
    //or the element is in the body of tagdependent custom tag
    if (URI_EMPTY.equals(uri) || (tagdependentQName != null && URI_JSP.equals(uri) && !JSP_ATTRIBUTE.equals(name) && !JSP_BODY.equals(name))) {
      push(EC_TEMPLATE_DATA);
      currentElement = new ElementCollection(parser);
      // template data element
      Element element = SAXJspElementFactory.createTemplateDataElement(parser, qName, debugInfo, attributes, true, tagdependentQName != null);
      processElementInElementCollection(currentElement, element);
      return;
    }

    // TODO optimize multiple 'if's with switch (name.charAt(0))
    // 'a' - 1  'g' - 1 's' - 2
    // 'b' - 1  'i' - 2 't' - 1
    // 'd' - 7  'o' - 1 'u' - 1
    // 'e' - 2  'p' - 3
    // 'f' - 2  'r' - 1
    Element parentElement = currentElement;
    // if the namespace is the JSP namespace then
    // parse all recognized elements
    if (URI_JSP.equals(uri)) {
      // <jsp:root> element
      if (JSP_ROOT.equals(name)) {
        if (countLevel > 1) {
          throw new SAXBuilderException(JspParseException.JSP_ROOT_CAN_ONLY_APPEAR_AS_ROOT_ELEMENT, new Object[]{qName});
        }
        push(EC_ROOT);
        try{
          currentElement = SAXJspElementFactory.createRootElement(parser,debugInfo,attributes);
          parser.setJspRoot(true);
        }catch (JspParseException e) {
          throw new SAXBuilderException(e.getMessageNumber(), e);
        }
      } else
      // <jsp:text> element
      if (JSP_TEXT.equals(name)) {
        push(EC_TEXT);
        currentElement = SAXJspElementFactory.createTextElement(parser,debugInfo,attributes);
      } else
      // <jsp:declaration> element
      if (JSP_DECLARATION.equals(name)) {
        push(EC_DECLARATION);
        currentElement = SAXJspElementFactory.createDeclarationElement(parser,debugInfo,attributes);
      } else
      // <jsp:scriptlet> element
      if (JSP_SCRIPTLET.equals(name)) {
        push(EC_SCRIPLET);
        currentElement = SAXJspElementFactory.createScriptletElement(parser,debugInfo,attributes);
      } else
      // <jsp:expression> element
      if (JSP_EXPRESSION.equals(name)) {
        push(EC_EXPRESSION);
        currentElement = SAXJspElementFactory.createExpressionElement(parser,debugInfo,attributes);
      } else
      // <jsp:useBean> element
      if (JSP_USE_BEAN.equals(name)) {
        push(EC_USE_BEAN);
        currentElement = SAXJspElementFactory.createUseBeanElement(parser,debugInfo,attributes);
      } else
      // <jsp:setProperty> element
      if (JSP_SET_PROPERTY.equals(name)) {
        push(EC_SET_PROPERTY);
        currentElement = SAXJspElementFactory.createSetPropertyElement(parser,debugInfo,attributes);
      } else
      // <jsp:getProperty> element
      if (JSP_GET_PROPERTY.equals(name)) {
        push(EC_GET_PROPERTY);
        currentElement = SAXJspElementFactory.createGetPropertyElement(parser,debugInfo,attributes);
      } else
      // <jsp:include> element
      if (JSP_INCLUDE.equals(name)) {
        push(EC_INCLUDE);
        currentElement = SAXJspElementFactory.createIncludeElement(parser,debugInfo,attributes);
      } else
      // <jsp:forward> element
      if (JSP_FORWARD.equals(name)) {
        push(EC_FORWARD);
        currentElement = SAXJspElementFactory.createForwardElement(parser,debugInfo,attributes);
      } else
      // <jsp:param> element
      if (JSP_PARAM.equals(name)) {
        push(EC_PARAM);
        currentElement = SAXJspElementFactory.createParamElement(parser,debugInfo,attributes);
      } else
      // <jsp:plugin> element
      if (JSP_PLUGIN.equals(name)) {
        push(EC_PLUGIN);
        currentElement = SAXJspElementFactory.createPluginElement(parser,debugInfo,attributes);
      } else
      // <jsp:params> element
      if (JSP_PARAMS.equals(name)) {
        push(EC_PARAMS);
        currentElement = SAXJspElementFactory.createParamsElement(parser,debugInfo,attributes);
      } else
      // <jsp:fallback> element
      if (JSP_FALLBACK.equals(name)) {
        push(EC_FALLBACK);
        currentElement = SAXJspElementFactory.createFallbackElement(parser,debugInfo,attributes);
      } else
      // <jsp:attribute> element
      if (JSP_ATTRIBUTE.equals(name)) {
        // only jsp:body of tagdependent tag should be ignored
        //if there is an jsp:attribute it should be evaluated
        //here we save the current tagdependentQName, and will restore it in endElement of jsp:attribute
        if (tagdependentQName != null) {
          tagdependentQNameSaved = tagdependentQName;
          tagdependentQName = null;
        } else {
          tagdependentQNameSaved = null;
        }
        push(EC_ATTRIBUTE);
        currentElement = SAXJspElementFactory.createAttributeElement(parser,debugInfo,attributes);
      } else
      // <jsp:body> element
      if (JSP_BODY.equals(name)) {
        push(EC_BODY);
        currentElement = SAXJspElementFactory.createBodyElement(parser,debugInfo,attributes);
      } else
      // <jsp:invoke> element
      if (JSP_INVOKE.equals(name)) {
        push(EC_INVOKE);
        currentElement = SAXJspElementFactory.createInvokeElement(parser,debugInfo,attributes);
      } else
      // <jsp:doBody> element
      if (JSP_DO_BODY.equals(name)) {
        push(EC_DO_BODY);
        currentElement = SAXJspElementFactory.createDoBodyElement(parser,debugInfo,attributes);
      } else
      // <jsp:element> element
      if (JSP_ELEMENT.equals(name)) {
        push(EC_ELEMENT);
        currentElement = SAXJspElementFactory.createElementElement(parser,debugInfo,attributes);
      } else
      // <jsp:output> element
      if (JSP_OUTPUT.equals(name)) {
        push(EC_OUTPUT);
        currentElement = SAXJspElementFactory.createOutputElement(parser,debugInfo,attributes);
      } else
      // <jsp:directive.page> element
      if (JSP_DIRECTIVE_PAGE.equals(name)) {
        push(EC_DIRECTIVE_PAGE);
        currentElement = SAXJspElementFactory.createDirectivePageElement(parser,debugInfo,attributes);
      } else
      // <jsp:directive.include> element
      if (JSP_DIRECTIVE_INCLUDE.equals(name)) {
        push(EC_DIRECTIVE_INCLUDE);
        // TODO fix included content like
        // remove jsp:root element
        currentElement = SAXJspElementFactory.createDirectiveIncludeElement(parser,debugInfo,attributes);
      } else
      // <jsp:directive.tag> element
      if (JSP_DIRECTIVE_TAG.equals(name)) {
        push(EC_DIRECTIVE_TAG);
        currentElement = SAXJspElementFactory.createDirectiveTagElement(parser,debugInfo,attributes);
      } else
      // <jsp:directive.attribute> element
      if (JSP_DIRECTIVE_ATTRIBUTE.equals(name)) {
        push(EC_DIRECTIVE_ATTRIBUTE);
        currentElement = SAXJspElementFactory.createDirectiveAttributeElement(parser,debugInfo,attributes);
      } else
      // <jsp:directive.variable> element
      if (JSP_DIRECTIVE_VARIABLE.equals(name)) {
        push(EC_DIRECTIVE_VARIABLE);
        currentElement = SAXJspElementFactory.createDirectiveVariableElement(parser,debugInfo,attributes);
      }else{
        /*
         * JSP:spec chapter 5
	       * Standard actions are represented using XML elements with a prefix of jsp (though
	       * that prefix can be redefined in the XML syntax). A translation error will result if the
	       * JSP prefix is used for an element that is not a standard action.
         */
	      throw new SAXBuilderException(JspParseException.PREFIX_RESERVED, new Object[]{qName});
      }

      // set previous element as parent
      if( currentElement instanceof JspElement ) {
        if( parentElement instanceof JspElement ){
          ((JspElement)currentElement).setParentJspElement((JspElement)parentElement);
        }

        List<Attribute> nameSpaceAttribute = getCustomTagNamespaceToRootTagAttributes(attributes);
        ((JspElement)currentElement).getStartTag().setNameSpaceAttribute(nameSpaceAttribute);
      }
      return;
    }

    String tagDesc = uri;
    if (uri.startsWith(URN_JSP_TAGDIR)) {
      tagDesc = uri.substring(URN_JSP_TAGDIR.length());
    } else if (uri.startsWith(URN_JSP_TLD)) {
      tagDesc = uri.substring(URN_JSP_TLD.length());
    }
    Object obj = parser.getTagLibDescriptors().get(tagDesc);

    if (obj == null) {
      //      JSP.6.3 Syntactic Elements in JSP Documents
      //      JSP.6.3.1 Namespaces, Standard Actions, and Tag Libraries
      //      In contrast to Section JSP.7.3.6.2, however, a translation error must not be
      //      generated if the given uri is not found in the taglib map. Instead, any actions in the
      //      namespace defined by the uri value must be treated as uninterpreted.
      push(EC_TEMPLATE_DATA);
      currentElement = new ElementCollection(parser);
      // template data element
      Element element = SAXJspElementFactory.createTemplateDataElement(parser, qName, debugInfo, attributes, true, tagdependentQName != null);
      processElementInElementCollection(currentElement, element);
      return;
    } else if (obj instanceof Throwable){
      throw new SAXBuilderException(JspParseException.RETHROW_EXCEPTION,new Object[]{((Throwable)obj).getLocalizedMessage()}, ((Throwable)obj));
    }

    TagLibDescriptor tli = (TagLibDescriptor)obj;
    //do additional namespaces as custom tags
    push(EC_CUSTOM_TAG);
    String prefix = tli.getPrefixString().trim();
    if( !prefix.equals("") ) {
      prefix += ":";
    }
    String startIndent = "<" + prefix + name;
    String endIndent = "</" +  prefix + name + ">";
    currentElement = SAXJspElementFactory.createCustomElement(parser,debugInfo,attributes,startIndent,endIndent);
    if (TagFilesUtil.checkTagDependentBody(tli.getPrefixString(), name, parser)) {
      this.tagdependentQName = qName;
    }
    // set previous element as parent
    if( parentElement instanceof JspElement ){
      ((JspElement)currentElement).setParentJspElement((JspElement)parentElement);
    }

    List<Attribute> nameSpaceAttribute = getCustomTagNamespaceToRootTagAttributes(attributes);
    ((JspElement)currentElement).getStartTag().setNameSpaceAttribute(nameSpaceAttribute);
  }

  /**
   * When namespace of the custom tag is not specified in jsp:root but in the tag itself like this:
   * <c:forEach xmlns:c="http://java.sun.com/jsp/jstl/core" var="counter" begin="1" end="${3}">
   * @param attributes
   * @return - null or the namespace attribute if any.
   */
  private List<Attribute> getCustomTagNamespaceToRootTagAttributes(Attributes attributes) {
    List<Attribute> result= new ArrayList<Attribute>(3);;
    if (attributes == null) {
      return null;
    }
    for (int i = 0; i < attributes.getLength(); i++) {
      // if there is one attribute which begins with "xmlns:" and the value of this attribute is nit html
      // then this is the declared namespace attribute for this custom tag.
      Indentifier name = null;
      if (SAXTreeBuilder.URI_EMPTY.equals(attributes.getURI(i))) {
        name = new Indentifier(attributes.getQName(i));
      } else {
        name = new Indentifier(attributes.getLocalName(i));
      }
      if( !name.value.equals(XMLNS) &&  !name.value.startsWith(XMLNS_PREFIX) ) {
        // interested in only namespace attributes
        continue;
      }
      Indentifier value = new Indentifier(attributes.getValue(i));
      name.indentifierType = Indentifier.NOT_QUOTED;
      value.indentifierType = Indentifier.QUOTED;
      result.add(new com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.Attribute(name, value, false));
    }
    return result;
  }

  /**
   * @see org.xml.sax.helpers.DefaultHandler#endElement(String uri, String name,String qName);
   */
  public void endElement (String uri, String name, String qName) throws SAXException {
    //assert locator != null;

    if (location.bePath()) {
      Object[] args = new Object[] {uri,name,qName};
      location.pathT("SAXTreeBuilder.endElement uri={0} name={1} qName={3} ",args);
    }

    // create some debug info
    Position lastPosition = currentPosition;
    currentPosition = new Position(locator.getLineNumber(),locator.getColumnNumber());
    DebugInfo debugInfo = new DebugInfo(lastPosition,currentPosition,parser.currentFileName(), parser.getWebContainerParameters().isJspDebugSupport());

    // template data tag parsing
    if (URI_EMPTY.equals (uri) || tagdependentQName != null) {
      if (qName.equals(tagdependentQName)) {
        tagdependentQName = null;
      } else {
        Element element = SAXJspElementFactory.createTemplateDataElement(parser, qName, debugInfo, null, false, tagdependentQName != null);
        //assert currentElement instanceof ElementCollection;
        processElementInElementCollection(currentElement, element);
        pop(EC_TEMPLATE_DATA);
        return;
      }
     }

    // default jsp tag parsing
    if (URI_JSP.equals(uri)) {
      // <jsp:root> element
      if (JSP_ROOT.equals(name)) {
        SAXJspElementFactory.closeRootElement(debugInfo,currentElement);
        verify(currentElement,JspElement.ROOT);
        pop(EC_ROOT);
      } else
      // <jsp:text> element
      if (JSP_TEXT.equals(name)) {
        if (((JspElement)currentElement).getStartTag().debugInfo.end.equals(debugInfo.end)) {
          currentElement = ((JspElement)currentElement).getStartTag();
          ((JspTag)currentElement).tagType = JspTag.SINGLE_TAG;
        } else {
          SAXJspElementFactory.closeTextElement(debugInfo,currentElement);
        }
        verify(currentElement,JspElement.TEXT);
        pop(EC_TEXT);
      } else
      // <jsp:declaration> element
      if (JSP_DECLARATION.equals(name)) {
        if (((JspElement)currentElement).getStartTag().debugInfo.end.equals(debugInfo.end)) {
          currentElement = ((JspElement)currentElement).getStartTag();
          ((JspTag)currentElement).tagType = JspTag.SINGLE_TAG;
        } else {
          SAXJspElementFactory.closeDeclarationElement(debugInfo,currentElement);
        }
        verify(currentElement,JspElement.DECLARATION);
        pop(EC_DECLARATION);
      } else
      //<jsp:scriptlet> element
      if (JSP_SCRIPTLET.equals(name)) {
        if (((JspElement)currentElement).getStartTag().debugInfo.end.equals(debugInfo.end)) {
          currentElement = ((JspElement)currentElement).getStartTag();
          ((JspTag)currentElement).tagType = JspTag.SINGLE_TAG;
        } else {
          SAXJspElementFactory.closeScriptletElement(debugInfo,currentElement);
        }
        verify(currentElement,JspElement.SCRIPLET);
        pop(EC_SCRIPLET);
      } else
      //<jsp:expression> element
      if (JSP_EXPRESSION.equals(name)) {
        if (((JspElement)currentElement).getStartTag().debugInfo.end.equals(debugInfo.end)) {
          currentElement = ((JspElement)currentElement).getStartTag();
          ((JspTag)currentElement).tagType = JspTag.SINGLE_TAG;
        } else {
          SAXJspElementFactory.closeExpressionElement(debugInfo,currentElement);
        }
        verify(currentElement,JspElement.EXPRESSION);
        pop(EC_EXPRESSION);
      } else
      //<jsp:useBean> element
      if (JSP_USE_BEAN.equals(name)) {
        if (((JspElement)currentElement).getStartTag().debugInfo.end.equals(debugInfo.end)) {
          currentElement = ((JspElement)currentElement).getStartTag();
          ((JspTag)currentElement).tagType = JspTag.SINGLE_TAG;
        } else {
          SAXJspElementFactory.closeUseBeanElement(debugInfo,currentElement);
        }
        verify(currentElement,JspElement.USE_BEAN);
        pop(EC_USE_BEAN);
      } else
      //<jsp:setProperty> element
      if (JSP_SET_PROPERTY.equals(name)) {
        if (((JspElement)currentElement).getStartTag().debugInfo.end.equals(debugInfo.end)) {
          currentElement = ((JspElement)currentElement).getStartTag();
          ((JspTag)currentElement).tagType = JspTag.SINGLE_TAG;
        } else {
          SAXJspElementFactory.closeSetPropertyElement(debugInfo,currentElement);
        }
        verify(currentElement,JspElement.SET_PROPERTY);
        pop(EC_SET_PROPERTY);
      } else
      //<jsp:getProperty> element
      if (JSP_GET_PROPERTY.equals(name)) {
        if (((JspElement)currentElement).getStartTag().debugInfo.end.equals(debugInfo.end)) {
          currentElement = ((JspElement)currentElement).getStartTag();
          ((JspTag)currentElement).tagType = JspTag.SINGLE_TAG;
        } else {
          SAXJspElementFactory.closeGetPropertyElement(debugInfo,currentElement);
        }
        verify(currentElement,JspElement.GET_PROPERTY);
        pop(EC_GET_PROPERTY);
      } else
      //<jsp:include> element
      if (JSP_INCLUDE.equals(name)) {
        if (((JspElement)currentElement).getStartTag().debugInfo.end.equals(debugInfo.end)) {
          currentElement = ((JspElement)currentElement).getStartTag();
          ((JspTag)currentElement).tagType = JspTag.SINGLE_TAG;
        } else {
          SAXJspElementFactory.closeIncludeElement(debugInfo,currentElement);
        }
        verify(currentElement,JspElement.INCLUDE);
        pop(EC_INCLUDE);
      } else
      //<jsp:forward> element
      if (JSP_FORWARD.equals(name)) {
        if (((JspElement)currentElement).getStartTag().debugInfo.end.equals(debugInfo.end)) {
          currentElement = ((JspElement)currentElement).getStartTag();
          ((JspTag)currentElement).tagType = JspTag.SINGLE_TAG;
        } else {
          SAXJspElementFactory.closeForwardElement(debugInfo,currentElement);
        }
        verify(currentElement,JspElement.FORWARD);
        pop(EC_FORWARD);
      } else
      //<jsp:param> element
      if (JSP_PARAM.equals(name)) {
        if (((JspElement)currentElement).getStartTag().debugInfo.end.equals(debugInfo.end)) {
          currentElement = ((JspElement)currentElement).getStartTag();
          ((JspTag)currentElement).tagType = JspTag.SINGLE_TAG;
        } else {
          SAXJspElementFactory.closeParamElement(debugInfo,currentElement);
        }
        verify(currentElement,JspElement.PARAM);
        pop(EC_PARAM);
      } else
      //<jsp:plugin> element
      if (JSP_PLUGIN.equals(name)) {
        if (((JspElement)currentElement).getStartTag().debugInfo.end.equals(debugInfo.end)) {
          currentElement = ((JspElement)currentElement).getStartTag();
          ((JspTag)currentElement).tagType = JspTag.SINGLE_TAG;
        } else {
          SAXJspElementFactory.closePluginElement(debugInfo,currentElement);
        }
        verify(currentElement,JspElement.PLUGIN);
        pop(EC_PLUGIN);
      } else
      //<jsp:params> element
      if (JSP_PARAMS.equals(name)) {
        if (((JspElement)currentElement).getStartTag().debugInfo.end.equals(debugInfo.end)) {
          currentElement = ((JspElement)currentElement).getStartTag();
          ((JspTag)currentElement).tagType = JspTag.SINGLE_TAG;
        } else {
          SAXJspElementFactory.closeParamsElement(debugInfo,currentElement);
        }
        verify(currentElement,JspElement.PARAMS);
        pop(EC_PARAMS);
      } else
      //<jsp:fallback> element
      if (JSP_FALLBACK.equals(name)) {
        if (((JspElement)currentElement).getStartTag().debugInfo.end.equals(debugInfo.end)) {
          currentElement = ((JspElement)currentElement).getStartTag();
          ((JspTag)currentElement).tagType = JspTag.SINGLE_TAG;
        } else {
          SAXJspElementFactory.closeFallbackElement(debugInfo,currentElement);
        }
        verify(currentElement,JspElement.FALLBACK);
        pop(EC_FALLBACK);
      } else
      //<jsp:attribute> element
      if (JSP_ATTRIBUTE.equals(name)) {
        if (tagdependentQNameSaved != null) {
          tagdependentQName = tagdependentQNameSaved;
        }
        if (((JspElement)currentElement).getStartTag().debugInfo.end.equals(debugInfo.end)) {
          currentElement = ((JspElement)currentElement).getStartTag();
          ((JspTag)currentElement).tagType = JspTag.SINGLE_TAG;
        } else {
          SAXJspElementFactory.closeAttributeElement(debugInfo,currentElement);
        }
        verify(currentElement,JspElement.ATTRIBUTE);
        pop(EC_ATTRIBUTE);
      } else
      //<jsp:body> element
      if (JSP_BODY.equals(name)) {
        if (((JspElement)currentElement).getStartTag().debugInfo.end.equals(debugInfo.end)) {
          currentElement = ((JspElement)currentElement).getStartTag();
          ((JspTag)currentElement).tagType = JspTag.SINGLE_TAG;
        } else {
          SAXJspElementFactory.closeBodyElement(debugInfo,currentElement);
        }
        verify(currentElement,JspElement.BODY);
        pop(EC_BODY);
      } else
      //<jsp:invoke> element
      if (JSP_INVOKE.equals(name)) {
        if (((JspElement)currentElement).getStartTag().debugInfo.end.equals(debugInfo.end)) {
          currentElement = ((JspElement)currentElement).getStartTag();
          ((JspTag)currentElement).tagType = JspTag.SINGLE_TAG;
        } else {
          SAXJspElementFactory.closeInvokeElement(debugInfo,currentElement);
        }
        verify(currentElement,JspElement.INVOKE);
        pop(EC_INVOKE);
      } else
      //<jsp:doBody> element
      if (JSP_DO_BODY.equals(name)) {
        if (((JspElement)currentElement).getStartTag().debugInfo.end.equals(debugInfo.end)) {
          currentElement = ((JspElement)currentElement).getStartTag();
          ((JspTag)currentElement).tagType = JspTag.SINGLE_TAG;
        } else {
          SAXJspElementFactory.closeDoBodyElement(debugInfo,currentElement);
        }
        verify(currentElement,JspElement.DO_BODY);
        pop(EC_DO_BODY);
      } else
      //<jsp:element> element
      if (JSP_ELEMENT.equals(name)) {
        if (((JspElement)currentElement).getStartTag().debugInfo.end.equals(debugInfo.end)) {
          currentElement = ((JspElement)currentElement).getStartTag();
          ((JspTag)currentElement).tagType = JspTag.SINGLE_TAG;
        } else {
          SAXJspElementFactory.closeElementElement(debugInfo,currentElement);
        }
        verify(currentElement,JspElement.ELEMENT);
        pop(EC_ELEMENT);
      } else
      //<jsp:output> element
      if (JSP_OUTPUT.equals(name)) {
        if (((JspTag)currentElement).debugInfo.end.equals(debugInfo.end)) {
          SAXJspElementFactory.closeOutputElement(debugInfo,currentElement);
          verify(currentElement,-1);
          pop(EC_OUTPUT);
        } else {
          //JSR-152 (JSP 2.0,Part I,5.16) The jsp:output action cannot have a body
          throw new SAXBuilderException(JspParseException.ELEMENT_HAS_NONE_EMPTY_BODY,new Object[]{JSP_OUTPUT});
        }
      } else
      //<jsp:directive.page> element
      if (JSP_DIRECTIVE_PAGE.equals(name)) {
        SAXJspElementFactory.closeDirectivePageElement(debugInfo,currentElement);
        verify(currentElement,-1);
        pop(EC_DIRECTIVE_PAGE);
      } else
      //<jsp:directive.include> element
      if (JSP_DIRECTIVE_INCLUDE.equals(name)) {
        SAXJspElementFactory.closeDirectivePageElement(debugInfo,currentElement);
        verify(currentElement,-1);
        pop(EC_DIRECTIVE_INCLUDE);
      } else
      //<jsp:directive.tag> element
      if (JSP_DIRECTIVE_TAG.equals(name)) {
        SAXJspElementFactory.closeDirectivePageElement(debugInfo,currentElement);
        verify(currentElement,-1);
        pop(EC_DIRECTIVE_TAG);
      } else
      //<jsp:directive.attribute> element
      if (JSP_DIRECTIVE_ATTRIBUTE.equals(name)) {
        SAXJspElementFactory.closeDirectivePageElement(debugInfo,currentElement);
        verify(currentElement,-1);
        pop(EC_DIRECTIVE_ATTRIBUTE);
      } else
      //<jsp:directive.variable> element
      if (JSP_DIRECTIVE_VARIABLE.equals(name)) {
        SAXJspElementFactory.closeDirectivePageElement(debugInfo,currentElement);
        verify(currentElement,-1);
        pop(EC_DIRECTIVE_VARIABLE);
      }
      return;
    }
    String tagDesc = uri;
    if (uri.startsWith(URN_JSP_TAGDIR)) {
      tagDesc = uri.substring(URN_JSP_TAGDIR.length());
    } else if (uri.startsWith(URN_JSP_TLD)) {
      tagDesc = uri.substring(URN_JSP_TLD.length());
    }
    TagLibDescriptor tli = (TagLibDescriptor)parser.getTagLibDescriptors().get(tagDesc);
    if (tli == null) {
			//      JSP.6.3 Syntactic Elements in JSP Documents
			//      JSP.6.3.1 Namespaces, Standard Actions, and Tag Libraries
			//      In contrast to Section JSP.7.3.6.2, however, a translation error must not be
			//      generated if the given uri is not found in the taglib map. Instead, any actions in the
			//      namespace defined by the uri value must be treated as uninterpreted.
      Element element = SAXJspElementFactory.createTemplateDataElement(parser,qName,debugInfo,null,false, tagdependentQName != null);
      //assert currentElement instanceof ElementCollection;
      processElementInElementCollection(currentElement,element);
      pop(EC_TEMPLATE_DATA);
      return;
    }

    // do custom tags
    SAXJspElementFactory.closeCustomElement(debugInfo,currentElement, qName);
    verify(currentElement,JspElement.CUSTOM);
    pop(EC_CUSTOM_TAG);
    return;

  }

  /**
   * @see org.xml.sax.helpers.DefaultHandler.characters(char ch[], int start, int length);
   */
  public void characters (char ch[], int start, int length) throws SAXException {
    //assert locator != null;

    if (location.bePath()) {
      Object[] args = new Object[] {new String(ch,start,length)};
      location.pathT("SAXTreeBuilder.characters [{0}] ",args);
    }

    //create some debug info
    Position lastPosition = currentPosition;
    currentPosition = new Position(locator.getLineNumber(),locator.getColumnNumber());
    DebugInfo debugInfo = new DebugInfo(lastPosition,currentPosition,parser.currentFileName(), parser.getWebContainerParameters().isJspDebugSupport());

    // first check context
    switch (currentContext.intValue()) {
      case EC_DIRECTIVE_PAGE      :
      case EC_DIRECTIVE_INCLUDE   :
      case EC_DIRECTIVE_TAG       :
      case EC_DIRECTIVE_ATTRIBUTE :
      case EC_DIRECTIVE_VARIABLE  :
        if( new String(ch,start,length).trim().length() > 0) {
          throw new SAXBuilderException(JspParseException.DIRECTIVE_SHOULD_HAVE_EMPTY_BODY);
        }
        break;
      case EC_OUTPUT              :
        throw new SAXBuilderException(JspParseException.DIRECTIVE_SHOULD_HAVE_EMPTY_BODY);
      default :
        break;
    }

    // append data to the parsed tree
    if (currentElement instanceof JspElement) {
      processDataInJspElement(currentElement,debugInfo,ch,start,length);
    } else if (currentElement instanceof ElementCollection) {
      processDataInElementCollection(currentElement,debugInfo,ch,start,length);
    } else if (currentElement instanceof JspTag) {
      processDataInJspTag(currentElement,debugInfo,ch,start,length);
    } else if (currentElement instanceof TemplateData) {
      currentElement = SAXJspElementFactory.mergeTemplateData((TemplateData)currentElement,debugInfo,ch,start,length, tagdependentQName != null);
    } else {
      throw new SAXBuilderException(JspParseException.UNKNOWN_ELEMENT_TYPE, new Object[]{currentElement.getClass().getName()});
    }
  }

  /**
   * Push current context , push current root element and
   * register new context
   *
   * @param context identifier for a context.
   */
  private void push(int context) {
    if (location.bePath()) {
      Object[] args = new Object[] { new Integer(context) };
      location.pathT("SAXTreeBuilder.push context {0} ",args);
    }
    contextStack.add(currentContext);
    elementStack.add(currentElement);
    currentContext = contextIds[context];
    currentElement = null;
  }

  /**
   * Gets previous context from the stack and gets
   * previous jsp element from the stack.Reduces the parsed
   * tree and restores previous context and jsp element
   * as the current one.
   *
   * @param context identifier for a context.
   */
  private void pop(int context) throws SAXBuilderException {

    if (location.bePath()) {
      Object[] args = new Object[] { new Integer(context) };
      location.pathT("SAXTreeBuilder.pop context {0} ",args);
    }

    //assert currentContext.equals(contextIds[context]);

    int index = contextStack.size() - 1;

    //assert index > -1;

    Integer lastContext = contextStack.remove(index);
    //
    Element lastElement = elementStack.remove(index);

    if (EC_DOCUMENT == context) {
      // the document is parsed so simply return in
      // order not to lose the parsed tree
      return;
    }

    //assert lastElement != null;

    //reduce tree
    if (currentElement instanceof JspElement) {
      // if possible set single template data as body
      Element[] elements = ((JspElement)currentElement).getBody().elements;
      if (elements != null && elements.length == 1 &&
          elements[0] instanceof TemplateData) {
            ((JspElement)currentElement).setBody(elements[0]);
      }
    } else
    if (currentElement instanceof JspTag) {
      // if possible set single template data as body
      Element e = ((JspTag)currentElement).getBody();
      if (e != null) {
        Element[] elements = e.elements;
        if (elements != null && elements.length == 1 &&
            elements[0] instanceof TemplateData) {
              ((JspTag)currentElement).setBody(elements[0]);
        }
      }
    }

    // if possible set single template data as body
    if (currentElement instanceof JspIncludeDirective ) {
      // XML View fix include declarations
      currentElement = ((JspIncludeDirective)currentElement).getBody();
    }

    if (currentElement != null) {
      if (lastElement instanceof JspElement) {
        processElementInJspElement(lastElement,currentElement);
      } else if (lastElement instanceof ElementCollection) {
        processElementInElementCollection(lastElement,currentElement);
      } else if (lastElement instanceof JspTag) {
        processElementInJspTag(lastElement,currentElement);
      } else {
        throw new SAXBuilderException(JspParseException.UNKNOWN_ELEMENT_TYPE, new Object[]{lastElement.getClass().getName()});
      }
    }
    // restore previous context and jsp element as current
    currentElement = lastElement;
    currentContext = lastContext;
  }


  /*
   * Merges tree element within JspTag element
   */
  private void processElementInJspTag(Element element,Element newElement) {
    Element collection = ((JspTag)element).getBody();
    processElementInElementCollection0(collection,newElement);
  }

  /*
   * Merges tree element within JspElement element
   */
  private void processElementInJspElement(Element element,Element newElement) {
    Element collection = ((JspElement)element).getBody();
    processElementInElementCollection(collection,newElement);
  }

  /*
   * Merges tree element within ElementCollection element
   */
  private void processElementInElementCollection(Element element,Element newElement) {
    if (newElement instanceof ElementCollection) {
      Element[] elements = element.getElements();
      Element[] addElements = newElement.getElements();
      Element[] newElements = SAXJspElementFactory.mergeElements(elements,addElements);
      element.elements = newElements;
    } else {
      processElementInElementCollection0(element,newElement);
    }
  }

  /*
   * Add tree element to ElementCollection element
   */
  private void processElementInElementCollection0(Element element,Element newElement) {
    Element[] elements = element.getElements();
    Element[] newElements = SAXJspElementFactory.addElement(elements,newElement, tagdependentQName != null);
    element.elements = newElements;
  }

  /*
   * Add data within JspTag element
   */
  private void processDataInJspTag(Element element,DebugInfo debugInfo,char[] ch,int start,int length) {
    Element collection = ((JspTag)element).getBody();
    processDataInElementCollection(collection,debugInfo,ch,start,length);
  }

  /*
   * Add data within JspElement element
   */
  private void processDataInJspElement(Element element,DebugInfo debugInfo,char[] ch,int start,int length) {
    Element collection = ((JspElement)element).getBody();
    processDataInElementCollection(collection,debugInfo,ch,start,length);
  }

  /*
   * Add data within ElementCollection element
   */
  private void processDataInElementCollection(Element element,DebugInfo debugInfo,char[] ch,int start,int length) {
    Element[] elements = element.getElements();
    Element[] newElements = SAXJspElementFactory.addTemplateData(parser,elements,debugInfo,ch,start,length, tagdependentQName != null);
    element.elements = newElements;
  }

  private void verify(Element element,int type) throws SAXException {
    try {
      if (element instanceof JspTag) {
        ((JspTag)element).verifyAttributes();
      } else
      if (element instanceof JspElement) {
        ((JspElement)element).verify(type);
      }
    } catch(JspParseException ex) {
      throw new SAXBuilderException(JspParseException.RETHROW_EXCEPTION,new Object[]{ex.getLocalizedMessage()}, ex);
    }
  }


  public Element getParsed() throws JspParseException {
    Element element = null;
    if (currentElement != null && currentElement.elements != null
        && currentElement.elements.length == 1) {
          element = currentElement.elements[0];
    } else {
      element = currentElement;
    }

    // XML View fix for the root element
    if (!(element instanceof JspElement && ((JspElement)element).getStartTag() instanceof RootTag )) {

      if ( element instanceof JspElement) {
        JspElement root = (JspElement)SAXJspElementFactory.createRootElement(parser,((JspElement)element).getStartTag().debugInfo,null);
        SAXJspElementFactory.closeRootElement(((JspElement)element).getEndTag().debugInfo,root);
        root.setBody(element);
        return root;
      } if(element instanceof ElementCollection || currentElement instanceof ElementCollection ) { //wrap Element collection into JspElement
        JspElement root = (JspElement)SAXJspElementFactory.createRootElement(parser,new DebugInfo(rootStartPosition, rootStartPosition, parser.currentFileName(), parser.getWebContainerParameters().isJspDebugSupport()),null);
        SAXJspElementFactory.closeRootElement(new DebugInfo(rootEndPosition, rootEndPosition, parser.currentFileName(), parser.getWebContainerParameters().isJspDebugSupport()),root);
        root.setBody(element);
        return root;
      } else {
        Element root = SAXJspElementFactory.createRootElement(parser,new DebugInfo(rootStartPosition, rootStartPosition, parser.currentFileName(), parser.getWebContainerParameters().isJspDebugSupport()),null);
        SAXJspElementFactory.closeRootElement(new DebugInfo(rootEndPosition, rootEndPosition, parser.currentFileName(), parser.getWebContainerParameters().isJspDebugSupport()),root);
        return root;
      }
    }

    //'pageEncoding' and 'contentType' attributes for the
    // page directive and all jsp:id attributes are added by
    // afterwards

    return element;
  }

  	/**
  	 * notified on non-recoverable error
  	 * @see org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException)
  	 */
    public void fatalError(SAXParseException e) throws SAXException {
        throw e;
    }



    /**
     * notified on recoverable error
     * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
     */
    public void error(SAXParseException e) throws SAXException {
        throw e;
    }
}

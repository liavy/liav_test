/*
 * Copyright (c) 2000 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax;

import java.io.File;
import java.util.HashMap;
import java.util.Stack;
import java.util.StringTokenizer;

import com.sap.engine.lib.util.ConcurrentArrayObject;
import com.sap.engine.services.servlets_jsp.jspparser_api.JspPageInterface;
import com.sap.engine.services.servlets_jsp.jspparser_api.exception.JspParseException;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.SAXTreeBuilder;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.xmlsyntax.CustomJspTag;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.xmlsyntax.RootTag;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.xmlsyntax.XMLTag;
import com.sap.engine.services.servlets_jsp.server.LogContext;

/**
 * This is the basic class for parsing elements, which
 * Defines main methods for all for all parsing elements
 * @author Ivo Simeonov
 * @version 4.0
 */
public abstract class Element {

  /*
   * element types
   */
  public static final int UNKNOWN = -1;
  public static final int TEMPLATE_DATA = 0;
  public static final int OUTPUT_COMMENT = 1;
  public static final int JSP_COMMENT = 2;
  public static final int PAGE_DIRECTIVE = 3;
  public static final int INCLUDE_DIRECTIVE = 4;
  public static final int TAGLIB_DIRECTIVE = 5;
  public static final int ATTRIBUTE = 6;
  public static final int INDENTIFIER = 7;
  public static final int ACTION = 8;
  public static final int XML_TAG = 9;
  public static final int SCRIPT = 10;
    /*
   * all white space chars
   */
  public static final String WHITE_SPACE_CHARS = "\n\r\t ";
  /*
   * static info with all registered jsp elements
   */
  //public LinkedList parsingElements = new LinkedList();
  /*
   * static info with all registered prefixes of jsp elements
   */
  private char[][] prefixes = new char[0][];

  /**
   * key - one of the page directive types as String - "page", "include", "taglib" etc..
   * value - instance of the specified directive type (JspIncludeDirective, JspPageDirective, ...).
   */
  private HashMap<String, JspDirective> directives = new HashMap<String, JspDirective>(6);
  /*
   * static info with all registered jsp elements
   */
  //public Element[] _elements = new Element[0];
  /*
   * static info with all registered prefixes of jsp elements
   */
  private HashMap<String, Element> allPrefixes = new HashMap<String, Element>();

  /**
   * contains prefixes for tag libraries declared in taglib directives
   */
  private String[] tagLibPrefixes ;

  private ConcurrentArrayObject notCustomTagPrefix;

  /*
   * type for this element
   */
  protected int elementType;
  /*
   * start index in the source
   */
  public int startIndex;
  /*
   * end index in the source
   */
  public int endIndex;
  /*
   * debug info
   */
  public DebugInfo debugInfo;
  /*
   * array of inner elements
   */
  public Element[] elements;
  /*
   * the parser this element is associated with
   */
  public JspPageInterface parser;

  /**
   * Contains tag name of the parent tag
   */
  protected String parentCustomTag = null;

  /**
   *  Default prefix for JSP syntax. It can be changed in XML syntax.
   *
   */
  public Stack<String> _default_prefix = new Stack<String>();
  public String _default_prefix_tag_start = "<jsp:";
  public String _default_prefix_tag_end = "</jsp:";
  public String _name = "";

  public static String END = ">";
  public static String SLASH_END = "/>";

  /*
   * the ID attribute for XML view.
   */
  public String JSP_ID = " jsp:id";
  public static String JSP_ID_1 = "=\"";
  public static String JSP_ID_2 = "\" ";

  public static String CDATA_START = "><![CDATA[\n";
  public static String CDATA_END = "\n]]>";

  /**
   * Default constructor
   */
  public Element() {
    _default_prefix.push("jsp");
  }


  /**
   * Takes specific action corresponding to this jsp element
   * logic
   *
   * @exception   JspParseException  thrown if error occurs during
   * verification
   */
  public abstract void action() throws JspParseException;

  /**
   * Takes specific action corresponding to this jsp element
   * logic and the output goes to writer
   *
   * param writer The output of the action goes to this StringBuffer
   * @exception   JspParseException  thrown if error occurs during
   * verification
   */
  public abstract void action(StringBuffer writer) throws JspParseException;

  /**
   * Identifies this element with specific prefix
   *
   * @return prefix to use as identification for finding
   * new element of this type in the jsp source
   */
  public abstract char[] indent() throws JspParseException;



  /**
   * Returns debug info about this object.The debug info contains
   * information where this element is allocated in the jsp source file
   * and the name of this file
   *
   * @return debug info for this element
   */
  public DebugInfo getDebugInfo() {
    return this.debugInfo;
  }

  /**
   * Returns array of elements allocated in the body if this element
   *
   * @return body elements or null if such do not exists
   */
  public Element[] getElements() {
    return this.elements;
  }

  /**
   * Sets debug for this element
   *
   * @param   debugInfo  debug info to set
   */
  public void setDebugInfo(DebugInfo debugInfo) {
    this.debugInfo = debugInfo;
  }

  /**
   * Checks character for being white space character
   *
   * @param   c  character to check
   * @return  true if the character is white space character
   */
  public static boolean isWhiteSpace(char c) {
    return (WHITE_SPACE_CHARS.indexOf(c) != -1);
  }

  /**
   * Dumps in the console string
   *
   * @param   str  string to dump
   */
  public void dump(String str) {
    //    System.out.println(str);
  }

  /**
   * Return start index position of this element in the jsp source
   * file
   *
   * @return index position
   */
  public int getStartIndex() {
    return this.startIndex;
  }

  /**
   * Sets start index position of this element in the jsp source
   * file
   *
   * @param   startIndex  index to set as start index
   */
  public void setStartIndex(int startIndex) {
    this.startIndex = startIndex;
  }

  /**
   * Return end index position of this element in the jsp source
   * file
   *
   * @return index position
   */
  public int getEndIndex() {
    return this.endIndex;
  }

  /**
   * Sets end  index position of this element in the jsp source
   * file
   *
   * @param   endIndex  index to set as end index
   */
  public void setEndIndex(int endIndex) {
    this.endIndex = endIndex;
  }


  /**
   * Registers new jsp element to use for parsing.
   *
   * @param   el  element to register
   */
  public final void registerParsingElement(Element el) throws JspParseException {

    Object o = allPrefixes.get(String.copyValueOf(el.indent()));

    if (o == null) {
      //parsingElements.add(el);
      char[][] newPrefixes = new char[prefixes.length + 1][];
     // Element[] new_elements = new Element[_elements.length + 1];
      System.arraycopy(prefixes, 0, newPrefixes, 0, prefixes.length);
     // System.arraycopy(_elements, 0, new_elements, 0, _elements.length);
      newPrefixes[prefixes.length] = el.indent();
      allPrefixes.put(String.copyValueOf(el.indent()), el);
     // new_elements[_elements.length] = el;
      prefixes = newPrefixes;
     // _elements = new_elements;
    }
    else
    {
			allPrefixes.put(String.copyValueOf(el.indent()), el);
    }
    if( el instanceof JspDirective ){
      JspDirective directive = (JspDirective)el;
      directives.put(directive.getDirectiveName(), directive);
    }
  }

  /**
   * namespace in JSP documents is closed and all tags from this namespace are unregistered.
   *
   * @param   indent  element to unregister
   */
  public final void unregisterParsingElement(String indent) {
    allPrefixes.remove(indent);
  }


  /**
   * Parses next tokens of jsp file
   *
   * @param   parser  parser to use
   * @return   newly created jsp element representing a portion of the
   * jsp file
   * @exception   JspParseException  thrown if error occurs during
   * parsing
   */
  public final Element getNextElement(JspPageInterface parser) throws JspParseException {
  	Element elementType = getNextElementType(parser);
  	if ( elementType == null )
  		return elementType;
  	Element nextElement = createElementOfType(elementType);
  	return nextElement;
  }

 	/**
 	 * creates new Element of the class of the given parameter
 	 * default constructor is used
 	 * @param elementType the given type
 	 * @return Element
 	 * @throws JspParseException  if an error occurs
 	 */
  private Element createElementOfType(Element elementType) throws JspParseException {
		if ( elementType instanceof TemplateData )
			return elementType; //  getNextElementType(parser, endIndent); creates implicitly new instance of TempleteData

		if ( elementType instanceof CustomJspTag ){
			/*
			CustomJspTag oldElement = (CustomJspTag) elementType;
			CustomJspTag newElement = new CustomJspTag(oldElement.getSTART_TAG_INDENT(), oldElement.getCMP_0());
			newElement.setCopy(true);
			return newElement;
			*/
			return elementType;
		}



		Class elementTypeClass = elementType.getClass();
		Element nextElement = null;
		try{
			nextElement = (Element)elementTypeClass.newInstance();
		}catch (Exception e){
			throw new JspParseException(JspParseException.CREATING_NEW_ELEMENT_FAILED, new Object[]{elementType.getClass().getName()}, e);
		}


		return nextElement;
  }



  private Element getNextElementType(JspPageInterface parser) throws JspParseException {
		try {
			if (parser.endReached()) {
				return null;
			}

			int whiteStart = parser.currentPos();
			Position whiteStartDebugPos = parser.currentDebugPos();
			parser.skipWhiteSpace();
			int whiteEnd = parser.currentPos();

			if (whiteStart != whiteEnd) {
				Position whiteEndDebugPos = parser.currentDebugPos();
				DebugInfo info = new DebugInfo(whiteStartDebugPos, whiteEndDebugPos, parser.currentFileName(), parser.getWebContainerParameters().isJspDebugSupport());
				return new TemplateData(whiteStart, whiteEnd, info).parse(parser);
			}

			if (parser.endReached()) {
				return null;
			}

			int position = parser.currentPos();
			Element found = null;

			for (int i = 0; i < prefixes.length; i++) {
				if (parser.compare(position, position + prefixes[i].length, prefixes[i])) {
					if (found == null) {
						found = allPrefixes.get(String.copyValueOf(prefixes[i]));
						//found = _elements[i];
					} else if (found.indent().length < (allPrefixes.get(String.copyValueOf(prefixes[i]))).indent().length) {
						found =allPrefixes.get(String.copyValueOf(prefixes[i]));
						//found = _elements[i];
					}
				}
			}
      if( found != null && found instanceof XMLTag && !parser.isXml()) {
        // in JSP pages (standard syntax) XML declaration is simple template text.
        // It should be able to use scriptlet expression in "encoding" attribute.
        // See 0120031469 0003410782 2006
        found = null ;
      }
			if (found != null) {
			  if( found instanceof JspDirective){
			    found = getDirectiveByName(parser);
			  }
				return found;
			}

      if (tagLibPrefixes != null) {
        for (int i = 0; i < tagLibPrefixes.length; i++) {
          if (parser.compare(position, position + tagLibPrefixes[i].length(), tagLibPrefixes[i].toCharArray())) {
            //undefined tag found
            //JSP:SPEC:220.2  Any tag files within a JAR file that are not defined within a TLD must be ignored by the container.
					  StringBuffer name = new StringBuffer();
            fillCustomTagName(parser, name, tagLibPrefixes[i].length());
            throw new JspParseException(JspParseException.UNRECOGNIZED_TAG_NAME, new Object[] { name.toString() },
                    parser.currentFileName(),  parser.currentDebugPos());
          }
        }
      }
			if (!parser.endReached()) {
				do {
			    //check for undefined custom tag and register as potential translation error
				  checkForCustomUndefinedTag(parser);
					parser.nextChar();
				} while (!hasElementAsNext(parser));

				Position whiteEndDebugPos = parser.currentDebugPos();
				DebugInfo info = new DebugInfo(whiteStartDebugPos, whiteEndDebugPos, parser.currentFileName(), parser.getWebContainerParameters().isJspDebugSupport());
				return new TemplateData(position, parser.currentPos(), info).parse(parser);
			}

			return null;
		} catch (IndexOutOfBoundsException ex) {
			throw new JspParseException(JspParseException.EXPECTING_JSP_TAG_WHILE_READ_END_OF_FILE,
              parser.currentFileName(), parser.currentDebugPos());
		}


  }

  /**
   * Parses next tokens of jsp file
   *
   * @param   parser  parser to use
   * @param   endIndent used to identify the end of parsing
   * @return   newly created jsp element representing a portion of the
   * jsp file
   * @exception   JspParseException  thrown if error occurs during
   * parsing
   */
  public final Element getNextElement(JspPageInterface parser, char[] endIndent) throws JspParseException {
  	Element elementType = getNextElementType(parser, endIndent);
		if ( elementType == null )
			return elementType;

		Element nextElement = createElementOfType(elementType);
		return nextElement;
  }

  /**
   * Parses until:
   * 1. whitespace is found -> TemplateData is returned
   * 2. end of file or the given end element is found -> NULL
   * 3. Element is found -> Element
   * 4. some text is found, not whitespace and not a recognized tag -> TemplateData
   * @param parser registered Parser implementation
   * @param endIndent - the expected closing element tag
   * @return  Element
   * @throws JspParseException  if an error occured
   */
  private Element getNextElementType(JspPageInterface parser, char[] endIndent)throws JspParseException {

		Position whiteStartDebugPos = null;
		try {
			if (parser.endReached()) {
				return null;
			}

			int whiteStart = parser.currentPos();
			whiteStartDebugPos = parser.currentDebugPos();
			parser.skipWhiteSpace();
			int whiteEnd = parser.currentPos();

			if (whiteStart != whiteEnd) {
				Position whiteEndDebugPos = parser.currentDebugPos();
				DebugInfo info = new DebugInfo(whiteStartDebugPos, whiteEndDebugPos, parser.currentFileName(), parser.getWebContainerParameters().isJspDebugSupport());
				return new TemplateData(whiteStart, whiteEnd, info).parse(parser);
			}

			if (parser.endReached()) {
				return null;
			}

			int position = parser.currentPos();

			 // check if next chars are the expected end tag
			if( parser.compareEndIndex(position, endIndent)) {
        return null;
      }

			Element found = null;

			for (int i = 0; i < prefixes.length; i++) {
				if (parser.compare(position, position + prefixes[i].length, prefixes[i])) {
					if (found == null) {
						found = allPrefixes.get(String.copyValueOf(prefixes[i]));
						//found = _elements[i];
					} else if (found.indent().length < (allPrefixes.get(String.copyValueOf(prefixes[i]))).indent().length ) {
						found = allPrefixes.get(String.copyValueOf(prefixes[i]));
						//found = _elements[i];
					}
				}
			}
      if( found != null && found instanceof XMLTag && !parser.isXml()) {
        // in JSP pages (standard syntax) XML declaration is simple template text.
        // It should be able to use scriptlet expression in "encoding" attribute.
        // See 0120031469 0003410782 2006
        found = null ;
      }
			if (found != null) {
			  if( found instanceof JspDirective){
			    found = getDirectiveByName(parser);
			  }
				return found;
			}

			if (tagLibPrefixes != null) {
				for (int i = 0; i < tagLibPrefixes.length; i++) {
					if (parser.compare(position, position + tagLibPrefixes[i].length(), tagLibPrefixes[i].toCharArray())) {
						//undefined tag found
						//JSP:SPEC:220.2  Any tag files within a JAR file that are not defined within a TLD must be ignored by the container.
					  StringBuffer name = new StringBuffer();
					  fillCustomTagName(parser, name, tagLibPrefixes[i].length());
					  throw new JspParseException(JspParseException.UNRECOGNIZED_TAG_NAME, new Object[] { name.toString() },
                    parser.currentFileName(), parser.currentDebugPos());
					}
				}
			}

			int pos = position;

			do {
		    //check for undefined custom tag and register as potential translation error
			  checkForCustomUndefinedTag(parser);
				parser.nextChar();
				pos++;
			} while (!parser.compareEndIndex(pos, endIndent) && !hasElementAsNext(parser));

			Position whiteEndDebugPos = parser.currentDebugPos();
			DebugInfo info = new DebugInfo(whiteStartDebugPos, whiteEndDebugPos, parser.currentFileName(), parser.getWebContainerParameters().isJspDebugSupport());
			return new TemplateData(position, parser.currentPos(), info).parse(parser);
		} catch (IndexOutOfBoundsException ex) {
			throw new JspParseException(JspParseException.EXPECTING_CHAR_WHILE_READ_END_OF_FILE, new Object[]{String.copyValueOf(endIndent)},
              parser.currentFileName(), whiteStartDebugPos);
		}
  }

  /**
   * Checks whether current position in the parser is start of new
   * jsp element or not
   *
   * @param   parser  parser to use
   * @return   true if jsp element was recognized
   */
  public final boolean hasElementAsNext(JspPageInterface parser) throws JspParseException {
    //check that checkForCustomUndefinedTag() is used together with this method
    if (parser.endReached()) {
      return true;
    }

    int position = parser.currentPos();


    for (int i = 0; i < prefixes.length; i++) {
      if (parser.compare(position, position + prefixes[i].length, prefixes[i])) {
        return true;
      }
    }

    //searches for undefined in the TLD tags
    if ( tagLibPrefixes != null ){
	    for (int i = 0; i < tagLibPrefixes.length; i++) {
	      if ( parser.compare(position, position + tagLibPrefixes[i].length(), tagLibPrefixes[i].toCharArray())){
	      	return true;
	      }
	    }
    }

    return false;
  }
  /**
   * According Jsp 2.0 EBNF custom action are
   *		CustomAction ::= TagPrefix ’:’ CustomActionName
   *		and custom action declaration should begin with
   * <CustomAction
   *
   *    	JSP.1.10.2 The taglib Directive
   *    It is a fatal translation error for the taglib directive to appear after actions or
   *   	functions using the prefix.
   * @throws JspParseException - if the prefix is the default JSP prefix -"jsp" and because we're in this method , the action is not standard action.
   */
  private void checkForCustomUndefinedTag(JspPageInterface parser) throws JspParseException {
    if (parser.currentChar() != '<') {
      return;
    }
    int beginPos = parser.currentPos(); // includes '<'

    // Parse 'CustomAction' production (tag prefix and custom action name)
    int currPos = beginPos + 1; // skip '<'

    if( parser.getSourceLength() == currPos ){
      //end of JSP found
      return;
    }

    StringBuffer potentialCustomTagBuffer = new StringBuffer();
    if (!isDelimiter(parser.charAt(currPos))) {
      // Read value until delimiter is found:
      do {
        potentialCustomTagBuffer.append((char) parser.charAt(currPos));
        currPos++;
        if( parser.getSourceLength() == currPos ){
          //end of JSP found
          break;
        }
      } while (!isDelimiter(parser.charAt(currPos)));
    }else{
      //this is not a well formatted XML
      // there shouldn't be any space or other delimiter after angular brackets
      return;
    }

    int colonIndex = potentialCustomTagBuffer.indexOf(":");
    if (colonIndex == -1) {
      // potential custom tag not found - retreat
      return ;
    }

    String prefix = potentialCustomTagBuffer.substring(0, colonIndex);

    // Check if this is a user-defined tag.
    Object tagLib = parser.getTaglibs().get(prefix);
    if (tagLib == null) {
      if( SAXTreeBuilder.JSP.equals(prefix) ){
        /*
         * JSP:spec chapter 5
	       * Standard actions are represented using XML elements with a prefix of jsp (though
	       * that prefix can be redefined in the XML syntax). A translation error will result if the
	       * JSP prefix is used for an element that is not a standard action.
         */
        throw new JspParseException(JspParseException.PREFIX_RESERVED, new Object[]{potentialCustomTagBuffer.toString()});
      }
      // Remember the prefix for later error checking
      registerNonCustomTagPrefix(prefix);
    }

  }

	/**
   * Parse utils - Is current character a token delimiter ? Delimiters are
   * currently defined to be =, &gt;, &lt;, ", and ' or any space character
   * as defined by <code>isSpace</code>.
   *
   * @return A boolean.
   */
  private boolean isDelimiter(char theChar) {
    if (theChar == ':' || Character.isJavaIdentifierPart(theChar)) {
      return false;
    }
    return true;
  }


  /**
   * Makes string representation for this object
   *
   * @return newly created string
   */
  public String toString() {
    if (elements == null) {
      return super.toString();
    } else {
      String ret = "\n{ ";

      for (int i = 0; i < elements.length; i++) {
        ret += "\n\t" + tab("" + elements[i].toString()) + "";
      }

      ret += "\n} ";
      return ret;
    }
  }

  /**
   * Adds a tab character in front of each line in the specified
   * string
   *
   * @param   s  string to use as source
   * @return  newly created string
   */
  protected String tab(String s) {
    StringTokenizer st = new StringTokenizer(s, "\n");
    StringBuffer sb = new StringBuffer();
    sb.append(st.nextToken());

    while (st.hasMoreTokens()) {
      sb.append("\n\t");
      sb.append(st.nextToken());
    }

    return sb.toString();
  }

  /**
   * Creates info string form position
   *
   * @param   pos  the position for witch is created info
   * @param   parser  parser to use for the creation of the info
   * @return  formatted info
   */
  protected String formatPosition(Position pos, JspPageInterface parser) {
    String s = parser.currentFileName();
    s = s.replace('\\', File.separatorChar);
    //    s = s.substring(s.lastIndexOf(File.separatorChar)+1);
    return "\r\nError in file:" + s + " at line:" + pos.line + " position:" + pos.linePos;
  }

  /**
   * Formats character into String
   *
   * @param   c  characted to format
   * @return  formatted string
   */
  protected String escape(char c) {
    switch (c) {
      case '\b': {
        return "\\b";
      }
      case '\t': {
        return "\\t";
      }
      case '\n': {
        return "\\n";
      }
      case '\f': {
        return "\\f";
      }
      default: {
        return "" + c;
      }
    }
  }

  /**
   * This method should be implemneted in order to return XML view representation of the parsing element
   * @param br IDcounter to generate unique ID
   * @return String representation of the element for XML view
   * @throws JspParseException
   */
  public abstract String getString(IDCounter br) throws JspParseException;

  /**
   * Utility method for checking given string for runtime value
   * @param expr expression to check
   * @return true if it is runtime expression
   */
  public boolean isRuntimeExpr(String expr) {
    expr = expr.trim();
    return (expr.startsWith("<%=") || expr.startsWith(_default_prefix_tag_start + "expression") || expr.startsWith("%=") || (expr.startsWith("${") && !parser.getIsELIgnored()));
  }

  /**
   * Evaluates runtime expression
   * @param exprOrig expression to be evaluated
   * @return evaluated code
   * @throws JspParseException if an error occured
   */
  public String evaluateExpression(String exprOrig) throws JspParseException {
    if (parser.getJspProperty() != null && parser.getJspProperty().isScriptingInvalid().equalsIgnoreCase("true")) {
      throw new JspParseException(JspParseException.SCRIPTING_ELEMENTS_ARE_NOT_ALLOWED_HERE,
              parser.currentFileName(), parser.currentDebugPos());
    }
    String expr = exprOrig.trim();
    if ((expr.startsWith("<%=") || expr.startsWith(_default_prefix_tag_start + "expression") || expr.startsWith("%=")) && (expr.endsWith("%>") || expr.endsWith("/>") || expr.endsWith("%"))) {
      if (expr.startsWith("<%=")) {
        expr = expr.substring("<%=".length());
      } else if (expr.startsWith(_default_prefix_tag_start + "expression")) {
        expr = expr.substring((_default_prefix_tag_start + "expression").length());
      } else if (expr.startsWith("%=")) {
        expr = expr.substring("%=".length());
      }

      if (expr.endsWith("%>")) {
        expr = expr.substring(0, expr.length() - "%>".length());
      } else if (expr.endsWith("/>")) {
        expr = expr.substring(0, expr.length() - "/>".length());
      } else if (expr.endsWith("%")) {
        expr = expr.substring(0, expr.length() - "%".length());
      }
      return expr;
    } else if (expr.startsWith("${") && expr.endsWith("}")) {
      return InnerExpression.getExpresionEvaluation(expr, java.lang.String.class, parser);
    }

    return exprOrig; //replaceQuotes(expr);
  }

  /**
   * Returns the ID for XMl view
   * @param id IDCounter object
   * @return String representingthe ID for this element
   */
  public String getId(IDCounter id) {
    return " " + _default_prefix.peek() + ":id" + JSP_ID_1 + id.getId() + JSP_ID_2;
  }

  /**
   * Adds the given String in format: "<" + prefix + ":"
   * to the list with registered prefixes for tag libraries for this JSP
   * @param prefix String with prefix chars
   */
  public void registerTagLibPrefix(String prefix){
  	if ( prefix == null ){
  		return;
  	}
  	if ( tagLibPrefixes == null ){
  		tagLibPrefixes = new String[1];
  		tagLibPrefixes[0] = prefix;
  		return;
  	}

  	for (int i = 0; i < tagLibPrefixes.length; i++) {
      String storedprefix = tagLibPrefixes[i];
      if ( prefix.equals(storedprefix)){
      	return;
      }
    }
    String[] prefixesCopy = new String[tagLibPrefixes.length+1];
    System.arraycopy(tagLibPrefixes,0, prefixesCopy, 0, tagLibPrefixes.length);
    prefixesCopy[prefixesCopy.length-1] = prefix;
		tagLibPrefixes = prefixesCopy;
  }

  /**
   * used when ending namespace in JSp documents
   * @param prefix String with prefix chars
   */
  public void removeTagLibPrefix(String prefix) {
    if ( prefix == null ){
      return;
    }
    // removes the given prefix from the tagLibPrefixes
    String[] prefixesCopy = new String[tagLibPrefixes.length-1];
    int prefixCopyIndex = 0;
    for (int i = 0; i < tagLibPrefixes.length; i++) {
      String storedprefix = tagLibPrefixes[i];
      if ( prefix.equals(storedprefix)){
        continue;
      }
      prefixesCopy[prefixCopyIndex++] = storedprefix;
    }
    tagLibPrefixes = prefixesCopy;
  }

  /**
   * adds potential custom tag prefix
   * @param prefix String with prefix chars
   */
  private void registerNonCustomTagPrefix(String prefix){
    if ( notCustomTagPrefix == null ){
      notCustomTagPrefix = new ConcurrentArrayObject();
    }
    if( !notCustomTagPrefix.contains(prefixes) ){
      notCustomTagPrefix.add(prefix);
    }
  }

  /**
   * Used in SAX parser to get the element for given prefix
   * @param indent String with prefix chars
   * @return Element corresponding to this prefix
   */
  public Element getRegisteredElement(String indent) {
   Element found = allPrefixes.get(indent);
    if (found == null) {
      found =  allPrefixes.get("<jsp:" + indent);
      if (found == null) {
        found = allPrefixes.get("<jsp:" + indent + END);
        if (found == null) {
          return null;
        }
      }
    }
    try {
      // only one instance of RootTag should exists
      if (found instanceof RootTag) {
        return found;
      }
      return createNewElement(found);
    } catch (JspParseException e) {
      if (LogContext.getLocationJspParser().beError()) {
				LogContext.getLocation(LogContext.LOCATION_JSP_PARSER).traceError("ASJ.web.000589", "getRegisteredElement ERROR {0}", new String[] { e.toString() }, null, null);
			}
			return null;
    }
  }

  /**
 	 * creates new Element of the class of the given parameter
 	 * default constructor is used
 	 * @param elementType The type of Element to be created
 	 * @return Element object for the given type
 	 * @throws JspParseException
 	 */
  private Element createNewElement(Element elementType) throws JspParseException {
		Class elementTypeClass = elementType.getClass();
		Element nextElement = null;
		try{
			nextElement = (Element)elementTypeClass.newInstance();
		}catch (Exception e){
			throw new JspParseException(JspParseException.CREATING_NEW_ELEMENT_FAILED, new Object[]{elementType.getClass().getName()}, e);
		}
		return nextElement;
  }

  /**
   * Each element must implement this method in order to parse the code for give syntax
   * Used only in JSP syntax
   * @param parser JspPageInterface implementation
   * @return Element
   * @throws JspParseException  if an error occured
   */
  public abstract Element parse(JspPageInterface parser) throws JspParseException;

	/**
	 * seeks in the list of declared custom actions which do not have associated taglibrary
	 * @param prefix String with prefix chars
	 * @return - true if custom tag with this prefix has been already used
	 */
	public boolean isAlreadyUsedCustomTag(String prefix){
	  if( notCustomTagPrefix != null ){
	    return notCustomTagPrefix.contains(prefix);
	  }
	  //else
	  return false;
	}

	/**
	 * Fills in the given buffer the text from the ( current position   of parsing + given offset) till the end of the valid java identifier.
   * @param parser JspPageInterface implementation
	 * @param name - the buffer where the found custom tag name will be added
	 * @param tagLibPrefixLength - offset
	 * @return
	 */
	private final void fillCustomTagName(JspPageInterface parser,StringBuffer name, int tagLibPrefixLength){
	  if( name == null){
	    return ;
	  }

    int beginPoss = parser.currentPos() + tagLibPrefixLength;
    int endPos = beginPoss;

    while (Character.isJavaIdentifierPart(parser.charAt(endPos))) {
      if (parser.endReached()) {
        break;
      }
      endPos++;
    }
    name.append(parser.getChars(beginPoss, endPos));
	}

  /**
   * Constructs new object that derives from this class
   * and calls his parse method.The type of the constructed
   * object depends on what is the name of the directive
   * that is found as next token.
   *
   * @param   parser  parser to use
   * @return  newly created element
   * @exception   JspParseException  thrown if error occurs
   * during parsing or verification
   */
  public Element getDirectiveByName(JspPageInterface parser) throws JspParseException {
    int startpos = parser.currentPos() + 3;

    while (isWhiteSpace(parser.charAt(startpos))) {
      startpos++;
    }

    int endpos = startpos;

    while (Character.isLetter(parser.charAt(endpos))) {
      endpos++;
    }

    String directiveName = parser.getChars(startpos, endpos);
    JspDirective directive = directives.get(directiveName);
    if( directive == null ){
      throw new JspParseException(JspParseException.UNRECOGNIZED_DIRECTIVE_NAME, new Object[]{parser.getChars(startpos, endpos)},
          parser.currentFileName(), parser.currentDebugPos());
    }
    directive.verifyDirective(parser);
    return directive;
  }

  /**
   * Returns directive object corresponding to given name.
   * @param directiveName  The name of directive
   * @return JspDirective implementation
   * @throws JspParseException if an error occured
   */
  public JspDirective getDirectiveByName(String directiveName) throws JspParseException {
    JspDirective directive = directives.get(directiveName);
    if( directive == null ){
      throw new JspParseException(JspParseException.UNRECOGNIZED_DIRECTIVE_NAME, new Object[]{directiveName});
    }
    return directive;
  }

  /**
   * Returns JspTaglibDirective implementation, registered by registerParsingElement(Element element).
   * It is needed in SAXTreeBuilder in order to invoke registerTaglib() method for any implementation of this element.
   * @return  JspTaglibDirective
   * @throws JspParseException if an error occured
   */
  public JspTaglibDirective getJspTaglibDirective() throws JspParseException {
    JspDirective directive = directives.get(JspDirective.DIRECTIVE_NAME_TAGLIB);
    if( directive == null ){
      throw new JspParseException(JspParseException.UNRECOGNIZED_DIRECTIVE_NAME, new Object[]{JspDirective.DIRECTIVE_NAME_TAGLIB});
    }
    return (JspTaglibDirective)directive;
  }
}


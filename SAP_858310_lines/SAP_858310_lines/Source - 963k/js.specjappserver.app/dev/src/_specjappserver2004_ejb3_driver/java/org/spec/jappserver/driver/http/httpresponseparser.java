/*
 * Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *
 * This source code is provided as is, without any express or implied warranty.
 *
 *  History:
 *  Date        ID, Company               Description
 *  ----------  ------------------------  ------------------------------------------------------------------------
 *  2003/07     Ning Sun, SUN             Created
 *  2003/08     Ning Sun, SUN             Modified to correct bugs in response parsing.
 *  2004/06     Ning Sun, SUN             Fixed problem with multiple cookie support on SAP WebAS.
 *                                        See osgjava-7221 and osgjava-7228.
 *  2004/12     Ning Sun, SUN             Bug fix to support case insensitive HTTP headers (osgjava-7764,7738)
 *  2004/12     Ning Sun, SUN             Bug fix to allow extra spaces in the chunk size (osgjava-7770).
 *
 */
package org.spec.jappserver.driver.http;

import java.io.IOException;
import java.util.Vector;

public class HttpResponseParser {

  private static final byte[] HTTP1_L =            {'h', 't', 't', 'p', '/', '1', '.'};
  private static final byte[] HTTP1_U =            {'H', 'T', 'T', 'P', '/', '1', '.'};
  private static final byte[] CONTENT_LENGTH_L =    {'c','o','n','t','e','n','t','-','l','e','n','g','t','h',':',' '};
  private static final byte[] CONTENT_LENGTH_U =    {'C','O','N','T','E','N','T','-','L','E','N','G','T','H',':',' '};
  private static final byte[] CONNECTION_CLOSE_L =  {'c','o','n','n','e','c','t','i','o','n',':',' ','c','l','o','s','e'};
  private static final byte[] CONNECTION_CLOSE_U =  {'C','O','N','N','E','C','T','I','O','N',':',' ','C','L','O','S','E'};
  private static final byte[] CONNECTION_KA_L =     {'c','o','n','n','e','c','t','i','o','n',':',' ','k','e','e','p','-','a','l','i','v','e'};
  private static final byte[] CONNECTION_KA_U =     {'C','O','N','N','E','C','T','I','O','N',':',' ','K','E','E','P','-','A','L','I','V','E'};
  private static final byte[] TRANSFER_ENCODING_L = {'t','r','a','n','s','f','e','r','-','e','n','c','o','d','i','n','g',':',' ','c','h','u','n','k','e','d'};
  private static final byte[] TRANSFER_ENCODING_U = {'T','R','A','N','S','F','E','R','-','E','N','C','O','D','I','N','G',':',' ','C','H','U','N','K','E','D'};
  private static final byte[] SET_COOKIE_L =        {'s','e','t','-','c','o','o','k','i','e',':',' '};
  private static final byte[] SET_COOKIE_U =        {'S','E','T','-','C','O','O','K','I','E',':',' '};
  private static final byte[] JSESSIONID_L =        {'j','s','e','s','s','i','o','n','i','d','='};
  private static final byte[] JSESSIONID_U =        {'J','S','E','S','S','I','O','N','I','D','='};

  private boolean debugging = false;

  public byte[]buf;
  public int buf_len; // valid data
  public int headerEnd=-1;
  public int contentLen=-1;
  public boolean toBeClosed;
  public boolean chunked;
  public int chunkEnd = -1; 
  public int protocol = -1; 
  public int status;

  public HttpResponseParser() {}

  public void parseHeader (CookieStrings cookies) {
    int i, j, k;
    int start=0;
    int end=-1;
    int headerStart = -1;
    byte b;
    int cookieNum = 1; // start from index 1 for regular cookie,
                       // idex of 0 is for JSESSIONID cookie

    // reset everything...
    headerEnd = -1;
    chunkEnd = -1;
    contentLen = -1;
    protocol = -1;
    toBeClosed = false;
    chunked = false;

    // cookies.num = 0;

    // assume callers that update buf[] also modify the buf_len accordingly

    i = 0;

first_loop:
    for (; i < buf_len - 12; i++) {

        // skip everything that's before HTTP/1.x line

        if ( buf[i] != 'H' && buf[i] != 'h' ) continue;
        i++;

        // HTTP/1.0 or HTTP/1.1
        for (j=1; j<7; j++) {
            if (buf[i] != HTTP1_L[j] && buf[i] != HTTP1_U[j]) {
                continue first_loop ;
            }    
            i++;
        }

        b = buf[i];
        if (b == '0') {
            toBeClosed = true; // HTTP/1.0 needs KA header to keep conn open
            protocol = 10;
        }
        else if  (b == '1') {
            toBeClosed = false;
            protocol = 11;
        }
        else { 
            // this is not the status line, search again
            continue; // this is not the start of the header search again
        }

        // now try to get status 
        // now at end of HTTP/1.x
        i++;

        if (buf[i] != ' ') continue;
        i++;

        status = 0;
	    for (j = 0; j < 3; j++) {
	        b = buf[i];
		    if (b >= '0' && b <= '9') {
		        status *= 10;
		        status += b - '0';
                i++;
            }
            else {
                continue first_loop;
            }
        }
   
        if (buf[i] != ' ') continue;
        headerStart = i - 12;
              
    }

    if (debugging)
        System.out.println ("headerStart=" + headerStart + "buf_len" + buf_len);
    if (headerStart > 0) {
        // shift header over
        for (j = 0, k = headerStart; k < buf_len; j++, k++) {
            buf[j] = buf[k];
        }
        buf_len -= headerStart;
    } else if (headerStart < 0) {
        buf_len = 0; // entire buffer is searched but couldn't find valid status line.
        return;
    }


    if (headerStart < 0) return;

main_loop:
    for (i=13; i < buf_len; i++) {
        b = buf[i];
        // Set-Cookie:
        if (b == 'S' || b == 's') {
            i ++;
            for (j = 1; j < 12; j ++, i++) {
                if (buf[i] != SET_COOKIE_L[j] && buf[i] != SET_COOKIE_U[j]) {
                    i--;
                    continue main_loop;
                }
            }

            start = i;

            //look for JSESSIONID cookie
            boolean jsessionIdCookie = false;
            if ( (buf[i] == 'J' || buf[i] == 'j') && i + 11 < buf_len) {
                jsessionIdCookie = true;
                i++;
                for (j = 1; j < 11; j++, i++) {
                   if (buf[i] != JSESSIONID_L[j] && buf[i] != JSESSIONID_U[j])
                        jsessionIdCookie = false;
                        break;
                }
            }

            // search for new line or ';'
            for ( ; i < buf_len - 1; i ++) {

                if (buf[i] == ';' || (buf[i] == '\r' && buf[i + 1] == '\n')) {
                    break;
                }
            }

            try {
                if (jsessionIdCookie)
                    cookies.entries[0] = new String ( buf, start, i-start, "ISO-8859-1");
                else {
                    cookies.entries[cookieNum] = new String ( buf, start, i-start, "ISO-8859-1");
                    // System.out.println("got a cookie: [" + cookies.entries[cookieNum]+"]");
                    cookieNum ++;
                }

            } catch (java.io.UnsupportedEncodingException e) {
                System.out.println ("cookie error");
                e.printStackTrace ();
            }
            i --;
        }

        // Content-Length: , Connection: Close, Connection: Keep-Alive
        else if (b == 'C' || b== 'c') {
            if ((end = i + 16) < buf_len) {
                b = buf[end];
                if (b >= '0' && b <= '9') {
                    i++;
                    for (j = 1; j < 16; j++, i++) {
                        if (buf[i] != CONTENT_LENGTH_L[j] && buf[i] != CONTENT_LENGTH_U[j]) {
                            i--;
                            continue main_loop;
                        }
                    }
                    contentLen = b - '0';
                    while (++i < buf_len) {
                        b = buf[i];
                        if (b >= '0' && b <= '9') {
                            contentLen *= 10;
                            contentLen += b - '0';
                        } else if (b == '\r' && i < buf_len - 1 && buf[i + 1] == '\n') {
                            i--;
                            break;
                        } else {
                            i--;
                            contentLen = -1;
                            break;
                        }
                    }
                } else if (b == 'e' || b == 'E') {
                    i++;
                    for (j = 1; j < 17; j++, i++) {
                        if (buf[i] != CONNECTION_CLOSE_L[j] && buf[i] != CONNECTION_CLOSE_U[j]) {
                            i--;
                            continue main_loop;
                        }
                    }
                    toBeClosed = true;
                    i--;
                } else if (b == '-') {
                    i++;
                    for (j = 1; j < 21; j++, i++) {
                        if (buf[i] != CONNECTION_KA_L[j] && buf[i] != CONNECTION_KA_U[j]) {
                            i--;
                            continue main_loop;
                        }
                    }
                    toBeClosed = false;
                    i--;
                }
            }
        }

        // Transfer-Encoding: Chunked
        else if (b == 'T' || b == 't') {
            i++;
            for (j=1; j<25; j++, i++) {
                if (buf[i] != TRANSFER_ENCODING_L[j] && buf[i] != TRANSFER_ENCODING_U[j]) {
                    i--;
                    continue main_loop;
                }
            }
            chunked = true;
            i--;
        } else if (b == '\r' && i + 4 <= buf_len) {
            if (buf[i + 1] == '\n' && buf[i + 2] == '\r' && buf[i + 3] == '\n') {
                headerEnd = i + 4;
                break;
            }
        }
    }


    if (cookieNum > 1) cookies.num = cookieNum;

    return;
  }

  /**
   * This is the method to process Chunk-Encoded response buffer.
   * It looks for all the chunk size fields in the buffer and
   * calculate how much more data to read to fulfill the chunk size.
   * An overflow buffer is used to store any possible data after
   * the chunksize 0 field and 2 CRLF characters (i.e.end of a response).
   * @param overflowBuf to store any possible data after a complete response
   * @return length of the overflowed data
   *     -4  other error
   *     -3  format error
   *     -2  chunkEnd >  buf_len  => need to read more data to finish last chunk
   *     -1  chunkEnd <= buf_len  => need to read size for next chunk
   *     >=0 chunkEnd = buf_len and chunkSize = 0  => got a complete HTTP response
   */
  public int processChunk (byte[] overflowBuf) {
      // assume callers that update buf[] also modify the buf_len accordingly

      int i;
      int j;

      if (debugging)
          debugBuf (buf, 0, buf_len, "calling processChunk BUFFER:");

      if (chunked == false) return -4;

      if (chunkEnd == -1) {
          chunkEnd = headerEnd;
      }

      if (chunkEnd >= buf_len)
          return -2; // need to read data

      // now we are sure chunkEnd < buf_len, i.e. to parse the size of next chunk
      // starting from chunkEnd

      int chunkSize=0;
      byte b;

      int skippedI = chunkEnd;   // index used for skipping the size field
      int toCopy=0;
      int numOfBytes = 0;

      boolean fullSizeFieldRead = false;
      int sizeFieldStart=0;
      int sizeFieldEnd=0;

      i = chunkEnd;  // iterator through buf_len

      while ( i < buf_len ) {

          chunkSize = 0;
          numOfBytes = 0;
          fullSizeFieldRead = false;

          sizeFieldStart = i;
          sizeFieldEnd = sizeFieldStart;

          // get the chunk size
          while (i < buf_len) {
              b = buf[i];

              if ( b == '\n' || (b == '\r' && i < buf_len - 1 && buf[i+1] == '\n')) {

                  if ( b== '\n') i++;
                  else i += 2;

                  // size field is complete, update where the next chunk ends

                  if (numOfBytes == 0) {
                      sizeFieldStart = i;
                      continue;
                  }

                  if (debugging) {
                      debugInfo (true, "got a valid chunkSize", i,  chunkSize,
                              chunkEnd, headerEnd, numOfBytes, buf_len,
                              sizeFieldStart, sizeFieldEnd, skippedI);
                      debugBuf (buf, chunkEnd, i-chunkEnd, "chunkField: ");
                  }

                  fullSizeFieldRead = true;
                  sizeFieldEnd = i;
                  chunkEnd += chunkSize;  // chunkSize

                  break;
              }
              // calculate size
              if ( b >= 'a' && b <= 'f') {
                  chunkSize *= 16;
                  chunkSize += b - 87;   // 87 = 'a' - 10
                  numOfBytes ++;
              }
              else if (b >= '0' && b <= '9' ) {
                  chunkSize *= 16;
                  chunkSize += b - 48;  // 48 = '0'
                  numOfBytes ++;

              } else if (b == ';') {
                  // ignore the rest text till CRLF
                  while (++i < buf_len && buf[i] != '\r');
              } else if (b == ' ') {
                  // ignore all other " " or "\t" till CRLF
                  while (++i < buf_len && (buf[i] == ' ' || buf[i] == '\t'));
                  if (i >= buf_len || buf[i] != '\r') {
                      System.out.println("wrong format in chunk size field b=" + b);
                      if (debugging){
                          debugBuf (buf, i, buf_len-i, "nonspace character found between blank spaces and CRLF in chunk size field b=" + b);
                          debugBuf (buf, 0, buf_len, "wrong format in chunk size - THE BUFFER ");
                      }
                      toBeClosed = true; // something is wrong, need to close the connection
                      return -3;
                  }
              } else if (b != '\r') {

                  System.out.println("wrong format in chunk size field b=" + b);
                  if (debugging){
                      debugBuf (buf, i, buf_len-i, "wrong format in chunk size field b=" + b);
                      debugBuf (buf, 0, buf_len, "wrong format in chunk size - THE BUFFER");
                  }
                  toBeClosed = true; // something is wrong, need to close the connection
                  return -3;
              }
              i++;
          }


          if (!fullSizeFieldRead)  {
              // chunk size field is incomplete

              if (debugging){
                  debugInfo (false, "need more data to get chunkSize", i,  chunkSize,
                          chunkEnd, headerEnd, numOfBytes, buf_len, sizeFieldStart,
                          sizeFieldEnd, skippedI);
                  debugBuf (buf, sizeFieldStart, buf_len-sizeFieldStart, "incomplete size field");
                  debugBuf (buf, 0, buf_len, "before copy incomplete size field - THE BUFFER");
              }
              if ( skippedI < sizeFieldStart ) {
                  b = -1;
                  for (j = sizeFieldStart; j < i; ) {
                      b = buf[j++];
                      buf[skippedI++] = b;
                  }
                  // System.out.println("last byte=" + b);
                  if (b == '\r') skippedI --;
                  buf_len = skippedI; // update the valid data length
              } else {
                  buf_len = i;
              }

              if (debugging) {
                  debugInfo (false, "before return -1 for more size", i,  chunkSize,
                          chunkEnd, headerEnd, numOfBytes, buf_len, sizeFieldStart,
                          sizeFieldEnd, skippedI);
                  debugBuf (buf, 0, buf_len, "after copy incomplete size field - THE BUFFER");
              }

              return -1; // not done yet, need more data
          }

          // size is 0
          if (chunkSize == 0) {

              // put extra bytes into the overflow buffer
              if (debugging)
                  System.out.println("i=" + i + " buf_len=" + buf_len + " chunkEnd=" + chunkEnd );

              if (i < buf_len -1 && buf[i] == '\r' && buf[i+1] == '\n') i += 2;
              for (j = 0; i < buf_len; i++, j ++) {
                  overflowBuf[j] = buf[i];
              }
              buf_len = skippedI; // update the valid data length
              contentLen = buf_len - headerEnd;

              if (debugging) {
                  System.out.println("done: contentLen=" + contentLen + " overflow=" + j );
                  debugInfo (false, "before return j with overflow buffer", i,  chunkSize,
                          chunkEnd, headerEnd, numOfBytes, buf_len, sizeFieldStart,
                          sizeFieldEnd, skippedI);
              }
              return j; // done, return size of the over flow data
          }

          // shift left to skip the size field
          toCopy = i + chunkSize;
          if (toCopy > buf_len) {
              toCopy = buf_len;
          }

          if (debugging) {
              debugInfo (false, "before shifting from i to toCopy=" + toCopy, i,  chunkSize, chunkEnd, headerEnd, numOfBytes, buf_len, sizeFieldStart, sizeFieldEnd, skippedI);
              debugBuf (buf, i, toCopy-i,"shifting bytes:");
              debugBuf (buf, 0, buf_len, "before shifting: BUFFER skippedI=" + skippedI);
          }
          while (i < toCopy) {
              buf[skippedI++] = buf[i++];
          }

          if (debugging) {
              debugInfo (false, "after shifting", i,  chunkSize,
                  chunkEnd, headerEnd, numOfBytes, buf_len, sizeFieldStart,
                  sizeFieldEnd, skippedI);
              debugBuf (buf, 0, buf_len, "after shifting: BUFFER skippedI=" + skippedI);
          }
      }

      buf_len = skippedI; // update the valid data length

      if (debugging) {
          debugInfo (false, "before return -2 for more data", i,  chunkSize,
              chunkEnd, headerEnd, numOfBytes, buf_len, sizeFieldStart,
              sizeFieldEnd, skippedI);
          debugBuf (buf, 0, buf_len, "before turn -1 for more data BUFFER skippedI=" + skippedI);
      }

      // need more data.
      return -2;  // not done  yet need more data
  }

  public String[] getData () throws IOException{

    int i, start, totLen;
    String line;
    Vector dataLines = new Vector();

    i = headerEnd;
    start = headerEnd;

    totLen = headerEnd + contentLen;

    int j = 0;
    while (i < totLen) {
        if (buf[i++] != '\n' ) continue; 
            try {         
                line = new String(buf, start, i-start, "ISO-8859-1");
                dataLines.addElement(line); 
                start = i; 
                // System.out.println ("j " + j + ":" + line);
                j++;
            } catch (java.io.UnsupportedEncodingException e) {
                throw new IOException ("getData: wrong response" + e);
            }
   
        
    }

    line = new String(buf, start, i-start, "ISO-8859-1");
    dataLines.addElement(line); 
    // System.out.println ("j " + j + ":" + line);
    j++;

    // System.out.println ("HttpResponseParser:getData - j " + j);

    String[] data = new String[j]; 

    for (i = 0; i < j; i++) {
        data[i] =  (String)dataLines.elementAt(i);
    }

    return data; 

  }

  public String getNextLineContaining(String str, int[] startIndex) {

      // find the first occurance of s
    int i;                  // index into buf
    int j;                  // index into s
    int k;
    char[] s = str.toCharArray();
    int max_j = s.length;
    int max_i = headerEnd+contentLen-max_j;

    int start;
    String line;

    char b = s[0];
    i = startIndex[0];
start:
    for (; i < max_i; i++) {
        if (buf[i] == b) {
            k = i;
            for (j = 0; j < max_j; ) {
                if (buf[k++] != s[j++]) {
                    i++ ;
                    continue start;
                }
            }

            // found it, now search for "\n"
            start = i; 
            while (i < max_i) {
                if (buf[i++] != '\n' ) continue; 
                else break;
            }

            try {
                line = new String(buf, start, i-start, "ISO-8859-1");
                startIndex[0] = i;
                return line;
            } catch (java.io.UnsupportedEncodingException e) {
                System.out.println ("getData: wrong response" + e);
            }

        }
    }
    // found nothing
    return null;

  }

  public int grepForError(String str, int startIndex) {

      // find the first occurance of s
    int i;                  // index into buf
    int j;                  // index into s
    int k;
    char[] s = str.toCharArray();
    int max_j = s.length;
    int max_i = headerEnd+contentLen-max_j;

    char b = s[0];
    i = startIndex;
start:    
    for (; i < max_i; i++) {
        if (buf[i] == b) {
            k = i;
            for (j = 0; j < max_j; ) {
                if (buf[k++] != s[j++]) {
                    i++ ;
                    continue start;
                }
            }
            // found one
            return 1;
        }
    }
    return 0;
  }
  
  
  /**
   * find a target string in the response 
   * @returns 1 : found string 0: not found 
   **/
  public int grepForString(String str, int startIndex) {

      // find the first occurance of s
    int i;                  // index into buf
    int j;                  // index into s
    int k;
    char[] s = str.toCharArray();
    int max_j = s.length;
    int max_i = headerEnd+contentLen-max_j;

    char b = s[0];
    i = startIndex;
start:    
    for (; i < max_i; i++) {
        if (buf[i] == b) {
            k = i;
            for (j = 0; j < max_j; ) {
                if (buf[k++] != s[j++]) {
                    i++ ;
                    continue start;
                }
            }
            // found one
            return 1;
        }
    }
    return 0;
  }
  

  public String toString() {

      String s = null;
      if (buf_len <= 0 || buf == null) System.out.println("negative len");
      else {
          try {
              s =  new String (buf, 0, buf_len, "ISO-8859-1");
          } catch (java.io.UnsupportedEncodingException e) {
              System.out.println ("chunkField wrong");
          }
      }
      return s;
  }

  private void debugBuf (byte[] buf, int start, int len, String s) {

      if (len <= 0 || buf == null) System.out.println("negative len");
      else {
                try {
                    System.out.println(s + "from " + start + " for" + len + " bytes\n["+
                            new String (buf, start, len, "ISO-8859-1") + "]buf.length=" + buf.length);
                } catch (java.io.UnsupportedEncodingException e) {
                        System.out.println ("chunkField wrong");
                }
      }
  }

  private void debugInfo (boolean printHeader, String notes, int a1, int a2, int a3, int a4,
                          int a5, int a6, int a7, int a8, int a9 ) {

      if (printHeader)
          System.out.println ("i     cSize    cEnd   hEnd  nOBytes  buf_len  fStart   fEnd   skippedI");
      
      System.out.println ( a1 + "\t" + a2 + "\t" +  a2 + "\t" + a3 + "\t" + a4
              + "\t"+ a5 + "\t" + a6 + '\t' + a7 + "\t" + a8 + '\t' + a9 +"\t" + notes);

  }
}


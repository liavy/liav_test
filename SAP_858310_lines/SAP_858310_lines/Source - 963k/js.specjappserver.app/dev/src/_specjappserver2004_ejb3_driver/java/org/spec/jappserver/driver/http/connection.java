/*
 * Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *
 * This source code is provided as is, without any express or implied warranty.
 *
 *  History:
 *  Date        ID, Company               Description
 *  ----------  ------------------------  ----------------------------------------------------------------
 *  2003/07     Ning Sun, SUN             Created
 *  2003/08     Ning Sun, SUN             Modified to correct bugs in response parsing.
 *  2003/10     Tom Daly, SUN             Added methods to track response times
 *                                        added String parameter to close() to identify where
 *                                        close() is called from.
 *  2004/02     John Stecher, IBM         Updated to not printout info when using non-KA connections
 *  2004/12     Samuel Kounev, Darmstadt  Changed to increase HEADERSIZE to 1024 (osgjava-7755,7817,7820).
 */
package org.spec.jappserver.driver.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.BindException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import org.spec.jappserver.driver.Timer;

public class Connection {
    
    public String hostServer;
    public int port;
    private Socket sessionSock;
    protected InputStream inStream;
    protected OutputStream outStream;

    private boolean debugging = false;

    private Timer timer;
    private int startTime = 0;
    private int endTime = 0;
    
    private static final int READ_BUFSIZE = 204800;
    private static final int REQ_BUFSIZE = 10240;
    private static final int HEADERSIZE = 1024;
    private static final int SIZEFIELD_LEN = 6;
    private static final int NON_BLOCKING_READ_TIMER = 200;
    private static final int RECONNECTCNT = 2;
    
    
    public byte[] reqBuf;
    public byte[] resBuf;
    public CookieStrings cookies;
    boolean  closed;
    
    public byte[] overflowBuf;
    int  overflowSize;
    int reqLen;
    
    /** Creates new Connection */
    
    public Connection(String hostServer, int port, Timer t) {
        
        this.hostServer = hostServer;
        this.port = port;
        this.resBuf = new byte[READ_BUFSIZE];
        this.reqBuf = new byte[REQ_BUFSIZE];
        this.reqLen = 0;
        this.timer = t ;
        this.cookies = new CookieStrings();
        closed = true;
        
        this.overflowBuf = new byte[HEADERSIZE];
    };
    
    public void connect(){
        
        overflowSize = 0;

        for (int i = 0; i < RECONNECTCNT; i++) {
            try {
                this.sessionSock = new Socket(hostServer, port);
                
                sessionSock.setSoTimeout(0);
                sessionSock.setTcpNoDelay(true);
                sessionSock.setKeepAlive(true);
                
                this.inStream = sessionSock.getInputStream();
                this.outStream = sessionSock.getOutputStream();

                closed = false;
                break;
            } catch (UnknownHostException e) {     // could not find server by name provided
                System.out.println("Error: Connection: UnknownHostException: " + hostServer);
                // e.printStackTrace();
            } catch (IllegalArgumentException e) { // likely port-out-of-range exception
                System.out.println("Error: Connection: IllegalArgumentException: " + e);
                // e.printStackTrace();
            } catch (BindException e) {
                System.out.println("Error: Connection: BindException: " + e);
                // e.printStackTrace();
            } catch (IOException e) {
                System.out.println("Error: Connection: IOException: " + e);
                // e.printStackTrace();
            }
        }
    }
    
    /** Sends request string to the web server.
     * @param reqLen
     * @throws IOException
     */
    protected void sendRequest(int reqLen) throws IOException {
        
        resetTimers();
        setStartTime();  //start response timer, implementing first send to last read measurement
        
        this.reqLen = reqLen;
        if (closed == true)  {
            if (debugging){
                System.out.println("Connection:sendRequest: closed - so reconnect");
            }
            connect();
        }
        
        if ( !sessionSock.isConnected()) {
            System.out.println("Connection:sendRequest: not connected - so close & reconnect");
            close("sessionSock.isConnected is false " );
            connect();
            
        }
        
        if ( sessionSock.isInputShutdown() || sessionSock.isOutputShutdown()) {
            System.out.println("Connection:sendRequest: in/out shutdown - so close & reconnect");
            close("input and output shutdown");
            connect();
            
        }
        if (reqBuf == null || reqLen == 0) {
            throw new IOException("sendRequest: null request buffer");
        } else {
        
            for (int i = 0; i < RECONNECTCNT; i++) {
                try {
                    outStream.write(reqBuf, 0, reqLen);
                    outStream.flush();
                    break;
                } catch (IOException e) {
                    System.out.println("Connection:sendRequest: write exception, retry ...");
                    // e.printStackTrace();
                    close("closed because write on connection failed" );
                    connect();
                }
            }
        }

    }
    
    public HttpResponseData readResponse()
    throws IOException {
        
        HttpResponseParser response = new HttpResponseParser();
        int iErr = -2;
        int toRead;;
        int newLen;
        int expectedLen;
        
        if ( !sessionSock.isConnected()) {
            System.out.println("Connection:readResponse: not connected - so close");
            close("response parser - not connected");
            setEndTime();
            return null;
        }
        
        if ( sessionSock.isInputShutdown() || sessionSock.isOutputShutdown()) {
            System.out.println("Connection:readResponse: in/out shutdown - so close");
            close("response parser - input and output shutdown");
            setEndTime();
            return null;
        }
        
        response.buf = resBuf;
        response.buf_len = 0;
        
        response.headerEnd = -1;
        
        // cp the overflow buffer
        if (overflowSize > 0) {
            if (debugging)
                System.out.println("overflow......");
            System.arraycopy(overflowBuf, 0, response.buf, 0, overflowSize);
            response.buf_len = overflowSize ;
            overflowSize = 0; // reset overflow buffer
        }

        if (debugging)
            debugResponseBuf (response, "overflow buffer", false);
        
        // get the header
        while (response.headerEnd < 0) {
            try {
                iErr = -2;
                iErr = inStream.read(response.buf, response.buf_len, HEADERSIZE - response.buf_len);
            } catch (SocketException e) {
                // retry
                if( debugging){
                    System.out.println("SocketException: read failed while trying to get HTTP header");
                }
                e.printStackTrace();
                closed = true;
                setEndTime();
                return null;
            } catch (IOException e) {
                if (debugging)
                    debugResponseBuf(response, "read header 2", false);
                System.out.println("IOException: read failed while trying to get HTTP header");
                e.printStackTrace();
                closed = true;
                setEndTime();
                return null;
            }
            
            if (iErr > 0) {
                response.buf_len += iErr;
                response.parseHeader(cookies) ;
            } else if (iErr == -1) {
                if(debugging) {
                    System.out.println("unexpected End of Stream, read() returned -1 while trying to get HTTP header."
                        + "Request will be retried. To avoid this error, increase web server keepalive timer");
                }
                closed = true;
                setEndTime();
                return null;
            }
            
        }

        // System.out.println("Connection: cookie num=" + cookies.num);

        if (debugging)
            debugResponseBuf (response, "second header read", false);

        // now we got the header
        
        if (response.contentLen >= 0 ) { // case 0: got content-length
            
            expectedLen = response.contentLen + response.headerEnd;
            
            if ( resBuf.length < expectedLen) {
                int bufLen = expectedLen;
                byte[] tmpByteArray = new byte[bufLen];
                // Copy valid data into new read buffer
                System.arraycopy(resBuf, 0, tmpByteArray, 0, response.buf_len);
                response.buf = tmpByteArray;
            }
            
            //there's still more
            toRead = expectedLen - response.buf_len;
            while (toRead > 0) {
                iErr = -2;
                try {
                    iErr = inStream.read(response.buf, response.buf_len, toRead);
                } catch (SocketException e) {
                    // retry
                    System.out.println("SocketException: read failed with content-length, will retry");
                    // e.printStackTrace();
                    closed = true;
                    setEndTime();
                    return null;
                } catch (IOException e) {
                    System.out.println("IOException: read failed with content-length, will retry");
                    // e.printStackTrace();
                    closed = true;
                    setEndTime();
                    return null;
                }
                
                if (iErr > 0) {
                    toRead -= iErr;
                    response.buf_len += iErr;
                } else if (iErr == -1) {
                    System.out.println("unexpected End of Stream, read() returned -1 with content-length, will retry");
                    setEndTime();
                    return null;
                }
                
            }
            
        } else {   // no content-length
            
            if (response.chunked == true) { // case 1: server encoding chunks

                if (debugging)
                    debugResponseBuf(response, "chunked - before !!!", true);
                while ( (overflowSize = response.processChunk(overflowBuf) ) < 0 ) {
                    toRead = 0;
                    // more chunk processing is needed
                    
                    if (overflowSize == -2) {
                        // need more data to fulfill the last chunk + CRLF
                        toRead = response.chunkEnd - response.buf_len + 2 + SIZEFIELD_LEN;
                    } else if (overflowSize == -1){
                        // need more data to get the size field
                        toRead = SIZEFIELD_LEN;
                    } else if (overflowSize <= -3) {
                        close("overflowSize error");
                        setEndTime();
                        return null;
                    }
                    // allocate new buffer if necessary
                    expectedLen = toRead + response.buf_len;
                    newLen = response.buf.length;
                    while (expectedLen > newLen) {
                        newLen *= 2;
                    }
                    if (newLen > response.buf.length) {
                        byte[] tmpByteArray = new byte[newLen];
                        // Copy partial msg into new read buffer
                        System.arraycopy(resBuf, 0, tmpByteArray, 0, response.buf_len);
                        response.buf = tmpByteArray;
                    }
                    
                    while (toRead > 0) {
                        try {
                            iErr = inStream.read(response.buf, response.buf_len, toRead);
                        } catch (SocketException e) {
                            // retry
                            System.out.println("SocketException: read failed inside chunk processing loop");
                            // e.printStackTrace();
                            closed = true;
                            setEndTime();
                            return null;
                        } catch (IOException e) {
                            System.out.println("IOException: read failed inside chunk processing loop");
                            // e.printStackTrace();
                            closed = true;
                            setEndTime();
                            return null;
                        }
                        if (iErr < 0) {
                            System.out.println("read returned " + iErr + "bytes inside chunk processing loop");
                            if (debugging)
                                debugResponseBuf(response, ("read error inside chunk processing loop, need "
                                    + toRead + " more bytes"), true);
                            closed = true;
                            setEndTime();
                            return null;
                        }
                        response.buf_len += iErr;
                        toRead -= iErr;
                        if (response.buf_len>= response.chunkEnd) break; // to avoid over read to the last chunk
                    }

                    if (debugging)
                        debugResponseBuf(response, "af processChunk & read!!!", true);
                }
                
                // done with an overflow buffer (size >=0);
            } else if (response.toBeClosed == true) { // case 2: server closes connection
                
                // server is expected to close conn.
                if (debugging)
                    debugResponseBuf(response, "no Content-Length and server is to close connection ", true);
                
                while (true) {
                    
                    while (response.buf_len < response.buf.length) {
                        try {
                            iErr = inStream.read(response.buf, response.buf_len, response.buf.length - response.buf_len);

                        } catch (IOException e) {
                            System.out.println("read error - case 2");
                            // e.printStackTrace();
                            closed = true;
                            setEndTime();
                            return null;
                        }
                        if (iErr <= 0) break; // no more data to read
                        response.buf_len += iErr;
                        
                    }
                    
                    // no more data to read, server closed connection
                    if (response.buf_len < response.buf.length) break;
                    
                    // otherwise, need a bigger buffer
                    if (debugging)
                        System.out.println("Connection: double buffer size " + response.buf.length);
                    byte[] tmpByteArray = new byte[response.buf.length * 2];
                    System.arraycopy(resBuf, 0, tmpByteArray, 0, response.buf_len);
                    response.buf = tmpByteArray;
                }
                response.contentLen = response.buf_len - response.headerEnd;
                
            } else { // case 3: no clue, client has to detect end of the reponse via a timer
                
                // no Content-Length on a persistent connection, do nonblocking read with a timer
                if (debugging)
                    debugResponseBuf(response, "no Content-Length on persistent connection", true);
                
                int sot = sessionSock.getSoTimeout();
                sessionSock.setSoTimeout(NON_BLOCKING_READ_TIMER);
                
                while ( true ) {
                    
                    while ( response.buf_len < response.buf.length) {
                        try {
                            iErr = inStream.read(response.buf, response.buf_len, response.buf.length - response.buf_len);
                            
                        } catch (SocketTimeoutException e) {

                            if (debugging)
                                debugResponseBuf(response, "in while after read ", true);

                            int ix = response.buf_len - 7;
                            byte[] b = response.buf;
                            if (b[ix++] != '<' || b[ix++] != '/' || b[ix++] != 'H' || b[ix++] != 'T'
                                    || b[ix++] != 'M' || b[ix++] != 'L' || b[ix++] != '>' ) {
                                if (debugging)
                                    System.out.println("incorrect timeout - increase NON_BLOCKING_READ_TIMER");
                                continue;
                            }
                            
                            break; //no more data to read
                            
                        } catch (IOException e) {
                            System.out.println("read error - case 3");
                            // e.printStackTrace();
                            closed = true;
                            setEndTime();
                            return null;
                        }
                        if (iErr <= 0) break; // no more data to read
                        response.buf_len += iErr;
                        
                    }
                    
                    // no more data to read, server closed connection
                    if (  response.buf_len < response.buf.length ) break;
                    
                    // otherwise, need a bigger buffer
                    if (debugging)
                        System.out.println("Connection: double buffer size " + response.buf.length);
                    byte[] tmpByteArray = new byte[response.buf.length * 2];
                    System.arraycopy(resBuf, 0, tmpByteArray, 0, response.buf_len);
                    response.buf = tmpByteArray;
                }
                response.contentLen = response.buf_len - response.headerEnd;
                sessionSock.setSoTimeout(sot);
              
            }
            
        }
        
        // close the connection if the other side send Connection: close
        if (response.toBeClosed == true) close("server sent close connection");
        
        if (response.protocol < 0) {
            // bad header
            // reset instance variables
            System.out.println("Error: Bad header: " + response.protocol);
            debugResponseBuf(response, "bad header", true);
            closed = true;
            setEndTime();
            return null;
        }
        
        if (response.status >= 400) {
            close("resp>400");
            System.out.println("got bad status: " + response.status);
            debugResponseBuf(response, "bad status", true);
            closed = true;
            setEndTime();
            return null;
        }
        
        setEndTime();
//        Sender sender = new Sender();
//        sender.append("ResponseTime.Driver.HTTP", getEndTime() - getStartTime());
//        sender.send();

 
        return new HttpResponseData(response, cookies, response.status);


    }
    
    public void close(String msg ) {
        
        try {
            if (inStream != null)
                inStream.close();
            inStream = null;
            if (outStream != null)
                outStream.close();
            outStream = null;
            if (sessionSock != null)
                sessionSock.close();
            sessionSock = null;
        } catch (IOException e) {
            // $JL-EXC$
        }

        if (debugging)
            System.out.println("close is called ........" + msg );
        closed = true;
    }
    
    
    /** calculate the time taken for the transaction calculated where tx start=end of the send
     * and txn end is the end of the read of the response from the server
     * @return int : the transaction response time
     */
    public int getTxnRespTime() {
        return getEndTime() - getStartTime();
       
    }
    
    public void resetTimers()  {
        //set timers to zero to indicate not set
        startTime=0;
        endTime=0;
    }
    
    /**
     * set the starting time for response time measurement
     */    
    public void setStartTime() {
        startTime=timer.getTime();
    }
    
    /**
     * @return int : the start time of the transaction
     */    
    public int getStartTime() {
   
        return startTime;
    }
    
    /**
     * set the end time of the transaction response
     */
    public void setEndTime() {
        endTime=timer.getTime();
        
    }
    
    /**
     * @return int : endTime time at which to measure the end of the transaction response
     */    
    public int getEndTime() {
        //get the time at the end of read response
        return endTime;
    }
    
   
    public static void debugResponseBuf(HttpResponseParser response, String s, boolean printBuf) {
        System.out.println( s + " ======= buf_len=" + response.buf_len + " headerEnd=" + response.headerEnd + " contentLen=" + response.contentLen + " chunkEnd" + response.chunkEnd + "========");
        if (printBuf) {
            try {
                System.out.println( (new String( response.buf, 0, response.buf_len, "ISO-8859-1")));
                
            } catch (java.io.UnsupportedEncodingException e) {
                System.out.println("debug error");
                e.printStackTrace();
            }
        }
        
    }

}

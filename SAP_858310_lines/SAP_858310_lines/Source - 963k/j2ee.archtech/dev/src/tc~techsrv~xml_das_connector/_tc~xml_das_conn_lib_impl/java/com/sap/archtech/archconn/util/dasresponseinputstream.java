package com.sap.archtech.archconn.util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 
 * Filters the status line (last line) of a response to
 * an archiving command containing the status message
 * of the XML Data Archiving Service.
 * 
 * @author D025792
 * @version 1.0
 * 
 */

public class DASResponseInputStream extends FilterInputStream
{
  private static final int SEPARATOR = 0x1F;
  private static final int MAXMLENGTH = 504;
  private static final int MIN_STATUSINFO_LENGTH = 3;
  // SKIP_BUFFER_SIZE is used to determine the size of skipBuffer
  private static final int SKIP_BUFFER_SIZE = 2048;
  // skipBuffer is initialized in skip(long), if needed.
  private static byte[] skipBuffer;
  
  private int startPosOfStatusInfo;
  private int nrOfReadBytes = 0;
  private int nrOfReadBytes_Marked = 0;
  private boolean isEndOfStreamReached = false;

  /**
   * Constructor for DASresponseInputStream.
   * @param in the wrapped InputStream
   */
  public DASResponseInputStream(InputStream in)
  {
    super(in);
    startPosOfStatusInfo = -1;
  }

  public DASResponseInputStream(InputStream in, int startPosOfStatusInfo)
  {
    super(in);
    this.startPosOfStatusInfo = startPosOfStatusInfo;
  }

  /**
   * Filters the staus line (last line of response body).
   * This method has to be tested carefully!
   * 
   * @see java.io.InputStream#read()
   */
  public int read() throws IOException
  {
    if(isEndOfStreamReached)
    {
      return -1;
    }
    
    int readbyte = 0;
    if(startPosOfStatusInfo > -1)
    {
      // info about the start position of the status line has been passed to the response
      // -> check if another byte may be read
      if(nrOfReadBytes == startPosOfStatusInfo)
      {
        // begin of status info should have been reached
        readbyte = in.read();
        if(readbyte == SEPARATOR)
        {
          // read next bytes to check status info
          byte[] statusInfoBuf = new byte[MIN_STATUSINFO_LENGTH];
          int statusInfoByte = 0;
          for(int i = 0; i < MIN_STATUSINFO_LENGTH; i++)
          {
            statusInfoByte = in.read();
            if(statusInfoByte != -1)
            {
              statusInfoBuf[i] = (byte)statusInfoByte;
            }
            else
            {
              break;
            }
          }
          String statusInfo = new String(statusInfoBuf, "UTF-8");
          if(!"200".equals(statusInfo))
          {
            throw new IOException(statusInfo);
          }
					// end of stream is reached
					readbyte = -1;
				}
				else
				{
					// could not find SEPARATOR as expected
					// -> this may happen if startPosOfStatusInfo = 0 has been passed for a resource stream longer than 0 bytes
					// -> in such cases startPosOfStatusInfo is useless => set it to -1 
					nrOfReadBytes++;
					startPosOfStatusInfo = -1;
				}
      }
      else
      {
        readbyte = in.read();
        nrOfReadBytes++;
      }
    }
    else    
    {
      // no info about start position of status line available
      readbyte = in.read();
      // did we really reach the status line?
      if (readbyte == SEPARATOR)
      {
        in.mark(MAXMLENGTH);
        int readbuffer;
        int count = 0;
        boolean statreached = true;
        byte[] buffer = new byte[MAXMLENGTH];
        while ((readbuffer = in.read()) != -1)
        {
          buffer[count] = (byte) readbuffer;
          count++;
          // if more than MAXMLENGTH chars are read or another SEPARATOR occurs,
          // this was not the status line
          if ((count >= MAXMLENGTH) || (readbuffer == SEPARATOR))
          {
            statreached = false;
            break;
          }
        }
        if (statreached)
        {
          // status line reached - check if status is 200 Ok
          String status = new String(buffer, "UTF-8").trim();
          if (status.startsWith("200"))
          {
            // status is ok - do nothing, end of stream reached
            readbyte = -1;
          }
          else
          {
            // status is not ok - throw Exception with status as message
            throw new IOException(status);
          }
        }
        else
        {
          // status line not reached
          in.reset();
        }
      }
    }
    
    isEndOfStreamReached = (readbyte == -1);
    return readbyte;
  }

  /**
   * 
   * The code is the same as in InputStream, but the local
   * (modified) read() method is used.
   * 
   * @see java.io.InputStream#read(byte[], int, int)
   */
  public int read(byte[] b, int off, int len) throws IOException
  {
    if (b == null)
    {
      throw new NullPointerException();
    }
    else if ((off < 0) || (off > b.length) || (len < 0) || ((off + len) > b.length) || ((off + len) < 0))
    {
      throw new IndexOutOfBoundsException();
    }
    else if (len == 0)
    {
      return 0;
    }

    int c = this.read();
    if (c == -1)
    {
      return -1;
    }
    b[off] = (byte) c;

    int i = 1;
    try
    {
      for (; i < len; i++)
      {
        c = this.read();
        if (c == -1)
        {
          break;
        }
        if (b != null)
        {
          b[off + i] = (byte) c;
        }
      }
    }
    catch (IOException ee)
    {
      //$JL-EXC$ taken from standard JDK
    }
    return i;
  }

  /**
   * The code is the same as in InputStream, but the local
   * (modified) read() method is used.
   * 
   * @see java.io.InputStream#read(byte[])
   */
  public int read(byte[] b) throws IOException
  {
    return this.read(b, 0, b.length);
  }

  /**
   * The code is the same as in InputStream, but the local
   * (modified) read() method is used.
   * 
   * @see java.io.InputStream#skip(long)
   */
  public long skip(long n) throws IOException
  {

    long remaining = n;
    int nr;
    if (skipBuffer == null)
      skipBuffer = new byte[SKIP_BUFFER_SIZE];

    byte[] localSkipBuffer = skipBuffer;

    if (n <= 0)
    {
      return 0;
    }

    while (remaining > 0)
    {
      nr = this.read(localSkipBuffer, 0, (int) Math.min(SKIP_BUFFER_SIZE, remaining));
      if (nr < 0)
      {
        break;
      }
      remaining -= nr;
    }

    return n - remaining;
  }

	public synchronized void mark(int readlimit)
	{
		if(markSupported())
		{
			super.mark(readlimit); 
			nrOfReadBytes_Marked = nrOfReadBytes;
		}
	}
	
	public synchronized void reset() throws IOException
	{
		if(markSupported())
		{
			super.reset();
			nrOfReadBytes = nrOfReadBytes_Marked;
		}
	}
}

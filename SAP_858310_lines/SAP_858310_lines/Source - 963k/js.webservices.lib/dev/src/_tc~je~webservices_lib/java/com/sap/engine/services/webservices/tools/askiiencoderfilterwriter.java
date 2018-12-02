/**
 * 
 */

package com.sap.engine.services.webservices.tools;

import java.io.FilterWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * 
 * @author I056242
 *
 */
public class ASKIIEncoderFilterWriter extends FilterWriter{

    public ASKIIEncoderFilterWriter(Writer writer)
    {
        super(writer);
    }

    public void write(char c)
        throws IOException
    {
        char ac[] = new char[1];
        ac[0] = c;
        write(ac, 0, 1);
    }

    public void write(char ac[], int i, int j)
        throws IOException
    {
        String lineSeparator = System.getProperty("line.separator");
        for(int k = 0; k < j; k++){
            if(ac[k] > '\177'){
                out.write(92);
                out.write(117);
                String s1 = Integer.toHexString(ac[k]);
                StringBuffer stringbuffer = new StringBuffer(s1);
                stringbuffer.reverse();
                int l = 4 - stringbuffer.length();
                for(int i1 = 0; i1 < l; i1++)
                    stringbuffer.append('0');

                for(int j1 = 0; j1 < 4; j1++)
                    out.write(stringbuffer.charAt(3 - j1));

            } else {
                out.write(ac[k]);
            }
        }
    }
}

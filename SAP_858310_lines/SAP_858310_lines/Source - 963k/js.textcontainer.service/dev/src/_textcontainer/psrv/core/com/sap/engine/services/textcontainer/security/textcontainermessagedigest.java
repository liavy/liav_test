package com.sap.engine.services.textcontainer.security;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.sap.engine.services.textcontainer.TextContainerException;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

public class TextContainerMessageDigest
{

	private static MessageDigest messageDigest = null;

    public static byte[] getDigest(String str) throws TextContainerException
    {
    	byte[] digest = null;

    	if (messageDigest == null)
    	{
    		try
    		{
				messageDigest = MessageDigest.getInstance("SHA-1");
			}
    		catch (NoSuchAlgorithmException e)
    		{
        		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "getDigest", e);
        		throw new TextContainerException("NoSuchAlgorithmException", e);
			}
    	}

    	try
    	{
			digest = messageDigest.digest(str.getBytes("UTF-8"));
		}
    	catch (UnsupportedEncodingException e)
		{
    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "getDigest", e);
    		throw new TextContainerException("UnsupportedEncodingException", e);
		}

    	return digest;
    }

    public static byte[] getDigestOptimizeAscii(String str) throws TextContainerException
    {
    	byte[] digest = null;

    	if (messageDigest == null)
    	{
    		try
    		{
				messageDigest = MessageDigest.getInstance("SHA-1");
			}
    		catch (NoSuchAlgorithmException e)
    		{
        		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "getDigest", e);
        		throw new TextContainerException("NoSuchAlgorithmException", e);
			}
    	}

		// We assume, that there are only ASCII characters in the string:
		digest = messageDigest.digest(str.getBytes());
/*
    	try
    	{
    		if (updateDigest(str))
            {
            	digest = messageDigest.digest();
            }
            else
            {
                // a non ASCII caharacter was detected
            	messageDigest.reset();
    			digest = messageDigest.digest(str.getBytes("UTF-8"));
            }
        }
    	catch (UnsupportedEncodingException e)
		{
    		CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "getDigest", e);
    		throw new TextContainerException("UnsupportedEncodingException", e);
		}
*/

    	return digest;
    }

/*
    private static boolean updateDigest(String str)
    {
        int charInt;
        int length = str.length();
        for (int i = 0; i < length; i++)
        {
            charInt = str.charAt(i);
            if (charInt >= 0 && charInt <= 127)
            {
            	messageDigest.update((byte)charInt);
            }
            else
            {
                return false;
            }
        }
        return true;
    }
*/

	// Logging:
	private static final Location LOCATION = Location.getLocation("com.sap.engine.services.textcontainer.security.TextContainerMessageDigest");
	private static final Category CATEGORY = Category.SYS_SERVER;

}

/*
 * Created on Jan 19, 2006
 */
package com.sap.engine.services.textcontainer.deployment;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

import com.sap.engine.services.textcontainer.TextContainerException;
import com.sap.engine.services.textcontainer.cache.SAPResourceBundleCache;
import com.sap.engine.services.textcontainer.context.Context;
import com.sap.engine.services.textcontainer.module.TextComponentContext;
import com.sap.engine.services.textcontainer.module.TextComponentException;
import com.sap.engine.services.textcontainer.module.TextComponentProcessor;
import com.sap.engine.services.textcontainer.module.TextComponentS2XText;
import com.sap.engine.services.textcontainer.security.TextContainerMessageDigest;

/**
 * @author d029702
 */
public abstract class TextComponentProcessorDeployment extends TextComponentProcessor
{

	public TextComponentProcessorDeployment(String component) throws TextComponentException
	{
		this.component = component;
		componentHash = getComponentHash(this.component);
/* Removed because TextContainerRecipient notification has been switched off! 
		deployedBundles = new HashMap<String, HashSet<String>>();
*/
		deployedBundles2 = new HashMap<String, HashMap<String, HashSet<String>>>();
		deployedLanguages = new HashSet<String>();
		undeployedBundles2 = new HashMap<String, HashMap<String, HashSet<String>>>();
	}

	/* (non-Javadoc)
	 * @see com.sap.engine.textcontainer.TextComponentProcessor#setContext(com.sap.engine.textcontainer.TextComponentContext)
	 */
	public void setContext(TextComponentContext context) throws TextComponentException
	{
		super.setContext(context);

		componentHash = getComponentHash(this.component);
	}

	/* (non-Javadoc)
	 * @see com.sap.engine.textcontainer.TextComponentProcessor#processTextBundle(java.lang.String, java.lang.String, com.sap.engine.textcontainer.TextComponentS2XText[])
	 */
	public void processTextBundle(String sBundle, String sRecipient, String sSourceLang, String sLang, TextComponentS2XText[] aoText) throws TextComponentException
	{
		deploy(sBundle, sRecipient, sSourceLang, sLang, aoText);

/* Removed because TextContainerRecipient notification has been switched off! 
		HashSet<String> set = deployedBundles.get(sRecipient);

		if (set == null)
		{
			set = new HashSet<String>();
			deployedBundles.put(sRecipient, set);
		}

		if (!set.contains(sBundle))
			set.add(sBundle);
*/

		HashMap<String, HashSet<String>> map = deployedBundles2.get(sRecipient);

		if (map == null)
		{
			map = new HashMap<String, HashSet<String>>();
			deployedBundles2.put(sRecipient, map);
		}

		HashSet<String> set;

		for (int i = 0; i < m_asOriginalComponent.length; i++)
		{
			set = map.get(m_asOriginalComponent[i]);

			if (set == null)
			{
				set = new HashSet<String>();
				map.put(m_asOriginalComponent[i], set);
			}

			if (!set.contains(sBundle))
				set.add(sBundle);
		}

		if (!deployedLanguages.contains(sLang))
			deployedLanguages.add(sLang);
	}

	public void beforeDeploy() throws TextComponentException
	{
		sequenceNumber = 0;
	}

	public void afterDeploy() throws TextComponentException
	{
	}

	public void deploy(String bundle, String recipient, String originalLocale, String lang, TextComponentS2XText[] text) throws TextComponentException
	{
		try
		{
			byte[] bundleHash = TextContainerMessageDigest.getDigestOptimizeAscii(bundle);

			int contextId = getContextId(new Context(lang, m_oContext.m_sIndustry, m_oContext.m_sRegion, m_oContext.m_sExtension), true);

			for (int i = 0; i < m_asOriginalComponent.length; i++)
			{
				// Create new originalComponentHash, until we know if we'd better raise an
				// exception if m_asOriginalComponent[i] has not yet been deployed...
				byte[] originalComponentHash = getComponentHash(m_asOriginalComponent[i]);

				getBundleHash(bundleHash, originalComponentHash, bundle, recipient);

				sequenceNumber = insertTextContainer(componentHash, sequenceNumber, originalComponentHash,
						bundleHash, contextId, text, originalLocale);

				modifyDirty(originalComponentHash, bundleHash);

				// Delete from the cache:
	 			SAPResourceBundleCache.remove(m_asOriginalComponent[i], bundle);
			}
		}
		catch (TextContainerException e)
		{
	    	throw new TextComponentException("Load bundle "+bundle+" in language "+lang+" failed with TextContainerException:\n"+e.getMessage(), e);
		}
		catch (TextComponentException e)
		{
	    	throw new TextComponentException("Load bundle "+bundle+" in language "+lang+" failed with TextComponentException:\n"+e.getMessage(), e);
		}
	}

	public void undeploy() throws TextComponentException
	{
	}

	public void resolve() throws TextComponentException
	{
	}

	public void unresolve() throws TextComponentException
	{
	}

/* Removed because TextContainerRecipient notification has been switched off! 
	public HashMap<String, HashMap<String, String[]>> getDeployedBundles()
	{
		HashMap<String, HashMap<String, String[]>> returnMap = new HashMap<String, HashMap<String, String[]>>();

		if (!deployedBundles.isEmpty())
		{
			Iterator<Entry<String, HashSet<String>>> iterRecipients = deployedBundles.entrySet().iterator();

			while (iterRecipients.hasNext())
			{
				Entry<String, HashSet<String>> entryOfRecipient = iterRecipients.next();

				HashMap<String, String[]> mapOfComponents = new HashMap<String, String[]>();
				mapOfComponents.put(this.component, (String[])entryOfRecipient.getValue().toArray(new String[] {}));
				returnMap.put(entryOfRecipient.getKey(), mapOfComponents);
			}
		}

		return returnMap;
	}
*/

	public HashMap<String, HashMap<String, String[]>> getDeployedBundles2()
	{
		HashMap<String, HashMap<String, String[]>> returnMap = new HashMap<String, HashMap<String, String[]>>();

		if (!deployedBundles2.isEmpty())
		{
			Iterator<Entry<String, HashMap<String, HashSet<String>>>> iterRecipients = deployedBundles2.entrySet().iterator();

			while (iterRecipients.hasNext())
			{
				Entry<String, HashMap<String, HashSet<String>>> entryOfRecipient = iterRecipients.next();

				Iterator<Entry<String, HashSet<String>>> iterOriginalComponents = entryOfRecipient.getValue().entrySet().iterator();

				HashMap<String, String[]> mapOfComponents = new HashMap<String, String[]>();

				while (iterOriginalComponents.hasNext())
				{
					Entry<String, HashSet<String>> entryOfOriginalComponent = iterOriginalComponents.next();

					mapOfComponents.put(entryOfOriginalComponent.getKey(), (String[])entryOfOriginalComponent.getValue().toArray(new String[] {}));
				}

				returnMap.put(entryOfRecipient.getKey(), mapOfComponents);
			}
		}

		return returnMap;
	}

	public String[] getDeployedLanguages()
	{
		return (String[])deployedLanguages.toArray(new String[] {});
	}

	public HashMap<String, HashMap<String, String[]>> getUndeployedBundles2()
	{
		HashMap<String, HashMap<String, String[]>> returnMap = new HashMap<String, HashMap<String, String[]>>();

		if (!undeployedBundles2.isEmpty())
		{
			Iterator<Entry<String, HashMap<String, HashSet<String>>>> iterRecipients = undeployedBundles2.entrySet().iterator();

			while (iterRecipients.hasNext())
			{
				Entry<String, HashMap<String, HashSet<String>>> entryOfRecipient = iterRecipients.next();

				Iterator<Entry<String, HashSet<String>>> iterOriginalComponents = entryOfRecipient.getValue().entrySet().iterator();

				HashMap<String, String[]> mapOfComponents = new HashMap<String, String[]>();

				while (iterOriginalComponents.hasNext())
				{
					Entry<String, HashSet<String>> entryOfOriginalComponent = iterOriginalComponents.next();

					mapOfComponents.put(entryOfOriginalComponent.getKey(), (String[])entryOfOriginalComponent.getValue().toArray(new String[] {}));
				}

				returnMap.put(entryOfRecipient.getKey(), mapOfComponents);
			}
		}

		return returnMap;
	}

	protected abstract byte[] getComponentHash(String component) throws TextComponentException;

	protected abstract void getBundleHash(byte[] bundleHash, byte[] componentHash, String bundle, String recipient) throws TextComponentException;

	protected abstract int getContextId(Context context, boolean createNewId) throws TextComponentException;

	protected abstract int insertTextContainer(byte[] deployComponentHash, int sequenceNumber, byte[] originalComponentHash,
			byte[] bundleHash, int contextId, TextComponentS2XText[] text, String originalLocale) throws TextComponentException;

	protected abstract void modifyDirty(byte[] originalComponentHash, byte[] bundleHash) throws TextComponentException;

	protected byte[] StringToByteArray(String string)
	{
		String sStr = string.toUpperCase();
		byte[] abArray = new byte[sStr.length() / 2];
		char cChar;
		for (int i = 0; i < sStr.length() / 2; i++)
		{
			cChar = sStr.charAt(i*2);
			if (cChar >= 'A')
				abArray[i] = (byte) ((cChar - 'A' + 10) << 4);
			else
				abArray[i] = (byte) ((cChar - '0') << 4);

			cChar = sStr.charAt(i*2+1);
			if (cChar >= 'A')
				abArray[i] += (byte) (cChar - 'A' + 10);
			else
				abArray[i] += (byte) (cChar - '0');
		}

		return abArray;
	}

	protected String ByteArrayToString(byte[] bytes)
	{
		char[] chars = new char[bytes.length*2];
		byte bByte;
		for (int i = 0; i < bytes.length; i++)
		{
			bByte = (byte) ((bytes[i] & 0xF0) >> 4);
			if (bByte >= 10)
				chars[i*2] = (char) ('A' + (char) (bByte - 10));
			else
				chars[i*2] = (char) ('0' + (char) bByte);
			bByte = (byte) (bytes[i] & 0x0F);
			if (bByte >= 10)
				chars[i*2+1] = (char) ('A' + (char) (bByte - 10));
			else
				chars[i*2+1] = (char) ('0' + (char) bByte);
		}
		return new String(chars);
	}

	int sequenceNumber;

	protected String component;
	protected byte[] componentHash;

/* Removed because TextContainerRecipient notification has been switched off! 
	protected HashMap<String, HashSet<String>> deployedBundles;
*/
	protected HashMap<String, HashMap<String, HashSet<String>>> deployedBundles2;
	protected HashSet<String> deployedLanguages;

	protected HashMap<String, HashMap<String, HashSet<String>>> undeployedBundles2;

}

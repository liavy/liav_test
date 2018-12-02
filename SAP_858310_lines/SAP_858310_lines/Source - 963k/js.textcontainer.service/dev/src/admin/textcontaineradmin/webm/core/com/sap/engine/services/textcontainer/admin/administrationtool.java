/*
 * Created on Apr 19, 2006
 */
package com.sap.engine.services.textcontainer.admin;

import java.security.Permission;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoParameterList;
import com.sap.conn.jco.JCoStructure;
import com.sap.conn.jco.JCoTable;
import com.sap.engine.interfaces.textcontainer.TextContainerExtension;
import com.sap.engine.interfaces.textcontainer.TextContainerIndustry;
import com.sap.engine.interfaces.textcontainer.TextContainerLanguage;
import com.sap.engine.interfaces.textcontainer.TextContainerLocale;
import com.sap.engine.interfaces.textcontainer.TextContainerManager;
import com.sap.engine.interfaces.textcontainer.TextContainerRegion;
import com.sap.engine.services.textcontainer.admin.dbaccess.BundleData;
import com.sap.engine.services.textcontainer.admin.dbaccess.ComponentData;
import com.sap.engine.services.textcontainer.admin.dbaccess.ContainerData;
import com.sap.engine.services.textcontainer.admin.dbaccess.ContextData;
import com.sap.engine.services.textcontainer.admin.dbaccess.ContextIterator;
import com.sap.engine.services.textcontainer.admin.dbaccess.Ctx;
import com.sap.engine.services.textcontainer.admin.dbaccess.DirtyData;
import com.sap.engine.services.textcontainer.admin.dbaccess.LoadData;
import com.sap.security.api.IUser;
import com.sap.security.api.UMFactory;
import com.sap.security.api.permissions.ActionPermission;

/**
 * @author d029702
 */
public class AdministrationTool
{

	public static void checkLogin(HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		IUser user = UMFactory.getAuthenticator().getLoggedInUser(request, response);

		if (null == user)
		{
			UMFactory.getAuthenticator().forceLoggedInUser(request, response);
		}
	}

	static public void checkAdministrationPermission(HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		if (tcm == null)
			initializeTextContainerManager();

		if (!tcm.checkAdministrationPermission())
		{
			IUser user = UMFactory.getAuthenticator().getLoggedInUser(request, response);

			Permission permission = new ActionPermission("administration", "change");

			throw new Exception("User " + user.getName() + " does not have permission " + permission.getClass().getName() + " with parameters " + permission.toString());
		}
	}

	public static String getSystemContextIndustry() throws Exception
	{
		if (tcm == null)
			initializeTextContainerManager();

		return tcm.getSystemContextIndustry();
	}

	public static String getSystemContextRegion() throws Exception
	{
		if (tcm == null)
			initializeTextContainerManager();

		return tcm.getSystemContextRegion();
	}

	public static String getSystemContextExtension() throws Exception
	{
		if (tcm == null)
			initializeTextContainerManager();

		return tcm.getSystemContextExtension();
	}

	public static void setSystemContext(String industry, String region, String extension) throws Exception
	{
		if (tcm == null)
			initializeTextContainerManager();

		tcm.setSystemContext(industry, region, extension);
	}

	public static void retrieve(String destination, boolean contextAttributeValues, boolean languageValues, boolean localeChains, boolean systemContext) throws Exception
	{
		if (tcm == null)
			initializeTextContainerManager();

		TXVRemote remote = new TXVRemote(destination);

		if (contextAttributeValues)
		{
			HashMap<String, TextContainerIndustry> industries = new HashMap<String, TextContainerIndustry>();

			JCoTable industryTable = remote.retrieveIndustryValues();

			for (int i = 0; i < industryTable.getNumRows(); i++)
			{
				industryTable.setRow(i);

				industries.put(industryTable.getString("INDUSTRY"),
						new Industry(industryTable.getString("INDUSTRY"), industryTable.getString("FATHER"),
								industryTable.getString("TERMDOMAIN"), industryTable.getString("COLLKEY")));
			}

			tcm.setIndustryValues(industries);

			HashMap<String, TextContainerRegion> regions = new HashMap<String, TextContainerRegion>();

			JCoTable regionTable = remote.retrieveRegionValues();

			for (int i = 0; i < regionTable.getNumRows(); i++)
			{
				regionTable.setRow(i);

				regions.put(regionTable.getString("REGION"),
						new Region(regionTable.getString("REGION"), regionTable.getString("FATHER"),
								regionTable.getString("TERMDOMAIN"), regionTable.getString("COLLKEY")));
			}

			tcm.setRegionValues(regions);

			HashMap<String, TextContainerExtension> extensions = new HashMap<String, TextContainerExtension>();

			JCoTable extensionTable = remote.retrieveExtensionValues();

			for (int i = 0; i < extensionTable.getNumRows(); i++)
			{
				extensionTable.setRow(i);

				extensions.put(extensionTable.getString("EXTENSION"),
						new Extension(extensionTable.getString("EXTENSION"), extensionTable.getString("FATHER"),
								extensionTable.getString("TERMDOMAIN"), extensionTable.getString("COLLKEY")));
			}

			tcm.setExtensionValues(extensions);
		}

		if (languageValues)
		{
			HashMap<String, TextContainerLanguage> languages = new HashMap<String, TextContainerLanguage>();

			JCoFunction function = remote.retrieveLanguageValues();

			JCoParameterList pl = function.getExportParameterList();

			String secondaryLocale = pl.getString("SECONDARY_LOCALE");

			pl = function.getTableParameterList();

			JCoTable languageTable = pl.getTable("T_PROCESSED_LOCALE");

			String locale;
			boolean secondaryLocaleFlag; 

			for (int i = 0; i < languageTable.getNumRows(); i++)
			{
				languageTable.setRow(i);

				locale = languageTable.getString("LOCALE");

				if (locale.equals(secondaryLocale))
					secondaryLocaleFlag = true;
				else
					secondaryLocaleFlag = false;

				languages.put(languageTable.getString("LOCALE"),
						new Language(languageTable.getString("LOCALE"), secondaryLocaleFlag));
			}

			tcm.setLanguageValues(languages);
		}

		if (localeChains)
		{
			HashMap<String, TextContainerLocale[]> locales = new HashMap<String, TextContainerLocale[]>();

			// Get the list (it's ordered by START_LOCALE and SEQNR!):
			JCoTable localeTable = remote.retrieveLocaleValues();

			String startLocale = "";

			ArrayList<TextContainerLocale> list = new ArrayList<TextContainerLocale>();

			for (int i = 0; i < localeTable.getNumRows(); i++)
			{
				localeTable.setRow(i);

				if (!localeTable.getString("START_LOCALE").equals(startLocale))
				{
					if (startLocale.length() > 0)
					{
						locales.put(startLocale, (TextContainerLocale[]) list.toArray(new TextContainerLocale[0]));

						list = new ArrayList<TextContainerLocale>();
					}

					startLocale = localeTable.getString("START_LOCALE");
				}

				list.add(new Locale(localeTable.getString("START_LOCALE"), localeTable.getInt("SEQNR"),
						localeTable.getString("LOCALE")));
			}

			if (startLocale.length() > 0)
			{
				locales.put(startLocale, (TextContainerLocale[]) list.toArray(new TextContainerLocale[0]));
			}

			tcm.setLocaleValues(locales);
		}

		if (systemContext)
		{
			JCoFunction function = remote.retrieveSystemContext();

			JCoParameterList pl = function.getExportParameterList();

			JCoStructure context = pl.getStructure("O_CONTEXT");

			String industry = context.getString("INDUSTRY");
			String region = context.getString("REGION");
			String extension = context.getString("EXTENSION");

			tcm.setSystemContext(industry, region, extension);
		}
	}

	public static void deleteLanguageAttributeValues() throws Exception
	{
		if (tcm == null)
			initializeTextContainerManager();

		HashMap<String, TextContainerLanguage> languages = new HashMap<String, TextContainerLanguage>();

		tcm.setLanguageValues(languages);

		HashMap<String, TextContainerLocale[]> locales = new HashMap<String, TextContainerLocale[]>();

		tcm.setLocaleValues(locales);
	}

	public static void deleteContextAttributeValues() throws Exception
	{
		if (tcm == null)
			initializeTextContainerManager();

		HashMap<String, TextContainerIndustry> industries = new HashMap<String, TextContainerIndustry>();

		tcm.setIndustryValues(industries);

		HashMap<String, TextContainerRegion> regions = new HashMap<String, TextContainerRegion>();

		tcm.setRegionValues(regions);

		HashMap<String, TextContainerExtension> extensions = new HashMap<String, TextContainerExtension>();

		tcm.setExtensionValues(extensions);

		HashMap<String, TextContainerLanguage> languages = new HashMap<String, TextContainerLanguage>();

		tcm.setLanguageValues(languages);

		HashMap<String, TextContainerLocale[]> locales = new HashMap<String, TextContainerLocale[]>();

		tcm.setLocaleValues(locales);
	}

	public static HashMap<String, TextContainerIndustry> getIndustries() throws Exception
	{
		if (tcm == null)
			initializeTextContainerManager();

		return tcm.getIndustryValues();
	}

	public static HashMap<String, TextContainerRegion> getRegions() throws Exception
	{
		if (tcm == null)
			initializeTextContainerManager();

		return tcm.getRegionValues();
	}

	public static HashMap<String, TextContainerExtension> getExtensions() throws Exception
	{
		if (tcm == null)
			initializeTextContainerManager();

		return tcm.getExtensionValues();
	}

	public static HashMap<String, TextContainerLanguage> getLocales() throws Exception
	{
		if (tcm == null)
			initializeTextContainerManager();

		return tcm.getLanguageValues();
	}

	public static HashMap<String, TextContainerLocale[]> getLocaleChains() throws Exception
	{
		if (tcm == null)
			initializeTextContainerManager();

		return tcm.getLocaleValues();
	}

	public static ContextIterator getContexts() throws Exception
	{
		if (dataSource == null)
			initializeDataSource();

		if (ctx == null)
			ctx = new Ctx(dataSource.getConnection());

		return ContextData.select(ctx);
	}

	public static HashMap<String, ComponentData> getComponents() throws Exception
	{
		if (dataSource == null)
			initializeDataSource();

		if (ctx == null)
			ctx = new Ctx(dataSource.getConnection());

		ComponentData[] arrComponents = ComponentData.select(ctx);

		HashMap<String, ComponentData> components = new HashMap<String, ComponentData>();

		for (ComponentData component : arrComponents)
			components.put(component.getComponent(), component);

		return components;
	}

	public static HashMap<String, HashMap<String, BundleData>> getBundles(String componentHash, String recipient) throws Exception
	{
		if (dataSource == null)
			initializeDataSource();

		if (ctx == null)
			ctx = new Ctx(dataSource.getConnection());

		HashMap<String, HashMap<String, BundleData>> bundles = new HashMap<String, HashMap<String, BundleData>>();

		if ((componentHash == null) || (componentHash.length() == 0))
		{
			BundleData[] arrBundles = BundleData.select(ctx);

			for (BundleData bundle : arrBundles)
			{
				if ((recipient == null) || (recipient.length() == 0) || (recipient.equals(bundle.getRecipient())))
				{
					ComponentData component = ComponentData.select(ctx, bundle.getComponentHash());

					HashMap<String, BundleData> componentBundles = bundles.get(component.getComponent());

					if (componentBundles == null)
					{
						componentBundles = new HashMap<String, BundleData>();

						bundles.put(component.getComponent(), componentBundles);
					}

					componentBundles.put(bundle.getBundle(), bundle);
				}
			}
		}
		else
		{
			ComponentData component = ComponentData.select(ctx, StringToByteArray(componentHash));

			bundles.put(component.getComponent(), new HashMap<String, BundleData>());

			HashSet<BundleData> setBundles = BundleData.select(ctx, StringToByteArray(componentHash));

			for (BundleData bundle : setBundles)
			{
				if ((recipient == null) || (recipient.length() == 0) || (recipient.equals(bundle.getRecipient())))
				{
					component = ComponentData.select(ctx, bundle.getComponentHash());

					HashMap<String, BundleData> componentBundles = bundles.get(component.getComponent());

					if (componentBundles == null)
					{
						componentBundles = new HashMap<String, BundleData>();

						bundles.put(component.getComponent(), componentBundles);
					}

					componentBundles.put(bundle.getBundle(), bundle);
				}
			}
		}

		return bundles;
	}

	public static ContainerData[] getDeployedTexts(String deployedComponentHash, String originalComponentHash, String bundle) throws Exception
	{
		if (dataSource == null)
			initializeDataSource();

		if (ctx == null)
			ctx = new Ctx(dataSource.getConnection());

		ContainerData[] arrTexts = null;

		if ((deployedComponentHash != null) && (deployedComponentHash.length() > 0))
		{
			arrTexts = ContainerData.selectDeployedCompHash(ctx, StringToByteArray(deployedComponentHash));
		}
		else if ((originalComponentHash != null) && (originalComponentHash.length() > 0))
		{
			if ((bundle == null) || (bundle.length() == 0))
				arrTexts = ContainerData.select(ctx, StringToByteArray(originalComponentHash));
			else
				arrTexts = ContainerData.select(ctx, StringToByteArray(originalComponentHash), StringToByteArray(bundle));
		}
		else
		{
			arrTexts = ContainerData.select(ctx);
		}

		return arrTexts;
	}

	public static LoadData[] getTextLoadTexts() throws Exception
	{
		if (dataSource == null)
			initializeDataSource();

		if (ctx == null)
			ctx = new Ctx(dataSource.getConnection());

		LoadData[] arrTexts = null;

		arrTexts = LoadData.select(ctx);

		return arrTexts;
	}

	public static DirtyData[] getDirtyData() throws Exception
	{
		if (dataSource == null)
			initializeDataSource();

		if (ctx == null)
			ctx = new Ctx(dataSource.getConnection());

		DirtyData[] arrDirtyData = null;

		arrDirtyData = DirtyData.select(ctx);

		return arrDirtyData;
	}

	public static String getComponentName(String componentHashString) throws Exception
	{
		String componentName = tableOfComponentHashStrings.get(componentHashString);

		if (componentName == null)
		{
			if (dataSource == null)
				initializeDataSource();

			if (ctx == null)
				ctx = new Ctx(dataSource.getConnection());

			ComponentData component = ComponentData.select(ctx, StringToByteArray(componentHashString));

			componentName = component.getComponent();

			if (componentName != null)
				tableOfComponentHashStrings.put(componentHashString, componentName);
		}

		return componentName;
	}

	public static String getBundleName(String componentHashString, String bundleHashString) throws Exception
	{
		String bundleName = tableOfBundleHashStrings.get(componentHashString + "|" + bundleHashString);

		if (bundleName == null)
		{
			if (dataSource == null)
				initializeDataSource();

			if (ctx == null)
				ctx = new Ctx(dataSource.getConnection());

			BundleData bundle = BundleData.select(ctx, StringToByteArray(bundleHashString), StringToByteArray(componentHashString));

			bundleName = bundle.getBundle();

			if (bundleName != null)
				tableOfBundleHashStrings.put(componentHashString + "|" + bundleHashString, bundleName);
		}

		return bundleName;
	}

	public static void closeConnection() throws Exception
	{
		if (ctx != null)
			ctx.close();

		ctx = null;

		tableOfComponentHashStrings.clear();
		tableOfBundleHashStrings.clear();
	}

	public static byte[] StringToByteArray(String string)
	{
		String sStr = string.toUpperCase(java.util.Locale.ENGLISH);
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

	public static String ByteArrayToString(byte[] bytes)
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

	protected static void initializeDataSource() throws Exception
	{
		Context ctx = new InitialContext();
		dataSource = (DataSource) ctx.lookup("jdbc/SAP/BC_TEXTCONTAINER");
	}

	protected static DataSource dataSource = null;

	protected static Ctx ctx = null;

	protected static void initializeTextContainerManager() throws Exception
	{
		InitialContext ctx = new InitialContext();
		tcm = (TextContainerManager) ctx.lookup("interfaces/textcontainer_api");
	}

	protected static TextContainerManager tcm = null;

	protected static Hashtable<String, String> tableOfComponentHashStrings = new Hashtable<String, String>();
	protected static Hashtable<String, String> tableOfBundleHashStrings = new Hashtable<String, String>();

}

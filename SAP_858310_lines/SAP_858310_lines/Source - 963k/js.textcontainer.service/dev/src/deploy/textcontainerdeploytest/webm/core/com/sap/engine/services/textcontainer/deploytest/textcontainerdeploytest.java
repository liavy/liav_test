/*
 * Created on Mar 9, 2007
 */
package com.sap.engine.services.textcontainer.deploytest;

import java.util.Set;

import javax.naming.InitialContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sap.engine.services.dc.api.Client;
import com.sap.engine.services.dc.api.ClientFactory;
import com.sap.engine.services.dc.api.ComponentManager;
import com.sap.engine.services.dc.api.explorer.RepositoryExplorer;
import com.sap.engine.services.dc.api.explorer.RepositoryExplorerFactory;
import com.sap.engine.services.dc.api.model.Sca;
import com.sap.engine.services.dc.api.model.Sda;
import com.sap.engine.services.dc.api.model.Sdu;
import com.sap.security.api.IUser;
import com.sap.security.api.UMFactory;

/**
 * @author d029702
 */
public class TextContainerDeployTest
{
	public static void checkLogin(HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		IUser user = UMFactory.getAuthenticator().getLoggedInUser(request, response);

		if (null == user)
		{
			UMFactory.getAuthenticator().forceLoggedInUser(request, response);
		}
		else
		{
			initRepositoryExplorerFactory();
		}
	}

	public static Sdu[] getSduList() throws Exception
	{
		RepositoryExplorer repositoryExplorer = repositoryExplorerFactory.createRepositoryExplorer();

		return repositoryExplorer.findAll();
	}

	public static Set getSdaList(String sca_name, String sca_vendor) throws Exception
	{
		RepositoryExplorer repositoryExplorer = repositoryExplorerFactory.createRepositoryExplorer();

		Sca sca = repositoryExplorer.findSca(sca_name, sca_vendor);

		if (sca != null)
			return sca.getSdaIds();

		return null;
	}

	public static Sda getSda(String sda_name, String sda_vendor) throws Exception
	{
		RepositoryExplorer repositoryExplorer = repositoryExplorerFactory.createRepositoryExplorer();

		return repositoryExplorer.findSda(sda_name, sda_vendor);
	}

	private static void initRepositoryExplorerFactory()
	{
		if (repositoryExplorerFactory == null)
		{
			try
			{
				InitialContext ctx = new InitialContext();
				ClientFactory clientFactory = (ClientFactory) ctx.lookup("interfaces/tc~bl~deploy_api");

				Client client = clientFactory.createClient();

				ComponentManager componentManager = client.getComponentManager();

				repositoryExplorerFactory = componentManager.getRepositoryExplorerFactory();
			}
			catch (Exception e)
			{
			}
		}
	}

	private static RepositoryExplorerFactory repositoryExplorerFactory = null;

}
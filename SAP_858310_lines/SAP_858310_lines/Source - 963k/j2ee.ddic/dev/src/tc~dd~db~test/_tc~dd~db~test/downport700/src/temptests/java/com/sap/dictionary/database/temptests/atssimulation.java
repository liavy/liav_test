import java.io.InputStream;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;


import javax.naming.InitialContext;
import javax.sql.DataSource;

import com.sap.dictionary.database.opentools.DbTableOpenTools;

/**
 * @ejbHome <{JddiTestBeanHome}>
 * @ejbLocal <{JddiTestBeanLocal}>
 * @ejbLocalHome <{JddiTestBeanLocalHome}>
 * @ejbRemote <{JddiTestBean}>
 * @stateless 
 */
public class AtsSimulation {

	

	/**
	 * Business Method.
	 */
	public StringBuffer testJddi(String dataSource) throws RemoteException {
		try {

			InitialContext jndiCtx = new InitialContext();
			DataSource ds = (DataSource) jndiCtx.lookup("jdbc/" + dataSource);

			Connection conn = ds.getConnection();

			ArrayList conns = new ArrayList();
			conns.add(conn);
			StringBuffer ret = Veri.exec(conns);
			ret.append("connection object:" + conn);
			conn.close();

			return ret;
		} catch (Exception e) {
			throw new RemoteException("error in ejb", e);
		}
	}
	/**
	 * business method
	 * @param dataSource
	 * @return
	 * @throws RemoteException
	 */
	public StringBuffer testOpenTools(String dataSource)
		throws RemoteException {
		try {

			InitialContext jndiCtx = new InitialContext();
			DataSource ds = (DataSource) jndiCtx.lookup("jdbc/" + dataSource);

			Connection conn = ds.getConnection();

			StringBuffer ret = new StringBuffer();
			boolean result = false;
			DbTableOpenTools ot = new DbTableOpenTools(conn);
			//create table
			InputStream stream =
				JddiTestBeanBean.class.getResourceAsStream(
					"testdata/TMP_DDICTEST_FLI.gdbtable");
			boolean ok = ot.createTable("TMP_DDICTEST_FLI", stream);
			result = ok;
			ret.append("create table TMP_DDICTEST_FLI: " + ok + "\n");
			//insert
			PreparedStatement stmt =
				conn.prepareStatement(
					"INSERT INTO TMP_DDICTEST_FLI (carrid,connid,fldate,price,planetype) VALUES ('43'"
						+ ",3,?,34,'LH')");
			stmt.setDate(1, Date.valueOf("2002-02-02"));
			ok = stmt.execute();
			
			ret.append("insert: " + ok + "\n");
			//select
			Statement st = conn.createStatement();
			ResultSet rs =
				st.executeQuery("SELECT carrid,connid FROM TMP_DDICTEST_FLI");
			ret.append("select:");
			while (rs.next()) {
				ret.append(
					"carrid = "
						+ rs.getString(1)
						+ "; connid="
						+ rs.getInt(2)+"\n");
			}
			stmt.close();
			rs.close();
			st.close();
			conn.commit();
			//drop table
			ok = ot.dropTable("TMP_DDICTEST_FLI");
			if (ok==false)result=ok;
			ret.append("drop: "+ok);
			ret.insert(0,result);
			conn.close();

			return ret;
		} catch (Exception e) {
			throw new RemoteException("error in ejb", e);
		}

	}

}

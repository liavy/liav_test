package com.sap.archtech.archconn.values;

/**
 * 
 * Value class; holds information
 * of a SELECT request. 
 * 
 * @author D025792
 * @version 1.0
 * 
 */

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.TreeSet;

import com.sap.guid.IGUID;

public class SelectClause implements Serializable
{

	// ----------
	// Constants ------------------------------------------------------
	// ----------

	/**
	 * versions of this class must declare this SUID to allow
	 * serialization between different versions
	 */
	private static final long serialVersionUID = -640083147517309688L;

	// ------------------
	// Instance Variables ------------------------------------------------------
	// ------------------

	private String indexname;
	private String indexclause;
	private String systemclause;
	private TreeSet<SelectParam> paramlist;

	// ------------
	// Constructors ------------------------------------------------------------
	// ------------
	/**
	 * @param indexname name of the index used for searching
	 * @param indexclause an OpenSQL WHERE-clause (without the 
	 * 'WHERE' keyword) operating on the fields of the specified index.
	 * @param systemclause A valid OpenSQL WHERE clause for table SAPXMLA_RES.
	 * This clause may contain the following attributes:
	 * <ul><li>'resType' - String (resource type in upper case)
	 * <li>'reslength' - int (length of resource)
	 * <li>'creationUser' - String
	 * <li>'checkstatus' - String
	 * <li>'isPacked' - String
	 * </ul>
	 * If an index name is specified, an index clause must also be specified, and vice versa.
	 */
	public SelectClause(String indexname, String indexclause, String systemclause)
	{
		this.indexname = indexname;
		this.indexclause = indexclause;
		this.systemclause = systemclause;
		this.paramlist = new TreeSet<SelectParam>();
	}

	// --------------
	// Public Methods ----------------------------------------------------------
	// --------------

	/**
	 * @return OpenSQL WHERE clause operating on the selected index
	 */
	public String getIndexclause()
	{
		return indexclause;
	}

	/**
	 * @return name of the used index
	 */
	public String getIndexname()
	{
		return indexname;
	}

	/**
	 * @return OpenSQL WHERE clause for meta data queries
	 */
	public String getSystemclause()
	{
		return systemclause;
	}

	public void setString(int pos, String value)
	{
		paramlist.add(new SelectParam(pos, "string", value));
	}

	public void setShort(int pos, short value)
	{
		paramlist.add(new SelectParam(pos, "short", Short.valueOf(value)));
	}

	public void setInt(int pos, int value)
	{
		paramlist.add(new SelectParam(pos, "int", Integer.valueOf(value)));
	}

	public void setLong(int pos, long value)
	{
		paramlist.add(new SelectParam(pos, "long", Long.valueOf(value)));
	}

	public void setFloat(int pos, float value)
	{
		paramlist.add(new SelectParam(pos, "float", new Float(value)));
	}

	public void setDouble(int pos, double value)
	{
		paramlist.add(new SelectParam(pos, "double", new Double(value)));
	}

	public void setTimestamp(int pos, Timestamp value)
	{
		//    String parsedate;
		//    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		//    sdf.setTimeZone(TimeZone.getDefault());
		//    parsedate = sdf.format(new java.util.Date(value.getTime() + (value.getNanos() / 1000000)));
		//    paramlist.add(new SelectParam(pos, "timestamp", parsedate));

		paramlist.add(new SelectParam(pos, "timestamp", value));
	}

	public void setGuid(int pos, IGUID value)
	{
	// store as hex string because IGUID does not implement Serializable
		paramlist.add(new SelectParam(pos, "binary", value.toHexString()));
	}
	
	/**
	 * @return parameter list of all added parameters as
	 * a TreeSet
	 */
	public TreeSet<SelectParam> getParamlist()
	{
		return paramlist;
	}
}

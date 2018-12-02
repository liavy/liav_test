package com.sap.dictionary.database.dbs;

/**
 * Ueberschrift:   Dictionary Types: Simple Types and Structures
 * Beschreibung:
 * Copyright:     Copyright (c) 2002
 * Organisation:
 * @author Kerstin Hoeft
 * @version 1.0
 */

import java.util.*;
import java.math.*;
import java.sql.*;
import java.text.*;

public class XmlMap extends HashMap {
	private static final SimpleDateFormat FDATE =
		new SimpleDateFormat("yyyy-MM-dd");
	private static final SimpleDateFormat FTIME =
		new SimpleDateFormat("H:mm:ss.SSS");
	private boolean emptyTagLeadsToNull = true;
	private boolean formatErrorLeadsToNull = true;
	private boolean nullElementLeadsToNullObject = true;

	public XmlMap() {
	}

	public boolean getLeadsEmptyTagToNull() {
		return emptyTagLeadsToNull;
	}

	public boolean getLeadsFormatErrorToNull() {
		return formatErrorLeadsToNull;
	}

	public boolean getLeadsNullElementToNullObject() {
		return nullElementLeadsToNullObject;
	}

	public void setEmptyTagLeadsToNull(boolean b) {
		emptyTagLeadsToNull = b;
	}

	public void setFormatErrorLeadsToNull(boolean b) {
		formatErrorLeadsToNull = b;
	}

	public void setNullElementLeadsToNullObject(boolean b) {
		nullElementLeadsToNullObject = b;
	}

	public XmlMap getXmlMap(String key) {
		Object object = get(key);
		if ((object == null) || (!(object instanceof XmlMap)))
			return new XmlMap();
		else 
			return ((XmlMap) object);
	}

  public XmlMap getXmlMap(String key, int i) {
		Object object = get(key + (i == 0 ? "" : "" + i));
		if ((object == null) || (!(object instanceof XmlMap)))
			return new XmlMap();
		else
			return ((XmlMap) object);
	}

	public String getAuthenticString(String key) {
		Object object = get(key);
		if (object == null)
			return null;			
		String text = (String) object;
		if (emptyTagLeadsToNull)
		  if (text.length() == 0)
			 return null;
		return (String) object;
	}

	public String getString(String key) {
		Object object = get(key);
		if (object == null)
			return null;
		String text = ((String) object).trim();
		if (emptyTagLeadsToNull)
			if (text.length() == 0)
				return null;
		return text;
	}

	public HashMap getStringMap(
		String basicTag,
		String keyTag,
		String valueTag) {
		HashMap map = new HashMap();
		XmlMap nextGroup = null;
		for (int i = 0;
			!(nextGroup = getXmlMap(basicTag + (i == 0 ? "" : "" + i)))
				.isEmpty();
			i++) {
			String key = nextGroup.getString(keyTag);
			String value = nextGroup.getString(valueTag);
			if (value == null) {
				if (nullElementLeadsToNullObject)
					return null;
			} else
				map.put(key, value);
		}
		if (map.isEmpty())
			return null;
		else
			return map;
	}

	public HashSet getStringSet(String basicTag, String valueTag) {
		HashSet set = new HashSet();
		XmlMap nextGroup = null;
		for (int i = 0;
			!(nextGroup = getXmlMap(basicTag + (i == 0 ? "" : "" + i)))
				.isEmpty();
			i++) {
			String value = nextGroup.getString(valueTag);
			if (value == null) {
				if (nullElementLeadsToNullObject)
					return null;
			} else
				set.add(value);
		}
		if (set.isEmpty())
			return null;
		else
			return set;
	}

	public HashSet getAuthenticStringSet(String basicTag, String valueTag) {
		HashSet set = new HashSet();
		XmlMap nextGroup = null;
		for (int i = 0;
			!(nextGroup = getXmlMap(basicTag + (i == 0 ? "" : "" + i)))
				.isEmpty();
			i++) {
			String value = nextGroup.getAuthenticString(valueTag);
			set.add(value);
		}
		if (set.isEmpty())
			return null;
		else
			return set;
	}

	public boolean getBoolean(String key) {
		Object object = get(key);
		if (object == null)
			return false;
		String text = ((String) object).trim();
		if (emptyTagLeadsToNull)
			if (text.length() == 0)
				return false;
		return (new Boolean(text).booleanValue());

	}

	public Boolean getBooleanObject(String key) {
		Object object = get(key);
		if (object == null)
			return null;
		String text = ((String) object).trim();
		if (emptyTagLeadsToNull)
			if (text.length() == 0)
				return null;
		return (new Boolean(text));

	}

	public byte getByte(String key) {
		Object object = get(key);
		if (object == null)
			return 0;
		String text = ((String) object).trim();
		if (emptyTagLeadsToNull)
			if (text.length() == 0)
				return 0;
		try {
			return (new Byte(text).byteValue());
		} catch (NumberFormatException ex) {
			if (formatErrorLeadsToNull)
				return 0;
			else
				throw ex;
		}
	}

	public double getDouble(String key) {
		Object object = get(key);
		if (object == null)
			return 0;
		String text = ((String) object).trim();
		if (emptyTagLeadsToNull)
			if (text.length() == 0)
				return 0;
		try {
			return (new Double(text).doubleValue());
		} catch (NumberFormatException ex) {
			if (formatErrorLeadsToNull)
				return 0;
			else
				throw ex;
		}
	}

	public float getFloat(String key) {
		Object object = get(key);
		if (object == null)
			return 0;
		String text = ((String) object).trim();
		if (emptyTagLeadsToNull)
			if (text.length() == 0)
				return 0;
		try {
			return (new Float(text).floatValue());
		} catch (NumberFormatException ex) {
			if (formatErrorLeadsToNull)
				return 0;
			else
				throw ex;
		}
	}

	public int getInt(String key) {
		Object object = get(key);
		if (object == null)
			return 0;
		String text = ((String) object).trim();
		if (emptyTagLeadsToNull)
			if (text.length() == 0)
				return 0;
		try {
			return (new Integer(text).intValue());
		} catch (NumberFormatException ex) {
			if (formatErrorLeadsToNull)
				return 0;
			else
				throw ex;
		}
	}

	public long getLong(String key) {
		Object object = get(key);
		if (object == null)
			return 0;
		String text = ((String) object).trim();
		if (emptyTagLeadsToNull)
			if (text.length() == 0)
				return 0;
		try {
			return (new Long(text).longValue());
		} catch (NumberFormatException ex) {
			if (formatErrorLeadsToNull)
				return 0;
			else
				throw ex;
		}
	}

	public short getShort(String key) {
		Object object = get(key);
		if (object == null)
			return 0;
		String text = ((String) object).trim();
		if (emptyTagLeadsToNull)
			if (text.length() == 0)
				return 0;
		try {
			return (new Short(text).shortValue());
		} catch (NumberFormatException ex) {
			if (formatErrorLeadsToNull)
				return 0;
			else
				throw ex;
		}
	}

	public BigDecimal getBigDecimal(String key) {
		Object object = get(key);
		if (object == null)
			return null;
		String text = ((String) object).trim();
		if (emptyTagLeadsToNull)
			if (text.length() == 0)
				return null;
		try {
			return (new BigDecimal(text));
		} catch (NumberFormatException ex) {
			if (formatErrorLeadsToNull)
				return null;
			else
				throw ex;
		}

	}

	public HashMap getBigDecimalMap(
		String basicTag,
		String keyTag,
		String valueTag) {
		HashMap map = new HashMap();
		XmlMap nextGroup = null;
		for (int i = 0;
			!(nextGroup = getXmlMap(basicTag + (i == 0 ? "" : "" + i)))
				.isEmpty();
			i++) {
			String key = nextGroup.getString(keyTag);
			BigDecimal value = nextGroup.getBigDecimal(valueTag);
			if (value == null) {
				if (nullElementLeadsToNullObject)
					return null;
			} else
				map.put(key, value);
		}
		if (map.isEmpty())
			return null;
		else
			return map;
	}

	public HashSet getBigDecimalSet(String basicTag, String valueTag) {
		HashSet set = new HashSet();
		XmlMap nextGroup = null;
		for (int i = 0;
			!(nextGroup = getXmlMap(basicTag + (i == 0 ? "" : "" + i)))
				.isEmpty();
			i++) {
			BigDecimal value = nextGroup.getBigDecimal(valueTag);
			if (value == null) {
				if (nullElementLeadsToNullObject)
					return null;
			} else
				set.add(value);
		}
		if (set.isEmpty())
			return null;
		else
			return set;
	}

	public BigInteger getBigInteger(String key) {
		Object object = get(key);
		if (object == null)
			return null;
		String text = ((String) object).trim();
		if (emptyTagLeadsToNull)
			if (text.length() == 0)
				return null;
		try {
			return (new BigInteger(text));
		} catch (NumberFormatException ex) {
			if (formatErrorLeadsToNull)
				return null;
			else
				throw ex;
		}

	}

	public HashMap getBigIntegerMap(
		String basicTag,
		String keyTag,
		String valueTag) {
		HashMap map = new HashMap();
		XmlMap nextGroup = null;
		for (int i = 0;
			!(nextGroup = getXmlMap(basicTag + (i == 0 ? "" : "" + i)))
				.isEmpty();
			i++) {
			String key = nextGroup.getString(keyTag);
			BigInteger value = nextGroup.getBigInteger(valueTag);
			if (value == null) {
				if (nullElementLeadsToNullObject)
					return null;
			} else
				map.put(key, value);
		}
		if (map.isEmpty())
			return null;
		else
			return map;
	}

	public HashSet getBigIntegerSet(String basicTag, String valueTag) {
		HashSet set = new HashSet();
		XmlMap nextGroup = null;
		for (int i = 0;
			!(nextGroup = getXmlMap(basicTag + (i == 0 ? "" : "" + i)))
				.isEmpty();
			i++) {
			BigInteger value = nextGroup.getBigInteger(valueTag);
			if (value == null) {
				if (nullElementLeadsToNullObject)
					return null;
			} else
				set.add(value);
		}
		if (set.isEmpty())
			return null;
		else
			return set;
	}

	public Byte getByteObject(String key) {
		Object object = get(key);
		if (object == null)
			return null;
		String text = ((String) object).trim();
		if (emptyTagLeadsToNull)
			if (text.length() == 0)
				return null;
		try {
			return (new Byte(text));
		} catch (NumberFormatException ex) {
			if (formatErrorLeadsToNull)
				return null;
			else
				throw ex;
		}

	}

	public HashMap getByteMap(
		String basicTag,
		String keyTag,
		String valueTag) {
		HashMap map = new HashMap();
		XmlMap nextGroup = null;
		for (int i = 0;
			!(nextGroup = getXmlMap(basicTag + (i == 0 ? "" : "" + i)))
				.isEmpty();
			i++) {
			String key = nextGroup.getString(keyTag);
			Byte value = nextGroup.getByteObject(valueTag);
			if (value == null) {
				if (nullElementLeadsToNullObject)
					return null;
			} else
				map.put(key, value);
		}
		if (map.isEmpty())
			return null;
		else
			return map;
	}

	public HashSet getByteSet(String basicTag, String valueTag) {
		HashSet set = new HashSet();
		XmlMap nextGroup = null;
		for (int i = 0;
			!(nextGroup = getXmlMap(basicTag + (i == 0 ? "" : "" + i)))
				.isEmpty();
			i++) {
			Byte value = nextGroup.getByteObject(valueTag);
			if (value == null) {
				if (nullElementLeadsToNullObject)
					return null;
			} else
				set.add(value);
		}
		if (set.isEmpty())
			return null;
		else
			return set;
	}

	public Double getDoubleObject(String key) {
		Object object = get(key);
		if (object == null)
			return null;
		String text = ((String) object).trim();
		if (emptyTagLeadsToNull)
			if (text.length() == 0)
				return null;
		try {
			return (new Double(text));
		} catch (NumberFormatException ex) {
			if (formatErrorLeadsToNull)
				return null;
			else
				throw ex;
		}

	}

	public HashMap getDoubleMap(
		String basicTag,
		String keyTag,
		String valueTag) {
		HashMap map = new HashMap();
		XmlMap nextGroup = null;
		for (int i = 0;
			!(nextGroup = getXmlMap(basicTag + (i == 0 ? "" : "" + i)))
				.isEmpty();
			i++) {
			String key = nextGroup.getString(keyTag);
			Double value = nextGroup.getDoubleObject(valueTag);
			if (value == null) {
				if (nullElementLeadsToNullObject)
					return null;
			} else
				map.put(key, value);
		}
		if (map.isEmpty())
			return null;
		else
			return map;
	}

	public HashSet getDoubleSet(String basicTag, String valueTag) {
		HashSet set = new HashSet();
		XmlMap nextGroup = null;
		for (int i = 0;
			!(nextGroup = getXmlMap(basicTag + (i == 0 ? "" : "" + i)))
				.isEmpty();
			i++) {
			Double value = nextGroup.getDoubleObject(valueTag);
			if (value == null) {
				if (nullElementLeadsToNullObject)
					return null;
			} else
				set.add(value);
		}
		if (set.isEmpty())
			return null;
		else
			return set;
	}

	public Float getFloatObject(String key) {
		Object object = get(key);
		if (object == null)
			return null;
		String text = ((String) object).trim();
		if (emptyTagLeadsToNull)
			if (text.length() == 0)
				return null;
		try {
			return (new Float(text));
		} catch (NumberFormatException ex) {
			if (formatErrorLeadsToNull)
				return null;
			else
				throw ex;
		}

	}

	public HashMap getFloatMap(
		String basicTag,
		String keyTag,
		String valueTag) {
		HashMap map = new HashMap();
		XmlMap nextGroup = null;
		for (int i = 0;
			!(nextGroup = getXmlMap(basicTag + (i == 0 ? "" : "" + i)))
				.isEmpty();
			i++) {
			String key = nextGroup.getString(keyTag);
			Float value = nextGroup.getFloatObject(valueTag);
			if (value == null) {
				if (nullElementLeadsToNullObject)
					return null;
			} else
				map.put(key, value);
		}
		if (map.isEmpty())
			return null;
		else
			return map;
	}

	public HashSet getFloatSet(String basicTag, String valueTag) {
		HashSet set = new HashSet();
		XmlMap nextGroup = null;
		for (int i = 0;
			!(nextGroup = getXmlMap(basicTag + (i == 0 ? "" : "" + i)))
				.isEmpty();
			i++) {
			Float value = nextGroup.getFloatObject(valueTag);
			if (value == null) {
				if (nullElementLeadsToNullObject)
					return null;
			} else
				set.add(value);
		}
		if (set.isEmpty())
			return null;
		else
			return set;
	}

	public Integer getIntegerObject(String key) {
		Object object = get(key);
		if (object == null)
			return null;
		String text = ((String) object).trim();
		if (emptyTagLeadsToNull)
			if (text.length() == 0)
				return null;
		try {
			return (new Integer(text));
		} catch (NumberFormatException ex) {
			if (formatErrorLeadsToNull)
				return null;
			else
				throw ex;
		}

	}

	public HashMap getIntegerMap(
		String basicTag,
		String keyTag,
		String valueTag) {
		HashMap map = new HashMap();
		XmlMap nextGroup = null;
		for (int i = 0;
			!(nextGroup = getXmlMap(basicTag + (i == 0 ? "" : "" + i)))
				.isEmpty();
			i++) {
			String key = nextGroup.getString(keyTag);
			Integer value = nextGroup.getIntegerObject(valueTag);
			if (value == null) {
				if (nullElementLeadsToNullObject)
					return null;
			} else
				map.put(key, value);
		}
		if (map.isEmpty())
			return null;
		else
			return map;
	}

	public HashSet getIntegerSet(String basicTag, String valueTag) {
		HashSet set = new HashSet();
		XmlMap nextGroup = null;
		for (int i = 0;
			!(nextGroup = getXmlMap(basicTag + (i == 0 ? "" : "" + i)))
				.isEmpty();
			i++) {
			Integer value = nextGroup.getIntegerObject(valueTag);
			if (value == null) {
				if (nullElementLeadsToNullObject)
					return null;
			} else
				set.add(value);
		}
		if (set.isEmpty())
			return null;
		else
			return set;
	}

	public Long getLongObject(String key) {
		Object object = get(key);
		if (object == null)
			return null;
		String text = ((String) object).trim();
		if (emptyTagLeadsToNull)
			if (text.length() == 0)
				return null;
		try {
			return (new Long(text));
		} catch (NumberFormatException ex) {
			if (formatErrorLeadsToNull)
				return null;
			else
				throw ex;
		}

	}

	public HashMap getLongMap(
		String basicTag,
		String keyTag,
		String valueTag) {
		HashMap map = new HashMap();
		XmlMap nextGroup = null;
		for (int i = 0;
			!(nextGroup = getXmlMap(basicTag + (i == 0 ? "" : "" + i)))
				.isEmpty();
			i++) {
			String key = nextGroup.getString(keyTag);
			Long value = nextGroup.getLongObject(valueTag);
			if (value == null) {
				if (nullElementLeadsToNullObject)
					return null;
			} else
				map.put(key, value);
		}
		if (map.isEmpty())
			return null;
		else
			return map;
	}

	public HashSet getLongSet(String basicTag, String valueTag) {
		HashSet set = new HashSet();
		XmlMap nextGroup = null;
		for (int i = 0;
			!(nextGroup = getXmlMap(basicTag + (i == 0 ? "" : "" + i)))
				.isEmpty();
			i++) {
			Long value = nextGroup.getLongObject(valueTag);
			if (value == null) {
				if (nullElementLeadsToNullObject)
					return null;
			} else
				set.add(value);
		}
		if (set.isEmpty())
			return null;
		else
			return set;
	}

	public Short getShortObject(String key) {
		Object object = get(key);
		if (object == null)
			return null;
		String text = ((String) object).trim();
		if (emptyTagLeadsToNull)
			if (text.length() == 0)
				return null;
		try {
			return (new Short(text));
		} catch (NumberFormatException ex) {
			if (formatErrorLeadsToNull)
				return null;
			else
				throw ex;
		}

	}

	public HashMap getShortMap(
		String basicTag,
		String keyTag,
		String valueTag) {
		HashMap map = new HashMap();
		XmlMap nextGroup = null;
		for (int i = 0;
			!(nextGroup = getXmlMap(basicTag + (i == 0 ? "" : "" + i)))
				.isEmpty();
			i++) {
			String key = nextGroup.getString(keyTag);
			Short value = nextGroup.getShortObject(valueTag);
			if (value == null) {
				if (nullElementLeadsToNullObject)
					return null;
			} else
				map.put(key, value);
		}
		if (map.isEmpty())
			return null;
		else
			return map;
	}

	public HashSet getShortSet(String basicTag, String valueTag) {
		HashSet set = new HashSet();
		XmlMap nextGroup = null;
		for (int i = 0;
			!(nextGroup = getXmlMap(basicTag + (i == 0 ? "" : "" + i)))
				.isEmpty();
			i++) {
			Short value = nextGroup.getShortObject(valueTag);
			if (value == null) {
				if (nullElementLeadsToNullObject)
					return null;
			} else
				set.add(value);
		}
		if (set.isEmpty())
			return null;
		else
			return set;
	}

	public java.util.Date getDate(String key) {
		Object object = get(key);
		if (object == null)
			return null;
		String text = ((String) object).trim();
		if (emptyTagLeadsToNull)
			if (text.length() == 0)
				return null;
		try {
			return FDATE.parse(text);
		} catch (Exception ex) {
			if (formatErrorLeadsToNull)
				return null;
			else
				throw new IllegalArgumentException();
		}

	}

	public HashMap getDateMap(
		String basicTag,
		String keyTag,
		String valueTag) {
		HashMap map = new HashMap();
		XmlMap nextGroup = null;
		for (int i = 0;
			!(nextGroup = getXmlMap(basicTag + (i == 0 ? "" : "" + i)))
				.isEmpty();
			i++) {
			String key = nextGroup.getString(keyTag);
			java.util.Date value = nextGroup.getDate(valueTag);
			if (value == null) {
				if (nullElementLeadsToNullObject)
					return null;
			} else
				map.put(key, value);
		}
		if (map.isEmpty())
			return null;
		else
			return map;
	}

	public HashSet getDateSet(String basicTag, String valueTag) {
		HashSet set = new HashSet();
		XmlMap nextGroup = null;
		for (int i = 0;
			!(nextGroup = getXmlMap(basicTag + (i == 0 ? "" : "" + i)))
				.isEmpty();
			i++) {
			java.util.Date value = nextGroup.getDate(valueTag);
			if (value == null) {
				if (nullElementLeadsToNullObject)
					return null;
			} else
				set.add(value);
		}
		if (set.isEmpty())
			return null;
		else
			return set;
	}

	public java.sql.Date getSqlDate(String key) {
		Object object = get(key);
		if (object == null)
			return null;
		String text = ((String) object).trim();
		if (emptyTagLeadsToNull)
			if (text.length() == 0)
				return null;
		try {
			return new java.sql.Date(FDATE.parse(text).getTime());
		} catch (Exception ex) {
			if (formatErrorLeadsToNull)
				return null;
			else
				throw new IllegalArgumentException();
		}

	}

	public HashMap getSqlDateMap(
		String basicTag,
		String keyTag,
		String valueTag) {
		HashMap map = new HashMap();
		XmlMap nextGroup = null;
		for (int i = 0;
			!(nextGroup = getXmlMap(basicTag + (i == 0 ? "" : "" + i)))
				.isEmpty();
			i++) {
			String key = nextGroup.getString(keyTag);
			java.sql.Date value = nextGroup.getSqlDate(valueTag);
			if (value == null) {
				if (nullElementLeadsToNullObject)
					return null;
			} else
				map.put(key, value);
		}
		if (map.isEmpty())
			return null;
		else
			return map;
	}

	public HashSet getSqlDateSet(String basicTag, String valueTag) {
		HashSet set = new HashSet();
		XmlMap nextGroup = null;
		for (int i = 0;
			!(nextGroup = getXmlMap(basicTag + (i == 0 ? "" : "" + i)))
				.isEmpty();
			i++) {
			java.sql.Date value = nextGroup.getSqlDate(valueTag);
			if (value == null) {
				if (nullElementLeadsToNullObject)
					return null;
			} else
				set.add(value);
		}
		if (set.isEmpty())
			return null;
		else
			return set;
	}

	public Time getTime(String key) {
		Object object = get(key);
		if (object == null)
			return null;
		String text = ((String) object).trim();
		if (emptyTagLeadsToNull)
			if (text.length() == 0)
				return null;
		try {
			return new Time(FTIME.parse(text).getTime());
		} catch (Exception ex) {
			if (formatErrorLeadsToNull)
				return null;
			else
				throw new IllegalArgumentException();
		}

	}

	public HashMap getTimeMap(
		String basicTag,
		String keyTag,
		String valueTag) {
		HashMap map = new HashMap();
		XmlMap nextGroup = null;
		for (int i = 0;
			!(nextGroup = getXmlMap(basicTag + (i == 0 ? "" : "" + i)))
				.isEmpty();
			i++) {
			String key = nextGroup.getString(keyTag);
			Time value = nextGroup.getTime(valueTag);
			if (value == null) {
				if (nullElementLeadsToNullObject)
					return null;
			} else
				map.put(key, value);
		}
		if (map.isEmpty())
			return null;
		else
			return map;
	}

	public HashSet getTimeSet(String basicTag, String valueTag) {
		HashSet set = new HashSet();
		XmlMap nextGroup = null;
		for (int i = 0;
			!(nextGroup = getXmlMap(basicTag + (i == 0 ? "" : "" + i)))
				.isEmpty();
			i++) {
			Time value = nextGroup.getTime(valueTag);
			if (value == null) {
				if (nullElementLeadsToNullObject)
					return null;
			} else
				set.add(value);
		}
		if (set.isEmpty())
			return null;
		else
			return set;
	}

	public Timestamp getTimestamp(String key) {
		Object object = get(key);
		if (object == null)
			return null;
		String text = ((String) object).trim();
		if (emptyTagLeadsToNull)
			if (text.length() == 0)
				return null;
		try {
			return Timestamp.valueOf(text);
		} catch (IllegalArgumentException ex) {
			if (formatErrorLeadsToNull)
				return null;
			else
				throw ex;
		}

	}

	public HashMap getTimestampMap(
		String basicTag,
		String keyTag,
		String valueTag) {
		HashMap map = new HashMap();
		XmlMap nextGroup = null;
		for (int i = 0;
			!(nextGroup = getXmlMap(basicTag + (i == 0 ? "" : "" + i)))
				.isEmpty();
			i++) {
			String key = nextGroup.getString(keyTag);
			Timestamp value = nextGroup.getTimestamp(valueTag);
			if (value == null) {
				if (nullElementLeadsToNullObject)
					return null;
			} else
				map.put(key, value);
		}
		if (map.isEmpty())
			return null;
		else
			return map;
	}

	public HashSet getTimestampSet(String basicTag, String valueTag) {
		HashSet set = new HashSet();
		XmlMap nextGroup = null;
		for (int i = 0;
			!(nextGroup = getXmlMap(basicTag + (i == 0 ? "" : "" + i)))
				.isEmpty();
			i++) {
			Timestamp value = nextGroup.getTimestamp(valueTag);
			if (value == null) {
				if (nullElementLeadsToNullObject)
					return null;
			} else
				set.add(value);
		}
		if (set.isEmpty())
			return null;
		else
			return set;
	}

}
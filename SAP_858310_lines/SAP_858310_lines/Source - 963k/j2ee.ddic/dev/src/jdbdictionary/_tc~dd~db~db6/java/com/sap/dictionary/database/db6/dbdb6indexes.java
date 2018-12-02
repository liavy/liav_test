package com.sap.dictionary.database.db6;

import com.sap.dictionary.database.dbs.DbIndexIterator;
import com.sap.dictionary.database.dbs.DbIndexes;

public class DbDb6Indexes extends DbIndexes
{

	private static final int MAX_INDEXES = 32767;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sap.dictionary.database.dbs.DbIndexes#checkSpecificContent()
	 */
	@Override
	public boolean checkSpecificContent()
	{
		// check that only one index is a clustered index
		DbIndexIterator iterator = iterator();
		DbDb6Index index = null;
		int clusterIndex = 0;

		while (iterator.hasNext())
		{
			index = (DbDb6Index) iterator.next();
			if (index.isClustered())
				clusterIndex++;
		}
		if (clusterIndex > 1)
			return false;

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sap.dictionary.database.dbs.DbIndexes#checkNumber()
	 */
	@Override
	public boolean checkNumber()
	{
		if (this.size() > MAX_INDEXES)
		  return false;
		return true;
	}
}

package org.mokai.persist.jdbc;

/**
 * Allows the initialization and custom dialect of a database. An implementation is required for each supported database.
 * 
 * @author German Escobar
 */
public interface SqlEngine {
	
	/**
	 * Initializes the database. Should execute only if {@link #isInitialized()} is false.
	 * 
	 * @throws Exception
	 */
	void init() throws Exception;
	
	/**
	 * @return true if the init method has already been called, false otherwise.
	 */
	boolean isInitialized();
	
	/**
	 * Adds the limit clause to a query.
	 * 
	 * @param query the query to which we are adding the limit clause.
	 * @param offset the offset from which we are showing the records.
	 * @param numRows the number of rows to show from offset.
	 */
	void addLimitToQuery(StringBuffer query, int offset, int numRows);
	
}

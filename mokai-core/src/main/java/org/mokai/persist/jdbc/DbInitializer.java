package org.mokai.persist.jdbc;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

/**
 * 
 * @author German Escobar
 */
public class DbInitializer {
	
	private DataSource dataSource;
	
	private String dbSchema;
	
	private Map<String,String> tableScripts = new HashMap<String,String>();
	
	/**
	 * Checks if the tables from {@link #getTableScripts()} already exists. If they
	 * don't, it creates them using the supplied script. 
	 * 
	 * @throws SQLException
	 * @throws IllegalStateException if the dataSource is null
	 */
	public void initialize() throws SQLException, IllegalStateException {
		
		if (dataSource == null) {
			throw new IllegalStateException("no provided dataSource");
		}
		
		// iterate through all the tables and create them
		for (Map.Entry<String,String> entry : getTableScripts().entrySet()) {
			
			String tableName = entry.getKey();
			String tableScript = entry.getValue();
			
			JdbcHelper.checkCreateTable(dataSource, getDbSchema(), tableName, tableScript);
		}
		
	}
	
	/**
	 * Usually the database name.
	 * 
	 * @return the database schema to use.
	 */
	public String getDbSchema() {
		return this.dbSchema;
	}
	
	public void setDbSchema(String dbSchema) {
		this.dbSchema = dbSchema;
	}
	
	/**
	 * The key of the map is the name of the table and the value is 
	 * the script to create that table.
	 * 
	 * @return a {@link Map} of table scripts.
	 */
	public Map<String,String> getTableScripts() {
		return this.tableScripts;
	}

	public void setTableScripts(Map<String, String> tableScripts) {
		this.tableScripts = tableScripts;
	}
	
	public void addTableScript(String tableName, String tableScript) {
		tableScripts.put(tableName, tableScript);
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
}

package org.mokai.persist.jdbc.util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

/**
 * Abstract class for initializing database schema.
 * 
 * @author German Escobar
 */
public abstract class DBInitializer {

	private DataSource dataSource;

	public final void init() throws SQLException {
		
		Connection connection = null;
		ResultSet rs = null;
		Statement statement = null;
		
		try {
			connection = dataSource.getConnection();
			
			DatabaseMetaData metadata = connection.getMetaData();
			rs = metadata.getTables("", getDbSchema(), "MESSAGE", null);
			if (!rs.next()) {
				statement = connection.createStatement();
				statement.executeUpdate(messagesTableScript());
			}
			
		} finally {
			if (rs != null) {
				try { rs.close(); } catch (Exception e) { }
			}
			if (statement != null) {
				try { statement.close(); } catch(Exception e) { }
			}
			if (connection != null) {
				try { connection.close(); } catch(Exception e) { }
			}
		}
		
	}
	
	public abstract String messagesTableScript();
	
	public abstract String getDbSchema();

	public final void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
}

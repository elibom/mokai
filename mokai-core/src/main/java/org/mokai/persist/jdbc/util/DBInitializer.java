package org.mokai.persist.jdbc.util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.sql.DataSource;

public abstract class DBInitializer {

	private DataSource dataSource;

	public void init() {
		
		Connection connection = null;
		ResultSet rs = null;
		Statement statement = null;
		
		try {
			connection = dataSource.getConnection();
			
			DatabaseMetaData metadata = connection.getMetaData();
			rs = metadata.getTables("", "mokai", "message", null);
			if (!rs.next()) {
				statement = connection.createStatement();
				statement.executeUpdate(messagesTableScript());
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try { if (rs != null) rs.close(); } catch(Exception e) { }
			try { if (statement != null) statement.close(); } catch(Exception e) { }
			try { if (connection != null) connection.close(); } catch(Exception e) { }
		}
		
	}
	
	public abstract String messagesTableScript();

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
}

package org.mokai.persist.jdbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

/**
 * Helper methods used by the {@link JdbcMessageStore} and other
 * {@link MessageStore} implementations based on JDBC.
 *
 * @author German Escobar
 */
public final class JdbcHelper {

	/**
	 * Hides the public constructor.
	 */
	private JdbcHelper() {}

	/**
	 * Checks if the table exists. If it doesn't, it creates it using the
	 * supplied script.
	 *
	 * @param dataSource the DataSource used to create the connection.
	 * @param schema usually the database name, but can be null.
	 * @param tableName the name of the table to create.
	 * @param creationScript the script to create the exception
	 * @throws SQLException if something goes wrong.
	 */
	public static void checkCreateTable(DataSource dataSource, String schema,
			String tableName, String creationScript) throws SQLException {

		Connection connection = null;
		ResultSet rs = null;
		Statement statement = null;

		try {
			// create the connection and the statement
			connection = dataSource.getConnection();
			statement = connection.createStatement();

			// check if the outbound message table exists or create it
			DatabaseMetaData metadata = connection.getMetaData();
			rs = metadata.getTables("", schema, tableName, null);
			if (!rs.next() && creationScript != null) {
				statement.executeUpdate(creationScript);
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

	/**
	 * Helper method to retrieve the generated id after an insert.
	 *
	 * @param stmt the statement used in the insert.
	 * @return the generated id.
	 * @throws SQLException if something goes wrong
	 */
	public static long retrieveGeneratedId(Statement stmt) throws SQLException {
		ResultSet rsKeys = null;

		try {
			rsKeys = stmt.getGeneratedKeys();
			if (rsKeys.next()) {
				return rsKeys.getLong(1);
			}

			return -1;

		} finally {
			if (rsKeys != null) {
				try { rsKeys.close(); } catch (Exception e) {}
			}
		}
	}
}

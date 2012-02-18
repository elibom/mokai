package org.mokai.persist.jdbc.sms;

import javax.sql.DataSource;

import org.mokai.persist.jdbc.JdbcHelper;
import org.mokai.persist.jdbc.SqlEngine;

public class MySqlEngine implements SqlEngine {
	
	private DataSource dataSource;
	
	private String schema = "mokai";
	
	private boolean initialized;

	@Override
	public void init() throws Exception {
		
		if (initialized) {
			return;
		}
		
		JdbcHelper.checkCreateTable(dataSource, getSchema(), ConnectionsSmsHandler.DEFAULT_TABLENAME, getConnectionsCreateScript());
		JdbcHelper.checkCreateTable(dataSource, getSchema(), ApplicationsSmsHandler.DEFAULT_TABLENAME, getApplicationsCreateScript());
		
		initialized = true;
	}

	@Override
	public boolean isInitialized() {
		return initialized;
	}

	public String getSchema() {
		return schema;
	}
	
	public void setSchema(String schema) {
		this.schema = schema;
	}

	@Override
	public String addLimitToQuery(String query, int offset, int numRows) {
		return query + " LIMIT " + offset + "," + numRows;
	}
	
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	protected String getConnectionsCreateScript() {
		return "CREATE TABLE " + ConnectionsSmsHandler.DEFAULT_TABLENAME + " (" +
					"id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
					"reference VARCHAR(100), " +
					"source VARCHAR(30) NOT NULL, " +
					"destination VARCHAR(30), " +
					"status TINYINT NOT NULL, " +
					"smsc_to VARCHAR(50), " +
					"smsc_from VARCHAR(50), " +
					"smsc_text VARCHAR(1000), " +
					"smsc_sequencenumber INT, " +
					"smsc_messageid VARCHAR(50), " +
					"smsc_commandstatus SMALLINT, " +
					"smsc_receiptstatus VARCHAR(20), " +
					"smsc_receipttime DATETIME, " +
					"other VARCHAR(1000), " +
					"creation_time DATETIME NOT NULL, " +
					"modification_time DATETIME) ENGINE=MyISAM";
	}
	
	protected String getApplicationsCreateScript() {
		return "CREATE TABLE " + ApplicationsSmsHandler.DEFAULT_TABLENAME + " (" +
					"id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
					"reference VARCHAR(100), " +
					"source VARCHAR(30) NOT NULL, " +
					"destination VARCHAR(30), " +
					"status TINYINT NOT NULL, " +
					"smsc_to VARCHAR(50), " +
					"smsc_from VARCHAR(50), " +
					"smsc_text VARCHAR(1000), " +
					"smsc_sequencenumber INT, " +
					"smsc_messageid VARCHAR(50), " +
					"smsc_commandstatus SMALLINT, " +
					"smsc_receiptstatus VARCHAR(20), " +
					"smsc_receipttime DATETIME, " +
					"other VARCHAR(1000), " +
					"creation_time DATETIME NOT NULL, " +
					"modification_time DATETIME) ENGINE=MyISAM";
	}

}

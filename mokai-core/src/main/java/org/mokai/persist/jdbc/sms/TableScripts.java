package org.mokai.persist.jdbc.sms;

/**
 * Enumeration of provided scripts for SMS tables.
 * 
 * @author German Escobar
 */
public enum TableScripts {
	
	/**
	 * Derby script to create an outbound table for sms messages.
	 */
	DERBY_OUTBOUND(OutboundSmsHandler.DEFAULT_TABLENAME, 
			"CREATE TABLE " + OutboundSmsHandler.DEFAULT_TABLENAME + " (" +
			"id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
			"account VARCHAR(30), " +
			"reference VARCHAR(100), " +
			"source VARCHAR(30) NOT NULL, " +
			"sourcetype SMALLINT NOT NULL, " +
			"destination VARCHAR(30), " +
			"destinationtype SMALLINT, " +
			"status SMALLINT NOT NULL, " +
			"smsc_to VARCHAR(30), " +
			"smsc_from VARCHAR(30), " +
			"smsc_text VARCHAR(1000), " +
			"smsc_messageid VARCHAR(50), " +
			"smsc_commandstatus INTEGER, " +
			"smsc_receiptstatus VARCHAR(20), " +
			"smsc_receipttime TIMESTAMP, " +
			"creation_time TIMESTAMP NOT NULL, " +
			"modification_time TIMESTAMP)"), 
			
	/**
	 * MySql script to create an outbound table for sms messages.
	 */
	MYSQL_OUTBOUND(OutboundSmsHandler.DEFAULT_TABLENAME, 
			"CREATE TABLE " + OutboundSmsHandler.DEFAULT_TABLENAME + " (" +
			"id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
			"account VARCHAR(30), " +
			"reference VARCHAR(100), " +
			"source VARCHAR(30) NOT NULL, " +
			"sourcetype TINYINT NOT NULL, " +
			"destination VARCHAR(30), " +
			"destinationtype TINYINT, " +
			"status TINYINT NOT NULL, " +
			"smsc_to VARCHAR(30), " +
			"smsc_from VARCHAR(30), " +
			"smsc_text VARCHAR(1000), " +
			"smsc_messageid VARCHAR(50), " +
			"smsc_commandstatus TINYINT, " +
			"smsc_receiptstatus VARCHAR(20), " +
			"smsc_receipttime DATETIME, " +
			"creation_time DATETIME NOT NULL, " +
			"modification_time DATETIME)");
	
	private String tableName;
	private String tableScript;
	
	private TableScripts(String tableName, String tableScript) {
		this.tableName = tableName;
		this.tableScript = tableScript;
	}
	
	public String getTableName() {
		return tableName;
	}
	
	public String getTableScript() {
		return tableScript;
	}
}

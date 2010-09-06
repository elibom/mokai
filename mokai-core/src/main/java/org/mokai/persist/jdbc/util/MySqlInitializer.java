package org.mokai.persist.jdbc.util;

/**
 * Initializes the MySql schema. 
 * 
 * @author German Escobar
 */
public class MySqlInitializer extends DBInitializer {

	@Override
	public final String messagesTableScript() {
		String script = "CREATE TABLE message (" +
			"id_message BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
			"account_message VARCHAR(30), " +
			"reference_message VARCHAR(100), " +
			"flow_message TINYINT NOT NULL, " +
			"source_message VARCHAR(30) NOT NULL, " +
			"sourcetype_message TINYINT NOT NULL, " +
			"destination_message VARCHAR(30), " +
			"destinationtype_message TINYINT, " +
			"status_message TINYINT NOT NULL, " +
			"to_message VARCHAR(30), " +
			"from_message VARCHAR(30), " +
			"text_message VARCHAR(255), " +
			"messageid_message VARCHAR(50), " +
			"commandstatus_message INTEGER, " +
			"creation_time DATETIME)";
	
		return script;
	}

	@Override
	public final String getDbSchema() {
		return "mokai";
	}

}

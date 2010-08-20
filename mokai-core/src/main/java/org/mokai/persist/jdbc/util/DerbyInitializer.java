package org.mokai.persist.jdbc.util;

public class DerbyInitializer extends DBInitializer {

	@Override
	public String messagesTableScript() {
		String script = "CREATE TABLE message (" +
			"id_message BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
			"to_message VARCHAR(30), " +
			"from_message VARCHAR(30), " +
			"text_message VARCHAR(255), " +
			"payload_message VARCHAR(255), " +
			"status_message TINYINT, " +
			"creation_time DATETIME)";
		
		return script;
	}
	
	
}

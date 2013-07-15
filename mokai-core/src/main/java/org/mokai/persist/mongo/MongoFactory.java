package org.mokai.persist.mongo;

import java.net.URISyntaxException;

import org.springframework.beans.factory.FactoryBean;

import com.mongodb.DB;
import com.mongodb.MongoURI;

/**
 * Utility class used in the Spring context configuration.
 *
 * @author German Escobar
 */
public class MongoFactory implements FactoryBean<DB> {

	private MongoURI mongoUri;

	public MongoFactory(String uri) throws URISyntaxException {
		this.mongoUri = new MongoURI(uri);
	}

	@Override
	public DB getObject() throws Exception {
		DB db = mongoUri.connectDB();
		db.authenticate(mongoUri.getUsername(), mongoUri.getPassword());

		return db;
	}

	@Override
	public Class<?> getObjectType() {
		return DB.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

}

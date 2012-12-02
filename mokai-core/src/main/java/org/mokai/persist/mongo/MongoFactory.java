package org.mokai.persist.mongo;

import org.springframework.beans.factory.FactoryBean;

import com.mongodb.DB;
import com.mongodb.Mongo;

/**
 * Utility class used in the Spring context configuration.
 * 
 * @author German Escobar
 */
public class MongoFactory implements FactoryBean<DB> {
	
	private Mongo mongo;
	private String name;

	@Override
	public DB getObject() throws Exception {
		return mongo.getDB(name);
	}

	@Override
	public Class<?> getObjectType() {
		return DB.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	public void setMongo(Mongo mongo) {
		this.mongo = mongo;
	}

	public void setName(String name) {
		this.name = name;
	}

}

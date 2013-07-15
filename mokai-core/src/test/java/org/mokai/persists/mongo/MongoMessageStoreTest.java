package org.mokai.persists.mongo;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

import org.bson.types.ObjectId;
import org.mokai.Message;
import org.mokai.Message.Direction;
import org.mokai.persist.MessageCriteria;
import org.mokai.persist.RejectedException;
import org.mokai.persist.mongo.MongoMessageStore;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;

/**
 *
 * @author German Escobar
 */
public class MongoMessageStoreTest {

	private static final String DATABASE_NAME = "embedded";

	private MongodProcess mongod;
	private Mongo mongo;
	private DB db;

	@BeforeMethod
	public void setup() throws Exception {
	    MongodStarter runtime = MongodStarter.getDefaultInstance();
	    MongodExecutable mongodExe = runtime.prepare( new MongodConfig(Version.V2_3_0, 12345,
	    		Network.localhostIsIPv6()) );
	    mongod = mongodExe.start();
	    mongo = new Mongo("localhost", 12345);
	    db = mongo.getDB(DATABASE_NAME);
	}

	@AfterMethod
	public void tearDown() throws Exception {
		if (mongod != null) {
			mongod.stop();
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldSaveConnectionMessage() throws Exception {
		MongoMessageStore store = new MongoMessageStore();
		store.setMongo(db);

		Message message = new Message();
		message.setDirection(Direction.TO_CONNECTIONS);
		message.setSource("source");
		message.setDestination("destination");

		message.setProperty("to", "3001111111");
		message.setProperty("from", "3542");
		message.setProperty("text", "this is a test");

		store.saveOrUpdate(message); // try to save the message

		Assert.assertNotNull(message.getId());

		DBCollection col = db.getCollection(MongoMessageStore.CONNECTIONS_MSGS);
		BasicDBObject obj = (BasicDBObject) col.findOne();

		Assert.assertNotNull(obj);
		Assert.assertEquals( (byte) obj.getInt("status"), Message.STATUS_CREATED );
		Assert.assertEquals( obj.getString("source"), "source" );
		Assert.assertEquals( obj.getString("destination"), "destination");
		Assert.assertNotNull( obj.get("creationTime") );

		Map<String,Object> properties = (Map<String,Object>) obj.get("properties");
		Assert.assertNotNull(properties);
		Assert.assertEquals( properties.size(), 3 );
		Assert.assertEquals( properties.get("to"), "3001111111" );
		Assert.assertEquals( properties.get("from"), "3542");
		Assert.assertEquals( properties.get("text"), "this is a test" );
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldSaveApplicationMessage() throws Exception {
		MongoMessageStore store = new MongoMessageStore();
		store.setMongo(db);

		Message message = new Message();
		message.setDirection(Direction.TO_APPLICATIONS);
		message.setSource("source");
		message.setDestination("destination");
		message.setProperty("to", "3001111111");
		message.setProperty("from", "3542");
		message.setProperty("text", "this is a test");

		store.saveOrUpdate(message);

		Assert.assertNotNull(message.getId());

		DBCollection col = db.getCollection(MongoMessageStore.APPLICATIONS_MSGS);
		BasicDBObject obj = (BasicDBObject) col.findOne();

		Assert.assertNotNull(obj);
		Assert.assertEquals( (byte) obj.getInt("status"), Message.STATUS_CREATED );
		Assert.assertEquals( obj.getString("source"), "source" );
		Assert.assertEquals( obj.getString("destination"), "destination");
		Assert.assertNotNull( obj.get("creationTime") );

		Map<String,Object> properties = (Map<String,Object>) obj.get("properties");
		Assert.assertNotNull(properties);
		Assert.assertEquals( properties.size(), 3 );
		Assert.assertEquals( properties.get("to"), "3001111111" );
		Assert.assertEquals( properties.get("from"), "3542");
		Assert.assertEquals( properties.get("text"), "this is a test" );
	}

	@Test(expectedExceptions=RejectedException.class)
	public void shouldFailToSaveOrUpdateWithUnknownDirection() throws Exception {
		MongoMessageStore store = new MongoMessageStore();
		store.setMongo(db);

		Message message = new Message();
		store.saveOrUpdate(message);
	}

	@Test(expectedExceptions=IllegalArgumentException.class)
	public void shouldFailToSaveOrUpdateWithNullMessage() throws Exception {
		MongoMessageStore store = new MongoMessageStore();
		store.setMongo(db);

		store.saveOrUpdate(null);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldUpdateConnectionMessage() throws Exception {
		DBCollection col = db.getCollection(MongoMessageStore.CONNECTIONS_MSGS);
		String id = createMessageForUpdate(col);

		MongoMessageStore store = new MongoMessageStore();
		store.setMongo(db);

		Message message = new Message();
		message.setId(id.toString());
		message.setDirection(Direction.TO_CONNECTIONS);
		message.setSource("source");
		message.setDestination("destination");
		message.setReference("54321");
		message.setModificationTime(new Date());
		message.setProperty("to", "3002222222");
		message.setProperty("from", "2453");
		message.setProperty("text", "this is a test");

		store.saveOrUpdate(message);

		BasicDBObject obj = (BasicDBObject) col.findOne();

		Assert.assertNotNull(obj);
		Assert.assertEquals( (byte) obj.getInt("status"), Message.STATUS_CREATED );
		Assert.assertEquals( obj.getString("source"), "source" );
		Assert.assertEquals( obj.getString("destination"), "destination");
		Assert.assertNotNull( obj.get("modificationTime") );

		Map<String,Object> properties = (Map<String,Object>) obj.get("properties");
		Assert.assertNotNull(properties);
		Assert.assertEquals( properties.size(), 3 );
		Assert.assertEquals( properties.get("to"), "3002222222" );
		Assert.assertEquals( properties.get("from"), "2453");
		Assert.assertEquals( properties.get("text"), "this is a test" );
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldUpdateApplicationMessage() throws Exception {
		DBCollection col = db.getCollection(MongoMessageStore.APPLICATIONS_MSGS);
		String id = createMessageForUpdate(col);

		MongoMessageStore store = new MongoMessageStore();
		store.setMongo(db);

		Message message = new Message();
		message.setId(id.toString());
		message.setDirection(Direction.TO_APPLICATIONS);
		message.setSource("source");
		message.setDestination("destination");
		message.setReference("54321");
		message.setModificationTime(new Date());
		message.setProperty("to", "3002222222");
		message.setProperty("from", "2453");
		message.setProperty("text", "this is a test");

		store.saveOrUpdate(message);

		BasicDBObject obj = (BasicDBObject) col.findOne();

		Assert.assertNotNull(obj);
		Assert.assertEquals( (byte) obj.getInt("status"), Message.STATUS_CREATED );
		Assert.assertEquals( obj.getString("source"), "source" );
		Assert.assertEquals( obj.getString("destination"), "destination");
		Assert.assertNotNull( obj.get("modificationTime") );

		Map<String,Object> properties = (Map<String,Object>) obj.get("properties");
		Assert.assertNotNull(properties);
		Assert.assertEquals( properties.size(), 3 );
		Assert.assertEquals( properties.get("to"), "3002222222" );
		Assert.assertEquals( properties.get("from"), "2453");
		Assert.assertEquals( properties.get("text"), "this is a test" );
	}

	@Test
	public void testUpdateStatusWithEmptyCriteria() throws Exception {
		DBCollection applicationsCol = db.getCollection(MongoMessageStore.APPLICATIONS_MSGS);
		DBCollection connectionsCol = db.getCollection(MongoMessageStore.CONNECTIONS_MSGS);

		createMessageForUpdate(applicationsCol, Message.STATUS_FAILED);
		createMessageForUpdate(connectionsCol, Message.STATUS_RETRYING);

		MongoMessageStore store = new MongoMessageStore();
		store.setMongo(db);

		store.updateStatus(new MessageCriteria(), Message.STATUS_PROCESSED);

		BasicDBObject obj = (BasicDBObject) applicationsCol.findOne();
		Assert.assertEquals( (byte) obj.getInt("status"), Message.STATUS_PROCESSED);

		obj = (BasicDBObject) connectionsCol.findOne();
		Assert.assertEquals( (byte) obj.getInt("status"), Message.STATUS_PROCESSED);
	}

	@Test
	public void testUpdateStatusWithNullMessageCriteria() throws Exception {
		DBCollection applicationsCol = db.getCollection(MongoMessageStore.APPLICATIONS_MSGS);
		DBCollection connectionsCol = db.getCollection(MongoMessageStore.CONNECTIONS_MSGS);

		createMessageForUpdate(applicationsCol, Message.STATUS_FAILED);
		createMessageForUpdate(connectionsCol, Message.STATUS_RETRYING);

		MongoMessageStore store = new MongoMessageStore();
		store.setMongo(db);

		store.updateStatus(null, Message.STATUS_PROCESSED);

		BasicDBObject obj = (BasicDBObject) applicationsCol.findOne();
		Assert.assertEquals( (byte) obj.getInt("status"), Message.STATUS_PROCESSED);

		obj = (BasicDBObject) connectionsCol.findOne();
		Assert.assertEquals( (byte) obj.getInt("status"), Message.STATUS_PROCESSED);
	}

	@Test
	public void testUpdateStatusWithConnectionMessageCriteria() throws Exception {
		DBCollection applicationsCol = db.getCollection(MongoMessageStore.APPLICATIONS_MSGS);
		DBCollection connectionsCol = db.getCollection(MongoMessageStore.CONNECTIONS_MSGS);

		createMessageForUpdate(applicationsCol, Message.STATUS_FAILED);
		String failedId = createMessageForUpdate(connectionsCol, Message.STATUS_FAILED);
		String retryingId = createMessageForUpdate(connectionsCol, Message.STATUS_RETRYING);
		String unroutableId = createMessageForUpdate(connectionsCol, Message.STATUS_UNROUTABLE);

		MongoMessageStore store = new MongoMessageStore();
		store.setMongo(db);

		MessageCriteria criteria = new MessageCriteria();
		criteria.setDirection(Direction.TO_CONNECTIONS);
		criteria.addStatus(Message.STATUS_FAILED);
		criteria.addStatus(Message.STATUS_RETRYING);
		store.updateStatus(criteria, Message.STATUS_PROCESSED);

		BasicDBObject obj = (BasicDBObject) applicationsCol.findOne();
		Assert.assertNotNull(obj);
		Assert.assertEquals( (byte) obj.getInt("status"), Message.STATUS_FAILED);

		obj = (BasicDBObject) connectionsCol.findOne( new BasicDBObject("_id", new ObjectId(failedId)) );
		Assert.assertNotNull(obj);
		Assert.assertEquals( (byte) obj.getInt("status"), Message.STATUS_PROCESSED);
		obj = (BasicDBObject) connectionsCol.findOne( new BasicDBObject("_id", new ObjectId(retryingId)) );
		Assert.assertNotNull(obj);
		Assert.assertEquals( (byte) obj.getInt("status"), Message.STATUS_PROCESSED);
		obj = (BasicDBObject) connectionsCol.findOne( new BasicDBObject("_id", new ObjectId(unroutableId)) );
		Assert.assertNotNull(obj);
		Assert.assertEquals( (byte) obj.getInt("status"), Message.STATUS_UNROUTABLE);
	}

	@Test
	public void testUpdateStatusWithApplicationMessageCriteria() throws Exception {
		DBCollection applicationsCol = db.getCollection(MongoMessageStore.APPLICATIONS_MSGS);
		DBCollection connectionsCol = db.getCollection(MongoMessageStore.CONNECTIONS_MSGS);

		createMessageForUpdate(connectionsCol, Message.STATUS_FAILED);
		String failedId = createMessageForUpdate(applicationsCol, Message.STATUS_FAILED);
		String retryingId = createMessageForUpdate(applicationsCol, Message.STATUS_RETRYING);
		String unroutableId = createMessageForUpdate(applicationsCol, Message.STATUS_UNROUTABLE);

		MongoMessageStore store = new MongoMessageStore();
		store.setMongo(db);

		MessageCriteria criteria = new MessageCriteria();
		criteria.setDirection(Direction.TO_APPLICATIONS);
		criteria.addStatus(Message.STATUS_FAILED);
		criteria.addStatus(Message.STATUS_RETRYING);
		store.updateStatus(criteria, Message.STATUS_PROCESSED);

		BasicDBObject obj = (BasicDBObject) connectionsCol.findOne();
		Assert.assertNotNull(obj);
		Assert.assertEquals( (byte) obj.getInt("status"), Message.STATUS_FAILED);

		obj = (BasicDBObject) applicationsCol.findOne( new BasicDBObject("_id", new ObjectId(failedId)) );
		Assert.assertNotNull(obj);
		Assert.assertEquals( (byte) obj.getInt("status"), Message.STATUS_PROCESSED);
		obj = (BasicDBObject) applicationsCol.findOne( new BasicDBObject("_id", new ObjectId(retryingId)) );
		Assert.assertNotNull(obj);
		Assert.assertEquals( (byte) obj.getInt("status"), Message.STATUS_PROCESSED);
		obj = (BasicDBObject) applicationsCol.findOne( new BasicDBObject("_id", new ObjectId(unroutableId)) );
		Assert.assertNotNull(obj);
		Assert.assertEquals( (byte) obj.getInt("status"), Message.STATUS_UNROUTABLE);
	}

	private String createMessageForUpdate(DBCollection col) {
		return createMessageForUpdate(col, Message.STATUS_CREATED);
	}

	private String createMessageForUpdate(DBCollection col, byte status) {
		BasicDBObject doc = new BasicDBObject();
		doc.put("status", status);
		doc.put("source", "source");
		doc.put("reference", "12345");
		doc.put("creationTime", new Date());

		BasicDBObject propertiesDoc = new BasicDBObject();
		propertiesDoc.put("to", "3001111111");
		propertiesDoc.put("from", "3542");
		doc.put("properties", propertiesDoc);

		col.insert(doc);

		ObjectId id = (ObjectId) doc.get( "_id" );
		return id.toString();
	}

	@Test
	public void testListWithEmptyCriteria() throws Exception {
		DBCollection applicationsCol = db.getCollection(MongoMessageStore.APPLICATIONS_MSGS);
		DBCollection connectionsCol = db.getCollection(MongoMessageStore.CONNECTIONS_MSGS);

		createMessageForUpdate(connectionsCol, Message.STATUS_FAILED);
		createMessageForUpdate(applicationsCol, Message.STATUS_FAILED);

		MongoMessageStore store = new MongoMessageStore();
		store.setMongo(db);

		Collection<Message> messages = store.list(new MessageCriteria());
		Assert.assertNotNull(messages);
		Assert.assertEquals(messages.size(), 2);
	}

	@Test
	public void testListWithNullCriteria() throws Exception {
		DBCollection applicationsCol = db.getCollection(MongoMessageStore.APPLICATIONS_MSGS);
		DBCollection connectionsCol = db.getCollection(MongoMessageStore.CONNECTIONS_MSGS);

		createMessageForUpdate(connectionsCol, Message.STATUS_FAILED);
		createMessageForUpdate(applicationsCol, Message.STATUS_FAILED);

		MongoMessageStore store = new MongoMessageStore();
		store.setMongo(db);

		Collection<Message> messages = store.list(null);
		Assert.assertNotNull(messages);
		Assert.assertEquals(messages.size(), 2);
	}

	@Test
	public void testListWithDirectionCriteria() throws Exception {
		DBCollection applicationsCol = db.getCollection(MongoMessageStore.APPLICATIONS_MSGS);
		DBCollection connectionsCol = db.getCollection(MongoMessageStore.CONNECTIONS_MSGS);

		createMessageForUpdate(connectionsCol, Message.STATUS_FAILED);
		createMessageForUpdate(applicationsCol, Message.STATUS_FAILED);

		MongoMessageStore store = new MongoMessageStore();
		store.setMongo(db);

		MessageCriteria criteria = new MessageCriteria()
			.direction(Direction.TO_APPLICATIONS);
		Collection<Message> messages = store.list(criteria);

		Assert.assertEquals(messages.size(), 1);
	}

	@Test(expectedExceptions=IllegalStateException.class)
	public void shouldFailSaveOrUpdateWithNullDB() throws Exception {
		MongoMessageStore store = new MongoMessageStore();
		store.saveOrUpdate(new Message());
	}

	@Test(expectedExceptions=IllegalStateException.class)
	public void shouldFailUpdateStatusWithNullDB() throws Exception {
		MongoMessageStore store = new MongoMessageStore();
		store.updateStatus(null, Message.STATUS_CREATED);
	}

	@Test(expectedExceptions=IllegalStateException.class)
	public void shouldFailListWithNullDB() throws Exception {
		MongoMessageStore store = new MongoMessageStore();
		store.list(null);
	}

}

package org.mokai.persist.mongo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.mokai.Message;
import org.mokai.Message.Direction;
import org.mokai.persist.MessageCriteria;
import org.mokai.persist.MessageStore;
import org.mokai.persist.RejectedException;
import org.mokai.persist.StoreException;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 * An implementation of a {@link MessageStore} that uses MongoDB to persist messages.
 * 
 * @author German Escobar
 */
public class MongoMessageStore implements MessageStore {
	
	/**
	 * The collection name for messages sent to connections.
	 */
	public static final String CONNECTIONS_MSGS = "connectionsMsgs";
	
	/**
	 * The collection name for messages sent to applications.
	 */
	public static final String APPLICATIONS_MSGS = "applicationsMsgs";
	
	/**
	 * The underlying MongoDB API.
	 */
	private DB mongo;

	@Override
	public void saveOrUpdate(Message message) throws StoreException, RejectedException {
		
		if (mongo == null) throw new IllegalStateException("No mongo specified");
		if (message == null) throw new IllegalArgumentException("No message specified");
		
		// check if the direction of the message is valid
		if (!Direction.TO_CONNECTIONS.equals(message.getDirection()) 
				&& !Direction.TO_APPLICATIONS.equals(message.getDirection())) {
			throw new RejectedException("can't save/update a message with direction: " 
					+ message.getDirection() == null ? "null" : message.getDirection().toString());
		}
		
		try {
			if (message.getId() == null) { // hasn't been persisted
				save(message);
			} else {
				update(message);
			}
		} catch (Exception e) {
			throw new StoreException(e);
		}
		
	}
	
	/**
	 * Helper method. Saves a messages in the persistence store. 
	 * 
	 * @param message the message to be saved.
	 */
	private void save(Message message) {
		if (Direction.TO_CONNECTIONS.equals(message.getDirection())) {
			save(CONNECTIONS_MSGS, message);
		} else if (Direction.TO_APPLICATIONS.equals(message.getDirection())) {
			save(APPLICATIONS_MSGS, message);
		}
	}
	
	/**
	 * Helper method. Saves the <code>message</code> in the specified <code>collectionName</code>.
	 * 
	 * @param collectionName the name of the collection in which we are saving the message.
	 * @param message the message to save.
	 */
	private void save(String collectionName, Message message) {
		
		DBCollection col = mongo.getCollection(collectionName);
		
		BasicDBObject doc = new BasicDBObject();
		doc.put("status", message.getStatus());
		doc.put("source", message.getSource());
		doc.put("destination", message.getDestination());
		doc.put("reference", message.getReference());
		doc.put("creationTime", message.getCreationTime());
		doc.put("properties", message.getProperties());
		
		col.insert(doc);

		ObjectId id = (ObjectId) doc.get( "_id" );
		message.setId(id.toString());
	}
	
	/**
	 * Helper method. Updates the message in the persistence store.
	 * 
	 * @param message the message to be updated.
	 */
	private void update(Message message) {
		if (Direction.TO_CONNECTIONS.equals(message.getDirection())) {
			update(CONNECTIONS_MSGS, message);
		} else if (Direction.TO_APPLICATIONS.equals(message.getDirection())) {
			update(APPLICATIONS_MSGS, message);
		}
	}
	
	/**
	 * Helper method. Updates the <code>message</code> in the specified <code>collectionName</code>.
	 * 
	 * @param collectionName the name of the collection in which we are updating the message.
	 * @param message the message to be saved.
	 */
	private void update(String collectionName, Message message) {
		
		DBCollection col = mongo.getCollection(collectionName);
		
		BasicDBObject idObject = new BasicDBObject( "_id", new ObjectId(message.getId().toString()) );
		DBObject doc = col.findOne( idObject );
		
		doc.put("status", message.getStatus());
		doc.put("source", message.getSource());
		doc.put("destination", message.getDestination());
		doc.put("reference", message.getReference());
		doc.put("modificationTime", message.getModificationTime());
		doc.put("properties", message.getProperties());
		
		col.update(idObject, doc);
	}

	@Override
	public void updateStatus(MessageCriteria criteria, byte newStatus) throws StoreException {
		
		if (mongo == null) throw new IllegalStateException("No mongo specified");
		
		Direction direction = null;
		if (criteria != null) {
			direction = criteria.getDirection();
		}
		
		try {
			if (direction == null || Direction.TO_CONNECTIONS.equals(direction)) {
				updateStatus(CONNECTIONS_MSGS, criteria, newStatus);
			} 
			
			if (direction == null || Direction.TO_APPLICATIONS.equals(direction)) {
				updateStatus(APPLICATIONS_MSGS, criteria, newStatus);
			}
		} catch (Exception e) {
			throw new StoreException(e);
		}
	}
	
	/**
	 * Helper method. Updates the status of the messages that match the specified <code>criteria</code in the 
	 * <code>collectionName</code> with the <code>newStatus</code>. 
	 * 
	 * @param collectionName the name of the collection in which we are updating the records.
	 * @param criteria the criteria used to match the records that will be updated.
	 * @param newStatus the new status with which the matched records will be updated.
	 */
	private void updateStatus(String collectionName, MessageCriteria criteria, byte newStatus) {
		
		DBCollection col = mongo.getCollection(collectionName);
		
		BasicDBObject mongoCriteria = new BasicDBObject();
		addCommonCriteria(criteria, mongoCriteria);
		
		BasicDBObject obj = new BasicDBObject().append("$set", new BasicDBObject().append("status", newStatus));
		col.updateMulti(mongoCriteria, obj);
	}

	@Override
	public Collection<Message> list(MessageCriteria criteria) throws StoreException {
		
		if (mongo == null) throw new IllegalStateException("No mongo specified");
		
		Collection<Message> messages = new ArrayList<Message>();
		
		// retrieve the direction
		Direction direction = null;
		if (criteria != null) {
			direction = criteria.getDirection();
		}
		
		try {
			if (direction == null || direction.equals(Direction.TO_CONNECTIONS)) {
				messages.addAll(list(CONNECTIONS_MSGS, criteria));
			}
			
			if (direction == null || direction.equals(Direction.TO_APPLICATIONS)) {
				messages.addAll(list(APPLICATIONS_MSGS, criteria));
			}
			
			return messages;
		} catch (Exception e) {
			throw new StoreException(e);
		}
	}
	
	/**
	 * Helper method. Lists the messages from the specified <code>collectionName</code> that match the specified 
	 * <code>criteria</code>.
	 * 
	 * @param collectionName the name of the collection from which we are listing the messages.
	 * @param criteria the criteria used to list the messages.
	 * 
	 * @return a java.util.List<Message> object with the messages that match the criteria or an empty list if no 
	 * record matches.
	 */
	@SuppressWarnings("unchecked")
	private List<Message> list(String collectionName, MessageCriteria criteria) {
		
		List<Message> messages = new ArrayList<Message>();
		DBCollection col = mongo.getCollection(collectionName);
		
		BasicDBObject mongoCriteria = new BasicDBObject();
		addCommonCriteria(criteria, mongoCriteria);
		
		DBCursor cursor = col.find(mongoCriteria, null);
		if (criteria != null) {
			cursor.skip(criteria.getLowerLimit());
			cursor.limit(criteria.getNumRecords());
		}
		
		while(cursor.hasNext()) {
			BasicDBObject object = (BasicDBObject) cursor.next();
			
			Message message = new Message();
			message.setId( object.getString("_id") );
			message.setStatus( (byte) object.getInt("status") );
			message.setSource( object.getString("source") );
			message.setDestination( object.getString("destination") );
			message.setReference( object.getString("reference") );
			message.setCreationTime( (Date) object.get("creationTime") );
			message.setModificationTime( (Date) object.get("modificationTime") );
			
			Map<String,Object> properties = (Map<String,Object>) object.get("properties");
			for (Map.Entry<String,Object> entry : properties.entrySet()) {
				message.setProperty(entry.getKey(), entry.getValue());
			}
			
			if (CONNECTIONS_MSGS.equals(collectionName)) {
				message.setDirection(Direction.TO_CONNECTIONS);
			} else if (APPLICATIONS_MSGS.equals(collectionName)) {
				message.setDirection(Direction.TO_APPLICATIONS);
			}
			
			messages.add(message);
		}
		
		return messages;
	}
	
	/**
	 * Helper method. Adds the common <code>criteria</code> to the <code>mongoCriteria</code> object. 
	 * 
	 * @param criteria the {@link MessageCriteria} object from which we'll take the criteria added to 
	 * 					<code>mongoCriteria</code>; can be null.
	 * @param mongoCriteria the MongoDB specific object used to query.
	 */
	private void addCommonCriteria(MessageCriteria criteria, BasicDBObject mongoCriteria) {
		
		if (criteria != null) {
			
			// status
			if (criteria.getStatus() != null && !criteria.getStatus().isEmpty()) {
				List<Byte> list = new ArrayList<Byte>();
				for (Byte st : criteria.getStatus()) {
					list.add(st);
				}
				
				mongoCriteria.append("status", new BasicDBObject("$in", list));
			}
			
			// destination
			if (criteria.getDestination() != null) {
				mongoCriteria.append( "destination", criteria.getDestination() );
			}
			
			// other properties
			if (criteria.getProperties() != null) {
				for (Map.Entry<String,Object> entry : criteria.getProperties().entrySet()) {
					mongoCriteria.append("properties." + entry.getKey(), entry.getValue());
				}
			}
			
		}
		
	}

	public void setMongo(DB mongo) {
		this.mongo = mongo;
	}

}

package org.mokai;

import java.util.List;

import org.mokai.Action;

/**
 * 
 * 
 * @author German Escobar
 */
public interface ReceiverService extends Service {
	
	String getId();
	
	Receiver getReceiver();
	
	boolean isServiceable();

	ReceiverService addPostReceivingAction(Action action) throws IllegalArgumentException, ObjectAlreadyExistsException;
	
	ReceiverService removePostReceivingAction(Action action) throws IllegalArgumentException, ObjectNotFoundException;
	
	List<Action> getPostReceivingActions();
	
	void destroy();
	
}

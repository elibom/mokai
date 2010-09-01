package org.mokai;

import java.util.List;

import org.mokai.Action;
import org.mokai.Monitorable.Status;

/**
 * 
 * 
 * @author German Escobar
 */
public interface ReceiverService extends Service {
	
	String getId();
	
	Receiver getReceiver();
	
	Status getStatus();

	ReceiverService addPostReceivingAction(Action action) throws IllegalArgumentException, ObjectAlreadyExistsException;
	
	ReceiverService removePostReceivingAction(Action action) throws IllegalArgumentException, ObjectNotFoundException;
	
	List<Action> getPostReceivingActions();
	
	void destroy();
	
}

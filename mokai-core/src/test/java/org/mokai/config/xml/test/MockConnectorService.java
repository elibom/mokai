package org.mokai.config.xml.test;

import java.util.ArrayList;
import java.util.List;

import org.mokai.Acceptor;
import org.mokai.Action;
import org.mokai.Connector;
import org.mokai.ConnectorService;
import org.mokai.ExecutionException;
import org.mokai.Monitorable.Status;
import org.mokai.ObjectAlreadyExistsException;
import org.mokai.ObjectNotFoundException;

public class MockConnectorService implements ConnectorService {

	private String id;

	private int priority;

	private State state = State.STOPPED;

	private Status status = Status.OK;

	private int maxConcurrentMsgs;

	private Connector connector;

	private List<Acceptor> acceptors = new ArrayList<Acceptor>();

	private List<Action> preProcessingActions = new ArrayList<Action>();

	private List<Action> postProcessingActions = new ArrayList<Action>();

	private List<Action> postReceivingActions = new ArrayList<Action>();

	public MockConnectorService(String id, Connector connector) {
		this.id = id;
		this.connector = connector;
	}

	@Override
	public void start() throws ExecutionException {}

	@Override
	public void stop() throws ExecutionException {}

	@Override
	public State getState() {
		return state;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public int getPriority() {
		return priority;
	}

	@Override
	public void setPriority(int priority) {
		this.priority = priority;
	}

	@Override
	public ConnectorService withPriority(int priority) {
		setPriority(priority);
		return this;
	}

	@Override
	public int getMaxConcurrentMsgs() {
		return maxConcurrentMsgs;
	}

	@Override
	public void setMaxConcurrentMsgs(int maxConcurrentMsgs) {
		this.maxConcurrentMsgs = maxConcurrentMsgs;
	}

	@Override
	public Connector getConnector() {
		return connector;
	}

	@Override
	public int getNumQueuedMessages() {
		return 0;
	}

	@Override
	public Status getStatus() {
		return status;
	}

	@Override
	public ConnectorService addAcceptor(Acceptor acceptor) throws IllegalArgumentException, ObjectAlreadyExistsException {
		acceptors.add(acceptor);
		return this;
	}

	@Override
	public ConnectorService removeAcceptor(Acceptor acceptor) throws IllegalArgumentException, ObjectNotFoundException {
		acceptors.remove(acceptor);
		return this;
	}

	@Override
	public List<Acceptor> getAcceptors() {
		return acceptors;
	}

	@Override
	public ConnectorService addPreProcessingAction(Action action) throws IllegalArgumentException, ObjectAlreadyExistsException {
		preProcessingActions.add(action);
		return this;
	}

	@Override
	public ConnectorService removePreProcessingAction(Action action) throws IllegalArgumentException, ObjectNotFoundException {
		preProcessingActions.remove(action);
		return this;
	}

	@Override
	public List<Action> getPreProcessingActions() {
		return preProcessingActions;
	}

	@Override
	public ConnectorService addPostProcessingAction(Action action) throws IllegalArgumentException, ObjectAlreadyExistsException {
		postProcessingActions.add(action);
		return this;
	}

	@Override
	public ConnectorService removePostProcessingAction(Action action) throws IllegalArgumentException, ObjectNotFoundException {
		postProcessingActions.remove(action);
		return this;
	}

	@Override
	public List<Action> getPostProcessingActions() {
		return postProcessingActions;
	}

	@Override
	public ConnectorService addPostReceivingAction(Action action) throws IllegalArgumentException, ObjectAlreadyExistsException {
		postReceivingActions.add(action);
		return this;
	}

	@Override
	public ConnectorService removePostReceivingAction(Action action) throws IllegalArgumentException, ObjectNotFoundException {
		postReceivingActions.remove(action);
		return this;
	}

	@Override
	public List<Action> getPostReceivingActions() {
		return postReceivingActions;
	}

	@Override
	public void destroy() {}

}

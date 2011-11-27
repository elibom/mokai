package org.mokai.types.mock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.mokai.Acceptor;
import org.mokai.ExposableConfiguration;
import org.mokai.Message;

public class MockAcceptorWithAcceptor implements Acceptor, ExposableConfiguration<MockAcceptorWithAcceptor> {

	private Acceptor acceptor;
	
	private Collection<Acceptor> listAcceptors = new ArrayList<Acceptor>();
	
	private Map<String, Acceptor> mapAcceptors = new HashMap<String, Acceptor>();
	
	@Override
	public boolean accepts(Message message) {
		return false;
	}
	
	@Override
	public MockAcceptorWithAcceptor getConfiguration() {
		return this;
	}

	public Acceptor getAcceptor() {
		return acceptor;
	}

	public void setAcceptor(Acceptor acceptor) {
		this.acceptor = acceptor;
	}

	public Collection<Acceptor> getListAcceptors() {
		return listAcceptors;
	}

	public void setListAcceptors(Collection<Acceptor> listAcceptors) {
		this.listAcceptors = listAcceptors;
	}

	public Map<String, Acceptor> getMapAcceptors() {
		return mapAcceptors;
	}

	public void setMapAcceptors(Map<String, Acceptor> mapAcceptors) {
		this.mapAcceptors = mapAcceptors;
	}

}

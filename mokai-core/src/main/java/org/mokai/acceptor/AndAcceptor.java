package org.mokai.acceptor;

import java.util.Collection;
import java.util.HashSet;

import org.mokai.Acceptor;
import org.mokai.ExposableConfiguration;
import org.mokai.Message;
import org.mokai.annotation.Description;
import org.mokai.annotation.Name;
import org.mokai.ui.annotation.AcceptorsList;
import org.mokai.ui.annotation.Label;

@Name("And Acceptor")
@Description("An AND switch for other acceptors")
public class AndAcceptor implements Acceptor, ExposableConfiguration<AndAcceptor> {

	@Label("Acceptors")
	@AcceptorsList
	private Collection<Acceptor> acceptors = new HashSet<Acceptor>();
	
	@Override
	public boolean accepts(Message message) {
		
		for (Acceptor acceptor : acceptors) {
			if (!acceptor.accepts(message)) {
				return false;
			}
		}
		
		return true;
	}

	@Override
	public AndAcceptor getConfiguration() {
		return this;
	}

	public Collection<Acceptor> getAcceptors() {
		return acceptors;
	}

	public void setAcceptors(Collection<Acceptor> acceptors) {
		this.acceptors = acceptors;
	}
	
	public AndAcceptor addAcceptor(Acceptor acceptor) {
		acceptors.add(acceptor);
		
		return this;
	}

}

package org.mokai.types.mock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.mokai.Connector;
import org.mokai.ExposableConfiguration;

public class MockConnectorWithConnectors implements Connector, ExposableConfiguration<MockConnectorWithConnectors> {

	private Connector connector;

	private Collection<Connector> listConnectors = new ArrayList<Connector>();

	private Map<String,Connector> mapConnectors = new HashMap<String,Connector>();

	@Override
	public MockConnectorWithConnectors getConfiguration() {
		return this;
	}

	public Connector getConnector() {
		return connector;
	}

	public void setConnector(Connector connector) {
		this.connector = connector;
	}

	public Collection<Connector> getListConnectors() {
		return listConnectors;
	}

	public void setListConnectors(Collection<Connector> listConnectors) {
		this.listConnectors = listConnectors;
	}

	public Map<String, Connector> getMapConnectors() {
		return mapConnectors;
	}

	public void setMapConnectors(Map<String, Connector> mapConnectors) {
		this.mapConnectors = mapConnectors;
	}

}

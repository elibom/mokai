package org.mokai.type;

import java.util.Set;

/**
 * Mechanism used to load types.
 *
 * @author German Escobar
 */
public interface TypeLoader {

	Set<AcceptorType> loadAcceptorTypes();

	Set<ConnectorType> loadConnectorTypes();

	Set<ActionType> loadActionTypes();

}

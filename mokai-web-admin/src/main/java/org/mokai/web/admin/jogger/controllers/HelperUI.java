package org.mokai.web.admin.jogger.controllers;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mokai.Acceptor;
import org.mokai.Action;
import org.mokai.Connector;
import org.mokai.ConnectorService;
import org.mokai.ExposableConfiguration;
import org.mokai.Processor;
import org.mokai.web.admin.vaadin.dashboard.Helper;

/**
 * Helper class to reuse code that is used by multiples controllers, specially {@link Connections} and 
 * {@link Applications}.
 * 
 * @author German Escobar
 */
public class HelperUI {

	/**
	 * Helper method. Converts a list of {@link ConnectorService} objects to a list of {@link ConnectorUI} objects.
	 * 
	 * @param connectorServices the connector services that we want to convert into {@link ConnectorUI}
	 * 
	 * @return a list of {@link ConnectorUI} objects.
	 */
	public static List<ConnectorUI> buildConnectorUIs(List<ConnectorService> connectorServices) {
		
		List<ConnectorUI> connectorUIs = new ArrayList<ConnectorUI>();
		for (ConnectorService connectorService : connectorServices) {
			connectorUIs.add( new ConnectorUI(connectorService) );
		}
		
		return connectorUIs;
	}
	
	/**
	 * Helper method. Generates the JSON representation of a {@link ConnectorService} with configuration, acceptors 
	 * (if it is a {@link Processor}) and actions.
	 * 
	 * @param connectorService the connector service from which we are generating the JSON representation.
	 * 
	 * @return a JSONObject that holds the information of the connector service.
	 * @throws JSONException
	 */
	public static JSONObject getConnectorJSON(ConnectorService connectorService) throws JSONException {
		
		JSONObject jsonConnector = new ConnectorUI(connectorService).toJSON();
		
		// include configuration
		Connector connector = connectorService.getConnector();
		if ( ExposableConfiguration.class.isInstance(connector) ) {
			jsonConnector.put( "configuration", getConfigJSON((ExposableConfiguration<?>) connector) );
		}
		
		// include acceptors
		if ( Processor.class.isInstance(connector) ) {
			JSONArray jsonAcceptors = new JSONArray();
			
			List<Acceptor> acceptors = connectorService.getAcceptors();
			for (Acceptor acceptor : acceptors) {
				JSONObject jsonAcceptor = new JSONObject().put( "name", Helper.getComponentName(acceptor) );
				if ( ExposableConfiguration.class.isInstance(acceptor) ) {
					jsonAcceptor.put( "configuration", getConfigJSON((ExposableConfiguration<?>) acceptor) );
				}
				jsonAcceptors.put(jsonAcceptor);
			}
			
			jsonConnector.put( "acceptors", jsonAcceptors );
		}
		
		// include post-receiving-actions
		JSONArray jsonActions = getActionsJSON(connectorService.getPostReceivingActions());
		jsonConnector.put( "post-receiving-actions", jsonActions );
		
		// include pre-processing-actions
		if ( Processor.class.isInstance(connector) ) {
			jsonActions = getActionsJSON(connectorService.getPreProcessingActions());
			jsonConnector.put( "pre-processing-actions", jsonActions );
			
			jsonActions = getActionsJSON(connectorService.getPostProcessingActions());
			jsonConnector.put( "post-processing-actions", jsonActions );
		}
		
		return jsonConnector;
	}
	
	/**
	 * Helper method. Generates the JSON representation of a list of {@link Action} objects. 
	 * 
	 * @param actions a list of actions from which we are generating the JSON representation.
	 * 
	 * @return a JSONArray that holds the information of the actions.
	 * @throws JSONException
	 */
	public static JSONArray getActionsJSON(List<Action> actions) throws JSONException {
		
		JSONArray jsonActions = new JSONArray();
		
		for (Action action : actions) {
			JSONObject jsonAction = new JSONObject().put( "name", Helper.getComponentName(action) );
			if ( ExposableConfiguration.class.isInstance(action) ) {
				jsonAction.put( "configuration", getConfigJSON((ExposableConfiguration<?>) action) );
			}
			jsonActions.put(jsonAction);
		}
		
		return jsonActions;
	}
	
	/**
	 * Helper method. Generates the JSON representation of the configuration of connector/acceptor/action.
	 * 
	 * @param o an object that implements {@link ExposableConfiguration} and from which we are generating the JSON 
	 * representation.
	 * 
	 * @return a JSONObject that holds the information of the configuration.
	 * @throws JSONException
	 */
	public static <T extends ExposableConfiguration<?>> JSONObject getConfigJSON(T o) throws JSONException {

		Object config = o.getConfiguration();
		List<Field> fields = Helper.getConfigurationFields(config.getClass());
		
		JSONObject jsonConfig = new JSONObject();
		for (Field field : fields) {
			try { 
				field.setAccessible(true);
				jsonConfig.put( field.getName(), field.get(config) );
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		
		return jsonConfig;
	}
}

package org.mokai.web.admin.jogger.helpers;

import com.elibom.jogger.http.Response;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
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
import org.mokai.annotation.Name;
import org.mokai.ui.annotation.AcceptorsList;
import org.mokai.web.admin.jogger.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Alejandro <lariverosc@gmail.com>
 */
public class WebUtil {

    private final static Logger log = LoggerFactory.getLogger(WebUtil.class);

    private WebUtil() {
    }

    public static boolean isAuthenticated(Response response) {
        Session session = getSession(response);
        if (session != null && session.getUser() != null && !session.getUser().isEmpty()) {
            return true;
        }
        return false;
    }

    public static Session getSession(Response response) {
        Object session = response.getAttributes().get("session");
        if (session != null) {
            return (Session) session;
        }
        return null;
    }

    /**
     * Helper method. Converts a list of {@link ConnectorService} objects to a list of {@link EndpointPresenter} objects.
     *
     * @param connectorServices the connector services that we want to convert into {@link EndpointPresenter}
     *
     * @return a list of {@link EndpointPresenter} objects.
     */
    public static JSONArray buildEndpointsJson(List<ConnectorService> connectorServices) throws JSONException {
        JSONArray jsonArray = new JSONArray();
        for (ConnectorService connectorService : connectorServices) {
            jsonArray.put(new EndpointPresenter(connectorService).toJSON());
        }

        return jsonArray;
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
        JSONObject jsonConnector = new EndpointPresenter(connectorService).toJSON();

        // include configuration
        Connector connector = connectorService.getConnector();
        if (ExposableConfiguration.class.isInstance(connector)) {
            jsonConnector.put("configuration", getConfigJSON((ExposableConfiguration<?>) connector));
        }

        // include acceptors
        if (Processor.class.isInstance(connector)) {
            JSONArray jsonAcceptors = new JSONArray();

            List<Acceptor> acceptors = connectorService.getAcceptors();
            for (Acceptor acceptor : acceptors) {
                JSONObject jsonAcceptor = new JSONObject().put("name", getComponentName(acceptor));
                if (ExposableConfiguration.class.isInstance(acceptor)) {
                    jsonAcceptor.put("configuration", getConfigJSON((ExposableConfiguration<?>) acceptor));
                }
                jsonAcceptors.put(jsonAcceptor);
            }

            jsonConnector.put("acceptors", jsonAcceptors);
        }

        // include post-receiving-actions
        JSONArray jsonActions = getActionsJSON(connectorService.getPostReceivingActions());
        jsonConnector.put("post-receiving-actions", jsonActions);

        // include pre-processing-actions
        if (Processor.class.isInstance(connector)) {
            jsonActions = getActionsJSON(connectorService.getPreProcessingActions());
            jsonConnector.put("pre-processing-actions", jsonActions);

            jsonActions = getActionsJSON(connectorService.getPostProcessingActions());
            jsonConnector.put("post-processing-actions", jsonActions);
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
            JSONObject jsonAction = new JSONObject().put("name", getComponentName(action));
            if (ExposableConfiguration.class.isInstance(action)) {
                jsonAction.put("configuration", getConfigJSON((ExposableConfiguration<?>) action));
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
        List<Field> fields = getConfigurationFields(config.getClass());

        JSONObject jsonConfig = new JSONObject();
        for (Field field : fields) {
            try {
                field.setAccessible(true);

                if (field.isAnnotationPresent(AcceptorsList.class)) {
                    Collection<Acceptor> acceptors = (Collection<Acceptor>) field.get(config);

                    JSONArray jsonAcceptors = new JSONArray();
                    for (Acceptor acceptor : acceptors) {
                        JSONObject jsonAcceptor = new JSONObject().put("name", getComponentName(acceptor));
                        if (ExposableConfiguration.class.isInstance(acceptor)) {
                            jsonAcceptor.put("configuration", getConfigJSON((ExposableConfiguration<?>) acceptor));
                        }
                        jsonAcceptors.put(jsonAcceptor);
                    }

                    jsonConfig.put(field.getName(), jsonAcceptors);
                } else {
                    jsonConfig.put(field.getName(), field.get(config));
                }
            } catch (IllegalAccessException e) {
                log.error(e.getMessage(), e);
            }
        }

        return jsonConfig;
    }

    /**
     * Helper method. Check if the field has a {@link Name} annotation and returns its value, otherwise, the name of
     * the class is returned.
     *
     * @param component the Object from which we are retrieving the name.
     * @return a String that is used as the name of the component.
     */
    public static String getComponentName(Object component) {
        Class<?> componentClass = component.getClass();

        Name nameAnnotation = componentClass.getAnnotation(Name.class);
        if (nameAnnotation != null) {
            return nameAnnotation.value();
        }

        return componentClass.getSimpleName();
    }

    /**
     * Helper method that retrieves all the fields that have a getter method (i.e. are readable) of an object.
     *
     * @param clazz the class from which we are going to retrieve the readable fields.
     * @return a List of Field objects that are readable.
     */
    public static List<Field> getConfigurationFields(Class<?> clazz) {

        List<Field> ret = new ArrayList<Field>();

        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            boolean existsGetter = existsGetter(clazz, field);
            if (existsGetter) {
                ret.add(field);
            }
        }

        return ret;

    }

    /**
     * Helper method. Checks if there is a getter method of the field in the configClass.
     *
     * @param configClass the class in which we are checking for the getter method.
     * @param field the field for whose getter method we are searching.
     * @return true if a getter method exists in the configClass, false otherwise.
     */
    private static boolean existsGetter(Class<?> configClass, Field field) {
        try {
            configClass.getMethod("get" + capitalize(field.getName()));
        } catch (NoSuchMethodException e) {
            return false;
        }
        return true;
    }

    /**
     * Helper method. Capitalizes the first letter of a string.
     *
     * @param name the string that we want to capitalize.
     * @return the capitalized string.
     */
    private static String capitalize(String name) {
        if (name == null || name.length() == 0) {
            return name;
        }

        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

}

package org.mokai.web.admin.jogger.controllers;

import com.elibom.jogger.Jogger;
import com.elibom.jogger.http.Cookie;
import com.elibom.jogger.http.Response;
import com.elibom.jogger.test.JoggerTest;
import com.elibom.jogger.test.MockResponse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;

import org.json.JSONArray;
import org.json.JSONObject;
import org.mockito.Mockito;
import org.mokai.Connector;
import org.mokai.ConnectorService;
import org.mokai.Monitorable.Status;
import org.mokai.RoutingEngine;
import org.mokai.Service.State;
import org.mokai.web.admin.jogger.interceptors.SessionInterceptor;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ApplicationsTest extends MokaiTest {

    @Test(enabled = false)
    public void shouldListApplications() throws Exception {
        Connector connector = mock(Connector.class);

        ConnectorService connectorService = mock(ConnectorService.class);
        when(connectorService.getId()).thenReturn("test-application");
        when(connectorService.getConnector()).thenReturn(connector);
        when(connectorService.getState()).thenReturn(State.STARTED);
        when(connectorService.getStatus()).thenReturn(Status.UNKNOWN);
        when(connectorService.getNumQueuedMessages()).thenReturn(0);
        when(connectorService.getPriority()).thenReturn(1000);

        RoutingEngine routingEngine = getSpringContext().getBean(RoutingEngine.class);
        Mockito.when(routingEngine.getApplications()).thenReturn(Collections.singletonList(connectorService));
        String sessionId = createSession();
        MockResponse response = get("/applications").addCookie(new Cookie(SessionInterceptor.SESSION_ID_COOKIE_KEY, sessionId)).run();

        Assert.assertEquals(response.getStatus(), Response.OK);

        JSONArray jsonResponse = new JSONArray(response.getOutputAsString());
        Assert.assertNotNull(jsonResponse);
        Assert.assertEquals(jsonResponse.length(), 1);

        JSONObject jsonConnection = jsonResponse.getJSONObject(0);
        Assert.assertNotNull(jsonConnection);
        Assert.assertEquals(jsonConnection.getString("id"), "test-application");
    }

}

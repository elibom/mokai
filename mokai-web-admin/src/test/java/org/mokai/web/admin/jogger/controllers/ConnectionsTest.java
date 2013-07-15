package org.mokai.web.admin.jogger.controllers;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;

import org.jogger.http.Cookie;
import org.jogger.http.Response;
import org.jogger.test.MockResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mokai.Connector;
import org.mokai.ConnectorService;
import org.mokai.Monitorable.Status;
import org.mokai.RoutingEngine;
import org.mokai.Service.State;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ConnectionsTest extends AbstractTest {

	@Test
	public void shouldListConnections() throws Exception {
		Connector connector = mock(Connector.class);

		ConnectorService cs = mock(ConnectorService.class);
		when( cs.getId() ).thenReturn("test-connection");
		when( cs.getConnector() ).thenReturn( connector );
		when( cs.getState() ).thenReturn( State.STARTED );
		when( cs.getStatus() ).thenReturn( Status.UNKNOWN );
		when( cs.getNumQueuedMessages() ).thenReturn(0);
		when( cs.getPriority() ).thenReturn(1000);

		RoutingEngine routingEngine = getSpringContext().getBean(RoutingEngine.class);
		when(routingEngine.getConnections()).thenReturn( Collections.singletonList(cs) );

		MockResponse response = get("/connections").addCookie(new Cookie("access_token", "true")).run();

		Assert.assertEquals( response.getStatus(), Response.OK );

		JSONArray jsonResponse = new JSONArray( response.getOutputAsString() );
		Assert.assertNotNull( jsonResponse );
		Assert.assertEquals( jsonResponse.length(), 1 );

		JSONObject jsonConnection = jsonResponse.getJSONObject(0);
		Assert.assertNotNull( jsonConnection );
		Assert.assertEquals( jsonConnection.getString("id"), "test-connection" );
	}

	@Test
	public void shouldListEmptyConnections() throws Exception {
		RoutingEngine routingEngine = getSpringContext().getBean(RoutingEngine.class);
		when(routingEngine.getConnections()).thenReturn( new ArrayList<ConnectorService>() );

		MockResponse response = get("/connections").addCookie(new Cookie("access_token", "true")).run();

		Assert.assertEquals( response.getStatus(), Response.OK );
		Assert.assertEquals( response.getOutputAsString() , "[]");
	}

	@Test
	public void shouldFailListConnectionsWithoutAccessToken() throws Exception {
		MockResponse response = get("/connections").run();
		Assert.assertEquals( response.getStatus(), Response.UNAUTHORIZED );
	}

	@Test
	public void shouldStopConnection() throws Exception {
		Connector connector = mock(Connector.class);

		ConnectorService cs = mock(ConnectorService.class);
		when( cs.getId() ).thenReturn("test-connection");
		when( cs.getConnector() ).thenReturn( connector );
		when( cs.getState() ).thenReturn( State.STARTED );
		when( cs.getStatus() ).thenReturn( Status.UNKNOWN );
		when( cs.getNumQueuedMessages() ).thenReturn(0);
		when( cs.getPriority() ).thenReturn(1000);

		RoutingEngine routingEngine = getSpringContext().getBean(RoutingEngine.class);
		when(routingEngine.getConnection("test-connection")).thenReturn(cs);

		MockResponse response = post("/connections/test-connection/stop")
				.addCookie(new Cookie("access_token", "true"))
				.run();

		Assert.assertEquals( response.getStatus(), Response.OK );
		verify(cs).stop();
		verify(cs, never()).start();
	}

}

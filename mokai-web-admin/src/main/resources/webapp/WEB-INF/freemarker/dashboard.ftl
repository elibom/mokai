<#ftl encoding="UTF-8">
<#import "layout.ftl" as layout>
<@layout.layout>
	
	<div class="metrics row">
		<div id="failedMessages" class="metric">
			<span class="number red">${failedMsgs}</span>
			<span class="tag">Failed</span>
		</div>
		<div id="unroutableMessages" class="metric">
			<span class="number red">${unroutableMsgs}</span>
			<span class="tag">Unroutable</span>
		</div>
	</div>
	
	<div id="body" class="row">
		<div class="span4">
			<h3>Applications</h3>
				
			<#list applications as connector>
				<#if connector.status = "OK">
					<section class="connector status-ok" id="application-${connector.id}" data="${connector.id}">
				<#elseif connector.status = "FAILED">
					<section class="connector status-failed" id="application-${connector.id}" data="${connector.id}">
				<#else>
					<section class="connector" id="application-${connector.id}" data="${connector.id}">
				</#if>
					<header>
						<div class="left"><h5>${connector.id}</h5><small>${connector.type}</small></div>
						<div class="right">
							<#if connector.state = "STARTED">
								<a id="application-${connector.id}-state" class="btn btn-mini cmd-applications-stop" href="#"><i class="icon-off"></i> Stop</a>
							<#else>
								<a id="application-${connector.id}-state" class="btn btn-mini btn-danger cmd-applications-start" href="#"><i class="icon-off icon-white"></i> Start</a>
							</#if>
						</div>
						<div class="divider"></div>
						<div class="right"><a class="btn btn-mini cmd-applications-info" href="#"><i class="icon-info-sign"></i></a></div>
					</header>
					<div>
						<div>
							<#if connector.processor >
								<div class="left queued"><span>${connector.queuedMessages}</span> queued</div>
							</#if>
							<div class="right">
								<span class="status"></span>
							</div>
						</div>
					</div>
				</section>
			</#list>	
		</div>
			
		<div class="span4 offset3">
			<h3 style="padding-bottom: 10px;">Connections</h3>
				
			<#list connections as connector>
				<#if connector.status = "OK">
					<section class="connector status-ok" id="connection-${connector.id}" data="${connector.id}">
				<#elseif connector.status = "FAILED">
					<section class="connector status-failed" id="connection-${connector.id}" data="${connector.id}">
				<#else>
					<section class="connector" id="connection-${connector.id}" data="${connector.id}">
				</#if>
					<header>
						<div class="left"><h5>${connector.id}</h5><small>${connector.type}</small></div>
						<div class="right">
							<#if connector.state = "STARTED">
								<a id="connection-${connector.id}-state" class="btn btn-mini cmd-connections-stop" href="#"><i class="icon-off"></i> Stop</a>
							<#else>
								<a id="connection-${connector.id}-state" class="btn btn-mini btn-danger cmd-connections-start" href="#"><i class="icon-off icon-white"></i> Start</a>
							</#if>
						</div>
						<div class="divider"></div>
						<div class="right"><a class="btn btn-mini cmd-connections-info" href="#"><i class="icon-info-sign"></i></a></div>
					</header>
					<div>
						<div>
							<#if connector.processor >
								<div class="left queued"><span>${connector.queuedMessages}</span> queued</div>
							</#if>
							<div class="right">
								<span class="status"></span>
							</div>
						</div>
					</div>
				</section>
			</#list>
		</div>
	</div>
	
	<!-- Modal Info -->
	<div class="modal hide" id="connector-modal"></div>
	<script id="connector-info" type="text/template">
		<div class="modal-header">
			<button type="button" class="close" data-dismiss="modal">×</button>
			<h3><%= id %></h3>
		</div>
		<div class="modal-body">
			<ul class="nav nav-tabs">
				<% if (typeof(configuration) != "undefined") { %>
					<li class="active"><a href="#configuration" data-toggle="tab">Config</a></li>
				<% } %>
				<% if (isProcessor) { %>
					<li><a href="#acceptors" data-toggle="tab">Acceptors</a></li>
				<% } %>
				<% if (obj["pre-processing-actions"].length > 0) { %>
					<li><a href="#pre-processing-actions" data-toggle="tab">Pre Processing</a></li>
				<% } %>
				<% if (obj["post-processing-actions"].length > 0) { %>
					<li><a href="#post-processing-actions" data-toggle="tab">Post Processing</a></li>
				<% } %>
				<% if (obj["post-receiving-actions"].length > 0) { %>
					<li><a href="#post-receiving-actions" data-toggle="tab">Post Receiving</a></li>
				<% } %>
			</ul>
			
			<div class="tab-content">
				<% if (typeof(configuration) != "undefined") { %>
					<div class="tab-pane active" id="configuration">
						<table class="table table-bordered table-condensed table-striped">
							<% _.each(configuration, function(value, key) { %>
								<tr>
									<th width="10%"><%= key %></th>
									<td>
										<% if (_.isObject(value) || _.isArray(value)) { %>
											<%= JSON.stringify(value) %>
										<% } else { %>
											<%= value %>
										<% } %>
									</td>
								</tr>
							<% }) %>
						</table>
					</div>
				<% } %>

				<% if (isProcessor) { %>
					<div class="tab-pane" id="acceptors">
						<% _.each(acceptors, function(acceptor) { %>
							<%= renderAcceptor(acceptor, true) %>
						<% }) %>
					</div>
				<% } %>

				<% if (obj["pre-processing-actions"].length > 0) { %>
					<div class="tab-pane" id="pre-processing-actions">
						<% _.each(obj["pre-processing-actions"], function(action) { %>
							<%= renderAction(action) %>		
						<% }) %>
					</div>
				<% } %>

				<% if (obj["post-processing-actions"].length > 0) { %>
					<div class="tab-pane" id="post-processing-actions">
						<% _.each(obj["post-processing-actions"], function(action) { %>
							<%= renderAction(action) %>		
						<% }) %>
					</div>
				<% } %>

				<% if (obj["post-receiving-actions"].length > 0) { %>
					<div class="tab-pane" id="post-receiving-actions">
						<% _.each(obj["post-receiving-actions"], function(action) { %>
							<%= renderAction(action) %>		
						<% }) %>
					</div>
				<% } %>
			</div>

		</div>
		<div class="modal-footer">
			<a href="#" class="btn" data-dismiss="modal">Close</a>
		</div>
	</script>
	<script id="acceptor-template" type="text/template">
		<p><i class="icon-chevron-right"></i> <strong><%= name %></strong></p>
		<% if (configuration) { %>
			<div style="margin-left: 20px;">
				<table class="table table-bordered table-condensed">
					<% _.each(configuration, function(value, key) { %>
						<tr>
							<th width="10%"><%= key %></th>
							<td><%= renderValue(value, true) %></td>
						</tr>
					<% }) %>
				</table>
			</div>
		<% } %>
	</script>
	<script id="action-template" type="text/template">
		<p><i class="icon-chevron-right"></i> <strong><%= name %></strong></p>
		<div style="margin-left: 20px;">
			<table class="table table-bordered table-condensed table-striped">
				<% _.each(configuration, function(value, key) { %>
					<tr>
						<th width="10%"><%= key %></th>
						<td>
							<% if (_.isObject(value) || _.isArray(value)) { %>
								<%= JSON.stringify(value) %>
							<% } else { %>
								<%= value %>
							<% } %>
						</td>
					</tr>
				<% }) %>
			</table>
		</div>
	</script>
	
	<script type="text/javascript" src="/js/backbone-min.js"></script>
	<script type="text/javascript" src="/js/dashboard.js"></script>
	<script type="text/javascript">
		new MainView({ el: 'body'});
		
		var socket = $.atmosphere;
		var request = { url: 'http://' + window.location.hostname + ':8585/changes',
						contentType : "application/json",
						logLevel : 'debug',
						transport : 'websocket' ,
						fallbackTransport: 'long-polling'};
		
		function connectorServiceChanged(json, prefix) {
			var id = json.data.id;
			var state = json.data.state;
			var status = json.data.status;
			var queuedMessages = json.data.queued;
				
			if (state === 'STARTED') {
				$('#' + prefix + '-' + id + '-state').html('<i class="icon-off"></i> Stop').attr('class', 'btn btn-mini cmd-' + prefix + 's-stop');
			} else {
				$('#' + prefix + '-' + id + '-state').html('<i class="icon-off icon-white"></i> Start').attr('class', 'btn btn-mini btn-danger cmd-' + prefix + 's-start');
			}
			
			$('#' + prefix + '-' + id + ' .queued').html('<span>' + queuedMessages + '</span> queued');
			$('#' + prefix + '-' + id).removeClass("status-ok status-failed");
			if (status === 'OK') {
				$('#' + prefix + '-' + id).addClass("status-ok");
			} else if (status === 'FAILED') {
				$('#' + prefix + '-' + id).addClass("status-failed");
			}
		}
		
		request.onMessage = function (response) {
			var message = response.responseBody;
			try {
				var json = JSON.parse(message);
			} catch (e) {
				console.log('This does not look like a valid JSON: ', message.data);
				return;
			}
			
			if (json.eventType === "APPLICATION_CHANGED") {
				connectorServiceChanged(json, "application");
			} else if (json.eventType === "CONNECTION_CHANGED") {
				connectorServiceChanged(json, "connection");
			} else if (json.eventType === "FAILED_CHANGED") {
				$('#failedMessages span.number').html(json.data.value);
			} else if (json.eventType == 'UNROUTABLE_CHANGED') {
				$('#unroutableMessages span.number').html(json.data.value);
			}
			
			//alert(message);
		}
		
		request.onError = function(response) {
			alert('Error de websockets: ' + response);
		};
		
		var subSocket = socket.subscribe(request);
		
	</script>
</@layout.layout>
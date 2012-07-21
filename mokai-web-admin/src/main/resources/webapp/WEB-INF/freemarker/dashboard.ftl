<#ftl encoding="UTF-8">
<#import "layout.ftl" as layout>
<@layout.layout>
	
	<div class="metrics header-space row">
		<div id="failedMessages" class="metric">
			<span class="number red">${failedMsgs}</span>
			<span class="tag">Failed</span>
		</div>
			
		<div id="toApplications" class="metric">
			<span class="number">${toApplications}</span>
			<span class="tag">To Applications</span>
		</div>
			
		<div id="toConnections" class="metric">
			<span class="number">${toConnections}</span>
			<span class="tag">To Connections</span>
		</div>
			
	</div>
	
	<div id="body" class="row">
		<div class="span4">
			<h3>Applications</h3>
				
			<#list applications as connector>
    			<section class="connector" id="application-${connector.id}" data="${connector.id}">
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
								<#if connector.status = "OK">
									<span class="status status-success">Running</span>
								<#elseif connector.status = "FAILED">
									<span class="status status-failed">Failed</span>
								</#if>
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
	  			<% if ( typeof(configuration) != "undefined" ) { %>
  					<li class="active"><a href="#configuration" data-toggle="tab">Config</a></li>
  				<% } %>
  				<% if ( isProcessor ) { %>
  					<li><a href="#acceptors" data-toggle="tab">Acceptors</a></li>
  				<% } %>
			</ul>
			
			<div class="tab-content">
				<% if ( typeof(configuration) != "undefined" ) { %>
  					<div class="tab-pane active" id="configuration">
  						<table class="table table-bordered table-condensed table-striped">
			    			<% 
			    			for ( var key in configuration ) {
			    				if ( configuration.hasOwnProperty(key) ) {
			    			%>
			    				<tr>
			    					<th><%= key %></th>
			    					<td>
			    						<% if (_.isObject(configuration[key]) || _.isArray(configuration[key])) { %>
			    							<%= JSON.stringify(configuration[key]) %>
			    						<% } else { %>
			    							<%= configuration[key] %>
			    						<% } %>
			    					</td>
			    				</tr>
			    			<% 
			    				} 
			    			}	
			    			%>
			    		</table>
  					</div>
  				<% } %>
  				
  				<% if ( isProcessor ) { %>
  					<div class="tab-pane" id="acceptors">
  						<% for (var i=0; i < acceptors.length; i++) { %>
  							<% if (acceptors[i].configuration) { %>
  								<p><i class="icon-chevron-right"></i> <strong><%= acceptors[i].name %></strong> - <a href="#">Show Configuration</a></p>
  							<% } else { %>
  								<p><i class="icon-chevron-right"></i> <strong><%= acceptors[i].name %></strong></p>
  							<% } %>
  							
  						<% } %>
  					</div>
  				<% } %>
			</div>

	  	</div>
	  	<div class="modal-footer">
	    	<a href="#" class="btn" data-dismiss="modal">Close</a>
	  	</div>
	</script>
	
	
	<script type="text/javascript">
	
		var connectorTemplate = _.template( $('#connector-info').html() );
		
		function showConnectorInfo(elem, type) {
			var id = elem.closest('section').attr('data');
			var req = $.ajax({
  				url: "/" + type + "/" + id,
  				dataType: "json"
			});
			req.success(function(data) {
				var html = connectorTemplate(data);
				$('#connector-modal').html( html );
				
				$('#connector-modal').modal();
			});
		}
		
		$('section a.cmd-applications-info').click(function() {
			showConnectorInfo( $(this), 'applications' );		
			return false;
		});
		
		$('section a.cmd-connections-info').click(function() {
			showConnectorInfo( $(this), 'connections' );		
			return false;
		});
		
		function startStopConnector(elem, type, state) {
			var id = elem.closest('section').attr('data');
			var req = $.ajax({
				type: "POST",
  				url: "/" + type + "/" + id + "/" + state,
  				dataType: "json"
			});
			
			alert("Petición enviada");
		}
		
		$('section a.cmd-applications-start').live('click', function() {
			startStopConnector( $(this), 'applications', 'start' );
			return false;
		});
		
		$('section a.cmd-applications-stop').live('click', function() {
			startStopConnector( $(this), 'applications', 'stop' );
			return false;
		});
		
		$('section a.cmd-connections-start').live('click', function() {
			startStopConnector( $(this), 'connections', 'start' );
			return false;
		});
		
		$('section a.cmd-connections-stop').live('click', function() {
			startStopConnector( $(this), 'connections', 'stop' );
			return false;
		});
		
		var socket = $.atmosphere;
 		var request = { url: 'http://' + window.location.hostname + ':8585/changes',
 						contentType : "application/json",
 						logLevel : 'debug',
 						transport : 'websocket' ,
						fallbackTransport: 'long-polling'};
						
		request.onOpen = function(response) {
 			// alert('Atmosphere connected using ' + response.transport);
 		};
 		
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
 			} else if (json.eventType === "TO_APPLICATIONS_CHANGED") {
 				$('#toApplications span.number').html(json.data.value);
 			} else if (json.eventType === "TO_CONNECTIONS_CHANGED") {
 				$('#toConnections span.number').html(json.data.value);
 			} else if (json.eventType === "FAILED_CHANGED") {
 				$('#failedMessages span.number').html(json.data.value);
 			}
 			
 			//alert(message);
 		}
 		
 		request.onError = function(response) {
 			alert('Error de websockets: ' + response);
 		};
 		
 		var subSocket = socket.subscribe(request);
		
	</script>
</@layout.layout>
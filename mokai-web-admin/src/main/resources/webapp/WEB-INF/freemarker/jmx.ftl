<#ftl encoding="UTF-8">
<#import "layout.ftl" as layout>
<@layout.layout>

	<div id="jmx">
	
		<div class="featured">
			<div id="used_memory">
				<span class="value"></span>
				<span class="tag">Used Memory</span>
			</div>
			<div id="max_memory">
				<span class="value"></span>
				<span class="tag">Max Memory</span>
			</div>
			<div id="thread_count">
				<span class="value"></span>
				<span class="tag">Active Threads</span>
			</div>
		</div>
	
		<div class="mbeans">
			<div id="mbeans_list" class="panel">
				<ul class="rounded-corner">
					<#list domains as domain>
						<li class="domain" data="${domain.name}">
							<a href="#">${domain.name}</a>
							<ul style="display:none;">
								<#list domain.mBeans as mbean>
									<li class="mbean"><a href="#">${mbean}</a></li>
								</#list>
							</ul>
						</li>
					</#list>
				</ul>
			</div>
			<div id="mbean_info" class="panel"></div>
		</div>
	</div>
	<div style="height:40px;"></div>
	
	<script id="mbean_template" type="text/template">
		<header>
			<h1><%= name %></h1>
			<h2><%= className %></h2>
			
			<p><%= description %>Test</p>
		</header>
		
		<section>
			<h3>Attributes</h3>
			
			<div class="attributes">
				<table class="table table-striped table-bordered">
					<thead>
						<tr>
							<th>Name</th>
							<th>Type</th>
							<th>Value</th>
							<th>Description</th>
						</tr>
					</thead>
				<% _.each(attributes, function(attribute) { %>
					<tr>
						<td><%= attribute.name %></td>
						<td><%= attribute.type %></td>
						<td><%= attribute.value %></td>
						<td><%= attribute.description %></td>
						
					</tr>
				<% }); %>
				</table>
			</div>
			
			<h3>Operations</h3>
			
			<% _.each(operations, function(operation) { %>
			
				<%
					var params = "";
					for (var i=0; i < operation.params.length; i++) {
						var param = operation.params[i];
						if (i === 0) {
							params = param.type + ' <em>' + param.name + '</em>';
						} else {
							params += ', ' + param.type + ' <em>' + param.name + '</em>';
						}
						
					}
				%>
			
				<div class="operation">
					<div>
						<span class="name"><%= operation.name %></span>
						<span class="return_type"><%= operation.returnType %></span>
						<% if (params !== "") { %>
							<span class="params"><%= params %></span>
						<% } %>	
						<span class="description"><%= operation.description %></span>
					</div>
					
					<div class="actions">
						<button class="btn btn-mini" type="button">Invoke</button>
					</div>
				</div>
			<% }); %>
			
		</section>
		
	</script>
	
	<script type="text/javascript" src="/js/backbone-min.js"></script>
	<script type="text/javascript">
	
		var mBeansView = Backbone.View.extend({
		
			initialize: function() {
				
				setInterval(function() {
				
					var request = $.ajax({
						url: '/jmx/' + encodeURIComponent('java.lang:type=Memory') + "/attributes/HeapMemoryUsage",
						dataType: 'json',
						global: false
					});
					request.done(function(data) {
						var value = $.parseJSON(data.value);

						$("div.featured div#used_memory span.value").html( (value.used / (1024 * 1024)).toFixed(0) + "MB" );
						$("div.featured div#max_memory span.value").html( (value.max / (1024 * 1024)).toFixed(0) + "MB" );
						
					});
					request.fail(function(jqXHR, textStatus) {
						
					});
					
					request = $.ajax({
						url: '/jmx/' + encodeURIComponent('java.lang:type=Threading') + "/attributes/ThreadCount",
						dataType: 'json',
						global: false
					});
					request.done(function(data) {
						$("div.featured div#thread_count span.value").html( data.value);
					});
					request.fail(function(jqXHR, textStatus) {
						
					});
					
				
				}, 2000);
				
				
			},
			
			events: {
				"click li.domain": "toggleDomain",
				"click li.mbean" : "showMBean"
			},
			
			toggleDomain: function(event) {
				var li = $(event.currentTarget);
				var innerUl = $(event.currentTarget).children('ul');
				
				if (!$(event.currentTarget).is("li")) {
					li = $(event.currentTarget).parents('li');
					innerUl = li.children('ul');
				}
				
				li.toggleClass('item_open');
				innerUl.toggle();
				
				return false;
			},
			
			showMBean: function(event) {
				var domain = $(event.currentTarget).parents('li.domain').attr('data');
				var keysElem = $(event.currentTarget);
				if ($(event.currentTarget).is("li")) {
					keysElem = $(event.currentTarget).children('a');
				}
				
				var keys = keysElem.html();
				
				var mbean = encodeURIComponent(domain + ':' + keys);
				var request = $.ajax({
					url: '/jmx/' + mbean,
					dataType: 'json'
				});
				
				request.done(function(data) {
					stopTimers();
					$('#fade').fadeOut();
					
					var template = _.template( $('#mbean_template').html() );
					$('#mbean_info').html( template(data) );
					
				});
				
				request.fail(function(jqXHR, textStatus) {
					stopTimers();
					
					if (jqXHR.status == 404) {
						$('#fade').fadeOut();
						alert("Not found!");
					} else {
						var output = App.errorTemplate({title: "Unexpected Error", body: "<strong>¡Sorry!</strong> Something went wrong. Please check Mokai's logs for more information." });
						$('#fade').html( output );
					}
				});
				
				return false;
			}
		
		});
		
		new mBeansView( { el: $("#mbeans_list ul") } );
		
	</script>

</@layout.layout>
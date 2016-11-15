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
	
	<!-- Footer div -->
	<div style="height:40px;"></div>
	
	<!-- Template: MBean -->
	<script id="mbean_template" type="text/template">
		<div class="back" style="padding:5px; font-size: 14px;"><a id="cmdBack" href="#"><strong>&#8592;</strong> MBeans List</a></div>
		<header>
			<h1><%= name %></h1>
			<h2><%= className %></h2>
			
			<p><%= description %></p>
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
				</table>
			</div>
			
			<h3>Operations</h3>
			
			<div class="operations"></div>
			
		</section>
	</script>
	
	<!-- Template: Attribute -->
	<script id="attribute_template" type="text/template">
		<td><%= attribute.name %></td>
		<td><%= fixType(attribute.type) %></td>
		<td class="attr_value"><%= attribute.value %></td>
		<td><%= attribute.description %></td>
	</script>
	
	<!-- Template: Operation -->
	<script id="operation_template" type="text/template">
		
		<div class="alert alert-info" style="display:none; margin-bottom: 10px;">
  			<button type="button" class="close" data-dismiss="alert">×</button>
  			<strong>Success!</strong> No data returned.
		</div>
		<div>
			<span class="name"><%= operation.name %></span>
			<span class="return_type"><%= fixType(operation.returnType) %></span>
			<% if (params !== "") { %>
				<span class="params"><%= params %></span>
			<% } %>	
			<span class="description"><%= operation.description %></span>
		</div>
					
		<div class="actions">
			<button id="<%= operation.name %>" class="invoke btn btn-mini" type="button">Invoke</button>
		</div>
			
	</script>
	
	<script id="params_template" type="text/template">
		<form class="form-horizontal">
			<div class="modal-header">
	    		<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
	    		<h3><%= operation.name %></h3>
	  		</div>
	  		<div class="modal-body">
	  		</div>
	  		<div class="modal-footer">
	    		<a href="#" class="btn" data-dismiss="modal">Close</a>
	    		<button class="submit btn btn-primary">Invoke</button>
	  		</div>
		</form>
	</script>
	
	<!-- Operation returns modal -->
	<div class="return_info modal hide fade">
  		<div class="modal-header">
    		<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
    		<h3></h3>
  		</div>
  		<div class="modal-body">
    		<p></p>
  		</div>
  		<div class="modal-footer">
    		<a href="#" class="btn" data-dismiss="modal">Close</a>
  		</div>
	</div>
	
	<script type="text/javascript" src="/javascript/lib/backbone-1.1.0.js"></script>
        <script type="text/javascript" src="/javascript/jmx.js"></script>
	<script type="text/javascript">
		new mainView();
		new mBeansView( { el: $("#mbeans_list ul") } );
		
		if (location.hash.length > 0) {
			var mbean = decodeURIComponent(location.hash.substring(1, location.hash.length));
			var domain = mbean.substring(0, mbean.indexOf(':'));
			$('#mbeans_list li.domain[data="' + domain + '"]').trigger('click');
		}
		
	</script>

</@layout.layout>
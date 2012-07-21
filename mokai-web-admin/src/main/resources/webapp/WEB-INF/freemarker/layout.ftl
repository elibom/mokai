<#macro layout>
<!DOCTYPE html>
<html>
	<head>
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
		
		<title>Mokai - Messaging Gateway</title>
		<link rel="stylesheet" type="text/css" href="/css/bootstrap.min.css">
		<link rel="stylesheet" type="text/css" href="/css/style.css">
		
		<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js"></script>
		<script src="/js/jquery.atmosphere.js"></script>
		<script src="/js/underscore.js"></script>
		<script src="/js/bootstrap.min.js"></script>
	</head>
	
	<body>
		<#if authenticated >
		<div class="navbar navbar-fixed-top">
	  		<div class="navbar-inner">
	    		<div class="container">
	    			
	    			<!-- .btn-navbar is used as the toggle for collapsed navbar content -->
	      			<a class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse">
						<span class="icon-bar"></span>
						<span class="icon-bar"></span>
						<span class="icon-bar"></span>
					</a>
					
	    			<a class="brand" href="/">
	  					Mokai
					</a>
					
					<div class="nav-collapse">
		      			<ul class="nav">
		      				<#if tab = "dashboard">
		  						<li class="active"><a href="#">Dashboard</a></li>
		  					<#else>
		  						<li><a href="/">Dashboard</a></li>
		  					</#if>
		  					<#if tab = "applications-messages">
		  						<li class="active"><a href="/messages/applications">Applications Msgs</a></li>
		  					<#else>
		  						<li><a href="/messages/applications">Applications Msgs</a></li>
		  					</#if>
		  					<#if tab = "connections-messages">
		  						<li class="active"><a href="/messages/connections">Connections Msgs</a></li>
		  					<#else>
		  						<li><a href="/messages/connections">Connections Msgs</a></li>
		  					</#if>
						</ul>
						
						<ul class="nav pull-right">
							<li class="divider-vertical"></li>
							<li class="dropdown">
		   						<a href="#" class="dropdown-toggle" data-toggle="dropdown">Account <b class="caret"></b></a>
		    					<ul class="dropdown-menu">
		      						<li><a href="#">Change Password</a></li>
		      						<li><a id="logout" href="#">Logout</a></li>
		    					</ul>
		  					</li>
						</ul>
					</div>
	    		</div>
	  		</div>
		</div>
		</#if>
		
		<div id="content" class="container" style="height: 100%; min-height: 100%;">
			<#nested/>
		</div>
	
		<div id="loading-template" style="display:none">
			<div id="loading">
				<img src="/img/ajax-loader.gif" alt="Cargando ..." />
			</div>
		</div>
		
		<script id="message-template" type="text/template"> 
			<div class="modal" id="message-modal" style="width:350px; margin-left:-175px;">
	  			<div class="modal-header">
	    			<button type="button" class="close" data-dismiss="modal">×</button>
	    			<h3><%= title %></h3>
	  			</div>
	  			<div class="modal-body">
	    			<p><%= body %></p>
	  			</div>
	  			<div class="modal-footer">
	    			<a href="#" class="btn" data-dismiss="modal">Close</a>
	  			</div>
			</div>
		</script>
		
		<script type="text/javascript">
			
			$(document).ready(function() {
				$('#logout').click(function() {
					$.ajax({
						type: 'DELETE',
						url: '/sessions'
					}).always(function() {
						window.location = "/sessions/new";
					});
				});
			});
		</script>
	</body>
</html>
</#macro>
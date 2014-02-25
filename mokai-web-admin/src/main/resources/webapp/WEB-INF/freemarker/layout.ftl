<#macro layout>
<!DOCTYPE html>
<html>
	<head>
		<meta http-equiv="content-type" content="text/html; charset=UTF-8">
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
		
		<title>Mokai - Messaging Gateway</title>
		<link rel="stylesheet" type="text/css" href="/css/bootstrap.min.css">
		<link rel="stylesheet" type="text/css" href="/css/style.css">
		
		<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js"></script>
		<script src="/js/jquery.atmosphere.js"></script>
		<script src="/js/underscore.js"></script>
		<script src="/js/bootstrap.min.js"></script>
		<script type="text/javascript" src="/js/common.js"></script>
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
			  					<#if tab = "jmx">
			  						<li class="active"><a href="/jmx">Management</a></li>
			  					<#else>
			  						<li><a href="/jmx">Management</a></li>
			  					</#if>
							</ul>
							
							<ul class="nav pull-right">
								<li class="divider-vertical"></li>
								<li class="dropdown">
			   						<a href="#" class="dropdown-toggle" data-toggle="dropdown">Account <b class="caret"></b></a>
			    					<ul class="dropdown-menu">
			      						<li><a id="showChangePassword" href="#changePassword">Change Password</a></li>
			      						<li><a id="logout">Logout</a></li>
			    					</ul>
			  					</li>
							</ul>
						</div>
		    		</div>
		  		</div>
			</div>
			<div class="header-space"></div>
			<div id="password_changed_alert" class="alert alert-info" style="display:none; text-align:center;">
		  		<a class="close" data-dismiss="alert" href="#">&times;</a>
	  			The password was changed successfully.
			</div>
		</#if>
		
		<div id="content" class="container" style="height: 100%; min-height: 100%;">
			<#nested/>
		</div>
		
		<div class="modal hide" id="changePassword">
	  		<div class="modal-header">
	    		<button type="button" class="close" data-dismiss="modal">×</button>
	    		<h3>Change password</h3>
	  		</div>
	  		<form id="change_password_form" class="form-horizontal" onsubmit="return false;" style="padding-top: 20px;">
	  			<div class="modal-body">
	  				<div class="control-group">
	    				<label class="control-label">New Password</label>
	    				<div class="controls">
	  						<input id="new_password" type="password" class="span3">
	  					</div>
	  				</div>
	  				<div class="control-group">
	    				<label class="control-label">Confirm Password</label>
	    				<div class="controls">
	  						<input id="confirm_password" type="password" class="span3">
	  					</div>
	  				</div>
	  			</div>
	  			<div class="modal-footer">
	  				<a href="#" class="btn" data-dismiss="modal">Close</a>
	    			<input type="submit" class="btn btn-primary" value="Submit">
	  			</div>
	  		</form>
	
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
			
				function error(component, message, focus, callback) {
					if (focus) {
							component.focus();
						}
						if ( component.siblings('span.help-inline').length === 0 ) {
							component.after('<span class="help-inline">' + message + '</requerido>');
						} else {
							component.siblings('span').html(message);
						}
						component.closest('.control-group').addClass('error');
						component.blur(function() {
							callback( $(this), false );
						});
				}
				
				function ok(component) {
					component.closest('.control-group').removeClass('error');
					component.siblings('span.help-inline').remove();
				}
			
				function validateNewPassword(component, focus) {
				
					var value = component.val();
				
					if ( !isNotEmpty(value) ) {
						error(component, 'Required', focus, validateNewPassword);
						return false;
					} else if ( !isValidPassword(value) ) {
						error(component, 'Too Weak', focus, validateNewPassword);
						return false;
					}
					
					ok(component);
					
					return true;
					
				}
				
				function validateConfirmPassword(component, focus) {
					
					var value = component.val();
					var password = $('input#new_password').val();
				
					if ( !isNotEmpty(value) ) {
						error(component, 'Required', focus, validateNewPassword);
						return false;
					} else if (value !== password) {
						error(component, "Doesn't match", focus, validateNewPassword);
						return false;
					}
					
					ok(component);
					
					return true;
					
				}
				
				$().alert();
				
				$('#showChangePassword').click(function() {
					$('#change_password_form')[0].reset();
					$('#changePassword').modal('show');
					$('.nav-collapse').collapse('hide');
					$('input#new_password').focus();
					
					return false;
				});
				
				ajaxBind( $('#change_password_form') );
				$('#change_password_form').submit(function() {
					
					var valid = true;
					
					valid = validateNewPassword( $('input#new_password'), true );
					if ( valid && !validateConfirmPassword($('input#confirm_password'), true) ) {
						valid = false;	
					}
					
					if (valid) {
						
						App.request = $.ajax({
							type: 'PUT',
							url: '/admin',
							data: '{"password": "' + $('input#new_password').val() + '"}'
						});
						
						App.request.done(function(data) {
							stopTimers();
							$('#fade').fadeOut();
							$('#password_changed_alert').slideDown('slow');
							setTimeout(function() { $('#password_changed_alert').slideUp('slow'); }, 3000);
							$('#change_password_form')[0].reset();
							$('input#new_password').off();
							$('input#confirm_password').off();
							$('#changePassword').modal('hide');
						});
						
						App.request.fail(function(jqXHR) {
							stopTimers();
							
							if (jqXHR.status != 200) { // error
								
								var output = App.errorTemplate({title: "Unexpected Error", body: "<strong>¡Sorry!</strong> Something went wrong. Please check Mokai's logs for more information." });
								$('#fade').html( output );
									
							}
						});
						
					
					}
					
				});
				
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
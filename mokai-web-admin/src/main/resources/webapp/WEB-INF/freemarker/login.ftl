<#ftl encoding="UTF-8">
<#import "layout.ftl" as layout>
<@layout.layout>
	<div class="modal" id="login">
  		<div class="modal-header">
    		<h2>Login</h2>
  		</div>
  		<div class="alert alert-error" style="display:none;">
  			Invalid user/password combination
		</div>
  		<form class="form-horizontal" onsubmit="return false;" style="padding-top: 20px;">
  			<div class="modal-body">
  				<div class="control-group">
    				<label class="control-label">Username</label>
    				<div class="controls">
  						<input id="username" type="text" class="span3">
  					</div>
  				</div>
  				<div class="control-group">
    				<label class="control-label">Password</label>
    				<div class="controls">
  						<input id="password" type="password" class="span3">
  					</div>
  				</div>
  			</div>
  			<div class="modal-footer">
    			<input type="submit" class="btn btn-primary" value="Submit">
  			</div>
  		</form>
	</div>
	
	<script type="text/javascript">
		
		$(document).ready(function() {
			$('input#username').focus();
			
			function validateInput(component, focus) {
				
				var value = component.val();
			
				if ( !isNotEmpty(value) ) {
					if (focus) {
						component.focus();
					}
					if ( component.siblings('span.help-inline').length === 0 ) {
						component.after('<span class="help-inline">Requerido</requerido>');
					}
					component.closest('.control-group').addClass('error');
					component.blur(function() {
						validateInput( $(this), false );
					});
					
					return false;
				}
				
				component.closest('.control-group').removeClass('error');
				component.siblings('span.help-inline').remove();
				
				return true;
				
			}
			
			ajaxBind( $('form') );
			$('form').submit(function() {
				var valid = true;
				
				valid = validateInput( $('input#password'), true );
				if ( !validateInput($('input#username'), true) ) {
					valid = false;	
				}
				
				if (valid) {
				
					App.request = $.ajax({
						type: 'POST',
						url: '/sessions',
						data: '{"username": "' + $('input#username').val() + '", "password": "' + $('input#password').val() + '"}'
					});
					
					App.request.done(function(data) {
						stopTimers();
						$('#fade').fadeOut();
						location.href = '/';
					});
					
					App.request.fail(function(jqXHR) {
						stopTimers();
						
						if (jqXHR.status == 401) { // unauthorized
							$('#fade').fadeOut();
							$('.alert-error').slideDown('slow');
						} else { // usually a status 500
							
							var output = App.errorTemplate({title: "Unexpected Error", body: "<strong>¡Sorry!</strong> Something went wrong. Please check Mokai's logs for more information." });
							$('#fade').html( output );
								
						}
					});
				}
			});
		});
		
	</script>
</@layout.layout>
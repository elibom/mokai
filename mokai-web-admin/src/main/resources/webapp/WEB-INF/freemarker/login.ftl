<#ftl encoding="UTF-8">
<#import "layout.ftl" as layout>
<@layout.layout>
	<div class="modal" id="login">
  		<div class="modal-header">
    		<h2>Login</h2>
  		</div>
  		<form class="form-horizontal" onsubmit="return false;">
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
			
				if (value.length == 0 || value.replace(/\s/g, '').length == 0) {
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
			
			$('form').submit(function() {
				var valid = true;
				
				valid = validateInput( $('input#password'), true );
				if ( !validateInput($('input#username'), true) ) {
					valid = false;	
				}
				
				
			});
		});
		
	</script>
</@layout.layout>
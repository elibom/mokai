	var App = {};
	
	function ajaxBind($form) {
		
		// ajax start
		$form.bind('ajaxStart', function() {
					
			// add the fade layer to the DOM
			if ( $('div#fade').length === 0 ) {
				$('body').append('<div id="fade"></div>');
			}
			$('#fade').html( $('#loading-template').html() );
					
			$('#fade').fadeIn(); // show the fade layer
					
			App.timeoutWarnTimer = setTimeout(function() {
				$('#fade #loading').css('background-color', '#FCF8E3');
				$('#fade #loading img').attr('src', "/img/ajax-loader-warn.gif");
			}, 8000);
					
			App.timeoutErrorTimer = setTimeout(function() {
				App.request.abort();
				var output = App.errorTemplate({title: "No response", body: "Check your internet connection and try again." });
				$('#fade').html( output );
			}, 30000);
		});
		
	}
	
	function stopTimers() {
		clearTimeout( App.timeoutWarnTimer );
		clearTimeout( App.timeoutErrorTimer );
	}

	$(document).ready(function() {
		
		// helper method to send ajax requests and receive json documents
		$.ajaxSetup({ 'beforeSend': function(xhr) { xhr.setRequestHeader("Accept", "text/javascript"); } });
		
		// compile error template and setup the close bindings
		App.errorTemplate = _.template( $('#message-template').html() );
		$('#fade a.btn, #fade .close').live('click', function() {
			$('#fade').fadeOut();
			return false;
		});
	});

	function validateField($field, validators, valid) {
		var v =  doValidateField($field, validators, true);
		return valid ? v : valid;
	}
			
	function doValidateField($field, validators, focusOnFail) {
		var val = $field.val();
				
		var valid = true;
		if ( !$.isArray(validators) ) {
			validators = [ validators ];
		}
				
		for (var i=0; i < validators.length && valid; i++) {
					
			var validator = validators[i];
			if ( !validator["function"](val) ) {
				valid = false;
				App.errValidator = validator;
			}
					
		}
				
		if ( !valid ) { 

			$field.parent().addClass('field_with_errors');
			$field.siblings('.error').css('display', 'inline').html(App.errValidator.message).show();
					
			if (focusOnFail) {
				$field.focus();
			}
					
			$field.blur(function() {
				doValidateField( $field, validators, false );
			});
					
		} else {
					
			$field.parent().removeClass('field_with_errors');
			$field.siblings('.error').hide();
		}
				
		return valid;
	}
	
	function isEmpty(value) {
		if (value.length == 0 || value.replace(/\s/g, '').length == 0) {
			return true;
		}
				
		return false;
	}
			
	function isNotEmpty(value) {
		if (value.length == 0 || value.replace(/\s/g, '').length == 0) {
			return false;
		}
				
		return true;
	}
	
	function isFloat (n) {
		if (isEmpty(n) || isNaN(n)) {
			return false;
		}
		
		n = Number(n);
		
		return n===+n && n!==(n|0);
	}

	function isInteger (n) {
		if (isEmpty(n) || isNaN(n)) {
			return false;
		}
		
		n = Number(n);
		
		return n===+n && n===(n|0);
	}
			
	function isEmail(value) { 
		var re = /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
		return re.test(value);
	}
			
	function isPasswordNotTooShort(value) {
		return value.length > 5;
	}
			
	function isPasswordNotTooLong(value) {
		return value.length < 41;
	}
			
	function isValidPassword(value) {
		return checkPassword(value) > 50;
	}
	
	function checkPassword(password) {
		
		var m_strUpperCase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		var m_strLowerCase = "abcdefghijklmnopqrstuvwxyz";
		var m_strNumber = "0123456789";
		var m_strCharacters = "!@#$%^&*?_~";
		
	    var score = 0;

	    // length
	    if (password.length < 5) {
	    	score += 5;
	    } else if (password.length > 4 && password.length < 8) {
	        score += 10;
	    } else if (password.length > 7)
	    {
	        score += 25;
	    }

	    // letters
	    var nUpperCount = countContain(password, m_strUpperCase);
	    var nLowerCount = countContain(password, m_strLowerCase);
	    var nLowerUpperCount = nUpperCount + nLowerCount;
	    
	    // case
	    if (nUpperCount == 0 && nLowerCount != 0) { // only lowercase 
	        score += 10; 
	    } else if (nUpperCount != 0 && nLowerCount != 0) { // both upper and lowercase
	        score += 20; 
	    } 

	    // numbers
	    var nNumberCount = countContain(password, m_strNumber);
	    if (nNumberCount == 1) {
	        score += 10;
	    }
	    if (nNumberCount >= 3) {
	        score += 20;
	    }

	    // characters
	    var nCharacterCount = countContain(password, m_strCharacters);
	    if (nCharacterCount == 1) {
	        score += 10;
	    }   
	    if (nCharacterCount > 1) {
	        score += 25;
	    }

	    // Bonus
	    // -- Letters and numbers
	    if (nNumberCount != 0 && nLowerUpperCount != 0){
	        score += 2;
	    }
	    // -- Letters, numbers, and characters
	    if (nNumberCount != 0 && nLowerUpperCount != 0 && nCharacterCount != 0) {
	        score += 3;
	    }
	    // -- Mixed case letters, numbers, and characters
	    if (nNumberCount != 0 && nUpperCount != 0 && nLowerCount != 0 && nCharacterCount != 0) {
	        score += 5;
	    }


	    return score;
	}

	//checks a string for a list of characters
	function countContain(strPassword, strCheck) { 
		
	    var nCount = 0;

	    for (var i = 0; i < strPassword.length; i++) {
	        if (strCheck.indexOf(strPassword.charAt(i)) > -1) { 
	        	nCount++;
	        } 
	    } 

	    return nCount; 
	} 
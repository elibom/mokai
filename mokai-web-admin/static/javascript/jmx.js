fixType = function(s) {
		
	var lastPeriodPos = s.lastIndexOf(".");
			
	if ((/^\[/).test(s)) {
		return "Array";
	}
			
	if (lastPeriodPos != -1) {
		return s.substring(lastPeriodPos + 1, s.length) ;
	}
			
	return s;
}
		
var showBean = function(mbean) {
		
	$.ajax({
		url: '/jmx/' + mbean,
		dataType: 'json'
	}).done(function(data) {
		$('#fade').fadeOut();
		new mBeanView(data);
	}).fail(function(jqXHR, textStatus) {
		if (jqXHR.status == 404) {
			$('#fade').fadeOut();
			alert("Not found!");
		} else {
			var output = App.errorTemplate({title: "Unexpected Error", body: "<strong>¡Sorry!</strong> Something went wrong. Please check Mokai's logs for more information." });
			$('#fade').html( output );
		}
	});
}

var paramsTypes = function(params) {
	
	var ret = [];
	for (var i=0; i < params.length; i++) {
		ret.push( params[i].type );
	}
	
	return ret;
}
		
var invokeOp = function(mbean, operation, jAlert, data) {
			
	$.ajax({
		type: 'POST',
		url: '/jmx/' + mbean + '/operations/' + operation.name,
		data: '{ "params": ' + JSON.stringify(data) + ', "signature": ' + JSON.stringify(paramsTypes(operation.params)) + ' }',
		contentType: "application/json; charset=utf-8"
	}).done(function(resp) {
		$('#fade').hide();
						
		if (operation.returnType === "void") {
			jAlert.show();
			setTimeout(function() { jAlert.slideUp('slow'); }, 2500);
		} else {
			$(".return_info h3").html(operation.name);
			$(".return_info .modal-body p").html('' + resp);
			$(".return_info").modal();
		}
	}).fail(function(jqXHR, textStatus) {
		if (jqXHR.status == 404) {
			$('#fade').fadeOut();
			alert("Not found!");
		} else {
			var output = App.errorTemplate({title: "Unexpected Error", body: "<strong>¡Sorry!</strong> Something went wrong. Please check Mokai's logs for more information." });
			$('#fade').html( output );
		}
	});	
				
}
	
var mainView = Backbone.View.extend({
		
	initialize: function() {
		setInterval( _.bind(this.updateMetrics, this), 2000);
				
		if (location.hash.length > 0) {
			showBean(location.hash.substring(1, location.hash.length));
		}
	},
		
	updateMetrics: function() {
		this.updateMemory();
		this.updateThreading();
	},
			
	updateMemory: function() {
		$.ajax({
			url: '/jmx/' + encodeURIComponent('java.lang:type=Memory') + "/attributes/HeapMemoryUsage",
			dataType: 'json',
			global: false
		}).done(function(data) {
			var value = $.parseJSON(data.value);

			$("div.featured div#used_memory span.value").html( (value.used / (1024 * 1024)).toFixed(0) + "MB" );
			$("div.featured div#max_memory span.value").html( (value.max / (1024 * 1024)).toFixed(0) + "MB" );
						
		}).fail(function(jqXHR, textStatus) {
			
		});
	},
			
	updateThreading: function() {
		$.ajax({
			url: '/jmx/' + encodeURIComponent('java.lang:type=Threading') + "/attributes/ThreadCount",
			dataType: 'json',
			global: false
		}).done(function(data) {
			$("div.featured div#thread_count span.value").html( data.value);
		}).fail(function(jqXHR, textStatus) {
						
		});
	}
			
});
	
var mBeansView = Backbone.View.extend({
		
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
		showBean(mbean);
				
		return false;
	}
		
});
		
var mBeanView = Backbone.View.extend({
		
	el: $('#mbean_info'),
		
	template: _.template( $('#mbean_template').html() ),
			
	initialize: function(options) {
            this.options = options;
		location.hash = encodeURIComponent(options.name);
		this.render();
	},
			
	render: function() {
		this.$el.parent().addClass("mbean_view");
		this.$el.html( this.template(this.options) );
		_.each(this.options.attributes, function(attribute) {
			new mBeanAttribute( { el: $('<tr></tr>').appendTo( $('.attributes table', this.$el) ), attribute: attribute, mbean: this.options.name } );
		}, this);
		_.each(this.options.operations, function(operation) {
			new mBeanOperation( { el: $('<div class="operation"></div>').appendTo( $('.operations', this.$el) ), operation: operation, mbean: this.options.name } );
		}, this);
		$("html, body, #content").animate({scrollTop:0}, 'slow');
	},
	
	events: {
		"click #cmdBack": "close"
	},
	
	close: function() {
		this.$el.parent().removeClass("mbean_view");
	}
		
});

var mBeanAttribute = Backbone.View.extend({
	
	template: _.template( $('#attribute_template').html() ),
	
	initialize: function(options) {
            this.options = options;
		_.bindAll(this, 'show', 'edit', 'cancel');
		this.render(options.attribute );
	},
	
	render: function(attribute) {
		this.$el.html( this.template({attribute: attribute}) );
	},
	
	events: {
		"dblclick .attr_value": "show",
		"change .attr_value input": "edit",
		"blur .attr_value input": "cancel",
		"keyup .attr_value input": "keyup"
		
	},
	
	show: function(event) {
		if (this.options.attribute.writable) {
			var originalVal = this.options.attribute.value ? this.options.attribute.value : "";
			
			// support only boolean, int, long and String
			if ( !(/^(int|long|boolean|java.lang.integer|java.lang.long|java.lang.boolean|java.lang.string)$/i).test(this.options.attribute.type) ) {
				return;
			}
			
			var width = 50;
			if (this.options.attribute.type === 'java.lang.String') {
				width = 150;
			}
			
			$(event.currentTarget).html('<input type="text" value="' + originalVal + '" style="width: ' + width + 'px; height: 15px; line-height: 1em; padding: 2px 4px; margin-bottom: 0;">');
			$('input', event.currentTarget).focus();
		}
	},
	
	keyup: function(e) {
		if (e.keyCode == 27) {
			this.cancel();
		} else if (e.keyCode == 13) {
			var originalVal = this.options.attribute.value ? this.options.attribute.value : "";
			var val = $('td.attr_value input', this.$el).val();
			if (val != originalVal) {
				this.edit();
			} else {
				this.cancel();
			}
		}
	},
	
	edit: function() {
		var that = this;
		
		var val = $('td.attr_value input', this.$el).val();
		$('td.attr_value', this.$el).html(val);		
		
		$.ajax({
			url: '/jmx/' + encodeURIComponent(this.options.mbean) + "/attributes/" + this.options.attribute.name,
			type: 'POST',
			data: '{"value": ' + val + '}',
			dataType: 'json'
		}).done(function(data) {
			$('#fade').fadeOut();
			that.options.attribute.value = val;
		}).fail(function(jqXHR, textStatus) {
			$('#fade').fadeOut();
			
			var originalVal = this.options.attribute.value ? this.options.attribute.value : "";
			$('td.attr_value', this.$el).html(originalVal);	
			alert("Couldn't update attribute. Please try again.\nError: " + textStatus);			
		});
	},
	
	cancel: function() {
		var originalVal = this.options.attribute.value ? this.options.attribute.value : "";
		$('td.attr_value', this.$el).html( originalVal );
	}
	
});
		
var mBeanOperation = Backbone.View.extend({
	
	paramsViews: [],
			
	template: _.template( $('#operation_template').html() ),
			
	initialize: function(options) {
            this.options = options;
		this.render( options.operation );
	},
			
	render: function(operation) {
		var params = "";
		for (var i=0; i < operation.params.length; i++) {
			var param = operation.params[i];
			if (i === 0) {
				params = fixType(param.type) + ' <em>' + param.name + '</em>';
			} else {
				params += ', ' + fixType(param.type) + ' <em>' + param.name + '</em>';
			}		
		}
				
		this.$el.html( this.template({operation: operation, params: params}) );
				
	},
			
	events: {
		"click button.invoke": "invoke"
	},
			
	invoke: function(event) {
			
		var operation = this.options.operation;
		var mbean = encodeURIComponent(this.options.mbean);
				
		if (operation.params.length === 0) {
			invokeOp(mbean, operation, $(event.currentTarget).parents('div.operation').children('.alert'), []);
		} else {
			new invokeOperation( { operation: operation, mbean: this.options.mbean, alert: $(event.currentTarget).parents('div.operation').children('.alert') } );
		}
				
		return false;
	}
		
});
		
// the view of the modal that allows users to invoke operations with parameters
var invokeOperation = Backbone.View.extend({
	
	params: [],
			
	template: _.template( $('#params_template').html() ),
			
	initialize: function() {
		this.params = [];
		_.bindAll(this, 'invoke');
		this.render(this.options.operation);
	},
			
	render: function(operation) {		
		this.$el.addClass('modal hide fade').html( this.template({ operation: operation}) );
		_.each(operation.params, function(param) {
			
			if (param.type === 'boolean') {
				var p = new booleanParam( {param: param} );
				p.render();
				$('.modal-body', this.$el).append(p.el);
			} else {
				var p = new textParam( {param: param});
				p.render();
				$('.modal-body', this.$el).append(p.el);
			}
			
			this.params.push(p);
		}, this);
		
		this.$el.modal();
	},
			
	events: {
		"click button.submit" : "invoke"
	},
			
	invoke: function() {
			
		var operation = this.options.operation;
		var mbean = encodeURIComponent(this.options.mbean);
		
		var valid = true;
		var data = [];
		for (var i=0; i < this.params.length; i++) {
			var param = this.params[i];
			
			if (param.isValid()) {
				var pVal = param.val();
				data.push( pVal );
			} else {
				valid = false;
			}
		}
				
		if (valid) {
			invokeOp(mbean, operation, this.options.alert, data);		
			this.$el.modal('hide');
		}
			
		return false;
	}
			
});

var textParam = Backbone.View.extend({
	
	tag: 'div',
	className: 'control-group',
	
	initialize: function() {
		_.bindAll(this, 'val', 'isValid');
	},
	
	template: _.template( ['<label class="control-label" for="<%= param.name %>"><%= param.name %>:</label>',
	                       '<div class="controls">',
	                       '	<input type="text" id="<%= param.name %>" placeholder="<%= param.type %>" class="input-small">',
	                       '	<span class="help-inline description"><%= param.description %></span>',
	                       '</div>'].join('') ),
	
	render: function() {
		this.$el.html( this.template({ param: this.options.param }) );
	},

	val: function() {
		return this.$el.find('input').val();
	},
	
	isValid: function() {
		
		var value = this.val();
		var type = this.options.param.type;
		
		if (type === 'long' || type === 'int') {
			if (!isInteger(value)) {
				this.$el.addClass("error");
				$('span.help-inline', this.$el).removeClass('description').html('Must be of type ' + type);
				return false;
			}
		}
		
		if (type === 'double' || type === 'float') {
			if (!isInteger(value) && !isFloat(value)) {
				this.$el.addClass("error");
				$('span.help-inline', this.$el).removeClass('description').html('Must be of type ' + type);
				return false;
			}
		}
		
		return true;
	}
	
});

var booleanParam = Backbone.View.extend({
	
	tag: 'div',
	className: 'control-group',
	
	initialize: function() {
		_.bindAll(this, 'val', 'isValid');
	},
	
	template: _.template( ['<label class="control-label" for="<%= param.name %>"><%= param.name %>:</label>',
	                       '<div class="controls">',
	                       '	<label class="checkbox" for="<%= param.name %>">',
	                       '		<input type="checkbox" id="<%= param.name %>"> <span class="help-inline description"><%= param.description %></span>',
	                       '	</label>',
	                       '</div>'].join('') ),
	
	render: function() {
		this.$el.html( this.template({ param: this.options.param }) );
	},
	
	val: function() {
		if (this.$el.find('input').is(':checked')) {
			return "true";
		}
		
		return "false";
	},
	
	isValid: function() {
		return true;
	}
	
});
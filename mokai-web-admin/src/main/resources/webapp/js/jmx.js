var fixType = function(s) {
		
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
		
	App.request = $.ajax({
		url: '/jmx/' + mbean,
		dataType: 'json'
	});			
	App.request.done(function(data) {
		stopTimers();
		$('#fade').fadeOut();
					
		new mBeanView(data);
					
	});
	App.request.fail(function(jqXHR, textStatus) {
		stopTimers();
					
		if (jqXHR.status == 404) {
			$('#fade').fadeOut();
			alert("Not found!");
		} else {
			var output = App.errorTemplate({title: "Unexpected Error", body: "<strong>¡Sorry!</strong> Something went wrong. Please check Mokai's logs for more information." });
			$('#fade').html( output );
		}
	});
}
		
var invokeOp = function(mbean, operation, jAlert, data) {
			
	App.request = $.ajax({
		type: 'POST',
		url: '/jmx/' + mbean + '/operations/' + operation.name,
		data: '[' + data + ']'
	});
	App.request.done(function(resp) {
		stopTimers();
		$('#fade').hide();
						
		if (operation.returnType === "void") {
			jAlert.show();
			setTimeout(function() { jAlert.slideUp('slow'); }, 2500);
		} else {
			$(".return_info h3").html(operation.name);
			$(".return_info .modal-body p").html('' + resp);
			$(".return_info").modal();
		}
	});
	App.request.fail(function(jqXHR, textStatus) {
		stopTimers();
					
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
	},
			
	updateThreading: function() {
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
			
	initialize: function() {
		location.hash = encodeURIComponent(this.options.name);
		this.render();
	},
			
	render: function() {
		this.$el.html( this.template(this.options) );
		_.each(this.options.operations, function(operation) {
			new mBeanOperation( { el: $('<div class="operation"></div>').appendTo( $('.operations', this.$el) ), operation: operation, mbean: this.options.name} );
		}, this);
	}
		
});
		
var mBeanOperation = Backbone.View.extend({
			
	template: _.template( $('#operation_template').html() ),
			
	initialize: function() {
		this.render( this.options.operation );
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
			
	template: _.template( $('#params_template').html() ),
			
	initialize: function() {
		this.render(this.options.operation);
	},
			
	render: function(operation) {		
		this.$el.addClass('modal hide fade').html( this.template({ operation: operation}) );
		this.$el.modal();
	},
			
	events: {
		"click button.submit" : "invoke"
	},
			
	invoke: function() {
			
		var operation = this.options.operation;
		var mbean = encodeURIComponent(this.options.mbean);
				
		var inputs = $('form input', this.$el);
		var data = [];
		for (var i=0; i < inputs.length; i++) {
			var input = inputs[i];
			data.push( $(input).val() );
		}
				
		invokeOp(mbean, operation, this.options.alert, data);
					
		this.$el.modal('hide');
			
		return false;
	}
			
});
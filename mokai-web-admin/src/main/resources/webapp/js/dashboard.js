var MainView = Backbone.View.extend({
  events: {
    "click section a.cmd-applications-info": "showApplicationInfo",
    "click section a.cmd-connections-info": "showConnectionInfo",
    "click section a.cmd-applications-start": "startApplication",
    "click section a.cmd-applications-stop": "stopApplication",
    "click section a.cmd-connections-start": "startConnection",
    "click section a.cmd-connections-stop": "stopConnection"
  },

  showApplicationInfo: function(e) {
    e.preventDefault();  
    this.showConnectorInfo($(e.currentTarget), 'applications');
  },

  showConnectionInfo: function(e) {
    e.preventDefault();
    this.showConnectorInfo($(e.currentTarget), 'connections');  
  },

  startApplication: function(e) {
    e.preventDefault();
    this.startStopConnector($(e.currentTarget), 'applications', 'start');  
  },

  stopApplication: function(e) {
    e.preventDefault();
    this.startStopConnector($(e.currentTarget), 'applications', 'stop');
  },

  startConnection: function(e) {
    e.preventDefault();
    this.startStopConnector($(e.currentTarget), 'connections', 'start');
  },

  stopConnection: function(e) {
    e.preventDefault();
    this.startStopConnector($(e.currentTarget), 'connections', 'stop');  
  },

  showConnectorInfo: function(elem, type) {
    new ConnectorModalView({ elem: elem, type: type });
  },

  startStopConnector: function(elem, type, state) {
    var id = elem.closest('section').attr('data');
    App.request = $.ajax({
      type: "POST",
      url: "/" + type + "/" + id + "/" + state,
      dataType: "json"
    });

    App.request.done(function() {
      stopTimers();
      $('#fade').fadeOut();
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

var ConnectorModalView = Backbone.View.extend({
  initialize: function(options) {
    _.bindAll(this, "renderAcceptor", "renderValue", "renderAction");
    this.render(options);
  },

  render: function(options) {
    var connectorTemplate = _.template($('#connector-info').html());

    var id = options.elem.closest('section').attr('data');
    var that = this;
    App.request = $.ajax({
      url: "/" + options.type + "/" + id,
      dataType: "json"
    }).done(function(data) {
      stopTimers();
      $('#fade').fadeOut();
      var html = connectorTemplate(_.extend(data, { renderAcceptor: that.renderAcceptor, renderValue: that.renderValue, renderAction: that.renderAction }));
      $('#connector-modal').html(html);
      $('#connector-modal').modal();
    }).fail(function(jqXHR) {
      stopTimers();
            
      if (jqXHR.status != 200) { // error
        var output = App.errorTemplate({title: "Unexpected Error", body: "<strong>¡Sorry!</strong> Something went wrong. Please check Mokai's logs for more information." });
        $('#fade').html( output );
      }
    });
  },

  renderAcceptor: function(acceptor, showIcon) {
    var acceptorTemplate = _.template($('#acceptor-template').html());
    return acceptorTemplate(_.extend(acceptor, { renderAcceptor: this.renderAcceptor, renderValue: this.renderValue }));
  },

  renderValue: function(value) {
    var helper = function(value, context) {
      if (_.isObject(value)) {
        if (value.name) return context.renderAcceptor(value, false);
        else return '<span class="acceptor-value">' + JSON.stringify(value) + "</span>";
      } else {
        return '<span class="acceptor-value">' + value + "</span>";
      }
    };

    if (_.isArray(value)) {
      return _.reduce(value, function(html, item) { 
        return html += helper(item, this);
      }, "", this);
    } else {
      return helper(value);
    }
  },

  renderAction: function(action) {
    var actionTemplate = _.template($('#action-template').html());
    return actionTemplate(action);
  }
});
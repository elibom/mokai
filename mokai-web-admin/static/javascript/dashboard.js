/* global _,Backbone */

var MainView = Backbone.View.extend({
    initialize: function () {
        this.updateEndpoints();
    },
    updateEndpoints: function () {
        var endPointItemTemplate = _.template($('#endpoint-item-template').text());
        var that = this;
        $.ajax({
            type: 'GET',
            url: '/data/'
        }).done(function (response) {
            that.clearInfo();

            var applicationsContainer = $('#applications-container', that.$el);
            response.applications.forEach(function (application) {
                application.class = 'applications';
                applicationsContainer.append(endPointItemTemplate({endpoint: application}));
                new EndpointItemView({el: $('#' + application.type + '-' + application.id, that.$el), endpoint: application});
            });

            var connectionsContainer = $('#connections-container', that.$el);
            response.connections.forEach(function (connection) {
                connection.class = 'connections';
                connectionsContainer.append(endPointItemTemplate({endpoint: connection}));
                new EndpointItemView({el: $('#' + connection.type + '-' + connection.id, that.$el), endpoint: connection});
            });
        });
    },
    clearInfo: function () {
        $('#applications-container', this.$el).empty();
        $('#connections-container', this.$el).empty();
    }
});

var EndpointItemView = Backbone.View.extend({
    initialize: function (options) {
        _.bindAll(this, 'render', 'startStopConnector', 'showConnectorInfo');
        this.endpoint = options.endpoint;
        this.render();
    },
    events: {
        'click #endpoint-state': 'startStopConnector',
        'click #endpoint-info': 'showConnectorInfo'
    },
    showConnectorInfo: function () {
        new EndpointModalView({endpoint: this.endpoint});
    },
    render: function () {
        if (this.endpoint.status === 'OK') {
            this.$el.addClass('panel-success');
        } else if (this.endpoint.status === 'FAILED') {
            this.$el.addClass('panel-danger');
        } else {
            this.$el.addClass('panel-warning');
        }
        if (this.endpoint.state === 'STARTED') {
            this.$('#endpoint-state').text('STOP');
        } else if (this.endpoint.state === 'STOPPED') {
            this.$('#endpoint-state').text('START');
        }
    },
    startStopConnector: function () {
        var that = this;
        function getNewStatus() {
            if (that.endpoint.state === 'STARTED') {
                return 'stop';
            } else if (that.endpoint.state === 'STOPPED') {
                return 'start';
            }
            return 'start';
        }
        $.ajax({
            type: 'POST',
            url: '/' + this.endpoint.class + '/' + this.endpoint.id + '/' + getNewStatus(),
            dataType: 'json'
        }).done(function (response) {
            that.endpoint.state = response.newState;
            that.render();
        }).fail(function (jqXHR) {
            console.log('ERROR');
        });
    }
});

var EndpointModalView = Backbone.View.extend({
    initialize: function (options) {
        _.bindAll(this, "renderAcceptor", "renderValue", "renderAction");
        this.endpoint = options.endpoint;
        this.render();
    },
    render: function () {
        var connectorTemplate = _.template($('#endpoint-info-template').html());

        var that = this;
        $.ajax({
            url: "/" + this.endpoint.class + "/" + this.endpoint.id,
            dataType: "json"
        }).done(function (data) {
            var html = connectorTemplate(_.extend(data, {renderAcceptor: that.renderAcceptor, renderValue: that.renderValue, renderAction: that.renderAction}));
            $('#endpoint-modal').html(html);
            $('#endpoint-modal .modal').modal();
        }).fail(function (jqXHR) {
            var output = App.errorTemplate({title: "Unexpected Error", body: "<strong>¡Sorry!</strong> Something went wrong. Please check Mokai's logs for more information."});
            $('#fade').html(output);
        });
    },
    renderAcceptor: function (acceptor, showIcon) {
        var acceptorTemplate = _.template($('#acceptor-template').html());
        return acceptorTemplate(_.extend(acceptor, {renderAcceptor: this.renderAcceptor, renderValue: this.renderValue}));
    },
    renderValue: function (value) {
        var helper = function (value, context) {
            if (_.isObject(value)) {
                if (value.name)
                    return context.renderAcceptor(value, false);
                else
                    return '<span class="acceptor-value">' + JSON.stringify(value) + "</span>";
            } else {
                return '<span class="acceptor-value">' + value + "</span>";
            }
        };

        if (_.isArray(value)) {
            return _.reduce(value, function (html, item) {
                return html += helper(item, this);
            }, "", this);
        } else {
            return helper(value);
        }
    },
    renderAction: function (action) {
        var actionTemplate = _.template($('#action-template').html());
        return actionTemplate(action);
    }
});
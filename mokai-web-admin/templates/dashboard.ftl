<#ftl encoding="UTF-8">
<#import "layout.ftl" as layout>
<@layout.layout>

    <div class="metrics row">
        <div id="failedMessages" class="metric">
            <span class="number red">-</span>
            <span class="tag">Failed</span>
        </div>
        <div id="unroutableMessages" class="metric">
            <span class="number red">-</span>
            <span class="tag">Unroutable</span>
        </div>
    </div>

    <div class="row">
        <div class="col-md-4 col-md-offset-1">
            <h3>Applications</h3>
            <div id="applications-container">
            </div>
        </div>
        <div class="col-md-4 col-md-offset-2">
            <h3>Connections</h3>
            <div id="connections-container">
            </div>
        </div>
    </div>

    <div id="endpoint-modal" class="row"></div>


    <script id="endpoint-item-template" type="text/template">
        <div class="panel" id="<% print(endpoint.type + '-' + endpoint.id) %>" >
            <div class="panel-heading">
                <div class="panel-title">
                    <div class="row">
                        <div class="col-xs-5">
                            <h5> <%= endpoint.id %> </h5>
                            <small> <%= endpoint.type %> </small>
                        </div>
                        <div class="btn-group col-xs-5 col-xs-offset-2">
                            <button id="endpoint-info" class="btn btn-default" href="#"><span class="glyphicon glyphicon-info-sign"></span></button>
                            <button id="endpoint-state" class="btn btn-default" href="#"><span class="glyphicon glyphicon-stop"></span>Stop</button>
                        </div>
                    </div>
                </div>
            </div>
            <div class="panel-body">
                <div>
                    <% if(endpoint.processor){ %>
                        print('<div class="left queued"><span>' + endpoint.queuedMessage + '</span> queued</div>')
                    <% } %>
                    <div class="right">
                        <span class="status"></span>
                    </div>
                </div>
            </div>
        </div>
    </script>

    <script id="endpoint-info-template" type="text/template">
        <div class="modal fade">
        <div class="modal-dialog">
        <div class="modal-content">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal">×</button>
            <h3><%= id %></h3>
            </div>
            <div class="modal-body">
            <ul class="nav nav-tabs">
            <% if (typeof(configuration) != "undefined") { %>
            <li class="active"><a href="#configuration" data-toggle="tab">Config</a></li>
            <% } %>
            <% if (isProcessor) { %>
            <li><a href="#acceptors" data-toggle="tab">Acceptors</a></li>
            <% } %>
            <% if (typeof(obj["pre-processing-actions"]) != "undefined" && obj["pre-processing-actions"].length > 0) { %>
            <li><a href="#pre-processing-actions" data-toggle="tab">Pre Processing</a></li>
            <% } %>
            <% if (typeof(obj["post-processing-actions"]) != "undefined" && obj["post-processing-actions"].length > 0) { %>
            <li><a href="#post-processing-actions" data-toggle="tab">Post Processing</a></li>
            <% } %>
            <% if (typeof(obj["post-receiving-actions"]) != "undefined" && obj["post-receiving-actions"].length > 0) { %>
            <li><a href="#post-receiving-actions" data-toggle="tab">Post Receiving</a></li>
            <% } %>
            </ul>

            <div class="tab-content">
            <% if (typeof(configuration) != "undefined") { %>
            <div class="tab-pane active" id="configuration">
            <table class="table table-bordered table-condensed table-striped">
            <% _.each(configuration, function(value, key) { %>
            <tr>
            <th width="10%"><%= key %></th>
            <td>
            <% if (_.isObject(value) || _.isArray(value)) { %>
            <%= JSON.stringify(value) %>
            <% } else { %>
            <%= value %>
            <% } %>
            </td>
            </tr>
            <% }) %>
            </table>
            </div>
            <% } %>

            <% if (isProcessor) { %>
            <div class="tab-pane" id="acceptors">
            <% _.each(acceptors, function(acceptor) { %>
            <%= renderAcceptor(acceptor, true) %>
            <% }) %>
            </div>
            <% } %>

            <% if (typeof(obj["pre-processing-actions"]) != "undefined" && obj["pre-processing-actions"].length > 0) { %>
            <div class="tab-pane" id="pre-processing-actions">
            <% _.each(obj["pre-processing-actions"], function(action) { %>
            <%= renderAction(action) %>
            <% }) %>
            </div>
            <% } %>

            <% if (typeof(obj["post-processing-actions"]) != "undefined" && obj["post-processing-actions"].length > 0) { %>
            <div class="tab-pane" id="post-processing-actions">
            <% _.each(obj["post-processing-actions"], function(action) { %>
            <%= renderAction(action) %>
            <% }) %>
            </div>
            <% } %>

            <% if (typeof(obj["post-receiving-actions"]) != "undefined" && obj["post-receiving-actions"].length > 0) { %>
            <div class="tab-pane" id="post-receiving-actions">
            <% _.each(obj["post-receiving-actions"], function(action) { %>
            <%= renderAction(action) %>
            <% }) %>
            </div>
            <% } %>
            </div>

        </div>
        <div class="modal-footer">
            <a href="#" class="btn" data-dismiss="modal">Close</a>
        </div>
        </div>
        </div>
        </div>
    </script>
    <script id="acceptor-template" type="text/template">
        <p><i class="icon-chevron-right"></i> <strong><%= name %></strong></p>
        <% if (typeof(configuration) != "undefined") { %>
        <div style="margin-left: 20px;">
        <table class="table table-bordered table-condensed">
        <% _.each(configuration, function(value, key) { %>
        <tr>
        <th width="10%"><%= key %></th>
        <td><%= renderValue(value, true) %></td>
        </tr>
        <% }) %>
        </table>
        </div>
        <% } %>
    </script>
    <script id="action-template" type="text/template">
        <p><i class="icon-chevron-right"></i> <strong><%= name %></strong></p>
        <% if (typeof(configuration) != "undefined") { %>
        <div style="margin-left: 20px;">
        <table class="table table-bordered table-condensed table-striped">
        <% _.each(configuration, function(value, key) { %>
        <tr>
        <th width="10%"><%= key %></th>
        <td>
        <% if (_.isObject(value) || _.isArray(value)) { %>
        <%= JSON.stringify(value) %>
        <% } else { %>
        <%= value %>
        <% } %>
        </td>
        </tr>
        <% }) %>
        </table>
        </div>
        <% } %>
    </script>

<script type="text/javascript" src="/javascript/lib/backbone-1.1.0.js"></script>
<script type="text/javascript" src="/javascript/dashboard.js"></script>
<script type="text/javascript">
    new MainView({el: 'body'});
</script>
</@layout.layout>
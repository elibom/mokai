<#macro layout>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="content-type" content="text/html; charset=UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">

        <title>Mokai</title>
        <link rel="stylesheet" href="/css/bootstrap-3.1.1.min.css"/>
        <link rel="stylesheet" type="text/css" href="/css/style.css">

        <script type="text/javascript" src="/javascript/lib/jquery-2.0.3.js"></script>
        <script type="text/javascript" src="/javascript/lib/underscore-1.5.2.js"></script>
        <script type="text/javascript" src="/javascript/lib/backbone-1.1.0.js"></script>
        <script type="text/javascript" src="/javascript/lib/bootstrap-3.1.1.min.js"></script>
    </head>

    <body>
        <#if user?? >
        <div class="navbar navbar-default" role="navigation">
            <div class="container-fluid">
                <div class="navbar-header">
                    <a class="navbar-brand text-center" href="/"><strong>Mokai^2</strong></a>
                </div>
                <div class="collapse navbar-collapse" id="navbar-collapse">
                    <ul class="nav navbar-nav">
                        <li id="menu-dashboard" ><a href="/">Dashboard</a></li>
                        <li id="menu-applications"><a href="/messages/applications">Applications Messages</a></li>
                        <li id="menu-connections"><a href="/messages/connections">Connections Messages</a></li>
                        <li id="menu-jmx"><a href="/jmx">Management</a></li>
                    </ul>
                    <ul class="nav navbar-nav navbar-right">
                        <li><a id="logout">Logout</a></li>
                    </ul>
                </div>
            </div>
        </div>
        </#if>
        <div id="content" class="container-fluid" >
            <div class="row">
                <div class="col-md-10 col-md-offset-1">
                    <#nested/>
                </div>
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

            $(document).ready(function () {

                if (window.location.href.endsWith('applications')) {
                    $('#menu-applications').addClass('active')
                } else if (window.location.href.endsWith('connections')) {
                    $('#menu-connections').addClass('active')
                } else if (window.location.href.endsWith('jmx')) {
                    $('#menu-jmx').addClass('active')
                } else {
                    $('#menu-dashboard').addClass('active')
                }

                $('#logout').click(function () {
                    $.ajax({
                        type: 'GET',
                        url: '/logout'
                    }).always(function () {
                        window.location = "/login";
                    });
                });

            });
        </script>
    </body>
</html>
</#macro>
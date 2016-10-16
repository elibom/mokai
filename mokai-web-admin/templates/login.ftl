<!DOCTYPE html>
<html lang="en">
    <head>
        <title>Mokai</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no"/>
        <link rel="stylesheet" href="css/bootstrap-3.1.1.min.css"/>
        <script type="text/javascript" src="javascript/lib/jquery-2.0.3.js"></script>
        <script type="text/javascript" src="javascript/lib/underscore-1.5.2.js"></script>
        <script type="text/javascript" src="javascript/lib/backbone-1.1.0.js"></script>
        <script type="text/javascript" src="javascript/lib/bootstrap-3.1.1.min.js"></script>
    </head>

    <body>
        <div class="container-fluid">
            <div id="header" class="row">
                <div class="col-lg-4 col-lg-offset-4">
                    <h3>Login</h3>
                </div>
            </div>
            <div id="body" class="row">
                <div class="col-lg-4 col-lg-offset-4">
                    <form role="form">
                        <div class="form-group">
                            <label for="user-input">Username</label>
                            <input type="text" class="form-control" id="user-input" placeholder="Username">
                        </div>
                        <div class="form-group">
                            <label for="pass-input">Password</label>
                            <input type="password" class="form-control" id="pass-input" placeholder="Password">
                        </div>
                        <div class="form-group">
                            <div class="checkbox">
                                <label>
                                    <input id="keep-input" type="checkbox" checked> Keep logged
                                </label>
                            </div>
                        </div>
                        <div id="errorMsg" class="text-danger" style="display: none">
                        </div>
                        <button id="btn-login" type="submit" class="btn btn-default">Submit</button>
                    </form>

                </div>
            </div>
        </div>
    </body>

    <script type="text/javascript" src="javascript/login.js"></script>
    <script type="text/javascript">
        app.init();
    </script>

</html>
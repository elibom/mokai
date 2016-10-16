var app = {};
app.events = _.extend({}, Backbone.Events);

var LoginView = Backbone.View.extend({
    el: $('#body'),
    initialize: function () {
        _.bindAll(this, 'submit', 'isValidForm', 'getFormData');
    },
    events: {
        'click #btn-login': 'submit'
    },
    submit: function (e) {
        e.preventDefault();
        if (!this.isValidForm()) {
            return;
        }
        var that = this;
        $.ajax({
            type: 'POST',
            url: '/login',
            beforeSend: function (xhr) {
                xhr.setRequestHeader("Accept", "html");
            },
            data: JSON.stringify(that.getFormData())
        }).done(function (response) {
            window.location.replace('/');
        }).error(function (response) {
            var errorCode = JSON.parse(response.responseText).errorCode;
            if (errorCode && errorCode === 'invalid_credentials') {
                $('#errorMsg').html('<p>Invalid username and/or password</p>').show();
            }
        });
    },
    isValidForm: function () {
        var hasErrors = false;
        userInput = $('#user-input', this.$el);
        passInput = $('#pass-input', this.$el);
        userInput.parent().removeClass('has-error');
        passInput.parent().removeClass('has-error');
        if (!userInput.val()) {
            userInput.parent().addClass('has-error');
            hasErrors = true;
        }
        if (!passInput.val()) {
            passInput.parent().addClass('has-error');
            hasErrors = true;
        }
        return !hasErrors;
    },
    getFormData: function () {
        var data = {
            username: $('#user-input', this.$el).val(),
            password: $('#pass-input', this.$el).val(),
            keepLogged: $('#keep-input', this.$el).is(':checked')
        };
        return data;
    }
});

app.init = function () {
    new LoginView();
};


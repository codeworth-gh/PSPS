/*jshint esversion: 6  */

function deleteUuid(sender, email){
    swal({
        title:"Are you sure you want to delete this invitation?",
        icon:"warning",
        buttons: {
            cancel:true,
            confirm:true
        }
    }).then( function(willDelete){
        if(willDelete) {
    var call = beRoutes.controllers.UserCtrl.deleteUserInvitation(sender, email);
            $.ajax({
                url: call.url,
                type: call.type,
                contentType: "application/json; charset=utf-8"
            }).done(function (data, status, xhr) {
                window.location.reload();
            }).fail(function (xhr, status, error) {
                console.log("error - " + error + ", status - " + status);
                console.log(xhr);
            });
        }
    });
}

function ResendEmail(email, protocol){
    var emailObj = {"email":email, "protocol":protocol[0].value};
    var call = beRoutes.controllers.UserCtrl.resendEmail(email, protocol[0].value);
    $.ajax({
        url: call.url,
        type: call.type,
        data: JSON.stringify(emailObj),
        contentType: "application/json; charset=utf-8"
    }).done(function (data, status, xhr) {
        swal("Email sent again to "+ email);
    }).fail(function (xhr, status, error) {
        console.log("error - " + error + ", status - " + status);
        console.log(xhr);
    });
}
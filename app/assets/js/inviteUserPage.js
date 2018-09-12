/*jshint esversion: 6  */

function deleteUuid(uuid){
    swal({
        title:"Are you sure you want to delete this invitation?",
        icon:"warning",
        buttons: {
            cancel:true,
            confirm:true
        }
    }).then( function(willDelete){
        if(willDelete) {
            new Playjax(beRoutes)
                .using(function(c){ return c.UserCtrl.apiDeleteInvitation(uuid);}).fetch()
                .then( function(res){
                    if (res.ok) {
                        window.location.reload();
                    } else {
                        Informationals.makeDanger("Deletion of invitation "+uuid+" failed", "See server log for details", 1500).show();
                    }
                });
        }
    });
}

function resendEmail(uuid){
    new Playjax(beRoutes).using(c=>c.UserCtrl.apiReInviteUser(uuid)).fetch()
        .then( resp => {
            if (resp.ok) {
                Informationals.makeSuccess("Invitation re-sent", "", 1500).show();
            } else {
                Informationals.makeDanger("Re-sending the invitation failed", "", 1500).show();
            }
        });
}
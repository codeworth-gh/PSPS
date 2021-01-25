/* jshint esversion:6 */
/**
 * Deals with displaying success/fail/info/blocking widgets to the user.
 *
 * Depends on UiUtils.js, Bootstrap 4
 */

const Informationals = (function(){
    const MESSAGE_TYPES = {
        YES_NO:"yesNoMessage",
        INFORMATION:"informational",
        BKG_PROCESS:"backgroundProcess"
    };

    function dismiss( emt ) {
        const $emt = $(emt);
        $emt.slideUp(300,function() { $emt.remove();} );
    }

    function initLoaderDialog() {
        const dialogHtml = "<div class=\"modal fade\" id=\"InformationalsLoaderModal\" tabindex=\"-1\" role=\"dialog\" aria-labelledby=\"myModalLabel\" aria-hidden=\"true\">\n" +
            "<div class=\"modal-dialog\"><div class=\"modal-content\"><div class=\"text-center\">\n" +
            "<br /><i class=\"fa fa-cog fa-spin fa-5x\"></i><hr /><h5 id=\"InformationalsLoaderModalText\" class=\"text-center\">Loading</h5><br />\n" +
            "</div></div></div></div>\n" +
            "<div id=\"InformationalsMessageContainer\">\n" +
            "</div>";
        $("body").append(dialogHtml);
        $loaderElement = $("#InformationalsLoaderModal").modal({
            keyboard: false,
            backdrop:"static",
            show: false
        });
        $loaderElementText = $loaderElement.find("#InformationalsLoaderModalText");
    }

    function initInformationalsArea() {
        const area = UiUtils.makeDiv({id:"InformationalsMessageContainer"});
        area.id = "InformationalsMessageContainer";
        $("body").append(area);
        $informationalsArea = $(area);
    }

    let $loaderElement = null;
    let $loaderElementText = null;
    let $informationalsArea = null;
    let bkgArea = null;

    function makeYesNoMessage(title, message, callback, timeout, type ) {

        if ( ! type ) type="info";
        const ynm = makeInformational(type, title, message, timeout);
        ynm.callback = callback;
        ynm.messageType = MESSAGE_TYPES.YES_NO;
        return ynm;
    }

    function makeInformational( type, title, message, timeout ) {
        const informational =  { title: title,
            type: type,
            message: message,
            messageType:MESSAGE_TYPES.INFORMATION
        };

        if ( timeout ) {
            const effTimeout = Number(timeout);
            if ( (! isNaN(effTimeout)) && effTimeout > 0 ) {
                informational.timeout = effTimeout;
            }
        }
        informational.show = function(){
            Informationals.show(informational);
        };

        return informational;
    }

    function showInformational(informational) {
        const dismissButton = UiUtils.makeButton( function(){},
            {classes:["btn-close"]}, []);
        dismissButton.dataset.bsDismiss="alert";

        const elements = [dismissButton];
        if ( informational.title ) {
            elements.push(UiUtils.makeElement("strong",{}, informational.title));
            elements.push(" ");
        }
        if ( informational.message ) {
            elements.push( informational.message );
        }

        const info = UiUtils.makeElement("div", {classes:["alert","alert-"+informational.type]}, elements);

        $informationalsArea.append( info );

        dismissButton.onclick = function(){dismiss(info);};

        if ( informational.timeout ) {
            window.setTimeout( function(){ dismiss(info);}, informational.timeout );
        }

        return info;
    }

    function showYesNo(ynMsg) {

        const yesButton = UiUtils.makeButton( function(){},
            {classes:["btn", "btn-primary"]},
            ["Yes"]
        );
        const noButton = UiUtils.makeButton( function(){},
            {classes:["btn", "btn-default"]},
            ["No"]
        );
        const btnContainer = UiUtils.makeDiv("btnContainer", [noButton, yesButton]);

        const elements = [btnContainer];
        if ( ynMsg.title ) {
            elements.push(UiUtils.makeElement("strong",{}, ynMsg.title));
            elements.push(" ");
        }
        if ( ynMsg.message ) {
            elements.push( ynMsg.message );
        }

        const info = UiUtils.makeElement("div", {classes:["alert","alert-"+ynMsg.type, ynMsg.messageType]}, elements);
        $informationalsArea.append( info );

        info.dismiss = function() {
            dismiss(info);
        };
        let callbackInProgress = false;
        noButton.onclick = function(){ callbackInProgress=true; ynMsg.callback(false, info); };
        yesButton.onclick = function(){ callbackInProgress=true;  ynMsg.callback(true, info); };

        if ( ynMsg.timeout ) {
            var timeoutCallback = function(){
                if ( ! callbackInProgress ) {
                    ynMsg.callback(undefined, info);
                }};
            window.setTimeout( timeoutCallback, ynMsg.timeout );
        }

        return info;
    }

    function makeBackgroundProcessMessage( title ) {
        const elements = [
            UiUtils.makeElement("i", {classes:["fa","fa-spin","fa-cog"]}, ""),
            UiUtils.makeElement("i", {classes:["fa","fa-check-circle-o","d-none"]}, ""),
            UiUtils.makeElement("p", {}, title)
        ];
        const info = UiUtils.makeElement("div", {classes:[MESSAGE_TYPES.BKG_PROCESS, "loading"]}, elements );
        info.dismiss = function(){ dismiss(info); };
        info.success = function(){
            info.dismiss = function(){}; // no-op, so client code can't double-dismiss this.
            $(this).addClass("done");
            $(this).find("i.fa-spin").remove();
            $(this).find("i.d-none").removeClass("d-none");
            window.setTimeout(function(){ dismiss(info);}, 1500);
        };
        info.update = function( value ) {
            const $p = $(this).find("p");
          $p.text(value);
          UiUtils.highlight($p);
        };
        return info;
    }

    let loaderModalTransitioning = false;
    let loaderModalShowing = false;
    function loader( toShow ) {
        if ( ! $loaderElement ) {
            initLoaderDialog();
        }

        if ( loaderModalTransitioning ) {
            // loader is currently animating, wait 500 ms
            window.setTimeout(function(){loader(toShow);},500);
            return;
        }

        if ( loaderModalShowing ) {
            // text update, really
            $loaderElementText.text(toShow);
            UiUtils.highlight($loaderElementText);
            return;

        } else {
            // show the blocking modal
            if ((typeof toShow !== 'undefined')) {
                $loaderElementText.text(toShow);
            } else {
                $loaderElementText.text("processing...");
            }
            loaderModalTransitioning = true;
            $loaderElement.modal( "show" ).on(
                "shown.bs.modal", function(){
                    loaderModalShowing=true;
                    loaderModalTransitioning=false;});
        }

    }

    loader.dismiss = function(){
        if ( loaderModalTransitioning ) {
            // loader is currently animating, wait 500 ms
            window.setTimeout(function(){loader.dismiss();},500);
            return;
        }
        if ( loaderModalShowing ) {
            loaderModalTransitioning = true;
            $loaderElement.modal( "hide" )
                .on("hidden.bs.modal", function(){
                    loaderModalShowing=false;
                    loaderModalTransitioning=false;});
        }
    };

    return {
        loader: loader,

        makeInfo: function( title, message, timeout ) {
            return makeInformational("info", title, message, timeout);
        },
        makeWarning: function( title, message, timeout ) {
            return makeInformational("warning", title, message, timeout);
        },
        makeSuccess: function( title, message, timeout ) {
            return makeInformational("success", title, message, timeout);
        },
        makeDanger: function( title, message, timeout ) {
            return makeInformational("danger", title, message, timeout);
        },
        make: makeInformational,
        makeYesNo: function( title, message, callback, timeout, type ) {
            return makeYesNoMessage(title, message, callback, timeout, type );
        },

        show: function( informational ) {
            if ( ! $informationalsArea ) {
                initInformationalsArea();
            }
            if ( informational.messageType === MESSAGE_TYPES.INFORMATION ) {
                showInformational(informational);
            } else if ( informational.messageType === MESSAGE_TYPES.YES_NO ) {
                showYesNo( informational );
            } else {
                console.log("ERROR: Invalid informational message type: " + informational.messageType );
            }
        },
        showBackgroundProcess: function( title ) {
            var ldrMsg = makeBackgroundProcessMessage(title);
            if ( !bkgArea ) {
                bkgArea = UiUtils.makeElement("div", {id:"BackgroundProcessMessageContainer"}, []);
                $("body").append(bkgArea);
            }
            $(bkgArea).append(ldrMsg);
            return ldrMsg;
        },
        messageTypes: {
            PRIMARY: "primary",
            SECONDARY: "secondary",
            SUCCESS: "success",
            DANGER: "danger",
            WARNING: "warning",
            INFO: "info",
            LIGHT: "light",
            DARK: "dark"
        }
    };
})();
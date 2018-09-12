/**
 * Little library for working with the PlayFramework router and
 * native JS Fetch api.
 */

var Playjax = function(router){

    var csrfTokenValue;

    /**
     * Lazy-loads the CSRF token, if exists. Replaces itself once called.
     * @return {boolean} true iff there's a CSRF token on the page
     */
    var scanForCsrfToken = function() {
        var csrfEmt = document.getElementById("Playjax_csrfTokenValue");
        if ( csrfEmt ) {
            csrfTokenValue = csrfEmt.innerText;
            scanForCsrfToken = function(){return true;};
        } else {
            scanForCsrfToken = function(){return false;};
        }

        return scanForCsrfToken();
    };

    function using( pathFn ){
        var route = pathFn(router.controllers);
        return {
            request: function( body ) { return routeToRequest(route, body);},
            fetch: function( body ) { return fetchRequest(route, body); }
        };
    }

    function routeToRequest(route, body) {
        var properties = {
            method: route.method,
            redirect: "follow",
            credentials: "same-origin",
            headers: new Headers()
        };

        if ( scanForCsrfToken() ) {
            properties.headers.append("Csrf-Token", csrfTokenValue);
        }
        if ( body ) {
            switch (typeof body) {
                case "string":
                    properties.body = body;
                    properties.headers.append("Content-Type", "text/plain");
                    break;
                case "object":
                case "number":
                case "boolean":
                    properties.body = JSON.stringify(body);
                    properties.headers.append("Content-Type", "application/json");
                    break;
                case "function":
                    throw "Cannot send function object over HTTP yet";

            }
        }

        return new Request(route.url, properties);
    }

    function fetchRequest(route, body){
        return fetch(routeToRequest(route, body));
    }

    return {
        request:routeToRequest,
        fetch:fetchRequest,
        using:using
    };

};
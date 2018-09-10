/**
 * Little library for working with the PlayFramework router and
 * native JS Fetch api.
 */

var Playax = function(router){

    function using( pathFn ){
        var route = pathFn(router.controllers);
        return {
            request: function( body ) { return routeToRequst(route, body);},
            fetch: function( body ) { return fetchRequest(route, body); }
        };
    }

    function routeToRequst(route, body) {
        var properties = {
            method: route.method,
            redirect: "follow",
            credentials: "same-origin"
        };
        var headers = new Headers();
        if ( body ) {
            switch (typeof body) {
                case "string":
                    properties.body = body;
                    headers.append("Content-Type", "text/plain");
                    break;
                case "object":
                case "number":
                case "boolean":
                    properties.body = JSON.stringify(body);
                    headers.append("Content-Type", "application/json");
                    break;
                case "function":
                    throw "Cannot send function object over HTTP yet";

            }
        }

        return new Request(route.url, properties);
    }

    function fetchRequest(route, body){
        return fetch(routeToRequst(route, body));
    }

    return {
        request:routeToRequst,
        fetch:fetchRequest,
        using:using
    };

};
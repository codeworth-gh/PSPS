/* jshint esversion:6 */
/**
 * Little library for working with the PlayFramework router and
 * native JS Fetch api.
 */

function Playjax(router){

    function using( pathFn ){
        const route = pathFn(router.controllers);
        return {
            /**
             * Return the request object
             * @param body optional body of request
             */
            request: function( body ) { return routeToRequest(route, body);},
            /**
             * Fetches the request
             * @param body optional request payload
             */
            fetch: function( body ) { return fetchRequest(route, body); }
        };
    }

    function routeToRequest(route, body) {
        const properties = {
            method: route.method,
            redirect: "follow",
            credentials: "same-origin",
            headers: new Headers()
        };

        if ( Playjax_csrfTokenValue ) {
            properties.headers.append("Csrf-Token", Playjax_csrfTokenValue);
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

}
/* jshint esversion:6 */
/**
 * Utility class for converting Javascript objects to HTML tables.
 */
const ObjectHTMLer = (function(){

    function objToTable( obj ) {
        const table = document.createElement("table");
        const tbody = document.createElement("tbody");
        table.appendChild(tbody);
        Object.keys(obj).forEach( function( k ) {
            const th = document.createElement("th");
            const td = toHtml(obj[k]);
            th.innerText = k;
            const tr = document.createElement("tr");
            tr.appendChild(th);
            tr.appendChild(td);
            tbody.appendChild(tr);
        });

        return table;
    }

    function arrayToUl( arr ){
        const retVal = document.createElement("ul");
        arr.forEach(function(emt){
            const li = document.createElement("li");
           li.appendChild( toHtml(emt) );
           retVal.appendChild(li);
        });
        return retVal;
    }

    function atomicValueToSpan( vlu ) {
        const retVal = document.createElement("span");
        retVal.innerText = String(vlu);
        return retVal;
    }

    function toHtml( obj ) {
        if ( typeof obj === "object" ) {
            return Array.isArray(obj) ? arrayToUl(obj) : objToTable(obj);
        } else {
            return atomicValueToSpan( obj );
        }
    }

    return {
        convert: toHtml
    };
})();
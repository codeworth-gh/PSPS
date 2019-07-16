ObjectHTMLer = (function(){

    function objToTable( obj ) {
        var table = document.createElement("table");
        var tbody = document.createElement("tbody");
        table.appendChild(tbody);
        Object.keys(obj).forEach( function( k ) {
            var th = document.createElement("th");
            var td = toHtml(obj[k]);
            th.innerText = k;
            var tr = document.createElement("tr");
            tr.appendChild(th);
            tr.appendChild(td);
            tbody.appendChild(tr);
        });

        return table;
    }

    function arrayToUl( arr ){
        var retVal = document.createElement("ul");
        arr.forEach(function(emt){
           var li = document.createElement("li");
           li.appendChild( toHtml(emt) );
           retVal.appendChild(li);
        });
        return retVal;
    }

    function atomicValueToSpan( vlu ) {
        var retVal = document.createElement("span");
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
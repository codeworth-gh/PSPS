/**
 * Utility stuff for UI in Javascript.
 *
 * Depends on JQuery!
 *
 * @Author: Michael Bar-Sinai
 */

var UiUtils = (function () {

    /**
     * Gets the default height of an element without displaying it.
     * @param emt
     * @returns {*} height (in pixels)
     */
    function defaultHeight( emt ) {
        var clone = emt.clone();
        clone.css('height', 'auto');
        clone.css('visibility', 'hidden');

        emt.parent().append(clone);
        var height = clone.innerHeight();
        clone.remove();
        return height;
    }

    var makeElement = function (type, options, content) {
        var emt = document.createElement(type);
        var $emt = $(emt);
        if (typeof options === 'string') {
            options = {
                classes: [options]
            };
        }
        if (options) {
            if (options.classes) {
                if (typeof options.classes === 'string') {
                    $emt.addClass(options.classes);
                } else {
                    for (var cls in options.classes) {
                        $emt.addClass(options.classes[cls]);
                    }
                }
            }
            if ( options.data  ) { $emt.data(options.data); }
            if ( options.value ) { $emt.val(options.value); }
            if ( options.id    ) { emt.id = options.id; console.log("setting id to " + options.id);}
            if ( options.name  ) { emt.name=options.name; }
            if ( options.src   ) { emt.src=options.src; }
            if ( options.selected    ) { emt.selected=true; }
            if ( options.checked     ) { emt.checked=true; }
            if ( options.placeholder ) { emt.placeholder=options.placeholder; }
        }

        if (typeof content !== 'undefined') {
            if ( !$.isArray(content) ) {
                content = [content];
            }
            $(content).each( function(i,sub) {
                if (typeof sub === 'string') {
                    if ( sub.indexOf("<") >= 0 ) {
                        $emt.html(sub);
                    } else {
                        $emt.append(document.createTextNode(sub));
                    }
                } else {
                    $emt.append(sub);
                }
            });
        }
        return emt;
    };

    var makeA = function (href, options, content) {
        var emt = makeElement("a", options, content);
        emt.href = href;
        if ( options.target ) {
            emt.target = options.target;
        }
        return emt;
    };

    var makeLi = function (options, content) {
        return makeElement("li", options, content);
    };

    var makeButton = function (onClick, options, content) {
        var btn = makeElement("button", options, content);
        btn.onclick = onClick;
        return btn;
    };

    var makeImg = function (src, option, content) {
        var img = makeElement("img", option, content);
        img.src = src;
        return img;
    };

    var makeMakeElement = function(elementType) {
        return function(options, content){
            return makeElement(elementType, options, content);
        };
    };

    var makeTr = function( options, elementArr ) {
      var $tr = $(makeElement("tr", options));
      $(elementArr).each( function(i,sub) {
          if (typeof sub === 'string') {
              $tr.append("<td>" + sub + "</td>");
          } else {
              $tr.append(sub);
          }
      });
      return $tr;
    };

    var fa = function (iconName) {
        return makeElement("i", {
            classes: ["fa", "fa-" + iconName]
        });
    };

    var editButton = function(cb) {
        return makeButton(cb,
            {classes:["btn","btn-sm","btn-outline-warning"]},
            [ fa("edit"), ""]
        );
    };
    var removeButton = function(cb) {
        return makeButton(cb,
            {classes:["btn","btn-sm","btn-outline-danger"]},
            [ fa("trash"), ""]
        );
    };

    // Properties used in "highlight"
    var HIGHLIGHT_PROPERTIES = {
        "error":   {color: "#FFAAAA", count:5, duration:1000},
        "warning": {color: "#ffcb59", count:2, duration:1000},
        "success": {color: "#88FF88", count:1, duration:4000},
        "defaults": {color:"#FFFFAA", count:1, duration:2000}
    };

    var highlight = function( emt, reason ) {
        if ( ! reason ) {
            reason = "defaults";
        }
        var properties = HIGHLIGHT_PROPERTIES[reason];
        if ( ! properties ) {
            console.log("Warning: reason '" + reason + "' unknown for highlight.");
            properties = HIGHLIGHT_PROPERTIES.defaults;
        }
        var mkHighlight = function(emt, times) {
            return function(){
                if ( times > 0 ) {
                    $(emt).effect("highlight", {color:properties.color}, properties.duration, mkHighlight(emt, times-1));
                }
            };
        };
        mkHighlight(emt, properties.count)();
    };

    return {
        makeElement: makeElement,
        makeA: makeA,
        makeLi: makeMakeElement("li"),
        makeButton: makeButton,
        makeTd: makeMakeElement("td"),
        makeTr: makeTr,
        makeImg: makeImg,
        makeDiv: function(options, content) {
            if ( content ) {
                return makeElement("div", options, content) ;
            } else {
                return makeElement("div", {}, options );
            }
        },
        fa: fa,
        buttons: {
            edit:editButton,
            remove: removeButton
        },
        defaultHeight:defaultHeight,
        highlight:highlight
    };
}());

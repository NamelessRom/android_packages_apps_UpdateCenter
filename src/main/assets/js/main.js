$(document).ready(function () {

    var DEBUG = true;

    $(this).delegate("li.toggleable", "click", function (e) {
        var parent = $(this);
        var child = "li." + $(this).attr('id').replace('main', 'content');
        logDebug(child);
        var isHidden = contains($(child).attr('class'), 'hidden');
        logDebug('isHidden: ' + isHidden);

        if (isHidden) {
            $(parent).addClass("dropup");
            $(child).removeClass("hidden");
            $(child).fadeIn(250, function () {
            });
        } else {
            $(child).fadeOut(250, function () {
                if (!isHidden) {
                    $(parent).removeClass("dropup");
                    $(child).addClass("hidden");
                }
            });
        }

        return false;
    });

    function logDebug(msg) {
        if (DEBUG) console.log(msg);
    }

    function contains(str, search) {
        return (str.indexOf(search) >= 0);
    }

})
;

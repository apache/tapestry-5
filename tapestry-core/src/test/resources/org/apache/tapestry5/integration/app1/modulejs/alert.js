/** Simple module: no dependencies, exports an object directory (no hygenic function wrapper). */
define({
    alert: function (message) { window.alert("app/alert: " + message); }
});
define(["require", "exports"], function (require, exports) {
    "use strict";
    var Keywords = /** @class */ (function () {
        function Keywords() {
        }
        Keywords.prototype.delete = function () {
            // delete is a reserved keyword, but should be fine
        };
        Keywords.prototype.return = function () {
            // another keyword
        };
        return Keywords;
    }());
    var k = new Keywords();
    k.delete();
    k.return();
});

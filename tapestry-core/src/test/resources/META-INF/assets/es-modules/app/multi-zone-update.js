import dom from "t5/core/dom";

export default function (id, message) {
	dom(id).update(message);
};
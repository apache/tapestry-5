import dom from "t5/core/dom";
import ajax from "t5/core/ajax";
import jQuery from "jquery";
	
function makeAjaxCall(eventName, eventElement, outputElement) {
	ajax(eventName, { 
		element: eventElement,
		success: function(response) {
			outputElement.innerHTML = response.json.origin;
		} 
	});
}

jQuery('tbody tr').each(function() {
	var td = jQuery(this).find('td');
	var eventName = td[1].innerHTML;
	var elementId = td[0].innerHTML;
	var eventElement = null;
	if (elementId != '(no element)') {
		eventElement = jQuery('#' + elementId);
	}
	makeAjaxCall(eventName, eventElement, td[3]);
});

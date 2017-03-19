require(["t5/core/dom", "t5/core/ajax", "jquery"], function (dom, ajax, $) {

	function makeAjaxCall(eventName, eventElement, outputElement) {
		ajax(eventName, { 
			element: eventElement,
			success: function(response) {
				outputElement.innerHTML = response.json.origin;
			} 
		});
	}
	
	$('tbody tr').each(function() {
		var td = $(this).find('td');
		var eventName = td[1].innerHTML;
		var elementId = td[0].innerHTML;
		var eventElement = null;
		if (elementId != '(no element)') {
			eventElement = $('#' + elementId);
		}
		makeAjaxCall(eventName, eventElement, td[3]);
	});

});


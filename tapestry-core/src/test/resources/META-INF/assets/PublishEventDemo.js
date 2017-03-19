require(["t5/core/dom", "t5/core/ajax", "jquery"], function (dom, ajax, $) {

    $(document).ready(function() {
        console.log('dom.getEventURL()   : ' + dom.getEventUrl('answer', document.getElementById("page")));
        console.log('dom.getEventURL() 1 : ' + dom.getEventUrl('answer', document.getElementById("componentParagraph")));
        console.log('dom.getEventURL() 2 : ' + dom.getEventUrl('answer', document.getElementById("componentParagraph2")));
        console.log('dom.getEventURL() 3 : ' + dom.getEventUrl('answer', document.getElementById("componentParagraph3")));
    });
    
});


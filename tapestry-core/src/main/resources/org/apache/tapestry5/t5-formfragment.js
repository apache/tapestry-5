T5.extendInitializers(function() {

    function init(spec) {

        var element = $(spec.element);

        var hidden = $(spec.element + "-hidden");
        var form = $(hidden.form);

        var opts = (spec.bound && { bound: spec.bound }) || {};
        if (! spec.alwaysSubmit) {
            hidden.disabled = !  element.isDeepVisible(opts);
        }

        function updateUI(makeVisible) {

            if (! spec.alwaysSubmit) {
                hidden.disabled = ! (makeVisible && element.parentNode.isDeepVisible(opts));
            }

            var effect = makeVisible ? Tapestry.ElementEffect[spec.show]
                || Tapestry.ElementEffect.slidedown
                : Tapestry.ElementEffect[spec.hide]
                || Tapestry.ElementEffect.slideup;
            return effect(element);
        }

        element.observe(Tapestry.CHANGE_VISIBILITY_EVENT, function(event) {
            // Since events propagate up, you have to call event.stop()
            // here to prevent hiding/revealing any container FormFragment elements.
            event.stop();

            var makeVisible = event.memo.visible;

            if (makeVisible == element.visible())
                return;

            updateUI(makeVisible);
        });

        element.observe(Tapestry.HIDE_AND_REMOVE_EVENT,
            function(event) {
                event.stop();
                var effect = updateUI(false);

                effect.options.afterFinish = function() {
                    Tapestry.remove(element);
                };
            });
    }

    /**
     * Links a FormFragment to a trigger (a radio or a checkbox), such
     * that changing the trigger will hide or show the FormFragment.
     * Care should be taken to render the page with the checkbox and the
     * FormFragment's visibility in agreement.
     */
    function linker(spec) {
        var trigger = $(spec.triggerId);

        function update() {
            var checked = trigger.checked;
            var makeVisible = checked == !spec.invert;

            $(spec.fragmentId).fire(Tapestry.CHANGE_VISIBILITY_EVENT, {
                visible : makeVisible
            }, true);
        }

        // Let the event bubble up to the form level.
        if (trigger.type == "radio") {
            $(trigger.form).observe("click", update);
            return;
        }

        // Normal trigger is a checkbox; listen just to it.
        trigger.observe("click", update);
    }

    return {
        formFragment : init,
        linkTriggerToFormFragment : linker
    };
});

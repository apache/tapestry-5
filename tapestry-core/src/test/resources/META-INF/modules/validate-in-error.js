define(['t5/core/dom', 't5/core/events'], function(dom, events){
  dom.default.onDocument(events.default.form.validateInError, 'form', function() {
    const attributes =
      {id: 'validate-in-error'};
    this.prepend(dom.default.create('div', attributes, 'Validate in error'));
  });
});

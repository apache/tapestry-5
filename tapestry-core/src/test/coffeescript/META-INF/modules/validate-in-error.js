define(['t5/core/dom', 't5/core/events'], function(dom, events){
  dom.onDocument(events.form.validateInError, 'form', function() {
    const attributes =
      {id: 'validate-in-error'};
    this.prepend(dom.create('div', attributes, 'Validate in error'));
  });
});

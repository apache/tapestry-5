import dom from 't5/core/dom';
import events from 't5/core/events';

dom.onDocument(events.form.validateInError, 'form', function() {
  const attributes =
    {id: 'validate-in-error'};
  this.prepend(dom.create('div', attributes, 'Validate in error'));
});

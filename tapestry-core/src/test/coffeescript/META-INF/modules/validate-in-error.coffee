define ['t5/core/dom', 't5/core/events'], (dom, events)->
  dom.onDocument events.form.validateInError, 'form', ->
    attributes =
      id: 'validate-in-error'
    @prepend dom.create 'div', attributes, 'Validate in error'
    return
  return

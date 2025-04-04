# ## t5/core/confirm-click
#
# Support for the Tapestry Confirm mixin, and for running confirmation dialogs programmatically.

define ["jquery", "t5/core/events", "bootstrap/modal"],

  ($, events) ->

    # Runs a modal dialog, invoking a callback if the user selects the OK option. On any form of cancel,
    # there is no callback.
    #
    # options.title - default "Confirm"
    # options.message - required
    # options.okClass - default "btn-warning"
    # options.okLabel - default "OK"
    # options.cancelLabel - default "Cancel"
    # options.ok - callback function, required
    runDialog = (options) ->

      confirmed = false

      content = """
                <div class="modal fade" role="dialog" tabindex='-1'>
                  <div class="modal-dialog">
                    <div class="modal-content">
                      <div class="modal-header">
                        <a class="close" data-dismiss="modal">&times;</a>
                        <h3>#{options.title or "Confirm"}</h3>
                      </div>
                      <div class="modal-body">#{options.message}</div>
                      <div class="modal-footer">
                        <button class="btn #{options.okClass or "btn-warning"}" data-dismiss="modal">#{options.okLabel or "OK"}</button>
                        <button class="btn btn-default" data-dismiss="modal">#{options.cancelLabel or "Cancel"}</button>
                      </div>
                    </div>
                  </div>
                </div>
                """

      $dialog = $ content

      $dialog.on "click", ".modal-footer button:first", ->
        confirmed = true
        return

      # Let the animation run before (perhaps) invoking the callback.
      $dialog.modal().on "hidden.bs.modal", ->
        $dialog.remove()
        if confirmed
          options.ok()

      $dialog.appendTo $ "body"

      # Focus on the first button (the "OK") button.
      $dialog.on "shown.bs.modal", ->
        $dialog.find(".modal-footer .btn").first().focus()

    # Support for the Confirm mixin
    $("body").on "click", "[data-confirm-message]:not(.disabled)", (event)->

      $this = $(this)

      # We use a data- attribute as a flag, to indicate that the user confirmed the behavior.

      if ($this.attr "data-confirm-state") is "confirmed"
        $this.attr "data-confirm-state", null
        return # allow default behavior to continue

      runDialog
        title: $this.attr "data-confirm-title"
        message: $this.attr "data-confirm-message"
        okClass: $this.attr "data-confirm-class-ok"
        okLabel: $this.attr "data-confirm-label-ok"
        cancelLabel: $this.attr "data-confirm-label-cancel"
        ok: ->
          $this.attr "data-confirm-state", "confirmed"
          # In the case of an Ajax update, or a button, this is enough. In the case of a simple link,
          # the default behavior when triggering click() is to do nothing, and our document event handler
          # (just below) picks up the slack.
          $this.click()

      # Cancel the original click event
      return false

    ($ document).on "click", "a[data-confirm-message]:not(.disabled, [data-update-zone], [data-async-trigger])", (event) ->

      target = $ event.target

      # See note above; this replicates the default behavior of a link element that is lost because
      # of the
      window.location.href = target.attr "href"
      return false

    # Exports:

    { runDialog }

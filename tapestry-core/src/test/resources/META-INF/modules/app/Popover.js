require(['bootstrap/popover'], () => {
	// Popovers are opt-in for performance reasons, so you must initialize them yourself.
	// https://getbootstrap.com/docs/4.3/components/popovers/#example-enable-popovers-everywhere
    $('[data-toggle="popover"]').popover()
})
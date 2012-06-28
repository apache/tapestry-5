# Compatibility module
# Invokes functions on the T5.initializers namespace.
# Introduced in 5.4, to be removed at some point in the future, when T5.initializers is itself no more.

define ->
  (initName, args...) ->
    fn = T5.initializers[initName]
    if not fn
      T5.console.error "Initialization function '#{initName}' not found in T5.initializers namespace."
    else
      fn.apply null, args

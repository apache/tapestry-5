Scratch pad for changes destined for the 5.8.3 release notes page.

# Added methods

* add(URL url, String memo) to URLChangeTracker
* getChangeResourcesMemos() to URLChangeTracker
* getValues() to MultiKey
* New getLogicalName() method in ComponentClassResolver.
* New getPlasticManager() method in PlasticProxyFactory

# Non-backward-compatible changes (but that probably won't cause problems)

* New addInvalidationCallback(Function<List<String>, List<String>> callback) method in InvalidationEventHub
* New getEmbeddedElementIds() method in ComponentPageElement (internal service)
* New registerClassName() method in ResourceChangeTracker (internal service)
* New clearClassName() method in ResourceChangeTracker (internal service)

# Overall notes
* Before 
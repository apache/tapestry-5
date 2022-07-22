package org.apache.tapestry5.ioc.test;

public class NonProxiedEagerLoadService {
    public NonProxiedEagerLoadService(EagerLoadService2 service2) {
        EagerProxyReloadModule.nonProxyEagerLoadServiceDidLoad = true;
    }
}

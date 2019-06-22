package org.apache.tapestry5.ioc;

public interface DefaultMethodService {

    public default String overriden() {
        return "Default";
    }

    public default String notOverriden() {
        return "Default";
    }

}

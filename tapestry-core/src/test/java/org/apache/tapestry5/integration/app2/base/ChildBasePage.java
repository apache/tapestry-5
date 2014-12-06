package org.apache.tapestry5.integration.app2.base;

public abstract class ChildBasePage extends ParentBasePage {
    @Override
    public String getObject() {
        return "foobar";
    }
}

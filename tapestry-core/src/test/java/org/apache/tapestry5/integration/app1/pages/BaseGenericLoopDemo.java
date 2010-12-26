package org.apache.tapestry5.integration.app1.pages;

import org.apache.tapestry5.annotations.BeginRender;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.corelib.components.Loop;

import java.util.List;

public abstract class BaseGenericLoopDemo<T> {

    @Property
    @Component
    private Loop<T> inheritedLoop;

    @Property
    private List<T> inheritedLoopSource;

    @BeginRender
    void setupLoop() {
        inheritedLoopSource = initInheritedLoop();        
    }

    abstract List<T> initInheritedLoop();
}

package org.apache.tapestry5.ioc.services;

import org.apache.tapestry5.ioc.annotations.Operation;

public interface OperationTrackedService
{
    @Operation("First operation")
    void first();

    @Operation("Second operation: %s")
    void second(String name);

    void nonOperation();
}

package org.apache.tapestry5.integration.app1.pages;

import org.apache.tapestry5.ioc.annotations.Operation;

public class OperationWorkerDemo
{
    @Operation("[Operation Description]")
    void onActionFromThrowException()
    {
        throw new RuntimeException("An exception inside an operation tracked method.");
    }
}

package org.apache.tapestry5.ioc.services;

import org.apache.tapestry5.ioc.MethodAdviceReceiver;
import org.apache.tapestry5.ioc.annotations.Advise;
import org.apache.tapestry5.ioc.annotations.Match;
import org.apache.tapestry5.ioc.annotations.Order;

public class OperationTrackedModule
{
    public OperationTrackedService buildTestSubject(DefaultImplementationBuilder builder)
    {
        return builder.createDefaultImplementation(OperationTrackedService.class);
    }

    @Advise @Match("*")
    @Order("before:*")
    public void addOperationTracking(MethodAdviceReceiver receiver, OperationAdvisor advisor)
    {
        advisor.addOperationAdvice(receiver);
    }


}

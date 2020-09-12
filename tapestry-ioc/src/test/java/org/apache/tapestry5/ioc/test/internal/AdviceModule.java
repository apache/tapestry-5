// Copyright 2013 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package org.apache.tapestry5.ioc.test.internal;


import org.apache.tapestry5.commons.ObjectLocator;
import org.apache.tapestry5.ioc.MethodAdviceReceiver;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.Advise;
import org.hibernate.Session;
import org.hibernate.cfg.Configuration;

public class AdviceModule
{

    public static void bind(ServiceBinder binder)
    {
        binder.bind(NonAnnotatedServiceInterface.class, NonAnnotatedServiceInterfaceImpl.class);
        binder.bind(AnnotatedServiceInterface.class, AnnotatedServiceInterfaceImpl.class);
        binder.bind(NonAnnotatedGenericSetServiceInterface.class,
                NonAnnotatedGenericSetServiceImpl.class);
    }

    @Advise(serviceInterface = NonAnnotatedServiceInterface.class)
    public static void adviseNonAnnotatedServiceInterface(MethodAdviceReceiver methodAdviceReceiver)
    {
        methodAdviceReceiver.adviseAllMethods(new TestAdvice());
    }

    @Advise(serviceInterface = AnnotatedServiceInterface.class)
    public static void adviseAnnotatedServiceInterface(MethodAdviceReceiver methodAdviceReceiver)
    {
        methodAdviceReceiver.adviseAllMethods(new TestAdvice());
    }

    @Advise(serviceInterface = NonAnnotatedGenericSetServiceInterface.class)
    public static void adviseNonAnnotatedGenericSetServiceInterface(
            final MethodAdviceReceiver methodAdviceReceiver) {
        methodAdviceReceiver.adviseAllMethods(new TestAdvice());
    }
    
}

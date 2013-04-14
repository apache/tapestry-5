//
// Copyright 2010 GOT5 (Gang Of Tapestry 5)
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// 	http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

package org.apache.tapestry5.beanvalidator;

import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.FormFragment;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.plastic.MethodAdvice;
import org.apache.tapestry5.plastic.MethodInvocation;
import org.apache.tapestry5.plastic.PlasticClass;
import org.apache.tapestry5.plastic.PlasticMethod;
import org.apache.tapestry5.services.TransformConstants;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;
import org.apache.tapestry5.services.transform.ComponentClassTransformWorker2;
import org.apache.tapestry5.services.transform.TransformationSupport;

/**
 * Respectfully borrowed from the tapestry5-jquery project
 * https://github.com/got5/tapestry5-jquery/blob/master/src/main/java/org/got5/tapestry5/jquery/services/FormResourcesInclusionWorker.java
 */
public class FormResourcesInclusionWorker implements ComponentClassTransformWorker2
{
    private final JavaScriptSupport javaScriptSupport;

    public FormResourcesInclusionWorker(JavaScriptSupport javaScriptSupport)
    {
        this.javaScriptSupport = javaScriptSupport;
    }

    public void transform(PlasticClass plasticClass, TransformationSupport support, MutableComponentModel model)
    {
        if (model.getComponentClassName().equals(Form.class.getName()) ||
                model.getComponentClassName().equals(FormFragment.class.getName()))
        {
            PlasticMethod setupRender = plasticClass.introduceMethod(TransformConstants.SETUP_RENDER_DESCRIPTION);

            setupRender.addAdvice(new MethodAdvice()
            {
                public void advise(MethodInvocation invocation)
                {
                    javaScriptSupport.importStack(BeanValidatorStack.STACK_ID);

                    invocation.proceed();
                }
            });

            model.addRenderPhase(SetupRender.class);
        }
    }
}
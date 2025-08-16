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

package org.apache.tapestry5.integration.app1.components;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.annotations.BeginRender;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.internal.services.ajax.RequireJsModeHelper;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.pageload.PageClassLoaderContextManager;

import java.util.Calendar;

/**
 * Here's a component with a template, including a t:body element. Really should rename this to "Layout" as that's the
 * T5 naming.
 */
@Import(stylesheet = "context:css/app.css")
public class Border
{
    @Inject
    @Property
    private Request request;

    @Inject
    private ComponentResources resources;
    
    @Inject @Property
    private PageClassLoaderContextManager pccm; 
    
    @Inject
    private RequireJsModeHelper requireJsModeHelper;

    public static final int year;

    static
    {
        year = Calendar.getInstance().get(Calendar.YEAR);
    }

    public String getSecure()
    {
        return request.isSecure() ? "secure" : "insecure";
    }

    boolean onActionFromReset()
    {
        resources.discardPersistentFieldChanges();

        return true;
    }
    
    public String getJvm() 
    {
        String version = System.getProperty("java.vendor.version");
        if (version == null)
        {
            version = System.getProperty("java.vm.version");
        }
        return version + " from " + System.getProperty("java.vendor");
    }
    
    @BeginRender
    void beginRender()
    {
        requireJsModeHelper.importModule("bootstrap/collapse");
        requireJsModeHelper.importModule("app/test-support");
    }
    
}

// Copyright 2009-2013 The Apache Software Foundation
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

package org.apache.tapestry5.integration.app1.pages;

import java.util.Date;

import org.apache.tapestry5.Block;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.corelib.components.BeanDisplay;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PartialTemplateRenderer;

public class PartialTemplateRendererDemo
{
    
    @Inject
    @Property
    private Block someBlock;
    
    @Inject
    private PartialTemplateRenderer partialTemplateRenderer;
    
    @InjectComponent
    private BeanDisplay beanDisplay;
    
    @Property
    private Pojo object;
    
    void setupRender() {
        object = new Pojo();
        object.setDate(new Date(21342345234444L));
        object.setString(String.valueOf(System.currentTimeMillis()));
        object.setInteger((int) (System.currentTimeMillis() % 234123));
    }
    
    public String getServiceRenderedBlock() {
        return partialTemplateRenderer.render(someBlock);
    }

    public String getServiceRenderedComponent() {
        return partialTemplateRenderer.render(beanDisplay);
    }

    public static class Pojo
    {
        private int integer;
        private String string;
        private Date date;

        public int getInteger()
        {
            return integer;
        }

        public void setInteger(int i)
        {
            this.integer = i;
        }

        public String getString()
        {
            return string;
        }

        public void setString(String s)
        {
            this.string = s;
        }

        public Date getDate()
        {
            return date;
        }

        public void setDate(Date date)
        {
            this.date = date;
        }

    }
}

// Copyright 2013 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package org.apache.tapestry5.cdi;

import javax.enterprise.context.spi.CreationalContext;

/**
 * A CDI bean wrapper that embeds a {@link javax.enterprise.context.spi.CreationalContext CreationalContext}
 *
 */
public class BeanInstance {
    private final Object bean;
    private boolean releasable;
    private final CreationalContext<?> context;

    public BeanInstance(final Object bean, final CreationalContext<?> context, boolean releasable) {
        this.bean = bean;
        this.context = context;
        this.releasable = releasable;
    }

    public boolean isResolved() {
        return bean != null;
    }

    public Object getBean() {
        return bean;
    }

    public void release() {
        if (isReleasable()) {
            context.release();
        }
    }

    public boolean isReleasable() {
        return releasable;
    }
}

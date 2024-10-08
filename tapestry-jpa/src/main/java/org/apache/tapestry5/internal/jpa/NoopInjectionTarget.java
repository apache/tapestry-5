/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tapestry5.internal.jpa;

import java.util.Set;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.InjectionTarget;

public class NoopInjectionTarget<T> implements InjectionTarget<T>
{

    @Override
    public T produce(CreationalContext<T> ctx)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void dispose(T instance)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public Set<InjectionPoint> getInjectionPoints()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void inject(T instance, CreationalContext<T> ctx)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void postConstruct(T instance)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void preDestroy(T instance)
    {
        // TODO Auto-generated method stub

    }

}

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
package org.apache.tapestry5.jpa;

import org.apache.tapestry5.ioc.Invokable;

public interface EntityTransactionManager
{

    void runInTransaction(String unitName, Runnable runnable);

    <T> T invokeInTransaction(String unitName, Invokable<T> invokable);

    void invokeBeforeCommit(String unitName, Invokable<Boolean> invokable);

    void invokeAfterCommit(String unitName, Invokable<Boolean> invokable);

    @SuppressWarnings("rawtypes")
    public static class VoidInvokable implements Invokable
    {
        private final Runnable runnable;

        public VoidInvokable(Runnable runnable)
        {
            this.runnable = runnable;
        }

        @Override
        public Object invoke()
        {
            runnable.run();
            return null;
        }

    }
}

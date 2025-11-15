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

package org.apache.tapestry5.services.javascript;

import org.apache.tapestry5.commons.OrderedConfiguration;

/**
 * Encapsulates a contribution to {@linkplain EsModuleManager}.
 * Contributions can be of 2 types: base, one that contributes a callback 
 * used when creating the base import map to be used for all requests;
 * and global per-request, one that contributes a callback used 
 * in all requests after the callbacks added through 
 * {@linkplain JavaScriptSupport#addEsModuleConfigurationCallback(EsModuleConfigurationCallback)} 
 * were called).
 *
 * @since 5.10.0
 * @see EsModuleManager
 * @see EsModuleConfigurationCallback
 */
public final class EsModuleManagerContribution
{
    private final EsModuleConfigurationCallback callback;
    
    private final boolean isBase;
    
    private EsModuleManagerContribution(EsModuleConfigurationCallback callback, boolean isBase) 
    {
        super();
        this.callback = callback;
        this.isBase = isBase;
    }
    
    public EsModuleConfigurationCallback getCallback() 
    {
        return callback;
    }
    
    public boolean isBase() 
    {
        return isBase;
    }

    /**
     * Creates a base contribution given a callback.
     * @param callback an {@linkplain EsModuleConfigurationCallback} instance.
     * @return a corresponding {@linkplain EsModuleManagerContribution}.
     */
    public static EsModuleManagerContribution base(EsModuleConfigurationCallback callback)
    {
        return new EsModuleManagerContribution(callback, true);
    }
    
    /**
     * Creates a base contribution which sets or overrides 
     * one module and its URL.
     * @param id the module id.
     * @param url the module URL.
     * @see EsModuleConfigurationCallback#create(String, String).
     */
    public static EsModuleManagerContribution base(String id, String url)
    {
        return new EsModuleManagerContribution(
                EsModuleConfigurationCallback.create(id, url), true);
    }

    /**
     * Creates a global per-request contribution given a callback.
     * @param callback an {@linkplain EsModuleConfigurationCallback} instance.
     * @return a corresponding {@linkplain EsModuleManagerContribution}.
     */
    public static EsModuleManagerContribution globalPerRequest(EsModuleConfigurationCallback callback)
    {
        return new EsModuleManagerContribution(callback, false);
    }
    
    /**
     * Contributes a base contribution which sets or overrides 
     * one module and its URL.
     * @param configuration an {@code OrderedConfiguration<EsModuleManagerContribution>}.
     * @param id the module id.
     * @param url the module URL.
     * @see EsModuleConfigurationCallback#create(String, String)
     */
    public static void base(
            OrderedConfiguration<EsModuleManagerContribution> configuration,
            String id, 
            String url)
    {
        configuration.add(id, base(id, url));
    }
    
    /**
     * Creates a global per-request contribution which sets or overrides 
     * one module and its URL.
     * @param id the module id.
     * @param url the module URL.
     * @see EsModuleConfigurationCallback#create(String, String).
     */
    public static EsModuleManagerContribution globalPerRequest(String id, String url)
    {
        return new EsModuleManagerContribution(
                EsModuleConfigurationCallback.create(id, url), false);
    }
    
    /**
     * Contributes a global per-thread contribution which sets or overrides 
     * one module and its URL.
     * @param configuration an {@code OrderedConfiguration<EsModuleManagerContribution>}.
     * @param id the module id.
     * @param url the module URL.
     * @see EsModuleConfigurationCallback#create(String, String)
     */
    public static void globalPerRequest(
            OrderedConfiguration<EsModuleManagerContribution> configuration,
            String id, 
            String url)
    {
        configuration.add(id, globalPerRequest(id, url));
    }
    
}
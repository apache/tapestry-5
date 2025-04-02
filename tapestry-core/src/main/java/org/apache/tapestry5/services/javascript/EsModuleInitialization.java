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

import java.util.Map;

/**
 * Provided by {@link JavaScriptSupport#importEsModule(String)} to allow additional, optional, 
 * details of the ES module import.
 *
 * @since 5.10.0
 */
public interface EsModuleInitialization extends AbstractInitialization<EsModuleInitialization>
{

    /**
     * Defines an attribute name and value to be added to the corresponding
     * <code>&lt;script&gt;</code> element. If the attribute was already set,
     * its value will be overwritten.
     * 
     * @param name The attribute name. It cannot be null nor empty.
     * @param value The attribute value. It cannot be null nor empty.
     * @return this <code>EsModuleInitialization</code> for further configuration.
     */
    EsModuleInitialization withAttribute(String name, String value);
    
    /**
     * Same as <code>withAttribute(name, name)</code>. Useful for attributes
     * without values, such as <code>defer</code> and <code>async</code>.
     * 
     * @param name The attribute name. It cannot be null nor empty.
     * @return this <code>EsModuleInitialization</code> for further configuration.
     */
    default EsModuleInitialization withAttribute(String name)
    {
        return withAttribute(name, name);
    }
    
    /**
     * Utility method for adding the <code>defer</code> attribute.
     * @return this <code>EsModuleInitialization</code> for further configuration.
     */
    default EsModuleInitialization withDefer()
    {
        return withAttribute("defer");
    }
    
    /**
     * Utility method for adding the <code>async</code> attribute.
     * @return this <code>EsModuleInitialization</code> for further configuration.
     */
    default EsModuleInitialization withAsync()
    {
        return withAttribute("async");
    }
    
    /**
     * Defines where this import should be done.
     * @param placement an {@linkplain ImportPlacement} instance. It cannot be null.
     * @return this <code>EsModuleInitialization</code> for further configuration.
     */
    EsModuleInitialization placement(ImportPlacement placement);
    
    /**
     * Specifies the name of an module exported function to invoke. 
     * If this method is not invoked, then the module is expected to export
     * just a single function (which may, or may not, take {@linkplain #with(Object...) parameters}).
     *
     * @param functionName
     *         name of a function exported by the module.
     * @return this <code>EsModuleInitialization</code>, for further configuration.
     */
    EsModuleInitialization invoke(String functionName);
    
    /**
     * Specifies the arguments to be passed to the function. Often, just a single {@link org.apache.tapestry5.json.JSONObject}
     * is passed. 
     *
     * @param arguments
     *         any number of values. Each value may be one of: null, String, Boolean, Number,
     *         {@link org.apache.tapestry5.json.JSONObject}, {@link org.apache.tapestry5.json.JSONArray}, or
     *         {@link org.apache.tapestry5.json.JSONLiteral}.
     */
    void with(Object... arguments);
    
}

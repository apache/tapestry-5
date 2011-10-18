/* Copyright 2011 The Apache Software Foundation
 *
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

/**
 * The master T5 namespace. A few critical functions are added directly to T5,
 * but mostly it is used as a containing namespace for namespaces defined by
 * other modules.
 *
 * @since 5.3
 */
var T5 = {

    /** _ is _.noConflict(), in other words, all Underscore functions are not inside
     * T5._, rather than simply _.
     */
    _ : _.noConflict()
};

/**
 * Extends an object using a source. In the simple case, the source object's
 * properties are overlaid on top of the destination object. In the typical
 * case, the source parameter is a function that returns the source object
 * ... this is to facilitate modularity and encapsulation.
 *
 * @param destination
 *            object to receive new or updated properties
 * @param source
 *            source object for properties, or function returning source
 *            object
 * @returns the destination object
 */
T5.extend = function(destination, source) {
    var _ = T5._;

    if (_.isFunction(source)) {
        source = source();
    }

    return _.extend(destination, source);
};

/**
 * Defines a new namespace under the T5 object.
 *
 * @param name
 *            string name of the namespace
 * @param source
 *            source object for properties (or function returning source
 *            object)
 * @return the namespace object
 */
T5.define = function(name, source) {
    var namespace = {};
    T5[name] = namespace;

    return this.extend(namespace, source);
};

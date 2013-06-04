// Copyright 2012 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http:#www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// Provides a small amount of backwards compatibility to the Tapestry 5.3 approach.
// This provides placeholders for the following:
//
// * `T5` namespace, including `extend`, `define`, and `initializers`, `extendInitializers`, and `_` properties
//
// * `Tapestry` namespace: just the `Initializer` property, as an alias of `T5.initializers`
require(["underscore"], function(_) {
    var T5, Tapestry;
    T5 = {
        _: _,
        extend: function(destination, source) {
            if (_.isFunction(source)) {
                source = source();
            }
            return _.extend(destination, source);
        },
        define: function(name, source) {
            var namespace;
            namespace = {};
            T5[name] = namespace;
            return T5.extend(namespace, source);
        },
        initializers: {},
        extendInitializers: function(source) {
            return T5.extend(T5.initializers, source);
        }
    };
    Tapestry = {
        Initializer: T5.initializers
    };
    window.T5 = T5;
    window.Tapestry = Tapestry;
    return null;
});

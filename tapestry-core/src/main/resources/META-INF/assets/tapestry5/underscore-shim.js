// Copyright 2013 The Apache Software Foundation
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

define(function () {
    // Because Underscore can load globally without causing a conflict, we can have the true underscore library
    // just be a part of the core stack. This doesn't work with jQuery because the $ symbol can be a conflict
    // between Prototype and jQuery when both are supported.
    return _.noConflict();
});

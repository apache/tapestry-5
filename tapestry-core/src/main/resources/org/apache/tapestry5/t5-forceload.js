/* Copyright 2012 The Apache Software Foundation
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

/*
 * During the transition stage, the old libraries are loaded somewhat like AMD modules, including
 * a call to define().  That means they are only actually loaded as needed. This module exists to force
 * the loading of the other modules before older (and third party) JavaScript code attempts to make use
 * of methods inside the T5 and Tapestry namespace objects.
 */
define("core/compat/t5-forceload", [
    "core/compat/t5-alerts",
    "core/compat/tree",
    "core/compat/tapestry-messages"],
        // Does nothing, but forces the other define()-ed "modules" to have their dependencies
        // loaded, and to be loaded themselves.
        null);
